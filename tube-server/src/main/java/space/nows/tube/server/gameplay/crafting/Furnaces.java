package space.nows.tube.server.gameplay.crafting;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.type.FurnaceInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import space.nows.tube.server.registry.RegistryTags;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class Furnaces {

    private static final Tag<BinaryTag> ITEMS = Tag.NBT("Items");
    private static final Tag<Short> LIT_TIME = Tag.Short("lit_time_remaining").defaultValue((short) 0);
    private static final Tag<Short> LIT_TOTAL = Tag.Short("lit_total_time").defaultValue((short) 0);
    private static final Tag<Short> COOK_TIME = Tag.Short("cooking_time_spent").defaultValue((short) 0);
    private static final Tag<Short> COOK_TOTAL = Tag.Short("cooking_total_time").defaultValue((short) 0);
    private static final int INPUT = 0;
    private static final int FUEL = 1;
    private static final int OUTPUT = 2;

    private record Kind(String block, String recipeType, String title, int speed) {
    }

    private static final Map<String, Kind> KINDS = Map.of(
            "minecraft:furnace", new Kind("minecraft:furnace", "minecraft:smelting", "container.furnace", 1),
            "minecraft:blast_furnace", new Kind("minecraft:blast_furnace", "minecraft:blasting", "container.blast_furnace", 2),
            "minecraft:smoker", new Kind("minecraft:smoker", "minecraft:smoking", "container.smoker", 2));

    private record Position(Instance instance, BlockVec position) {
    }

    private static final class State {
        final Kind kind;
        final FurnaceInventory inventory;
        int litTime;
        int litTotal;
        int cookTime;
        int cookTotal;
        float storedExperience;
        boolean dirty;
        int sinceSave;

        State(Kind kind, Block block) {
            this.kind = kind;
            this.inventory = new FurnaceInventory(Component.translatable(kind.title()));
            if (block.getTag(ITEMS) instanceof ListBinaryTag list) {
                for (BinaryTag element : list) {
                    if (element instanceof CompoundBinaryTag compound) {
                        int slot = compound.getByte("Slot");
                        if (slot >= 0 && slot < 3) {
                            inventory.setItemStack(slot, ItemStack.fromItemNBT(compound.remove("Slot")));
                        }
                    }
                }
            }
            litTime = block.getTag(LIT_TIME);
            litTotal = block.getTag(LIT_TOTAL);
            cookTime = block.getTag(COOK_TIME);
            cookTotal = block.getTag(COOK_TOTAL);
        }
    }

    private final Supplier<RecipeBook> recipes;
    private final Map<Position, State> states = new ConcurrentHashMap<>();
    private final Map<FurnaceInventory, Position> owners = new ConcurrentHashMap<>();
    private final Handler handler = new Handler();

    public Furnaces(Supplier<RecipeBook> recipes) {
        this.recipes = recipes;
    }

    private final class Handler implements BlockHandler {

        @Override
        public Key getKey() {
            return Key.key("minecraft:furnace");
        }

        @Override
        public boolean isTickable() {
            return true;
        }

        @Override
        public void tick(Tick tick) {
            Kind kind = KINDS.get(tick.getBlock().key().asString());
            if (kind != null) {
                Furnaces.this.tick(tick.getInstance(), tick.getBlockPosition().asBlockVec(), tick.getBlock(), kind);
            }
        }
    }

    public void install(GlobalEventHandler events) {
        for (String block : KINDS.keySet()) {
            MinecraftServer.getBlockManager().registerHandler(block, () -> handler);
        }
        events.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (KINDS.containsKey(event.getBlock().key().asString())) {
                event.setBlock(event.getBlock().withHandler(handler));
            }
        });
        events.addListener(PlayerBlockInteractEvent.class, event -> {
            Kind kind = KINDS.get(event.getBlock().key().asString());
            if (event.isCancelled() || kind == null || event.getPlayer().isSneaking() && !event.getPlayer().getItemInMainHand().isAir()) {
                return;
            }
            event.setBlockingItemUse(true);
            Block block = event.getBlock();
            if (block.handler() == null) {
                block = block.withHandler(handler);
                event.getInstance().setBlock(event.getBlockPosition(), block);
            }
            State state = state(event.getInstance(), event.getBlockPosition(), block, kind);
            event.getPlayer().openInventory(state.inventory);
        });
        events.addListener(InventoryPreClickEvent.class, this::click);
        events.addListener(InventoryCloseEvent.class, event -> {
            if (event.getInventory() instanceof FurnaceInventory inventory) {
                Position position = owners.get(inventory);
                State state = position == null ? null : states.get(position);
                if (state != null) {
                    save(position, state);
                }
            }
        });
        events.addListener(PlayerBlockBreakEvent.class, event -> {
            Kind kind = KINDS.get(event.getBlock().key().asString());
            if (event.isCancelled() || kind == null) {
                return;
            }
            Position position = new Position(event.getInstance(), event.getBlockPosition());
            State state = states.remove(position);
            if (state == null) {
                state = new State(kind, event.getBlock());
            } else {
                owners.remove(state.inventory);
                java.util.List.copyOf(state.inventory.getViewers()).forEach(viewer -> viewer.closeInventory());
            }
            for (ItemStack item : state.inventory.getItemStacks()) {
                dropItem(event.getInstance(), event.getBlockPosition().add(0.5, 0.5, 0.5), item);
            }
            spawnExperience(event.getInstance(), event.getBlockPosition().add(0.5, 0.5, 0.5), state);
        });
    }

    private static void dropItem(Instance instance, Point position, ItemStack item) {
        if (item.isAir()) {
            return;
        }
        net.minestom.server.entity.ItemEntity entity = new net.minestom.server.entity.ItemEntity(item);
        entity.setPickupDelay(java.time.Duration.ofMillis(500));
        entity.setInstance(instance, position);
    }

    private State state(Instance instance, BlockVec position, Block block, Kind kind) {
        Position key = new Position(instance, position);
        return states.computeIfAbsent(key, ignored -> {
            State created = new State(kind, block);
            owners.put(created.inventory, key);
            return created;
        });
    }

    private void tick(Instance instance, BlockVec position, Block block, Kind kind) {
        Position key = new Position(instance, position);
        State state = states.get(key);
        if (state == null) {
            if (block.getTag(LIT_TIME) <= 0 && !(block.getTag(ITEMS) instanceof ListBinaryTag)) {
                return;
            }
            state = state(instance, position, block, kind);
        }
        boolean wasLit = state.litTime > 0;
        if (state.litTime > 0) {
            state.litTime--;
        }
        FurnaceInventory inventory = state.inventory;
        ItemStack input = inventory.getItemStack(INPUT);
        ItemStack fuel = inventory.getItemStack(FUEL);
        ItemStack output = inventory.getItemStack(OUTPUT);
        RecipeBook.Cooking recipe = recipes.get().cooking(kind.recipeType(), input).orElse(null);
        boolean canCook = recipe != null && (output.isAir()
                || output.isSimilar(recipe.result()) && output.amount() + recipe.result().amount() <= output.maxStackSize());
        if (state.litTime == 0 && canCook) {
            int burn = fuelTime(fuel.material()) / kind.speed();
            if (burn > 0) {
                state.litTime = burn;
                state.litTotal = burn;
                Material remainder = fuel.material() == Material.LAVA_BUCKET ? Material.BUCKET : null;
                inventory.setItemStack(FUEL, remainder != null && fuel.amount() == 1 ? ItemStack.of(remainder) : fuel.consume(1));
                state.dirty = true;
            }
        }
        if (state.litTime > 0 && canCook) {
            state.cookTotal = recipe.time() / kind.speed();
            state.cookTime++;
            if (state.cookTime >= state.cookTotal) {
                state.cookTime = 0;
                inventory.setItemStack(OUTPUT, output.isAir() ? recipe.result() : output.withAmount(output.amount() + recipe.result().amount()));
                inventory.setItemStack(INPUT, input.consume(1));
                state.storedExperience += recipe.experience();
                state.dirty = true;
            }
        } else if (state.litTime == 0 && state.cookTime > 0) {
            state.cookTime = Math.max(0, state.cookTime - 2);
        } else if (!canCook) {
            state.cookTime = 0;
        }
        inventory.setRemainingFuelTick((short) state.litTime);
        inventory.setMaximumFuelBurnTime((short) Math.max(1, state.litTotal));
        inventory.setProgressArrow((short) state.cookTime);
        inventory.setMaximumProgress((short) Math.max(1, state.cookTotal));
        boolean lit = state.litTime > 0;
        if (wasLit != lit) {
            state.dirty = true;
        }
        if (state.dirty && ++state.sinceSave >= 20 || wasLit != lit) {
            save(key, state);
        }
    }

    private void save(Position key, State state) {
        Block current = key.instance().getBlock(key.position());
        if (!KINDS.containsKey(current.key().asString())) {
            return;
        }
        ListBinaryTag.Builder<CompoundBinaryTag> items = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        ItemStack[] stacks = state.inventory.getItemStacks();
        for (int slot = 0; slot < stacks.length; slot++) {
            if (!stacks[slot].isAir()) {
                items.add(stacks[slot].toItemNBT().putByte("Slot", (byte) slot));
            }
        }
        Block updated = current.withTag(ITEMS, items.build())
                .withTag(LIT_TIME, (short) state.litTime)
                .withTag(LIT_TOTAL, (short) state.litTotal)
                .withTag(COOK_TIME, (short) state.cookTime)
                .withTag(COOK_TOTAL, (short) state.cookTotal)
                .withProperty("lit", Boolean.toString(state.litTime > 0));
        if (updated.handler() == null) {
            updated = updated.withHandler(handler);
        }
        key.instance().setBlock(key.position(), updated, false);
        state.dirty = false;
        state.sinceSave = 0;
    }

    private void click(InventoryPreClickEvent event) {
        if (!(event.getInventory() instanceof FurnaceInventory inventory) && !(event.getPlayer().getOpenInventory() instanceof FurnaceInventory)) {
            return;
        }
        FurnaceInventory furnace = event.getInventory() instanceof FurnaceInventory own ? own : (FurnaceInventory) event.getPlayer().getOpenInventory();
        Position position = owners.get(furnace);
        State state = position == null ? null : states.get(position);
        if (state == null) {
            return;
        }
        Click click = event.getClick();
        boolean inFurnace = event.getInventory() == furnace;
        ItemStack cursor = event.getPlayer().getInventory().getCursorItem();
        if (inFurnace && click.slot() == OUTPUT) {
            if ((click instanceof Click.Left || click instanceof Click.Right) && !cursor.isAir() && !cursor.isSimilar(furnace.getItemStack(OUTPUT))) {
                event.setCancelled(true);
                return;
            }
            if (!furnace.getItemStack(OUTPUT).isAir()) {
                spawnExperience(event.getPlayer().getInstance(), event.getPlayer().getPosition(), state);
            }
            return;
        }
        if (click instanceof Click.Drag drag && drag.slots().contains(OUTPUT)) {
            event.setCancelled(true);
            return;
        }
        if (!inFurnace && (click instanceof Click.LeftShift || click instanceof Click.RightShift)) {
            event.setCancelled(true);
            var playerInventory = event.getPlayer().getInventory();
            ItemStack item = playerInventory.getItemStack(click.slot());
            int target = recipes.get().cooking(state.kind.recipeType(), item).isPresent() ? INPUT : fuelTime(item.material()) > 0 ? FUEL : -1;
            if (target < 0 || item.isAir()) {
                return;
            }
            ItemStack existing = furnace.getItemStack(target);
            if (existing.isAir()) {
                furnace.setItemStack(target, item);
                playerInventory.setItemStack(click.slot(), ItemStack.AIR);
            } else if (existing.isSimilar(item)) {
                int moved = Math.min(item.amount(), existing.maxStackSize() - existing.amount());
                furnace.setItemStack(target, existing.withAmount(existing.amount() + moved));
                playerInventory.setItemStack(click.slot(), item.consume(moved));
            }
        }
    }

    private static void spawnExperience(Instance instance, Point position, State state) {
        int whole = (int) state.storedExperience;
        float rest = state.storedExperience - whole;
        if (ThreadLocalRandom.current().nextFloat() < rest) {
            whole++;
        }
        state.storedExperience = 0;
        if (whole > 0 && instance != null) {
            new ExperienceOrb((short) whole).setInstance(instance, position);
        }
    }

    public static int fuelTime(Material material) {
        String key = material.key().value();
        switch (key) {
            case "lava_bucket": return 20000;
            case "coal_block": return 16000;
            case "dried_kelp_block": return 4001;
            case "blaze_rod": return 2400;
            case "coal", "charcoal": return 1600;
            case "crafting_table", "bookshelf", "chest", "trapped_chest", "ladder", "bow", "crossbow", "fishing_rod",
                 "jukebox", "note_block", "barrel", "cartography_table", "fletching_table", "smithing_table", "loom",
                 "lectern", "composter", "daylight_detector", "chiseled_bookshelf": return 300;
            case "stick", "bowl", "dead_bush", "azalea": return 100;
            case "bamboo", "scaffolding": return 50;
            default: break;
        }
        if (has("#minecraft:logs_that_burn", material) || has("#minecraft:planks", material) || has("#minecraft:wooden_stairs", material)
                || has("#minecraft:wooden_fences", material) || has("#minecraft:fence_gates", material) || has("#minecraft:wooden_trapdoors", material)
                || has("#minecraft:wooden_pressure_plates", material)) {
            return 300;
        }
        if (has("#minecraft:boats", material) || has("#minecraft:chest_boats", material)) {
            return 1200;
        }
        if (has("#minecraft:wooden_doors", material) || has("#minecraft:signs", material) || key.startsWith("wooden_")) {
            return 200;
        }
        if (has("#minecraft:wooden_slabs", material) || has("#minecraft:wooden_buttons", material)) {
            return 150;
        }
        if (has("#minecraft:saplings", material) || has("#minecraft:wool", material)) {
            return 100;
        }
        if (has("#minecraft:wool_carpets", material)) {
            return 67;
        }
        return 0;
    }

    private static boolean has(String tag, Material material) {
        return RegistryTags.has(material, tag);
    }
}

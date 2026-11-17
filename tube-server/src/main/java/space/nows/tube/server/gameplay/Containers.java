package space.nows.tube.server.gameplay;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class Containers {

    private static final Tag<BinaryTag> ITEMS = Tag.NBT("Items");
    private static final int CHEST_SIZE = 27;

    record Kind(InventoryType type, String title) {
    }

    private record Key(Instance instance, BlockVec first, BlockVec second) {
    }

    private static final Map<Key, Inventory> OPEN = new ConcurrentHashMap<>();
    private static final Map<Inventory, Key> OWNERS = new ConcurrentHashMap<>();

    private Containers() {
    }

    static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.isCancelled() || event.getPlayer().isSneaking() && !event.getPlayer().getItemInMainHand().isAir()) {
                return;
            }
            Kind kind = kind(event.getBlock());
            if (kind == null) {
                return;
            }
            event.setBlockingItemUse(true);
            Key key = key(event.getInstance(), event.getBlockPosition(), event.getBlock());
            boolean large = key.second() != null;
            Inventory inventory = OPEN.computeIfAbsent(key, ignored -> {
                Inventory created = new Inventory(large ? InventoryType.CHEST_6_ROW : kind.type(),
                        Component.translatable(large ? "container.chestDouble" : kind.title()));
                read(event.getInstance().getBlock(key.first()), created, 0);
                if (large) {
                    read(event.getInstance().getBlock(key.second()), created, CHEST_SIZE);
                }
                OWNERS.put(created, key);
                return created;
            });
            event.getPlayer().openInventory(inventory);
        });
        handler.addListener(InventoryCloseEvent.class, event -> {
            if (!(event.getInventory() instanceof Inventory inventory)) {
                return;
            }
            Key key = OWNERS.get(inventory);
            if (key == null) {
                return;
            }
            write(key, inventory);
            if (inventory.getViewers().stream().allMatch(viewer -> viewer == event.getPlayer())) {
                OPEN.remove(key);
                OWNERS.remove(inventory);
            }
        });
        handler.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (event.isCancelled() || !event.getBlock().key().value().endsWith("shulker_box")) {
                return;
            }
            List<ItemStack> contents = event.getPlayer().getItemInHand(event.getHand()).get(DataComponents.CONTAINER);
            if (contents != null && !contents.isEmpty()) {
                event.setBlock(event.getBlock().withTag(ITEMS, items(contents.toArray(ItemStack[]::new))));
            }
        });
        handler.addListener(PlayerBlockBreakEvent.class, event -> {
            Block block = event.getBlock();
            if (event.isCancelled() || kind(block) == null) {
                return;
            }
            Instance instance = event.getInstance();
            BlockVec position = event.getBlockPosition();
            for (var entry : List.copyOf(OPEN.entrySet())) {
                Key key = entry.getKey();
                if (key.instance() == instance && (position.equals(key.first()) || position.equals(key.second()))) {
                    write(key, entry.getValue());
                    OPEN.remove(key);
                    OWNERS.remove(entry.getValue());
                    List.copyOf(entry.getValue().getViewers()).forEach(viewer -> viewer.closeInventory());
                }
            }
            Inventory scratch = new Inventory(InventoryType.CHEST_3_ROW, "");
            read(instance.getBlock(position), scratch, 0);
            List<ItemStack> contents = new ArrayList<>();
            for (ItemStack item : scratch.getItemStacks()) {
                if (!item.isAir()) {
                    contents.add(item);
                }
            }
            if (block.key().value().endsWith("shulker_box")) {
                if (event.getPlayer().getGameMode() != GameMode.CREATIVE || !contents.isEmpty()) {
                    ItemStack box = ItemStack.of(block.registry().material());
                    Drops.spawn(instance, position.add(0.5, 0.5, 0.5), contents.isEmpty() ? box : box.with(DataComponents.CONTAINER, contents));
                }
                return;
            }
            for (ItemStack item : contents) {
                Drops.spawn(instance, position.add(0.5, 0.5, 0.5), item);
            }
        });
    }

    private static Key key(Instance instance, BlockVec position, Block block) {
        String type = block.getProperty("type");
        if (block.properties().containsKey("facing") && ("left".equals(type) || "right".equals(type))) {
            int[] step = clockwise(block.getProperty("facing"));
            BlockVec partner = "left".equals(type) ? position.add(step[0], 0, step[1]) : position.add(-step[0], 0, -step[1]);
            Block other = instance.getBlock(partner);
            if (other.compare(block)) {
                return "right".equals(type) ? new Key(instance, position, partner) : new Key(instance, partner, position);
            }
        }
        return new Key(instance, position, null);
    }

    private static int[] clockwise(String facing) {
        return switch (facing) {
            case "north" -> new int[]{1, 0};
            case "east" -> new int[]{0, 1};
            case "south" -> new int[]{-1, 0};
            default -> new int[]{0, -1};
        };
    }

    static Kind kind(Block block) {
        String key = block.key().asString();
        return switch (key) {
            case "minecraft:chest", "minecraft:trapped_chest" -> new Kind(InventoryType.CHEST_3_ROW, "container.chest");
            case "minecraft:barrel" -> new Kind(InventoryType.CHEST_3_ROW, "container.barrel");
            case "minecraft:dispenser" -> new Kind(InventoryType.WINDOW_3X3, "container.dispenser");
            case "minecraft:dropper" -> new Kind(InventoryType.WINDOW_3X3, "container.dropper");
            case "minecraft:hopper" -> new Kind(InventoryType.HOPPER, "container.hopper");
            default -> key.endsWith("shulker_box") ? new Kind(InventoryType.SHULKER_BOX, "container.shulkerBox") : null;
        };
    }

    static void read(Block block, Inventory inventory) {
        read(block, inventory, 0);
    }

    private static void read(Block block, Inventory inventory, int offset) {
        if (!(block.getTag(ITEMS) instanceof ListBinaryTag list)) {
            return;
        }
        for (BinaryTag element : list) {
            if (element instanceof CompoundBinaryTag compound) {
                int slot = (compound.getByte("Slot") & 0xFF) + offset;
                if (slot < inventory.getSize()) {
                    inventory.setItemStack(slot, ItemStack.fromItemNBT(compound.remove("Slot")));
                }
            }
        }
    }

    static ListBinaryTag items(Inventory inventory) {
        return items(inventory.getItemStacks());
    }

    private static ListBinaryTag items(ItemStack[] stacks) {
        ListBinaryTag.Builder<CompoundBinaryTag> list = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        for (int slot = 0; slot < stacks.length; slot++) {
            if (!stacks[slot].isAir()) {
                list.add(stacks[slot].toItemNBT().putByte("Slot", (byte) slot));
            }
        }
        return list.build();
    }

    private static void write(Key key, Inventory inventory) {
        ItemStack[] stacks = inventory.getItemStacks();
        if (key.second() == null) {
            store(key.instance(), key.first(), stacks);
            return;
        }
        store(key.instance(), key.first(), java.util.Arrays.copyOfRange(stacks, 0, CHEST_SIZE));
        store(key.instance(), key.second(), java.util.Arrays.copyOfRange(stacks, CHEST_SIZE, CHEST_SIZE * 2));
    }

    private static void store(Instance instance, BlockVec position, ItemStack[] stacks) {
        Block current = instance.getBlock(position);
        if (kind(current) != null) {
            instance.setBlock(position, current.withTag(ITEMS, items(stacks)), false);
        }
    }
}

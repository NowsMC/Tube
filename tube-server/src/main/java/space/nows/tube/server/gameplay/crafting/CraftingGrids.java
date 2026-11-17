package space.nows.tube.server.gameplay.crafting;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryItemChangeEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public final class CraftingGrids {

    private static final Tag<Boolean> TABLE = Tag.Transient("tube:crafting_table");

    private final Supplier<RecipeBook> recipes;
    private final BiPredicate<Player, ItemStack> allowCraft;

    public CraftingGrids(Supplier<RecipeBook> recipes) {
        this(recipes, (player, result) -> true);
    }

    public CraftingGrids(Supplier<RecipeBook> recipes, BiPredicate<Player, ItemStack> allowCraft) {
        this.recipes = recipes;
        this.allowCraft = allowCraft;
    }

    private record Grid(AbstractInventory inventory, int resultSlot, int firstSlot, int size) {

        static Grid of(AbstractInventory inventory) {
            if (inventory instanceof PlayerInventory) {
                return new Grid(inventory, PlayerInventoryUtils.CRAFT_RESULT, PlayerInventoryUtils.CRAFT_SLOT_1, 2);
            }
            if (inventory instanceof Inventory table && table.getInventoryType() == InventoryType.CRAFTING && Boolean.TRUE.equals(table.getTag(TABLE))) {
                return new Grid(inventory, 0, 1, 3);
            }
            return null;
        }

        boolean isGridSlot(int slot) {
            return slot >= firstSlot && slot < firstSlot + size * size;
        }

        ItemStack[] items() {
            ItemStack[] items = new ItemStack[size * size];
            for (int i = 0; i < items.length; i++) {
                items[i] = inventory.getItemStack(firstSlot + i);
            }
            return items;
        }
    }

    public void install(GlobalEventHandler handler) {
        handler.addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.isCancelled() || !event.getBlock().compare(net.minestom.server.instance.block.Block.CRAFTING_TABLE)
                    || event.getPlayer().isSneaking() && !event.getPlayer().getItemInMainHand().isAir()) {
                return;
            }
            event.setBlockingItemUse(true);
            Inventory table = new Inventory(InventoryType.CRAFTING, Component.translatable("container.crafting"));
            table.setTag(TABLE, true);
            event.getPlayer().openInventory(table);
        });
        handler.addListener(InventoryItemChangeEvent.class, event -> {
            Grid grid = Grid.of(event.getInventory());
            if (grid != null && grid.isGridSlot(event.getSlot())) {
                refresh(grid);
            }
        });
        handler.addListener(InventoryPreClickEvent.class, this::click);
        handler.addListener(InventoryCloseEvent.class, event -> {
            Grid grid = Grid.of(event.getInventory());
            if (grid != null) {
                returnItems(event.getPlayer(), grid);
            }
        });
        handler.addListener(PlayerPacketEvent.class, event -> {
            if (event.getPacket() instanceof ClientCloseWindowPacket close && close.windowId() == 0) {
                returnItems(event.getPlayer(), Grid.of(event.getPlayer().getInventory()));
            }
        });
    }

    public static void returnPlayerGrid(Player player) {
        returnItems(player, Grid.of(player.getInventory()));
    }

    private void refresh(Grid grid) {
        ItemStack result = recipes.get().craft(grid.items(), grid.size());
        if (!grid.inventory().getItemStack(grid.resultSlot()).equals(result)) {
            grid.inventory().setItemStack(grid.resultSlot(), result);
        }
    }

    private void click(InventoryPreClickEvent event) {
        Grid grid = Grid.of(event.getInventory());
        Player player = event.getPlayer();
        if (grid == null) {
            return;
        }
        Click click = event.getClick();
        if (click instanceof Click.Double) {
            grid.inventory().setItemStack(grid.resultSlot(), ItemStack.AIR);
            player.scheduleNextTick(ignored -> refresh(grid));
            return;
        }
        if (click instanceof Click.Drag drag && drag.slots().contains(grid.resultSlot())) {
            event.setCancelled(true);
            return;
        }
        if (click.slot() != grid.resultSlot()) {
            return;
        }
        event.setCancelled(true);
        refresh(grid);
        ItemStack offered = grid.inventory().getItemStack(grid.resultSlot());
        if (offered.isAir() || !allowCraft.test(player, offered)) {
            return;
        }
        switch (click) {
            case Click.Left ignored -> takeToCursor(player, grid);
            case Click.Right ignored -> takeToCursor(player, grid);
            case Click.LeftShift ignored -> takeAll(player, grid);
            case Click.RightShift ignored -> takeAll(player, grid);
            case Click.HotbarSwap swap -> {
                ItemStack result = grid.inventory().getItemStack(grid.resultSlot());
                if (!result.isAir() && player.getInventory().getItemStack(swap.hotbarSlot()).isAir()) {
                    player.getInventory().setItemStack(swap.hotbarSlot(), result);
                    consume(player, grid);
                }
            }
            case Click.DropSlot ignored -> {
                ItemStack result = grid.inventory().getItemStack(grid.resultSlot());
                if (!result.isAir()) {
                    player.dropItem(result);
                    consume(player, grid);
                }
            }
            default -> {
            }
        }
        refresh(grid);
    }

    private void takeToCursor(Player player, Grid grid) {
        ItemStack result = grid.inventory().getItemStack(grid.resultSlot());
        if (result.isAir()) {
            return;
        }
        ItemStack cursor = player.getInventory().getCursorItem();
        if (cursor.isAir()) {
            player.getInventory().setCursorItem(result);
        } else if (cursor.isSimilar(result) && cursor.amount() + result.amount() <= cursor.maxStackSize()) {
            player.getInventory().setCursorItem(cursor.withAmount(cursor.amount() + result.amount()));
        } else {
            return;
        }
        consume(player, grid);
    }

    private void takeAll(Player player, Grid grid) {
        for (int crafted = 0; crafted < 64; crafted++) {
            ItemStack result = recipes.get().craft(grid.items(), grid.size());
            if (result.isAir() || !player.getInventory().addItemStack(result)) {
                return;
            }
            consume(player, grid);
        }
    }

    private static void consume(Player player, Grid grid) {
        for (int i = 0; i < grid.size() * grid.size(); i++) {
            int slot = grid.firstSlot() + i;
            ItemStack item = grid.inventory().getItemStack(slot);
            if (item.isAir()) {
                continue;
            }
            Material remainder = remainder(item.material());
            if (remainder != null && item.amount() == 1) {
                grid.inventory().setItemStack(slot, ItemStack.of(remainder));
                continue;
            }
            grid.inventory().setItemStack(slot, item.consume(1));
            if (remainder != null && !player.getInventory().addItemStack(ItemStack.of(remainder))) {
                player.dropItem(ItemStack.of(remainder));
            }
        }
    }

    static Material remainder(Material material) {
        String key = material.key().value();
        if (key.endsWith("_bucket") && !key.equals("bucket")) {
            return Material.BUCKET;
        }
        if (key.equals("honey_bottle") || key.equals("dragon_breath")) {
            return Material.GLASS_BOTTLE;
        }
        return null;
    }

    private static void returnItems(Player player, Grid grid) {
        if (grid == null) {
            return;
        }
        for (int i = 0; i < grid.size() * grid.size(); i++) {
            int slot = grid.firstSlot() + i;
            ItemStack item = grid.inventory().getItemStack(slot);
            if (item.isAir()) {
                continue;
            }
            grid.inventory().setItemStack(slot, ItemStack.AIR);
            if (!player.getInventory().addItemStack(item)) {
                player.dropItem(item);
            }
        }
        grid.inventory().setItemStack(grid.resultSlot(), ItemStack.AIR);
    }
}

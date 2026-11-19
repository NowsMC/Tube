package space.nows.tube.bukkit.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public final class BukkitInventoryView implements InventoryView {

    private final space.nows.tube.api.entity.player.Player tube;
    private final HumanEntity player;
    private final Inventory top;
    private final Inventory bottom;
    private final InventoryType type;
    private final String originalTitle;
    private String title;
    private ItemStack cursor;

    public BukkitInventoryView(space.nows.tube.api.entity.player.Player tube, HumanEntity player, Inventory top, Inventory bottom, InventoryType type, String title) {
        this.tube = tube;
        this.player = player;
        this.top = top;
        this.bottom = bottom;
        this.type = type;
        this.originalTitle = title;
        this.title = title;
    }

    @Override
    public Inventory getTopInventory() {
        return top;
    }

    @Override
    public Inventory getBottomInventory() {
        return bottom;
    }

    @Override
    public HumanEntity getPlayer() {
        return player;
    }

    @Override
    public InventoryType getType() {
        return type;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getOriginalTitle() {
        return originalTitle;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public ItemStack getCursor() {
        return cursor;
    }

    @Override
    public void setCursor(ItemStack item) {
        this.cursor = item;
    }

    @Override
    public void close() {
        tube.closeInventory();
    }

    @Override
    public int countSlots() {
        return top == bottom ? top.getSize() : InventoryView.super.countSlots();
    }

    @Override
    public int convertSlot(int rawSlot) {
        return top == bottom ? rawSlot : InventoryView.super.convertSlot(rawSlot);
    }
}

package org.bukkit.event.inventory;

import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class InventoryClickEvent extends InventoryInteractEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final ClickType click;
    private final InventoryAction action;
    private final InventoryType.SlotType slotType;
    private final int whichSlot;
    private final int rawSlot;
    private final int hotbarKey;
    private ItemStack current;

    public InventoryClickEvent(InventoryView view, InventoryType.SlotType type, int slot, ClickType click, InventoryAction action) {
        this(view, type, slot, click, action, -1);
    }

    public InventoryClickEvent(InventoryView view, InventoryType.SlotType type, int slot, ClickType click, InventoryAction action, int key) {
        super(view);
        this.slotType = type;
        this.rawSlot = slot;
        this.whichSlot = view.convertSlot(slot);
        this.click = click;
        this.action = action;
        this.hotbarKey = key;
    }

    public InventoryType.SlotType getSlotType() {
        return slotType;
    }

    public ItemStack getCursor() {
        return getView().getCursor();
    }

    public ItemStack getCurrentItem() {
        if (slotType == InventoryType.SlotType.OUTSIDE) {
            return current;
        }
        return getView().getItem(rawSlot);
    }

    public void setCurrentItem(ItemStack item) {
        if (slotType == InventoryType.SlotType.OUTSIDE) {
            current = item;
        } else {
            getView().setItem(rawSlot, item);
        }
    }

    public Inventory getClickedInventory() {
        return getView().getInventory(rawSlot);
    }

    public boolean isRightClick() {
        return click.isRightClick();
    }

    public boolean isLeftClick() {
        return click.isLeftClick();
    }

    public boolean isShiftClick() {
        return click.isShiftClick();
    }

    public int getSlot() {
        return whichSlot;
    }

    public int getRawSlot() {
        return rawSlot;
    }

    public int getHotbarButton() {
        return hotbarKey;
    }

    public InventoryAction getAction() {
        return action;
    }

    public ClickType getClick() {
        return click;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

package org.bukkit.event.inventory;

import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class InventoryDragEvent extends InventoryInteractEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DragType type;
    private final Map<Integer, ItemStack> addedItems;
    private final Set<Integer> containerSlots;
    private final ItemStack oldCursor;
    private ItemStack newCursor;

    public InventoryDragEvent(InventoryView what, ItemStack newCursor, ItemStack oldCursor, boolean right, Map<Integer, ItemStack> slots) {
        super(what);
        this.type = right ? DragType.SINGLE : DragType.EVEN;
        this.newCursor = newCursor;
        this.oldCursor = oldCursor;
        this.addedItems = slots;
        Set<Integer> converted = new LinkedHashSet<>();
        for (int slot : slots.keySet()) {
            converted.add(what.convertSlot(slot));
        }
        this.containerSlots = Collections.unmodifiableSet(converted);
    }

    public Map<Integer, ItemStack> getNewItems() {
        return Collections.unmodifiableMap(addedItems);
    }

    public Set<Integer> getRawSlots() {
        return addedItems.keySet();
    }

    public Set<Integer> getInventorySlots() {
        return containerSlots;
    }

    public ItemStack getCursor() {
        return newCursor;
    }

    public void setCursor(ItemStack newCursor) {
        this.newCursor = newCursor;
    }

    public ItemStack getOldCursor() {
        return oldCursor == null ? null : oldCursor.clone();
    }

    public DragType getType() {
        return type;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

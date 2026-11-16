package org.bukkit.event.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;

public class InventoryCloseEvent extends InventoryEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public InventoryCloseEvent(InventoryView transaction) {
        super(transaction);
    }

    public final HumanEntity getPlayer() {
        return transaction.getPlayer();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

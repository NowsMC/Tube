package org.bukkit.event.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

public class InventoryCloseEvent extends InventoryEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final HumanEntity player;

    public InventoryCloseEvent(HumanEntity player, Inventory inventory) {
        super(inventory);
        this.player = player;
    }

    public HumanEntity getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

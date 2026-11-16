package org.bukkit.event.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.List;

public abstract class InventoryEvent extends Event {

    protected InventoryView transaction;

    public InventoryEvent(InventoryView transaction) {
        this.transaction = transaction;
    }

    public Inventory getInventory() {
        return transaction.getTopInventory();
    }

    public List<HumanEntity> getViewers() {
        return List.of(transaction.getPlayer());
    }

    public InventoryView getView() {
        return transaction;
    }
}

package org.bukkit.event.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.InventoryView;

public abstract class InventoryInteractEvent extends InventoryEvent implements Cancellable {

    private Result result = Result.DEFAULT;

    public InventoryInteractEvent(InventoryView transaction) {
        super(transaction);
    }

    public HumanEntity getWhoClicked() {
        return getView().getPlayer();
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Result getResult() {
        return result;
    }

    @Override
    public boolean isCancelled() {
        return result == Result.DENY;
    }

    @Override
    public void setCancelled(boolean cancel) {
        setResult(cancel ? Result.DENY : Result.DEFAULT);
    }
}

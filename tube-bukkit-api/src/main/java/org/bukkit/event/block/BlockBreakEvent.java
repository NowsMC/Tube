package org.bukkit.event.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class BlockBreakEvent extends BlockEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private final Player player;
    private boolean dropItems = true;
    private int exp;

    public BlockBreakEvent(Block block, Player player) {
        super(block);
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isDropItems() {
        return dropItems;
    }

    public void setDropItems(boolean dropItems) {
        this.dropItems = dropItems;
    }

    public int getExpToDrop() {
        return exp;
    }

    public void setExpToDrop(int exp) {
        this.exp = exp;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}

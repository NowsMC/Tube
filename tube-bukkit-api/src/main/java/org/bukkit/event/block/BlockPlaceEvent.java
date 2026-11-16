package org.bukkit.event.block;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceEvent extends BlockEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled;
    private final Player player;
    private final ItemStack itemInHand;
    private boolean canBuild = true;

    public BlockPlaceEvent(Block placed, ItemStack itemInHand, Player player) {
        super(placed);
        this.itemInHand = itemInHand;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public Block getBlockPlaced() {
        return getBlock();
    }

    public ItemStack getItemInHand() {
        return itemInHand;
    }

    public boolean canBuild() {
        return canBuild;
    }

    public void setBuild(boolean canBuild) {
        this.canBuild = canBuild;
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

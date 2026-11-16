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
    private final org.bukkit.block.BlockState replacedBlockState;
    private final Block placedAgainst;
    private final org.bukkit.inventory.EquipmentSlot hand;

    public BlockPlaceEvent(Block placed, ItemStack itemInHand, Player player) {
        this(placed, null, null, itemInHand, player, true, org.bukkit.inventory.EquipmentSlot.HAND);
    }

    public BlockPlaceEvent(Block placed, org.bukkit.block.BlockState replacedBlockState, Block placedAgainst, ItemStack itemInHand, Player player, boolean canBuild) {
        this(placed, replacedBlockState, placedAgainst, itemInHand, player, canBuild, org.bukkit.inventory.EquipmentSlot.HAND);
    }

    public BlockPlaceEvent(Block placed, org.bukkit.block.BlockState replacedBlockState, Block placedAgainst, ItemStack itemInHand, Player player, boolean canBuild, org.bukkit.inventory.EquipmentSlot hand) {
        super(placed);
        this.replacedBlockState = replacedBlockState;
        this.placedAgainst = placedAgainst;
        this.itemInHand = itemInHand;
        this.player = player;
        this.canBuild = canBuild;
        this.hand = hand;
    }

    public Block getBlockPlaced() {
        return getBlock();
    }

    public org.bukkit.block.BlockState getBlockReplacedState() {
        return replacedBlockState;
    }

    public Block getBlockAgainst() {
        return placedAgainst;
    }

    public org.bukkit.inventory.EquipmentSlot getHand() {
        return hand;
    }

    public Player getPlayer() {
        return player;
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

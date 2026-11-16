package org.bukkit.event.player;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Action action;
    private final ItemStack item;
    private final Block clickedBlock;
    private final BlockFace blockFace;
    private final EquipmentSlot hand;
    private boolean cancelled;

    public PlayerInteractEvent(Player who, Action action, ItemStack item, Block clickedBlock, BlockFace blockFace, EquipmentSlot hand) {
        super(who);
        this.action = action;
        this.item = item;
        this.clickedBlock = clickedBlock;
        this.blockFace = blockFace;
        this.hand = hand;
    }

    public Action getAction() {
        return action;
    }

    public ItemStack getItem() {
        return item;
    }

    public boolean hasItem() {
        return item != null && !item.isEmpty();
    }

    public Block getClickedBlock() {
        return clickedBlock;
    }

    public boolean hasBlock() {
        return clickedBlock != null;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public EquipmentSlot getHand() {
        return hand;
    }

    public Material getMaterial() {
        return hasItem() ? item.getType() : Material.AIR;
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

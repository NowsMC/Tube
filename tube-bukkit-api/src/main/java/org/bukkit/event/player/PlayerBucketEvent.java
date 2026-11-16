package org.bukkit.event.player;

public abstract class PlayerBucketEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    private org.bukkit.inventory.ItemStack itemStack;
    private boolean cancelled;
    private org.bukkit.block.Block block;
    private org.bukkit.block.Block blockClicked;
    private org.bukkit.block.BlockFace blockFace;
    private org.bukkit.Material bucket;
    private org.bukkit.inventory.EquipmentSlot hand;

    public PlayerBucketEvent(org.bukkit.entity.Player p0, org.bukkit.block.Block p1, org.bukkit.block.BlockFace p2, org.bukkit.Material p3, org.bukkit.inventory.ItemStack p4) {
        this(p0, (org.bukkit.block.Block) null, p1, p2, p3, p4);
    }

    public PlayerBucketEvent(org.bukkit.entity.Player p0, org.bukkit.block.Block p1, org.bukkit.block.Block p2, org.bukkit.block.BlockFace p3, org.bukkit.Material p4, org.bukkit.inventory.ItemStack p5) {
        this(p0, p1, p2, p3, p4, p5, org.bukkit.inventory.EquipmentSlot.HAND);
    }

    public PlayerBucketEvent(org.bukkit.entity.Player p0, org.bukkit.block.Block p1, org.bukkit.block.Block p2, org.bukkit.block.BlockFace p3, org.bukkit.Material p4, org.bukkit.inventory.ItemStack p5, org.bukkit.inventory.EquipmentSlot p6) {
        super(p0);
        this.cancelled = false;
        this.block = p1;
        this.blockClicked = p2;
        this.blockFace = p3;
        this.itemStack = p5;
        this.bucket = p4;
        this.hand = p6;
    }

    public org.bukkit.Material getBucket() {
        return bucket;
    }

    public org.bukkit.inventory.ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(org.bukkit.inventory.ItemStack p0) {
        this.itemStack = p0;
    }

    public org.bukkit.block.Block getBlock() {
        return block;
    }

    public org.bukkit.block.Block getBlockClicked() {
        return blockClicked;
    }

    public org.bukkit.block.BlockFace getBlockFace() {
        return blockFace;
    }

    public org.bukkit.inventory.EquipmentSlot getHand() {
        return hand;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean p0) {
        this.cancelled = p0;
    }
}

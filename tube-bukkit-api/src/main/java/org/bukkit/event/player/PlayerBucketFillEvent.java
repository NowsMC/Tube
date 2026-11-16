package org.bukkit.event.player;

public class PlayerBucketFillEvent extends org.bukkit.event.player.PlayerBucketEvent {
    private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();

    public PlayerBucketFillEvent(org.bukkit.entity.Player p0, org.bukkit.block.Block p1, org.bukkit.block.BlockFace p2, org.bukkit.Material p3, org.bukkit.inventory.ItemStack p4) {
        super(p0, p1, p2, p3, p4);
    }

    public PlayerBucketFillEvent(org.bukkit.entity.Player p0, org.bukkit.block.Block p1, org.bukkit.block.Block p2, org.bukkit.block.BlockFace p3, org.bukkit.Material p4, org.bukkit.inventory.ItemStack p5) {
        super(p0, p1, p2, p3, p4, p5);
    }

    public PlayerBucketFillEvent(org.bukkit.entity.Player p0, org.bukkit.block.Block p1, org.bukkit.block.Block p2, org.bukkit.block.BlockFace p3, org.bukkit.Material p4, org.bukkit.inventory.ItemStack p5, org.bukkit.inventory.EquipmentSlot p6) {
        super(p0, p1, p2, p3, p4, p5, p6);
    }

    @Override
    public org.bukkit.event.HandlerList getHandlers() {
        return HANDLERS;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return HANDLERS;
    }
}

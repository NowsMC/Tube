package org.bukkit.event.block;

public class BlockDispenseArmorEvent extends org.bukkit.event.block.BlockDispenseEvent {
    private org.bukkit.entity.LivingEntity target;

    public BlockDispenseArmorEvent(org.bukkit.block.Block p0, org.bukkit.inventory.ItemStack p1, org.bukkit.entity.LivingEntity p2) {
        super(p0, p1, new org.bukkit.util.Vector(0, 0, 0));
        this.target = p2;
    }

    public org.bukkit.entity.LivingEntity getTargetEntity() {
        return target;
    }
}

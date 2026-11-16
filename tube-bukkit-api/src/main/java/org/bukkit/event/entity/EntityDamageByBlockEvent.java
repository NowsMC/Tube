package org.bukkit.event.entity;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;

public class EntityDamageByBlockEvent extends EntityDamageEvent {

    private final Block damager;
    private final BlockState damagerState;

    public EntityDamageByBlockEvent(Block damager, Entity damagee, DamageCause cause, double damage) {
        this(damager, damager == null ? null : damager.getState(), damagee, cause, damage);
    }

    public EntityDamageByBlockEvent(Block damager, BlockState damagerState, Entity damagee, DamageCause cause, double damage) {
        super(damagee, cause, damage);
        this.damager = damager;
        this.damagerState = damagerState;
    }

    public Block getDamager() {
        return damager;
    }

    public BlockState getDamagerBlockState() {
        return damagerState;
    }
}

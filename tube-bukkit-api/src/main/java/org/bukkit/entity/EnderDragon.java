package org.bukkit.entity;

public interface EnderDragon extends org.bukkit.entity.ComplexLivingEntity, org.bukkit.entity.Boss, org.bukkit.entity.Mob, org.bukkit.entity.Enemy {

    public enum Phase {
        CIRCLING,
        STRAFING,
        FLY_TO_PORTAL,
        LAND_ON_PORTAL,
        LEAVE_PORTAL,
        BREATH_ATTACK,
        SEARCH_FOR_BREATH_ATTACK_TARGET,
        ROAR_BEFORE_ATTACK,
        CHARGE_PLAYER,
        DYING,
        HOVER
    }
}

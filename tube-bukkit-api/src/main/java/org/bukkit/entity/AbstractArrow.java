package org.bukkit.entity;

public interface AbstractArrow extends org.bukkit.entity.Projectile {

    public enum PickupStatus {
        DISALLOWED,
        ALLOWED,
        CREATIVE_ONLY
    }
}

package org.bukkit.entity;

public interface FishHook extends org.bukkit.entity.Projectile {

    public enum HookState {
        UNHOOKED,
        HOOKED_ENTITY,
        BOBBING
    }
}

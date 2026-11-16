package org.bukkit.entity;

public interface ZombieNautilus extends org.bukkit.entity.AbstractNautilus {

    void setVariant(org.bukkit.entity.ZombieNautilus.Variant p0);

    public interface Variant extends org.bukkit.Keyed, org.bukkit.registry.RegistryAware {
    }
}

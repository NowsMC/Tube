package org.bukkit.entity;

public interface Pig extends org.bukkit.entity.Steerable, org.bukkit.entity.Vehicle {

    void setVariant(org.bukkit.entity.Pig.Variant p0);

    public interface Variant extends org.bukkit.Keyed, org.bukkit.registry.RegistryAware {
    }
}

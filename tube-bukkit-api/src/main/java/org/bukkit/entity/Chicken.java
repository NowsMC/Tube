package org.bukkit.entity;

public interface Chicken extends org.bukkit.entity.Animals {

    void setVariant(org.bukkit.entity.Chicken.Variant p0);

    public interface Variant extends org.bukkit.Keyed, org.bukkit.registry.RegistryAware {
    }
}

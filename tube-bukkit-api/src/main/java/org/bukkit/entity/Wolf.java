package org.bukkit.entity;

public interface Wolf extends org.bukkit.entity.Tameable, org.bukkit.entity.Sittable {

    void setAngry(boolean p0);

    org.bukkit.DyeColor getCollarColor();

    void setCollarColor(org.bukkit.DyeColor p0);

    org.bukkit.entity.Wolf.Variant getVariant();

    void setVariant(org.bukkit.entity.Wolf.Variant p0);

    public interface Variant extends org.bukkit.Keyed, org.bukkit.registry.RegistryAware {
    }
}

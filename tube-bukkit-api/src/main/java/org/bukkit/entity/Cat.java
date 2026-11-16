package org.bukkit.entity;

public interface Cat extends org.bukkit.entity.Tameable, org.bukkit.entity.Sittable {

    org.bukkit.entity.Cat.Type getCatType();

    void setCatType(org.bukkit.entity.Cat.Type p0);

    org.bukkit.DyeColor getCollarColor();

    void setCollarColor(org.bukkit.DyeColor p0);

    public interface Type extends org.bukkit.util.OldEnum<org.bukkit.entity.Cat.Type>, org.bukkit.Keyed, org.bukkit.registry.RegistryAware {
    }
}

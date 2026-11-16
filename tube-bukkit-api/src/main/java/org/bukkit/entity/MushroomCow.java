package org.bukkit.entity;

public interface MushroomCow extends org.bukkit.entity.AbstractCow {

    org.bukkit.entity.MushroomCow.Variant getVariant();

    void setVariant(org.bukkit.entity.MushroomCow.Variant p0);

    public enum Variant {
        RED,
        BROWN
    }
}

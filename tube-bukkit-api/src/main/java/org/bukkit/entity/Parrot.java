package org.bukkit.entity;

public interface Parrot extends org.bukkit.entity.Tameable, org.bukkit.entity.Sittable {

    org.bukkit.entity.Parrot.Variant getVariant();

    void setVariant(org.bukkit.entity.Parrot.Variant p0);

    public enum Variant {
        RED,
        BLUE,
        GREEN,
        CYAN,
        GRAY
    }
}

package org.bukkit.entity;

public interface Salmon extends org.bukkit.entity.Fish {

    void setVariant(org.bukkit.entity.Salmon.Variant p0);

    public enum Variant {
        SMALL,
        MEDIUM,
        LARGE
    }
}

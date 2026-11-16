package org.bukkit.entity;

public interface Ocelot extends org.bukkit.entity.Animals {

    void setCatType(org.bukkit.entity.Ocelot.Type p0);

    public enum Type {
        WILD_OCELOT,
        BLACK_CAT,
        RED_CAT,
        SIAMESE_CAT
    }
}

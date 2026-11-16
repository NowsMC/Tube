package org.bukkit.entity;

public interface Boat extends org.bukkit.entity.Leashable, org.bukkit.entity.Vehicle {

    void setWoodType(org.bukkit.TreeSpecies p0);

    void setBoatType(org.bukkit.entity.Boat.Type p0);

    public enum Status {
        IN_WATER,
        UNDER_WATER,
        UNDER_FLOWING_WATER,
        ON_LAND,
        IN_AIR
    }

    public enum Type {
        OAK,
        SPRUCE,
        BIRCH,
        JUNGLE,
        ACACIA,
        CHERRY,
        DARK_OAK,
        MANGROVE,
        BAMBOO
    }
}

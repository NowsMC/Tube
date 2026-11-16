package org.bukkit.entity;

public interface Fox extends org.bukkit.entity.Animals, org.bukkit.entity.Sittable {

    org.bukkit.entity.Fox.Type getFoxType();

    void setFoxType(org.bukkit.entity.Fox.Type p0);

    public enum Type {
        RED,
        SNOW
    }
}

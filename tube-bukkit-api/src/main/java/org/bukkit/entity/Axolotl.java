package org.bukkit.entity;

public interface Axolotl extends org.bukkit.entity.Animals {

    org.bukkit.entity.Axolotl.Variant getVariant();

    void setVariant(org.bukkit.entity.Axolotl.Variant p0);

    public enum Variant {
        LUCY,
        WILD,
        GOLD,
        CYAN,
        BLUE
    }
}

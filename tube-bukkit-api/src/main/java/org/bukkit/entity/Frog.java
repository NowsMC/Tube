package org.bukkit.entity;

public interface Frog extends org.bukkit.entity.Animals {

    org.bukkit.entity.Frog.Variant getVariant();

    void setVariant(org.bukkit.entity.Frog.Variant p0);

    public interface Variant extends org.bukkit.util.OldEnum<org.bukkit.entity.Frog.Variant>, org.bukkit.Keyed, org.bukkit.registry.RegistryAware {
    }
}

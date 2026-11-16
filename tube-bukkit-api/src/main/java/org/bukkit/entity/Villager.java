package org.bukkit.entity;

public interface Villager extends org.bukkit.entity.AbstractVillager {

    org.bukkit.entity.Villager.Profession getProfession();

    void setProfession(org.bukkit.entity.Villager.Profession p0);

    org.bukkit.entity.Villager.Type getVillagerType();

    void setVillagerType(org.bukkit.entity.Villager.Type p0);

    int getVillagerLevel();

    void setVillagerLevel(int p0);

    int getVillagerExperience();

    void setVillagerExperience(int p0);

    public interface ReputationEvent {
    }

    public interface ReputationType {
    }

    public interface Profession extends org.bukkit.util.OldEnum<org.bukkit.entity.Villager.Profession>, org.bukkit.Keyed, org.bukkit.registry.RegistryAware {
    }

    public interface Type extends org.bukkit.util.OldEnum<org.bukkit.entity.Villager.Type>, org.bukkit.Keyed, org.bukkit.registry.RegistryAware {
    }
}

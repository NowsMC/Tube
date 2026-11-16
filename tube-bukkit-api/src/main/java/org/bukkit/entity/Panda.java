package org.bukkit.entity;

public interface Panda extends org.bukkit.entity.Animals, org.bukkit.entity.Sittable {

    org.bukkit.entity.Panda.Gene getMainGene();

    void setMainGene(org.bukkit.entity.Panda.Gene p0);

    org.bukkit.entity.Panda.Gene getHiddenGene();

    void setHiddenGene(org.bukkit.entity.Panda.Gene p0);

    public enum Gene {
        NORMAL,
        LAZY,
        WORRIED,
        PLAYFUL,
        BROWN,
        WEAK,
        AGGRESSIVE
    }
}

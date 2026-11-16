package org.bukkit.entity;

public interface TropicalFish extends org.bukkit.entity.Fish {

    org.bukkit.DyeColor getPatternColor();

    void setPatternColor(org.bukkit.DyeColor p0);

    org.bukkit.DyeColor getBodyColor();

    void setBodyColor(org.bukkit.DyeColor p0);

    org.bukkit.entity.TropicalFish.Pattern getPattern();

    void setPattern(org.bukkit.entity.TropicalFish.Pattern p0);

    public enum Pattern {
        KOB,
        SUNSTREAK,
        SNOOPER,
        DASHER,
        BRINELY,
        SPOTTY,
        FLOPPER,
        STRIPEY,
        GLITTER,
        BLOCKFISH,
        BETTY,
        CLAYFISH
    }
}

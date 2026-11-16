package org.bukkit.entity;

public interface Spellcaster extends org.bukkit.entity.Illager {

    org.bukkit.entity.Spellcaster.Spell getSpell();

    void setSpell(org.bukkit.entity.Spellcaster.Spell p0);

    public enum Spell {
        NONE,
        SUMMON_VEX,
        FANGS,
        WOLOLO,
        DISAPPEAR,
        BLINDNESS
    }
}

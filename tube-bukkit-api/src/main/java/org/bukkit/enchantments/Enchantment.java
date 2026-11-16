package org.bukkit.enchantments;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Enchantment implements Keyed {

    private static final Map<String, Enchantment> BY_KEY = new HashMap<>();
    private static final Map<String, Enchantment> BY_NAME = new HashMap<>();
    private static final List<Enchantment> ALL = new ArrayList<>();

    public static final Enchantment PROTECTION = register("protection", 4, "PROTECTION_ENVIRONMENTAL");
    public static final Enchantment FIRE_PROTECTION = register("fire_protection", 4, "PROTECTION_FIRE");
    public static final Enchantment FEATHER_FALLING = register("feather_falling", 4, "PROTECTION_FALL");
    public static final Enchantment BLAST_PROTECTION = register("blast_protection", 4, "PROTECTION_EXPLOSIONS");
    public static final Enchantment PROJECTILE_PROTECTION = register("projectile_protection", 4, "PROTECTION_PROJECTILE");
    public static final Enchantment RESPIRATION = register("respiration", 3, "OXYGEN");
    public static final Enchantment AQUA_AFFINITY = register("aqua_affinity", 1, "WATER_WORKER");
    public static final Enchantment THORNS = register("thorns", 3, "THORNS");
    public static final Enchantment DEPTH_STRIDER = register("depth_strider", 3, "DEPTH_STRIDER");
    public static final Enchantment FROST_WALKER = register("frost_walker", 2, "FROST_WALKER");
    public static final Enchantment BINDING_CURSE = register("binding_curse", 1, "BINDING_CURSE");
    public static final Enchantment SHARPNESS = register("sharpness", 5, "DAMAGE_ALL");
    public static final Enchantment SMITE = register("smite", 5, "DAMAGE_UNDEAD");
    public static final Enchantment BANE_OF_ARTHROPODS = register("bane_of_arthropods", 5, "DAMAGE_ARTHROPODS");
    public static final Enchantment KNOCKBACK = register("knockback", 2, "KNOCKBACK");
    public static final Enchantment FIRE_ASPECT = register("fire_aspect", 2, "FIRE_ASPECT");
    public static final Enchantment LOOTING = register("looting", 3, "LOOT_BONUS_MOBS");
    public static final Enchantment SWEEPING_EDGE = register("sweeping_edge", 3, "SWEEPING_EDGE");
    public static final Enchantment EFFICIENCY = register("efficiency", 5, "DIG_SPEED");
    public static final Enchantment SILK_TOUCH = register("silk_touch", 1, "SILK_TOUCH");
    public static final Enchantment UNBREAKING = register("unbreaking", 3, "DURABILITY");
    public static final Enchantment FORTUNE = register("fortune", 3, "LOOT_BONUS_BLOCKS");
    public static final Enchantment POWER = register("power", 5, "ARROW_DAMAGE");
    public static final Enchantment PUNCH = register("punch", 2, "ARROW_KNOCKBACK");
    public static final Enchantment FLAME = register("flame", 1, "ARROW_FIRE");
    public static final Enchantment INFINITY = register("infinity", 1, "ARROW_INFINITE");
    public static final Enchantment LUCK_OF_THE_SEA = register("luck_of_the_sea", 3, "LUCK");
    public static final Enchantment LURE = register("lure", 3, "LURE");
    public static final Enchantment LOYALTY = register("loyalty", 3, "LOYALTY");
    public static final Enchantment IMPALING = register("impaling", 5, "IMPALING");
    public static final Enchantment RIPTIDE = register("riptide", 3, "RIPTIDE");
    public static final Enchantment CHANNELING = register("channeling", 1, "CHANNELING");
    public static final Enchantment MULTISHOT = register("multishot", 1, "MULTISHOT");
    public static final Enchantment QUICK_CHARGE = register("quick_charge", 3, "QUICK_CHARGE");
    public static final Enchantment PIERCING = register("piercing", 4, "PIERCING");
    public static final Enchantment DENSITY = register("density", 5, "DENSITY");
    public static final Enchantment BREACH = register("breach", 4, "BREACH");
    public static final Enchantment WIND_BURST = register("wind_burst", 3, "WIND_BURST");
    public static final Enchantment MENDING = register("mending", 1, "MENDING");
    public static final Enchantment VANISHING_CURSE = register("vanishing_curse", 1, "VANISHING_CURSE");
    public static final Enchantment SOUL_SPEED = register("soul_speed", 3, "SOUL_SPEED");
    public static final Enchantment SWIFT_SNEAK = register("swift_sneak", 3, "SWIFT_SNEAK");
    public static final Enchantment LUNGE = register("lunge", 3, "LUNGE");
    public static final Enchantment PROTECTION_ENVIRONMENTAL = PROTECTION;
    public static final Enchantment PROTECTION_FIRE = FIRE_PROTECTION;
    public static final Enchantment PROTECTION_FALL = FEATHER_FALLING;
    public static final Enchantment PROTECTION_EXPLOSIONS = BLAST_PROTECTION;
    public static final Enchantment PROTECTION_PROJECTILE = PROJECTILE_PROTECTION;
    public static final Enchantment OXYGEN = RESPIRATION;
    public static final Enchantment WATER_WORKER = AQUA_AFFINITY;
    public static final Enchantment DAMAGE_ALL = SHARPNESS;
    public static final Enchantment DAMAGE_UNDEAD = SMITE;
    public static final Enchantment DAMAGE_ARTHROPODS = BANE_OF_ARTHROPODS;
    public static final Enchantment LOOT_BONUS_MOBS = LOOTING;
    public static final Enchantment DIG_SPEED = EFFICIENCY;
    public static final Enchantment DURABILITY = UNBREAKING;
    public static final Enchantment LOOT_BONUS_BLOCKS = FORTUNE;
    public static final Enchantment ARROW_DAMAGE = POWER;
    public static final Enchantment ARROW_KNOCKBACK = PUNCH;
    public static final Enchantment ARROW_FIRE = FLAME;
    public static final Enchantment ARROW_INFINITE = INFINITY;
    public static final Enchantment LUCK = LUCK_OF_THE_SEA;

    private final String key;
    private final int maxLevel;
    private final String name;

    private Enchantment(String key, int maxLevel, String name) {
        this.key = key;
        this.maxLevel = maxLevel;
        this.name = name;
    }

    private static Enchantment register(String key, int maxLevel, String name) {
        Enchantment enchantment = new Enchantment(key, maxLevel, name);
        BY_KEY.put(key, enchantment);
        BY_NAME.put(name, enchantment);
        BY_NAME.put(key.toUpperCase(Locale.ROOT), enchantment);
        ALL.add(enchantment);
        return enchantment;
    }

    @Override
    public NamespacedKey getKey() {
        return NamespacedKey.minecraft(key);
    }

    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getStartLevel() {
        return 1;
    }

    public boolean canEnchantItem(ItemStack item) {
        return item != null;
    }

    public static Enchantment getByKey(NamespacedKey key) {
        return key == null ? null : BY_KEY.get(key.getKey());
    }

    public static Enchantment getByName(String name) {
        return name == null ? null : BY_NAME.get(name.toUpperCase(Locale.ROOT));
    }

    public static Enchantment[] values() {
        return ALL.toArray(new Enchantment[0]);
    }

    @Override
    public String toString() {
        return "Enchantment[" + key + ", " + name + "]";
    }
}

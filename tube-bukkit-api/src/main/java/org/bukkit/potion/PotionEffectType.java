package org.bukkit.potion;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PotionEffectType implements Keyed {

    private static final Map<String, PotionEffectType> BY_KEY = new HashMap<>();
    private static final Map<String, PotionEffectType> BY_NAME = new HashMap<>();
    private static final Map<Integer, PotionEffectType> BY_ID = new HashMap<>();
    private static final List<PotionEffectType> ALL = new ArrayList<>();

    public static final PotionEffectType SPEED = register(1, "speed", "SPEED");
    public static final PotionEffectType SLOWNESS = register(2, "slowness", "SLOW");
    public static final PotionEffectType HASTE = register(3, "haste", "FAST_DIGGING");
    public static final PotionEffectType MINING_FATIGUE = register(4, "mining_fatigue", "SLOW_DIGGING");
    public static final PotionEffectType STRENGTH = register(5, "strength", "INCREASE_DAMAGE");
    public static final PotionEffectType INSTANT_HEALTH = register(6, "instant_health", "HEAL");
    public static final PotionEffectType INSTANT_DAMAGE = register(7, "instant_damage", "HARM");
    public static final PotionEffectType JUMP_BOOST = register(8, "jump_boost", "JUMP");
    public static final PotionEffectType NAUSEA = register(9, "nausea", "CONFUSION");
    public static final PotionEffectType REGENERATION = register(10, "regeneration", "REGENERATION");
    public static final PotionEffectType RESISTANCE = register(11, "resistance", "DAMAGE_RESISTANCE");
    public static final PotionEffectType FIRE_RESISTANCE = register(12, "fire_resistance", "FIRE_RESISTANCE");
    public static final PotionEffectType WATER_BREATHING = register(13, "water_breathing", "WATER_BREATHING");
    public static final PotionEffectType INVISIBILITY = register(14, "invisibility", "INVISIBILITY");
    public static final PotionEffectType BLINDNESS = register(15, "blindness", "BLINDNESS");
    public static final PotionEffectType NIGHT_VISION = register(16, "night_vision", "NIGHT_VISION");
    public static final PotionEffectType HUNGER = register(17, "hunger", "HUNGER");
    public static final PotionEffectType WEAKNESS = register(18, "weakness", "WEAKNESS");
    public static final PotionEffectType POISON = register(19, "poison", "POISON");
    public static final PotionEffectType WITHER = register(20, "wither", "WITHER");
    public static final PotionEffectType HEALTH_BOOST = register(21, "health_boost", "HEALTH_BOOST");
    public static final PotionEffectType ABSORPTION = register(22, "absorption", "ABSORPTION");
    public static final PotionEffectType SATURATION = register(23, "saturation", "SATURATION");
    public static final PotionEffectType GLOWING = register(24, "glowing", "GLOWING");
    public static final PotionEffectType LEVITATION = register(25, "levitation", "LEVITATION");
    public static final PotionEffectType LUCK = register(26, "luck", "LUCK");
    public static final PotionEffectType UNLUCK = register(27, "unluck", "UNLUCK");
    public static final PotionEffectType SLOW_FALLING = register(28, "slow_falling", "SLOW_FALLING");
    public static final PotionEffectType CONDUIT_POWER = register(29, "conduit_power", "CONDUIT_POWER");
    public static final PotionEffectType DOLPHINS_GRACE = register(30, "dolphins_grace", "DOLPHINS_GRACE");
    public static final PotionEffectType BAD_OMEN = register(31, "bad_omen", "BAD_OMEN");
    public static final PotionEffectType HERO_OF_THE_VILLAGE = register(32, "hero_of_the_village", "HERO_OF_THE_VILLAGE");
    public static final PotionEffectType DARKNESS = register(33, "darkness", "DARKNESS");
    public static final PotionEffectType TRIAL_OMEN = register(34, "trial_omen", "TRIAL_OMEN");
    public static final PotionEffectType RAID_OMEN = register(35, "raid_omen", "RAID_OMEN");
    public static final PotionEffectType WIND_CHARGED = register(36, "wind_charged", "WIND_CHARGED");
    public static final PotionEffectType WEAVING = register(37, "weaving", "WEAVING");
    public static final PotionEffectType OOZING = register(38, "oozing", "OOZING");
    public static final PotionEffectType INFESTED = register(39, "infested", "INFESTED");
    public static final PotionEffectType BREATH_OF_THE_NAUTILUS = register(40, "breath_of_the_nautilus", "BREATH_OF_THE_NAUTILUS");
    public static final PotionEffectType SLOW = SLOWNESS;
    public static final PotionEffectType FAST_DIGGING = HASTE;
    public static final PotionEffectType SLOW_DIGGING = MINING_FATIGUE;
    public static final PotionEffectType INCREASE_DAMAGE = STRENGTH;
    public static final PotionEffectType HEAL = INSTANT_HEALTH;
    public static final PotionEffectType HARM = INSTANT_DAMAGE;
    public static final PotionEffectType JUMP = JUMP_BOOST;
    public static final PotionEffectType CONFUSION = NAUSEA;
    public static final PotionEffectType DAMAGE_RESISTANCE = RESISTANCE;

    private final int id;
    private final String key;
    private final String name;

    private PotionEffectType(int id, String key, String name) {
        this.id = id;
        this.key = key;
        this.name = name;
    }

    private static PotionEffectType register(int id, String key, String name) {
        PotionEffectType type = new PotionEffectType(id, key, name);
        BY_KEY.put(key, type);
        BY_NAME.put(name, type);
        BY_NAME.put(key.toUpperCase(Locale.ROOT), type);
        BY_ID.put(id, type);
        ALL.add(type);
        return type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public NamespacedKey getKey() {
        return NamespacedKey.minecraft(key);
    }

    public boolean isInstant() {
        return this == INSTANT_HEALTH || this == INSTANT_DAMAGE || this == SATURATION;
    }

    public PotionEffect createEffect(int duration, int amplifier) {
        return new PotionEffect(this, duration, amplifier);
    }

    public static PotionEffectType getById(int id) {
        return BY_ID.get(id);
    }

    public static PotionEffectType getByName(String name) {
        return name == null ? null : BY_NAME.get(name.toUpperCase(Locale.ROOT));
    }

    public static PotionEffectType getByKey(NamespacedKey key) {
        return key == null ? null : BY_KEY.get(key.getKey());
    }

    public static PotionEffectType[] values() {
        return ALL.toArray(new PotionEffectType[0]);
    }

    @Override
    public String toString() {
        return "PotionEffectType[" + id + ", " + name + "]";
    }
}

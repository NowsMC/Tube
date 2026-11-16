package org.bukkit.entity.memory;

import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MemoryKey<T> implements Keyed {

    private static final Map<NamespacedKey, MemoryKey<?>> BY_KEY = new LinkedHashMap<>();

    public static final MemoryKey<Location> HOME = new MemoryKey<>("home", Location.class);
    public static final MemoryKey<Location> POTENTIAL_JOB_SITE = new MemoryKey<>("potential_job_site", Location.class);
    public static final MemoryKey<Location> JOB_SITE = new MemoryKey<>("job_site", Location.class);
    public static final MemoryKey<Location> MEETING_POINT = new MemoryKey<>("meeting_point", Location.class);
    public static final MemoryKey<Boolean> GOLEM_DETECTED_RECENTLY = new MemoryKey<>("golem_detected_recently", Boolean.class);
    public static final MemoryKey<Long> LAST_SLEPT = new MemoryKey<>("last_slept", Long.class);
    public static final MemoryKey<Long> LAST_WOKEN = new MemoryKey<>("last_woken", Long.class);
    public static final MemoryKey<Long> LAST_WORKED_AT_POI = new MemoryKey<>("last_worked_at_poi", Long.class);
    public static final MemoryKey<Boolean> UNIVERSAL_ANGER = new MemoryKey<>("universal_anger", Boolean.class);
    public static final MemoryKey<UUID> ANGRY_AT = new MemoryKey<>("angry_at", UUID.class);
    public static final MemoryKey<Boolean> ADMIRING_ITEM = new MemoryKey<>("admiring_item", Boolean.class);
    public static final MemoryKey<Boolean> ADMIRING_DISABLED = new MemoryKey<>("admiring_disabled", Boolean.class);
    public static final MemoryKey<Boolean> HUNTED_RECENTLY = new MemoryKey<>("hunted_recently", Boolean.class);
    public static final MemoryKey<Integer> PLAY_DEAD_TICKS = new MemoryKey<>("play_dead_ticks", Integer.class);
    public static final MemoryKey<UUID> LIKED_PLAYER = new MemoryKey<>("liked_player", UUID.class);
    public static final MemoryKey<Location> LIKED_NOTEBLOCK_POSITION = new MemoryKey<>("liked_noteblock", Location.class);

    private final NamespacedKey key;
    private final Class<T> type;

    private MemoryKey(String key, Class<T> type) {
        this.key = NamespacedKey.minecraft(key);
        this.type = type;
        BY_KEY.put(this.key, this);
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    public Class<T> getMemoryClass() {
        return type;
    }

    public static MemoryKey<?> getByKey(NamespacedKey key) {
        return BY_KEY.get(key);
    }

    public static Set<MemoryKey<?>> values() {
        return Collections.unmodifiableSet(new java.util.LinkedHashSet<>(BY_KEY.values()));
    }
}

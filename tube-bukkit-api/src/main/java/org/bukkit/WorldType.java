package org.bukkit;

import java.util.Locale;

public enum WorldType {
    NORMAL("DEFAULT"),
    FLAT("FLAT"),
    LARGE_BIOMES("LARGEBIOMES"),
    AMPLIFIED("AMPLIFIED");

    private final String name;

    WorldType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static WorldType getByName(String name) {
        for (WorldType type : values()) {
            if (type.name.equalsIgnoreCase(name) || type.name().equals(name.toUpperCase(Locale.ROOT))) {
                return type;
            }
        }
        return null;
    }
}

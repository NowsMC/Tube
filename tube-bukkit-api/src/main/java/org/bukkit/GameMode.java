package org.bukkit;

public enum GameMode {
    CREATIVE(1),
    SURVIVAL(0),
    ADVENTURE(2),
    SPECTATOR(3);

    private final int value;

    GameMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static GameMode getByValue(int value) {
        for (GameMode mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        return null;
    }
}

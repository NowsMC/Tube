package space.nows.tube.api.world;

import java.util.Arrays;
import java.util.Optional;

public enum GameRule {
    DO_DAYLIGHT_CYCLE("doDaylightCycle", true),
    KEEP_INVENTORY("keepInventory", false),
    MOB_GRIEFING("mobGriefing", true);

    private final String key;
    private final boolean defaultValue;

    GameRule(String key, boolean defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String key() {
        return key;
    }

    public boolean defaultValue() {
        return defaultValue;
    }

    public static Optional<GameRule> byKey(String key) {
        return Arrays.stream(values()).filter(rule -> rule.key.equalsIgnoreCase(key)).findFirst();
    }
}

package space.nows.tube.server.console;

import java.util.Locale;

public enum ColorMode {
    AUTO,
    ALWAYS,
    NEVER;

    static ColorMode of(String value) {
        try {
            return valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return AUTO;
        }
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}

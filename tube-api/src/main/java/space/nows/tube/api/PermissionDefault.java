package space.nows.tube.api;

import java.util.Locale;
import java.util.Map;

public enum PermissionDefault {
    TRUE,
    FALSE,
    OP,
    NOT_OP;

    private static final Map<String, PermissionDefault> NAMES = Map.ofEntries(
            Map.entry("true", TRUE),
            Map.entry("false", FALSE),
            Map.entry("op", OP),
            Map.entry("isop", OP),
            Map.entry("operator", OP),
            Map.entry("isoperator", OP),
            Map.entry("admin", OP),
            Map.entry("isadmin", OP),
            Map.entry("not-op", NOT_OP),
            Map.entry("not_op", NOT_OP),
            Map.entry("notop", NOT_OP),
            Map.entry("!op", NOT_OP),
            Map.entry("!operator", NOT_OP),
            Map.entry("notoperator", NOT_OP),
            Map.entry("!admin", NOT_OP),
            Map.entry("notadmin", NOT_OP));

    public static PermissionDefault parse(String value, PermissionDefault fallback) {
        if (value == null) {
            return fallback;
        }
        return NAMES.getOrDefault(value.strip().toLowerCase(Locale.ROOT).replace(' ', '-'), fallback);
    }

    public boolean grants(boolean op) {
        return this == TRUE || this == OP && op || this == NOT_OP && !op;
    }
}

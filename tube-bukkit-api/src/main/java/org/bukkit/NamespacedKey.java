package org.bukkit;

import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.Objects;

public final class NamespacedKey {

    public static final String MINECRAFT = "minecraft";
    public static final String BUKKIT = "bukkit";

    private final String namespace;
    private final String key;

    public NamespacedKey(String namespace, String key) {
        this.namespace = Objects.requireNonNull(namespace, "namespace").toLowerCase(Locale.ROOT);
        this.key = Objects.requireNonNull(key, "key").toLowerCase(Locale.ROOT);
    }

    public NamespacedKey(Plugin plugin, String key) {
        this(plugin.getName(), key);
    }

    public static NamespacedKey minecraft(String key) {
        return new NamespacedKey(MINECRAFT, key);
    }

    public static NamespacedKey fromString(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        int colon = string.indexOf(':');
        return colon < 0 ? minecraft(string) : new NamespacedKey(string.substring(0, colon), string.substring(colon + 1));
    }

    public String getNamespace() {
        return namespace;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof NamespacedKey that && namespace.equals(that.namespace) && key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return 31 * namespace.hashCode() + key.hashCode();
    }

    @Override
    public String toString() {
        return namespace + ":" + key;
    }
}

package org.bukkit.attribute;

import org.bukkit.NamespacedKey;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class SimpleAttribute implements Attribute {

    private static final Map<String, Attribute> BY_NAME = new LinkedHashMap<>();

    private final NamespacedKey key;
    private final int ordinal;

    private SimpleAttribute(String name, int ordinal) {
        this.key = NamespacedKey.minecraft(name);
        this.ordinal = ordinal;
    }

    static synchronized Attribute of(String name) {
        return BY_NAME.computeIfAbsent(name, n -> new SimpleAttribute(n, BY_NAME.size()));
    }

    static synchronized Attribute byName(String name) {
        Attribute attribute = BY_NAME.get(name.toLowerCase(Locale.ROOT).replace("generic_", ""));
        if (attribute == null) {
            throw new IllegalArgumentException("No attribute named " + name);
        }
        return attribute;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public int compareTo(Attribute other) {
        return Integer.compare(ordinal, ((SimpleAttribute) other).ordinal);
    }

    @Override
    public String toString() {
        return key.getKey().toUpperCase(Locale.ROOT);
    }
}

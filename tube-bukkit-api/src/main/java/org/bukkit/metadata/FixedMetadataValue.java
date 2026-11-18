package org.bukkit.metadata;

import org.bukkit.plugin.Plugin;

public class FixedMetadataValue implements MetadataValue {

    private final Plugin owner;
    private final Object value;

    public FixedMetadataValue(Plugin owner, Object value) {
        this.owner = owner;
        this.value = value;
    }

    @Override
    public Object value() {
        return value;
    }

    @Override
    public boolean asBoolean() {
        return value instanceof Boolean flag ? flag : value instanceof Number number ? number.intValue() != 0 : value != null;
    }

    @Override
    public Plugin getOwningPlugin() {
        return owner;
    }
}

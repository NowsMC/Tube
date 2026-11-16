package org.bukkit.permissions;

import java.util.LinkedHashMap;
import java.util.Map;

public class Permission {

    public static final PermissionDefault DEFAULT_PERMISSION = PermissionDefault.OP;

    private final String name;
    private final Map<String, Boolean> children;
    private PermissionDefault defaultValue;
    private String description;

    public Permission(String name) {
        this(name, null, null, null);
    }

    public Permission(String name, String description) {
        this(name, description, null, null);
    }

    public Permission(String name, PermissionDefault defaultValue) {
        this(name, null, defaultValue, null);
    }

    public Permission(String name, String description, PermissionDefault defaultValue) {
        this(name, description, defaultValue, null);
    }

    public Permission(String name, String description, PermissionDefault defaultValue, Map<String, Boolean> children) {
        this.name = name;
        this.description = description == null ? "" : description;
        this.defaultValue = defaultValue == null ? DEFAULT_PERMISSION : defaultValue;
        this.children = children == null ? new LinkedHashMap<>() : children;
    }

    public String getName() {
        return name;
    }

    public Map<String, Boolean> getChildren() {
        return children;
    }

    public PermissionDefault getDefault() {
        return defaultValue;
    }

    public void setDefault(PermissionDefault value) {
        this.defaultValue = value == null ? DEFAULT_PERMISSION : value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }
}

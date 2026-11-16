package org.bukkit.configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemorySection implements ConfigurationSection {

    private final Map<String, Object> map = new LinkedHashMap<>();
    private final ConfigurationSection parent;
    private final String path;
    private final String name;

    protected MemorySection() {
        this.parent = null;
        this.path = "";
        this.name = "";
    }

    protected MemorySection(ConfigurationSection parent, String name) {
        this.parent = parent;
        this.name = name;
        String parentPath = parent.getCurrentPath();
        this.path = parentPath == null || parentPath.isEmpty() ? name : parentPath + "." + name;
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        Set<String> keys = new LinkedHashSet<>();
        collect(keys, "", deep);
        return keys;
    }

    private void collect(Set<String> keys, String prefix, boolean deep) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            keys.add(prefix + entry.getKey());
            if (deep && entry.getValue() instanceof MemorySection section) {
                section.collect(keys, prefix + entry.getKey() + ".", true);
            }
        }
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        Map<String, Object> values = new LinkedHashMap<>();
        collectValues(values, "", deep);
        return values;
    }

    private void collectValues(Map<String, Object> values, String prefix, boolean deep) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            values.put(prefix + entry.getKey(), entry.getValue());
            if (deep && entry.getValue() instanceof MemorySection section) {
                section.collectValues(values, prefix + entry.getKey() + ".", true);
            }
        }
    }

    @Override
    public boolean contains(String path) {
        return get(path) != null;
    }

    @Override
    public boolean isSet(String path) {
        return getOwn(path) != null;
    }

    @Override
    public String getCurrentPath() {
        return path;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Configuration getRoot() {
        ConfigurationSection current = this;
        while (current != null) {
            if (current instanceof Configuration configuration && current.getParent() == null) {
                return configuration;
            }
            current = current.getParent();
        }
        return null;
    }

    @Override
    public ConfigurationSection getParent() {
        return parent;
    }

    private Object getOwn(String key) {
        if (key == null || key.isEmpty()) {
            return this;
        }
        MemorySection section = this;
        String remaining = key;
        while (true) {
            int dot = remaining.indexOf('.');
            if (dot < 0) {
                return section.map.get(remaining);
            }
            Object next = section.map.get(remaining.substring(0, dot));
            if (!(next instanceof MemorySection nested)) {
                return null;
            }
            section = nested;
            remaining = remaining.substring(dot + 1);
        }
    }

    @Override
    public Object get(String path) {
        return get(path, null);
    }

    @Override
    public Object get(String path, Object def) {
        Object value = getOwn(path);
        if (value != null) {
            return value;
        }
        Configuration root = getRoot();
        if (root != null && root.getDefaults() != null) {
            String fullPath = this.path.isEmpty() ? path : this.path + "." + path;
            Object fallback = root.getDefaults().get(fullPath);
            if (fallback != null) {
                return fallback;
            }
        }
        return def;
    }

    @Override
    public void set(String path, Object value) {
        MemorySection section = this;
        String remaining = path;
        while (true) {
            int dot = remaining.indexOf('.');
            if (dot < 0) {
                if (value == null) {
                    section.map.remove(remaining);
                } else {
                    section.map.put(remaining, normalize(section, remaining, value));
                }
                return;
            }
            String head = remaining.substring(0, dot);
            Object next = section.map.get(head);
            MemorySection nested;
            if (next instanceof MemorySection found) {
                nested = found;
            } else {
                if (value == null) {
                    return;
                }
                nested = new MemorySection(section, head);
                section.map.put(head, nested);
            }
            section = nested;
            remaining = remaining.substring(dot + 1);
        }
    }

    private static Object normalize(MemorySection owner, String key, Object value) {
        if (value instanceof Map<?, ?> nested) {
            MemorySection section = new MemorySection(owner, key);
            nested.forEach((entryKey, entryValue) -> section.set(String.valueOf(entryKey), entryValue));
            return section;
        }
        return value;
    }

    @Override
    public ConfigurationSection createSection(String path) {
        int dot = path.lastIndexOf('.');
        MemorySection owner = dot < 0 ? this : ensureSection(path.substring(0, dot));
        String key = path.substring(dot + 1);
        MemorySection created = new MemorySection(owner, key);
        owner.map.put(key, created);
        return created;
    }

    private MemorySection ensureSection(String path) {
        MemorySection section = this;
        for (String part : path.split("\\.")) {
            Object next = section.map.get(part);
            MemorySection nested;
            if (next instanceof MemorySection found) {
                nested = found;
            } else {
                nested = new MemorySection(section, part);
                section.map.put(part, nested);
            }
            section = nested;
        }
        return section;
    }

    @Override
    public ConfigurationSection createSection(String path, Map<?, ?> values) {
        ConfigurationSection section = createSection(path);
        values.forEach((key, value) -> section.set(String.valueOf(key), value));
        return section;
    }

    @Override
    public String getString(String path) {
        return getString(path, null);
    }

    @Override
    public String getString(String path, String def) {
        Object value = get(path, def);
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public boolean isString(String path) {
        return get(path) instanceof String;
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public int getInt(String path, int def) {
        return get(path, def) instanceof Number number ? number.intValue() : def;
    }

    @Override
    public boolean isInt(String path) {
        return get(path) instanceof Integer;
    }

    @Override
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return get(path, def) instanceof Boolean value ? value : def;
    }

    @Override
    public boolean isBoolean(String path) {
        return get(path) instanceof Boolean;
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0);
    }

    @Override
    public double getDouble(String path, double def) {
        return get(path, def) instanceof Number number ? number.doubleValue() : def;
    }

    @Override
    public boolean isDouble(String path) {
        return get(path) instanceof Double;
    }

    @Override
    public long getLong(String path) {
        return getLong(path, 0);
    }

    @Override
    public long getLong(String path, long def) {
        return get(path, def) instanceof Number number ? number.longValue() : def;
    }

    @Override
    public boolean isLong(String path) {
        return get(path) instanceof Long;
    }

    @Override
    public List<?> getList(String path) {
        return getList(path, null);
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        return get(path, def) instanceof List<?> list ? list : def;
    }

    @Override
    public boolean isList(String path) {
        return get(path) instanceof List<?>;
    }

    @Override
    public List<String> getStringList(String path) {
        List<?> list = getList(path);
        List<String> result = new ArrayList<>();
        if (list != null) {
            for (Object element : list) {
                if (element instanceof String || element instanceof Number || element instanceof Boolean || element instanceof Character) {
                    result.add(String.valueOf(element));
                }
            }
        }
        return result;
    }

    @Override
    public List<Integer> getIntegerList(String path) {
        List<?> list = getList(path);
        List<Integer> result = new ArrayList<>();
        if (list != null) {
            for (Object element : list) {
                if (element instanceof Number number) {
                    result.add(number.intValue());
                }
            }
        }
        return result;
    }

    @Override
    public List<Map<?, ?>> getMapList(String path) {
        List<?> list = getList(path);
        List<Map<?, ?>> result = new ArrayList<>();
        if (list != null) {
            for (Object element : list) {
                if (element instanceof Map<?, ?> value) {
                    result.add(value);
                }
            }
        }
        return result;
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        return get(path) instanceof ConfigurationSection section ? section : null;
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return get(path) instanceof ConfigurationSection;
    }

    @Override
    public void addDefault(String path, Object value) {
        Configuration root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot add default without root");
        }
        root.addDefault(this.path.isEmpty() ? path : this.path + "." + path, value);
    }

    Map<String, Object> toPlainMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue() instanceof MemorySection section ? section.toPlainMap() : entry.getValue());
        }
        return result;
    }
}

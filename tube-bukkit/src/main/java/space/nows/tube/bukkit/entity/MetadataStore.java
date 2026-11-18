package space.nows.tube.bukkit.entity;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MetadataStore {

    private final Map<UUID, Map<String, List<MetadataValue>>> values = new ConcurrentHashMap<>();

    public void set(UUID owner, String key, MetadataValue value) {
        List<MetadataValue> list = values.computeIfAbsent(owner, id -> new ConcurrentHashMap<>()).computeIfAbsent(key, name -> new ArrayList<>());
        synchronized (list) {
            list.removeIf(existing -> existing.getOwningPlugin() == value.getOwningPlugin());
            list.add(value);
        }
    }

    public List<MetadataValue> get(UUID owner, String key) {
        Map<String, List<MetadataValue>> byKey = values.get(owner);
        List<MetadataValue> list = byKey == null ? null : byKey.get(key);
        if (list == null) {
            return List.of();
        }
        synchronized (list) {
            return List.copyOf(list);
        }
    }

    public void remove(UUID owner, String key, Plugin plugin) {
        Map<String, List<MetadataValue>> byKey = values.get(owner);
        List<MetadataValue> list = byKey == null ? null : byKey.get(key);
        if (list != null) {
            synchronized (list) {
                list.removeIf(existing -> existing.getOwningPlugin() == plugin);
            }
        }
    }

    public void forget(UUID owner) {
        values.remove(owner);
    }
}

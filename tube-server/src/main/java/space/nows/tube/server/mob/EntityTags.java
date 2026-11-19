package space.nows.tube.server.mob;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;

import java.util.HashSet;
import java.util.Set;

public final class EntityTags {

    private EntityTags() {
    }

    public static Set<EntityType> of(String tag) {
        RegistryTag<EntityType> entries = EntityType.staticRegistry().getTag(Key.key(tag));
        Set<EntityType> types = new HashSet<>();
        if (entries != null) {
            for (RegistryKey<EntityType> key : entries) {
                EntityType type = EntityType.fromKey(key.key());
                if (type != null) {
                    types.add(type);
                }
            }
        }
        return Set.copyOf(types);
    }
}

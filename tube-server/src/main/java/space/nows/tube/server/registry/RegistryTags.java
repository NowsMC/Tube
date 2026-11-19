package space.nows.tube.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public final class RegistryTags {

    private RegistryTags() {
    }

    public static Set<Material> items(String tag) {
        return members(Material.staticRegistry(), tag, Material::fromKey);
    }

    public static Set<EntityType> entities(String tag) {
        return members(EntityType.staticRegistry(), tag, EntityType::fromKey);
    }

    public static Set<Block> blocks(String tag) {
        return members(Block.staticRegistry(), tag, Block::fromKey);
    }

    public static boolean has(Material material, String tag) {
        return contains(Material.staticRegistry(), tag, material);
    }

    public static boolean has(EntityType type, String tag) {
        return contains(EntityType.staticRegistry(), tag, type);
    }

    public static boolean has(Block block, String tag) {
        return contains(Block.staticRegistry(), tag, block);
    }

    private static <T> boolean contains(Registry<T> registry, String tag, RegistryKey<T> value) {
        RegistryTag<T> entries = registry.getTag(key(tag));
        return entries != null && entries.contains(value);
    }

    private static <T> Set<T> members(Registry<T> registry, String tag, Function<Key, T> lookup) {
        RegistryTag<T> entries = registry.getTag(key(tag));
        if (entries == null) {
            return Set.of();
        }
        Set<T> members = new HashSet<>();
        for (RegistryKey<T> entry : entries) {
            T value = lookup.apply(entry.key());
            if (value != null) {
                members.add(value);
            }
        }
        return Set.copyOf(members);
    }

    private static <T> TagKey<T> key(String tag) {
        return tag.startsWith("#") ? TagKey.ofHash(tag) : TagKey.of(tag);
    }
}

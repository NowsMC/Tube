package space.nows.tube.server.mob.interact;

import net.kyori.adventure.key.Key;
import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;

import java.util.HashSet;
import java.util.Set;

public final class ItemTags {

    private ItemTags() {
    }

    public static Set<Material> of(String tag) {
        RegistryTag<Material> entries = Material.staticRegistry().getTag(Key.key(tag));
        Set<Material> materials = new HashSet<>();
        if (entries != null) {
            for (RegistryKey<Material> key : entries) {
                Material material = Material.fromKey(key.key());
                if (material != null) {
                    materials.add(material);
                }
            }
        }
        return Set.copyOf(materials);
    }
}

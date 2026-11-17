package space.nows.tube.server.command.support;

import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.potion.PotionEffect;

import java.util.Collection;
import java.util.List;

public final class RegistryNames {

    private static volatile List<String> items;
    private static volatile List<String> blocks;
    private static volatile List<String> entities;
    private static volatile List<String> effects;

    private RegistryNames() {
    }

    public static List<String> items() {
        if (items == null) {
            items = keys(Material.values());
        }
        return items;
    }

    public static List<String> blocks() {
        if (blocks == null) {
            blocks = keys(Block.values());
        }
        return blocks;
    }

    public static List<String> entities() {
        if (entities == null) {
            entities = EntityType.values().stream()
                    .filter(type -> type != EntityType.PLAYER)
                    .map(type -> type.key().asString())
                    .sorted()
                    .toList();
        }
        return entities;
    }

    public static List<String> effects() {
        if (effects == null) {
            effects = keys(PotionEffect.values());
        }
        return effects;
    }

    private static List<String> keys(Collection<? extends Keyed> values) {
        return values.stream().map(value -> value.key().asString()).sorted().toList();
    }
}

package space.nows.tube.server.gameplay.throwing;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;

import java.util.Arrays;
import java.util.Optional;

enum ThrownKind {
    SNOWBALL(Material.SNOWBALL, EntityType.SNOWBALL, "minecraft:entity.snowball.throw", 0),
    EGG(Material.EGG, EntityType.EGG, "minecraft:entity.egg.throw", 0),
    BROWN_EGG(Material.BROWN_EGG, EntityType.EGG, "minecraft:entity.egg.throw", 0),
    BLUE_EGG(Material.BLUE_EGG, EntityType.EGG, "minecraft:entity.egg.throw", 0),
    ENDER_PEARL(Material.ENDER_PEARL, EntityType.ENDER_PEARL, "minecraft:entity.ender_pearl.throw", 20);

    private final Material item;
    private final EntityType entity;
    private final String sound;
    private final int cooldownTicks;

    ThrownKind(Material item, EntityType entity, String sound, int cooldownTicks) {
        this.item = item;
        this.entity = entity;
        this.sound = sound;
        this.cooldownTicks = cooldownTicks;
    }

    Material item() {
        return item;
    }

    EntityType entity() {
        return entity;
    }

    String sound() {
        return sound;
    }

    int cooldownTicks() {
        return cooldownTicks;
    }

    boolean isEgg() {
        return entity == EntityType.EGG;
    }

    static Optional<ThrownKind> of(Material material) {
        return Arrays.stream(values()).filter(kind -> kind.item == material).findFirst();
    }

    static Optional<ThrownKind> of(EntityType type) {
        return Arrays.stream(values()).filter(kind -> kind.entity == type).findFirst();
    }
}

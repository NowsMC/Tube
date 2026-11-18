package space.nows.tube.server.mob.kind.monster;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import space.nows.tube.server.mob.Creature;

final class BabyZombie {

    private static final AttributeModifier SPEED = new AttributeModifier("minecraft:baby", 0.5, AttributeOperation.ADD_MULTIPLIED_BASE);

    private BabyZombie() {
    }

    static void make(Creature zombie) {
        if (zombie.getEntityMeta() instanceof ZombieMeta meta) {
            meta.setBaby(true);
            zombie.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(SPEED);
        }
    }
}

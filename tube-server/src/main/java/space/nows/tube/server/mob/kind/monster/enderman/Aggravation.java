package space.nows.tube.server.mob.kind.monster.enderman;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.metadata.monster.EndermanMeta;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Aggravation implements Trait {

    private static final AttributeModifier ATTACKING = new AttributeModifier("minecraft:attacking", 0.15, AttributeOperation.ADD_VALUE);

    private final Creature enderman;
    private Entity lastTarget;
    private long changedAt;

    Aggravation(Creature enderman) {
        this.enderman = enderman;
    }

    long changedAt() {
        return changedAt;
    }

    void staredAt() {
        if (enderman.getEntityMeta() instanceof EndermanMeta meta) {
            meta.setStaring(true);
        }
    }

    @Override
    public void tick() {
        Entity target = enderman.getTarget();
        if (target == lastTarget) {
            return;
        }
        lastTarget = target;
        EndermanMeta meta = enderman.getEntityMeta() instanceof EndermanMeta ender ? ender : null;
        var speed = enderman.getAttribute(Attribute.MOVEMENT_SPEED);
        if (target == null) {
            changedAt = 0;
            speed.removeModifier(ATTACKING);
            if (meta != null) {
                meta.setScreaming(false);
                meta.setStaring(false);
            }
        } else {
            changedAt = enderman.getAliveTicks();
            speed.removeModifier(ATTACKING);
            speed.addModifier(ATTACKING);
            if (meta != null) {
                meta.setScreaming(true);
            }
        }
    }
}

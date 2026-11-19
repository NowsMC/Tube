package space.nows.tube.server.mob.sense;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.tag.Tag;

import java.util.UUID;

public final class Assault {

    private static final Tag<UUID> STRUCK = Tag.UUID("tube:struck");
    private static final Tag<Integer> STRUCK_COUNT = Tag.Integer("tube:struck_count").defaultValue(0);
    private static final Tag<UUID> STRUCK_BY = Tag.UUID("tube:struck_by");
    private static final Tag<Integer> STRUCK_BY_COUNT = Tag.Integer("tube:struck_by_count").defaultValue(0);

    private Assault() {
    }

    public static void record(LivingEntity attacker, LivingEntity victim) {
        attacker.setTag(STRUCK, victim.getUuid());
        attacker.setTag(STRUCK_COUNT, attacker.getTag(STRUCK_COUNT) + 1);
        victim.setTag(STRUCK_BY, attacker.getUuid());
        victim.setTag(STRUCK_BY_COUNT, victim.getTag(STRUCK_BY_COUNT) + 1);
    }

    public static int struckCount(LivingEntity entity) {
        return entity.getTag(STRUCK_COUNT);
    }

    public static int struckByCount(LivingEntity entity) {
        return entity.getTag(STRUCK_BY_COUNT);
    }

    public static LivingEntity lastStruck(LivingEntity entity) {
        return find(entity, entity.getTag(STRUCK));
    }

    public static LivingEntity lastStruckBy(LivingEntity entity) {
        return find(entity, entity.getTag(STRUCK_BY));
    }

    private static LivingEntity find(LivingEntity near, UUID uuid) {
        if (uuid == null || near.getInstance() == null) {
            return null;
        }
        Entity found = near.getInstance().getEntityByUuid(uuid);
        return found instanceof LivingEntity living && !living.isDead() ? living : null;
    }
}

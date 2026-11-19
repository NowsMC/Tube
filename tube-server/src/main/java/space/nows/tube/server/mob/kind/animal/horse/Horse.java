package space.nows.tube.server.mob.kind.animal.horse;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.animal.HorseMeta;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;

import java.util.random.RandomGenerator;

public final class Horse extends Equine {

    private static final CreatureStats STATS = CreatureStats.animal(53, 0.225);

    @Override
    public EntityType type() {
        return EntityType.HORSE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        rollHealth(creature, random);
        rollSpeedAndJump(creature, random);
        if (creature.getEntityMeta() instanceof HorseMeta meta) {
            HorseMeta.Variant[] colors = HorseMeta.Variant.values();
            HorseMeta.Marking[] markings = HorseMeta.Marking.values();
            meta.setVariantAndMarking(colors[random.nextInt(colors.length)], markings[random.nextInt(markings.length)]);
        }
    }
}

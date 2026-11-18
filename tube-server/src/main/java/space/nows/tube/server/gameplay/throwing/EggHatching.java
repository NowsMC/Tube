package space.nows.tube.server.gameplay.throwing;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;

import java.util.random.RandomGenerator;

final class EggHatching {

    private static final int HATCH_ONE_IN = 8;
    private static final int LITTER_ONE_IN = 32;
    private static final int LITTER = 4;

    private EggHatching() {
    }

    static int hatch(Instance instance, Pos at, RandomGenerator random) {
        if (random.nextInt(HATCH_ONE_IN) != 0) {
            return 0;
        }
        int count = random.nextInt(LITTER_ONE_IN) == 0 ? LITTER : 1;
        for (int i = 0; i < count; i++) {
            Creature chick = new Creature(CreatureKinds.of(EntityType.CHICKEN).orElseThrow());
            if (chick.getEntityMeta() instanceof AgeableMobMeta meta) {
                meta.setBaby(true);
            }
            CreatureKinds.spawn(chick, instance, at);
        }
        return count;
    }
}

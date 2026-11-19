package space.nows.tube.server.mob.kind.monster.skeleton;

import net.minestom.server.entity.EntityType;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import space.nows.tube.server.mob.CreatureStats;

public final class Parched extends Skeleton {

    private static final CreatureStats STATS = CreatureStats.monster(16, 0.25);
    private static final int HARD_INTERVAL = 50;
    private static final int INTERVAL = 70;

    @Override
    public EntityType type() {
        return EntityType.PARCHED;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    protected int attackInterval(boolean hard) {
        return hard ? HARD_INTERVAL : INTERVAL;
    }

    @Override
    protected Potion arrowEffect() {
        return new Potion(PotionEffect.WEAKNESS, 0, 600);
    }
}

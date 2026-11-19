package space.nows.tube.server.mob.kind.monster;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.idle.ClimbPowderSnow;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.trait.Lifespan;

public final class Endermite extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(8, 0.25);
    private static final int LIFESPAN = 2400;

    @Override
    public EntityType type() {
        return EntityType.ENDERMITE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(1, new StayAfloat(creature));
        brain.add(1, new ClimbPowderSnow(creature));
        brain.add(2, new MeleeAttack(creature, 1.0, false));
        brain.add(3, Wander.onLand(creature, 1.0));
        brain.add(7, WatchNearby.players(creature, 8f));
        brain.add(8, new LookAround(creature));
        brain.add(1, new Retaliate(creature).alertingOthers());
        brain.add(2, HuntNearest.players(creature, true));
        creature.addTrait(new Lifespan(creature, LIFESPAN));
    }
}

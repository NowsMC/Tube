package space.nows.tube.server.mob.kind.monster.blaze;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;
import space.nows.tube.server.mob.trait.WaterAllergy;

public final class Blaze extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(20, 0.23).withDamage(6).withFollowRange(48);

    @Override
    public EntityType type() {
        return EntityType.BLAZE;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public boolean fireproof() {
        return true;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(4, new Barrage(creature));
        brain.add(7, Wander.onLandOnly(creature, 1.0));
        brain.add(8, WatchNearby.players(creature, 8f));
        brain.add(8, new LookAround(creature));
        brain.add(1, new Retaliate(creature).alertingOthers());
        brain.add(2, HuntNearest.players(creature, true));
        creature.addTrait(new Hover(creature));
        creature.addTrait(new WaterAllergy(creature));
    }
}

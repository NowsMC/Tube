package space.nows.tube.server.mob.kind.monster.shulker;

import net.minestom.server.entity.EntityType;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class Shulker extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(30, 0).withKnockbackResistance(1);

    @Override
    public EntityType type() {
        return EntityType.SHULKER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public boolean leavesWhenFar(Creature creature) {
        return false;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.setEnabled(Control.MOVE, false);
        creature.addTrait(new Peek(creature));
        brain.add(4, new Barrage(creature));
        brain.add(1, WatchNearby.players(creature, 8f));
        brain.add(8, new LookAround(creature));
        brain.add(1, new Retaliate(creature).alertingOthers());
        brain.add(2, HuntNearest.players(creature, true));
    }
}

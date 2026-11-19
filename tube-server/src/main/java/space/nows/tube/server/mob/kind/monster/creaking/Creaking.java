package space.nows.tube.server.mob.kind.monster.creaking;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class Creaking extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(1, 0.4).withDamage(3).withFollowRange(32);
    private static final double STEP = 1.0625;

    @Override
    public EntityType type() {
        return EntityType.CREAKING;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.getAttribute(Attribute.STEP_HEIGHT).setBaseValue(STEP);
        creature.addTrait(new Watched(creature));
        brain.add(1, new MeleeAttack(creature, 1.0, true));
        brain.add(5, Wander.onLand(creature, 0.6));
        brain.add(1, HuntNearest.players(creature, true));
    }
}

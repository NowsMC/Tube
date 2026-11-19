package space.nows.tube.server.mob.kind.monster.ghast;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.idle.FloatAround;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.kind.monster.Monster;
import space.nows.tube.server.mob.move.FloatControl;

public final class Ghast extends Monster {

    private static final CreatureStats STATS = CreatureStats.mob(10, 0.7).withFollowRange(100);
    private static final double FLYING_SPEED = 0.06;
    private static final float EXPLOSION_POWER = 1;
    private static final double REACH_HEIGHT = 4;

    @Override
    public EntityType type() {
        return EntityType.GHAST;
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
        creature.getAttribute(Attribute.FLYING_SPEED).setBaseValue(FLYING_SPEED);
        FloatControl control = new FloatControl(creature);
        creature.addTrait(control);
        brain.add(5, new FloatAround(creature, control));
        brain.add(7, new Volley(creature, EXPLOSION_POWER));
        brain.add(1, HuntNearest.players(creature, true,
                target -> Math.abs(target.getPosition().y() - creature.getPosition().y()) <= REACH_HEIGHT));
    }
}

package space.nows.tube.server.mob.kind.monster.illager;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class Ravager extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(100, 0.3).withDamage(12).withKnockbackResistance(0.75).withFollowRange(32);
    private static final double KNOCKBACK = 1.5;
    private static final double STEP = 1.0;

    @Override
    public EntityType type() {
        return EntityType.RAVAGER;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.getAttribute(Attribute.ATTACK_KNOCKBACK).setBaseValue(KNOCKBACK);
        creature.getAttribute(Attribute.STEP_HEIGHT).setBaseValue(STEP);
        brain.add(0, new StayAfloat(creature));
        brain.add(4, new MeleeAttack(creature, 1.0, true));
        brain.add(5, Wander.onLand(creature, 0.4));
        brain.add(6, WatchNearby.players(creature, 6f));
        brain.add(10, WatchNearby.of(creature, entity -> entity instanceof Creature, 8f));
        brain.add(2, new Retaliate(creature, Illager.RAIDERS).alertingOthers());
        brain.add(3, HuntNearest.players(creature, true));
        brain.add(4, HuntNearest.of(creature, Illager::villager, 10, true, target -> !(target instanceof Creature villager && villager.isBaby())));
        brain.add(4, HuntNearest.of(creature, EntityType.IRON_GOLEM, true));
    }
}

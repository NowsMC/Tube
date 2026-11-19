package space.nows.tube.server.mob.kind.monster.hoglin;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class Zoglin extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(40, 0.3).withKnockbackResistance(0.6).withDamage(6);
    private static final int ADULT_INTERVAL = 40;
    private static final int BABY_INTERVAL = 15;

    @Override
    public EntityType type() {
        return EntityType.ZOGLIN;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.getAttribute(Attribute.ATTACK_KNOCKBACK).setBaseValue(1);
        brain.add(1, new MeleeAttack(creature, 1.0, true, creature.isBaby() ? BABY_INTERVAL : ADULT_INTERVAL));
        brain.add(5, Wander.onLand(creature, 0.4));
        brain.add(6, WatchNearby.of(creature, entity -> entity instanceof Player || entity instanceof Creature, 8f));
        brain.add(7, new LookAround(creature));
        brain.add(1, new Retaliate(creature));
        brain.add(2, HuntNearest.of(creature, entity -> entity instanceof LivingEntity && entity.getEntityType() != EntityType.ZOGLIN
                && entity.getEntityType() != EntityType.CREEPER, 10, true, target -> true));
    }

    @Override
    public float attackDamage(Creature creature) {
        return Gore.damage(creature);
    }

    @Override
    public void landedHit(Creature creature, LivingEntity target) {
        Gore.toss(creature, target);
    }
}

package space.nows.tube.server.mob.kind.monster.warden;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class Warden extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(500, 0.3).withDamage(30).withKnockbackResistance(1)
            .withFollowRange(24);
    private static final double KNOCKBACK = 1.5;
    private static final int ATTACK_INTERVAL = 18;

    @Override
    public EntityType type() {
        return EntityType.WARDEN;
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
    public boolean leavesWhenFar(Creature creature) {
        return false;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        creature.getAttribute(Attribute.ATTACK_KNOCKBACK).setBaseValue(KNOCKBACK);
        creature.addTrait(new Listening(creature));
        creature.addTrait(new Gloom(creature));
        brain.add(1, new SonicBoom(creature));
        brain.add(2, new MeleeAttack(creature, 1.2, true, ATTACK_INTERVAL));
        brain.add(5, Wander.onLand(creature, 0.5));
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        if (damage.getAttacker() instanceof LivingEntity attacker) {
            creature.trait(Listening.class).ifPresent(listening -> listening.provoke(attacker));
        }
    }
}

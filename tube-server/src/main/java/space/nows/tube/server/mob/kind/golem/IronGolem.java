package space.nows.tube.server.mob.kind.golem;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.sense.Grudge;

public final class IronGolem extends Golem {

    private static final CreatureStats STATS = CreatureStats.mob(100, 0.25).withDamage(15).withKnockbackResistance(1);
    private static final int MIN_ANGER = 400;
    private static final int MAX_ANGER = 780;
    private static final double TOSS = 0.4 * 20;

    @Override
    public EntityType type() {
        return EntityType.IRON_GOLEM;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public Disposition disposition() {
        return Disposition.NEUTRAL;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Grudge grudge = new Grudge(creature, MIN_ANGER, MAX_ANGER);
        creature.addTrait(grudge);
        brain.add(1, new MeleeAttack(creature, 1.0, true));
        brain.add(4, Wander.onLand(creature, 0.6));
        brain.add(7, WatchNearby.players(creature, 6f));
        brain.add(8, new LookAround(creature));
        brain.add(2, new Retaliate(creature));
        brain.add(3, HuntNearest.players(creature, true, grudge::holds));
        brain.add(3, HuntNearest.of(creature, entity -> Golem.enemy(entity) && entity.getEntityType() != EntityType.CREEPER,
                5, false, target -> true));
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        if (damage.getAttacker() instanceof LivingEntity attacker) {
            creature.trait(Grudge.class).ifPresent(grudge -> grudge.provoke(attacker));
        }
    }

    @Override
    public float attackDamage(Creature creature) {
        float base = (float) creature.getAttributeValue(Attribute.ATTACK_DAMAGE);
        return (int) base > 0 ? base / 2 + creature.random().nextInt((int) base) : base;
    }

    @Override
    public void landedHit(Creature creature, LivingEntity target) {
        target.setVelocity(target.getVelocity().add(0, TOSS, 0));
    }
}

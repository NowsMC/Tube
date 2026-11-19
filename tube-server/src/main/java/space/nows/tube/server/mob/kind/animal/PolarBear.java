package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.Material;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.Disposition;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.Panic;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.idle.Wander;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.social.FollowParent;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.life.Lifecycle;
import space.nows.tube.server.mob.sense.Grudge;

import java.util.Set;

public final class PolarBear extends Animal {

    private static final CreatureStats STATS = CreatureStats.animal(30, 0.25).withFollowRange(20).withDamage(6);
    private static final int MIN_ANGER = 400;
    private static final int MAX_ANGER = 780;
    private static final double CUB_WATCH_RANGE = 8;

    @Override
    public EntityType type() {
        return EntityType.POLAR_BEAR;
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
        Lifecycle life = lifecycle(creature);
        Grudge grudge = new Grudge(creature, MIN_ANGER, MAX_ANGER);
        creature.addTrait(grudge);
        brain.add(0, new StayAfloat(creature));
        brain.add(1, new MeleeAttack(creature, 1.25, true));
        brain.add(1, new Panic(creature, 2.0));
        brain.add(4, new FollowParent(creature, life, 1.25));
        brain.add(5, Wander.anywhere(creature, 1.0));
        brain.add(6, WatchNearby.players(creature, 6f));
        brain.add(7, new LookAround(creature));
        brain.add(1, new Retaliate(creature).alertingOthers());
        brain.add(2, HuntNearest.players(creature, false).when(bear -> !bear.isBaby() && guardsCub(bear)));
        brain.add(3, HuntNearest.players(creature, true, grudge::holds));
        brain.add(4, HuntNearest.of(creature, EntityType.FOX, true).when(bear -> !bear.isBaby()));
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        if (damage.getAttacker() instanceof LivingEntity attacker) {
            creature.trait(Grudge.class).ifPresent(grudge -> grudge.provoke(attacker));
        }
    }

    @Override
    public Set<Material> breedingFood() {
        return Set.of();
    }

    private static boolean guardsCub(Creature bear) {
        for (Entity entity : bear.getInstance().getNearbyEntities(bear.getPosition(), CUB_WATCH_RANGE)) {
            if (entity instanceof Creature other && other.kind() == bear.kind() && other.isBaby()) {
                return true;
            }
        }
        return false;
    }
}

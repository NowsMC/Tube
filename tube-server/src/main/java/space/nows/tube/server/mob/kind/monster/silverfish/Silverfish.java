package space.nows.tube.server.mob.kind.monster.silverfish;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.combat.MeleeAttack;
import space.nows.tube.server.mob.brain.idle.ClimbPowderSnow;
import space.nows.tube.server.mob.brain.idle.StayAfloat;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.brain.target.Retaliate;
import space.nows.tube.server.mob.kind.monster.Monster;

public final class Silverfish extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(8, 0.25).withDamage(1);
    private static final float HOST_PREFERENCE = 10f;

    @Override
    public EntityType type() {
        return EntityType.SILVERFISH;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(1, new StayAfloat(creature));
        brain.add(1, new ClimbPowderSnow(creature));
        brain.add(3, new WakeFriends(creature));
        brain.add(4, new MeleeAttack(creature, 1.0, false));
        brain.add(5, new MergeWithStone(creature));
        brain.add(1, new Retaliate(creature).alertingOthers());
        brain.add(2, HuntNearest.players(creature, true));
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        if (damage.getAttacker() != null) {
            creature.brain().behaviors().stream()
                    .filter(WakeFriends.class::isInstance)
                    .map(WakeFriends.class::cast)
                    .forEach(WakeFriends::alarm);
        }
    }

    @Override
    public float walkPreference(Creature creature, Point at) {
        if (Infestation.canHost(creature.blockAt(at.add(0, -1, 0)))) {
            return HOST_PREFERENCE;
        }
        return super.walkPreference(creature, at);
    }
}

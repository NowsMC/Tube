package space.nows.tube.server.mob.kind.monster.guardian;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import space.nows.tube.api.entity.mob.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;
import space.nows.tube.server.mob.brain.idle.LookAround;
import space.nows.tube.server.mob.brain.idle.SwimAbout;
import space.nows.tube.server.mob.brain.idle.WatchNearby;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.kind.monster.Monster;

public class Guardian extends Monster {

    private static final CreatureStats STATS = CreatureStats.monster(30, 0.5).withDamage(6);
    private static final double SWIM_SPEED = 0.06;
    private static final double TOO_CLOSE = 9;
    private static final float THORNS = 2;
    private static final int BEAM_TICKS = 80;

    @Override
    public EntityType type() {
        return EntityType.GUARDIAN;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public boolean aquatic() {
        return true;
    }

    protected boolean elder() {
        return false;
    }

    protected int beamTicks() {
        return BEAM_TICKS;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        brain.add(4, new Beam(creature, elder(), beamTicks()));
        brain.add(7, new SwimAbout(creature, SWIM_SPEED));
        brain.add(8, WatchNearby.players(creature, 8f));
        brain.add(8, WatchNearby.of(creature, entity -> entity.getEntityType() == EntityType.GUARDIAN, 12f));
        brain.add(9, new LookAround(creature));
        brain.add(1, HuntNearest.of(creature, Guardian::prey, 10, true,
                target -> target.getPosition().distanceSquared(creature.getPosition()) > TOO_CLOSE));
    }

    private static boolean prey(Entity entity) {
        return entity instanceof Player || entity.getEntityType() == EntityType.SQUID || entity.getEntityType() == EntityType.AXOLOTL;
    }

    @Override
    public void damaged(Creature creature, Damage damage) {
        boolean still = creature.getVelocity().lengthSquared() < 1.0E-4;
        if (still && damage.getType() != DamageType.THORNS && damage.getSource() instanceof LivingEntity cause && cause != creature) {
            cause.damage(new Damage(DamageType.THORNS, creature, creature, creature.getPosition(), THORNS));
        }
    }
}

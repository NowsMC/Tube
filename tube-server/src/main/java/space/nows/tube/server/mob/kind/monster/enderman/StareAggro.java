package space.nows.tube.server.mob.kind.monster.enderman;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.target.Targeting;

final class StareAggro extends Behavior {

    private static final int AGGRO_DELAY = 5;
    private static final int CHASE_TELEPORT_DELAY = 30;
    private static final double TOO_CLOSE_SQUARED = 16.0;
    private static final double FAR_SQUARED = 256.0;

    private final Creature enderman;
    private final Aggravation aggravation;
    private Player pending;
    private LivingEntity target;
    private int aggroIn;
    private int chaseTimer;

    StareAggro(Creature enderman, Aggravation aggravation) {
        super(Control.TARGET);
        this.enderman = enderman;
        this.aggravation = aggravation;
    }

    private double range() {
        return enderman.getAttributeValue(Attribute.FOLLOW_RANGE);
    }

    @Override
    public boolean canStart() {
        pending = Targeting.combat(range()).only(this::stares).nearestPlayer(enderman);
        return pending != null;
    }

    private boolean stares(LivingEntity entity) {
        return entity instanceof Player player && Gazing.staredAtBy(enderman, player);
    }

    @Override
    public void start() {
        aggroIn = ticks(AGGRO_DELAY);
        chaseTimer = 0;
        aggravation.staredAt();
    }

    @Override
    public boolean canContinue() {
        if (pending != null) {
            if (!stares(pending)) {
                return false;
            }
            enderman.gaze().lookAt(pending, 10f, 10f);
            return true;
        }
        if (target == null || !Targeting.combat(range()).blind().test(enderman, target)) {
            return false;
        }
        enderman.setTarget(target);
        return true;
    }

    @Override
    public void stop() {
        pending = null;
        target = null;
        enderman.setTarget(null);
    }

    @Override
    public void tick() {
        if (enderman.getTarget() == null) {
            target = null;
        }
        if (pending != null) {
            if (--aggroIn <= 0) {
                target = pending;
                pending = null;
                enderman.setTarget(target);
            }
            return;
        }
        if (!(target instanceof Player player)) {
            return;
        }
        double distance = player.getPosition().distanceSquared(enderman.getPosition());
        if (Gazing.staredAtBy(enderman, player)) {
            if (distance < TOO_CLOSE_SQUARED) {
                EnderTeleport.randomly(enderman);
            }
            chaseTimer = 0;
        } else if (distance > FAR_SQUARED && chaseTimer++ >= ticks(CHASE_TELEPORT_DELAY) && EnderTeleport.towards(enderman, player)) {
            chaseTimer = 0;
        }
    }
}

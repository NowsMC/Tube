package space.nows.tube.server.mob.kind.monster.zombie;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.combat.Reach;
import space.nows.tube.server.mob.move.Swim;

final class Dive extends Behavior {

    private static final double SWIM_SPEED = 0.08;
    private static final int ATTACK_INTERVAL = 20;

    private final Creature drowned;
    private int attackIn;

    Dive(Creature drowned) {
        super(Control.MOVE, Control.LOOK);
        this.drowned = drowned;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = drowned.target();
        return target != null && !target.isDead() && drowned.inWater();
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = drowned.target();
        if (target == null) {
            return;
        }
        drowned.gaze().lookAt(target, 30f, 30f);
        Swim.steer(drowned, target.getPosition().add(0, target.getBoundingBox().height() / 2, 0), SWIM_SPEED);
        attackIn = Math.max(attackIn - 1, 0);
        if (attackIn == 0 && Reach.melee(drowned, target) && drowned.sight().canSee(target)) {
            attackIn = ATTACK_INTERVAL;
            drowned.strike(target);
        }
    }
}

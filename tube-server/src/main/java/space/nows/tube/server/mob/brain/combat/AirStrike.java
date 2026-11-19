package space.nows.tube.server.mob.brain.combat;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.FloatControl;

public final class AirStrike extends Behavior {

    private static final int INTERVAL = 20;

    private final Creature flier;
    private final FloatControl control;
    private int attackIn;

    public AirStrike(Creature flier, FloatControl control) {
        super(Control.MOVE, Control.LOOK);
        this.flier = flier;
        this.control = control;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = flier.target();
        return target != null && !target.isDead();
    }

    @Override
    public void stop() {
        control.stop();
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = flier.target();
        if (target == null) {
            return;
        }
        flier.gaze().lookAt(target, 30f, 30f);
        control.moveTo(target.getPosition().add(0, target.getBoundingBox().height() / 2, 0));
        attackIn = Math.max(attackIn - 1, 0);
        if (attackIn == 0 && Reach.melee(flier, target)) {
            attackIn = INTERVAL;
            flier.strike(target);
        }
    }
}

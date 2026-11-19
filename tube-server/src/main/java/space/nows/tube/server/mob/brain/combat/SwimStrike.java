package space.nows.tube.server.mob.brain.combat;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.Swim;

public final class SwimStrike extends Behavior {

    private static final int ATTACK_INTERVAL = 20;

    private final Creature swimmer;
    private final double speed;
    private int attackIn;

    public SwimStrike(Creature swimmer, double blocksPerTick) {
        super(Control.MOVE, Control.LOOK);
        this.swimmer = swimmer;
        this.speed = blocksPerTick;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = swimmer.target();
        return target != null && !target.isDead() && swimmer.inWater();
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = swimmer.target();
        if (target == null) {
            return;
        }
        swimmer.gaze().lookAt(target, 30f, 30f);
        Swim.steer(swimmer, target.getPosition().add(0, target.getBoundingBox().height() / 2, 0), speed);
        attackIn = Math.max(attackIn - 1, 0);
        if (attackIn == 0 && Reach.melee(swimmer, target) && swimmer.sight().canSee(target)) {
            attackIn = ATTACK_INTERVAL;
            swimmer.strike(target);
        }
    }
}

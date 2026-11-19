package space.nows.tube.server.mob.kind.monster.breeze;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.projectile.Fireball;
import space.nows.tube.server.mob.projectile.Gust;
import space.nows.tube.server.mob.sound.MobVoice;

final class Gale extends Behavior {

    private static final double MAX_RANGE = 24;
    private static final int SHOT_INTERVAL = 40;
    private static final int MIN_JUMP = 40;
    private static final int MAX_JUMP = 100;
    private static final double SHOT_SPEED = 0.7;
    private static final double JUMP_UP = 0.6 * 20;
    private static final double JUMP_SIDE = 0.35 * 20;

    private final Creature breeze;
    private int shootIn = SHOT_INTERVAL;
    private int jumpIn = MIN_JUMP;

    Gale(Creature breeze) {
        super(Control.MOVE, Control.LOOK, Control.JUMP);
        this.breeze = breeze;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = breeze.target();
        return target != null && !target.isDead() && breeze.getPosition().distanceSquared(target.getPosition()) < MAX_RANGE * MAX_RANGE;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = breeze.target();
        if (target == null) {
            return;
        }
        breeze.gaze().lookAt(target, 30f, 30f);
        if (--jumpIn <= 0 && breeze.isOnGround()) {
            jumpIn = MIN_JUMP + breeze.random().nextInt(MAX_JUMP - MIN_JUMP);
            double angle = breeze.random().nextDouble() * Math.PI * 2;
            breeze.setVelocity(new Vec(Math.cos(angle) * JUMP_SIDE, JUMP_UP, Math.sin(angle) * JUMP_SIDE));
            MobVoice.play(breeze, "jump");
        }
        if (--shootIn <= 0 && breeze.sight().canSee(target)) {
            shootIn = SHOT_INTERVAL;
            Pos from = breeze.getPosition().add(0, breeze.getBoundingBox().height() / 2 + 0.3, 0);
            Vec toward = target.getPosition().add(0, target.getBoundingBox().height() / 2, 0).sub(from).asVec().normalize();
            Fireball.steady(EntityType.BREEZE_WIND_CHARGE, breeze, toward.mul(SHOT_SPEED), new Gust()).setInstance(breeze.getInstance(), from);
            MobVoice.play(breeze, "shoot");
        }
    }
}

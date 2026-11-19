package space.nows.tube.server.mob.kind.monster.shulker;

import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.projectile.Bullet;
import space.nows.tube.server.mob.sound.MobVoice;

final class Barrage extends Behavior {

    private static final double RANGE = 20;
    private static final int MIN_DELAY = 20;
    private static final int RANDOM_DELAY = 10;

    private final Creature shulker;
    private int shootIn;

    Barrage(Creature shulker) {
        super(Control.LOOK);
        this.shulker = shulker;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = shulker.target();
        return target != null && !target.isDead() && shulker.getPosition().distanceSquared(target.getPosition()) < RANGE * RANGE;
    }

    @Override
    public void start() {
        shootIn = MIN_DELAY;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = shulker.target();
        if (target == null) {
            return;
        }
        shulker.gaze().lookAt(target, 180f, 180f);
        if (--shootIn > 0) {
            return;
        }
        shootIn = MIN_DELAY + shulker.random().nextInt(RANDOM_DELAY) * MIN_DELAY / 2;
        new Bullet(shulker, target).setInstance(shulker.getInstance(), shulker.getPosition().add(0, 0.5, 0));
        MobVoice.play(shulker, "shoot");
    }
}

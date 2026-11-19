package space.nows.tube.server.mob.kind.boss;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.entity.mob.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.move.FloatControl;
import space.nows.tube.server.mob.projectile.Fireball;
import space.nows.tube.server.mob.projectile.Withering;
import space.nows.tube.server.mob.sound.MobVoice;

final class SkullFire extends Behavior {

    private static final int INTERVAL = 40;
    private static final double HOVER = 5;
    private static final double KEEP_AWAY = 9;

    private final Creature wither;
    private final FloatControl control;
    private int shootIn = INTERVAL;

    SkullFire(Creature wither, FloatControl control) {
        super(Control.MOVE, Control.LOOK);
        this.wither = wither;
        this.control = control;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = wither.target();
        return target != null && !target.isDead();
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = wither.target();
        if (target == null) {
            return;
        }
        wither.gaze().lookAt(target, 30f, 30f);
        if (wither.getPosition().distanceSquared(target.getPosition()) > KEEP_AWAY) {
            control.moveTo(target.getPosition().add(0, HOVER, 0));
        }
        if (--shootIn > 0 || !wither.sight().canSee(target)) {
            return;
        }
        shootIn = INTERVAL;
        Pos from = wither.getPosition().add(0, wither.getBoundingBox().height() * 0.9, 0);
        Vec toward = target.getPosition().add(0, target.getBoundingBox().height() / 2, 0).sub(from).asVec();
        new Fireball(EntityType.WITHER_SKULL, wither, toward, new Withering()).setInstance(wither.getInstance(), from);
        MobVoice.play(wither, "shoot");
    }
}

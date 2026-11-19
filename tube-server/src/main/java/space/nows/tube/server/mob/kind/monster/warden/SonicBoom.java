package space.nows.tube.server.mob.kind.monster.warden;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.sound.MobVoice;

final class SonicBoom extends Behavior {

    private static final double MIN_RANGE = 5;
    private static final double MAX_RANGE = 15;
    private static final int CHARGE = 34;
    private static final int COOLDOWN = 40;
    private static final float DAMAGE = 10;
    private static final double KNOCK_UP = 0.5 * 20;
    private static final double KNOCK_BACK = 2.5 * 20;

    private final Creature warden;
    private int charging;
    private long readyAt;

    SonicBoom(Creature warden) {
        super(Control.MOVE, Control.LOOK);
        this.warden = warden;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = warden.target();
        if (target == null || warden.getAliveTicks() < readyAt) {
            return false;
        }
        double distance = warden.getPosition().distance(target.getPosition());
        return distance > MIN_RANGE && distance < MAX_RANGE && warden.sight().canSee(target);
    }

    @Override
    public boolean canContinue() {
        return warden.target() != null && charging < CHARGE;
    }

    @Override
    public void start() {
        charging = 0;
        warden.navigation().stop();
        MobVoice.play(warden, "sonic_charge");
    }

    @Override
    public void stop() {
        readyAt = warden.getAliveTicks() + COOLDOWN;
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = warden.target();
        if (target == null) {
            return;
        }
        warden.gaze().lookAt(target, 30f, 30f);
        if (++charging < CHARGE) {
            return;
        }
        MobVoice.play(warden, "sonic_boom");
        if (target.damage(new Damage(DamageType.SONIC_BOOM, warden, warden, warden.getPosition(), DAMAGE))) {
            Vec away = target.getPosition().sub(warden.getPosition()).asVec().withY(0).normalize();
            target.setVelocity(target.getVelocity().add(away.x() * KNOCK_BACK * 0.2, KNOCK_UP, away.z() * KNOCK_BACK * 0.2));
        }
    }
}

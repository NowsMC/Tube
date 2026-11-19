package space.nows.tube.server.mob.kind.monster.ghast;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.flying.GhastMeta;
import space.nows.tube.api.entity.brain.Behavior;
import space.nows.tube.api.entity.brain.Control;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.projectile.Blast;
import space.nows.tube.server.mob.projectile.Fireball;
import space.nows.tube.server.world.effect.SoundPlayer;

final class Volley extends Behavior {

    private static final double MAX_RANGE = 4096;
    private static final int WARN_AT = 10;
    private static final int FIRE_AT = 20;
    private static final int COOLDOWN = -40;
    private static final double MUZZLE = 4;
    private static final double MAX_LOOK = 4096;

    private final Creature ghast;
    private final float power;
    private int charge;

    Volley(Creature ghast, float power) {
        super(Control.LOOK);
        this.ghast = ghast;
        this.power = power;
    }

    @Override
    public boolean canStart() {
        return true;
    }

    @Override
    public void start() {
        charge = 0;
    }

    @Override
    public void stop() {
        attacking(false);
    }

    @Override
    public boolean everyTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = ghast.target();
        if (target == null) {
            Vec motion = ghast.getVelocity();
            if (motion.x() * motion.x() + motion.z() * motion.z() > 1.0E-6) {
                ghast.setView((float) Math.toDegrees(-Math.atan2(motion.x(), motion.z())), ghast.getPosition().pitch());
            }
            return;
        }
        if (target.getPosition().distanceSquared(ghast.getPosition()) < MAX_LOOK) {
            double dx = target.getPosition().x() - ghast.getPosition().x();
            double dz = target.getPosition().z() - ghast.getPosition().z();
            ghast.setView((float) Math.toDegrees(-Math.atan2(dx, dz)), ghast.getPosition().pitch());
        }
        if (target.getPosition().distanceSquared(ghast.getPosition()) < MAX_RANGE && ghast.sight().canSee(target)) {
            charge++;
            if (charge == WARN_AT) {
                SoundPlayer.play(ghast.getInstance(), "minecraft:entity.ghast.warn", Sound.Source.HOSTILE, ghast.getPosition(), 10f, pitch());
            }
            if (charge == FIRE_AT) {
                fire(target);
                charge = COOLDOWN;
            }
        } else if (charge > 0) {
            charge--;
        }
        attacking(charge > WARN_AT);
    }

    private void fire(LivingEntity target) {
        Vec view = ghast.getPosition().direction();
        double middle = ghast.getBoundingBox().height() / 2;
        Pos from = ghast.getPosition().add(view.x() * MUZZLE, middle + 0.5, view.z() * MUZZLE);
        Vec direction = new Vec(target.getPosition().x() - from.x(),
                target.getPosition().y() + target.getBoundingBox().height() / 2 - (0.5 + ghast.getPosition().y() + middle),
                target.getPosition().z() - from.z());
        new Fireball(EntityType.FIREBALL, ghast, direction, new Blast(power)).setInstance(ghast.getInstance(), from);
        SoundPlayer.play(ghast.getInstance(), "minecraft:entity.ghast.shoot", Sound.Source.HOSTILE, ghast.getPosition(), 10f, pitch());
    }

    private float pitch() {
        return (ghast.random().nextFloat() - ghast.random().nextFloat()) * 0.2f + 1f;
    }

    private void attacking(boolean attacking) {
        if (ghast.getEntityMeta() instanceof GhastMeta meta) {
            meta.setAttacking(attacking);
        }
    }
}

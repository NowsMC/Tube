package space.nows.tube.server.mob.kind.monster.witch;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.combat.Volley;
import space.nows.tube.server.mob.kind.monster.Raiders;
import space.nows.tube.server.mob.projectile.Brew;
import space.nows.tube.server.mob.projectile.SplashPotion;
import space.nows.tube.server.world.effect.Pitch;
import space.nows.tube.server.world.effect.SoundPlayer;

final class PotionThrow implements Volley {

    private static final double SPREAD = 0.0075 * 8;
    private static final double TICKS_PER_SECOND = 20;
    private static final double AIM_DROP = 1.1;

    private final Sipping sipping;

    PotionThrow(Sipping sipping) {
        this.sipping = sipping;
    }

    @Override
    public void fire(Creature witch, LivingEntity target, float power) {
        if (sipping.drinking()) {
            return;
        }
        Vec drift = target.getVelocity().div(TICKS_PER_SECOND);
        double dx = target.getPosition().x() + drift.x() - witch.getPosition().x();
        double dy = target.getPosition().y() + target.getEyeHeight() - AIM_DROP - witch.getPosition().y();
        double dz = target.getPosition().z() + drift.z() - witch.getPosition().z();
        double distance = Math.sqrt(dx * dx + dz * dz);
        Brew brew = choose(witch, target, distance);
        double speed = distance <= 2.0 ? 0.45 : 0.75;
        Vec heading = new Vec(dx, dy + distance * 0.2, dz).normalize();
        Vec scattered = new Vec(heading.x() + witch.random().nextGaussian() * SPREAD, heading.y() + witch.random().nextGaussian() * SPREAD,
                heading.z() + witch.random().nextGaussian() * SPREAD).mul(speed);
        Pos from = witch.getPosition().add(0, witch.getEyeHeight() - 0.1, 0);
        new SplashPotion(witch, brew, scattered).setInstance(witch.getInstance(), from);
        SoundPlayer.play(witch, "minecraft:entity.witch.throw", Sound.Source.HOSTILE, 1f,
                Pitch.between(witch.random(), 0.8f, 1.2f));
    }

    private static Brew choose(Creature witch, LivingEntity target, double distance) {
        if (Raiders.is(target)) {
            witch.setTarget(null);
            return target.getHealth() <= 4 ? Brew.HEALING : Brew.REGENERATION;
        }
        if (distance >= 8 && !Brew.SLOWNESS.active(target)) {
            return Brew.SLOWNESS;
        }
        if (target.getHealth() >= 8 && !Brew.POISON.active(target)) {
            return Brew.POISON;
        }
        if (distance <= 3 && !Brew.WEAKNESS.active(target) && witch.random().nextFloat() < 0.25f) {
            return Brew.WEAKNESS;
        }
        return Brew.HARMING;
    }
}

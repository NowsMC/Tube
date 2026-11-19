package space.nows.tube.server.mob.projectile;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import space.nows.tube.server.world.effect.SoundPlayer;

public final class Gust implements Impact {

    private static final float DAMAGE = 1;
    private static final double RADIUS = 1.2;
    private static final double PUSH = 1.1 * 20;

    @Override
    public void hitEntity(Fireball fireball, LivingEntity target) {
        target.damage(new Damage(DamageType.WIND_CHARGE, fireball, fireball.owner(), fireball.getPosition(), DAMAGE));
        burst(fireball, fireball.getPosition());
    }

    @Override
    public void hitBlock(Fireball fireball, Point block, Point before) {
        burst(fireball, before);
    }

    private void burst(Fireball fireball, Point at) {
        for (Entity entity : fireball.getInstance().getNearbyEntities(at, RADIUS * 2)) {
            if (!(entity instanceof LivingEntity living) || living.isDead()) {
                continue;
            }
            Vec away = entity.getPosition().add(0, entity.getBoundingBox().height() / 2, 0).sub(at).asVec();
            double distance = away.length();
            if (distance > RADIUS * 2 || distance < 1.0E-6) {
                continue;
            }
            double strength = 1 - distance / (RADIUS * 2);
            entity.setVelocity(entity.getVelocity().add(away.normalize().mul(PUSH * strength)));
        }
        SoundPlayer.play(fireball.getInstance(), "minecraft:entity.wind_charge.wind_burst", Sound.Source.HOSTILE, at, 1f, 1f);
    }
}

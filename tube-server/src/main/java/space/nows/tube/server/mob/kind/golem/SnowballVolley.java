package space.nows.tube.server.mob.kind.golem;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.server.gameplay.SweptProjectile;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.combat.Volley;
import space.nows.tube.server.world.effect.SoundPlayer;

final class SnowballVolley implements Volley {

    private static final double SPEED = 1.6;
    private static final double SPREAD = 12;
    private static final double LIFT_PER_REACH = 0.2;
    private static final double AIM_DROP = 1.1;

    @Override
    public void fire(Creature golem, LivingEntity target, float power) {
        Pos from = golem.getPosition().add(0, golem.getEyeHeight() - 0.1, 0);
        double dx = target.getPosition().x() - golem.getPosition().x();
        double dz = target.getPosition().z() - golem.getPosition().z();
        double dy = target.getPosition().y() + target.getEyeHeight() - AIM_DROP - from.y();
        new SweptProjectile(golem, EntityType.SNOWBALL)
                .launch(golem.getInstance(), from, new Vec(dx, dy + Math.hypot(dx, dz) * LIFT_PER_REACH, dz), SPEED, SPREAD);
        float pitch = 0.4f / (golem.random().nextFloat() * 0.4f + 0.8f);
        SoundPlayer.play(golem.getInstance(), "minecraft:entity.snow_golem.shoot", Sound.Source.NEUTRAL, golem.getPosition(), 1f, pitch);
    }
}

package space.nows.tube.server.mob.brain.combat;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.server.gameplay.Archery;
import space.nows.tube.server.gameplay.SweptArrow;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.world.effect.SoundPlayer;

public final class CrossbowShot implements Volley {

    private static final double SPEED = 1.6;
    private static final double BASE_DAMAGE = 2.0;
    private static final double LIFT_PER_REACH = 0.2;
    private static final int BASE_INACCURACY = 14;
    private static final int INACCURACY_PER_DIFFICULTY = 4;

    @Override
    public void fire(Creature shooter, LivingEntity target, float power) {
        int difficulty = MinecraftServer.getDifficulty().ordinal();
        Pos from = shooter.getPosition().add(0, shooter.getEyeHeight() - 0.1, 0);
        Pos aim = target.getPosition().add(0, target.getBoundingBox().height() / 3.0, 0);
        double reach = Math.hypot(aim.x() - from.x(), aim.z() - from.z());
        SweptArrow arrow = new SweptArrow(shooter);
        arrow.setTag(Archery.ARROW_DAMAGE, Math.ceil(SPEED * BASE_DAMAGE));
        arrow.launch(shooter.getInstance(), from, new Vec(aim.x() - from.x(), aim.y() - from.y() + reach * LIFT_PER_REACH, aim.z() - from.z()),
                SPEED, BASE_INACCURACY - difficulty * INACCURACY_PER_DIFFICULTY);
        float pitch = 1f / (shooter.random().nextFloat() * 0.5f + 1.8f) + 0.53f;
        SoundPlayer.play(shooter, "minecraft:item.crossbow.shoot", Sound.Source.HOSTILE, 1f, pitch);
    }
}

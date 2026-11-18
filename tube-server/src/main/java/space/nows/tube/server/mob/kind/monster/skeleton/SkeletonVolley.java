package space.nows.tube.server.mob.kind.monster.skeleton;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.server.gameplay.Archery;
import space.nows.tube.server.gameplay.SweptArrow;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.ai.combat.Volley;
import space.nows.tube.server.world.effect.SoundPlayer;

final class SkeletonVolley implements Volley {

    private static final double ARROW_SPEED = 1.6;
    private static final double LIFT_PER_REACH = 0.2;
    private static final double BASE_DAMAGE = 2.0;
    private static final double DIFFICULTY_DAMAGE = 0.11;
    private static final double DAMAGE_SPREAD = 0.57425;
    private static final int BASE_INACCURACY = 14;
    private static final int INACCURACY_PER_DIFFICULTY = 4;

    @Override
    public void fire(Creature shooter, LivingEntity target, float power) {
        int difficulty = MinecraftServer.getDifficulty().ordinal();
        Pos from = shooter.getPosition().add(0, shooter.getEyeHeight() - 0.1, 0);
        Pos aim = target.getPosition().add(0, target.getBoundingBox().height() / 3.0, 0);
        double base = power * BASE_DAMAGE + triangle(shooter, DIFFICULTY_DAMAGE * difficulty, DAMAGE_SPREAD);
        SweptArrow arrow = new SweptArrow(shooter);
        arrow.setTag(Archery.ARROW_DAMAGE, Math.ceil(ARROW_SPEED * base));
        double inaccuracy = BASE_INACCURACY - difficulty * INACCURACY_PER_DIFFICULTY;
        double reach = Math.hypot(aim.x() - from.x(), aim.z() - from.z());
        arrow.launch(shooter.getInstance(), from, new Vec(aim.x() - from.x(), aim.y() - from.y() + reach * LIFT_PER_REACH, aim.z() - from.z()), ARROW_SPEED, inaccuracy);
        float pitch = 1f / (shooter.random().nextFloat() * 0.4f + 0.8f);
        SoundPlayer.play(shooter.getInstance(), "minecraft:entity.skeleton.shoot", Sound.Source.HOSTILE, shooter.getPosition(), 1f, pitch);
    }

    private static double triangle(Creature shooter, double mode, double deviation) {
        return mode + deviation * (shooter.random().nextDouble() - shooter.random().nextDouble());
    }
}

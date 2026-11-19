package space.nows.tube.server.mob.kind.animal.llama;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import space.nows.tube.api.entity.brain.MemoryKey;
import space.nows.tube.server.gameplay.SweptProjectile;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.brain.combat.Volley;
import space.nows.tube.server.world.effect.SoundPlayer;

final class Spit implements Volley {

    static final MemoryKey<Boolean> SPAT = MemoryKey.of("tube:spat", Boolean.class);

    private static final double SPEED = 1.5;
    private static final double SPREAD = 10;
    private static final double LIFT_PER_REACH = 0.2;

    @Override
    public void fire(Creature llama, LivingEntity target, float power) {
        SweptProjectile spit = new SweptProjectile(llama, EntityType.LLAMA_SPIT);
        double width = llama.getBoundingBox().width();
        double yaw = Math.toRadians(llama.getPosition().yaw());
        Pos from = llama.getPosition().add(-Math.sin(yaw) * width * 0.5, llama.getEyeHeight() - 0.1, Math.cos(yaw) * width * 0.5);
        double dx = target.getPosition().x() - llama.getPosition().x();
        double dz = target.getPosition().z() - llama.getPosition().z();
        double dy = target.getPosition().y() + target.getBoundingBox().height() / 3.0 - from.y();
        spit.launch(llama.getInstance(), from, new Vec(dx, dy + Math.hypot(dx, dz) * LIFT_PER_REACH, dz), SPEED, SPREAD);
        float pitch = 1f + (llama.random().nextFloat() - llama.random().nextFloat()) * 0.2f;
        SoundPlayer.play(llama.getInstance(), "minecraft:entity.llama.spit", Sound.Source.NEUTRAL, llama.getPosition(), 1f, pitch);
        llama.brain().remember(SPAT, true);
    }
}

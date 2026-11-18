package space.nows.tube.server.gameplay.mob;

import net.minestom.server.instance.Instance;
import space.nows.tube.server.world.effect.ParticleSpawner;

final class DeathEffect {

    private static final String PUFF = "poof";
    private static final int PUFF_COUNT = 20;

    private DeathEffect() {
    }

    static void play(Mob mob) {
        MobVoice.play(mob, "death");
        Instance instance = mob.getInstance();
        if (instance != null) {
            ParticleSpawner.spawn(instance, PUFF, mob.getPosition().add(0, mob.getBoundingBox().height() / 2, 0), PUFF_COUNT, 0.3, 0.02);
        }
    }
}

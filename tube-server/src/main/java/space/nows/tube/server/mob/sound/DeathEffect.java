package space.nows.tube.server.mob.sound;

import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.world.effect.ParticleSpawner;

public final class DeathEffect {

    private static final String PUFF = "poof";
    private static final int PUFF_COUNT = 20;

    private DeathEffect() {
    }

    public static void play(Creature creature) {
        MobVoice.play(creature, "death");
        Instance instance = creature.getInstance();
        if (instance != null) {
            ParticleSpawner.spawn(instance, PUFF, creature.getPosition().add(0, creature.getBoundingBox().height() / 2, 0), PUFF_COUNT, 0.3, 0.02);
        }
    }
}

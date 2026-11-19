package space.nows.tube.server.mob.sound;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.world.effect.SoundPlayer;

public final class MobVoice {

    private static final float BABY_PITCH_BONUS = 0.5f;

    private MobVoice() {
    }

    public static String key(Creature creature, String event) {
        return "minecraft:entity." + creature.kind().voice() + "." + event;
    }

    public static void play(Creature creature, String event) {
        Instance instance = creature.getInstance();
        if (instance != null) {
            SoundPlayer.play(instance, key(creature, event), source(creature), creature.getPosition(), 1f, pitch(creature));
        }
    }

    private static Sound.Source source(Creature creature) {
        return creature.kind().disposition().hostile() ? Sound.Source.HOSTILE : Sound.Source.NEUTRAL;
    }

    private static float pitch(Creature creature) {
        float pitch = (creature.random().nextFloat() - creature.random().nextFloat()) * 0.2f + 1f;
        return creature.isBaby() ? pitch + BABY_PITCH_BONUS : pitch;
    }
}

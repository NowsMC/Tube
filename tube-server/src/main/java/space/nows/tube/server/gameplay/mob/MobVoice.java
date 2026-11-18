package space.nows.tube.server.gameplay.mob;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.world.effect.SoundPlayer;

import java.util.concurrent.ThreadLocalRandom;

final class MobVoice {

    private MobVoice() {
    }

    static String key(Mob mob, String event) {
        return "minecraft:entity." + mob.kind().type.key().value() + "." + event;
    }

    static void play(Mob mob, String event) {
        Instance instance = mob.getInstance();
        if (instance != null) {
            SoundPlayer.play(instance, key(mob, event), source(mob), mob.getPosition(), 1f, pitch());
        }
    }

    private static Sound.Source source(Mob mob) {
        return mob.kind().hostile() ? Sound.Source.HOSTILE : Sound.Source.NEUTRAL;
    }

    private static float pitch() {
        return 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f;
    }
}

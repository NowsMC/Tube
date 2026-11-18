package space.nows.tube.server.world.effect;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;

public final class SoundPlayer {

    private SoundPlayer() {
    }

    public static void play(Instance instance, Sound sound, double x, double y, double z) {
        instance.playSound(sound, x, y, z);
    }

    public static void play(Instance instance, String key, Sound.Source source, Point at, float volume, float pitch) {
        play(instance, Sound.sound(Key.key(key), source, volume, pitch), at.x(), at.y(), at.z());
    }
}

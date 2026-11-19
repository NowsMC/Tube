package space.nows.tube.server.world.effect;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;

public final class SoundPlayer {

    private SoundPlayer() {
    }

    public static void play(Instance instance, Sound sound, double x, double y, double z) {
        if (instance != null) {
            instance.playSound(sound, x, y, z);
        }
    }

    public static void play(Instance instance, Sound sound, Point at) {
        play(instance, sound, at.x(), at.y(), at.z());
    }

    public static void play(Instance instance, String key, Sound.Source source, Point at, float volume, float pitch) {
        play(instance, Sound.sound(Key.key(key), source, volume, pitch), at);
    }

    public static void play(Instance instance, SoundEvent event, Sound.Source source, Point at, float volume, float pitch) {
        play(instance, Sound.sound(event, source, volume, pitch), at);
    }

    public static void play(Entity entity, String key, Sound.Source source, float volume, float pitch) {
        play(entity.getInstance(), key, source, entity.getPosition(), volume, pitch);
    }

    public static void playExcept(Instance instance, Player except, Sound sound, Point at) {
        for (Player viewer : instance.getPlayers()) {
            if (viewer != except) {
                viewer.playSound(sound, at.x(), at.y(), at.z());
            }
        }
    }

    public static void playAround(Entity entity, Sound sound) {
        if (entity.getInstance() == null) {
            return;
        }
        entity.getViewersAsAudience().playSound(sound, entity);
        if (entity instanceof Player player) {
            player.playSound(sound);
        }
    }
}

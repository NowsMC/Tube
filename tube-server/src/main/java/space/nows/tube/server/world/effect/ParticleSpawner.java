package space.nows.tube.server.world.effect;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;

public final class ParticleSpawner {

    private ParticleSpawner() {
    }

    public static boolean spawn(Instance instance, String key, double x, double y, double z, int count, double spreadX, double spreadY, double spreadZ, double speed) {
        Particle particle = Particle.fromKey(key.contains(":") ? key : "minecraft:" + key);
        if (particle == null) {
            return false;
        }
        spawn(instance, particle, x, y, z, count, spreadX, spreadY, spreadZ, speed);
        return true;
    }

    public static boolean spawn(Instance instance, String key, Point at, int count, double spread, double speed) {
        return spawn(instance, key, at.x(), at.y(), at.z(), count, spread, spread, spread, speed);
    }

    public static void spawn(Instance instance, Particle particle, Point at, int count, double spread, double speed) {
        spawn(instance, particle, at.x(), at.y(), at.z(), count, spread, spread, spread, speed);
    }

    private static void spawn(Instance instance, Particle particle, double x, double y, double z, int count, double spreadX, double spreadY, double spreadZ, double speed) {
        if (instance != null) {
            instance.sendGroupedPacket(new ParticlePacket(particle, x, y, z, (float) spreadX, (float) spreadY, (float) spreadZ, (float) speed, count));
        }
    }
}

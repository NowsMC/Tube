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
        instance.sendGroupedPacket(new ParticlePacket(particle, x, y, z, (float) spreadX, (float) spreadY, (float) spreadZ, (float) speed, count));
        return true;
    }

    public static boolean spawn(Instance instance, String key, Point at, int count, double spread, double speed) {
        return spawn(instance, key, at.x(), at.y(), at.z(), count, spread, spread, spread, speed);
    }
}

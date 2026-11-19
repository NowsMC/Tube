package space.nows.tube.server.world.effect;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.WorldEventPacket;

public enum WorldEffect {
    BLOCK_BREAK(2001),
    SPLASH_POTION(2002),
    INSTANT_SPLASH_POTION(2007);

    private final int id;

    WorldEffect(int id) {
        this.id = id;
    }

    public void play(Instance instance, Point at, int data) {
        if (instance != null) {
            instance.sendGroupedPacket(new WorldEventPacket(id, at, data, false));
        }
    }
}

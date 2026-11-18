package space.nows.tube.server.mob;

import net.minestom.server.event.GlobalEventHandler;
import space.nows.tube.server.mob.interact.Interactions;
import space.nows.tube.server.mob.sound.HurtSound;
import space.nows.tube.server.mob.spawn.NaturalSpawning;

public final class Mobs {

    private Mobs() {
    }

    public static void install(GlobalEventHandler handler) {
        HurtSound.install(handler);
        Interactions.install(handler);
        NaturalSpawning.start();
    }
}

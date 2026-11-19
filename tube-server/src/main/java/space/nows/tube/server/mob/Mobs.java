package space.nows.tube.server.mob;

import net.minestom.server.event.GlobalEventHandler;
import space.nows.tube.server.mob.interact.Interactions;
import space.nows.tube.server.mob.kind.animal.llama.SpitHits;
import space.nows.tube.server.mob.kind.monster.silverfish.InfestedBreak;
import space.nows.tube.server.mob.sound.HurtSound;
import space.nows.tube.server.mob.spawn.NaturalSpawning;

public final class Mobs {

    private Mobs() {
    }

    public static void install(GlobalEventHandler handler) {
        HurtSound.install(handler);
        Interactions.install(handler);
        InfestedBreak.install(handler);
        SpitHits.install(handler);
        NaturalSpawning.start();
    }
}

package space.nows.tube.server.mob.interact;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import space.nows.tube.server.mob.Creature;

public final class Interactions {

    private Interactions() {
    }

    public static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerEntityInteractEvent.class, event -> {
            if (event.getHand() == PlayerHand.MAIN && event.getTarget() instanceof Creature creature) {
                boolean handled = Feeding.apply(event.getPlayer(), creature)
                        || Shearing.apply(event.getPlayer(), creature)
                        || Milking.apply(event.getPlayer(), creature);
                if (handled) {
                    event.getPlayer().swingMainHand();
                }
            }
        });
    }
}

package space.nows.tube.server.gameplay.melee;

import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.tag.Tag;

public final class Pace {

    private static final Tag<Double> STEP = Tag.Transient("tube:step_squared");

    private Pace() {
    }

    public static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerMoveEvent.class, event -> {
            double dx = event.getNewPosition().x() - event.getPlayer().getPosition().x();
            double dz = event.getNewPosition().z() - event.getPlayer().getPosition().z();
            event.getPlayer().setTag(STEP, dx * dx + dz * dz);
        });
    }

    public static double stepSquared(Player player) {
        Double step = player.getTag(STEP);
        return step == null ? 0 : step;
    }
}

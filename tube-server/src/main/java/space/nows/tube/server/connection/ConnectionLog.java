package space.nows.tube.server.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.player.PlayerJoinEvent;
import space.nows.tube.api.event.player.PlayerQuitEvent;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.access.AccessControl;

import java.util.Locale;

public final class ConnectionLog {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private ConnectionLog() {
    }

    public static void install(EventBus events, boolean logIps) {
        events.subscribe(PlayerJoinEvent.class, EventPriority.MONITOR, false, event -> {
            String where = logIps ? event.player().address().map(address -> "/" + AccessControl.host(address) + ":" + address.getPort()).orElse("local") : "local";
            Location at = event.player().location();
            LOGGER.info("{}[{}] logged in at ([{}] {}, {}, {})", event.player().name(), where, at.world().name(),
                    round(at.x()), round(at.y()), round(at.z()));
            if (event.joinMessage() != null) {
                LOGGER.info("{} joined the game", event.player().name());
            }
        });
        events.subscribe(PlayerQuitEvent.class, EventPriority.MONITOR, false, event -> {
            if (event.quitMessage() != null) {
                LOGGER.info("{} left the game", event.player().name());
            }
        });
    }

    private static String round(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}

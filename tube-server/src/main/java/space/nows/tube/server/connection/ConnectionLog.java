package space.nows.tube.server.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.PlayerJoinEvent;
import space.nows.tube.api.event.PlayerQuitEvent;
import space.nows.tube.server.access.AccessControl;

public final class ConnectionLog {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private ConnectionLog() {
    }

    public static void install(EventBus events, boolean logIps) {
        events.subscribe(PlayerJoinEvent.class, EventPriority.MONITOR, false, event -> {
            String where = logIps ? event.player().address().map(address -> "/" + AccessControl.host(address)).orElse("local") : "local";
            LOGGER.info("{}[{}] logged in", event.player().name(), where);
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
}

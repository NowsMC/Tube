package space.nows.tube.server.connection;

import net.kyori.adventure.resource.ResourcePackStatus;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerResourcePackStatusEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.entity.player.ResourcePack;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.player.PlayerResourcePackEvent;
import space.nows.tube.server.config.Markup;
import space.nows.tube.server.config.ServerSettings;
import space.nows.tube.server.player.PlayerService;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public final class ResourcePackHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private final ServerSettings.ResourcePack settings;
    private final EventBus events;
    private final PlayerService players;
    private final ResourcePack pack;

    public ResourcePackHandler(ServerSettings.ResourcePack settings, EventBus events, PlayerService players) {
        this.settings = settings;
        this.events = events;
        this.players = players;
        this.pack = createPack(settings);
    }

    public Optional<ResourcePack> pack() {
        return Optional.ofNullable(pack);
    }

    public void install(GlobalEventHandler handler) {
        handler.addListener(PlayerResourcePackStatusEvent.class, event -> {
            var status = event.getStatus();
            events.post(new PlayerResourcePackEvent(players.wrap(event.getPlayer()), event.getPackUuid(), status));
            boolean failed = !status.intermediate() && status != ResourcePackStatus.SUCCESSFULLY_LOADED;
            if (settings.required() && failed) {
                event.getPlayer().kick(Component.text("This server requires its resource pack"));
            }
        });
    }

    private static ResourcePack createPack(ServerSettings.ResourcePack settings) {
        if (settings.url().isEmpty()) {
            return null;
        }
        try {
            URI.create(settings.url());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("server.properties: resource-pack is not a valid URL, no resource pack will be sent");
            return null;
        }
        UUID id = settings.id().isEmpty()
                ? UUID.nameUUIDFromBytes(settings.url().getBytes(StandardCharsets.UTF_8))
                : UUID.fromString(settings.id());
        Component prompt = settings.prompt().isEmpty() ? null : Markup.parse(settings.prompt());
        return new ResourcePack(id, settings.url(), settings.sha1(), settings.required(), prompt);
    }
}

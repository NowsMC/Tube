package space.nows.tube.server.connection;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.Status;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.server.config.Markup;
import space.nows.tube.server.config.ServerSettings;

public final class StatusResponder {

    private final ServerSettings settings;
    private final EventBus events;
    private final String versionName;
    private final Component motd;

    public StatusResponder(ServerSettings settings, EventBus events, String versionName) {
        this.settings = settings;
        this.events = events;
        this.versionName = versionName;
        this.motd = Markup.parse(settings.status().motd());
    }

    public void install(GlobalEventHandler handler) {
        handler.addListener(ServerListPingEvent.class, this::respond);
    }

    private void respond(ServerListPingEvent event) {
        if (!settings.status().enabled()) {
            event.setCancelled(true);
            return;
        }
        Status status = event.getStatus();
        int online = MinecraftServer.getConnectionManager().getOnlinePlayers().size();
        var ping = events.post(new space.nows.tube.api.event.server.ServerListPingEvent(
                event.getConnection() == null ? null : event.getConnection().getRemoteAddress(),
                status.versionInfo().protocolVersion(), motd, online, settings.connection().maxPlayers(), settings.status().hideOnlinePlayers()));
        if (ping.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        Status.PlayerInfo playerInfo = ping.hidePlayers()
                ? new Status.PlayerInfo(ping.online(), ping.maxPlayers())
                : Status.PlayerInfo.builder(Status.PlayerInfo.online(12)).maxPlayers(ping.maxPlayers()).build();
        event.setStatus(Status.builder(status)
                .description(ping.motd())
                .playerInfo(playerInfo)
                .versionInfo(new Status.VersionInfo(versionName, status.versionInfo().protocolVersion()))
                .build());
    }
}

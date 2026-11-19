package space.nows.tube.server.connection;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.listener.preplay.LoginListener;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.player.PlayerSocketConnection;
import net.minestom.server.utils.StringUtils;
import space.nows.tube.api.entity.player.LoginMode;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.player.PlayerLoginStartEvent;
import space.nows.tube.api.event.player.PlayerPreLoginEvent;
import space.nows.tube.server.player.PlayerService;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class LoginGate {

    private final boolean onlineMode;
    private final EventBus events;
    private final PlayerService players;
    private final Map<PlayerConnection, LoginMode> modes = Collections.synchronizedMap(new WeakHashMap<>());

    public LoginGate(boolean onlineMode, EventBus events, PlayerService players) {
        this.onlineMode = onlineMode;
        this.events = events;
        this.players = players;
    }

    public void install(GlobalEventHandler handler) {
        MinecraftServer.getPacketListenerManager().setListener(ConnectionState.LOGIN, ClientLoginStartPacket.class, this::loginStart);
        handler.addListener(AsyncPlayerPreLoginEvent.class, this::preLogin);
    }

    public static UUID offlineUuid(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    void loginStart(ClientLoginStartPacket packet, PlayerConnection connection) {
        if (!StringUtils.isValidUsername(packet.username())) {
            LoginListener.loginStartListener(packet, connection);
            return;
        }
        LoginMode defaultMode = onlineMode ? LoginMode.ONLINE : LoginMode.OFFLINE;
        var posted = events.post(new PlayerLoginStartEvent(packet.username(), packet.profileId(), connection.getRemoteAddress(), defaultMode));
        if (posted.denyReason().isPresent()) {
            connection.kick(posted.denyReason().get());
            return;
        }
        LoginMode mode = onlineMode ? posted.mode() : LoginMode.OFFLINE;
        modes.put(connection, mode);
        if (mode == LoginMode.ONLINE || !onlineMode) {
            LoginListener.loginStartListener(packet, connection);
            return;
        }
        loginOffline(packet, connection);
    }

    private static void loginOffline(ClientLoginStartPacket packet, PlayerConnection connection) {
        if (MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(packet.username()) != null) {
            connection.kick(Component.text("You are already connected to this server"));
            return;
        }
        if (connection instanceof PlayerSocketConnection socket) {
            socket.UNSAFE_setLoginUsername(packet.username());
        }
        GameProfile profile = new GameProfile(offlineUuid(packet.username()), packet.username());
        Thread.startVirtualThread(() -> {
            try {
                MinecraftServer.getConnectionManager().transitionLoginToConfig(connection, profile);
            } catch (Throwable e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        });
    }

    void preLogin(AsyncPlayerPreLoginEvent event) {
        LoginMode mode = modes.getOrDefault(event.getConnection(), onlineMode ? LoginMode.ONLINE : LoginMode.OFFLINE);
        var posted = events.post(new PlayerPreLoginEvent(event.getUsername(), event.getPlayerUuid(), event.getConnection().getRemoteAddress(), mode));
        if (posted.denyReason().isPresent()) {
            event.getConnection().kick(posted.denyReason().get());
            return;
        }
        players.rememberLoginMode(event.getPlayerUuid(), mode);
    }
}

package space.nows.tube.server.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.client.login.ClientLoginStartPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.login.LoginDisconnectPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.network.plugin.LoginPluginMessageProcessor;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.entity.LoginMode;
import space.nows.tube.api.event.PlayerLoginStartEvent;
import space.nows.tube.api.event.PlayerPreLoginEvent;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.event.DefaultEventBus;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.WorldService;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EnvTest
class LoginGateTest {

    @TempDir
    Path directory;

    private final DefaultEventBus bus = new DefaultEventBus();

    private PlayerService players() {
        return new PlayerService(new WorldService(directory, "main"), new PermissionService(directory));
    }

    @Test
    void deniedLoginsAreKickedBeforeAuthentication(Env env) {
        LoginGate gate = new LoginGate(true, bus, players());
        bus.subscribe(PlayerLoginStartEvent.class, event -> event.deny(Component.text("No")));
        RecordingConnection connection = new RecordingConnection();

        gate.loginStart(new ClientLoginStartPacket("Steve", UUID.randomUUID()), connection);

        assertTrue(connection.sent.stream().anyMatch(LoginDisconnectPacket.class::isInstance));
    }

    @Test
    void pluginsCanLetAClientInOfflineOnAnOnlineServer(Env env) {
        PlayerService players = players();
        LoginGate gate = new LoginGate(true, bus, players);
        List<LoginMode> seen = new ArrayList<>();
        bus.subscribe(PlayerLoginStartEvent.class, event -> {
            seen.add(event.mode());
            event.setMode(LoginMode.OFFLINE);
        });
        bus.subscribe(PlayerPreLoginEvent.class, event -> seen.add(event.mode()));
        RecordingConnection connection = new RecordingConnection();

        gate.loginStart(new ClientLoginStartPacket("Guest", UUID.randomUUID()), connection);
        UUID uuid = LoginGate.offlineUuid("Guest");
        gate.preLogin(new AsyncPlayerPreLoginEvent(connection, new GameProfile(uuid, "Guest"), new LoginPluginMessageProcessor(connection)));

        assertEquals(List.of(LoginMode.ONLINE, LoginMode.OFFLINE), seen);
        assertEquals(LoginMode.OFFLINE, players.loginMode(uuid));
    }

    @Test
    void offlineServersNeverAuthenticate(Env env) {
        PlayerService players = players();
        LoginGate gate = new LoginGate(false, bus, players);
        bus.subscribe(PlayerLoginStartEvent.class, event -> event.setMode(LoginMode.ONLINE));
        RecordingConnection connection = new RecordingConnection();
        UUID uuid = UUID.randomUUID();

        gate.loginStart(new ClientLoginStartPacket("Alex", uuid), connection);
        gate.preLogin(new AsyncPlayerPreLoginEvent(connection, new GameProfile(uuid, "Alex"), new LoginPluginMessageProcessor(connection)));

        assertEquals(LoginMode.OFFLINE, players.loginMode(uuid));
    }

    @Test
    void offlineUuidsFollowTheVanillaScheme() {
        assertEquals(UUID.nameUUIDFromBytes("OfflinePlayer:Notch".getBytes(java.nio.charset.StandardCharsets.UTF_8)), LoginGate.offlineUuid("Notch"));
    }

    private static final class RecordingConnection extends PlayerConnection {

        private final List<SendablePacket> sent = new ArrayList<>();

        private RecordingConnection() {
            setClientState(ConnectionState.LOGIN);
            setServerState(ConnectionState.LOGIN);
        }

        @Override
        public void sendPacket(SendablePacket packet) {
            sent.add(packet);
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return new InetSocketAddress("127.0.0.1", 50000);
        }
    }
}

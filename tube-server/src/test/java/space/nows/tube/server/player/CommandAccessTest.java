package space.nows.tube.server.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import net.minestom.testing.TestConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.server.access.PermissionService;

import java.nio.file.Path;
import java.util.UUID;

@EnvTest
class CommandAccessTest {

    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-0000000000b2");

    @TempDir
    Path directory;

    @Test
    void anOperatorGetsTheirCommandsWithoutRejoining(Env env) {
        PermissionService permissions = new PermissionService(directory);
        new CommandAccess(permissions).install(MinecraftServer.getGlobalEventHandler());
        TestConnection connection = env.createConnection(new GameProfile(BOB, "Bob"));
        Player player = connection.connect(env.createFlatInstance(), new Pos(0, 42, 0));
        var declared = connection.trackIncoming(DeclareCommandsPacket.class);
        assertEquals(0, player.getPermissionLevel());

        permissions.setOp(BOB, "Bob", true);
        env.tick();

        assertEquals(4, player.getPermissionLevel());
        declared.assertSingle();
    }

    @Test
    void losingOperatorStatusIsSentToo(Env env) {
        PermissionService permissions = new PermissionService(directory);
        permissions.setOp(BOB, "Bob", true);
        new CommandAccess(permissions).install(MinecraftServer.getGlobalEventHandler());
        TestConnection connection = env.createConnection(new GameProfile(BOB, "Bob"));
        Player player = connection.connect(env.createFlatInstance(), new Pos(0, 42, 0));
        assertEquals(4, player.getPermissionLevel());
        var declared = connection.trackIncoming(DeclareCommandsPacket.class);

        permissions.setOp(BOB, "Bob", false);
        env.tick();

        assertEquals(0, player.getPermissionLevel());
        declared.assertSingle();
    }

    @Test
    void grantingAPermissionRefreshesCommands(Env env) {
        PermissionService permissions = new PermissionService(directory);
        new CommandAccess(permissions).install(MinecraftServer.getGlobalEventHandler());
        TestConnection connection = env.createConnection(new GameProfile(BOB, "Bob"));
        connection.connect(env.createFlatInstance(), new Pos(0, 42, 0));
        var declared = connection.trackIncoming(DeclareCommandsPacket.class);

        permissions.grant(BOB, "tube.command.weather");
        env.tick();

        declared.assertSingle();
    }
}

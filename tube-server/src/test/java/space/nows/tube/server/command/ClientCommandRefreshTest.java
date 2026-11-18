package space.nows.tube.server.command;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;
import java.util.UUID;

@EnvTest
class ClientCommandRefreshTest {

    @TempDir
    Path directory;

    @Test
    void registeringAndUnregisteringResendsTheCommandsOnce(Env env) {
        WorldService worlds = new WorldService(directory, "main");
        CommandService commands = new CommandService(new PlayerService(worlds, new PermissionService(directory)));
        var connection = env.createConnection(new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000c3"), "Cy"));
        connection.connect(env.createFlatInstance(), new Pos(0, 42, 0));
        var declared = connection.trackIncoming(DeclareCommandsPacket.class);

        commands.register("first", (sender, args) -> {
        });
        commands.register("second", (sender, args) -> {
        });
        env.tick();
        declared.assertSingle();

        var after = connection.trackIncoming(DeclareCommandsPacket.class);
        commands.unregister("first");
        env.tick();
        after.assertSingle();
    }
}

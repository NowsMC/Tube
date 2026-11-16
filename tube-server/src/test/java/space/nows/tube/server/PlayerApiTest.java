package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.minestom.server.coordinate.Pos;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.GameMode;
import space.nows.tube.api.Location;
import space.nows.tube.api.Text;

import java.nio.file.Path;

@EnvTest
class PlayerApiTest {

    @TempDir
    Path directory;

    @Test
    void changesGameModeAndHealth(Env env) {
        WorldService worlds = new WorldService(directory, "main");
        PlayerService players = new PlayerService(worlds, new PermissionService(directory));
        var handle = env.createPlayer(((TubeWorld) worlds.defaultWorld()).instance(), new Pos(0, 42, 0));
        TubePlayer player = players.wrap(handle);

        player.setGameMode(GameMode.CREATIVE);
        player.setHealth(7);

        assertEquals(net.minestom.server.entity.GameMode.CREATIVE, handle.getGameMode());
        assertEquals(GameMode.CREATIVE, player.gameMode());
        assertEquals(7.0, player.health());
    }

    @Test
    void teleportsBetweenWorlds(Env env) {
        WorldService worlds = new WorldService(directory, "main");
        PlayerService players = new PlayerService(worlds, new PermissionService(directory));
        var handle = env.createPlayer(((TubeWorld) worlds.defaultWorld()).instance(), new Pos(0, 42, 0));
        TubePlayer player = players.wrap(handle);
        var other = worlds.load("other");

        player.teleport(new Location(other, 5, 50, 5)).join();

        assertSame(other, player.world());
        assertEquals(5.0, player.location().x());
    }

    @Test
    void sendsRichTextWithoutFailing(Env env) {
        WorldService worlds = new WorldService(directory, "main");
        TubePlayer player = new PlayerService(worlds, new PermissionService(directory)).wrap(env.createPlayer(((TubeWorld) worlds.defaultWorld()).instance(), new Pos(0, 42, 0)));
        player.sendMessage(Text.mini("<green>hello"));
        player.sendActionBar(Text.mini("<gold>bar"));
        player.showTitle(Text.mini("<red>title"), Text.plain("sub"));
    }
}

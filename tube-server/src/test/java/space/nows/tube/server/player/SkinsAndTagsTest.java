package space.nows.tube.server.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.coordinate.Pos;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.entity.player.LoginMode;
import space.nows.tube.api.entity.player.Skin;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

@EnvTest
class SkinsAndTagsTest {

    @TempDir
    Path directory;

    private MinestomPlayer join(Env env, PlayerService players, WorldService worlds) {
        return players.wrap(env.createPlayer(((MinestomWorld) worlds.defaultWorld()).instance(), new Pos(0, 42, 0)));
    }

    @Test
    void skinsCanBeReadAndReplaced(Env env) {
        WorldService worlds = new WorldService(directory, "main");
        PlayerService players = new PlayerService(worlds, new PermissionService(directory));
        MinestomPlayer player = join(env, players, worlds);

        player.setSkin(new Skin("dGV4dHVyZXM=", "c2lnbmF0dXJl"));

        assertEquals(Optional.of(new Skin("dGV4dHVyZXM=", "c2lnbmF0dXJl")), player.skin());
    }

    @Test
    void entityTagsAreASet(Env env) {
        WorldService worlds = new WorldService(directory, "main");
        PlayerService players = new PlayerService(worlds, new PermissionService(directory));
        MinestomPlayer player = join(env, players, worlds);

        assertTrue(player.addTag("guest"));
        assertFalse(player.addTag("guest"));
        assertTrue(player.addTag("vip"));
        assertEquals(Set.of("guest", "vip"), player.tags());
        assertTrue(player.removeTag("guest"));
        assertFalse(player.removeTag("guest"));
        assertEquals(Set.of("vip"), player.tags());
    }

    @Test
    void loginModeComesFromTheLogin(Env env) {
        WorldService worlds = new WorldService(directory, "main");
        PlayerService players = new PlayerService(worlds, new PermissionService(directory));
        MinestomPlayer player = join(env, players, worlds);

        assertEquals(LoginMode.OFFLINE, player.loginMode());
        players.rememberLoginMode(player.uuid(), LoginMode.ONLINE);
        assertEquals(LoginMode.ONLINE, player.loginMode());
    }
}

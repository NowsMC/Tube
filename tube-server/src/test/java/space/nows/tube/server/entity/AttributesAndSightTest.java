package space.nows.tube.server.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;

@EnvTest
class AttributesAndSightTest {

    @TempDir
    Path directory;

    private MinestomWorld world;
    private PlayerService players;

    private void setup() {
        WorldService worlds = new WorldService(directory, "main");
        players = new PlayerService(worlds, new PermissionService(directory));
        world = (MinestomWorld) worlds.defaultWorld();
        world.instance().loadChunk(0, 0).join();
    }

    @Test
    void attributesAreReadAndChangedByKey(Env env) {
        setup();
        var player = env.createPlayer(world.instance(), new Pos(0, 42, 0));
        space.nows.tube.api.entity.player.Player tube = players.wrap(player);

        assertEquals(20, tube.defaultAttribute("minecraft:max_health"), 1.0E-9);
        assertEquals(20, tube.baseAttribute("max_health"), 1.0E-9);
        tube.setBaseAttribute("minecraft:max_health", 30);

        assertEquals(30, tube.attribute("minecraft:max_health"), 1.0E-9);
        assertEquals(30, tube.maxHealth(), 1.0E-9);
        assertThrows(IllegalArgumentException.class, () -> tube.attribute("minecraft:nothing_like_it"));
    }

    @Test
    void wallsBlockLineOfSight(Env env) {
        setup();
        var watcher = env.createPlayer(world.instance(), new Pos(0, 42, 0));
        var target = env.createPlayer(world.instance(), new Pos(8, 42, 0));
        space.nows.tube.api.entity.player.Player tubeWatcher = players.wrap(watcher);
        space.nows.tube.api.entity.player.Player tubeTarget = players.wrap(target);

        assertTrue(tubeWatcher.hasLineOfSight(tubeTarget));

        for (int y = 40; y <= 46; y++) {
            for (int z = -2; z <= 2; z++) {
                world.instance().setBlock(4, y, z, Block.STONE);
            }
        }

        assertFalse(tubeWatcher.hasLineOfSight(tubeTarget));
        assertTrue(tubeWatcher.hasLineOfSight(tubeWatcher));
    }
}

package space.nows.tube.server.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.coordinate.Pos;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;
import java.util.UUID;

@EnvTest
class PlayerVisibilityTest {

    @TempDir
    Path directory;

    @Test
    void trackingStaysConsistentWhenPlayersLeave() {
        PlayerVisibility visibility = new PlayerVisibility();
        UUID viewer = UUID.randomUUID();
        UUID target = UUID.randomUUID();

        assertTrue(visibility.canSee(viewer, target));
        visibility.hide(viewer, target);
        assertFalse(visibility.canSee(viewer, target));
        assertTrue(visibility.canSee(target, viewer));
        visibility.forget(target);
        assertTrue(visibility.canSee(viewer, target));
    }

    @Test
    void aHiddenPlayerStopsBeingShownToTheViewerOnly(Env env) {
        WorldService worlds = new WorldService(directory, "main");
        PlayerService players = new PlayerService(worlds, new PermissionService(directory));
        var instance = ((MinestomWorld) worlds.defaultWorld()).instance();
        var watcher = env.createPlayer(instance, new Pos(0, 42, 0));
        var hider = env.createPlayer(instance, new Pos(2, 42, 0));
        var other = env.createPlayer(instance, new Pos(0, 42, 2));
        space.nows.tube.api.entity.Player tubeWatcher = players.wrap(watcher);
        space.nows.tube.api.entity.Player tubeHider = players.wrap(hider);
        players.wrap(other);
        assertTrue(hider.getViewers().contains(watcher));
        assertTrue(tubeWatcher.canSee(tubeHider));

        tubeWatcher.hidePlayer(tubeHider);

        assertFalse(tubeWatcher.canSee(tubeHider));
        assertFalse(hider.getViewers().contains(watcher));
        assertTrue(hider.getViewers().contains(other));
        assertEquals(true, watcher.getViewers().contains(hider));

        tubeWatcher.showPlayer(tubeHider);

        assertTrue(tubeWatcher.canSee(tubeHider));
        assertTrue(hider.getViewers().contains(watcher));
    }
}

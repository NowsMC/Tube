package space.nows.tube.server.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.MinestomWorld;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;
import java.util.List;

@EnvTest
class EntityStateTest {

    @TempDir
    Path directory;

    private MinestomWorld world;
    private PlayerService players;

    private void setup() {
        WorldService worlds = new WorldService(directory, "main");
        players = new PlayerService(worlds, new PermissionService(directory));
        world = (MinestomWorld) worlds.defaultWorld();
    }

    @Test
    void entitiesReportTheirState(Env env) {
        setup();
        var player = env.createPlayer(world.instance(), new Pos(0, 42, 0));
        space.nows.tube.api.entity.player.Player tube = players.wrap(player);

        assertFalse(tube.isSneaking());
        assertFalse(tube.isSprinting());
        assertFalse(tube.isSwimming());
        player.setSneaking(true);
        player.setSprinting(true);
        assertTrue(tube.isSneaking());
        assertTrue(tube.isSprinting());

        assertFalse(tube.isGlowing());
        tube.setGlowing(true);
        assertTrue(tube.isGlowing());

        assertEquals(0, tube.fireTicks());
        tube.setFireTicks(40);
        assertEquals(40, tube.fireTicks());
        assertFalse(tube.isGliding());

        assertEquals(player.getEyeHeight(), tube.eyeHeight(), 1.0E-9);
        assertEquals(tube.location().y() + tube.eyeHeight(), tube.eyeLocation().y(), 1.0E-9);
    }

    @Test
    void anOpenContainerIsReported(Env env) {
        setup();
        var player = env.createPlayer(world.instance(), new Pos(0, 42, 0));
        space.nows.tube.api.entity.player.Player tube = players.wrap(player);
        assertTrue(tube.openedInventory().isEmpty());

        player.openInventory(new Inventory(InventoryType.CHEST_3_ROW, "Chest"));

        assertTrue(tube.openedInventory().isPresent());
        player.closeInventory();
        assertTrue(tube.openedInventory().isEmpty());
    }

    @Test
    void nearbyEntitiesAreFoundInABoxAroundAPoint(Env env) {
        setup();
        LivingEntity near = new LivingEntity(EntityType.COW);
        near.setInstance(world.instance(), new Pos(2, 42, 1)).join();
        LivingEntity far = new LivingEntity(EntityType.COW);
        far.setInstance(world.instance(), new Pos(30, 42, 0)).join();
        LivingEntity high = new LivingEntity(EntityType.COW);
        high.setInstance(world.instance(), new Pos(1, 60, 0)).join();

        List<String> found = world.nearbyEntities(new Location(world, 0, 42, 0), 4, 3, 4).stream().map(entity -> entity.uuid().toString()).toList();

        assertEquals(List.of(near.getUuid().toString()), found);
        assertEquals(2, world.nearbyEntities(new Location(world, 0, 50, 0), 10, 20, 10).size());
    }

    @Test
    void anEntityWhoseBodyTouchesTheBoxIsFoundEvenWhenItsCentreIsOutside(Env env) {
        setup();
        LivingEntity edge = new LivingEntity(EntityType.COW);
        edge.setInstance(world.instance(), new Pos(1.4, 42, 0)).join();

        assertEquals(1, world.nearbyEntities(new Location(world, 0, 42, 0), 1, 0, 0).size());
        assertEquals(0, world.nearbyEntities(new Location(world, 0, 42, 0), 0.5, 0, 0).size());
    }
}

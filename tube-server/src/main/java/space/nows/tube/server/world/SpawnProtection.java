package space.nows.tube.server.world;

import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.event.Cancellable;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.block.BlockBreakEvent;
import space.nows.tube.api.event.block.BlockPlaceEvent;
import space.nows.tube.api.permission.PermissionManager;
import space.nows.tube.api.world.World;
import space.nows.tube.api.world.WorldManager;

public final class SpawnProtection {

    private final int radius;
    private final WorldManager worlds;
    private final PermissionManager permissions;

    public SpawnProtection(int radius, WorldManager worlds, PermissionManager permissions) {
        this.radius = radius;
        this.worlds = worlds;
        this.permissions = permissions;
    }

    public void install(EventBus events) {
        events.subscribe(BlockBreakEvent.class, EventPriority.LOW, true, event -> protect(event.player(), event.world(), event.x(), event.z(), event));
        events.subscribe(BlockPlaceEvent.class, EventPriority.LOW, true, event -> protect(event.player(), event.world(), event.x(), event.z(), event));
    }

    private void protect(Player player, World world, int x, int z, Cancellable event) {
        if (radius <= 0 || !world.equals(worlds.defaultWorld()) || permissions.isOp(player.uuid())) {
            return;
        }
        var spawn = world.spawn();
        if (Math.abs(x - (int) spawn.x()) <= radius && Math.abs(z - (int) spawn.z()) <= radius) {
            event.setCancelled(true);
            player.sendMessage("You cannot modify the area around the spawn point");
        }
    }
}

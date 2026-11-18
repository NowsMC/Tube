package space.nows.tube.server.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerSpawnEvent;
import space.nows.tube.server.access.PermissionService;

import java.util.UUID;

public final class CommandAccess {

    private static final int OPERATOR_LEVEL = 4;

    private final PermissionService permissions;

    public CommandAccess(PermissionService permissions) {
        this.permissions = permissions;
    }

    public void install(GlobalEventHandler handler) {
        handler.addListener(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn()) {
                setLevel(event.getPlayer());
            }
        });
        permissions.onChange(uuid -> MinecraftServer.getSchedulerManager().scheduleNextTick(() -> refresh(uuid)));
    }

    private void refresh(UUID uuid) {
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
        if (player != null) {
            setLevel(player);
            player.refreshCommands();
        }
    }

    private void setLevel(Player player) {
        player.setPermissionLevel(permissions.isOp(player.getUuid()) ? OPERATOR_LEVEL : 0);
    }
}

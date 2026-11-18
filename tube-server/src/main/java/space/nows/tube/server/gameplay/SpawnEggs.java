package space.nows.tube.server.gameplay;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.item.ItemStack;
import space.nows.tube.api.world.Location;
import space.nows.tube.server.world.WorldService;

import java.util.concurrent.ThreadLocalRandom;

public final class SpawnEggs {

    private static final String SUFFIX = "_spawn_egg";
    private static final String NAMESPACE = "minecraft:";

    private final WorldService worlds;

    public SpawnEggs(WorldService worlds) {
        this.worlds = worlds;
    }

    public void install(GlobalEventHandler handler) {
        handler.addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.isCancelled() || event.isBlockingItemUse()) {
                return;
            }
            Player player = event.getPlayer();
            ItemStack item = player.getItemInHand(event.getHand());
            EntityType type = entity(item);
            if (type == null) {
                return;
            }
            BlockVec at = event.getBlockPosition().relative(event.getBlockFace());
            var world = worlds.of(event.getInstance());
            world.spawn(type.key().asString(), new Location(world, at.x() + 0.5, at.y(), at.z() + 0.5, ThreadLocalRandom.current().nextFloat() * 360, 0));
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setItemInHand(event.getHand(), item.consume(1));
            }
            event.setBlockingItemUse(true);
        });
    }

    static EntityType entity(ItemStack item) {
        String key = item.material().key().asString();
        if (!key.endsWith(SUFFIX)) {
            return null;
        }
        EntityType type = EntityType.fromKey(NAMESPACE + key.substring(NAMESPACE.length(), key.length() - SUFFIX.length()));
        return type == null || type == EntityType.PLAYER ? null : type;
    }
}

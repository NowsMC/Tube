package space.nows.tube.server.gameplay;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.tag.Tag;

public final class Falling {

    private static final Tag<Double> FALL_START = Tag.Transient("tube:fall_start");
    private static final int VOID_MARGIN = 64;

    private Falling() {
    }

    static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerSpawnEvent.class, event -> event.getPlayer().removeTag(FALL_START));
        handler.addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            double y = event.getNewPosition().y();
            if (player.getInstance() != null && y < player.getInstance().getCachedDimensionType().minY() - VOID_MARGIN
                    && player.getGameMode() != GameMode.SPECTATOR && player.getAliveTicks() % 10 == 0) {
                player.damage(DamageType.OUT_OF_WORLD, 4);
            }
            if (!affected(player) || player.isFlying() || inLiquid(player)) {
                player.removeTag(FALL_START);
                return;
            }
            Double start = player.getTag(FALL_START);
            if (!event.isOnGround()) {
                if (start == null || y > start) {
                    player.setTag(FALL_START, y);
                }
                return;
            }
            player.removeTag(FALL_START);
            if (start != null) {
                int damage = (int) Math.ceil(start - y - 3);
                if (damage > 0) {
                    player.damage(DamageType.FALL, damage);
                }
            }
        });
    }

    public static double distance(Player player) {
        Double start = player.getTag(FALL_START);
        return start == null ? 0 : Math.max(0, start - player.getPosition().y());
    }

    static boolean affected(Player player) {
        return player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE;
    }

    private static boolean inLiquid(Player player) {
        var block = player.getInstance() == null ? null : player.getInstance().getBlock(player.getPosition());
        return block != null && block.liquid();
    }
}

package space.nows.tube.server.mob;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.tag.Tag;

public final class KillCredit {

    private static final Tag<Long> LAST_PLAYER_HIT = Tag.Transient("tube:last_player_hit");
    private static final Tag<Player> LAST_PLAYER = Tag.Transient("tube:last_player");
    private static final long CREDIT_TICKS = 100;

    private KillCredit() {
    }

    public static void hitBy(LivingEntity target, Player player) {
        target.setTag(LAST_PLAYER_HIT, target.getAliveTicks());
        target.setTag(LAST_PLAYER, player);
    }

    public static Player killer(LivingEntity target) {
        Long hit = target.getTag(LAST_PLAYER_HIT);
        Player player = target.getTag(LAST_PLAYER);
        return hit != null && player != null && target.getAliveTicks() - hit < CREDIT_TICKS ? player : null;
    }
}

package space.nows.tube.server.gameplay.melee;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;

public final class Attackable {

    private Attackable() {
    }

    public static boolean by(LivingEntity target) {
        if (target.isDead()) {
            return false;
        }
        if (target instanceof Player player) {
            return player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR;
        }
        return !(target.getEntityMeta() instanceof ArmorStandMeta stand && stand.isMarker());
    }
}

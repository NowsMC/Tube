package space.nows.tube.server.mob.kind.monster.enderman;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;

final class Gazing {

    private static final double CONE = 0.025;

    private Gazing() {
    }

    static boolean staredAtBy(Creature enderman, Player player) {
        if (player.getEquipment(EquipmentSlot.HELMET).material() == Material.CARVED_PUMPKIN) {
            return false;
        }
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec look = player.getPosition().direction();
        double gazeY = enderman.getPosition().y() + enderman.getEyeHeight();
        Vec toward = new Vec(enderman.getPosition().x() - eye.x(), gazeY - eye.y(), enderman.getPosition().z() - eye.z());
        double distance = toward.length();
        if (distance < 1.0E-7) {
            return false;
        }
        double dot = look.normalize().dot(toward.div(distance));
        return dot > 1.0 - CONE / distance && player.hasLineOfSight(enderman, false);
    }
}

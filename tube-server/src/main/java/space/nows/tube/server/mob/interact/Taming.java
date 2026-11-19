package space.nows.tube.server.mob.interact;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.life.Tamable;
import space.nows.tube.server.mob.life.Tameness;
import space.nows.tube.server.world.effect.EntityEffect;

final class Taming {

    private Taming() {
    }

    static boolean apply(Player player, Creature creature) {
        if (!(creature.kind() instanceof Tamable tamable)) {
            return false;
        }
        Tameness tameness = creature.trait(Tameness.class).orElse(null);
        if (tameness == null) {
            return false;
        }
        ItemStack held = player.getItemInMainHand();
        if (!tameness.tamed()) {
            if (!tamable.tamingFood().contains(held.material()) || creature.target() != null) {
                return false;
            }
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setItemInMainHand(held.consume(1));
            }
            if (creature.random().nextInt(tamable.tameOneIn()) == 0) {
                tameness.tame(player);
                tamable.tamed(creature);
                creature.navigation().stop();
                creature.setTarget(null);
                EntityEffect.TAMING_SUCCEEDED.play(creature);
            } else {
                EntityEffect.TAMING_FAILED.play(creature);
            }
            return true;
        }
        if (tameness.ownedBy(player) && !tamable.tamingFood().contains(held.material())) {
            tameness.sit(!tameness.sitting());
            creature.navigation().stop();
            creature.setTarget(null);
            return true;
        }
        return false;
    }
}

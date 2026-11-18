package space.nows.tube.server.mob.interact;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.life.Breedable;
import space.nows.tube.server.mob.life.Lifecycle;

final class Feeding {

    private Feeding() {
    }

    static boolean apply(Player player, Creature creature) {
        if (!(creature.kind() instanceof Breedable breedable)) {
            return false;
        }
        ItemStack held = player.getItemInMainHand();
        if (!breedable.breedingFood().contains(held.material())) {
            return false;
        }
        Lifecycle life = creature.trait(Lifecycle.class).orElse(null);
        if (life == null) {
            return false;
        }
        if (life.canFallInLove()) {
            consume(player, held);
            life.fallInLove(player);
            return true;
        }
        if (life.baby()) {
            consume(player, held);
            life.feedBaby();
            return true;
        }
        return false;
    }

    private static void consume(Player player, ItemStack held) {
        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setItemInMainHand(held.consume(1));
        }
    }
}

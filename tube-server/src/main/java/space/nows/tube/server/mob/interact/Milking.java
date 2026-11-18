package space.nows.tube.server.mob.interact;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.server.mob.Creature;

final class Milking {

    private Milking() {
    }

    static boolean apply(Player player, Creature creature) {
        ItemStack held = player.getItemInMainHand();
        if (held.material() != Material.BUCKET || creature.getEntityType() != EntityType.COW || creature.isBaby()) {
            return false;
        }
        ItemStack milk = ItemStack.of(Material.MILK_BUCKET);
        if (player.getGameMode() == GameMode.CREATIVE) {
            player.getInventory().addItemStack(milk);
        } else if (held.amount() == 1) {
            player.setItemInMainHand(milk);
        } else {
            player.setItemInMainHand(held.consume(1));
            if (!player.getInventory().addItemStack(milk)) {
                player.dropItem(milk);
            }
        }
        return true;
    }
}

package space.nows.tube.server.gameplay;

import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;

final class DeathDrops {

    private DeathDrops() {
    }

    static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerDeathEvent.class, event -> {
            Player player = event.getPlayer();
            if (!Falling.affected(player) || player.getInstance() == null) {
                return;
            }
            var inventory = player.getInventory();
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                if (slot == PlayerInventoryUtils.CRAFT_RESULT) {
                    continue;
                }
                ItemStack item = inventory.getItemStack(slot);
                if (!item.isAir()) {
                    Drops.spawn(player.getInstance(), player.getPosition().add(0, 1, 0), item);
                }
            }
            inventory.clear();
            int xp = Math.min(100, player.getLevel() * 7);
            if (xp > 0) {
                Experience.spawn(player.getInstance(), player.getPosition().add(0, 0.5, 0), xp);
            }
            Experience.reset(player);
            player.setFood(20);
            player.setFoodSaturation(5);
        });
    }
}

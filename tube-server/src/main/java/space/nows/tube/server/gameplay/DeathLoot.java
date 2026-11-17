package space.nows.tube.server.gameplay;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import space.nows.tube.server.event.DeathDrops;
import space.nows.tube.server.gameplay.mob.Mob;
import space.nows.tube.server.gameplay.mob.Mobs;

import java.util.ArrayList;
import java.util.List;

final class DeathLoot implements DeathDrops {

    @Override
    public Drops collect(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            return Mobs.loot(mob);
        }
        if (entity instanceof Player player && Falling.affected(player)) {
            return playerDrops(player);
        }
        return new Drops(List.of(), 0);
    }

    private static Drops playerDrops(Player player) {
        List<ItemStack> items = new ArrayList<>();
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItemStack(slot);
            if (slot != PlayerInventoryUtils.CRAFT_RESULT && !item.isAir()) {
                items.add(item);
            }
        }
        inventory.clear();
        int experience = Math.min(100, player.getLevel() * 7);
        Experience.reset(player);
        player.setFood(20);
        player.setFoodSaturation(5);
        return new Drops(items, experience);
    }
}

package space.nows.tube.server.gameplay;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.sound.SoundEvent;
import space.nows.tube.server.gameplay.loot.LootTables;

import java.util.concurrent.ThreadLocalRandom;

public final class Durability {

    private Durability() {
    }

    public static void damage(LivingEntity holder, EquipmentSlot slot, int amount) {
        if (holder instanceof Player player && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        ItemStack item = holder.getEquipment(slot);
        ItemStack damaged = damaged(item, amount);
        if (damaged == item) {
            return;
        }
        holder.setEquipment(slot, damaged);
        if (damaged.isAir() && holder.getInstance() != null) {
            holder.getViewersAsAudience().playSound(net.kyori.adventure.sound.Sound.sound(SoundEvent.ENTITY_ITEM_BREAK, net.kyori.adventure.sound.Sound.Source.PLAYER, 0.8f, 1f), holder);
            if (holder instanceof Player player) {
                player.playSound(net.kyori.adventure.sound.Sound.sound(SoundEvent.ENTITY_ITEM_BREAK, net.kyori.adventure.sound.Sound.Source.PLAYER, 0.8f, 1f));
            }
        }
    }

    public static ItemStack damaged(ItemStack item, int amount) {
        Integer max = item.get(DataComponents.MAX_DAMAGE);
        if (max == null || item.has(DataComponents.UNBREAKABLE) || amount <= 0) {
            return item;
        }
        int unbreaking = LootTables.level(item, "minecraft:unbreaking");
        int applied = 0;
        for (int i = 0; i < amount; i++) {
            if (unbreaking == 0 || ThreadLocalRandom.current().nextInt(unbreaking + 1) == 0) {
                applied++;
            }
        }
        if (applied == 0) {
            return item;
        }
        int damage = item.get(DataComponents.DAMAGE, 0) + applied;
        return damage >= max ? ItemStack.AIR : item.with(DataComponents.DAMAGE, damage);
    }
}

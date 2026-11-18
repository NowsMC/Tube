package space.nows.tube.server.mob.kind.monster;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.world.Difficulty;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.spawn.LocalDifficulty;

import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

public final class ArmorRoll {

    private static final float CHANCE = 0.15f;
    private static final float UPGRADE_CHANCE = 0.1087f;
    private static final int UPGRADE_ROLLS = 3;
    private static final List<EquipmentSlot> ORDER = List.of(EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS);
    private static final Map<EquipmentSlot, Material[]> TIERS = Map.of(
            EquipmentSlot.HELMET, new Material[]{Material.LEATHER_HELMET, Material.COPPER_HELMET, Material.GOLDEN_HELMET,
                    Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET},
            EquipmentSlot.CHESTPLATE, new Material[]{Material.LEATHER_CHESTPLATE, Material.COPPER_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
                    Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE},
            EquipmentSlot.LEGGINGS, new Material[]{Material.LEATHER_LEGGINGS, Material.COPPER_LEGGINGS, Material.GOLDEN_LEGGINGS,
                    Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS},
            EquipmentSlot.BOOTS, new Material[]{Material.LEATHER_BOOTS, Material.COPPER_BOOTS, Material.GOLDEN_BOOTS,
                    Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS});

    private ArmorRoll() {
    }

    public static void dress(Creature creature, LocalDifficulty difficulty, RandomGenerator random) {
        if (random.nextFloat() >= CHANCE * difficulty.specialMultiplier()) {
            return;
        }
        int tier = random.nextInt(3);
        for (int i = 0; i < UPGRADE_ROLLS; i++) {
            if (random.nextFloat() < UPGRADE_CHANCE) {
                tier++;
            }
        }
        float stopChance = difficulty.base() == Difficulty.HARD ? 0.1f : 0.25f;
        boolean first = true;
        for (EquipmentSlot slot : ORDER) {
            if (!first && random.nextFloat() < stopChance) {
                return;
            }
            first = false;
            if (creature.getEquipment(slot).isAir()) {
                Material[] tiers = TIERS.get(slot);
                creature.setEquipment(slot, ItemStack.of(tiers[Math.min(tier, tiers.length - 1)]));
            }
        }
    }
}

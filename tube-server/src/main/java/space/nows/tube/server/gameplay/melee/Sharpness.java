package space.nows.tube.server.gameplay.melee;

import net.minestom.server.item.ItemStack;
import space.nows.tube.server.item.Enchantments;

public final class Sharpness {

    private static final double PER_LEVEL = 0.5;

    private Sharpness() {
    }

    public static double bonus(ItemStack weapon) {
        int level = Enchantments.level(weapon, "minecraft:sharpness");
        return level > 0 ? PER_LEVEL * level + PER_LEVEL : 0;
    }
}

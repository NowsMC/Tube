package space.nows.tube.server.loot;

import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;

import java.util.random.RandomGenerator;

public record LootContext(RandomGenerator random, ItemStack tool, Block block, boolean explosion, boolean killedByPlayer, boolean onFire,
                          String sheepColor, boolean sheared, int luck, boolean openWater) {

    public static LootContext block(Block block, ItemStack tool, RandomGenerator random) {
        return new LootContext(random, tool, block, false, false, false, null, false, 0, false);
    }

    public static LootContext mob(ItemStack weapon, boolean killedByPlayer, boolean onFire, RandomGenerator random) {
        return new LootContext(random, weapon, null, false, killedByPlayer, onFire, null, false, 0, false);
    }

    public static LootContext fishing(ItemStack rod, int luck, boolean openWater, RandomGenerator random) {
        return new LootContext(random, rod, null, false, false, false, null, false, luck, openWater);
    }

    public LootContext withSheep(String color, boolean isSheared) {
        return new LootContext(random, tool, block, explosion, killedByPlayer, onFire, color, isSheared, luck, openWater);
    }
}

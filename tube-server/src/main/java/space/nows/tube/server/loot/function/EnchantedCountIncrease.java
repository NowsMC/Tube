package space.nows.tube.server.loot.function;

import net.minestom.server.item.ItemStack;
import space.nows.tube.server.item.Enchantments;
import space.nows.tube.server.loot.LootContext;
import space.nows.tube.server.loot.number.NumberProvider;

record EnchantedCountIncrease(String enchantment, NumberProvider perLevel, int limit) implements LootFunction {

    @Override
    public ItemStack apply(ItemStack item, LootContext context) {
        int level = Enchantments.level(context.tool(), enchantment);
        if (level <= 0) {
            return item;
        }
        int count = item.amount() + (int) Math.round(perLevel.sample(context.random()) * level);
        return Stacks.withCount(item, limit > 0 ? Math.min(limit, count) : count);
    }
}

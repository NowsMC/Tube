package space.nows.tube.server.loot.function;

import net.minestom.server.item.ItemStack;
import space.nows.tube.server.loot.LootContext;
import space.nows.tube.server.loot.number.NumberProvider;

record LimitCount(NumberProvider min, NumberProvider max) implements LootFunction {

    @Override
    public ItemStack apply(ItemStack item, LootContext context) {
        int count = item.amount();
        if (min != null) {
            count = Math.max(count, min.sampleInt(context.random()));
        }
        if (max != null) {
            count = Math.min(count, max.sampleInt(context.random()));
        }
        return Stacks.withCount(item, count);
    }
}

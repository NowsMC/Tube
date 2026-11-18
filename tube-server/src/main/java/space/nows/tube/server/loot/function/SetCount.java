package space.nows.tube.server.loot.function;

import net.minestom.server.item.ItemStack;
import space.nows.tube.server.loot.LootContext;
import space.nows.tube.server.loot.number.NumberProvider;

record SetCount(NumberProvider count, boolean add) implements LootFunction {

    @Override
    public ItemStack apply(ItemStack item, LootContext context) {
        int amount = count.sampleInt(context.random());
        return Stacks.withCount(item, add ? item.amount() + amount : amount);
    }
}

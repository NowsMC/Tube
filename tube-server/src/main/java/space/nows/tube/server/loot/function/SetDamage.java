package space.nows.tube.server.loot.function;

import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import space.nows.tube.server.loot.LootContext;
import space.nows.tube.server.loot.number.NumberProvider;

record SetDamage(NumberProvider fraction) implements LootFunction {

    @Override
    public ItemStack apply(ItemStack item, LootContext context) {
        Integer max = item.get(DataComponents.MAX_DAMAGE);
        if (max == null) {
            return item;
        }
        return item.with(DataComponents.DAMAGE, (int) (max * fraction.sample(context.random())));
    }
}

package space.nows.tube.server.loot.entry;

import net.minestom.server.item.ItemStack;
import space.nows.tube.server.loot.LootContext;
import space.nows.tube.server.loot.condition.LootCondition;

import java.util.List;

record EmptyEntry(int weight, int quality, List<LootCondition> conditions) implements LootEntry.Leaf {

    @Override
    public void expand(LootContext context, List<Leaf> out) {
        if (LootCondition.all(conditions, context)) {
            out.add(this);
        }
    }

    @Override
    public int weight(LootContext context) {
        return Weights.of(weight, quality, context.luck());
    }

    @Override
    public void produce(LootContext context, List<ItemStack> out) {
    }
}

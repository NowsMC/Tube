package space.nows.tube.server.loot.entry;

import net.minestom.server.item.ItemStack;
import space.nows.tube.server.loot.LootContext;
import space.nows.tube.server.loot.LootTable;
import space.nows.tube.server.loot.condition.LootCondition;
import space.nows.tube.server.loot.function.LootFunction;

import java.util.List;
import java.util.function.Function;

record TableEntry(String reference, Function<String, LootTable> resolver, int weight, int quality, List<LootCondition> conditions,
                  List<LootFunction> functions) implements LootEntry.Leaf {

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
        LootTable table = resolver.apply(reference);
        if (table == null) {
            return;
        }
        for (ItemStack item : table.roll(context)) {
            out.add(LootFunction.applyAll(functions, item, context));
        }
    }
}

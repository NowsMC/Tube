package space.nows.tube.server.loot.entry;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.server.loot.LootContext;
import space.nows.tube.server.loot.condition.LootCondition;
import space.nows.tube.server.loot.function.LootFunction;

import java.util.List;

record ItemEntry(Material material, int weight, int quality, List<LootCondition> conditions, List<LootFunction> functions) implements LootEntry.Leaf {

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
        out.add(LootFunction.applyAll(functions, ItemStack.of(material), context));
    }
}

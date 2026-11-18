package space.nows.tube.server.loot.function;

import net.minestom.server.item.ItemStack;
import space.nows.tube.server.loot.LootContext;

import java.util.List;

@FunctionalInterface
public interface LootFunction {

    ItemStack apply(ItemStack item, LootContext context);

    static ItemStack applyAll(List<LootFunction> functions, ItemStack item, LootContext context) {
        ItemStack result = item;
        for (LootFunction function : functions) {
            result = function.apply(result, context);
        }
        return result;
    }
}

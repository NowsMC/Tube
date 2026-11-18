package space.nows.tube.server.loot;

import net.minestom.server.item.ItemStack;
import space.nows.tube.server.loot.condition.LootCondition;
import space.nows.tube.server.loot.entry.LootEntry;
import space.nows.tube.server.loot.function.LootFunction;
import space.nows.tube.server.loot.number.NumberProvider;

import java.util.ArrayList;
import java.util.List;

record LootPool(NumberProvider rolls, List<LootEntry> entries, List<LootCondition> conditions, List<LootFunction> functions) {

    void roll(LootContext context, List<ItemStack> out) {
        if (!LootCondition.all(conditions, context)) {
            return;
        }
        List<ItemStack> drops = new ArrayList<>();
        int count = rolls.sampleInt(context.random());
        for (int i = 0; i < count; i++) {
            List<LootEntry.Leaf> choices = new ArrayList<>();
            entries.forEach(entry -> entry.expand(context, choices));
            LootEntry.Leaf chosen = pick(choices, context);
            if (chosen != null) {
                chosen.produce(context, drops);
            }
        }
        drops.replaceAll(drop -> LootFunction.applyAll(functions, drop, context));
        out.addAll(drops);
    }

    private static LootEntry.Leaf pick(List<LootEntry.Leaf> choices, LootContext context) {
        int total = 0;
        for (LootEntry.Leaf choice : choices) {
            total += choice.weight(context);
        }
        if (total <= 0) {
            return null;
        }
        int roll = context.random().nextInt(total);
        for (LootEntry.Leaf choice : choices) {
            roll -= choice.weight(context);
            if (roll < 0) {
                return choice;
            }
        }
        return null;
    }
}

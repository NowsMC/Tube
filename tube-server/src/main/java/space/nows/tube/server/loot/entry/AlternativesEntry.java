package space.nows.tube.server.loot.entry;

import space.nows.tube.server.loot.LootContext;
import space.nows.tube.server.loot.condition.LootCondition;

import java.util.ArrayList;
import java.util.List;

record AlternativesEntry(List<LootEntry> children, List<LootCondition> conditions) implements LootEntry {

    @Override
    public void expand(LootContext context, List<Leaf> out) {
        if (!LootCondition.all(conditions, context)) {
            return;
        }
        for (LootEntry child : children) {
            List<Leaf> expanded = new ArrayList<>();
            child.expand(context, expanded);
            if (!expanded.isEmpty()) {
                out.addAll(expanded);
                return;
            }
        }
    }
}

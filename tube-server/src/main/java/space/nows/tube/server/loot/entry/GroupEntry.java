package space.nows.tube.server.loot.entry;

import space.nows.tube.server.loot.LootContext;
import space.nows.tube.server.loot.condition.LootCondition;

import java.util.List;

record GroupEntry(List<LootEntry> children, List<LootCondition> conditions) implements LootEntry {

    @Override
    public void expand(LootContext context, List<Leaf> out) {
        if (LootCondition.all(conditions, context)) {
            children.forEach(child -> child.expand(context, out));
        }
    }
}

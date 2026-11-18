package space.nows.tube.server.loot.condition;

import space.nows.tube.server.loot.LootContext;

import java.util.List;

@FunctionalInterface
public interface LootCondition {

    boolean test(LootContext context);

    static boolean all(List<LootCondition> conditions, LootContext context) {
        for (LootCondition condition : conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }
        return true;
    }
}

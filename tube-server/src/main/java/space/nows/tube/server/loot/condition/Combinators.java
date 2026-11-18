package space.nows.tube.server.loot.condition;

import java.util.List;

final class Combinators {

    private Combinators() {
    }

    static LootCondition anyOf(List<LootCondition> terms) {
        return context -> {
            for (LootCondition term : terms) {
                if (term.test(context)) {
                    return true;
                }
            }
            return false;
        };
    }

    static LootCondition allOf(List<LootCondition> terms) {
        return context -> LootCondition.all(terms, context);
    }

    static LootCondition not(LootCondition term) {
        return context -> !term.test(context);
    }
}

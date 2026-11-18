package space.nows.tube.server.loot.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.loot.number.NumberProvider;
import space.nows.tube.server.loot.number.NumberProviders;

import java.util.ArrayList;
import java.util.List;

public final class Conditions {

    private static final float EXPLOSION_SURVIVAL = 0.3f;

    private Conditions() {
    }

    public static List<LootCondition> of(JsonObject holder) {
        if (!holder.has("conditions")) {
            return List.of();
        }
        return list(holder.getAsJsonArray("conditions"));
    }

    private static List<LootCondition> list(Iterable<JsonElement> elements) {
        List<LootCondition> conditions = new ArrayList<>();
        for (JsonElement element : elements) {
            conditions.add(parse(element.getAsJsonObject()));
        }
        return List.copyOf(conditions);
    }

    static LootCondition parse(JsonObject condition) {
        return switch (condition.get("condition").getAsString()) {
            case "minecraft:survives_explosion" -> context -> !context.explosion() || context.random().nextFloat() < EXPLOSION_SURVIVAL;
            case "minecraft:inverted" -> Combinators.not(parse(condition.getAsJsonObject("term")));
            case "minecraft:any_of" -> Combinators.anyOf(list(condition.getAsJsonArray("terms")));
            case "minecraft:all_of" -> Combinators.allOf(list(condition.getAsJsonArray("terms")));
            case "minecraft:block_state_property" -> new BlockStateProperty(condition.getAsJsonObject("properties"));
            case "minecraft:match_tool" -> {
                ItemPredicate predicate = ItemPredicate.parse(condition.getAsJsonObject("predicate"));
                yield context -> predicate.test(context.tool());
            }
            case "minecraft:table_bonus" -> TableBonus.parse(condition);
            case "minecraft:random_chance" -> {
                NumberProvider chance = NumberProviders.parse(condition.get("chance"));
                yield context -> context.random().nextFloat() < chance.sample(context.random());
            }
            case "minecraft:random_chance_with_enchanted_bonus" -> EnchantedChance.parse(condition);
            case "minecraft:killed_by_player" -> context -> context.killedByPlayer();
            case "minecraft:entity_properties" -> EntityProperties.parse(condition);
            default -> context -> false;
        };
    }
}

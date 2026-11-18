package space.nows.tube.server.loot.function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.potion.PotionType;
import space.nows.tube.server.loot.condition.Conditions;
import space.nows.tube.server.loot.condition.LootCondition;
import space.nows.tube.server.loot.number.NumberProviders;

import java.util.ArrayList;
import java.util.List;

public final class Functions {

    private Functions() {
    }

    public static List<LootFunction> of(JsonObject holder) {
        if (!holder.has("functions")) {
            return List.of();
        }
        List<LootFunction> functions = new ArrayList<>();
        for (JsonElement element : holder.getAsJsonArray("functions")) {
            functions.add(parse(element.getAsJsonObject()));
        }
        return List.copyOf(functions);
    }

    private static LootFunction parse(JsonObject function) {
        LootFunction parsed = switch (function.get("function").getAsString()) {
            case "minecraft:set_count" -> new SetCount(NumberProviders.parse(function.get("count")), function.has("add") && function.get("add").getAsBoolean());
            case "minecraft:apply_bonus" -> ApplyBonus.parse(function);
            case "minecraft:enchanted_count_increase" -> new EnchantedCountIncrease(function.get("enchantment").getAsString(), NumberProviders.parse(function.get("count")),
                    function.has("limit") ? function.get("limit").getAsInt() : 0);
            case "minecraft:limit_count" -> limit(function.getAsJsonObject("limit"));
            case "minecraft:furnace_smelt" -> new FurnaceSmelt();
            case "minecraft:set_damage" -> new SetDamage(NumberProviders.parse(function.get("damage")));
            case "minecraft:set_potion" -> potion(function);
            case "minecraft:enchant_with_levels" -> new EnchantWithLevels(function.get("options").getAsString());
            default -> (item, context) -> item;
        };
        List<LootCondition> conditions = Conditions.of(function);
        if (conditions.isEmpty()) {
            return parsed;
        }
        return (item, context) -> LootCondition.all(conditions, context) ? parsed.apply(item, context) : item;
    }

    private static LootFunction limit(JsonObject limit) {
        return new LimitCount(limit.has("min") ? NumberProviders.parse(limit.get("min")) : null, limit.has("max") ? NumberProviders.parse(limit.get("max")) : null);
    }

    private static LootFunction potion(JsonObject function) {
        PotionType type = PotionType.fromKey(function.get("id").getAsString());
        return type == null ? (item, context) -> item : new SetPotion(type);
    }
}

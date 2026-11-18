package space.nows.tube.server.loot.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.item.Enchantments;
import space.nows.tube.server.loot.LootContext;

record EnchantedChance(String enchantment, double plain, double flat, double base, double perLevelAboveFirst, boolean scaled) implements LootCondition {

    static LootCondition parse(JsonObject condition) {
        JsonElement enchanted = condition.get("enchanted_chance");
        String enchantment = condition.get("enchantment").getAsString();
        double plain = condition.get("unenchanted_chance").getAsDouble();
        if (enchanted.isJsonObject()) {
            JsonObject linear = enchanted.getAsJsonObject();
            return new EnchantedChance(enchantment, plain, 0, linear.get("base").getAsDouble(), linear.get("per_level_above_first").getAsDouble(), true);
        }
        return new EnchantedChance(enchantment, plain, enchanted.getAsDouble(), 0, 0, false);
    }

    @Override
    public boolean test(LootContext context) {
        int level = Enchantments.level(context.tool(), enchantment);
        double chance = plain;
        if (level > 0) {
            chance = scaled ? base + perLevelAboveFirst * (level - 1) : flat;
        }
        return context.random().nextDouble() < chance;
    }
}

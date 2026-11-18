package space.nows.tube.server.loot.function;

import com.google.gson.JsonObject;
import net.minestom.server.item.ItemStack;
import space.nows.tube.server.item.Enchantments;
import space.nows.tube.server.loot.LootContext;

import java.util.random.RandomGenerator;

record ApplyBonus(String enchantment, String formula, JsonObject parameters) implements LootFunction {

    static LootFunction parse(JsonObject function) {
        JsonObject parameters = function.has("parameters") ? function.getAsJsonObject("parameters") : new JsonObject();
        return new ApplyBonus(function.get("enchantment").getAsString(), function.get("formula").getAsString(), parameters);
    }

    @Override
    public ItemStack apply(ItemStack item, LootContext context) {
        int level = Enchantments.level(context.tool(), enchantment);
        int count = item.amount();
        RandomGenerator random = context.random();
        int bonus = switch (formula) {
            case "minecraft:ore_drops" -> level > 0 ? count * (Math.max(0, random.nextInt(level + 2) - 1) + 1) : count;
            case "minecraft:uniform_bonus_count" -> count + random.nextInt(parameters.get("bonusMultiplier").getAsInt() * level + 1);
            case "minecraft:binomial_with_bonus_count" -> count + extra(random, level);
            default -> count;
        };
        return Stacks.withCount(item, bonus);
    }

    private int extra(RandomGenerator random, int level) {
        int trials = parameters.get("extra").getAsInt() + level;
        float chance = parameters.get("probability").getAsFloat();
        int extra = 0;
        for (int i = 0; i < trials; i++) {
            if (random.nextFloat() < chance) {
                extra++;
            }
        }
        return extra;
    }
}

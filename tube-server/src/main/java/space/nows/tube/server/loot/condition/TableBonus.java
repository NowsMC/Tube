package space.nows.tube.server.loot.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import space.nows.tube.server.item.Enchantments;
import space.nows.tube.server.loot.LootContext;

record TableBonus(String enchantment, float[] chances) implements LootCondition {

    static LootCondition parse(JsonObject condition) {
        JsonArray array = condition.getAsJsonArray("chances");
        float[] chances = new float[array.size()];
        for (int i = 0; i < chances.length; i++) {
            chances[i] = array.get(i).getAsFloat();
        }
        return new TableBonus(condition.get("enchantment").getAsString(), chances);
    }

    @Override
    public boolean test(LootContext context) {
        int level = Enchantments.level(context.tool(), enchantment);
        return context.random().nextFloat() < chances[Math.min(level, chances.length - 1)];
    }
}

package space.nows.tube.server.loot.condition;

import com.google.gson.JsonObject;
import space.nows.tube.server.loot.LootContext;

record EntityProperties(Boolean onFire, String sheepColor, Boolean sheared, Boolean openWater) implements LootCondition {

    private static final String FLAGS = "minecraft:flags";
    private static final String COMPONENTS = "minecraft:components";
    private static final String SHEEP_COLOR = "minecraft:sheep/color";
    private static final String SHEEP = "minecraft:type_specific/sheep";
    private static final String FISHING_HOOK = "minecraft:type_specific/fishing_hook";

    static LootCondition parse(JsonObject condition) {
        if (!"this".equals(condition.get("entity").getAsString())) {
            return context -> false;
        }
        JsonObject predicate = condition.getAsJsonObject("predicate");
        Boolean onFire = null;
        String color = null;
        Boolean sheared = null;
        Boolean openWater = null;
        if (predicate.has(FLAGS) && predicate.getAsJsonObject(FLAGS).has("is_on_fire")) {
            onFire = predicate.getAsJsonObject(FLAGS).get("is_on_fire").getAsBoolean();
        }
        if (predicate.has(COMPONENTS) && predicate.getAsJsonObject(COMPONENTS).has(SHEEP_COLOR)) {
            color = predicate.getAsJsonObject(COMPONENTS).get(SHEEP_COLOR).getAsString();
            sheared = predicate.has(SHEEP) && predicate.getAsJsonObject(SHEEP).get("sheared").getAsBoolean();
        }
        if (predicate.has(FISHING_HOOK)) {
            openWater = predicate.getAsJsonObject(FISHING_HOOK).get("in_open_water").getAsBoolean();
        }
        if (onFire == null && color == null && openWater == null) {
            return context -> false;
        }
        return new EntityProperties(onFire, color, sheared, openWater);
    }

    @Override
    public boolean test(LootContext context) {
        return (onFire == null || onFire == context.onFire())
                && (sheepColor == null || sheepColor.equals(context.sheepColor()) && sheared == context.sheared())
                && (openWater == null || openWater == context.openWater());
    }
}

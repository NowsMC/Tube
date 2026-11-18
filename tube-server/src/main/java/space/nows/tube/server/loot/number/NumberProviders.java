package space.nows.tube.server.loot.number;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class NumberProviders {

    private static final NumberProvider ONE = new Constant(1);

    private NumberProviders() {
    }

    public static NumberProvider one() {
        return ONE;
    }

    public static NumberProvider parse(JsonElement element) {
        if (element == null) {
            return ONE;
        }
        if (element.isJsonPrimitive()) {
            return new Constant(element.getAsDouble());
        }
        JsonObject provider = element.getAsJsonObject();
        String type = provider.has("type") ? provider.get("type").getAsString() : "minecraft:uniform";
        return switch (type) {
            case "minecraft:constant" -> new Constant(provider.get("value").getAsDouble());
            case "minecraft:uniform" -> new Uniform(parse(provider.get("min")), parse(provider.get("max")));
            case "minecraft:binomial" -> new Binomial(parse(provider.get("n")), parse(provider.get("p")));
            default -> ONE;
        };
    }
}

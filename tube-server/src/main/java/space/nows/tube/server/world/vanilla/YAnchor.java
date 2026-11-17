package space.nows.tube.server.world.vanilla;

import com.google.gson.JsonObject;

public final class YAnchor {

    private YAnchor() {
    }

    public static int resolve(JsonObject json, int minY, int height) {
        if (json.has("absolute")) {
            return json.get("absolute").getAsInt();
        }
        if (json.has("above_bottom")) {
            return minY + json.get("above_bottom").getAsInt();
        }
        if (json.has("below_top")) {
            return minY + height - 1 - json.get("below_top").getAsInt();
        }
        throw new IllegalArgumentException("Unknown height anchor " + json);
    }
}

package space.nows.tube.server.world.vanilla.feature.tree;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;

record TreeSize(int lowerLimit, int upperLimit, int lowerSize, int middleSize, int upperSize, int minClippedHeight) {

    static TreeSize read(JsonObject json) {
        int clipped = json.has("min_clipped_height") ? json.get("min_clipped_height").getAsInt() : -1;
        if (WorldgenData.type(json).equals("three_layers_feature_size")) {
            return new TreeSize(intOr(json, "limit", 1), intOr(json, "upper_limit", 1), intOr(json, "lower_size", 0),
                    intOr(json, "middle_size", 1), intOr(json, "upper_size", 1), clipped);
        }
        int upper = intOr(json, "upper_size", 1);
        return new TreeSize(intOr(json, "limit", 1), 0, intOr(json, "lower_size", 0), upper, upper, clipped);
    }

    private static int intOr(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    int clearanceAt(int treeHeight, int y) {
        if (y < lowerLimit) {
            return lowerSize;
        }
        return y >= treeHeight - upperLimit ? upperSize : middleSize;
    }

    boolean acceptsClipped(int freeHeight) {
        return minClippedHeight >= 0 && freeHeight >= minClippedHeight;
    }
}

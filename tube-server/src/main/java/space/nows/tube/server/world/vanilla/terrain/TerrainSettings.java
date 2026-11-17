package space.nows.tube.server.world.vanilla.terrain;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.density.DensityReader;

public record TerrainSettings(int minY, int height, int cellWidth, int cellHeight, int seaLevel, BlockState stone, BlockState sea,
                              boolean groundWater, boolean oreVeins, JsonObject surfaceRule, TerrainRouter router) {

    public static TerrainSettings read(JsonObject json, DensityReader reader) {
        JsonObject noise = json.getAsJsonObject("noise");
        return new TerrainSettings(
                noise.get("min_y").getAsInt(),
                noise.get("height").getAsInt(),
                noise.get("size_horizontal").getAsInt() * 4,
                noise.get("size_vertical").getAsInt() * 4,
                json.get("sea_level").getAsInt(),
                BlockStates.of(json.getAsJsonObject("default_block")),
                BlockStates.of(json.getAsJsonObject("default_fluid")),
                json.get("aquifers_enabled").getAsBoolean(),
                json.get("ore_veins_enabled").getAsBoolean(),
                json.getAsJsonObject("surface_rule"),
                TerrainRouter.read(json.getAsJsonObject("noise_router"), reader));
    }

    public int maxY() {
        return minY + height;
    }
}

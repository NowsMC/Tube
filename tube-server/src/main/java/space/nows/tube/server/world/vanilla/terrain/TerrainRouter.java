package space.nows.tube.server.world.vanilla.terrain;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.density.Density;
import space.nows.tube.server.world.vanilla.density.DensityReader;

public record TerrainRouter(Density barrier, Density floodedness, Density fluidSpread, Density lava, Density temperature,
                            Density humidity, Density continentalness, Density erosion, Density depth, Density weirdness,
                            Density surfaceEstimate, Density finalDensity, Density veinToggle, Density veinRidged,
                            Density veinGap) {

    static TerrainRouter read(JsonObject json, DensityReader reader) {
        return new TerrainRouter(
                reader.read(json.get("barrier")),
                reader.read(json.get("fluid_level_floodedness")),
                reader.read(json.get("fluid_level_spread")),
                reader.read(json.get("lava")),
                reader.read(json.get("temperature")),
                reader.read(json.get("vegetation")),
                reader.read(json.get("continents")),
                reader.read(json.get("erosion")),
                reader.read(json.get("depth")),
                reader.read(json.get("ridges")),
                reader.read(json.get("preliminary_surface_level")),
                reader.read(json.get("final_density")),
                reader.read(json.get("vein_toggle")),
                reader.read(json.get("vein_ridged")),
                reader.read(json.get("vein_gap")));
    }
}

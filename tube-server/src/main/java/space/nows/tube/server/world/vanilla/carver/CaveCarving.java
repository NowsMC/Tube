package space.nows.tube.server.world.vanilla.carver;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.biome.BiomeInfo;
import space.nows.tube.server.world.vanilla.random.LcgRandom;
import space.nows.tube.server.world.vanilla.terrain.ChunkDraft;
import space.nows.tube.server.world.vanilla.terrain.GroundWater;
import space.nows.tube.server.world.vanilla.terrain.TerrainSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CaveCarving {

    private static final int START_RANGE = 8;

    @FunctionalInterface
    public interface StartBiomes {
        BiomeInfo at(int chunkX, int chunkZ);
    }

    private final WorldgenData data;
    private final long seed;
    private final int minY;
    private final int height;
    private final StartBiomes startBiomes;
    private final Map<String, Optional<Carver>> carvers = new HashMap<>();

    public CaveCarving(WorldgenData data, long seed, TerrainSettings settings, StartBiomes startBiomes) {
        this.data = data;
        this.seed = seed;
        this.minY = settings.minY();
        this.height = settings.height();
        this.startBiomes = startBiomes;
    }

    public void carve(ChunkDraft draft, GroundWater water) {
        CarveVolume volume = new CarveVolume(draft, water);
        LcgRandom random = new LcgRandom(0);
        for (int dx = -START_RANGE; dx <= START_RANGE; dx++) {
            for (int dz = -START_RANGE; dz <= START_RANGE; dz++) {
                int startX = draft.chunkX + dx;
                int startZ = draft.chunkZ + dz;
                BiomeInfo biome = startBiomes.at(startX, startZ);
                if (biome != null) {
                    carveFrom(volume, random, biome, startX, startZ);
                }
            }
        }
    }

    private void carveFrom(CarveVolume volume, LcgRandom random, BiomeInfo biome, int startX, int startZ) {
        int index = 0;
        for (String id : biome.carvers()) {
            random.seedForChunk(seed + index, startX, startZ);
            Optional<Carver> carver = carver(id);
            if (carver.isPresent() && random.nextFloat() <= carver.get().settings().chance()) {
                carver.get().carve(volume, random, startX, startZ);
            }
            index++;
        }
    }

    private synchronized Optional<Carver> carver(String id) {
        return carvers.computeIfAbsent(id, this::readCarver);
    }

    private Optional<Carver> readCarver(String id) {
        if (!data.has("configured_carver", id)) {
            return Optional.empty();
        }
        JsonObject json = data.get("configured_carver", id);
        JsonObject config = json.getAsJsonObject("config");
        return switch (WorldgenData.type(json)) {
            case "cave" -> Optional.of(new CaveCarver(config, minY, height));
            case "canyon" -> Optional.of(new RavineCarver(config, minY, height));
            default -> Optional.empty();
        };
    }
}

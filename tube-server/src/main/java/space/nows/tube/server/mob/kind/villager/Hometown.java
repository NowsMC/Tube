package space.nows.tube.server.mob.kind.villager;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.VillagerType;
import net.minestom.server.instance.Instance;

import java.util.HashMap;
import java.util.Map;

public final class Hometown {

    private static final Map<String, VillagerType> BY_BIOME = new HashMap<>();

    static {
        put(VillagerType.DESERT, "desert", "badlands", "eroded_badlands", "wooded_badlands");
        put(VillagerType.JUNGLE, "bamboo_jungle", "jungle", "sparse_jungle");
        put(VillagerType.SAVANNA, "savanna_plateau", "savanna", "windswept_savanna");
        put(VillagerType.SNOW, "deep_frozen_ocean", "frozen_ocean", "frozen_river", "ice_spikes", "snowy_beach", "snowy_plains",
                "snowy_slopes", "snowy_taiga", "grove", "jagged_peaks", "frozen_peaks");
        put(VillagerType.SWAMP, "mangrove_swamp", "swamp");
        put(VillagerType.TAIGA, "old_growth_spruce_taiga", "old_growth_pine_taiga", "windswept_forest", "windswept_gravelly_hills",
                "windswept_hills", "taiga");
    }

    private Hometown() {
    }

    private static void put(VillagerType type, String... biomes) {
        for (String biome : biomes) {
            BY_BIOME.put(biome, type);
        }
    }

    public static VillagerType at(Instance instance, Point at) {
        String biome = instance.getBiome(at).key().value();
        return BY_BIOME.getOrDefault(biome, VillagerType.PLAINS);
    }
}

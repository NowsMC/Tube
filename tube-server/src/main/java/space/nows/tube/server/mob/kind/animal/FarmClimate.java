package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.registry.RegistryKey;

import java.util.Locale;
import java.util.Set;

enum FarmClimate {
    WARM,
    COLD,
    TEMPERATE;

    private static final Set<String> WARM_BIOMES = Set.of(
            "minecraft:desert", "minecraft:warm_ocean", "minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle",
            "minecraft:savanna", "minecraft:savanna_plateau", "minecraft:windswept_savanna", "minecraft:nether_wastes",
            "minecraft:soul_sand_valley", "minecraft:crimson_forest", "minecraft:warped_forest", "minecraft:basalt_deltas",
            "minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands", "minecraft:mangrove_swamp",
            "minecraft:deep_lukewarm_ocean", "minecraft:lukewarm_ocean");
    private static final Set<String> COLD_BIOMES = Set.of(
            "minecraft:snowy_plains", "minecraft:ice_spikes", "minecraft:frozen_peaks", "minecraft:jagged_peaks",
            "minecraft:snowy_slopes", "minecraft:frozen_ocean", "minecraft:deep_frozen_ocean", "minecraft:grove",
            "minecraft:deep_dark", "minecraft:frozen_river", "minecraft:snowy_taiga", "minecraft:snowy_beach", "minecraft:the_end",
            "minecraft:end_highlands", "minecraft:end_midlands", "minecraft:small_end_islands", "minecraft:end_barrens",
            "minecraft:cold_ocean", "minecraft:deep_cold_ocean", "minecraft:old_growth_pine_taiga",
            "minecraft:old_growth_spruce_taiga", "minecraft:taiga", "minecraft:windswept_forest",
            "minecraft:windswept_gravelly_hills", "minecraft:windswept_hills", "minecraft:stony_peaks");

    <T> RegistryKey<T> variant() {
        return RegistryKey.of("minecraft:" + name().toLowerCase(Locale.ROOT));
    }

    static FarmClimate at(Instance instance, Point position) {
        String biome = instance.getBiome(position).key().asString();
        if (WARM_BIOMES.contains(biome)) {
            return WARM;
        }
        return COLD_BIOMES.contains(biome) ? COLD : TEMPERATE;
    }
}

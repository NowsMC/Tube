package space.nows.tube.server.mob.spawn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;

public final class VariantTable {

    private static final List<VariantRule> RULES = List.of(
            new VariantRule("cat_variant", "minecraft:black", 0, Set.of()),
            new VariantRule("cat_variant", "minecraft:british_shorthair", 0, Set.of()),
            new VariantRule("cat_variant", "minecraft:calico", 0, Set.of()),
            new VariantRule("cat_variant", "minecraft:jellie", 0, Set.of()),
            new VariantRule("cat_variant", "minecraft:persian", 0, Set.of()),
            new VariantRule("cat_variant", "minecraft:ragdoll", 0, Set.of()),
            new VariantRule("cat_variant", "minecraft:red", 0, Set.of()),
            new VariantRule("cat_variant", "minecraft:siamese", 0, Set.of()),
            new VariantRule("cat_variant", "minecraft:tabby", 0, Set.of()),
            new VariantRule("cat_variant", "minecraft:white", 0, Set.of()),
            new VariantRule("chicken_variant", "minecraft:cold", 1, Set.of("minecraft:cold_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_dark", "minecraft:deep_frozen_ocean", "minecraft:end_barrens", "minecraft:end_highlands", "minecraft:end_midlands", "minecraft:frozen_ocean", "minecraft:frozen_peaks", "minecraft:frozen_river", "minecraft:grove", "minecraft:ice_spikes", "minecraft:jagged_peaks", "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga", "minecraft:small_end_islands", "minecraft:snowy_beach", "minecraft:snowy_plains", "minecraft:snowy_slopes", "minecraft:snowy_taiga", "minecraft:stony_peaks", "minecraft:taiga", "minecraft:the_end", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:windswept_hills")),
            new VariantRule("chicken_variant", "minecraft:temperate", 0, Set.of()),
            new VariantRule("chicken_variant", "minecraft:warm", 1, Set.of("minecraft:badlands", "minecraft:bamboo_jungle", "minecraft:basalt_deltas", "minecraft:crimson_forest", "minecraft:deep_lukewarm_ocean", "minecraft:desert", "minecraft:eroded_badlands", "minecraft:jungle", "minecraft:lukewarm_ocean", "minecraft:mangrove_swamp", "minecraft:nether_wastes", "minecraft:savanna", "minecraft:savanna_plateau", "minecraft:soul_sand_valley", "minecraft:sparse_jungle", "minecraft:warm_ocean", "minecraft:warped_forest", "minecraft:windswept_savanna", "minecraft:wooded_badlands")),
            new VariantRule("cow_variant", "minecraft:cold", 1, Set.of("minecraft:cold_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_dark", "minecraft:deep_frozen_ocean", "minecraft:end_barrens", "minecraft:end_highlands", "minecraft:end_midlands", "minecraft:frozen_ocean", "minecraft:frozen_peaks", "minecraft:frozen_river", "minecraft:grove", "minecraft:ice_spikes", "minecraft:jagged_peaks", "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga", "minecraft:small_end_islands", "minecraft:snowy_beach", "minecraft:snowy_plains", "minecraft:snowy_slopes", "minecraft:snowy_taiga", "minecraft:stony_peaks", "minecraft:taiga", "minecraft:the_end", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:windswept_hills")),
            new VariantRule("cow_variant", "minecraft:temperate", 0, Set.of()),
            new VariantRule("cow_variant", "minecraft:warm", 1, Set.of("minecraft:badlands", "minecraft:bamboo_jungle", "minecraft:basalt_deltas", "minecraft:crimson_forest", "minecraft:deep_lukewarm_ocean", "minecraft:desert", "minecraft:eroded_badlands", "minecraft:jungle", "minecraft:lukewarm_ocean", "minecraft:mangrove_swamp", "minecraft:nether_wastes", "minecraft:savanna", "minecraft:savanna_plateau", "minecraft:soul_sand_valley", "minecraft:sparse_jungle", "minecraft:warm_ocean", "minecraft:warped_forest", "minecraft:windswept_savanna", "minecraft:wooded_badlands")),
            new VariantRule("frog_variant", "minecraft:cold", 1, Set.of("minecraft:deep_dark", "minecraft:deep_frozen_ocean", "minecraft:end_barrens", "minecraft:end_highlands", "minecraft:end_midlands", "minecraft:frozen_ocean", "minecraft:frozen_peaks", "minecraft:frozen_river", "minecraft:grove", "minecraft:ice_spikes", "minecraft:jagged_peaks", "minecraft:small_end_islands", "minecraft:snowy_beach", "minecraft:snowy_plains", "minecraft:snowy_slopes", "minecraft:snowy_taiga", "minecraft:the_end")),
            new VariantRule("frog_variant", "minecraft:temperate", 0, Set.of()),
            new VariantRule("frog_variant", "minecraft:warm", 1, Set.of("minecraft:badlands", "minecraft:bamboo_jungle", "minecraft:basalt_deltas", "minecraft:crimson_forest", "minecraft:desert", "minecraft:eroded_badlands", "minecraft:jungle", "minecraft:mangrove_swamp", "minecraft:nether_wastes", "minecraft:savanna", "minecraft:savanna_plateau", "minecraft:soul_sand_valley", "minecraft:sparse_jungle", "minecraft:warm_ocean", "minecraft:warped_forest", "minecraft:windswept_savanna", "minecraft:wooded_badlands")),
            new VariantRule("pig_variant", "minecraft:cold", 1, Set.of("minecraft:cold_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_dark", "minecraft:deep_frozen_ocean", "minecraft:end_barrens", "minecraft:end_highlands", "minecraft:end_midlands", "minecraft:frozen_ocean", "minecraft:frozen_peaks", "minecraft:frozen_river", "minecraft:grove", "minecraft:ice_spikes", "minecraft:jagged_peaks", "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga", "minecraft:small_end_islands", "minecraft:snowy_beach", "minecraft:snowy_plains", "minecraft:snowy_slopes", "minecraft:snowy_taiga", "minecraft:stony_peaks", "minecraft:taiga", "minecraft:the_end", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:windswept_hills")),
            new VariantRule("pig_variant", "minecraft:temperate", 0, Set.of()),
            new VariantRule("pig_variant", "minecraft:warm", 1, Set.of("minecraft:badlands", "minecraft:bamboo_jungle", "minecraft:basalt_deltas", "minecraft:crimson_forest", "minecraft:deep_lukewarm_ocean", "minecraft:desert", "minecraft:eroded_badlands", "minecraft:jungle", "minecraft:lukewarm_ocean", "minecraft:mangrove_swamp", "minecraft:nether_wastes", "minecraft:savanna", "minecraft:savanna_plateau", "minecraft:soul_sand_valley", "minecraft:sparse_jungle", "minecraft:warm_ocean", "minecraft:warped_forest", "minecraft:windswept_savanna", "minecraft:wooded_badlands")),
            new VariantRule("wolf_variant", "minecraft:ashen", 1, Set.of("minecraft:snowy_taiga")),
            new VariantRule("wolf_variant", "minecraft:black", 1, Set.of("minecraft:old_growth_pine_taiga")),
            new VariantRule("wolf_variant", "minecraft:chestnut", 1, Set.of("minecraft:old_growth_spruce_taiga")),
            new VariantRule("wolf_variant", "minecraft:pale", 0, Set.of()),
            new VariantRule("wolf_variant", "minecraft:rusty", 1, Set.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle")),
            new VariantRule("wolf_variant", "minecraft:snowy", 1, Set.of("minecraft:grove")),
            new VariantRule("wolf_variant", "minecraft:spotted", 1, Set.of("minecraft:savanna", "minecraft:savanna_plateau", "minecraft:windswept_savanna")),
            new VariantRule("wolf_variant", "minecraft:striped", 1, Set.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands")),
            new VariantRule("wolf_variant", "minecraft:woods", 1, Set.of("minecraft:forest")));

    private VariantTable() {
    }

    public static Optional<String> pick(String registry, String biome, RandomGenerator random) {
        List<VariantRule> best = new ArrayList<>();
        int top = Integer.MIN_VALUE;
        for (VariantRule rule : RULES) {
            if (!rule.registry().equals(registry) || !rule.fits(biome)) {
                continue;
            }
            if (rule.priority() > top) {
                top = rule.priority();
                best.clear();
            }
            if (rule.priority() == top) {
                best.add(rule);
            }
        }
        return best.isEmpty() ? Optional.empty() : Optional.of(best.get(random.nextInt(best.size())).variant());
    }
}

package space.nows.tube.server.mob.spawn;

import java.util.Set;

record VariantRule(String registry, String variant, int priority, Set<String> biomes) {

    boolean fits(String biome) {
        return biomes.isEmpty() || biomes.contains(biome);
    }
}

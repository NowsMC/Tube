package space.nows.tube.server.world.vanilla.feature;

import space.nows.tube.server.world.vanilla.biome.BiomeInfo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

final class FeatureOrder {

    private FeatureOrder() {
    }

    static List<List<String>> of(Map<String, BiomeInfo> biomes) {
        Map<String, BiomeInfo> sorted = new TreeMap<>(biomes);
        int stepCount = sorted.values().stream().mapToInt(biome -> biome.featureSteps().size()).max().orElse(0);
        List<List<String>> steps = new ArrayList<>();
        for (int step = 0; step < stepCount; step++) {
            steps.add(orderStep(sorted, step));
        }
        return List.copyOf(steps);
    }

    private static List<String> orderStep(Map<String, BiomeInfo> biomes, int step) {
        Map<String, Set<String>> followers = new HashMap<>();
        Map<String, Integer> waitingOn = new HashMap<>();
        Set<String> seen = new LinkedHashSet<>();
        for (BiomeInfo biome : biomes.values()) {
            if (step >= biome.featureSteps().size()) {
                continue;
            }
            List<String> list = biome.featureSteps().get(step);
            for (int i = 0; i < list.size(); i++) {
                String feature = list.get(i);
                seen.add(feature);
                waitingOn.putIfAbsent(feature, 0);
                if (i > 0 && followers.computeIfAbsent(list.get(i - 1), key -> new LinkedHashSet<>()).add(feature)) {
                    waitingOn.merge(feature, 1, Integer::sum);
                }
            }
        }
        List<String> order = new ArrayList<>();
        ArrayDeque<String> ready = new ArrayDeque<>();
        seen.stream().filter(feature -> waitingOn.get(feature) == 0).forEach(ready::add);
        while (!ready.isEmpty()) {
            String feature = ready.poll();
            order.add(feature);
            for (String next : followers.getOrDefault(feature, Set.of())) {
                if (waitingOn.merge(next, -1, Integer::sum) == 0) {
                    ready.add(next);
                }
            }
        }
        seen.stream().filter(feature -> !order.contains(feature)).forEach(order::add);
        return List.copyOf(order);
    }
}

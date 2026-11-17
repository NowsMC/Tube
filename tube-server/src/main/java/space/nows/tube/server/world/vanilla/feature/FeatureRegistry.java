package space.nows.tube.server.world.vanilla.feature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.feature.kind.BlockColumnFeature;
import space.nows.tube.server.world.vanilla.feature.kind.BoulderFeature;
import space.nows.tube.server.world.vanilla.feature.kind.ChanceListFeature;
import space.nows.tube.server.world.vanilla.feature.kind.DecorationSource;
import space.nows.tube.server.world.vanilla.feature.kind.DiskFeature;
import space.nows.tube.server.world.vanilla.feature.kind.FreezeFeature;
import space.nows.tube.server.world.vanilla.feature.kind.KelpFeature;
import space.nows.tube.server.world.vanilla.feature.kind.PickOneFeature;
import space.nows.tube.server.world.vanilla.feature.kind.SeagrassFeature;
import space.nows.tube.server.world.vanilla.feature.kind.SequenceFeature;
import space.nows.tube.server.world.vanilla.feature.kind.SimpleBlockFeature;
import space.nows.tube.server.world.vanilla.feature.kind.SpringFeature;
import space.nows.tube.server.world.vanilla.feature.placement.PlacementStep;
import space.nows.tube.server.world.vanilla.feature.placement.PlacementSteps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

public final class FeatureRegistry implements DecorationSource {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private final WorldgenData data;
    private final PlacementSteps steps;
    private final Map<String, BiFunction<JsonObject, DecorationSource, Feature>> kinds = new HashMap<>();
    private final Map<String, Decoration> decorations = new HashMap<>();
    private final Map<String, Feature> features = new HashMap<>();
    private final Set<String> skipped = new TreeSet<>();

    public FeatureRegistry(WorldgenData data, int minY, int height) {
        this.data = data;
        this.steps = new PlacementSteps(minY, height);
        kinds.put("simple_block", (config, source) -> SimpleBlockFeature.read(config));
        kinds.put("block_column", (config, source) -> BlockColumnFeature.read(config));
        kinds.put("random_selector", ChanceListFeature::read);
        kinds.put("simple_random_selector", PickOneFeature::uniform);
        kinds.put("random_boolean_selector", PickOneFeature::coinFlip);
        kinds.put("weighted_random_selector", PickOneFeature::weighted);
        kinds.put("sequence", SequenceFeature::read);
        kinds.put("disk", (config, source) -> DiskFeature.read(config));
        kinds.put("seagrass", (config, source) -> SeagrassFeature.read(config));
        kinds.put("kelp", (config, source) -> new KelpFeature());
        kinds.put("spring_feature", (config, source) -> SpringFeature.read(config));
        kinds.put("freeze_top_layer", (config, source) -> new FreezeFeature());
        kinds.put("block_blob", (config, source) -> BoulderFeature.read(config));
    }

    public synchronized Decoration placed(String id) {
        String key = WorldgenData.key(id);
        Decoration decoration = decorations.get(key);
        if (decoration == null) {
            decoration = readDecoration(data.get("placed_feature", key));
            decorations.put(key, decoration);
        }
        return decoration;
    }

    public synchronized Set<String> skipped() {
        return Set.copyOf(skipped);
    }

    @Override
    public Decoration decoration(JsonElement json) {
        return json.isJsonPrimitive() ? placed(json.getAsString()) : readDecoration(json.getAsJsonObject());
    }

    private Decoration readDecoration(JsonObject json) {
        Feature feature = feature(json.get("feature"));
        List<PlacementStep> chain = new ArrayList<>();
        JsonArray list = json.getAsJsonArray("placement");
        if (list != null) {
            for (JsonElement element : list) {
                try {
                    chain.add(steps.read(element.getAsJsonObject()));
                } catch (IllegalArgumentException e) {
                    skip(e.getMessage());
                    return Decoration.EMPTY;
                }
            }
        }
        return new Decoration(feature, List.copyOf(chain));
    }

    private Feature feature(JsonElement json) {
        if (!json.isJsonPrimitive()) {
            return readFeature(json.getAsJsonObject());
        }
        String key = WorldgenData.key(json.getAsString());
        Feature feature = features.get(key);
        if (feature == null) {
            feature = readFeature(data.get("configured_feature", key));
            features.put(key, feature);
        }
        return feature;
    }

    private Feature readFeature(JsonObject json) {
        String type = WorldgenData.type(json);
        BiFunction<JsonObject, DecorationSource, Feature> kind = kinds.get(type);
        if (kind == null) {
            skip("feature type " + type);
            return Feature.NONE;
        }
        try {
            return kind.apply(json.has("config") ? json.getAsJsonObject("config") : new JsonObject(), this);
        } catch (IllegalArgumentException e) {
            skip(type + ": " + e.getMessage());
            return Feature.NONE;
        }
    }

    private void skip(String reason) {
        if (skipped.add(reason)) {
            LOGGER.debug("World generation skips unsupported {}", reason);
        }
    }
}

package space.nows.tube.server.world.vanilla.feature.tree.decorator;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class TreeDecorators {

    private static final Map<String, Function<JsonObject, TreeDecorator>> KINDS = new HashMap<>();

    static {
        KINDS.put("trunk_vine", json -> new TrunkVines());
        KINDS.put("leave_vine", json -> new HangingVines(json.get("probability").getAsFloat()));
    }

    private TreeDecorators() {
    }

    public static Optional<TreeDecorator> read(JsonObject json) {
        Function<JsonObject, TreeDecorator> reader = KINDS.get(WorldgenData.type(json));
        return reader == null ? Optional.empty() : Optional.of(reader.apply(json));
    }
}

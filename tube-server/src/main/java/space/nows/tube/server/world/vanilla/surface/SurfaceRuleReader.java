package space.nows.tube.server.world.vanilla.surface;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;

import java.util.ArrayList;
import java.util.List;

final class SurfaceRuleReader {

    private final SurfaceConditionReader conditions;
    private final ClayBands bands;

    SurfaceRuleReader(SurfaceConditionReader conditions, ClayBands bands) {
        this.conditions = conditions;
        this.bands = bands;
    }

    SurfaceRule read(JsonObject json) {
        String type = WorldgenData.type(json);
        return switch (type) {
            case "block" -> fixed(BlockStates.of(json.getAsJsonObject("result_state")));
            case "sequence" -> firstMatch(json);
            case "condition" -> guarded(conditions.read(json.getAsJsonObject("if_true")), read(json.getAsJsonObject("then_run")));
            case "bandlands" -> column -> bands.at(column.x, column.y, column.z);
            default -> throw new IllegalArgumentException("Unsupported surface rule " + type);
        };
    }

    private static SurfaceRule fixed(BlockState state) {
        return column -> state;
    }

    private static SurfaceRule guarded(SurfaceCondition condition, SurfaceRule rule) {
        return column -> condition.test(column) ? rule.paint(column) : null;
    }

    private SurfaceRule firstMatch(JsonObject json) {
        List<SurfaceRule> list = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("sequence")) {
            list.add(read(element.getAsJsonObject()));
        }
        SurfaceRule[] rules = list.toArray(SurfaceRule[]::new);
        return column -> {
            for (SurfaceRule rule : rules) {
                BlockState state = rule.paint(column);
                if (state != null) {
                    return state;
                }
            }
            return null;
        };
    }
}

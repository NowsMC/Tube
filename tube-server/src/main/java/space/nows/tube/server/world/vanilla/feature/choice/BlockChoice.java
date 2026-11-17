package space.nows.tube.server.world.vanilla.feature.choice;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;
import space.nows.tube.server.world.vanilla.random.SeededRandom;

@FunctionalInterface
public interface BlockChoice {

    BlockState choose(SeededRandom random, DecorationArea area, int x, int y, int z);

    static BlockChoice read(JsonElement json) {
        JsonObject object = json.getAsJsonObject();
        return BlockChoiceReaders.read(WorldgenData.type(object), object);
    }

    static BlockChoice always(BlockState state) {
        return (random, area, x, y, z) -> state;
    }
}

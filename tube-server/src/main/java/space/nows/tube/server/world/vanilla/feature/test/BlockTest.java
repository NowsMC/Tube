package space.nows.tube.server.world.vanilla.feature.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;

@FunctionalInterface
public interface BlockTest {

    BlockTest ANY = (area, x, y, z) -> true;

    boolean test(DecorationArea area, int x, int y, int z);

    static BlockTest read(JsonElement json) {
        JsonObject object = json.getAsJsonObject();
        BlockTest test = BlockTestReaders.read(WorldgenData.type(object), object);
        JsonArray offset = object.getAsJsonArray("offset");
        if (offset == null) {
            return test;
        }
        int dx = offset.get(0).getAsInt();
        int dy = offset.get(1).getAsInt();
        int dz = offset.get(2).getAsInt();
        return (area, x, y, z) -> test.test(area, x + dx, y + dy, z + dz);
    }
}

package space.nows.tube.server.world.vanilla.feature.test;

import com.google.gson.JsonElement;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.block.BlockTags;

import java.util.HashSet;
import java.util.Set;

public final class BlockNames {

    private BlockNames() {
    }

    public static Set<String> read(JsonElement json) {
        Set<String> names = new HashSet<>();
        if (json.isJsonArray()) {
            json.getAsJsonArray().forEach(element -> names.add(WorldgenData.key(element.getAsString())));
            return names;
        }
        String value = json.getAsString();
        if (value.startsWith("#")) {
            names.addAll(BlockTags.members(WorldgenData.key(value.substring(1))));
        } else {
            names.add(WorldgenData.key(value));
        }
        return names;
    }
}

package space.nows.tube.server.world.vanilla.feature.kind;

import com.google.gson.JsonElement;
import space.nows.tube.server.world.vanilla.feature.Decoration;

@FunctionalInterface
public interface DecorationSource {

    Decoration decoration(JsonElement json);
}

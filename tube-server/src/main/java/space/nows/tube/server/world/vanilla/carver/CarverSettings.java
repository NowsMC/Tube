package space.nows.tube.server.world.vanilla.carver;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.YAnchor;
import space.nows.tube.server.world.vanilla.feature.sample.FloatSampler;
import space.nows.tube.server.world.vanilla.feature.sample.HeightSampler;

record CarverSettings(float chance, HeightSampler y, FloatSampler yScale, int lavaBelow, String carvable) {

    static CarverSettings read(JsonObject config, int minY, int height) {
        return new CarverSettings(
                config.get("probability").getAsFloat(),
                HeightSampler.read(config.get("y"), minY, height),
                FloatSampler.read(config.get("yScale")),
                YAnchor.resolve(config.getAsJsonObject("lava_level"), minY, height),
                WorldgenData.key(config.get("replaceable").getAsString().substring(1)));
    }
}

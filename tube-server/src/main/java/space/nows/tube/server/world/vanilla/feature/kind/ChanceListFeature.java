package space.nows.tube.server.world.vanilla.feature.kind;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.Decoration;
import space.nows.tube.server.world.vanilla.feature.DecorationPass;
import space.nows.tube.server.world.vanilla.feature.Feature;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

public record ChanceListFeature(List<Entry> entries, Decoration fallback) implements Feature {

    public record Entry(float chance, Decoration decoration) {
    }

    public static ChanceListFeature read(JsonObject config, DecorationSource source) {
        List<Entry> entries = new ArrayList<>();
        for (JsonElement element : config.getAsJsonArray("features")) {
            JsonObject entry = element.getAsJsonObject();
            entries.add(new Entry(entry.get("chance").getAsFloat(), source.decoration(entry.get("feature"))));
        }
        return new ChanceListFeature(List.copyOf(entries), source.decoration(config.get("default")));
    }

    @Override
    public boolean place(DecorationPass pass, Xoroshiro random, int x, int y, int z) {
        for (Entry entry : entries) {
            if (random.nextFloat() < entry.chance) {
                return entry.decoration.place(pass, random, x, y, z);
            }
        }
        return fallback.place(pass, random, x, y, z);
    }
}

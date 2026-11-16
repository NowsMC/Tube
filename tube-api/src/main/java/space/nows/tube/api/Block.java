package space.nows.tube.api;

import java.util.Map;
import java.util.Objects;

public record Block(String key, Map<String, String> properties) {

    public static final Block AIR = new Block("minecraft:air", Map.of());

    public Block {
        Objects.requireNonNull(key, "key");
        key = key.indexOf(':') < 0 ? "minecraft:" + key : key;
        properties = Map.copyOf(properties);
    }

    public static Block of(String key) {
        return new Block(key, Map.of());
    }

    public boolean isAir() {
        return key.equals("minecraft:air") || key.equals("minecraft:cave_air") || key.equals("minecraft:void_air");
    }
}

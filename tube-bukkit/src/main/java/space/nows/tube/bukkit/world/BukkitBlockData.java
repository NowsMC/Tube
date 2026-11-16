package space.nows.tube.bukkit.world;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.Map;
import java.util.TreeMap;

public final class BukkitBlockData implements BlockData {

    private final space.nows.tube.api.world.Block tube;

    BukkitBlockData(space.nows.tube.api.world.Block tube) {
        this.tube = tube;
    }

    public static BukkitBlockData parse(String text) {
        String trimmed = text.strip();
        int bracket = trimmed.indexOf('[');
        String key = bracket < 0 ? trimmed : trimmed.substring(0, bracket);
        Map<String, String> properties = new TreeMap<>();
        if (bracket >= 0) {
            if (!trimmed.endsWith("]")) {
                throw new IllegalArgumentException("Could not parse data: " + text);
            }
            String body = trimmed.substring(bracket + 1, trimmed.length() - 1);
            if (!body.isBlank()) {
                for (String pair : body.split(",")) {
                    int equals = pair.indexOf('=');
                    if (equals <= 0) {
                        throw new IllegalArgumentException("Could not parse data: " + text);
                    }
                    properties.put(pair.substring(0, equals).strip(), pair.substring(equals + 1).strip());
                }
            }
        }
        space.nows.tube.api.world.Block block = new space.nows.tube.api.world.Block(key, properties);
        if (Material.matchMaterial(block.key()) == null) {
            throw new IllegalArgumentException("Could not parse data: " + text);
        }
        return new BukkitBlockData(block);
    }

    space.nows.tube.api.world.Block tube() {
        return tube;
    }

    @Override
    public Material getMaterial() {
        Material material = Material.matchMaterial(tube.key());
        return material == null ? Material.AIR : material;
    }

    @Override
    public Material getPlacementMaterial() {
        return getMaterial();
    }

    @Override
    public String getAsString() {
        return getAsString(false);
    }

    @Override
    public String getAsString(boolean hideUnspecified) {
        if (tube.properties().isEmpty()) {
            return tube.key();
        }
        StringBuilder out = new StringBuilder(tube.key()).append('[');
        boolean first = true;
        for (Map.Entry<String, String> entry : new TreeMap<>(tube.properties()).entrySet()) {
            if (!first) {
                out.append(',');
            }
            out.append(entry.getKey()).append('=').append(entry.getValue());
            first = false;
        }
        return out.append(']').toString();
    }

    @Override
    public boolean matches(BlockData other) {
        if (!(other instanceof BukkitBlockData data) || !data.tube.key().equals(tube.key())) {
            return false;
        }
        return data.tube.properties().entrySet().containsAll(tube.properties().entrySet()) || tube.properties().entrySet().containsAll(data.tube.properties().entrySet());
    }

    @Override
    public BlockData clone() {
        return new BukkitBlockData(tube);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BukkitBlockData data && data.tube.equals(tube);
    }

    @Override
    public int hashCode() {
        return tube.hashCode();
    }

    @Override
    public String toString() {
        return "BlockData{" + getAsString() + "}";
    }
}

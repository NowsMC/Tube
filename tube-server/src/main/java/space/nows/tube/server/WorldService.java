package space.nows.tube.server;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import space.nows.tube.api.World;
import space.nows.tube.api.WorldManager;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

final class WorldService implements WorldManager {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_-]+");

    private final Path root;
    private final String defaultName;
    private final Map<String, TubeWorld> worlds = new ConcurrentHashMap<>();
    private final Map<Instance, TubeWorld> byInstance = new ConcurrentHashMap<>();

    WorldService(Path root, String defaultName) {
        this.root = root;
        this.defaultName = defaultName;
    }

    @Override
    public World defaultWorld() {
        return load(defaultName);
    }

    @Override
    public Optional<World> get(String name) {
        return Optional.ofNullable(worlds.get(name));
    }

    @Override
    public World load(String name) {
        if (!VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid world name: " + name);
        }
        return worlds.computeIfAbsent(name, this::open);
    }

    @Override
    public Collection<World> all() {
        return List.copyOf(worlds.values());
    }

    @Override
    public void saveAll() {
        worlds.values().forEach(TubeWorld::save);
    }

    TubeWorld of(Instance instance) {
        TubeWorld world = byInstance.get(instance);
        if (world == null) {
            throw new IllegalStateException("Instance does not belong to a Tube world");
        }
        return world;
    }

    Instance instanceOf(World world) {
        if (world instanceof TubeWorld tubeWorld) {
            return tubeWorld.instance();
        }
        throw new IllegalArgumentException("Unknown world implementation: " + world);
    }

    private TubeWorld open(String name) {
        Path directory = root.resolve(name);
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        YamlConfig settings = YamlConfig.load(directory.resolve("tube-world.yml"));
        if (!settings.contains("spawn.x")) {
            settings.set("spawn.x", 0.5);
            settings.set("spawn.y", 41.0);
            settings.set("spawn.z", 0.5);
            settings.save();
        }
        double[] spawn = {
                settings.getDouble("spawn.x", 0.5),
                settings.getDouble("spawn.y", 41.0),
                settings.getDouble("spawn.z", 0.5)
        };

        InstanceContainer instance = MinecraftServer.getInstanceManager()
                .createInstanceContainer(DimensionType.OVERWORLD, new AnvilLoader(directory, Key.key("minecraft:overworld")));
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));

        TubeWorld world = new TubeWorld(name, instance, spawn);
        byInstance.put(instance, world);
        return world;
    }
}

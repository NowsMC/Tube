package space.nows.tube.server.world;

import ca.spottedleaf.concurrentutil.map.concurrent.objects.ConcurrentChainedReference2ReferenceHashTable;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.world.Environment;
import space.nows.tube.api.world.World;
import space.nows.tube.api.world.WorldGenerator;
import space.nows.tube.api.world.WorldManager;
import space.nows.tube.api.world.WorldOptions;
import space.nows.tube.server.config.FileConfig;
import space.nows.tube.server.entity.MinestomEntity;
import space.nows.tube.server.entity.MinestomItemEntity;
import space.nows.tube.server.entity.MinestomLivingEntity;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.generator.FlatGenerator;
import space.nows.tube.server.world.generator.TerrainGenerator;
import space.nows.tube.server.world.generator.VoidGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongFunction;
import java.util.regex.Pattern;

public final class WorldService implements WorldManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");
    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_-]+");

    private final Path root;
    private final String defaultName;
    private final WorldOptions defaultOptions;
    private final Map<String, MinestomWorld> worlds = new ConcurrentHashMap<>();
    private final Map<String, LongFunction<WorldGenerator>> generators = new ConcurrentHashMap<>();
    private PlayerService players;
    private final ConcurrentChainedReference2ReferenceHashTable<Instance, MinestomWorld> byInstance = ConcurrentChainedReference2ReferenceHashTable.createWithExpected(8);

    public WorldService(Path root, String defaultName) {
        this(root, defaultName, WorldOptions.of(WorldOptions.FLAT, 0));
    }

    public WorldService(Path root, String defaultName, WorldOptions defaultOptions) {
        this.root = root;
        this.defaultName = defaultName;
        this.defaultOptions = defaultOptions;
        generators.put(WorldOptions.FLAT, seed -> new FlatGenerator());
        generators.put(WorldOptions.VOID, seed -> new VoidGenerator());
        generators.put(WorldOptions.NORMAL, TerrainGenerator::new);
    }

    @Override
    public World defaultWorld() {
        return load(defaultName);
    }

    public Optional<World> loadExisting(String name) {
        if (worlds.containsKey(name)) {
            return Optional.of(worlds.get(name));
        }
        if (VALID_NAME.matcher(name).matches() && Files.isDirectory(root.resolve(name))) {
            return Optional.of(load(name));
        }
        return Optional.empty();
    }

    @Override
    public Optional<World> get(String name) {
        return Optional.ofNullable(worlds.get(name));
    }

    @Override
    public World load(String name) {
        return create(name, defaultOptions);
    }

    @Override
    public World create(String name, WorldOptions options) {
        if (!VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid world name: " + name);
        }
        return worlds.computeIfAbsent(name, key -> open(key, options));
    }

    @Override
    public boolean unload(String name, boolean save) {
        if (name.equals(defaultName)) {
            return false;
        }
        MinestomWorld world = worlds.get(name);
        if (world == null || !world.instance().getPlayers().isEmpty()) {
            return false;
        }
        if (save) {
            world.save();
        }
        worlds.remove(name);
        byInstance.remove(world.instance());
        MinecraftServer.getInstanceManager().unregisterInstance(world.instance());
        return true;
    }

    @Override
    public void registerGenerator(String key, LongFunction<WorldGenerator> factory) {
        if (generators.putIfAbsent(key, factory) != null) {
            throw new IllegalArgumentException("Generator already registered: " + key);
        }
    }

    @Override
    public Set<String> generators() {
        return Set.copyOf(generators.keySet());
    }

    @Override
    public Collection<World> all() {
        return List.copyOf(worlds.values());
    }

    @Override
    public void saveAll() {
        worlds.values().forEach(MinestomWorld::save);
    }

    public Path folderOf(String name) {
        return root.resolve(name);
    }

    public void attach(PlayerService players) {
        this.players = players;
    }

    public space.nows.tube.api.entity.Entity wrap(net.minestom.server.entity.Entity entity) {
        if (entity instanceof net.minestom.server.entity.Player player) {
            return players.wrap(player);
        }
        if (entity instanceof net.minestom.server.entity.LivingEntity living) {
            return new MinestomLivingEntity(living, this);
        }
        if (entity instanceof net.minestom.server.entity.ItemEntity item) {
            return new MinestomItemEntity(item, this);
        }
        return new MinestomEntity(entity, this);
    }

    public MinestomWorld of(Instance instance) {
        MinestomWorld world = byInstance.get(instance);
        if (world == null) {
            throw new IllegalStateException("Instance does not belong to a Tube world");
        }
        return world;
    }

    public Instance instanceOf(World world) {
        if (world instanceof MinestomWorld tubeWorld) {
            return tubeWorld.instance();
        }
        throw new IllegalArgumentException("Unknown world implementation: " + world);
    }

    private MinestomWorld open(String name, WorldOptions requested) {
        Path directory = root.resolve(name);
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        FileConfig settings = FileConfig.load(directory.resolve("tube-world.yml"));
        boolean existing = settings.contains("spawn.x");
        WorldOptions options = storedOptions(settings, existing ? WorldOptions.of(WorldOptions.FLAT, 0) : requested);
        WorldGenerator generator = generator(name, options);
        if (!existing) {
            settings.set("generator", options.generator());
            settings.set("seed", Long.toString(options.seed()));
            settings.set("environment", options.environment().name().toLowerCase(java.util.Locale.ROOT));
            settings.set("spawn.x", 0.5);
            settings.set("spawn.y", (double) generator.spawnHeight(0, 0));
            settings.set("spawn.z", 0.5);
            settings.save();
        }
        double[] spawn = {
                settings.getDouble("spawn.x", 0.5),
                settings.getDouble("spawn.y", 41.0),
                settings.getDouble("spawn.z", 0.5)
        };

        InstanceContainer instance = MinecraftServer.getInstanceManager()
                .createInstanceContainer(dimension(options.environment()), new AnvilLoader(directory, Key.key(dimensionKey(options.environment()))));
        instance.setGenerator(unit -> generator.generate(new MinestomChunkBuilder(unit)));

        MinestomWorld world = new MinestomWorld(name, instance, spawn, options, this);
        byInstance.put(instance, world);
        return world;
    }

    private static WorldOptions storedOptions(FileConfig settings, WorldOptions fallback) {
        String generator = settings.getString("generator", fallback.generator());
        long seed = fallback.seed();
        try {
            seed = Long.parseLong(settings.getString("seed", Long.toString(fallback.seed())));
        } catch (NumberFormatException ignored) {
            // a hand-edited seed falls back to the requested one
        }
        Environment environment = fallback.environment();
        try {
            environment = Environment.valueOf(settings.getString("environment", fallback.environment().name()).toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            // an unknown environment falls back to the requested one
        }
        return new WorldOptions(generator, seed, environment);
    }

    private WorldGenerator generator(String world, WorldOptions options) {
        LongFunction<WorldGenerator> factory = generators.get(options.generator());
        if (factory == null) {
            LOGGER.warn("World {} uses the unknown generator {}; new chunks stay empty until it is registered", world, options.generator());
            return new VoidGenerator();
        }
        return factory.apply(options.seed());
    }

    private static RegistryKey<DimensionType> dimension(Environment environment) {
        return switch (environment) {
            case OVERWORLD -> DimensionType.OVERWORLD;
            case NETHER -> DimensionType.THE_NETHER;
            case END -> DimensionType.THE_END;
        };
    }

    private static String dimensionKey(Environment environment) {
        return switch (environment) {
            case OVERWORLD -> "minecraft:overworld";
            case NETHER -> "minecraft:the_nether";
            case END -> "minecraft:the_end";
        };
    }
}

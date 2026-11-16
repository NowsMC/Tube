package space.nows.tube.server.world;

import ca.spottedleaf.concurrentutil.map.concurrent.objects.ConcurrentChainedReference2ReferenceHashTable;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import space.nows.tube.api.world.World;
import space.nows.tube.api.world.WorldManager;
import space.nows.tube.server.config.FileConfig;
import space.nows.tube.server.entity.MinestomEntity;
import space.nows.tube.server.entity.MinestomLivingEntity;
import space.nows.tube.server.player.PlayerService;

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

public final class WorldService implements WorldManager {

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_-]+");

    private final Path root;
    private final String defaultName;
    private final Map<String, MinestomWorld> worlds = new ConcurrentHashMap<>();
    private PlayerService players;
    private final ConcurrentChainedReference2ReferenceHashTable<Instance, MinestomWorld> byInstance = ConcurrentChainedReference2ReferenceHashTable.createWithExpected(8);

    public WorldService(Path root, String defaultName) {
        this.root = root;
        this.defaultName = defaultName;
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
        worlds.values().forEach(MinestomWorld::save);
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

    private MinestomWorld open(String name) {
        Path directory = root.resolve(name);
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        FileConfig settings = FileConfig.load(directory.resolve("tube-world.yml"));
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

        MinestomWorld world = new MinestomWorld(name, instance, spawn, this);
        byInstance.put(instance, world);
        return world;
    }
}

package space.nows.tube.api.world;

import space.nows.tube.api.world.generation.WorldGenerator;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongFunction;

public interface WorldManager {

    World defaultWorld();

    Optional<World> get(String name);

    World load(String name);

    World create(String name, WorldOptions options);

    boolean unload(String name, boolean save);

    void registerGenerator(String key, LongFunction<WorldGenerator> factory);

    Set<String> generators();

    Collection<World> all();

    void saveAll();
}

package space.nows.tube.api.world;

import java.util.Collection;
import java.util.Optional;

public interface WorldManager {

    World defaultWorld();

    Optional<World> get(String name);

    World load(String name);

    Collection<World> all();

    void saveAll();
}

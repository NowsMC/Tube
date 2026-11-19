package space.nows.tube.server.world.vanilla;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.nows.tube.api.world.generation.WorldGenerator;
import space.nows.tube.server.vanilla.MojangServerJar;
import space.nows.tube.server.world.generator.TerrainGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.LongFunction;

public final class WorldgenLoader implements LongFunction<WorldGenerator> {

    private static final Logger LOGGER = LoggerFactory.getLogger("tube");

    private final Path cache;
    private final String version;
    private WorldgenData data;
    private boolean unavailable;

    public WorldgenLoader(Path cache, String version) {
        this.cache = cache;
        this.version = version;
    }

    @Override
    public synchronized WorldGenerator apply(long seed) {
        if (data == null && !unavailable) {
            load();
        }
        return data == null ? new TerrainGenerator(seed) : new VanillaGenerator(data, seed);
    }

    private void load() {
        try {
            data = WorldgenData.read(MojangServerJar.extracted(cache, version, MojangServerJar.WORLDGEN));
        } catch (IOException | InterruptedException | RuntimeException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            unavailable = true;
            LOGGER.warn("Could not load the vanilla world generation data for Minecraft {}; normal worlds use Tube's simple terrain: {}",
                    version, e.toString());
        }
    }
}

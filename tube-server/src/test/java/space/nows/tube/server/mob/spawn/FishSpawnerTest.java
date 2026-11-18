package space.nows.tube.server.mob.spawn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.registry.RegistryKey;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

@EnvTest
class FishSpawnerTest {

    @Test
    void riversHoldSalmonAndOpenOceansHoldCod(Env env) {
        Instance instance = env.createFlatInstance();
        instance.loadChunk(0, 0).join();
        instance.setBiome(0, 45, 0, RegistryKey.of("minecraft:river"));
        instance.setBiome(8, 45, 0, RegistryKey.of("minecraft:ocean"));
        instance.setBiome(12, 45, 0, RegistryKey.of("minecraft:plains"));

        assertEquals(EntityType.SALMON, FishSpawner.species(instance, 0, 45, 0, ThreadLocalRandom.current()));
        assertEquals(EntityType.COD, FishSpawner.species(instance, 8, 45, 0, ThreadLocalRandom.current()));
        assertNull(FishSpawner.species(instance, 12, 45, 0, ThreadLocalRandom.current()));
    }
}

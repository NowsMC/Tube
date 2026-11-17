package space.nows.tube.server.world.vanilla.biome;

import space.nows.tube.server.world.vanilla.density.Density;
import space.nows.tube.server.world.vanilla.terrain.ChunkDensity;
import space.nows.tube.server.world.vanilla.terrain.ChunkDraft;
import space.nows.tube.server.world.vanilla.terrain.TerrainRouter;

public final class BiomeSampler {

    private final BiomeTree tree;
    private final TerrainRouter router;

    public BiomeSampler(BiomeTree tree, TerrainRouter router) {
        this.tree = tree;
        this.router = router;
    }

    public void fill(ChunkDraft draft, ChunkDensity chunk) {
        Density temperature = chunk.localize(router.temperature());
        Density humidity = chunk.localize(router.humidity());
        Density continentalness = chunk.localize(router.continentalness());
        Density erosion = chunk.localize(router.erosion());
        Density depth = chunk.localize(router.depth());
        Density weirdness = chunk.localize(router.weirdness());
        BiomeTree.Cursor cursor = tree.cursor();
        for (int quartX = 0; quartX < 4; quartX++) {
            for (int quartZ = 0; quartZ < 4; quartZ++) {
                int x = draft.minX() + (quartX << 2);
                int z = draft.minZ() + (quartZ << 2);
                long[] target = {
                        quantize(temperature, x, 0, z),
                        quantize(humidity, x, 0, z),
                        quantize(continentalness, x, 0, z),
                        quantize(erosion, x, 0, z),
                        0,
                        quantize(weirdness, x, 0, z)
                };
                for (int quartY = 0; quartY < draft.quartHeight(); quartY++) {
                    target[4] = quantize(depth, x, draft.minY + (quartY << 2), z);
                    draft.setQuartBiome(quartX, quartY, quartZ, cursor.closest(target));
                }
            }
        }
    }

    public String at(int x, int y, int z) {
        long[] target = {
                quantize(router.temperature(), x, y, z),
                quantize(router.humidity(), x, y, z),
                quantize(router.continentalness(), x, y, z),
                quantize(router.erosion(), x, y, z),
                quantize(router.depth(), x, y, z),
                quantize(router.weirdness(), x, y, z)
        };
        return tree.cursor().closest(target);
    }

    private static long quantize(Density density, int x, int y, int z) {
        return ClimateRange.quantize((float) density.at(x, y, z));
    }
}

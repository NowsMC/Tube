package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.density.Density;
import space.nows.tube.server.world.vanilla.random.PositionalRandom;

public final class TerrainBuilder {

    public interface Stage {
        void apply(ChunkDraft draft, ChunkDensity density, GroundWater water);
    }

    private final TerrainSettings settings;
    private final PositionalRandom waterRandom;
    private final PositionalRandom veinRandom;
    private final Stage[] stages;

    public TerrainBuilder(TerrainSettings settings, PositionalRandom waterRandom, PositionalRandom veinRandom, Stage... stages) {
        this.settings = settings;
        this.waterRandom = waterRandom;
        this.veinRandom = veinRandom;
        this.stages = stages;
    }

    public ChunkDraft build(int chunkX, int chunkZ) {
        ChunkDensity density = new ChunkDensity(settings, chunkX, chunkZ);
        GroundWater water = new GroundWater(settings, density, waterRandom);
        ChunkDraft draft = new ChunkDraft(chunkX, chunkZ, settings.minY(), settings.height());
        fill(draft, density, water);
        for (Stage stage : stages) {
            stage.apply(draft, density, water);
        }
        return draft;
    }

    private void fill(ChunkDraft draft, ChunkDensity density, GroundWater water) {
        Density shape = density.localize(settings.router().finalDensity());
        OreVeins veins = settings.oreVeins() ? new OreVeins(settings, density, veinRandom) : null;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = draft.minX() + x;
                int worldZ = draft.minZ() + z;
                for (int y = settings.minY(); y < settings.maxY(); y++) {
                    BlockState state = blockAt(shape, water, veins, worldX, y, worldZ);
                    if (!state.air) {
                        draft.set(x, y, z, state);
                    }
                }
            }
        }
    }

    private BlockState blockAt(Density shape, GroundWater water, OreVeins veins, int x, int y, int z) {
        BlockState open = water.fill(x, y, z, shape.at(x, y, z));
        if (open != null) {
            return open;
        }
        BlockState vein = veins == null ? null : veins.fill(x, y, z);
        return vein != null ? vein : settings.stone();
    }
}

package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.density.CacheHint;
import space.nows.tube.server.world.vanilla.density.Density;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ChunkDensity {

    final int minX;
    final int minZ;
    final int minY;
    final int height;
    final int cellWidth;
    final int cellHeight;
    private final Map<Density, Density> localized = new IdentityHashMap<>();
    private final Map<Long, Integer> surfaceEstimates = new HashMap<>();
    private final Density surfaceEstimate;

    public ChunkDensity(TerrainSettings settings, int chunkX, int chunkZ) {
        this.minX = chunkX << 4;
        this.minZ = chunkZ << 4;
        this.minY = settings.minY();
        this.height = settings.height();
        this.cellWidth = settings.cellWidth();
        this.cellHeight = settings.cellHeight();
        this.surfaceEstimate = localize(settings.router().surfaceEstimate());
    }

    public Density localize(Density density) {
        Density done = localized.get(density);
        if (done != null) {
            return done;
        }
        Density rebuilt = density.rebuild(this::localize);
        if (rebuilt instanceof CacheHint hint) {
            rebuilt = switch (hint.kind()) {
                case CELL_INTERPOLATED -> new CellInterpolated(hint.input(), this);
                case QUART_COLUMN -> new QuartColumnCache(hint.input(), this);
                case COLUMN -> new ColumnCache(hint.input());
                case POINT -> new PointCache(hint.input());
                case CELL -> hint.input();
            };
        }
        localized.put(density, rebuilt);
        return rebuilt;
    }

    public int surfaceEstimate(int x, int z) {
        int quartX = x >> 2 << 2;
        int quartZ = z >> 2 << 2;
        long key = (long) quartX << 32 | quartZ & 0xFFFFFFFFL;
        Integer level = surfaceEstimates.get(key);
        if (level == null) {
            level = (int) Math.floor(surfaceEstimate.at(quartX, 0, quartZ));
            surfaceEstimates.put(key, level);
        }
        return level;
    }

    boolean contains(int x, int y, int z) {
        int localX = x - minX;
        int localY = y - minY;
        int localZ = z - minZ;
        return localX >= 0 && localX < 16 && localZ >= 0 && localZ < 16 && localY >= 0 && localY < height;
    }
}

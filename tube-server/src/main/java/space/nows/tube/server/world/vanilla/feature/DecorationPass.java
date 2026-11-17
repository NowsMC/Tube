package space.nows.tube.server.world.vanilla.feature;

public final class DecorationPass {

    private static final int REACH = 16;

    public final DecorationArea area;
    private final int targetMinX;
    private final int targetMinZ;
    private String current;

    public DecorationPass(DecorationArea area, int targetChunkX, int targetChunkZ) {
        this.area = area;
        this.targetMinX = targetChunkX << 4;
        this.targetMinZ = targetChunkZ << 4;
    }

    void start(String placedFeature) {
        this.current = placedFeature;
    }

    public String current() {
        return current;
    }

    boolean reaches(int x, int z) {
        return x >= targetMinX - REACH && x < targetMinX + 16 + REACH && z >= targetMinZ - REACH && z < targetMinZ + 16 + REACH;
    }
}

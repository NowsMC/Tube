package space.nows.tube.server.world.vanilla.biome;

public record ClimateSpot(String biome, ClimateRange temperature, ClimateRange humidity, ClimateRange continentalness,
                          ClimateRange erosion, ClimateRange depth, ClimateRange weirdness) {

    static final int AXES = 7;

    long[] bounds() {
        ClimateRange[] ranges = {temperature, humidity, continentalness, erosion, depth, weirdness, ClimateRange.at(0.0F)};
        long[] bounds = new long[AXES * 2];
        for (int i = 0; i < AXES; i++) {
            bounds[i * 2] = ranges[i].min();
            bounds[i * 2 + 1] = ranges[i].max();
        }
        return bounds;
    }
}

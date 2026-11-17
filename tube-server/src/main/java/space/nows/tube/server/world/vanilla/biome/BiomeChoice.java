package space.nows.tube.server.world.vanilla.biome;

enum BiomeChoice {
    MIDDLE,
    MIDDLE_OR_BADLANDS,
    MIDDLE_OR_BADLANDS_OR_SLOPE,
    MIDDLE_OR_SAVANNA,
    PLATEAU,
    SHATTERED,
    SHATTERED_OR_SAVANNA,
    SHATTERED_COAST,
    PEAK,
    SLOPE,
    SLOPE_OR_PLATEAU,
    BEACH;

    private static final String[][] MIDDLE_TABLE = {
            {"snowy_plains", "snowy_plains", "snowy_plains", "snowy_taiga", "taiga"},
            {"plains", "plains", "forest", "taiga", "old_growth_spruce_taiga"},
            {"flower_forest", "plains", "forest", "birch_forest", "dark_forest"},
            {"savanna", "savanna", "forest", "jungle", "jungle"},
            {"desert", "desert", "desert", "desert", "desert"}
    };
    private static final String[][] MIDDLE_WEIRD = {
            {"ice_spikes", null, "snowy_taiga", null, null},
            {null, null, null, null, "old_growth_pine_taiga"},
            {"sunflower_plains", null, null, "old_growth_birch_forest", null},
            {null, null, "plains", "sparse_jungle", "bamboo_jungle"},
            {null, null, null, null, null}
    };
    private static final String[][] PLATEAU_TABLE = {
            {"snowy_plains", "snowy_plains", "snowy_plains", "snowy_taiga", "snowy_taiga"},
            {"meadow", "meadow", "forest", "taiga", "old_growth_spruce_taiga"},
            {"meadow", "meadow", "meadow", "meadow", "dark_forest"},
            {"savanna_plateau", "savanna_plateau", "forest", "forest", "jungle"},
            {"badlands", "badlands", "badlands", "wooded_badlands", "wooded_badlands"}
    };
    private static final String[][] PLATEAU_WEIRD = {
            {"ice_spikes", null, null, null, null},
            {"cherry_grove", null, "meadow", "meadow", "old_growth_pine_taiga"},
            {"cherry_grove", "cherry_grove", "forest", "birch_forest", "pale_garden"},
            {null, null, null, null, null},
            {"eroded_badlands", "eroded_badlands", null, null, null}
    };
    private static final String[][] SHATTERED_TABLE = {
            {"windswept_gravelly_hills", "windswept_gravelly_hills", "windswept_hills", "windswept_forest", "windswept_forest"},
            {"windswept_gravelly_hills", "windswept_gravelly_hills", "windswept_hills", "windswept_forest", "windswept_forest"},
            {"windswept_hills", "windswept_hills", "windswept_hills", "windswept_forest", "windswept_forest"},
            {null, null, null, null, null},
            {null, null, null, null, null}
    };

    private static final int FROZEN = 0;
    private static final int HOT = 4;

    String pick(int temperature, int humidity, boolean weird) {
        return switch (this) {
            case MIDDLE -> middle(temperature, humidity, weird);
            case MIDDLE_OR_BADLANDS -> temperature == HOT ? badlands(humidity, weird) : middle(temperature, humidity, weird);
            case MIDDLE_OR_BADLANDS_OR_SLOPE -> temperature == FROZEN ? SLOPE.pick(temperature, humidity, weird)
                    : MIDDLE_OR_BADLANDS.pick(temperature, humidity, weird);
            case MIDDLE_OR_SAVANNA -> savannaOr(temperature, humidity, weird, middle(temperature, humidity, weird));
            case PLATEAU -> weirdOr(PLATEAU_WEIRD, PLATEAU_TABLE, temperature, humidity, weird);
            case SHATTERED -> shattered(temperature, humidity, weird);
            case SHATTERED_OR_SAVANNA -> savannaOr(temperature, humidity, weird, shattered(temperature, humidity, weird));
            case SHATTERED_COAST -> savannaOr(temperature, humidity, weird, weird ? middle(temperature, humidity, true) : beach(temperature));
            case PEAK -> peak(temperature, humidity, weird);
            case SLOPE -> temperature >= 3 ? PLATEAU.pick(temperature, humidity, weird) : humidity <= 1 ? "snowy_slopes" : "grove";
            case SLOPE_OR_PLATEAU -> temperature == FROZEN ? SLOPE.pick(temperature, humidity, weird) : PLATEAU.pick(temperature, humidity, weird);
            case BEACH -> beach(temperature);
        };
    }

    private static String middle(int temperature, int humidity, boolean weird) {
        return weirdOr(MIDDLE_WEIRD, MIDDLE_TABLE, temperature, humidity, weird);
    }

    private static String weirdOr(String[][] weirdTable, String[][] table, int temperature, int humidity, boolean weird) {
        String variant = weird ? weirdTable[temperature][humidity] : null;
        return variant != null ? variant : table[temperature][humidity];
    }

    private static String shattered(int temperature, int humidity, boolean weird) {
        String biome = SHATTERED_TABLE[temperature][humidity];
        return biome != null ? biome : middle(temperature, humidity, weird);
    }

    private static String savannaOr(int temperature, int humidity, boolean weird, String fallback) {
        return temperature > 1 && humidity < 4 && weird ? "windswept_savanna" : fallback;
    }

    private static String beach(int temperature) {
        return switch (temperature) {
            case FROZEN -> "snowy_beach";
            case HOT -> "desert";
            default -> "beach";
        };
    }

    private static String badlands(int humidity, boolean weird) {
        if (humidity < 2) {
            return weird ? "eroded_badlands" : "badlands";
        }
        return humidity < 3 ? "badlands" : "wooded_badlands";
    }

    private static String peak(int temperature, int humidity, boolean weird) {
        if (temperature <= 2) {
            return weird ? "frozen_peaks" : "jagged_peaks";
        }
        return temperature == 3 ? "stony_peaks" : badlands(humidity, weird);
    }
}

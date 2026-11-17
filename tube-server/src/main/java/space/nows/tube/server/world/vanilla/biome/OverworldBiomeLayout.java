package space.nows.tube.server.world.vanilla.biome;

import java.util.ArrayList;
import java.util.List;

import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.BEACH;
import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.MIDDLE;
import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.MIDDLE_OR_BADLANDS;
import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.MIDDLE_OR_BADLANDS_OR_SLOPE;
import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.MIDDLE_OR_SAVANNA;
import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.PEAK;
import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.PLATEAU;
import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.SHATTERED;
import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.SHATTERED_COAST;
import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.SHATTERED_OR_SAVANNA;
import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.SLOPE;
import static space.nows.tube.server.world.vanilla.biome.BiomeChoice.SLOPE_OR_PLATEAU;

public final class OverworldBiomeLayout {

    private static final ClimateRange FULL = ClimateRange.FULL;
    private static final ClimateRange[] TEMPERATURE = {
            ClimateRange.of(-1.0F, -0.45F), ClimateRange.of(-0.45F, -0.15F), ClimateRange.of(-0.15F, 0.2F),
            ClimateRange.of(0.2F, 0.55F), ClimateRange.of(0.55F, 1.0F)
    };
    private static final ClimateRange[] HUMIDITY = {
            ClimateRange.of(-1.0F, -0.35F), ClimateRange.of(-0.35F, -0.1F), ClimateRange.of(-0.1F, 0.1F),
            ClimateRange.of(0.1F, 0.3F), ClimateRange.of(0.3F, 1.0F)
    };
    private static final ClimateRange[] EROSION = {
            ClimateRange.of(-1.0F, -0.78F), ClimateRange.of(-0.78F, -0.375F), ClimateRange.of(-0.375F, -0.2225F),
            ClimateRange.of(-0.2225F, 0.05F), ClimateRange.of(0.05F, 0.45F), ClimateRange.of(0.45F, 0.55F), ClimateRange.of(0.55F, 1.0F)
    };
    private static final ClimateRange FROZEN = TEMPERATURE[0];
    private static final ClimateRange THAWED = TEMPERATURE[1].to(TEMPERATURE[4]);
    private static final ClimateRange COOL = TEMPERATURE[1].to(TEMPERATURE[2]);
    private static final ClimateRange WARM = TEMPERATURE[3].to(TEMPERATURE[4]);
    private static final ClimateRange MUSHROOM_ISLANDS = ClimateRange.of(-1.2F, -1.05F);
    private static final ClimateRange DEEP_OCEAN = ClimateRange.of(-1.05F, -0.455F);
    private static final ClimateRange OCEAN = ClimateRange.of(-0.455F, -0.19F);
    private static final ClimateRange COAST = ClimateRange.of(-0.19F, -0.11F);
    private static final ClimateRange INLAND = ClimateRange.of(-0.11F, 0.55F);
    private static final ClimateRange NEAR = ClimateRange.of(-0.11F, 0.03F);
    private static final ClimateRange MID = ClimateRange.of(0.03F, 0.3F);
    private static final ClimateRange FAR = ClimateRange.of(0.3F, 1.0F);
    private static final ClimateRange COAST_TO_NEAR = COAST.to(NEAR);
    private static final ClimateRange COAST_TO_FAR = COAST.to(FAR);
    private static final ClimateRange NEAR_TO_MID = NEAR.to(MID);
    private static final ClimateRange NEAR_TO_FAR = NEAR.to(FAR);
    private static final ClimateRange MID_TO_FAR = MID.to(FAR);
    private static final ClimateRange INLAND_TO_FAR = INLAND.to(FAR);

    private static final String[] OCEANS = {"frozen_ocean", "cold_ocean", "ocean", "lukewarm_ocean", "warm_ocean"};
    private static final String[] DEEP_OCEANS = {"deep_frozen_ocean", "deep_cold_ocean", "deep_ocean", "deep_lukewarm_ocean", "warm_ocean"};

    private enum When {
        ALWAYS, NEGATIVE, NON_NEGATIVE, FROZEN_ONLY;

        boolean holds(ClimateRange weirdness, int temperature) {
            return switch (this) {
                case ALWAYS -> true;
                case NEGATIVE -> weirdness.negative();
                case NON_NEGATIVE -> !weirdness.negative();
                case FROZEN_ONLY -> temperature == 0;
            };
        }
    }

    private record Row(ClimateRange continentalness, ClimateRange erosion, BiomeChoice choice, When when) {
        Row(ClimateRange continentalness, ClimateRange erosion, BiomeChoice choice) {
            this(continentalness, erosion, choice, When.ALWAYS);
        }
    }

    private record Fixed(ClimateRange temperature, ClimateRange continentalness, ClimateRange erosion, String biome, String negativeBiome) {
        Fixed(ClimateRange temperature, ClimateRange continentalness, ClimateRange erosion, String biome) {
            this(temperature, continentalness, erosion, biome, biome);
        }
    }

    private record Slice(List<Fixed> fixed, List<Row> rows) {
    }

    private static final List<Fixed> LOWLAND_FIXED = List.of(
            new Fixed(FULL, COAST, EROSION[0].to(EROSION[2]), "stony_shore"),
            new Fixed(COOL, NEAR_TO_FAR, EROSION[6], "swamp"),
            new Fixed(WARM, NEAR_TO_FAR, EROSION[6], "mangrove_swamp"));

    private static final Slice PEAKS = new Slice(List.of(), List.of(
            new Row(COAST_TO_FAR, EROSION[0], PEAK),
            new Row(COAST_TO_NEAR, EROSION[1], MIDDLE_OR_BADLANDS_OR_SLOPE),
            new Row(MID_TO_FAR, EROSION[1], PEAK),
            new Row(COAST_TO_NEAR, EROSION[2].to(EROSION[3]), MIDDLE),
            new Row(MID_TO_FAR, EROSION[2], PLATEAU),
            new Row(MID, EROSION[3], MIDDLE_OR_BADLANDS),
            new Row(FAR, EROSION[3], PLATEAU),
            new Row(COAST_TO_FAR, EROSION[4], MIDDLE),
            new Row(COAST_TO_NEAR, EROSION[5], SHATTERED_OR_SAVANNA),
            new Row(MID_TO_FAR, EROSION[5], SHATTERED),
            new Row(COAST_TO_FAR, EROSION[6], MIDDLE)));

    private static final Slice HIGHLANDS = new Slice(List.of(), List.of(
            new Row(COAST, EROSION[0].to(EROSION[1]), MIDDLE),
            new Row(NEAR, EROSION[0], SLOPE),
            new Row(MID_TO_FAR, EROSION[0], PEAK),
            new Row(NEAR, EROSION[1], MIDDLE_OR_BADLANDS_OR_SLOPE),
            new Row(MID_TO_FAR, EROSION[1], SLOPE),
            new Row(COAST_TO_NEAR, EROSION[2].to(EROSION[3]), MIDDLE),
            new Row(MID_TO_FAR, EROSION[2], PLATEAU),
            new Row(MID, EROSION[3], MIDDLE_OR_BADLANDS),
            new Row(FAR, EROSION[3], PLATEAU),
            new Row(COAST_TO_FAR, EROSION[4], MIDDLE),
            new Row(COAST_TO_NEAR, EROSION[5], MIDDLE_OR_SAVANNA),
            new Row(MID_TO_FAR, EROSION[5], SHATTERED),
            new Row(COAST_TO_FAR, EROSION[6], MIDDLE)));

    private static final Slice MIDLANDS = new Slice(LOWLAND_FIXED, List.of(
            new Row(NEAR_TO_FAR, EROSION[0], SLOPE),
            new Row(NEAR_TO_MID, EROSION[1], MIDDLE_OR_BADLANDS_OR_SLOPE),
            new Row(FAR, EROSION[1], SLOPE_OR_PLATEAU),
            new Row(NEAR, EROSION[2], MIDDLE),
            new Row(MID, EROSION[2], MIDDLE_OR_BADLANDS),
            new Row(FAR, EROSION[2], PLATEAU),
            new Row(COAST_TO_NEAR, EROSION[3], MIDDLE),
            new Row(MID_TO_FAR, EROSION[3], MIDDLE_OR_BADLANDS),
            new Row(COAST, EROSION[4], BEACH, When.NEGATIVE),
            new Row(NEAR_TO_FAR, EROSION[4], MIDDLE, When.NEGATIVE),
            new Row(COAST_TO_FAR, EROSION[4], MIDDLE, When.NON_NEGATIVE),
            new Row(COAST, EROSION[5], SHATTERED_COAST),
            new Row(NEAR, EROSION[5], SHATTERED_OR_SAVANNA),
            new Row(MID_TO_FAR, EROSION[5], SHATTERED),
            new Row(COAST, EROSION[6], BEACH, When.NEGATIVE),
            new Row(COAST, EROSION[6], MIDDLE, When.NON_NEGATIVE),
            new Row(NEAR_TO_FAR, EROSION[6], MIDDLE, When.FROZEN_ONLY)));

    private static final Slice LOWLANDS = new Slice(LOWLAND_FIXED, List.of(
            new Row(NEAR, EROSION[0].to(EROSION[1]), MIDDLE_OR_BADLANDS),
            new Row(MID_TO_FAR, EROSION[0].to(EROSION[1]), MIDDLE_OR_BADLANDS_OR_SLOPE),
            new Row(NEAR, EROSION[2].to(EROSION[3]), MIDDLE),
            new Row(MID_TO_FAR, EROSION[2].to(EROSION[3]), MIDDLE_OR_BADLANDS),
            new Row(COAST, EROSION[3].to(EROSION[4]), BEACH),
            new Row(NEAR_TO_FAR, EROSION[4], MIDDLE),
            new Row(COAST, EROSION[5], SHATTERED_COAST),
            new Row(NEAR, EROSION[5], MIDDLE_OR_SAVANNA),
            new Row(MID_TO_FAR, EROSION[5], MIDDLE),
            new Row(COAST, EROSION[6], BEACH),
            new Row(NEAR_TO_FAR, EROSION[6], MIDDLE, When.FROZEN_ONLY)));

    private static final Slice VALLEYS = new Slice(List.of(
            new Fixed(FROZEN, COAST, EROSION[0].to(EROSION[1]), "frozen_river", "stony_shore"),
            new Fixed(THAWED, COAST, EROSION[0].to(EROSION[1]), "river", "stony_shore"),
            new Fixed(FROZEN, NEAR, EROSION[0].to(EROSION[1]), "frozen_river"),
            new Fixed(THAWED, NEAR, EROSION[0].to(EROSION[1]), "river"),
            new Fixed(FROZEN, COAST_TO_FAR, EROSION[2].to(EROSION[5]), "frozen_river"),
            new Fixed(THAWED, COAST_TO_FAR, EROSION[2].to(EROSION[5]), "river"),
            new Fixed(FROZEN, COAST, EROSION[6], "frozen_river"),
            new Fixed(THAWED, COAST, EROSION[6], "river"),
            new Fixed(COOL, INLAND_TO_FAR, EROSION[6], "swamp"),
            new Fixed(WARM, INLAND_TO_FAR, EROSION[6], "mangrove_swamp"),
            new Fixed(FROZEN, INLAND_TO_FAR, EROSION[6], "frozen_river")), List.of(
            new Row(MID_TO_FAR, EROSION[0].to(EROSION[1]), MIDDLE_OR_BADLANDS)));

    private record Band(float from, float to, Slice slice) {
    }

    private static final List<Band> WEIRDNESS_BANDS = List.of(
            new Band(-1.0F, -0.93333334F, MIDLANDS),
            new Band(-0.93333334F, -0.7666667F, HIGHLANDS),
            new Band(-0.7666667F, -0.56666666F, PEAKS),
            new Band(-0.56666666F, -0.4F, HIGHLANDS),
            new Band(-0.4F, -0.26666668F, MIDLANDS),
            new Band(-0.26666668F, -0.05F, LOWLANDS),
            new Band(-0.05F, 0.05F, VALLEYS),
            new Band(0.05F, 0.26666668F, LOWLANDS),
            new Band(0.26666668F, 0.4F, MIDLANDS),
            new Band(0.4F, 0.56666666F, HIGHLANDS),
            new Band(0.56666666F, 0.7666667F, PEAKS),
            new Band(0.7666667F, 0.93333334F, HIGHLANDS),
            new Band(0.93333334F, 1.0F, MIDLANDS));

    private final List<ClimateSpot> spots = new ArrayList<>();

    private OverworldBiomeLayout() {
    }

    public static List<ClimateSpot> spots() {
        OverworldBiomeLayout layout = new OverworldBiomeLayout();
        layout.addOceans();
        for (Band band : WEIRDNESS_BANDS) {
            layout.addSlice(band.slice, ClimateRange.of(band.from, band.to));
        }
        layout.addCaves();
        return List.copyOf(layout.spots);
    }

    private void addOceans() {
        surface("mushroom_fields", FULL, FULL, MUSHROOM_ISLANDS, FULL, FULL);
        for (int t = 0; t < TEMPERATURE.length; t++) {
            surface(DEEP_OCEANS[t], TEMPERATURE[t], FULL, DEEP_OCEAN, FULL, FULL);
            surface(OCEANS[t], TEMPERATURE[t], FULL, OCEAN, FULL, FULL);
        }
    }

    private void addSlice(Slice slice, ClimateRange weirdness) {
        for (Fixed fixed : slice.fixed) {
            String biome = weirdness.negative() ? fixed.negativeBiome : fixed.biome;
            surface(biome, fixed.temperature, FULL, fixed.continentalness, fixed.erosion, weirdness);
        }
        for (int t = 0; t < TEMPERATURE.length; t++) {
            for (int h = 0; h < HUMIDITY.length; h++) {
                for (Row row : slice.rows) {
                    if (row.when.holds(weirdness, t)) {
                        String biome = row.choice.pick(t, h, !weirdness.negative());
                        surface(biome, TEMPERATURE[t], HUMIDITY[h], row.continentalness, row.erosion, weirdness);
                    }
                }
            }
        }
    }

    private void addCaves() {
        ClimateRange underground = ClimateRange.of(0.2F, 0.9F);
        add("dripstone_caves", FULL, FULL, ClimateRange.of(0.8F, 1.0F), FULL, underground, FULL);
        add("lush_caves", FULL, ClimateRange.of(0.7F, 1.0F), FULL, FULL, underground, FULL);
        add("deep_dark", FULL, FULL, FULL, EROSION[0].to(EROSION[1]), ClimateRange.at(1.1F), FULL);
    }

    private void surface(String biome, ClimateRange temperature, ClimateRange humidity, ClimateRange continentalness,
                         ClimateRange erosion, ClimateRange weirdness) {
        add(biome, temperature, humidity, continentalness, erosion, ClimateRange.at(0.0F), weirdness);
        add(biome, temperature, humidity, continentalness, erosion, ClimateRange.at(1.0F), weirdness);
    }

    private void add(String biome, ClimateRange temperature, ClimateRange humidity, ClimateRange continentalness,
                     ClimateRange erosion, ClimateRange depth, ClimateRange weirdness) {
        spots.add(new ClimateSpot("minecraft:" + biome, temperature, humidity, continentalness, erosion, depth, weirdness));
    }
}

package space.nows.tube.server.mob.kind.animal;

import net.minestom.server.color.DyeColor;

import java.util.Map;
import java.util.random.RandomGenerator;

final class WoolPalette {

    private static final int RARE_COLORS_WEIGHT = 18;
    private static final int TOTAL_WEIGHT = 100;
    private static final int PINK_ONE_IN = 500;
    private static final Map<FarmClimate, DyeColor[]> RARE = Map.of(
            FarmClimate.TEMPERATE, new DyeColor[]{DyeColor.BLACK, DyeColor.GRAY, DyeColor.LIGHT_GRAY, DyeColor.BROWN},
            FarmClimate.WARM, new DyeColor[]{DyeColor.GRAY, DyeColor.LIGHT_GRAY, DyeColor.WHITE, DyeColor.BLACK},
            FarmClimate.COLD, new DyeColor[]{DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.WHITE, DyeColor.BROWN});
    private static final int[] RARE_WEIGHTS = {5, 5, 5, 3};
    private static final Map<FarmClimate, DyeColor> COMMON = Map.of(
            FarmClimate.TEMPERATE, DyeColor.WHITE,
            FarmClimate.WARM, DyeColor.BROWN,
            FarmClimate.COLD, DyeColor.BLACK);

    private WoolPalette() {
    }

    static DyeColor pick(FarmClimate climate, RandomGenerator random) {
        int roll = random.nextInt(TOTAL_WEIGHT);
        if (roll < RARE_COLORS_WEIGHT) {
            DyeColor[] colors = RARE.get(climate);
            for (int i = 0; i < colors.length; i++) {
                roll -= RARE_WEIGHTS[i];
                if (roll < 0) {
                    return colors[i];
                }
            }
        }
        return random.nextInt(PINK_ONE_IN) == 0 ? DyeColor.PINK : COMMON.get(climate);
    }
}

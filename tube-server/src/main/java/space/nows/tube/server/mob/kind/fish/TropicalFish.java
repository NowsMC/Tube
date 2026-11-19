package space.nows.tube.server.mob.kind.fish;

import net.minestom.server.color.DyeColor;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.water.fish.TropicalFishMeta;
import net.minestom.server.entity.metadata.water.fish.TropicalFishMeta.Pattern;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureStats;

import java.util.List;
import java.util.random.RandomGenerator;

public final class TropicalFish extends Fish {

    private static final CreatureStats STATS = CreatureStats.mob(3, 0.1);
    private static final float COMMON_CHANCE = 0.9f;
    private static final List<TropicalFishMeta.Variant> COMMON = List.of(
            look(Pattern.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY), look(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.GRAY),
            look(Pattern.FLOPPER, DyeColor.GRAY, DyeColor.BLUE), look(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY),
            look(Pattern.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY), look(Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE),
            look(Pattern.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE), look(Pattern.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW),
            look(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.RED), look(Pattern.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW),
            look(Pattern.GLITTER, DyeColor.WHITE, DyeColor.GRAY), look(Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE),
            look(Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK), look(Pattern.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE),
            look(Pattern.BETTY, DyeColor.RED, DyeColor.WHITE), look(Pattern.SNOOPER, DyeColor.GRAY, DyeColor.RED),
            look(Pattern.BLOCKFISH, DyeColor.RED, DyeColor.WHITE), look(Pattern.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW),
            look(Pattern.KOB, DyeColor.RED, DyeColor.WHITE), look(Pattern.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE),
            look(Pattern.DASHER, DyeColor.CYAN, DyeColor.YELLOW), look(Pattern.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW));

    private static TropicalFishMeta.Variant look(Pattern pattern, DyeColor base, DyeColor stripes) {
        return new TropicalFishMeta.Variant(pattern, base, stripes);
    }

    @Override
    public EntityType type() {
        return EntityType.TROPICAL_FISH;
    }

    @Override
    public CreatureStats stats() {
        return STATS;
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        TropicalFishMeta.Variant variant;
        if (random.nextFloat() < COMMON_CHANCE) {
            variant = COMMON.get(random.nextInt(COMMON.size()));
        } else {
            Pattern[] patterns = Pattern.values();
            DyeColor[] colors = DyeColor.values();
            variant = look(patterns[random.nextInt(patterns.length)], colors[random.nextInt(colors.length)], colors[random.nextInt(colors.length)]);
        }
        if (creature.getEntityMeta() instanceof TropicalFishMeta meta) {
            meta.setVariant(variant);
        }
    }
}

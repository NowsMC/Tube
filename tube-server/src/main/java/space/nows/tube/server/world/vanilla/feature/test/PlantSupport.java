package space.nows.tube.server.world.vanilla.feature.test;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.feature.DecorationArea;

import java.util.Map;
import java.util.Set;

public final class PlantSupport {

    private interface Rule {
        boolean allows(DecorationArea area, BlockState plant, BlockState ground, int x, int y, int z);
    }

    private static final Set<String> VEGETATION = Set.of(
            "minecraft:sunflower", "minecraft:lilac", "minecraft:rose_bush", "minecraft:peony", "minecraft:short_grass",
            "minecraft:fern", "minecraft:tall_grass", "minecraft:large_fern", "minecraft:bush", "minecraft:firefly_bush",
            "minecraft:sweet_berry_bush", "minecraft:wildflowers", "minecraft:pink_petals");

    private static final Rule ON_SOLID = (area, plant, ground, x, y, z) -> ground.blocksMotion;

    private static final Map<String, Rule> RULES = Map.ofEntries(
            Map.entry("minecraft:dead_bush", onTag("minecraft:supports_dry_vegetation")),
            Map.entry("minecraft:short_dry_grass", onTag("minecraft:supports_dry_vegetation")),
            Map.entry("minecraft:tall_dry_grass", onTag("minecraft:supports_dry_vegetation")),
            Map.entry("minecraft:azalea", onTag("minecraft:supports_azalea")),
            Map.entry("minecraft:flowering_azalea", onTag("minecraft:supports_azalea")),
            Map.entry("minecraft:mangrove_propagule", onTag("minecraft:supports_mangrove_propagule")),
            Map.entry("minecraft:cactus", PlantSupport::cactus),
            Map.entry("minecraft:sugar_cane", PlantSupport::sugarCane),
            Map.entry("minecraft:lily_pad", (area, plant, ground, x, y, z) -> ground.water && ground.isSource() && area.isAir(x, y, z)),
            Map.entry("minecraft:brown_mushroom", (area, plant, ground, x, y, z) -> ground.blocksMotion && !ground.leaves),
            Map.entry("minecraft:red_mushroom", (area, plant, ground, x, y, z) -> ground.blocksMotion && !ground.leaves),
            Map.entry("minecraft:seagrass", underwater("minecraft:cannot_support_seagrass")),
            Map.entry("minecraft:tall_seagrass", underwater("minecraft:cannot_support_seagrass")),
            Map.entry("minecraft:kelp", PlantSupport::kelp),
            Map.entry("minecraft:kelp_plant", PlantSupport::kelp),
            Map.entry("minecraft:snow", (area, plant, ground, x, y, z) -> ground.is("minecraft:support_override_snow_layer")
                    || ground.blocksMotion && !ground.is("minecraft:cannot_support_snow_layer")),
            Map.entry("minecraft:leaf_litter", ON_SOLID),
            Map.entry("minecraft:moss_carpet", ON_SOLID),
            Map.entry("minecraft:pale_moss_carpet", ON_SOLID));

    private PlantSupport() {
    }

    public static boolean canStay(DecorationArea area, BlockState plant, int x, int y, int z) {
        BlockState ground = area.get(x, y - 1, z);
        if (isVegetation(plant)) {
            return "upper".equals(plant.property("half")) ? ground.key.equals(plant.key) : ground.is("minecraft:supports_vegetation");
        }
        Rule rule = RULES.get(plant.key);
        return rule == null || rule.allows(area, plant, ground, x, y, z);
    }

    private static boolean isVegetation(BlockState plant) {
        return plant.key.endsWith("_sapling") || plant.is("minecraft:small_flowers") || VEGETATION.contains(plant.key);
    }

    private static Rule onTag(String tag) {
        return (area, plant, ground, x, y, z) -> ground.is(tag);
    }

    private static Rule underwater(String excluded) {
        return (area, plant, ground, x, y, z) -> ground.blocksMotion && !ground.is(excluded);
    }

    private static boolean cactus(DecorationArea area, BlockState plant, BlockState ground, int x, int y, int z) {
        return ground.is("minecraft:supports_cactus") && !area.get(x, y + 1, z).lava
                && !area.get(x + 1, y, z).solid && !area.get(x - 1, y, z).solid
                && !area.get(x, y, z + 1).solid && !area.get(x, y, z - 1).solid;
    }

    private static boolean sugarCane(DecorationArea area, BlockState plant, BlockState ground, int x, int y, int z) {
        if (ground.key.equals(plant.key)) {
            return true;
        }
        return ground.is("minecraft:supports_sugar_cane") && (area.get(x + 1, y - 1, z).water || area.get(x - 1, y - 1, z).water
                || area.get(x, y - 1, z + 1).water || area.get(x, y - 1, z - 1).water);
    }

    private static boolean kelp(DecorationArea area, BlockState plant, BlockState ground, int x, int y, int z) {
        return ground.key.equals("minecraft:kelp_plant") || ground.blocksMotion && !ground.is("minecraft:cannot_support_kelp");
    }
}

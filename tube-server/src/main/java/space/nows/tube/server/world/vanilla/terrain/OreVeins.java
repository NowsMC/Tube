package space.nows.tube.server.world.vanilla.terrain;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.density.Density;
import space.nows.tube.server.world.vanilla.random.PositionalRandom;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

public final class OreVeins {

    private record Vein(BlockState ore, BlockState richOre, BlockState rock, int bottom, int top) {

        int distanceToEdge(int y) {
            return Math.min(top - y, y - bottom);
        }
    }

    private static final Vein COPPER = new Vein(BlockStates.of("copper_ore"), BlockStates.of("raw_copper_block"), BlockStates.of("granite"), 0, 50);
    private static final Vein IRON = new Vein(BlockStates.of("deepslate_iron_ore"), BlockStates.of("raw_iron_block"), BlockStates.of("tuff"), -60, -8);

    private final Density toggle;
    private final Density ridged;
    private final Density gap;
    private final PositionalRandom random;

    public OreVeins(TerrainSettings settings, ChunkDensity chunk, PositionalRandom random) {
        this.toggle = chunk.localize(settings.router().veinToggle());
        this.ridged = chunk.localize(settings.router().veinRidged());
        this.gap = chunk.localize(settings.router().veinGap());
        this.random = random;
    }

    public BlockState fill(int x, int y, int z) {
        double selector = toggle.at(x, y, z);
        Vein vein = selector > 0 ? COPPER : IRON;
        int edge = vein.distanceToEdge(y);
        if (edge < 0) {
            return null;
        }
        double strength = Math.abs(selector);
        double fade = Math.clamp(-0.2 + edge / 20.0 * 0.2, -0.2, 0.0);
        if (strength + fade < 0.4) {
            return null;
        }
        Xoroshiro roll = random.at(x, y, z);
        if (roll.nextFloat() > 0.7F || ridged.at(x, y, z) >= 0) {
            return null;
        }
        double oreChance = Math.clamp(0.1 + (strength - 0.4) / 0.2 * 0.2, 0.1, 0.3);
        if (roll.nextFloat() < oreChance && gap.at(x, y, z) > -0.3F) {
            return roll.nextFloat() < 0.02F ? vein.richOre : vein.ore;
        }
        return vein.rock;
    }
}

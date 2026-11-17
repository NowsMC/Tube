package space.nows.tube.server.world.vanilla.surface;

import space.nows.tube.server.world.vanilla.block.BlockState;
import space.nows.tube.server.world.vanilla.block.BlockStates;
import space.nows.tube.server.world.vanilla.noise.PairedNoise;
import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.Arrays;

final class ClayBands {

    private static final int LAYERS = 192;

    private final BlockState[] layers = new BlockState[LAYERS];
    private final PairedNoise offset;

    ClayBands(Xoroshiro random, PairedNoise offset) {
        this.offset = offset;
        Arrays.fill(layers, BlockStates.of("terracotta"));
        scatterOrange(random);
        stripe(random, 1, BlockStates.of("yellow_terracotta"));
        stripe(random, 2, BlockStates.of("brown_terracotta"));
        stripe(random, 1, BlockStates.of("red_terracotta"));
        whiteLines(random);
    }

    BlockState at(int x, int y, int z) {
        int shift = (int) Math.round(offset.value(x, 0, z) * 4.0);
        return layers[Math.floorMod(y + shift, LAYERS)];
    }

    private void scatterOrange(Xoroshiro random) {
        BlockState orange = BlockStates.of("orange_terracotta");
        for (int layer = 0; layer < LAYERS; layer++) {
            layer += random.nextInt(5) + 1;
            if (layer < LAYERS) {
                layers[layer] = orange;
            }
        }
    }

    private void stripe(Xoroshiro random, int baseWidth, BlockState state) {
        int count = random.nextBetween(6, 15);
        for (int i = 0; i < count; i++) {
            int width = baseWidth + random.nextInt(3);
            int start = random.nextInt(LAYERS);
            for (int layer = start; layer < LAYERS && layer < start + width; layer++) {
                layers[layer] = state;
            }
        }
    }

    private void whiteLines(Xoroshiro random) {
        BlockState white = BlockStates.of("white_terracotta");
        BlockState lightGray = BlockStates.of("light_gray_terracotta");
        int lines = random.nextBetween(9, 15);
        int placed = 0;
        for (int layer = 0; placed < lines && layer < LAYERS; layer += random.nextInt(16) + 4) {
            layers[layer] = white;
            if (layer - 1 > 0 && random.nextBoolean()) {
                layers[layer - 1] = lightGray;
            }
            if (layer + 1 < LAYERS && random.nextBoolean()) {
                layers[layer + 1] = lightGray;
            }
            placed++;
        }
    }
}

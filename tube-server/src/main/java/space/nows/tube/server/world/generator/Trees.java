package space.nows.tube.server.world.generator;

import space.nows.tube.api.world.Block;

import java.util.Random;

final class Trees {

    enum Kind {
        OAK("oak", 4, 3),
        BIRCH("birch", 5, 3),
        SPRUCE("spruce", 6, 4);

        final Block log;
        final Block leaves;
        final int minHeight;
        final int extraHeight;

        Kind(String wood, int minHeight, int extraHeight) {
            this.log = Block.of(wood + "_log");
            this.leaves = Block.of(wood + "_leaves");
            this.minHeight = minHeight;
            this.extraHeight = extraHeight;
        }
    }

    private static final Block DIRT = Block.of("dirt");

    private Trees() {
    }

    static void grow(ChunkColumns blocks, Random random, int x, int baseY, int z, Kind kind) {
        int height = kind.minHeight + random.nextInt(kind.extraHeight);
        int top = baseY + height;
        if (kind == Kind.SPRUCE) {
            spruceCanopy(blocks, random, x, baseY, z, top, kind.leaves);
        } else {
            roundCanopy(blocks, random, x, z, top, kind.leaves);
        }
        blocks.set(x, baseY - 1, z, DIRT);
        for (int y = baseY; y < top; y++) {
            blocks.set(x, y, z, kind.log);
        }
    }

    private static void roundCanopy(ChunkColumns blocks, Random random, int x, int z, int top, Block leaves) {
        for (int y = top - 3; y <= top; y++) {
            int radius = y >= top - 1 ? 1 : 2;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    boolean corner = Math.abs(dx) == radius && Math.abs(dz) == radius;
                    if (corner && (y == top || random.nextBoolean())) {
                        continue;
                    }
                    leaf(blocks, x + dx, y, z + dz, leaves);
                }
            }
        }
    }

    private static void spruceCanopy(ChunkColumns blocks, Random random, int x, int baseY, int z, int top, Block leaves) {
        int radius = 0;
        for (int y = top; y >= baseY + 2; y--) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius > 0 && Math.abs(dx) == radius && Math.abs(dz) == radius) {
                        continue;
                    }
                    leaf(blocks, x + dx, y, z + dz, leaves);
                }
            }
            radius = radius >= 2 || radius == 1 && random.nextBoolean() ? 0 : radius + 1;
        }
        leaf(blocks, x, top + 1, z, leaves);
    }

    private static void leaf(ChunkColumns blocks, int x, int y, int z, Block leaves) {
        if (blocks.isAir(x, y, z)) {
            blocks.set(x, y, z, leaves);
        }
    }
}

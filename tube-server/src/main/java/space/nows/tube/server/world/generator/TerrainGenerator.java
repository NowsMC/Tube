package space.nows.tube.server.world.generator;

import de.articdive.jnoise.generators.noisegen.opensimplex.FastSimplexNoiseGenerator;
import space.nows.tube.api.world.Block;
import space.nows.tube.api.world.ChunkBuilder;
import space.nows.tube.api.world.WorldGenerator;

import java.util.Map;
import java.util.Random;

public final class TerrainGenerator implements WorldGenerator {

    public static final int SEA_LEVEL = 62;

    private static final Block BEDROCK = Block.of("bedrock");
    private static final Block DEEPSLATE = Block.of("deepslate");
    private static final Block STONE = Block.of("stone");
    private static final Block DIRT = Block.of("dirt");
    private static final Block GRASS = Block.of("grass_block");
    private static final Block SNOWY_GRASS = new Block("grass_block", Map.of("snowy", "true"));
    private static final Block SAND = Block.of("sand");
    private static final Block SANDSTONE = Block.of("sandstone");
    private static final Block GRAVEL = Block.of("gravel");
    private static final Block WATER = Block.of("water");
    private static final Block ICE = Block.of("ice");
    private static final Block LAVA = Block.of("lava");
    private static final Block SNOW = Block.of("snow");
    private static final Block SHORT_GRASS = Block.of("short_grass");
    private static final Block DANDELION = Block.of("dandelion");
    private static final Block POPPY = Block.of("poppy");
    private static final Block CACTUS = Block.of("cactus");
    private static final Block DEAD_BUSH = Block.of("dead_bush");
    private static final Block SUGAR_CANE = Block.of("sugar_cane");

    private enum Biome {
        OCEAN("minecraft:ocean"),
        FROZEN_OCEAN("minecraft:frozen_ocean"),
        BEACH("minecraft:beach"),
        SNOWY_BEACH("minecraft:snowy_beach"),
        PLAINS("minecraft:plains"),
        FOREST("minecraft:forest"),
        BIRCH_FOREST("minecraft:birch_forest"),
        DESERT("minecraft:desert"),
        SNOWY_PLAINS("minecraft:snowy_plains"),
        HILLS("minecraft:windswept_hills");

        final String key;

        Biome(String key) {
            this.key = key;
        }

        boolean cold() {
            return this == FROZEN_OCEAN || this == SNOWY_BEACH || this == SNOWY_PLAINS;
        }
    }

    private record Ore(String name, int veins, int size, int minY, int maxY, boolean hillsOnly) {
    }

    private static final Ore[] ORES = {
            new Ore("coal_ore", 20, 14, 0, 190, false),
            new Ore("iron_ore", 12, 8, -48, 72, false),
            new Ore("copper_ore", 10, 9, -16, 112, false),
            new Ore("gold_ore", 4, 8, -64, 32, false),
            new Ore("redstone_ore", 6, 7, -64, 15, false),
            new Ore("lapis_ore", 3, 6, -48, 30, false),
            new Ore("diamond_ore", 6, 6, -64, 14, false),
            new Ore("emerald_ore", 4, 1, 80, 250, true),
            new Ore("granite", 3, 30, 0, 80, false),
            new Ore("diorite", 3, 30, 0, 80, false),
            new Ore("andesite", 3, 30, 0, 80, false),
            new Ore("dirt", 4, 30, 0, 160, false),
            new Ore("gravel", 4, 30, -64, 160, false),
            new Ore("tuff", 2, 30, -64, 0, false)
    };

    private final long seed;
    private volatile int[] spawn;
    private final FastSimplexNoiseGenerator continents;
    private final FastSimplexNoiseGenerator hills;
    private final FastSimplexNoiseGenerator detail;
    private final FastSimplexNoiseGenerator temperature;
    private final FastSimplexNoiseGenerator humidity;
    private final CaveNoise cheese;
    private final CaveNoise tunnelA;
    private final CaveNoise tunnelB;

    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.continents = FastSimplexNoiseGenerator.newBuilder().setSeed(seed).build();
        this.hills = FastSimplexNoiseGenerator.newBuilder().setSeed(seed + 1).build();
        this.detail = FastSimplexNoiseGenerator.newBuilder().setSeed(seed + 2).build();
        this.temperature = FastSimplexNoiseGenerator.newBuilder().setSeed(seed + 3).build();
        this.humidity = FastSimplexNoiseGenerator.newBuilder().setSeed(seed + 4).build();
        this.cheese = new CaveNoise(seed + 5, 80, 45);
        this.tunnelA = new CaveNoise(seed + 6, 50, 30);
        this.tunnelB = new CaveNoise(seed + 7, 50, 30);
    }

    public int height(int x, int z) {
        double land = continents.evaluateNoise(x / 700.0, z / 700.0);
        double roughness = Math.max(0, land + 0.2) * 22;
        double surface = 64 + land * 20 + hills.evaluateNoise(x / 160.0, z / 160.0) * roughness + detail.evaluateNoise(x / 40.0, z / 40.0) * 3;
        return (int) Math.round(surface);
    }

    public String biomeAt(int x, int z) {
        return biome(x, z, height(x, z)).key;
    }

    private Biome biome(int x, int z, int top) {
        double temp = temperature.evaluateNoise(x / 600.0, z / 600.0);
        double wet = humidity.evaluateNoise(x / 500.0, z / 500.0);
        boolean cold = temp < -0.45;
        boolean hot = temp > 0.4 && wet < 0.1;
        if (top < SEA_LEVEL - 1) {
            return cold ? Biome.FROZEN_OCEAN : Biome.OCEAN;
        }
        if (top <= SEA_LEVEL + 1 && !hot) {
            return cold ? Biome.SNOWY_BEACH : Biome.BEACH;
        }
        if (top > 105) {
            return Biome.HILLS;
        }
        if (cold) {
            return Biome.SNOWY_PLAINS;
        }
        if (hot) {
            return Biome.DESERT;
        }
        if (wet > 0.1) {
            return wet > 0.5 ? Biome.BIRCH_FOREST : Biome.FOREST;
        }
        return Biome.PLAINS;
    }

    @Override
    public void generate(ChunkBuilder chunk) {
        int minY = chunk.minY();
        int maxY = chunk.maxY();
        int baseX = chunk.chunkX() << 4;
        int baseZ = chunk.chunkZ() << 4;
        ChunkColumns blocks = new ChunkColumns(minY, maxY);
        int[] tops = new int[256];
        Biome[] biomes = new Biome[256];

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = Math.clamp(height(baseX + x, baseZ + z), minY + 8, maxY - 20);
                Biome biome = biome(baseX + x, baseZ + z, top);
                tops[x << 4 | z] = top;
                biomes[x << 4 | z] = biome;
                terrainColumn(blocks, x, z, baseX + x, baseZ + z, top, biome);
            }
        }

        carveCaves(blocks, baseX, baseZ, tops);
        Random random = new Random(seed ^ chunk.chunkX() * 341873128712L ^ chunk.chunkZ() * 132897987541L);
        placeOres(blocks, random, tops);
        decorate(blocks, random, baseX, baseZ, tops, biomes);
        blocks.writeTo(chunk);

        for (int x = 0; x < 16; x += 4) {
            for (int z = 0; z < 16; z += 4) {
                String key = biomes[(x + 2) << 4 | (z + 2)].key;
                for (int y = minY; y < maxY; y += 4) {
                    chunk.setBiome(x, y, z, key);
                }
            }
        }
    }

    private void terrainColumn(ChunkColumns blocks, int x, int z, int worldX, int worldZ, int top, Biome biome) {
        int minY = blocks.minY;
        blocks.set(x, minY, z, BEDROCK);
        for (int y = minY + 1; y < minY + 5; y++) {
            if (hash(worldX, y, worldZ) % 5 >= y - minY) {
                blocks.set(x, y, z, BEDROCK);
            }
        }
        for (int y = minY + 1; y < top - 3; y++) {
            if (blocks.get(x, y, z) != null) {
                continue;
            }
            boolean deep = y < 0 || y < 8 && hash(worldX, y, worldZ) % 8 >= y;
            blocks.set(x, y, z, deep ? DEEPSLATE : STONE);
        }
        Block filler;
        Block surface;
        if (biome == Biome.DESERT) {
            filler = SAND;
            surface = SAND;
            blocks.fill(x, z, top - 6, top - 3, SANDSTONE);
        } else if (biome == Biome.BEACH || biome == Biome.SNOWY_BEACH) {
            filler = SAND;
            surface = SAND;
        } else if (biome == Biome.OCEAN || biome == Biome.FROZEN_OCEAN) {
            filler = top >= SEA_LEVEL - 4 ? SAND : GRAVEL;
            surface = filler;
        } else if (biome == Biome.HILLS && top > 120) {
            filler = STONE;
            surface = STONE;
        } else {
            filler = DIRT;
            surface = biome.cold() ? SNOWY_GRASS : GRASS;
        }
        blocks.fill(x, z, top - 3, top, filler);
        blocks.set(x, top, z, surface);
        if (top < SEA_LEVEL) {
            blocks.fill(x, z, top + 1, SEA_LEVEL + 1, WATER);
            if (biome.cold()) {
                blocks.set(x, SEA_LEVEL, z, ICE);
            }
        } else if (biome.cold() || biome == Biome.HILLS && top > 135) {
            blocks.set(x, top + 1, z, SNOW);
        }
    }

    private void carveCaves(ChunkColumns blocks, int baseX, int baseZ, int[] tops) {
        int minY = blocks.minY;
        int maxTop = 0;
        for (int top : tops) {
            maxTop = Math.max(maxTop, top);
        }
        int gridMax = Math.min(blocks.maxY, maxTop + 8);
        double[][][] big = cheese.grid(baseX, baseZ, minY, gridMax);
        double[][][] a = tunnelA.grid(baseX, baseZ, minY, gridMax);
        double[][][] b = tunnelB.grid(baseX, baseZ, minY, gridMax);
        int[] spawn = spawnColumn();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = tops[x << 4 | z];
                boolean dry = top >= SEA_LEVEL + 2 && (Math.abs(baseX + x - spawn[0]) > 2 || Math.abs(baseZ + z - spawn[1]) > 2);
                for (int y = minY + 5; y <= top; y++) {
                    boolean deepEnough = y < top - 7;
                    if (!deepEnough && !dry) {
                        break;
                    }
                    double ta = CaveNoise.at(a, x, y, z, minY);
                    double tb = CaveNoise.at(b, x, y, z, minY);
                    boolean tunnel = Math.abs(ta) < 0.06 && Math.abs(tb) < 0.06;
                    boolean cavern = deepEnough && y < 40 && CaveNoise.at(big, x, y, z, minY) > 0.58;
                    if (tunnel || cavern) {
                        Block current = blocks.get(x, y, z);
                        if (current == BEDROCK || current == WATER) {
                            continue;
                        }
                        blocks.set(x, y, z, y < minY + 10 ? LAVA : null);
                        if (y == top && blocks.get(x, y + 1, z) == SNOW) {
                            blocks.set(x, y + 1, z, null);
                        }
                    }
                }
            }
        }
    }

    private void placeOres(ChunkColumns blocks, Random random, int[] tops) {
        boolean hills = false;
        for (int top : tops) {
            hills |= top > 100;
        }
        for (Ore ore : ORES) {
            if (ore.hillsOnly() && !hills) {
                continue;
            }
            for (int vein = 0; vein < ore.veins(); vein++) {
                int x = random.nextInt(16);
                int z = random.nextInt(16);
                int y = ore.minY() + random.nextInt(Math.max(1, ore.maxY() - ore.minY()));
                for (int i = 0; i < ore.size(); i++) {
                    Block host = blocks.get(x, y, z);
                    if (host == STONE || host == DEEPSLATE) {
                        blocks.set(x, y, z, oreFor(ore.name(), host == DEEPSLATE));
                    }
                    x += random.nextInt(3) - 1;
                    y += random.nextInt(3) - 1;
                    z += random.nextInt(3) - 1;
                }
            }
        }
    }

    private static Block oreFor(String name, boolean deep) {
        if (deep && name.endsWith("_ore")) {
            return Block.of("deepslate_" + name);
        }
        return Block.of(name);
    }

    private void decorate(ChunkColumns blocks, Random random, int baseX, int baseZ, int[] tops, Biome[] biomes) {
        int[] spawn = spawnColumn();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = tops[x << 4 | z];
                Biome biome = biomes[x << 4 | z];
                Block ground = blocks.get(x, top, z);
                if (!blocks.isAir(x, top + 1, z) && blocks.get(x, top + 1, z) != SNOW) {
                    continue;
                }
                boolean inner = x >= 2 && x <= 13 && z >= 2 && z <= 13;
                if (Math.abs(baseX + x - spawn[0]) < 3 && Math.abs(baseZ + z - spawn[1]) < 3) {
                    continue;
                }
                double roll = random.nextDouble();
                if (ground == GRASS || ground == SNOWY_GRASS) {
                    double treeChance = switch (biome) {
                        case FOREST, BIRCH_FOREST -> 0.035;
                        case PLAINS -> 0.003;
                        case SNOWY_PLAINS, HILLS -> 0.006;
                        default -> 0;
                    };
                    if (inner && roll < treeChance && clearForTree(blocks, x, top, z)) {
                        Trees.grow(blocks, random, x, top + 1, z, treeFor(biome, random));
                    } else if (biome == Biome.PLAINS || biome == Biome.FOREST || biome == Biome.BIRCH_FOREST) {
                        if (roll < 0.2) {
                            blocks.set(x, top + 1, z, SHORT_GRASS);
                        } else if (roll < 0.215) {
                            blocks.set(x, top + 1, z, random.nextBoolean() ? DANDELION : POPPY);
                        }
                    }
                } else if (ground == SAND && biome == Biome.DESERT) {
                    if (roll < 0.004 && inner && isolated(blocks, x, top + 1, z)) {
                        int height = 1 + random.nextInt(3);
                        blocks.fill(x, z, top + 1, top + 1 + height, CACTUS);
                    } else if (roll < 0.012) {
                        blocks.set(x, top + 1, z, DEAD_BUSH);
                    }
                } else if ((ground == SAND || ground == GRASS || ground == DIRT) && top == SEA_LEVEL && roll < 0.2 && besideWater(blocks, x, top, z)) {
                    blocks.fill(x, z, top + 1, top + 2 + random.nextInt(3), SUGAR_CANE);
                }
            }
        }
    }

    private static Trees.Kind treeFor(Biome biome, Random random) {
        return switch (biome) {
            case BIRCH_FOREST -> Trees.Kind.BIRCH;
            case FOREST -> random.nextInt(5) == 0 ? Trees.Kind.BIRCH : Trees.Kind.OAK;
            case SNOWY_PLAINS, HILLS -> Trees.Kind.SPRUCE;
            default -> Trees.Kind.OAK;
        };
    }

    private static boolean clearForTree(ChunkColumns blocks, int x, int top, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                Block block = blocks.get(x + dx, top + 1, z + dz);
                if (block != null && block != SHORT_GRASS && block != SNOW && block != DANDELION && block != POPPY) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isolated(ChunkColumns blocks, int x, int y, int z) {
        return blocks.isAir(x + 1, y, z) && blocks.isAir(x - 1, y, z) && blocks.isAir(x, y, z + 1) && blocks.isAir(x, y, z - 1);
    }

    private static boolean besideWater(ChunkColumns blocks, int x, int y, int z) {
        return blocks.get(x + 1, y, z) == WATER || blocks.get(x - 1, y, z) == WATER
                || blocks.get(x, y, z + 1) == WATER || blocks.get(x, y, z - 1) == WATER;
    }

    private long hash(int x, int y, int z) {
        long h = seed ^ x * 3129871L ^ (long) z * 116129781L ^ y;
        h = h * h * 42317861L + h * 11L;
        return (h >>> 16) & Long.MAX_VALUE;
    }

    private int[] spawnColumn() {
        int[] column = spawn;
        if (column == null) {
            column = SpawnSearch.find(this);
            spawn = column;
        }
        return column;
    }

    @Override
    public int spawnHeight(int x, int z) {
        return Math.max(height(x, z), SEA_LEVEL) + 1;
    }

    @Override
    public boolean canSpawnAt(int x, int z) {
        int top = height(x, z);
        return top > SEA_LEVEL + 1 && top < 100;
    }
}

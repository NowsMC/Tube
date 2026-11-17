package space.nows.tube.server.world.vanilla.random;

public final class Xoroshiro implements SeededRandom {

    static final long GOLDEN_RATIO = 0x9E3779B97F4A7C15L;
    static final long SILVER_RATIO = 0x6A09E667F3BCC909L;

    private long low;
    private long high;
    private double spareGaussian;
    private boolean hasSpareGaussian;

    public Xoroshiro(long seed) {
        reseed(seed);
    }

    public Xoroshiro(long low, long high) {
        setState(low, high);
    }

    public void reseed(long seed) {
        long base = seed ^ SILVER_RATIO;
        setState(stafford(base), stafford(base + GOLDEN_RATIO));
        hasSpareGaussian = false;
    }

    private void setState(long low, long high) {
        boolean empty = (low | high) == 0;
        this.low = empty ? GOLDEN_RATIO : low;
        this.high = empty ? SILVER_RATIO : high;
    }

    private static long stafford(long value) {
        long mixed = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        mixed = (mixed ^ mixed >>> 27) * 0x94D049BB133111EBL;
        return mixed ^ mixed >>> 31;
    }

    @Override
    public long nextLong() {
        long first = low;
        long second = high;
        long result = Long.rotateLeft(first + second, 17) + first;
        second ^= first;
        low = Long.rotateLeft(first, 49) ^ second ^ second << 21;
        high = Long.rotateLeft(second, 28);
        return result;
    }

    @Override
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        long product = Integer.toUnsignedLong((int) nextLong()) * bound;
        if ((product & 0xFFFFFFFFL) < bound) {
            int threshold = Integer.remainderUnsigned(-bound, bound);
            while ((product & 0xFFFFFFFFL) < threshold) {
                product = Integer.toUnsignedLong((int) nextLong()) * bound;
            }
        }
        return (int) (product >> 32);
    }

    @Override
    public boolean nextBoolean() {
        return (nextLong() & 1) != 0;
    }

    @Override
    public float nextFloat() {
        return (nextLong() >>> 40) * 0x1.0p-24f;
    }

    @Override
    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    @Override
    public double nextGaussian() {
        if (hasSpareGaussian) {
            hasSpareGaussian = false;
            return spareGaussian;
        }
        while (true) {
            double u = 2 * nextDouble() - 1;
            double v = 2 * nextDouble() - 1;
            double square = u * u + v * v;
            if (square < 1 && square != 0) {
                double scale = Math.sqrt(-2 * Math.log(square) / square);
                spareGaussian = v * scale;
                hasSpareGaussian = true;
                return u * scale;
            }
        }
    }

    public PositionalRandom split() {
        return new PositionalRandom(nextLong(), nextLong());
    }
}

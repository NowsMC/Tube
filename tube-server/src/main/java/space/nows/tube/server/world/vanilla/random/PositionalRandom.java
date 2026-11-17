package space.nows.tube.server.world.vanilla.random;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public record PositionalRandom(long low, long high) {

    public Xoroshiro at(int x, int y, int z) {
        return new Xoroshiro(positionHash(x, y, z) ^ low, high);
    }

    public Xoroshiro named(String name) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5").digest(name.getBytes(StandardCharsets.UTF_8));
            ByteBuffer hash = ByteBuffer.wrap(digest);
            return new Xoroshiro(hash.getLong() ^ low, hash.getLong() ^ high);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static long positionHash(int x, int y, int z) {
        long hash = (long) (x * 3129871) ^ (long) z * 116129781L ^ y;
        hash = hash * hash * 42317861L + hash * 11L;
        return hash >> 16;
    }
}

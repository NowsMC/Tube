package space.nows.tube.server.command.support;

public record BlockBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public static final int EDIT_LIMIT = 32768;

    public static BlockBox between(Positions.BlockPosition a, Positions.BlockPosition b) {
        return new BlockBox(Math.min(a.x(), b.x()), Math.min(a.y(), b.y()), Math.min(a.z(), b.z()),
                Math.max(a.x(), b.x()), Math.max(a.y(), b.y()), Math.max(a.z(), b.z()));
    }

    public long volume() {
        return (long) (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    public void checkEditable() {
        if (volume() > EDIT_LIMIT) {
            throw new CommandFailure("Too many blocks in the specified area (maximum " + EDIT_LIMIT + ", specified " + volume() + ")");
        }
    }

    public boolean onEdge(int x, int y, int z) {
        return x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ;
    }
}

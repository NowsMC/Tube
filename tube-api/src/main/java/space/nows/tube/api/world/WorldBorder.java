package space.nows.tube.api.world;

public record WorldBorder(double centerX, double centerZ, double diameter, int warningDistance, int warningTime) {

    public static final WorldBorder DEFAULT = new WorldBorder(0, 0, 59_999_968, 5, 15);

    public WorldBorder withDiameter(double diameter) {
        return new WorldBorder(centerX, centerZ, diameter, warningDistance, warningTime);
    }

    public WorldBorder withCenter(double centerX, double centerZ) {
        return new WorldBorder(centerX, centerZ, diameter, warningDistance, warningTime);
    }

    public WorldBorder withWarningDistance(int warningDistance) {
        return new WorldBorder(centerX, centerZ, diameter, warningDistance, warningTime);
    }

    public WorldBorder withWarningTime(int warningTime) {
        return new WorldBorder(centerX, centerZ, diameter, warningDistance, warningTime);
    }

    public boolean contains(double x, double z) {
        double radius = diameter / 2;
        return x >= centerX - radius && x <= centerX + radius && z >= centerZ - radius && z <= centerZ + radius;
    }
}

package space.nows.tube.server.command.builtin.entity.spread;

public record SpreadPoint(double x, double z) {

    double distanceSquared(SpreadPoint other) {
        double dx = x - other.x;
        double dz = z - other.z;
        return dx * dx + dz * dz;
    }
}

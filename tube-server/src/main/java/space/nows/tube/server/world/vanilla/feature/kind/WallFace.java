package space.nows.tube.server.world.vanilla.feature.kind;

enum WallFace {
    NORTH(0, -1, "north"),
    EAST(1, 0, "east"),
    SOUTH(0, 1, "south"),
    WEST(-1, 0, "west");

    final int dx;
    final int dz;
    final String property;

    WallFace(int dx, int dz, String property) {
        this.dx = dx;
        this.dz = dz;
        this.property = property;
    }

    WallFace opposite() {
        return values()[(ordinal() + 2) % 4];
    }

    String axis() {
        return dx != 0 ? "x" : "z";
    }
}

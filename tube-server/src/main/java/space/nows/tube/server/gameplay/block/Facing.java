package space.nows.tube.server.gameplay.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.BlockFace;

enum Facing {
    NORTH("north", 0, 0, -1),
    EAST("east", 1, 0, 0),
    SOUTH("south", 0, 0, 1),
    WEST("west", -1, 0, 0),
    UP("up", 0, 1, 0),
    DOWN("down", 0, -1, 0);

    static final Facing[] HORIZONTAL = {NORTH, EAST, SOUTH, WEST};

    final String id;
    final int x;
    final int y;
    final int z;

    Facing(String id, int x, int y, int z) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    boolean horizontal() {
        return y == 0;
    }

    Facing opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
            case UP -> DOWN;
            case DOWN -> UP;
        };
    }

    Facing clockwise() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
            default -> this;
        };
    }

    Facing counterClockwise() {
        return clockwise().opposite();
    }

    String axis() {
        return x != 0 ? "x" : y != 0 ? "y" : "z";
    }

    Point offset(Point point) {
        return point.add(x, y, z);
    }

    BlockFace face() {
        return switch (this) {
            case NORTH -> BlockFace.NORTH;
            case SOUTH -> BlockFace.SOUTH;
            case EAST -> BlockFace.EAST;
            case WEST -> BlockFace.WEST;
            case UP -> BlockFace.TOP;
            case DOWN -> BlockFace.BOTTOM;
        };
    }

    static Facing of(BlockFace face) {
        return switch (face) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case TOP -> UP;
            case BOTTOM -> DOWN;
        };
    }

    static Facing of(String id) {
        for (Facing facing : values()) {
            if (facing.id.equals(id)) {
                return facing;
            }
        }
        return null;
    }

    static Facing horizontal(float yaw) {
        return switch (Math.floorMod(Math.round(yaw / 90f), 4)) {
            case 0 -> SOUTH;
            case 1 -> WEST;
            case 2 -> NORTH;
            default -> EAST;
        };
    }

    static Facing looking(Pos position) {
        if (position.pitch() < -45) {
            return UP;
        }
        if (position.pitch() > 45) {
            return DOWN;
        }
        return horizontal(position.yaw());
    }
}

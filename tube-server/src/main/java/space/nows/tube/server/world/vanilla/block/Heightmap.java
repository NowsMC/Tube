package space.nows.tube.server.world.vanilla.block;

public enum Heightmap {
    ANY_BLOCK,
    SOLID,
    SOLID_OR_FLUID,
    SOLID_OR_FLUID_NOT_LEAVES;

    public static Heightmap parse(String name) {
        return switch (name) {
            case "WORLD_SURFACE", "WORLD_SURFACE_WG" -> ANY_BLOCK;
            case "OCEAN_FLOOR", "OCEAN_FLOOR_WG" -> SOLID;
            case "MOTION_BLOCKING_NO_LEAVES" -> SOLID_OR_FLUID_NOT_LEAVES;
            default -> SOLID_OR_FLUID;
        };
    }

    public boolean counts(BlockState state) {
        return switch (this) {
            case ANY_BLOCK -> !state.air;
            case SOLID -> state.blocksMotion;
            case SOLID_OR_FLUID -> state.blocksMotion || state.hasFluid();
            case SOLID_OR_FLUID_NOT_LEAVES -> (state.blocksMotion || state.hasFluid()) && !state.leaves;
        };
    }
}

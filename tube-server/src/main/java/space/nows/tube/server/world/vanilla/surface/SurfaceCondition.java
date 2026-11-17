package space.nows.tube.server.world.vanilla.surface;

@FunctionalInterface
interface SurfaceCondition {

    boolean test(SurfaceColumn column);
}

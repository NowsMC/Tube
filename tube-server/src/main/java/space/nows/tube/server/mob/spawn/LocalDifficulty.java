package space.nows.tube.server.mob.spawn;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.Difficulty;

public record LocalDifficulty(Difficulty base, float effective) {

    private static final float[] MOON_BRIGHTNESS = {1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f};
    private static final float GLOBAL_OFFSET = -72000f;
    private static final float GLOBAL_MAX = 1440000f;
    private static final float LOCAL_MAX = 3600000f;
    private static final long DAY_TICKS = 24000;

    public static LocalDifficulty at(Instance instance, long inhabitedTicks) {
        Difficulty base = MinecraftServer.getDifficulty();
        if (base == Difficulty.PEACEFUL) {
            return new LocalDifficulty(base, 0f);
        }
        float moon = MOON_BRIGHTNESS[(int) Math.floorMod(instance.getTime() / DAY_TICKS, MOON_BRIGHTNESS.length)];
        float scale = 0.75f;
        float global = Math.clamp((instance.getWorldAge() + GLOBAL_OFFSET) / GLOBAL_MAX, 0f, 1f) * 0.25f;
        scale += global;
        float local = Math.clamp(inhabitedTicks / LOCAL_MAX, 0f, 1f) * (base == Difficulty.HARD ? 1f : 0.75f);
        local += Math.clamp(moon * 0.25f, 0f, global);
        if (base == Difficulty.EASY) {
            local *= 0.5f;
        }
        scale += local;
        return new LocalDifficulty(base, base.ordinal() * scale);
    }

    public float specialMultiplier() {
        if (effective < 2f) {
            return 0f;
        }
        return effective > 4f ? 1f : (effective - 2f) / 2f;
    }
}

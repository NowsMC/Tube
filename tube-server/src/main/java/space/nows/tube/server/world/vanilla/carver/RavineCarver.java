package space.nows.tube.server.world.vanilla.carver;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.sample.FloatSampler;
import space.nows.tube.server.world.vanilla.random.LcgRandom;

final class RavineCarver implements Carver {

    private final CarverSettings settings;
    private final FloatSampler tilt;
    private final FloatSampler lengthFactor;
    private final FloatSampler thickness;
    private final int wallRoughness;
    private final FloatSampler widthFactor;
    private final float baseHeightFactor;
    private final float middleHeightFactor;

    RavineCarver(JsonObject config, int minY, int height) {
        JsonObject shape = config.getAsJsonObject("shape");
        this.settings = CarverSettings.read(config, minY, height);
        this.tilt = FloatSampler.read(config.get("vertical_rotation"));
        this.lengthFactor = FloatSampler.read(shape.get("distance_factor"));
        this.thickness = FloatSampler.read(shape.get("thickness"));
        this.wallRoughness = shape.get("width_smoothness").getAsInt();
        this.widthFactor = FloatSampler.read(shape.get("horizontal_radius_factor"));
        this.baseHeightFactor = shape.get("vertical_radius_default_factor").getAsFloat();
        this.middleHeightFactor = shape.get("vertical_radius_center_factor").getAsFloat();
    }

    @Override
    public CarverSettings settings() {
        return settings;
    }

    @Override
    public void carve(CarveVolume volume, LcgRandom random, int startChunkX, int startChunkZ) {
        double x = (startChunkX << 4) + random.nextInt(16);
        double y = settings.y().sample(random);
        double z = (startChunkZ << 4) + random.nextInt(16);
        float yaw = random.nextFloat() * (float) (Math.PI * 2);
        float pitch = tilt.sample(random);
        double yScale = settings.yScale().sample(random);
        float width = thickness.sample(random);
        int length = (int) (CaveCarver.MAX_LENGTH * lengthFactor.sample(random));
        dig(volume, new LcgRandom(random.nextLong()), x, y, z, width, yaw, pitch, length, yScale);
    }

    private void dig(CarveVolume volume, LcgRandom random, double x, double y, double z, float width, float yaw, float pitch,
                     int length, double yScale) {
        float[] wallWidths = wallWidths(random, volume.height());
        int minY = volume.minY();
        CarveVolume.Shape shape = (dx, dy, dz, blockY) -> (dx * dx + dz * dz) * wallWidths[Math.max(blockY - minY - 1, 0)] + dy * dy / 6.0 >= 1.0;
        float yawDrift = 0.0F;
        float pitchDrift = 0.0F;
        for (int step = 0; step < length; step++) {
            double radius = 1.5 + FastTrig.sin(step * (float) Math.PI / length) * width;
            double radiusY = radius * yScale;
            radius *= widthFactor.sample(random);
            float alongMiddle = 1.0F - Math.abs(0.5F - (float) step / length) * 2.0F;
            radiusY = (baseHeightFactor + middleHeightFactor * alongMiddle) * radiusY * (random.nextFloat() * 0.25F + 0.75F);
            float flat = FastTrig.cos(pitch);
            float rise = FastTrig.sin(pitch);
            x += FastTrig.cos(yaw) * flat;
            y += rise;
            z += FastTrig.sin(yaw) * flat;
            pitch *= 0.7F;
            pitch += pitchDrift * 0.05F;
            yaw += yawDrift * 0.05F;
            pitchDrift *= 0.8F;
            yawDrift *= 0.5F;
            pitchDrift += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawDrift += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (random.nextInt(4) != 0) {
                if (!volume.reachable(x, z, step, length, width)) {
                    return;
                }
                volume.carveEllipsoid(settings, x, y, z, radius, radiusY, shape);
            }
        }
    }

    private float[] wallWidths(LcgRandom random, int height) {
        float[] widths = new float[height];
        float current = 1.0F;
        for (int i = 0; i < height; i++) {
            if (i == 0 || random.nextInt(wallRoughness) == 0) {
                current = 1.0F + random.nextFloat() * random.nextFloat();
            }
            widths[i] = current * current;
        }
        return widths;
    }
}

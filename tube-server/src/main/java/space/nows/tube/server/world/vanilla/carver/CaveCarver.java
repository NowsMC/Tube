package space.nows.tube.server.world.vanilla.carver;

import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.feature.sample.FloatSampler;
import space.nows.tube.server.world.vanilla.random.LcgRandom;

final class CaveCarver implements Carver {

    static final int MAX_LENGTH = 112;

    private final CarverSettings settings;
    private final FloatSampler widthScale;
    private final FloatSampler heightScale;
    private final FloatSampler floor;

    CaveCarver(JsonObject config, int minY, int height) {
        this.settings = CarverSettings.read(config, minY, height);
        this.widthScale = FloatSampler.read(config.get("horizontal_radius_multiplier"));
        this.heightScale = FloatSampler.read(config.get("vertical_radius_multiplier"));
        this.floor = FloatSampler.read(config.get("floor_level"));
    }

    @Override
    public CarverSettings settings() {
        return settings;
    }

    private record Tunnel(double widthScale, double heightScale, CarveVolume.Shape shape) {
    }

    @Override
    public void carve(CarveVolume volume, LcgRandom random, int startChunkX, int startChunkZ) {
        int systems = random.nextInt(random.nextInt(random.nextInt(15) + 1) + 1);
        for (int system = 0; system < systems; system++) {
            double x = (startChunkX << 4) + random.nextInt(16);
            double y = settings.y().sample(random);
            double z = (startChunkZ << 4) + random.nextInt(16);
            double width = widthScale.sample(random);
            double tall = heightScale.sample(random);
            double floorLevel = floor.sample(random);
            Tunnel tunnel = new Tunnel(width, tall, (dx, dy, dz, blockY) -> dy <= floorLevel || dx * dx + dy * dy + dz * dz >= 1.0);
            int branches = 1;
            if (random.nextInt(4) == 0) {
                double roomHeight = settings.yScale().sample(random);
                float roomSize = 1.0F + random.nextFloat() * 6.0F;
                double radius = 1.5 + FastTrig.sin(Math.PI / 2) * roomSize;
                volume.carveEllipsoid(settings, x + 1.0, y, z, radius, radius * roomHeight, tunnel.shape);
                branches += random.nextInt(4);
            }
            for (int branch = 0; branch < branches; branch++) {
                float yaw = random.nextFloat() * (float) (Math.PI * 2);
                float pitch = (random.nextFloat() - 0.5F) / 4.0F;
                float thickness = random.nextFloat() * 2.0F + random.nextFloat();
                if (random.nextInt(10) == 0) {
                    thickness *= random.nextFloat() * random.nextFloat() * 3.0F + 1.0F;
                }
                int length = MAX_LENGTH - random.nextInt(MAX_LENGTH / 4);
                dig(volume, tunnel, random.nextLong(), x, y, z, thickness, yaw, pitch, 0, length, 1.0);
            }
        }
    }

    private void dig(CarveVolume volume, Tunnel tunnel, long seed, double x, double y, double z, float thickness, float yaw,
                     float pitch, int start, int length, double yScale) {
        LcgRandom random = new LcgRandom(seed);
        int splitAt = random.nextInt(length / 2) + length / 4;
        boolean steep = random.nextInt(6) == 0;
        float yawDrift = 0.0F;
        float pitchDrift = 0.0F;
        for (int step = start; step < length; step++) {
            double radius = 1.5 + FastTrig.sin((float) Math.PI * step / length) * thickness;
            double radiusY = radius * yScale;
            float flat = FastTrig.cos(pitch);
            x += FastTrig.cos(yaw) * flat;
            y += FastTrig.sin(pitch);
            z += FastTrig.sin(yaw) * flat;
            pitch *= steep ? 0.92F : 0.7F;
            pitch += pitchDrift * 0.1F;
            yaw += yawDrift * 0.1F;
            pitchDrift *= 0.9F;
            yawDrift *= 0.75F;
            pitchDrift += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawDrift += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (step == splitAt && thickness > 1.0F) {
                dig(volume, tunnel, random.nextLong(), x, y, z, random.nextFloat() * 0.5F + 0.5F, yaw - (float) (Math.PI / 2), pitch / 3.0F, step, length, 1.0);
                dig(volume, tunnel, random.nextLong(), x, y, z, random.nextFloat() * 0.5F + 0.5F, yaw + (float) (Math.PI / 2), pitch / 3.0F, step, length, 1.0);
                return;
            }
            if (random.nextInt(4) != 0) {
                if (!volume.reachable(x, z, step, length, thickness)) {
                    return;
                }
                volume.carveEllipsoid(settings, x, y, z, radius * tunnel.widthScale, radiusY * tunnel.heightScale, tunnel.shape);
            }
        }
    }
}

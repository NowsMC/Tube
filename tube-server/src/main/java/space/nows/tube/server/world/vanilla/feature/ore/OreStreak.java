package space.nows.tube.server.world.vanilla.feature.ore;

import space.nows.tube.server.world.vanilla.random.Xoroshiro;

import java.util.ArrayList;
import java.util.List;

final class OreStreak {

    record Ball(double x, double y, double z, double radius) {
    }

    private OreStreak() {
    }

    static List<Ball> along(Xoroshiro random, int size, double x1, double y1, double z1, double x2, double y2, double z2) {
        double[] radii = new double[size];
        double[][] centers = new double[size][];
        for (int i = 0; i < size; i++) {
            float t = (float) i / size;
            centers[i] = new double[]{x1 + t * (x2 - x1), y1 + t * (y2 - y1), z1 + t * (z2 - z1)};
            double swell = random.nextDouble() * size / 16.0;
            radii[i] = ((Math.sin((float) Math.PI * t) + 1.0F) * swell + 1.0) / 2.0;
        }
        dropSwallowed(centers, radii);
        List<Ball> balls = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (radii[i] >= 0) {
                balls.add(new Ball(centers[i][0], centers[i][1], centers[i][2], radii[i]));
            }
        }
        return balls;
    }

    private static void dropSwallowed(double[][] centers, double[] radii) {
        for (int i = 0; i < radii.length - 1; i++) {
            if (radii[i] <= 0) {
                continue;
            }
            for (int j = i + 1; j < radii.length; j++) {
                if (radii[j] <= 0) {
                    continue;
                }
                double dx = centers[i][0] - centers[j][0];
                double dy = centers[i][1] - centers[j][1];
                double dz = centers[i][2] - centers[j][2];
                double dr = radii[i] - radii[j];
                if (dr * dr > dx * dx + dy * dy + dz * dz) {
                    if (dr > 0) {
                        radii[j] = -1;
                    } else {
                        radii[i] = -1;
                    }
                }
            }
        }
    }
}

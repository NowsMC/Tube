package space.nows.tube.server.world.vanilla.density;

import java.util.function.UnaryOperator;

public sealed interface Curve {

    float at(int x, int y, int z);

    Curve rebuild(UnaryOperator<Density> children);

    record Flat(float value) implements Curve {

        @Override
        public float at(int x, int y, int z) {
            return value;
        }

        @Override
        public Curve rebuild(UnaryOperator<Density> children) {
            return this;
        }
    }

    record Hermite(Density input, float[] knots, Curve[] values, float[] slopes) implements Curve {

        @Override
        public float at(int x, int y, int z) {
            float position = (float) input.at(x, y, z);
            int segment = segmentBefore(position);
            int last = knots.length - 1;
            if (segment < 0) {
                return extrapolate(position, 0, values[0].at(x, y, z));
            }
            if (segment == last) {
                return extrapolate(position, last, values[last].at(x, y, z));
            }
            float start = knots[segment];
            float end = knots[segment + 1];
            float width = end - start;
            float t = (position - start) / width;
            float from = values[segment].at(x, y, z);
            float to = values[segment + 1].at(x, y, z);
            float rise = to - from;
            float startTangent = slopes[segment] * width - rise;
            float endTangent = -slopes[segment + 1] * width + rise;
            return lerp(t, from, to) + t * (1 - t) * lerp(t, startTangent, endTangent);
        }

        private int segmentBefore(float position) {
            int low = 0;
            int high = knots.length;
            while (low < high) {
                int middle = (low + high) >>> 1;
                if (position < knots[middle]) {
                    high = middle;
                } else {
                    low = middle + 1;
                }
            }
            return low - 1;
        }

        private float extrapolate(float position, int knot, float value) {
            float slope = slopes[knot];
            return slope == 0 ? value : value + slope * (position - knots[knot]);
        }

        private static float lerp(float t, float from, float to) {
            return from + t * (to - from);
        }

        @Override
        public Curve rebuild(UnaryOperator<Density> children) {
            Curve[] rebuilt = new Curve[values.length];
            for (int i = 0; i < values.length; i++) {
                rebuilt[i] = values[i].rebuild(children);
            }
            return new Hermite(children.apply(input), knots, rebuilt, slopes);
        }
    }
}

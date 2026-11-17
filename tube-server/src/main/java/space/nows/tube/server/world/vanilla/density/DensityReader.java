package space.nows.tube.server.world.vanilla.density;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import space.nows.tube.server.world.vanilla.WorldgenData;
import space.nows.tube.server.world.vanilla.noise.TerrainBlendNoise;
import space.nows.tube.server.world.vanilla.noise.WorldNoises;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class DensityReader {

    private static final Map<String, Combined.Operator> OPERATORS = Map.of(
            "add", Combined.Operator.ADD,
            "mul", Combined.Operator.MULTIPLY,
            "min", Combined.Operator.MIN,
            "max", Combined.Operator.MAX);
    private static final Map<String, Transformed.Shape> SHAPES = Map.of(
            "abs", Transformed.Shape.ABS,
            "square", Transformed.Shape.SQUARE,
            "cube", Transformed.Shape.CUBE,
            "half_negative", Transformed.Shape.HALF_NEGATIVE,
            "quarter_negative", Transformed.Shape.QUARTER_NEGATIVE,
            "squeeze", Transformed.Shape.SQUEEZE,
            "invert", Transformed.Shape.INVERT);
    private static final Map<String, CacheHint.Kind> CACHES = Map.of(
            "interpolated", CacheHint.Kind.CELL_INTERPOLATED,
            "flat_cache", CacheHint.Kind.QUART_COLUMN,
            "cache_2d", CacheHint.Kind.COLUMN,
            "cache_once", CacheHint.Kind.POINT,
            "cache_all_in_cell", CacheHint.Kind.CELL);
    private static final Map<String, CoordinateWarp.Axes> WARPS = Map.of(
            "shift_a", CoordinateWarp.Axes.XZ,
            "shift_b", CoordinateWarp.Axes.ZX,
            "shift", CoordinateWarp.Axes.XYZ);

    private final WorldgenData data;
    private final Map<String, Function<JsonObject, Density>> readers = new HashMap<>();
    private final Map<String, Density> known = new HashMap<>();

    public DensityReader(WorldgenData data, WorldNoises noises) {
        this.data = data;
        OPERATORS.forEach((type, operator) -> readers.put(type, json -> new Combined(operator, read(json.get("argument1")), read(json.get("argument2")))));
        SHAPES.forEach((type, shape) -> readers.put(type, json -> new Transformed(shape, read(json.get("argument")))));
        CACHES.forEach((type, kind) -> readers.put(type, json -> new CacheHint(kind, read(json.get("argument")))));
        WARPS.forEach((type, axes) -> readers.put(type, json -> new CoordinateWarp(axes, noises.noise(json.get("argument")))));
        readers.put("constant", json -> new ConstantDensity(json.get("argument").getAsDouble()));
        readers.put("y_clamped_gradient", json -> new YGradient(json.get("from_y").getAsInt(), json.get("to_y").getAsInt(),
                json.get("from_value").getAsDouble(), json.get("to_value").getAsDouble()));
        readers.put("noise", json -> new NoiseDensity(noises.noise(json.get("noise")), json.get("xz_scale").getAsDouble(), json.get("y_scale").getAsDouble()));
        readers.put("shifted_noise", json -> new ShiftedNoiseDensity(read(json.get("shift_x")), read(json.get("shift_y")), read(json.get("shift_z")),
                json.get("xz_scale").getAsDouble(), json.get("y_scale").getAsDouble(), noises.noise(json.get("noise"))));
        readers.put("clamp", json -> new Clamped(read(json.get("input")), json.get("min").getAsDouble(), json.get("max").getAsDouble()));
        readers.put("range_choice", json -> new RangeSwitch(read(json.get("input")), json.get("min_inclusive").getAsDouble(),
                json.get("max_exclusive").getAsDouble(), read(json.get("when_in_range")), read(json.get("when_out_of_range"))));
        readers.put("interval_select", this::thresholdSwitch);
        readers.put("weird_scaled_sampler", json -> new RarityScaledNoise(read(json.get("input")), noises.noise(json.get("noise")),
                WorldgenData.path(json.get("rarity_value_mapper").getAsString()).equals("type_2") ? RarityScaledNoise.Table.TUNNELS : RarityScaledNoise.Table.CAVES));
        readers.put("old_blended_noise", json -> new TerrainBlend(new TerrainBlendNoise(noises.named("minecraft:terrain"),
                json.get("xz_scale").getAsDouble(), json.get("y_scale").getAsDouble(), json.get("xz_factor").getAsDouble(),
                json.get("y_factor").getAsDouble(), json.get("smear_scale_multiplier").getAsDouble())));
        readers.put("spline", json -> new SplineDensity(curve(json.get("spline"))));
        readers.put("find_top_surface", json -> new TopSurfaceScan(read(json.get("density")), read(json.get("upper_bound")),
                json.get("lower_bound").getAsInt(), json.get("cell_height").getAsInt()));
        readers.put("blend_density", json -> read(json.get("argument")));
        readers.put("blend_alpha", json -> new ConstantDensity(1));
        readers.put("blend_offset", json -> new ConstantDensity(0));
        readers.put("beardifier", json -> new ConstantDensity(0));
        readers.put("end_islands", json -> new ConstantDensity(0));
    }

    public Density read(JsonElement json) {
        String text = json.toString();
        Density existing = known.get(text);
        if (existing != null) {
            return existing;
        }
        Density density = readNew(json);
        known.put(text, density);
        return density;
    }

    private Density readNew(JsonElement json) {
        if (json.isJsonPrimitive()) {
            if (json.getAsJsonPrimitive().isNumber()) {
                return new ConstantDensity(json.getAsDouble());
            }
            return read(data.get("density_function", json.getAsString()));
        }
        JsonObject object = json.getAsJsonObject();
        String type = WorldgenData.type(object);
        Function<JsonObject, Density> reader = readers.get(type);
        if (reader == null) {
            throw new IllegalArgumentException("Unsupported density function " + type);
        }
        return reader.apply(object);
    }

    private Density thresholdSwitch(JsonObject json) {
        JsonArray thresholdList = json.getAsJsonArray("thresholds");
        double[] thresholds = new double[thresholdList.size()];
        for (int i = 0; i < thresholds.length; i++) {
            thresholds[i] = thresholdList.get(i).getAsDouble();
        }
        JsonArray branchList = json.getAsJsonArray("functions");
        Density[] branches = new Density[branchList.size()];
        for (int i = 0; i < branches.length; i++) {
            branches[i] = read(branchList.get(i));
        }
        return new ThresholdSwitch(read(json.get("input")), thresholds, branches);
    }

    private Curve curve(JsonElement json) {
        if (json.isJsonPrimitive()) {
            return new Curve.Flat(json.getAsFloat());
        }
        JsonObject object = json.getAsJsonObject();
        JsonArray points = object.getAsJsonArray("points");
        float[] knots = new float[points.size()];
        Curve[] values = new Curve[points.size()];
        float[] slopes = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            JsonObject point = points.get(i).getAsJsonObject();
            knots[i] = point.get("location").getAsFloat();
            values[i] = curve(point.get("value"));
            slopes[i] = point.get("derivative").getAsFloat();
        }
        return new Curve.Hermite(read(object.get("coordinate")), knots, values, slopes);
    }
}

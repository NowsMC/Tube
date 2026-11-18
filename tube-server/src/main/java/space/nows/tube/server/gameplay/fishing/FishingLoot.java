package space.nows.tube.server.gameplay.fishing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.PotionType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

public final class FishingLoot {

    public record Conditions(boolean openWater, int luck) {
    }

    private static final String DATA = "/net/minestom/data/loot_tables/gameplay_loot_tables.json";
    private static final String ROOT_TABLE = "minecraft:fishing";
    private static final String TABLE_PREFIX = "minecraft:gameplay/";
    private static final String TABLE_KEY_PREFIX = "minecraft:";

    private static volatile Map<String, JsonObject> tables;

    private FishingLoot() {
    }

    public static ItemStack roll(Conditions conditions, RandomGenerator random) {
        List<ItemStack> items = new ArrayList<>();
        roll(ROOT_TABLE, conditions, random, items);
        return items.isEmpty() ? ItemStack.AIR : items.getFirst();
    }

    private static void roll(String key, Conditions conditions, RandomGenerator random, List<ItemStack> out) {
        JsonObject table = tables().get(key);
        if (table == null) {
            return;
        }
        for (JsonElement poolElement : table.getAsJsonArray("pools")) {
            JsonObject pool = poolElement.getAsJsonObject();
            List<JsonObject> entries = new ArrayList<>();
            for (JsonElement entry : pool.getAsJsonArray("entries")) {
                if (allowed(entry.getAsJsonObject(), conditions)) {
                    entries.add(entry.getAsJsonObject());
                }
            }
            JsonObject chosen = pick(entries, conditions.luck(), random);
            if (chosen != null) {
                produce(chosen, conditions, random, out);
            }
        }
    }

    private static boolean allowed(JsonObject entry, Conditions conditions) {
        if (!entry.has("conditions")) {
            return true;
        }
        for (JsonElement element : entry.getAsJsonArray("conditions")) {
            if (element.toString().contains("in_open_water") && !conditions.openWater()) {
                return false;
            }
        }
        return true;
    }

    private static JsonObject pick(List<JsonObject> entries, int luck, RandomGenerator random) {
        int total = 0;
        for (JsonObject entry : entries) {
            total += weight(entry, luck);
        }
        if (total <= 0) {
            return null;
        }
        int roll = random.nextInt(total);
        for (JsonObject entry : entries) {
            roll -= weight(entry, luck);
            if (roll < 0) {
                return entry;
            }
        }
        return null;
    }

    private static int weight(JsonObject entry, int luck) {
        int weight = entry.has("weight") ? entry.get("weight").getAsInt() : 1;
        int quality = entry.has("quality") ? entry.get("quality").getAsInt() : 0;
        return Math.max(0, weight + quality * luck);
    }

    private static void produce(JsonObject entry, Conditions conditions, RandomGenerator random, List<ItemStack> out) {
        switch (entry.get("type").getAsString()) {
            case "minecraft:loot_table" -> roll(TABLE_KEY_PREFIX + entry.get("value").getAsString().substring(TABLE_PREFIX.length()), conditions, random, out);
            case "minecraft:item" -> {
                Material material = Material.fromKey(entry.get("name").getAsString());
                if (material != null) {
                    out.add(functions(entry, ItemStack.of(material), random));
                }
            }
            default -> {
            }
        }
    }

    private static ItemStack functions(JsonObject entry, ItemStack item, RandomGenerator random) {
        if (!entry.has("functions")) {
            return item;
        }
        for (JsonElement element : entry.getAsJsonArray("functions")) {
            JsonObject function = element.getAsJsonObject();
            item = switch (function.get("function").getAsString()) {
                case "minecraft:set_count" -> item.withAmount(function.get("count").getAsInt());
                case "minecraft:set_damage" -> damaged(item, function.getAsJsonObject("damage"), random);
                case "minecraft:set_potion" -> item.with(DataComponents.POTION_CONTENTS, new PotionContents(PotionType.WATER));
                case "minecraft:enchant_with_levels" -> LootEnchanting.apply(item, random);
                default -> item;
            };
        }
        return item;
    }

    private static ItemStack damaged(ItemStack item, JsonObject range, RandomGenerator random) {
        int max = item.get(DataComponents.MAX_DAMAGE) == null ? 0 : item.get(DataComponents.MAX_DAMAGE);
        double fraction = range.get("min").getAsDouble() + random.nextDouble() * (range.get("max").getAsDouble() - range.get("min").getAsDouble());
        return max == 0 ? item : item.with(DataComponents.DAMAGE, (int) (max * fraction));
    }

    private static Map<String, JsonObject> tables() {
        Map<String, JsonObject> loaded = tables;
        if (loaded == null) {
            loaded = read();
            tables = loaded;
        }
        return loaded;
    }

    private static Map<String, JsonObject> read() {
        try (InputStream in = FishingLoot.class.getResourceAsStream(DATA)) {
            if (in == null) {
                return Map.of();
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, JsonObject> found = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (entry.getKey().startsWith(ROOT_TABLE)) {
                    found.put(entry.getKey(), entry.getValue().getAsJsonObject());
                }
            }
            return Map.copyOf(found);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

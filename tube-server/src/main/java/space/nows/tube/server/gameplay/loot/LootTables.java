package space.nows.tube.server.gameplay.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.component.DataComponents;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

public final class LootTables {

    public record Context(ItemStack tool, Block block, boolean explosion, boolean killedByPlayer, boolean onFire,
                          String sheepColor, boolean sheared, RandomGenerator random) {

        public static Context block(Block block, ItemStack tool, RandomGenerator random) {
            return new Context(tool, block, false, false, false, null, false, random);
        }

        public static Context entity(ItemStack weapon, boolean killedByPlayer, boolean onFire, RandomGenerator random) {
            return new Context(weapon, null, false, killedByPlayer, onFire, null, false, random);
        }

        public Context withSheep(String color, boolean sheared) {
            return new Context(tool, block, explosion, killedByPlayer, onFire, color, sheared, random);
        }
    }

    private static final String ROOT = "/net/minestom/data/loot_tables/";

    private static volatile Map<String, JsonObject> blocks;
    private static volatile Map<String, JsonObject> entities;

    private LootTables() {
    }

    public static List<ItemStack> forBlock(Block block, Context context) {
        return roll(blockTables().get(block.key().asString()), context);
    }

    public static List<ItemStack> forEntity(String entityKey, Context context) {
        return roll(entityTables().get(entityKey), context);
    }

    private static Map<String, JsonObject> blockTables() {
        Map<String, JsonObject> tables = blocks;
        if (tables == null) {
            tables = load("block_loot_tables.json");
            blocks = tables;
        }
        return tables;
    }

    private static Map<String, JsonObject> entityTables() {
        Map<String, JsonObject> tables = entities;
        if (tables == null) {
            tables = load("entity_loot_tables.json");
            entities = tables;
        }
        return tables;
    }

    private static Map<String, JsonObject> load(String file) {
        try (InputStream in = LootTables.class.getResourceAsStream(ROOT + file)) {
            if (in == null) {
                return Map.of();
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, JsonObject> tables = new java.util.HashMap<>();
            root.entrySet().forEach(entry -> tables.put(entry.getKey(), entry.getValue().getAsJsonObject()));
            return Map.copyOf(tables);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<ItemStack> roll(JsonObject table, Context context) {
        List<ItemStack> drops = new ArrayList<>();
        if (table == null || !table.has("pools")) {
            return drops;
        }
        for (JsonElement poolElement : table.getAsJsonArray("pools")) {
            JsonObject pool = poolElement.getAsJsonObject();
            if (!conditions(pool, context)) {
                continue;
            }
            int rolls = (int) Math.round(number(pool.get("rolls"), context));
            for (int i = 0; i < rolls; i++) {
                List<JsonObject> candidates = new ArrayList<>();
                for (JsonElement entry : pool.getAsJsonArray("entries")) {
                    expand(entry.getAsJsonObject(), context, candidates);
                }
                JsonObject chosen = weighted(candidates, context);
                if (chosen != null) {
                    produce(chosen, context, drops);
                }
            }
            for (int i = 0; i < drops.size(); i++) {
                drops.set(i, functions(pool, drops.get(i), context));
            }
        }
        for (int i = 0; i < drops.size(); i++) {
            drops.set(i, functions(table, drops.get(i), context));
        }
        drops.removeIf(ItemStack::isAir);
        return drops;
    }

    private static void expand(JsonObject entry, Context context, List<JsonObject> out) {
        if (!conditions(entry, context)) {
            return;
        }
        String type = entry.get("type").getAsString();
        switch (type) {
            case "minecraft:alternatives" -> {
                for (JsonElement child : entry.getAsJsonArray("children")) {
                    List<JsonObject> expanded = new ArrayList<>();
                    expand(child.getAsJsonObject(), context, expanded);
                    if (!expanded.isEmpty()) {
                        out.addAll(expanded);
                        return;
                    }
                }
            }
            case "minecraft:group", "minecraft:sequence" -> {
                for (JsonElement child : entry.getAsJsonArray("children")) {
                    expand(child.getAsJsonObject(), context, out);
                }
            }
            case "minecraft:item", "minecraft:loot_table", "minecraft:empty" -> out.add(entry);
            default -> {
            }
        }
    }

    private static JsonObject weighted(List<JsonObject> candidates, Context context) {
        int total = 0;
        for (JsonObject candidate : candidates) {
            total += weight(candidate);
        }
        if (total <= 0) {
            return null;
        }
        int pick = context.random().nextInt(total);
        for (JsonObject candidate : candidates) {
            pick -= weight(candidate);
            if (pick < 0) {
                return candidate;
            }
        }
        return null;
    }

    private static int weight(JsonObject entry) {
        return entry.has("weight") ? entry.get("weight").getAsInt() : 1;
    }

    private static void produce(JsonObject entry, Context context, List<ItemStack> drops) {
        String type = entry.get("type").getAsString();
        if (type.equals("minecraft:loot_table")) {
            String value = entry.get("value").getAsString().replace("minecraft:entities/", "minecraft:").replace("minecraft:blocks/", "minecraft:");
            JsonObject nested = entityTables().getOrDefault(value, blockTables().get(value));
            drops.addAll(roll(nested, context));
            return;
        }
        if (!type.equals("minecraft:item")) {
            return;
        }
        Material material = Material.fromKey(entry.get("name").getAsString());
        if (material == null) {
            return;
        }
        drops.add(functions(entry, ItemStack.of(material), context));
    }

    private static ItemStack functions(JsonObject holder, ItemStack item, Context context) {
        if (!holder.has("functions")) {
            return item;
        }
        for (JsonElement element : holder.getAsJsonArray("functions")) {
            JsonObject function = element.getAsJsonObject();
            if (!conditions(function, context)) {
                continue;
            }
            item = apply(function, item, context);
        }
        return item;
    }

    private static ItemStack apply(JsonObject function, ItemStack item, Context context) {
        String name = function.get("function").getAsString();
        return switch (name) {
            case "minecraft:set_count" -> {
                int count = (int) Math.floor(number(function.get("count"), context));
                boolean add = function.has("add") && function.get("add").getAsBoolean();
                yield withCount(item, add ? item.amount() + count : count);
            }
            case "minecraft:apply_bonus" -> withCount(item, bonus(function, item.amount(), context));
            case "minecraft:enchanted_count_increase" -> {
                int level = level(context.tool(), function.get("enchantment").getAsString());
                if (level <= 0) {
                    yield item;
                }
                int extra = (int) Math.round(number(function.get("count"), context) * level);
                int limit = function.has("limit") ? function.get("limit").getAsInt() : 0;
                int count = item.amount() + extra;
                yield withCount(item, limit > 0 ? Math.min(limit, count) : count);
            }
            case "minecraft:limit_count" -> {
                JsonObject limit = function.getAsJsonObject("limit");
                int count = item.amount();
                if (limit.has("min")) {
                    count = Math.max(count, (int) number(limit.get("min"), context));
                }
                if (limit.has("max")) {
                    count = Math.min(count, (int) number(limit.get("max"), context));
                }
                yield withCount(item, count);
            }
            case "minecraft:furnace_smelt" -> smelted(item);
            default -> item;
        };
    }

    private static ItemStack smelted(ItemStack item) {
        String key = item.material().key().asString();
        Material cooked = switch (key) {
            case "minecraft:beef" -> Material.COOKED_BEEF;
            case "minecraft:porkchop" -> Material.COOKED_PORKCHOP;
            case "minecraft:chicken" -> Material.COOKED_CHICKEN;
            case "minecraft:mutton" -> Material.COOKED_MUTTON;
            case "minecraft:rabbit" -> Material.COOKED_RABBIT;
            case "minecraft:cod" -> Material.COOKED_COD;
            case "minecraft:salmon" -> Material.COOKED_SALMON;
            default -> null;
        };
        return cooked == null ? item : ItemStack.of(cooked, item.amount());
    }

    private static ItemStack withCount(ItemStack item, int count) {
        if (count <= 0) {
            return ItemStack.AIR;
        }
        return item.withAmount(Math.min(count, item.maxStackSize()));
    }

    private static int bonus(JsonObject function, int count, Context context) {
        int level = level(context.tool(), function.get("enchantment").getAsString());
        String formula = function.get("formula").getAsString();
        RandomGenerator random = context.random();
        JsonObject parameters = function.has("parameters") ? function.getAsJsonObject("parameters") : new JsonObject();
        return switch (formula) {
            case "minecraft:ore_drops" -> level > 0 ? count * (Math.max(0, random.nextInt(level + 2) - 1) + 1) : count;
            case "minecraft:uniform_bonus_count" -> count + random.nextInt(parameters.get("bonusMultiplier").getAsInt() * level + 1);
            case "minecraft:binomial_with_bonus_count" -> {
                int trials = parameters.get("extra").getAsInt() + level;
                float chance = parameters.get("probability").getAsFloat();
                int extra = 0;
                for (int i = 0; i < trials; i++) {
                    if (random.nextFloat() < chance) {
                        extra++;
                    }
                }
                yield count + extra;
            }
            default -> count;
        };
    }

    private static boolean conditions(JsonObject holder, Context context) {
        if (!holder.has("conditions")) {
            return true;
        }
        for (JsonElement condition : holder.getAsJsonArray("conditions")) {
            if (!condition(condition.getAsJsonObject(), context)) {
                return false;
            }
        }
        return true;
    }

    private static boolean condition(JsonObject condition, Context context) {
        String name = condition.get("condition").getAsString();
        return switch (name) {
            case "minecraft:survives_explosion" -> !context.explosion() || context.random().nextFloat() < 0.3f;
            case "minecraft:inverted" -> !condition(condition.getAsJsonObject("term"), context);
            case "minecraft:any_of" -> {
                for (JsonElement term : condition.getAsJsonArray("terms")) {
                    if (condition(term.getAsJsonObject(), context)) {
                        yield true;
                    }
                }
                yield false;
            }
            case "minecraft:all_of" -> {
                for (JsonElement term : condition.getAsJsonArray("terms")) {
                    if (!condition(term.getAsJsonObject(), context)) {
                        yield false;
                    }
                }
                yield true;
            }
            case "minecraft:block_state_property" -> blockState(condition.getAsJsonObject("properties"), context.block());
            case "minecraft:match_tool" -> matchTool(condition.getAsJsonObject("predicate"), context.tool());
            case "minecraft:table_bonus" -> {
                JsonArray chances = condition.getAsJsonArray("chances");
                int level = level(context.tool(), condition.get("enchantment").getAsString());
                yield context.random().nextFloat() < chances.get(Math.min(level, chances.size() - 1)).getAsFloat();
            }
            case "minecraft:random_chance" -> context.random().nextFloat() < number(condition.get("chance"), context);
            case "minecraft:random_chance_with_enchanted_bonus" -> {
                int level = level(context.tool(), condition.get("enchantment").getAsString());
                double chance = condition.get("unenchanted_chance").getAsDouble();
                if (level > 0 && condition.get("enchanted_chance").isJsonObject()) {
                    JsonObject linear = condition.getAsJsonObject("enchanted_chance");
                    chance = linear.get("base").getAsDouble() + linear.get("per_level_above_first").getAsDouble() * (level - 1);
                } else if (level > 0) {
                    chance = condition.get("enchanted_chance").getAsDouble();
                }
                yield context.random().nextDouble() < chance;
            }
            case "minecraft:killed_by_player" -> context.killedByPlayer();
            case "minecraft:entity_properties" -> entityProperties(condition, context);
            default -> false;
        };
    }

    private static boolean entityProperties(JsonObject condition, Context context) {
        if (!"this".equals(condition.get("entity").getAsString())) {
            return false;
        }
        JsonObject predicate = condition.getAsJsonObject("predicate");
        if (predicate.has("minecraft:flags")) {
            JsonObject flags = predicate.getAsJsonObject("minecraft:flags");
            return flags.has("is_on_fire") && flags.get("is_on_fire").getAsBoolean() == context.onFire();
        }
        if (predicate.has("minecraft:components")) {
            JsonObject components = predicate.getAsJsonObject("minecraft:components");
            if (components.has("minecraft:sheep/color")) {
                boolean sheared = predicate.has("minecraft:type_specific/sheep")
                        && predicate.getAsJsonObject("minecraft:type_specific/sheep").get("sheared").getAsBoolean();
                return components.get("minecraft:sheep/color").getAsString().equals(context.sheepColor()) && sheared == context.sheared();
            }
        }
        return false;
    }

    private static boolean blockState(JsonObject properties, Block block) {
        if (block == null) {
            return false;
        }
        for (var entry : properties.entrySet()) {
            if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsString().equals(block.getProperty(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchTool(JsonObject predicate, ItemStack tool) {
        if (tool == null || tool.isAir()) {
            return false;
        }
        if (predicate.has("items")) {
            JsonElement items = predicate.get("items");
            boolean matches = false;
            if (items.isJsonArray()) {
                for (JsonElement item : items.getAsJsonArray()) {
                    matches |= itemMatches(item.getAsString(), tool.material());
                }
            } else {
                matches = itemMatches(items.getAsString(), tool.material());
            }
            if (!matches) {
                return false;
            }
        }
        if (predicate.has("predicates")) {
            JsonObject predicates = predicate.getAsJsonObject("predicates");
            if (predicates.has("minecraft:enchantments")) {
                for (JsonElement element : predicates.getAsJsonArray("minecraft:enchantments")) {
                    JsonObject required = element.getAsJsonObject();
                    int min = required.has("levels") && required.getAsJsonObject("levels").has("min")
                            ? required.getAsJsonObject("levels").get("min").getAsInt() : 1;
                    if (level(tool, required.get("enchantments").getAsString()) < min) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean itemMatches(String id, Material material) {
        if (id.startsWith("#")) {
            RegistryTag<Material> tag = Material.staticRegistry().getTag(TagKey.ofHash(id));
            return tag != null && tag.contains(material);
        }
        return material.key().asString().equals(id);
    }

    public static int level(ItemStack item, String enchantment) {
        if (item == null || enchantment.startsWith("#")) {
            return 0;
        }
        var enchantments = item.get(DataComponents.ENCHANTMENTS);
        return enchantments == null ? 0 : enchantments.level(RegistryKey.<Enchantment>of(enchantment));
    }

    private static double number(JsonElement element, Context context) {
        if (element == null) {
            return 1;
        }
        if (element.isJsonPrimitive()) {
            return element.getAsDouble();
        }
        JsonObject provider = element.getAsJsonObject();
        String type = provider.has("type") ? provider.get("type").getAsString() : "minecraft:uniform";
        return switch (type) {
            case "minecraft:constant" -> provider.get("value").getAsDouble();
            case "minecraft:uniform" -> {
                double min = number(provider.get("min"), context);
                double max = number(provider.get("max"), context);
                yield Math.floor(min + context.random().nextDouble() * (max - min + 1));
            }
            case "minecraft:binomial" -> {
                int trials = (int) number(provider.get("n"), context);
                double chance = number(provider.get("p"), context);
                int hits = 0;
                for (int i = 0; i < trials; i++) {
                    if (context.random().nextDouble() < chance) {
                        hits++;
                    }
                }
                yield hits;
            }
            default -> 1;
        };
    }
}

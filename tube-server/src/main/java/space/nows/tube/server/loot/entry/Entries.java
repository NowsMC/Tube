package space.nows.tube.server.loot.entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.item.Material;
import space.nows.tube.server.loot.LootTable;
import space.nows.tube.server.loot.condition.Conditions;
import space.nows.tube.server.loot.condition.LootCondition;
import space.nows.tube.server.loot.function.Functions;
import space.nows.tube.server.loot.function.LootFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class Entries {

    private Entries() {
    }

    public static LootEntry parse(JsonObject entry, Function<String, LootTable> resolver) {
        List<LootCondition> conditions = Conditions.of(entry);
        int weight = entry.has("weight") ? entry.get("weight").getAsInt() : 1;
        int quality = entry.has("quality") ? entry.get("quality").getAsInt() : 0;
        return switch (entry.get("type").getAsString()) {
            case "minecraft:item" -> item(entry, weight, quality, conditions);
            case "minecraft:loot_table" -> new TableEntry(entry.get("value").getAsString(), resolver, weight, quality, conditions, Functions.of(entry));
            case "minecraft:empty" -> new EmptyEntry(weight, quality, conditions);
            case "minecraft:alternatives" -> new AlternativesEntry(children(entry, resolver), conditions);
            case "minecraft:group", "minecraft:sequence" -> new GroupEntry(children(entry, resolver), conditions);
            default -> new GroupEntry(List.of(), conditions);
        };
    }

    private static LootEntry item(JsonObject entry, int weight, int quality, List<LootCondition> conditions) {
        Material material = Material.fromKey(entry.get("name").getAsString());
        if (material == null) {
            return new GroupEntry(List.of(), conditions);
        }
        List<LootFunction> functions = Functions.of(entry);
        return new ItemEntry(material, weight, quality, conditions, functions);
    }

    private static List<LootEntry> children(JsonObject entry, Function<String, LootTable> resolver) {
        List<LootEntry> children = new ArrayList<>();
        for (JsonElement child : entry.getAsJsonArray("children")) {
            children.add(parse(child.getAsJsonObject(), resolver));
        }
        return List.copyOf(children);
    }
}

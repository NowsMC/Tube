package space.nows.tube.server.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.item.ItemStack;
import space.nows.tube.server.loot.condition.Conditions;
import space.nows.tube.server.loot.entry.Entries;
import space.nows.tube.server.loot.entry.LootEntry;
import space.nows.tube.server.loot.function.Functions;
import space.nows.tube.server.loot.function.LootFunction;
import space.nows.tube.server.loot.number.NumberProviders;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class LootTable {

    private final List<LootPool> pools;
    private final List<LootFunction> functions;

    private LootTable(List<LootPool> pools, List<LootFunction> functions) {
        this.pools = pools;
        this.functions = functions;
    }

    static LootTable parse(JsonObject json, Function<String, LootTable> resolver) {
        List<LootPool> pools = new ArrayList<>();
        if (json.has("pools")) {
            for (JsonElement element : json.getAsJsonArray("pools")) {
                pools.add(pool(element.getAsJsonObject(), resolver));
            }
        }
        return new LootTable(List.copyOf(pools), Functions.of(json));
    }

    private static LootPool pool(JsonObject json, Function<String, LootTable> resolver) {
        List<LootEntry> entries = new ArrayList<>();
        for (JsonElement entry : json.getAsJsonArray("entries")) {
            entries.add(Entries.parse(entry.getAsJsonObject(), resolver));
        }
        return new LootPool(NumberProviders.parse(json.get("rolls")), List.copyOf(entries), Conditions.of(json), Functions.of(json));
    }

    public List<ItemStack> roll(LootContext context) {
        List<ItemStack> drops = new ArrayList<>();
        pools.forEach(pool -> pool.roll(context, drops));
        drops.replaceAll(drop -> LootFunction.applyAll(functions, drop, context));
        drops.removeIf(ItemStack::isAir);
        return drops;
    }
}

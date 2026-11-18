package space.nows.tube.server.loot.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.instance.block.Block;
import space.nows.tube.server.loot.LootContext;

import java.util.Map;

record BlockStateProperty(JsonObject properties) implements LootCondition {

    @Override
    public boolean test(LootContext context) {
        Block block = context.block();
        if (block == null) {
            return false;
        }
        for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
            if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsString().equals(block.getProperty(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }
}

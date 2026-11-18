package space.nows.tube.server.loot;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;

import java.util.List;

public final class LootTables {

    private static final String NAMESPACE = "minecraft:";
    private static final String ENTITIES = "entities/";
    private static final String BLOCKS = "blocks/";
    private static final String CHESTS = "chests/";
    private static final String GAMEPLAY = "gameplay/";

    private static final TableSet BLOCK_TABLES = new TableSet("block_loot_tables.json", LootTables::reference);
    private static final TableSet ENTITY_TABLES = new TableSet("entity_loot_tables.json", LootTables::reference);
    private static final TableSet CHEST_TABLES = new TableSet("chest_loot_tables.json", LootTables::reference);
    private static final TableSet GAMEPLAY_TABLES = new TableSet("gameplay_loot_tables.json", LootTables::reference);

    private LootTables() {
    }

    public static List<ItemStack> block(Block block, LootContext context) {
        return roll(BLOCK_TABLES.find(block.key().asString()), context);
    }

    public static List<ItemStack> entity(EntityType type, LootContext context) {
        return roll(ENTITY_TABLES.find(type.key().asString()), context);
    }

    public static List<ItemStack> chest(String id, LootContext context) {
        return roll(CHEST_TABLES.find(id), context);
    }

    public static List<ItemStack> gameplay(String id, LootContext context) {
        return roll(GAMEPLAY_TABLES.find(id), context);
    }

    private static List<ItemStack> roll(LootTable table, LootContext context) {
        return table == null ? List.of() : table.roll(context);
    }

    private static LootTable reference(String id) {
        if (!id.startsWith(NAMESPACE)) {
            return null;
        }
        String path = id.substring(NAMESPACE.length());
        if (path.startsWith(ENTITIES)) {
            return ENTITY_TABLES.find(NAMESPACE + path.substring(ENTITIES.length()));
        }
        if (path.startsWith(BLOCKS)) {
            return BLOCK_TABLES.find(NAMESPACE + path.substring(BLOCKS.length()));
        }
        if (path.startsWith(CHESTS)) {
            return CHEST_TABLES.find(NAMESPACE + path.substring(CHESTS.length()));
        }
        if (path.startsWith(GAMEPLAY)) {
            return GAMEPLAY_TABLES.find(NAMESPACE + path.substring(GAMEPLAY.length()));
        }
        for (TableSet set : List.of(ENTITY_TABLES, BLOCK_TABLES, GAMEPLAY_TABLES, CHEST_TABLES)) {
            LootTable table = set.find(id);
            if (table != null) {
                return table;
            }
        }
        return null;
    }
}

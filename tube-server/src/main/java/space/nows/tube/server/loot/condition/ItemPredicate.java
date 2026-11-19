package space.nows.tube.server.loot.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.server.item.Enchantments;
import space.nows.tube.server.registry.RegistryTags;

import java.util.ArrayList;
import java.util.List;

record ItemPredicate(List<String> items, List<EnchantRequirement> enchantments) {

    private static final String ENCHANTMENTS = "minecraft:enchantments";

    record EnchantRequirement(String enchantment, int minimumLevel) {
    }

    static ItemPredicate parse(JsonObject predicate) {
        List<String> items = new ArrayList<>();
        if (predicate.has("items")) {
            JsonElement element = predicate.get("items");
            if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(item -> items.add(item.getAsString()));
            } else {
                items.add(element.getAsString());
            }
        }
        List<EnchantRequirement> required = new ArrayList<>();
        if (predicate.has("predicates") && predicate.getAsJsonObject("predicates").has(ENCHANTMENTS)) {
            for (JsonElement element : predicate.getAsJsonObject("predicates").getAsJsonArray(ENCHANTMENTS)) {
                JsonObject requirement = element.getAsJsonObject();
                JsonObject levels = requirement.has("levels") ? requirement.getAsJsonObject("levels") : null;
                int minimum = levels != null && levels.has("min") ? levels.get("min").getAsInt() : 1;
                required.add(new EnchantRequirement(requirement.get("enchantments").getAsString(), minimum));
            }
        }
        return new ItemPredicate(List.copyOf(items), List.copyOf(required));
    }

    boolean test(ItemStack item) {
        if (item == null || item.isAir()) {
            return false;
        }
        if (!items.isEmpty() && items.stream().noneMatch(id -> matches(id, item.material()))) {
            return false;
        }
        for (EnchantRequirement requirement : enchantments) {
            if (Enchantments.level(item, requirement.enchantment()) < requirement.minimumLevel()) {
                return false;
            }
        }
        return true;
    }

    private static boolean matches(String id, Material material) {
        if (id.startsWith("#")) {
            return RegistryTags.has(material, id);
        }
        return material.key().asString().equals(id);
    }
}

package space.nows.tube.server.gameplay.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import space.nows.tube.server.registry.RegistryTags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class RecipeBook {

    public static final RecipeBook EMPTY = new RecipeBook(List.of(), List.of(), Map.of(), List.of());

    public record Ingredient(Set<Material> materials) {

        public boolean test(ItemStack item) {
            return !item.isAir() && materials.contains(item.material());
        }
    }

    public sealed interface Crafting permits Shaped, Shapeless, Transmute {
    }

    public record Shaped(int width, int height, Ingredient[] pattern, ItemStack result) implements Crafting {
    }

    public record Shapeless(List<Ingredient> ingredients, ItemStack result) implements Crafting {
    }

    public record Transmute(Ingredient input, Ingredient material, Material result) implements Crafting {
    }

    public record Cooking(String type, Ingredient input, ItemStack result, float experience, int time) {
    }

    public record Stonecutting(Ingredient input, ItemStack result) {
    }

    private final List<Crafting> crafting;
    private final List<Cooking> cooking;
    private final Map<Material, List<Cooking>> cookingByInput;
    private final List<Stonecutting> stonecutting;

    private RecipeBook(List<Crafting> crafting, List<Cooking> cooking, Map<Material, List<Cooking>> cookingByInput, List<Stonecutting> stonecutting) {
        this.crafting = crafting;
        this.cooking = cooking;
        this.cookingByInput = cookingByInput;
        this.stonecutting = stonecutting;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isEmpty() {
        return crafting.isEmpty();
    }

    public int size() {
        return crafting.size() + cooking.size() + stonecutting.size();
    }

    public Optional<Cooking> cooking(String type, ItemStack input) {
        if (input.isAir()) {
            return Optional.empty();
        }
        for (Cooking recipe : cookingByInput.getOrDefault(input.material(), List.of())) {
            if (recipe.type().equals(type)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    public List<Stonecutting> stonecutting(ItemStack input) {
        List<Stonecutting> matches = new ArrayList<>();
        for (Stonecutting recipe : stonecutting) {
            if (recipe.input().test(input)) {
                matches.add(recipe);
            }
        }
        return matches;
    }

    public ItemStack craft(ItemStack[] grid, int size) {
        int minX = size;
        int minY = size;
        int maxX = -1;
        int maxY = -1;
        List<ItemStack> present = new ArrayList<>();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (!grid[y * size + x].isAir()) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    present.add(grid[y * size + x]);
                }
            }
        }
        if (present.isEmpty()) {
            return ItemStack.AIR;
        }
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        for (Crafting recipe : crafting) {
            ItemStack result = switch (recipe) {
                case Shaped shaped -> shaped.width() == width && shaped.height() == height
                        && (matches(shaped, grid, size, minX, minY, false) || matches(shaped, grid, size, minX, minY, true))
                        ? shaped.result() : ItemStack.AIR;
                case Shapeless shapeless -> matchesShapeless(shapeless.ingredients(), present) ? shapeless.result() : ItemStack.AIR;
                case Transmute transmute -> transmute(transmute, present);
            };
            if (!result.isAir()) {
                return result;
            }
        }
        return ItemStack.AIR;
    }

    private static boolean matches(Shaped recipe, ItemStack[] grid, int size, int offsetX, int offsetY, boolean mirrored) {
        for (int y = 0; y < recipe.height(); y++) {
            for (int x = 0; x < recipe.width(); x++) {
                int patternX = mirrored ? recipe.width() - 1 - x : x;
                Ingredient ingredient = recipe.pattern()[y * recipe.width() + patternX];
                ItemStack item = grid[(y + offsetY) * size + x + offsetX];
                if (ingredient == null ? !item.isAir() : !ingredient.test(item)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean matchesShapeless(List<Ingredient> ingredients, List<ItemStack> items) {
        if (ingredients.size() != items.size()) {
            return false;
        }
        return assign(ingredients, items, 0, new boolean[items.size()]);
    }

    private static boolean assign(List<Ingredient> ingredients, List<ItemStack> items, int index, boolean[] used) {
        if (index == ingredients.size()) {
            return true;
        }
        for (int i = 0; i < items.size(); i++) {
            if (!used[i] && ingredients.get(index).test(items.get(i))) {
                used[i] = true;
                if (assign(ingredients, items, index + 1, used)) {
                    return true;
                }
                used[i] = false;
            }
        }
        return false;
    }

    private static ItemStack transmute(Transmute recipe, List<ItemStack> items) {
        if (items.size() != 2) {
            return ItemStack.AIR;
        }
        for (int i = 0; i < 2; i++) {
            ItemStack input = items.get(i);
            ItemStack material = items.get(1 - i);
            if (recipe.input().test(input) && recipe.material().test(material) && input.material() != recipe.result()) {
                return input.withMaterial(recipe.result()).withAmount(1);
            }
        }
        return ItemStack.AIR;
    }

    public static final class Builder {

        private final List<Crafting> crafting = new ArrayList<>();
        private final List<Cooking> cooking = new ArrayList<>();
        private final List<Stonecutting> stonecutting = new ArrayList<>();

        public Builder add(String name, JsonObject json) {
            String type = json.get("type").getAsString();
            try {
                switch (type) {
                    case "minecraft:crafting_shaped" -> shaped(json);
                    case "minecraft:crafting_shapeless" -> {
                        List<Ingredient> ingredients = new ArrayList<>();
                        for (JsonElement element : json.getAsJsonArray("ingredients")) {
                            ingredients.add(ingredient(element));
                        }
                        crafting.add(new Shapeless(List.copyOf(ingredients), result(json.get("result"))));
                    }
                    case "minecraft:crafting_transmute" -> crafting.add(new Transmute(ingredient(json.get("input")), ingredient(json.get("material")),
                            Material.fromKey(json.getAsJsonObject("result").get("id").getAsString())));
                    case "minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking" -> {
                        int defaultTime = type.equals("minecraft:smelting") ? 200 : type.equals("minecraft:campfire_cooking") ? 600 : 100;
                        cooking.add(new Cooking(type, ingredient(json.get("ingredient")), result(json.get("result")),
                                json.has("experience") ? json.get("experience").getAsFloat() : 0,
                                json.has("cookingtime") ? json.get("cookingtime").getAsInt() : defaultTime));
                    }
                    case "minecraft:stonecutting" -> stonecutting.add(new Stonecutting(ingredient(json.get("ingredient")), result(json.get("result"))));
                    default -> {
                    }
                }
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Bad recipe " + name + ": " + e.getMessage(), e);
            }
            return this;
        }

        private void shaped(JsonObject json) {
            List<String> rows = new ArrayList<>();
            json.getAsJsonArray("pattern").forEach(row -> rows.add(row.getAsString()));
            int width = rows.stream().mapToInt(String::length).max().orElse(0);
            Map<Character, Ingredient> keys = new HashMap<>();
            json.getAsJsonObject("key").entrySet().forEach(entry -> keys.put(entry.getKey().charAt(0), ingredient(entry.getValue())));
            Ingredient[] pattern = new Ingredient[width * rows.size()];
            for (int y = 0; y < rows.size(); y++) {
                String row = rows.get(y);
                for (int x = 0; x < row.length(); x++) {
                    char symbol = row.charAt(x);
                    pattern[y * width + x] = symbol == ' ' ? null : keys.get(symbol);
                }
            }
            crafting.add(new Shaped(width, rows.size(), pattern, result(json.get("result"))));
        }

        private static ItemStack result(JsonElement element) {
            JsonObject result = element.getAsJsonObject();
            Material material = Material.fromKey(result.get("id").getAsString());
            if (material == null) {
                throw new IllegalArgumentException("unknown item " + result.get("id"));
            }
            return ItemStack.of(material, result.has("count") ? result.get("count").getAsInt() : 1);
        }

        static Ingredient ingredient(JsonElement element) {
            Set<Material> materials = new HashSet<>();
            if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(entry -> addIngredient(entry.getAsString(), materials));
            } else if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                addIngredient(object.has("tag") ? "#" + object.get("tag").getAsString() : object.get("item").getAsString(), materials);
            } else {
                addIngredient(element.getAsString(), materials);
            }
            return new Ingredient(Set.copyOf(materials));
        }

        private static void addIngredient(String id, Set<Material> materials) {
            if (id.startsWith("#")) {
                materials.addAll(RegistryTags.items(id));
                return;
            }
            Material material = Material.fromKey(id);
            if (material != null) {
                materials.add(material);
            }
        }

        public RecipeBook build() {
            Map<Material, List<Cooking>> byInput = new HashMap<>();
            for (Cooking recipe : cooking) {
                for (Material material : recipe.input().materials()) {
                    byInput.computeIfAbsent(material, ignored -> new ArrayList<>()).add(recipe);
                }
            }
            byInput.replaceAll((material, list) -> List.copyOf(list));
            return new RecipeBook(List.copyOf(crafting), List.copyOf(cooking), Map.copyOf(byInput), List.copyOf(stonecutting));
        }
    }
}

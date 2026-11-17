package space.nows.tube.server.gameplay;

import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import space.nows.tube.server.gameplay.crafting.CraftingGrids;
import space.nows.tube.server.gameplay.crafting.Furnaces;
import space.nows.tube.server.gameplay.crafting.RecipeBook;
import space.nows.tube.server.gameplay.crafting.VanillaRecipes;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;

public final class SurvivalRules {

    private final Path cache;
    private final String minecraftVersion;
    private final WorldService worlds;
    private final int simulationDistance;
    private volatile RecipeBook recipes = RecipeBook.EMPTY;

    public SurvivalRules(Path cache, String minecraftVersion, WorldService worlds, int simulationDistance) {
        this.cache = cache;
        this.minecraftVersion = minecraftVersion;
        this.worlds = worlds;
        this.simulationDistance = simulationDistance;
    }

    public RecipeBook recipes() {
        return recipes;
    }

    public void install(GlobalEventHandler handler) {
        Thread.ofVirtual().name("tube-recipes").start(() -> recipes = VanillaRecipes.load(cache, minecraftVersion));
        BlockDrops.install(handler);
        Containers.install(handler);
        new CraftingGrids(this::recipes).install(handler);
        new Furnaces(this::recipes).install(handler);
        Falling.install(handler);
        Hunger.install(handler);
        Experience.install(handler);
        DeathDrops.install(handler);
    }

    public static void prepareForSave(Player player) {
        CraftingGrids.returnPlayerGrid(player);
    }
}

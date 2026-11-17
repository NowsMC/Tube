package space.nows.tube.server.gameplay;

import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import space.nows.tube.server.gameplay.block.BlockInteractions;
import space.nows.tube.server.gameplay.crafting.CraftingGrids;
import space.nows.tube.server.gameplay.crafting.Furnaces;
import space.nows.tube.server.gameplay.crafting.RecipeBook;
import space.nows.tube.server.gameplay.crafting.VanillaRecipes;
import space.nows.tube.server.gameplay.mob.Mobs;
import space.nows.tube.server.gameplay.physics.FallingBlocks;
import space.nows.tube.server.gameplay.physics.Fluids;
import space.nows.tube.server.gameplay.physics.Growth;
import space.nows.tube.server.gameplay.physics.ItemUse;
import space.nows.tube.server.gameplay.physics.NetherPortals;
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
        BlockInteractions.install(handler);
        BlockDrops.install(handler);
        Containers.install(handler);
        new CraftingGrids(this::recipes).install(handler);
        new Furnaces(this::recipes).install(handler);
        Combat.install(handler);
        Falling.install(handler);
        Hunger.install(handler);
        Experience.install(handler);
        DeathDrops.install(handler);
        new Beds(worlds).install(handler);
        Mobs.install(handler);
        Hazards.install();
        NetherPortals portals = new NetherPortals(worlds);
        portals.install();
        new ItemUse(portals).install(handler);
        new Fluids().install(handler);
        new FallingBlocks().install(handler);
        new Growth(simulationDistance).install(handler);
    }

    public static void prepareForSave(Player player) {
        CraftingGrids.returnPlayerGrid(player);
    }
}

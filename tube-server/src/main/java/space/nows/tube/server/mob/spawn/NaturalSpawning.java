package space.nows.tube.server.mob.spawn;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.Difficulty;
import space.nows.tube.server.mob.Creature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class NaturalSpawning {

    private static final int HOSTILE_CAP = 16;
    private static final int ANIMAL_CAP = 10;
    private static final int FISH_CAP = 15;
    private static final double CAP_RANGE = 128;
    private static final int INTERVAL_TICKS = 20;

    private NaturalSpawning() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager().buildTask(NaturalSpawning::tick).repeat(TaskSchedule.tick(INTERVAL_TICKS)).schedule();
    }

    private static void tick() {
        Difficulty difficulty = MinecraftServer.getDifficulty();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            List<Player> players = new ArrayList<>(instance.getPlayers());
            List<Creature> creatures = new ArrayList<>();
            for (Entity entity : instance.getEntities()) {
                if (entity instanceof Creature creature && !creature.isDead()) {
                    creatures.add(creature);
                }
            }
            if (!SpawnSpots.overworld(instance)) {
                continue;
            }
            for (Player player : players) {
                if (player.getGameMode() != GameMode.SPECTATOR) {
                    populate(instance, player, creatures, difficulty, random);
                }
            }
        }
    }

    private static void populate(Instance instance, Player player, List<Creature> creatures, Difficulty difficulty, ThreadLocalRandom random) {
        int hostiles = 0;
        int animals = 0;
        int fish = 0;
        for (Creature creature : creatures) {
            if (creature.getPosition().distanceSquared(player.getPosition()) < CAP_RANGE * CAP_RANGE) {
                if (creature.kind().aquatic()) {
                    fish++;
                } else if (creature.kind().disposition().hostile()) {
                    hostiles++;
                } else {
                    animals++;
                }
            }
        }
        if (difficulty != Difficulty.PEACEFUL && hostiles < HOSTILE_CAP) {
            MonsterSpawner.attempt(instance, player, random);
        }
        if (animals < ANIMAL_CAP && random.nextInt(4) == 0) {
            HerdSpawner.attempt(instance, player, random);
        }
        if (fish < FISH_CAP && random.nextInt(2) == 0) {
            FishSpawner.attempt(instance, player, random);
        }
    }
}

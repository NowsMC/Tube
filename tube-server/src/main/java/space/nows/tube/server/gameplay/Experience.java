package space.nows.tube.server.gameplay;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.PickupExperienceEvent;
import net.minestom.server.instance.Instance;

import java.util.concurrent.ThreadLocalRandom;

final class Experience {

    private Experience() {
    }

    static void install(GlobalEventHandler handler) {
        handler.addListener(PickupExperienceEvent.class, event -> add(event.getPlayer(), event.getExperienceOrb().getExperienceCount()));
    }

    static void spawn(Instance instance, Point position, int amount) {
        while (amount > 0) {
            int orb = Math.min(amount, amount >= 37 ? 37 : amount >= 17 ? 17 : amount >= 7 ? 7 : amount >= 3 ? 3 : 1);
            amount -= orb;
            ExperienceOrb entity = new ExperienceOrb((short) orb);
            entity.setInstance(instance, position.add(ThreadLocalRandom.current().nextDouble(-0.3, 0.3), 0, ThreadLocalRandom.current().nextDouble(-0.3, 0.3)));
        }
    }

    static int between(int min, int max) {
        return min + ThreadLocalRandom.current().nextInt(max - min + 1);
    }

    static int toNextLevel(int level) {
        return level >= 30 ? 112 + (level - 30) * 9 : level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
    }

    static int total(Player player) {
        int total = 0;
        for (int level = 0; level < player.getLevel(); level++) {
            total += toNextLevel(level);
        }
        return total + Math.round(player.getExp() * toNextLevel(player.getLevel()));
    }

    static void add(Player player, int amount) {
        int level = player.getLevel();
        float progress = player.getExp() * toNextLevel(level) + amount;
        while (progress >= toNextLevel(level)) {
            progress -= toNextLevel(level);
            level++;
        }
        player.setLevel(level);
        player.setExp(Math.clamp(progress / toNextLevel(level), 0f, 0.9999f));
    }

    static void reset(Player player) {
        player.setLevel(0);
        player.setExp(0);
    }
}

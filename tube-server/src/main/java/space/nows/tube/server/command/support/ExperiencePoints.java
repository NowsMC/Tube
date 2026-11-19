package space.nows.tube.server.command.support;

import space.nows.tube.api.entity.player.Player;

public final class ExperiencePoints {

    private ExperiencePoints() {
    }

    public static int toNextLevel(int level) {
        if (level >= 30) {
            return 9 * level - 158;
        }
        if (level >= 15) {
            return 5 * level - 38;
        }
        return 2 * level + 7;
    }

    public static int total(Player player) {
        int points = 0;
        for (int level = 0; level < player.level(); level++) {
            points += toNextLevel(level);
        }
        return points + Math.round(player.experienceProgress() * toNextLevel(player.level()));
    }

    public static void setTotal(Player player, int points) {
        int remaining = Math.max(0, points);
        int level = 0;
        while (remaining >= toNextLevel(level)) {
            remaining -= toNextLevel(level);
            level++;
        }
        player.setLevel(level);
        player.setExperienceProgress((float) remaining / toNextLevel(level));
    }
}

package space.nows.tube.server.mob.kind.boss;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

import java.util.HashSet;
import java.util.Set;

final class Banner implements Trait {

    private static final double RANGE = 64;

    private final Creature boss;
    private final BossBar bar;
    private final Set<Player> viewers = new HashSet<>();

    Banner(Creature boss, String translation, BossBar.Color color) {
        this.boss = boss;
        this.bar = BossBar.bossBar(Component.translatable(translation), 1f, color, BossBar.Overlay.PROGRESS);
    }

    @Override
    public void tick() {
        float progress = (float) Math.clamp(boss.getHealth() / boss.getAttributeValue(Attribute.MAX_HEALTH), 0, 1);
        bar.progress(progress);
        Set<Player> near = new HashSet<>();
        if (!boss.isDead() && !boss.isRemoved() && boss.getInstance() != null) {
            for (Player player : boss.getInstance().getPlayers()) {
                if (player.getPosition().distanceSquared(boss.getPosition()) < RANGE * RANGE) {
                    near.add(player);
                }
            }
        }
        for (Player player : near) {
            if (viewers.add(player)) {
                player.showBossBar(bar);
            }
        }
        viewers.removeIf(player -> {
            if (!near.contains(player)) {
                player.hideBossBar(bar);
                return true;
            }
            return false;
        });
    }
}

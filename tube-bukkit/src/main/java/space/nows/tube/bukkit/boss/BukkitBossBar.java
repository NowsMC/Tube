package space.nows.tube.bukkit.boss;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import space.nows.tube.bukkit.BukkitRuntime;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class BukkitBossBar implements org.bukkit.boss.BossBar {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final BukkitRuntime runtime;
    private final BossBar bar;
    private final Set<UUID> viewers = new LinkedHashSet<>();
    private String title;
    private BarColor color;
    private BarStyle style;
    private boolean visible = true;

    public BukkitBossBar(BukkitRuntime runtime, String title, BarColor color, BarStyle style, BarFlag... flags) {
        this.runtime = runtime;
        this.title = title == null ? "" : title;
        this.color = color;
        this.style = style;
        this.bar = BossBar.bossBar(LEGACY.deserialize(this.title), 1.0f, adventure(color), adventure(style));
        for (BarFlag flag : flags) {
            addFlag(flag);
        }
    }

    private static BossBar.Color adventure(BarColor color) {
        return BossBar.Color.valueOf(color.name());
    }

    private static BossBar.Overlay adventure(BarStyle style) {
        return switch (style) {
            case SOLID -> BossBar.Overlay.PROGRESS;
            case SEGMENTED_6 -> BossBar.Overlay.NOTCHED_6;
            case SEGMENTED_10 -> BossBar.Overlay.NOTCHED_10;
            case SEGMENTED_12 -> BossBar.Overlay.NOTCHED_12;
            case SEGMENTED_20 -> BossBar.Overlay.NOTCHED_20;
        };
    }

    private static BossBar.Flag adventure(BarFlag flag) {
        return switch (flag) {
            case DARKEN_SKY -> BossBar.Flag.DARKEN_SCREEN;
            case PLAY_BOSS_MUSIC -> BossBar.Flag.PLAY_BOSS_MUSIC;
            case CREATE_FOG -> BossBar.Flag.CREATE_WORLD_FOG;
        };
    }

    private space.nows.tube.api.Player tube(UUID uuid) {
        return runtime.tube().players().stream().filter(player -> player.uuid().equals(uuid)).findFirst().orElse(null);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title == null ? "" : title;
        bar.name(LEGACY.deserialize(this.title));
    }

    @Override
    public BarColor getColor() {
        return color;
    }

    @Override
    public void setColor(BarColor color) {
        this.color = color;
        bar.color(adventure(color));
    }

    @Override
    public BarStyle getStyle() {
        return style;
    }

    @Override
    public void setStyle(BarStyle style) {
        this.style = style;
        bar.overlay(adventure(style));
    }

    @Override
    public void removeFlag(BarFlag flag) {
        bar.removeFlag(adventure(flag));
    }

    @Override
    public void addFlag(BarFlag flag) {
        bar.addFlag(adventure(flag));
    }

    @Override
    public boolean hasFlag(BarFlag flag) {
        return bar.hasFlag(adventure(flag));
    }

    @Override
    public void setProgress(double progress) {
        if (progress < 0 || progress > 1) {
            throw new IllegalArgumentException("Progress must be between 0.0 and 1.0 (" + progress + ")");
        }
        bar.progress((float) progress);
    }

    @Override
    public double getProgress() {
        return bar.progress();
    }

    @Override
    public synchronized void addPlayer(Player player) {
        if (viewers.add(player.getUniqueId()) && visible) {
            var tube = tube(player.getUniqueId());
            if (tube != null) {
                tube.showBossBar(bar);
            }
        }
    }

    @Override
    public synchronized void removePlayer(Player player) {
        if (viewers.remove(player.getUniqueId())) {
            var tube = tube(player.getUniqueId());
            if (tube != null) {
                tube.hideBossBar(bar);
            }
        }
    }

    @Override
    public synchronized void removeAll() {
        for (UUID uuid : new ArrayList<>(viewers)) {
            var tube = tube(uuid);
            if (tube != null) {
                tube.hideBossBar(bar);
            }
        }
        viewers.clear();
    }

    @Override
    public synchronized List<Player> getPlayers() {
        List<Player> result = new ArrayList<>();
        for (UUID uuid : viewers) {
            var tube = tube(uuid);
            if (tube != null) {
                result.add(runtime.adapters().player(tube));
            }
        }
        return result;
    }

    @Override
    public synchronized void setVisible(boolean visible) {
        if (this.visible == visible) {
            return;
        }
        this.visible = visible;
        for (UUID uuid : viewers) {
            var tube = tube(uuid);
            if (tube != null) {
                if (visible) {
                    tube.showBossBar(bar);
                } else {
                    tube.hideBossBar(bar);
                }
            }
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void show() {
        setVisible(true);
    }

    @Override
    public void hide() {
        setVisible(false);
    }
}

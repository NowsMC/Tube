package org.bukkit.boss;

import org.bukkit.entity.Player;

import java.util.List;

public interface BossBar {

    String getTitle();

    void setTitle(String title);

    BarColor getColor();

    void setColor(BarColor color);

    BarStyle getStyle();

    void setStyle(BarStyle style);

    void removeFlag(BarFlag flag);

    void addFlag(BarFlag flag);

    boolean hasFlag(BarFlag flag);

    void setProgress(double progress);

    double getProgress();

    void addPlayer(Player player);

    void removePlayer(Player player);

    void removeAll();

    List<Player> getPlayers();

    void setVisible(boolean visible);

    boolean isVisible();

    void show();

    void hide();
}

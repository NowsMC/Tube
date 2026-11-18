package org.bukkit.entity;

import org.bukkit.OfflinePlayer;

import java.net.InetSocketAddress;

public interface Player extends HumanEntity, OfflinePlayer {

    boolean isSneaking();

    boolean isSprinting();

    void hidePlayer(Player other);

    void hidePlayer(org.bukkit.plugin.Plugin plugin, Player other);

    void showPlayer(Player other);

    void showPlayer(org.bukkit.plugin.Plugin plugin, Player other);

    boolean canSee(Player other);

    org.bukkit.scoreboard.Scoreboard getScoreboard();

    void setScoreboard(org.bukkit.scoreboard.Scoreboard scoreboard);

    void setPlayerListName(String name);


    String getDisplayName();

    String getPlayerListName();

    void kickPlayer(String message);

    boolean performCommand(String command);

    int getFoodLevel();

    void setFoodLevel(int level);

    int getLevel();

    int getPing();

    void sendPluginMessage(org.bukkit.plugin.Plugin source, String channel, byte[] message);

    java.util.Set<String> getListeningPluginChannels();

    void setLevel(int level);

    float getExp();

    void setExp(float exp);

    float getSaturation();

    void setSaturation(float saturation);

    boolean getAllowFlight();

    void setAllowFlight(boolean flight);

    org.bukkit.Location getRespawnLocation();

    void setRespawnLocation(org.bukkit.Location location);

    InetSocketAddress getAddress();

    @Override
    boolean isOnline();

    @Override
    Player getPlayer();

    void sendTitle(String title, String subtitle);

    void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut);

    void resetTitle();

    String getPlayerListHeader();

    String getPlayerListFooter();

    void setPlayerListHeader(String header);

    void setPlayerListFooter(String footer);

    void setPlayerListHeaderFooter(String header, String footer);

    void playSound(org.bukkit.Location location, String sound, float volume, float pitch);

    void playSound(org.bukkit.Location location, String sound, org.bukkit.SoundCategory category, float volume, float pitch);

    void stopSound(String sound);

    void stopSound(String sound, org.bukkit.SoundCategory category);

    void stopAllSounds();

    @Override
    default Spigot spigot() {
        Player player = this;
        return new Spigot() {
            @Override
            public int getPing() {
                return player.getPing();
            }
        };
    }

    class Spigot extends Entity.Spigot {

        public int getPing() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}

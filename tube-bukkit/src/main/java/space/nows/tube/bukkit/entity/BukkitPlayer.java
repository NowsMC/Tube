package space.nows.tube.bukkit.entity;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import space.nows.tube.bukkit.BukkitRuntime;
import space.nows.tube.bukkit.inventory.BukkitPlayerInventory;

import java.net.InetSocketAddress;
import java.util.Set;

public final class BukkitPlayer extends BukkitLivingEntity implements Player {

    private final space.nows.tube.api.entity.Player tube;
    private String playerListHeader;
    private String playerListFooter;

    public BukkitPlayer(BukkitRuntime runtime, space.nows.tube.api.entity.Player tube) {
        super(runtime, tube);
        this.tube = tube;
    }

    @Override
    protected boolean granted(String node) {
        return tube.hasPermission(node);
    }

    @Override
    public String getName() {
        return tube.name();
    }

    @Override
    public String getDisplayName() {
        return tube.name();
    }

    @Override
    public String getPlayerListName() {
        return tube.name();
    }

    @Override
    public boolean isOnline() {
        return !tube.isRemoved();
    }

    @Override
    public Player getPlayer() {
        return this;
    }

    @Override
    public EntityType getType() {
        return EntityType.PLAYER;
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.valueOf(tube.gameMode().name());
    }

    @Override
    public void setGameMode(GameMode mode) {
        tube.setGameMode(space.nows.tube.api.entity.GameMode.valueOf(mode.name()));
    }

    @Override
    public int getFoodLevel() {
        return tube.food();
    }

    @Override
    public void setFoodLevel(int level) {
        tube.setFood(level);
    }

    @Override
    public int getLevel() {
        return tube.level();
    }

    @Override
    public void setLevel(int level) {
        tube.setLevel(level);
    }

    @Override
    public float getExp() {
        return tube.experienceProgress();
    }

    @Override
    public void setExp(float exp) {
        tube.setExperienceProgress(exp);
    }

    @Override
    public float getSaturation() {
        return tube.saturation();
    }

    @Override
    public void setSaturation(float saturation) {
        tube.setSaturation(saturation);
    }

    @Override
    public boolean getAllowFlight() {
        return tube.allowFlight();
    }

    @Override
    public void setAllowFlight(boolean flight) {
        tube.setAllowFlight(flight);
    }

    @Override
    public org.bukkit.Location getRespawnLocation() {
        return tube.respawnPoint().map(runtime.adapters()::location).orElse(null);
    }

    @Override
    public void setRespawnLocation(org.bukkit.Location location) {
        tube.setRespawnPoint(location == null ? null : runtime.adapters().tubeLocation(location));
    }

    @Override
    public PlayerInventory getInventory() {
        return new BukkitPlayerInventory(tube, this);
    }

    @Override
    public ItemStack getItemInHand() {
        return getInventory().getItemInMainHand();
    }

    @Override
    public void setItemInHand(ItemStack item) {
        getInventory().setItemInMainHand(item);
    }

    @Override
    public int getPing() {
        return tube.latency();
    }

    @Override
    public void sendPluginMessage(Plugin source, String channel, byte[] message) {
        runtime.messenger().send(source, tube, channel, message);
    }

    @Override
    public Set<String> getListeningPluginChannels() {
        return runtime.messenger().listening();
    }

    @Override
    public InetSocketAddress getAddress() {
        return tube.address().orElse(null);
    }

    @Override
    public void kickPlayer(String message) {
        tube.kick(message == null ? "" : message);
    }

    @Override
    public boolean performCommand(String command) {
        return runtime.commands().dispatch(this, command);
    }

    @Override
    public boolean isOp() {
        return runtime.tube().permissions().isOp(tube.uuid());
    }

    @Override
    public void setOp(boolean value) {
        runtime.tube().permissions().setOp(tube.uuid(), tube.name(), value);
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public boolean isWhitelisted() {
        return false;
    }

    @Override
    public void setWhitelisted(boolean whitelisted) {
    }

    @Override
    public long getFirstPlayed() {
        return 0;
    }

    @Override
    public long getLastPlayed() {
        return System.currentTimeMillis();
    }

    @Override
    public boolean hasPlayedBefore() {
        return true;
    }

    @Override
    public void sendMessage(String message) {
        tube.sendMessage(LEGACY.deserialize(message));
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public void sendTitle(String title, String subtitle) {
        sendTitle(title, subtitle, 10, 70, 20);
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        tube.showTitle(legacy(title), legacy(subtitle), fadeIn < 0 ? 10 : fadeIn, stay < 0 ? 70 : stay, fadeOut < 0 ? 20 : fadeOut);
    }

    @Override
    public void resetTitle() {
        tube.resetTitle();
    }

    @Override
    public String getPlayerListHeader() {
        return playerListHeader;
    }

    @Override
    public String getPlayerListFooter() {
        return playerListFooter;
    }

    @Override
    public void setPlayerListHeader(String header) {
        setPlayerListHeaderFooter(header, playerListFooter);
    }

    @Override
    public void setPlayerListFooter(String footer) {
        setPlayerListHeaderFooter(playerListHeader, footer);
    }

    @Override
    public void setPlayerListHeaderFooter(String header, String footer) {
        this.playerListHeader = header;
        this.playerListFooter = footer;
        tube.setPlayerListHeaderAndFooter(legacy(header), legacy(footer));
    }

    @Override
    public void playSound(Location location, String sound, float volume, float pitch) {
        playSound(location, sound, SoundCategory.MASTER, volume, pitch);
    }

    @Override
    public void playSound(Location location, String sound, SoundCategory category, float volume, float pitch) {
        if (location == null || sound == null || category == null) {
            return;
        }
        tube.playSound(Sound.sound(Key.key(sound), source(category), volume, pitch), location.getX(), location.getY(), location.getZ());
    }

    @Override
    public void stopSound(String sound) {
        tube.stopSound(SoundStop.named(Key.key(sound)));
    }

    @Override
    public void stopSound(String sound, SoundCategory category) {
        tube.stopSound(SoundStop.namedOnSource(Key.key(sound), source(category)));
    }

    @Override
    public void stopAllSounds() {
        tube.stopSound(SoundStop.all());
    }

    private static Sound.Source source(SoundCategory category) {
        return switch (category) {
            case MASTER -> Sound.Source.MASTER;
            case MUSIC -> Sound.Source.MUSIC;
            case RECORDS -> Sound.Source.RECORD;
            case WEATHER -> Sound.Source.WEATHER;
            case BLOCKS -> Sound.Source.BLOCK;
            case HOSTILE -> Sound.Source.HOSTILE;
            case NEUTRAL -> Sound.Source.NEUTRAL;
            case PLAYERS -> Sound.Source.PLAYER;
            case AMBIENT -> Sound.Source.AMBIENT;
            case VOICE -> Sound.Source.VOICE;
            case UI -> Sound.Source.UI;
        };
    }

    private static Component legacy(String text) {
        return text == null || text.isEmpty() ? Component.empty() : LEGACY.deserialize(text);
    }

    @Override
    public String toString() {
        return "CraftPlayer{name=" + tube.name() + "}";
    }
}

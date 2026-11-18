package space.nows.tube.api.entity;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import space.nows.tube.api.ResourcePack;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.inventory.Inventory;
import space.nows.tube.api.world.Location;

import java.net.InetSocketAddress;
import java.util.Optional;

public interface Player extends LivingEntity, CommandSender {

    Optional<InetSocketAddress> address();

    GameMode gameMode();

    void setGameMode(GameMode gameMode);

    Inventory inventory();

    int food();

    void setFood(int food);

    int level();

    void setLevel(int level);

    float experienceProgress();

    void setExperienceProgress(float progress);

    float saturation();

    void setSaturation(float saturation);

    boolean allowFlight();

    void setAllowFlight(boolean allowFlight);

    Optional<Location> respawnPoint();

    void setRespawnPoint(Location location);

    int heldSlot();

    void setHeldSlot(int slot);

    void openInventory(Inventory inventory);

    Optional<Inventory> openedInventory();

    void closeInventory();

    void sendActionBar(Component message);

    void showBossBar(net.kyori.adventure.bossbar.BossBar bar);

    void hideBossBar(net.kyori.adventure.bossbar.BossBar bar);

    void showTitle(Component title, Component subtitle);

    void showTitle(Component title, Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks);

    void clearTitle();

    void resetTitle();

    void setPlayerListHeaderAndFooter(Component header, Component footer);

    void playSound(Sound sound, double x, double y, double z);

    void stopSound(SoundStop stop);

    Optional<Skin> skin();

    void setSkin(Skin skin);

    LoginMode loginMode();

    int latency();

    int protocolVersion();

    Optional<String> clientBrand();

    void sendResourcePack(ResourcePack pack);

    void kick(Component reason);

    default void kick(String reason) {
        kick(Component.text(reason));
    }
}

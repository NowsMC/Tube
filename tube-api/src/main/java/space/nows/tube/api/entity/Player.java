package space.nows.tube.api.entity;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.ResourcePack;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.inventory.Inventory;

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

    int heldSlot();

    void setHeldSlot(int slot);

    void openInventory(Inventory inventory);

    void closeInventory();

    void sendActionBar(Component message);

    void showBossBar(net.kyori.adventure.bossbar.BossBar bar);

    void hideBossBar(net.kyori.adventure.bossbar.BossBar bar);

    void showTitle(Component title, Component subtitle);

    int latency();

    int protocolVersion();

    Optional<String> clientBrand();

    void sendResourcePack(ResourcePack pack);

    void kick(Component reason);

    default void kick(String reason) {
        kick(Component.text(reason));
    }
}

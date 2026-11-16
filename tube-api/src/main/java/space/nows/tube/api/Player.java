package space.nows.tube.api;

import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.util.Optional;

public interface Player extends LivingEntity, CommandSender {

    Optional<InetSocketAddress> address();

    GameMode gameMode();

    void setGameMode(GameMode gameMode);

    Inventory inventory();

    int heldSlot();

    void setHeldSlot(int slot);

    void openInventory(Inventory inventory);

    void closeInventory();

    void sendActionBar(Component message);

    void showTitle(Component title, Component subtitle);

    void kick(Component reason);

    default void kick(String reason) {
        kick(Component.text(reason));
    }
}

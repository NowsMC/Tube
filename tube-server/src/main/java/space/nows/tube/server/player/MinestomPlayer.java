package space.nows.tube.server.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import space.nows.tube.api.GameMode;
import space.nows.tube.api.Inventory;
import space.nows.tube.api.Player;
import space.nows.tube.api.ResourcePack;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.entity.MinestomLivingEntity;
import space.nows.tube.server.inventory.MinestomInventory;
import space.nows.tube.server.world.WorldService;

import java.net.InetSocketAddress;
import java.util.Optional;

public final class MinestomPlayer extends MinestomLivingEntity implements Player {

    private final net.minestom.server.entity.Player player;
    private final PermissionService permissions;
    private final PlayerService service;
    private volatile String clientBrand;

    MinestomPlayer(net.minestom.server.entity.Player player, WorldService worlds, PermissionService permissions, PlayerService service) {
        super(player, worlds);
        this.player = player;
        this.permissions = permissions;
        this.service = service;
    }

    @Override
    public java.util.concurrent.CompletableFuture<Void> teleport(space.nows.tube.api.Location target) {
        var bus = service.events();
        if (bus != null) {
            var posted = bus.post(new space.nows.tube.api.event.PlayerTeleportEvent(this, location(), target));
            if (posted.isCancelled()) {
                return java.util.concurrent.CompletableFuture.completedFuture(null);
            }
            target = posted.to();
        }
        return super.teleport(target);
    }

    @Override
    public String name() {
        return player.getUsername();
    }

    @Override
    public boolean hasPermission(String node) {
        return permissions.has(player.getUuid(), node);
    }

    @Override
    public Optional<InetSocketAddress> address() {
        return player.getPlayerConnection().getRemoteAddress() instanceof InetSocketAddress address ? Optional.of(address) : Optional.empty();
    }

    @Override
    public GameMode gameMode() {
        return GameMode.valueOf(player.getGameMode().name());
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        player.setGameMode(net.minestom.server.entity.GameMode.valueOf(gameMode.name()));
    }

    @Override
    public Inventory inventory() {
        return new MinestomInventory(player.getInventory());
    }

    @Override
    public int food() {
        return player.getFood();
    }

    @Override
    public void setFood(int food) {
        player.setFood(food);
    }

    @Override
    public int level() {
        return player.getLevel();
    }

    @Override
    public void setLevel(int level) {
        player.setLevel(level);
    }

    @Override
    public int heldSlot() {
        return player.getHeldSlot();
    }

    @Override
    public void setHeldSlot(int slot) {
        if (slot < 0 || slot > 8) {
            throw new IllegalArgumentException("Held slot must be between 0 and 8");
        }
        player.setHeldItemSlot((byte) slot);
    }

    @Override
    public void openInventory(Inventory inventory) {
        if (inventory instanceof MinestomInventory tube && tube.handle() instanceof net.minestom.server.inventory.Inventory container) {
            player.openInventory(container);
            return;
        }
        throw new IllegalArgumentException("Only inventories created by the server can be opened");
    }

    @Override
    public void closeInventory() {
        player.closeInventory();
    }

    @Override
    public void sendActionBar(Component message) {
        player.sendActionBar(message);
    }

    @Override
    public void showTitle(Component title, Component subtitle) {
        player.showTitle(Title.title(title, subtitle));
    }

    public void setClientBrand(String brand) {
        this.clientBrand = brand;
    }

    @Override
    public int latency() {
        return player.getLatency();
    }

    @Override
    public int protocolVersion() {
        return player.getPlayerConnection().getProtocolVersion();
    }

    @Override
    public Optional<String> clientBrand() {
        return Optional.ofNullable(clientBrand);
    }

    @Override
    public void sendResourcePack(ResourcePack pack) {
        var request = net.kyori.adventure.resource.ResourcePackRequest.resourcePackRequest()
                .packs(net.kyori.adventure.resource.ResourcePackInfo.resourcePackInfo(pack.id(), java.net.URI.create(pack.url()), pack.sha1()))
                .required(pack.required())
                .prompt(pack.prompt())
                .build();
        player.sendResourcePacks(request);
    }

    @Override
    public void showBossBar(net.kyori.adventure.bossbar.BossBar bar) {
        player.showBossBar(bar);
    }

    @Override
    public void hideBossBar(net.kyori.adventure.bossbar.BossBar bar) {
        player.hideBossBar(bar);
    }

    @Override
    public void kick(Component reason) {
        player.kick(reason);
    }

    @Override
    public void remove() {
        player.kick(Component.text("Removed from the server"));
    }

    @Override
    public void sendMessage(Component message) {
        player.sendMessage(message);
    }
}

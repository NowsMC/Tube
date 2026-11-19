package space.nows.tube.server.player;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.entity.PlayerSkin;
import space.nows.tube.api.entity.player.GameMode;
import space.nows.tube.api.entity.player.LoginMode;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.entity.player.ResourcePack;
import space.nows.tube.api.entity.player.Skin;
import space.nows.tube.api.inventory.Inventory;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.entity.MinestomLivingEntity;
import space.nows.tube.server.inventory.MinestomInventory;
import space.nows.tube.server.scoreboard.MinestomTabList;
import space.nows.tube.server.world.WorldService;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Optional;

public final class MinestomPlayer extends MinestomLivingEntity implements Player {

    private final net.minestom.server.entity.Player player;
    private final PermissionService permissions;
    private final PlayerService service;
    private volatile String clientBrand;
    private final MinestomTabList tabList;

    MinestomPlayer(net.minestom.server.entity.Player player, WorldService worlds, PermissionService permissions, PlayerService service) {
        super(player, worlds);
        this.player = player;
        this.permissions = permissions;
        this.service = service;
        this.tabList = new MinestomTabList(player);
    }

    public net.minestom.server.entity.Player handle() {
        return player;
    }

    @Override
    public space.nows.tube.api.scoreboard.tab.TabList tabList() {
        return tabList;
    }

    @Override
    public java.util.concurrent.CompletableFuture<Void> teleport(space.nows.tube.api.world.Location target) {
        var bus = service.events();
        if (bus != null) {
            var posted = bus.post(new space.nows.tube.api.event.player.PlayerTeleportEvent(this, location(), target));
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
    public float experienceProgress() {
        return player.getExp();
    }

    @Override
    public void setExperienceProgress(float progress) {
        player.setExp(Math.clamp(progress, 0f, 1f));
    }

    @Override
    public float saturation() {
        return player.getFoodSaturation();
    }

    @Override
    public void setSaturation(float saturation) {
        player.setFoodSaturation(Math.clamp(saturation, 0f, 20f));
    }

    @Override
    public boolean allowFlight() {
        return player.isAllowFlying();
    }

    @Override
    public void setAllowFlight(boolean allowFlight) {
        player.setAllowFlying(allowFlight);
        if (!allowFlight) {
            player.setFlying(false);
        }
    }

    @Override
    public Optional<space.nows.tube.api.world.Location> respawnPoint() {
        RespawnPoint point = player.getTag(RespawnPoint.TAG);
        if (point == null) {
            return Optional.empty();
        }
        return worlds.get(point.world()).map(world ->
                new space.nows.tube.api.world.Location(world, point.position().x(), point.position().y(), point.position().z()));
    }

    @Override
    public void setRespawnPoint(space.nows.tube.api.world.Location location) {
        if (location == null) {
            player.removeTag(RespawnPoint.TAG);
            return;
        }
        player.setTag(RespawnPoint.TAG, new RespawnPoint(location.world().name(),
                new net.minestom.server.coordinate.Pos(location.x(), location.y(), location.z())));
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
    public Optional<Inventory> openedInventory() {
        return Optional.ofNullable(player.getOpenInventory()).map(MinestomInventory::new);
    }

    @Override
    public void openBook(net.kyori.adventure.inventory.Book book) {
        player.openBook(book);
    }

    @Override
    public void hidePlayer(space.nows.tube.api.entity.player.Player other) {
        service.visibility().hide(uuid(), other.uuid());
        refreshViewers(other);
    }

    @Override
    public void showPlayer(space.nows.tube.api.entity.player.Player other) {
        service.visibility().show(uuid(), other.uuid());
        refreshViewers(other);
    }

    @Override
    public boolean canSee(space.nows.tube.api.entity.player.Player other) {
        return service.visibility().canSee(uuid(), other.uuid());
    }

    private static void refreshViewers(space.nows.tube.api.entity.player.Player target) {
        if (target instanceof MinestomPlayer minestom) {
            minestom.player.updateViewableRule();
        }
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

    @Override
    public void showTitle(Component title, Component subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        player.showTitle(Title.title(title, subtitle, Title.Times.times(ticks(fadeInTicks), ticks(stayTicks), ticks(fadeOutTicks))));
    }

    private static Duration ticks(int ticks) {
        return Duration.ofMillis(Math.max(0, ticks) * 50L);
    }

    @Override
    public void clearTitle() {
        player.clearTitle();
    }

    @Override
    public void resetTitle() {
        player.resetTitle();
    }

    @Override
    public void setPlayerListHeaderAndFooter(Component header, Component footer) {
        tabList.setHeaderAndFooter(header, footer);
    }

    @Override
    public void playSound(Sound sound, double x, double y, double z) {
        player.playSound(sound, x, y, z);
    }

    @Override
    public void stopSound(SoundStop stop) {
        player.stopSound(stop);
    }

    @Override
    public Optional<Skin> skin() {
        PlayerSkin skin = player.getSkin();
        return skin == null ? Optional.empty() : Optional.of(new Skin(skin.textures(), skin.signature()));
    }

    @Override
    public void setSkin(Skin skin) {
        player.setSkin(skin == null ? null : new PlayerSkin(skin.textures(), skin.signature()));
    }

    @Override
    public LoginMode loginMode() {
        return service.loginMode(player.getUuid());
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

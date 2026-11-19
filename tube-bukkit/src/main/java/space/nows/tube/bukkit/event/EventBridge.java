package space.nows.tube.bukkit.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.Subscription;
import space.nows.tube.api.event.block.BlockBreakEvent;
import space.nows.tube.api.event.block.BlockPlaceEvent;
import space.nows.tube.api.event.entity.EntityDamageEvent;
import space.nows.tube.api.event.entity.EntityDeathEvent;
import space.nows.tube.api.event.entity.EntityPickupItemEvent;
import space.nows.tube.api.event.entity.EntitySpawnEvent;
import space.nows.tube.api.event.entity.ExplosionEvent;
import space.nows.tube.api.event.inventory.InventoryClickEvent;
import space.nows.tube.api.event.inventory.InventoryCloseEvent;
import space.nows.tube.api.event.inventory.InventoryDragEvent;
import space.nows.tube.api.event.inventory.InventoryOpenEvent;
import space.nows.tube.api.event.player.PlayerChatEvent;
import space.nows.tube.api.event.player.PlayerDropItemEvent;
import space.nows.tube.api.event.player.PlayerGameModeChangeEvent;
import space.nows.tube.api.event.player.PlayerInteractEvent;
import space.nows.tube.api.event.player.PlayerJoinEvent;
import space.nows.tube.api.event.player.PlayerMoveEvent;
import space.nows.tube.api.event.player.PlayerPreLoginEvent;
import space.nows.tube.api.event.player.PlayerQuitEvent;
import space.nows.tube.api.event.player.PlayerRespawnEvent;
import space.nows.tube.api.event.player.PlayerTeleportEvent;
import space.nows.tube.api.event.server.CommandDispatchEvent;
import space.nows.tube.bukkit.BukkitRuntime;
import space.nows.tube.bukkit.inventory.BukkitInventory;
import space.nows.tube.bukkit.inventory.BukkitInventoryView;
import space.nows.tube.bukkit.inventory.ItemConverter;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EventBridge {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final BukkitRuntime runtime;

    public EventBridge(BukkitRuntime runtime) {
        this.runtime = runtime;
    }

    public void install(EventBus bus, List<Subscription> subscriptions) {
        subscriptions.add(bus.subscribe(space.nows.tube.api.event.player.PlayerSneakEvent.class, EventPriority.NORMAL, false,
                event -> runtime.pluginManager().callEvent(new org.bukkit.event.player.PlayerToggleSneakEvent(runtime.adapters().player(event.player()), event.sneaking()))));
        subscriptions.add(bus.subscribe(space.nows.tube.api.event.player.PlayerSprintEvent.class, EventPriority.NORMAL, false,
                event -> runtime.pluginManager().callEvent(new org.bukkit.event.player.PlayerToggleSprintEvent(runtime.adapters().player(event.player()), event.sprinting()))));
        subscriptions.add(bus.subscribe(space.nows.tube.api.event.entity.EntityGlideEvent.class, EventPriority.NORMAL, false,
                event -> runtime.pluginManager().callEvent(new org.bukkit.event.entity.EntityToggleGlideEvent((org.bukkit.entity.LivingEntity) runtime.adapters().entity(event.entity()), event.gliding()))));
        subscriptions.add(bus.subscribe(PlayerJoinEvent.class, EventPriority.NORMAL, false, this::join));
        subscriptions.add(bus.subscribe(PlayerQuitEvent.class, EventPriority.NORMAL, false, this::quit));
        subscriptions.add(bus.subscribe(PlayerChatEvent.class, EventPriority.NORMAL, false, this::chat));
        subscriptions.add(bus.subscribe(BlockBreakEvent.class, EventPriority.NORMAL, false, this::blockBreak));
        subscriptions.add(bus.subscribe(BlockPlaceEvent.class, EventPriority.NORMAL, false, this::blockPlace));
        subscriptions.add(bus.subscribe(InventoryClickEvent.class, EventPriority.NORMAL, false, this::inventoryClick));
        subscriptions.add(bus.subscribe(InventoryCloseEvent.class, EventPriority.NORMAL, false, this::inventoryClose));
        subscriptions.add(bus.subscribe(EntityDamageEvent.class, EventPriority.NORMAL, false, this::entityDamage));
        subscriptions.add(bus.subscribe(EntityDeathEvent.class, EventPriority.NORMAL, false, this::entityDeath));
        subscriptions.add(bus.subscribe(ExplosionEvent.class, EventPriority.NORMAL, false, this::explosion));
        subscriptions.add(bus.subscribe(EntitySpawnEvent.class, EventPriority.NORMAL, false, this::entitySpawn));
        subscriptions.add(bus.subscribe(PlayerTeleportEvent.class, EventPriority.NORMAL, false, this::teleport));
        subscriptions.add(bus.subscribe(PlayerMoveEvent.class, EventPriority.NORMAL, false, this::move));
        subscriptions.add(bus.subscribe(PlayerInteractEvent.class, EventPriority.NORMAL, false, this::interact));
        subscriptions.add(bus.subscribe(PlayerDropItemEvent.class, EventPriority.NORMAL, false, this::dropItem));
        subscriptions.add(bus.subscribe(EntityPickupItemEvent.class, EventPriority.NORMAL, false, this::pickupItem));
        subscriptions.add(bus.subscribe(PlayerRespawnEvent.class, EventPriority.NORMAL, false, this::respawn));
        subscriptions.add(bus.subscribe(PlayerPreLoginEvent.class, EventPriority.NORMAL, false, this::preLogin));
        subscriptions.add(bus.subscribe(CommandDispatchEvent.class, EventPriority.NORMAL, false, this::command));
        subscriptions.add(bus.subscribe(PlayerGameModeChangeEvent.class, EventPriority.NORMAL, false, this::gameModeChange));
        subscriptions.add(bus.subscribe(InventoryOpenEvent.class, EventPriority.NORMAL, false, this::inventoryOpen));
        subscriptions.add(bus.subscribe(InventoryDragEvent.class, EventPriority.NORMAL, false, this::inventoryDrag));
    }

    private void join(PlayerJoinEvent event) {
        var player = runtime.adapters().player(event.player());
        runtime.scoreboards().playerJoined(event.player());
        var address = event.player().address().map(java.net.InetSocketAddress::getAddress).orElse(null);
        var login = new org.bukkit.event.player.PlayerLoginEvent(player, "", address);
        runtime.pluginManager().callEvent(login);
        if (login.getResult() != org.bukkit.event.player.PlayerLoginEvent.Result.ALLOWED) {
            String message = login.getKickMessage();
            event.setJoinMessage(null);
            event.player().kick(message == null || message.isEmpty() ? Component.text("You are not allowed to join") : LEGACY.deserialize(message));
            return;
        }
        var bukkit = new org.bukkit.event.player.PlayerJoinEvent(player, text(event.joinMessage()));
        runtime.pluginManager().callEvent(bukkit);
        event.setJoinMessage(component(bukkit.getJoinMessage()));
    }

    private void quit(PlayerQuitEvent event) {
        var player = runtime.adapters().player(event.player());
        var bukkit = new org.bukkit.event.player.PlayerQuitEvent(player, text(event.quitMessage()));
        runtime.pluginManager().callEvent(bukkit);
        event.setQuitMessage(component(bukkit.getQuitMessage()));
        runtime.adapters().forget(event.player().uuid());
    }

    private void chat(PlayerChatEvent event) {
        var player = runtime.adapters().player(event.player());
        var recipients = new HashSet<>(runtime.adapters().onlinePlayers());
        var bukkit = new org.bukkit.event.player.AsyncPlayerChatEvent(false, player, event.message(), recipients);
        runtime.pluginManager().callEvent(bukkit);
        if (bukkit.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        event.setMessage(bukkit.getMessage());
    }

    private void blockBreak(BlockBreakEvent event) {
        var block = runtime.adapters().world(event.world()).getBlockAt(event.x(), event.y(), event.z());
        var bukkit = new org.bukkit.event.block.BlockBreakEvent(block, runtime.adapters().player(event.player()));
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
        event.setDropItems(bukkit.isDropItems());
    }

    private void blockPlace(BlockPlaceEvent event) {
        var player = runtime.adapters().player(event.player());
        var block = runtime.adapters().world(event.world()).getBlockAt(event.x(), event.y(), event.z());
        var bukkit = new org.bukkit.event.block.BlockPlaceEvent(block, player.getInventory().getItemInMainHand(), player);
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled() || !bukkit.canBuild());
    }

    private void inventoryClick(InventoryClickEvent event) {
        var view = view(event.player(), event.inventory());
        var click = org.bukkit.event.inventory.ClickType.valueOf(event.click().name());
        var slotType = event.slot() < 0 ? org.bukkit.event.inventory.InventoryType.SlotType.OUTSIDE : view.getSlotType(event.slot());
        var bukkit = new org.bukkit.event.inventory.InventoryClickEvent(view, slotType, event.slot(), click, action(click, event.clicked()), event.hotbarButton());
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private static org.bukkit.event.inventory.InventoryAction action(org.bukkit.event.inventory.ClickType click, space.nows.tube.api.inventory.ItemStack clicked) {
        boolean empty = clicked == null || clicked.isAir();
        return switch (click) {
            case LEFT -> empty ? org.bukkit.event.inventory.InventoryAction.PLACE_ALL : org.bukkit.event.inventory.InventoryAction.PICKUP_ALL;
            case RIGHT -> empty ? org.bukkit.event.inventory.InventoryAction.PLACE_ONE : org.bukkit.event.inventory.InventoryAction.PICKUP_HALF;
            case SHIFT_LEFT, SHIFT_RIGHT -> org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY;
            case NUMBER_KEY, SWAP_OFFHAND -> org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP;
            case DROP -> org.bukkit.event.inventory.InventoryAction.DROP_ONE_SLOT;
            case CONTROL_DROP -> org.bukkit.event.inventory.InventoryAction.DROP_ALL_SLOT;
            case DOUBLE_CLICK -> org.bukkit.event.inventory.InventoryAction.COLLECT_TO_CURSOR;
            case MIDDLE -> org.bukkit.event.inventory.InventoryAction.CLONE_STACK;
            case WINDOW_BORDER_LEFT -> org.bukkit.event.inventory.InventoryAction.DROP_ALL_CURSOR;
            case WINDOW_BORDER_RIGHT -> org.bukkit.event.inventory.InventoryAction.DROP_ONE_CURSOR;
            default -> org.bukkit.event.inventory.InventoryAction.UNKNOWN;
        };
    }

    private void inventoryDrag(InventoryDragEvent event) {
        var view = view(event.player(), event.inventory());
        Map<Integer, org.bukkit.inventory.ItemStack> slots = new LinkedHashMap<>();
        for (int slot : event.slots()) {
            slots.put(slot, view.getItem(slot));
        }
        var bukkit = new org.bukkit.event.inventory.InventoryDragEvent(view, null, view.getCursor(), event.isRightClick(), slots);
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void inventoryOpen(InventoryOpenEvent event) {
        var bukkit = new org.bukkit.event.inventory.InventoryOpenEvent(view(event.player(), event.inventory()));
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void inventoryClose(InventoryCloseEvent event) {
        runtime.pluginManager().callEvent(new org.bukkit.event.inventory.InventoryCloseEvent(view(event.player(), event.inventory())));
    }

    private void command(CommandDispatchEvent event) {
        String line = event.line().startsWith("/") ? event.line().substring(1) : event.line();
        if (event.sender() instanceof space.nows.tube.api.entity.player.Player tubePlayer) {
            var bukkit = new org.bukkit.event.player.PlayerCommandPreprocessEvent(runtime.adapters().player(tubePlayer), "/" + line);
            runtime.pluginManager().callEvent(bukkit);
            if (bukkit.isCancelled()) {
                event.setCancelled(true);
            } else if (!bukkit.getMessage().equals("/" + line)) {
                event.setLine(bukkit.getMessage().startsWith("/") ? bukkit.getMessage().substring(1) : bukkit.getMessage());
            }
            return;
        }
        var bukkit = new org.bukkit.event.server.ServerCommandEvent(runtime.adapters().sender(event.sender()), line);
        runtime.pluginManager().callEvent(bukkit);
        if (bukkit.isCancelled()) {
            event.setCancelled(true);
        } else if (!bukkit.getCommand().equals(line)) {
            event.setLine(bukkit.getCommand());
        }
    }

    private void gameModeChange(PlayerGameModeChangeEvent event) {
        var bukkit = new org.bukkit.event.player.PlayerGameModeChangeEvent(runtime.adapters().player(event.player()), org.bukkit.GameMode.valueOf(event.newGameMode().name()));
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void entityDamage(EntityDamageEvent event) {
        var damagee = runtime.adapters().living(event.entity());
        var cause = DamageCauses.fromKey(event.cause());
        var bukkit = event.attacker()
                .<org.bukkit.event.entity.EntityDamageEvent>map(attacker -> new org.bukkit.event.entity.EntityDamageByEntityEvent(
                        runtime.adapters().entity(attacker), damagee, cause, event.damage()))
                .orElseGet(() -> new org.bukkit.event.entity.EntityDamageEvent(damagee, cause, event.damage()));
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
        event.setDamage(bukkit.getDamage());
    }

    private void entityDeath(EntityDeathEvent event) {
        java.util.List<org.bukkit.inventory.ItemStack> drops = new java.util.ArrayList<>();
        for (var item : event.drops()) {
            drops.add(ItemConverter.fromTube(item));
        }
        org.bukkit.event.entity.EntityDeathEvent bukkit;
        if (event.entity() instanceof space.nows.tube.api.entity.player.Player tubePlayer) {
            var player = runtime.adapters().player(tubePlayer);
            bukkit = new org.bukkit.event.entity.PlayerDeathEvent(player, drops, event.experience(), player.getName() + " died");
        } else {
            bukkit = new org.bukkit.event.entity.EntityDeathEvent(runtime.adapters().living(event.entity()), drops, event.experience());
        }
        runtime.pluginManager().callEvent(bukkit);
        event.drops().clear();
        for (var item : bukkit.getDrops()) {
            if (item != null && !item.getType().isAir()) {
                event.drops().add(ItemConverter.toTube(item));
            }
        }
        event.setExperience(bukkit.getDroppedExp());
    }

    private void explosion(ExplosionEvent event) {
        if (event.source().isEmpty()) {
            return;
        }
        var world = runtime.adapters().location(event.location()).getWorld();
        java.util.List<org.bukkit.block.Block> blocks = new java.util.ArrayList<>();
        for (var position : event.blocks()) {
            blocks.add(world.getBlockAt(position.x(), position.y(), position.z()));
        }
        var bukkit = new org.bukkit.event.entity.EntityExplodeEvent(runtime.adapters().entity(event.source().get()),
                runtime.adapters().location(event.location()), blocks, 1f / Math.max(1f, event.power()), null);
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
        event.blocks().clear();
        for (var block : bukkit.blockList()) {
            event.blocks().add(new ExplosionEvent.BlockPosition(block.getX(), block.getY(), block.getZ()));
        }
    }

    private void entitySpawn(EntitySpawnEvent event) {
        var entity = runtime.adapters().entity(event.entity());
        org.bukkit.event.entity.EntitySpawnEvent bukkit = entity instanceof org.bukkit.entity.LivingEntity living
                ? new org.bukkit.event.entity.CreatureSpawnEvent(living, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.NATURAL)
                : new org.bukkit.event.entity.EntitySpawnEvent(entity);
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void move(PlayerMoveEvent event) {
        var player = runtime.adapters().player(event.player());
        var to = runtime.adapters().location(event.to());
        var bukkit = new org.bukkit.event.player.PlayerMoveEvent(player, runtime.adapters().location(event.from()), to);
        runtime.pluginManager().callEvent(bukkit);
        if (bukkit.isCancelled()) {
            event.setCancelled(true);
        } else if (bukkit.getTo() != to) {
            event.setTo(runtime.adapters().tubeLocation(bukkit.getTo()));
        }
    }

    private void teleport(PlayerTeleportEvent event) {
        var player = runtime.adapters().player(event.player());
        var to = runtime.adapters().location(event.to());
        var bukkit = new org.bukkit.event.player.PlayerTeleportEvent(player, runtime.adapters().location(event.from()), to, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
        runtime.pluginManager().callEvent(bukkit);
        if (bukkit.isCancelled()) {
            event.setCancelled(true);
        } else if (bukkit.getTo() != to) {
            event.setTo(runtime.adapters().tubeLocation(bukkit.getTo()));
        }
    }

    private void interact(PlayerInteractEvent event) {
        var player = runtime.adapters().player(event.player());
        var block = runtime.adapters().world(event.world()).getBlockAt(event.x(), event.y(), event.z());
        var hand = event.mainHand() ? org.bukkit.inventory.EquipmentSlot.HAND : org.bukkit.inventory.EquipmentSlot.OFF_HAND;
        var item = event.mainHand() ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
        var action = switch (event.action()) {
            case LEFT_CLICK_BLOCK -> org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
            case LEFT_CLICK_AIR -> org.bukkit.event.block.Action.LEFT_CLICK_AIR;
            case RIGHT_CLICK_BLOCK -> org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
            case RIGHT_CLICK_AIR -> org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
        };
        boolean air = action == org.bukkit.event.block.Action.LEFT_CLICK_AIR || action == org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
        var bukkit = new org.bukkit.event.player.PlayerInteractEvent(player, action, item, air ? null : block, air ? null : org.bukkit.block.BlockFace.SELF, hand);
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void dropItem(PlayerDropItemEvent event) {
        var bukkit = new org.bukkit.event.player.PlayerDropItemEvent(runtime.adapters().player(event.player()), runtime.adapters().item(event.item()));
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void pickupItem(EntityPickupItemEvent event) {
        var item = runtime.adapters().item(event.item());
        var entity = runtime.adapters().living(event.entity());
        boolean cancelled = false;
        if (entity instanceof org.bukkit.entity.Player player) {
            var legacy = new org.bukkit.event.player.PlayerPickupItemEvent(player, item, 0);
            runtime.pluginManager().callEvent(legacy);
            cancelled = legacy.isCancelled();
        }
        var bukkit = new org.bukkit.event.entity.EntityPickupItemEvent(entity, item, 0);
        bukkit.setCancelled(cancelled);
        runtime.pluginManager().callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void preLogin(PlayerPreLoginEvent event) {
        var address = event.address() instanceof java.net.InetSocketAddress inet ? inet.getAddress() : null;
        var bukkit = new org.bukkit.event.player.AsyncPlayerPreLoginEvent(event.name(), address, event.uuid());
        runtime.pluginManager().callEvent(bukkit);
        if (bukkit.getLoginResult() != org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            String message = bukkit.getKickMessage();
            event.deny(message == null || message.isEmpty() ? Component.text("You are not allowed to join") : LEGACY.deserialize(message));
        }
    }

    private void respawn(PlayerRespawnEvent event) {
        var location = runtime.adapters().location(event.location());
        var bukkit = new org.bukkit.event.player.PlayerRespawnEvent(runtime.adapters().player(event.player()), location, false);
        runtime.pluginManager().callEvent(bukkit);
        if (bukkit.getRespawnLocation() != location) {
            event.setLocation(runtime.adapters().tubeLocation(bukkit.getRespawnLocation()));
        }
    }

    private org.bukkit.inventory.InventoryView view(space.nows.tube.api.entity.player.Player owner, space.nows.tube.api.inventory.Inventory inventory) {
        var player = runtime.adapters().player(owner);
        org.bukkit.inventory.Inventory own = player.getInventory();
        if (inventory.equals(owner.inventory())) {
            return new BukkitInventoryView(owner, player, own, own, org.bukkit.event.inventory.InventoryType.PLAYER, "Inventory");
        }
        return new BukkitInventoryView(owner, player, new BukkitInventory(inventory), own, org.bukkit.event.inventory.InventoryType.CHEST, "Chest");
    }

    private static String text(Component component) {
        return component == null ? null : LEGACY.serialize(component);
    }

    private static Component component(String text) {
        return text == null || text.isEmpty() ? null : LEGACY.deserialize(text);
    }
}

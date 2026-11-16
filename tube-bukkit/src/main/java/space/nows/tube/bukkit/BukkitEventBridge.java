package space.nows.tube.bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import space.nows.tube.api.event.BlockBreakEvent;
import space.nows.tube.api.event.BlockPlaceEvent;
import space.nows.tube.api.event.EntityDamageEvent;
import space.nows.tube.api.event.EntityDeathEvent;
import space.nows.tube.api.event.PlayerInteractEvent;
import space.nows.tube.api.event.PlayerMoveEvent;
import space.nows.tube.api.event.PlayerTeleportEvent;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.EventPriority;
import space.nows.tube.api.event.InventoryClickEvent;
import space.nows.tube.api.event.InventoryCloseEvent;
import space.nows.tube.api.event.PlayerChatEvent;
import space.nows.tube.api.event.PlayerJoinEvent;
import space.nows.tube.api.event.PlayerQuitEvent;
import space.nows.tube.api.event.Subscription;

import java.util.HashSet;
import java.util.List;

final class BukkitEventBridge {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final BukkitRuntime runtime;

    BukkitEventBridge(BukkitRuntime runtime) {
        this.runtime = runtime;
    }

    void install(EventBus bus, List<Subscription> subscriptions) {
        subscriptions.add(bus.subscribe(PlayerJoinEvent.class, EventPriority.NORMAL, false, this::join));
        subscriptions.add(bus.subscribe(PlayerQuitEvent.class, EventPriority.NORMAL, false, this::quit));
        subscriptions.add(bus.subscribe(PlayerChatEvent.class, EventPriority.NORMAL, false, this::chat));
        subscriptions.add(bus.subscribe(BlockBreakEvent.class, EventPriority.NORMAL, false, this::blockBreak));
        subscriptions.add(bus.subscribe(BlockPlaceEvent.class, EventPriority.NORMAL, false, this::blockPlace));
        subscriptions.add(bus.subscribe(InventoryClickEvent.class, EventPriority.NORMAL, false, this::inventoryClick));
        subscriptions.add(bus.subscribe(InventoryCloseEvent.class, EventPriority.NORMAL, false, this::inventoryClose));
        subscriptions.add(bus.subscribe(EntityDamageEvent.class, EventPriority.NORMAL, false, this::entityDamage));
        subscriptions.add(bus.subscribe(EntityDeathEvent.class, EventPriority.NORMAL, false, this::entityDeath));
        subscriptions.add(bus.subscribe(PlayerTeleportEvent.class, EventPriority.NORMAL, false, this::teleport));
        subscriptions.add(bus.subscribe(PlayerMoveEvent.class, EventPriority.NORMAL, false, this::move));
        subscriptions.add(bus.subscribe(PlayerInteractEvent.class, EventPriority.NORMAL, false, this::interact));
    }

    private void join(PlayerJoinEvent event) {
        var player = runtime.wrappers.player(event.player());
        var bukkit = new org.bukkit.event.player.PlayerJoinEvent(player, text(event.joinMessage()));
        runtime.pluginManager.callEvent(bukkit);
        event.setJoinMessage(component(bukkit.getJoinMessage()));
    }

    private void quit(PlayerQuitEvent event) {
        var player = runtime.wrappers.player(event.player());
        var bukkit = new org.bukkit.event.player.PlayerQuitEvent(player, text(event.quitMessage()));
        runtime.pluginManager.callEvent(bukkit);
        event.setQuitMessage(component(bukkit.getQuitMessage()));
        runtime.wrappers.forget(event.player().uuid());
    }

    private void chat(PlayerChatEvent event) {
        var player = runtime.wrappers.player(event.player());
        var recipients = new HashSet<>(runtime.wrappers.onlinePlayers());
        var bukkit = new org.bukkit.event.player.AsyncPlayerChatEvent(false, player, event.message(), recipients);
        runtime.pluginManager.callEvent(bukkit);
        if (bukkit.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        event.setMessage(bukkit.getMessage());
    }

    private void blockBreak(BlockBreakEvent event) {
        var block = runtime.wrappers.world(event.world()).getBlockAt(event.x(), event.y(), event.z());
        var bukkit = new org.bukkit.event.block.BlockBreakEvent(block, runtime.wrappers.player(event.player()));
        runtime.pluginManager.callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void blockPlace(BlockPlaceEvent event) {
        var player = runtime.wrappers.player(event.player());
        var block = runtime.wrappers.world(event.world()).getBlockAt(event.x(), event.y(), event.z());
        var bukkit = new org.bukkit.event.block.BlockPlaceEvent(block, player.getInventory().getItemInMainHand(), player);
        runtime.pluginManager.callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled() || !bukkit.canBuild());
    }

    private void inventoryClick(InventoryClickEvent event) {
        var player = runtime.wrappers.player(event.player());
        var bukkit = new org.bukkit.event.inventory.InventoryClickEvent(player, inventory(event.player(), event.inventory()), event.slot(), Items.fromTube(event.clicked()));
        runtime.pluginManager.callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void inventoryClose(InventoryCloseEvent event) {
        var player = runtime.wrappers.player(event.player());
        runtime.pluginManager.callEvent(new org.bukkit.event.inventory.InventoryCloseEvent(player, inventory(event.player(), event.inventory())));
    }

    private void entityDamage(EntityDamageEvent event) {
        var bukkit = new org.bukkit.event.entity.EntityDamageEvent(runtime.wrappers.living(event.entity()), cause(event.cause()), event.damage());
        runtime.pluginManager.callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private void entityDeath(EntityDeathEvent event) {
        if (event.entity() instanceof space.nows.tube.api.Player tubePlayer) {
            var player = runtime.wrappers.player(tubePlayer);
            runtime.pluginManager.callEvent(new org.bukkit.event.entity.PlayerDeathEvent(player, player.getName() + " died"));
        } else {
            runtime.pluginManager.callEvent(new org.bukkit.event.entity.EntityDeathEvent(runtime.wrappers.living(event.entity())));
        }
    }

    private void move(PlayerMoveEvent event) {
        var player = runtime.wrappers.player(event.player());
        var to = runtime.wrappers.location(event.to());
        var bukkit = new org.bukkit.event.player.PlayerMoveEvent(player, runtime.wrappers.location(event.from()), to);
        runtime.pluginManager.callEvent(bukkit);
        if (bukkit.isCancelled()) {
            event.setCancelled(true);
        } else if (bukkit.getTo() != to) {
            event.setTo(runtime.wrappers.tubeLocation(bukkit.getTo()));
        }
    }

    private void teleport(PlayerTeleportEvent event) {
        var player = runtime.wrappers.player(event.player());
        var to = runtime.wrappers.location(event.to());
        var bukkit = new org.bukkit.event.player.PlayerTeleportEvent(player, runtime.wrappers.location(event.from()), to, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
        runtime.pluginManager.callEvent(bukkit);
        if (bukkit.isCancelled()) {
            event.setCancelled(true);
        } else if (bukkit.getTo() != to) {
            event.setTo(runtime.wrappers.tubeLocation(bukkit.getTo()));
        }
    }

    static org.bukkit.event.entity.EntityDamageEvent.DamageCause cause(String key) {
        String name = key == null ? "" : key.replaceFirst("^minecraft:", "");
        return switch (name) {
            case "player_attack", "mob_attack", "mob_attack_no_aggro" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_ATTACK;
            case "arrow", "trident", "thrown", "fireball", "unattributed_fireball", "wither_skull", "mob_projectile" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.PROJECTILE;
            case "in_wall" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.SUFFOCATION;
            case "fall", "ender_pearl" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL;
            case "in_fire", "campfire" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE;
            case "on_fire" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE_TICK;
            case "lava" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.LAVA;
            case "drown" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.DROWNING;
            case "explosion", "player_explosion" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;
            case "out_of_world" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.VOID;
            case "lightning_bolt" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.LIGHTNING;
            case "generic_kill" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.SUICIDE;
            case "starve" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.STARVATION;
            case "magic", "indirect_magic" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.MAGIC;
            case "wither" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.WITHER;
            case "falling_block", "falling_anvil", "falling_stalactite" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALLING_BLOCK;
            case "thorns" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.THORNS;
            case "dragon_breath" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.DRAGON_BREATH;
            case "fly_into_wall" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.FLY_INTO_WALL;
            case "hot_floor" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.HOT_FLOOR;
            case "cactus", "sweet_berry_bush", "stalagmite" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.CONTACT;
            case "cramming" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.CRAMMING;
            case "dry_out" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.DRYOUT;
            case "freeze" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.FREEZE;
            case "sonic_boom" -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.SONIC_BOOM;
            default -> org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM;
        };
    }

    private void interact(PlayerInteractEvent event) {
        var player = runtime.wrappers.player(event.player());
        var block = runtime.wrappers.world(event.world()).getBlockAt(event.x(), event.y(), event.z());
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
        runtime.pluginManager.callEvent(bukkit);
        event.setCancelled(bukkit.isCancelled());
    }

    private org.bukkit.inventory.Inventory inventory(space.nows.tube.api.Player owner, space.nows.tube.api.Inventory inventory) {
        if (inventory.equals(owner.inventory())) {
            return (org.bukkit.inventory.Inventory) runtime.wrappers.player(owner).getInventory();
        }
        return new BukkitInventory(inventory);
    }

    private static String text(Component component) {
        return component == null ? null : LEGACY.serialize(component);
    }

    private static Component component(String text) {
        return text == null || text.isEmpty() ? null : LEGACY.deserialize(text);
    }
}

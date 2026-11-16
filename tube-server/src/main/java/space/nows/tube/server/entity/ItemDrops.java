package space.nows.tube.server.entity;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import space.nows.tube.api.entity.LivingEntity;
import space.nows.tube.api.event.EntityPickupItemEvent;
import space.nows.tube.api.event.EventBus;
import space.nows.tube.api.event.PlayerDropItemEvent;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.WorldService;

import java.time.Duration;

public final class ItemDrops {

    private static final Duration PICKUP_DELAY = Duration.ofSeconds(2);
    private static final double THROW_SPEED = 6;
    private static final double THROW_LIFT = 2;

    private final EventBus events;
    private final PlayerService players;
    private final WorldService worlds;

    public ItemDrops(EventBus events, PlayerService players, WorldService worlds) {
        this.events = events;
        this.players = players;
        this.worlds = worlds;
    }

    public void install(GlobalEventHandler handler) {
        handler.addListener(PlayerSpawnEvent.class, event -> event.getPlayer().setCanPickupItem(true));
        handler.addListener(ItemDropEvent.class, this::drop);
        handler.addListener(PickupItemEvent.class, this::pickup);
    }

    private void drop(ItemDropEvent event) {
        Player player = event.getPlayer();
        ItemEntity entity = new ItemEntity(event.getItemStack());
        entity.setPickupDelay(PICKUP_DELAY);
        var eyes = player.getPosition().add(0, player.getEyeHeight() - 0.3, 0);
        entity.setVelocity(eyes.direction().mul(THROW_SPEED).add(new Vec(0, THROW_LIFT, 0)));
        entity.setInstance(player.getInstance(), eyes);
        var posted = events.post(new PlayerDropItemEvent(players.wrap(player), new MinestomItemEntity(entity, worlds)));
        if (posted.isCancelled()) {
            entity.remove();
            event.setCancelled(true);
        }
    }

    private void pickup(PickupItemEvent event) {
        var picker = (LivingEntity) worlds.wrap(event.getLivingEntity());
        var posted = events.post(new EntityPickupItemEvent(picker, new MinestomItemEntity(event.getItemEntity(), worlds)));
        if (posted.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        if (event.getLivingEntity() instanceof Player player && !player.getInventory().addItemStack(event.getItemStack())) {
            event.setCancelled(true);
        }
    }
}

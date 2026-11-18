package space.nows.tube.server.gameplay;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.projectile.ArrowMeta;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import space.nows.tube.server.gameplay.loot.LootTables;
import space.nows.tube.server.mob.KillCredit;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Archery {

    public static final Tag<Double> ARROW_DAMAGE = Tag.Transient("tube:arrow_damage");
    private static final Tag<Boolean> PICKUP = Tag.Transient("tube:arrow_pickup");
    private static final Tag<Long> STUCK_AT = Tag.Transient("tube:arrow_stuck");
    private static final Set<EntityProjectile> STUCK = ConcurrentHashMap.newKeySet();

    private Archery() {
    }

    public static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerCancelItemUseEvent.class, event -> {
            if (event.getItemStack().material() == Material.BOW) {
                shoot(event.getPlayer(), event.getItemStack(), event.getUseDuration());
            }
        });
        handler.addListener(ProjectileCollideWithEntityEvent.class, event -> {
            if (!(event.getEntity() instanceof EntityProjectile arrow) || arrow.getEntityType() != EntityType.ARROW) {
                return;
            }
            Entity shooter = arrow.getShooter();
            if (event.getTarget() == shooter && arrow.getAliveTicks() < 5 || !(event.getTarget() instanceof LivingEntity target)) {
                event.setCancelled(true);
                return;
            }
            double damage = arrow.hasTag(ARROW_DAMAGE) ? arrow.getTag(ARROW_DAMAGE) : 2;
            target.damage(new Damage(DamageType.ARROW, arrow, shooter, event.getCollisionPosition(), (float) damage));
            if (shooter instanceof Player player && !(target instanceof Player)) {
                KillCredit.hitBy(target, player);
            }
            var velocity = arrow.getVelocity();
            double length = Math.hypot(velocity.x(), velocity.z());
            if (length > 0.01) {
                target.takeKnockback(0.4f, -velocity.x() / length, -velocity.z() / length);
            }
            arrow.remove();
        });
        handler.addListener(ProjectileCollideWithBlockEvent.class, event -> {
            if (event.getEntity() instanceof EntityProjectile arrow && arrow.getEntityType() == EntityType.ARROW) {
                arrow.setTag(STUCK_AT, arrow.getAliveTicks());
                STUCK.add(arrow);
            }
        });
        MinecraftServer.getSchedulerManager().buildTask(Archery::collect).repeat(TaskSchedule.tick(4)).schedule();
    }

    private static void shoot(Player player, ItemStack bow, long useTicks) {
        boolean creative = player.getGameMode() == GameMode.CREATIVE;
        boolean infinity = LootTables.level(bow, "minecraft:infinity") > 0;
        int arrowSlot = findArrow(player);
        if (arrowSlot < 0 && !creative) {
            return;
        }
        double pull = useTicks / 20.0;
        pull = Math.min(1, (pull * pull + pull * 2) / 3);
        if (pull < 0.1) {
            return;
        }
        EntityProjectile arrow = new SweptArrow(player);
        ArrowMeta meta = (ArrowMeta) arrow.getEntityMeta();
        boolean critical = pull >= 1;
        meta.setCritical(critical);
        double damage = 2 * 3 * pull + (critical ? Math.random() * 3 : 0);
        int power = LootTables.level(bow, "minecraft:power");
        if (power > 0) {
            damage += power * 0.5 + 0.5;
        }
        arrow.setTag(ARROW_DAMAGE, damage);
        if (LootTables.level(bow, "minecraft:flame") > 0) {
            meta.setOnFire(true);
        }
        double speed = 3 * pull;
        boolean consume = !creative && !infinity;
        arrow.setTag(PICKUP, consume);
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        arrow.setInstance(player.getInstance(), eye).thenRun(() -> arrow.shoot(eye.add(eye.direction().mul(10)), speed, 1));
        if (consume && arrowSlot >= 0) {
            ItemStack arrows = player.getInventory().getItemStack(arrowSlot);
            player.getInventory().setItemStack(arrowSlot, arrows.consume(1));
        }
        if (!creative) {
            ItemStack worn = Durability.damaged(bow, 1);
            player.setItemInMainHand(worn);
        }
    }

    private static int findArrow(Player player) {
        if (player.getItemInOffHand().material() == Material.ARROW) {
            return net.minestom.server.utils.inventory.PlayerInventoryUtils.OFFHAND_SLOT;
        }
        for (int slot = 0; slot < 36; slot++) {
            if (player.getInventory().getItemStack(slot).material() == Material.ARROW) {
                return slot;
            }
        }
        return -1;
    }

    private static void collect() {
        for (EntityProjectile arrow : STUCK) {
            if (arrow.isRemoved()) {
                STUCK.remove(arrow);
                continue;
            }
            if (arrow.getAliveTicks() - arrow.getTag(STUCK_AT) > 1200) {
                arrow.remove();
                STUCK.remove(arrow);
                continue;
            }
            if (arrow.getInstance() == null) {
                continue;
            }
            for (Player player : arrow.getInstance().getPlayers()) {
                if (player.getPosition().distanceSquared(arrow.getPosition()) < 2.25 && player.getGameMode() != GameMode.SPECTATOR) {
                    if (Boolean.TRUE.equals(arrow.getTag(PICKUP)) && !player.getInventory().addItemStack(ItemStack.of(Material.ARROW))) {
                        continue;
                    }
                    arrow.remove();
                    STUCK.remove(arrow);
                    break;
                }
            }
        }
    }
}

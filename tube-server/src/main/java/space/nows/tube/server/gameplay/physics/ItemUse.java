package space.nows.tube.server.gameplay.physics;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import space.nows.tube.server.gameplay.Durability;
import space.nows.tube.server.world.effect.Pitch;
import space.nows.tube.server.world.effect.SoundPlayer;

import java.util.concurrent.ThreadLocalRandom;

public final class ItemUse {

    private final NetherPortals portals;

    public ItemUse(NetherPortals portals) {
        this.portals = portals;
    }

    public void install(GlobalEventHandler handler) {
        handler.addListener(PlayerBlockInteractEvent.class, event -> {
            if (event.isCancelled() || event.isBlockingItemUse()) {
                return;
            }
            if (useOnBlock(event.getPlayer(), event.getHand(), event.getInstance(), event.getBlockPosition(), event.getBlockFace(), event.getBlock())) {
                event.setBlockingItemUse(true);
            }
        });
        handler.addListener(PlayerUseItemEvent.class, event -> {
            Material material = event.getItemStack().material();
            if (material == Material.BUCKET || material == Material.WATER_BUCKET || material == Material.LAVA_BUCKET) {
                useBucketInAir(event.getPlayer(), event.getHand());
            }
        });
    }

    private boolean useOnBlock(Player player, PlayerHand hand, Instance instance, BlockVec position, BlockFace face, Block block) {
        ItemStack item = player.getItemInHand(hand);
        String tool = item.material().key().value();
        String name = block.key().value();
        boolean airAbove = instance.getBlock(position.relative(BlockFace.TOP)).air();
        if (tool.endsWith("_hoe") && airAbove && face != BlockFace.BOTTOM) {
            Block tilled = switch (name) {
                case "grass_block", "dirt", "dirt_path" -> Block.FARMLAND;
                case "coarse_dirt", "rooted_dirt" -> Block.DIRT;
                default -> null;
            };
            if (tilled != null) {
                instance.setBlock(position, tilled);
                sound(instance, position, SoundEvent.ITEM_HOE_TILL);
                wear(player, hand);
                return true;
            }
        }
        if (tool.endsWith("_shovel") && airAbove && face != BlockFace.BOTTOM
                && (name.equals("grass_block") || name.equals("dirt") || name.equals("podzol") || name.equals("mycelium")
                || name.equals("coarse_dirt") || name.equals("rooted_dirt"))) {
            instance.setBlock(position, Block.DIRT_PATH);
            sound(instance, position, SoundEvent.ITEM_SHOVEL_FLATTEN);
            wear(player, hand);
            return true;
        }
        if (tool.endsWith("_axe") && !name.startsWith("stripped_") && (name.endsWith("_log") || name.endsWith("_wood")
                || name.endsWith("_stem") && !name.contains("melon") && !name.contains("pumpkin") || name.endsWith("_hyphae") || name.equals("bamboo_block"))) {
            Block stripped = Block.fromKey("minecraft:stripped_" + name);
            if (stripped != null) {
                instance.setBlock(position, stripped.withProperties(block.properties()));
                sound(instance, position, SoundEvent.ITEM_AXE_STRIP);
                wear(player, hand);
                return true;
            }
        }
        if (item.material() == Material.FLINT_AND_STEEL || item.material() == Material.FIRE_CHARGE) {
            BlockVec target = position.relative(face);
            if (portals.tryLight(instance, target)) {
                sound(instance, position, SoundEvent.ITEM_FLINTANDSTEEL_USE);
                wearOrConsume(player, hand, item);
                return true;
            }
            if (name.equals("tnt")) {
                instance.setBlock(position, Block.AIR);
                Tnt.prime(instance, position, player);
                wearOrConsume(player, hand, item);
                return true;
            }
            if (instance.getBlock(target).air() && instance.getBlock(target.relative(BlockFace.BOTTOM)).solid()) {
                instance.setBlock(target, Block.FIRE);
                sound(instance, position, SoundEvent.ITEM_FLINTANDSTEEL_USE);
                wearOrConsume(player, hand, item);
                return true;
            }
        }
        if (item.material() == Material.BONE_MEAL && Growth.boneMeal(instance, position, block)) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setItemInHand(hand, item.consume(1));
            }
            return true;
        }
        return false;
    }

    private void useBucketInAir(Player player, PlayerHand hand) {
        Instance instance = player.getInstance();
        ItemStack item = player.getItemInHand(hand);
        Pos eye = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vec direction = eye.direction();
        BlockVec previous = null;
        for (double distance = 0; distance < 5; distance += 0.1) {
            BlockVec at = eye.add(direction.mul(distance)).asBlockVec();
            if (at.equals(previous)) {
                continue;
            }
            Block block = instance.getBlock(at);
            if (item.material() == Material.BUCKET && block.liquid() && "0".equals(block.getProperty("level"))) {
                instance.setBlock(at, Block.AIR);
                fill(player, hand, item, Fluids.isWater(block) ? Material.WATER_BUCKET : Material.LAVA_BUCKET);
                sound(instance, at, Fluids.isWater(block) ? SoundEvent.ITEM_BUCKET_FILL : SoundEvent.ITEM_BUCKET_FILL_LAVA);
                return;
            }
            if (item.material() == Material.BUCKET && "true".equals(block.getProperty("waterlogged"))) {
                instance.setBlock(at, block.withProperty("waterlogged", "false"));
                fill(player, hand, item, Material.WATER_BUCKET);
                sound(instance, at, SoundEvent.ITEM_BUCKET_FILL);
                return;
            }
            if (!block.air() && !block.liquid()) {
                if (item.material() != Material.BUCKET) {
                    BlockVec target = block.properties().containsKey("waterlogged") && item.material() == Material.WATER_BUCKET
                            || block.registry().isReplaceable() || previous == null ? at : previous;
                    empty(player, hand, instance, target);
                }
                return;
            }
            previous = at;
        }
    }

    private boolean empty(Player player, PlayerHand hand, Instance instance, BlockVec target) {
        ItemStack item = player.getItemInHand(hand);
        boolean water = item.material() == Material.WATER_BUCKET;
        Block current = instance.getBlock(target);
        if (water && instance.getCachedDimensionType().hasCeiling()) {
            sound(instance, target, SoundEvent.BLOCK_FIRE_EXTINGUISH);
        } else if (water && current.properties().containsKey("waterlogged")) {
            instance.setBlock(target, current.withProperty("waterlogged", "true"));
        } else if (current.air() || current.liquid() || current.registry().isReplaceable()) {
            if (!current.air() && !current.liquid()) {
                space.nows.tube.server.gameplay.block.BlockDropper.drop(instance, target, current);
            }
            instance.setBlock(target, water ? Block.WATER : Block.LAVA);
            sound(instance, target, water ? SoundEvent.ITEM_BUCKET_EMPTY : SoundEvent.ITEM_BUCKET_EMPTY_LAVA);
        } else {
            return false;
        }
        if (player.getGameMode() != GameMode.CREATIVE) {
            player.setItemInHand(hand, ItemStack.of(Material.BUCKET));
        }
        return true;
    }

    private static void fill(Player player, PlayerHand hand, ItemStack bucket, Material filled) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            player.getInventory().addItemStack(ItemStack.of(filled));
            return;
        }
        if (bucket.amount() == 1) {
            player.setItemInHand(hand, ItemStack.of(filled));
        } else {
            player.setItemInHand(hand, bucket.consume(1));
            if (!player.getInventory().addItemStack(ItemStack.of(filled))) {
                player.dropItem(ItemStack.of(filled));
            }
        }
    }

    private static void wear(Player player, PlayerHand hand) {
        Durability.damage(player, hand == PlayerHand.MAIN ? EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, 1);
    }

    private static void wearOrConsume(Player player, PlayerHand hand, ItemStack item) {
        if (item.material() == Material.FIRE_CHARGE) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setItemInHand(hand, item.consume(1));
            }
        } else {
            wear(player, hand);
        }
    }

    static void sound(Instance instance, Point position, SoundEvent sound) {
        SoundPlayer.play(instance, sound, Sound.Source.BLOCK, position.add(0.5, 0.5, 0.5), 1f,
                Pitch.between(ThreadLocalRandom.current(), 0.9f, 1.1f));
    }
}

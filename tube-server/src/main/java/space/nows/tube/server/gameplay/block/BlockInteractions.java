package space.nows.tube.server.gameplay.block;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.TaskSchedule;
import space.nows.tube.server.world.effect.Pitch;
import space.nows.tube.server.world.effect.SoundPlayer;

import java.util.concurrent.ThreadLocalRandom;

public final class BlockInteractions {

    private BlockInteractions() {
    }

    public static void registerRules() {
        registerRules(MinecraftServer.getBlockManager());
    }

    public static void registerRules(net.minestom.server.instance.block.BlockManager manager) {
        for (Block block : Block.values()) {
            if (PlacementRule.applies(block)) {
                manager.registerBlockPlacementRule(new PlacementRule(block));
            }
        }
    }

    public static void install(GlobalEventHandler handler) {
        registerRules();
        Signs.install(handler);
        PickBlock.install(handler);
        handler.addListener(PlayerBlockPlaceEvent.class, BlockInteractions::placeMultiBlock);
        handler.addListener(PlayerBlockBreakEvent.class, event -> {
            if (event.isCancelled()) {
                return;
            }
            Block block = event.getBlock();
            if ("upper".equals(block.getProperty("half"))) {
                Point below = event.getBlockPosition().add(0, -1, 0);
                if (event.getInstance().getBlock(below).compare(block)) {
                    event.getInstance().setBlock(below, Block.AIR);
                }
            }
        });
        handler.addListener(PlayerBlockInteractEvent.class, BlockInteractions::interact);
    }

    private static void placeMultiBlock(PlayerBlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        String name = block.key().value();
        Instance instance = event.getInstance();
        Point position = event.getBlockPosition();
        Player player = event.getPlayer();
        Facing look = Facing.horizontal(player.getPosition().yaw());
        Block existing = instance.getBlock(position);
        if (name.endsWith("_slab") && existing.compare(block) && !"double".equals(existing.getProperty("type"))) {
            event.setBlock(existing.withProperty("type", "double").withProperty("waterlogged", "false"));
            event.setDoBlockUpdates(false);
            return;
        }
        if ("lower".equals(block.getProperty("half"))) {
            Point above = position.add(0, 1, 0);
            if (!instance.getBlock(above).registry().isReplaceable() || above.y() >= instance.getCachedDimensionType().maxY()) {
                event.setCancelled(true);
                return;
            }
            if (name.endsWith("_door")) {
                block = block.withProperties(java.util.Map.of("facing", look.id, "hinge", hinge(look, event.getCursorPosition(), position)));
            }
            event.setBlock(block);
            instance.setBlock(above, block.withProperty("half", "upper"));
            return;
        }
        if (name.endsWith("_bed")) {
            Point head = look.offset(position);
            if (!instance.getBlock(head).registry().isReplaceable()) {
                event.setCancelled(true);
                return;
            }
            Block foot = block.withProperty("facing", look.id).withProperty("part", "foot");
            event.setBlock(foot);
            instance.setBlock(head, foot.withProperty("part", "head"));
        }
    }

    private static String hinge(Facing facing, Point cursor, Point position) {
        double x = cursor.x() - Math.floor(cursor.x());
        double z = cursor.z() - Math.floor(cursor.z());
        boolean left = (facing.x >= 0 || z >= 0.5) && (facing.x <= 0 || z <= 0.5) && (facing.z >= 0 || x <= 0.5) && (facing.z <= 0 || x >= 0.5);
        return left ? "left" : "right";
    }

    private static void interact(PlayerBlockInteractEvent event) {
        Player player = event.getPlayer();
        if (event.isCancelled() || player.isSneaking() && !player.getItemInMainHand().isAir()) {
            return;
        }
        Block block = event.getBlock();
        String name = block.key().value();
        Instance instance = event.getInstance();
        Point position = event.getBlockPosition();
        boolean iron = name.startsWith("iron_");
        if (name.endsWith("_door") && !iron) {
            boolean open = !"true".equals(block.getProperty("open"));
            Point other = "upper".equals(block.getProperty("half")) ? position.add(0, -1, 0) : position.add(0, 1, 0);
            instance.setBlock(position, block.withProperty("open", Boolean.toString(open)), false);
            Block partner = instance.getBlock(other);
            if (partner.compare(block)) {
                instance.setBlock(other, partner.withProperty("open", Boolean.toString(open)), false);
            }
            sound(instance, player, position, open ? SoundEvent.BLOCK_WOODEN_DOOR_OPEN : SoundEvent.BLOCK_WOODEN_DOOR_CLOSE);
            event.setBlockingItemUse(true);
        } else if (name.endsWith("_trapdoor") && !iron) {
            boolean open = !"true".equals(block.getProperty("open"));
            instance.setBlock(position, block.withProperty("open", Boolean.toString(open)), false);
            sound(instance, player, position, open ? SoundEvent.BLOCK_WOODEN_TRAPDOOR_OPEN : SoundEvent.BLOCK_WOODEN_TRAPDOOR_CLOSE);
            event.setBlockingItemUse(true);
        } else if (name.endsWith("_fence_gate")) {
            boolean open = !"true".equals(block.getProperty("open"));
            Block updated = block.withProperty("open", Boolean.toString(open));
            Facing look = Facing.horizontal(player.getPosition().yaw());
            if (open && Facing.of(block.getProperty("facing")) == look.opposite()) {
                updated = updated.withProperty("facing", look.id);
            }
            instance.setBlock(position, updated, false);
            sound(instance, player, position, open ? SoundEvent.BLOCK_FENCE_GATE_OPEN : SoundEvent.BLOCK_FENCE_GATE_CLOSE);
            event.setBlockingItemUse(true);
        } else if (name.equals("lever")) {
            boolean powered = !"true".equals(block.getProperty("powered"));
            instance.setBlock(position, block.withProperty("powered", Boolean.toString(powered)), false);
            sound(instance, null, position, SoundEvent.BLOCK_LEVER_CLICK);
            event.setBlockingItemUse(true);
        } else if (name.endsWith("_button") && !"true".equals(block.getProperty("powered"))) {
            instance.setBlock(position, block.withProperty("powered", "true"), false);
            boolean wooden = !name.contains("stone") && !name.contains("blackstone");
            sound(instance, null, position, wooden ? SoundEvent.BLOCK_WOODEN_BUTTON_CLICK_ON : SoundEvent.BLOCK_STONE_BUTTON_CLICK_ON);
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                Block current = instance.getBlock(position);
                if (current.compare(block)) {
                    instance.setBlock(position, current.withProperty("powered", "false"), false);
                    sound(instance, null, position, wooden ? SoundEvent.BLOCK_WOODEN_BUTTON_CLICK_OFF : SoundEvent.BLOCK_STONE_BUTTON_CLICK_OFF);
                }
            }).delay(TaskSchedule.tick(wooden ? 30 : 20)).schedule();
            event.setBlockingItemUse(true);
        }
    }

    static void sound(Instance instance, Player except, Point position, SoundEvent sound) {
        Sound effect = Sound.sound(sound, Sound.Source.BLOCK, 1f, Pitch.between(ThreadLocalRandom.current(), 0.9f, 1f));
        SoundPlayer.playExcept(instance, except, effect, position.add(0.5, 0.5, 0.5));
    }

    static boolean isFace(BlockFace face) {
        return face != null;
    }
}

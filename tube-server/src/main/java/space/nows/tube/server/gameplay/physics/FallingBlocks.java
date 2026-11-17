package space.nows.tube.server.gameplay.physics;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.instance.InstanceBlockUpdateEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import space.nows.tube.server.gameplay.block.BlockDropper;

public final class FallingBlocks {

    public void install(GlobalEventHandler handler) {
        handler.addListener(InstanceBlockUpdateEvent.class, event -> {
            Instance instance = event.getInstance();
            BlockVec position = event.getBlockPosition();
            check(instance, position);
            check(instance, position.relative(BlockFace.TOP));
        });
    }

    public static boolean hasGravity(Block block) {
        String name = block.key().value();
        return name.equals("sand") || name.equals("red_sand") || name.equals("gravel") || name.endsWith("concrete_powder")
                || name.endsWith("anvil") || name.equals("dragon_egg") || name.equals("suspicious_sand") || name.equals("suspicious_gravel");
    }

    static boolean canFallInto(Block block) {
        return block.air() || block.liquid() || block.compare(Block.FIRE) || block.registry().isReplaceable();
    }

    private static void check(Instance instance, BlockVec position) {
        Block block = instance.getBlock(position, Block.Getter.Condition.TYPE);
        if (block == null || !hasGravity(block)) {
            return;
        }
        instance.scheduler().buildTask(() -> {
            Block current = instance.getBlock(position);
            if (!hasGravity(current) || position.y() <= instance.getCachedDimensionType().minY()
                    || !canFallInto(instance.getBlock(position.relative(BlockFace.BOTTOM)))) {
                return;
            }
            instance.setBlock(position, Block.AIR);
            new Falling(current).setInstance(instance, new Pos(position.x() + 0.5, position.y(), position.z() + 0.5));
        }).delay(TaskSchedule.tick(2)).schedule();
    }

    private static final class Falling extends Entity {

        private final Block block;

        Falling(Block block) {
            super(EntityType.FALLING_BLOCK);
            this.block = block;
            ((FallingBlockMeta) getEntityMeta()).setBlock(block);
        }

        @Override
        public void update(long time) {
            super.update(time);
            if (getAliveTicks() > 600) {
                remove();
                return;
            }
            if (!isOnGround() || getAliveTicks() < 2) {
                return;
            }
            Instance instance = getInstance();
            BlockVec at = getPosition().asBlockVec();
            Block there = instance.getBlock(at);
            if (canFallInto(there) && !there.liquid() || there.compare(Block.WATER)) {
                if (!there.air() && !there.liquid()) {
                    BlockDropper.drop(instance, at, there);
                }
                instance.setBlock(at, block);
            } else {
                BlockDropper.spawn(instance, getPosition(), ItemStack.of(block.registry().material()));
            }
            remove();
        }
    }
}

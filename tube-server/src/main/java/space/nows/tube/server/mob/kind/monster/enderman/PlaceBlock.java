package space.nows.tube.server.mob.kind.monster.enderman;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.metadata.monster.EndermanMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import space.nows.tube.api.entity.mob.brain.Behavior;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.world.rule.RuleBook;

final class PlaceBlock extends Behavior {

    private static final int CHANCE = 2000;

    private final Creature enderman;

    PlaceBlock(Creature enderman) {
        this.enderman = enderman;
    }

    @Override
    public boolean canStart() {
        return enderman.getEntityMeta() instanceof EndermanMeta meta && meta.getCarriedBlock() != null
                && RuleBook.enabled(enderman.getInstance(), GameRule.MOB_GRIEFING) && enderman.random().nextInt(halved(CHANCE)) == 0;
    }

    @Override
    public void tick() {
        EndermanMeta meta = (EndermanMeta) enderman.getEntityMeta();
        Block carried = meta.getCarriedBlock();
        if (carried == null) {
            return;
        }
        Instance instance = enderman.getInstance();
        Pos at = enderman.getPosition();
        int x = (int) Math.floor(at.x() - 1 + enderman.random().nextDouble() * 2);
        int y = (int) Math.floor(at.y() + enderman.random().nextDouble() * 2);
        int z = (int) Math.floor(at.z() - 1 + enderman.random().nextDouble() * 2);
        Block target = instance.getBlock(x, y, z);
        Block below = instance.getBlock(x, y - 1, z);
        if (target.air() && !below.air() && !below.compare(Block.BEDROCK) && below.collisionShape().isFaceFull(BlockFace.TOP)
                && empty(instance, x, y, z)) {
            instance.setBlock(x, y, z, carried);
            meta.setCarriedBlock(null);
        }
    }

    private boolean empty(Instance instance, int x, int y, int z) {
        for (Entity entity : instance.getNearbyEntities(new Pos(x + 0.5, y + 0.5, z + 0.5), 2)) {
            if (entity == enderman) {
                continue;
            }
            BoundingBox box = entity.getBoundingBox();
            Pos p = entity.getPosition();
            if (p.x() + box.minX() < x + 1 && p.x() + box.maxX() > x && p.y() + box.minY() < y + 1 && p.y() + box.maxY() > y
                    && p.z() + box.minZ() < z + 1 && p.z() + box.maxZ() > z) {
                return false;
            }
        }
        return true;
    }
}

package space.nows.tube.server.mob.kind.monster.enderman;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.metadata.monster.EndermanMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.ai.Goal;
import space.nows.tube.server.world.rule.RuleBook;

import java.util.Set;

final class TakeBlock extends Goal {

    private static final int CHANCE = 20;
    private static final Set<String> HOLDABLE = Set.of(
            "allium", "azure_bluet", "blue_orchid", "brown_mushroom", "cactus", "cactus_flower", "carved_pumpkin", "clay",
            "closed_eyeblossom", "coarse_dirt", "cornflower", "crimson_fungus", "crimson_nylium", "crimson_roots", "dandelion",
            "dirt", "golden_dandelion", "grass_block", "gravel", "lily_of_the_valley", "melon", "moss_block", "mud",
            "muddy_mangrove_roots", "mycelium", "open_eyeblossom", "orange_tulip", "oxeye_daisy", "pale_moss_block", "pink_tulip",
            "podzol", "poppy", "pumpkin", "red_mushroom", "red_sand", "red_tulip", "rooted_dirt", "sand", "tnt", "torchflower",
            "warped_fungus", "warped_nylium", "warped_roots", "white_tulip", "wither_rose");

    private final Creature enderman;

    TakeBlock(Creature enderman) {
        this.enderman = enderman;
    }

    @Override
    public boolean canStart() {
        return enderman.getEntityMeta() instanceof EndermanMeta meta && meta.getCarriedBlock() == null
                && RuleBook.enabled(enderman.getInstance(), GameRule.MOB_GRIEFING) && enderman.random().nextInt(halved(CHANCE)) == 0;
    }

    @Override
    public void tick() {
        Instance instance = enderman.getInstance();
        Pos at = enderman.getPosition();
        int x = (int) Math.floor(at.x() - 2 + enderman.random().nextDouble() * 4);
        int y = (int) Math.floor(at.y() + enderman.random().nextDouble() * 3);
        int z = (int) Math.floor(at.z() - 2 + enderman.random().nextDouble() * 4);
        Block block = instance.getBlock(x, y, z);
        if (!HOLDABLE.contains(block.key().value()) || !reachable(instance, at, x, y, z)) {
            return;
        }
        instance.setBlock(x, y, z, Block.AIR);
        ((EndermanMeta) enderman.getEntityMeta()).setCarriedBlock(block.defaultState());
    }

    private static boolean reachable(Instance instance, Pos at, int x, int y, int z) {
        double fromX = at.blockX() + 0.5;
        double fromZ = at.blockZ() + 0.5;
        double dx = x + 0.5 - fromX;
        double dz = z + 0.5 - fromZ;
        int steps = (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dz)) * 4);
        for (int i = 1; i < steps; i++) {
            int bx = (int) Math.floor(fromX + dx * i / steps);
            int bz = (int) Math.floor(fromZ + dz * i / steps);
            if ((bx != x || bz != z) && !instance.getBlock(bx, y, bz).air()) {
                return false;
            }
        }
        return true;
    }
}

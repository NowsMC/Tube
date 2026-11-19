package space.nows.tube.server.mob.kind.golem;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.biome.Biome;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;
import space.nows.tube.server.world.rule.RuleBook;

final class Snowfall implements Trait {

    private static final float MELTING_TEMPERATURE = 1.0f;
    private static final double FOOT = 0.25;

    private final Creature golem;

    Snowfall(Creature golem) {
        this.golem = golem;
    }

    @Override
    public void tick() {
        Instance instance = golem.getInstance();
        if (golem.isDead() || instance == null) {
            return;
        }
        if (golem.inWater() || melting(instance, golem.getPosition())) {
            golem.damage(DamageType.ON_FIRE, 1);
        }
        if (!RuleBook.enabled(instance, GameRule.MOB_GRIEFING)) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            Point at = new Vec(Math.floor(golem.getPosition().x() + (i % 2 * 2 - 1) * FOOT), Math.floor(golem.getPosition().y()),
                    Math.floor(golem.getPosition().z() + (i / 2 % 2 * 2 - 1) * FOOT));
            if (golem.blockAt(at).air() && golem.blockAt(at.add(0, -1, 0)).solid()) {
                instance.setBlock(at, Block.SNOW);
            }
        }
    }

    private static boolean melting(Instance instance, Point at) {
        Biome biome = MinecraftServer.getBiomeRegistry().get(instance.getBiome(at));
        return biome != null && biome.temperature() > MELTING_TEMPERATURE;
    }
}

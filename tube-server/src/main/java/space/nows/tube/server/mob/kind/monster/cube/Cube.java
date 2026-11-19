package space.nows.tube.server.mob.kind.monster.cube;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.cube.AbstractCubeMeta;
import space.nows.tube.api.entity.brain.Brain;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;
import space.nows.tube.server.mob.brain.target.HuntNearest;
import space.nows.tube.server.mob.kind.monster.Monster;
import space.nows.tube.server.mob.spawn.LocalDifficulty;

import java.util.random.RandomGenerator;

abstract class Cube extends Monster {

    private static final double UNIT = 0.52;
    private static final double REACH_HEIGHT = 4;

    static int size(Creature cube) {
        return cube.getEntityMeta() instanceof AbstractCubeMeta meta ? meta.getSize() : 1;
    }

    static void resize(Creature cube, int size, boolean heal) {
        int actual = Math.clamp(size, 1, 127);
        if (cube.getEntityMeta() instanceof AbstractCubeMeta meta) {
            meta.setSize(actual);
        }
        double side = UNIT * actual;
        cube.setBoundingBox(side, side, side);
        cube.getAttribute(Attribute.MAX_HEALTH).setBaseValue(actual * actual);
        cube.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.2 + 0.1 * actual);
        ((Cube) cube.kind()).sized(cube, actual);
        if (heal) {
            cube.setHealth((float) (actual * actual));
        }
    }

    protected abstract void sized(Creature cube, int size);

    abstract boolean harmful(Creature cube);

    abstract float squashDamage(Creature cube);

    int jumpDelay(Creature cube) {
        return cube.random().nextInt(20) + 10;
    }

    double jumpPower(Creature cube) {
        return 0.42;
    }

    String jumpSound(Creature cube) {
        return size(cube) > 1 ? "jump" : "jump_small";
    }

    protected boolean hunts() {
        return true;
    }

    @Override
    public void configure(Creature creature, Brain brain) {
        Hop hop = new Hop(creature, this);
        creature.addTrait(hop);
        creature.addTrait(new Squash(creature, this));
        brain.add(1, new KeepHopping(creature, hop, true));
        brain.add(2, new HopAttack(creature, this, hop));
        brain.add(4, new HopAbout(creature, hop));
        brain.add(5, new KeepHopping(creature, hop, false));
        if (!hunts()) {
            return;
        }
        brain.add(1, HuntNearest.players(creature, true,
                target -> Math.abs(target.getPosition().y() - creature.getPosition().y()) <= REACH_HEIGHT));
        brain.add(3, HuntNearest.of(creature, EntityType.IRON_GOLEM, true));
    }

    @Override
    public void prepare(Creature creature, RandomGenerator random) {
        int roll = random.nextInt(3);
        if (roll < 2 && random.nextFloat() < 0.5f * LocalDifficulty.at(creature.getInstance(), 0).specialMultiplier()) {
            roll++;
        }
        resize(creature, 1 << roll, true);
    }

    @Override
    public void died(Creature creature) {
        int size = size(creature);
        if (size <= 1) {
            return;
        }
        double offset = creature.getBoundingBox().width() / 2;
        int count = 2 + creature.random().nextInt(3);
        for (int i = 0; i < count; i++) {
            Creature child = new Creature(this);
            Pos at = creature.getPosition().add((i % 2 - 0.5) * offset, 0.5, (i / 2 - 0.5) * offset)
                    .withView(creature.random().nextFloat() * 360, 0);
            CreatureKinds.spawn(child, creature.getInstance(), at).thenRun(() -> resize(child, size / 2, true));
        }
    }
}

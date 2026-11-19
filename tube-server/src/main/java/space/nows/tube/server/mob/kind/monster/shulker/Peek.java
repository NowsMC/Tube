package space.nows.tube.server.mob.kind.monster.shulker;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.golem.ShulkerMeta;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Peek implements Trait {

    private static final double CLOSED_ARMOR = 20;
    private static final int PEEK_ONE_IN = 40;
    private static final int MIN_PEEK = 40;
    private static final int MAX_PEEK = 100;
    private static final byte OPEN = 100;
    private static final byte PEEKING = 30;

    private final Creature shulker;
    private int openFor;

    Peek(Creature shulker) {
        this.shulker = shulker;
        shield((byte) 0);
    }

    @Override
    public void tick() {
        if (shulker.isDead()) {
            return;
        }
        if (shulker.target() != null) {
            shield(OPEN);
            return;
        }
        if (openFor > 0) {
            if (--openFor == 0) {
                shield((byte) 0);
            }
        } else if (shulker.random().nextInt(PEEK_ONE_IN) == 0) {
            openFor = MIN_PEEK + shulker.random().nextInt(MAX_PEEK - MIN_PEEK);
            shield(PEEKING);
        }
    }

    private void shield(byte height) {
        if (shulker.getEntityMeta() instanceof ShulkerMeta meta) {
            meta.setShieldHeight(height);
        }
        shulker.getAttribute(Attribute.ARMOR).setBaseValue(height == 0 ? CLOSED_ARMOR : 0);
    }
}

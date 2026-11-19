package space.nows.tube.server.world.effect;

import net.minestom.server.entity.Entity;

public enum EntityEffect {
    FANGS_BITE(4),
    TAMING_FAILED(6),
    TAMING_SUCCEEDED(7),
    EAT_GRASS(10),
    IN_LOVE(18),
    GUARDIAN_ATTACK(21),
    TELEPORT(46);

    private final byte status;

    EntityEffect(int status) {
        this.status = (byte) status;
    }

    public void play(Entity entity) {
        entity.triggerStatus(status);
    }
}

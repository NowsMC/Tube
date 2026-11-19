package space.nows.tube.server.mob.life;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.tameable.TameableAnimalMeta;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

import java.util.UUID;

public final class Tameness implements Trait {

    private final Creature animal;
    private UUID owner;
    private boolean sitting;

    public Tameness(Creature animal) {
        this.animal = animal;
    }

    public boolean tamed() {
        return owner != null;
    }

    public boolean ownedBy(Player player) {
        return owner != null && owner.equals(player.getUuid());
    }

    public Player owner() {
        if (owner == null || animal.getInstance() == null) {
            return null;
        }
        for (Player player : animal.getInstance().getPlayers()) {
            if (player.getUuid().equals(owner)) {
                return player;
            }
        }
        return null;
    }

    public void tame(Player player) {
        owner = player.getUuid();
        animal.setPersistent(true);
        if (animal.getEntityMeta() instanceof TameableAnimalMeta meta) {
            meta.setTamed(true);
            meta.setOwner(owner);
        }
        sit(true);
    }

    public boolean sitting() {
        return sitting;
    }

    public void sit(boolean value) {
        sitting = value;
        if (animal.getEntityMeta() instanceof TameableAnimalMeta meta) {
            meta.setSitting(value);
        }
    }

    @Override
    public void tick() {
    }
}

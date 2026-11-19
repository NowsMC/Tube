package space.nows.tube.server.mob.kind.monster.warden;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Gloom implements Trait {

    private static final int EVERY = 120;
    private static final double RANGE = 20;
    private static final int DURATION = 260;

    private final Creature warden;

    Gloom(Creature warden) {
        this.warden = warden;
    }

    @Override
    public void tick() {
        if (warden.isDead() || warden.getAliveTicks() % EVERY != 0) {
            return;
        }
        for (Player player : warden.getInstance().getPlayers()) {
            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR
                    && player.getPosition().distanceSquared(warden.getPosition()) < RANGE * RANGE) {
                player.addEffect(new Potion(PotionEffect.DARKNESS, 0, DURATION));
            }
        }
    }
}

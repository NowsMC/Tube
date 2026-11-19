package space.nows.tube.server.mob.kind.monster.guardian;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

final class Curse implements Trait {

    private static final int EVERY = 1200;
    private static final double RANGE = 50;
    private static final int DURATION = 6000;
    private static final int AMPLIFIER = 2;
    private static final int REFRESH_BELOW = 1200;

    private final Creature elder;

    Curse(Creature elder) {
        this.elder = elder;
    }

    @Override
    public void tick() {
        if (elder.isDead() || (elder.getAliveTicks() + elder.getEntityId()) % EVERY != 0) {
            return;
        }
        for (Player player : elder.getInstance().getPlayers()) {
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR
                    || player.getPosition().distanceSquared(elder.getPosition()) > RANGE * RANGE || !needsCurse(player)) {
                continue;
            }
            player.addEffect(new Potion(PotionEffect.MINING_FATIGUE, AMPLIFIER, DURATION));
            player.sendPacket(new ChangeGameStatePacket(ChangeGameStatePacket.Reason.PLAYER_ELDER_GUARDIAN_MOB_APPEARANCE, 1f));
        }
    }

    private static boolean needsCurse(Player player) {
        for (TimedPotion active : player.getActiveEffects()) {
            if (active.potion().effect() == PotionEffect.MINING_FATIGUE) {
                return active.potion().amplifier() < AMPLIFIER || active.potion().duration() < REFRESH_BELOW;
            }
        }
        return true;
    }
}

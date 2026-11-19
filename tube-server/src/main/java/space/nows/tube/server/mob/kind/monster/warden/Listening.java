package space.nows.tube.server.mob.kind.monster.warden;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.monster.WardenMeta;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.trait.Trait;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class Listening implements Trait {

    private static final double HEARING = 16;
    private static final int STEP_ANGER = 35;
    private static final int HIT_ANGER = 100;
    private static final int ANGRY = 80;
    private static final int MAX_ANGER = 150;
    private static final int LISTEN_EVERY = 20;
    private static final int CALM_EVERY = 20;
    private static final double MOVED = 0.01;

    private final Creature warden;
    private final Map<UUID, Integer> anger = new HashMap<>();
    private final Map<UUID, Pos> lastSeen = new HashMap<>();

    Listening(Creature warden) {
        this.warden = warden;
    }

    void provoke(LivingEntity by) {
        add(by, HIT_ANGER);
    }

    @Override
    public void tick() {
        long now = warden.getAliveTicks();
        if (now % CALM_EVERY == 0) {
            anger.replaceAll((id, value) -> Math.max(0, value - 1));
        }
        if (now % LISTEN_EVERY != 0) {
            return;
        }
        for (Player player : warden.getInstance().getPlayers()) {
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR
                    || player.getPosition().distanceSquared(warden.getPosition()) > HEARING * HEARING) {
                continue;
            }
            Pos before = lastSeen.put(player.getUuid(), player.getPosition());
            boolean moved = before != null && before.distanceSquared(player.getPosition()) > MOVED;
            if (moved && !player.isSneaking()) {
                add(player, STEP_ANGER);
            }
        }
    }

    private void add(LivingEntity entity, int amount) {
        int level = Math.min(MAX_ANGER, anger.getOrDefault(entity.getUuid(), 0) + amount);
        anger.put(entity.getUuid(), level);
        if (warden.getEntityMeta() instanceof WardenMeta meta) {
            meta.setAngerLevel(level);
        }
        if (level >= ANGRY && warden.target() == null) {
            warden.setTarget(entity);
        }
    }
}

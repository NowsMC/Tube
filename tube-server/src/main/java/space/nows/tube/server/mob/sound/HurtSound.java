package space.nows.tube.server.mob.sound;

import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.sound.SoundEvent;
import space.nows.tube.server.mob.Creature;

public final class HurtSound {

    private HurtSound() {
    }

    public static void install(GlobalEventHandler handler) {
        handler.addListener(EntityDamageEvent.class, event -> {
            if (!event.isCancelled() && event.getEntity() instanceof Creature creature) {
                SoundEvent hurt = SoundEvent.fromKey(MobVoice.key(creature, "hurt"));
                if (hurt != null) {
                    event.setSound(hurt);
                }
            }
        });
    }
}

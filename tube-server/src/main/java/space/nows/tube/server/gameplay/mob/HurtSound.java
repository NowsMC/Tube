package space.nows.tube.server.gameplay.mob;

import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.sound.SoundEvent;

final class HurtSound {

    private HurtSound() {
    }

    static void install(GlobalEventHandler handler) {
        handler.addListener(EntityDamageEvent.class, event -> {
            if (!event.isCancelled() && event.getEntity() instanceof Mob mob) {
                SoundEvent hurt = SoundEvent.fromKey(MobVoice.key(mob, "hurt"));
                if (hurt != null) {
                    event.setSound(hurt);
                }
            }
        });
    }
}

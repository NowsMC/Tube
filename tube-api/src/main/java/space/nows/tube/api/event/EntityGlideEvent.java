package space.nows.tube.api.event;

import space.nows.tube.api.entity.LivingEntity;

public record EntityGlideEvent(LivingEntity entity, boolean gliding) implements Event {
}

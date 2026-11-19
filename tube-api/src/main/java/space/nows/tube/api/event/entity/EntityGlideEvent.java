package space.nows.tube.api.event.entity;

import space.nows.tube.api.entity.LivingEntity;
import space.nows.tube.api.event.Event;

public record EntityGlideEvent(LivingEntity entity, boolean gliding) implements Event {
}

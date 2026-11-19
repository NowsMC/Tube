package space.nows.tube.server.mob.brain;

import space.nows.tube.api.entity.mob.brain.Activity;
import space.nows.tube.api.entity.mob.brain.Brain;

import java.util.function.Predicate;

record ActivityRule(Activity activity, Predicate<Brain> when) {
}

package space.nows.tube.server.mob.brain;

import space.nows.tube.api.entity.brain.Activity;
import space.nows.tube.api.entity.brain.Brain;

import java.util.function.Predicate;

record ActivityRule(Activity activity, Predicate<Brain> when) {
}

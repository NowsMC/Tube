package space.nows.tube.server.mob.life;

import net.minestom.server.coordinate.Pos;
import space.nows.tube.server.gameplay.Experience;
import space.nows.tube.server.mob.Creature;
import space.nows.tube.server.mob.CreatureKinds;

public final class Birth {

    private static final int MAX_EXPERIENCE = 7;

    private Birth() {
    }

    public static void deliver(Creature mother, Creature father) {
        Lifecycle motherLife = mother.trait(Lifecycle.class).orElseThrow();
        Lifecycle fatherLife = father.trait(Lifecycle.class).orElseThrow();
        Creature child = new Creature(mother.kind());
        Pos at = mother.getPosition().withView(0, 0);
        CreatureKinds.spawn(child, mother.getInstance(), at).thenRun(() -> {
            child.trait(Lifecycle.class).ifPresent(Lifecycle::makeBaby);
            if (mother.kind() instanceof Breedable breedable) {
                breedable.inherit(child, mother, father, mother.random());
            }
        });
        motherLife.bred();
        fatherLife.bred();
        Experience.spawn(mother.getInstance(), mother.getPosition(), mother.random().nextInt(MAX_EXPERIENCE) + 1);
    }
}

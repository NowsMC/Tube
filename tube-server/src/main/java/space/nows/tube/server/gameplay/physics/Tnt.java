package space.nows.tube.server.gameplay.physics;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import space.nows.tube.server.gameplay.Explosions;
import space.nows.tube.server.world.effect.SoundPlayer;

import java.util.concurrent.ThreadLocalRandom;

final class Tnt extends Entity {

    private static final int FUSE = 80;

    private final Entity source;

    private Tnt(Entity source) {
        super(EntityType.TNT);
        this.source = source;
        ((PrimedTntMeta) getEntityMeta()).setFuseTime(FUSE);
    }

    static void prime(Instance instance, Point position, Entity source) {
        Tnt tnt = new Tnt(source);
        double angle = ThreadLocalRandom.current().nextDouble(Math.PI * 2);
        tnt.setVelocity(new Vec(-Math.sin(angle) * 0.4, 4, -Math.cos(angle) * 0.4));
        tnt.setInstance(instance, new Pos(position.blockX() + 0.5, position.blockY(), position.blockZ() + 0.5));
        SoundPlayer.play(instance, SoundEvent.ENTITY_TNT_PRIMED, Sound.Source.BLOCK, position, 1f, 1f);
    }

    @Override
    public void update(long time) {
        super.update(time);
        if (getAliveTicks() >= FUSE && getInstance() != null) {
            Instance instance = getInstance();
            Pos position = getPosition();
            remove();
            Explosions.explode(instance, position.add(0, 0.06, 0), 4, true, source);
        }
    }
}

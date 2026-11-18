package space.nows.tube.server.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

@EnvTest
class EndermanTest {

    private Instance instance;
    private Player player;

    private void setup(Env env) {
        instance = env.createFlatInstance();
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        instance.setTime(18000);
        player = env.createPlayer(instance, new Pos(0, 40, 0));
        player.setGameMode(GameMode.SURVIVAL);
    }

    private Creature spawnEnderman(Pos at) {
        Creature enderman = new Creature(CreatureKinds.of(EntityType.ENDERMAN).orElseThrow());
        CreatureKinds.spawn(enderman, instance, at).join();
        return enderman;
    }

    @Test
    void staringAtTheHeadAngersIt(Env env) {
        setup(env);
        Creature enderman = spawnEnderman(new Pos(8, 40, 0));
        for (int i = 0; i < 60; i++) {
            Pos eye = enderman.getPosition().add(0, enderman.getEyeHeight(), 0);
            player.teleport(player.getPosition().withLookAt(eye.sub(0, player.getEyeHeight(), 0))).join();
            env.tick();
        }
        assertEquals(player, enderman.getTarget());
    }

    @Test
    void arrowsDoNotHurtIt(Env env) {
        setup(env);
        Creature enderman = spawnEnderman(new Pos(8, 40, 0));
        EntityProjectile arrow = new EntityProjectile(player, EntityType.ARROW);
        assertFalse(enderman.damage(Damage.fromProjectile(player, arrow, 5)));
        assertEquals(40f, enderman.getHealth());
    }
}

package space.nows.tube.server.world.rule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minestom.server.instance.Instance;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.server.config.FileConfig;

import java.nio.file.Path;

@EnvTest
class RuleBookTest {

    @TempDir
    Path directory;

    @Test
    void fallsBackToTheDefaultUntilChanged(Env env) {
        Instance instance = env.process().instance().createInstanceContainer();

        assertTrue(RuleBook.enabled(instance, GameRule.MOB_GRIEFING));
        RuleBook.set(instance, GameRule.MOB_GRIEFING, false);

        assertFalse(RuleBook.enabled(instance, GameRule.MOB_GRIEFING));
    }

    @Test
    void storesAndLoadsRules(Env env) {
        Instance saved = env.process().instance().createInstanceContainer();
        RuleBook.set(saved, GameRule.KEEP_INVENTORY, true);
        FileConfig file = FileConfig.load(directory.resolve("rules.yml"));
        RuleBook.store(saved, file);
        file.save();

        Instance loaded = env.process().instance().createInstanceContainer();
        RuleBook.load(loaded, FileConfig.load(directory.resolve("rules.yml")));

        assertTrue(RuleBook.enabled(loaded, GameRule.KEEP_INVENTORY));
    }

    @Test
    void pausesTheClockWithTheDaylightCycle(Env env) {
        Instance instance = env.process().instance().createInstanceContainer();

        RuleBook.set(instance, GameRule.DO_DAYLIGHT_CYCLE, false);
        assertTrue(instance.defaultClock().paused());

        RuleBook.set(instance, GameRule.DO_DAYLIGHT_CYCLE, true);
        assertFalse(instance.defaultClock().paused());
    }
}

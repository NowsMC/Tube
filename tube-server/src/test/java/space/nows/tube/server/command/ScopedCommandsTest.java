package space.nows.tube.server.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandInfo;
import space.nows.tube.api.command.CommandRegistry;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.plugin.PluginCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ScopedCommandsTest {

    private static final class Labels implements CommandRegistry {

        final Map<String, List<String>> registered = new LinkedHashMap<>();

        @Override
        public void register(String name, Command command, String... aliases) {
            registered.put(name, List.of(aliases));
        }

        @Override
        public void unregister(String name) {
            registered.remove(name);
        }

        @Override
        public Collection<String> names() {
            return registered.keySet();
        }

        @Override
        public Optional<CommandInfo> find(String label) {
            boolean known = registered.containsKey(label) || registered.values().stream().anyMatch(aliases -> aliases.contains(label));
            return known ? Optional.of(new CommandInfo(label, List.of(), null, null, null)) : Optional.empty();
        }

        @Override
        public boolean dispatch(CommandSender sender, String line) {
            return false;
        }
    }

    private static final Command NOTHING = (sender, args) -> {
    };

    @Test
    void registersTheNameAndAQualifiedLabel() {
        Labels labels = new Labels();
        ScopedCommands scoped = new ScopedCommands(labels, "Home Plugin", List.of());

        scoped.register("home", NOTHING, "h");

        assertEquals(List.of("h", "home-plugin:home"), labels.registered.get("home"));
    }

    @Test
    void fallsBackToTheQualifiedLabelWhenTheNameIsTaken() {
        Labels labels = new Labels();
        labels.register("stop", NOTHING);
        ScopedCommands scoped = new ScopedCommands(labels, "extras", List.of());

        scoped.register("stop", NOTHING, "halt");

        assertEquals(List.of("halt"), labels.registered.get("extras:stop"));
        assertTrue(labels.registered.containsKey("stop"));
    }

    @Test
    void skipsTakenAliasesAndKeepsTheRest() {
        Labels labels = new Labels();
        labels.register("tp", NOTHING);
        ScopedCommands scoped = new ScopedCommands(labels, "warps", List.of());

        scoped.register("warp", NOTHING, "tp", "w");

        assertEquals(List.of("w", "warps:warp"), labels.registered.get("warp"));
    }

    @Test
    void unregistersOnlyOwnCommands() {
        Labels labels = new Labels();
        labels.register("stop", NOTHING);
        ScopedCommands scoped = new ScopedCommands(labels, "extras", List.of());
        scoped.register("home", NOTHING);

        assertThrows(IllegalArgumentException.class, () -> scoped.unregister("stop"));
        scoped.unregister("home");

        assertEquals(List.of("stop"), new ArrayList<>(labels.registered.keySet()));
    }

    @Test
    void unregistersByTheLabelItFellBackTo() {
        Labels labels = new Labels();
        labels.register("stop", NOTHING);
        ScopedCommands scoped = new ScopedCommands(labels, "extras", List.of());
        scoped.register("stop", NOTHING);

        scoped.unregister("extras:stop");

        assertEquals(List.of("stop"), new ArrayList<>(labels.registered.keySet()));
    }

    @Test
    void rejectsRegisteringTheSameNameTwice() {
        ScopedCommands scoped = new ScopedCommands(new Labels(), "extras", List.of());
        scoped.register("home", NOTHING);

        assertThrows(IllegalArgumentException.class, () -> scoped.register("home", NOTHING));
    }

    @Test
    void reportsDeclaredCommandsThatWereNeverRegistered() {
        ScopedCommands scoped = new ScopedCommands(new Labels(), "extras", List.of(new PluginCommand("home", null, null, null, null, null), new PluginCommand("spawn", null, null, null, null, null)));
        scoped.register("home", NOTHING);

        assertEquals(List.of("spawn"), scoped.declaredButMissing());
    }

    @Test
    void unregistersEverythingItOwns() {
        Labels labels = new Labels();
        ScopedCommands scoped = new ScopedCommands(labels, "extras", List.of());
        scoped.register("home", NOTHING);
        scoped.register("spawn", NOTHING);

        scoped.unregisterAll();

        assertTrue(labels.registered.isEmpty());
    }
}

package space.nows.tube.server.console;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import space.nows.tube.server.command.CommandService;

import java.util.Collection;
import java.util.List;

final class CommandCompleter implements Completer {

    private final TubeConsole console;

    CommandCompleter(TubeConsole console) {
        this.console = console;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        CommandService commands = console.commands();
        if (commands == null) {
            return;
        }
        if (line.wordIndex() == 0) {
            offer(commands.availableTo(commands.console()), candidates);
            return;
        }
        offer(commands.suggest(commands.console(), line.line().substring(0, line.cursor())), candidates);
    }

    private static void offer(Collection<String> options, List<Candidate> candidates) {
        for (String option : options) {
            candidates.add(new Candidate(option, option, null, null, null, null, true));
        }
    }
}

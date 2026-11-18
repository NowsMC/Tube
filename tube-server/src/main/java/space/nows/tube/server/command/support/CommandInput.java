package space.nows.tube.server.command.support;

import java.util.List;

public final class CommandInput extends space.nows.tube.api.command.CommandInput {

    public CommandInput(List<String> words, String usage) {
        super(words, usage);
    }
}

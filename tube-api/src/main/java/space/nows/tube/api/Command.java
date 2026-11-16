package space.nows.tube.api;

import java.util.List;

@FunctionalInterface
public interface Command {

    void execute(CommandSender sender, List<String> args);
}

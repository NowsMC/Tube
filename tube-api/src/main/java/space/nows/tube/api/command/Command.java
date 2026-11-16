package space.nows.tube.api.command;

import java.util.List;

@FunctionalInterface
public interface Command {

    void execute(CommandSender sender, List<String> args);

    default String permission() {
        return null;
    }

    static Command withPermission(String permission, Command delegate) {
        return new Command() {
            @Override
            public void execute(CommandSender sender, List<String> args) {
                delegate.execute(sender, args);
            }

            @Override
            public String permission() {
                return permission;
            }
        };
    }
}

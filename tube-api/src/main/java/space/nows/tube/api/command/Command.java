package space.nows.tube.api.command;

import java.util.List;

@FunctionalInterface
public interface Command {

    void execute(CommandSender sender, List<String> args);

    default String permission() {
        return null;
    }

    default List<String> suggest(CommandSender sender, List<String> args) {
        return List.of();
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

            @Override
            public List<String> suggest(CommandSender sender, List<String> args) {
                return delegate.suggest(sender, args);
            }
        };
    }
}

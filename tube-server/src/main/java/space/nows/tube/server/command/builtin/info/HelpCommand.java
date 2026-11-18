package space.nows.tube.server.command.builtin.info;

import space.nows.tube.api.command.Command;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.server.command.CommandService;
import space.nows.tube.server.command.support.CommandInput;
import space.nows.tube.server.command.support.ServerCommand;

import java.util.List;
import java.util.Optional;

final class HelpCommand extends ServerCommand {

    private final CommandService commands;

    HelpCommand(CommandService commands) {
        super("help", null, "help [command]");
        this.commands = commands;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        if (!input.hasNext()) {
            sender.sendMessage("Commands:");
            for (String name : commands.availableTo(sender)) {
                String description = commands.find(name).map(Command::description).orElse(null);
                sender.sendMessage(description == null || description.isBlank() ? "/" + name : "/" + name + " - " + description);
            }
            return;
        }
        String label = input.word();
        input.end();
        Optional<Command> found = commands.find(label);
        if (found.isEmpty() || !visible(sender, found.get())) {
            sender.sendMessage("Unknown command: " + label);
            return;
        }
        Command command = found.get();
        sender.sendMessage("/" + label + (command.description() == null || command.description().isBlank() ? "" : " - " + command.description()));
        if (command.usage() != null && !command.usage().isBlank()) {
            sender.sendMessage("Usage: " + usage(command.usage(), label));
        }
        List<String> aliases = commands.aliases(label);
        if (!aliases.isEmpty()) {
            sender.sendMessage("Aliases: " + String.join(", ", aliases));
        }
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return args.size() == 1 ? Suggestions.matching(args.getFirst(), commands.availableTo(sender)) : List.of();
    }

    private static boolean visible(CommandSender sender, Command command) {
        return command.permission() == null || sender.hasPermission(command.permission());
    }

    static String usage(String usage, String label) {
        String text = usage.strip().replace("<command>", label);
        return text.startsWith("/") ? text : "/" + text;
    }
}

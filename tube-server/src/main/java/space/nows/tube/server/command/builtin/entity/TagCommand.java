package space.nows.tube.server.command.builtin.entity;

import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.command.Typing;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

final class TagCommand extends ServerCommand {

    private final Targets targets;

    TagCommand(Targets targets) {
        super("tag", Permissions.command("tag"), "tag <targets> add|remove <name> | tag <targets> list");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        List<Entity> entities = targets.entities(sender, input.word());
        switch (input.keyword()) {
            case "add" -> change(sender, entities, input.word(), true);
            case "remove" -> change(sender, entities, input.word(), false);
            case "list" -> list(sender, entities);
            default -> throw input.usage();
        }
        input.end();
    }

    private static void change(CommandSender sender, List<Entity> entities, String tag, boolean add) {
        int changed = 0;
        for (Entity entity : entities) {
            if (add ? entity.addTag(tag) : entity.removeTag(tag)) {
                changed++;
            }
        }
        if (changed == 0) {
            throw new CommandFailure(add ? "Nothing changed, the targets already have the tag" : "Nothing changed, the targets do not have the tag");
        }
        sender.sendMessage((add ? "Added tag '" : "Removed tag '") + tag + (add ? "' to " : "' from ") + changed + (changed == 1 ? " entity" : " entities"));
    }

    private static void list(CommandSender sender, List<Entity> entities) {
        Set<String> tags = new TreeSet<>();
        entities.forEach(entity -> tags.addAll(entity.tags()));
        sender.sendMessage(tags.isEmpty() ? "The targets have no tags" : "The targets have " + tags.size() + " tags: " + String.join(", ", tags));
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        String typed = Typing.current(args);
        return switch (Typing.position(args)) {
            case 0 -> targets.suggestEntities(typed);
            case 1 -> Suggestions.matching(typed, "add", "list", "remove");
            case 2 -> Typing.word(args, 1).equalsIgnoreCase("remove") ? Suggestions.matching(typed, knownTags(sender, Typing.word(args, 0))) : List.of();
            default -> List.of();
        };
    }

    private List<String> knownTags(CommandSender sender, String selector) {
        try {
            Set<String> tags = new TreeSet<>();
            targets.entities(sender, selector).forEach(entity -> tags.addAll(entity.tags()));
            return List.copyOf(tags);
        } catch (CommandFailure e) {
            return List.of();
        }
    }
}

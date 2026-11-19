package space.nows.tube.server.command.builtin.entity;

import space.nows.tube.api.command.CommandInput;
import space.nows.tube.api.command.CommandSender;
import space.nows.tube.api.command.argument.Suggestions;
import space.nows.tube.api.command.argument.Typing;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.entity.LivingEntity;
import space.nows.tube.server.command.builtin.Permissions;
import space.nows.tube.server.command.support.ServerCommand;
import space.nows.tube.server.command.support.Targets;

import java.util.List;

final class KillCommand extends ServerCommand {

    private final Targets targets;

    KillCommand(Targets targets) {
        super("kill", Permissions.command("kill"), "kill [targets]");
        this.targets = targets;
    }

    @Override
    protected void run(CommandSender sender, CommandInput input) {
        List<Entity> victims = input.hasNext() ? targets.entities(sender, input.word()) : List.of(Targets.self(sender));
        input.end();
        for (Entity victim : victims) {
            if (victim instanceof LivingEntity living) {
                living.setHealth(0);
            } else {
                victim.remove();
            }
        }
        sender.sendMessage(victims.size() == 1 ? "Killed " + describe(victims.getFirst()) : "Killed " + victims.size() + " entities");
    }

    private static String describe(Entity entity) {
        return entity instanceof space.nows.tube.api.entity.player.Player player ? player.name() : entity.type();
    }

    @Override
    public List<String> suggest(CommandSender sender, List<String> args) {
        return Typing.position(args) == 0 ? targets.suggestEntities(Typing.current(args)) : List.of();
    }
}

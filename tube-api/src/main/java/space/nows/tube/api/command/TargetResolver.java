package space.nows.tube.api.command;

import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.world.Location;

import java.util.List;
import java.util.Optional;

public interface TargetResolver {

    Optional<Player> online(String name);

    Player player(CommandSender sender, String text);

    List<Player> players(CommandSender sender, String text);

    List<Entity> entities(CommandSender sender, String text);

    Location origin(CommandSender sender);

    List<String> suggestPlayers(String typed);

    List<String> suggestEntities(String typed);
}

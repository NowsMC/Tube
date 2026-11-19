package space.nows.tube.api.scoreboard;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.scoreboard.team.Team;

import java.util.Collection;
import java.util.Optional;

public interface Scoreboards {

    Sidebar createSidebar(Component title);

    Team createTeam(String name);

    Optional<Team> team(String name);

    Collection<Team> teams();

    void removeTeam(Team team);

    default Optional<Team> teamOf(Entity entity) {
        return teams().stream().filter(team -> team.hasMember(entity)).findFirst();
    }
}

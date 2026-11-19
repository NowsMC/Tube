package space.nows.tube.api.scoreboard;

import net.kyori.adventure.text.Component;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.scoreboard.objective.Objective;
import space.nows.tube.api.scoreboard.objective.ObjectiveSlot;
import space.nows.tube.api.scoreboard.team.Team;

import java.util.Collection;
import java.util.Optional;

public interface Scoreboards {

    Sidebar createSidebar(Component title);

    Team createTeam(String name);

    Team createPrivateTeam(String name);

    Optional<Team> team(String name);

    Collection<Team> teams();

    void removeTeam(Team team);

    Objective createObjective(String name, ObjectiveSlot slot, Component title);

    Optional<Objective> objective(String name);

    Collection<Objective> objectives();

    void removeObjective(Objective objective);

    default Optional<Team> teamOf(Entity entity) {
        return teams().stream().filter(team -> team.hasMember(entity)).findFirst();
    }
}

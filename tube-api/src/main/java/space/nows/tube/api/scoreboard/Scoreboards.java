package space.nows.tube.api.scoreboard;

import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.Optional;

public interface Scoreboards {

    Sidebar createSidebar(Component title);

    Team createTeam(String name);

    Optional<Team> team(String name);

    Collection<Team> teams();

    void removeTeam(Team team);
}

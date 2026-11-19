package space.nows.tube.server.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.scoreboard.Sidebar;
import space.nows.tube.api.scoreboard.SidebarLine;
import space.nows.tube.api.scoreboard.tab.TabEntry;
import space.nows.tube.api.scoreboard.team.NameTagVisibility;
import space.nows.tube.api.scoreboard.team.Team;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@EnvTest
class ScoreboardTest {

    @TempDir
    Path directory;

    private PlayerService players;
    private ScoreboardService scoreboards;

    private void setup() {
        WorldService worlds = new WorldService(directory, "main");
        players = new PlayerService(worlds, new PermissionService(directory));
        scoreboards = new ScoreboardService(players);
    }

    @Test
    void aSidebarKeepsLinesInScoreOrderAndShowsToChosenPlayers(Env env) {
        setup();
        var handle = env.createPlayer(env.createFlatInstance(), new Pos(0, 42, 0));
        Player player = players.wrap(handle);
        Sidebar sidebar = scoreboards.createSidebar(Component.text("Tube"));

        sidebar.setLine("a", Component.text("first"), 1);
        sidebar.setLine("b", Component.text("second"), 5);
        sidebar.setLine("a", Component.text("changed"), 3);
        sidebar.show(player);

        List<SidebarLine> lines = sidebar.lines();
        assertEquals(List.of("b", "a"), lines.stream().map(SidebarLine::id).toList());
        assertEquals(Component.text("changed"), lines.get(1).text());
        assertEquals(1, sidebar.viewers().size());

        sidebar.setNumbersHidden(true);
        assertTrue(sidebar.numbersHidden());
        sidebar.removeLine("b");
        sidebar.removeLine("missing");
        assertEquals(1, sidebar.lines().size());
        sidebar.hide(player);
        assertTrue(sidebar.viewers().isEmpty());
        sidebar.clear();
        assertTrue(sidebar.lines().isEmpty());
    }

    @Test
    void teamsKeepTheirLooksAndMembers(Env env) {
        setup();
        Team team = scoreboards.createTeam("staff");

        team.setPrefix(Component.text("[Staff] "));
        team.setColor(NamedTextColor.RED);
        team.setNameTagVisibility(NameTagVisibility.NEVER);
        team.addMember("Cy");

        assertEquals(Component.text("[Staff] "), team.prefix());
        assertEquals(NamedTextColor.RED, team.color());
        assertEquals(NameTagVisibility.NEVER, team.nameTagVisibility());
        assertEquals(java.util.Set.of("Cy"), team.members());
        assertTrue(scoreboards.team("staff").isPresent());
        assertThrows(IllegalArgumentException.class, () -> scoreboards.createTeam("staff"));

        scoreboards.removeTeam(team);
        assertFalse(scoreboards.team("staff").isPresent());
        assertTrue(scoreboards.teams().isEmpty());
    }

    @Test
    void theTabListSendsEntryChangesToItsViewerOnly(Env env) {
        setup();
        var connection = env.createConnection(new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000d1"), "Dee"));
        var viewerHandle = connection.connect(env.createFlatInstance(), new Pos(0, 42, 0));
        Player viewer = players.wrap(viewerHandle);
        var updates = connection.trackIncoming(PlayerInfoUpdatePacket.class);
        var removals = connection.trackIncoming(PlayerInfoRemovePacket.class);
        UUID fake = UUID.fromString("00000000-0000-0000-0000-0000000000f1");

        viewer.tabList().add(TabEntry.of(fake, "Spacer").withDisplayName(Component.text("--")).withOrder(9));
        viewer.tabList().setDisplayName(viewer, Component.text("Boss"));
        viewer.tabList().setOrder(viewer, 2);
        env.tick();

        assertEquals(3, updates.collect().size());
        assertEquals(1, viewer.tabList().entries().size());
        viewer.tabList().remove(fake);
        env.tick();

        removals.assertSingle();
        assertTrue(viewer.tabList().entries().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> viewer.tabList().update(TabEntry.of(fake, "Spacer")));
    }
}

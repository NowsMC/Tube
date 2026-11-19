package space.nows.tube.server.scoreboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.DisplayScoreboardPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket;
import net.minestom.server.network.packet.server.play.ScoreboardObjectivePacket;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.network.packet.server.play.UpdateScorePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import space.nows.tube.api.entity.player.Player;
import space.nows.tube.api.scoreboard.Sidebar;
import space.nows.tube.api.scoreboard.SidebarLine;
import space.nows.tube.api.scoreboard.objective.Objective;
import space.nows.tube.api.scoreboard.objective.ObjectiveSlot;
import space.nows.tube.api.scoreboard.objective.ScoreStyle;
import space.nows.tube.api.scoreboard.tab.TabEntry;
import space.nows.tube.api.scoreboard.team.NameTagVisibility;
import space.nows.tube.api.scoreboard.team.Team;
import space.nows.tube.server.access.PermissionService;
import space.nows.tube.server.player.PlayerService;
import space.nows.tube.server.world.WorldService;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
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
        assertEquals(Set.of("Cy"), team.members());
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
        assertEquals(9, viewer.tabList().entry(fake).orElseThrow().order());
        viewer.tabList().remove(fake);
        env.tick();

        removals.assertSingle();
        assertTrue(viewer.tabList().entries().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> viewer.tabList().update(TabEntry.of(fake, "Spacer")));
        assertThrows(IllegalArgumentException.class, () -> TabEntry.of(fake, "SeventeenLetters!"));

        Team guests = scoreboards.createTeam("guests");
        guests.addMember(viewer);
        assertEquals(guests, scoreboards.teamOf(viewer).orElseThrow());
        scoreboards.removeTeam(guests);
        assertTrue(scoreboards.teamOf(viewer).isEmpty());
    }

    @Test
    void anObjectiveShowsScoresInItsSlotAndGivesWayToAnotherInTheSameSlot(Env env) {
        setup();
        var connection = env.createConnection(new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000e1"), "Eve"));
        Player eve = players.wrap(connection.connect(env.createFlatInstance(), new Pos(0, 42, 0)));
        var objectivePackets = connection.trackIncoming(ScoreboardObjectivePacket.class);
        var scorePackets = connection.trackIncoming(UpdateScorePacket.class);
        var displays = connection.trackIncoming(DisplayScoreboardPacket.class);
        Objective health = scoreboards.createObjective("hp", ObjectiveSlot.BELOW_NAME, Component.text("HP"));

        health.setScore(eve, 20);
        health.show(eve);
        health.setScore("Zed", 5);
        health.setStyle(ScoreStyle.HEARTS);
        env.tick();

        assertEquals(2, objectivePackets.collect().size());
        assertEquals(2, scorePackets.collect().size());
        displays.assertSingle();
        assertEquals(20, health.score(eve).orElseThrow());
        assertEquals(Set.of(eve), health.viewers());
        assertThrows(IllegalArgumentException.class, () -> scoreboards.createObjective("hp", ObjectiveSlot.TAB_LIST, null));

        Objective level = scoreboards.createObjective("level", ObjectiveSlot.BELOW_NAME, Component.text("Level"));
        level.show(eve);
        assertTrue(health.viewers().isEmpty());
        assertEquals(Set.of(eve), level.viewers());

        scoreboards.removeObjective(level);
        assertTrue(scoreboards.objective("level").isEmpty());
        assertTrue(level.viewers().isEmpty());
    }

    @Test
    void aPrivateTeamReachesOnlyThePlayersItIsShownTo(Env env) {
        setup();
        var shownConnection = env.createConnection(new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000a1"), "Ann"));
        Player ann = players.wrap(shownConnection.connect(env.createFlatInstance(), new Pos(0, 42, 0)));
        var otherConnection = env.createConnection(new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000b1"), "Bob"));
        otherConnection.connect(env.createFlatInstance(), new Pos(0, 42, 0));
        var annTeams = shownConnection.trackIncoming(TeamsPacket.class);
        var bobTeams = otherConnection.trackIncoming(TeamsPacket.class);
        Team red = scoreboards.createPrivateTeam("red");

        red.show(ann);
        red.addMember("Bob");
        red.setPrefix(Component.text("[R] "));
        env.tick();

        assertEquals(3, annTeams.collect().size());
        bobTeams.assertEmpty();
        assertFalse(red.shared());
        assertEquals(Set.of(ann), red.viewers());
        Team everyone = scoreboards.createTeam("all");
        assertTrue(everyone.shared());
        assertThrows(IllegalStateException.class, () -> everyone.show(ann));
    }

    @Test
    void aRemovedSidebarHidesFromEveryone(Env env) {
        setup();
        Player player = players.wrap(env.createPlayer(env.createFlatInstance(), new Pos(0, 42, 0)));
        Sidebar sidebar = scoreboards.createSidebar(Component.text("Tube"));
        sidebar.setLine("a", Component.text("line"), 1);
        sidebar.show(player);

        sidebar.remove();

        assertTrue(sidebar.viewers().isEmpty());
        assertTrue(sidebar.lines().isEmpty());
    }

    @Test
    void theTabListIsSentAgainAfterARejoinAndCanBeReset(Env env) {
        setup();
        var connection = env.createConnection(new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000c1"), "Cat"));
        var handle = connection.connect(env.createFlatInstance(), new Pos(0, 42, 0));
        Player cat = players.wrap(handle);
        UUID fake = UUID.fromString("00000000-0000-0000-0000-0000000000f2");
        cat.tabList().setHeaderAndFooter(Component.text("top"), null);
        cat.tabList().add(TabEntry.of(fake, "Spacer"));
        var headers = connection.trackIncoming(PlayerListHeaderAndFooterPacket.class);
        var updates = connection.trackIncoming(PlayerInfoUpdatePacket.class);

        MinestomTabList list = players.tabLists().of(handle);
        list.replay();
        env.tick();

        assertSame(cat.tabList(), list);
        headers.assertSingle();
        assertTrue(updates.collect().stream().anyMatch(packet -> packet.entries().stream().anyMatch(entry -> entry.uuid().equals(fake))));

        cat.tabList().reset();
        assertTrue(cat.tabList().entries().isEmpty());
    }
}

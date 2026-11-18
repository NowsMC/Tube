package space.nows.tube.bukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

class ScoreboardLayerTest {

    @TempDir
    Path directory;

    private StubServer stub;
    private BukkitRuntime runtime;

    @BeforeEach
    void start() {
        BukkitRuntime.forgetInstalledServer();
        stub = new StubServer();
        runtime = BukkitRuntime.install(stub.server, directory);
        runtime.useCurrentThreadAsTickThread();
    }

    @AfterEach
    void stop() {
        runtime.shutdown();
    }

    @Test
    void objectivesKeepScoresAndFollowTheirDisplaySlot() {
        Scoreboard board = runtime.scoreboards().main();
        Objective objective = board.registerNewObjective("kills", "dummy", "Kills");

        objective.getScore("Cy").setScore(3);
        objective.getScore("Dee").setScore(7);
        assertEquals(3, objective.getScore("Cy").getScore());
        assertEquals(0, objective.getScore("Nobody").getScore());
        assertFalse(objective.getScore("Nobody").isScoreSet());
        assertEquals(Set.of("Cy", "Dee"), board.getEntries());
        assertThrows(IllegalArgumentException.class, () -> board.registerNewObjective("kills", "dummy"));

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        assertSame(objective, board.getObjective(DisplaySlot.SIDEBAR));
        assertTrue(stub.scoreboardLog.contains("sidebar.line:Cy=3"));
        assertTrue(stub.scoreboardLog.contains("sidebar.line:Dee=7"));

        objective.getScore("Cy").setScore(9);
        assertTrue(stub.scoreboardLog.contains("sidebar.line:Cy=9"));
        board.resetScores("Dee");
        assertEquals(Set.of("Cy"), board.getEntries());

        objective.unregister();
        assertNull(board.getObjective("kills"));
        assertNull(board.getObjective(DisplaySlot.SIDEBAR));
    }

    @Test
    void aPlayerSeesTheSidebarOfTheScoreboardTheyAreGiven() {
        var cy = runtime.adapters().player(stub.player("Cy"));
        Scoreboard own = runtime.scoreboards().getNewScoreboard();
        Objective objective = own.registerNewObjective("side", "dummy", "Side");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore("line").setScore(1);

        assertSame(runtime.scoreboards().main(), cy.getScoreboard());
        cy.setScoreboard(own);

        assertSame(own, cy.getScoreboard());
        assertTrue(stub.scoreboardLog.contains("sidebar.show:Cy"));
        cy.setScoreboard(runtime.scoreboards().main());
        assertTrue(stub.scoreboardLog.contains("sidebar.hide:Cy"));
    }

    @Test
    void mainTeamsReachTubeAndOtherScoreboardsOnlyKeepData() {
        Scoreboard board = runtime.scoreboards().main();
        Team staff = board.registerNewTeam("staff");

        staff.setPrefix("[S] ");
        staff.setColor(ChatColor.RED);
        staff.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        staff.addEntry("Cy");

        assertEquals("[S] ", staff.getPrefix());
        assertSame(staff, board.getEntryTeam("Cy"));
        assertTrue(stub.scoreboardLog.contains("team.setPrefix:" + net.kyori.adventure.text.Component.text("[S] ")));
        assertTrue(stub.scoreboardLog.contains("team.addMember:Cy"));
        assertThrows(IllegalArgumentException.class, () -> staff.setColor(ChatColor.BOLD));
        assertThrows(IllegalArgumentException.class, () -> board.registerNewTeam("staff"));

        int before = stub.scoreboardLog.size();
        Team other = runtime.scoreboards().getNewScoreboard().registerNewTeam("staff");
        other.setPrefix("x");
        other.addEntry("Dee");
        assertEquals(before, stub.scoreboardLog.size());
        assertEquals(Set.of("Dee"), other.getEntries());

        staff.removeEntry("Cy");
        assertEquals(List.of(), List.copyOf(staff.getEntries()));
        staff.unregister();
        assertNull(board.getTeam("staff"));
    }
}

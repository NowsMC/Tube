package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.UUID;

class AccessListsTest {

    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @TempDir
    Path directory;

    @Test
    void userCacheFindsNamesIgnoringCaseAndSurvivesRestarts() throws IOException {
        new UserCache(directory).remember(ALICE, "Alice");

        UserCache reloaded = new UserCache(directory);

        assertEquals(ALICE, reloaded.find("aLiCe").orElseThrow());
        assertEquals("Alice", reloaded.name(ALICE).orElseThrow());
        assertTrue(reloaded.find("Nobody").isEmpty());
        assertTrue(Files.readString(directory.resolve("usercache.json")).contains("expiresOn"));
    }

    @Test
    void userCacheIgnoresExpiredEntries() throws IOException {
        String expired = JsonFiles.TIMESTAMP.format(ZonedDateTime.now().minusDays(1));
        Files.writeString(directory.resolve("usercache.json"),
                "[{\"uuid\":\"" + ALICE + "\",\"name\":\"Alice\",\"expiresOn\":\"" + expired + "\"}]");

        assertTrue(new UserCache(directory).find("Alice").isEmpty());
    }

    @Test
    void whitelistPersistsEntries() {
        Whitelist whitelist = new Whitelist(directory, true);
        assertTrue(whitelist.add(ALICE, "Alice"));
        assertFalse(whitelist.add(ALICE, "Alice"));

        Whitelist reloaded = new Whitelist(directory, true);
        assertTrue(reloaded.contains(ALICE));
        assertTrue(reloaded.remove(ALICE));
        assertFalse(new Whitelist(directory, true).contains(ALICE));
    }

    @Test
    void bansUseTheVanillaFileFormat() throws IOException {
        BanList bans = new BanList(directory);
        bans.banPlayer(ALICE, "Alice", "griefing", "Console");
        bans.banIp("203.0.113.9", "spam", "Console");

        String players = Files.readString(directory.resolve("banned-players.json"));
        assertTrue(players.contains("\"uuid\": \"" + ALICE + "\""));
        assertTrue(players.contains("\"expires\": \"forever\""));
        assertTrue(players.contains("\"reason\": \"griefing\""));
        assertTrue(Files.readString(directory.resolve("banned-ips.json")).contains("\"ip\": \"203.0.113.9\""));

        BanList reloaded = new BanList(directory);
        assertEquals("griefing", reloaded.player(ALICE).orElseThrow().reason());
        assertTrue(reloaded.ip("203.0.113.9").isPresent());
        assertTrue(reloaded.pardonPlayer(ALICE));
        assertTrue(new BanList(directory).player(ALICE).isEmpty());
    }

    @Test
    void expiredBansNoLongerApply() throws IOException {
        String past = JsonFiles.TIMESTAMP.format(ZonedDateTime.now().minusHours(1));
        Files.writeString(directory.resolve("banned-players.json"),
                "[{\"uuid\":\"" + ALICE + "\",\"name\":\"Alice\",\"created\":\"" + past + "\",\"source\":\"Server\",\"expires\":\"" + past + "\",\"reason\":\"old\"}]");

        assertTrue(new BanList(directory).player(ALICE).isEmpty());
    }

    @Test
    void accessControlAppliesBansThenTheWhitelist() {
        BanList bans = new BanList(directory);
        Whitelist whitelist = new Whitelist(directory, false);
        AccessControl access = new AccessControl(whitelist, bans);

        assertTrue(access.deny(ALICE, "198.51.100.1").isEmpty());

        bans.banPlayer(ALICE, "Alice", "griefing", "Console");
        assertEquals("You are banned from this server.\nReason: griefing", text(access, ALICE, "198.51.100.1"));

        bans.banIp("198.51.100.2", "spam", "Console");
        assertEquals("Your IP address is banned from this server.\nReason: spam", text(access, BOB, "198.51.100.2"));

        whitelist.setEnabled(true);
        assertEquals("You are not white-listed on this server!", text(access, BOB, "198.51.100.3"));
        whitelist.add(BOB, "Bob");
        assertTrue(access.deny(BOB, "198.51.100.3").isEmpty());
    }

    private static String text(AccessControl access, UUID uuid, String ip) {
        return PlainTextComponentSerializer.plainText().serialize(access.deny(uuid, ip).orElseThrow());
    }
}

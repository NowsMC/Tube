package space.nows.tube.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;

class LibraryTest {

    @Test
    void parsesDependencyListOutput() {
        String text = """
                The following files have been resolved:
                   net.kyori:adventure-api:jar:5.2.0:compile -- module net.kyori.adventure.api
                   org.example:thing:jar:natives-windows:1.0:runtime
                   org.example:pom-only:pom:1.0:compile

                """;
        assertEquals(List.of(
                new Library("net.kyori", "adventure-api", "5.2.0", ""),
                new Library("org.example", "thing", "1.0", "natives-windows")), Library.parse(text));
    }

    @Test
    void buildsMavenRepositoryPath() {
        assertEquals("net/kyori/adventure-api/5.2.0/adventure-api-5.2.0.jar",
                new Library("net.kyori", "adventure-api", "5.2.0", "").path());
        assertEquals("org/example/thing/1.0/thing-1.0-natives.jar",
                new Library("org.example", "thing", "1.0", "natives").path());
    }

    @Test
    void readsShortCoordinates() {
        assertEquals(new Library("com.zaxxer", "HikariCP", "6.3.0", ""), Library.of("com.zaxxer:HikariCP:6.3.0"));
        assertEquals(new Library("org.example", "thing", "1.0", "natives"), Library.of(" org.example:thing:1.0:natives "));
        assertThrows(IllegalArgumentException.class, () -> Library.of("com.zaxxer:HikariCP"));
        assertThrows(IllegalArgumentException.class, () -> Library.of("com.zaxxer::6.3.0"));
    }
}

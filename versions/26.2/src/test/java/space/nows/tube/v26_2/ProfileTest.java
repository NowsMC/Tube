package space.nows.tube.v26_2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import space.nows.tube.server.VersionProfile;

import java.util.ServiceLoader;

class ProfileTest {

    @Test
    void isTheOnlyProfileOnTheClasspath() {
        var profiles = ServiceLoader.load(VersionProfile.class).stream().map(ServiceLoader.Provider::get).toList();
        assertEquals(1, profiles.size());
        assertEquals("26.2", profiles.getFirst().minecraftVersion());
    }
}

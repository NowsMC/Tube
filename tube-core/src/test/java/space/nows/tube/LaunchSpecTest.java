package space.nows.tube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

class LaunchSpecTest {

    @Test
    void tellsTheServerWhichExitCodeMeansRestart() {
        LaunchSpec spec = new LaunchSpec(Path.of("java"), Path.of("tube.jar"), Path.of("run"), List.of("-Xmx2G"));

        List<String> command = spec.command();

        assertEquals("-Dtube.restart.exit=" + LaunchSpec.RESTART_EXIT_CODE, command.get(1));
        assertEquals("-Xmx2G", command.get(2));
        assertTrue(command.indexOf("-jar") > command.indexOf("-Xmx2G"));
    }
}

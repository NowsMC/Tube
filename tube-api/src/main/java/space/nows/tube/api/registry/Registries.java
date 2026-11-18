package space.nows.tube.api.registry;

import java.util.List;

public interface Registries {

    List<String> items();

    List<String> blocks();

    List<String> entities();

    List<String> effects();

    List<String> sounds();

    List<String> particles();
}

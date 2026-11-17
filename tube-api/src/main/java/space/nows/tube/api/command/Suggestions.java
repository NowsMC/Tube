package space.nows.tube.api.command;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class Suggestions {

    private static final String NAMESPACE = "minecraft:";
    private static final int LIMIT = 200;

    private Suggestions() {
    }

    public static List<String> matching(String typed, Collection<String> options) {
        String prefix = typed.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> matches(option.toLowerCase(Locale.ROOT), prefix))
                .sorted()
                .limit(LIMIT)
                .toList();
    }

    public static List<String> matching(String typed, String... options) {
        return matching(typed, List.of(options));
    }

    private static boolean matches(String option, String prefix) {
        if (option.startsWith(prefix)) {
            return true;
        }
        return option.startsWith(NAMESPACE) && option.substring(NAMESPACE.length()).startsWith(prefix);
    }
}

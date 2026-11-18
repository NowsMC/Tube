package space.nows.tube.server.command.support;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import space.nows.tube.api.command.CommandFailure;
import space.nows.tube.api.command.Suggestions;
import space.nows.tube.api.entity.Entity;
import space.nows.tube.api.entity.Player;
import space.nows.tube.api.world.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class Selector {

    private static final List<String> KEYS = List.of("type=", "name=", "tag=", "limit=", "distance=", "sort=");
    private static final List<String> SORTS = List.of("nearest", "furthest", "random", "arbitrary");
    private static final String NAMESPACE = "minecraft:";

    private final String base;
    private final Map<String, String> arguments;

    private Selector(String base, Map<String, String> arguments) {
        this.base = base;
        this.arguments = arguments;
    }

    public static boolean is(String text) {
        return text.startsWith("@");
    }

    public static Selector parse(String text) {
        int open = text.indexOf('[');
        if (open < 0) {
            return new Selector(text, Map.of());
        }
        if (!text.endsWith("]")) {
            throw new CommandFailure("Unclosed selector " + text);
        }
        Map<String, String> arguments = new LinkedHashMap<>();
        String body = text.substring(open + 1, text.length() - 1);
        if (!body.isBlank()) {
            for (String pair : body.split(",")) {
                int equals = pair.indexOf('=');
                if (equals <= 0) {
                    throw new CommandFailure("Invalid selector option " + pair);
                }
                String key = pair.substring(0, equals).strip().toLowerCase(Locale.ROOT);
                if (!KEYS.contains(key + "=")) {
                    throw new CommandFailure("Unknown selector option " + key);
                }
                arguments.put(key, pair.substring(equals + 1).strip());
            }
        }
        return new Selector(text.substring(0, open), arguments);
    }

    public String base() {
        return base;
    }

    public <T extends Entity> List<T> apply(List<T> candidates, Location origin) {
        List<T> found = new ArrayList<>(candidates.stream().filter(this::matchesType).filter(this::matchesName).filter(this::matchesTag)
                .filter(entity -> matchesDistance(entity, origin)).toList());
        switch (sort()) {
            case "nearest" -> found.sort(Comparator.comparingDouble(entity -> distance(entity, origin)));
            case "furthest" -> found.sort(Comparator.comparingDouble((T entity) -> distance(entity, origin)).reversed());
            case "random" -> java.util.Collections.shuffle(found, ThreadLocalRandom.current());
            default -> {
            }
        }
        int limit = limit();
        return limit >= 0 && found.size() > limit ? List.copyOf(found.subList(0, limit)) : List.copyOf(found);
    }

    private String sort() {
        String sort = arguments.get("sort");
        if (sort == null) {
            return base.equals("@p") ? "nearest" : base.equals("@r") ? "random" : "arbitrary";
        }
        String lowered = sort.toLowerCase(Locale.ROOT);
        if (!SORTS.contains(lowered)) {
            throw new CommandFailure("Invalid sort " + sort);
        }
        return lowered;
    }

    private int limit() {
        String limit = arguments.get("limit");
        if (limit == null) {
            return base.equals("@p") || base.equals("@r") ? 1 : -1;
        }
        try {
            int parsed = Integer.parseInt(limit);
            if (parsed < 1) {
                throw new CommandFailure("The limit must be at least 1");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new CommandFailure("Invalid limit " + limit);
        }
    }

    private boolean matchesType(Entity entity) {
        String type = arguments.get("type");
        if (type == null) {
            return true;
        }
        boolean inverted = type.startsWith("!");
        String wanted = bare(inverted ? type.substring(1) : type);
        String actual = entity instanceof Player ? "player" : bare(entity.type());
        return actual.equals(wanted) != inverted;
    }

    private boolean matchesName(Entity entity) {
        String name = arguments.get("name");
        if (name == null) {
            return true;
        }
        boolean inverted = name.startsWith("!");
        String wanted = inverted ? name.substring(1) : name;
        String actual = entity instanceof Player player ? player.name()
                : entity.customName().map(component -> PlainTextComponentSerializer.plainText().serialize(component)).orElse("");
        return actual.equals(wanted) != inverted;
    }

    private boolean matchesTag(Entity entity) {
        String tag = arguments.get("tag");
        if (tag == null) {
            return true;
        }
        if (tag.isEmpty()) {
            return entity.tags().isEmpty();
        }
        boolean inverted = tag.startsWith("!");
        if (inverted && tag.length() == 1) {
            return !entity.tags().isEmpty();
        }
        return entity.tags().contains(inverted ? tag.substring(1) : tag) != inverted;
    }

    private boolean matchesDistance(Entity entity, Location origin) {
        String range = arguments.get("distance");
        if (range == null) {
            return true;
        }
        int dots = range.indexOf("..");
        try {
            double actual = distance(entity, origin);
            if (dots < 0) {
                return actual == Double.parseDouble(range);
            }
            String low = range.substring(0, dots);
            String high = range.substring(dots + 2);
            return (low.isEmpty() || actual >= Double.parseDouble(low)) && (high.isEmpty() || actual <= Double.parseDouble(high));
        } catch (NumberFormatException e) {
            throw new CommandFailure("Invalid distance " + range);
        }
    }

    private static double distance(Entity entity, Location origin) {
        Location at = entity.location();
        if (at.world() != origin.world()) {
            return Double.MAX_VALUE;
        }
        double dx = at.x() - origin.x();
        double dy = at.y() - origin.y();
        double dz = at.z() - origin.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static String bare(String type) {
        String lowered = type.toLowerCase(Locale.ROOT);
        return lowered.startsWith(NAMESPACE) ? lowered.substring(NAMESPACE.length()) : lowered;
    }

    public static List<String> suggest(String typed, Collection<String> bases, Collection<String> types) {
        int open = typed.indexOf('[');
        if (open < 0) {
            for (String base : bases) {
                if (base.startsWith("@") && base.equalsIgnoreCase(typed)) {
                    return List.of(typed + "[");
                }
            }
            return List.of();
        }
        if (typed.endsWith("]")) {
            return List.of();
        }
        int split = Math.max(open, typed.lastIndexOf(','));
        String prefix = typed.substring(0, split + 1);
        String current = typed.substring(split + 1);
        int equals = current.indexOf('=');
        if (equals < 0) {
            return Suggestions.matching(current, KEYS).stream().map(key -> prefix + key).toList();
        }
        String key = current.substring(0, equals).toLowerCase(Locale.ROOT);
        String value = current.substring(equals + 1);
        String negation = value.startsWith("!") ? "!" : "";
        String head = prefix + current.substring(0, equals + 1) + negation;
        Collection<String> options = switch (key) {
            case "type" -> types;
            case "sort" -> SORTS;
            default -> List.of();
        };
        return Suggestions.matching(value.substring(negation.length()), options).stream().map(option -> head + option).toList();
    }
}

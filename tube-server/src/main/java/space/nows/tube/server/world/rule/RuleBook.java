package space.nows.tube.server.world.rule;

import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import space.nows.tube.api.world.GameRule;
import space.nows.tube.server.config.FileConfig;

import java.util.EnumMap;
import java.util.Map;

public final class RuleBook {

    private static final String SECTION = "rules.";
    private static final Map<GameRule, Tag<Boolean>> TAGS = tags();

    private RuleBook() {
    }

    public static boolean enabled(Instance instance, GameRule rule) {
        if (instance == null) {
            return rule.defaultValue();
        }
        Boolean value = instance.getTag(TAGS.get(rule));
        return value == null ? rule.defaultValue() : value;
    }

    public static void set(Instance instance, GameRule rule, boolean value) {
        instance.setTag(TAGS.get(rule), value);
        if (rule == GameRule.DO_DAYLIGHT_CYCLE) {
            if (value) {
                instance.defaultClock().resume();
            } else {
                instance.defaultClock().pause();
            }
        }
    }

    public static void load(Instance instance, FileConfig settings) {
        for (GameRule rule : GameRule.values()) {
            String key = SECTION + rule.key();
            if (settings.contains(key)) {
                set(instance, rule, settings.getBoolean(key, rule.defaultValue()));
            }
        }
    }

    public static void store(Instance instance, FileConfig settings) {
        for (GameRule rule : GameRule.values()) {
            settings.set(SECTION + rule.key(), enabled(instance, rule));
        }
    }

    private static Map<GameRule, Tag<Boolean>> tags() {
        Map<GameRule, Tag<Boolean>> tags = new EnumMap<>(GameRule.class);
        for (GameRule rule : GameRule.values()) {
            tags.put(rule, Tag.Transient("tube:rule:" + rule.key()));
        }
        return tags;
    }
}

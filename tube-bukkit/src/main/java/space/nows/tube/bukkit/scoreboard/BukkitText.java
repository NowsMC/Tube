package space.nows.tube.bukkit.scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

final class BukkitText {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private BukkitText() {
    }

    static Component component(String text) {
        return text == null || text.isEmpty() ? Component.empty() : LEGACY.deserialize(text);
    }
}

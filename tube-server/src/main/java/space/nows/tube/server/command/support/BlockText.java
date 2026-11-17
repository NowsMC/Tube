package space.nows.tube.server.command.support;

import space.nows.tube.api.world.Block;
import space.nows.tube.server.world.BlockMapper;

import java.util.HashMap;
import java.util.Map;

public final class BlockText {

    private BlockText() {
    }

    public static Block parse(String text) {
        int open = text.indexOf('[');
        String key = open < 0 ? text : text.substring(0, open);
        Map<String, String> properties = new HashMap<>();
        if (open >= 0) {
            if (!text.endsWith("]")) {
                throw new CommandFailure("Invalid block " + text);
            }
            for (String pair : text.substring(open + 1, text.length() - 1).split(",")) {
                if (pair.isBlank()) {
                    continue;
                }
                int equals = pair.indexOf('=');
                if (equals < 0) {
                    throw new CommandFailure("Invalid block property " + pair);
                }
                properties.put(pair.substring(0, equals).strip(), pair.substring(equals + 1).strip());
            }
        }
        try {
            return BlockMapper.fromMinestom(BlockMapper.toMinestom(new Block(key, properties)));
        } catch (IllegalArgumentException e) {
            throw new CommandFailure("Unknown block " + text);
        }
    }
}

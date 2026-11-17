package space.nows.tube.server.gameplay.block;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerEditSignEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.OpenSignEditorPacket;

import java.util.List;

public final class Signs {

    private static final int MAX_LINE = 384;

    private Signs() {
    }

    static boolean isSign(Block block) {
        return block.key().value().endsWith("_sign");
    }

    public static void install(GlobalEventHandler handler) {
        handler.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (!event.isCancelled() && isSign(event.getBlock())) {
                Player player = event.getPlayer();
                BlockVec position = event.getBlockPosition();
                player.scheduleNextTick(ignored -> {
                    if (isSign(player.getInstance().getBlock(position))) {
                        player.sendPacket(new OpenSignEditorPacket(position, true));
                    }
                });
            }
        });
        handler.addListener(PlayerBlockInteractEvent.class, event -> {
            Block block = event.getBlock();
            if (event.isCancelled() || !isSign(block) || event.getPlayer().isSneaking()
                    || block.nbt() != null && block.nbt().getBoolean("is_waxed")) {
                return;
            }
            event.setBlockingItemUse(true);
            event.getPlayer().sendPacket(new OpenSignEditorPacket(event.getBlockPosition(), true));
        });
        handler.addListener(PlayerEditSignEvent.class, event -> {
            Block current = event.getInstance().getBlock(event.getBlockPosition());
            if (!isSign(current) || event.getPlayer().getPosition().distanceSquared(event.getBlockPosition()) > 64) {
                return;
            }
            CompoundBinaryTag nbt = current.nbt() == null ? CompoundBinaryTag.empty() : current.nbt();
            String side = event.isFrontText() ? "front_text" : "back_text";
            CompoundBinaryTag text = nbt.getCompound(side).put("messages", messages(event.getLines()));
            if (!text.keySet().contains("color")) {
                text = text.putString("color", "black").putBoolean("has_glowing_text", false);
            }
            event.getInstance().setBlock(event.getBlockPosition(), current.withNbt(nbt.put(side, text)), false);
        });
    }

    private static ListBinaryTag messages(List<String> lines) {
        ListBinaryTag.Builder<StringBinaryTag> builder = ListBinaryTag.builder(BinaryTagTypes.STRING);
        for (int i = 0; i < 4; i++) {
            String line = i < lines.size() ? lines.get(i) : "";
            builder.add(StringBinaryTag.stringBinaryTag(line.length() > MAX_LINE ? line.substring(0, MAX_LINE) : line));
        }
        return builder.build();
    }
}

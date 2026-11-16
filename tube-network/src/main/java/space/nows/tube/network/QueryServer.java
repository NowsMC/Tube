package space.nows.tube.network;

import space.nows.tube.api.network.UdpSocket;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class QueryServer implements AutoCloseable {

    public record Info(String motd, String gameType, String map, String version, String plugins, int online, int maxPlayers, int port, String hostIp, List<String> players) {
    }

    private static final int TYPE_STAT = 0;
    private static final int TYPE_HANDSHAKE = 9;
    private static final long TOKEN_LIFETIME_MILLIS = 30_000;
    private static final byte[] FULL_PADDING = {0x73, 0x70, 0x6C, 0x69, 0x74, 0x6E, 0x75, 0x6D, 0x00, (byte) 0x80, 0x00};
    private static final byte[] PLAYER_PADDING = {0x01, 0x70, 0x6C, 0x61, 0x79, 0x65, 0x72, 0x5F, 0x00, 0x00};

    private record Challenge(int token, long expires) {
    }

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();
    private final Supplier<Info> info;
    private final UdpSocket socket;

    public QueryServer(NettyNetwork network, InetSocketAddress address, Supplier<Info> info) {
        this.info = info;
        this.socket = network.bindUdp(address, (udp, sender, data) -> handle(udp, sender, data));
    }

    public InetSocketAddress address() {
        return socket.address();
    }

    @Override
    public void close() {
        socket.close();
    }

    private void handle(UdpSocket udp, InetSocketAddress sender, byte[] data) {
        if (data.length < 7 || (data[0] & 0xFF) != 0xFE || (data[1] & 0xFF) != 0xFD) {
            return;
        }
        ByteBuffer in = ByteBuffer.wrap(data, 2, data.length - 2);
        int type = in.get() & 0xFF;
        int session = in.getInt() & 0x0F0F0F0F;
        long now = System.currentTimeMillis();
        challenges.values().removeIf(challenge -> challenge.expires() < now);
        String key = sender.getAddress().getHostAddress();

        if (type == TYPE_HANDSHAKE) {
            int token = random.nextInt() & 0x7FFFFFFF;
            challenges.put(key, new Challenge(token, now + TOKEN_LIFETIME_MILLIS));
            ByteArrayOutputStream out = header(TYPE_HANDSHAKE, session);
            text(out, Integer.toString(token));
            udp.send(sender, out.toByteArray());
            return;
        }
        if (type != TYPE_STAT || in.remaining() < 4) {
            return;
        }
        Challenge challenge = challenges.get(key);
        if (challenge == null || challenge.token() != in.getInt()) {
            return;
        }
        boolean full = in.remaining() >= 4;
        udp.send(sender, (full ? full(session, info.get()) : basic(session, info.get())));
    }

    static byte[] basic(int session, Info info) {
        ByteArrayOutputStream out = header(TYPE_STAT, session);
        text(out, info.motd());
        text(out, info.gameType());
        text(out, info.map());
        text(out, Integer.toString(info.online()));
        text(out, Integer.toString(info.maxPlayers()));
        out.writeBytes(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) info.port()).array());
        text(out, info.hostIp());
        return out.toByteArray();
    }

    static byte[] full(int session, Info info) {
        ByteArrayOutputStream out = header(TYPE_STAT, session);
        out.writeBytes(FULL_PADDING);
        pair(out, "hostname", info.motd());
        pair(out, "gametype", info.gameType());
        pair(out, "game_id", "MINECRAFT");
        pair(out, "version", info.version());
        pair(out, "plugins", info.plugins());
        pair(out, "map", info.map());
        pair(out, "numplayers", Integer.toString(info.online()));
        pair(out, "maxplayers", Integer.toString(info.maxPlayers()));
        pair(out, "hostport", Integer.toString(info.port()));
        pair(out, "hostip", info.hostIp());
        out.write(0);
        out.writeBytes(PLAYER_PADDING);
        info.players().forEach(player -> text(out, player));
        out.write(0);
        return out.toByteArray();
    }

    private static ByteArrayOutputStream header(int type, int session) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(type);
        out.writeBytes(ByteBuffer.allocate(4).putInt(session).array());
        return out;
    }

    private static void pair(ByteArrayOutputStream out, String key, String value) {
        text(out, key);
        text(out, value);
    }

    private static void text(ByteArrayOutputStream out, String value) {
        out.writeBytes(value.getBytes(StandardCharsets.UTF_8));
        out.write(0);
    }
}

package space.nows.tube.network;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.nows.tube.api.network.TcpHandler;
import space.nows.tube.api.network.TcpConnection;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class NetworkTest {

    private static final InetAddress LOCAL = InetAddress.getLoopbackAddress();

    private NettyNetwork network;

    @BeforeEach
    void start() {
        network = new NettyNetwork(null);
    }

    @AfterEach
    void stop() {
        network.close();
    }

    @Test
    void tcpHandlersReceiveAndReply() throws IOException {
        var server = network.listenTcp(new InetSocketAddress(LOCAL, 0), new TcpHandler() {
            @Override
            public void received(TcpConnection connection, byte[] data) {
                connection.send(new String(data, StandardCharsets.UTF_8).toUpperCase().getBytes(StandardCharsets.UTF_8));
            }
        });
        try (Socket socket = new Socket(LOCAL, server.address().getPort())) {
            socket.setSoTimeout(5000);
            socket.getOutputStream().write("ping".getBytes(StandardCharsets.UTF_8));
            assertEquals("PING", new String(socket.getInputStream().readNBytes(4), StandardCharsets.UTF_8));
        } finally {
            server.close();
        }
    }

    @Test
    void udpSocketsReceiveAndReply() throws IOException {
        var socket = network.bindUdp(new InetSocketAddress(LOCAL, 0), (udp, sender, data) -> udp.send(sender, data));
        try (DatagramSocket client = new DatagramSocket()) {
            client.setSoTimeout(5000);
            byte[] message = {1, 2, 3};
            client.send(new DatagramPacket(message, message.length, LOCAL, socket.address().getPort()));
            DatagramPacket reply = new DatagramPacket(new byte[16], 16);
            client.receive(reply);
            assertArrayEquals(message, java.util.Arrays.copyOf(reply.getData(), reply.getLength()));
        } finally {
            socket.close();
        }
    }

    private static void rcon(OutputStream out, int id, int type, String payload) throws IOException {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(14 + bytes.length).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(10 + bytes.length).putInt(id).putInt(type).put(bytes).put((byte) 0).put((byte) 0);
        out.write(buffer.array());
        out.flush();
    }

    private record Reply(int id, int type, String payload) {
    }

    private static Reply read(InputStream in) throws IOException {
        DataInputStream data = new DataInputStream(in);
        int length = Integer.reverseBytes(data.readInt());
        int id = Integer.reverseBytes(data.readInt());
        int type = Integer.reverseBytes(data.readInt());
        byte[] payload = data.readNBytes(length - 10);
        data.readNBytes(2);
        return new Reply(id, type, new String(payload, StandardCharsets.UTF_8));
    }

    @Test
    void rconRequiresTheCorrectPassword() throws IOException {
        try (RconServer server = new RconServer(network, new InetSocketAddress(LOCAL, 0), "secret", command -> CompletableFuture.completedFuture("ran " + command));
             Socket socket = new Socket(LOCAL, server.address().getPort())) {
            socket.setSoTimeout(5000);
            rcon(socket.getOutputStream(), 7, 3, "wrong");
            assertEquals(-1, read(socket.getInputStream()).id());
        }
    }

    @Test
    void rconRunsCommandsAfterLogin() throws IOException {
        try (RconServer server = new RconServer(network, new InetSocketAddress(LOCAL, 0), "secret", command -> CompletableFuture.completedFuture("ran " + command));
             Socket socket = new Socket(LOCAL, server.address().getPort())) {
            socket.setSoTimeout(5000);
            rcon(socket.getOutputStream(), 1, 3, "secret");
            assertEquals(1, read(socket.getInputStream()).id());
            rcon(socket.getOutputStream(), 2, 2, "list");
            Reply reply = read(socket.getInputStream());
            assertEquals(2, reply.id());
            assertEquals("ran list", reply.payload());
        }
    }

    @Test
    void rconRefusesCommandsBeforeLogin() throws IOException {
        try (RconServer server = new RconServer(network, new InetSocketAddress(LOCAL, 0), "secret", command -> CompletableFuture.completedFuture("ran"));
             Socket socket = new Socket(LOCAL, server.address().getPort())) {
            socket.setSoTimeout(5000);
            rcon(socket.getOutputStream(), 3, 2, "stop");
            assertEquals(-1, read(socket.getInputStream()).id());
        }
    }

    @Test
    void rconSplitsLongResponses() throws IOException {
        String big = "x".repeat(5000);
        try (RconServer server = new RconServer(network, new InetSocketAddress(LOCAL, 0), "secret", command -> CompletableFuture.completedFuture(big));
             Socket socket = new Socket(LOCAL, server.address().getPort())) {
            socket.setSoTimeout(5000);
            rcon(socket.getOutputStream(), 1, 3, "secret");
            read(socket.getInputStream());
            rcon(socket.getOutputStream(), 2, 2, "big");
            Reply first = read(socket.getInputStream());
            Reply second = read(socket.getInputStream());
            assertEquals(4096, first.payload().length());
            assertEquals(904, second.payload().length());
        }
    }

    private static byte[] query(DatagramSocket client, int port, byte[] request) throws IOException {
        client.send(new DatagramPacket(request, request.length, LOCAL, port));
        DatagramPacket reply = new DatagramPacket(new byte[2048], 2048);
        client.receive(reply);
        return java.util.Arrays.copyOf(reply.getData(), reply.getLength());
    }

    private static byte[] request(int type, int session, Integer token, boolean full) {
        ByteBuffer buffer = ByteBuffer.allocate(15);
        buffer.put((byte) 0xFE).put((byte) 0xFD).put((byte) type).putInt(session);
        if (token != null) {
            buffer.putInt(token);
            if (full) {
                buffer.putInt(0);
            }
        }
        return java.util.Arrays.copyOf(buffer.array(), buffer.position());
    }

    private static List<String> strings(byte[] data, int from) {
        List<String> result = new java.util.ArrayList<>();
        ByteArrayOutputStream current = new ByteArrayOutputStream();
        for (int i = from; i < data.length; i++) {
            if (data[i] == 0) {
                result.add(current.toString(StandardCharsets.UTF_8));
                current.reset();
            } else {
                current.write(data[i]);
            }
        }
        return result;
    }

    @Test
    void queryAnswersBasicAndFullStats() throws IOException {
        var info = new QueryServer.Info("A Tube Server", "SMP", "world", "26.2", "Tube 1: Demo 1.0", 2, 20, 25565, "127.0.0.1", List.of("Steve", "Alex"));
        try (QueryServer server = new QueryServer(network, new InetSocketAddress(LOCAL, 0), () -> info);
             DatagramSocket client = new DatagramSocket()) {
            client.setSoTimeout(5000);
            int port = server.address().getPort();

            byte[] handshake = query(client, port, request(9, 5, null, false));
            assertEquals(9, handshake[0]);
            int token = Integer.parseInt(new String(handshake, 5, handshake.length - 6, StandardCharsets.UTF_8));

            byte[] basic = query(client, port, request(0, 5, token, false));
            assertEquals(List.of("A Tube Server", "SMP", "world", "2", "20"), strings(basic, 5).subList(0, 5));
            assertEquals(25565, ByteBuffer.wrap(basic, basic.length - 1 - "127.0.0.1".length() - 1 - 1, 2).order(ByteOrder.LITTLE_ENDIAN).getShort() & 0xFFFF);

            byte[] full = query(client, port, request(0, 5, token, true));
            List<String> fields = strings(full, 16);
            assertTrue(fields.containsAll(List.of("hostname", "A Tube Server", "numplayers", "2", "Steve", "Alex")), fields.toString());
        }
    }

    @Test
    void queryIgnoresStatsWithoutAHandshake() throws IOException {
        var info = new QueryServer.Info("m", "SMP", "w", "v", "", 0, 1, 1, "", List.of());
        try (QueryServer server = new QueryServer(network, new InetSocketAddress(LOCAL, 0), () -> info);
             DatagramSocket client = new DatagramSocket()) {
            client.setSoTimeout(500);
            byte[] bad = request(0, 1, 12345, false);
            client.send(new DatagramPacket(bad, bad.length, LOCAL, server.address().getPort()));
            DatagramPacket reply = new DatagramPacket(new byte[64], 64);
            try {
                client.receive(reply);
                throw new AssertionError("unexpected reply");
            } catch (java.net.SocketTimeoutException expected) {
                assertTrue(true);
            }
        }
    }
}

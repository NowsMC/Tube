package space.nows.tube.network;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import space.nows.tube.api.network.tcp.TcpConnection;
import space.nows.tube.api.network.tcp.TcpHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class NetworkTest {

    private static final InetAddress LOCAL = InetAddress.getLoopbackAddress();

    private NettyNetwork network;

    @BeforeEach
    void start() {
        network = new NettyNetwork(null, Transport.NIO);
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
    void bindingABusyPortFailsWithAnIoError() {
        var first = network.listenTcp(new InetSocketAddress(LOCAL, 0), (connection, data) -> {
        });
        try {
            assertThrows(UncheckedIOException.class, () -> network.listenTcp(first.address(), (connection, data) -> {
            }));
            assertThrows(UncheckedIOException.class, () -> new RconServer(network, first.address(), "secret",
                    command -> CompletableFuture.completedFuture("")));
        } finally {
            first.close();
        }
    }

    @Test
    void rconLocksOutAnAddressAfterThreeFailedLogins() throws IOException {
        try (RconServer server = new RconServer(network, new InetSocketAddress(LOCAL, 0), "secret", command -> CompletableFuture.completedFuture("ran"))) {
            for (int attempt = 0; attempt < 3; attempt++) {
                try (Socket socket = new Socket(LOCAL, server.address().getPort())) {
                    socket.setSoTimeout(5000);
                    rcon(socket.getOutputStream(), attempt, 3, "wrong");
                    assertEquals(-1, read(socket.getInputStream()).id());
                }
            }
            try (Socket socket = new Socket(LOCAL, server.address().getPort())) {
                socket.setSoTimeout(5000);
                assertThrows(IOException.class, () -> {
                    rcon(socket.getOutputStream(), 9, 3, "secret");
                    read(socket.getInputStream());
                });
            }
        }
    }

    @Test
    void closingATcpServerClosesItsConnections() throws IOException {
        var server = network.listenTcp(new InetSocketAddress(LOCAL, 0), (connection, data) -> connection.send(data));
        try (Socket socket = new Socket(LOCAL, server.address().getPort())) {
            socket.setSoTimeout(5000);
            socket.getOutputStream().write(1);
            assertEquals(1, socket.getInputStream().read());

            server.close();

            assertEquals(-1, socket.getInputStream().read());
        }
    }

    @Test
    void rconLockoutAlsoStopsConnectionsOpenedBeforeIt() throws IOException {
        try (RconServer server = new RconServer(network, new InetSocketAddress(LOCAL, 0), "secret", command -> CompletableFuture.completedFuture("ran"));
             Socket waiting = new Socket(LOCAL, server.address().getPort())) {
            waiting.setSoTimeout(5000);
            for (int attempt = 0; attempt < 3; attempt++) {
                try (Socket socket = new Socket(LOCAL, server.address().getPort())) {
                    socket.setSoTimeout(5000);
                    rcon(socket.getOutputStream(), attempt, 3, "wrong");
                    assertEquals(-1, read(socket.getInputStream()).id());
                }
            }
            rcon(waiting.getOutputStream(), 9, 3, "secret");
            assertEquals(-1, read(waiting.getInputStream()).id());
        }
    }

    @Test
    void slowPluginHandlersDoNotHoldUpRcon() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        var server = network.listenTcp(new InetSocketAddress(LOCAL, 0), (connection, data) -> {
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        List<Socket> stuck = new ArrayList<>();
        try (RconServer rcon = new RconServer(network, new InetSocketAddress(LOCAL, 0), "secret", command -> CompletableFuture.completedFuture("ran"))) {
            for (int i = 0; i < 4; i++) {
                Socket socket = new Socket(LOCAL, server.address().getPort());
                socket.getOutputStream().write(1);
                stuck.add(socket);
            }
            try (Socket socket = new Socket(LOCAL, rcon.address().getPort())) {
                socket.setSoTimeout(1000);
                rcon(socket.getOutputStream(), 5, 3, "secret");
                assertEquals(5, read(socket.getInputStream()).id());
            }
        } finally {
            release.countDown();
            for (Socket socket : stuck) {
                socket.close();
            }
            server.close();
        }
    }

    @Test
    void aConnectionReceivesEverythingInOrderEvenWhenItsHandlerFallsBehind() throws Exception {
        int size = 4 << 20;
        CountDownLatch release = new CountDownLatch(1);
        ByteArrayOutputStream received = new ByteArrayOutputStream();
        CountDownLatch done = new CountDownLatch(1);
        var server = network.listenTcp(new InetSocketAddress(LOCAL, 0), (connection, data) -> {
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            synchronized (received) {
                received.writeBytes(data);
                if (received.size() == size) {
                    done.countDown();
                }
            }
        });
        byte[] sent = new byte[size];
        for (int i = 0; i < size; i++) {
            sent[i] = (byte) (i * 31);
        }
        try (Socket socket = new Socket(LOCAL, server.address().getPort())) {
            Thread writer = Thread.ofVirtual().start(() -> {
                try {
                    socket.getOutputStream().write(sent);
                } catch (IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            });
            Thread.sleep(300);
            release.countDown();
            assertTrue(done.await(10, TimeUnit.SECONDS));
            writer.join();
            synchronized (received) {
                assertArrayEquals(sent, received.toByteArray());
            }
        } finally {
            server.close();
        }
    }

    @Test
    void aConnectionStopsBeingWritableForASlowReaderAndSaysWhenItCanTakeMore() throws Exception {
        AtomicReference<TcpConnection> opened = new AtomicReference<>();
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch writableAgain = new CountDownLatch(1);
        var server = network.listenTcp(new InetSocketAddress(LOCAL, 0), new TcpHandler() {
            @Override
            public void connected(TcpConnection connection) {
                opened.set(connection);
                connected.countDown();
            }

            @Override
            public void received(TcpConnection connection, byte[] data) {
            }

            @Override
            public void writable(TcpConnection connection) {
                writableAgain.countDown();
            }
        });
        try (Socket socket = new Socket()) {
            socket.setReceiveBufferSize(4096);
            socket.connect(server.address());
            socket.setSoTimeout(10_000);
            assertTrue(connected.await(5, TimeUnit.SECONDS));
            TcpConnection connection = opened.get();
            byte[] chunk = new byte[64 << 10];
            long sent = 0;
            CompletableFuture<Void> last = null;
            while (connection.isWritable() && sent < (64L << 20)) {
                last = connection.send(chunk);
                sent += chunk.length;
                Thread.sleep(1);
            }
            assertFalse(connection.isWritable());

            long read = 0;
            byte[] buffer = new byte[64 << 10];
            while (read < sent) {
                int count = socket.getInputStream().read(buffer);
                assertTrue(count > 0);
                read += count;
            }
            assertTrue(writableAgain.await(5, TimeUnit.SECONDS));
            last.get(5, TimeUnit.SECONDS);
            assertTrue(connection.isWritable());
        } finally {
            server.close();
        }
    }

    @Test
    void aPluginThatIgnoresWritabilityIsCutOffBeforeMemoryRunsOut() throws Exception {
        AtomicReference<TcpConnection> opened = new AtomicReference<>();
        CountDownLatch connected = new CountDownLatch(1);
        var server = network.listenTcp(new InetSocketAddress(LOCAL, 0), new TcpHandler() {
            @Override
            public void connected(TcpConnection connection) {
                opened.set(connection);
                connected.countDown();
            }

            @Override
            public void received(TcpConnection connection, byte[] data) {
            }
        });
        try (Socket socket = new Socket()) {
            socket.setReceiveBufferSize(4096);
            socket.connect(server.address());
            assertTrue(connected.await(5, TimeUnit.SECONDS));
            TcpConnection connection = opened.get();
            byte[] chunk = new byte[1 << 20];
            CompletableFuture<Void> refused = null;
            for (int i = 0; i < 64 && refused == null; i++) {
                CompletableFuture<Void> write = connection.send(chunk);
                if (write.isCompletedExceptionally()) {
                    refused = write;
                }
            }
            assertTrue(refused != null);
            CompletableFuture<Void> failed = refused;
            assertThrows(ExecutionException.class, () -> failed.get(5, TimeUnit.SECONDS));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (connection.isOpen() && System.nanoTime() < deadline) {
                Thread.sleep(10);
            }
            assertFalse(connection.isOpen());
        } finally {
            server.close();
        }
    }

    @Test
    void sendsReportWhenTheyAreDoneOrFailed() throws Exception {
        var udp = network.bindUdp(new InetSocketAddress(LOCAL, 0), (socket, sender, data) -> {
        });
        try (DatagramSocket client = new DatagramSocket()) {
            InetSocketAddress target = new InetSocketAddress(LOCAL, client.getLocalPort());
            udp.send(target, new byte[] {1, 2, 3}).get(5, TimeUnit.SECONDS);
            assertThrows(ExecutionException.class, () -> udp.send(target, new byte[70_000]).get(5, TimeUnit.SECONDS));
        } finally {
            udp.close();
        }
    }

    @Test
    void sendCallbacksRunOffTheNetworkThreads() throws Exception {
        CompletableFuture<Boolean> virtual = new CompletableFuture<>();
        var server = network.listenTcp(new InetSocketAddress(LOCAL, 0), (connection, data) ->
                connection.send(data).thenRun(() -> virtual.complete(Thread.currentThread().isVirtual())));
        try (Socket socket = new Socket(LOCAL, server.address().getPort())) {
            socket.setSoTimeout(5000);
            socket.getOutputStream().write(7);
            assertEquals(7, socket.getInputStream().read());
            assertTrue(virtual.get(5, TimeUnit.SECONDS));
        } finally {
            server.close();
        }
    }

    @Test
    void aHandlerThatSneaksOutACheckedExceptionClosesOnlyItsConnection() throws Exception {
        var server = network.listenTcp(new InetSocketAddress(LOCAL, 0), (connection, data) -> {
            if (data[0] == 0) {
                NetworkTest.<RuntimeException>sneak(new IOException("plugin bug"));
            }
            connection.send(data);
        });
        try (Socket broken = new Socket(LOCAL, server.address().getPort());
             Socket healthy = new Socket(LOCAL, server.address().getPort())) {
            broken.setSoTimeout(5000);
            healthy.setSoTimeout(5000);
            broken.getOutputStream().write(0);
            assertEquals(-1, broken.getInputStream().read());
            healthy.getOutputStream().write(5);
            assertEquals(5, healthy.getInputStream().read());
        } finally {
            server.close();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneak(Throwable error) throws T {
        throw (T) error;
    }

    @Test
    void queryKeepsSeparateTokensForClientsBehindOneAddress() throws IOException {
        var info = new QueryServer.Info("m", "SMP", "w", "v", "", 0, 1, 1, "", List.of());
        try (QueryServer server = new QueryServer(network, new InetSocketAddress(LOCAL, 0), () -> info);
             DatagramSocket first = new DatagramSocket();
             DatagramSocket second = new DatagramSocket()) {
            first.setSoTimeout(5000);
            second.setSoTimeout(5000);
            int port = server.address().getPort();
            byte[] firstHandshake = query(first, port, request(9, 1, null, false));
            byte[] secondHandshake = query(second, port, request(9, 2, null, false));
            int firstToken = Integer.parseInt(new String(firstHandshake, 5, firstHandshake.length - 6, StandardCharsets.UTF_8));
            int secondToken = Integer.parseInt(new String(secondHandshake, 5, secondHandshake.length - 6, StandardCharsets.UTF_8));

            assertEquals(0, query(first, port, request(0, 1, firstToken, false))[0]);
            assertEquals(0, query(second, port, request(0, 2, secondToken, false))[0]);
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
    void queryBuildsItsAnswerOnceEveryFiveSecondsLikeVanilla() throws IOException {
        var info = new QueryServer.Info("m", "SMP", "w", "v", "", 0, 1, 1, "", List.of());
        AtomicInteger built = new AtomicInteger();
        try (QueryServer server = new QueryServer(network, new InetSocketAddress(LOCAL, 0), () -> {
            built.incrementAndGet();
            return info;
        });
             DatagramSocket client = new DatagramSocket()) {
            client.setSoTimeout(5000);
            int port = server.address().getPort();
            byte[] handshake = query(client, port, request(9, 5, null, false));
            int token = Integer.parseInt(new String(handshake, 5, handshake.length - 6, StandardCharsets.UTF_8));

            for (int i = 0; i < 5; i++) {
                query(client, port, request(0, 5, token, i % 2 == 0));
            }

            assertEquals(1, built.get());
        }
    }

    @Test
    void closingRconDisconnectsItsClients() throws IOException {
        RconServer server = new RconServer(network, new InetSocketAddress(LOCAL, 0), "secret", command -> CompletableFuture.completedFuture("ran"));
        try (Socket socket = new Socket(LOCAL, server.address().getPort())) {
            socket.setSoTimeout(5000);
            rcon(socket.getOutputStream(), 1, 3, "secret");
            assertEquals(1, read(socket.getInputStream()).id());

            server.close();

            assertEquals(-1, socket.getInputStream().read());
        }
    }

    @Test
    void aPluginsNetworkForgetsSocketsItClosedAndClosesTheRest() throws IOException {
        ScopedNetwork scoped = new ScopedNetwork(network);
        var kept = scoped.listenTcp(new InetSocketAddress(LOCAL, 0), (connection, data) -> {
        });
        var closed = scoped.bindUdp(new InetSocketAddress(LOCAL, 0), (udp, sender, data) -> {
        });
        closed.close();
        assertEquals(1, scoped.open());

        try (Socket socket = new Socket(LOCAL, kept.address().getPort())) {
            socket.setSoTimeout(5000);
            scoped.closeAll();
            assertEquals(-1, socket.getInputStream().read());
        }
        assertEquals(0, scoped.open());
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

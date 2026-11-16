package space.nows.tube.server.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class PluginChannelServiceTest {

    @Test
    void readsTheClientBrandFromAPluginMessage() {
        byte[] message = {7, 'v', 'a', 'n', 'i', 'l', 'l', 'a'};
        assertEquals("vanilla", PluginChannelService.readBrand(message));
        assertEquals("", PluginChannelService.readBrand(new byte[0]));
        assertEquals("fabric", PluginChannelService.readBrand(("\u0006fabric").getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void channelNamesDefaultToTheMinecraftNamespace() {
        assertEquals("minecraft:brand", PluginChannelService.normalize("Brand"));
        assertEquals("bungeecord:main", PluginChannelService.normalize("BungeeCord:Main"));
    }
}

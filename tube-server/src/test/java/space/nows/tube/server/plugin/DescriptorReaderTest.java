package space.nows.tube.server.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import space.nows.tube.api.PermissionDefault;
import space.nows.tube.api.plugin.PluginCommand;
import space.nows.tube.api.plugin.PluginDescriptor;
import space.nows.tube.api.plugin.PluginPermission;
import space.nows.tube.server.config.ConfigFormat;

import java.util.List;
import java.util.Map;

class DescriptorReaderTest {

    private static PluginDescriptor yaml(String text) {
        return DescriptorReader.read(ConfigFormat.ofFileName("tube-plugin.yml").orElseThrow().parse(text));
    }

    @Test
    void readsTubeKeys() {
        PluginDescriptor descriptor = yaml("""
                name: shop
                version: 2.1.0
                main: demo.Shop
                description: A small shop
                authors: [Alice, Bob]
                website: https://example.com
                prefix: Shop
                depends: [economy]
                soft-depends: [chat]
                load-before: [market]
                tube: '0.2611'
                minecraft: ['26.1.2', '26.2']
                """);
        assertEquals("A small shop", descriptor.description());
        assertEquals(List.of("Alice", "Bob"), descriptor.authors());
        assertEquals("https://example.com", descriptor.website());
        assertEquals("Shop", descriptor.logName());
        assertEquals(List.of("economy"), descriptor.depends());
        assertEquals(List.of("chat"), descriptor.softDepends());
        assertEquals(List.of("market"), descriptor.loadBefore());
        assertEquals("0.2611", descriptor.tube());
        assertEquals(List.of("26.1.2", "26.2"), descriptor.minecraft());
    }

    @Test
    void acceptsBukkitNames() {
        PluginDescriptor descriptor = yaml("""
                name: shop
                version: 1
                main: demo.Shop
                author: Alice
                depend: [economy]
                softdepend: chat
                loadbefore: [market]
                commands:
                  shop:
                    description: Opens the shop
                    usage: /<command> [item]
                    aliases: store
                    permission: shop.use
                    permission-message: '&cNo shop for you'
                  sell:
                permissions:
                  shop.use:
                    description: Use the shop
                    default: not op
                  shop.staff:
                    children:
                      shop.use: true
                      shop.close: false
                  shop.all:
                    default: true
                    children: [shop.use]
                """);
        assertEquals(List.of("Alice"), descriptor.authors());
        assertEquals(List.of("economy"), descriptor.depends());
        assertEquals(List.of("chat"), descriptor.softDepends());
        assertEquals(List.of("market"), descriptor.loadBefore());
        assertEquals(List.of(
                new PluginCommand("shop", "Opens the shop", "/<command> [item]", List.of("store"), "shop.use", "&cNo shop for you"),
                new PluginCommand("sell", null, null, List.of(), null, null)), descriptor.commands());
        assertEquals(List.of(
                new PluginPermission("shop.use", "Use the shop", PermissionDefault.NOT_OP, Map.of()),
                new PluginPermission("shop.staff", null, PermissionDefault.OP, Map.of("shop.use", true, "shop.close", false)),
                new PluginPermission("shop.all", null, PermissionDefault.TRUE, Map.of("shop.use", true))), descriptor.permissions());
        assertNull(descriptor.tube());
        assertEquals("shop", descriptor.logName());
    }

    @Test
    void refusesMissingRequiredKeys() {
        assertThrows(IllegalArgumentException.class, () -> yaml("name: shop\nversion: 1\n"));
        assertThrows(IllegalArgumentException.class, () -> yaml("name: 'bad name'\nversion: 1\nmain: demo.Shop\n"));
    }
}

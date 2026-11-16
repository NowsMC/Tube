package space.nows.tube.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import space.nows.tube.api.ItemStack;

import java.util.Map;

class ItemMapperTest {

    @Test
    void enchantmentsAndDamageSurviveTheRoundTrip() {
        ItemStack original = ItemStack.of("diamond_sword").withEnchantments(Map.of("sharpness", 3, "minecraft:unbreaking", 2)).withDamage(12);

        ItemStack back = ItemMapper.fromMinestom(ItemMapper.toMinestom(original));

        assertEquals(Map.of("minecraft:sharpness", 3, "minecraft:unbreaking", 2), back.enchantments());
        assertEquals(12, back.damage());
        assertEquals("minecraft:diamond_sword", back.type());
    }
}

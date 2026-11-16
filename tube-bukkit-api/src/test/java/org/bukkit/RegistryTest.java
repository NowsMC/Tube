package org.bukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.Test;

class RegistryTest {

    @Test
    void registriesLookUpByKey() {
        assertSame(Enchantment.SHARPNESS, Registry.ENCHANTMENT.get(NamespacedKey.minecraft("sharpness")));
        assertSame(Material.STONE, Registry.MATERIAL.get(NamespacedKey.minecraft("stone")));
        assertSame(PotionEffectType.SPEED, Registry.EFFECT.get(NamespacedKey.minecraft("speed")));
        assertNull(Registry.ENCHANTMENT.get(NamespacedKey.minecraft("nothing")));
    }

    @Test
    void legacyNamesPointAtTheSameObjects() {
        assertSame(Enchantment.SHARPNESS, Enchantment.DAMAGE_ALL);
        assertSame(Enchantment.SHARPNESS, Enchantment.getByName("DAMAGE_ALL"));
        assertSame(PotionEffectType.SLOWNESS, PotionEffectType.SLOW);
        assertSame(PotionEffectType.SPEED, PotionEffectType.getById(1));
    }

    @Test
    void itemStacksCarryEnchantments() {
        ItemStack stack = new ItemStack(Material.DIAMOND_SWORD);
        stack.addEnchantment(Enchantment.SHARPNESS, 3);
        assertEquals(3, stack.getEnchantmentLevel(Enchantment.SHARPNESS));
        assertEquals(1, stack.clone().getEnchantments().size());
        assertEquals(3, stack.removeEnchantment(Enchantment.SHARPNESS));
    }
}

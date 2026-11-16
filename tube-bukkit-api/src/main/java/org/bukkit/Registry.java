package org.bukkit;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Registry<T extends Keyed> extends Iterable<T> {

    Registry<Material> MATERIAL = of(Material.values(), key -> Material.matchMaterial(key.getKey()));
    Registry<EntityType> ENTITY_TYPE = of(EntityType.values(), key -> EntityType.fromName(key.getKey()));
    Registry<Enchantment> ENCHANTMENT = of(Enchantment.values(), Enchantment::getByKey);
    Registry<PotionEffectType> EFFECT = of(PotionEffectType.values(), PotionEffectType::getByKey);

    T get(NamespacedKey key);

    Stream<T> stream();

    @Override
    Iterator<T> iterator();

    default T getOrThrow(NamespacedKey key) {
        T value = get(key);
        if (value == null) {
            throw new NoSuchElementException("No value registered for " + key);
        }
        return value;
    }

    default NamespacedKey getKey(T value) {
        return value == null ? null : value.getKey();
    }

    default NamespacedKey getKeyOrThrow(T value) {
        return java.util.Objects.requireNonNull(getKey(value), "value");
    }

    static <T extends Keyed> Registry<T> of(T[] values, Function<NamespacedKey, T> lookup) {
        return new Registry<>() {
            @Override
            public T get(NamespacedKey key) {
                return key == null ? null : lookup.apply(key);
            }

            @Override
            public Stream<T> stream() {
                return Arrays.stream(values);
            }

            @Override
            public Iterator<T> iterator() {
                return Arrays.asList(values).iterator();
            }
        };
    }
}

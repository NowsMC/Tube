package space.nows.tube.server.entity;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import space.nows.tube.api.entity.LivingEntity;
import space.nows.tube.api.inventory.EquipmentSlot;
import space.nows.tube.api.inventory.ItemStack;
import space.nows.tube.server.inventory.ItemMapper;
import space.nows.tube.server.world.WorldService;

public class MinestomLivingEntity extends MinestomEntity implements LivingEntity {

    private final net.minestom.server.entity.LivingEntity living;

    public MinestomLivingEntity(net.minestom.server.entity.LivingEntity handle, WorldService worlds) {
        super(handle, worlds);
        this.living = handle;
    }

    @Override
    public net.minestom.server.entity.LivingEntity minestom() {
        return living;
    }

    @Override
    public double health() {
        return living.getHealth();
    }

    @Override
    public int fireTicks() {
        return minestom().getFireTicks();
    }

    @Override
    public void setFireTicks(int ticks) {
        minestom().setFireTicks(ticks);
    }

    @Override
    public boolean isGliding() {
        return minestom().isFlyingWithElytra();
    }

    @Override
    public double attribute(String key) {
        return minestom().getAttributeValue(attribute(key, true));
    }

    @Override
    public double baseAttribute(String key) {
        return minestom().getAttribute(attribute(key, true)).getBaseValue();
    }

    @Override
    public double defaultAttribute(String key) {
        return attribute(key, true).defaultValue();
    }

    @Override
    public void setBaseAttribute(String key, double value) {
        minestom().getAttribute(attribute(key, true)).setBaseValue(value);
    }

    private static net.minestom.server.entity.attribute.Attribute attribute(String key, boolean required) {
        net.minestom.server.entity.attribute.Attribute attribute = net.minestom.server.entity.attribute.Attribute.fromKey(key.indexOf(':') < 0 ? "minecraft:" + key : key);
        if (attribute == null && required) {
            throw new IllegalArgumentException("Unknown attribute " + key);
        }
        return attribute;
    }

    @Override
    public boolean hasLineOfSight(space.nows.tube.api.entity.Entity other) {
        if (!(other instanceof MinestomEntity target) || target.minestom().getInstance() != minestom().getInstance() || minestom().getInstance() == null) {
            return false;
        }
        net.minestom.server.coordinate.Point from = minestom().getPosition().add(0, minestom().getEyeHeight(), 0);
        net.minestom.server.coordinate.Point to = target.minestom().getPosition().add(0, target.minestom().getEyeHeight(), 0);
        return space.nows.tube.server.world.LineOfSight.clear(minestom().getInstance(), from, to);
    }

    @Override
    public void setHealth(double health) {
        living.setHealth((float) health);
    }

    @Override
    public double maxHealth() {
        return living.getAttributeValue(Attribute.MAX_HEALTH);
    }

    @Override
    public void damage(double amount) {
        living.damage(DamageType.GENERIC, (float) amount);
    }

    @Override
    public java.util.List<space.nows.tube.api.entity.effect.PotionEffect> effects() {
        return living().getActiveEffects().stream().map(timed -> {
            var potion = timed.potion();
            return new space.nows.tube.api.entity.effect.PotionEffect(potion.effect().key().asString(), potion.duration(), potion.amplifier(), potion.isAmbient(), potion.hasParticles(), potion.hasIcon());
        }).toList();
    }

    @Override
    public void addEffect(space.nows.tube.api.entity.effect.PotionEffect effect) {
        var type = net.minestom.server.potion.PotionEffect.fromKey(effect.type());
        if (type == null) {
            throw new IllegalArgumentException("Unknown potion effect: " + effect.type());
        }
        byte flags = (byte) ((effect.ambient() ? net.minestom.server.potion.Potion.AMBIENT_FLAG : 0)
                | (effect.particles() ? net.minestom.server.potion.Potion.PARTICLES_FLAG : 0)
                | (effect.icon() ? net.minestom.server.potion.Potion.ICON_FLAG : 0));
        living().addEffect(new net.minestom.server.potion.Potion(type, effect.amplifier(), effect.duration(), flags));
    }

    @Override
    public void removeEffect(String type) {
        var effect = net.minestom.server.potion.PotionEffect.fromKey(type.indexOf(':') < 0 ? "minecraft:" + type : type);
        if (effect != null) {
            living().removeEffect(effect);
        }
    }

    @Override
    public void clearEffects() {
        living().clearEffects();
    }

    private net.minestom.server.entity.LivingEntity living() {
        return (net.minestom.server.entity.LivingEntity) handle;
    }

    @Override
    public ItemStack equipment(EquipmentSlot slot) {
        return ItemMapper.fromMinestom(living.getEquipment(toMinestom(slot)));
    }

    @Override
    public void setEquipment(EquipmentSlot slot, ItemStack item) {
        living.setEquipment(toMinestom(slot), ItemMapper.toMinestom(item));
    }

    private static net.minestom.server.entity.EquipmentSlot toMinestom(EquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> net.minestom.server.entity.EquipmentSlot.MAIN_HAND;
            case OFF_HAND -> net.minestom.server.entity.EquipmentSlot.OFF_HAND;
            case HEAD -> net.minestom.server.entity.EquipmentSlot.HELMET;
            case CHEST -> net.minestom.server.entity.EquipmentSlot.CHESTPLATE;
            case LEGS -> net.minestom.server.entity.EquipmentSlot.LEGGINGS;
            case FEET -> net.minestom.server.entity.EquipmentSlot.BOOTS;
        };
    }
}

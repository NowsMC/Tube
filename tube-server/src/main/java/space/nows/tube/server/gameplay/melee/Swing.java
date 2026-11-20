package space.nows.tube.server.gameplay.melee;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public record Swing(float damage, float base, double charge, boolean critical, boolean sweeping, boolean sprinting) {

    private static final double FULL_CHARGE = 0.9;
    private static final double MIN_STRENGTH = 0.2;
    private static final double CHARGE_STRENGTH = 0.8;

    public static Swing of(Player attacker, LivingEntity target, ItemStack weapon, double charge, double attackDamage) {
        float base = (float) (attackDamage * (MIN_STRENGTH + charge * charge * CHARGE_STRENGTH));
        boolean sprinting = attacker.isSprinting() && charge > FULL_CHARGE;
        boolean critical = CriticalHit.lands(attacker, target, charge);
        if (critical) {
            base *= CriticalHit.MULTIPLIER;
        }
        float damage = base + (float) (Sharpness.bonus(weapon) * charge);
        return new Swing(damage, base, charge, critical, SweepAttack.applies(attacker, weapon, charge, critical, sprinting), sprinting);
    }

    public void landed(Player attacker, LivingEntity target, ItemStack weapon) {
        if (critical) {
            CriticalHit.show(attacker, target);
        }
        if (sweeping) {
            SweepAttack.strike(attacker, target, weapon, base, charge);
        }
    }
}

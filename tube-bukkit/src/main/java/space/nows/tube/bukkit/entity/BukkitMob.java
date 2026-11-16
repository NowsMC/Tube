package space.nows.tube.bukkit.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import space.nows.tube.bukkit.BukkitRuntime;

public final class BukkitMob extends BukkitLivingEntity implements Mob {

    public BukkitMob(BukkitRuntime runtime, space.nows.tube.api.entity.LivingEntity tube) {
        super(runtime, tube);
    }

    @Override
    public void setTarget(LivingEntity target) {
    }
}

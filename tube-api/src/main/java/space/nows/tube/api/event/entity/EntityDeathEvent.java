package space.nows.tube.api.event.entity;

import space.nows.tube.api.entity.LivingEntity;
import space.nows.tube.api.event.Event;
import space.nows.tube.api.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class EntityDeathEvent implements Event {

    private final LivingEntity entity;
    private final List<ItemStack> drops;
    private int experience;

    public EntityDeathEvent(LivingEntity entity) {
        this(entity, List.of(), 0);
    }

    public EntityDeathEvent(LivingEntity entity, List<ItemStack> drops, int experience) {
        this.entity = entity;
        this.drops = new ArrayList<>(drops);
        this.experience = experience;
    }

    public LivingEntity entity() {
        return entity;
    }

    public List<ItemStack> drops() {
        return drops;
    }

    public int experience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = Math.max(0, experience);
    }
}

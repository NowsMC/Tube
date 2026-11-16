package org.bukkit.entity;

public interface ArmorStand extends org.bukkit.entity.LivingEntity {

    void setArms(boolean p0);

    public enum LockType {
        ADDING_OR_CHANGING,
        REMOVING_OR_CHANGING,
        ADDING
    }
}

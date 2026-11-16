package org.bukkit.event.entity;

public class CreatureSpawnEvent extends org.bukkit.event.entity.EntitySpawnEvent {
    private org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason spawnReason;

    public CreatureSpawnEvent(org.bukkit.entity.LivingEntity p0, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason p1) {
        super(p0);
        this.spawnReason = p1;
    }

    public org.bukkit.entity.LivingEntity getEntity() {
        return (org.bukkit.entity.LivingEntity) entity;
    }

    public org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason getSpawnReason() {
        return spawnReason;
    }

    public enum SpawnReason {
        NATURAL,
        JOCKEY,
        CHUNK_GEN,
        SPAWNER,
        TRIAL_SPAWNER,
        EGG,
        SPAWNER_EGG,
        BUCKET,
        LIGHTNING,
        BUILD_SNOWMAN,
        BUILD_IRONGOLEM,
        BUILD_COPPERGOLEM,
        BUILD_WITHER,
        VILLAGE_DEFENSE,
        VILLAGE_INVASION,
        BREEDING,
        SLIME_SPLIT,
        REINFORCEMENTS,
        NETHER_PORTAL,
        DISPENSE_EGG,
        INFECTION,
        CURED,
        OCELOT_BABY,
        SILVERFISH_BLOCK,
        MOUNT,
        TRAP,
        ENDER_PEARL,
        SHOULDER_ENTITY,
        DROWNED,
        SHEARED,
        EXPLOSION,
        RAID,
        PATROL,
        BEEHIVE,
        PIGLIN_ZOMBIFIED,
        SPELL,
        FROZEN,
        METAMORPHOSIS,
        DUPLICATION,
        COMMAND,
        ENCHANTMENT,
        POTION_EFFECT,
        REANIMATE,
        CUSTOM,
        DEFAULT
    }
}

package space.nows.tube.server.gameplay.fishing;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import space.nows.tube.server.gameplay.Experience;
import space.nows.tube.server.loot.LootContext;
import space.nows.tube.server.loot.LootTables;
import space.nows.tube.server.mob.move.Liquids;
import space.nows.tube.server.world.effect.ParticleSpawner;
import space.nows.tube.server.world.effect.SoundPlayer;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

final class Bobber extends Entity {

    private static final String FISHING_TABLE = "minecraft:fishing";
    private static final double TICKS_PER_SECOND = 20;
    private static final double CAST_SPEED = 1.1 * TICKS_PER_SECOND;
    private static final double SCATTER = 0.0103365;
    private static final double HOOK_REACH = 0.4;
    private static final int PATH_SAMPLES = 4;
    private static final double MAX_DISTANCE_SQUARED = 32 * 32;
    private static final double SURFACE_OFFSET = 0.85;
    private static final double RISE_SPEED = 2 * TICKS_PER_SECOND;
    private static final double DIP_SPEED = -0.2 * TICKS_PER_SECOND;
    private static final double SETTLE = 0.5;
    private static final double PULL = 0.1 * TICKS_PER_SECOND;
    private static final double LOOT_LIFT = 0.08 * TICKS_PER_SECOND;
    private static final int MAX_XP = 6;
    private static final int SOLID_CATCH_DAMAGE = 2;
    private static final int FISH_DAMAGE = 1;
    private static final int ITEM_PULL_DAMAGE = 3;
    private static final int ENTITY_PULL_DAMAGE = 5;
    private static final int PICKUP_DELAY_MS = 500;

    private enum State {
        FLYING, FLOATING, HOOKED, GROUNDED
    }

    private final Player owner;
    private final int luck;
    private final Bite bite;
    private State state = State.FLYING;
    private Entity hooked;
    private Pos lastSeen;

    Bobber(Player owner, int lureLevel, int luck) {
        super(EntityType.FISHING_BOBBER);
        this.owner = owner;
        this.luck = luck;
        this.bite = new Bite(ThreadLocalRandom.current(), lureLevel);
        ((FishingHookMeta) getEntityMeta()).setOwnerEntity(owner);
    }

    void cast(Vec direction) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Vec heading = direction.normalize();
        Vec velocity = new Vec(heading.x() + random.nextGaussian() * SCATTER, heading.y() + random.nextGaussian() * SCATTER, heading.z() + random.nextGaussian() * SCATTER);
        setVelocity(velocity.mul(CAST_SPEED));
        Pos eye = owner.getPosition().add(0, owner.getEyeHeight(), 0);
        setInstance(owner.getInstance(), eye.add(heading.mul(0.3)));
    }

    boolean biting() {
        return state == State.FLOATING && bite.nibbling();
    }

    boolean grounded() {
        return state == State.GROUNDED;
    }

    Entity hookedEntity() {
        return hooked;
    }

    int reel() {
        int damage = 0;
        if (hooked != null) {
            pull(hooked);
            damage = hooked instanceof ItemEntity ? ITEM_PULL_DAMAGE : ENTITY_PULL_DAMAGE;
        } else if (state == State.FLOATING && bite.nibbling()) {
            land();
            damage = FISH_DAMAGE;
        }
        if (state == State.GROUNDED) {
            damage = SOLID_CATCH_DAMAGE;
        }
        remove();
        return damage;
    }

    @Override
    public void tick(long time) {
        if (!stillWanted()) {
            remove();
            return;
        }
        switch (state) {
            case FLYING -> fly();
            case FLOATING -> drift();
            case HOOKED -> follow();
            case GROUNDED -> setVelocity(Vec.ZERO);
        }
        super.tick(time);
    }

    private boolean stillWanted() {
        Instance level = getInstance();
        return level != null && !owner.isRemoved() && !owner.isDead() && owner.getInstance() == level
                && owner.getPosition().distanceSquared(getPosition()) < MAX_DISTANCE_SQUARED;
    }

    private void fly() {
        Instance level = getInstance();
        if (Liquids.isWater(level.getBlock(getPosition()))) {
            state = State.FLOATING;
            setNoGravity(true);
            setVelocity(getVelocity().mul(SETTLE));
            return;
        }
        Entity target = entityAlongPath();
        lastSeen = getPosition();
        if (target != null) {
            hooked = target;
            state = State.HOOKED;
            setNoGravity(true);
            ((FishingHookMeta) getEntityMeta()).setHookedEntity(target);
            return;
        }
        if (isOnGround()) {
            state = State.GROUNDED;
        }
    }

    private Entity entityAlongPath() {
        Pos now = getPosition();
        Pos from = lastSeen == null ? now : lastSeen;
        for (int step = 1; step <= PATH_SAMPLES; step++) {
            Pos at = from.add(now.sub(from).mul((double) step / PATH_SAMPLES));
            for (Entity entity : getInstance().getNearbyEntities(at, 2)) {
                boolean hittable = entity instanceof LivingEntity living && !living.isDead() || entity instanceof ItemEntity;
                if (hittable && entity != owner && !(entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR)
                        && getBoundingBox().expand(HOOK_REACH, HOOK_REACH, HOOK_REACH).intersectEntity(at.sub(0, HOOK_REACH / 2, 0), entity)) {
                    return entity;
                }
            }
        }
        return null;
    }

    private void drift() {
        Instance level = getInstance();
        Pos at = getPosition();
        if (!Liquids.isWater(level.getBlock(at)) && !Liquids.isWater(level.getBlock(at.sub(0, 1, 0)))) {
            state = State.FLYING;
            setNoGravity(false);
            return;
        }
        double surface = Math.floor(at.y()) + SURFACE_OFFSET;
        boolean submerged = Liquids.isWater(level.getBlock(at.add(0, 1, 0)));
        double rise = submerged ? RISE_SPEED : Math.max(-RISE_SPEED, Math.min(RISE_SPEED, (surface - at.y()) * TICKS_PER_SECOND));
        setVelocity(new Vec(getVelocity().x() * SETTLE, rise, getVelocity().z() * SETTLE));
        if (!submerged) {
            fish(at);
        }
    }

    private void fish(Pos at) {
        Bite.Phase finished = bite.tick();
        FishingHookMeta meta = (FishingHookMeta) getEntityMeta();
        meta.setCatchable(bite.nibbling());
        if (finished == Bite.Phase.LURING) {
            setVelocity(getVelocity().withY(DIP_SPEED));
            ParticleSpawner.spawn(getInstance(), "minecraft:fishing", at, 6, 0.2, 0.05);
            SoundPlayer.play(getInstance(), "minecraft:entity.fishing_bobber.splash", Sound.Source.NEUTRAL, at, 0.25f, 1f);
        }
    }

    private void follow() {
        if (hooked.isRemoved() || hooked instanceof LivingEntity living && living.isDead()) {
            hooked = null;
            state = State.FLYING;
            setNoGravity(false);
            ((FishingHookMeta) getEntityMeta()).setHookedEntity(null);
            return;
        }
        teleport(hooked.getPosition().add(0, hooked.getBoundingBox().height() * 0.8, 0));
        setVelocity(Vec.ZERO);
    }

    private void pull(Entity target) {
        Vec toOwner = owner.getPosition().sub(target.getPosition()).asVec();
        target.setVelocity(target.getVelocity().add(toOwner.mul(PULL)));
    }

    private void land() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Instance level = getInstance();
        LootContext context = LootContext.fishing(ItemStack.AIR, luck, OpenWater.at(level, getPosition()), random);
        for (ItemStack loot : LootTables.gameplay(FISHING_TABLE, context)) {
            Vec toOwner = owner.getPosition().sub(getPosition()).asVec();
            ItemEntity item = new ItemEntity(loot);
            item.setPickupDelay(Duration.ofMillis(PICKUP_DELAY_MS));
            item.setVelocity(new Vec(toOwner.x() * PULL, toOwner.y() * PULL + Math.sqrt(Math.sqrt(toOwner.length())) * LOOT_LIFT, toOwner.z() * PULL));
            item.setInstance(level, getPosition());
        }
        Experience.spawn(level, owner.getPosition().add(0, 0.5, 0.5), 1 + random.nextInt(MAX_XP));
        SoundPlayer.play(level, "minecraft:entity.fishing_bobber.splash", Sound.Source.NEUTRAL, getPosition(), 0.5f, 1f);
    }
}

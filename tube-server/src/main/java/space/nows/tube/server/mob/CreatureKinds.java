package space.nows.tube.server.mob;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import space.nows.tube.server.mob.kind.ambient.Bat;
import space.nows.tube.server.mob.kind.animal.Armadillo;
import space.nows.tube.server.mob.kind.animal.Axolotl;
import space.nows.tube.server.mob.kind.animal.Camel;
import space.nows.tube.server.mob.kind.animal.CamelHusk;
import space.nows.tube.server.mob.kind.animal.Cat;
import space.nows.tube.server.mob.kind.animal.Chicken;
import space.nows.tube.server.mob.kind.animal.Cow;
import space.nows.tube.server.mob.kind.animal.Fox;
import space.nows.tube.server.mob.kind.animal.Frog;
import space.nows.tube.server.mob.kind.animal.Goat;
import space.nows.tube.server.mob.kind.animal.Mooshroom;
import space.nows.tube.server.mob.kind.animal.Ocelot;
import space.nows.tube.server.mob.kind.animal.Panda;
import space.nows.tube.server.mob.kind.animal.Pig;
import space.nows.tube.server.mob.kind.animal.PolarBear;
import space.nows.tube.server.mob.kind.animal.Rabbit;
import space.nows.tube.server.mob.kind.animal.Sheep;
import space.nows.tube.server.mob.kind.animal.Sniffer;
import space.nows.tube.server.mob.kind.animal.Strider;
import space.nows.tube.server.mob.kind.animal.Turtle;
import space.nows.tube.server.mob.kind.animal.Wolf;
import space.nows.tube.server.mob.kind.animal.horse.Donkey;
import space.nows.tube.server.mob.kind.animal.horse.Horse;
import space.nows.tube.server.mob.kind.animal.horse.Mule;
import space.nows.tube.server.mob.kind.animal.horse.SkeletonHorse;
import space.nows.tube.server.mob.kind.animal.horse.ZombieHorse;
import space.nows.tube.server.mob.kind.animal.llama.Llama;
import space.nows.tube.server.mob.kind.animal.llama.TraderLlama;
import space.nows.tube.server.mob.kind.fish.Cod;
import space.nows.tube.server.mob.kind.fish.Pufferfish;
import space.nows.tube.server.mob.kind.fish.Salmon;
import space.nows.tube.server.mob.kind.fish.Tadpole;
import space.nows.tube.server.mob.kind.fish.TropicalFish;
import space.nows.tube.server.mob.kind.flier.Allay;
import space.nows.tube.server.mob.kind.flier.Bee;
import space.nows.tube.server.mob.kind.flier.HappyGhast;
import space.nows.tube.server.mob.kind.flier.Parrot;
import space.nows.tube.server.mob.kind.flier.Phantom;
import space.nows.tube.server.mob.kind.golem.CopperGolem;
import space.nows.tube.server.mob.kind.golem.IronGolem;
import space.nows.tube.server.mob.kind.golem.SnowGolem;
import space.nows.tube.server.mob.kind.monster.Endermite;
import space.nows.tube.server.mob.kind.monster.Giant;
import space.nows.tube.server.mob.kind.monster.blaze.Blaze;
import space.nows.tube.server.mob.kind.monster.breeze.Breeze;
import space.nows.tube.server.mob.kind.monster.creaking.Creaking;
import space.nows.tube.server.mob.kind.monster.creeper.Creeper;
import space.nows.tube.server.mob.kind.monster.cube.MagmaCube;
import space.nows.tube.server.mob.kind.monster.cube.Slime;
import space.nows.tube.server.mob.kind.monster.cube.SulfurCube;
import space.nows.tube.server.mob.kind.monster.enderman.Enderman;
import space.nows.tube.server.mob.kind.monster.ghast.Ghast;
import space.nows.tube.server.mob.kind.monster.guardian.ElderGuardian;
import space.nows.tube.server.mob.kind.monster.guardian.Guardian;
import space.nows.tube.server.mob.kind.monster.hoglin.Hoglin;
import space.nows.tube.server.mob.kind.monster.hoglin.Zoglin;
import space.nows.tube.server.mob.kind.monster.illager.Evoker;
import space.nows.tube.server.mob.kind.monster.illager.Illusioner;
import space.nows.tube.server.mob.kind.monster.illager.Pillager;
import space.nows.tube.server.mob.kind.monster.illager.Ravager;
import space.nows.tube.server.mob.kind.monster.illager.Vindicator;
import space.nows.tube.server.mob.kind.monster.piglin.Piglin;
import space.nows.tube.server.mob.kind.monster.piglin.PiglinBrute;
import space.nows.tube.server.mob.kind.monster.shulker.Shulker;
import space.nows.tube.server.mob.kind.monster.silverfish.Silverfish;
import space.nows.tube.server.mob.kind.monster.skeleton.Bogged;
import space.nows.tube.server.mob.kind.monster.skeleton.Parched;
import space.nows.tube.server.mob.kind.monster.skeleton.Skeleton;
import space.nows.tube.server.mob.kind.monster.skeleton.Stray;
import space.nows.tube.server.mob.kind.monster.skeleton.WitherSkeleton;
import space.nows.tube.server.mob.kind.monster.spider.CaveSpider;
import space.nows.tube.server.mob.kind.monster.spider.Spider;
import space.nows.tube.server.mob.kind.monster.vex.Vex;
import space.nows.tube.server.mob.kind.monster.warden.Warden;
import space.nows.tube.server.mob.kind.monster.witch.Witch;
import space.nows.tube.server.mob.kind.monster.zombie.Drowned;
import space.nows.tube.server.mob.kind.monster.zombie.Husk;
import space.nows.tube.server.mob.kind.monster.zombie.Zombie;
import space.nows.tube.server.mob.kind.monster.zombie.ZombieVillager;
import space.nows.tube.server.mob.kind.monster.zombie.ZombifiedPiglin;
import space.nows.tube.server.mob.kind.squid.GlowSquid;
import space.nows.tube.server.mob.kind.squid.Squid;
import space.nows.tube.server.mob.kind.villager.Villager;
import space.nows.tube.server.mob.kind.villager.WanderingTrader;
import space.nows.tube.server.mob.kind.water.Dolphin;
import space.nows.tube.server.mob.kind.water.Nautilus;
import space.nows.tube.server.mob.kind.water.ZombieNautilus;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public final class CreatureKinds {

    private static final Map<EntityType, CreatureKind> KINDS = new HashMap<>();

    static {
        for (CreatureKind kind : List.of(new Cow(), new Shulker(), new Warden(), new Breeze(), new SulfurCube(), new Creaking(), new Phantom(), new HappyGhast(), new Allay(), new Parrot(), new Bee(), new Illusioner(), new PiglinBrute(), new Piglin(), new CopperGolem(), new WanderingTrader(), new Villager(), new Fox(), new Panda(), new Strider(), new Sniffer(), new CamelHusk(), new Camel(), new Armadillo(), new Goat(), new Frog(), new Axolotl(), new Turtle(), new Tadpole(), new ZombieNautilus(), new Nautilus(), new Dolphin(), new Pig(), new Sheep(), new Chicken(), new Mooshroom(), new Ocelot(), new PolarBear(), new Rabbit(), new Wolf(), new Cat(), new Horse(), new Donkey(), new Mule(), new SkeletonHorse(), new ZombieHorse(), new Llama(), new TraderLlama(),
                new Zombie(), new Husk(), new Drowned(), new ZombieVillager(), new ZombifiedPiglin(), new Skeleton(), new Stray(), new Bogged(), new Parched(), new WitherSkeleton(), new Endermite(), new Silverfish(), new Giant(), new Spider(), new CaveSpider(), new Creeper(), new Slime(), new MagmaCube(), new Blaze(), new Ghast(), new Vex(), new Bat(), new Pillager(), new Vindicator(), new Evoker(), new Witch(), new Guardian(), new ElderGuardian(), new Hoglin(), new Zoglin(), new Ravager(), new Enderman(), new IronGolem(), new SnowGolem(), new Squid(), new GlowSquid(), new Cod(), new Pufferfish(), new TropicalFish(), new Salmon())) {
            KINDS.put(kind.type(), kind);
        }
    }

    private CreatureKinds() {
    }

    public static Optional<CreatureKind> of(EntityType type) {
        return Optional.ofNullable(KINDS.get(type));
    }

    public static Collection<CreatureKind> all() {
        return KINDS.values();
    }

    public static CompletableFuture<Void> spawn(Creature creature, Instance instance, Pos at) {
        return creature.setInstance(instance, at).thenRun(() -> creature.kind().prepare(creature, ThreadLocalRandom.current()));
    }
}

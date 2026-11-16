package org.bukkit;

import java.util.Random;

public class WorldCreator {

    private final String name;
    private final NamespacedKey key;
    private long seed;
    private World.Environment environment = World.Environment.NORMAL;
    private String generator;
    private WorldType type = WorldType.NORMAL;
    private boolean generateStructures = true;
    private String generatorSettings = "";
    private boolean hardcore;
    private boolean bonusChest;

    public WorldCreator(String name) {
        this(name, NamespacedKey.minecraft(name.toLowerCase(java.util.Locale.ROOT)));
    }

    public WorldCreator(NamespacedKey key) {
        this(key.getKey(), key);
    }

    public WorldCreator(String name, NamespacedKey key) {
        if (name == null || key == null) {
            throw new IllegalArgumentException("World name and key cannot be null");
        }
        this.name = name;
        this.key = key;
        this.seed = new Random().nextLong();
    }

    public static WorldCreator name(String name) {
        return new WorldCreator(name);
    }

    public static WorldCreator ofKey(NamespacedKey key) {
        return new WorldCreator(key);
    }

    public static WorldCreator ofNameAndKey(String name, NamespacedKey key) {
        return new WorldCreator(name, key);
    }

    public WorldCreator copy(World world) {
        seed = world.getSeed();
        environment = world.getEnvironment();
        return this;
    }

    public WorldCreator copy(WorldCreator creator) {
        seed = creator.seed;
        environment = creator.environment;
        generator = creator.generator;
        type = creator.type;
        generateStructures = creator.generateStructures;
        generatorSettings = creator.generatorSettings;
        hardcore = creator.hardcore;
        bonusChest = creator.bonusChest;
        return this;
    }

    public String name() {
        return name;
    }

    public NamespacedKey key() {
        return key;
    }

    public long seed() {
        return seed;
    }

    public WorldCreator seed(long seed) {
        this.seed = seed;
        return this;
    }

    public World.Environment environment() {
        return environment;
    }

    public WorldCreator environment(World.Environment environment) {
        this.environment = environment;
        return this;
    }

    public WorldType type() {
        return type;
    }

    public WorldCreator type(WorldType type) {
        this.type = type;
        return this;
    }

    public String generator() {
        return generator;
    }

    public WorldCreator generator(String generator) {
        this.generator = generator;
        return this;
    }

    public String generatorSettings() {
        return generatorSettings;
    }

    public WorldCreator generatorSettings(String generatorSettings) {
        this.generatorSettings = generatorSettings;
        return this;
    }

    public boolean generateStructures() {
        return generateStructures;
    }

    public WorldCreator generateStructures(boolean generate) {
        this.generateStructures = generate;
        return this;
    }

    public boolean hardcore() {
        return hardcore;
    }

    public WorldCreator hardcore(boolean hardcore) {
        this.hardcore = hardcore;
        return this;
    }

    public boolean bonusChest() {
        return bonusChest;
    }

    public WorldCreator bonusChest(boolean bonusChest) {
        this.bonusChest = bonusChest;
        return this;
    }

    public World createWorld() {
        return Bukkit.createWorld(this);
    }
}

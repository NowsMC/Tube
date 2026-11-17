# Changelog

## 0.2611.0 - 2026-11-17

First release, for Minecraft 26.2.

### Server

- Runs from a single jar and downloads its libraries on first start. (`cf2ff83`, `ba25998`)
- Reads `server.properties`, `eula.txt`, `ops.json`, the whitelist, the ban lists and the user cache. (`dfee062`, `382da4f`, `7f1336f`, `ab9f2aa`)
- Saves player data in the vanilla file layout. (`86d1a14`)
- Writes logs to `logs/latest.log` and archives older logs on start. (`f710b71`)
- Includes vanilla-style commands, operators and command permissions. (`7d20e5b`, `7f1336f`)
- Supports RCON and Query. (`7d821e8`)
- Uses epoll sockets on Linux. (`01a73fb`)
- Includes a tick watchdog, a packet rate limit and an idle timeout. (`468dd11`)

### Worlds

- Generates terrain with biomes, caves, ores, trees, seas and beaches. (`01df099`, `95bac3c`)
- Generates a Nether reached through portals. (`95bac3c`, `79d754a`)
- Also includes flat and void generators, and plugins can register their own generators. (`01df099`)
- Keeps the world time and the respawn points across restarts. (`95bac3c`, `10601d6`)

### Gameplay

- Block drops follow the vanilla loot tables, including silk touch and fortune. (`7167e04`)
- XP from ores, mobs and smelting. (`7167e04`, `81d7a3a`)
- Crafting in the inventory and on the crafting table. (`7167e04`)
- Furnaces, smokers and blast furnaces. (`7167e04`)
- Recipes are read from Mojang's server jar, which is downloaded once on first start. (`7167e04`)
- Blocks are placed facing the vanilla way: stairs, slabs, logs, doors, beds, torches, fences and double chests. (`10601d6`)
- Containers such as chests, barrels and shulker boxes keep their items. (`7167e04`)
- Doors, trapdoors, fence gates, buttons, levers and signs work. (`10601d6`)
- Combat includes armor, protection enchantments, attack cooldown, critical hits and bows. (`81d7a3a`)
- Hunger, falling, drowning, fire, lava and cactus damage. (`7167e04`, `81d7a3a`)
- Cows, pigs, sheep and chickens spawn on grass, and zombies, skeletons, spiders and creepers spawn in the dark. (`81d7a3a`)
- Beds set the respawn point and skip the night. (`10601d6`)
- Water and lava flow, sand and gravel fall, and TNT explodes. (`79d754a`)
- Crops, saplings, sugar cane and cactus grow, and leaves decay. (`79d754a`)
- Buckets, hoes, shovels, axes, bone meal and flint and steel work. (`79d754a`)

### Plugins

- Tube has its own plugin API for commands, events, the scheduler, worlds, players, inventories, items, entities, configuration (YAML, JSON and TOML), plugin channels, resource packs, skins and logins.
- Plugins can change damage, death drops and XP, block drops, explosions, crafting, mob spawns, the world time and player respawn points. (`7e86a70`, `f01c04c`)
- Bukkit plugins run through Tube's own `org.bukkit` translator. This is not the real Bukkit, and plugins that need NMS, CraftBukkit or Paper-only APIs do not work. (`66265ad`, `b640b65`)

### Not implemented yet

- The End.
- Villages and other structures.
- Redstone circuits.
- Enchanting, anvils and brewing.
- Advancements.
- Breeding, weather, boats and minecarts.
- Nether mobs.

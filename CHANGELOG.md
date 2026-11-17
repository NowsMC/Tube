# Changelog

## 0.2611.0 - 2026-11-17

First release, for Minecraft 26.2.

### Server

- Runs from a single jar and downloads its libraries on first start.
- Reads `server.properties`, `eula.txt`, `ops.json`, the whitelist, the ban lists and the user cache.
- Saves player data in the vanilla file layout.
- Writes logs to `logs/latest.log` and archives older logs on start.
- Includes vanilla-style commands, operators and command permissions.
- Supports RCON and Query.
- Uses epoll sockets on Linux.
- Includes a tick watchdog, a packet rate limit and an idle timeout.

### Worlds

- Generates terrain with biomes, caves, ores, trees, seas and beaches.
- Generates a Nether reached through portals.
- Also includes flat and void generators, and plugins can register their own generators.
- Keeps the world time and the respawn points across restarts.

### Gameplay

- Block drops follow the vanilla loot tables, including silk touch and fortune.
- XP from ores, mobs and smelting.
- Crafting in the inventory and on the crafting table.
- Furnaces, smokers and blast furnaces.
- Recipes are read from Mojang's server jar, which is downloaded once on first start.
- Blocks are placed facing the vanilla way: stairs, slabs, logs, doors, beds, torches, fences and double chests.
- Containers such as chests, barrels and shulker boxes keep their items.
- Doors, trapdoors, fence gates, buttons, levers and signs work.
- Combat includes armor, protection enchantments, attack cooldown, critical hits and bows.
- Hunger, falling, drowning, fire, lava and cactus damage.
- Cows, pigs, sheep and chickens spawn on grass, and zombies, skeletons, spiders and creepers spawn in the dark.
- Beds set the respawn point and skip the night.
- Water and lava flow, sand and gravel fall, and TNT explodes.
- Crops, saplings, sugar cane and cactus grow, and leaves decay.
- Buckets, hoes, shovels, axes, bone meal and flint and steel work.

### Plugins

- Tube has its own plugin API for commands, events, the scheduler, worlds, players, inventories, items, entities, configuration (YAML, JSON and TOML), plugin channels, resource packs, skins and logins.
- Plugins can change damage, death drops and XP, block drops, explosions, crafting, mob spawns, the world time and player respawn points.
- Bukkit plugins run through Tube's own `org.bukkit` translator. This is not the real Bukkit, and plugins that need NMS, CraftBukkit or Paper-only APIs do not work.

### Not implemented yet

- The End.
- Villages and other structures.
- Redstone circuits.
- Enchanting, anvils and brewing.
- Advancements.
- Breeding, weather, boats and minecarts.
- Nether mobs.

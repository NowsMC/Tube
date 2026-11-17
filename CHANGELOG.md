# Changelog

## 0.2611.0 - 2026-11-17

First release, for Minecraft 26.2.

### Server

- Runs from a single jar and downloads its libraries on first start. ([`cf2ff83`](https://github.com/NowsMC/Tube/commit/cf2ff8322bd32562dc087c531a7d6bc18519b8b6), [`ba25998`](https://github.com/NowsMC/Tube/commit/ba25998e68be5b774173d432e779b85c4d6e93c4))
- Reads `server.properties`, `eula.txt`, `ops.json`, the whitelist, the ban lists and the user cache. ([`dfee062`](https://github.com/NowsMC/Tube/commit/dfee0628ce79d82e23595dd35b839595f9d836c9), [`382da4f`](https://github.com/NowsMC/Tube/commit/382da4f3a61c46024bf7b4ff1e1a516f4dd0cc4b), [`7f1336f`](https://github.com/NowsMC/Tube/commit/7f1336f7e17c753a17d5ca3054c405ce7d84442c), [`ab9f2aa`](https://github.com/NowsMC/Tube/commit/ab9f2aaf4da25e91a79c622d9bec89a1a0c900ed))
- Saves player data in the vanilla file layout. ([`86d1a14`](https://github.com/NowsMC/Tube/commit/86d1a149cda9152c4f0fbeca39190fe6d9a8d4e5))
- Writes logs to `logs/latest.log` and archives older logs on start. ([`f710b71`](https://github.com/NowsMC/Tube/commit/f710b718e8066123b1eb6b66d39f2b147b67a3cd))
- Includes vanilla-style commands, operators and command permissions. ([`7d20e5b`](https://github.com/NowsMC/Tube/commit/7d20e5b0d3c4d41febd753f8810297818e1d4622), [`7f1336f`](https://github.com/NowsMC/Tube/commit/7f1336f7e17c753a17d5ca3054c405ce7d84442c))
- Supports RCON and Query. ([`7d821e8`](https://github.com/NowsMC/Tube/commit/7d821e8db8ca37414df39e99406b165ad3e075a5))
- Uses epoll sockets on Linux. ([`01a73fb`](https://github.com/NowsMC/Tube/commit/01a73fb9d7aa28b86e765c846522ccf8a4f23eba))
- Includes a tick watchdog, a packet rate limit and an idle timeout. ([`468dd11`](https://github.com/NowsMC/Tube/commit/468dd1194271b27412ec2fdac2de6a9d17f07147))

### Worlds

- Generates terrain with biomes, caves, ores, trees, seas and beaches. ([`01df099`](https://github.com/NowsMC/Tube/commit/01df0994c672ed1db3945c6d82a0483e3f5339a8), [`95bac3c`](https://github.com/NowsMC/Tube/commit/95bac3cc18de7d442ef9dd9f41f846c471045969))
- Generates a Nether reached through portals. ([`95bac3c`](https://github.com/NowsMC/Tube/commit/95bac3cc18de7d442ef9dd9f41f846c471045969), [`79d754a`](https://github.com/NowsMC/Tube/commit/79d754a6fc5f24fd07e488c2f8f8bb1837d70df8))
- Also includes flat and void generators, and plugins can register their own generators. ([`01df099`](https://github.com/NowsMC/Tube/commit/01df0994c672ed1db3945c6d82a0483e3f5339a8))
- Keeps the world time and the respawn points across restarts. ([`95bac3c`](https://github.com/NowsMC/Tube/commit/95bac3cc18de7d442ef9dd9f41f846c471045969), [`10601d6`](https://github.com/NowsMC/Tube/commit/10601d6180483a72ce88796129a04a8702f40fb8))

### Gameplay

- Block drops follow the vanilla loot tables, including silk touch and fortune. ([`7167e04`](https://github.com/NowsMC/Tube/commit/7167e0410f8c23e4afe0f85a355d5aa0c938b7be))
- XP from ores, mobs and smelting. ([`7167e04`](https://github.com/NowsMC/Tube/commit/7167e0410f8c23e4afe0f85a355d5aa0c938b7be), [`81d7a3a`](https://github.com/NowsMC/Tube/commit/81d7a3a34278071d6be7a33679e6016dc3856acd))
- Crafting in the inventory and on the crafting table. ([`7167e04`](https://github.com/NowsMC/Tube/commit/7167e0410f8c23e4afe0f85a355d5aa0c938b7be))
- Furnaces, smokers and blast furnaces. ([`7167e04`](https://github.com/NowsMC/Tube/commit/7167e0410f8c23e4afe0f85a355d5aa0c938b7be))
- Recipes are read from Mojang's server jar, which is downloaded once on first start. ([`7167e04`](https://github.com/NowsMC/Tube/commit/7167e0410f8c23e4afe0f85a355d5aa0c938b7be))
- Blocks are placed facing the vanilla way: stairs, slabs, logs, doors, beds, torches, fences and double chests. ([`10601d6`](https://github.com/NowsMC/Tube/commit/10601d6180483a72ce88796129a04a8702f40fb8))
- Containers such as chests, barrels and shulker boxes keep their items. ([`7167e04`](https://github.com/NowsMC/Tube/commit/7167e0410f8c23e4afe0f85a355d5aa0c938b7be))
- Doors, trapdoors, fence gates, buttons, levers and signs work. ([`10601d6`](https://github.com/NowsMC/Tube/commit/10601d6180483a72ce88796129a04a8702f40fb8))
- Combat includes armor, protection enchantments, attack cooldown, critical hits and bows. ([`81d7a3a`](https://github.com/NowsMC/Tube/commit/81d7a3a34278071d6be7a33679e6016dc3856acd))
- Hunger, falling, drowning, fire, lava and cactus damage. ([`7167e04`](https://github.com/NowsMC/Tube/commit/7167e0410f8c23e4afe0f85a355d5aa0c938b7be), [`81d7a3a`](https://github.com/NowsMC/Tube/commit/81d7a3a34278071d6be7a33679e6016dc3856acd))
- Cows, pigs, sheep and chickens spawn on grass, and zombies, skeletons, spiders and creepers spawn in the dark. ([`81d7a3a`](https://github.com/NowsMC/Tube/commit/81d7a3a34278071d6be7a33679e6016dc3856acd))
- Beds set the respawn point and skip the night. ([`10601d6`](https://github.com/NowsMC/Tube/commit/10601d6180483a72ce88796129a04a8702f40fb8))
- Water and lava flow, sand and gravel fall, and TNT explodes. ([`79d754a`](https://github.com/NowsMC/Tube/commit/79d754a6fc5f24fd07e488c2f8f8bb1837d70df8))
- Crops, saplings, sugar cane and cactus grow, and leaves decay. ([`79d754a`](https://github.com/NowsMC/Tube/commit/79d754a6fc5f24fd07e488c2f8f8bb1837d70df8))
- Buckets, hoes, shovels, axes, bone meal and flint and steel work. ([`79d754a`](https://github.com/NowsMC/Tube/commit/79d754a6fc5f24fd07e488c2f8f8bb1837d70df8))

### Plugins

- Tube has its own plugin API for commands, events, the scheduler, worlds, players, inventories, items, entities, configuration (YAML, JSON and TOML), plugin channels, resource packs, skins and logins.
- Plugins can change damage, death drops and XP, block drops, explosions, crafting, mob spawns, the world time and player respawn points. ([`7e86a70`](https://github.com/NowsMC/Tube/commit/7e86a70cdcf3195bc47a8b50e70effd1282d8462), [`f01c04c`](https://github.com/NowsMC/Tube/commit/f01c04c11560c8fb41bfcf608c489124d8767ca9))
- Bukkit plugins run through Tube's own `org.bukkit` translator. This is not the real Bukkit, and plugins that need NMS, CraftBukkit or Paper-only APIs do not work. ([`66265ad`](https://github.com/NowsMC/Tube/commit/66265ad883aa3fb1195c21efb17536594bc2b49c), [`b640b65`](https://github.com/NowsMC/Tube/commit/b640b6531fe113084e1bd1dc71b4755dae76a83e))

### Not implemented yet

- The End.
- Villages and other structures.
- Redstone circuits.
- Enchanting, anvils and brewing.
- Advancements.
- Breeding, weather, boats and minecarts.
- Nether mobs.

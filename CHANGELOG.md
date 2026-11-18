# Changelog

## 0.2611.3 - 2026-11-18

### Mobs

- Mobs run on Tube's own AI with vanilla's goals and priorities: they wander toward spots they like, look at nearby players, panic when hurt, stay afloat, follow players holding their food, and hunt or retaliate like vanilla. ([`3644d60`](https://github.com/NowsMC/Tube/commit/3644d60de955a8b62fd49b18321a00731e3f0931), [`a3705ba`](https://github.com/NowsMC/Tube/commit/a3705bab1c0c0706e0d82699a9cbdd4f0cc2feae), [`306170b`](https://github.com/NowsMC/Tube/commit/306170b32606fa665b4b8fb4d38de400f371917a), [`5cbd64d`](https://github.com/NowsMC/Tube/commit/5cbd64d042fdc960a53d2b64515fd8b5b6544f47))
- Mobs walk at vanilla speed; they used to move about twice as fast. Heads turn toward what they look at no faster than in vanilla. ([`1a1c2d4`](https://github.com/NowsMC/Tube/commit/1a1c2d461824a45a8d5e6963d56d2a8fec31142a))
- Zombies raise their arms and burn in daylight, skeletons shoot with vanilla's damage and accuracy per difficulty and fight up close without a bow, spiders only hunt in the dark and pounce, and creepers swell and explode like vanilla. ([`7e60ef2`](https://github.com/NowsMC/Tube/commit/7e60ef29ab126322a0f88a9701b1d9b27ed15abe), [`ea5e260`](https://github.com/NowsMC/Tube/commit/ea5e2608ae2ac396b6433d5c1e60b99d9f1d2c7f), [`c7521f4`](https://github.com/NowsMC/Tube/commit/c7521f4972b0107d86c96e701731a289d9a1850a), [`d89dbdf`](https://github.com/NowsMC/Tube/commit/d89dbdfba4ef930a384c99e4d176c88bb500d2ae))
- Cows, pigs and chickens get the warm, cold or temperate look of their biome, sheep get the wool colours of their climate, sheep regrow wool by eating grass, and chickens lay eggs and fall slowly. ([`223770d`](https://github.com/NowsMC/Tube/commit/223770d348c10850cc8d9883ef5db1866c0f12a0))
- Mobs spawned with `/summon` or by plugins get their AI too. ([`7c4e818`](https://github.com/NowsMC/Tube/commit/7c4e81890b9d0348976ef29cf84bae0390ab6b65))
- Cows, pigs, sheep and chickens breed when fed their food, babies take a parent's look, follow adults and grow up in 20 minutes (faster when fed), and some herds spawn with babies. ([`397b3ba`](https://github.com/NowsMC/Tube/commit/397b3ba461d361fb8f00d6e7479aeaaab83c7138), [`2d33e4b`](https://github.com/NowsMC/Tube/commit/2d33e4b008993a04c551a03bf2311929001453d8), [`06f82fa`](https://github.com/NowsMC/Tube/commit/06f82fa33687e5940b0c15b32baa5688747aa0b2), [`3bed18e`](https://github.com/NowsMC/Tube/commit/3bed18ecf02f922fcd7029200e66cba3c0a13a14), [`d620884`](https://github.com/NowsMC/Tube/commit/d620884e4f98839b6749bbe4014ad96ba938ca1b))
- Some zombies spawn as fast babies, and zombies and skeletons can spawn with armor and zombies with iron weapons, based on local difficulty and the moon. ([`66b57a5`](https://github.com/NowsMC/Tube/commit/66b57a5a1eb73ddb25b893cf5b37423ee33f0dd4), [`b70132b`](https://github.com/NowsMC/Tube/commit/b70132b2eeee0b588baddb411dc832f03057e3d3))
- Endermen: they get angry when a player stares at them, freeze while being looked at, teleport away from arrows, water and daylight, pick up and place blocks, and spawn at night. ([`8623fb3`](https://github.com/NowsMC/Tube/commit/8623fb3f683d0ed9f0f1ace846a2e2c3f7857bbf), [`5909adf`](https://github.com/NowsMC/Tube/commit/5909adf7c9ca58100e32f3d6701d7c2e0ea7378c), [`cbacb4c`](https://github.com/NowsMC/Tube/commit/cbacb4c385930fb676f352e2f0e72028cef8e92e))

### Commands

- Selectors take options in brackets, such as `@e[type=zombie,limit=2,distance=..10,sort=nearest]`, with `type`, `name`, `limit`, `distance` and `sort`, and tab completion offers the option names and their values. ([`7f75fbb`](https://github.com/NowsMC/Tube/commit/7f75fbb4e507b353febcd3f0758871839127f06d), [`e9e58e3`](https://github.com/NowsMC/Tube/commit/e9e58e3b629db50850523b0cdb7bc24e44e6c935))
- `@p` run by a player picks the nearest player in that player's world instead of one in another world. ([`13cfe2a`](https://github.com/NowsMC/Tube/commit/13cfe2aebf67569b7e948424e675ccdfb520e4c5))
- Coordinates complete to `~` for `/tp`, `/setblock`, `/fill`, `/clone`, `/summon`, `/spawnpoint` and `/setworldspawn`. ([`7f75fbb`](https://github.com/NowsMC/Tube/commit/7f75fbb4e507b353febcd3f0758871839127f06d), [`e9e58e3`](https://github.com/NowsMC/Tube/commit/e9e58e3b629db50850523b0cdb7bc24e44e6c935))

### Combat

- Arrows hit what they fly through within a tick, so fast arrows no longer pass through players and mobs. ([`c94b6dd`](https://github.com/NowsMC/Tube/commit/c94b6dd3cd820c99a50d469aa54776622409ab53), [`e11413a`](https://github.com/NowsMC/Tube/commit/e11413a09f0f6389e777be02c271cad77dbb15ae))

### Not implemented yet

- Villagers, zombie reinforcements, sun-avoiding paths, vanilla path costs and natural spawning, and every mob type other than cows, pigs, sheep, chickens, zombies, skeletons, spiders, creepers and endermen.

## 0.2611.2 - 2026-11-18

### Worlds

- Chunks near flower meadows are no longer missing from normal worlds. ([`e1b3a25`](https://github.com/NowsMC/Tube/commit/e1b3a25b9a5ccd6f859d8fdcd86dba97801cc870))
- A feature that cannot be read is skipped with one warning instead of leaving whole chunks empty. ([`d1a2e49`](https://github.com/NowsMC/Tube/commit/d1a2e4949e95ae17ab0e7a5d0533e0975e826195))
- Trees are no longer cut in half at chunk borders: every chunk's features are placed once and shared with its neighbours, which also makes generation faster. ([`605eb02`](https://github.com/NowsMC/Tube/commit/605eb027f9f4dab243043e655b39f9c2bd80826c))
- Worlds can play sounds and show particles, players nearby hear blocks being placed, and mobs make ambient, hurt and death sounds with a puff when they die. ([`b856cd2`](https://github.com/NowsMC/Tube/commit/b856cd2), [`a9160c0`](https://github.com/NowsMC/Tube/commit/a9160c0), [`3ded861`](https://github.com/NowsMC/Tube/commit/3ded861))

### Console

- The console keeps its input line at the bottom while logs print above it, completes commands and their arguments with Tab, shows known commands in green and unknown ones in red, remembers earlier commands, and stops the server on Ctrl+C. ([`13156dd`](https://github.com/NowsMC/Tube/commit/13156dd293d5faa99a475e984fc0af65d60edb2f), [`db6ebf9`](https://github.com/NowsMC/Tube/commit/db6ebf924bc56ebfcb76035ebbe08fa62e3604e5))
- Log levels, color codes and plugin names are colored in the console and kept plain in `logs/latest.log`. ([`8b6cfd8`](https://github.com/NowsMC/Tube/commit/8b6cfd8f8836554a4ae326a138043d762233d59b), [`663853e`](https://github.com/NowsMC/Tube/commit/663853edde78520ec52958c118b8302d09d30669), [`fbe8609`](https://github.com/NowsMC/Tube/commit/fbe8609ee171c936f217768e2ec22ab85246b3da))
- `tube.yml` sets the line format, time format, level and colors of the console and the log file separately, and the prompt and history size of the console. ([`e965d0c`](https://github.com/NowsMC/Tube/commit/e965d0c85a678f3f559e5cc721ea80760d882d70), [`8b6cfd8`](https://github.com/NowsMC/Tube/commit/8b6cfd8f8836554a4ae326a138043d762233d59b))
- The start shows the Java version and system, the default game mode and difficulty, how long the world took, the address, and `Done (…s)! For help, type "help"`. ([`20b4c54`](https://github.com/NowsMC/Tube/commit/20b4c545a76103615ef1aaf418fdb32bc26bf585))
- Logins show the address with port and the position, commands players run are logged, and an unknown command points to `help`. ([`2144557`](https://github.com/NowsMC/Tube/commit/21445571e25b99acb4d5bcf587837f392a6b4c0d), [`663853e`](https://github.com/NowsMC/Tube/commit/663853edde78520ec52958c118b8302d09d30669))

### Plugins

- `tube-plugin.yml` gains `description`, `authors`, `website`, `prefix`, `soft-depends`, `load-before`, `tube` (lowest Tube version), `minecraft`, `commands` and `permissions`. Bukkit's names (`author`, `depend`, `softdepend`, `loadbefore`) work too. ([`8f7ce65`](https://github.com/NowsMC/Tube/commit/8f7ce65), [`bdd5c7f`](https://github.com/NowsMC/Tube/commit/bdd5c7f), [`81d82ac`](https://github.com/NowsMC/Tube/commit/81d82ac))
- Tube does not download libraries for plugins: a plugin shades what it needs into its own jar, so Tube never runs jars that no one checked. A `libraries` key only logs a warning. ([`d72187a`](https://github.com/NowsMC/Tube/commit/d72187ab111e44f00c1167318f1391d0be7d0c54))
- Declared commands get their description, usage, aliases, permission and permission message; declared permissions get a default (`true`, `false`, `op`, `not-op`) and children. ([`9946081`](https://github.com/NowsMC/Tube/commit/9946081), [`9c371d7`](https://github.com/NowsMC/Tube/commit/9c371d7))
- `help` lists command descriptions, `help <command>` shows usage and aliases, and `version <plugin>` shows a plugin's details. ([`a3b5233`](https://github.com/NowsMC/Tube/commit/a3b5233), [`8f7ce65`](https://github.com/NowsMC/Tube/commit/8f7ce65))

### Commands

- Auto-fill offers every option as soon as a space is typed, for example `/gamemode `, instead of showing `[<args>]`. ([`c2ae326`](https://github.com/NowsMC/Tube/commit/c2ae3264a201434265c6618cd4a8b9d587a73f49))

## 0.2611.1 - 2026-11-17

### Server

- Shows a single "Preparing libraries..." line on first start instead of one line per downloaded library. ([`a0e20a5`](https://github.com/NowsMC/Tube/commit/a0e20a574135a6dfea001723ceb285b4985bf6e9))
- Downloads the world generation data from Mojang's server jar together with the recipes. ([`05de9a6`](https://github.com/NowsMC/Tube/commit/05de9a63e1dfe315a4a5dc88b9dea23168bedc1e))

### Worlds

- Normal worlds are generated from Minecraft's own world generation data, so terrain, biomes and caves are the same as in vanilla for the same seed. ([`21d1df7`](https://github.com/NowsMC/Tube/commit/21d1df7d14f1cd60c25a964e2f13ef20634d7de6))
- All overworld biomes, including mountains, rivers, oceans, badlands and the cave biomes. ([`b9b0ea9`](https://github.com/NowsMC/Tube/commit/b9b0ea93cbfd76144fc0bf98a682893b6530d8a6))
- Surface blocks follow each biome, with snow, sand, terracotta bands, deepslate and bedrock. ([`e5433d4`](https://github.com/NowsMC/Tube/commit/e5433d413f9d4619511194b72e36420dddf62d1e))
- Noise caves, winding caves and ravines, with underground water and lava lakes. ([`a29fa84`](https://github.com/NowsMC/Tube/commit/a29fa8455ab7d8e8d6aeb9cbadc024515b379e16), [`29d4f68`](https://github.com/NowsMC/Tube/commit/29d4f68da81c724dc6fc0fecf56d9478aa3843ae), [`eb01a55`](https://github.com/NowsMC/Tube/commit/eb01a559f2ef4da463250110e18598f2f2d31b01))
- Ores at vanilla heights and amounts, and large copper and iron veins. ([`8f445ae`](https://github.com/NowsMC/Tube/commit/8f445aea760940c75608b0ae512d7bec0e860dfb), [`2642b20`](https://github.com/NowsMC/Tube/commit/2642b20b2ea286841b284d854194c6dfe7424fa9))
- Every vanilla tree type, and trees are not cut at chunk borders. ([`39da44c`](https://github.com/NowsMC/Tube/commit/39da44cb8dd3312afca165414a46d98f1606a740), [`27ed448`](https://github.com/NowsMC/Tube/commit/27ed4486b652a8dceade58295909cb18eb9a1dc3))
- Grass, flowers, fallen logs, seagrass, kelp, snow and ice where vanilla places them. ([`c66d92a`](https://github.com/NowsMC/Tube/commit/c66d92a23c3ab846d37562fd754e3259e4167da1), [`9501f7d`](https://github.com/NowsMC/Tube/commit/9501f7d2f42805408f0dd784cc2ee55c3b4d635f))
- The spawn is placed on dry land. Without the network on the first start, normal worlds use Tube's simple terrain instead. ([`328580a`](https://github.com/NowsMC/Tube/commit/328580aa48a7866bfbb22914de4ddf8a396120dd))

### Commands

- Commands are filled in while typing in game: command names, online players, selectors, items, blocks, entities, effects and each command's options. Ids can be typed without `minecraft:`, and only commands the player may use are offered. ([`73030e6`](https://github.com/NowsMC/Tube/commit/73030e64d20290ad693284b62ff0d67d148f1e3c), [`f95da57`](https://github.com/NowsMC/Tube/commit/f95da57be0512864f9a5996a23f53d63e30dcae0))
- Bukkit plugins with a tab completer get the same auto-fill. ([`3cd1c3e`](https://github.com/NowsMC/Tube/commit/3cd1c3e151ad172433ab6515dab6da7cdaec77c3))
- New everyday commands `time`, `kill`, `clear`, `xp`, `effect`, `spawnpoint`, `setworldspawn`, `me`, `seed` and `setblock`. ([`2c05d36`](https://github.com/NowsMC/Tube/commit/2c05d368d04b391374a7dc3cb1d75ba527343656), [`a88d7b9`](https://github.com/NowsMC/Tube/commit/a88d7b9007f8ac875544cedb710cd3fea753dda8), [`572f0b6`](https://github.com/NowsMC/Tube/commit/572f0b60a5f5c3ac765c94d0ef9b7da2edae3060), [`43f515c`](https://github.com/NowsMC/Tube/commit/43f515cf4d4dd723fb119b049907ac8154833d54), [`2ee74de`](https://github.com/NowsMC/Tube/commit/2ee74de286a97305a56e9b5b52fa70801b631aa9), [`effd67f`](https://github.com/NowsMC/Tube/commit/effd67f87e8fe58740c2b148d06f47fbf95d7793), [`b588147`](https://github.com/NowsMC/Tube/commit/b58814736e84e26f0595209fa36fa7badb6398a5), [`4a838ce`](https://github.com/NowsMC/Tube/commit/4a838ce5ff5a4d9e3fdcf1dbfb603e713732f299), [`604c1a3`](https://github.com/NowsMC/Tube/commit/604c1a392eb79fdfc74b01df4d090e3d1c06d66f), [`e18d1ea`](https://github.com/NowsMC/Tube/commit/e18d1eab56bbb9ecad8788284119f8c09803cfe6))
- New server commands `difficulty`, `defaultgamemode`, `worldborder` and `tps`. Difficulty and the default game mode are saved to `server.properties`, and the world spawn to `tube-world.yml`. ([`f08202b`](https://github.com/NowsMC/Tube/commit/f08202b69a11e8c54b1b17176fd9d4a357d3c8e8), [`b94c7eb`](https://github.com/NowsMC/Tube/commit/b94c7eb2617a3692d0cf7fec91c241563f1f0371), [`8303bbb`](https://github.com/NowsMC/Tube/commit/8303bbb31d43710c23ab9f622c6b085901a42f8c), [`b7c75eb`](https://github.com/NowsMC/Tube/commit/b7c75eba87c70f7ad51a20e5c86b1492a156329f))
- New building commands `fill` and `clone`, limited to 32768 blocks like vanilla. ([`cb74109`](https://github.com/NowsMC/Tube/commit/cb74109dd7f81f7835f7310be3134f9ca5969b23), [`415e173`](https://github.com/NowsMC/Tube/commit/415e173252ebe608ea414f66ce9725429819752d))
- Player arguments accept `@a`, `@p`, `@r` and `@s`, and coordinates accept `~`. `gamemode`, `give` and `tp` can target several players. ([`a55966b`](https://github.com/NowsMC/Tube/commit/a55966b4052647889cfbbd5df1d0e11a067af3cc), [`6814b14`](https://github.com/NowsMC/Tube/commit/6814b144887ec92725809d577cfb8cb2def36de6))
- `/plugins` can also be run as `/pl` and `/plugin`. ([`7a1d35f`](https://github.com/NowsMC/Tube/commit/7a1d35f8bbc8ee9d12a848d39a42669e02bd821e))

### API

- `Command.suggest` lets plugin commands offer auto-fill, with `Suggestions.matching` for filtering. ([`f95da57`](https://github.com/NowsMC/Tube/commit/f95da57be0512864f9a5996a23f53d63e30dcae0))
- `Server` gains the difficulty, the default game mode and tick rates, and `World.setSpawn` moves the world spawn. ([`c46982c`](https://github.com/NowsMC/Tube/commit/c46982c2ae41b03f452839f6cab305f5e26925e2), [`a2bf934`](https://github.com/NowsMC/Tube/commit/a2bf934bd6f9a3314613feb9e2b9776d778ebdd4), [`0c2758d`](https://github.com/NowsMC/Tube/commit/0c2758de7aea44c9b31206fdd5f98afaad7a39b1), [`bf0edf9`](https://github.com/NowsMC/Tube/commit/bf0edf9ece46748d1cce7e3945c712498c454356))

### Not implemented yet

- Lakes, geodes, dripstone, monster rooms, underwater magma and structures in generated worlds. Trees and ores do not appear at the same positions as in vanilla.

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

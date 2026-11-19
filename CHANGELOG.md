# Changelog

## 0.2611.5 - 2026-11-20

### Plugin API

- The API packages are reorganized, so plugins built for 0.2611.4 need their imports updated. Player types (`Player`, `GameMode`, `LoginMode`, `Skin`, `ResourcePack`) are in `entity.player`, `Mob` in `entity.mob`, `PotionEffect` in `entity.effect`, and `Text`, `Chat` and `ChatText` in `text`. Events are split into `event.block`, `event.entity`, `event.inventory`, `event.player` and `event.server`. Permissions, command arguments, TCP and UDP, scoreboard teams and the tab list, and world generation have their own packages. `Difficulty` is in `world`. ([`e064f8f`](https://github.com/NowsMC/Tube/commit/e064f8f0988a6e3aae996ce0919d37ca11d1de96))
- Plugins can reach a mob's brain through `Mob.brain()`: add or remove behaviors with a priority, run them in activities, keep memories that expire, and add senses that update them on a timer. ([`8385cb2`](https://github.com/NowsMC/Tube/commit/8385cb2ed9264c7b7b1567bd6371932a40ba9dd3), [`e663e3f`](https://github.com/NowsMC/Tube/commit/e663e3f61802f0ca13538195425d43081245d847))
- `TabList.entry(id)` finds an entry a plugin added, and `Scoreboards.teamOf(entity)` finds the team of a player or entity. Tab entry names longer than 16 characters are refused, since the client disconnects on them, and a sidebar no longer lists players who went offline. ([`4b72402`](https://github.com/NowsMC/Tube/commit/4b72402b32d477cc0d113e6a993b12f1fbb7230b), [`e731f21`](https://github.com/NowsMC/Tube/commit/e731f21647415aa5b4ac1c49cb8597757f6bfc0b))
- Plugins can show scores below players' names and in the tab list with `Scoreboards.createObjective`, as numbers or hearts. Each player sees one objective per slot, as in vanilla. `Scoreboards.createPrivateTeam` makes a team that only the players it is shown to can see, so different players can see different teams. Teams made with `createTeam` are still seen by everyone. `Sidebar.remove()` takes a sidebar away from all its viewers. ([`20387ac`](https://github.com/NowsMC/Tube/commit/20387ac80732d926c423e7c728ac987c385a929d))
- A player's tab list keeps its header, footer, extra entries and changes to other players when the player rejoins, and changes made to a player who leaves and comes back are sent again. `TabList.reset()` clears all of it. ([`20387ac`](https://github.com/NowsMC/Tube/commit/20387ac80732d926c423e7c728ac987c385a929d))

### Bukkit layer

- Below-name and tab list objectives of Bukkit scoreboards are shown, and the teams of every scoreboard reach the players using that scoreboard, not only those of the main scoreboard. Teams allow friendly fire by default, as in vanilla. Scores from criteria such as `health` are not filled in by the server yet. ([`34d9df5`](https://github.com/NowsMC/Tube/commit/34d9df5673170d0c5dd0aff268d33848bb7b38a0))

### Network

- RCON closes a client that does not log in within 30 seconds and refuses an address for a minute after three wrong passwords, including on connections it had already opened. ([`cd45364`](https://github.com/NowsMC/Tube/commit/cd453641dd2e1eed094116d6faa27ef5d4c136f4), [`04e3859`](https://github.com/NowsMC/Tube/commit/04e38599e2ccd08563ea39a3baccf694e1c58821))
- If the RCON or Query port, or a port a plugin listens on, is already in use, Tube reports which address could not be bound instead of failing to start. ([`cd45364`](https://github.com/NowsMC/Tube/commit/cd453641dd2e1eed094116d6faa27ef5d4c136f4))
- When a plugin closes a TCP server, or is disabled, the connections that server accepted are closed too, so handlers of an unloaded plugin stop receiving data. A plugin channel handler registered while another one unsubscribes is no longer lost. ([`04e3859`](https://github.com/NowsMC/Tube/commit/04e38599e2ccd08563ea39a3baccf694e1c58821))
- Query keeps its handshake tokens in check under a flood of requests and no longer accepts expired ones. ([`cd45364`](https://github.com/NowsMC/Tube/commit/cd453641dd2e1eed094116d6faa27ef5d4c136f4))

### Mobs

- Every vanilla mob has a brain built on vanilla's behaviors, priorities and attributes: animals, fish, squids, golems, villagers and traders, pets, every overworld, Nether and End monster, illagers with their spells, the warden, the breeze, and the wither and ender dragon. ([`4b17a64`](https://github.com/NowsMC/Tube/commit/4b17a64460457b1ef698d9b11a8915a2478fff71), [`4458a8e`](https://github.com/NowsMC/Tube/commit/4458a8e29541a1d7a8c665b3c67a89c13e928d29), [`9e441df`](https://github.com/NowsMC/Tube/commit/9e441df2a31b218a8fb0823c4dd36db19325e767), [`8e7c584`](https://github.com/NowsMC/Tube/commit/8e7c584a35720a913f1b82b4d3327e8c91e1daa3), [`6314597`](https://github.com/NowsMC/Tube/commit/63145979e332d580b5c99afe71013fb03d1b37c5), [`53ceadc`](https://github.com/NowsMC/Tube/commit/53ceadcb75dadacf3ac50e5f6cbdb747151d41e2), [`6977559`](https://github.com/NowsMC/Tube/commit/6977559156eff4adc2aa0d03708abd584d244b32), [`edb8dca`](https://github.com/NowsMC/Tube/commit/edb8dca6dd7382c3697300e50880d97f308b850d), [`2e73adc`](https://github.com/NowsMC/Tube/commit/2e73adcbe48b2a7730c8b22f2a544392e5ff5118), [`fcd0a30`](https://github.com/NowsMC/Tube/commit/fcd0a30c76f64a4f209088e2e4aa7ab2429f26a3))
- Goal priorities and attributes were checked against vanilla 26.2 and corrected where they differed. ([`e66b44c`](https://github.com/NowsMC/Tube/commit/e66b44c06c9e1c30b3949a9bcd4992a7a4521713), [`01cf0d2`](https://github.com/NowsMC/Tube/commit/01cf0d292d2cff8abd12f586c78648b6289f301f))
- Mobs speed up gradually like vanilla instead of moving at full speed at once, and stop wandering when no player has been near for a while. ([`c5f4867`](https://github.com/NowsMC/Tube/commit/c5f486790f06ca015ecaafc8a9e564cfd8c67fa0), [`3569d9c`](https://github.com/NowsMC/Tube/commit/3569d9cd03b10f5fcd069cd143a6683d929dbad6))
- Mobs despawn by vanilla's rules. Before, every mob was removed when no player was online. ([`4ca95ed`](https://github.com/NowsMC/Tube/commit/4ca95edd9fcb722ae923fe03ff307afa5372fa6b))
- Mobs no longer read or walk into unloaded chunks, which used to crash the server near the edge of the loaded world. ([`8bca6f9`](https://github.com/NowsMC/Tube/commit/8bca6f981e899d81bb80a76248d03732806ee41f), [`2f44e8b`](https://github.com/NowsMC/Tube/commit/2f44e8b3fd11226d83654ec20d72fd299caac28d))
- Wolves, cats, frogs, pigs, cows and chickens get the variant vanilla picks for their biome. ([`e6a9d4a`](https://github.com/NowsMC/Tube/commit/e6a9d4a89d01ab3ac0c243af2f9470f300cc00ed), [`48d1c2b`](https://github.com/NowsMC/Tube/commit/48d1c2b0f6c3f2930529e89f0d52fb5dcde84f58))
- Players can tame wolves, cats, parrots and other pets and order them to sit, and pets follow their owner and defend them. ([`c8e2c12`](https://github.com/NowsMC/Tube/commit/c8e2c121d0cd3a7c14c8c776c7f7815f9bc0da09), [`6e2d3bc`](https://github.com/NowsMC/Tube/commit/6e2d3bcc59cc127cf62e2ef51ed6aa119cc05d22))
- Fireproof mobs ignore fire and lava, and piglins and hoglins outside the Nether turn into their zombie forms. ([`aeac63b`](https://github.com/NowsMC/Tube/commit/aeac63bafa03a08147e2591b5af2275df740742a), [`d48c80d`](https://github.com/NowsMC/Tube/commit/d48c80d9b19b9fab1903e6fecc742bfec4bdf13a))

### Combat

- Arrows can carry a potion effect, as from strays, bogged and parched, and witches throw splash potions. ([`4816e20`](https://github.com/NowsMC/Tube/commit/4816e207913b6c029dcbdfd3f705876637432660), [`a3f2d8a`](https://github.com/NowsMC/Tube/commit/a3f2d8a650e4650bcb1dd90625e64eb11b5b7083))

### Internals

- Registry tags, sound pitch and playback, entity status effects and world events are each handled in one shared place instead of many copies. ([`a485f5f`](https://github.com/NowsMC/Tube/commit/a485f5f457470430106ad74ddeb1feedce901c5c), [`ee93f4a`](https://github.com/NowsMC/Tube/commit/ee93f4a0e5c47f3f07b98b47970dbd9437180838), [`0348735`](https://github.com/NowsMC/Tube/commit/0348735f631b4af0004415ec9e494e8f54bf0333))

## 0.2611.4 - 2026-11-18

### Builds

- The Snapshot workflow, started by hand from the Actions tab, deploys the `-SNAPSHOT` version to files.nows.space under `snapshots/tube` and keeps the server jars as a workflow artifact for 14 days, so fixes between releases do not need a new version.
- Every push to `main` and every pull request is built and tested by the CI workflow. ([`0ccdad7`](https://github.com/NowsMC/Tube/commit/0ccdad7defb2f85e290960cb3cf9f2f21f70c2c2))

### Plugin API

- Entities report whether they are sneaking, sprinting, swimming, glowing or gliding, how long they will burn, and where their eyes are. `Player.openedInventory()` shows the container a player has open, and `World.nearbyEntities` finds entities in a box around a point. ([`59b9246`](https://github.com/NowsMC/Tube/commit/59b9246a7a3b618b5ae39eddbc7df970126e24b2), [`3b13912`](https://github.com/NowsMC/Tube/commit/3b13912d2549fc0a8b4d35aa131efeaacfab9463))
- `PlayerSneakEvent`, `PlayerSprintEvent` and `EntityGlideEvent` tell plugins when the state of a player or entity changes, and `Server.protocolVersion()` gives the protocol version the server speaks. ([`4f39589`](https://github.com/NowsMC/Tube/commit/4f39589bbd2ae816b00d6d71d6845096ec728bba))
- `Scheduler.region()` and `Scheduler.entity(entity)` run tasks at a place or for an entity. An entity task stops and calls its retired callback when the entity is gone. Tube runs them on its one scheduler, so they are the shape a Folia port needs without the threads. ([`8ec4b4b`](https://github.com/NowsMC/Tube/commit/8ec4b4be15d7193d2c69366913b9767118a68a77))
- Living entities report and change attributes by key (`attribute`, `baseAttribute`, `defaultAttribute`, `setBaseAttribute`), and `hasLineOfSight` tells whether a wall is in the way. A player can hide another player from themselves with `hidePlayer` and `showPlayer`, and `openBook` shows a book. ([`5d21924`](https://github.com/NowsMC/Tube/commit/5d21924c4171dda800a195b6546264a4afbafd12), [`6cfa817`](https://github.com/NowsMC/Tube/commit/6cfa817b38ef7c74c5ecbcdaefefe97e3d0cb0a7))
- Plugins can show a sidebar to chosen players (`Server.scoreboards().createSidebar`), create teams with a prefix, suffix, colour, name tag visibility, collision rule and members, and shape a player's tab list through `Player.tabList()`: header and footer, display name, order, latency and listing of players, and extra entries with their own name, skin and game mode. ([`d4ac050`](https://github.com/NowsMC/Tube/commit/d4ac0501945e2d1f03c3667d7ad20c81ddb8ac17), [`a04f828`](https://github.com/NowsMC/Tube/commit/a04f828f6929bb48521826f5f0d8520ed5e4f6e7), [`744238e`](https://github.com/NowsMC/Tube/commit/744238ec35aec1bcd8445f2ce9d855cc31df4e33), [`0c17432`](https://github.com/NowsMC/Tube/commit/0c174325d8e3e67cc81a1b065a2f1f5f902c5199))

### Bukkit layer

- Bukkit plugins can use `Player.isSneaking`, `isSprinting`, `getEyeLocation`, `getOpenInventory`, `isGlowing`, `getFireTicks`, `World.getNearbyEntities`, `getGameRuleValue`, `Bukkit.getItemFactory`, `Location.subtract(Location)`, `Block.getFace`, `Material.getEquipmentSlot`, `PlayerToggleSneakEvent`, `PlayerToggleSprintEvent`, `EntityToggleGlideEvent` and `UnsafeValues.getProtocolVersion`. Legacy numeric block and item ids are still not available. ([`5c3977e`](https://github.com/NowsMC/Tube/commit/5c3977e9802383c60ffe7b8e1789fefb28647665), [`a8743d0`](https://github.com/NowsMC/Tube/commit/a8743d01c5daadfdaa6bf01344f9774107acfd62), [`e41b1f3`](https://github.com/NowsMC/Tube/commit/e41b1f3df328c0dad6221ea886d4cd4b38358033), [`cc64151`](https://github.com/NowsMC/Tube/commit/cc6415142c7d4cd8c60c8e1ef35f1d22995ba010), [`7a36169`](https://github.com/NowsMC/Tube/commit/7a3616919037db4328f4f0b87505acf8bc80aba9))
- Bukkit plugins can keep metadata on entities, read and change attributes through `Attribute` and `getAttribute`, call `hasLineOfSight`, hide and show players, and ask for `Bukkit.getTPS`, `getAverageTickTime` and `getMinecraftVersion`. ([`d432147`](https://github.com/NowsMC/Tube/commit/d432147c43fb5fe5f74b82892dd7192989b750fd), [`91950eb`](https://github.com/NowsMC/Tube/commit/91950ebf93c83bd1ddb4ec72ff1cf3af70d0f5af), [`523da1c`](https://github.com/NowsMC/Tube/commit/523da1cd22b8f89588f2f365774883fa915ed789), [`026b65c`](https://github.com/NowsMC/Tube/commit/026b65c1b606bdf71484649263f05d06ec8c28f2))
- Bukkit plugins can use scoreboards: `Bukkit.getScoreboardManager()`, objectives with scores, the sidebar, teams with prefix, suffix, colour and options, `Player.getScoreboard` and `setScoreboard`, and `Player.setPlayerListName`. The sidebar and the teams of the main scoreboard are shown through Tube. Below-name and tab list objectives, and the teams of other scoreboards, are kept but not shown yet. ([`c97e699`](https://github.com/NowsMC/Tube/commit/c97e699b7b7e36a68df03f6f0da5ef0bd1df3384), [`ca2d27d`](https://github.com/NowsMC/Tube/commit/ca2d27d7d493bd180434ef91059b32169a7618a6), [`bb59436`](https://github.com/NowsMC/Tube/commit/bb5943633649bc794574c9253aaacf0b0621a226), [`618fc83`](https://github.com/NowsMC/Tube/commit/618fc830ecfe82f0de84bc17d24da236ecf60bf4))

### Chat

- Plugins can build chat text with colours, hover text and click actions through `Chat` and `ChatText` in the API: copy to the clipboard, open a web link (http and https only), run or suggest a command, and insert text on shift click. `Chat.linkify` turns the links inside a text into clickable ones. ([`4bec423`](https://github.com/NowsMC/Tube/commit/4bec42392d5446662f4f6310b876771e835f0854))
- A player's name in chat can be clicked to start a private message, and a plugin can set the whole chat line with `PlayerChatEvent.setFormatted`. What players type is still plain text. ([`1a7a38a`](https://github.com/NowsMC/Tube/commit/1a7a38a716c7f85a2a23f2f0d5c17684a58934ed))
- A whisper shows the sender's name as a click to reply. ([`2e3c3bd`](https://github.com/NowsMC/Tube/commit/2e3c3bd1c5a39be090ec6f058007aa2e11c9a200))

### Loot

- Drops from blocks, mobs, explosions and fishing are rolled by one engine that reads the vanilla loot tables, so every system that gives items can share it. It replaces the single class that did this before and the separate fishing code. ([`ca99bc8`](https://github.com/NowsMC/Tube/commit/ca99bc83a51962e57e1a11b501911cecee42cb98), [`a5c2d53`](https://github.com/NowsMC/Tube/commit/a5c2d53d60514919324e8d80b3bd448d7f22b5f9), [`c6f89d2`](https://github.com/NowsMC/Tube/commit/c6f89d2b0f88d3300f9bf1f836b9067d10674d0d), [`7fac211`](https://github.com/NowsMC/Tube/commit/7fac21106ae4e1a6aa775c79bc46700e59fc034b))

### Items

- Fishing rods can be cast and reeled in. The bobber floats on water, dips with a splash when something bites, and reeling in during a bite catches fish, junk or treasure from the vanilla fishing loot tables: Lure shortens the wait, Luck of the Sea shifts the odds, treasure needs open water, and a catch gives 1 to 6 experience. Reeling in a bobber hooked to a mob or an item pulls it toward the player. A reel costs durability as in vanilla: 1 for a catch, 2 on the ground, 3 for an item and 5 for a mob. Enchanted treasure gets one random enchantment. ([`ecabfb5`](https://github.com/NowsMC/Tube/commit/ecabfb521aa65732bc5f367184622bd8209b10cd), [`aadb395`](https://github.com/NowsMC/Tube/commit/aadb3953116451784e66ad5191d4729252d91b6d))
- Spawn eggs spawn their mob when used on a block, and use up one egg outside creative mode. Mobs Tube has a brain for behave as usual, and the others appear without one. ([`be158f7`](https://github.com/NowsMC/Tube/commit/be158f7e71b6ff0838902168a898d20036c50b85))

### Mobs

- Mobs and players that overlap push each other apart instead of standing inside one another. ([`0d8ecfb`](https://github.com/NowsMC/Tube/commit/0d8ecfb50d7426a67425e767c48e5723d7a23efe))
- Cod and salmon spawn in schools in oceans and rivers, swim around, and die when they end up out of the water. ([`574c936`](https://github.com/NowsMC/Tube/commit/574c936dadced345ff927cf6b2e000b3b603746c), [`8a05669`](https://github.com/NowsMC/Tube/commit/8a05669c48d40fc51489d5622857ab8be159861b))

### Combat

- Arrows shot from a bow fly instead of dropping in front of the player: the client is now told the arrow's speed when the arrow appears. Arrows leave the bow along the crosshair as in vanilla, without the upward lift that made them arc too high, and skeletons keep vanilla's aim. ([`bfae984`](https://github.com/NowsMC/Tube/commit/bfae984389f369fa97e8704a8c3530324d042783))
- Snowballs, eggs and ender pearls can be thrown. Snowballs and eggs knock back what they hit, an egg hatches a chick one time in eight, and an ender pearl takes the thrower to where it lands for 5 damage, with a one second wait before the next one. ([`8928592`](https://github.com/NowsMC/Tube/commit/8928592aeb5fa6b968e81c82b4a7a0db7196f60e))

### Commands

- A player who is made an operator, or loses it, or is granted or denied a permission, sees the right commands at once instead of after rejoining. Operators also get the operator level on the client. ([`19eabf3`](https://github.com/NowsMC/Tube/commit/19eabf31b2e353a65b7033a5695139f6c55c0dad))
- Commands that a plugin registers or unregisters while players are online show up in their command list without rejoining. ([`1cc3047`](https://github.com/NowsMC/Tube/commit/1cc3047be8e5e5513854348d7c072e8b6699e230))

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
- `/weather` sets clear, rain or thunder, with an optional duration in ticks, seconds (`s`) or days (`d`). ([`7153873`](https://github.com/NowsMC/Tube/commit/715387355fa4111b55590107b3431edb4261c7d4), [`b32f12e`](https://github.com/NowsMC/Tube/commit/b32f12ee166bb517f7e4eb9a0c358f88e0cb8d56))
- `/gamerule` reads and sets `doDaylightCycle`, `keepInventory` and `mobGriefing` per world, and the values are saved with the world. Other vanilla rules are not supported yet. ([`7153873`](https://github.com/NowsMC/Tube/commit/715387355fa4111b55590107b3431edb4261c7d4), [`d8bb45d`](https://github.com/NowsMC/Tube/commit/d8bb45d354341c609264b854bb8b8054f02d8fe7), [`b32f12e`](https://github.com/NowsMC/Tube/commit/b32f12ee166bb517f7e4eb9a0c358f88e0cb8d56))
- `/tag` adds, removes and lists entity tags, and selectors can filter with `tag=`. ([`221d70d`](https://github.com/NowsMC/Tube/commit/221d70d712470fa1c121615f99eefe32fc909d39), [`5a26ced`](https://github.com/NowsMC/Tube/commit/5a26ced450423634a00042d8334944c421393f6d))
- `/playsound` and `/particle` play a sound to players or show a particle in the world. ([`a8ac923`](https://github.com/NowsMC/Tube/commit/a8ac923e23cd71c987e1dca22fc881363df87050))
- `/spreadplayers` scatters players and entities on safe ground around a point, a minimum distance apart. ([`c19009d`](https://github.com/NowsMC/Tube/commit/c19009da4b180239673279983ba53d3f617a7517))
- `/save-on` and `/save-off` switch autosaving, and `/setidletimeout` changes how long players may stand idle and saves it to `server.properties`. ([`66da44c`](https://github.com/NowsMC/Tube/commit/66da44c665c7930102037e3a5a66f10523769ccc), [`b671691`](https://github.com/NowsMC/Tube/commit/b67169148e131b55a5938136e8f1fff0ed886dc4))
- `/reload` rereads the whitelist and the ban lists and tells plugins with a `ServerReloadEvent` so they can reread their own settings. `/reload plugins` disables and enables every Tube plugin again from disk, and `/reload all` does both. Plugins loaded by an extension, such as Bukkit plugins, are not reloaded by `/reload plugins`. ([`37eef8c`](https://github.com/NowsMC/Tube/commit/37eef8c3db528e43b0df8194c1ec1503c5aa87f1))
- `/restart [reason]` stops the server cleanly and starts it again. It needs a launcher: `tube run` starts the server again by itself, and a script can do the same by passing `-Dtube.restart.exit=<code>` and starting the server whenever it exits with that code. Without one the command says so and does nothing. ([`9baa66a`](https://github.com/NowsMC/Tube/commit/9baa66afa617fcfe4774f39801fdb3717c4ddd71))
- `/stop [reason]` and `/restart [reason]` tell players why they are being disconnected. ([`9baa66a`](https://github.com/NowsMC/Tube/commit/9baa66afa617fcfe4774f39801fdb3717c4ddd71))
- `/version` also shows the data version, Java and the number of plugins, and `/version <plugin>` lists what a plugin depends on and how many commands it declares. ([`941a94e`](https://github.com/NowsMC/Tube/commit/941a94e288068f478c88460ad1e3381463f71c15))
- `/kick` and `/ban` accept selectors such as `@a` and `@r`. ([`b36544b`](https://github.com/NowsMC/Tube/commit/b36544b6e7bf23dceeabf06c4e4c980c6b9e91fc))
- `/plugin` is gone; use `/plugins` or `/pl`. ([`eaa9f5d`](https://github.com/NowsMC/Tube/commit/eaa9f5d9ec4b84583b4206ecafab0419f5fbbb35))

### Plugin API

- Plugin commands can run other commands with `CommandRegistry.dispatch`, look commands up with `find`, and use the console through `Server.console()`. ([`34228e1`](https://github.com/NowsMC/Tube/commit/34228e1fe363ba8be67a6f4af80d32ffbfee0f7a))
- A command can throw `CommandFailure` to show a message to the sender. Any other error in a command is logged and reported to the sender instead of escaping. ([`0d15c15`](https://github.com/NowsMC/Tube/commit/0d15c155139a0d8db0afd0a66e364b9fa6818c4c), [`652ff5e`](https://github.com/NowsMC/Tube/commit/652ff5ed8d37f31b5acdf9ef6d4e8810c70756b5))
- Argument reading (`CommandInput`), coordinates (`Positions`), target selectors (`Server.targets()`) and the names of blocks, items, entities, effects, sounds and particles (`Server.registries()`) are part of the API, so plugin commands accept `@a[type=zombie]` and `~ ~ ~` like the built-in ones. ([`0d15c15`](https://github.com/NowsMC/Tube/commit/0d15c155139a0d8db0afd0a66e364b9fa6818c4c), [`b83e2f3`](https://github.com/NowsMC/Tube/commit/b83e2f3d19d7ab662bc8638104a0d2139dfee10a))
- A plugin command whose name is already taken is registered as `plugin:command` instead of stopping the plugin from enabling, and every plugin command can be reached that way. Taken aliases are skipped with a warning. ([`367554f`](https://github.com/NowsMC/Tube/commit/367554fa378fa7c9528efa3713ab00f23b87a176))
- A plugin can only unregister the commands it registered itself. ([`367554f`](https://github.com/NowsMC/Tube/commit/367554fa378fa7c9528efa3713ab00f23b87a176))

### Blocks

- Breaking a plant or placing a block next to one no longer removes the plants around it. Grass, flowers, saplings, dead bushes, sugar cane and cactus now stay on every soil vanilla allows, such as grass blocks, podzol, mycelium and moss. ([`cae15e6`](https://github.com/NowsMC/Tube/commit/cae15e6320d6fdd014432f3dc93584a73cc1d17d))
- Middle click picks a block in creative mode: it selects the item if it is in the hotbar, and otherwise puts it in the first free hotbar slot. In survival it brings an item you already carry to the hotbar. ([`bb9a1f6`](https://github.com/NowsMC/Tube/commit/bb9a1f6a18b192576ccf6fc83720fe8451181f8c))

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

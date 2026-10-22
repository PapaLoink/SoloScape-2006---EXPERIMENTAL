# Content audit (non-quest)

This audit compares the server with the **LostCityRS/Content** repository at the May 2, 2006 commit
[`4949e61`](https://github.com/LostCityRS/Content/tree/4949e619a53f40c0fe83395df89bd35ff1f044f4), which is the closest
period-accurate reference available. It lists gaps for contributors to work on. See [ROADMAP.md](ROADMAP.md) for quests.

## How reliable is this?
- The reference is a **preservation reconstruction**, not Jagex's original data. Where it disagrees with the 443 cache or
  with period evidence, the period evidence wins.
- NPC and item ids in the reference line up with this server for most NPCs (about 85 to 95 percent matched by name in the ranges checked), so spawn comparisons use the NPC id or name.
- Code searches below count references in the server source. A hit can be a dialogue line or a guide entry and not a working feature,
  and no hits can mean the feature is implemented under a different name. Treat every line as a lead to verify in the game, not a verdict.

## 1. NPC spawns (the biggest gap)
| | Count |
|---|---|
| Spawns in the reference maps | 11,666 |
| Spawns in the server (`Npc spawn.dat`) | 4,942 |
| Reference spawns with no spawn of the same NPC (by id or name) within 24 tiles on the same plane | **6,364** |

Examples of the same area in the reference and the server: Lumbridge map square (50,50) has 99 spawns in the reference and 51 in the server; the square covering the Varrock area (50,52) has 40 and 9.
Some NPCs may be spawned by quest or minigame code instead of the spawn file, so a missing row is not always missing in game.

Most-affected kinds: rats, butterflies, spiders, ducks, will o' the wisps, shadows, mummies, giant bats, skeletons, rabbits, guards, gulls, pirates,
plus many dungeon monsters. About a third of the missing rows are underground.

**Data:** [docs/audit/npc_spawn_gaps.csv](docs/audit/npc_spawn_gaps.csv) lists every missing spawn with region, NPC id, name, x, y and plane.
Adding spawns means appending to `Server/data/npcs/Npc spawn.dat` (a binary file) or spawning them in code with `GameplayHelper.spawnNpc`.
Fishing spots, rocks, boulders and eggs in the CSV are scenery-like NPCs and are usually handled by the skill code instead.

**Status update:** 2,290 of these (overground, not quest-gated) have been imported - see [NPC-SPAWNS.md](NPC-SPAWNS.md). The other 4,074 were held back (quest NPCs that should only appear during a quest, dungeons, special areas) and are listed with reasons in [docs/audit/npc_spawns_held_back.csv](docs/audit/npc_spawns_held_back.csv) for a later pass.

## 2. Ground item spawns
Reference ground items: 1,347. Server (`Item spawn.dat`): 1,261. Reference items with no matching spawn within 3 tiles: **239**
(for example bones, coins, buckets, rocks, broken glass, seaweed, hammers). This area is mostly covered.
**Data:** [docs/audit/item_spawn_gaps.csv](docs/audit/item_spawn_gaps.csv).

## 3. NPC drops
The reference has drop tables for 80 NPC groups. By name, the server has a drop table for all of them except Grip and the TzHaar-Ket and TzHaar-Xil
(the TzHaar difference may only be spelling, because this cache names them with a hyphen).
**Not compared:** the actual items, quantities and rates. Verifying those against period sources is a good task.

## 4. Shops
The server has 202 shops. The reference's shop data comes from a later revision (its unpacked inventories are from 727), so it cannot be used
to check 2006 stock. Use period sources, such as archived wiki shop pages, for any change.

## 5. Minigames
| Minigame | In the 2006 reference | Server |
|---|---|---|
| Castle Wars | yes | code present |
| Duel Arena | yes | code present |
| Party Room | yes | code present |
| Mage Arena | yes (area) | code present |
| Fight Caves, Barrows | not checked in the reference | code present |
| Pest Control | folder exists but is empty | no dedicated code found (only a mention in cache revision notes) |
| Fishing Trawler | yes (14 script files) | no dedicated code found (dialogue and skill-guide mentions only) |
| Gnome Ball | yes (7 files) | no dedicated code found (guide and clue-question mentions only) |
| Agility Arena (Brimhaven) | yes (4 files) | no dedicated code found (skill-guide mentions only) |
| Ranging Guild minigame | yes (10 files) | only the entry level check was found |
| Champion's Challenge | folder exists but is empty | not checked |
| Board games (Draughts, Runelink and others) | yes (5 files) | none found |

## 6. Random events
The reference has these macro events: big fish, whirlpool, river troll, candlelight, drunken dwarf, genie, lost and found, mysterious old man, strange box, swarm,
triffid, gas (poisonous gas), lost pickaxe, rock golem, shade, zombie, watchman, dryad, ent, lost axe, plus prayer, thieving, mining, fishing and woodcutting variants.

The server has: Drunken Dwarf, Genie, Rick Turpentine, Dr Jekyll, Sandwich Lady, and skill events for Zombie, Shade, Tree Spirit, Rock Golem, River Troll and Evil Chicken.
No code was found for: Mysterious Old Man, Strange Box, Triffid, Lost and Found, Dryad, Big Fish, Poisonous Gas. Check release dates before adding: some server events (Sandwich Lady, Rick Turpentine, Dr Jekyll)
may be later than 2006, and some reference events may be as well.

## 7. Skills
The reference has scripts for the features below. "No code found" means a keyword search of the server source returned nothing; verify in game.

| Skill | Reference features | Leads |
|---|---|---|
| Cooking | cakes, pies, pizza, stew, potatoes, wine, kebabs, oomlie bird meat, swamp toad, gnome cooking, cooking guild | no code found for oomlie bird meat or the Cooking Guild |
| Fletching | arrows, bolts, bows, darts, ogre arrows, cutting logs | ogre arrow references are minimal; verify |
| Agility | gnome, barbarian and wilderness courses, shortcuts | shortcuts exist; no course-specific code found by name, check each course in game |
| Smithing | smelting, cannonballs, dragon square shield | cannonballs appear only in a skill-guide entry |
| Magic | alchemy, charge, enchant, superheat, telekinetic grab, bones conversions, teleports | code present |
| Slayer | aberrant spectre, abyssal demon, banshee, basilisk, cockatrice, desert lizard, gargoyle, infernal mage, nechryael, rockslug | monster references present; task data not compared |
| Crafting, Fishing, Herblore, Mining, Prayer, Runecrafting, Thieving, Woodcutting, Firemaking | broad coverage | code present; per-activity checks not done |
| Farming | not in the 2006 reference | server has farming code; check whether it belongs in this revision |

## Suggested next audits
- Compare combat stats, aggression and respawn of monsters against the reference NPC configs.
- Compare object-based content (doors, ladders, stairs) in the reference map `LOC` sections with what is usable in game.
- Compare music, emotes and skill capes against the cache.

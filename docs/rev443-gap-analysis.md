# Revision 443 gap analysis

Target: RuneScape as of revision 443 (18 December 2006).
Compiled 23 October 2026 from release dates on the [OSRS wiki](https://oldschool.runescape.wiki/), compared with this server's code.

## How this was checked

- **Wiki side:** release dates come from [Quests/Release dates](https://oldschool.runescape.wiki/w/Quests/Release_dates) and the `release` field of every page in the Minigames, Bosses, Guilds, Skills, Dungeons and Random events categories. Anything dated on or before 18 Dec 2006 counts as in scope.
- **Server side:** quests come from the registry in `QuestDefinition.java`. Everything else was found by searching the server code for names, which can miss features that use different names. Lines marked *verify* need a look in game.
- **Limits of the wiki:** the OSRS wiki only covers content that was still in the game in August 2007. Its pages describe the August 2007 version, so anything changed between January and August 2007 needs a 2006-dated source.
- **Items, NPCs and objects** come from the revision 443 cache, so their definitions are already correct. What can be missing is behaviour: spawns, shops, drops and interactions.

## Summary

| Area | In scope by 18 Dec 2006 | On this server | Missing |
|---|---|---|---|
| Quests | 116 | 38 | **78** |
| Skills | 23 | 21 | **Construction** (31 May 2006), **Hunter** (21 Nov 2006) |
| Minigames | 23 | about 8 | about 15 |
| Guilds | 11 | 9 (*verify*) | **Warriors' Guild** (13 Jun 2006); Servants' Guild comes with Construction |
| Overground NPC spawns | — | 2,290 added 22 Oct; 435 surplus spawns removed 25 Oct (see below) | 4,074 held back (quest, dungeon and special-area NPCs) in `docs/audit/npc_spawns_held_back.csv` |
| Post-cutoff content on the server | — | God Wars Dungeon (June 2007), Grand Exchange (Nov 2007), dragon claws in bot loadouts (2007) | Decide whether to keep these |

### NPC spawn trim (25 Oct)

The upstream `Npc spawn.dat` had piles of extra NPCs, such as 26 Khazard troopers in West
Ardougne where LostCity's 2004 data has 4. LostCity's May 2006 maps have about the same
number of NPCs per square as 2004, so these were not 2005-06 additions. The reference is
LostCity's revision 274 spawns, which is also the file osrs.world shows for every cache
up to revision 474 (`npc-spawns-2004.json`). For every NPC and map square where we had
more than that, the spawns nearest to LostCity's positions were kept and the rest removed:
435 spawns (431 by that rule, plus 4 scorpions removed earlier at the zoo pit). Three
zoo scorpions were also moved onto LostCity's tiles. Id 206 was skipped because it is a
Dwarf in 2004 but a Guard in revision 443. Every removed spawn is listed in
`docs/audit/npc_spawns_removed_2026-10-25.csv`. There are no exact duplicates (same NPC
on the same tile).

## Quests (78 missing)

The server has these 38: Cook's Assistant, Demon Slayer, The Restless Ghost, Romeo & Juliet, Sheep Shearer, Shield of Arrav, Ernest the Chicken, Vampyre Slayer, Imp Catcher, Prince Ali Rescue, Doric's Quest, Black Knights' Fortress, Witch's Potion, The Knight's Sword, Goblin Diplomacy, Pirate's Treasure, Dragon Slayer I, Druidic Ritual, Lost City, Witch's House, Merlin's Crystal, Heroes' Quest, Scorpion Catcher, Family Crest, Monk's Friend, Clock Tower, Holy Grail, Tree Gnome Village, Sheep Herder, Plague City, Jungle Potion, The Grand Tree, Gertrude's Cat, Rune Mysteries, Elemental Workshop I, Priest in Peril, The Fremennik Trials and Monkey Madness I.

### Group A: LostCity revision 274 scripts exist (27)

These can be ported the same way as this week's four quests. Each one's LostCity folder (in `LostCityRS/Content`, branch `274`, under `scripts/quests/`) is in brackets.

| Released | Quest | LostCity folder | Needs first (on this server unless noted) |
|---|---|---|---|
| 2002-04-30 | Tribal Totem | `quest_totem` | — |
| 2002-05-28 | Fishing Contest | `quest_fishingcompo` | — |
| 2002-06-17 | Temple of Ikov | `quest_ikov` | — |
| 2002-07-23 | Fight Arena | `quest_arena` | — |
| 2002-08-15 | Hazeel Cult | `quest_hazeelcult` | — |
| 2002-09-09 | Sea Slug | `quest_seaslug` | — |
| 2002-09-24 | Waterfall Quest | `quest_waterfall` | — |
| 2002-10-23 | Biohazard | `quest_biohazard` | Plague City |
| 2003-01-27 | Shilo Village | `quest_zombiequeen` | Jungle Potion |
| 2003-03-03 | Underground Pass | `quest_upass` | Biohazard |
| 2003-03-17 | Observatory Quest | `quest_itgronigen` | — |
| 2003-04-14 | The Tourist Trap | `quest_desertrescue` | — |
| 2003-05-07 | Watchtower | `quest_itwatchtower` | — |
| 2003-05-27 | Dwarf Cannon | `quest_mcannon` | — |
| 2003-06-09 | Murder Mystery | `quest_murder` | — |
| 2003-07-09 | The Dig Site | `quest_itexam` | — |
| 2003-08-20 | Legends' Quest | `quest_legends` | Family Crest, Heroes' Quest, Shilo Village, Underground Pass, Waterfall Quest |
| 2004-05-18 | Big Chompy Bird Hunting | `quest_chompybird` | — |
| 2004-07-13 | Nature Spirit | `quest_druidspirit` | Priest in Peril, The Restless Ghost |
| 2004-08-09 | Death Plateau | `quest_death` | — |
| 2004-08-24 | Troll Stronghold | `quest_troll` | Death Plateau |
| 2004-09-14 | Tai Bwo Wannai Trio | `quest_tbwt` | Jungle Potion |
| 2004-09-20 | Regicide | `quest_regicide` | Underground Pass |
| 2004-10-05 | Eadgar's Ruse | `quest_eadgar` | Druidic Ritual, Troll Stronghold |
| 2004-10-18 | Shades of Mort'ton | `quest_mortton` | Priest in Peril |
| 2004-11-17 | Horror from the Deep | `quest_horror` | — |
| 2004-12-21 | Haunted Mine | `quest_haunted` | Priest in Peril |

The prerequisites listed are the main ones. Check each quest's wiki page for skill requirements before starting it.

### Group B: scripts only on LostCity's `377-wip` branch (about 6)

`377-wip` targets revision 377 (2 May 2006) and is unfinished, so expect gaps.

| Released | Quest | LostCity folder |
|---|---|---|
| 2005-01-05 | Troll Romance | `quest_troll_love` |
| 2005-04-11 | The Golem | `quest_golem` |
| 2005-06-27 | Recruitment Drive | `quest_rd` |
| 2005-10-17 | Mourning's End Part II | `quest_mourning2` (Part I has no script) |
| 2006-02-07 | Cabin Fever | `quest_fever` |
| 2006-03-15 | Recipe for Disaster | `quest_100` (probably; check what it contains) |

### Group C: no script source (about 45)

These need to be rebuilt from 2006-era guides (2007rshelp.com) and the OSRS wiki's quest transcripts, which record dialogue as of August 2007.

- **2004 (1):** Throne of Miscellania.
- **2005 (23):** In Search of the Myreque, Creature of Fenkenstrain, Roving Elves, Ghosts Ahoy, One Small Favour, Mountain Daughter, Between a Rock..., The Feud, Desert Treasure I, Icthlarin's Little Helper, Tears of Guthix, Zogre Flesh Eaters, The Giant Dwarf, The Lost Tribe, Mourning's End Part I, Forgettable Tale..., Garden of Tranquillity, A Tail of Two Cats, Wanted!, Rum Deal, Shadow of the Storm, Making History, Ratcatchers, Spirits of the Elid, Devious Minds.
- **2006 (19):** The Hand in the Sand, Enakhra's Lament, Fairytale I – Growing Pains, In Aid of the Myreque, A Soul's Bane, Rag and Bone Man I, Swan Song, Royal Trouble, Death to the Dorgeshuun, Fairytale II – Cure a Queen, Lunar Diplomacy, The Eyes of Glouphrie, Darkness of Hallowvale, The Slug Menace, Elemental Workshop II, My Arm's Big Adventure, Enlightened Journey, Eagles' Peak, Animal Magnetism (12 Dec 2006).

## Skills

- **Construction** (31 May 2006): only a skill-guide page exists. It needs houses, rooms, furniture building, butlers and the Servants' Guild, which makes it the largest single gap.
- **Hunter** (21 Nov 2006): not implemented. It needs traps, implings, creature spawns and Hunter areas such as the Feldip Hills and the Piscatoris area.
- The other 21 skills exist. Their 2006-specific details, like rates, level requirements and the 2005–06 additions, haven't been audited.

## Minigames

| Released | Minigame | On this server? |
|---|---|---|
| 2003-01-28 | Gnome Ball | Skill-guide entry only. LostCity has `game_gnomeball`. |
| 2003-07-28 | Fishing Trawler | No. LostCity has `game_trawler`. |
| 2003-09-22 | Mage Arena | Yes |
| 2004-03-25 | Duel Arena | Yes |
| 2004-05-18 | Chompy bird hunting | No; comes with Big Chompy Bird Hunting |
| 2004-07-27 | Brimhaven Agility Arena | Yes. LostCity has `game_agilityarena`. |
| 2004-10-18 | Shades of Mort'ton (temple) | No. LostCity has `game_mortton`. |
| 2004-11-24 | Burthorpe Games Room | *Verify*. LostCity has `game_gamesroom` and `boardgames`. |
| 2004-12-13 | Castle Wars | Yes |
| 2005-05-04 | Tears of Guthix | No; comes with the quest |
| 2005-05-31 | Keldagrim tasks | No |
| 2005-06-22 | Rogues' Den | No |
| 2005-08-09 | Tai Bwo Wannai Cleanup | No; needs Tai Bwo Wannai Trio |
| 2005-08-23 | Blast Furnace | No |
| 2005-09-19 | TzHaar Fight Pit | Partly: an area check only. *Verify*. |
| 2005-10-04 | TzHaar Fight Cave | Yes |
| 2005-11-28 | Rat Pits | No; comes with Ratcatchers |
| 2006-01-04 | Mage Training Arena | Yes |
| 2006-03-28 | Temple Trekking | No |
| 2006-04-18 | Pest Control | No. LostCity's `377-wip` has `game_pest`. |
| 2006-07-04 | Trouble Brewing | No; needs Cabin Fever |
| 2006-07-17 | Pyramid Plunder | No |
| 2006-08-07 | Gnome Restaurant | No |

Also in scope: Champions' Challenge (December 2005; LostCity `377-wip` has `game_champion_challenge`), Barrows (May 2005, on the server) and the Party Room (on the server). Barbarian Assault is just after the cutoff (4 Jan 2007), so it's out of scope.

## Bosses

Bosses that aren't tied to a quest are all present except the **Giant Mole** (7 Mar 2006). That includes the Kalphite Queen, the Dagannoth Kings, the Chaos Elemental, the King Black Dragon, the Barrows brothers and TzTok-Jad. Most quest bosses on the wiki's list (Agrith-Na-Na, Dessous, the Sea Troll Queen, the Giant Roc and others) come with their quests above.

## Guilds

The 2006 guilds are the **Warriors' Guild** (13 Jun 2006) and the **Servants' Guild** (31 May 2006, part of Construction). Neither was found on the server. The nine older guilds (Champions', Cooks', Mining, Crafting, Heroes', Fishing, Wizards', Legends' and Ranging) are mostly map areas with entry requirements. *Verify* their entry checks in game.

## Dungeons and areas

81 dungeons on the wiki predate the cutoff. Their maps come from the cache, so they exist on this server. What's likely missing is entrances, NPC spawns and interactions, especially for areas added in 2005–06 alongside missing quests. The held-back spawn list (`docs/audit/npc_spawns_held_back.csv`) covers many of these and is the place to start.

## Random events

The wiki lists the 2004–06 random events. These include Drill Demon, Freaky Forester, Evil Bob, Kiss the Frog, Pillory, Prison Pete, Quiz Master, Sandwich Lady, Gravedigger, Pious Pete, Mime, Maze, Lost and Found, Rick Turpentine, the Surprise Exam and others. The server has a `randomevent` package, but nobody has compared its contents with this list yet.

## Post-cutoff content to decide on

These came after 18 Dec 2006 but exist on this server, probably from the upstream SoloScape project:

- **God Wars Dungeon** (June 2007): the `model/gameplay/godwars` package, referenced in about 32 files.
- **Grand Exchange** (Nov 2007): the `model/grandexchange` package, about 28 files, and `data/geOfferData.dat`.
- **Dragon claws** (2007): offered to PK bots in `BotCombatLoadoutManager`.

For a strict revision 443 game, put each of these behind a setting that's off by default, rather than deleting it.

## Suggested order

1. **Finish this week's quests:** test and fix Monk's Friend, Clock Tower, Plague City and Sheep Herder. Then the porting pattern (the `QuestDlg` builder, journal and rewards) is proven.
2. **Port Group A in prerequisite order.** These are the cheapest quests: a script source exists and the 274 content has barely changed by 2006. One possible order:
   - Waterfall, Fight Arena, Tribal Totem, Fishing Contest, Hazeel Cult, Sea Slug, Murder Mystery, Dwarf Cannon, Temple of Ikov
   - Biohazard, Underground Pass, Regicide
   - The Dig Site, The Tourist Trap, Observatory, Watchtower, Shilo Village, Legends' Quest
   - Death Plateau, Troll Stronghold, Eadgar's Ruse
   - Nature Spirit, Big Chompy Bird Hunting, Tai Bwo Wannai Trio, Shades of Mort'ton, Horror from the Deep, Haunted Mine
3. **Add the LostCity-backed minigames** alongside the quests that unlock them: Fishing Trawler, Gnome Ball, the Mort'ton temple, Pest Control and Champions' Challenge.
4. **Release the held-back NPC spawns** as each quest or area that owns them is added.
5. **Port Group B** from `377-wip`, checking each script against 2006 guides.
6. **Rebuild Group C** from guides and transcripts. Order by prerequisite chains (for example Myreque, Fairytale, Desert Treasure and Elemental Workshop II).
7. **Build Hunter and Construction.** Hunter is smaller and newer (Nov 2006); Construction is the largest piece of work in this list.
8. **Fill in the remaining 2005–06 minigames:** Blast Furnace, Rogues' Den, Pyramid Plunder, Gnome Restaurant, Temple Trekking, Rat Pits, Trouble Brewing and Keldagrim tasks. Also add the Giant Mole, the Warriors' Guild, and the random-event comparison.
9. **Decide on the post-cutoff content** (God Wars, Grand Exchange, dragon claws).

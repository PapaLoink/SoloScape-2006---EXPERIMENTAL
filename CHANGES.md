# What changed compared to the original source

Base: [SoloScape/SoloScape-2006](https://github.com/SoloScape/SoloScape-2006) at commit `29a4fcd`
("Merge remote-tracking branch 'origin/feature/wilderness-bot-loot-management'").
Only the files listed here differ from that commit.

## Server (`Server/src/main/java/com/rs2/...`)

### New files
| File | Purpose |
|---|---|
| `model/quest/impl/PlagueCityQuest.java` | Plague City (quest 71). Dialogue and flow follow the 2004scape `quest_elena` scripts; Edmond waits in the sewer; rewards include the Ardougne Teleport scroll. |
| `model/quest/impl/ClockTowerQuest.java` | Clock Tower (23), ported from the 274 LostCity scripts. |
| `model/quest/impl/MonksFriendQuest.java` | Monk's Friend (63). |
| `model/quest/impl/SheepHerderQuest.java` | Sheep Herder (84), ported from the 274 LostCity scripts. |
| `model/quest/impl/QuestDlg.java` | Small dialogue-script builder used by the new quests. |
| `model/quest/impl/Ring.java` | Stone-circle entrance to the thieves' cave (Monk's Friend). |
| `model/quest/impl/Spiders.java` | Cellar spiders and NPCs for the Clock Tower cog room. |
| `model/quest/impl/QuestExtras.java` | Starts the quest world spawns at server start-up. |
| `HiscoresHttp.java` | Local-only (127.0.0.1:43597) endpoint with live player stats for the client tool bar. |

- `model/npc/ExtraNpcSpawns.java` - loads `Server/data/npcs/extra_spawns.csv` (2,290 extra overground NPC spawns, quest-gated NPCs excluded). Delete or empty the CSV to disable.
- `Server/data/npcs/extra_spawns.csv` - the spawn data (npc_id,name,x,y,plane,walk).

### Modified files
`model/quest/impl/QuestExtras.java` now also calls `PlagueCityQuest.spawnWorld()` (Milli Rehnison, Elena and Edmond in the sewer were never spawned before) and `ExtraNpcSpawns.load()`.

| File | Change |
|---|---|
| `model/quest/QuestDefinition.java` | Registers the four new quest scripts. |
| `model/quest/QuestManager.java` | Clicking a quest the stock client lists but the server lacks (12 of them) shows a "not added yet" page. |
| `net/packet/InterfaceBridge.java` | Maps those 12 quest-list rows to server button ids. |
| `net/packet/PacketSender.java` | Paints those rows black like other unimplemented quests. |
| `net/packet/handler/ItemActionPacketHandler.java` | Hooks Clock Tower's use-item-on-ground handling. |
| `net/packet/handler/IdlePacketHandler.java` | Logs the player out when the client reports the chosen idle time. |
| `model/player/Player.java` | Disables the server's fixed idle timer in favour of the client's setting. |
| `model/player/ModernPlayerUpdateTask.java` | Rebuilds the scene earlier so the player stays near the middle of the loaded area. |
| `launcher/ControlPanel.java` | Does not open the world map window on start; shutdown saves and closes immediately instead of a 5-minute countdown. |
| `Server.java` | Starts the quest extras and the hiscores endpoint. |

## Client (`Client/src/...`)

### New files
| File | Purpose |
|---|---|
| `unpackaged/ModPanel.java` | The docked tool bar and its tool windows (hiscores, wiki, stats, calculator, world map, help, settings). |
| `unpackaged/ModHook.java` | Hooks into the game: stretch and fullscreen, WASD camera, remembered login, idle-logout setting, chat prompt. |
| `unpackaged/XpOverlay.java` | XP drops and level-progress card. |
| `assets/mod/ids/items.txt`, `npcs.txt` | Full item and NPC ID lists for the Help tool. |

### Modified files
`jagex/graphics/JImage.java`, `jagex/io/Buffer.java`, `unpackaged/Canvas_Sub1.java`, `Class32.java`, `Class34.java`,
`Class38.java`, `Class39_Sub12.java`, `Class39_Sub5_Sub7.java`, `Class62_Sub1_Sub1.java`, `ClientApplet.java`,
`JKeyListener.java`, `Model.java` — small hooks that call the new classes, fit the game picture beside the tool bar,
and route keyboard and drawing events. Run `git diff` against the base commit to see each one.

## Not changed
The cache, configuration, bots, combat, skills, build scripts and all other game content are as in the original.

> Note for maintainers: this list was produced by comparing the working tree with the base commit. If you rebase
> onto a newer upstream, re-check the modified files for conflicts.

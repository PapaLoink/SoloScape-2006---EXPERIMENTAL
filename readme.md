# SoloScape 2006

**A local, single-player RuneScape experience built around the December 2006 era.**

SoloScape brings together a Java game client, a self-hosted game server, and the data needed to explore a 2006-era RuneScape world. It is designed to run on your own Windows computer, with tools for both players and developers.

The project targets the **18 December 2006 / revision 443** cache and protocol. Revision compatibility and gameplay content are still being audited; see [Project status](#project-status) before assuming that every historical interaction is complete.

[Get started](#quick-start) · [Current features](#current-working-features) · [Roadmap](#roadmap) · [Developer setup](#development) · [Server documentation](Server/README.md) · [Browser client](Client/WebClient/README.md)

## Current Working Features

The following functionality is **present in the current repository**, with launchers, implementations, documentation, and/or targeted checks. This is a summary of the available systems—not a claim that every interaction or historical detail has passed a full playthrough.

### Client and world

- **Java 8 client and local game server:** Build and launch both components on Windows, connect to the local server on port `43594`, and use the desktop server control panel.
- **Revision 443 game foundation:** Bundled 18 December 2006-era JS5 cache, map/XTEA data, and revision-specific login, update, and region handling. Some packet/interface behavior still needs visual verification.
- **Basic world interaction:** Client rendering, player/NPC updates, movement, inventory/equipment, chat, interfaces, and server-side NPC/object/item interaction routing.
- **Server-managed world:** World simulation, content data, shops, player trading, and configurable simulated bot populations. Individual routes and content remain subject to testing.

### Gameplay and content

- **Existing skill implementations:** Code modules for Woodcutting, Mining, Fishing, Firemaking, Cooking, Smithing, Crafting, Fletching, Farming, Herblore, Agility, Thieving, Magic, Prayer, Runecrafting, and Slayer. **Availability does not mean every action, level, or item combination is complete.**
- **Combat and activities:** Existing combat systems and activity modules including Barrows, Castle Wars, Fight Caves, and the Duel Arena; completeness and era-appropriate mechanics vary.
- **Quests and onboarding:** A Tutorial Island flow and quest framework, with targeted regression checks for selected quests and interactions such as *The Restless Ghost*.
- **2006 Christmas rescue:** Implemented Shanty Claws/Gublinch quest flow, snowball mechanics, cage progress, and event rewards, with dedicated server and client checks. See the [event documentation](Server/README.md#2006-christmas-rescue).

### Saves, access, and developer tools

- **Character persistence:** JSON character saves, automatic migration from legacy `.dat` files, backup/recovery paths, and an optional SQLite-backed storage path.
- **Browser/phone play:** A web view streamed from the actual Java client, including touch/camera controls and optional browser audio; the Java renderer remains on the host computer.
- **Live development:** Java source watching/HotSwap, client screenshots and state inspection, a localhost MCP endpoint, and an activity dashboard.
- **Diagnostics and tests:** Packet/interaction debug traces, targeted Java regression checks, strict-content and interaction audit tools, and GitHub Actions workflows.

For deeper implementation details, see [the server guide](Server/README.md), [client developer tools](docs/Client/DEVELOPER-TOOLS.md), and the [browser client guide](Client/WebClient/README.md).

## Requirements

- **Windows** for the supported `.bat` launchers and PowerShell tooling.
- **Java 8 JDK** (not just a JRE). The scripts require Java 8 rather than Java 11+.
- Enough free memory to run the client and server; the server launcher is configured with a **1 GB maximum Java heap**.
- Internet access on the first run **if Java 8 is not already installed**.

**You usually do not need to install Java manually.** The scripts look for a compatible Java 8 JDK and, if none is found, download Eclipse Temurin Java 8, verify its SHA-256 checksum, and install it under `%LOCALAPPDATA%\SoloScape\java8` without administrator permissions. Existing local installations can be selected with `JAVA_HOME`.

Keep the complete repository together: the client depends on the sibling `Server/cache` folder, and the build scripts use files in `tools/`.

## Quick start

### Option A — Build and play automatically

1. [Download the repository](https://github.com/SoloScape/SoloScape-2006/archive/refs/heads/main.zip) and extract it, or clone it using Git.
2. Open the extracted **SoloScape-2006** folder.
3. Double-click **`Start-Here-SoloScape.bat`**.
4. Choose **`4 — Play SoloScape`**.
5. The launcher builds the server and client, starts the server automatically, then opens the game. Log in with the username and password you want to use for your local character.

Keep the server running while playing. On your first launch, the Java setup step may need an internet connection.

### Option B — Build once and use separate launchers

1. Double-click `Start-Here-SoloScape.bat` and choose **`1 — First-time setup`**.
2. After both builds succeed, open **`Start-Server.bat`** from the main folder.
3. In the server control panel, click **Start Server** and wait until it is online.
4. Open **`Play-Client.bat`** from the main folder and log in.

The two main-folder launchers are **generated by a successful build**; they are not missing from the source download. For later play sessions, use those launchers without rebuilding unless the code has changed.

> **Tip:** If the game cannot connect, make sure the server control panel says the server is online. Launching the control panel alone does not start the game world.

## Launcher reference

| File | Purpose |
| --- | --- |
| `Start-Here-SoloScape.bat` | Main menu for setup, playing, development, cleanup, and updates. |
| `Start-Server.bat` | Generated launcher for the server control panel. |
| `Play-Client.bat` | Generated launcher for the Java client. |
| `Server/Build-Server.bat` | Compiles the server into `Server/dist/server.jar`. |
| `Client/Build-Client.bat` | Compiles the client into `Client/build/client.jar`. |
| `Server/Start-Server-Dev-Mode.bat` | Server with automatic recompilation and live-development support. |
| `Client/Start-Client-Dev-Mode.bat` | Client with HotSwap and the local developer tools endpoint. |
| `Client/WebClient/Play-In-Browser-Or-Phone.bat` | Streams the Java client to a web browser or phone. |
| `Setup-Java-8.bat` | Shared Java 8 setup helper; called by the other scripts. |

The main menu offers:

| Option | Action |
| --- | --- |
| `1` | Build server and client |
| `2` | Build client only |
| `3` | Build server only |
| `4` | Build and play |
| `5` | Start developer mode |
| `6` | Clean generated build files |
| `7` | Pull Git updates (fast-forward only) |
| `0` | Exit |

## Development

The project uses **Java 8** and bundled JAR dependencies rather than Gradle or Maven. Builds are driven by the included batch and PowerShell scripts.

### PowerShell commands

From the repository root:

```powershell
.\tools\scape.ps1 build          # Build server and client
.\tools\scape.ps1 build server   # Server only
.\tools\scape.ps1 build client   # Client only
.\tools\scape.ps1 run            # Build and launch both
.\tools\scape.ps1 dev            # Start live development mode
.\tools\scape.ps1 clean          # Delete generated build output
.\tools\scape.ps1 update         # Git pull --ff-only
```

You can also pass commands to the main launcher, for example:

```bat
Start-Here-SoloScape.bat build client
```

**Build outputs:** `Server/dist/server.jar` and `Client/build/client.jar`. Keep the server's `lib/` directory and the shared `Server/cache/` data in their original locations when running the project.

The `clean` command removes compiled files and generated launchers. It **does not delete** the bundled cache, character saves, or server content data. Stop running development watchers before building or cleaning.

### Live development and client inspection

Run `Start-Here-SoloScape.bat` and choose **`5`**, or launch the development scripts individually. Java source changes are recompiled automatically; edits that cannot be HotSwapped cause an application restart.

With the client developer launcher running, open:

| Endpoint | Address |
| --- | --- |
| Developer activity dashboard | [http://127.0.0.1:7780/](http://127.0.0.1:7780/) |
| MCP endpoint | `http://127.0.0.1:7780/mcp` |

These endpoints are local to the machine running the client and are **disabled during normal play**. The tools can inspect client state, widgets, inventories, NPCs, objects, dialogue, screenshots, and interaction outcomes. See [docs/Client/DEVELOPER-TOOLS.md](docs/Client/DEVELOPER-TOOLS.md) for configuration, tool names, and examples.

### Repository layout

```text
SoloScape-2006/
├── Client/
│   ├── src/                   Java client and assets
│   ├── tests/                 Client regression checks
│   ├── WebClient/             Browser/phone streaming bridge
│   └── Build-Client.bat
├── Server/
│   ├── src/main/java/         Server and control-panel source
│   ├── src/test/              Server checks
│   ├── cache/                 Revision 443 cache and map data
│   ├── data/                  Content, settings, and player data
│   ├── lib/                   Bundled Java dependencies
│   ├── Build-Server.bat
│   └── README.md
├── tools/                     Build helpers and audit scripts
├── docs/                      Additional development notes
├── .github/workflows/         Automated verification workflows
├── Start-Here-SoloScape.bat
├── Setup-Java-8.bat
└── readme.md
```

## Play in a browser or on your phone

SoloScape also offers a browser front end. **This is not a JavaScript port of RuneScape.** The actual Java client continues to load the cache, render the world, and process the game protocol on the host computer; the browser receives rendered frames and forwards input.

1. Start the game server.
2. Run `Client/WebClient/Play-In-Browser-Or-Phone.bat`.
3. Open **[http://127.0.0.1:8081/](http://127.0.0.1:8081/)** on the host computer.
4. For a phone on the same local network, use the LAN address printed in the launcher console.

The web view keeps the native **765 × 503** game aspect ratio and supports touch controls, camera gestures, and browser audio (after enabling sound). The host computer must remain on, with the Java bridge running.

**Security:** The browser bridge uses local HTTP/WebSocket transport. Do **not** expose port `8081` directly to the public internet. Use a trusted private network or secure tunnel for access outside your LAN.

See [Client/WebClient/README.md](Client/WebClient/README.md) for supported controls, audio notes, and the original Windows executable option.

## Player data and configuration

The server uses **`127.0.0.1:43594`** for local client connections by default. The server's control panel provides settings for the world and gameplay; more details are in [Server/README.md](Server/README.md).

For file-based characters:

- Active saves: `Server/data/characters/<username>.json`
- Migrated legacy saves: `<username>.dat.bak`
- Previous JSON backups: `<username>.json.bak`
- Server snapshots: `Server/data/backups/`

Older binary character files are migrated to JSON automatically. If an existing JSON save and legacy file share a username, the JSON save takes precedence. Optional SQLite-backed persistence is separate from the default file-based flow.

**Back up your saves before upgrading or editing them.** Stop the server before editing a character, and never commit player saves to a public repository: they may contain account information, including credentials in the project's legacy representation.

Read [docs/Server/CHARACTER-SAVES.md](docs/Server/CHARACTER-SAVES.md) before migrating or manually changing character data.

## Testing and audits

Selected regression checks run through [GitHub Actions](.github/workflows/). Local verification scripts include:

```powershell
.\tools\developer-tools-checks.ps1
.\tools\interaction-audit.ps1 -Check
.\tools\strict-content-checks.ps1
```

These commands check different parts of the client and content; they are **not** a substitute for logging in and verifying gameplay. The strict content audit deliberately flags unresolved IDs and source-review candidates, so its output may contain outstanding work rather than an all-green report.

See [the interaction migration notes](Server/README.md#interaction-coverage-and-revision-443-routing) and [strict content check notes](docs/tools/strict-content-checks.md).

## Project status

SoloScape is an **actively developed and audited recreation**, not a claim of complete 2006 game parity.

- The current client/server work targets the **revision 443** cache, login/update handshake, and region layout.
- Some packet handling, interfaces, and interactions still require graphical or in-game verification.
- Existing game systems provide a foundation, but **not every NPC, item action, quest, or minigame has been fully verified**.
- Automated checks cover selected behaviors; a passing check does not prove that all gameplay is historically accurate.

For detailed implementation notes and limitations, see [Server/README.md](Server/README.md) and the [revision audit](docs/AUDIT_2006-12-18.md).

## Roadmap

SoloScape aims to recreate the **18 December 2006 (revision 443)** experience. The tracker below follows the requested skill-by-skill layout and measures **historical completeness**, not simply whether source code for a skill exists.

**Status key:** ✅ Complete and verified · 🚧 In progress · ⛔ Not yet marked complete (may have partial implementations) · ➖ Outside the 2006 scope

### Skills & combat

| Skill | Status | Skill | Status |
| --- | :---: | --- | :---: |
| Woodcutting | 🚧 | Mining | ⛔ |
| Agility | ⛔ | Smithing | ⛔ |
| Herblore | ⛔ | Fishing | ⛔ |
| Thieving | ⛔ | Cooking | ⛔ |
| Crafting | ⛔ | Firemaking | ⛔ |
| Fletching | ⛔ | Runecrafting | ⛔ |
| Slayer | ⛔ | Farming | ⛔ |
| Construction | ⛔ | Hunter | ⛔ |
| Prayer | ⛔ | Magic | ⛔ |
| Ranged | ⛔ | Melee (Attack, Strength, Defence & Hitpoints) | ⛔ |

**Sailing:** ➖ Not part of the December 2006 skill set, so it is outside this project's historical target rather than an unfinished 2006 skill.

**Important:** Some skills marked ⛔ already have working mechanics in the server. This tracker deliberately does **not** equate ⛔ with “no code exists”; it means the skill has not yet been marked fully implemented and historically verified. Woodcutting is shown as 🚧, as in the proposed tracker. Update statuses as features are tested and completed.

### Bosses & major encounters

| Content | Status | Content | Status |
| --- | :---: | --- | :---: |
| King Black Dragon | 🚧 | Kalphite Queen | 🚧 |
| Barrows brothers | 🚧 | TzTok-Jad (Fight Caves) | 🚧 |
| Dagannoth Kings | ⛔ | Chaos Elemental | ⛔ |
| Giant Mole / Wyson rewards | ⛔ |  |  |

Bosses marked 🚧 have related combat, reward, or encounter code in the repository, **not verified end-to-end boss mechanics**. The remaining bosses need an implementation and/or live-play audit.

### Quests

<details>
<summary><strong>Existing quest scripts (34 entries) — expand tracker</strong></summary>

These quests have dedicated source files under `Server/src/main/java/com/rs2/model/quest/impl/`. Their 🚧 status means **a script exists**, not that every stage, requirement, reward, and dialogue is fully playable.

| Content | Status | Content | Status |
| --- | :---: | --- | :---: |
| Black Knights' Fortress | 🚧 | Cook's Assistant | 🚧 |
| Demon Slayer | 🚧 | Doric's Quest | 🚧 |
| Dragon Slayer | 🚧 | Druidic Ritual | 🚧 |
| Elemental Workshop | 🚧 | Ernest the Chicken | 🚧 |
| Family Crest | 🚧 | Gertrude's Cat | 🚧 |
| Goblin Diplomacy | 🚧 | Heroes' Quest | 🚧 |
| Holy Grail | 🚧 | Imp Catcher | 🚧 |
| Jungle Potion | 🚧 | Lost City | 🚧 |
| Merlin's Crystal | 🚧 | Monkey Madness | 🚧 |
| Pirate's Treasure | 🚧 | Priest in Peril | 🚧 |
| Prince Ali Rescue | 🚧 | Romeo & Juliet | 🚧 |
| Rune Mysteries | 🚧 | Scorpion Catcher | 🚧 |
| Sheep Shearer | 🚧 | Shield of Arrav | 🚧 |
| The Fremennik Trials | 🚧 | The Grand Tree | 🚧 |
| The Knight's Sword | 🚧 | The Restless Ghost | 🚧 |
| Tree Gnome Village | 🚧 | Vampire Slayer | 🚧 |
| Witch's House | 🚧 | Witch's Potion | 🚧 |

</details>

<details>
<summary><strong>Missing late-2006 quest coverage (12 entries) — expand tracker</strong></summary>

The [historical audit](docs/AUDIT_2006-12-18.md) identified these target-date quests as missing from the **older quest dataset**. ⛔ means they are not yet marked complete; their present-day runtime status should be rechecked before claiming they are fully absent.

| Content | Status | Content | Status |
| --- | :---: | --- | :---: |
| Royal Trouble | ⛔ | Death to the Dorgeshuun | ⛔ |
| Fairy Tale II — Cure a Queen | ⛔ | Lunar Diplomacy | ⛔ |
| The Eyes of Glouphrie | ⛔ | Darkness of Hallowvale | ⛔ |
| The Slug Menace | ⛔ | Elemental Workshop II | ⛔ |
| My Arm's Big Adventure | ⛔ | Enlightened Journey | ⛔ |
| Eagles' Peak | ⛔ | Animal Magnetism | ⛔ |

</details>

The above lists track identified quest scripts and late-2006 gaps; they are **not an exhaustive list of all 116 official quests** available by 18 December 2006. A complete quest-by-quest acceptance pass is still needed. **Tutorial Island** is tracked below as a separate onboarding feature.

### Minigames & activities

| Content | Status | Content | Status |
| --- | :---: | --- | :---: |
| Barrows | 🚧 | Castle Wars | 🚧 |
| Duel Arena | 🚧 | TzHaar Fight Caves | 🚧 |
| Mage Training Arena | 🚧 | Tai Bwo Wannai Cleanup | 🚧 |
| Falador Party Room | 🚧 | Pest Control | ⛔ |
| TzHaar Fight Pits | ⛔ | Pyramid Plunder | ⛔ |
| Rogues' Den | ⛔ | Trouble Brewing | ⛔ |
| Temple Trekking | ⛔ | Blast Furnace | ⛔ |
| Fishing Trawler | ⛔ | Rat Pits | ⛔ |
| Stronghold of Security | ⛔ | Warriors' Guild | ⛔ |
| Brimhaven Agility Arena | ⛔ | Gnome Restaurant | ⛔ |

🚧 means there are dedicated controllers or partial mechanics in source; ⛔ means there is no confirmed complete gameplay loop in the repository audit. In particular, a transport route, dialogue, area flag, or cache asset does **not** establish a working minigame.

### Combat, world & economy

| Content | Status | Content | Status |
| --- | :---: | --- | :---: |
| Melee / Ranged / Magic combat | 🚧 | PvP / Wilderness combat | 🚧 |
| Prayer & special attacks | 🚧 | NPC combat and drops | 🚧 |
| Player trading | 🚧 | Banking & equipment | 🚧 |
| Shops & prices | 🚧 | Treasure Trails / clue scrolls | 🚧 |
| Random events | 🚧 | Slayer masters & tasks | 🚧 |
| NPC dialogue & world interactions | 🚧 | Quest progression & rewards | 🚧 |
| Movement, teleports & travel | 🚧 | 2006 Christmas rescue | 🚧 |
| Fairy rings | ⛔ | Player-owned houses (POH) | ⛔ |
| 2006 skill tutors | ⛔ | Capes of Accomplishment | ⛔ |
| Grand Exchange (released 2007) | ➖ | Modern bank tabs (released 2008) | ➖ |
| God Wars Dungeon (released 2007) | ➖ | Modern Ironman modes | ➖ |

Entries marked ➖ are deliberately **outside the strict 18 December 2006 target**, even where optional or later-era code already exists.

### Client, tools & supporting systems

| Content | Status | Content | Status |
| --- | :---: | --- | :---: |
| Java 8 setup and Windows launchers | 🚧 | Client / server revision 443 support | 🚧 |
| Character JSON saves & migration | 🚧 | Optional SQLite save backend | 🚧 |
| World simulation & bot AI | 🚧 | Web / mobile client streaming | 🚧 |
| Client developer tools / MCP | 🚧 | Automated content audits & checks | 🚧 |

These systems have supporting code or launchers; 🚧 does not imply the associated workflow has passed testing on every supported computer or client configuration.

### Other development priorities

- **Historical accuracy:** Isolate later RuneScape systems, secure progression commands, and audit post-2006 items, drops, spawns, and death mechanics.
- **Revision 443 data:** Finish the target-era NPC/quest population, Hunter/Construction support, skill persistence, and cache/definition validation.
- **Content completion:** Extend quests, Lunar magic, random events, activities, Slayer, Treasure Trails, and other late-2006 systems.
- **Quality and parity:** Fix period-specific combat, Prayer, weapon and food behavior; finish interaction coverage and add live client/server acceptance tests.

These are priorities, **not release commitments**. The [historical audit](docs/AUDIT_2006-12-18.md) and [ordered fix plan](docs/fix-order.md) provide the detailed backlog; because they document an earlier code snapshot, recheck each finding before treating it as unresolved. The [Current Working Features](#current-working-features) section lists systems already present in the repository.

## Troubleshooting

<details>
<summary><strong>I can't find Play-Client.bat or Start-Server.bat.</strong></summary>

These files are generated after a successful build. Run `Start-Here-SoloScape.bat` and select option `1`, or build the corresponding component directly.
</details>

<details>
<summary><strong>The build cannot find Java or javac.</strong></summary>

Run the provided launcher again with internet access, or install a **Java 8 JDK** and set `JAVA_HOME` to its directory. A JRE or Java 11+ is not a replacement for this project's required toolchain.
</details>

<details>
<summary><strong>The client fails to connect or load the cache.</strong></summary>

Start the server first and check that it reports online. Keep the original `Client/` and `Server/` directory layout intact; the client launcher expects `Server/cache/main_file_cache.dat2` and its map-fallback files.
</details>

<details>
<summary><strong>Changes aren't appearing after I edit the source.</strong></summary>

Rebuild the modified component, or use the live development launcher. Stop development watchers before a clean/release build and restart the client if you are checking cache or visual changes.
</details>

<details>
<summary><strong>The browser view doesn't open on my phone.</strong></summary>

Keep the bridge console running, use the LAN URL printed by the script (not the host's `127.0.0.1` address), and make sure both devices can reach each other on the local network.
</details>

## Contributing

Issues and pull requests are welcome for reproducible bugs, revision compatibility, gameplay content, tests, and documentation. Describe the behavior you observed, the expected result, and how to reproduce it. For code changes, include relevant verification steps and call out anything you could not test in a live client.

## Credits and legal notice

SoloScape is an **unofficial fan project** and is not affiliated with or endorsed by Jagex. RuneScape names, artwork, assets, and trademarks belong to their respective owners.

The server includes bundled third-party dependencies and code adapted in part from the **RS Mod pathfinder**. Refer to [Server/README.md#third-party-notices](Server/README.md#third-party-notices) for the included attribution.

**Licensing:** No repository-wide `LICENSE` file is currently provided. Public source availability should not be interpreted as blanket permission to redistribute all code, bundled libraries, or game assets.

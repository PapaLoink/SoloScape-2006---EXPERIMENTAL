# SoloScape-2006 Plus

A community fork of [SoloScape-2006](https://github.com/SoloScape/SoloScape-2006), the single-player
RuneScape revision 443 (2006) server and client. This fork adds extra quests, a docked tool bar in the
client, XP drops, searchable ID lists and a few quality-of-life settings. Everything else is the original project.

> This is an unofficial fork. It is not affiliated with Jagex or with the original SoloScape authors.
> See [CREDITS.md](CREDITS.md) for who built what.

## What this fork adds

**Client**
- A slim docked side bar with tools: Hiscores, Wiki lookup, My stats, Level calculator, World map, Help (item and NPC ID lists), plus Stretch to fill window, Screenshot, Keep on top, Fullscreen (F11) and Settings.
- XP drops and a level-progress card in the top right of the game screen.
- Searchable lists of every item ID and NPC ID.
- WASD camera control (toggle with `::wasd`), stretch-to-fill window mode and fullscreen.
- Remembered login, and a choice of idle-logout time (90 seconds, 5, 15 or 30 minutes, or never).

**Server**
- Four new quests: Clock Tower, Monk's Friend, Sheep Herder and Plague City (see [CHANGES.md](CHANGES.md)).
- Quests that the client lists but the server has no content for now open a "not added yet" page instead of doing nothing, and are shown in black.
- A small local hiscores/players endpoint used by the client tool bar (127.0.0.1 only).
- Smoother scene loading around the player, instant shutdown from the control panel, and idle logout driven by the client's setting.

## Install guide (Windows)

1. Click the green **Code** button on this page, then **Download ZIP**, and unzip it somewhere (for example `C:\SoloScape`).
2. Open the unzipped folder and double-click `Start-Here-SoloScape.bat`.
3. Choose **1 - First-time setup**. It installs a Java 8 JDK if you do not have one (needs internet on the first run) and builds the server and client. Wait until both finish.
4. Double-click `Start-Server.bat` in the main folder, click **Start Server** in the control panel and wait for it to say online.
5. Double-click `Play-Client.bat`, log in with any name and password (the account is created on first login), and play.

Later sessions: start the server, then the client. After changing code, choose option **2** (client) or **3** (server) in `Start-Here-SoloScape.bat` to rebuild.

### Adding only the mods to an existing SoloScape install

Copy the `Client` and `Server` folders from this repository over your existing ones (allow overwrite), then rebuild with option 1. Back up your folder first. Your saved characters in `Server/data` are not touched.

### Trying the new quests

| Quest | Where to start |
|---|---|
| Plague City | Talk to Edmond in the north-west house of East Ardougne |
| Clock Tower | Brother Kojo at the Clock Tower, south of East Ardougne |
| Monk's Friend | Brother Omad at the monastery south of Ardougne |
| Sheep Herder | Councillor Halgrive near the Zoo in East Ardougne |

Quest rewards and the Ardougne Teleport scroll from Plague City work: completing the quest and reading the scroll unlocks the Ardougne Teleport spell (Magic 51).

## Known limitations

- Twelve quests that the stock client lists (such as Elemental Workshop II, The Eyes of Glouphrie and Enlightened Journey) have no server content yet.
- Plague City was tested in the real client for the Rehnison door, scroll reward, sewer entry and Ardougne Teleport; the plague-house doors, Bravek's door, the cell door and staircases were only tested by stepping the quest state, not by playing through.
- Windows is the supported platform; the build scripts are `.bat` files.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). New quests, bug fixes and documentation are all welcome.

## Original project readme

The original build and developer documentation is kept in [docs/UPSTREAM-README.md](docs/UPSTREAM-README.md).

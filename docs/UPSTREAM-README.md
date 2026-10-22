# SoloScape

## Live AI client testing

Run `Client/Start-Client-Dev-Mode.bat` to enable live HotSwap and the local MCP endpoint at
`http://127.0.0.1:7780/mcp` and activity dashboard at `http://127.0.0.1:7780/`.
The tools inspect and operate the real game client. Start the server and log in
normally first. Setup, supported tools and verification are documented in
[Client/DEVELOPER-TOOLS.md](Client/DEVELOPER-TOOLS.md).

## New player guide: build and play

The **server** runs the game world and saves your player. The **client** is the
game window you play in. **Building** turns the source code into files that can
run; you need to build both before your first game and again after code changes.

Keep the `Client` and `Server` folders together in the main SoloScape folder.
`Start-Here-SoloScape.bat` automatically calls `Setup-Java-8.bat` to check for
a Java 8 JDK. If none is found, it downloads and installs one, then continues.
The build scripts do the same check. Allow internet access on the first run
if you do not already have a Java 8 JDK; no separate setup step is needed.

### First-time setup: build once, then play from the main folder

1. In the main SoloScape folder, double-click `Start-Here-SoloScape.bat`.
2. Choose **1 - First-time setup** and wait for both builds to finish.
3. After both builds succeed, the command window closes automatically.
   `Start-Server.bat` and `Play-Client.bat` appear beside `Start-Here-SoloScape.bat`.
4. Double-click `Start-Server.bat` in that main folder. In the control panel,
   click **Start Server** and wait for the server to be online.
5. Double-click `Play-Client.bat` in the same folder and log in.

Use those two main-folder launchers for later play sessions. Keep the server
running while you play. If either build fails, the setup window stays open so
you can read the error; fix the problem and try option 1 again.

### Easiest way: build and start everything

1. Open the main SoloScape folder in File Explorer.
2. Double-click `Start-Here-SoloScape.bat`.
3. Choose **4 - Play SoloScape**. It builds the server and client, starts the
   server automatically, then opens the game window.
4. Wait for the server to be online, then log in with the username and password
   you want to use. Your local player is created and saved by the server.
5. Keep the server window open while playing.

### Build the server and client yourself

1. Open the `Server` folder and double-click `Build-Server.bat`.
2. Wait for **Build complete: dist\server.jar**. `Start-Server.bat` now appears
   in both the `Server` folder and the main folder. Press any key when prompted.
3. Open the `Client` folder and double-click `Build-Client.bat`.
4. Wait for **Build complete: build\client.jar**. `Play-Client.bat` now appears
   in both the `Client` folder and the main folder. The client build window closes automatically when finished.

Choose **1** in `Start-Here-SoloScape.bat` to build both and close the menu
after success, **2** to build only the client, or **3** to build only the server. Building does not start
the game. If you see an error, fix it before continuing.

The play launchers are created only after their builds succeed. If you do not
see them yet, build that component first. Leave the `.bat.template` files in
place: the build scripts use them to create the launchers.

### Start the server and client yourself

1. After building, open the main SoloScape folder and double-click `Start-Server.bat`.
2. In the server control panel, click **Start Server** and wait for it to show
   that the server is online.
3. In the same main folder, double-click `Play-Client.bat`.
4. Log in with the username and password you want to use.
5. Keep the server running for your whole play session. When finished, close the
   game client, then stop the server through its control panel.

For later sessions, use these same two launchers. Rebuild after changing or
updating code, and stop any running developer-mode watchers before rebuilding.
If the client cannot connect, check that you clicked **Start Server** and that
the server is online.

## Which file should I open?

**New player? Double-click `Start-Here-SoloScape.bat` in the main folder,
then choose option 1 to build both and create the play launchers here.** Keep the
server window open while you play: it runs the game world and saves your player.

| Batch file | What it does |
| --- | --- |
| `Start-Here-SoloScape.bat` | Opens the main menu to play, rebuild, or update the game. |
| `Play-Client.bat` | Created by the client build. Opens the game window; start the server first. |
| `Start-Server.bat` | Created by the server build. Opens the control panel; click **Start Server**. |
| `Client/Build-Client.bat` | Turns the client source code into runnable game files. Does not start the game. |
| `Server/Build-Server.bat` | Turns the server source code into runnable server files. Does not start the server. |
| `Client/Start-Client-Dev-Mode.bat` | For editing code: starts the client with live HotSwap, automatic rebuilds and the developer tools dashboard/MCP endpoint on port 7780. |
| `Server/Start-Server-Dev-Mode.bat` | For editing code: starts the server and rebuilds it when source files change. |
| `Client/WebClient/Play-In-Browser-Or-Phone.bat` | Starts the client with browser/phone access and prints the address to open. Start the server first. |
| `Setup-Java-8.bat` | Helper used automatically by the other scripts to find or install Java 8. You do not need to open it yourself. |

Install a Java 8 JDK (any update, including 8u101 and 8u491). The batch scripts
automatically find it through `JAVA_HOME`, PATH, the Windows registry, or common
installation folders. For a custom location, set `JAVA_HOME` to the JDK folder.
A JRE alone or Java 11+ cannot build the client. Keep `Setup-Java-8.bat` in the repository root and `java8.ps1` and `scape.ps1`
in the `tools` folder when distributing the scripts.

If no Java 8 JDK is found, the scripts download a free Eclipse Temurin Java 8
JDK from Adoptium, verify its SHA-256 checksum, and extract it under
`%LOCALAPPDATA%\SoloScape\java8`. This requires internet access on the first run
and no administrator permissions. Later runs reuse that installation offline.

From the repository root, use Windows PowerShell:

```powershell
.\tools\scape.ps1 build          # Build both
.\tools\scape.ps1 build client   # Build only the client
.\tools\scape.ps1 build server   # Build only the server
.\tools\scape.ps1 run            # Build and launch both; automatically start the server
.\tools\scape.ps1 dev            # Launch both with live recompilation
.\tools\scape.ps1 clean          # Remove generated build files
.\tools\scape.ps1 update         # Pull latest commits (fast-forward only)
```

Double-click `Start-Here-SoloScape.bat` for the same commands as menu options 1-7, with `0`
to exit. It also accepts commands, such as `Start-Here-SoloScape.bat build client`, and uses
PowerShell with execution-policy bypass for that invocation.

`run` and `dev` open separate client and server consoles. Development mode uses
the existing HotSwap watchers: saving Java sources recompiles changes, and
structural edits trigger application restarts. Both applications need the
bundled `Server/cache` folder. Close the applications to stop them; stop both
development watchers before building or cleaning.

`clean` removes `Client/build`, `Server/build`, `Server/build-release`, and
`Server/dist`, plus the generated `Play-Client.bat` and `Start-Server.bat`
launchers in the main and component folders. Rebuild to recreate them. Cache files, libraries, configuration, and player saves are kept.

`update` requires Git on PATH and a branch with an upstream remote. It preserves
local work and reports conflicting changes or diverged history for you to resolve.

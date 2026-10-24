# 06 WebClient

This is a thin mobile/browser view of the real `06-Client` Java client.

It does **not** reimplement the RuneScape UI, cache, packets, widgets, models, scene, login protocol, ISAAC, RSA, or gameplay in JavaScript. Those remain inside the existing revision-443 Java client.

## How it works

`06-Client` still does all normal work:

- revision 443 JS5/cache loading
- login and RSA/ISAAC
- server packets
- players/NPCs
- scene/model rendering
- interfaces/widgets
- inventory/chat/minimap
- mouse/keyboard handling

A small `WebClientBridge` was added to the Java client. Every native `JImage.draw(...)` blit is copied into a 765x503 composite framebuffer. At the end of a normal Java render pass that framebuffer is encoded as PNG and streamed to Safari over WebSocket.

Browser input is converted back to AWT mouse/key events and dispatched to the existing Java `Canvas`, so the same Java input handlers run.

## Start

1. Start `06-Server` normally on port `43594`.
2. Run:

```bat
Client\WebClient\Play-In-Browser-Or-Phone.bat
```

This rebuilds and starts the real `06-Client` with the bridge enabled.

The console prints the exact phone URL, for example:

```text
[webclient] Phone: http://192.168.0.129:8081/
```

Keep both the command window and Java client window open.

## Original Windows EXE

For the original Jagex `runescape.exe` with its URL changed to
`http://127.0.0.1:8081/`, run `Enable-Original-Client.bat` once, then close and
reopen the EXE. This enables IE11 rendering for that executable for the current
Windows user. The page automatically selects an ES5 desktop view for its embedded
browser, with the same framebuffer, mouse and keyboard connection to Java.
Its game area stays at 765x503 pixels at 1:1 scale. The patched original EXE has
a fixed window with resizing and maximize disabled. The game remains focusable
for keyboard input without drawing a dotted focus outline.

Save the EXE from the [v1 release](https://github.com/SoloScape/SoloScape-2006/releases/tag/v1)
as `Client/WebClient/runescape.exe` or `Client/WebClient/Original/runescape.exe` (git ignores
both), start the game server, then run
`Start-Original-Client.bat`. It starts Java in the background with an off-screen
utility window, opens the EXE once the page is ready, and stops the background
client when you close the EXE. Any background client left from an earlier session
is replaced, so a stale one cannot leave the EXE on a black screen. Rebuild the
client after pulling this change. Normal desktop/phone startup stays visible.
The game server on port 43594 does not serve the webpage on port 8081; the Java
client bridge provides that page. Modern browsers keep using the existing view.

## Phone controls

- Tap = normal left click.
- Long press (about 0.45 sec) = right click / context menu.
- Drag = normal left-button drag.
- The small keyboard button opens the iPhone keyboard; characters are sent through the Java client's existing key handler.
- Landscape orientation is recommended because the native client framebuffer is 765x503.

## Current transport

Frames are lossless PNG at up to 50 FPS, matching the Java client loop. Encoding
uses a fast Sub filter and level-1 compression to reduce latency. Slow decoding
keeps the newest waiting frame rather than accumulating a playback backlog.

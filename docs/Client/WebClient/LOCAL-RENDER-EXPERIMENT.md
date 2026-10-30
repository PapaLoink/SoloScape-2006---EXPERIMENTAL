# Browser-local rendering experiment (CheerpJ 4.3; LAN WebSocket transport)

**Experimental: not yet verified as a playable mobile client.** The phone runs the
revision-443 Java renderer via CheerpJ. Linux runs the normal SoloScape game
server and a lightweight WebSocket/TCP relay; it does not render PNG frames.
The existing streamed client on port 8081 remains unchanged.

## Architecture

- On Linux, run the existing game server on TCP port **43594**.
- The launcher starts `WebGateway.java` on **8082**. It serves the JAR, cache,
  HTML, JavaScript and an HTTP-upgraded **`/game` WebSocket** on one origin.
- The gateway relays each `/game` connection to `127.0.0.1:43594` without
  modifying game protocol bytes. The HTTP file server no longer uses Python.
- On the phone, CheerpJ runs the original client and its renderer. The opt-in
  `client.transport=lan-websocket` setting makes `Signlink` return a
  `LanWebSocketSocket` instead of a conventional `Socket`. The Java wrapper
  delegates reads and writes to JavaScript native methods in
  `local-render-bridge.js`. Desktop Java sockets are unchanged.
- The client and game server require **no Tailscale, WAN address, or router
  port-forwarding**. However, CheerpJ still loads assets from its external
  CDN; a device with no internet access may not load its runtime.

## How to try it

1. Start the ordinary SoloScape game server on Linux (TCP 43594).
2. From the repository root, in another Linux terminal, run:

   ```bash
   bash Client/WebClient/Try-Local-Render.sh
   ```

3. On your phone (trusted LAN), open
   `http://<linux-lan-ip>:8082/local-render.html`.
4. Tap **Try local rendering**. The page connects back to its own host
   and port via `/game`; **no Tailscale IP or login is needed**.
5. Check browser console, Java output and the gateway terminal for startup,
   cache, connection and AWT errors. The full game may still fail to start.

The launcher builds `Client/build/client.jar`, copies the cache/JAR/page/JS
into `Client/WebClient/build/local-render/`, compiles the gateway and starts
port **8082**. It fails if 8082 is occupied rather than selecting a different
port. `LOCAL_RENDER_HTTP_PORT` and `LOCAL_RENDER_GAME_PORT` can override the
listener and upstream destination. The relay socket ignores Java `client.port`
and uses the gateway’s configured upstream port.

## Safety

Do **not** port-forward 8082 or 43594. The gateway serves the whole staged
client cache/JAR to reachable clients and has no authentication. Restrict 8082
to trusted LAN devices in your firewall. The gateway checks WebSocket
`Origin` against the page host, but this is not an authentication mechanism.
To publish beyond a trusted LAN, use HTTPS/WSS and an authenticated proxy.

## Cache and compatibility

`BrowserClientBootstrap` copies `main_file_cache.dat2` and its idx files from
the read-only `/app/cache` to writable `/files/soloscape-cache`. It only
compares file lengths, so clear browser site storage after changing a cache
file without changing its length. The UID is still generated per browser.

CheerpJ AWT rendering, audio, touch controls, full login, mobile memory
usage, and continuous gameplay **remain unvalidated**. The code uses CheerpJ
4.3 JNI native methods and its `Int8Array` ↔ `byte[]` conversion.
Run `node --test Client/WebClient/tests/local-render-bridge.test.cjs` to
check the browser-side packet bridge without running CheerpJ. Passing that
test does not establish on-device compatibility. For a working fallback, use
`bash Client/WebClient/Play-In-Browser-Or-Phone.sh` on port 8081.

## References

- https://cheerpj.com/docs/guides/Networking.html
- https://cheerpj.com/docs/guides/implementing-native-methods.html
- https://cheerpj.com/docs/reference/CJ3Library.html
- `Client/src/unpackaged/LanWebSocketSocket.java`
- `Client/WebClient/gateway/src/WebGateway.java`
- `Client/WebClient/web/local-render-bridge.js`

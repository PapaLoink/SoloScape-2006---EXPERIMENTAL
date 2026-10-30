#!/usr/bin/env bash
# Experimental CheerpJ browser-local Java rendering over a LAN WS/TCP gateway.
set -euo pipefail
web="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
client="$(cd "$web/.." && pwd -P)"
server="$(cd "$web/../../Server" && pwd -P)"
stage="$web/build/local-render"
cache="$server/cache"
port="${LOCAL_RENDER_HTTP_PORT:-8082}"
game_port="${LOCAL_RENDER_GAME_PORT:-43594}"

if [[ ! -f "$cache/main_file_cache.dat2" ]]; then
    echo "Missing game cache: $cache/main_file_cache.dat2" >&2
    exit 1
fi
for index in $(seq 0 13) 255; do
    if [[ ! -f "$cache/main_file_cache.idx$index" ]]; then
        echo "Missing game cache index: $cache/main_file_cache.idx$index" >&2
        exit 1
    fi
done

export JAVA_HOME
JAVA_HOME=$(bash "$web/../../Setup-Java-8.sh")
export PATH="$JAVA_HOME/bin:$PATH"
bash "$client/Build-Client.sh"
mkdir -p "$stage/cache" "$web/build/gateway-classes"
cp "$client/build/client.jar" "$stage/client.jar"
for page in local-render.html mobile.html desktop.html; do
    cp "$web/web/$page" "$stage/$page"
done
cp "$web/web/local-render-bridge.js" "$stage/local-render-bridge.js"
cp "$cache/main_file_cache.dat2" "$stage/cache/main_file_cache.dat2"
for index in $(seq 0 13) 255; do
    cp "$cache/main_file_cache.idx$index" "$stage/cache/main_file_cache.idx$index"
done

# CheerpJ cannot enumerate the HTTP /app mount, so publish an explicit map list.
mkdir -p "$stage/cache/map-fallback"
: > "$stage/cache/map-fallback/manifest.txt"
for fallback in "$cache"/map-fallback/l*.dat; do
    [[ -f "$fallback" ]] || continue
    name=${fallback##*/}
    cp "$fallback" "$stage/cache/map-fallback/$name"
    printf '%s\n' "$name" >> "$stage/cache/map-fallback/manifest.txt"
done
[[ -s "$stage/cache/map-fallback/manifest.txt" ]] ||
    { echo "Missing location map fallbacks in $cache/map-fallback" >&2; exit 1; }

"$JAVA_HOME/bin/javac" -encoding UTF-8 -d "$web/build/gateway-classes" "$web/gateway/src/WebGateway.java"

if [[ "${1:-}" == --prepare-only ]]; then
    echo "Browser-local cache and gateway prepared in $stage"
    exit 0
fi

echo
echo "EXPERIMENTAL browser-local Java client, LAN WebSocket transport (not PNG streaming)"
echo "Phone: http://<linux-lan-ip>:$port/mobile.html"
echo "Desktop: http://127.0.0.1:$port/desktop.html"
echo "No Tailscale required; the gateway relays /game to 127.0.0.1:$game_port."
echo "Keep the SoloScape game server running on port $game_port."
echo "The page also downloads CheerpJ from its CDN (internet required)."
echo "Do not port-forward or expose $port to the public Internet."
echo "Press Ctrl+C to stop the gateway."
echo

# Same origin on port 8082 serves cache/JAR and WS/TCP relay; no Linux renderer.
# Strict port selection prevents an unexpected port change if 8082 is occupied.
exec "$JAVA_HOME/bin/java" -cp "$web/build/gateway-classes" WebGateway \
    0.0.0.0 "$port" 127.0.0.1 "$game_port" "$stage" strict

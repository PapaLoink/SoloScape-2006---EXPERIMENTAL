#!/usr/bin/env bash
set -euo pipefail
web=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)
client=$(cd "$web/.." && pwd -P)
server=$(cd "$web/../../Server" && pwd -P)
url=http://127.0.0.1:8081/
open_browser() {
    if command -v xdg-open >/dev/null 2>&1 && [[ -n "${DISPLAY:-}${WAYLAND_DISPLAY:-}" ]]; then
        xdg-open "$url" >/dev/null 2>&1 &
    else
        echo "Open $url in a browser."
    fi
}
if command -v curl >/dev/null 2>&1 && curl -fsS --max-time 2 "$url" 2>/dev/null | grep -q '/windows-client.js'; then
    echo "Reusing existing browser client on port 8081."
    open_browser
    exit 0
fi
[[ -f "$server/cache/main_file_cache.dat2" ]] ||
    { echo "Missing server cache in $server/cache" >&2; exit 1; }
export JAVA_HOME
JAVA_HOME=$(bash "$web/../../Setup-Java-8.sh")
export PATH="$JAVA_HOME/bin:$PATH"
bash "$client/Build-Client.sh"
cd "$client"
echo "Starting SoloScape browser client: $url"
echo "The renderer stays attached to this terminal. Ctrl+C stops it."
# Open only after the Java bridge has bound its HTTP port. Opening earlier
# can leave Chrome/Firefox stuck on a connection-refused page.
if command -v xdg-open >/dev/null 2>&1 && [[ -n "${DISPLAY:-}${WAYLAND_DISPLAY:-}" ]]; then
    (
        for ((attempt=0; attempt<120; attempt++)); do
            if command -v curl >/dev/null 2>&1 &&
               curl -fsS --max-time 2 "$url" 2>/dev/null | grep -q '/windows-client.js'; then
                xdg-open "$url" >/dev/null 2>&1 || true
                exit 0
            fi
            sleep 1
        done
        echo "Browser bridge did not become ready automatically; open $url manually." >&2
    ) &
else
    echo "Open $url in a desktop browser when the renderer is ready."
fi
exec "$JAVA_HOME/bin/java" \
    -Dclient.background=true -Dweb.audio.remote=true \
    -Dweb.bridge.openBrowser=false -Dweb.bridge.strictPort=true \
    -XX:ErrorFile=client-web-hs-err.log -Dweb.bridge.port=8081 \
    "-Dweb.bridge.root=$web/web" "-Dclient.cache=$server/cache" \
    -Dclient.host=127.0.0.1 -Dclient.port=43594 \
    -jar build/client.jar 1 local live highmem members english "$@"

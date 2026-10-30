#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)
export JAVA_HOME
JAVA_HOME=$(bash "$root/../Setup-Java-8.sh")
export PATH="$JAVA_HOME/bin:$PATH"
cd "$root"
if [[ ! -f discord-relay.properties && -f discord-relay.properties.example ]]; then
    cp discord-relay.properties.example discord-relay.properties
    echo "Created discord-relay.properties from the example."
fi
for fix in server-totallevelfix.jar server-regionfix.jar; do
    [[ ! -f "dist/$fix" ]] || mv -f "dist/$fix" dist/server.jar
done
if [[ ! -f dist/server.jar ]]; then
    echo "Server/dist/server.jar not found. Run bash Server/Build-Server.sh first." >&2
    exit 1
fi
export PRS_AUDIT443="${PRS_AUDIT443:-true}"
auto=false
if [[ "${1:-}" == --auto-start ]]; then auto=true; shift; fi
echo "Revision 443 audit: $PRS_AUDIT443; auto-start: $auto"
exec "$JAVA_HOME/bin/java" -Xmx1024m \
    "-Dprs.audit443=$PRS_AUDIT443" \
    -Dprs.traceGameplay=true -Dprs.exitOnServerShutdown=true \
    "-Dprs.dev.autoStart=$auto" -jar dist/server.jar "$@"

#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)
export JAVA_HOME
JAVA_HOME=$(bash "$root/../Setup-Java-8.sh")
export PATH="$JAVA_HOME/bin:$PATH"
cd "$root"
cache="$root/../Server/cache"
[[ -f "$cache/main_file_cache.dat2" ]] || { echo "Server cache missing: $cache/main_file_cache.dat2" >&2; exit 1; }
[[ -d "$cache/map-fallback" ]] || { echo "Map fallback missing: $cache/map-fallback" >&2; exit 1; }
if [[ ! -f build/client.jar ]]; then bash "$root/Build-Client.sh"; fi
args=("$@")
if [[ ${#args[@]} -eq 0 ]]; then args=(1 local live highmem members english); fi
echo "Client output: $root/client.log"
printf 'Client started at %s\n' "$(date -Iseconds)" > client.log
set +e
"$JAVA_HOME/bin/java" -Dsun.java2d.d3d=false \
    -XX:ErrorFile=client-hs-err.log \
    "-Dclient.cache=$cache" -Dclient.host=127.0.0.1 -Dclient.port=43594 \
    -jar build/client.jar "${args[@]}" >> client.log 2>&1
status=$?
set -e
cat client.log
echo "Client exited with code $status"
exit "$status"

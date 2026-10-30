#!/usr/bin/env bash
# Cross-platform Java 8 HotSwap watcher, used by Linux dev launchers.
set -euo pipefail
if [[ $# -lt 1 || ( "$1" != client && "$1" != server ) ]]; then
    echo "Usage: bash tools/linux-dev.sh {client|server} [--prepare-only] [--mcp-port PORT] [client args...]" >&2
    exit 2
fi
component=$1
shift
project=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)
root="$project/${component^}"
export JAVA_HOME
JAVA_HOME=$(bash "$project/Setup-Java-8.sh")
export PATH="$JAVA_HOME/bin:$PATH"
cd "$root"
prepare=false
port=7780
args=()
while [[ $# -gt 0 ]]; do
    case "$1" in
        --prepare-only|-PrepareOnly) prepare=true; shift ;;
        --mcp-port|-McpPort)
            [[ $# -ge 2 && "$2" =~ ^[0-9]+$ && "$2" -ge 1 && "$2" -le 65535 ]] ||
                { echo "Invalid MCP port" >&2; exit 2; }
            port=$2; shift 2 ;;
        *) args+=("$1"); shift ;;
    esac
done
if [[ "$component" == client ]]; then
    src=src
    [[ -f "$project/Server/cache/main_file_cache.dat2" ]] || {
        if [[ "$prepare" != true ]]; then echo "Missing Server/cache/main_file_cache.dat2" >&2; exit 1; fi
    }
else
    src=src/main/java
fi
build="$root/build"
classes="$build/classes"
agent_classes="$build/hotswap-agent-classes"
agent_jar="$build/hotswap-agent.jar"
flag="$build/hotswap-restart.flag"
mkdir -p "$classes" "$build"
write_sources() {
    find "$src" -type f -name '*.java' -print0 | sort -z |
        while IFS= read -r -d '' file; do printf '"%s"\n' "$file"; done > "$build/dev-sources.txt"
    [[ -s "$build/dev-sources.txt" ]]
}
compile_all() {
    write_sources || { echo "No sources to compile" >&2; return 1; }
    echo "[Dev] Compiling $component Java sources..."
    if [[ "$component" == server ]]; then
        javac -encoding UTF-8 -Xmaxerrs 5000 -cp 'lib/*' -d "$classes" @"$build/dev-sources.txt"
    else
        javac -encoding UTF-8 -d "$classes" @"$build/dev-sources.txt"
        if [[ -d src/assets ]]; then
            mkdir -p "$classes/assets"
            cp -a src/assets/. "$classes/assets/"
        fi
    fi
}
build_agent() {
    rm -rf "$agent_classes"
    mkdir -p "$agent_classes"
    javac -encoding UTF-8 -d "$agent_classes" dev/hotswap/HotSwapAgent.java
    jar cfm "$agent_jar" dev/hotswap/MANIFEST.MF -C "$agent_classes" .
}
compile_all
build_agent
if [[ "$prepare" == true ]]; then
    echo "[Dev] HotSwap $component build prepared successfully."
    exit 0
fi
if [[ "$component" == client && ${#args[@]} -eq 0 ]]; then
    args=(1 local live highmem members english)
fi
snapshot() {
    find "$src" -type f -name '*.java' -printf '%p %T@ %s\n' | LC_ALL=C sort
}
start_process() {
    rm -f "$flag"
    echo "[Dev] Starting $component with HotSwap..."
    if [[ "$component" == server ]]; then
        export PRS_AUDIT443="${PRS_AUDIT443:-true}"
        java -Xmx1024m "-Dprs.audit443=$PRS_AUDIT443" \
          -Dprs.traceGameplay=true -Dprs.exitOnServerShutdown=true \
          -Dprs.dev.hotReload=true -Dprs.dev.autoStart=true \
          "-Dprs.dev.restartFlag=$flag" "-javaagent:$agent_jar=$classes;$flag" \
          -cp "$classes:$root/lib/*" com.rs2.launcher.ControlPanel &
    else
        echo "[Dev] Developer dashboard: http://127.0.0.1:$port/"
        java -Dsun.java2d.d3d=false "-XX:ErrorFile=$root/client-hs-err.log" \
          "-Dclient.cache=$project/Server/cache" -Dclient.host=127.0.0.1 \
          -Dclient.port=43594 "-Dsoloscape.mcp.port=$port" \
          "-javaagent:$agent_jar=$classes;$flag" -cp "$classes" \
          unpackaged.Client "${args[@]}" &
    fi
    pid=$!
}
stop_child() {
    if [[ -n "${pid:-}" ]] && kill -0 "$pid" 2>/dev/null; then
        kill "$pid" 2>/dev/null || true
        wait "$pid" 2>/dev/null || true
    fi
}
trap stop_child EXIT INT TERM
old_snapshot=$(snapshot)
start_process
while kill -0 "$pid" 2>/dev/null; do
    sleep 1
    if [[ -s "$flag" ]]; then
        echo "[Dev] Structural class edit detected; restarting $component."
        stop_child
        rm -rf "$classes"
        mkdir -p "$classes"
        compile_all
        old_snapshot=$(snapshot)
        start_process
        continue
    fi
    current=$(snapshot)
    if [[ "$current" != "$old_snapshot" ]]; then
        echo "[Dev] Source change detected: recompiling $component..."
        if compile_all; then
            old_snapshot="$current"
            echo "[Dev] Compiled changes; JVM HotSwap will reload compatible classes."
        else
            echo "[Dev] Compilation failed; retrying when sources change." >&2
            old_snapshot="$current"
        fi
    fi
done
wait "$pid" || exit $?

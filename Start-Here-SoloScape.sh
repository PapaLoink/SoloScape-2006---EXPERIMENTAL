#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)
cd "$root"
usage() {
    echo "Usage: bash Start-Here-SoloScape.sh {build [client|server]|run|dev|clean|update}"
}
build() {
    case "${1:-all}" in
        all) bash Server/Build-Server.sh; bash Client/Build-Client.sh ;;
        client) bash Client/Build-Client.sh ;;
        server) bash Server/Build-Server.sh ;;
        *) usage >&2; exit 2 ;;
    esac
}
first_pid=''
second_pid=''
stop_launchers() {
    local p
    for p in "$second_pid" "$first_pid"; do
        if [[ -n "$p" ]]; then
            kill "$p" 2>/dev/null || true
        fi
    done
}
launch_pair() {
    local command=$1
    if [[ "$command" == run ]]; then
        build all
        bash Server/Start-Server.sh --auto-start &
    else
        bash Server/Start-Server-Dev-Mode.sh &
    fi
    first_pid=$!
    trap stop_launchers EXIT INT TERM
    echo "Starting server; allowing ten seconds before client launch..."
    sleep 10
    if ! kill -0 "$first_pid" 2>/dev/null; then
        echo "Server exited before the client could start." >&2
        wait "$first_pid"
        return 1
    fi
    if [[ "$command" == run ]]; then
        bash Client/Play-Client.sh &
    else
        bash Client/Start-Client-Dev-Mode.sh &
    fi
    second_pid=$!
    echo "Both processes are running. Ctrl+C stops both."
    wait "$second_pid" || true
    stop_launchers
    wait "$first_pid" 2>/dev/null || true
}
clean() {
    # Only generated outputs. Never touch caches, config, saved players or libs.
    rm -rf Client/build Server/build Server/build-release Server/dist
    rm -f Client/Play-Client.bat Server/Start-Server.bat \
        Play-Client.bat Start-Server.bat
    echo "Cleaned build outputs; game data and Linux scripts are preserved."
}
run_command() {
    case "${1:-}" in
        build) shift; build "${1:-all}" ;;
        run|dev) launch_pair "$1" ;;
        clean) clean ;;
        update) git -C "$root" pull --ff-only ;;
        *) usage >&2; exit 2 ;;
    esac
}
if [[ $# -gt 0 ]]; then run_command "$@"; exit $?; fi
while true; do
    printf '\nSoloScape — Linux launcher\n'
    printf '1. First-time setup (build client and server)\n'
    printf '2. Build client\n3. Build server\n4. Play\n'
    printf '5. Developer mode (HotSwap)\n6. Clean generated builds\n'
    printf '7. Update from Git (fast-forward only)\n0. Exit\n'
    read -r -p "Select an option: " selection || exit 0
    case "$selection" in
        1) run_command build ;;
        2) run_command build client ;;
        3) run_command build server ;;
        4) run_command run ;;
        5) run_command dev ;;
        6) run_command clean ;;
        7) run_command update ;;
        0) exit 0 ;;
        *) echo "Choose 0-7." ;;
    esac
done

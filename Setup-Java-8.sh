#!/usr/bin/env bash
# Print the selected Java 8 JDK directory to stdout; diagnostics go to stderr.
set -euo pipefail

java8_home() {
    local home="${1%/}" output tool
    [[ -x "$home/bin/java" && -x "$home/bin/javac" && -x "$home/bin/jar" ]] || return 1
    for tool in java javac; do
        output=$("$home/bin/$tool" -version 2>&1) || return 1
        if [[ "$tool" == java ]]; then
            [[ "$output" =~ (java|openjdk)[[:space:]]+version[[:space:]]+\"?1\.8\.0 ]] || return 1
        else
            [[ "$output" =~ javac[[:space:]]+1\.8\.0 ]] || return 1
        fi
    done
}

choose_java8() {
    local candidate javac_path managed="$HOME/.local/share/SoloScape/java8"
    local -a candidates=()
    [[ -n "${JAVA_HOME:-}" ]] && candidates+=("$JAVA_HOME")
    if command -v javac >/dev/null 2>&1; then
        javac_path=$(readlink -f "$(command -v javac)") || true
        [[ -n "$javac_path" ]] && candidates+=("$(dirname "$(dirname "$javac_path")")")
    fi
    shopt -s nullglob
    candidates+=("$managed"/temurin8-*/ "$HOME"/.jdks/*/ /usr/lib/jvm/*/ /opt/java/*/ /opt/jdk*/ )
    shopt -u nullglob
    for candidate in "${candidates[@]}"; do
        if java8_home "$candidate"; then
            printf '%s\n' "$(cd "$candidate" && pwd -P)"
            return 0
        fi
    done
    return 1
}

if [[ "$(uname -s)" != Linux ]]; then
    echo "Linux Java 8 setup requires Linux. Use Setup-Java-8.bat on Windows." >&2
    exit 1
fi

if choose_java8; then exit 0; fi

echo "No Java 8 JDK found. Installing Eclipse Temurin 8 for this user..." >&2
for tool in python3 tar; do
    command -v "$tool" >/dev/null 2>&1 || { echo "Install $tool or a Java 8 JDK and set JAVA_HOME." >&2; exit 1; }
done
if command -v curl >/dev/null 2>&1; then
    fetch() { curl --fail --location --silent --show-error --connect-timeout 20 --max-time 600 "$1" -o "$2"; }
elif command -v wget >/dev/null 2>&1; then
    fetch() { wget -q --timeout=600 -O "$2" "$1"; }
else
    echo "Install curl or wget, or install Java 8 manually and set JAVA_HOME." >&2
    exit 1
fi

case "$(uname -m)" in
    x86_64|amd64) arch=x64 ;;
    aarch64|arm64) arch=aarch64 ;;
    *) echo "Unsupported CPU architecture; install Java 8 manually." >&2; exit 1 ;;
esac

managed="$HOME/.local/share/SoloScape/java8"
mkdir -p "$managed"
temp=$(mktemp -d "$managed/.setup.XXXXXXXX")
trap 'rm -rf "$temp"' EXIT
api="https://api.adoptium.net/v3/assets/latest/8/hotspot?architecture=$arch&image_type=jdk&os=linux&vendor=eclipse"
fetch "$api" "$temp/metadata.json"
# Fail closed if metadata does not point to an expected Temurin release asset.
IFS=$'\t' read -r download checksum < <(
    python3 - "$temp/metadata.json" <<'PY'
import json, sys
with open(sys.argv[1], encoding="utf-8") as source:
    assets = json.load(source)
binary = assets[0]["binary"]["package"]
print(binary["link"] + "\t" + binary["checksum"])
PY
)
if [[ ! "$download" == https://github.com/adoptium/temurin8-binaries/releases/download/*.tar.gz ]] ||
   [[ ! "$checksum" =~ ^[0-9a-fA-F]{64}$ ]]; then
    echo "Unexpected Temurin metadata; refusing unsigned installation." >&2
    exit 1
fi
fetch "$download" "$temp/temurin8.tar.gz"
python3 - "$temp/temurin8.tar.gz" "$checksum" <<'PY'
import hashlib, sys
sha = hashlib.sha256()
with open(sys.argv[1], "rb") as binary:
    for block in iter(lambda: binary.read(1024 * 1024), b""):
        sha.update(block)
if sha.hexdigest().lower() != sys.argv[2].lower():
    raise SystemExit("Temurin download failed its SHA-256 check")
PY
mkdir "$temp/extracted"
# The archive is checksum-verified against trusted Temurin release metadata.
tar -xzf "$temp/temurin8.tar.gz" -C "$temp/extracted"
for candidate in "$temp"/extracted/*; do
    if [[ -d "$candidate" ]] && java8_home "$candidate"; then
        dest="$managed/temurin8-$arch"
        if [[ -d "$dest" ]]; then rm -rf "$dest"; fi
        mv "$candidate" "$dest"
        echo "Installed Temurin Java 8 at $dest" >&2
        printf '%s\n' "$dest"
        exit 0
    fi
done
echo "The Temurin archive did not contain a valid Java 8 JDK." >&2
exit 1

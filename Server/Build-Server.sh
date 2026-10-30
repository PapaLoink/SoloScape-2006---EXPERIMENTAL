#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)
export JAVA_HOME
JAVA_HOME=$(bash "$root/../Setup-Java-8.sh")
export PATH="$JAVA_HOME/bin:$PATH"
cd "$root"

build=build-release
classes="$build/classes"
dist=dist
mkdir -p "$dist"
rm -rf "$build"
mkdir -p "$classes"
trap 'echo "ERROR: server build failed; inspect Server/build-release." >&2' ERR
find src/main/java -type f -name '*.java' -print0 | sort -z |
    while IFS= read -r -d '' src; do printf '"%s"\n' "$src"; done > "$build/sources.txt"
[[ -s "$build/sources.txt" ]] || { echo "No server sources found." >&2; exit 1; }
echo "Compiling server with Java 8..."
"$JAVA_HOME/bin/javac" -Xmaxerrs 5000 -classpath 'lib/*' -d "$classes" @"$build/sources.txt"
cat > "$build/MANIFEST.MF" <<'MANIFEST'
Manifest-Version: 1.0
Class-Path: ../lib/commons-compress-1.0.jar ../lib/javac++.jar ../lib/joda-time-2.3.jar ../lib/sqlite-jdbc-3.53.4.0.jar ../lib/slf4j-api-2.0.17.jar ../lib/slf4j-nop-2.0.17.jar
Main-Class: com.rs2.launcher.ControlPanel

MANIFEST
"$JAVA_HOME/bin/jar" cfm "$build/server.jar" "$build/MANIFEST.MF" -C "$classes" .
mv -f "$build/server.jar" "$dist/server.jar"
rm -rf "$build"
trap - ERR
echo "Server build complete: Server/dist/server.jar"
echo "Launch with: bash Server/Start-Server.sh"

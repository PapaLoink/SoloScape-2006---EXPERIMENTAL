#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)
export JAVA_HOME
JAVA_HOME=$(bash "$root/../Setup-Java-8.sh")
export PATH="$JAVA_HOME/bin:$PATH"
cd "$root"
mkdir -p build/classes
find src -type f -name '*.java' -print0 | sort -z |
    while IFS= read -r -d '' src; do printf '"%s"\n' "$src"; done > build/sources.txt
[[ -s build/sources.txt ]] || { echo "No client sources found." >&2; exit 1; }
echo "Compiling client with Java 8..."
"$JAVA_HOME/bin/javac" -encoding UTF-8 -d build/classes @build/sources.txt
if [[ -d src/assets ]]; then
    mkdir -p build/classes/assets
    cp -a src/assets/. build/classes/assets/
fi
"$JAVA_HOME/bin/jar" cfe build/client.jar unpackaged.Client -C build/classes .
echo "Client build complete: Client/build/client.jar"
echo "Launch with: bash Client/Play-Client.sh"

#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)
echo "The original Windows runescape.exe cannot run natively on Linux."
echo "Launching the equivalent Linux browser/phone renderer instead."
exec bash "$root/Play-In-Browser-Or-Phone.sh" "$@"

#!/bin/sh
set -eu
cd "$(dirname "$0")"
out=${1:-dist}
mkdir -p "$out"
out=$(cd "$out" && pwd)
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT HUP INT TERM
curl -fLsS --retry 3 -o "$tmp/rsprox-launcher.jar" \
  https://github.com/blurite/rsprox/releases/download/v1.0/rsprox-launcher.jar
actual=$(shasum -a 256 "$tmp/rsprox-launcher.jar" | cut -d ' ' -f 1)
expected=44c60c78809e83a39173d9ba780928091f89defc17acd90ad81e44b85048ce11
if [ "$actual" != "$expected" ]; then
  echo 'RSProx download hash does not match the reviewed release.' >&2
  exit 1
fi
javac --release 21 -d "$tmp" bundle/ScapeMarLauncher.java
for platform in macos windows linux; do
  dir="$tmp/ScapeMar-$platform"
  mkdir -p "$dir"
  cp "$tmp/ScapeMarLauncher.class" "$tmp/rsprox-launcher.jar" \
    proxy-targets.yaml bundle/README.txt bundle/RSProx-LICENSE.txt "$dir/"
  case "$platform" in
    macos) cp 'bundle/Launch ScapeMar.command' "$dir/" ;;
    windows) cp 'bundle/Launch ScapeMar.bat' "$dir/" ;;
    linux) cp 'bundle/Launch ScapeMar.sh' "$dir/" ;;
  esac
  (cd "$tmp" && zip -q -r "$out/ScapeMar-$platform.zip" "ScapeMar-$platform")
done
(cd "$out" && shasum -a 256 ./ScapeMar-*.zip > SHA256SUMS.txt)

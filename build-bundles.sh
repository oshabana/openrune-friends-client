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
fetch() {
  curl -fLsS --retry 3 -o "$tmp/lib/$(basename "$1")" "$1"
  echo "$2  $tmp/lib/$(basename "$1")" | shasum -a 256 -c --quiet -
}
mkdir -p "$tmp/lib" "$tmp/plugin"
fetch https://repo.runelite.net/net/runelite/client/1.12.39/client-1.12.39.jar 438e3d0c13601e5bbf6e9b247b9548a46f665091f32ed20be1c07244858b0c13
fetch https://repo.runelite.net/net/runelite/runelite-api/1.12.39/runelite-api-1.12.39.jar 3e2fea69e3996a3e5c4dc1a2326b686561f594b47c3afe1754362db8d4c2037a
fetch https://repo1.maven.org/maven2/javax/inject/javax.inject/1/javax.inject-1.jar 91c77044a50c481636c32d916fd89c9118a72195390452c81065080f957de7ff
fetch https://repo1.maven.org/maven2/com/google/inject/guice/4.1.0/guice-4.1.0.jar 9b9df27a5b8c7864112b4137fd92b36c3f1395bfe57be42fedf2f520ead1a93e
javac --release 11 -proc:none -cp "$tmp/lib/*" -d "$tmp/plugin" \
  bundle/plugin/src/dev/scapemar/login/ScapeMarLoginPlugin.java
(cd "$tmp/plugin" && jar cf "$tmp/ScapeMar-Login.jar" dev)
for platform in macos windows linux; do
  dir="$tmp/ScapeMar-$platform"
  mkdir -p "$dir"
  cp "$tmp/ScapeMarLauncher.class" "$tmp/rsprox-launcher.jar" "$tmp/ScapeMar-Login.jar" \
    proxy-targets.yaml bundle/README.txt bundle/RSProx-LICENSE.txt "$dir/"
  case "$platform" in
    macos) cp 'bundle/Launch ScapeMar.command' "$dir/" ;;
    windows) cp 'bundle/Launch ScapeMar.bat' "$dir/" ;;
    linux) cp 'bundle/Launch ScapeMar.sh' "$dir/" ;;
  esac
  (cd "$tmp" && zip -q -r "$out/ScapeMar-$platform.zip" "ScapeMar-$platform")
done
(cd "$out" && shasum -a 256 ./ScapeMar-*.zip > SHA256SUMS.txt)

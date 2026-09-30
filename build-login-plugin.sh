#!/bin/sh
# Builds ScapeMar-Login.jar. Usage: build-login-plugin.sh [output jar]
set -eu
cd "$(dirname "$0")"
jar_out=${1:-dist/ScapeMar-Login.jar}
mkdir -p "$(dirname "$jar_out")"
jar_out=$(cd "$(dirname "$jar_out")" && pwd)/$(basename "$jar_out")
lib=dist/lib
mkdir -p "$lib"
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT HUP INT TERM
fetch() {
  file="$lib/$(basename "$1")"
  if ! echo "$2  $file" | shasum -a 256 -c --quiet - > /dev/null 2>&1; then
    curl -fLsS --retry 3 -o "$file" "$1"
    echo "$2  $file" | shasum -a 256 -c --quiet -
  fi
}
fetch https://repo.runelite.net/net/runelite/client/1.12.39/client-1.12.39.jar 438e3d0c13601e5bbf6e9b247b9548a46f665091f32ed20be1c07244858b0c13
fetch https://repo.runelite.net/net/runelite/runelite-api/1.12.39/runelite-api-1.12.39.jar 3e2fea69e3996a3e5c4dc1a2326b686561f594b47c3afe1754362db8d4c2037a
fetch https://repo1.maven.org/maven2/javax/inject/javax.inject/1/javax.inject-1.jar 91c77044a50c481636c32d916fd89c9118a72195390452c81065080f957de7ff
fetch https://repo1.maven.org/maven2/com/google/inject/guice/4.1.0/guice-4.1.0.jar 9b9df27a5b8c7864112b4137fd92b36c3f1395bfe57be42fedf2f520ead1a93e
javac --release 11 -proc:none -cp "$lib/*" -d "$tmp" \
  bundle/plugin/src/dev/scapemar/login/ScapeMarLoginPlugin.java
(cd "$tmp" && jar cf "$jar_out" dev)
echo "$jar_out"

#!/bin/sh
set -eu
cd "$(dirname "$0")"
JAVA=$(/usr/libexec/java_home -v 21)/bin/java
if ! ifconfig lo0 | grep -q 'inet 127.0.255.3 '; then
  osascript -e 'do shell script "ifconfig lo0 alias 127.0.255.3 up" with administrator privileges'
fi
exec "$JAVA" -cp . ScapeMarLauncher

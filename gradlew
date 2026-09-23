#!/bin/sh
# KeyPass Gradle wrapper bootstrap. Requires Java 21+ and internet on first run.
set -e
DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION="8.14.3"
CACHE="$DIR/.gradle-wrapper"
DIST="$CACHE/gradle-$GRADLE_VERSION"
ZIP="$CACHE/gradle-$GRADLE_VERSION-bin.zip"
if [ ! -x "$DIST/bin/gradle" ]; then
  mkdir -p "$CACHE"
  if [ ! -f "$ZIP" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    curl -fL "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP"
  fi
  rm -rf "$DIST.tmp"
  mkdir -p "$DIST.tmp"
  unzip -q "$ZIP" -d "$DIST.tmp"
  mv "$DIST.tmp/gradle-$GRADLE_VERSION" "$DIST"
  rm -rf "$DIST.tmp"
fi
exec "$DIST/bin/gradle" "$@"

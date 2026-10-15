#!/usr/bin/env bash
set -euo pipefail
GRADLE_VERSION="8.10.2"
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
CACHE_DIR="${HOME}/.gradle-bootstrap"
DIST_DIR="$CACHE_DIR/gradle-$GRADLE_VERSION"
ZIP="$CACHE_DIR/gradle-$GRADLE_VERSION-bin.zip"
mkdir -p "$CACHE_DIR"
if [ ! -x "$DIST_DIR/bin/gradle" ]; then
  echo "Gradle not preinstalled; downloading Gradle $GRADLE_VERSION..."
  curl -fL --retry 3 --retry-delay 2 \
    "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" \
    -o "$ZIP"
  rm -rf "$DIST_DIR"
  unzip -q "$ZIP" -d "$CACHE_DIR"
fi
exec "$DIST_DIR/bin/gradle" "$@"

#!/usr/bin/env bash
set -euo pipefail
UPSTREAM="https://github.com/streetpea/chiaki-ng.git"
DEST="third_party/chiaki/upstream"
REF="${CHIAKI_REF:-main}"
rm -rf "$DEST"
git clone --filter=blob:none --recurse-submodules --shallow-submodules "$UPSTREAM" "$DEST"
git -C "$DEST" checkout "$REF"
git -C "$DEST" submodule update --init --recursive --depth 1
git -C "$DEST" rev-parse HEAD | tee third_party/chiaki/UPSTREAM_COMMIT
echo "Vendored chiaki-ng at $(cat third_party/chiaki/UPSTREAM_COMMIT)"

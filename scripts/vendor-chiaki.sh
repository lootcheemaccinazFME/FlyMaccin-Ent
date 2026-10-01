#!/usr/bin/env bash
set -euo pipefail
UPSTREAM="https://github.com/streetpea/chiaki-ng.git"
DEST="third_party/chiaki/upstream"
REF="${CHIAKI_REF:-a9a2805884cfa83865fdfcc09ca3ddfcd628aa42}"

rm -rf "$DEST"
git clone --filter=blob:none --recurse-submodules --shallow-submodules "$UPSTREAM" "$DEST"
git -C "$DEST" fetch --depth 1 origin "$REF"
git -C "$DEST" checkout --detach FETCH_HEAD
git -C "$DEST" submodule update --init --recursive --depth 1

ACTUAL="$(git -C "$DEST" rev-parse HEAD)"
if [[ "$ACTUAL" != "$REF" ]]; then
  echo "FME ERROR: chiaki-ng pin mismatch: expected $REF, got $ACTUAL" >&2
  exit 1
fi
printf '%s\n' "$ACTUAL" | tee third_party/chiaki/UPSTREAM_COMMIT
echo "Vendored chiaki-ng exact commit $ACTUAL"

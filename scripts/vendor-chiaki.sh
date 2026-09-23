#!/usr/bin/env bash
set -euo pipefail
UPSTREAM="https://github.com/streetpea/chiaki-ng.git"
DEST="third_party/chiaki/upstream"
REF="${CHIAKI_REF:-v1.10.0}"

rm -rf "$DEST"
git clone --filter=blob:none --recurse-submodules --shallow-submodules "$UPSTREAM" "$DEST"

# The initial clone may not contain older tag refs. Fetch the requested ref
# explicitly before checkout so pinned releases work reliably in CI.
if ! git -C "$DEST" checkout --detach "$REF" 2>/dev/null; then
  git -C "$DEST" fetch --depth 1 origin "refs/tags/$REF:refs/tags/$REF" || \
  git -C "$DEST" fetch --depth 1 origin "$REF"
  git -C "$DEST" checkout --detach FETCH_HEAD
fi

git -C "$DEST" submodule update --init --recursive --depth 1
git -C "$DEST" rev-parse HEAD | tee third_party/chiaki/UPSTREAM_COMMIT
echo "Vendored chiaki-ng ref $REF at $(cat third_party/chiaki/UPSTREAM_COMMIT)"

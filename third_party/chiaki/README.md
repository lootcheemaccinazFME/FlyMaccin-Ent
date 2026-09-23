# Chiaki protocol integration boundary

FME Agent will integrate the open-source Chiaki protocol library behind the Android JNI boundary.

Upstream references:
- Original Chiaki: https://github.com/thestr4ng3r/chiaki
- chiaki-ng: https://github.com/streetpea/chiaki-ng

License: AGPL-3.0. Do not remove upstream notices. If upstream source is vendored or modified, preserve corresponding-source obligations and record the exact commit here.

## Android integration mode

The current chiaki-ng CMake project exposes CHIAKI_ENABLE_ANDROID for Gradle-project builds. FME keeps its UI/application code separate and calls the protocol core through JNI.

## Pin
UPSTREAM_COMMIT=PENDING
STATUS=INTEGRATION_BOUNDARY_CREATED

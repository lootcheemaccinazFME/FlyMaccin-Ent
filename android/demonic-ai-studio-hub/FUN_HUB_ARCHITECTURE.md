# Demonic Fun Hub room architecture

Demonic Fun Hub is the building. FME products are rooms, not launcher tiles.

## Contract
- One native Hub process owns navigation, shared identity, project/session state and room lifecycle.
- A room is an in-process feature surface. It must not exist solely to launch another FME APK.
- Native engines are migrated behind room adapters instead of replaced with WebViews.
- Network-backed products remain rooms, but their remote dependency is disclosed inside the room.
- Third-party integrations such as Sony PS Remote Play may hand off externally from inside a room.
- Standalone APKs remain useful for QA and backwards compatibility until each engine is fully migrated.

## Registered rooms
Demonic Studio, Demonic DAW, Demonic DAW 2, FME Universal Emulator, 8-Bit Ism, HY-PHYXELS: Arcade Invasion, Bay Carnival3 Parade, FME Games / HY-PHYXELS, FME Agent, Pocket Potna, Pocket Potna Remote, Demonic TV, FME AI Video Generator, Octop FME, Bay Auto RP.

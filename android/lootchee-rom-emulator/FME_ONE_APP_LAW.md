# FME ONE APP LAW

**1 APP → 0 LAUNCHER → 1 APK → EVERYTHING FME INSIDE**

## Hard architecture contract
1. Exactly one Android application ID and one installable APK represents FME.
2. FME capabilities are rooms, engines, services, views, or libraries inside that process. They are not buttons whose implementation is another FME APK.
3. No FME room may use `getLaunchIntentForPackage`, explicit intents to another FME package, or app-store/browser handoff as its FME implementation.
4. Third-party systems may be explicit integrations where technically required. A third-party integration is not represented as native FME functionality.
5. Cloud-backed FME features may call declared network services from their in-process room.
6. Emulator systems are internal cores under the Universal Emulator room.
7. Games execute in-process under the FME Games rooms.
8. DAW, media, agent, Pocket Potna, Octop, Bay Auto, AI video, and future FME products migrate behind in-process room contracts.
9. Standalone historical APKs are migration inputs / QA references only. They are not release architecture.
10. Release CI fails if the final FME product contains or depends on multiple FME launcher APKs.

## Release artifact
The canonical Android release target is one APK. Any compatibility-certification backlog must remain visible and cannot be converted into fake product completion.

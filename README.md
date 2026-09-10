# FlyMaccin Ent

Current build target: **FME Bookwriter OpenAI Studios v1.2.2**.

This repository is configured for Codemagic / GitHub Android debug APK builds.

## House architecture

The first reusable House is **Music & Media House**. It owns audio creation and entertainment through this core loop:

**Enter House → make/select beat → write lyrics → record vocals → mix/play song → save project → kick back at TV (optional).**

House state persists locally, required stages unlock in sequence, and the TV Lounge remains optional after the production loop. The same House pattern is intended for future specialized Houses such as Comic Studio, Bookwriter, Video Studio, and Visual Studio without flattening their individual workflows.

No OpenAI API keys or signing secrets are stored in this repository.

# FME Bookwriter OpenAI Studios

FME Bookwriter is an Android writing workspace for planning, researching, drafting, and organizing books and other long-form projects. Its current build is **v1.2.2** (`FME Bookwriter OpenAI Studios`).

The app combines a local project workspace with optional AI assistance through OpenAI. You can write and manage project materials without configuring an API key; AI actions require an internet connection and your own OpenAI API key.

## What you can do

- **Plan fiction or nonfiction books.** Generate and edit a structured plan, lock individual fields while revising, and approve the plan before drafting actions are enabled.
- **Draft and revise.** Work in a manuscript editor; create a blueprint, outline, or chapter list; write or continue chapters; and rewrite, expand, or improve selected text.
- **Keep project context together.** Maintain a story bible, research notes, personal input, project status, and version snapshots alongside each manuscript.
- **Organize multiple projects.** Create, duplicate, reopen, and archive projects. Undo and redo edits while working.
- **Use AI studio planning workspaces.** Build and approve staged production plans for creator/canon, music, visual, video, app/game, and research workflows.
- **Track a Music & Media House workflow.** Save notes for the core sequence—choose a beat, write lyrics, record vocals, mix/play, and save—then optionally move to the TV Lounge. House progress and notes are stored separately from book projects.

The studios are planning workspaces, not direct integrations with the third-party services named in their descriptions. AI generation in this Android build is provided through OpenAI.

## AI and data

1. In the app's **Settings**, enter your OpenAI API key and choose a model.
2. The key is encrypted with Android Keystore-backed AES-GCM storage on the device. You can clear it from Settings.
3. When you request AI assistance, the app sends the prompt to OpenAI's Responses API. Prompts can include project material and selected text needed for the requested action.
4. Book projects and studio workspaces are saved in the app's local WebView storage. House workflows use local browser storage as well.

No OpenAI API keys or signing secrets are stored in this repository. Keep in mind that AI requests require network access and are subject to the provider's policies.

## Android build

Requirements:

- Java 17
- Android SDK with platform 35 installed
- Node.js to run the source validation script

Build the installable debug APK:

```sh
./gradlew :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. To run the repository's source checks:

```sh
./VALIDATE_OPENAI_EDITION.sh
```

The Codemagic workflow runs the source checks, builds the debug APK, and publishes the APK as a workflow artifact.

## App details

- Android application ID: `com.flymaccin.bookwriter.openai`
- Minimum Android version: Android 8.0 (API 26)
- Target SDK: 35
- Version name: `1.2.2-house-core-loop`

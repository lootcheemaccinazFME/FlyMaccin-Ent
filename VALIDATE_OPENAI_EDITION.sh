#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"
node --check app/src/main/assets/app.js
node --check app/src/main/assets/core.mjs
grep -q 'applicationId = "com.flymaccin.bookwriter.openai"' app/build.gradle.kts
grep -q 'https://api.openai.com/v1/responses' app/src/main/java/com/flymaccin/bookwriter/BookwriterBridge.java
grep -q 'gpt-5.6-terra' app/src/main/assets/index.html
grep -q 'setOpenAIKey' app/src/main/java/com/flymaccin/bookwriter/BookwriterBridge.java
! grep -RniE 'generativelanguage.googleapis.com|setGeminiKey|hasGeminiKey|GEMINI_API_KEY' app >/dev/null
! grep -RniE 'sk-(proj-)?[A-Za-z0-9_-]{16,}' . --exclude='VALIDATE_OPENAI_EDITION.sh' >/dev/null
printf 'FME Bookwriter OpenAI static validation: PASS\n'

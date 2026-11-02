# FME Universal App: Successful-Build Intake Gate
Date: 2026-11-02
Status: AUDIT ONLY; no modules merged; no APK compiled.

## Dual foundations
- lootcheemaccinazFME/FME-FunHouse (Kotlin/C++ Android, applicationId com.flymaccin.funhouse). Repository source present. Historical successful Run #172 is a candidate; artifact SHA and source commit must be matched before admission.
- lootcheemaccinazFME/FME-FUN-HUB-Studio (Kotlin/Compose Android, applicationId id.soundbreaker.studio). Source present, but current successful build artifact not independently verified. NOT ADMITTED yet.

## Evidence tiers
A = artifact bytes available, cryptographic hash matches authoritative record, Android package parses, build provenance confirmed.
B = historical success report and checksum, but artifact not yet independently inspected.
C = source or historical claim only.
D = known failed, corrupted, or blocked.
Only A can enter integration. B is prioritized for retrieval. C/D are excluded.

## Historical candidates, not yet A
- FME FunHouse Run #172: historical successful build (B).
- FME AI Writer July 16: 24,445,767-byte APK; historical SHA256 prefix f4cfb05f, suffix 8359c9; reportedly installed/launched (B).
- Aippy September 15 batch: 62 APKs reported with distinct package IDs, ZIP integrity and JAR signature passes, but no device tests; verify actual artifacts individually (B).
- Demonic AI Studio / Pocket Potna: 32 APK entries with sizes and SHA256 in Drive inventory ALL_AI_APK_VERSIONS_INVENTORY_2026-09-15.txt (B; inventory does not prove current file presence).
- FME FunHouse and FUN HUB source trees: source presence alone is not build evidence (C).
- FME Everything One October 14 failed CI/runtime history (D).
- FME Bookwriter APK pipeline blocked (D until successful artifact).
- Integrated FME v0.6 191-file source ZIP: source archive, not an APK (C).
- FME Gateway backend and Core Pack assets: non-APK; independently validate before module/asset inclusion.

## Integration procedure
1. Retrieve exact candidate binaries and provenance, compute SHA256 and compare against recorded hash.
2. Validate ZIP central directory / archive extraction, inspect APK manifest, package ID, ABIs, signatures, SDK requirements and permissions.
3. Reconcile duplicated functions and versions; preserve every distinct verified feature, do not blindly bundle multiple application shells.
4. Convert validated source/functionality into one host Android app with FunHouse and FUN HUB rooms; preserve approved UI and data.
5. Build in CI; test emulator and real devices; only then mark integrated/release-ready.
6. Keep previous working artifacts immutable. No replacement of approved canon or destructive cleanup.

## Important limitation
No APK or ZIP binary has been freshly validated by this manifest. No module has been integrated or compiled.

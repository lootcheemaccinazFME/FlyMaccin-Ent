# FME Android Release Ledger

This ledger records build truth. Do not mark physical-device checks PASS until they are performed on hardware.

| Version | Commit | CI | APK | Physical phone | Notes |
|---|---|---|---|---|---|
| agent-apk-0.1-debug | 191d4babf63f6f1e75406f74d1d43762b1e2d2df | PASS | VERIFIED | PENDING | Unit tests, Android lint, assembleDebug, and artifact upload passed in GitHub Actions runs 35892063450 and 35892067485. APK SHA-256: `81805195a69290ae5963b9ef77580e49f34c3476163fa140c3dc0820449a5bb8`. |
| agent-bootstrap | pending | PENDING | PENDING | PENDING | Historical bootstrap entry; superseded by the verified agent-apk-0.1-debug candidate above. |

## Physical phone checklist template
- [ ] APK installs successfully
- [ ] App launches after clean install
- [ ] App relaunches after process kill
- [ ] PS Remote Play handoff works
- [ ] Browser navigation works
- [ ] Direct file download works
- [ ] Pause/resume download works
- [ ] Installed-app discovery works
- [ ] App launch intent works
- [ ] File access works with current Android permissions
- [ ] Controller/D-pad navigation works
- [ ] Background/resume behavior verified
- [ ] No protected-media/DRM bypass behavior

# FME Android Release Ledger

This ledger records build truth. Do not mark physical-device checks PASS until they are performed on hardware.

| Version | Commit | CI | APK | Physical phone | Notes |
|---|---|---|---|---|---|
| agent-bootstrap | pending | PENDING | PENDING | PENDING | FME Agent engineering/CI framework installed. Native Android project still needs to be created or imported. |

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

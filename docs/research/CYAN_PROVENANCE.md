# Cyan Glasses Research Provenance

## Input

The interoperability research used a user-exported installation package for the companion application used with the user's own AIMB-G1 glasses.

- App: Cyan Glasses
- Package: `com.aitowe.aitoglasses`
- Version: `1.0.2.18_20260811`
- Version code: `85`
- Exported split package filename: `Cyan Glasses-1.0.2.18_20260811-split-apks.zip`
- Size: 139,799,818 bytes
- SHA-256: `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`

## Public-repository rule

The third-party APK bytes are deliberately not committed to this public repository.

The repository stores:
- hash and version provenance,
- interoperability findings,
- protocol constants necessary for the user's own hardware,
- safe diagnostic/build code,
- no account credentials or tokens.

## Major findings retained elsewhere

See `docs/protocol/AIMB_G1_PROTOCOL.md` for:
- AIMB-G1 support evidence,
- BLE UUIDs,
- frame layout,
- CRC algorithm,
- media-mode P2P/AP payload evidence,
- local HTTP media path evidence,
- authentication-risk assessment.

## Superseded diagnostic assumptions

The earliest project phase considered Developer Options/HCI/network capture as the first physical step. Static Cyan analysis provided enough confidence to replace that with the safer read-only Discovery v0.1 APK. Historical tools remain under the v0.4 archive only for provenance.

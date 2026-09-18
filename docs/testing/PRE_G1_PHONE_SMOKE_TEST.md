# Pre-G1 Phone-Only Smoke Test

Purpose: separate Android installation/launch problems from physical AIMB-G1 discovery.

This check does **not** require the glasses and does not send any BLE command.

## Conditions

- Keep AIMB-G1 powered OFF.
- Cyan Glasses may remain installed.
- Do not unpair/reset the glasses.
- Do not change Wi-Fi settings for this check.
- Use the same Android phone intended for G1.

## Steps

1. Install:
   `releases/v0.1/K_G1_Discovery_v0_1.apk`

2. Confirm Android accepts the APK installation.

3. Open **K G1 Discovery**.

4. Confirm the screen loads and shows the read-only discovery UI.

5. Do **not** start a meaningful glasses scan yet. If Android requests Nearby Devices/Bluetooth permission, either:
   - grant it now, or
   - leave it for the actual G1 run.

6. Close the app.

## PASS

- APK installs normally.
- App launches without crashing.
- Discovery UI is visible.

## FAIL

Record:
- Android version,
- phone model,
- exact Android installation/error message,
- whether the failure happened during install or launch.

Do not modify pairing, reset the glasses, or install a different build as a workaround before reviewing the failure.

## Note about G1 scanning

Discovery v0.1 deliberately matches the advertised name `AIMB-G1` and scans for 12 seconds. If G1 later reports **not found**, retry one clean scan before changing anything. A repeated not-found result is valid diagnostic evidence and should be reviewed rather than bypassed by changing pairing/reset settings.

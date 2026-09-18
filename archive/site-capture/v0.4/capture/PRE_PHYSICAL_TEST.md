# AIMB-G1 physical capture preflight — historical v0.4 snapshot

This file records the earlier diagnostic strategy before static Cyan analysis yielded enough information to build a read-only Discovery APK.

The old plan included:
- freezing phone/app/firmware versions,
- proving Bluetooth and network capture tooling before the real test,
- using disposable media only,
- performing an offline local-transfer gate,
- collecting two correlated traces.

## Superseded workflow
For the current project, the first physical gate is simpler:

1. Force-stop Cyan Glasses.
2. Turn on AIMB-G1.
3. Install/run K G1 Discovery v0.1.
4. Grant Nearby Devices/Bluetooth permission.
5. Scan, connect, enumerate GATT services/characteristics.
6. Share the generated report.
7. Do not send a proprietary BLE command until that report is reviewed.

Developer Options, ADB and packet capture are not required for this first gate.

# AIMB-G1 G4A Wi-Fi Direct Association — PASS

Date: 2026-09-19

## Test metadata

- App: K G1 P2P Association Probe v0.4.0
- APK SHA-256: `b75d217d3b2b0c49300cdbd3beded0a199747c25638b99fb8969bb8414b3e880`
- Gate: G4A phone-side Wi-Fi Direct discovery/association only
- Internet permission: absent
- HTTP/socket/media access: not implemented
- Credential logging/persistence: disabled
- Public record: sanitized

## BLE and transfer-mode setup

The probe:
- found exactly one bonded AIMB-G1-family target,
- connected over LE GATT,
- found the Cyan service/notify/write characteristics,
- enabled notifications,
- sent the exact P2P enter command `0x41 / 02 01 04 01` once,
- received a valid transfer-credential response,
- parsed a 20-byte SSID and 9-byte password only in memory.

Credential values are intentionally omitted from the public repository.

## Wi-Fi Direct discovery

- `discoverPeers` start: SUCCESS
- peers observed: 1
- exact BLE-reported P2P peer-name match: YES
- no nearby peer names or addresses were logged

## Wi-Fi Direct association

- WPS: PBC
- `groupOwnerIntent = 0`
- connect request: SUCCESS
- P2P group formed: TRUE
- phone is group owner: TRUE
- group-owner address: `192.168.49.1`
- HTTP/socket requests performed: 0

Because the phone is the group owner, `192.168.49.1` is the phone-side P2P address and does not identify the glasses client address.

Several valid asynchronous `0x73` notifications were observed during association, but v0.4.0 deliberately did not log their payload/event types.

## Exit and cleanup

The probe sent the exact transfer-exit command `0x41 / 02 01 09` once and received a valid `0x41` exit response.

The subsequent `removeGroup` call returned reason code `2` (Android Wi-Fi Direct BUSY). This is recorded as a cleanup anomaly, not a gate failure, because:
- the bounded transfer-exit command succeeded,
- the group had already completed the tested association lifecycle,
- no HTTP/media action occurred.

## Safety outcome

- enter P2P writes: 1
- exit-transfer writes: 1
- extra proprietary writes: 0
- retry loop: none
- HTTP/socket operations: 0
- media/file access: 0
- credentials logged/persisted: no
- exact target peer matched: true

## Conclusion

**G4A PASS.**

Phone-side Wi-Fi Direct discovery and association to the exact glasses peer are physically confirmed without HTTP or media access.

## Next gate

**G4A2 — passive P2P-IP notification capture only.**

Repeat the same bounded G4A association flow, but expose only sanitized `0x73` event identifiers. If event `0x08` appears, parse its IPv4 bytes in memory and report only that a glasses client IP was resolved.

G4A2 adds:
- no new proprietary BLE command,
- no HTTP/socket request,
- no media/file access.

If `0x08` is not observed, a later separate gate may test the exact read-style `0x41 / 02 03` P2P-IP query.

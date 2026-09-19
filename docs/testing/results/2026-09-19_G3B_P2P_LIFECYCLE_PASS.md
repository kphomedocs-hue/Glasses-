# AIMB-G1 G3B P2P Transfer Lifecycle — PASS

Date: 2026-09-19

## Test metadata

- App: K G1 P2P Lifecycle Probe v0.3.1
- APK SHA-256: `1578b6540596625825c16a95da64f72f46ee29aaad47c866bb9025a3389dce7c`
- Gate: G3B bounded P2P transfer-mode lifecycle
- Maximum proprietary writes: 2
- Phone Wi-Fi/P2P/network APIs: not implemented
- HTTP/media transfer: not implemented
- Public record: sanitized

## Enter transfer mode

Command:
```text
0x41 / 02 01 04 01
```

Android write start: SUCCESS  
Android write callback: SUCCESS

The glasses returned one valid command-`0x41` response carrying:
- the echoed transfer-mode prefix,
- SSID length: **20 bytes**,
- password length: **9 bytes**,
- a 20-byte device-specific SSID,
- a 9-byte transfer password.

The credential bytes are intentionally not stored in this public repository.

This confirms that P2P transfer-mode entry causes the glasses to generate and report local network credentials.

During enter observation two valid asynchronous `0x73` event frames with event type `0x0B` were observed. Their state values are retained as raw semantics-pending evidence; they are not assigned a meaning here.

## Exit transfer mode

Command:
```text
0x41 / 02 01 09
```

Android write start: SUCCESS  
Android write callback: SUCCESS

A valid command-`0x41` exit response was observed.

A normal media-inventory `0x73 / 0x01` frame followed after exit.

## Safety outcome

- P2P enter writes attempted: 1
- transfer-exit writes attempted: 1
- total proprietary writes attempted: 2
- retries: 0
- AP-mode command: none
- phone-side Wi-Fi Direct: none
- HTTP/socket/media transfer: none
- file mutation: none
- reset/restart/OTA: none

## Conclusion

**G3B PASS.**

Physically confirmed:
1. exact Cyan P2P enter command is accepted,
2. the glasses emit local transfer credentials,
3. exact Cyan exit-transfer command is accepted,
4. BLE operation returns to normal asynchronous inventory reporting afterward.

## Next gate

Use **G4A — phone-side Wi-Fi Direct association only** before any HTTP request.

G4A may:
- enter the already confirmed P2P transfer mode,
- discover the corresponding Wi-Fi Direct peer,
- establish/inspect the local P2P connection,
- record local/group-owner IP metadata,
- exit transfer mode.

G4A must not:
- issue HTTP requests,
- read `media.config`,
- enumerate files,
- download media,
- modify/delete files,
- expose or persist transfer credentials.

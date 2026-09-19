# G4A2 Physical P2P-IP Notification Result — PASS

Date: 2026-09-19  
App: K G1 P2P IP Notify Probe v0.4.1  
Gate: G4A2 passive glasses P2P-IP notification capture only  
Result: **PASS**

## Scope

This run repeated the already proven G4A P2P association lifecycle and added only sanitized asynchronous `0x73` event-ID reporting plus IPv4 parsing for event `0x08`.

Allowed proprietary writes:
1. P2P enter: `0x41 / 02 01 04 01`, once.
2. Transfer exit: `0x41 / 02 01 09`, once.

Not used:
- `0x41 / 02 03` P2P-IP query,
- Internet permission,
- HTTP/socket access,
- media listing/download,
- file mutation,
- AP mode,
- reset/restart/OTA,
- arbitrary command input.

## Sanitized physical observations

- exactly one bonded AIMB-G1-family target was selected;
- Cyan GATT service/notify/write characteristics were present;
- notification subscription succeeded;
- P2P enter write succeeded with no retry;
- transfer-credential response was valid;
- SSID length: 20 bytes;
- password length: 9 bytes;
- credential values were not logged or persisted;
- one Wi-Fi Direct peer was observed;
- exact BLE-reported P2P peer match: yes;
- Wi-Fi Direct connect request: success;
- P2P group formed: true;
- phone is group owner: true;
- phone/group-owner address: `192.168.49.1`;
- HTTP/socket requests performed: 0.

Observed asynchronous `0x73` event IDs:
- `0x0B`,
- `0x08`,
- `0x01`.

A valid `0x73 / 0x08` event resolved the glasses-side IPv4 address as:

```text
192.168.49.176
```

The result is consistent with the verified v0.4.1 parser rule:
- event ID at raw frame index 6,
- IPv4 bytes at raw frame indices `[7..10]`.

## Exit and cleanup

- transfer exit `0x41 / 02 01 09`: success;
- exit response: valid `0x41` frame;
- BLE write callback: success;
- normal `0x73 / 0x01` reporting followed;
- `removeGroup`: Android Wi-Fi Direct reason 2 / BUSY, retained as the same non-blocking cleanup anomaly already seen at G4A.

## Privacy review

The committed evidence contains:
- no Bluetooth MAC address,
- no device-specific AIMB-G1 suffix,
- no Wi-Fi SSID/password,
- no nearby peer address,
- no account/token data,
- no user/client media.

## Conclusion

G4A2 exit criterion is met.

The glasses-side Wi-Fi Direct client IP was resolved passively from `0x73 / 0x08` without using the separate `02 03` IP query and without performing any HTTP, socket or media operation.

**G4A2: PASS.**

G4B read-only local media listing is now unblocked as a separate, independently verified gate. No G4B HTTP request has been performed at this checkpoint.

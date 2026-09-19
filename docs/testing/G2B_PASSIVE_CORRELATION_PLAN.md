# G2B Passive Event Correlation Plan

## Purpose

Resolve the semantic meaning of the spontaneous AIMB-G1 command `0x73` notifications observed during G2 without guessing and without sending a proprietary BLE control command.

This plan combines two independent evidence streams:

1. targeted Cyan parser tracing;
2. passive physical event correlation.

G3 remains blocked until this plan is reviewed.

## Starting evidence

G2 produced three spontaneous frames with no proprietary characteristic write:

```text
BC 73 03 00 52 31 05 47 00
BC 73 08 00 01 07 01 01 00 00 00 01 00 01
BC 73 03 00 53 A1 05 46 00
```

All three validate as:

```text
BC | command | len_le16 | crc16_modbus(payload) | payload
```

The semantics of `0x73`, `05 47 00`, `05 46 00` and the 8-byte payload are currently unknown.

## Track A — targeted Cyan parser tracing

Trace the official Cyan implementation in this order:

```text
BLE notification callback
→ frame accumulation / validation
→ command-byte dispatch
→ payload/subcommand dispatch
→ application state / event / UI consumer
```

Search specifically for:
- command `0x73`,
- decimal equivalent `115`,
- payload dispatch using the first payload byte,
- mappings involving observed bytes `0x46` and `0x47`,
- any initialization/time synchronization state machine,
- code executed immediately after notification subscription or BLE connection.

Rules:
- do not assign meanings from symbol names outside the actual receive path,
- distinguish direct Cyan evidence from inference,
- record file/class/method provenance for every semantic claim,
- do not commit the third-party APK or decompiled bulk source to the public repository.

## Track B — passive Event Correlator

If static analysis does not completely resolve the semantics, build a separate diagnostic whose only BLE mutation is the standard CCCD enable-notification write.

### Required behavior

- select exactly one bonded `AIMB-G1` / `AIMB-G1_*` device;
- connect over LE;
- verify `de5bf728-d711-4e47-af26-65e3012a5dc7`;
- verify and subscribe to `de5bf729-d711-4e47-af26-65e3012a5dc7`;
- observe for a bounded session;
- timestamp every notification relative to session start;
- parse only the validated generic envelope;
- show command, payload length, CRC validity and payload bytes;
- allow timestamped user event markers;
- group identical/repeated payloads;
- summarize event-to-frame timing correlations;
- sanitize device identity in exported reports.

### Suggested event markers

The UI should support neutral markers rather than hard-code an interpretation:

- `BASELINE / IDLE`
- `WEAR`
- `REMOVE`
- `PHYSICAL CONTROL ACTION`
- `CAPTURE ACTION`
- `OTHER`

Only include a marker that corresponds to a safe action actually available on the glasses. The marker records what the user did; it does not claim what a packet means.

### Repetition rule

For a candidate mapping:
- repeat the same action at least 3 times when practical,
- separate repetitions with a baseline interval,
- compare timing and payload consistency,
- treat one-off coincidences as inconclusive,
- record negative trials where an action produces no matching packet.

A strong physical correlation is repeatable; it is not a single temporal coincidence.

## Safety boundary

Allowed:
- bonded-device metadata read,
- LE connection,
- GATT service discovery,
- notification subscription on the confirmed Cyan notify characteristic,
- standard CCCD enable-notification write,
- receive notifications,
- local parsing/CRC validation,
- user event markers,
- local/sanitized report generation.

Forbidden:
- `writeCharacteristic`,
- proprietary write UUID `de5bf72a-d711-4e47-af26-65e3012a5dc7`,
- generic BLE command entry,
- Cyan command-frame generator in the diagnostic,
- initialization/time write,
- P2P/AP media-mode command,
- Wi-Fi/P2P/AP APIs,
- Internet/HTTP/media transfer,
- pairing/unpairing/reset,
- restart/OTA/firmware commands.

## Privacy

Public reports must not contain:
- Bluetooth MAC address,
- device-specific name suffix,
- serial number,
- Wi-Fi credentials,
- unrelated nearby-device data,
- account identifiers,
- client/site media.

Safe report fields include:
- app/build version,
- relative timestamps,
- sanitized event labels,
- command byte,
- payload bytes relevant to interoperability,
- CRC/length validity,
- repeated correlation counts.

## Proposed report shape

```text
K G1 EVENT CORRELATION REPORT
Session: ...
Safety: PASSIVE / NO PROPRIETARY WRITE

MARK +00.000 BASELINE
RX   +01.240 cmd=73 payload=... crc=PASS

MARK +10.000 WEAR
RX   +10.118 cmd=73 payload=... crc=PASS

MARK +20.000 REMOVE
RX   +20.131 cmd=73 payload=... crc=PASS

CORRELATION SUMMARY
WEAR   candidate payload: ...   repeated 3/3
REMOVE candidate payload: ...   repeated 3/3
...
```

The summary must use terms such as `candidate` until supported by repeated evidence and/or Cyan code.

## Exit criteria

G2B can be considered complete when one of the following is true:

1. Cyan static evidence directly explains `0x73` and the relevant initialization semantics, with physical observations consistent with that explanation; or
2. repeated passive physical correlations establish the needed event semantics with enough confidence to safely define the next test; or
3. the semantics remain unresolved, in which case G3 stays blocked and the uncertainty is explicitly documented.

## Next action after this checkpoint

Do not start G3.

First implement/execute Track A. Build the passive Event Correlator only as needed or in parallel after this plan is committed and repository hygiene is green.

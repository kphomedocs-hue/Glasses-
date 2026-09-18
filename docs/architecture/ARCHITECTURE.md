# AIMB-G1 Project Architecture

This repository separates **frozen evidence**, **current physical-test code**, **legacy media-sync engineering**, and **future production modules**. Old baselines are preserved; they are never silently rewritten into the current implementation.

## Repository layers

```text
Glasses-/
├── README.md
├── LATEST_CHECKPOINT.md
├── PROJECT_MANIFEST.md
│
├── apps/
│   ├── discovery/              # current physical-test app lineage
│   └── site-capture/           # future production app integration layer
│
├── modules/                    # target production architecture
│   ├── protocol/               # framing, CRC, command allow-list, response parsing
│   ├── device-ble/             # scan, connect, GATT discovery, notifications
│   ├── device-network/         # Wi-Fi/P2P/AP handoff only
│   ├── media-transfer/         # remote list/download/resume/integrity
│   ├── media-storage/          # daily folders, numbering, metadata preservation
│   ├── sync-ledger/            # imported/pending/failed/dedup state
│   └── diagnostics/            # reports and safe logs
│
├── docs/
│   ├── architecture/
│   ├── protocol/
│   ├── testing/
│   ├── research/
│   └── decisions/
│
├── source/v0.1/               # frozen Discovery v0.1 source
├── releases/v0.1/             # frozen Discovery v0.1 APK/package/QA
│
├── archive/
│   ├── site-capture/           # legacy v0.1–v0.4 history
│   └── discovery/              # development history/provenance
│
└── tools/
```

## Design rules

1. **Frozen versions stay immutable.** Once a build is declared a baseline, its source and hashes remain unchanged.
2. **Protocol knowledge is separate from transport.** BLE framing/CRC/command definitions must not live inside Android UI or storage code.
3. **Transport is replaceable.** The app talks to a small device-source interface so FakeAIMBG1 and RealAIMBG1 can share the same sync/storage tests.
4. **Writes are allow-listed.** No generic BLE console or arbitrary command entry is part of production architecture.
5. **Storage is device-independent.** Numbering, daily folders, crash recovery, integrity checks and dedup must work without physical glasses.
6. **Network access is local-only.** Future media HTTP code must restrict destinations to the glasses/local private network, reject unexpected redirects and never depend on cloud APIs.
7. **Evidence and inference are labeled.** Values recovered from Cyan are documented separately from values confirmed on the physical AIMB-G1.
8. **Physical gates are incremental.** Read-only discovery precedes notification subscription; notification subscription precedes any controlled command; file listing precedes download; one-file download precedes automatic sync.
9. **No destructive device controls in the Site Capture app.** Factory reset, restart, OTA and generic maintenance commands remain out of scope.
10. **Every release has provenance.** Source hash, build hash, test status and next gate are recorded.

## Target application dependency direction

```text
UI / Android lifecycle
        │
        ▼
SyncCoordinator
 ┌───────────────┬────────────────┬──────────────────┐
 ▼               ▼                ▼
DeviceSession   MediaTransfer    MediaStorage
 │               │                │
 ▼               ▼                ▼
BLE/Network    RemoteMedia       Numbering + files
        │                           │
        └─────────────┬─────────────┘
                      ▼
                  SyncLedger

Protocol definitions are consumed by DeviceSession,
but never depend on Android UI, storage or networking.
```

## Immediate implementation status

The current **Discovery v0.1** remains the only app authorized for the first physical test. It is intentionally read-only.

The older **K Site Capture v0.4** is preserved as a legacy engineering reference because it already contains useful storage, ledger, streaming, fake-device and test concepts. Those components should be migrated module-by-module after physical protocol confirmation rather than copied wholesale into Discovery v0.1.

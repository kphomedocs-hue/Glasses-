# G6 Automatic Sync — Architecture Freeze

Status: DESIGN ONLY. No G6 transport or background-sync code is authorized by this document.

## Proven physical foundation

G5.7 physically proved the complete bounded single-JPG path:
1. baseline inventory/catalog;
2. capture exactly one photo;
3. passive +1 image visibility;
4. active 0x41 / 02 04 confirmation;
5. exact-peer P2P association;
6. passive glasses IPv4;
7. refreshed media.config;
8. exact-one new safe JPG by set difference;
9. exactly one media GET;
10. JPEG validation;
11. local temporary cleanup;
12. transfer exit.

Evidence:
`docs/testing/results/2026-09-20_G5_7_SINGLE_JPG_DOWNLOAD_PASS.md`

## G6 objective

Turn the proven read-only transfer path into a resumable import pipeline for new:
- JPG/JPEG;
- MP4;
- OPUS.

G6 still does not delete, rename, edit or mutate files on the glasses.

## Architecture

### 1. Protocol / device boundary

Reuse the physically proven BLE + P2P sequence from the active G5 line.

Responsibilities:
- exactly one bonded AIMB-G1-family target;
- Cyan notify/write UUIDs;
- media-count query `0x41 / 02 04`;
- P2P enter `0x41 / 02 01 04 01`;
- passive `0x73 / 0x08` IPv4;
- exact peer-name match;
- transfer exit `0x41 / 02 01 09`;
- no AP fallback;
- no `02 03`;
- no reset/restart/OTA.

### 2. Catalog snapshot

Input: strict-safe lines from `/files/media.config`.

Rules:
- no raw catalog logging;
- no remote filename/path logging;
- reject duplicate/unsafe lines;
- preserve catalog order only as metadata, never as proof of chronology;
- accepted media types for G6 v1: JPG/JPEG, MP4, OPUS.

### 3. Persistent import ledger

Legacy `FileImportLedger` is conceptually reusable but must be replaced/hardened before production.

Required fields per imported remote object:
- opaque remote identity key;
- media type;
- local final relative path;
- byte count;
- import status;
- first-seen timestamp;
- completed timestamp.

Required states:
- DISCOVERED;
- DOWNLOADING;
- VERIFIED;
- COMMITTED;
- FAILED_RETRYABLE.

Rules:
- write ledger only after local durable commit;
- never mark imported before validation;
- recover incomplete `.part` work safely;
- duplicate remote identity must not create a second final file.

### 4. Local archive / naming

Reuse the legacy daily-folder + monotonic counter idea, not its implementation verbatim.

Target structure:

```
<root>/
  YYYY-MM-DD/
    0001.jpg
    0002.opus
    0003.mp4
```

Rules:
- one chronological counter shared across supported media types per day;
- next number derived from committed files + ledger, not remote queue position;
- final file appears only after successful validation and atomic/near-atomic commit;
- temporary `.part` file stays app-private or inside target day folder and is never treated as imported.

### 5. Integrity validation

Common:
- HTTP 200;
- redirects disabled;
- no Range/resume in first G6 increment;
- hard byte cap;
- non-empty body;
- Content-Length match when present.

JPG/JPEG:
- SOI + EOI signature.

MP4:
- bounded header check for ISO BMFF `ftyp` box;
- no full decode required in first G6 increment.

OPUS:
- require Ogg container `OggS` signature and Opus identification where available;
- do not transcode.

### 6. Retry / recovery

First G6 increment must stay conservative:
- at most one retryable item in flight;
- no parallel downloads;
- no glasses mutation;
- process death must leave enough local state to resume without duplicate commit;
- failed partial file is never exposed as final;
- retry count bounded.

### 7. UI

Minimal production UI:
- Connected / Not connected;
- New items discovered;
- Current item type + progress;
- Imported count;
- Failed count;
- Last successful sync;
- Start Sync / Stop.

Do not expose:
- Wi-Fi credential values;
- Bluetooth/peer addresses;
- raw remote names/paths;
- raw catalog;
- private device suffixes.

## Legacy-code reuse decision

Potentially reusable concepts:
- `G1Transport` hardware boundary;
- `RemoteMedia` metadata record concept;
- `NumberingPolicy` daily folder + shared counter;
- `FileMediaArchive` part-file then final-move pattern;
- `FileImportLedger` dedup/recovery concept.

Must not be copied unchanged:
- legacy transport implementation, because active BLE/P2P behavior is now proven elsewhere;
- legacy identity key containing original remote filename in plaintext;
- any recovery behavior that deletes files without explicit scope checks;
- media-type allowances outside JPG/JPEG, MP4 and OPUS for first G6 increment.

## First implementation gate: G6A

G6A is intentionally smaller than full automatic sync.

Goal:
- persistently import exactly one newly discovered safe media object using the production ledger/archive path.

Scope:
- one item maximum per run;
- supported type initially JPG only;
- reuse G5.7 transport;
- persistent opaque ledger;
- deterministic daily numbering;
- `.part` -> validated -> final commit;
- restart-safe duplicate prevention;
- no background service yet;
- no MP4/OPUS transfer yet;
- no automatic repeated polling;
- no glasses mutation.

Only after G6A physical PASS should G6B add multi-item JPG sync, followed by separate MP4 and OPUS gates.

# Last Physical Report Incorporation Recheck

Date: 2026-10-01

## Report re-read

The last physical report was the v0.6.4 no-capture run generated at:
`2026-10-01T08:41:51+0530`.

It is preserved at:
`releases/v0.6.4/observations/2026-10-01_NO_CAPTURE_STABILITY_UNRESOLVED_BUILD.md`.

Observed physical facts:
- Snapshot A BLE inventory: 10 images, 0 videos, 1 recording.
- Snapshot A catalog: 10 JPG, 0 MP4, 1 OPUS, 11 safe entries.
- Snapshot B BLE inventory: 10 images, 0 videos, 1 recording.
- Snapshot B catalog: 10 JPG, 0 MP4, 1 OPUS, 11 safe entries.
- JPG identity set retained 10/10.
- full safe catalog identity set retained 11/11.
- no missing or unexpected identities.
- two inventory queries.
- two P2P enters.
- two exits.
- two catalog GETs.
- zero media-file GETs.
- zero glasses mutation/deletion.

## What the report actually established

It is evidence that, during that specific 30-second no-capture interval, the observed inventory/catalog was stable at the 10-JPG state.

It does NOT establish:
- why the earlier v0.6.3 baseline was 8 images but later state was 10;
- long-term stability;
- stability across glasses power cycles/restarts;
- exact v0.6.4 binary identity;
- valid exit attribution;
- verified P2P cleanup;
- a valid G6A PASS.

## How v0.6.4.2 incorporates the report

### Preserved because the physical report was useful
- same read-only two-snapshot no-capture question;
- same 30-second bounded quiet interval;
- one catalog GET per snapshot;
- zero media GETs;
- in-memory opaque identity-set comparison;
- no glasses mutation/deletion.

### Tightened because the physical report exposed/left unresolved validation gaps
- requires BLE/catalog parity for ALL media classes:
  - images == JPG,
  - videos == MP4,
  - recordings == OPUS;
- requires exact operation totals for PASS;
- verifies the quiet interval with monotonic elapsed time;
- requires P2P group absence after cleanup;
- rejects BOM-bearing catalogs rather than normalizing them;
- rejects leading/trailing-whitespace lines rather than trimming identity text;
- removes exported per-file deterministic hash tokens.

### Tightened because v0.6.4 provenance was unresolved
- new package ID;
- exact immutable build folder;
- exact source ZIP from GITHUB_SHA;
- report embeds build commit, run ID, and run attempt;
- physical evidence must be stored under the exact build folder.

### Tightened because old exit/entry semantics were not sufficient
- P2P discovery waits for BOTH Android enter-write callback and valid credential response;
- generic exit-time command-0x41 notifications do not prove exit;
- exit proof requires successful exit write callback followed by a valid matching 0x73/0x01 inventory event;
- failure paths use bounded single-exit recovery and local P2P cleanup/readback.

## What is intentionally NOT incorporated as an assumption

The v0.6.4 observation of 10 JPG is NOT hard-coded as an expected count. v0.6.4.2 must accept whatever current count is physically observed and test internal/cross-snapshot consistency.

The prior 10→10 result is NOT treated as proof that no-capture stability is solved. Because the old build was red-team-invalid and exact APK provenance was unresolved, the corrected exact build must repeat the bounded no-capture test once.

The 8→10 transition remains unexplained. A successful v0.6.4.2 run will establish a trustworthy short-window no-capture stability result; it will not, by itself, explain that earlier transition.

## Recheck decision

**YES — the last physical report was studied and materially incorporated into v0.6.4.2.**

The current physical candidate remains:
- run `36811812916`;
- attempt `1`;
- build commit `e97bfc4ac66d6de49007296fdfff311df129801d`;
- APK SHA-256 `550d4935f17ed6250feda603a9d2963e79aaa716dd20ed59be00124e97f4ae99`.

One important repository wording defect was found during this recheck: historical roadmap sections still called superseded v0.6.4 and blocked v0.6.4.1 “VERIFIED PHYSICAL CANDIDATE.” Their headings were corrected to historical/superseded status so a future resume cannot mistake them for the active candidate.

G6A remains open. G6B remains blocked.

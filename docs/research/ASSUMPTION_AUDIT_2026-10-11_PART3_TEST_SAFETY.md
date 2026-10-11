# Assumption Audit — Part 3: State-machine, Safety, Evidence and Release (2026-10-11)

Statuses CONFIRMED / REFUTED / OPEN / RULE refer only to documented test scope.

| ID | Assumption or interpretation | Status | Evidence and correction |
|---|---|---|---|
| S01 | Passing shell static audit means APK is safe for physical test | REFUTED | v0.6.4.1 and first v0.6.8.5 audits passed but manual red-team found critical cleanup/timer flaws |
| S02 | Successful BLE ENTER characteristic-write callback proves device entered P2P | REFUTED as inference | v0.6.8.1-.4 got successful callback with short 0x41 and no validated credential readiness |
| S03 | Successful EXIT write callback proves entire transfer/P2P cleanup | REFUTED as inference | Need post-exit BLE state plus Android group-state evidence; early G4A removeGroup returned BUSY |
| S04 | Any matching 0x41 response after EXIT confirms exit semantics | REFUTED | Generic 0x41 responses not reliable typed proof; later source deliberately ignores them |
| S05 | Wi-Fi Direct group formed means it belongs to the selected glasses | REFUTED as inference | v0.6.8.5 red-team required independent selected-peer group membership |
| S06 | removeGroup BUSY can be ignored safely without group verification | REFUTED as policy | Historical G4A tolerated BUSY; later gate demanded final absent-group query |
| S07 | Wi-Fi Direct pre-existing group may be removed automatically | RULE: FORBIDDEN | Require read-only preflight and preserve unknown/pre-existing group |
| S08 | One shared timeout watchdog suffices for discovery, BLE and cleanup | REFUTED | v0.6.8.5 review found cross-cancellation of 16s discovery deadline; switched to independently named clocks |
| S09 | BLE EXIT can be sent while ENTER callback remains pending | REFUTED as safe policy | v0.6.8.5 red-team deferred EXIT or reports uncertainty |
| S10 | Cleanup callback will always arrive | OPEN / UNSAFE | Bounded cleanup watchdogs introduced; Android callbacks can fail/stall |
| S11 | onStop/onDestroy always guarantees cleanup | REFUTED as universal | OS force-kill/process death bypasses normal application lifecycle completion |
| S12 | A group removed by OS is automatically known to be our own | REFUTED as inference | Must prove clean preflight, connect request, test formation and exact selected peer membership |
| S13 | Android Wi-Fi Direct system broadcast always delivered with receiver NOT_EXPORTED | REFUTED as safe assumption | v0.6.8.5 manual Android system UID review corrected receiver registration |
| S14 | Android discoverPeers SUCCESS implies P2P group formed | REFUTED | v0.6.8.5/.6 one peer observed; no connect initiated; no group created |
| S15 | No 0x73/08 during a bounded window means the event never occurs | REFUTED as inference | Observation windows limited and event timing dependent on connection state |
| S16 | One early 0x73 event was 0x08 or 0x0B | OPEN | v0.6.8.5 counted pending-callback event but suppressed ID; v0.6.8.6 observed none |
| S17 | v0.6.8.6 OTHER peer is safe to connect because it is the only one | RULE: FORBIDDEN | No authenticated identity and no connect API in passive app |
| S18 | App report version string always uniquely identifies binary | REFUTED | v0.6.7 report mislabeled itself 0.6.6; v0.6.4 had multiple same-version binaries |
| S19 | A successful Actions CI build proves physical behavior | REFUTED as inference | CI compiles/audits/signs but cannot attest glasses runtime, peer identity, or cleanup on hardware |
| S20 | SHA-256/run/commit trace proves tested APK code provenance | CONFIRMED scoped | Exact 0.6.8.5/.6 CI artifacts matched source/hashed APK; behavior still requires physical evidence |
| S21 | G4A proof of group formation guarantees fresh current 2026-10-11 G6A path | REFUTED as universal | Historical success was with a valid credential reply; later scans did not reach association |
| S22 | Correct HTTP/single JPG demo means production automatic sync is complete | REFUTED as inference | G6A open; G6B blocked; background/batch/video/audio not validated |
| S23 | No INTERNET permission is needed for passive peer observer | CONFIRMED for scoped APK | v0.6.8.5/.6 are no-connect/no-HTTP diagnostic only; do not generalize to local HTTP import builds |
| S24 | 12 Android Lint warnings equal 12 critical functional failures | REFUTED | Warnings were nonblocking CI findings; still require manual review of relevance |
| S25 | More unchanged physical retries will resolve unidentified peer | OPEN / NOT JUSTIFIED | v0.6.8.5 and .6 both saw one unverified peer; prefer identity static research |
| S26 | Every operator report conclusion line accurately describes implemented code | REFUTED | v0.6.8.5 claimed name-or-suffix but implemented suffix-only; v0.6.8.6 'peer not found' text in passive observer |
| S27 | No proprietary DELETE means no remote state mutation | REFUTED | Media JPG GET itself caused remote catalog consumption in tested path |
| S28 | Research-only candidate may be automatically promoted to production | RULE: FORBIDDEN | Exact build, independent red-team, explicit one-run physical approval, then gate review separately |
| S29 | Private credentials and raw device IDs should go in public GitHub evidence | RULE: FORBIDDEN | Preserve sanitized counts, IDs, hashes/provenance only; never publish APK binary or actual SSID/password |
| S30 | Current G6A officially passed and G6B available | REFUTED | G6A remains OPEN and G6B BLOCKED in latest checkpoint |

Sources: `LATEST_CHECKPOINT.md`, `docs/research/G6A_v0.6.8.5_SAFETY_REWORK_2026-10-11.md`, `docs/research/G6A_v0.6.8.5_INDEPENDENT_MANUAL_REVIEW_2026-10-11.md`, `releases/v0.6.8.5/builds/run-38116572973-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`, `releases/v0.6.8.6/builds/run-38118060511-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`, and exact .5/.6 physical reports.

Note: The later DEX trace on 2026-10-11 shows Cyan constructs a cached Wi-Fi Direct candidate name on BLE connection. It changes the source-level identity hypothesis, but it does NOT retrospectively classify the unknown peer in the v0.6.8.6 scan.

# AIMB-G1 / K Site Capture — assumption audit index
**Evidence cutoff:** 2026-10-11 after direct original Cyan DEX trace. **Repository basis:** original physical reports and frozen per-version sources, plus exact Cyan APK and DEX method/call-site evidence. **Gate state:** G6A OPEN, G6B BLOCKED.

## Full assumption register: 90 material claims
- [Part 1 — Protocol, BLE and Wi-Fi peer identity](ASSUMPTION_AUDIT_2026-10-11_PART1_PROTOCOL_IDENTITY.md) — P01–P30
- [Part 2 — Capture, catalogs, media and persistence](ASSUMPTION_AUDIT_2026-10-11_PART2_MEDIA_STORAGE.md) — M01–M30
- [Part 3 — State machine, testing, cleanup and release safety](ASSUMPTION_AUDIT_2026-10-11_PART3_TEST_SAFETY.md) — S01–S30

Status rule: CONFIRMED means proven **within a stated physical/source scope**; REFUTED means contradicted in its broad form; OPEN means no adequate current evidence; RULE is a deliberate design/test constraint rather than a hardware fact. Some rows combine a status with its qualifier. These are materially traceable assumptions, not a claim to have catalogued every unspoken hypothesis.

## Timeline of changed conclusions
1. Sep 18–19: AIMB-G1 BLE connection, 0x73 framing and commands established; exact-name scan was too narrow, bonded target fallback worked in tested conditions.
2. Sep 19: Catalog mistakenly treated as JSON; exact Cyan parser + physical G4B2/G4B3 corrected configFileType=1 to text line list.
3. Sep 19–20: Immediate file visibility was not reliable; BLE 0x73 capture visibility plus new-JPG set delta enabled safe single JPG GET (G5.7).
4. Sep 29: Local file and ledger stored; restart current-catalog ledger match failed, initially raising hash/rename/retention questions. One +2 capture observation failed closed.
5. Oct 1: Controlled no-capture and single-capture/reconnect stability isolated the missing-image transition to media transfer path; v0.6.7 proved remote GET consumption (11→12 after capture, then 12→11 after GET; exact transferred new JPG disappears).
6. Oct 2–7: v0.6.8 single-JPG persistent-import candidate could not progress because credential-bearing 0x41 no longer appeared. A prompt valid 5-byte short 0x41 was observed; 0x40 time sync did not restore credentials. Temporary GATT transport failure was a distinct precondition, later recovered.
7. Oct 8–11: Static Cyan showed discovery starts before ENTER; v0.6.8.5/.6 found one anonymous peer, never connected. Family prefix/full normalized suffix were absent in .6's in-memory classification; identity remained unknown.
8. Oct 11 exact-APK DEX: Cyan Bluetooth callback creates a candidate Wi-Fi name from device-name segment + underscore + colon-stripped BLE address, stores it using MMKV, and checks exact stored-name equality plus a different raw-address suffix alternative. Hence fresh credential-bearing 0x41 is **not the only code path capable of supplying a candidate peer name**.

## Highest-impact corrections
- **Originals untouched conflict:** A JPG GET caused the glasses to consume/remove the fetched JPG on the physically tested path, despite zero explicit delete commands. Product expectation to leave originals on glasses is not established.
- **Identity risk:** One anonymous peer is not necessarily the glasses. Never auto-connect by count, proximity or generic prefix.
- **Identity comparison inconsistency to resolve:** The constructed Cyan candidate ends in the colon-stripped BLE address. If K's same paired Bluetooth address and same naming formula were used, such a name should fall into v0.6.8.6's full-suffix category; instead the one peer was OTHER. Merely removing `AIMB-G1` from the name does **not** explain the mismatch. Possible causes include unrelated peer, different actual Bluetooth address source, nonmatching advertised name, or platform observation effects. No conclusion is proven.
- **Handshake distinction:** successful characteristic write, short 0x41, P2P advertisement, Android group formation and 0x73/08 readiness are five separate milestones. Do not collapse them.
- **Dedup distinction:** durable ledger persistence is proven for one record; remote file's post-GET absence makes catalog-match-based restart checks invalid as a universal oracle, and complete crash-safe exactly-once import remains unproven.
- **Release distinction:** passing static/CI tests is not runtime evidence. Preserve exact commit, run, attempt, APK SHA-256, full physical report, red-team review and cleanup proof.

## Next evidence needed (research-only)
1. Static independent comparison of exact Cyan name-construction semantics with K's v0.6.8.5/.6 actual suffix predicates and Bluetooth address sources; include direct code-unit branch validation.
2. Classify when Cyan assigns/overwrites the default nine-character password and whether a later credential response updates it; never publish or reuse secret literal without protocol justification.
3. Study the 0x41 short-frame meaning from exact Cyan parsing/firmware evidence, without labelling unknown bytes as busy/rejection.
4. Design a non-destructive archiving policy acknowledging the observed remote-consumptive GET before promising preservation of originals on glasses.
5. Do not initiate further physical scans, unknown-peer association, bulk import, or G6B without a separately reviewed hypothesis and explicit authorization.

**Source of truth:** `LATEST_CHECKPOINT.md` plus immutable reports; see linked register for per-assumption provenance. No private device names, addresses, credentials, original Cyan APK bytes, or client media included.


## Assumption-first static closure (2026-10-11)
- [Exact K vs Cyan comparison and static closures](G6A_ASSUMPTION_FIRST_STATIC_CLOSURE_2026-10-11.md)
- [Resolution register, blocked unknowns and red-team tests](G6A_ASSUMPTION_FIRST_RESOLUTION_REGISTER_2026-10-11.md)
These do **not** declare all 90 assumptions solved. No new APK or physical run authorized. G6A OPEN; G6B BLOCKED.

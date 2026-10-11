# Assumption Audit — Part 2: Catalog, Capture, Transfer, Storage (2026-10-11)

Statuses: CONFIRMED (limited exact tested scope); REFUTED (general claim contradicted); OPEN (unproven); RULE (requirement, not proof).

| ID | Historical assumption | Status | Evidence, exception, correction |
|---|---|---|---|
| M01 | Cyan album catalog is JSON for every device | REFUTED | G4B diagnostic JSON exception; for configFileType=1 /files/media.config is line-oriented |
| M02 | JSON branch is never used by Cyan | REFUTED | Exact Cyan configFileType=2 uses vf_list.txt and JSON model |
| M03 | G4B2 counted 4 physical file entries | REFUTED | G4B2 preliminary 4-line diagnostic; G4B3 actual 3 safe entries, 2 JPG + 1 OPUS |
| M04 | Initial BLE image count always equals catalog JPG count | REFUTED as universal | G3 initial image 1 while G4B3 catalog had 2 JPG; later verified parity in scoped runs |
| M05 | Catalog position reliably indicates most recent photo | OPEN / UNSAFE | No chronology guarantee in exact Cyan source; safe selection requires set difference |
| M06 | Every catalog line is safe to GET directly | OPEN / UNSAFE | Strict relative path, duplicate, traversal/URL controls imposed, not assumed universal |
| M07 | Always use /files/media.config for every hardware mode | REFUTED | Proven selected on configFileType=1; configFileType=2 uses alternative endpoint |
| M08 | An immediate post-capture GET must show newly captured file | REFUTED as rule | G5 v0.5.0/v0.5.2 no-delta; G5.5 capture became visible only after elapsed time |
| M09 | No catalog +1 right after photo means glasses never captured | REFUTED as inference | G5.5 physical delayed +1 on passive 0x73 and active 02 04 |
| M10 | One user photo is guaranteed to correspond to exact +1 | REFUTED as universal | G6A v0.6.3 saw +2, correctly aborted as ambiguous |
| M11 | Cyan's 1000ms catalog delay alone caused G5 no-delta | OPEN | Cyan uses 1000ms and count handshake; causal role not isolated |
| M12 | A 02 04 inventory read necessarily refreshes catalog | OPEN | Cyan reads it before P2P; exact causal relationship not proven |
| M13 | No-capture catalog spontaneously changes in every idle session | REFUTED as universal | v0.6.4.2 verified short-window 10/0/1 inventory and 10 JPG +1 OPUS catalog stability |
| M14 | Capture+reconnect necessarily loses untransferred JPG | REFUTED | v0.6.5 exactly one new JPG retained across verified reconnect with zero media GET |
| M15 | A successful JPG GET leaves that same JPG available on glasses | REFUTED | v0.6.7 pre-GET +1 and post-GET -1; exact downloaded identity disappeared; baseline retained |
| M16 | K must send an explicit delete to remove remote media | REFUTED as exclusive explanation | K performed no delete command, yet GET-induced remote consumption observed |
| M17 | Inventory decrease after GET must be a transient notification | REFUTED as sole cause | v0.6.7 fresh reconnect/catalog sustained change |
| M18 | Restart reversion (9 JPG to 8) proves ledger hash bug | REFUTED as sole cause | Same hash path in both parts; later GET consumption explains a tested mechanism |
| M19 | A downloaded photo can be reliably found again by scanning current remote catalog | REFUTED as general rule | Remote GET consumes identity; a receipt cannot require post-GET remote presence |
| M20 | Plain remote path string hash is guaranteed stable forever | OPEN | Differences across sessions/firmware not globally proven; post-GET item disappears |
| M21 | Successful media GET alone establishes complete preserved JPG | PARTIAL | JPEG SOI/EOI, length and size caps validated in G5.7/G6; full metadata/original fidelity not end-to-end verified |
| M22 | The app can persist an actual JPG on the phone | CONFIRMED scoped | G6A 0.6.0/0.6.2 Part1 final file; one deterministic path 2026-09-29/0001.jpg |
| M23 | The local ledger survives an app restart | CONFIRMED scoped | 0.6.1/0.6.2 next session saw ledger entries=1 |
| M24 | Ledger persistence equals proven restart-safe deduplication | REFUTED as inference | Ledger survived while current remote catalog had zero matching identity; full gate remained OPEN |
| M25 | Exactly-once import under crashes/retry/disconnections is production-proven | OPEN | Isolated one-item success; crash consistency and complete dedup across all cases not demonstrated |
| M26 | GET can never affect originals because K does not send delete | REFUTED on tested firmware path | Device autonomously consumes fetched JPG; clashes with user goal to keep originals untouched |
| M27 | Video and OPUS imports are validated as for JPG | OPEN | Observed catalog categories (.opus), but media import tests verified one JPG only |
| M28 | Cross-media daily sequential numbering and timestamp fidelity are complete | OPEN | User requirement; only scoped JPG deterministic commit observed |
| M29 | Fully automatic foreground/background capture-to-phone sync is working | OPEN | Existing gates were user-triggered diagnostics; no final unattended multi-item sync proven |
| M30 | No cloud usage and local-only storage is final production-tested | RULE / OPEN | Design requirement and constrained test APKs; no complete production app validation |

Key historical source paths: `docs/research/CYAN_EXACT_G4B_TRACE_2026-09-19.md`; `docs/testing/results/2026-09-19_G4B2_RESPONSE_SHAPE_PASS.md`; `docs/testing/results/2026-09-19_G4B3_CATALOG_LINE_PASS.md`; `docs/testing/results/2026-09-19_G5_CAPTURE_VISIBILITY_V0_5_5_PASS.md`; `docs/testing/results/2026-09-20_G5_7_SINGLE_JPG_DOWNLOAD_PASS.md`; `docs/testing/results/2026-09-29_G6A_V0_6_2_PART2_CATALOG_REVERSION.md`; physical reports under `releases/v0.6.5/`, `releases/v0.6.6/`, `releases/v0.6.7/`.

Critical product conflict: the user originally required glasses originals untouched; current physical proof shows a successful media JPG GET consumes/removes remote file on tested firmware. This is a limitation of actual transport, not an app-command deletion. Address in product specification before claiming originals remain preserved.

G6A OPEN; G6B BLOCKED.

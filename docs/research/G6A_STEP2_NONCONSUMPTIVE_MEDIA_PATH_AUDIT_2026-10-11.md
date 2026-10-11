# G6A Step 2 — non-consumptive media alternatives (STATIC EVIDENCE REVIEW)
Date: 2026-10-11. No glasses operation, no new APK and no HTTP requests.

## Evidence and limits
Reviewed frozen research and reports in `docs/research/CYAN_EXACT_G4B_TRACE_2026-09-19.md`, `docs/research/CYAN_EXACT_G5_TRACE_2026-09-19.md`, `docs/protocol/AIMB_G1_PROTOCOL.md`, and exact v0.6.5/.6/.7 physical reports in immutable `releases/` folders. These reports derive from the historically SHA-verified Cyan 1.0.2.18 APK. This pass compares retained exact-bytecode **findings**; it does **not** purport to re-disassemble every Cyan method or prove that an unexamined firmware endpoint cannot exist.

## Transport/media pathway matrix
| Path or idea | Cyan source / physical evidence | Is it a verified non-consumptive alternative? | Judgment |
|---|---|---|---|
| P2P, configFileType=1 | P2P ENTER `02 01 04 01`; catalog `http://<glassIP>/files/media.config` as UTF-8 line list; each line becomes `http://<glassIP>/files/<line>`; `AlbumDepository.downloadGlassFile()` uses `AndroidNetworking.download`; G5.7/G6 physically downloaded JPG | **NO** | Tested successful single-JPG GET is associated with disappearance of exact JPG afterward |
| AP mode `02 01 04 02` | Cyan uses distinct transport when configFileType=2 or onlySupportApImport, or on HarmonyOS NEXT; physical AIMB-G1 reported type1 and AP-only=false, normal Android -> P2P | **NO EVIDENCE** | Different radio association mode does not by itself change device-side GET semantics; forcing AP is not justified |
| configFileType=2 catalog / JSON `vf_list.txt` | Exact Cyan branch uses `:80/storage/sd0/C/DCIM/1/vf_list.txt` and Moshi PtPFileModel, `FileBean.getF()` for media URL construction | **NO EVIDENCE** | Different catalog shape/endpoints for other mode/firmware, not proven selectable/safe on this type1 device |
| `/files/log/` log-list mode | Cyan conditional log-download handling | **NO** | Log path, not evidence for JPG preservation |
| `0x41 / 02 03` | P2P-IP read query documented but G4A2 used passive 0x73/08 | **NO** | Address query, not a media-read mode |
| HTTP HEAD / Range / custom download headers | No HEAD/Range/custom header observed in the traced Cyan `downloadGlassFile` caller | **NO EVIDENCE** | Do not assume HEAD, Range or a different URL disables server-side consume behavior; no request authorized |
| Cyan app retry/resume | Cyan may retry on errors; early K tests intentionally prohibited retries | **NO** | Repeating an allegedly consumptive GET risks originals; no evidence retry alters remote semantics |
| Remote copy/move/share or vendor API | No exact validated nonconsumptive media-copy command/endpoint recovered from retained source traces | **UNKNOWN** | Absence of evidence in reviewed methods is not proof that firmware lacks one |
| Preserve original in K app by local commit | Local persistent JPG and receipt demonstrated (G6A Phase 1) | **Only phone-side preservation** | Does not retain original on glasses after the tested transfer |

## Strongest physically supported timeline
- v0.6.5: baseline -> exactly one capture -> fresh reconnect with **no media GET**; new JPG still in catalog and inventory, baseline retained.
- v0.6.6: one successful JPG GET -> post-exit `0x73/0x01` inventory differs; test stopped before fresh re-read.
- v0.6.7: controls retained new JPG before GET. Exactly one validated successful JPG GET, then EXIT and verified cleanup; after fresh reconnect image count reduced by one and exactly that new JPG identity absent while every baseline JPG remained.
- No explicit K remote delete command was sent.

## Causality precision
The experiment isolates a **GET-associated remote consumption transition over the subsequent exit/reconnect interval**. Because post-GET observations occur after EXIT, current evidence does not conclusively timestamp the actual removal at the HTTP GET boundary rather than at a subsequent device-side transfer-completion/exit action. The clean no-GET reconnect controls strongly support transfer-path association. Do not overstate the internal firmware trigger or claim to know server implementation.

## Assumption closure
1. The universal claim `HTTP GET is read-only for this device` is FALSE on the tested transfer path: remote JPG no longer appears afterward.
2. Swapping P2P to AP, switching catalog type, changing HTTP verb, or sending recovery commands is **not a verified solution** and would be a new, higher-risk experiment.
3. Existing exact Cyan media-download caller traces one ordinary local HTTP downloader; it does not document a switch for `keep remote file`.
4. No verified non-consumptive alternative currently exists **in the reviewed evidence**. This is a limit of current knowledge, not a universal impossibility statement.
5. Production must not promise original-on-glasses preservation using this current path without new strong evidence.

## Safer architecture implications (design-only)
- Treat remote media as **potentially consumable once fetched**, not as an immutable source to which K can return after restart.
- Preserve temporary complete downloaded bytes and durable local receipt before marking success; on partial GET treat remote presence/absence as unknown and do not automatically retry. This is a design hypothesis needing fault injection and hardware validation, not an implemented guarantee.
- For a product that strictly requires originals to remain on glasses, **block importing via this method** until a verified non-consumptive method exists or the requirement is explicitly changed.
- No request to inspect or re-download real client media is part of this research.

## Red-team questions still open
- If server consumption occurs at the first completed GET or at transfer EXIT, could an interrupted GET leave neither a remote copy nor a complete local copy? Unverified; potentially high consequence.
- Does this behavior apply to video/OPUS or different firmware/config types? Unknown; do not generalize.
- Could a supported vendor mechanism retain remote media? Not documented in reviewed traces; requires primary-source evidence, not a speculative command.

**Current result:** physical GET-associated consumption confirmed; non-consumptive alternative NOT ESTABLISHED. G6A OPEN; G6B BLOCKED.

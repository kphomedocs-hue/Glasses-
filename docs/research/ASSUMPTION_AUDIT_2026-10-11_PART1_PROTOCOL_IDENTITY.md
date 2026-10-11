# Assumption Audit — Part 1: Protocol and Peer Identity (2026-10-11)

Source basis: project GitHub testing reports, precise app sources, Cyan exact-DEX call-site audit. Status terms: CONFIRMED = scoped direct evidence, REFUTED = contradicted in stated general form, OPEN = not established, RULE = intended design constraint.

| ID | Assumption / early interpretation | Status | Actual evidence or corrective boundary |
|---|---|---|---|
| P01 | AIMB-G1 supported by Cyan app | CONFIRMED | Exact app supported-model evidence plus G1 physical test |
| P02 | Target Bluetooth name must equal literal AIMB-G1 | REFUTED | Actual bonded target has device suffix; early scan exact-name filtering missed it |
| P03 | A bonded record guarantees connectable glasses | REFUTED | v0.6.8.4 GATT timeout with bonded target present; later transport recovered |
| P04 | Cyan BLE service, notify, write UUIDs apply to glasses | CONFIRMED | G1 physical GATT |
| P05 | Notification channel uses Cyan 0xBC, length, payload CRC-16/MODBUS | CONFIRMED | G2 physical unsolicited 0x73 CRC and G2C |
| P06 | All 0x73 notifications have unknown meaning | REFUTED | Exact Cyan code decodes 0x01 inventory; 0x05 charging; 0x08 IP during G4A2. Other IDs/states unresolved |
| P07 | All firmware variants have same 0x73 payload length | REFUTED | G2 inventory report one byte shorter than newer Cyan parser's optional flag |
| P08 | BLE time sync 0x40 universally required for P2P entry | REFUTED | G3B historical direct entry worked; 0x6.8.4 0x40 did not fix current short reply |
| P09 | 0x40 response status byte semantics already known | OPEN | Command/frame and write confirmed; exact semantic byte not fully verified |
| P10 | Any valid 0x41 after ENTER proves accepted transfer mode | REFUTED | Repeated valid, short, NONcredential 0x41; callback vs device readiness separated |
| P11 | Short 0x41 payload 89 01 8A 00 00 means busy/rejected/reset | OPEN | Outside examined work-type-4 success parser; precise 0x89/0x8A meaning unknown |
| P12 | ENTER always returns 20-byte SSID and 9-byte password | REFUTED | Historically G3B/G4A yes; 0.6.8.1-.4 short frames no credentials for 20s |
| P13 | Current failure is merely early/late credential arrival | NOT SUPPORTED | v0.6.8.1/3/4 monitored ordinary + late 10s windows; no valid credential frame |
| P14 | Current image count causes short 0x41 | REFUTED as sole cause | Seen with 11/0/1 and 0/0/0 inventories |
| P15 | Cyan requires fresh credential-bearing 0x41 before deriving candidate peer name | REFUTED | Exact DEX MyBluetoothReceiver.connectStatue constructs and stores name from Bluetooth name/address |
| P16 | Cyan Wi-Fi name is only a transient in-memory variable | REFUTED | UserConfig MMKV-backed app_config delegated property |
| P17 | Cyan name always starts AIMB-G1 | REFUTED as general rule | Last underscore-split Bluetooth-name component + '_' + colon-stripped address may omit prefix |
| P18 | Cyan P2P fallback suffix strips colons at comparison | REFUTED | Exact PictureFragment.onPeersChanged endsWith raw saved deviceAddressNoClear; earlier research normalized too broadly |
| P19 | Cyan peer matching accepts saved exact name OR suffix test | CONFIRMED source path | DEX exact stored-name case-insensitive equality, fallback endsWith saved Bluetooth address; runtime match not yet confirmed |
| P20 | One Android P2P peer observed must be glasses | OPEN / UNSAFE | v0.6.8.5/.6 discovered 1, never authenticated or connected |
| P21 | v0.6.8.6 OTHER category excludes glasses | REFUTED as inference | It only lacks AIMB-G1 prefix and normalized suffix; candidate may have other naming/addr; no raw name verification |
| P22 | Android discoverPeers success proves glasses advertise | REFUTED as inference | Success starts a phone scan, not glasses identity or readiness |
| P23 | Discovery-before-ENTER fixes credential/association problem | OPEN | Both v0.6.8.5/.6 used order, but never reached association |
| P24 | 16s scan timeout proves peer absent from hardware | REFUTED as inference | It bounds observer; broadcasts, cached peers and timing may differ |
| P25 | 0x73/0x0B or pending-ENTER 0x73 establishes P2P readiness | OPEN | 0.6.8.5 one early event without ID; .6 none; 0x0B semantics unconfirmed |
| P26 | 0x73/0x08 can provide glasses IP during formed P2P group | CONFIRMED scoped | G4A2 observed and decoded a client IPv4, not universal timing guarantee |
| P27 | Group owner 192.168.49.1 is glasses address | REFUTED | In G4A2 it was phone; glasses address observed separately as .176 on that run |
| P28 | Phone is always P2P group owner | OPEN | Observed as phone owner in historical G4A, not guaranteed for all groups |
| P29 | Cyan normal setup uses reset 02 01 0F before ENTER | REFUTED | Cyan 0F is conditional recovery, not normal preparation |
| P30 | K currently has verified peer identity with no fresh credentials | OPEN | Candidate name algorithm now source-verified; current advertisement match never physically tested |

Sources: `docs/protocol/AIMB_G1_PROTOCOL.md`; `docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md`; `docs/testing/results/2026-09-19_G4A2_P2P_IP_NOTIFY_PASS.md`; `docs/research/CYAN_P2P_ROOT_CAUSE_RECHECK_2026-10-08.md`; `docs/research/G6A_CYAN_EXACT_DEX_WIFI_IDENTITY_CALLSITE_TRACE_2026-10-11.md`; exact 0.6.8.1/3/4/5/6 reports in their immutable release folders.

Do not interpret an arbitrary peer as AIMB-G1. G6A OPEN / G6B BLOCKED.

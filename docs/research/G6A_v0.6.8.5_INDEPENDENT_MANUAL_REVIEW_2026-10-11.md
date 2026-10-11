# G6A v0.6.8.5 — independent manual review (2026-10-11)

**Scope:** diagnostic source only, never a production or physically authorized APK.

## New findings and corrections

1. **P2P peer identity (high):** the previous name-or-suffix matcher treated the generic literal `AIMB-G1` as an acceptable peer even without matching the bonded BLE target. Corrected: require the exact uppercase no-separator bonded BLE address suffix to appear at the end of the peer name. Zero arbitrary peer fallback.
2. **EXIT while ENTER write in flight (high):** a failure or 16-second deadline could trigger a write of EXIT while the BLE ENTER write callback was pending. Corrected: defer the one EXIT until ENTER settles, with a bounded wait and at most one EXIT write attempt. If it cannot be started, fail closed and report separately.
3. **Premature group attribution (high):** a system connection-info `groupFormed=true` was treated as evidence that our target formed the group without verifying the selected peer's membership. Corrected: one bounded platform group membership verification before a positive conclusion. Unverified groups are not removed.
4. **Privileged Wi-Fi broadcasts (medium):** `RECEIVER_NOT_EXPORTED` on recent Android can miss broadcasts from privileged system apps. Updated receiver flag to `RECEIVER_EXPORTED` for the two Wi-Fi Direct system intents; broadcast payloads are not trusted and every decision uses system queries. Android official guidance: https://developer.android.com/develop/background-work/background-tasks/broadcasts

## Continuing limitations

- A lifecycle callback cannot guarantee cleanup after process death.
- An already connected P2P group may be temporarily missing its peer membership list. This diagnostic intentionally fails closed rather than inferring target identity.
- The `EXIT_WAIT` timeout tries exactly once if ENTER never settles; Android may refuse while GATT is busy. Such an outcome must be marked incomplete cleanup, never success.
- New platform-source changes require Android compile, lint, signature verification, and independent post-build review before any physical authorization.
- Static audits cannot prove actual system Wi-Fi Direct behavior.

## Validation and gate status

- Safety/RED_TEAM/STATE_MACHINE audits must pass on the exact amended source.
- No media, HTTP, capture, credential parsing, remote deletion, or retry was added.
- No physical test authorized; **G6A OPEN, G6B BLOCKED**.

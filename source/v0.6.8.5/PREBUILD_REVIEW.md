# Prebuild review — v0.6.8.5 Cyan P2P state discriminator

Purpose: distinguish whether P2P is available despite the repeated short
non-credential `0x41` response.

Allowed behavior: read-only P2P group preflight; start Wi-Fi Direct discovery;
one P2P ENTER; connect only to one Cyan-matching peer with WPS-PBC; observe
CRC-valid `0x73` event IDs; one P2P EXIT; cancel pending connection, remove a
group created by this test only, and verify no group remains.

Required Cyan alignment: discovery starts before ENTER; 16-second discovery
bound; peer name must end in the paired BLE address with separators removed;
no forced group-owner intent.

Forbidden: time sync, inventory/media commands, credential parsing/logging,
Internet, HTTP/sockets, catalog/media access, capture, import, reset, retry,
AP mode, arbitrary peer fallback, and network-address reporting.

This is diagnostic-only. It cannot close G6A or unblock G6B.

## 2026-10-11 corrected source / independent review required

The original prebuild red-team review identified 7 lifecycle and cleanup defects.
The updated source routes every terminal condition through one EXIT attempt where BLE
is usable and a bounded cleanup sequence, or explicitly reports the attempt impossible.

Validation at source level:
- safety shell audit;
- red-team shell audit;
- host-JVM pure-policy exhaustive boolean combinations;
- structural checks of named timers, EXIT path and group verification.

Clean group preflight alone does **not** authorize removing an unrelated group;
removal additionally requires exact selected peer identity in the group.
A remaining/unverified group blocks all positive classification.
Android process death cannot guarantee cleanup, so OS kill/reboot remains an
operational risk that cannot be solved by an Activity-only callback.

Still required: independent manual review, Android compile + lint in CI,
signature/archive provenance, and an explicit authorization decision before
any physical diagnostic. No APK or physical PASS is claimed here.

## 2026-10-11 provenance correction
The original report included only the app version, while the immutable archive's
physical report contract requires the exact commit, workflow run, and attempt.
The corrected APK now injects these three values from GitHub Actions at build time
and includes them in both a full-run report and a permission/precondition abort.
A non-CI local build is stamped LOCAL_UNVERIFIED and is **not** a physical candidate.
The immutable archive still requires full independent review before promotion.

## Second independent safety correction — cleanup group attribution
The group-removal policy now explicitly requires observed group formation by
this test, not merely a clean preflight and matching peer identity. If the
app cannot prove attribution, it must not remove the group; it fails closed.

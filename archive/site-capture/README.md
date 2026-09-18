# K Site Capture Legacy Archive

This folder preserves the pre-physical media-sync engineering lineage.

## Version lineage

| Version | Original ZIP SHA-256 | Preservation |
|---|---|---|
| v0.1 | `eecbd428a53977c7858b17010db1c646493f5ddf6cc2773ec78b8ac44fee0db6` | provenance recorded |
| v0.2 | `204fa7942637c79e9da1889050503c230e69d9f3f29e88ba960a60a7497af1eb` | provenance recorded |
| v0.3 | `a306bb4a182007f3e9e2974a4611b54673334f94139bcea171aa8ffb587bacdd` | provenance recorded |
| v0.4 | `2843b69ac743986d76a8c1d6821e88fc4978e10517a972184b4e32920b621a01` | expanded source preserved under `v0.4/` |

v0.4 is the engineering reference because it contains the strongest storage/ledger/streaming/test implementation. Earlier versions are superseded and retained as lineage hashes rather than duplicated active code.

Generated Python bytecode caches from the old ZIP are intentionally not preserved; their source scripts are preserved.

## What v0.4 contributes to the future app

- replaceable hardware transport boundary,
- fake device for tests,
- chronological remote ordering,
- daily folders,
- one counter across media types,
- streaming download,
- declared-size verification,
- temporary-file cleanup,
- crash-window recovery,
- persistent dedup ledger,
- OPUS support,
- 32 MiB streaming stress test,
- protocol framing/CRC research tests.

The current Discovery v0.1 physical gate supersedes v0.4's old first-test workflow.

# ADR-001 — Frozen Baselines and Append-Only History

Status: Accepted

## Decision

Once a version is declared a verified baseline, its source, hashes and release artifacts are treated as immutable.

New work goes into a new version/module. Historical code may be copied into a migration branch/module, but the original baseline is not edited to make it look cleaner.

## Why

This project combines:
- reverse-engineered interoperability evidence,
- physical-device testing,
- Android permission/platform behavior,
- file-integrity guarantees.

Being able to answer "what exactly was tested?" matters more than keeping one continuously rewritten source tree.

## Consequences

- Discovery v0.1 remains frozen.
- Site Capture v0.4 remains a historical engineering reference.
- current architecture docs point to both instead of rewriting them.
- every future release must record source hash, build hash and physical-test status.

# ADR-002 — No Generic Device Command Console

Status: Accepted

## Decision

Any future AIMB-G1 write path must use explicit, named, reviewed commands. The application will not expose arbitrary BLE bytes or a generic command console.

## Why

The production goal is media import, not device maintenance or experimentation. A generic writer makes accidental reset/restart/maintenance commands easier to send and makes test provenance harder to reason about.

## Rules

- protocol module may encode only documented commands needed for the approved workflow,
- command names must describe intent,
- each command requires a test vector,
- destructive/maintenance commands stay outside the production application,
- the first physical test remains read-only.

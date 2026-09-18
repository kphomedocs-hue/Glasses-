# Public Repository Data Rules

This repository is public. Physical-device testing must use sanitized records.

## Never commit

- Bluetooth MAC addresses
- device serial numbers
- Wi-Fi SSIDs/passwords that are unique to the user's device
- account identifiers, tokens or credentials
- client/site photographs, videos or audio
- personal names, phone numbers or addresses
- raw bug reports containing unrelated phone/app data
- full third-party Cyan APKs/split APKs
- packet captures unless manually reviewed and sanitized

## Safe to commit

- app/build version
- Android version and generic phone model
- GATT service UUIDs
- characteristic UUIDs
- characteristic property flags
- Android/GATT status codes
- sanitized protocol payload/response bytes relevant to interoperability
- non-identifying firmware/model strings when needed
- pass/fail results
- source/build hashes

## Physical report workflow

1. Generate the diagnostic report locally.
2. Review it before committing.
3. Remove identifiers/secrets.
4. Commit only the sanitized copy under `docs/testing/results/`.
5. If raw evidence must be retained, store it privately outside this public repository and record only its hash/provenance here.

## Media-testing rule

Use disposable test media only until automatic transfer is proven safe.
Do not use client/site media for protocol-development tests.

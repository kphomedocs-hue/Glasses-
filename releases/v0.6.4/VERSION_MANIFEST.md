# v0.6.4 Version Manifest

- Version: v0.6.4
- Build role: first no-capture catalog-stability diagnostic
- Status: **SUPERSEDED / RED-TEAM INVALID FOR PROMOTION**
- Replacement engineering line: v0.6.4.2 after red-team fixes.

## Preserved build instances

### Build run 36806675980
- exact bundle: `builds/run-36806675980/`
- source/build commit: `a6dc537c37906ed353ccaa23e152933989bdcd59`
- APK SHA-256: `5b59c2b577edffe2c28e03fa9a47703f83f934997808a63aee4e1084eb310438`

### Build run 36809242952
- exact bundle: `builds/run-36809242952/`
- source/build commit: `b2f4a286a6c91c865a0a82ca402d0dd4f65c7d74`
- APK SHA-256: `7ad38e645204ad910c73213fbd6257d12473af5d4229bbf590e5990ebe633e90`

The v0.6.4 `MainActivity.java` blob is identical in both build commits:
`0869be48abe202f39b090eb2095759fc2741bddb`.

Therefore the diagnostic runtime source is the same across both v0.6.4 binaries, but exact binary provenance still matters and is not recoverable from the exported report alone.

## Physical observation

Observed report generated:
`2026-10-01T08:41:51+0530`

Stored at:
`observations/2026-10-01_NO_CAPTURE_STABILITY_UNRESOLVED_BUILD.md`

Physical observation:
- Snapshot A: BLE images=10, catalog JPG=10, total safe entries=11;
- Snapshot B: BLE images=10, catalog JPG=10, total safe entries=11;
- JPG identity set unchanged 10/10;
- full safe catalog identity set unchanged 11/11;
- two inventory queries;
- two P2P enters;
- two exits;
- two catalog GETs;
- zero media GETs;
- no glasses mutation/deletion.

Interpretation:
- useful evidence that the catalog/inventory remained stable during this specific 30-second no-capture interval;
- does not explain the earlier 8→10 transition;
- does not promote v0.6.4 to a valid gate build because the red-team validity issues remain;
- exact APK hash that generated this report is unresolved between the two preserved v0.6.4 build instances.

Do not use v0.6.4 for further physical promotion.

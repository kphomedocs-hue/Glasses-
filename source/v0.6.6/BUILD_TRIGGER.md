# Build Trigger

This file intentionally triggers the v0.6.6 workflow after the workflow definition is present on the default branch.

No runtime code or diagnostic boundary is changed by this file.

The build must still pass:
- safety audit;
- red-team static audit;
- compile/lint;
- APK signature verification;
- immutable exact-build archive;
- manual state-machine/provenance review before physical promotion.

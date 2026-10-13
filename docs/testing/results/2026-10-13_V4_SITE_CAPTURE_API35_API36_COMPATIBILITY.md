# K Site Capture — Android 15 and Android 16 local archive runtime compatibility

Date: 2026-10-13. **PASS on two different GitHub-hosted virtual Android platform versions.** Does not authorize installing an APK on a physical phone or connecting AIMB-G1 hardware.

## Reason and bounded change
Prior `apps/site-capture/android-preview/app/build.gradle.kts` required `minSdk = 36`, blocking an Android 15 phone from installing even the read-only preview. Changed **only** the isolated preview's minimum to `minSdk = 35`, retained `compileSdk=36` and `targetSdk=36`. New separate virtual-device CI workflow uses `api-level:35` and explicitly sets `K_SITE_EMULATOR_API=35`. The same existing runner strictly checks `ro.kernel.qemu=1`, EXACT requested local API 35 or 36 and precisely one adb emulator, never a physical device.

## Exact successful actions
- **Android 15/API35 emulator**: [workflow 38341334991](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38341334991) **SUCCESS**, commit `1ff058a8e365841333d14a5f230d19395b705f70`.
- **Android 16/API36 emulator**: [workflow 38341308157](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38341308157) **SUCCESS**, commit `aa3d5fca6812e9a46aeccf68d24e6af2cfed05ce` containing the same Android runtime source and minSdk35. Subsequent changes only fixed compile-workflow guard or added API35 test workflow.
- **API36 source compile with minSdk35 guard**: [workflow 38341320200](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38341320200) **SUCCESS** for commit `01723e6652a028907523aeab82c0c38435b8514f`.
- **Final repo hygiene**: [workflow 38341334986](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38341334986) **SUCCESS** for latest compatibility workflow commit.

### Independently evidenced runtime on EACH platform
Both log streams independently contain:
```text
V4-SITE-CAPTURE-UI PASS: API35 or API36 emulator visibly exposes local archive and transfer-disabled warning
V4-SITE-CAPTURE-ARCHIVE-UI LOCAL_ONLY_SAFE PASS
V4-SITE-CAPTURE-ARCHIVE-UI LOCAL_ONLY_SAFE PASS
V4-SITE-CAPTURE-ARCHIVE-UI LOCAL_REVIEW_NO_REMOTE_RETRY PASS
V4-SITE-CAPTURE-ARCHIVE-UI LOCAL_LEDGER_UNVERIFIED PASS
V4-SITE-CAPTURE-ARCHIVE-STATE 4/4 PASS: restart, mixed receipt counts, uncertain and corrupt journal fail-closed
```
The first line is a semantic quote: individual raw runs identify their exact API35 or API36. The two `LOCAL_ONLY_SAFE` results are initial three-media receipt screen and fresh-process restart without reseeding. MP4 and OPUS remain finite synthetic shape fixtures, not vendor decoder/playback proof.

## Failure provenance and changes
- A transient compile-only failure on minSdk-change commit `717bc2e5bae7fe9002493aa7e155e8976f7206f5` (run `38341291330`) resulted from the **previous CI static guard still demanding `minSdk = 36`**. It did not demonstrate a Java or Android API incompatibility.
- Guard updated to `minSdk = 35` in `01723e6652a028907523aeab82c0c38435b8514f`, followed by successful compile and both Android runtime tests.
- The new API35 job's internal human-readable step title was copied from the API36 workflow, but its actual runner is explicitly `api-level:35`, and script independently verifies the Android SDK integer 35 and logs `API35 emulator`. Do not misread its step label as an API36 run.

## Safety and limits
- An ephemeral **zero-permission** main preview APK plus separate zero-permission AndroidTest APK are installed **only on a GitHub emulator**; the runner uninstalls both and uploads neither.
- Production preview JavaCompile excludes `SharedTransferCoordinator`, `SharedObservationController`, `SharedTransferContracts` and thus synthetic grant path; source-only CI separately checks compiled class absence.
- The app remains read-only, without BLE, Wi-Fi Direct, network, remote media GET or device scan.
- Actual handset compatibility, battery restrictions, task scheduling, SAF file export, vendor container/playback and firmware original retention remain unverified. This is not a signed final sync release.

**Milestone: API35 and API36 read-only archive UI runtime PASS. C1 shipping HOLD; C2 BLOCKED; C3 NO-GO; C4 BLOCKED; G6A OPEN; G6B BLOCKED.**

#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6apersist68/MainActivity.java"
ARCH="$SRC/java/com/parkarsite/g6a/FileArchive.java"
LEDGER="$SRC/java/com/parkarsite/g6a/ImportLedger.java"
GRADLE="$ROOT/app/build.gradle.kts"
fail=0
must(){ grep -Fq "$1" "$2" || { echo "FAIL: missing $3"; fail=1; }; }
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a68_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a68_hits; fail=1; else echo "PASS: $2 absent"; fi; }

must 'applicationId = "com.parkarsite.g6apersist68"' "$GRADLE" 'fresh package'
must 'versionName = "0.6.8"' "$GRADLE" 'version'
must 'App version: 0.6.8' "$JAVA" 'runtime report version'
must 'ONE PERSISTENT JPG IMPORT' "$JAVA" 'persistent import stage'
must 'Persistent final file committed: YES' "$JAVA" 'final file evidence'
must 'Persistent receipt/ledger commit: PASS' "$JAVA" 'receipt evidence'
must 'postGetConsumptionExact' "$JAVA" 'remote consumption proof'
must 'G6A PERSISTENT IMPORT RESULT: PASS' "$JAVA" 'phase 1 success output'
must 'LOCAL-ONLY RESTART VERIFICATION' "$JAVA" 'restart verification'
must 'if(importCommittedThisProcess)' "$JAVA" 'same-process restart guard'
must 'Media-file GET requests: 0' "$JAVA" 'restart zero-download output'
must 'Total HTTP GET requests: 0' "$JAVA" 'restart zero-http output'
must 'BLE/P2P operations: 0' "$JAVA" 'restart zero-transport output'
must 'records()' "$LEDGER" 'ledger record enumeration'
must 'raw.getFD().sync()' "$ARCH" 'part-file fsync'
must 'writeSynced(source,opaqueId)' "$ARCH" 'synced opaque source sidecar'
must 'StandardCopyOption.ATOMIC_MOVE' "$ARCH" 'atomic move attempt'
must 'raw.getFD().sync()' "$LEDGER" 'ledger fsync'
must 'Opaque remote identity persisted: YES — value not reported' "$JAVA" 'opaque receipt privacy'
must 'Remote filename/path persisted: NO' "$JAVA" 'raw remote path prohibition'

bad 'equalsIgnoreCase\(expectedP2pName\)' 'case-insensitive peer match'
bad 'setInstanceFollowRedirects\(true\)|setFollowRedirects\(true\)' 'redirect enablement'
bad 'setRequestProperty\(' 'custom header / Range'
bad 'setRequestMethod\("(POST|PUT|PATCH|DELETE|HEAD)"\)' 'non-GET HTTP'
bad '/files/log/|vf_list\.txt|storage/sd0/C/DCIM/1' 'alternate remote endpoint'

url_count="$(grep -Fo 'new URL(' "$JAVA" | wc -l | tr -d ' ')"
[[ "$url_count" == "2" ]] || { echo "FAIL: expected catalog+media URL sites=2, got $url_count"; fail=1; }
get_count="$(grep -Fo 'setRequestMethod("GET")' "$JAVA" | wc -l | tr -d ' ')"
[[ "$get_count" == "2" ]] || { echo "FAIL: expected GET callsites=2, got $get_count"; fail=1; }

echo "v0.6.8 safety audit complete"
exit "$fail"

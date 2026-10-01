#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6apersist68/MainActivity.java"
ARCH="$ROOT/app/src/main/java/com/parkarsite/g6a/FileArchive.java"
LEDGER="$ROOT/app/src/main/java/com/parkarsite/g6a/ImportLedger.java"
fail=0
require(){ grep -Fq "$1" "$2" || { echo "FAIL: $3"; fail=1; }; }
forbid(){ if grep -Fq "$1" "$2"; then echo "FAIL: $3"; fail=1; fi; }

require 'if(inv.imageCount!=baselineImageCount+1)' "$JAVA" 'exact +1 capture gate missing'
require 'jpgMissing==0&&newJpgs.size()==1&&allMissing==0&&allUnexpected==1' "$JAVA" 'one-JPG set delta missing'
require 'preGetRetentionExact=exactRetention;' "$JAVA" 'pre-GET retention missing'
require 'if(importLedger.contains(opaqueId))' "$JAVA" 'persistent pre-download dedup guard missing'
require 'importCoordinator.importNewJpg(candidate' "$JAVA" 'coordinated persistent import missing'
require 'after!=before+1' "$JAVA" 'exact ledger delta check missing'
require 'validateLocalJpeg(finalFile,record.byteCount())' "$JAVA" 'post-commit local validation missing'
require 'persistentImportCommitted=true;receiptCommitted=true;importCommittedThisProcess=true;' "$JAVA" 'commit state transition missing'
require 'expectedConsumedCounts=currentImageCount==postCaptureImageCount-1' "$JAVA" 'expected remote -1 count missing'
require 'jpgHashes.equals(baselineJpgHashes)&&allHashes.equals(baselineAllHashes)' "$JAVA" 'baseline exact remote set requirement missing'
require '!newJpgPresent' "$JAVA" 'downloaded remote identity absence requirement missing'
require 'postGetConsumptionExact=' "$JAVA" 'consumption aggregate missing'
require 'countWriteCount==4&&enterWriteCount==4&&exitWriteCount==4&&catalogGetCount==4&&mediaGetCount==1&&httpRequestCount==5' "$JAVA" 'phase1 totals missing'

require 'private static boolean importCommittedThisProcess=false;' "$JAVA" 'process restart guard state missing'
require 'if(importCommittedThisProcess){setStatus("Restart verification is blocked in the same process.' "$JAVA" 'same-process verify block missing'
require 'List<ImportRecord> records=importLedger.records();' "$JAVA" 'disk-loaded receipt verification missing'
require 'records.size()!=1' "$JAVA" 'exact receipt count missing'
require 'validateLocalJpeg(local,r.byteCount())' "$JAVA" 'restart JPEG validation missing'
require 'countCommittedMediaFiles(importRoot)' "$JAVA" 'restart duplicate count missing'
require 'files==1&&ImportRecord.COMMITTED.equals(r.status())' "$JAVA" 'restart PASS invariant missing'
require 'ZERO REDOWNLOAD; ZERO DUPLICATE' "$JAVA" 'restart result missing'

require 'writeSynced(source,opaqueId);' "$ARCH" 'recovery sidecar missing'
require 'moveCommitted(part,finalFile);' "$JARCH" 'final move missing'
require 'ledger.commit(record);' "$ARCH" 'ledger commit after final move missing'
require 'recoverCommittedSidecars(root);' "$LEDGER" 'crash-window receipt recovery missing'
require 'records()' "$LEDGER" 'restart enumeration missing'

forbid 'App version: 0.6.6' "$JAVA" 'stale report version'
forbid 'App version: 0.6.7' "$JAVA" 'stale report version'
forbid 'equalsIgnoreCase(expectedP2pName)' "$JAVA" 'case-insensitive peer match'

echo "v0.6.8 red-team static audit complete"
exit "$fail"

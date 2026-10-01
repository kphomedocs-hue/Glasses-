#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6aobserver67/MainActivity.java"
fail=0
require(){ grep -Fq "$1" "$2" || { echo "FAIL: $3"; fail=1; }; }
forbid(){ if grep -Fq "$1" "$2"; then echo "FAIL: $3"; fail=1; fi; }

# Proven v0.6.5/v0.6.6 chain retained.
require 'if(inv.imageCount!=baselineImageCount+1)' "$JAVA" 'exact +1 capture gate missing'
require 'jpgMissing==0&&newJpgs.size()==1&&allMissing==0&&allUnexpected==1' "$JAVA" 'exact one-JPG delta missing'
require 'preGetRetentionExact=exactRetention;' "$JAVA" 'pre-GET exact retention missing'
require 'if(!preGetRetentionExact){abortRun("Media GET blocked because pre-GET retention was not proven.");return;}' "$JAVA" 'GET precondition missing'
require 'OpaqueIdentity.sha256(entry).equals(newJpgHash)' "$JAVA" 'exact current-catalog JPG resolution missing'
require 'if(mediaGetCount!=0)' "$JAVA" 'single GET guard missing'
require '!m.jpegStart||!m.jpegEnd' "$JAVA" 'JPEG validation missing'
require 'tempCleanupPass=!localFile.exists()||localFile.delete();' "$JAVA" 'temp deletion check missing'

# Critical observation gate must be narrow.
require 'boolean observationalPostGetExit=stage==Stage.PRE_GET_RETENTION&&mediaValidated&&mediaGetCount==1;' "$JAVA" 'post-GET observation gate too broad/missing'
require 'if(observationalPostGetExit)' "$JAVA" 'observation branch missing'
require 'postGetExitInventoryObserved=true;' "$JAVA" 'observation state missing'
require 'postGetExitImageCount=inv.imageCount;' "$JAVA" 'image count not retained'
require 'postGetExitVideoCount=inv.videoCount;' "$JAVA" 'video count not retained'
require 'postGetExitRecordCount=inv.recordCount;' "$JAVA" 'record count not retained'
require 'postGetExitConfigType=inv.configFileType;' "$JAVA" 'config not retained'
require 'Post-GET exit inventory mismatch is observational, not a failure.' "$JAVA" 'nonfatal observation marker missing'

# All other exit mismatches remain fatal.
require 'if(!matches){' "$JAVA" 'general mismatch branch missing'
require 'outside the post-GET observation stage; exit separation not proven.' "$JAVA" 'non-post-GET mismatch not fail-closed'

# Fresh reconnect drives classification.
require 'stage==Stage.POST_GET_RETENTION' "$JAVA" 'fresh post-GET stage missing'
require 'postGetRetentionExact=exactRetention;' "$JAVA" 'fresh post-GET comparison missing'
require 'Downloaded new JPG identity still present after fresh reconnect:' "$JAVA" 'downloaded JPG presence missing'
require 'TRANSIENT EXIT-TIME INVENTORY TRANSITION' "$JAVA" 'transient classification missing'
require 'PERSISTENT CHANGE — DOWNLOADED NEW JPG ABSENT AFTER FRESH POST-GET RECONNECT' "$JAVA" 'persistent deletion classification missing'
require 'G6A POST-GET TRANSITION OBSERVER RESULT: COMPLETE' "$JAVA" 'observer completion output missing'

# Transport/provenance boundaries.
require 'd.deviceName.equals(expectedP2pName)' "$JAVA" 'exact peer missing'
require 'Post-exit 0x41 frame observed: IGNORED for exit confirmation' "$JAVA" 'generic exit 0x41 not ignored'
require 'P2P group present after cleanup request:' "$JAVA" 'group absence readback missing'
require 'BuildConfig.BUILD_COMMIT' "$JAVA" 'build commit missing'
require 'BuildConfig.BUILD_RUN' "$JAVA" 'build run missing'
require 'BuildConfig.BUILD_ATTEMPT' "$JAVA" 'build attempt missing'
forbid 'equalsIgnoreCase(expectedP2pName)' "$JAVA" 'case-insensitive peer match remains'

echo "v0.6.7 red-team static audit complete"
exit "$fail"

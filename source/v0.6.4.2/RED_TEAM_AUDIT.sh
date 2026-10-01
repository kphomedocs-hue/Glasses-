#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6astability642/MainActivity.java"
fail=0
require(){ grep -Fq "$1" "$2" || { echo "FAIL: $3"; fail=1; }; }
forbid(){ if grep -Fq "$1" "$2"; then echo "FAIL: $3"; fail=1; fi; }

require 'Post-exit 0x41 frame observed: IGNORED for exit confirmation' "$JAVA" 'generic 0x41 is not ignored during exit'
require 'postExitInventoryConfirmed=true;' "$JAVA" 'post-exit inventory confirmation assignment missing'
confirm_count="$(grep -Fo 'postExitInventoryConfirmed=true;' "$JAVA" | wc -l | tr -d ' ')"
[[ "$confirm_count" == "1" ]] || { echo "FAIL: postExitInventoryConfirmed must be assigned true exactly once; found $confirm_count"; fail=1; }
require 'private void abortRun(String reason)' "$JAVA" 'central abort missing'
require 'if(enterWriteStarted&&!exitWriteAttempted&&gatt!=null&&writeChar!=null)' "$JAVA" 'abort-time single exit guard missing'
require 'if(runActive&&!reportFinished&&!isChangingConfigurations())abortRun(' "$JAVA" 'onStop does not use central abort'
require 'if(runActive&&!reportFinished)abortRun(' "$JAVA" 'onDestroy does not use central abort'
require 'd.deviceName.equals(expectedP2pName)' "$JAVA" 'exact peer equality missing'
forbid 'equalsIgnoreCase(expectedP2pName)' "$JAVA" 'case-insensitive peer equality remains'
forbid 'substring(0,12)' "$JAVA" 'truncated deterministic identity token remains'
require 'if(!line.equals(line.trim()))' "$JAVA" 'line-normalization rejection missing'
require 'if(!"NONE".equals(out.bom))return out;' "$JAVA" 'BOM normalization rejection missing'
require 'BuildConfig.BUILD_COMMIT' "$JAVA" 'runtime build commit marker missing'
require 'BuildConfig.BUILD_RUN' "$JAVA" 'runtime build run marker missing'
require 'BuildConfig.BUILD_ATTEMPT' "$JAVA" 'runtime build attempt marker missing'

echo "v0.6.4.2 red-team static audit complete"
exit "$fail"

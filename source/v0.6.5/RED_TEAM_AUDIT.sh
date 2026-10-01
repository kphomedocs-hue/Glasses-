#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6acapture65/MainActivity.java"
fail=0
require(){ grep -Fq "$1" "$2" || { echo "FAIL: $3"; fail=1; }; }
forbid(){ if grep -Fq "$1" "$2"; then echo "FAIL: $3"; fail=1; fi; }

# Capture must remain user-physical and P2P must be downstream of exact +1 + active count confirmation.
require 'phase=Phase.CAPTURE_WATCH;' "$JAVA" 'capture-watch phase missing'
require 'if(inv.imageCount==baselineImageCount)' "$JAVA" 'unchanged passive event handling missing'
require 'if(inv.imageCount!=baselineImageCount+1)' "$JAVA" 'exact +1 passive gate missing'
require 'capturePassiveConfirmed=true;' "$JAVA" 'passive confirmation state missing'
require 'sendMediaCountQuery(gatt);' "$JAVA" 'active confirmation not triggered after passive +1'
require 'if(stage==Stage.POST_CAPTURE)' "$JAVA" 'post-capture active validation branch missing'
require 'Active post-capture inventory confirms exact +1 image:' "$JAVA" 'active exact +1 evidence missing'

# Catalog retention invariants.
require 'jpgMissing==0&&newJpgs.size()==1&&allMissing==0&&allUnexpected==1' "$JAVA" 'exact single-JPG set-delta gate missing'
require 'postCaptureAllHashes.clear();postCaptureAllHashes.addAll(allHashes);' "$JAVA" 'post-capture identity set not retained'
require 'newJpgHash=newJpgs.iterator().next();' "$JAVA" 'new JPG identity not retained in memory'
require 'boolean newJpgPresent=newJpgHash!=null&&jpgHashes.contains(newJpgHash);' "$JAVA" 'new JPG retention check missing'
require 'allHashes.equals(postCaptureAllHashes)' "$JAVA" 'full post-capture set equality missing'
require 'jpgHashes.equals(postCaptureJpgHashes)' "$JAVA" 'JPG post-capture set equality missing'

# Proven v0.6.4.2 handshakes retained.
require 'P2P enter write/credential handshake: COMPLETE' "$JAVA" 'enter dual-condition handshake missing'
require 'Post-exit 0x41 frame observed: IGNORED for exit confirmation' "$JAVA" 'generic exit response can still prove exit'
require 'postExitInventoryConfirmed=true;' "$JAVA" 'post-exit inventory confirmation missing'
require 'P2P group present after cleanup request:' "$JAVA" 'P2P cleanup readback missing'
require 'd.deviceName.equals(expectedP2pName)' "$JAVA" 'case-sensitive peer match missing'
forbid 'equalsIgnoreCase(expectedP2pName)' "$JAVA" 'case-insensitive peer match remains'

# Privacy/network boundary.
forbid 'substring(0,12)' "$JAVA" 'deterministic truncated identity token remains'
require 'if(!line.equals(line.trim()))' "$JAVA" 'catalog normalization rejection missing'
require 'if(!"NONE".equals(out.bom))return out;' "$JAVA" 'BOM rejection missing'
require 'BuildConfig.BUILD_COMMIT' "$JAVA" 'build provenance missing'
require 'BuildConfig.BUILD_RUN' "$JAVA" 'build run provenance missing'
require 'BuildConfig.BUILD_ATTEMPT' "$JAVA" 'build attempt provenance missing'

echo "v0.6.5 red-team static audit complete"
exit "$fail"

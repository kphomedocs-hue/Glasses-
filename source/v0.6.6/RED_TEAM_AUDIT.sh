#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6aget66/MainActivity.java"
fail=0
require(){ grep -Fq "$1" "$2" || { echo "FAIL: $3"; fail=1; }; }
forbid(){ if grep -Fq "$1" "$2"; then echo "FAIL: $3"; fail=1; fi; }

# Proven capture path retained.
require 'if(inv.imageCount!=baselineImageCount+1)' "$JAVA" 'exact +1 passive gate missing'
require 'Active post-capture inventory confirms exact +1 image:' "$JAVA" 'active +1 confirmation missing'
require 'jpgMissing==0&&newJpgs.size()==1&&allMissing==0&&allUnexpected==1' "$JAVA" 'exact one-JPG set delta missing'

# GET cannot happen before pre-GET retention.
require 'preGetRetentionExact=exactRetention;' "$JAVA" 'pre-GET exact retention assignment missing'
require 'if(!preGetRetentionExact){abortRun("Media GET blocked because pre-GET retention was not proven.");return;}' "$JAVA" 'media GET not gated by pre-GET retention'
require 'String candidate=null;' "$JAVA" 'current-catalog candidate resolution missing'
require 'OpaqueIdentity.sha256(entry).equals(newJpgHash)' "$JAVA" 'candidate not selected by exact opaque identity'
require 'if(mediaGetCount!=0)' "$JAVA" 'single GET guard missing'
require 'mediaGetCount++;httpRequestCount++;phase=Phase.MEDIA_GET;' "$JAVA" 'media GET accounting missing'

# Disposable validation.
require 'declared>MEDIA_MAX_BYTES' "$JAVA" 'Content-Length safety cap missing'
require 'total+n>maxBytes' "$JAVA" 'stream cap missing'
require '!m.jpegStart||!m.jpegEnd' "$JAVA" 'JPEG signature fail-closed missing'
require 'tempCleanupPass=!localFile.exists()||localFile.delete();' "$JAVA" 'temp delete check missing'
require 'Persistent import created: NO' "$JAVA" 'persistent import prohibition missing'
require 'Persistent ledger updated: NO' "$JAVA" 'ledger prohibition missing'

# Post-GET comparison.
require 'stage==Stage.POST_GET_RETENTION' "$JAVA" 'post-GET stage missing'
require 'postGetRetentionExact=exactRetention;' "$JAVA" 'post-GET exact comparison assignment missing'
require 'failureReason="Pre-GET stage ended without proven retention and one validated media GET.";' "$JAVA" 'post-cleanup PRE-GET failure finalizer missing'
forbid 'abortRun("Pre-GET stage ended without proven retention and one validated media GET.")' "$JAVA" 'PRE-GET cleanup fallback can dead-end in CLEANUP state'
require 'Downloaded new JPG identity still present remotely:' "$JAVA" 'new JPG post-GET check missing'
require 'allHashes.equals(postCaptureAllHashes)' "$JAVA" 'full catalog exact retention missing'
require 'jpgHashes.equals(postCaptureJpgHashes)' "$JAVA" 'JPG exact retention missing'

# Existing transport/provenance hardening retained.
require 'P2P enter write/credential handshake: COMPLETE' "$JAVA" 'enter dual-condition handshake missing'
require 'Post-exit 0x41 frame observed: IGNORED for exit confirmation' "$JAVA" 'generic exit 0x41 can prove exit'
require 'postExitInventoryConfirmed=true;' "$JAVA" 'post-exit inventory proof missing'
require 'P2P group present after cleanup request:' "$JAVA" 'P2P group readback missing'
require 'BuildConfig.BUILD_COMMIT' "$JAVA" 'build commit missing'
require 'BuildConfig.BUILD_RUN' "$JAVA" 'build run missing'
require 'BuildConfig.BUILD_ATTEMPT' "$JAVA" 'build attempt missing'
forbid 'equalsIgnoreCase(expectedP2pName)' "$JAVA" 'case-insensitive exact-peer violation'

echo "v0.6.6 red-team static audit complete"
exit "$fail"

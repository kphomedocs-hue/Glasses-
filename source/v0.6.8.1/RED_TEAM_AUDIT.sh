#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6acreddiag681/MainActivity.java"
fail=0
require(){ grep -Fq "$1" "$2" || { echo "FAIL: $3"; fail=1; }; }
forbid(){ if grep -Fq "$1" "$2"; then echo "FAIL: $3"; fail=1; fi; }

require 'private enum Phase { IDLE, COUNT_SENT, ENTER_SENT, LATE_OBSERVE, EXIT_SENT, COMPLETE }' "$JAVA" 'bounded phase model missing'
require 'if(countWriteAttempted)return;' "$JAVA" 'media-count retry guard missing'
require 'if(enterWriteAttempted)return;' "$JAVA" 'enter retry guard missing'
require 'if(finished||exitWriteAttempted)return;' "$JAVA" 'exit retry guard missing'
require 'scheduleNormalCredentialTimeout();' "$JAVA" 'normal credential timeout missing'
require 'phase=Phase.LATE_OBSERVE;' "$JAVA" 'late observation transition missing'
require 'scheduleLateCredentialTimeout();' "$JAVA" 'late observation timeout missing'
require 'CredentialShape classifyCredentialFrame(byte[] f)' "$JAVA" 'structural classifier missing'
require 'ShapeCode.PREFIX_MISMATCH' "$JAVA" 'prefix mismatch classification missing'
require 'ShapeCode.BOUNDS_OVERFLOW' "$JAVA" 'length bounds classification missing'
require 'normalValidCredential=true;' "$JAVA" 'normal valid classification missing'
require 'lateValidCredential=true;' "$JAVA" 'late valid classification missing'
require 'credentialSsidLength=sh.ssidLength;' "$JAVA" 'SSID length-only capture missing'
require 'credentialPasswordLength=sh.passwordLength;' "$JAVA" 'password length-only capture missing'
require 'cmd41++; CredentialShape sh=classifyCredentialFrame(data);' "$JAVA" 'enter-window 0x41 observation missing'
require 'invalidFrames++' "$JAVA" 'invalid-frame counter missing'
require 'sendExit();' "$JAVA" 'bounded exit missing'
require 'if(cmd==0x73&&data.length>=7&&(data[6]&255)==0x01)' "$JAVA" 'exit 0x73/01 confirmation path missing'
require 'matchesBase(i)' "$JAVA" 'exit inventory match missing'
require 'G6A CREDENTIAL HANDSHAKE DIAGNOSTIC CLASSIFICATION:' "$JAVA" 'classification output missing'
require 'Media-count writes: "+countWrites' "$JAVA" 'operation count output missing'
require 'P2P enter writes: "+enterWrites' "$JAVA" 'enter count output missing'
require 'Transfer-exit writes: "+exitWrites' "$JAVA" 'exit count output missing'
require 'NORMAL_CREDENTIAL_WINDOW_MS=10000L' "$JAVA" 'normal 10-second bound missing'
require 'LATE_OBSERVE_WINDOW_MS=10000L' "$JAVA" 'late 10-second bound missing'

forbid 'startP2pDiscovery' "$JAVA" 'Wi-Fi discovery must not exist'
forbid 'media.config' "$JAVA" 'catalog access must not exist'
forbid 'HttpURLConnection' "$JAVA" 'HTTP code must not exist'
forbid 'new URL(' "$JAVA" 'URL code must not exist'
forbid 'credentialPassword=' "$JAVA" 'password value storage must not exist'
forbid 'expectedP2pName=' "$JAVA" 'SSID value storage must not exist'
forbid 'App version: 0.6.8"' "$JAVA" 'stale app version'

echo "v0.6.8.1 red-team static audit complete"
exit "$fail"

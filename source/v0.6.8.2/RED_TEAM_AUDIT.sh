#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6acreddiag682/MainActivity.java"
fail=0
require(){ grep -Fq "$1" "$2" || { echo "FAIL: $3"; fail=1; }; }
forbid(){ if grep -Fq "$1" "$2"; then echo "FAIL: $3"; fail=1; fi; }

require 'NORMAL_CREDENTIAL_WINDOW_MS=10000L' "$JAVA" 'normal bound missing'
require 'LATE_OBSERVE_WINDOW_MS=10000L' "$JAVA" 'late bound missing'
require 'if(countWriteAttempted)return;' "$JAVA" 'count retry guard missing'
require 'if(enterWriteAttempted)return;' "$JAVA" 'enter retry guard missing'
require 'if(finished||exitWriteAttempted)return;' "$JAVA" 'exit retry guard missing'
require 'if(declared>=8||f.length!=declared+6)return "";' "$JAVA" 'short payload privacy guard missing'
require 'Arrays.equals(p,ENTER_P2P)' "$JAVA" 'exact enter echo comparison missing'
require 'p.length>=4&&(p[0]&255)==2&&(p[1]&255)==1&&(p[2]&255)==4&&(p[3]&255)==1' "$JAVA" 'enter-prefix status classification missing'
require 'SHORT_ENTER_ECHO_SHAPE_ONLY' "$JAVA" 'final echo-only classification missing'
require 'SHORT_ENTER_PREFIX_STATUS_ONLY' "$JAVA" 'final status-only classification missing'
require 'OTHER_SAFE_SHORT_0x41_ONLY' "$JAVA" 'other short classification missing'
require 'CredentialShape classifyCredentialFrame(byte[] f)' "$JAVA" 'credential classifier missing'
require 'sendExit();' "$JAVA" 'bounded exit missing'
forbid 'startP2pDiscovery' "$JAVA" 'Wi-Fi discovery must not exist'
forbid 'media.config' "$JAVA" 'catalog access must not exist'
forbid 'HttpURLConnection' "$JAVA" 'HTTP code must not exist'
forbid 'new URL(' "$JAVA" 'URL code must not exist'
forbid 'credentialPassword=' "$JAVA" 'password storage must not exist'
forbid 'expectedP2pName=' "$JAVA" 'SSID storage must not exist'

echo "v0.6.8.2 red-team static audit complete"
exit "$fail"

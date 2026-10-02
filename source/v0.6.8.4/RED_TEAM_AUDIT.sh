#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6ainitdiag684/MainActivity.java"
fail=0
require(){ grep -Fq "$1" "$2" || { echo "FAIL: $3"; fail=1; }; }
forbid(){ if grep -Fq "$1" "$2"; then echo "FAIL: $3"; fail=1; fi; }

require 'private enum Phase { IDLE, TIME_SENT, COUNT_SENT, ENTER_SENT, LATE_OBSERVE, EXIT_SENT, COMPLETE }' "$JAVA" 'bounded state machine missing'
require 'if(timeWriteAttempted)return;' "$JAVA" '0x40 retry guard missing'
require 'if(countWriteAttempted)return;' "$JAVA" 'count retry guard missing'
require 'if(enterWriteAttempted)return;' "$JAVA" 'enter retry guard missing'
require 'if(finished||exitWriteAttempted)return;' "$JAVA" 'exit retry guard missing'
require 'if(phase==Phase.TIME_SENT){timeWriteCb=true;' "$JAVA" 'time callback gate missing'
require 'cancel();sendCount(y);' "$JAVA" 'count must follow time write callback'
require 'Response is observed but NOT required as a gate' "$JAVA" 'time response must not gate'
require 'if(phase==Phase.TIME_SENT)' "$JAVA" 'time observation branch missing'
require 'timePhaseValid40++' "$JAVA" '0x40 response counter missing'
require 'payload not logged' "$JAVA" 'time response privacy missing'
require 'Calendar.getInstance()' "$JAVA" 'dynamic time source missing'
require 'calendar.add(Calendar.SECOND,1)' "$JAVA" 'Cyan +1 second parity missing'
require 'decimalToBcd' "$JAVA" 'BCD encoding missing'
require 'cyanLanguageCode' "$JAVA" 'language encoding missing'
require 'TimeZone.getDefault().getOffset' "$JAVA" 'timezone encoding missing'
require 'p[8]=0x01' "$JAVA" 'time trailing mode byte missing'
require 'f[1]=0x40' "$JAVA" 'command-0x40 framing missing'
require 'NORMAL_CREDENTIAL_WINDOW_MS=10000L' "$JAVA" 'normal credential window missing'
require 'LATE_OBSERVE_WINDOW_MS=10000L' "$JAVA" 'late observation window missing'
require 'PREVIOUS_SHORT_STATUS=new byte[]{(byte)0x89,0x01,(byte)0x8A,0x00,0x00}' "$JAVA" 'prior short-status comparator missing'
require 'SAME_V0_6_8_3_SHORT_STATUS_AFTER_0x40_INIT_ONLY' "$JAVA" 'same-status classification missing'
require 'NORMAL_VALID_AFTER_0x40_INIT' "$JAVA" 'normal valid-after-init classification missing'
require 'LATE_VALID_AFTER_0x40_INIT' "$JAVA" 'late valid-after-init classification missing'
require 'MIXED_SHORT_OR_REJECTED_0x41_ACTIVITY' "$JAVA" 'mixed response classification missing'
require 'Post-exit 0x41 frame observed: IGNORED for exit confirmation; payload not classified/logged' "$JAVA" 'post-exit isolation missing'
require 'Credential-capable notification payload bytes logged: NO' "$JAVA" 'credential-capable privacy summary missing'

forbid 'startP2pDiscovery' "$JAVA" 'Wi-Fi discovery must not exist'
forbid 'media.config' "$JAVA" 'catalog access must not exist'
forbid 'HttpURLConnection' "$JAVA" 'HTTP code must not exist'
forbid 'new URL(' "$JAVA" 'URL code must not exist'
forbid 'credentialPassword=' "$JAVA" 'password storage must not exist'
forbid 'expectedP2pName=' "$JAVA" 'SSID storage must not exist'
forbid 'Raw notification payload logged: NO' "$JAVA" 'inaccurate raw-payload wording must not return'

echo "v0.6.8.4 red-team static audit complete"
exit "$fail"

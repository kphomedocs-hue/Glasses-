#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6ap2pdiag685/MainActivity.java"
fail=0
require(){ grep -Fq -- "$1" "$JAVA" || { echo "FAIL: $2"; fail=1; }; }
forbid(){
  if grep -Eq -- "$1" "$JAVA"; then
    echo "FAIL: $2"; fail=1
  else
    rc=$?
    if [ "$rc" -ne 1 ]; then echo "FAIL: $2 scan error ($rc)"; fail=1; fi
  fi
}
require 'startGroupPreflight' 'read-only clean-group preflight missing'
require 'startP2pDiscovery();' 'discovery-before-ENTER path missing'
require 'pairedAddressSuffix=addressSuffix(target)' 'paired BLE suffix identity missing'
require 'matchesCyanPeer(d)' 'Cyan peer predicate missing'
require 'cfg.wps.setup=WpsInfo.PBC' 'WPS PBC missing'
require 'cancelConnect' 'pending P2P connection cleanup missing'
require 'queryGroupVerification' 'post-cleanup group verification missing'
require 'ENTER_P2P' 'single ENTER path missing'
require 'EXIT_TRANSFER' 'single EXIT path missing'
forbid 'groupOwnerIntent' 'forced group-owner role returned'
forbid 'parseTransferCredentials|credentialPassword|expectedP2pName' 'credential parser/storage returned'
forbid 'HttpURLConnection|java\.net\.Socket|media\.config|FileOutputStream' 'network or media code returned'
forbid '0x0[Ff]' 'P2P reset payload returned'
echo "v0.6.8.5 red-team static audit complete"
exit "$fail"

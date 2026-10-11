#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6apeerdiag686/MainActivity.java"
fail=0
require(){ grep -Fq -- "$1" "$JAVA" || { echo "FAIL: $2"; fail=1; }; }
forbid(){ if grep -Eq -- "$1" "$JAVA"; then echo "FAIL: $2"; fail=1; else rc=$?; if [ "$rc" -ne 1 ]; then echo "FAIL: scan error for $2"; fail=1; fi; fi; }
require 'startGroupPreflight' 'preflight omitted'
require 'startP2pDiscovery();' 'discovery omitted'
require 'append("discoverPeers: SUCCESS");sendEnter(gatt);' 'discovery-before-ENTER omitted'
require 'DiagnosticSafetyRules.classifyPeerName' 'passive classification omitted'
require 'if(peerSnapshots<MAX_PEER_SNAPSHOTS)' 'bounded peer reporting omitted'
require 'if(pendingEnterEvents.size()<MAX_EVENTS_PER_BUCKET)' 'bounded pending BLE event omitted'
require 'pendingWrites.contains(BleWrite.ENTER)' 'deferred EXIT omitted'
require 'if(enterWriteAttempted)sendExitOnce();else startCleanup();' 'central EXIT route omitted'
require 'VERIFY_GROUP_FIRST' 'first cleanup check omitted'
require 'VERIFY_GROUP_SECOND' 'second cleanup check omitted'
require 'cleanTerminalClassification(' 'cleanup-gated result omitted'
forbid 'p2pManager.connect\(|p2pManager.cancelConnect\(|p2pManager.removeGroup\(|WifiP2pConfig|WpsInfo' 'P2P mutation API detected'
forbid 'HttpURLConnection|java.net.Socket|FileOutputStream|parseTransferCredentials' 'network/media/credentials detected'
forbid 'requestConnectionInfo\(|getDeviceList\(\).*connect' 'connection path detected'
forbid 'matchesCyanPeer\(' 'misleading previous name matcher returned'
echo "v0.6.8.6 passive red-team static audit complete"
exit "$fail"

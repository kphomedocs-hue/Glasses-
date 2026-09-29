#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6aimport63/MainActivity.java"
CORE="$SRC/java/com/parkarsite/g6a"
DIAG="$CORE/IdentityDiagnosticStore.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a63_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a63_hits; fail=1; else echo "PASS: $2 absent"; fi; }

grep -Fq 'android.permission.INTERNET' "$MANIFEST" || { echo "FAIL: INTERNET missing"; fail=1; }
grep -Fq 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" || { echo "FAIL: nearby Wi-Fi missing"; fail=1; }
bad 'java\.net\.Socket|ServerSocket|DatagramSocket|okhttp|AndroidNetworking' 'alternate socket/network stack'
bad 'setRequestMethod\("(POST|PUT|PATCH|DELETE|HEAD)"\)' 'non-GET HTTP method'
bad 'setInstanceFollowRedirects\(true\)|setFollowRedirects\(true\)' 'redirect enablement'
bad 'setRequestProperty\(' 'custom HTTP request headers / Range'
bad '/files/log/|vf_list\.txt|storage/sd0/C/DCIM/1' 'alternate catalog endpoint'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04[[:space:]]*,[[:space:]]*0x02' 'AP-mode payload'
bad 'new byte\[\][[:space:]]*\{[[:space:]]*0x02[[:space:]]*,[[:space:]]*0x03' 'P2P-IP query payload'
bad 'EditText|ACTION_VIEW' 'arbitrary URL/user endpoint input'

grep -Fq 'new File(getFilesDir(),"g6a_imports")' "$JAVA" || { echo "FAIL: app-private persistent root missing"; fail=1; }
grep -Fq 'OpaqueIdentity.sha256(candidate)' "$JAVA" || { echo "FAIL: opaque dedup precheck missing"; fail=1; }
grep -Fq 'importLedger.contains(opaqueId)' "$JAVA" || { echo "FAIL: persistent dedup guard missing"; fail=1; }
grep -Fq 'importCoordinator.importNewJpg' "$JAVA" || { echo "FAIL: coordinator integration missing"; fail=1; }
grep -Fq 'IdentityDiagnosticStore.derive(candidate)' "$JAVA" || { echo "FAIL: diagnostic identity capture missing"; fail=1; }
grep -Fq 'identityDiagnosticStore.commit(candidate)' "$JAVA" || { echo "FAIL: diagnostic capsule commit missing"; fail=1; }
grep -Fq 'Diagnostic capsules contain remote filename/path text: NO' "$JAVA" || { echo "FAIL: diagnostic privacy marker missing"; fail=1; }
grep -Fq 'substring(0,12)' "$DIAG" || { echo "FAIL: diagnostic token truncation missing"; fail=1; }
grep -Fq 'OpaqueIdentity.sha256(remoteIdentity)' "$DIAG" || { echo "FAIL: diagnostic exact hash missing"; fail=1; }
grep -Fq 'OpaqueIdentity.sha256(basename)' "$DIAG" || { echo "FAIL: diagnostic basename hash missing"; fail=1; }
grep -Fq 'OpaqueIdentity.sha256(lower)' "$DIAG" || { echo "FAIL: diagnostic lowercase hash missing"; fail=1; }
grep -Fq 'Ledger entry delta: +1' "$JAVA" || { echo "FAIL: ledger commit evidence missing"; fail=1; }
grep -Fq 'Remote filename/path persisted: NO' "$JAVA" || { echo "FAIL: privacy marker missing"; fail=1; }
grep -Fq 'new byte[]{0x02,0x04}' "$JAVA" || { echo "FAIL: media-count payload missing"; fail=1; }
grep -Fq 'new byte[]{0x02,0x01,0x04,0x01}' "$JAVA" || { echo "FAIL: P2P enter missing"; fail=1; }
grep -Fq 'new byte[]{0x02,0x01,0x09}' "$JAVA" || { echo "FAIL: transfer exit missing"; fail=1; }

url_count="$(grep -Fo 'new URL(' "$JAVA" | wc -l | tr -d ' ')"
[[ "$url_count" == "2" ]] || { echo "FAIL: expected exactly two URL constructor sites, found $url_count"; fail=1; }
get_count="$(grep -Fo 'setRequestMethod("GET")' "$JAVA" | wc -l | tr -d ' ')"
[[ "$get_count" == "2" ]] || { echo "FAIL: expected exactly two GET call sites, found $get_count"; fail=1; }
media_prefix_count="$(grep -Fo 'MEDIA_PREFIX+candidate' "$JAVA" | wc -l | tr -d ' ')"
[[ "$media_prefix_count" == "1" ]] || { echo "FAIL: expected exactly one media URL construction, found $media_prefix_count"; fail=1; }

grep -RniE 'http://|https://' "$CORE" && { echo "FAIL: persistence core must remain transport-free"; fail=1; } || true
grep -RniE 'Bluetooth|WifiP2p|HttpURLConnection|android\.' "$CORE" && { echo "FAIL: persistence core contains transport/android dependency"; fail=1; } || true

grep -Fq 'VERIFY_DEDUP' "$JAVA" || { echo "FAIL: restart dedup stage missing"; fail=1; }
grep -Fq 'Verify restart dedup — NO DOWNLOAD' "$JAVA" || { echo "FAIL: restart dedup UI missing"; fail=1; }
grep -Fq 'Opaque ledger exact matches in current catalog:' "$JAVA" || { echo "FAIL: exact ledger comparison evidence missing"; fail=1; }
grep -Fq 'Basename-derived opaque matches:' "$JAVA" || { echo "FAIL: basename diagnostic missing"; fail=1; }
grep -Fq 'Lowercase-exact opaque matches:' "$JAVA" || { echo "FAIL: lowercase diagnostic missing"; fail=1; }
grep -Fq 'Identity-shape matches:' "$JAVA" || { echo "FAIL: shape diagnostic missing"; fail=1; }
grep -Fq 'G6A IDENTITY DIAGNOSTIC:' "$JAVA" || { echo "FAIL: diagnostic classification missing"; fail=1; }
grep -Fq 'G6A DEDUP VERIFY RESULT: PASS' "$JAVA" || { echo "FAIL: dedup PASS marker missing"; fail=1; }
grep -Fq 'Media-file GET allowed: 0' "$JAVA" || { echo "FAIL: zero-media verification boundary missing"; fail=1; }
grep -Fq 'RetentionDiagnosticStore' "$JAVA" || { echo "FAIL: retention diagnostic integration missing"; fail=1; }
grep -Fq 'retentionDiagnosticStore.commit(baselineCatalog,candidate)' "$JAVA" || { echo "FAIL: baseline/new retention commit missing"; fail=1; }
grep -Fq 'Current catalog equals saved pre-capture baseline exactly:' "$JAVA" || { echo "FAIL: exact baseline comparison missing"; fail=1; }
grep -Fq 'Saved new-item identity present:' "$JAVA" || { echo "FAIL: saved new-item membership check missing"; fail=1; }
grep -Fq 'G6A RETENTION DIAGNOSTIC:' "$JAVA" || { echo "FAIL: retention classification missing"; fail=1; }
grep -Fq 'substring(0,12)' "$CORE/RetentionDiagnosticStore.java" || { echo "FAIL: retention token truncation missing"; fail=1; }
exit "$fail"

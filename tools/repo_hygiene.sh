#!/usr/bin/env bash
set -euo pipefail

fail=0

echo "Checking tracked filenames..."

while IFS= read -r path; do
  lower="$(printf '%s' "$path" | tr '[:upper:]' '[:lower:]')"

  case "$lower" in
    *cyan*glasses*.apk|*cyan*glasses*.zip|*.apks|*.xapk)
      echo "FAIL third-party package tracked: $path"
      fail=1
      ;;
    *.pcap|*.pcapng|*.btsnoop|*.btsnoop_hci)
      echo "FAIL packet/device trace tracked: $path"
      fail=1
      ;;
    *.jpg|*.jpeg|*.heic|*.mov|*.m4a|*.aac|*.wav|*.opus)
      echo "FAIL raw media tracked: $path"
      fail=1
      ;;
    *.mp4)
      echo "FAIL raw video tracked: $path"
      fail=1
      ;;
  esac

done < <(git ls-files)

echo "Checking tracked file sizes..."
while IFS=$'\t' read -r size path; do
  # Keep the public source repository lightweight. Curated releases are expected
  # to remain comfortably below this ceiling.
  if (( size > 100 * 1024 * 1024 )); then
    echo "FAIL tracked file exceeds 100 MiB: $path ($size bytes)"
    fail=1
  fi
done < <(
  git ls-files -z |
  while IFS= read -r -d '' path; do
    if [[ -f "$path" ]]; then
      printf '%s\t%s\n' "$(wc -c < "$path")" "$path"
    fi
  done
)

echo "Checking for literal Bluetooth MAC addresses in public text..."
set +e
MAC_HITS="$(
  git grep -nEI '(^|[^0-9A-Fa-f])([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}([^0-9A-Fa-f]|$)' --     ':!tools/testdata/g1_report_sensitive.txt'     ':!tools/repo_hygiene.sh'
)"
code=$?
set -e
if [[ $code -eq 0 && -n "$MAC_HITS" ]]; then
  echo "FAIL possible literal Bluetooth MAC address:"
  printf '%s\n' "$MAC_HITS"
  fail=1
elif [[ $code -gt 1 ]]; then
  echo "FAIL git grep error"
  fail=1
fi

echo "Verifying frozen v0.1 release hashes..."
echo "84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68  releases/v0.1/K_G1_Discovery_v0_1_source_v4.zip" | sha256sum -c -
echo "c32758f898b91041bc7e13a272096d2629836e4465542ee00c1fbd9764e7479a  releases/v0.1/K_G1_Discovery_v0_1.apk" | sha256sum -c -
echo "e6b23d5d0eaed6a07112f7f14343e011299bf706fe0e98f447a74d24f92c827a  releases/v0.1/K_G1_Discovery_v0_1_package.zip" | sha256sum -c -

if [[ $fail -ne 0 ]]; then
  exit 1
fi

echo "PASS repository hygiene"

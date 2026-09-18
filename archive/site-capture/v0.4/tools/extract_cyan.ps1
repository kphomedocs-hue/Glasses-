param(
  [string]$Package = "com.aitowe.aitoglasses",
  [string]$OutDir = ".\cyan_apk_extract"
)
$ErrorActionPreference = "Stop"
if (-not (Get-Command adb -ErrorAction SilentlyContinue)) { throw "adb not found in PATH" }
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$paths = adb shell pm path $Package
if (-not $paths) { throw "Package $Package not found on connected Android device" }
$pulled = @()
$i = 0
foreach ($line in $paths) {
  $remote = ($line -replace '^package:','').Trim()
  if (-not $remote) { continue }
  $name = Split-Path $remote -Leaf
  if ([string]::IsNullOrWhiteSpace($name)) { $name = "split_$i.apk" }
  $local = Join-Path $OutDir $name
  adb pull $remote $local | Out-Host
  if (-not (Test-Path $local)) { throw "Failed to pull $remote" }
  $pulled += $local
  $i++
}
$meta = Join-Path $OutDir "package_info.txt"
"package=$Package" | Set-Content $meta
adb shell dumpsys package $Package | Select-String -Pattern 'versionName=|versionCode=|firstInstallTime=|lastUpdateTime=' | Add-Content $meta
Write-Host "Extracted $($pulled.Count) APK split(s) to $OutDir"
Write-Host "Package metadata: $meta"

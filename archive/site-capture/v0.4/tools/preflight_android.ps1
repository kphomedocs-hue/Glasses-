$ErrorActionPreference = "Stop"
$Pkg = "com.aitowe.aitoglasses"

if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    throw "adb not found in PATH. Install Android Platform Tools first."
}

$devices = adb devices
if (($devices | Select-String "\tdevice$").Count -lt 1) {
    throw "No authorized Android device detected by adb."
}

$out = New-Object System.Collections.Generic.List[string]
$out.Add("AIMB-G1 PRE-PHYSICAL TEST METADATA")
$out.Add("Captured: $(Get-Date -Format o)")
$out.Add("Phone model: $(adb shell getprop ro.product.model)")
$out.Add("Manufacturer: $(adb shell getprop ro.product.manufacturer)")
$out.Add("Android release: $(adb shell getprop ro.build.version.release)")
$out.Add("Android SDK: $(adb shell getprop ro.build.version.sdk)")
$out.Add("Build fingerprint: $(adb shell getprop ro.build.fingerprint)")
$out.Add("Bluetooth btsnoop mode: $(adb shell settings get global bluetooth_btsnooplogmode 2>$null)")
$out.Add("")
$out.Add("CyanGlasses package: $Pkg")
$pkgDump = adb shell dumpsys package $Pkg 2>$null
$versionLines = $pkgDump | Select-String "versionName=|versionCode="
if ($versionLines) {
    foreach ($line in $versionLines) { $out.Add($line.ToString().Trim()) }
} else {
    $out.Add("CyanGlasses package not found or version could not be read.")
}

$path = Join-Path $PSScriptRoot "..\capture\device_preflight.txt"
$out | Set-Content -Encoding UTF8 $path
$out | ForEach-Object { Write-Host $_ }
Write-Host ""
Write-Host "Saved: $path"

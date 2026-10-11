# Cyan Glasses — exact DEX Wi-Fi identity call-site trace
**Date:** 2026-10-11. **Scope:** static analysis of owner-provided original Cyan APK, no execution or device writes.

## Exact binary identity
- Original application: `com.aitowe.aitoglasses`, version `1.0.2.18_20260811` (85).
- Extracted `base.apk` SHA-256: `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64` — matches the historical provenance record.
- Uploaded `classes.dex` through `classes6.dex` independently compared **byte-for-byte** with the respective entries embedded in that base APK: **all six match exactly**.
- APK bytes, device-specific names/addresses, private keys, and passwords are **not committed**.

## Verified method references (DEX instruction offsets in 16-bit code units)
1. `classes.dex`: `com.aitowe.aitoglasses.ble.receiver.MyBluetoothReceiver.connectStatue(BluetoothDevice, boolean)` has **two direct invoke sites** for `UserConfig.setGlassDeviceWifiName(String)`: code-unit offsets **416** and **487**. Both are on the BLE connection handling path. This is **not** the previously assumed credential-response callback.
2. The same method calls `BluetoothDevice.getName()`, persists Bluetooth device name and address in `UserConfig`, then creates a candidate Wi-Fi name. A name containing `_` follows a split path: a selected trailing component of the stored device name plus `_` plus the colon-stripped saved Bluetooth address. A name without the delimiter follows the whole-name-plus-underscore-plus-normalized-address path. The implementation uses the last split component if multiple components are present; it does not necessarily prefix the result with `AIMB-G1`.
3. In that same source method, `setGlassDeviceWifiPassword(String)` is invoked at offsets **425** and **496** with a predetermined nine-character literal. The literal itself is deliberately withheld. This demonstrates an independent default credential initialization path, not proof that the glasses always accept this value or that no BLE response is ever consumed elsewhere.
4. `classes4.dex`: `com.aitowe.aitoglasses.home.PictureFragment.onPeersChanged(Collection)` at code-unit offset **59** invokes `UserConfig.getGlassDeviceWifiName()`. At offset **64**, it calls Kotlin string `equals(..., ignoreCase=true)` with the discovered Android peer name. If that equality does not succeed, code at offsets **83–93** obtains `UserConfig.getDeviceAddressNoClear()` and applies Kotlin `endsWith` to the peer name.
5. **Important correction to prior research:** the `getDeviceAddressNoClear()` property is assigned the unmodified `BluetoothDevice.getAddress()` value in `connectStatue` (offset **269**), through a standard MMKV delegate. The examined alternate peer predicate directly uses this stored value, with **no additional colon-stripping transformation inside `onPeersChanged`**. Earlier descriptions implying normalization within this suffix branch were too broad.
6. `UserConfig.getGlassDeviceWifiName()` calls `mmkvDelegate.getValue`; `setGlassDeviceWifiName()` calls `mmkvDelegate.setValue`. `UserConfig` initializes `glassDeviceWifiName$delegate` in its constructor, and `UserConfig.<clinit>` declares the Kotlin reflected property `glassDeviceWifiName`.
7. `mmkvDelegate.getValue` / `setValue` use the Kotlin property name when no explicit override key exists, then call `MMKVConfig.getString`/`putString`. `MMKVConfig.getString` delegates to Tencent MMKV `decodeString`; `putString` delegates to `encode`. `MMKVConfig.<init>` initializes an `MMKV.mmkvWithID` named `app_config`. This provides **real persistent per-app storage**, not merely an in-memory variable. Retention across a normal app restart is consistent with that implementation; retention after clearing app data/uninstall is not implied.

## Practical reconstruction (syntax schematic, not device data)
```
on BLE connected:
    bluetoothName = BluetoothDevice.getName() or saved scan-name fallback
    bluetoothAddress = BluetoothDevice.getAddress()
    savedBluetoothName = bluetoothName
    savedBluetoothAddress = bluetoothAddress
    normalizedAddress = removeColons(bluetoothAddress)
    if '_' is in savedBluetoothName:
        prefix = last part of split(savedBluetoothName, '_')
    else:
        prefix = savedBluetoothName
    savedWifiName = prefix + '_' + normalizedAddress
    persist savedWifiName to UserConfig's MMKV-backed property

on Android Wi-Fi Direct peer discovery:
    candidateName = WifiP2pDevice.deviceName
    eligible = equalsIgnoreCase(candidateName, savedWifiName)
               OR endsWith(candidateName, savedDeviceAddressNoClear)
```
The suffix branch is written against the stored `deviceAddressNoClear` value. This scheme is a code-path description, **not** validation that a discovered peer is the user's glasses.

## Relevance to v0.6.8.5 / v0.6.8.6
- These source findings overturn the narrower assumption that Cyan *must first receive a credential-bearing 0x41 response* in order to obtain a candidate P2P Wi-Fi name. Its Bluetooth connection callback **constructs one independently**.
- In v0.6.8.6 the one visible Wi-Fi Direct peer was categorized `OTHER`: its name was nonempty but was neither generic `AIMB-G1` prefix nor a full 12-hex BLE-address suffix.
- An OTHER classification **does not exclude** a Cyan-derived name lacking the model prefix, but a current exact-device match is **not proved** either. The K suffix mismatch remains significant and could reflect a different visible peer, mismatch in relevant device addresses, different actual advertising name, or platform visibility/caching. It must not justify connecting to the sole nearby device.
- Static data supports a **source-code-only exact identity-construction comparison** in a subsequent design review; new physical association/transfer testing is not approved by this file.

## Confidence and limitations
**Direct DEX proof:** two exact setter call sites, name/address composition instructions, MMKV get/put delegation, exact getter use in peer equality, unnormalized stored-address suffix fallback, and no need for new media writes to infer the code structure.

**Unverified on current hardware:** whether Cyan's generated name matches the current AIMB-G1 Wi-Fi Direct advertisement, whether the current BLE address equals the exact address Cyan uses, whether a particular nearby peer is the target, or why recent 0x41 responses are short.

G6A OPEN. G6B BLOCKED. Do not install/run new APK or change peer matching without independent policy/state-machine review.

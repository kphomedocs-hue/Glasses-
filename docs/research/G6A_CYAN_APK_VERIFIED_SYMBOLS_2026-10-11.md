# Cyan APK verified symbol findings — 2026-10-11

The uploaded Cyan Glasses 1.0.2.18_20260811 base and configuration splits were each checked against the supplied manifest. All four SHA-256 values match. The base.apk digest also matches previously recorded forensic provenance: `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`.

Static DEX symbol evidence identifies `UserConfig`, `glassDeviceWifiName`, `glassDeviceWifiName$delegate`, `getGlassDeviceWifiName`, and `setGlassDeviceWifiName`. Symbols confirm an application configuration property but do not establish which callback assigns it, whether the current device provides its value independently, or how long it is retained.

Original third-party APK files and device-specific values have **not** been committed. Targeted read/write call-site analysis remains necessary. No new device test is authorized. G6A OPEN; G6B BLOCKED.

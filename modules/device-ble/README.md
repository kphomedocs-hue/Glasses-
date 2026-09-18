# device-ble

Android BLE discovery/control session.

Owns:
- scanning,
- device selection/identity,
- GATT connect/disconnect,
- service/characteristic discovery,
- notification lifecycle when later authorized,
- Android GATT error/status diagnostics.

Does not own media storage or HTTP transfer.

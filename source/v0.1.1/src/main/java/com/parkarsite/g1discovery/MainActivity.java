package com.parkarsite.g1discovery;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

/**
 * K G1 Discovery v0.1
 *
 * Safety boundary: this activity contains no GATT characteristic writes,
 * descriptor writes, notification subscriptions, Wi-Fi APIs, network APIs,
 * firmware/reset commands, or media-transfer commands.
 */
public final class MainActivity extends Activity {
    private static final int REQ_PERMISSIONS = 1001;
    private static final long SCAN_TIMEOUT_MS = 12_000L;
    private static final String TARGET_NAME = "AIMB-G1";

    private static final UUID CYAN_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");

    private static final UUID DEVICE_INFO_SERVICE = uuid16(0x180A);
    private static final Map<UUID, String> SAFE_DEVICE_INFO = createSafeDeviceInfoMap();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final StringBuilder report = new StringBuilder();
    private final Queue<BluetoothGattCharacteristic> readQueue = new ArrayDeque<>();

    private TextView statusView;
    private TextView reportView;
    private Button scanButton;
    private Button copyButton;
    private Button shareButton;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private BluetoothGatt gatt;
    private boolean scanning;
    private boolean targetFound;
    private Runnable scanTimeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = manager == null ? null : manager.getAdapter();
        renderIdleState();
    }

    private void buildUi() {
        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("K G1 Discovery");
        title.setTextSize(24f);
        root.addView(title);

        TextView note = new TextView(this);
        note.setText("Read-only AIMB-G1 Bluetooth service discovery. No control commands are sent.");
        note.setPadding(0, dp(8), 0, dp(16));
        root.addView(note);

        statusView = new TextView(this);
        statusView.setTextSize(16f);
        root.addView(statusView);

        scanButton = new Button(this);
        scanButton.setText("Scan for AIMB-G1");
        scanButton.setOnClickListener(v -> beginScanFlow());
        root.addView(scanButton);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        copyButton = new Button(this);
        copyButton.setText("Copy report");
        copyButton.setEnabled(false);
        copyButton.setOnClickListener(v -> copyReport());
        actions.addView(copyButton);
        shareButton = new Button(this);
        shareButton.setText("Share report");
        shareButton.setEnabled(false);
        shareButton.setOnClickListener(v -> shareReport());
        actions.addView(shareButton);
        root.addView(actions);

        ScrollView scroll = new ScrollView(this);
        reportView = new TextView(this);
        reportView.setTextSize(13f);
        reportView.setTextIsSelectable(true);
        scroll.addView(reportView);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f));

        setContentView(root);
    }

    private void renderIdleState() {
        if (bluetoothAdapter == null) {
            setStatus("Bluetooth LE is unavailable on this phone.");
            scanButton.setEnabled(false);
            return;
        }
        if (!hasRuntimePermissions()) {
            setStatus("Ready. Tap Scan and allow Nearby Devices permission.");
            return;
        }
        try {
            if (!bluetoothAdapter.isEnabled()) {
                setStatus("Bluetooth is off. Turn Bluetooth on, then tap Scan.");
            } else {
                setStatus("Ready. Turn on AIMB-G1 and tap Scan.");
            }
        } catch (SecurityException e) {
            setStatus("Bluetooth permission is required for discovery.");
        }
    }

    private void beginScanFlow() {
        if (bluetoothAdapter == null) return;
        if (!hasRuntimePermissions()) {
            requestRuntimePermissions();
            return;
        }
        try {
            if (!bluetoothAdapter.isEnabled()) {
                setStatus("Bluetooth is off. Turn it on first.");
                return;
            }
        } catch (SecurityException e) {
            setStatus("Bluetooth permission is required for discovery.");
            return;
        }
        startReadOnlyScan();
    }

    private boolean hasRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, REQ_PERMISSIONS);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_PERMISSIONS) return;
        if (hasRuntimePermissions()) {
            startReadOnlyScan();
        } else {
            setStatus("Bluetooth/Nearby Devices permission is required for discovery.");
        }
    }

    private void startReadOnlyScan() {
        stopScanIfNeeded();
        disconnectGatt();
        report.setLength(0);
        targetFound = false;
        append("K G1 DISCOVERY REPORT");
        append("Generated: " + isoNow());
        append("App version: 0.1");
        append("Safety mode: READ ONLY");
        append("");

        try {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (scanner == null) {
                setStatus("BLE scanner unavailable.");
                return;
            }

            scanning = true;
            scanButton.setEnabled(false);
            setStatus("Scanning for AIMB-G1…");
            scanner.startScan(scanCallback);
        } catch (SecurityException e) {
            scanning = false;
            scanButton.setEnabled(true);
            setStatus("Bluetooth permission is required for discovery.");
            return;
        }
        scanTimeout = () -> {
            if (!scanning) return;
            stopScanIfNeeded();
            if (!targetFound) {
                append("RESULT: AIMB-G1 not found during 12-second scan.");
                finishReport("AIMB-G1 not found. Report ready.");
            }
        };
        mainHandler.postDelayed(scanTimeout, SCAN_TIMEOUT_MS);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String name = null;
            try {
                if (result.getScanRecord() != null) name = result.getScanRecord().getDeviceName();
                if (name == null && hasRuntimePermissions()) name = device.getName();
            } catch (SecurityException ignored) {
                // Permission state changed mid-scan; fail closed below.
            }

            if (name != null && TARGET_NAME.equalsIgnoreCase(name.trim()) && !targetFound) {
                targetFound = true;
                stopScanIfNeeded();
                append("TARGET");
                append("Advertised name: " + name);
                append("Bluetooth address: " + maskedAddress(device));
                append("");
                connectReadOnly(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            stopScanIfNeeded();
            append("SCAN ERROR: Android BLE scan error code " + errorCode);
            finishReport("Scan failed. Report ready.");
        }
    };

    private void connectReadOnly(BluetoothDevice device) {
        setStatus("AIMB-G1 found. Connecting read-only…");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                gatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
            } else {
                gatt = device.connectGatt(this, false, gattCallback);
            }
        } catch (SecurityException e) {
            append("CONNECT ERROR: Bluetooth permission unavailable.");
            finishReport("Unable to connect. Report ready.");
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt callbackGatt, int status, int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                appendThreadSafe("GATT ERROR: connection status " + status);
                closeAndFinish("Connection failed. Report ready.");
                return;
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                appendThreadSafe("GATT: connected");
                setStatusThreadSafe("Connected. Discovering services…");
                try {
                    boolean started = callbackGatt.discoverServices();
                    if (!started) {
                        appendThreadSafe("GATT ERROR: discoverServices() did not start.");
                        closeAndFinish("Service discovery did not start. Report ready.");
                    }
                } catch (SecurityException e) {
                    appendThreadSafe("GATT ERROR: permission lost before service discovery.");
                    closeAndFinish("Permission error. Report ready.");
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                appendThreadSafe("GATT: disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt callbackGatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                appendThreadSafe("GATT ERROR: service discovery status " + status);
                closeAndFinish("Service discovery failed. Report ready.");
                return;
            }

            appendThreadSafe("");
            appendThreadSafe("SERVICES");
            List<BluetoothGattService> services = callbackGatt.getServices();
            for (BluetoothGattService service : services) {
                appendThreadSafe("SERVICE " + service.getUuid());
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    appendThreadSafe("  CHAR " + characteristic.getUuid()
                            + " [" + describeProperties(characteristic.getProperties()) + "]");
                }
            }

            boolean servicePresent = callbackGatt.getService(CYAN_SERVICE) != null;
            BluetoothGattService cyan = callbackGatt.getService(CYAN_SERVICE);
            boolean notifyPresent = cyan != null && cyan.getCharacteristic(CYAN_NOTIFY) != null;
            boolean writePresent = cyan != null && cyan.getCharacteristic(CYAN_WRITE) != null;

            appendThreadSafe("");
            appendThreadSafe("CYAN PROFILE CHECK");
            appendThreadSafe("Service de5bf728…: " + yesNo(servicePresent));
            appendThreadSafe("Notify de5bf729…: " + yesNo(notifyPresent));
            appendThreadSafe("Write  de5bf72a…: " + yesNo(writePresent));

            queueSafeDeviceInfoReads(callbackGatt);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt callbackGatt,
                                         BluetoothGattCharacteristic characteristic,
                                         byte[] value,
                                         int status) {
            handleCharacteristicRead(callbackGatt, characteristic, value, status);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onCharacteristicRead(BluetoothGatt callbackGatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            handleCharacteristicRead(callbackGatt, characteristic, characteristic.getValue(), status);
        }
    };

    private void queueSafeDeviceInfoReads(BluetoothGatt callbackGatt) {
        readQueue.clear();
        BluetoothGattService info = callbackGatt.getService(DEVICE_INFO_SERVICE);
        if (info != null) {
            for (Map.Entry<UUID, String> entry : SAFE_DEVICE_INFO.entrySet()) {
                BluetoothGattCharacteristic c = info.getCharacteristic(entry.getKey());
                if (c != null && (c.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                    readQueue.add(c);
                }
            }
        }
        if (readQueue.isEmpty()) {
            appendThreadSafe("");
            appendThreadSafe("DEVICE INFO: no safe readable standard fields exposed.");
            closeAndFinish("Discovery complete. Disconnected. Report ready.");
        } else {
            appendThreadSafe("");
            appendThreadSafe("DEVICE INFO (standard readable fields only)");
            readNext(callbackGatt);
        }
    }

    private void readNext(BluetoothGatt callbackGatt) {
        BluetoothGattCharacteristic next = readQueue.poll();
        if (next == null) {
            closeAndFinish("Discovery complete. Disconnected. Report ready.");
            return;
        }
        try {
            boolean started = callbackGatt.readCharacteristic(next);
            if (!started) {
                appendThreadSafe("  " + labelFor(next.getUuid()) + ": read did not start");
                readNext(callbackGatt);
            }
        } catch (SecurityException e) {
            appendThreadSafe("  " + labelFor(next.getUuid()) + ": permission error");
            readNext(callbackGatt);
        }
    }

    private void handleCharacteristicRead(BluetoothGatt callbackGatt,
                                          BluetoothGattCharacteristic characteristic,
                                          byte[] value,
                                          int status) {
        if (SAFE_DEVICE_INFO.containsKey(characteristic.getUuid())) {
            String label = labelFor(characteristic.getUuid());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                appendThreadSafe("  " + label + ": " + printable(value));
            } else {
                appendThreadSafe("  " + label + ": unavailable (status " + status + ")");
            }
        }
        readNext(callbackGatt);
    }

    private void closeAndFinish(String message) {
        mainHandler.post(() -> {
            disconnectGatt();
            finishReport(message);
        });
    }

    private void finishReport(String message) {
        append("END REPORT");
        setStatus(message);
        reportView.setText(report.toString());
        copyButton.setEnabled(report.length() > 0);
        shareButton.setEnabled(report.length() > 0);
        scanButton.setEnabled(true);
    }

    private void disconnectGatt() {
        BluetoothGatt local = gatt;
        gatt = null;
        if (local != null) {
            try {
                local.disconnect();
            } catch (SecurityException ignored) {
            }
            try {
                local.close();
            } catch (SecurityException ignored) {
            }
        }
    }

    private void stopScanIfNeeded() {
        if (scanTimeout != null) {
            mainHandler.removeCallbacks(scanTimeout);
            scanTimeout = null;
        }
        if (scanning && scanner != null) {
            try {
                scanner.stopScan(scanCallback);
            } catch (SecurityException ignored) {
            }
        }
        scanning = false;
    }

    private void copyReport() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("G1 Discovery Report", report.toString()));
            Toast.makeText(this, "Report copied", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareReport() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "AIMB-G1 Discovery Report");
        send.putExtra(Intent.EXTRA_TEXT, report.toString());
        startActivity(Intent.createChooser(send, "Share discovery report"));
    }

    @Override
    protected void onDestroy() {
        stopScanIfNeeded();
        disconnectGatt();
        super.onDestroy();
    }

    private void append(String line) {
        report.append(line).append('\n');
        reportView.setText(report.toString());
    }

    private void appendThreadSafe(String line) {
        mainHandler.post(() -> append(line));
    }

    private void setStatus(String text) {
        statusView.setText(text);
    }

    private void setStatusThreadSafe(String text) {
        mainHandler.post(() -> setStatus(text));
    }

    private static String describeProperties(int p) {
        StringBuilder out = new StringBuilder();
        addProperty(out, p, BluetoothGattCharacteristic.PROPERTY_READ, "READ");
        addProperty(out, p, BluetoothGattCharacteristic.PROPERTY_WRITE, "WRITE");
        addProperty(out, p, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, "WRITE_NO_RESPONSE");
        addProperty(out, p, BluetoothGattCharacteristic.PROPERTY_NOTIFY, "NOTIFY");
        addProperty(out, p, BluetoothGattCharacteristic.PROPERTY_INDICATE, "INDICATE");
        addProperty(out, p, BluetoothGattCharacteristic.PROPERTY_BROADCAST, "BROADCAST");
        return out.length() == 0 ? "NONE" : out.toString();
    }

    private static void addProperty(StringBuilder out, int properties, int bit, String label) {
        if ((properties & bit) != 0) {
            if (out.length() > 0) out.append('|');
            out.append(label);
        }
    }

    private static String printable(byte[] value) {
        if (value == null || value.length == 0) return "<empty>";
        StringBuilder s = new StringBuilder();
        for (byte b : value) {
            int c = b & 0xff;
            if (c >= 32 && c <= 126) s.append((char) c);
            else if (c == 0) break;
            else s.append('?');
        }
        String cleaned = s.toString().trim();
        return cleaned.isEmpty() ? "<non-text value>" : cleaned;
    }

    private static String maskedAddress(BluetoothDevice device) {
        try {
            String a = device.getAddress();
            if (a == null || a.length() < 5) return "masked";
            return "**:**:**:**:" + a.substring(a.length() - 5);
        } catch (SecurityException e) {
            return "masked";
        }
    }

    private static String yesNo(boolean value) {
        return value ? "PRESENT" : "NOT FOUND";
    }

    private static UUID uuid16(int shortUuid) {
        return UUID.fromString(String.format(Locale.US,
                "0000%04x-0000-1000-8000-00805f9b34fb", shortUuid));
    }

    private static Map<UUID, String> createSafeDeviceInfoMap() {
        Map<UUID, String> map = new HashMap<>();
        map.put(uuid16(0x2A24), "Model number");
        map.put(uuid16(0x2A26), "Firmware revision");
        map.put(uuid16(0x2A27), "Hardware revision");
        map.put(uuid16(0x2A28), "Software revision");
        map.put(uuid16(0x2A29), "Manufacturer");
        // Deliberately omit 0x2A25 Serial Number String.
        return map;
    }

    private static String labelFor(UUID uuid) {
        String label = SAFE_DEVICE_INFO.get(uuid);
        return label == null ? uuid.toString() : label;
    }

    private static String isoNow() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(new Date());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

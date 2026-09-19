package com.parkarsite.g1responseprobe;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * K G1 Response Probe v0.2
 *
 * G2 safety boundary:
 * - connects only to one uniquely bonded AIMB-G1-family device,
 * - discovers the physically confirmed Cyan service,
 * - enables notifications only on the physically confirmed response characteristic,
 * - performs the standard CCCD descriptor write required to enable notifications,
 * - observes for a bounded interval and disconnects.
 *
 * Deliberately absent:
 * - proprietary characteristic writes,
 * - control/media payloads,
 * - Wi-Fi/P2P/AP,
 * - HTTP/network,
 * - pairing/unpairing/reset,
 * - OTA/firmware operations.
 */
public final class MainActivity extends Activity {
    private static final int REQ_PERMISSION = 2001;
    private static final long CONNECT_TIMEOUT_MS = 30_000L;
    private static final long OBSERVATION_MS = 30_000L;
    private static final int MAX_LOGGED_NOTIFICATIONS = 20;
    private static final int MAX_LOGGED_BYTES_PER_NOTIFICATION = 64;
    private static final String TARGET_NAME = "AIMB-G1";

    private static final UUID CYAN_SERVICE =
            UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_NOTIFY =
            UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CLIENT_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final StringBuilder report = new StringBuilder();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic notifyCharacteristic;

    private TextView statusView;
    private TextView reportView;
    private Button runButton;
    private Button copyButton;
    private Button shareButton;

    private Runnable connectTimeout;
    private Runnable observationTimeout;
    private boolean reportFinished;
    private boolean observationStarted;
    private int notificationCount;
    private int loggedNotificationCount;
    private long notificationBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        BluetoothManager manager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = manager == null ? null : manager.getAdapter();
        renderIdleState();
    }

    private void buildUi() {
        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("K G1 Response Probe");
        title.setTextSize(24f);
        root.addView(title);

        TextView note = new TextView(this);
        note.setText(
                "G2 notification-only probe. It enables the confirmed Cyan response notification path, observes for 30 seconds, and sends no proprietary control command.");
        note.setPadding(0, dp(8), 0, dp(16));
        root.addView(note);

        statusView = new TextView(this);
        statusView.setTextSize(16f);
        root.addView(statusView);

        runButton = new Button(this);
        runButton.setText("Run notification-only G2 probe");
        runButton.setOnClickListener(v -> beginProbeFlow());
        root.addView(runButton);

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
            setStatus("Bluetooth is unavailable on this phone.");
            runButton.setEnabled(false);
            return;
        }
        if (!hasBluetoothConnectPermission()) {
            setStatus("Ready. Tap Run and allow Nearby Devices/Bluetooth permission.");
            return;
        }
        try {
            setStatus(bluetoothAdapter.isEnabled()
                    ? "Ready. Force-stop Cyan Glasses, turn on AIMB-G1, then run the G2 probe."
                    : "Bluetooth is off. Turn Bluetooth on first.");
        } catch (SecurityException e) {
            setStatus("Bluetooth permission is required.");
        }
    }

    private void beginProbeFlow() {
        if (bluetoothAdapter == null) return;

        if (!hasBluetoothConnectPermission()) {
            requestBluetoothConnectPermission();
            return;
        }

        try {
            if (!bluetoothAdapter.isEnabled()) {
                setStatus("Bluetooth is off. Turn it on first.");
                return;
            }
        } catch (SecurityException e) {
            setStatus("Bluetooth permission is required.");
            return;
        }

        runNotificationOnlyProbe();
    }

    private boolean hasBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQ_PERMISSION);
        } else {
            runNotificationOnlyProbe();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_PERMISSION) return;

        if (hasBluetoothConnectPermission()) {
            runNotificationOnlyProbe();
        } else {
            setStatus("Bluetooth/Nearby Devices permission is required.");
        }
    }

    private void runNotificationOnlyProbe() {
        cancelTimers();
        disconnectGatt();

        report.setLength(0);
        reportFinished = false;
        observationStarted = false;
        notificationCount = 0;
        loggedNotificationCount = 0;
        notificationBytes = 0;

        runButton.setEnabled(false);
        copyButton.setEnabled(false);
        shareButton.setEnabled(false);

        append("K G1 RESPONSE PROBE REPORT");
        append("Generated: " + isoNow());
        append("App version: 0.2");
        append("Gate: G2 response-channel confirmation");
        append("Safety mode: NOTIFICATION ONLY");
        append("Proprietary characteristic writes: DISABLED / NOT IMPLEMENTED");
        append("");

        BluetoothDevice target = findUniqueBondedTarget();
        if (target == null) return;

        append("");
        append("GATT CONNECTION");
        append("Target selection: exactly one bonded AIMB-G1-family device");
        append("Target name: " + safeNameForReport(safeGetName(target)));
        append("Device type: " + deviceTypeLabel(target));
        append("Transport requested: LE");
        append("Bluetooth address: not logged");
        append("");

        setStatus("Connecting to the uniquely bonded AIMB-G1 over LE GATT…");
        scheduleConnectTimeout();

        try {
            gatt = target.connectGatt(
                    this,
                    false,
                    gattCallback,
                    BluetoothDevice.TRANSPORT_LE);
        } catch (SecurityException e) {
            finishReport("Bluetooth permission error while connecting.");
        } catch (RuntimeException e) {
            finishReport("Android rejected the LE GATT connection request.");
        }
    }

    private BluetoothDevice findUniqueBondedTarget() {
        append("BONDED TARGET CHECK");

        int total = 0;
        int matches = 0;
        BluetoothDevice unique = null;

        try {
            Set<BluetoothDevice> bonded = bluetoothAdapter.getBondedDevices();
            total = bonded == null ? 0 : bonded.size();

            if (bonded != null) {
                for (BluetoothDevice device : bonded) {
                    if (matchesTargetName(safeGetName(device))) {
                        matches++;
                        unique = device;
                    }
                }
            }
        } catch (SecurityException e) {
            append("Bonded-device metadata unavailable: Bluetooth permission error.");
            finishReport("Unable to inspect bonded devices.");
            return null;
        }

        append("Bonded devices total: " + total);
        append("AIMB-G1-family bonded matches: " + matches);

        if (matches != 1 || unique == null) {
            append("RESULT: refusing to select a target because exactly one AIMB-G1-family bonded device is required.");
            finishReport("No unique bonded AIMB-G1 target.");
            return null;
        }

        append("Bonded target name: " + safeNameForReport(safeGetName(unique)));
        append("Bonded target address: not logged");
        return unique;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(
                BluetoothGatt callbackGatt,
                int status,
                int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                appendThreadSafe("GATT ERROR: connection status " + status);
                closeAndFinish("GATT connection failed.");
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                appendThreadSafe("GATT: connected");
                setStatusThreadSafe("Connected. Discovering the confirmed Cyan response path…");

                try {
                    if (!callbackGatt.discoverServices()) {
                        appendThreadSafe("GATT ERROR: discoverServices() did not start.");
                        closeAndFinish("Service discovery did not start.");
                    }
                } catch (SecurityException e) {
                    appendThreadSafe("GATT ERROR: permission lost before service discovery.");
                    closeAndFinish("Bluetooth permission error.");
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                appendThreadSafe("GATT: disconnected");
                if (!reportFinished && !observationStarted) {
                    closeAndFinish("Disconnected before notification observation began.");
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt callbackGatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                appendThreadSafe("GATT ERROR: service discovery status " + status);
                closeAndFinish("Service discovery failed.");
                return;
            }

            BluetoothGattService service = callbackGatt.getService(CYAN_SERVICE);
            if (service == null) {
                appendThreadSafe("CYAN SERVICE: NOT FOUND");
                closeAndFinish("Confirmed Cyan service was not found.");
                return;
            }

            BluetoothGattCharacteristic response = service.getCharacteristic(CYAN_NOTIFY);
            if (response == null) {
                appendThreadSafe("CYAN NOTIFY: NOT FOUND");
                closeAndFinish("Confirmed Cyan notify characteristic was not found.");
                return;
            }

            if ((response.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                appendThreadSafe("CYAN NOTIFY: characteristic present but NOTIFY property is absent.");
                closeAndFinish("Response characteristic is not notifiable.");
                return;
            }

            appendThreadSafe("CYAN SERVICE: PRESENT");
            appendThreadSafe("CYAN NOTIFY: PRESENT / NOTIFY");
            notifyCharacteristic = response;
            enableConfirmedNotificationPath(callbackGatt, response);
        }

        @Override
        public void onDescriptorWrite(
                BluetoothGatt callbackGatt,
                BluetoothGattDescriptor descriptor,
                int status) {
            if (!CLIENT_CONFIG.equals(descriptor.getUuid())
                    || descriptor.getCharacteristic() == null
                    || !CYAN_NOTIFY.equals(descriptor.getCharacteristic().getUuid())) {
                appendThreadSafe("GATT ERROR: unexpected descriptor callback.");
                closeAndFinish("Unexpected descriptor callback.");
                return;
            }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                appendThreadSafe("CCCD write status: " + status);
                closeAndFinish("Notification subscription failed.");
                return;
            }

            appendThreadSafe("CCCD write status: SUCCESS");
            appendThreadSafe("Response notifications: ENABLED");
            startObservation();
        }

        @Override
        public void onCharacteristicChanged(
                BluetoothGatt callbackGatt,
                BluetoothGattCharacteristic characteristic,
                byte[] value) {
            handleNotification(characteristic, value);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onCharacteristicChanged(
                BluetoothGatt callbackGatt,
                BluetoothGattCharacteristic characteristic) {
            handleNotification(characteristic, characteristic.getValue());
        }
    };

    @SuppressWarnings("deprecation")
    private void enableConfirmedNotificationPath(
            BluetoothGatt callbackGatt,
            BluetoothGattCharacteristic response) {
        appendThreadSafe("");
        appendThreadSafe("NOTIFICATION SUBSCRIPTION");
        appendThreadSafe("Characteristic: de5bf729-d711-4e47-af26-65e3012a5dc7");
        appendThreadSafe("CCCD: 00002902-0000-1000-8000-00805f9b34fb");
        appendThreadSafe("Characteristic write: NONE");

        try {
            boolean localEnabled =
                    callbackGatt.setCharacteristicNotification(response, true);
            if (!localEnabled) {
                appendThreadSafe("Local notification registration: FAILED");
                closeAndFinish("Android rejected local notification registration.");
                return;
            }
            appendThreadSafe("Local notification registration: SUCCESS");

            BluetoothGattDescriptor cccd = response.getDescriptor(CLIENT_CONFIG);
            if (cccd == null) {
                appendThreadSafe("CCCD: NOT FOUND");
                closeAndFinish("CCCD descriptor not present.");
                return;
            }

            cccd.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            if (!callbackGatt.writeDescriptor(cccd)) {
                appendThreadSafe("CCCD write start: FAILED");
                closeAndFinish("CCCD write did not start.");
                return;
            }

            appendThreadSafe("CCCD write start: SUCCESS");
        } catch (SecurityException e) {
            appendThreadSafe("Subscription error: Bluetooth permission lost.");
            closeAndFinish("Bluetooth permission error during subscription.");
        }
    }

    private void startObservation() {
        mainHandler.post(() -> {
            if (reportFinished || observationStarted) return;

            cancelConnectTimeout();
            observationStarted = true;

            append("");
            append("OBSERVATION");
            append("Window: 30 seconds");
            append("No proprietary command will be sent.");
            setStatus("Notification path enabled. Observing for 30 seconds…");

            observationTimeout = () -> {
                if (reportFinished) return;

                append("");
                append("OBSERVATION SUMMARY");
                append("Notifications received: " + notificationCount);
                append("Notification bytes received: " + notificationBytes);
                if (notificationCount == 0) {
                    append("Result: no spontaneous response notification observed.");
                } else {
                    append("Result: spontaneous response traffic observed.");
                }

                disconnectGatt();
                finishReport("G2 notification-only observation complete.");
            };
            mainHandler.postDelayed(observationTimeout, OBSERVATION_MS);
        });
    }

    private void handleNotification(
            BluetoothGattCharacteristic characteristic,
            byte[] value) {
        if (characteristic == null
                || !CYAN_NOTIFY.equals(characteristic.getUuid())
                || reportFinished) {
            return;
        }

        byte[] safeValue = value == null ? new byte[0] : value.clone();
        mainHandler.post(() -> {
            if (reportFinished) return;

            notificationCount++;
            notificationBytes += safeValue.length;

            if (loggedNotificationCount < MAX_LOGGED_NOTIFICATIONS) {
                loggedNotificationCount++;
                append("NOTIFY #" + notificationCount
                        + " len=" + safeValue.length
                        + " hex=" + hexPreview(
                                safeValue,
                                MAX_LOGGED_BYTES_PER_NOTIFICATION));
            }
        });
    }

    private void scheduleConnectTimeout() {
        cancelConnectTimeout();
        connectTimeout = () -> {
            if (reportFinished || observationStarted) return;
            append("GATT TIMEOUT: subscription was not established within 30 seconds.");
            disconnectGatt();
            finishReport("GATT/subscription timeout.");
        };
        mainHandler.postDelayed(connectTimeout, CONNECT_TIMEOUT_MS);
    }

    private void cancelConnectTimeout() {
        if (connectTimeout != null) {
            mainHandler.removeCallbacks(connectTimeout);
            connectTimeout = null;
        }
    }

    private void cancelObservationTimeout() {
        if (observationTimeout != null) {
            mainHandler.removeCallbacks(observationTimeout);
            observationTimeout = null;
        }
    }

    private void cancelTimers() {
        cancelConnectTimeout();
        cancelObservationTimeout();
    }

    private void closeAndFinish(String message) {
        mainHandler.post(() -> {
            disconnectGatt();
            finishReport(message);
        });
    }

    private void finishReport(String message) {
        if (reportFinished) return;

        cancelTimers();
        reportFinished = true;

        append("END REPORT");
        setStatus(message);
        reportView.setText(report.toString());

        copyButton.setEnabled(report.length() > 0);
        shareButton.setEnabled(report.length() > 0);
        runButton.setEnabled(true);
    }

    private void disconnectGatt() {
        BluetoothGatt local = gatt;
        gatt = null;
        notifyCharacteristic = null;

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

    private void copyReport() {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(
                    ClipData.newPlainText("G2 Response Probe Report", report.toString()));
            Toast.makeText(this, "Report copied", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareReport() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "AIMB-G1 G2 Response Probe Report");
        send.putExtra(Intent.EXTRA_TEXT, report.toString());
        startActivity(Intent.createChooser(send, "Share response probe report"));
    }

    @Override
    protected void onDestroy() {
        cancelTimers();
        disconnectGatt();
        super.onDestroy();
    }

    private void append(String line) {
        report.append(line).append('\n');
        reportView.setText(report.toString());
    }

    private void appendThreadSafe(String line) {
        mainHandler.post(() -> {
            if (!reportFinished) append(line);
        });
    }

    private void setStatus(String text) {
        statusView.setText(text);
    }

    private void setStatusThreadSafe(String text) {
        mainHandler.post(() -> setStatus(text));
    }

    private static boolean matchesTargetName(String name) {
        if (name == null) return false;
        String normalized = name.trim().toUpperCase(Locale.US);
        return normalized.equals(TARGET_NAME)
                || normalized.startsWith(TARGET_NAME + "_");
    }

    private static String safeGetName(BluetoothDevice device) {
        if (device == null) return null;
        try {
            return device.getName();
        } catch (SecurityException e) {
            return null;
        }
    }

    private static String safeNameForReport(String name) {
        if (name == null || name.trim().isEmpty()) return "<none>";
        String normalized = name.trim().toUpperCase(Locale.US);
        if (normalized.equals(TARGET_NAME)) return TARGET_NAME;
        if (normalized.startsWith(TARGET_NAME + "_")) {
            return TARGET_NAME + "_<suffix>";
        }
        return "<non-AIMB name not logged>";
    }

    private static String deviceTypeLabel(BluetoothDevice device) {
        if (device == null) return "UNKNOWN";
        try {
            switch (device.getType()) {
                case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                    return "CLASSIC";
                case BluetoothDevice.DEVICE_TYPE_LE:
                    return "LE";
                case BluetoothDevice.DEVICE_TYPE_DUAL:
                    return "DUAL";
                default:
                    return "UNKNOWN";
            }
        } catch (SecurityException e) {
            return "UNKNOWN (permission)";
        }
    }

    private static String hexPreview(byte[] value, int maxBytes) {
        if (value == null || value.length == 0) return "<empty>";

        int n = Math.min(value.length, maxBytes);
        StringBuilder out = new StringBuilder(n * 3 + 16);

        for (int i = 0; i < n; i++) {
            if (i > 0) out.append(' ');
            out.append(String.format(Locale.US, "%02X", value[i] & 0xFF));
        }

        if (value.length > n) {
            out.append(" …(+").append(value.length - n).append(" bytes)");
        }

        return out.toString();
    }

    private static String isoNow() {
        return new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ssZ",
                Locale.US).format(new Date());
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density);
    }
}

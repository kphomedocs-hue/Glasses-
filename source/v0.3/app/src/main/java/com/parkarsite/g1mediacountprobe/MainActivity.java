package com.parkarsite.g1mediacountprobe;

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
 * K G1 Media Count Probe v0.3
 *
 * G3 boundary:
 * - connect to exactly one bonded AIMB-G1-family device,
 * - enable the physically confirmed Cyan notification path,
 * - send exactly one command-0x41 frame with the exact Cyan media-count payload 02 04,
 * - observe and record raw framed responses,
 * - disconnect.
 *
 * Deliberately absent:
 * - media-mode payloads 02 01 04 01 / 02 01 04 02,
 * - Wi-Fi/P2P/AP,
 * - HTTP/media transfer,
 * - delete/modify/reset/restart/OTA,
 * - arbitrary command input,
 * - proprietary-write retry.
 */
public final class MainActivity extends Activity {
    private static final int REQ_PERMISSION = 3001;
    private static final long CONNECT_TIMEOUT_MS = 30_000L;
    private static final long RESPONSE_WINDOW_MS = 12_000L;
    private static final int MAX_LOGGED_NOTIFICATIONS = 24;
    private static final int MAX_LOGGED_BYTES = 96;
    private static final String TARGET_NAME = "AIMB-G1";

    private static final UUID CYAN_SERVICE =
            UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_NOTIFY =
            UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_WRITE =
            UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CLIENT_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final byte[] MEDIA_COUNT_PAYLOAD =
            new byte[]{0x02, 0x04};

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final StringBuilder report = new StringBuilder();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic writeCharacteristic;

    private TextView statusView;
    private TextView reportView;
    private Button runButton;
    private Button copyButton;
    private Button shareButton;

    private Runnable connectTimeout;
    private Runnable responseTimeout;
    private boolean reportFinished;
    private boolean commandWriteAttempted;
    private boolean responseWindowStarted;
    private int notificationCount;
    private int command41NotificationCount;
    private int command73NotificationCount;

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
        title.setText("K G1 Media Count Probe");
        title.setTextSize(24f);
        root.addView(title);

        TextView note = new TextView(this);
        note.setText(
                "G3: sends exactly one Cyan media-count query (0x41 / 02 04), records raw responses, then disconnects. No media mode or Wi-Fi.");
        note.setPadding(0, dp(8), 0, dp(16));
        root.addView(note);

        statusView = new TextView(this);
        statusView.setTextSize(16f);
        root.addView(statusView);

        runButton = new Button(this);
        runButton.setText("Run single media-count query");
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
                    ? "Ready. Force-stop Cyan Glasses, keep AIMB-G1 paired, then run G3."
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

        runSingleQueryProbe();
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
            runSingleQueryProbe();
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
            runSingleQueryProbe();
        } else {
            setStatus("Bluetooth/Nearby Devices permission is required.");
        }
    }

    private void runSingleQueryProbe() {
        cancelTimers();
        disconnectGatt();

        report.setLength(0);
        reportFinished = false;
        commandWriteAttempted = false;
        responseWindowStarted = false;
        notificationCount = 0;
        command41NotificationCount = 0;
        command73NotificationCount = 0;

        runButton.setEnabled(false);
        copyButton.setEnabled(false);
        shareButton.setEnabled(false);

        append("K G1 MEDIA COUNT PROBE REPORT");
        append("Generated: " + isoNow());
        append("App version: 0.3");
        append("Gate: G3 one-command media inventory/count query");
        append("Allowed proprietary command: 0x41 with payload 02 04 ONLY");
        append("Maximum proprietary characteristic writes: 1");
        append("Media-mode commands: NOT IMPLEMENTED");
        append("Wi-Fi/network: NOT IMPLEMENTED");
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

        setStatus("Connecting to AIMB-G1 over LE GATT…");
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
            append("RESULT: refusing target selection because exactly one AIMB-G1-family bonded device is required.");
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
                setStatusThreadSafe("Connected. Discovering confirmed Cyan characteristics…");

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
                if (!reportFinished) {
                    closeAndFinish("Disconnected before G3 completed.");
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

            BluetoothGattCharacteristic notify = service.getCharacteristic(CYAN_NOTIFY);
            BluetoothGattCharacteristic writer = service.getCharacteristic(CYAN_WRITE);

            if (notify == null
                    || (notify.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                appendThreadSafe("CYAN NOTIFY: missing or not notifiable");
                closeAndFinish("Confirmed response characteristic unavailable.");
                return;
            }

            if (writer == null
                    || (writer.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
                appendThreadSafe("CYAN WRITE: missing or WRITE property unavailable");
                closeAndFinish("Confirmed write characteristic unavailable.");
                return;
            }

            appendThreadSafe("CYAN SERVICE: PRESENT");
            appendThreadSafe("CYAN NOTIFY: PRESENT / NOTIFY");
            appendThreadSafe("CYAN WRITE: PRESENT / WRITE");
            writeCharacteristic = writer;
            enableConfirmedNotificationPath(callbackGatt, notify);
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
            sendSingleMediaCountQuery(callbackGatt);
        }

        @Override
        public void onCharacteristicWrite(
                BluetoothGatt callbackGatt,
                BluetoothGattCharacteristic characteristic,
                int status) {
            if (characteristic == null || !CYAN_WRITE.equals(characteristic.getUuid())) {
                appendThreadSafe("GATT ERROR: unexpected characteristic-write callback.");
                closeAndFinish("Unexpected write callback.");
                return;
            }

            appendThreadSafe("0x41 / 02 04 characteristic write status: " + statusLabel(status));

            if (status != BluetoothGatt.GATT_SUCCESS) {
                closeAndFinish("Single media-count query write failed.");
            }
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

    @SuppressWarnings("deprecation")
    private void sendSingleMediaCountQuery(BluetoothGatt callbackGatt) {
        mainHandler.post(() -> {
            if (reportFinished || commandWriteAttempted) return;

            BluetoothGattCharacteristic writer = writeCharacteristic;
            if (writer == null) {
                finishReport("Confirmed write characteristic is unavailable.");
                return;
            }

            cancelConnectTimeout();

            byte[] frame = frameCommand41MediaCount();

            append("");
            append("SINGLE PROPRIETARY WRITE");
            append("Command: 0x41");
            append("Payload: 02 04");
            append("Purpose: media inventory/count query");
            append("Frame bytes: " + hexPreview(frame, frame.length));
            append("Frame CRC: VALIDATED BEFORE WRITE");
            append("Write type: WRITE_DEFAULT");
            append("No retry policy: TRUE");

            commandWriteAttempted = true;
            writer.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            writer.setValue(frame);

            try {
                boolean started = callbackGatt.writeCharacteristic(writer);
                append("0x41 / 02 04 write start: " + (started ? "SUCCESS" : "FAILED"));

                if (!started) {
                    disconnectGatt();
                    finishReport("Android did not start the single media-count query.");
                    return;
                }

                startResponseWindow();
            } catch (SecurityException e) {
                disconnectGatt();
                finishReport("Bluetooth permission error during media-count query.");
            } catch (RuntimeException e) {
                disconnectGatt();
                finishReport("Android rejected the single media-count query.");
            }
        });
    }

    private void startResponseWindow() {
        if (reportFinished || responseWindowStarted) return;

        responseWindowStarted = true;
        append("");
        append("RESPONSE OBSERVATION");
        append("Window: 12 seconds");
        append("Additional proprietary writes: 0");
        append("Response parsing policy: RAW FRAMES ONLY; semantic decode after review");
        setStatus("Media-count query sent once. Observing responses for 12 seconds…");

        responseTimeout = () -> {
            if (reportFinished) return;

            append("");
            append("OBSERVATION SUMMARY");
            append("Notifications received: " + notificationCount);
            append("Command 0x41 notifications: " + command41NotificationCount);
            append("Command 0x73 notifications: " + command73NotificationCount);
            append("Proprietary characteristic writes attempted: "
                    + (commandWriteAttempted ? 1 : 0));

            disconnectGatt();
            finishReport("G3 single media-count query observation complete.");
        };
        mainHandler.postDelayed(responseTimeout, RESPONSE_WINDOW_MS);
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
            int command = framedCommand(safeValue);
            if (command == 0x41) command41NotificationCount++;
            if (command == 0x73) command73NotificationCount++;

            String validation = validateFrame(safeValue) ? "PASS" : "UNVERIFIED";
            if (notificationCount <= MAX_LOGGED_NOTIFICATIONS) {
                append("NOTIFY #" + notificationCount
                        + " cmd=" + commandLabel(command)
                        + " frame=" + validation
                        + " len=" + safeValue.length
                        + " hex=" + hexPreview(safeValue, MAX_LOGGED_BYTES));
            }
        });
    }

    private static byte[] frameCommand41MediaCount() {
        byte[] payload = MEDIA_COUNT_PAYLOAD.clone();
        int crc = crc16Modbus(payload);

        byte[] frame = new byte[payload.length + 6];
        frame[0] = (byte) 0xBC;
        frame[1] = 0x41;
        frame[2] = (byte) (payload.length & 0xFF);
        frame[3] = (byte) ((payload.length >>> 8) & 0xFF);
        frame[4] = (byte) (crc & 0xFF);
        frame[5] = (byte) ((crc >>> 8) & 0xFF);
        System.arraycopy(payload, 0, frame, 6, payload.length);

        if (!validateFrame(frame)) {
            throw new IllegalStateException("Internal media-count frame validation failed");
        }

        return frame;
    }

    private static int crc16Modbus(byte[] data) {
        int crc = 0xFFFF;

        for (byte value : data) {
            crc ^= value & 0xFF;
            for (int bit = 0; bit < 8; bit++) {
                if ((crc & 1) != 0) {
                    crc = (crc >>> 1) ^ 0xA001;
                } else {
                    crc >>>= 1;
                }
            }
        }

        return crc & 0xFFFF;
    }

    private static boolean validateFrame(byte[] frame) {
        if (frame == null || frame.length < 6) return false;
        if ((frame[0] & 0xFF) != 0xBC) return false;

        int length = (frame[2] & 0xFF) | ((frame[3] & 0xFF) << 8);
        if (frame.length != length + 6) return false;

        byte[] payload = new byte[length];
        System.arraycopy(frame, 6, payload, 0, length);

        int expected = (frame[4] & 0xFF) | ((frame[5] & 0xFF) << 8);
        return crc16Modbus(payload) == expected;
    }

    private static int framedCommand(byte[] frame) {
        if (frame == null || frame.length < 2 || (frame[0] & 0xFF) != 0xBC) {
            return -1;
        }
        return frame[1] & 0xFF;
    }

    private static String commandLabel(int command) {
        return command < 0
                ? "<unframed>"
                : String.format(Locale.US, "0x%02X", command);
    }

    private void scheduleConnectTimeout() {
        cancelConnectTimeout();
        connectTimeout = () -> {
            if (reportFinished || commandWriteAttempted) return;
            append("GATT TIMEOUT: G3 query was not started within 30 seconds.");
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

    private void cancelResponseTimeout() {
        if (responseTimeout != null) {
            mainHandler.removeCallbacks(responseTimeout);
            responseTimeout = null;
        }
    }

    private void cancelTimers() {
        cancelConnectTimeout();
        cancelResponseTimeout();
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
        writeCharacteristic = null;

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
                    ClipData.newPlainText("G3 Media Count Probe Report", report.toString()));
            Toast.makeText(this, "Report copied", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareReport() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "AIMB-G1 G3 Media Count Probe Report");
        send.putExtra(Intent.EXTRA_TEXT, report.toString());
        startActivity(Intent.createChooser(send, "Share G3 report"));
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

    private static String statusLabel(int status) {
        return status == BluetoothGatt.GATT_SUCCESS
                ? "SUCCESS"
                : Integer.toString(status);
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

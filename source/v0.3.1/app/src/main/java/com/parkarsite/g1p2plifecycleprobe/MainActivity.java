package com.parkarsite.g1p2plifecycleprobe;

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
 * K G1 P2P Lifecycle Probe v0.3.1
 *
 * G3B safety boundary:
 * - connect to exactly one bonded AIMB-G1-family device,
 * - subscribe to the physically confirmed Cyan response characteristic,
 * - send exact Cyan P2P-enter payload 02 01 04 01 once,
 * - observe BLE responses,
 * - send exact Cyan exit-transfer payload 02 01 09 once,
 * - observe BLE responses,
 * - disconnect.
 *
 * Deliberately absent:
 * - Android Wi-Fi/P2P/AP APIs,
 * - AP-mode payload 02 01 04 02,
 * - HTTP/network/media transfer,
 * - file operations,
 * - reset/restart/OTA,
 * - arbitrary command input,
 * - retry loops.
 */
public final class MainActivity extends Activity {
    private static final int REQ_PERMISSION = 3101;
    private static final long CONNECT_TIMEOUT_MS = 30_000L;
    private static final long ENTER_OBSERVATION_MS = 8_000L;
    private static final long EXIT_OBSERVATION_MS = 8_000L;
    private static final int MAX_LOGGED_NOTIFICATIONS = 32;
    private static final int MAX_LOGGED_BYTES = 120;
    private static final String TARGET_NAME = "AIMB-G1";

    private static final UUID CYAN_SERVICE =
            UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_NOTIFY =
            UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_WRITE =
            UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CLIENT_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final byte[] ENTER_P2P_PAYLOAD =
            new byte[]{0x02, 0x01, 0x04, 0x01};
    private static final byte[] EXIT_TRANSFER_PAYLOAD =
            new byte[]{0x02, 0x01, 0x09};

    private enum Phase {
        CONNECTING,
        ENTER_WRITE,
        ENTER_OBSERVE,
        EXIT_WRITE,
        EXIT_OBSERVE,
        COMPLETE
    }

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
    private Runnable phaseTimeout;

    private Phase phase = Phase.COMPLETE;
    private boolean reportFinished;
    private boolean enterWriteAttempted;
    private boolean exitWriteAttempted;
    private boolean enterWriteCallbackSuccess;
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
        title.setText("K G1 P2P Lifecycle Probe");
        title.setTextSize(24f);
        root.addView(title);

        TextView note = new TextView(this);
        note.setText(
                "G3B: enters Cyan P2P transfer mode once, observes BLE only, exits transfer mode once, observes, then disconnects. No phone Wi-Fi/P2P or file transfer.");
        note.setPadding(0, dp(8), 0, dp(16));
        root.addView(note);

        statusView = new TextView(this);
        statusView.setTextSize(16f);
        root.addView(statusView);

        runButton = new Button(this);
        runButton.setText("Run bounded P2P lifecycle");
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
                    ? "Ready. Force-stop Cyan Glasses, keep AIMB-G1 paired, then run G3B."
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

        runLifecycleProbe();
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
            runLifecycleProbe();
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
            runLifecycleProbe();
        } else {
            setStatus("Bluetooth/Nearby Devices permission is required.");
        }
    }

    private void runLifecycleProbe() {
        cancelTimers();
        disconnectGatt();

        report.setLength(0);
        reportFinished = false;
        enterWriteAttempted = false;
        exitWriteAttempted = false;
        enterWriteCallbackSuccess = false;
        notificationCount = 0;
        command41NotificationCount = 0;
        command73NotificationCount = 0;
        phase = Phase.CONNECTING;

        runButton.setEnabled(false);
        copyButton.setEnabled(false);
        shareButton.setEnabled(false);

        append("K G1 P2P LIFECYCLE PROBE REPORT");
        append("Generated: " + isoNow());
        append("App version: 0.3.1");
        append("Gate: G3B bounded P2P transfer-mode lifecycle");
        append("Allowed proprietary writes: 2 MAX");
        append("Write #1: 0x41 / 02 01 04 01 (enter P2P transfer mode)");
        append("Write #2: 0x41 / 02 01 09 (exit transfer mode)");
        append("AP mode: NOT IMPLEMENTED");
        append("Phone Wi-Fi/P2P/network: NOT IMPLEMENTED");
        append("HTTP/media transfer: NOT IMPLEMENTED");
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
                    closeAndFinish("Disconnected before G3B completed.");
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
            mainHandler.post(() -> sendEnterTransfer(callbackGatt));
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

            mainHandler.post(() -> {
                if (reportFinished) return;

                if (phase == Phase.ENTER_WRITE || phase == Phase.ENTER_OBSERVE) {
                    append("ENTER characteristic write status: " + statusLabel(status));
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        append("EXIT write: NOT ATTEMPTED because enter callback failed.");
                        disconnectGatt();
                        finishReport("P2P enter write callback failed.");
                        return;
                    }
                    enterWriteCallbackSuccess = true;
                } else if (phase == Phase.EXIT_WRITE || phase == Phase.EXIT_OBSERVE) {
                    append("EXIT characteristic write status: " + statusLabel(status));
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        disconnectGatt();
                        finishReport("Exit-transfer write callback failed.");
                    }
                } else {
                    append("GATT ERROR: characteristic-write callback in unexpected phase " + phase);
                    disconnectGatt();
                    finishReport("Unexpected write callback phase.");
                }
            });
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

    private void sendEnterTransfer(BluetoothGatt callbackGatt) {
        if (reportFinished || enterWriteAttempted || exitWriteAttempted) return;

        cancelConnectTimeout();

        append("");
        append("ENTER TRANSFER MODE");
        append("Command: 0x41");
        append("Payload: 02 01 04 01");
        append("Frame: BC 41 04 00 93 5C 02 01 04 01");
        append("Purpose: exact Cyan P2P transfer-mode entry");
        append("Phone-side P2P discovery: NONE");
        append("No retry policy: TRUE");

        enterWriteAttempted = true;
        phase = Phase.ENTER_WRITE;

        boolean started = writeAllowListedFrame(
                callbackGatt,
                frameCommand41(ENTER_P2P_PAYLOAD));

        append("ENTER write start: " + (started ? "SUCCESS" : "FAILED"));

        if (!started) {
            disconnectGatt();
            finishReport("Android did not start P2P enter write.");
            return;
        }

        phase = Phase.ENTER_OBSERVE;
        append("");
        append("ENTER OBSERVATION");
        append("Window: 8 seconds");
        setStatus("P2P enter sent once. Observing BLE responses…");

        schedulePhaseTimeout(() -> {
            if (!enterWriteCallbackSuccess) {
                append("EXIT write: NOT ATTEMPTED because successful enter callback was not observed.");
                disconnectGatt();
                finishReport("Enter-write callback was not confirmed.");
                return;
            }
            sendExitTransfer(callbackGatt);
        }, ENTER_OBSERVATION_MS);
    }

    private void sendExitTransfer(BluetoothGatt callbackGatt) {
        if (reportFinished || !enterWriteAttempted || exitWriteAttempted) return;

        cancelPhaseTimeout();

        append("");
        append("EXIT TRANSFER MODE");
        append("Command: 0x41");
        append("Payload: 02 01 09");
        append("Frame: BC 41 03 00 11 96 02 01 09");
        append("Purpose: exact Cyan fileDownloadComplete exit-transfer command");
        append("No retry policy: TRUE");

        exitWriteAttempted = true;
        phase = Phase.EXIT_WRITE;

        boolean started = writeAllowListedFrame(
                callbackGatt,
                frameCommand41(EXIT_TRANSFER_PAYLOAD));

        append("EXIT write start: " + (started ? "SUCCESS" : "FAILED"));

        if (!started) {
            disconnectGatt();
            finishReport("Android did not start exit-transfer write.");
            return;
        }

        phase = Phase.EXIT_OBSERVE;
        append("");
        append("EXIT OBSERVATION");
        append("Window: 8 seconds");
        setStatus("Exit-transfer sent once. Observing BLE responses…");

        schedulePhaseTimeout(() -> {
            append("");
            append("OBSERVATION SUMMARY");
            append("Notifications received: " + notificationCount);
            append("Command 0x41 notifications: " + command41NotificationCount);
            append("Command 0x73 notifications: " + command73NotificationCount);
            append("ENTER writes attempted: " + (enterWriteAttempted ? 1 : 0));
            append("EXIT writes attempted: " + (exitWriteAttempted ? 1 : 0));
            append("Total proprietary writes attempted: "
                    + ((enterWriteAttempted ? 1 : 0) + (exitWriteAttempted ? 1 : 0)));

            phase = Phase.COMPLETE;
            disconnectGatt();
            finishReport("G3B bounded transfer-mode lifecycle complete.");
        }, EXIT_OBSERVATION_MS);
    }

    @SuppressWarnings("deprecation")
    private boolean writeAllowListedFrame(
            BluetoothGatt callbackGatt,
            byte[] frame) {
        BluetoothGattCharacteristic writer = writeCharacteristic;
        if (writer == null || frame == null || !validateFrame(frame)) {
            return false;
        }

        writer.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        writer.setValue(frame);

        try {
            return callbackGatt.writeCharacteristic(writer);
        } catch (SecurityException e) {
            return false;
        } catch (RuntimeException e) {
            return false;
        }
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
                        + " phase=" + phase
                        + " cmd=" + commandLabel(command)
                        + " frame=" + validation
                        + " len=" + safeValue.length
                        + " hex=" + hexPreview(safeValue, MAX_LOGGED_BYTES));
            }
        });
    }

    private static byte[] frameCommand41(byte[] inputPayload) {
        byte[] payload = inputPayload.clone();
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
            throw new IllegalStateException("Internal command-0x41 frame validation failed");
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
            if (reportFinished || enterWriteAttempted) return;

            append("GATT TIMEOUT: G3B lifecycle did not begin within 30 seconds.");
            disconnectGatt();
            finishReport("GATT/subscription timeout.");
        };
        mainHandler.postDelayed(connectTimeout, CONNECT_TIMEOUT_MS);
    }

    private void schedulePhaseTimeout(Runnable action, long delayMs) {
        cancelPhaseTimeout();
        phaseTimeout = action;
        mainHandler.postDelayed(phaseTimeout, delayMs);
    }

    private void cancelConnectTimeout() {
        if (connectTimeout != null) {
            mainHandler.removeCallbacks(connectTimeout);
            connectTimeout = null;
        }
    }

    private void cancelPhaseTimeout() {
        if (phaseTimeout != null) {
            mainHandler.removeCallbacks(phaseTimeout);
            phaseTimeout = null;
        }
    }

    private void cancelTimers() {
        cancelConnectTimeout();
        cancelPhaseTimeout();
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
        phase = Phase.COMPLETE;

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
                    ClipData.newPlainText("G3B P2P Lifecycle Report", report.toString()));
            Toast.makeText(this, "Report copied", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareReport() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, "AIMB-G1 G3B P2P Lifecycle Report");
        send.putExtra(Intent.EXTRA_TEXT, report.toString());
        startActivity(Intent.createChooser(send, "Share G3B report"));
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

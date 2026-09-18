package com.parkarsite.g1capture.g1;

import android.content.Context;
import java.io.OutputStream;
import java.util.List;

/**
 * Intentionally incomplete until the real AIMB-G1 transfer trigger is observed.
 * Do not guess UUIDs, BLE payloads, SSID/passwords, IPs, ports or endpoints.
 */
public final class RealAimbG1Transport implements G1Transport {
    @SuppressWarnings("unused") private final Context context;

    public RealAimbG1Transport(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override public void connect() {
        throw new UnsupportedOperationException("Awaiting AIMB-G1 BLE trace");
    }

    @Override public void enterTransferMode() {
        throw new UnsupportedOperationException("Awaiting correlated BLE/Wi-Fi trace");
    }

    @Override public List<RemoteMedia> listMedia() {
        throw new UnsupportedOperationException("Awaiting G1 Wi-Fi protocol trace");
    }

    @Override public void download(RemoteMedia media, OutputStream destination) {
        throw new UnsupportedOperationException("Awaiting G1 Wi-Fi protocol trace");
    }

    @Override public void disconnect() { }
}

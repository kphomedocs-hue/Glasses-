package com.parkarsite.g1capture.g1;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FakeAimbG1Transport implements G1Transport {
    private final Map<String, byte[]> payloads = new HashMap<>();
    private boolean connected;

    @Override public void connect() { connected = true; }

    @Override public void enterTransferMode() {
        if (!connected) throw new IllegalStateException("Not connected");
    }

    @Override public List<RemoteMedia> listMedia() {
        if (!connected) throw new IllegalStateException("Not connected");
        long now = Instant.now().toEpochMilli();
        List<RemoteMedia> out = new ArrayList<>();
        add(out, "fake-001", "IMG_0001.JPG", now - 30_000, "fake-jpeg-1");
        add(out, "fake-002", "VID_0002.MP4", now - 20_000, "fake-video-2");
        add(out, "fake-003", "AUD_0003.M4A", now - 10_000, "fake-audio-3");
        return out;
    }

    private void add(List<RemoteMedia> out, String id, String name, long time, String body) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        payloads.put(id, bytes);
        out.add(new RemoteMedia(id, name, time, bytes.length));
    }

    @Override public void download(RemoteMedia media, OutputStream destination) throws Exception {
        byte[] data = payloads.get(media.remoteId());
        if (data == null) throw new IllegalArgumentException("Unknown fake media: " + media.remoteId());
        destination.write(data);
    }

    @Override public void disconnect() { connected = false; }
}

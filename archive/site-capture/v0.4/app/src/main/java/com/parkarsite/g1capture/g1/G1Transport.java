package com.parkarsite.g1capture.g1;

import java.io.OutputStream;
import java.util.List;

/**
 * Hardware boundary. Everything AIMB-G1 specific stays behind this interface.
 * Downloads stream directly to disk so large videos are never buffered in RAM.
 */
public interface G1Transport {
    void connect() throws Exception;
    void enterTransferMode() throws Exception;
    List<RemoteMedia> listMedia() throws Exception;
    void download(RemoteMedia media, OutputStream destination) throws Exception;
    void disconnect();
}

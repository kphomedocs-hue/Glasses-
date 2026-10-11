package com.parkarsite.g1.virtual;

import java.util.*;

/** Deterministic, key-scoped event clock. Deadlines cannot cancel other keys. */
public final class VirtualClock {
    private record Event(String key, long at, long sequence, Runnable callback) {}
    private final Map<String, Event> pending = new HashMap<>();
    private long now, sequence;
    public long now() { return now; }
    public void deadline(String key, long afterMs, Runnable callback) {
        if (afterMs < 0 || callback == null) throw new IllegalArgumentException("Invalid deadline");
        pending.put(key, new Event(key, Math.addExact(now, afterMs), sequence++, callback));
    }
    public void cancel(String key) { pending.remove(key); }
    public boolean scheduled(String key) { return pending.containsKey(key); }
    public void advance(long targetMs) {
        if (targetMs < now) throw new IllegalArgumentException("Clock cannot rewind");
        while (true) {
            Event first = null;
            for (Event e : pending.values())
                if (e.at <= targetMs && (first == null || e.at < first.at ||
                        (e.at == first.at && e.sequence < first.sequence))) first = e;
            if (first == null) break;
            pending.remove(first.key);
            now = first.at;
            first.callback.run();
        }
        now = targetMs;
    }
}

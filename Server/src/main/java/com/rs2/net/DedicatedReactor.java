package com.rs2.net;

import com.rs2.Server;
import com.rs2.model.player.Player;
import com.rs2.net.packet.PacketDispatcher;
import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public final class DedicatedReactor
extends Thread {
    private static DedicatedReactor instance;
    private final Selector selector;
    private static final long DIAGNOSTIC_POLL_INTERVAL_MS = 20L;
    private static final long RECENT_FPS_PING_NANOS = 10_000_000_000L;
    // Do not scan every connected player on each diagnostic poll.
    private final java.util.concurrent.ConcurrentHashMap<Player, Long> recentlyPinging =
            new java.util.concurrent.ConcurrentHashMap<Player, Long>();

    // Track selector progress independently of individual packet processing.
    private final java.util.concurrent.atomic.AtomicLong longestReactorGapNanos =
            new java.util.concurrent.atomic.AtomicLong();
    // A slow handler blocks the entire selector, delaying unrelated pings.
    private final java.util.concurrent.atomic.AtomicLong longestReadDispatchNanos =
            new java.util.concurrent.atomic.AtomicLong();

    public DedicatedReactor(Selector selector) {
        this.selector = selector;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    @Override
    public final void run() {
        Thread.currentThread().setName("DedicatedReactor");
        long previousSelectReturnNanos = 0L;
        while (!Thread.currentThread().isInterrupted() && this.selector.isOpen()) {
            try {
                this.selector.select(DIAGNOSTIC_POLL_INTERVAL_MS);
                long now = System.nanoTime();
                if (previousSelectReturnNanos != 0L) {
                    long gap = Math.max(0L, now - previousSelectReturnNanos);
                    longestReactorGapNanos.accumulateAndGet(gap, Math::max);
                }
                previousSelectReturnNanos = now;
                if (Thread.currentThread().isInterrupted() || !this.selector.isOpen()) {
                    break;
                }

                Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    if (!selectionKey.isValid()) {
                        continue;
                    }
                    if (selectionKey.isAcceptable()) {
                        Server.acceptConnection(selectionKey);
                        continue;
                    }

                    Player player = (Player)selectionKey.attachment();
                    if (selectionKey.isReadable()) {
                        long dispatchStart = System.nanoTime();
                        try {
                            PacketDispatcher.processIncoming(player);
                        } finally {
                            long spent = Math.max(0L, System.nanoTime() - dispatchStart);
                            longestReadDispatchNanos.accumulateAndGet(spent, Math::max);
                        }
                    }
                    if (selectionKey.isValid() && selectionKey.isWritable()) {
                        PacketDispatcher.flushOutgoing(player);
                    }
                }
                // Diagnostic fallback: on some platforms the registered OP_READ
                // event appears to be delayed even though the client wrote the
                // packet promptly. Poll recently pinging connections only, on
                // this same reactor thread, to avoid concurrent buffer reads.
                pollRecentFpsConnections(now);
            }
            catch (ClosedSelectorException closedSelectorException) {
                break;
            }
            catch (IOException iOException) {
                if (this.selector.isOpen() && !Thread.currentThread().isInterrupted()) {
                    iOException.printStackTrace();
                }
                break;
            }
        }
    }

    /** Only actively using ::fps connections are eligible for read polling. */
    public void noteFpsPing(Player player) {
        recentlyPinging.put(player, System.nanoTime());
    }

    private void pollRecentFpsConnections(long now) {
        for (java.util.Map.Entry<Player, Long> entry : recentlyPinging.entrySet()) {
            Player player = entry.getKey();
            long lastPing = entry.getValue();
            if (now - lastPing < 0L || now - lastPing > RECENT_FPS_PING_NANOS
                    || !player.getSocketChannel().isOpen()
                    || !player.getSelectionKey().isValid()) {
                recentlyPinging.remove(player, lastPing);
                continue;
            }
            // SocketChannels are non-blocking. processIncoming returns
            // immediately when no bytes are currently available.
            PacketDispatcher.processIncoming(player);
        }
    }

    /**
     * Snapshot the slowest recent socket-read/packet-dispatch operation.
     * Called by the ping handler on the same selector thread.
     */
    /** Longest observed interval between selector returns since the last ping. */
    public int takeLongestReactorGapMillis() {
        long nanos = longestReactorGapNanos.getAndSet(0L);
        return (int) Math.min(Integer.MAX_VALUE, (nanos + 500_000L) / 1_000_000L);
    }

    public int takeLongestReadDispatchMillis() {
        long nanos = longestReadDispatchNanos.getAndSet(0L);
        return (int) Math.min(Integer.MAX_VALUE, (nanos + 500_000L) / 1_000_000L);
    }

    public final Selector getSelector() {
        return this.selector;
    }

    public static DedicatedReactor getInstance() {
        return instance;
    }

    public static synchronized void shutdown() {
        DedicatedReactor dedicatedReactor = instance;
        if (dedicatedReactor == null) {
            return;
        }

        dedicatedReactor.interrupt();
        try {
            dedicatedReactor.selector.wakeup();
            for (SelectionKey selectionKey : dedicatedReactor.selector.keys()) {
                try {
                    selectionKey.channel().close();
                }
                catch (IOException ignored) {
                }
            }
            dedicatedReactor.selector.close();
        }
        catch (Exception exception) {
            if (dedicatedReactor.selector.isOpen()) {
                exception.printStackTrace();
            }
        }
        instance = null;
    }

    public static synchronized void setInstance(DedicatedReactor dedicatedReactor) {
        if (instance != null) {
            throw new IllegalStateException("Instance already set");
        }
        instance = dedicatedReactor;
    }
}

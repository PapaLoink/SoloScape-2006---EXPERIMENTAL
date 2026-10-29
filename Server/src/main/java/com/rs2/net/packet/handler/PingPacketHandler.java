package com.rs2.net.packet.handler;

import com.rs2.model.player.Player;
import com.rs2.net.DedicatedReactor;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.PacketHandler;
import com.rs2.net.packet.PacketWriter;

/** Echoes an FPS-overlay ping ID, so the client can measure real server RTT. */
public final class PingPacketHandler implements PacketHandler {
    private static final int PING_OPCODE = 254;

    @Override
    public void handle(Player player, IncomingPacket packet) {
        if (packet.getLength() != 12 || player.isBot) {
            return;
        }
        int pingId = packet.getReader().readInt();
        packet.getReader().readLong(); // Client epoch; echoed timing is derived on the client.
        long receivedEpochMillis = System.currentTimeMillis();
        long receivedNanos = System.nanoTime();
        long previousNanos = player.lastFpsPingReceivedNanos;
        player.lastFpsPingReceivedNanos = receivedNanos;
        DedicatedReactor reactor = DedicatedReactor.getInstance();
        if (reactor != null) {
            reactor.noteFpsPing(player);
        }
        int receiveGapMs = previousNanos == 0L ? -1
                : nanosToMillis(Math.max(0L, receivedNanos - previousNanos));

        // Include the time waiting for the player lock: a zero outbound queue
        // by itself cannot rule out server-side lock contention.
        long waitingSince = System.nanoTime();
        int queuedBytes;
        synchronized (player) {
            queuedBytes = player.getOutboundBuffer().position();
        }
        int playerLockWaitMs = nanosToMillis(System.nanoTime() - waitingSince);
        // Capture time spent assembling a potentially split ping packet,
        // rather than merely the time since the *last* socket read.
        int readToHandlerMs = player.fpsPingFirstByteNanos == 0L ? -1
                : nanosToMillis(Math.max(0L, receivedNanos - player.fpsPingFirstByteNanos));
        player.fpsPingFirstByteNanos = 0L;
        int longestReadDispatchMs = reactor == null ? -1 : reactor.takeLongestReadDispatchMillis();
        int longestReactorGapMs = reactor == null ? -1 : reactor.takeLongestReactorGapMillis();
        PacketWriter reply = PacketBuffer.allocateWriter(45);
        reply.writeOpcode(player.getOutboundCipher(), PING_OPCODE);
        reply.writeInt(pingId);
        reply.writeInt(queuedBytes);
        reply.writeInt(playerLockWaitMs);
        reply.writeInt(receiveGapMs);
        reply.writeLong(receivedEpochMillis);
        // Both server stamps are from the same clock. The client only uses
        // cross-machine one-way estimates if it connects over loopback.
        reply.writeLong(System.currentTimeMillis());
        reply.writeInt(readToHandlerMs);
        reply.writeInt(longestReadDispatchMs);
        reply.writeInt(longestReactorGapMs);
        player.writePacketBuffer(reply.getBuffer());
    }

    private static int nanosToMillis(long nanos) {
        return (int) Math.min(Integer.MAX_VALUE, (nanos + 500_000L) / 1_000_000L);
    }
}

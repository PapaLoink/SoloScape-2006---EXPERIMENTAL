package com.rs2.net.packet;

import com.rs2.net.IsaacCipher;
import com.rs2.model.player.Player;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Reproduce concurrent packet construction where network enqueue order differs
 * from construction order. The client's ISAAC stream must match wire order.
 */
public final class OutboundCipherOrderingChecks {
    private OutboundCipherOrderingChecks() {
    }

    public static void main(String[] args) {
        reverseSubmissionOrder();
        abandonedPacketDoesNotAdvanceCipher();
        rawLoginResponseDoesNotAdvanceCipher();
        finalizationIsIdempotent();
        variablePacketLengthIsPreserved();
        initial443RegionPrecedesCastleWarsReconnectPackets();
        System.out.println("Revision 443 outbound ISAAC ordering checks passed");
        System.exit(0);
    }

    private static void reverseSubmissionOrder() {
        IsaacCipher server = cipher();
        IsaacCipher client = cipher();
        PacketWriter first = PacketBuffer.allocateWriter(4);
        first.writeOpcode(server, 104);
        first.writeByte(77);
        ByteBuffer firstPacket = first.getBuffer();

        PacketWriter second = PacketBuffer.allocateWriter(4);
        second.writeOpcode(server, 154);
        second.writeByte(88);
        ByteBuffer secondPacket = second.getBuffer();

        // Thread B can acquire the player's network lock before thread A,
        // despite having constructed its packet second.
        PacketWriter.finalizeOutboundOpcodes(secondPacket);
        PacketWriter.finalizeOutboundOpcodes(firstPacket);

        require(opcode(secondPacket, client) == 154,
                "Second-built packet sent first must use first ISAAC value");
        require((secondPacket.get() & 255) == 88, "Second packet payload changed");
        require(opcode(firstPacket, client) == 104,
                "First-built packet sent second must use second ISAAC value");
        require((firstPacket.get() & 255) == 77, "First packet payload changed");
    }

    private static void abandonedPacketDoesNotAdvanceCipher() {
        IsaacCipher server = cipher();
        IsaacCipher client = cipher();
        PacketWriter discarded = PacketBuffer.allocateWriter(1);
        discarded.writeOpcode(server, 27);
        ByteBuffer neverSent = discarded.getBuffer();
        PacketWriter.discardOutboundOpcodes(neverSent);

        PacketWriter sent = PacketBuffer.allocateWriter(1);
        sent.writeOpcode(server, 32);
        ByteBuffer buffer = sent.getBuffer();
        PacketWriter.finalizeOutboundOpcodes(buffer);
        require(opcode(buffer, client) == 32,
                "Unsented packet must not consume an ISAAC value");
    }

    private static void rawLoginResponseDoesNotAdvanceCipher() {
        IsaacCipher server = cipher();
        IsaacCipher client = cipher();
        ByteBuffer login = ByteBuffer.allocate(6);
        login.put((byte) 2).put((byte) 0).put((byte) 0);
        PacketWriter.finalizeOutboundOpcodes(login);
        login.flip();
        require(login.get() == 2, "Raw login response was encrypted");

        PacketWriter sent = PacketBuffer.allocateWriter(1);
        sent.writeOpcode(server, 91);
        ByteBuffer packet = sent.getBuffer();
        PacketWriter.finalizeOutboundOpcodes(packet);
        require(opcode(packet, client) == 91,
                "Login response must not advance the game packet cipher");
    }

    private static void finalizationIsIdempotent() {
        IsaacCipher server = cipher();
        IsaacCipher client = cipher();
        PacketWriter sent = PacketBuffer.allocateWriter(1);
        sent.writeOpcode(server, 154);
        ByteBuffer packet = sent.getBuffer();
        PacketWriter.finalizeOutboundOpcodes(packet);
        PacketWriter.finalizeOutboundOpcodes(packet);
        require(opcode(packet, client) == 154,
                "A repeated finalize must not consume another ISAAC value");
    }

    private static void variablePacketLengthIsPreserved() {
        IsaacCipher server = cipher();
        IsaacCipher client = cipher();
        PacketWriter sent = PacketBuffer.allocateWriter(3);
        sent.startVariableBytePacket(server, 25);
        sent.writeByte(7);
        sent.writeByte(8);
        sent.finishVariableBytePacket();
        ByteBuffer packet = sent.getBuffer();
        PacketWriter.finalizeOutboundOpcodes(packet);
        require(opcode(packet, client) == 25, "Variable packet opcode corrupted");
        require((packet.get() & 255) == 2, "Variable packet length corrupted");
        require((packet.get() & 255) == 7 && (packet.get() & 255) == 8,
                "Variable packet payload corrupted");
    }

    /**
     * Castle Wars reconnect may queue an inventory/notice packet while the
     * client's native 443 login reader is still waiting for the first region
     * frame. The region must reach the wire first, with ISAAC still in order.
     */
    private static void initial443RegionPrecedesCastleWarsReconnectPackets() {
        try (ServerSocketChannel listener = ServerSocketChannel.open();
             Selector selector = Selector.open()) {
            listener.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            try (SocketChannel receiver = SocketChannel.open(listener.getLocalAddress());
                 SocketChannel sender = listener.accept()) {
                sender.configureBlocking(false);
                SelectionKey key = sender.register(selector, SelectionKey.OP_READ);
                Player player = new Player(key);
                player.setOutboundCipher(cipher());
                player.beginInitial443Region();

                // This frame is generated by Castle Wars reconnect before the
                // map is rebuilt; it must NOT reach the client first.
                PacketWriter early = PacketBuffer.allocateWriter(2);
                early.writeOpcode(player.getOutboundCipher(), 62);
                early.writeByte(7);
                player.writePacketBuffer(early.getBuffer());

                PacketWriter region = PacketBuffer.allocateWriter(4);
                region.startVariableShortPacket(player.getOutboundCipher(), 121);
                region.writeByte(0x5a);
                region.finishVariableShortPacket();
                player.writeInitial443Region(region.getBuffer());

                receiver.configureBlocking(false);
                ByteBuffer received = ByteBuffer.allocate(6);
                long deadline = System.currentTimeMillis() + 2000L;
                while (received.hasRemaining() && System.currentTimeMillis() < deadline) {
                    int count = receiver.read(received);
                    if (count < 0) break;
                    if (count == 0) Thread.yield();
                }
                require(!received.hasRemaining(),
                        "Initial 443 region or deferred Castle Wars frame was not sent");
                received.flip();
                IsaacCipher client = cipher();
                require(((received.get() & 255) - client.nextInt() & 255) == 121,
                        "First ciphered 443 packet must be the region rebuild (121)");
                require((received.get() & 255) == 0
                        && (received.get() & 255) == 1
                        && (received.get() & 255) == 0x5a,
                        "Initial region packet must preserve its variable-short framing");
                require(((received.get() & 255) - client.nextInt() & 255) == 62
                        && (received.get() & 255) == 7,
                        "Deferred Castle Wars packet must follow region with ISAAC in order");
            }
        } catch (IOException ex) {
            throw new AssertionError("Unable to test initial 443 region socket ordering", ex);
        }
    }

    private static int opcode(ByteBuffer buffer, IsaacCipher client) {
        buffer.flip();
        return ((buffer.get() & 255) - client.nextInt()) & 255;
    }

    private static IsaacCipher cipher() {
        return new IsaacCipher(new int[]{1, 2, 3, 4});
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

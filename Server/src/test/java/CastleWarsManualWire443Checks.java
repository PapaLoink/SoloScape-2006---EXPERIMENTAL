import com.rs2.ServerSettings;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.player.Player;
import com.rs2.net.IsaacCipher;
import com.rs2.net.packet.ClientPackets;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.handler.InterfaceActionPacketHandler;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Real socket packets: after a 443 book-arrow click, the server must resend
 * opcode 160 to release the native client's pending-widget lock. Also verify
 * that headings are plain text: 443 does not interpret 377 or HTML tags.
 */
public final class CastleWarsManualWire443Checks {
    private static final int PACKED_FIRST_LINE = (49 << 16) | 87;

    private CastleWarsManualWire443Checks() {}

    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        try (ServerSocketChannel listener = ServerSocketChannel.open();
                Selector selector = Selector.open()) {
            listener.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            try (SocketChannel receiver = SocketChannel.open(listener.getLocalAddress());
                    SocketChannel sender = listener.accept()) {
                sender.configureBlocking(false);
                SelectionKey key = sender.register(selector, SelectionKey.OP_READ);
                Player player = new Player(key);
                player.setOutboundCipher(new IsaacCipher(new int[]{1, 2, 3, 4}));
                // Use the real packet sender; isBot=true would skip the network
                // frames, concealing the original book-navigation regression.
                CastleWarsManager.openCastleWarsManual(player);
                require(player.activeBookPageIndex == 0, "Book must start at first spread");
                nativeWidgetSelect(player, 85);
                require(player.activeBookPageIndex == 1, "Next arrow did not select second spread");
                nativeWidgetSelect(player, 83);
                require(player.activeBookPageIndex == 0, "Previous arrow did not select first spread");
                nativeWidgetSelect(player, 112);
                require(player.activeBookItemId == 0, "Native close button did not dismiss book");

                receiver.configureBlocking(false);
                ByteBuffer received = ByteBuffer.allocate(16384);
                Parsed parsed = null;
                long deadline = System.currentTimeMillis() + 2500L;
                while (System.currentTimeMillis() < deadline) {
                    int count = receiver.read(received);
                    if (count < 0) break;
                    Parsed candidate = parse(Arrays.copyOf(received.array(), received.position()));
                    if (candidate != null && candidate.openCount == 3
                            && candidate.textCount == 75 && candidate.colourCount == 6
                            && candidate.closeCount == 1) {
                        parsed = candidate;
                        break;
                    }
                    if (count == 0) Thread.yield();
                }
                require(parsed != null,
                        "Book open/Next/Previous must send 3 interface-open frames, 75 text updates, 6 heading colours, and close");
                require(parsed.firstLineHeadings.size() == 3,
                        "Book must update the first line of every spread");
                require("Objective".equals(parsed.firstLineHeadings.get(0))
                        && "Bandages".equals(parsed.firstLineHeadings.get(1))
                        && "Objective".equals(parsed.firstLineHeadings.get(2)),
                        "Native 443 book headings must be plain Objective/Bandages/Objective, not raw colour markup: "
                                + parsed.firstLineHeadings);
            }
        }
        System.out.println("Castle Wars 443 book navigation reset and plain text wire checks passed");
    }

    private static Parsed parse(byte[] stream) {
        ByteBuffer wire = ByteBuffer.wrap(stream);
        IsaacCipher cipher = new IsaacCipher(new int[]{1, 2, 3, 4});
        Parsed parsed = new Parsed();
        while (wire.hasRemaining()) {
            int opcode = ((wire.get() & 255) - cipher.nextInt()) & 255;
            if (opcode == 160) {
                if (wire.remaining() < 2) return null;
                wire.position(wire.position() + 2);
                parsed.openCount++;
            } else if (opcode == 180) {
                if (wire.remaining() < 2) return null;
                int length = ((wire.get() & 255) << 8) | (wire.get() & 255);
                if (wire.remaining() < length) return null;
                int payloadStart = wire.position();
                if (length >= 5) {
                    int packed = wire.order(ByteOrder.LITTLE_ENDIAN).getInt();
                    wire.order(ByteOrder.BIG_ENDIAN);
                    if (packed == PACKED_FIRST_LINE) {
                        byte[] chars = new byte[length - 5];
                        wire.get(chars);
                        parsed.firstLineHeadings.add(new String(chars, StandardCharsets.ISO_8859_1));
                    }
                }
                wire.position(payloadStart + length);
                parsed.textCount++;
            } else if (opcode == 3) {
                // Native 443 widget-text-colour packet: packed component ID,
                // then 16-bit RGB555. Both page headings get their own colour
                // packet instead of rendering a literal HTML/377 colour tag.
                if (wire.remaining() < 6) return null;
                wire.position(wire.position() + 4);
                int colour = wire.order(ByteOrder.LITTLE_ENDIAN).getShort() & 65535;
                wire.order(ByteOrder.BIG_ENDIAN);
                require(colour == 0x4000, "Castle Wars manual section headings must use dark red");
                parsed.colourCount++;
            } else if (opcode == 178) {
                parsed.closeCount++;
            } else {
                throw new AssertionError("Unexpected outgoing 443 book opcode " + opcode);
            }
        }
        return parsed;
    }

    private static void nativeWidgetSelect(Player player, int child) {
        ByteBuffer payload = ByteBuffer.allocate(6);
        payload.order(ByteOrder.LITTLE_ENDIAN).putShort((short) 0);
        payload.order(ByteOrder.BIG_ENDIAN).putInt((49 << 16) | child);
        payload.flip();
        new InterfaceActionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.WIDGET_SELECT, 6, PacketBuffer.wrapReader(payload)));
    }

    private static void require(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }

    private static final class Parsed {
        int openCount;
        int textCount;
        int colourCount;
        int closeCount;
        final List<String> firstLineHeadings = new ArrayList<String>();
    }
}

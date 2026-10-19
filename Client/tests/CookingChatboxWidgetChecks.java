package unpackaged;

import jagex.io.JSocket;
import jagex.io.FrameBuffer;
import jagex.utils.Huffmans;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/** Native cache 307 has no food-name text field; client must add child 8. */
public final class CookingChatboxWidgetChecks {
    public static void main(String[] args) throws Exception {
        Class39_Sub5_Sub4.widgetsLoaded = new boolean[500];
        Class62_Sub1.widgets = new Widget[500][];

        Widget[] cached = new Widget[8];
        for (int child = 0; child < cached.length; child++) {
            cached[child] = new Widget();
            cached[child].anInt2084 = 307 << 16 | child;
            cached[child].anInt2050 = -1;
            cached[child].type = child < 2 ? 5 : child == 2 ? 6 : 4;
        }
        cached[7].aClass3_2029 = Class39_Sub5_Sub9.createJstring(
                "How many would you like to cook?");
        cached[7].anInt2105 = 496;
        Class62_Sub1.widgets[307] = cached;
        Class39_Sub5_Sub4.widgetsLoaded[307] = true;

        require(JSocket.loadWidget(307), "Unable to load native cooking interface");
        Widget[] actual = Class62_Sub1.widgets[307];
        require(actual.length == 9 && actual[8] != null, "Food label not created");
        Widget name = actual[8];
        require(name.type == 4, "Food label is not a text widget");
        require(name.anInt2084 == (307 << 16 | 8), "Food label id incorrect");
        require(name.anInt2050 == -1, "Food label is not attached to chatbox root");
        require(name.anInt2021 == 96 && name.quadHeight == 17,
                "Food label is not below the item model");
        require(name.anInt2032 == 1 && name.activeQuadColor == 0,
                "Food label not centered in black");
        for (int child = 0; child < 8; child++) {
            require(actual[child] == cached[child],
                    "Native cooking child " + child + " was overwritten");
        }
        require(Class37.getWidget(307 << 16 | 8) == name,
                "The native text-update path cannot address 307:8");

        // Deliver the actual revision-443 dynamic widget text packet,
        // not just an assignment to the test widget.
        readFoodNamePacket("Raw chicken");
        require(name.aClass3_2029.method97(
                        Class39_Sub5_Sub9.createJstring("Raw chicken"), -66),
                "Opcode 180 did not paint Raw chicken into the new text child");

        // A second interface open must preserve the food-name widget and text.
        require(JSocket.loadWidget(307), "Cooking chatbox could not be reopened");
        require(Class62_Sub1.widgets[307][8] == name
                && name.aClass3_2029.method97(
                        Class39_Sub5_Sub9.createJstring("Raw chicken"), -66),
                "Reopening cooking reset the food name");

        System.out.println("Cooking chatbox widget checks passed.");
    }

    private static void readFoodNamePacket(String value) throws Exception {
        byte[] text = value.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        byte[] payload = new byte[4 + text.length + 1];
        int widget = 307 << 16 | 8;
        payload[0] = (byte) widget;
        payload[1] = (byte) (widget >>> 8);
        payload[2] = (byte) (widget >>> 16);
        payload[3] = (byte) (widget >>> 24);
        System.arraycopy(text, 0, payload, 4, text.length);

        Class39_Sub5_Sub11.gameBuffer = new FrameBuffer(64);
        Class37.gameSocket = new JSocket(new Socket() {
            public void setSoTimeout(int timeout) {}
            public void setTcpNoDelay(boolean enabled) {}
            public InputStream getInputStream() {
                return new ByteArrayInputStream(payload);
            }
            public OutputStream getOutputStream() {
                return new ByteArrayOutputStream();
            }
        }, null);
        Class4.frameId = 180;
        Huffmans.frameSize = payload.length;
        require(Bzip2Block.readFrame(), "Opcode 180 was not handled");
        require(Class4.frameId == -1, "Opcode 180 was not fully consumed");
    }

    private static void require(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }
}

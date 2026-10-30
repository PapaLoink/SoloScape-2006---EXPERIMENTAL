package unpackaged;

import jagex.io.JSocket;
import jagex.io.FrameBuffer;
import jagex.utils.Huffmans;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/** Regression: product captions stay inside the native 443 chatbox clip. */
public final class FletchingChatboxWidgetChecks {
    public static void main(String[] args) throws Exception {
        Class39_Sub5_Sub4.widgetsLoaded = new boolean[500];
        Class62_Sub1.widgets = new Widget[500][];

        check(309, 8, 1, "Unstrung comp bow");
        check(303, 13, 2, "Oak shortbow");
        check(304, 18, 3, "Arrow shafts");
        System.out.println("Fletching chatbox widget checks passed.");
    }

    private static void check(int group, int firstLabel, int choices, String sample)
            throws Exception {
        Widget[] nativeWidgets = new Widget[firstLabel];
        for (int child = 0; child < firstLabel; child++) {
            Widget widget = new Widget();
            widget.anInt2084 = group << 16 | child;
            widget.anInt2050 = -1;
            widget.type = child < 2 ? 5 : child < 2 + choices ? 6 : 4;
            nativeWidgets[child] = widget;
        }
        Widget title = nativeWidgets[firstLabel - 1];
        title.aClass3_2029 = Class39_Sub5_Sub9.createJstring("What would you like to make?");
        title.anInt2105 = 496;
        Class62_Sub1.widgets[group] = nativeWidgets;
        Class39_Sub5_Sub4.widgetsLoaded[group] = true;
        require(JSocket.loadWidget(group), "Native chatbox failed to load");
        Widget[] augmented = Class62_Sub1.widgets[group];
        require(augmented.length == firstLabel + choices, "Wrong number of item captions");

        for (int child = 0; child < firstLabel; child++) {
            require(augmented[child] == nativeWidgets[child],
                    "Overwrote a native item model or Make-X option");
        }
        for (int slot = 0; slot < choices; slot++) {
            int id = group << 16 | firstLabel + slot;
            Widget caption = augmented[firstLabel + slot];
            require(caption != null && caption.type == 4 && caption.anInt2084 == id,
                    "Incorrect caption widget ID");
            require(caption.anInt2050 == -1 && caption.anInt2032 == 1,
                    "Caption is not a top-level centered chatbox label");
            require(caption.anInt2021 == 78 && caption.anInt2021 + caption.quadHeight <= 96,
                    "Item caption is clipped outside the chatbox");
            require(Class37.getWidget(id) == caption,
                    "Native widget text update cannot find caption");
        }

        readNamePacket(group, firstLabel, sample);
        Widget caption = augmented[firstLabel];
        require(caption.aClass3_2029.method97(Class39_Sub5_Sub9.createJstring(sample), -66),
                "Revision-443 text packet did not update item caption");
        require(JSocket.loadWidget(group), "Chatbox failed reopening");
        require(Class62_Sub1.widgets[group][firstLabel] == caption,
                "Reopening replaced the caption widget");
    }

    private static void readNamePacket(int group, int child, String value) throws Exception {
        byte[] text = value.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        byte[] payload = new byte[4 + text.length + 1];
        int widget = group << 16 | child;
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

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

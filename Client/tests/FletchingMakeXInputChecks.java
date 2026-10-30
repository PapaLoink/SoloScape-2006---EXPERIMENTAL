package unpackaged;

import jagex.io.FrameBuffer;
import jagex.utils.IsaacPrng;

/** A native Make-X click must open the number input in the client immediately. */
public final class FletchingMakeXInputChecks {
    public static void main(String[] args) {
        int[][] nativeButtons = {
            {304, 5, 6, 9, 10, 13, 14},
            {303, 4, 5, 8, 9},
            {309, 4, 3, 5, 6}
        };
        for (int[] buttons : nativeButtons) {
            int group = buttons[0];
            for (int i = 1; i < buttons.length; i++) {
                int child = buttons[i];
                boolean isMakeX = group == 304 ? child == 5 || child == 9 || child == 13
                        : group == 303 ? child == 4 || child == 8 : child == 4;
                int packed = group << 16 | child;
                require(Bzip2Block.isFletchingMakeX(packed, group) == isMakeX,
                        "Wrong native Make-X selection: " + group + ":" + child);
                require(!Bzip2Block.isFletchingMakeX(packed, group + 1),
                        "Make-X triggered on another open chatbox");

                FrameBuffer.outgoingGameBuffer = new FrameBuffer(64);
                FrameBuffer.outgoingGameBuffer.isaacCipher = new IsaacPrng(new int[]{1, 2, 3, 4});
                Class39_Sub5_Sub14.anInt1912 = group;
                Class39_Sub5_Sub4_Sub4.anInt2285 = 0;
                Class39_Sub12.aBoolean1489 = true;
                Class66.aClass3_1151 = Class39_Sub5_Sub9.createJstring("123");
                Bzip2Block.method1035((byte)4, 0, packed);
                require(FrameBuffer.outgoingGameBuffer.offset == 7,
                        "Native WIDGET_SELECT packet was not sent");
                require(Class39_Sub5_Sub4_Sub4.anInt2285 == (isMakeX ? 1 : 0),
                        "Make-X did not enter native numeric input mode");
                if (isMakeX) {
                    require(!Class39_Sub12.aBoolean1489,
                            "Numeric input obscured by an existing prompt");
                    require(Class66.aClass3_1151.getLength() == 0,
                            "Numeric input was not reset");
                }
            }
        }

        // Selecting an off-screen widget from another fletching group must
        // never open a quantity dialogue.
        Class39_Sub5_Sub14.anInt1912 = 304;
        Class39_Sub5_Sub4_Sub4.anInt2285 = 0;
        Bzip2Block.method1035((byte)4, 0, (303 << 16) | 4);
        require(Class39_Sub5_Sub4_Sub4.anInt2285 == 0,
                "Hidden/off-screen Make-X action opened an input");

        System.out.println("Fletching Make-X client checks passed: native numeric "
                + "input on every product and all three layouts.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

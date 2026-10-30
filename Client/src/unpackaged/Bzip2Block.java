package unpackaged;

import jagex.graphics.BitmapFont;
import jagex.io.BufferedFile;
import jagex.utils.HashTable;
import jagex.utils.Huffmans;
import jagex.world.actors.GroundItem;
import jagex.graphics.sprites.IndexedColorSprite;
import jagex.world.actors.StillGraphic;
import jagex.world.actors.Projectile;
import jagex.utils.SubNode;
import jagex.utils.Node;
import jagex.utils.IsaacPrng;
import jagex.utils.JString;
import jagex.world.actors.Player;
import jagex.world.map.TraversalMap;
import jagex.utils.Deque;
import jagex.io.FrameBuffer;
import jagex.io.JSocket;
import jagex.io.Buffer;
import jagex.utils.Cache;

/* Class60 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Bzip2Block {

    public byte[] aByteArray1038;
    public int anInt1039;
    public byte[] aByteArray1040 = new byte[256];
    public static JString aClass3_1041 = (Class39_Sub5_Sub9.createJstring("No matching objects found)1 please shorten search"));
    public byte[] aByteArray1042;
    public int anInt1043;
    public int[] anIntArray1044;
    public int anInt1045;
    public byte[][] aByteArrayArray1046 = new byte[6][258];
    public int anInt1047;
    public boolean[] aBooleanArray1048 = new boolean[16];
    public static JString aClass3_1049;
    public static JString aClass3_1050;
    public static int anInt1051;
    public int anInt1052;
    public int anInt1053;
    public static int anInt1054;
    public int[] anIntArray1055;
    public static JString aClass3_1056;
    public byte[] aByteArray1057;
    public static JString aClass3_1058 = Class39_Sub5_Sub9.createJstring("Trade)4compete");
    public byte[] aByteArray1059;
    public int anInt1060;
    public int anInt1061;
    public byte[] aByteArray1062;
    public static long aLong1063;
    public boolean[] aBooleanArray1064;
    public int[] anIntArray1065;
    public int anInt1066;
    public int[][] anIntArrayArray1067;
    public int anInt1068;
    public int[][] anIntArrayArray1069;
    public int[][] anIntArrayArray1070;
    public int anInt1071;
    public static FileTable aClass9_1072;
    public int anInt1073;
    public static JString aClass3_1074;
    public int[] anIntArray1075;
    public int anInt1076;
    public int anInt1077;
    public static int anInt1078;
    public int anInt1079;
    public byte aByte1080;

    public static void method1031(int i) {
        aClass3_1056 = null;
        aClass3_1058 = null;
        aClass3_1049 = null;
        aClass3_1074 = null;
        aClass9_1072 = null;
        aClass3_1050 = null;
        aClass3_1041 = null;
    }

    public static void method1032(FileTable class9, byte i, FileTable class9_0_,
            BitmapFont class39_sub5_sub10_sub1, boolean bool) {
        Class47.aBoolean894 = bool;
        Class4.aClass9_71 = class9;
        Class31.itemFileLoader = class9_0_;
        Class37.anInt663 = Class31.itemFileLoader.getAmountChildren(10);
        FrameBuffer.aClass39_Sub5_Sub10_Sub1_2148 = class39_sub5_sub10_sub1;
    }

    public static void method1033(int i, int i_1_, boolean bool, int i_2_) {
        for (int i_3_ = 0; i_3_ < 8; i_3_++) {
            for (int i_4_ = 0; i_4_ < 8; i_4_++) {
                Class67.heightMap[i][i_3_ + i_2_][i_1_ + i_4_] = 0;
            }
        }
        if (i_2_ > 0) {
            for (int i_5_ = 1; i_5_ < 8; i_5_++) {
                Class67.heightMap[i][i_2_][i_1_ + i_5_] = (Class67.heightMap[i][i_2_ - 1][i_1_ + i_5_]);
            }
        }
        if (i_1_ > 0) {
            for (int i_6_ = 1; i_6_ < 8; i_6_++) {
                Class67.heightMap[i][i_6_ + i_2_][i_1_] = (Class67.heightMap[i][i_2_ + i_6_][i_1_ - 1]);
            }
        }
        if (i_2_ > 0
                && Class67.heightMap[i][i_2_ - 1][i_1_] != 0) {
            Class67.heightMap[i][i_2_][i_1_] = Class67.heightMap[i][i_2_ - 1][i_1_];
        } else if (i_1_ <= 0
                || Class67.heightMap[i][i_2_][i_1_ - 1] == 0) {
            if (i_2_ > 0 && i_1_ > 0
                    && (Class67.heightMap[i][i_2_ - 1][i_1_ - 1]
                    != 0)) {
                Class67.heightMap[i][i_2_][i_1_] = Class67.heightMap[i][i_2_ - 1][i_1_ - 1];
            }
        } else {
            Class67.heightMap[i][i_2_][i_1_] = Class67.heightMap[i][i_2_][i_1_ - 1];
        }
    }

    public static void method1034(IndexedColorSprite class39_sub5_sub10_sub4, byte i) {
        int i_7_ = 256;
        for (int i_8_ = 0; i_8_ < Cache.anIntArray112.length; i_8_++) {
            Cache.anIntArray112[i_8_] = 0;
        }
        for (int i_9_ = 0; i_9_ < 5000; i_9_++) {
            int i_10_ = (int) ((double) i_7_ * (Math.random() * 128.0));
            Cache.anIntArray112[i_10_] = (int) (Math.random() * 256.0);
        }
        for (int i_11_ = 0; i_11_ < 20; i_11_++) {
            for (int i_12_ = 1; i_12_ < i_7_ - 1; i_12_++) {
                for (int i_13_ = 1; i_13_ < 127; i_13_++) {
                    int i_14_ = i_13_ + (i_12_ << 7);
                    HashTable.anIntArray377[i_14_] = (Cache.anIntArray112[i_14_ - 128]
                            + Cache.anIntArray112[i_14_ - 1]
                            + Cache.anIntArray112[i_14_ + 1]
                            + Cache.anIntArray112[i_14_ + 128]) / 4;
                }
            }
            int[] is = Cache.anIntArray112;
            Cache.anIntArray112 = HashTable.anIntArray377;
            HashTable.anIntArray377 = is;
        }
        if (class39_sub5_sub10_sub4 != null) {
            int i_15_ = 0;
            for (int i_16_ = 0; i_16_ < class39_sub5_sub10_sub4.anInt2481;
                    i_16_++) {
                for (int i_17_ = 0; class39_sub5_sub10_sub4.anInt2480 > i_17_;
                        i_17_++) {
                    if (class39_sub5_sub10_sub4.index[i_15_++] != 0) {
                        int i_18_ = i_17_ + 16 + class39_sub5_sub10_sub4.offsetX;
                        int i_19_ = class39_sub5_sub10_sub4.offsetY + (i_16_ + 16);
                        int i_20_ = (i_19_ << 7) + i_18_;
                        Cache.anIntArray112[i_20_] = 0;
                    }
                }
            }
        }
    }

    /** Native 443 Make-X action widgets in the 1/2/3-product fletching chatboxes. */
    static boolean isFletchingMakeX(int packedWidgetId, int openChatboxGroup) {
        int group = packedWidgetId >>> 16;
        int child = packedWidgetId & 0xffff;
        if (group != openChatboxGroup) return false;
        if (group == 304) return child == 5 || child == 9 || child == 13;
        if (group == 303) return child == 4 || child == 8;
        // Single-product 309 starts with Make All at child 3, X at 4.
        return group == 309 && child == 4;
    }

    public static void method1035(byte i, int i_21_, int i_22_) {
        FrameBuffer.outgoingGameBuffer.putFrame(153);
        FrameBuffer.outgoingGameBuffer.putWordLe(i_21_);
        FrameBuffer.outgoingGameBuffer.putDword(i_22_);

        // The native Make X click can put the chatbox into its "Please wait"
        // state while the server round-trip is pending. Show the numeric entry
        // immediately for the paired revision-443 client instead of relying
        // solely on the server's subsequent opcode 32 to open it. The same
        // WIDGET_SELECT packet above still tells the server which item was
        // selected and it validates the eventual entered quantity.
        if (isFletchingMakeX(i_22_, Class39_Sub5_Sub14.anInt1912)) {
            Class39_Sub5_Sub4_Sub4.anInt2285 = 1; // native numeric input mode
            Class39_Sub12.aBoolean1489 = false;
            Class66.aClass3_1151 = Class66.blankString;
            Class14.aBoolean245 = true;
        }
    }

    public static IndexedColorSprite method1036(boolean bool) {
        IndexedColorSprite class39_sub5_sub10_sub4 = new IndexedColorSprite();
        class39_sub5_sub10_sub4.colors = Class39_Sub11.anIntArray1460;
        class39_sub5_sub10_sub4.anInt2483 = Class39_Sub5_Sub12.anInt1854;
        class39_sub5_sub10_sub4.index = TraversalMap.aByteArrayArray517[0];
        class39_sub5_sub10_sub4.offsetY = SubNode.anIntArray1352[0];
        class39_sub5_sub10_sub4.anInt2480 = Class39_Sub5_Sub9.anIntArray1799[0];
        class39_sub5_sub10_sub4.anInt2482 = Class13.anInt203;
        class39_sub5_sub10_sub4.anInt2481 = Class39_Sub14.anIntArray1512[0];
        class39_sub5_sub10_sub4.offsetX = Class46_Sub1.anIntArray1548[0];
        RuntimeException_Sub1.method1123();
        return class39_sub5_sub10_sub4;
    }

    public Bzip2Block() {
        aByteArray1038 = new byte[4096];
        anIntArray1044 = new int[256];
        anIntArray1065 = new int[257];
        aBooleanArray1064 = new boolean[256];
        aByteArray1059 = new byte[18002];
        anIntArrayArray1069 = new int[6][258];
        anIntArrayArray1067 = new int[6][258];
        anIntArrayArray1070 = new int[6][258];
        anIntArray1075 = new int[6];
        anInt1077 = 0;
        anInt1076 = 0;
        anIntArray1055 = new int[16];
        aByteArray1042 = new byte[18002];
    }

    public static boolean readFrame() {
        // HotSwap does not rerun Client's static packet-size initialization.
        Client.incomingSizes[18] = 3;
        Client.incomingSizes[254] = 44; // pong: IDs, queue/lock/gap, epochs, read/dispatch/reactor delays
        if (Class37.gameSocket == null) {
            return false;
        }
        try {
            int available = Class37.gameSocket.available();
            if (available == 0) {
                return false;
            }
            if (Class4.frameId == -1) {
                available--;
                Class37.gameSocket.read((Class39_Sub5_Sub11.gameBuffer.payload), 0, 1);
                Class39_Sub5_Sub11.gameBuffer.offset = 0;
                Class4.frameId = Class39_Sub5_Sub11.gameBuffer.getFrame();
                Huffmans.frameSize = Client.incomingSizes[Class4.frameId];
            }
            if (Huffmans.frameSize == -1) {
                if (available > 0) {
                    available--;
                    Class37.gameSocket.read((Class39_Sub5_Sub11.gameBuffer.payload), 0, 1);
                    Huffmans.frameSize = (Class39_Sub5_Sub11.gameBuffer.payload[0]) & 0xff;
                } else {
                    return false;
                }
            }
            if (Huffmans.frameSize == -2) {
                if (available <= 1) {
                    return false;
                }
                Class37.gameSocket.read((Class39_Sub5_Sub11.gameBuffer.payload), 0, 2);
                available -= 2;
                Class39_Sub5_Sub11.gameBuffer.offset = 0;
                Huffmans.frameSize = Class39_Sub5_Sub11.gameBuffer.getUword();
            }
            if (available < Huffmans.frameSize) {
                return false;
            }
            Class39_Sub5_Sub11.gameBuffer.offset = 0;
            Class37.gameSocket.read(Class39_Sub5_Sub11.gameBuffer.payload, 0, Huffmans.frameSize);
            ScriptState.anInt448 = FileLoader.anInt1283;
            FileLoader.anInt1283 = Class63.anInt1117;
            Class39_Sub5_Sub11.anInt1827 = 0;
            Class63.anInt1117 = Class4.frameId;
            if (Class4.frameId == 254) {
                int pingId = Class39_Sub5_Sub11.gameBuffer.getDword();
                int serverQueueBytes = Class39_Sub5_Sub11.gameBuffer.getDword();
                int serverLockWaitMs = Class39_Sub5_Sub11.gameBuffer.getDword();
                int serverReceiveGapMs = Class39_Sub5_Sub11.gameBuffer.getDword();
                long serverReceivedMillis = Class39_Sub5_Sub11.gameBuffer.getQword();
                long serverRepliedMillis = Class39_Sub5_Sub11.gameBuffer.getQword();
                int serverReadToHandlerMs = Class39_Sub5_Sub11.gameBuffer.getDword();
                int serverLongestDispatchMs = Class39_Sub5_Sub11.gameBuffer.getDword();
                int serverLongestReactorGapMs = Class39_Sub5_Sub11.gameBuffer.getDword();
                FpsDebugOverlay.onPingReply(pingId, serverQueueBytes,
                        serverLockWaitMs, serverReceiveGapMs,
                        serverReceivedMillis, serverRepliedMillis,
                        serverReadToHandlerMs, serverLongestDispatchMs,
                        serverLongestReactorGapMs);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 178 || Class4.frameId == 160 || Class4.frameId == 146
                    || Class4.frameId == 32 || Class4.frameId == 51) {
                GrandExchangeWidgets.cancelSearch();
            }
            if (Class4.frameId == 205) {
                int i_24_ = Class39_Sub5_Sub11.gameBuffer.getUwordLe();
                if (i_24_ == 65535) {
                    i_24_ = -1;
                }
                Class45.method916(i_24_, 118);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 155) {
                int i_25_ = Class39_Sub5_Sub11.gameBuffer.method822((byte) -21);
                int i_26_ = Class39_Sub5_Sub11.gameBuffer.method818(-1);
                if (i_26_ == 65535) {
                    i_26_ = -1;
                }
                Class37.method351(1, i_25_, i_26_);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 156) {
                int i_27_ = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                int i_28_ = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                int i_29_ = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                int i_30_ = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class44.aBooleanArray837[i_27_] = true;
                Class13.anIntArray197[i_27_] = i_28_;
                Class2.anIntArray49[i_27_] = i_29_;
                Class45.anIntArray857[i_27_] = i_30_;
                Class4.anIntArray75[i_27_] = 0;
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 148) {
                ClientScript.anInt1703 = Class39_Sub5_Sub11.gameBuffer.method815((byte) 9);
                Class4.frameId = -1;
                if (ClientScript.anInt1703 == Node.anInt728) {
                    if (ClientScript.anInt1703 == 3) {
                        Node.anInt728 = 1;
                    } else {
                        Node.anInt728 = 3;
                    }
                    Class39_Sub14.aBoolean1520 = true;
                }
                return true;
            }
            if (Class4.frameId == 192) {
                JString paramStr = Class39_Sub5_Sub11.gameBuffer.getJstr();
                Object[] params = new Object[paramStr.getLength() + 1];
                for (int i = paramStr.getLength() - 1; i >= 0; i--) {
                    if (paramStr.charAt(i) != 's') {
                        params[i + 1] = new Integer(Class39_Sub5_Sub11.gameBuffer.getDword());
                    } else {
                        params[i + 1] = Class39_Sub5_Sub11.gameBuffer.getJstr();
                    }
                }
                params[0] = new Integer(Class39_Sub5_Sub11.gameBuffer.getDword());
                Class39_Sub5_Sub4_Sub4.executeClientScript(0, params, null, 115, 0, null, 0);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 111) {
                TraversalMap.aBoolean504 = true;
                GroundItem.anInt2238 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class26.anInt478 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class45.anInt869 = Class39_Sub5_Sub11.gameBuffer.getUword();
                Class46_Sub1.anInt1568 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class43.anInt807 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                if (Class43.anInt807 >= 100) {
                    Class39_Sub11.anInt1470 = GroundItem.anInt2238 * 128 + 64;
                    Node.anInt742 = Class26.anInt478 * 128 + 64;
                    Class39_Sub10.anInt1437 = (Class14.method212(Node.anInt742, 9990,
                            NameTable.height,
                            Class39_Sub11.anInt1470)
                            - Class45.anInt869);
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 41) {
                IsaacPrng.method1044();
                Class4.frameId = -1;
                return false;
            }
            if (Class4.frameId == 137) {
                int i_32_ = Class39_Sub5_Sub11.gameBuffer.getDword();
                int i_33_ = Class39_Sub5_Sub11.gameBuffer.getDword();
                int i_34_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                if (i_34_ == 65535) {
                    i_34_ = -1;
                }
                Widget class39_sub5_sub17 = Class37.getWidget(i_33_);
                if (GrandExchangeWidgets.isItemIcon(i_33_)) {
                    class39_sub5_sub17.anInt1997 = i_34_;
                    class39_sub5_sub17.anInt2096 = i_33_ == GrandExchangeWidgets.packed(19008) ? i_32_ : 1;
                    GrandExchangeWidgets.itemChosen(i_33_, i_34_);
                } else if (!class39_sub5_sub17.aBoolean2013) {
                    if (i_34_ == -1) {
                        Class4.frameId = -1;
                        class39_sub5_sub17.anInt2009 = 0;
                        return true;
                    }
                    ItemDefinition class39_sub5_sub1 = Class26.getItemDefinition(i_34_);
                    class39_sub5_sub17.anInt2009 = 4;
                    class39_sub5_sub17.anInt2098 = class39_sub5_sub1.anInt1669;
                    class39_sub5_sub17.anInt2074 = class39_sub5_sub1.anInt1649 * 100 / i_32_;
                    class39_sub5_sub17.anInt2026 = i_34_;
                    class39_sub5_sub17.anInt2011 = class39_sub5_sub1.anInt1676;
                } else {
                    class39_sub5_sub17.anInt1997 = i_34_;
                    class39_sub5_sub17.anInt2096 = i_32_;
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 3) {
                int i_35_ = Class39_Sub5_Sub11.gameBuffer.method829();
                int i_36_ = Class39_Sub5_Sub11.gameBuffer.getUwordLe();
                int i_37_ = i_36_ >> 10 & 0x1f;
                int i_38_ = i_36_ >> 5 & 0x1f;
                int i_39_ = i_36_ & 0x1f;
                Widget class39_sub5_sub17 = Class37.getWidget(i_35_);
                class39_sub5_sub17.activeQuadColor = (i_38_ << 11) + (i_37_ << 19) + (i_39_ << 3);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 204) {
                Class31.method322(Class39_Sub5_Sub9.signlink,
                        Huffmans.frameSize,
                        Class39_Sub5_Sub11.gameBuffer,
                        -125);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 77) {
                long encodedUsername = Class39_Sub5_Sub11.gameBuffer.getQword();
                JString jstr = Class63.decodeHuffmans(Class39_Sub5_Sub11.gameBuffer).method58(true);
                JMouseListener.method902(Deque.decodeBase37(encodedUsername).formatUsername(), jstr, false, 6);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 58) {
                Class39_Sub14.aBoolean1520 = true;
                int i_40_ = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                int i_41_ = Class39_Sub5_Sub11.gameBuffer.method788((byte) 115);
                int i_42_ = Class39_Sub5_Sub11.gameBuffer.method812(788075800);
                Class39_Sub14.anIntArray1543[i_40_] = i_42_;
                Class31.anIntArray555[i_40_] = i_41_;
                Class39_Sub12.anIntArray1491[i_40_] = 1;
                for (int i_43_ = 0; i_43_ < 98; i_43_++) {
                    if (i_42_ >= Class53.anIntArray952[i_43_]) {
                        Class39_Sub12.anIntArray1491[i_40_] = i_43_ + 2;
                    }
                }
                Client.anInt1269 = Class2.logicCycle;
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 147) {
                int i_44_ = Class39_Sub5_Sub11.gameBuffer.method829();
                int i_45_ = Class39_Sub5_Sub11.gameBuffer.getUwordLe();
                int i_46_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                int i_47_ = Class39_Sub5_Sub11.gameBuffer.method833((byte) 123);
                Widget class39_sub5_sub17 = Class37.getWidget(i_44_);
                class39_sub5_sub17.anInt2011 = i_46_;
                class39_sub5_sub17.anInt2074 = i_45_;
                class39_sub5_sub17.anInt2098 = i_47_;
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 159) {
                Class15.amountIgnores = Huffmans.frameSize / 8;
                for (int i_48_ = 0; Class15.amountIgnores > i_48_; i_48_++) {
                    Class39_Sub5_Sub9.ignoreUsernames[i_48_] = Class39_Sub5_Sub11.gameBuffer.getQword();
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 74) {
                int i_49_ = Class39_Sub5_Sub11.gameBuffer.method812(788075800);
                int i_50_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                Class39_Sub5_Sub4_Sub2.queuedStateValues[i_50_] = i_49_;
                if (i_49_ != Class66.stateValues[i_50_]) {
                    Class66.stateValues[i_50_] = i_49_;
                    Node.method408(i_50_, 1);
                    if (IsaacPrng.anInt1095 != -1) {
                        Class14.aBoolean245 = true;
                    }
                    Class39_Sub14.aBoolean1520 = true;
                }
                Class4.frameId = -1;
                Class66.anInt1153 = Class2.logicCycle;
                return true;
            }
            if (Class4.frameId == 29) {
                Class2.parsePlayerUpdate();
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 221) {
                int i_51_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                int i_52_ = Class39_Sub5_Sub11.gameBuffer.method833((byte) 120);
                Varbit.method594(-107, i_51_);
                if (i_52_ != -1) {
                    Varbit.method594(-95, i_52_);
                }
                if (Class39_Sub11.anInt1478 != -1) {
                    Class62_Sub2.method1084((byte) 105,
                            Class39_Sub11.anInt1478);
                    Class39_Sub11.anInt1478 = -1;
                }
                if (StillGraphic.anInt2338 != -1) {
                    Class62_Sub2.method1084((byte) -35,
                            StillGraphic.anInt2338);
                    StillGraphic.anInt2338 = -1;
                }
                Class39_Sub5_Sub14.closeChatboxInterface();
                if (SubNode.anInt1348 != i_51_) {
                    Class62_Sub2.method1084((byte) -69,
                            SubNode.anInt1348);
                    SubNode.anInt1348 = i_51_;
                    Class39_Sub14.setState(35);
                } else {
                    Node.method410(SubNode.anInt1348, true);
                }
                if (i_51_ != ClientScript.anInt1713) {
                    Class62_Sub2.method1084((byte) 125,
                            ClientScript.anInt1713);
                    ClientScript.anInt1713 = i_52_;
                } else {
                    Node.method410(ClientScript.anInt1713, true);
                }
                Class39_Sub5_Sub4_Sub4.anInt2285 = 0;
                Class39_Sub10.anInt1420 = -1;
                Class39_Sub4.method457(84, SubNode.anInt1348);
                Class39_Sub4.method457(101, ClientScript.anInt1713);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 228) {
                Class39_Sub14.aBoolean1520 = true;
                int i_53_ = Class39_Sub5_Sub11.gameBuffer.getDword();
                int i_54_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                Widget class39_sub5_sub17;
                if (i_53_ >= 0) {
                    class39_sub5_sub17 = Class37.getWidget(i_53_);
                } else {
                    class39_sub5_sub17 = null;
                }
                if (class39_sub5_sub17 != null) {
                    for (int i_55_ = 0;
                            i_55_ < class39_sub5_sub17.anIntArray2087.length;
                            i_55_++) {
                        class39_sub5_sub17.anIntArray2087[i_55_] = 0;
                        class39_sub5_sub17.anIntArray2073[i_55_] = 0;
                    }
                }
                ArchiveRequest.method863(i_54_, 92);
                int i_56_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                for (int i_57_ = 0; i_56_ > i_57_; i_57_++) {
                    int i_58_ = Class39_Sub5_Sub11.gameBuffer.method815((byte) 107);
                    if (i_58_ == 255) {
                        i_58_ = Class39_Sub5_Sub11.gameBuffer.getDword();
                    }
                    int i_59_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                    if (class39_sub5_sub17 != null
                            && class39_sub5_sub17.anIntArray2087.length > i_57_) {
                        class39_sub5_sub17.anIntArray2087[i_57_] = i_59_;
                        class39_sub5_sub17.anIntArray2073[i_57_] = i_58_;
                    }
                    Huffmans.method889(i_57_, i_54_, i_59_ - 1, 28, i_58_);
                }
                Class4.frameId = -1;
                Buffer.anInt1364 = Class2.logicCycle;
                return true;
            }
            if (Class4.frameId == 57) {
                Class14.anInt232 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class4.frameId = -1;
                Class39_Sub14.aBoolean1520 = true;
                return true;
            }
            if (Class4.frameId == 109 || Class4.frameId == 79
                    || Class4.frameId == 129 || Class4.frameId == 94
                    || Class4.frameId == 115 || Class4.frameId == 101
                    || Class4.frameId == 84 || Class4.frameId == 207
                    || Class4.frameId == 170 || Class4.frameId == 69
                    || Class4.frameId == 122) {
                Class12.parseEntityFrame(2834);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 146) {
                int i_60_ = Class39_Sub5_Sub11.gameBuffer.method833((byte) 120);
                int i_61_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                Class39_Sub5_Sub14.closeChatboxInterface();
                if (SubNode.anInt1348 != -1) {
                    Class62_Sub2.method1084((byte) 113,
                            SubNode.anInt1348);
                    SubNode.anInt1348 = -1;
                    Class39_Sub14.setState(30);
                }
                if (ClientScript.anInt1713 != -1) {
                    Class62_Sub2.method1084((byte) 119,
                            ClientScript.anInt1713);
                    ClientScript.anInt1713 = -1;
                }
                if (i_60_ != Class39_Sub11.anInt1478) {
                    Class62_Sub2.method1084((byte) 107,
                            Class39_Sub11.anInt1478);
                    Class39_Sub11.anInt1478 = i_60_;
                } else {
                    Node.method410(Class39_Sub11.anInt1478, true);
                }
                if (i_61_ == StillGraphic.anInt2338) {
                    Node.method410(StillGraphic.anInt2338, true);
                } else {
                    Class62_Sub2.method1084((byte) 108,
                            StillGraphic.anInt2338);
                    StillGraphic.anInt2338 = i_61_;
                }
                IsaacPrng.aBoolean1089 = true;
                Class39_Sub14.aBoolean1520 = true;
                Class39_Sub10.anInt1420 = -1;
                if (Class39_Sub5_Sub4_Sub4.anInt2285 != 0) {
                    Class39_Sub5_Sub4_Sub4.anInt2285 = 0;
                    Class14.aBoolean245 = true;
                }
                Class39_Sub4.method457(59, Class39_Sub11.anInt1478);
                Class39_Sub4.method457(106, StillGraphic.anInt2338);
                GrandExchangeWidgets.opened(Class39_Sub11.anInt1478);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 154) {
                int i_62_ = Class39_Sub5_Sub11.gameBuffer.method821();
                Player.aClass56_2529 = Class39_Sub5_Sub9.signlink.method256(i_62_, 0);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 179) {
                long l = Class39_Sub5_Sub11.gameBuffer.getQword();
                int i_63_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                JString class3 = Deque.decodeBase37(l).formatUsername();
                for (int i_64_ = 0; i_64_ < Class4.anInt62; i_64_++) {
                    if (ClientApplet.aLongArray2[i_64_] == l) {
                        if (Player.anIntArray2533[i_64_]
                                != i_63_) {
                            Player.anIntArray2533[i_64_] = i_63_;
                            Class39_Sub14.aBoolean1520 = true;
                            if (i_63_ > 0) {
                                JMouseListener.method902(Class66.blankString,
                                        (Class39_Sub5_Sub11.method708((new JString[]{class3,
                                            (Class39_Sub5_Sub6.aClass3_1769)}))),
                                        false, 5);
                            }
                            if (i_63_ == 0) {
                                JMouseListener.method902(Class66.blankString,
                                        (Class39_Sub5_Sub11.method708((new JString[]{class3,
                                            ScriptState.aClass3_442}))),
                                        false, 5);
                            }
                        }
                        class3 = null;
                        break;
                    }
                }
                boolean bool = false;
                if (class3 != null && Class4.anInt62 < 200) {
                    ClientApplet.aLongArray2[Class4.anInt62] = l;
                    Projectile.aClass3Array2188[Class4.anInt62] = class3;
                    Player.anIntArray2533[Class4.anInt62] = i_63_;
                    Class39_Sub14.aBoolean1520 = true;
                    Class4.anInt62++;
                }
                while (!bool) {
                    bool = true;
                    for (int i_65_ = 0; Class4.anInt62 - 1 > i_65_; i_65_++) {
                        if (((Player.anIntArray2533[i_65_]
                                != BufferedFile.worldId)
                                && (Player.anIntArray2533[i_65_ + 1]) == BufferedFile.worldId)
                                || ((Player.anIntArray2533[i_65_]) == 0
                                && (Player.anIntArray2533[i_65_ + 1]) != 0)) {
                            bool = false;
                            int i_66_ = (Player.anIntArray2533[i_65_]);
                            Player.anIntArray2533[i_65_] = (Player.anIntArray2533[i_65_ + 1]);
                            Player.anIntArray2533[(i_65_
                                    + 1)] = i_66_;
                            JString class3_67_ = (Projectile.aClass3Array2188[i_65_]);
                            Projectile.aClass3Array2188[i_65_] = (Projectile.aClass3Array2188[i_65_ + 1]);
                            Projectile.aClass3Array2188[i_65_ + 1] = class3_67_;
                            long l_68_ = ClientApplet.aLongArray2[i_65_];
                            ClientApplet.aLongArray2[i_65_] = ClientApplet.aLongArray2[i_65_ + 1];
                            ClientApplet.aLongArray2[i_65_ + 1] = l_68_;
                            Class39_Sub14.aBoolean1520 = true;
                        }
                    }
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 87) {
                Class39_Sub5_Sub7.minimapState = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 72) {
                if (Node.anInt728 == 12) {
                    Class39_Sub14.aBoolean1520 = true;
                }
                Class46_Sub1.anInt1562 = Class39_Sub5_Sub11.gameBuffer.getWord();
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 140) {
                int i_69_ = Class39_Sub5_Sub11.gameBuffer.method818(-1);
                Class63.method1086((byte) -73, i_69_);
                Class4.frameId = -1;
                Buffer.anInt1364 = Class2.logicCycle;
                return true;
            }
            if (Class4.frameId == 104) {
                for (int i_70_ = 0;
                        (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211.length
                        > i_70_);
                        i_70_++) {
                    if (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_70_]
                            != null) {
                        Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_70_].anInt2268 = -1;
                    }
                }
                for (int i_71_ = 0;
                        i_71_ < (GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241).length;
                        i_71_++) {
                    if ((GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_71_])
                            != null) {
                        GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_71_].anInt2268 = -1;
                    }
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 73) {
                int i_72_ = Class39_Sub5_Sub11.gameBuffer.method829();
                int i_73_ = Class39_Sub5_Sub11.gameBuffer.method833((byte) 120);
                int i_74_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                Widget widget = Class37.getWidget(i_72_);
                widget.anInt2069 = i_73_ + (i_74_ << 16);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 88) {
                int i_75_ = Class39_Sub5_Sub11.gameBuffer.method829();
                int i_76_ = Class39_Sub5_Sub11.gameBuffer.method818(-1);
                Widget class39_sub5_sub17 = Class37.getWidget(i_75_);
                if (class39_sub5_sub17 != null
                        && class39_sub5_sub17.type == 0) {
                    if (i_76_ < 0) {
                        i_76_ = 0;
                    }
                    if (i_76_ > (class39_sub5_sub17.anInt2095
                            - class39_sub5_sub17.quadHeight)) {
                        i_76_ = (class39_sub5_sub17.anInt2095
                                - class39_sub5_sub17.quadHeight);
                    }
                    class39_sub5_sub17.anInt1994 = i_76_;
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 219) {
                int i_77_ = Class39_Sub5_Sub11.gameBuffer.method792(true);
                if (i_77_ >= 0) {
                    Varbit.method594(-125, i_77_);
                }
                if (i_77_ != Class26.anInt485) {
                    Class62_Sub2.method1084((byte) 115, Class26.anInt485);
                    Class26.anInt485 = i_77_;
                }
                Class39_Sub4.method457(94, Class26.anInt485);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 149) {
                int i_78_ = Class39_Sub5_Sub11.gameBuffer.method818(-1);
                Varbit.method594(-98, i_78_);
                Class39_Sub5_Sub14.closeChatboxInterface();
                if (SubNode.anInt1348 != -1) {
                    Class62_Sub2.method1084((byte) 127,
                            SubNode.anInt1348);
                    SubNode.anInt1348 = -1;
                    Class39_Sub14.setState(30);
                }
                if (ClientScript.anInt1713 != -1) {
                    Class62_Sub2.method1084((byte) 112,
                            ClientScript.anInt1713);
                    ClientScript.anInt1713 = -1;
                }
                if (Class39_Sub11.anInt1478 != -1) {
                    Class62_Sub2.method1084((byte) 117,
                            Class39_Sub11.anInt1478);
                    Class39_Sub11.anInt1478 = -1;
                }
                if (StillGraphic.anInt2338 != i_78_) {
                    Class62_Sub2.method1084((byte) -62,
                            StillGraphic.anInt2338);
                    StillGraphic.anInt2338 = i_78_;
                } else {
                    Node.method410(StillGraphic.anInt2338, true);
                }
                Class39_Sub14.aBoolean1520 = true;
                IsaacPrng.aBoolean1089 = true;
                Class39_Sub10.anInt1420 = -1;
                if (Class39_Sub5_Sub4_Sub4.anInt2285 != 0) {
                    Class14.aBoolean245 = true;
                    Class39_Sub5_Sub4_Sub4.anInt2285 = 0;
                }
                Class39_Sub4.method457(104, StillGraphic.anInt2338);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 160) {
                int i_79_ = Class39_Sub5_Sub11.gameBuffer.method818(-1);
                Varbit.method594(-99, i_79_);
                if (StillGraphic.anInt2338 != -1) {
                    Class62_Sub2.method1084((byte) 117,
                            StillGraphic.anInt2338);
                    Class39_Sub14.aBoolean1520 = true;
                    IsaacPrng.aBoolean1089 = true;
                    StillGraphic.anInt2338 = -1;
                }
                // Character design can precede the tutorial mode marker on login.
                if (!(i_79_ == 269 && Class39_Sub5_Sub14.anInt1912 == 214)) {
                    Class39_Sub5_Sub14.closeChatboxInterface();
                }
                if (SubNode.anInt1348 != -1) {
                    Class62_Sub2.method1084((byte) 100,
                            SubNode.anInt1348);
                    SubNode.anInt1348 = -1;
                    Class39_Sub14.setState(30);
                }
                if (ClientScript.anInt1713 != -1) {
                    Class62_Sub2.method1084((byte) 123,
                            ClientScript.anInt1713);
                    ClientScript.anInt1713 = -1;
                }
                if (i_79_ != Class39_Sub11.anInt1478) {
                    Class62_Sub2.method1084((byte) 126,
                            Class39_Sub11.anInt1478);
                    Class39_Sub11.anInt1478 = i_79_;
                } else {
                    Node.method410(Class39_Sub11.anInt1478, true);
                }
                Class55.characterDesignActive = i_79_ == 269;
                Class39_Sub10.anInt1420 = -1;
                if (Class39_Sub5_Sub4_Sub4.anInt2285 != 0) {
                    Class14.aBoolean245 = true;
                    Class39_Sub5_Sub4_Sub4.anInt2285 = 0;
                }
                Class39_Sub4.method457(65, Class39_Sub11.anInt1478);
                GrandExchangeWidgets.opened(Class39_Sub11.anInt1478);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 121) {
                Class25.method284(-519790717, false);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 238) {
                Class39_Sub14.method876(0);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 62) {
                int i_80_ = Class39_Sub5_Sub11.gameBuffer.method810(4);
                int i_81_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                Class39_Sub5_Sub4_Sub2.queuedStateValues[i_81_] = i_80_;
                if (Class66.stateValues[i_81_] != i_80_) {
                    Class66.stateValues[i_81_] = i_80_;
                    Node.method408(i_81_, 1);
                    if (IsaacPrng.anInt1095 != -1) {
                        Class14.aBoolean245 = true;
                    }
                    Class39_Sub14.aBoolean1520 = true;
                }
                Class4.frameId = -1;
                Class66.anInt1153 = Class2.logicCycle;
                return true;
            }
            if (Class4.frameId == 242) {
                TraversalMap.aBoolean504 = false;
                for (int i_82_ = 0; i_82_ < 5; i_82_++) {
                    Class44.aBooleanArray837[i_82_] = false;
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 230) {
                Class48.anInt907 = Class39_Sub5_Sub11.gameBuffer.method833((byte) 125);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 244) {
                Node.mSectorX = Class39_Sub5_Sub11.gameBuffer.method815((byte) -36);
                IsaacPrng.mSectorY = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                while (Class39_Sub5_Sub11.gameBuffer.offset
                        < Huffmans.frameSize) {
                    Class4.frameId = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                    Class12.parseEntityFrame(2834);
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 130) {
                int i_83_ = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                JString class3 = Class39_Sub5_Sub11.gameBuffer.getJstr();
                int i_84_ = Class39_Sub5_Sub11.gameBuffer.method788((byte) 30);
                if (i_83_ >= 1 && i_83_ <= 5) {
                    if (class3.method97(Class36.aClass3_633, -66)) {
                        class3 = null;
                    }
                    Class39_Sub14.aClass3Array1519[i_83_ - 1] = class3;
                    RuntimeException_Sub1.aBooleanArray1225[i_83_ - 1] = i_84_ == 0;
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 232) {
                int i_85_ = Class39_Sub5_Sub11.gameBuffer.method792(true);
                int i_86_ = Class39_Sub5_Sub11.gameBuffer.getWord();
                int i_87_ = Class39_Sub5_Sub11.gameBuffer.method812(788075800);
                Widget class39_sub5_sub17 = Class37.getWidget(i_87_);
                Class4.frameId = -1;
                class39_sub5_sub17.anInt2021 = class39_sub5_sub17.anInt2024 + i_85_;
                class39_sub5_sub17.anInt2091 = class39_sub5_sub17.anInt2090 + i_86_;
                return true;
            }
            if (Class4.frameId == 199) {
                int i_88_ = Class39_Sub5_Sub11.gameBuffer.method804(68);
                int i_89_ = Class39_Sub5_Sub11.gameBuffer.method815((byte) -63);
                int i_90_ = Class39_Sub5_Sub11.gameBuffer.method815((byte) 100);
                NameTable.height = i_90_ >> 1;
                Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.method508(i_89_, (i_90_ & 0x1) == 1, (byte) -119, i_88_);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 163) {
                for (int i_91_ = 0; ObjectDefinition.anInt1923 > i_91_;
                        i_91_++) {
                    Class39_Sub5_Sub16 class39_sub5_sub16 = Class62.method1056(i_91_, 87);
                    if (class39_sub5_sub16 != null
                            && class39_sub5_sub16.anInt1978 == 0) {
                        Class39_Sub5_Sub4_Sub2.queuedStateValues[i_91_] = 0;
                        Class66.stateValues[i_91_] = 0;
                    }
                }
                Class39_Sub14.aBoolean1520 = true;
                if (IsaacPrng.anInt1095 != -1) {
                    Class14.aBoolean245 = true;
                }
                Class66.anInt1153 = Class2.logicCycle;
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 117) {
                Class39_Sub7.anInt1380 = (Class39_Sub5_Sub11.gameBuffer.method818(-1)
                        * 30);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 96) {
                JMouseListener.anInt787 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                if (JMouseListener.anInt787 == 1) {
                    Class30.anInt542 = Class39_Sub5_Sub11.gameBuffer.getUword();
                }
                if (JMouseListener.anInt787 >= 2 && JMouseListener.anInt787 <= 6) {
                    if (JMouseListener.anInt787 == 2) {
                        Class43.anInt823 = 64;
                        Class39_Sub5_Sub18.anInt2124 = 64;
                    }
                    if (JMouseListener.anInt787 == 3) {
                        Class39_Sub5_Sub18.anInt2124 = 64;
                        Class43.anInt823 = 0;
                    }
                    if (JMouseListener.anInt787 == 4) {
                        Class39_Sub5_Sub18.anInt2124 = 64;
                        Class43.anInt823 = 128;
                    }
                    if (JMouseListener.anInt787 == 5) {
                        Class39_Sub5_Sub18.anInt2124 = 0;
                        Class43.anInt823 = 64;
                    }
                    if (JMouseListener.anInt787 == 6) {
                        Class39_Sub5_Sub18.anInt2124 = 128;
                        Class43.anInt823 = 64;
                    }
                    JMouseListener.anInt787 = 2;
                    JString.anInt1229 = Class39_Sub5_Sub11.gameBuffer.getUword();
                    Class25.anInt471 = Class39_Sub5_Sub11.gameBuffer.getUword();
                    Class66.anInt1158 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                }
                if (JMouseListener.anInt787 == 10) {
                    Class34.anInt607 = Class39_Sub5_Sub11.gameBuffer.getUword();
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 6) {
                int i_92_ = Class39_Sub5_Sub11.gameBuffer.getWordLe((byte) -76);
                if (IsaacPrng.anInt1095 != i_92_) {
                    Class62_Sub2.method1084((byte) -61, IsaacPrng.anInt1095);
                    IsaacPrng.anInt1095 = i_92_;
                }
                Class14.aBoolean245 = true;
                Class39_Sub4.method457(103, IsaacPrng.anInt1095);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 90) {
                int i_93_ = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                int i_94_ = Class39_Sub5_Sub11.gameBuffer.method818(-1);
                if (i_94_ == 65535) {
                    i_94_ = -1;
                }
                if (Class39_Sub5_Sub14.anIntArray1914[i_93_] == i_94_) {
                    Node.method410(Class39_Sub5_Sub14.anIntArray1914[i_93_],
                            true);
                } else {
                    Class62_Sub2.method1084((byte) 109,
                            (Class39_Sub5_Sub14.anIntArray1914[i_93_]));
                    Class39_Sub5_Sub14.anIntArray1914[i_93_] = i_94_;
                }
                IsaacPrng.aBoolean1089 = true;
                Class39_Sub14.aBoolean1520 = true;
                Class39_Sub4.method457(115, Class39_Sub5_Sub14.anIntArray1914[i_93_]);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 15) {
                Class30.anInt544 = 0;
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 245) {
                int i_95_ = Class39_Sub5_Sub11.gameBuffer.method812(788075800);
                Widget class39_sub5_sub17 = Class37.getWidget(i_95_);
                for (int i_96_ = 0;
                        i_96_ < class39_sub5_sub17.anIntArray2087.length;
                        i_96_++) {
                    class39_sub5_sub17.anIntArray2087[i_96_] = -1;
                    class39_sub5_sub17.anIntArray2087[i_96_] = 0;
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 10) {
                Node.anInt728 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class39_Sub14.aBoolean1520 = true;
                IsaacPrng.aBoolean1089 = true;
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 18) {
                int component = Class39_Sub5_Sub11.gameBuffer.getUword();
                int progress = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                GrandExchangeWidgets.progress(component, progress);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 180) {
                int i_97_ = Class39_Sub5_Sub11.gameBuffer.method821();
                JString class3 = Class39_Sub5_Sub11.gameBuffer.getJstr();
                Widget class39_sub5_sub17 = Class37.getWidget(i_97_);
                class3 = JSocket.sanitizeTutorialChatText(i_97_, class3);
                if (class39_sub5_sub17 != null) {
                    if (i_97_ == ((102 << 16) | 2)) {
                        JString survivalReward = Class39_Sub5_Sub9.createJstring(
                                "The Survival Guide gives you a @blu@tinderbox@bla@ and a @blu@bronze@bla@");
                        class39_sub5_sub17.anInt2091 = class3.method97(survivalReward, -66)
                                ? class39_sub5_sub17.anInt2090 - 6
                                : class39_sub5_sub17.anInt2090;
                    }
                    class39_sub5_sub17.aClass3_2029 = class3;
                    if (i_97_ == ((228 << 16) | 0)) {
                        JSocket.configureCastleWarsOfferSwords();
                    }
                }
                // Hidden 211:3 selector controls the icon in the adjacent
                // passive 211:4 sprite. It is never printed in the chatbox.
                if (i_97_ == ((211 << 16) | 3)) {
                    JSocket.updateLevelUpSkillIcon(class3);
                }
                if ((i_97_ >>> 16) == 214) {
                    JSocket.updateTutorialChatScroll();
                    Class14.aBoolean245 = true;
                }
                if (i_97_ >> 16
                        == Class39_Sub5_Sub14.anIntArray1914[Node.anInt728]) {
                    Class39_Sub14.aBoolean1520 = true;
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 234) {
                boolean bool = (Class39_Sub5_Sub11.gameBuffer.method788((byte) -101)
                        == 1);
                int i_98_ = Class39_Sub5_Sub11.gameBuffer.method812(788075800);
                Widget class39_sub5_sub17 = Class37.getWidget(i_98_);
                Class4.frameId = -1;
                if (class39_sub5_sub17 != null) {
                    class39_sub5_sub17.aBoolean2055 = bool;
                }
                return true;
            }
            if (Class4.frameId == 25) {
                long l = Class39_Sub5_Sub11.gameBuffer.getQword();
                long l_99_ = (long) Class39_Sub5_Sub11.gameBuffer.getUword();
                long l_100_ = (long) Class39_Sub5_Sub11.gameBuffer.getUtri((byte) -62);
                int i_101_ = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                long l_102_ = (l_99_ << 32) - -l_100_;
                boolean bool = false;
                for (int i_103_ = 0; i_103_ < 100; i_103_++) {
                    if (Class39_Sub7.aLongArray1385[i_103_] == l_102_) {
                        bool = true;
                        break;
                    }
                }
                if (i_101_ <= 1) {
                    for (int i_104_ = 0; Class15.amountIgnores > i_104_; i_104_++) {
                        if (l == Class39_Sub5_Sub9.ignoreUsernames[i_104_]) {
                            bool = true;
                            break;
                        }
                    }
                }
                if (!bool && Class36.anInt630 == 0) {
                    Class39_Sub7.aLongArray1385[Class46.anInt884] = l_102_;
                    Class46.anInt884 = (Class46.anInt884 + 1) % 100;
                    JString class3 = Class63.decodeHuffmans(Class39_Sub5_Sub11.gameBuffer).method58(true);
                    if (i_101_ != 2 && i_101_ != 3) {
                        if (i_101_ != 1) {
                            JMouseListener.method902(Deque.decodeBase37(l).formatUsername(),
                                    class3, false, 3);
                        } else {
                            JMouseListener.method902((Class39_Sub5_Sub11.method708(new JString[]{Class37.aClass3_661,
                                        Deque.decodeBase37(l).formatUsername()})),
                                    class3, false, 7);
                        }
                    } else {
                        JMouseListener.method902((Class39_Sub5_Sub11.method708((new JString[]{Class53.aClass3_959,
                                    Deque.decodeBase37(l).formatUsername()}))),
                                class3, false, 7);
                    }
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 97) {
                int i_105_ = Class39_Sub5_Sub11.gameBuffer.method833((byte) 126);
                Varbit.method594(-90, i_105_);
                if (StillGraphic.anInt2338 != -1) {
                    Class62_Sub2.method1084((byte) 114,
                            StillGraphic.anInt2338);
                    StillGraphic.anInt2338 = -1;
                    Class39_Sub14.aBoolean1520 = true;
                    IsaacPrng.aBoolean1089 = true;
                }
                if (SubNode.anInt1348 != -1) {
                    Class62_Sub2.method1084((byte) -113,
                            SubNode.anInt1348);
                    SubNode.anInt1348 = -1;
                    Class39_Sub14.setState(30);
                }
                if (ClientScript.anInt1713 != -1) {
                    Class62_Sub2.method1084((byte) -75,
                            ClientScript.anInt1713);
                    ClientScript.anInt1713 = -1;
                }
                // Character design (group 269) intentionally coexists with the
                // Tutorial Island instruction chatbox. Stock packet 97 normally
                // closes the viewport interface, which made character creation
                // disappear as soon as "Getting Started" was opened.
                if (Class39_Sub11.anInt1478 != -1
                        && Class39_Sub11.anInt1478 != 269
                        && !((Class39_Sub11.anInt1478 == 312
                        || Class39_Sub11.anInt1478 == 465 && JSocket.isTutorialInstructionMode())
                        && i_105_ == 214)) {
                    Class62_Sub2.method1084((byte) 107,
                            Class39_Sub11.anInt1478);
                    Class39_Sub11.anInt1478 = -1;
                }
                if (i_105_ != Class39_Sub5_Sub14.anInt1912) {
                    Class62_Sub2.method1084((byte) 122,
                            Class39_Sub5_Sub14.anInt1912);
                    Class39_Sub5_Sub14.anInt1912 = i_105_;
                } else {
                    Node.method410(Class39_Sub5_Sub14.anInt1912, true);
                }
                Class39_Sub10.anInt1420 = -1;
                Class39_Sub4.method457(45, Class39_Sub5_Sub14.anInt1912);
                Class14.aBoolean245 = true;
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 213) {
                Class39_Sub14.aBoolean1520 = true;
                int i_106_ = Class39_Sub5_Sub11.gameBuffer.getDword();
                int i_107_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                Widget class39_sub5_sub17;
                if (i_106_ < 0) {
                    class39_sub5_sub17 = null;
                } else {
                    class39_sub5_sub17 = Class37.getWidget(i_106_);
                }
                while (Class39_Sub5_Sub11.gameBuffer.offset
                        < Huffmans.frameSize) {
                    int i_108_ = Class39_Sub5_Sub11.gameBuffer.getSmartB();
                    int i_109_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                    int i_110_ = 0;
                    if (i_109_ != 0) {
                        i_110_ = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                        if (i_110_ == 255) {
                            i_110_ = Class39_Sub5_Sub11.gameBuffer.getDword();
                        }
                    }
                    if (class39_sub5_sub17 != null && i_108_ >= 0
                            && class39_sub5_sub17.anIntArray2087.length > i_108_) {
                        class39_sub5_sub17.anIntArray2087[i_108_] = i_109_;
                        class39_sub5_sub17.anIntArray2073[i_108_] = i_110_;
                    }
                    Huffmans.method889(i_108_, i_107_, i_109_ - 1, 28, i_110_);
                }
                Buffer.anInt1364 = Class2.logicCycle;
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 82) {
                Node.mSectorX = Class39_Sub5_Sub11.gameBuffer.method804(58);
                IsaacPrng.mSectorY = Class39_Sub5_Sub11.gameBuffer.method804(101);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 95) {
                int i_111_ = Class39_Sub5_Sub11.gameBuffer.method821();
                int i_112_ = Class39_Sub5_Sub11.gameBuffer.method820(-1);
                Widget class39_sub5_sub17 = Class37.getWidget(i_111_);
                if (i_112_ != class39_sub5_sub17.anInt2103 || i_112_ == -1) {
                    class39_sub5_sub17.anInt2079 = 0;
                    class39_sub5_sub17.anInt2103 = i_112_;
                    class39_sub5_sub17.anInt1999 = 0;
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 157) {
                JString class3 = Class39_Sub5_Sub11.gameBuffer.getJstr();
                if (!class3.method84(FileLoader.aClass3_1293, 0)) {
                    if (class3.method84(Class34.aClass3_614, 0)) {
                        boolean bool = false;
                        JString class3_113_ = (class3.method59(0, -1,
                                class3.method80(22938, (OndemandRequest.aClass3_1723))));
                        long l = class3_113_.encodeBase37();
                        for (int i_114_ = 0; i_114_ < Class15.amountIgnores;
                                i_114_++) {
                            if (Class39_Sub5_Sub9.ignoreUsernames[i_114_]
                                    == l) {
                                bool = true;
                                break;
                            }
                        }
                        if (!bool && Class36.anInt630 == 0) {
                            JMouseListener.method902(class3_113_,
                                    IsaacPrng.aClass3_1085, false, 8);
                        }
                    } else if (!class3.method84(JMouseListener.aClass3_792, 0)) {
                        JMouseListener.method902(Class66.blankString, class3, false,
                                0);
                    } else {
                        JString class3_115_ = (class3.method59(0, -1,
                                class3.method80(22938, (OndemandRequest.aClass3_1723))));
                        long l = class3_115_.encodeBase37();
                        boolean bool = false;
                        for (int i_116_ = 0; Class15.amountIgnores > i_116_;
                                i_116_++) {
                            if (l
                                    == Class39_Sub5_Sub9.ignoreUsernames[i_116_]) {
                                bool = true;
                                break;
                            }
                        }
                        if (!bool && Class36.anInt630 == 0) {
                            JString class3_117_ = (class3.method59((class3.method80(22938, (OndemandRequest.aClass3_1723))
                                    + 1),
                                    -1, class3.getLength() - 9));
                            JMouseListener.method902(class3_115_, class3_117_, false,
                                    8);
                        }
                    }
                } else {
                    JString class3_118_ = class3.method59(0, -1,
                            class3.method80(22938,
                            (OndemandRequest.aClass3_1723)));
                    long l = class3_118_.encodeBase37();
                    boolean bool = false;
                    for (int i_119_ = 0; Class15.amountIgnores > i_119_; i_119_++) {
                        if (l == Class39_Sub5_Sub9.ignoreUsernames[i_119_]) {
                            bool = true;
                            break;
                        }
                    }
                    if (!bool && Class36.anInt630 == 0) {
                        JMouseListener.method902(class3_118_, Class30.aClass3_535,
                                false, 4);
                    }
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 193) {
                Class25.method284(-519790717, true);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 24) {
                int i_120_ = Class39_Sub5_Sub11.gameBuffer.getUwordLe();
                int i_121_ = Class39_Sub5_Sub11.gameBuffer.method829();
                Widget class39_sub5_sub17 = Class37.getWidget(i_121_);
                class39_sub5_sub17.anInt2026 = i_120_;
                class39_sub5_sub17.anInt2009 = 1;
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 241) {
                TraversalMap.aBoolean504 = true;
                Class53.anInt965 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class39_Sub5_Sub18.anInt2121 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                ScriptState.anInt454 = Class39_Sub5_Sub11.gameBuffer.getUword();
                FrameBuffer.anInt2156 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class32.anInt590 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                if (Class32.anInt590 >= 100) {
                    int i_122_ = Class53.anInt965 * 128 + 64;
                    int i_123_ = Class39_Sub5_Sub18.anInt2121 * 128 + 64;
                    int i_124_ = (Class14.method212(i_123_, 9990,
                            NameTable.height, i_122_)
                            - ScriptState.anInt454);
                    int i_125_ = i_122_ - Class39_Sub11.anInt1470;
                    int i_126_ = -Class39_Sub10.anInt1437 + i_124_;
                    int i_127_ = i_123_ - Node.anInt742;
                    int i_128_ = (int) Math.sqrt((double) (i_125_ * i_125_
                            + i_127_ * i_127_));
                    Class43.anInt799 = (int) (Math.atan2((double) i_126_, (double) i_128_)
                            * 325.949) & 0x7ff;
                    Class39_Sub5_Sub4_Sub4.anInt2315 = (int) (Math.atan2((double) i_125_, (double) i_127_)
                            * -325.949) & 0x7ff;
                    if (Class43.anInt799 < 128) {
                        Class43.anInt799 = 128;
                    }
                    if (Class43.anInt799 > 383) {
                        Class43.anInt799 = 383;
                    }
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 91) {
                anInt1051 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                NameTable.anInt177 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Cache.anInt118 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class39_Sub5_Sub4_Sub4.aBoolean2253 = true;
                Class14.aBoolean245 = true;
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 133) {
                IsaacPrng.mSectorY = Class39_Sub5_Sub11.gameBuffer.method788((byte) 27);
                Node.mSectorX = Class39_Sub5_Sub11.gameBuffer.method804(37);
                for (int i_129_ = Node.mSectorX;
                        i_129_ < Node.mSectorX + 8; i_129_++) {
                    for (int i_130_ = IsaacPrng.mSectorY;
                            i_130_ < IsaacPrng.mSectorY + 8; i_130_++) {
                        if ((Class20.groundItems[NameTable.height][i_129_][i_130_])
                                != null) {
                            Class20.groundItems[NameTable.height][i_129_][i_130_] = null;
                            Class65.updateGroundItems(i_129_, i_130_);
                        }
                    }
                }
                for (Class39_Sub11 class39_sub11 = ((Class39_Sub11) Class15.aClass49_278.getFirst());
                        class39_sub11 != null;
                        class39_sub11 = ((Class39_Sub11) Class15.aClass49_278.getNext())) {
                    if (Node.mSectorX <= class39_sub11.anInt1466
                            && Node.mSectorX + 8 > class39_sub11.anInt1466
                            && IsaacPrng.mSectorY <= class39_sub11.anInt1474
                            && class39_sub11.anInt1474 < IsaacPrng.mSectorY + 8
                            && NameTable.height == class39_sub11.anInt1458) {
                        class39_sub11.anInt1456 = 0;
                    }
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 226) {
                if (Node.anInt728 == 12) {
                    Class39_Sub14.aBoolean1520 = true;
                }
                ArchiveRequest.anInt1405 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 32) {
                Class39_Sub5_Sub14.closeChatboxInterface();
                Class4.frameId = -1;
                Class14.aBoolean245 = true;
                Class39_Sub5_Sub4_Sub4.anInt2285 = 1;
                Class39_Sub12.aBoolean1489 = false;
                Class66.aClass3_1151 = Class66.blankString;
                return true;
            }
            if (Class4.frameId == 81) {
                int i_131_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                int i_132_ = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                int i_133_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                TraversalMap.method296(i_133_, i_132_, true, i_131_);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 236) {
                Huffmans.anInt756 = Class39_Sub5_Sub11.gameBuffer.getUbyte();
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 178) {
                if (StillGraphic.anInt2338 != -1) {
                    Class62_Sub2.method1084((byte) -34,
                            StillGraphic.anInt2338);
                    IsaacPrng.aBoolean1089 = true;
                    Class39_Sub14.aBoolean1520 = true;
                    StillGraphic.anInt2338 = -1;
                }
                Class39_Sub5_Sub14.closeChatboxInterface();
                if (SubNode.anInt1348 != -1) {
                    Class62_Sub2.method1084((byte) -125,
                            SubNode.anInt1348);
                    SubNode.anInt1348 = -1;
                    Class39_Sub14.setState(30);
                }
                if (ClientScript.anInt1713 != -1) {
                    Class62_Sub2.method1084((byte) 106,
                            ClientScript.anInt1713);
                    ClientScript.anInt1713 = -1;
                }
                if (Class39_Sub11.anInt1478 != -1) {
                    Class62_Sub2.method1084((byte) -88,
                            Class39_Sub11.anInt1478);
                    Class39_Sub11.anInt1478 = -1;
                }
                Class4.frameId = -1;
                Class39_Sub10.anInt1420 = -1;
                if (Class39_Sub5_Sub4_Sub4.anInt2285 != 0) {
                    Class14.aBoolean245 = true;
                    Class39_Sub5_Sub4_Sub4.anInt2285 = 0;
                }
                return true;
            }
            if (Class4.frameId == 22) {
                int i_134_ = Class39_Sub5_Sub11.gameBuffer.method833((byte) 121);
                if (i_134_ == 65535) {
                    i_134_ = -1;
                }
                int i_135_ = Class39_Sub5_Sub11.gameBuffer.method812(788075800);
                int i_136_ = Class39_Sub5_Sub11.gameBuffer.method821();
                Widget class39_sub5_sub17 = Class62_Sub2.method1081(i_136_, i_134_, 0);
                if (class39_sub5_sub17 != null) {
                    class39_sub5_sub17.anInt2049 = i_135_;
                }
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 220) {
                for (int i = 0; Class66.stateValues.length > i; i++) {
                    if (Class39_Sub5_Sub4_Sub2.queuedStateValues[i] != Class66.stateValues[i]) {
                        Class66.stateValues[i] = Class39_Sub5_Sub4_Sub2.queuedStateValues[i];
                        Node.method408(i, 1);
                        Class39_Sub14.aBoolean1520 = true;
                    }
                }
                Class4.frameId = -1;
                Class66.anInt1153 = Class2.logicCycle;
                return true;
            }
            if (Class4.frameId == 227) {
                int i_138_ = Class39_Sub5_Sub11.gameBuffer.getUword();
                int i_139_ = Class39_Sub5_Sub11.gameBuffer.getDword();
                Widget class39_sub5_sub17 = Class37.getWidget(i_139_);
                class39_sub5_sub17.anInt2026 = i_138_;
                Class4.frameId = -1;
                class39_sub5_sub17.anInt2009 = 2;
                return true;
            }
            if (Class4.frameId == 31) {
                int i_140_ = Class39_Sub5_Sub11.gameBuffer.method821();
                Widget class39_sub5_sub17 = Class37.getWidget(i_140_);
                class39_sub5_sub17.anInt2009 = 3;
                class39_sub5_sub17.anInt2026 = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.aClass45_2516.method928(102);
                Class4.frameId = -1;
                return true;
            }
            if (Class4.frameId == 51) {
                Class39_Sub5_Sub14.closeChatboxInterface();
                Class4.frameId = -1;
                Class39_Sub5_Sub4_Sub4.anInt2285 = 2;
                Class39_Sub12.aBoolean1489 = false;
                Class66.aClass3_1151 = Class66.blankString;
                Class14.aBoolean245 = true;
                return true;
            }
            Class39_Sub7.method849(null, 64, ("T1 - " + Class4.frameId + ","
                    + FileLoader.anInt1283 + ","
                    + ScriptState.anInt448 + " - "
                    + Huffmans.frameSize));
            IsaacPrng.method1044();
        } catch (java.io.IOException ioexception) {
            Class37.method354((byte) 39);
        } catch (Exception exception) {
            String string = ("T2 - " + Class4.frameId + "," + FileLoader.anInt1283 + ","
                    + ScriptState.anInt448 + " - " + Huffmans.frameSize + ","
                    + (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anIntArray2314[0]
                    + Class65.anInt1145)
                    + ","
                    + (JKeyListener.anInt618
                    + (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anIntArray2255[0]))
                    + " - ");
            for (int i_141_ = 0; i_141_ < Huffmans.frameSize && i_141_ < 50;
                    i_141_++) {
                string += (Class39_Sub5_Sub11.gameBuffer.payload[i_141_]) + ",";
            }
            Class39_Sub7.method849(exception, 64, string);
            IsaacPrng.method1044();
        }
        return true;
    }

    static {
        aClass3_1049 = aClass3_1058;
        aClass3_1050 = aClass3_1041;
        anInt1054 = 0;
        anInt1051 = 0;
        aClass3_1074 = Class39_Sub5_Sub9.createJstring("Willkommen auf RuneScape");
        aClass3_1056 = Class39_Sub5_Sub9.createJstring("backbase1");
    }
}

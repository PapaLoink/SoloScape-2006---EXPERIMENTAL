package unpackaged;

/* Class34 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import jagex.io.BufferedFile;
import jagex.utils.Huffmans;
import jagex.world.actors.GroundItem;
import jagex.graphics.JImage;
import jagex.graphics.AbstractImage;
import jagex.graphics.sprites.IndexedColorSprite;
import jagex.world.actors.StillGraphic;
import jagex.world.actors.Projectile;
import jagex.utils.SubNode;
import jagex.utils.Node;
import jagex.utils.IsaacPrng;
import jagex.utils.JString;
import jagex.io.JSocket;
import jagex.world.actors.Player;
import jagex.world.map.TraversalMap;
import jagex.utils.Deque;
import jagex.io.FrameBuffer;
import jagex.io.Buffer;
import jagex.utils.Cache;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

public abstract class Class34 {

    public static int anInt604;
    public static int anInt605;
    public static IndexedColorSprite aClass39_Sub5_Sub10_Sub4_606;
    public static int anInt607 = 0;
    public static JString aClass3_608;
    public static JString aClass3_609;
    public static AbstractImage aClass57_610;
    public static JString aClass3_611 = Class39_Sub5_Sub9.createJstring("Okay");
    public static JString aClass3_612;
    public static JString aClass3_613;
    public static JString aClass3_614;
    public static JString aClass3_615;

    public abstract void method332(int i, Component component);

    public static void method333(int i, int i_0_, int i_1_, int i_2_) {
        if (i_1_ == i_2_) {
            FrameBuffer.outgoingGameBuffer.putFrame(13);
            FrameBuffer.outgoingGameBuffer.putDword(i);
            FrameBuffer.outgoingGameBuffer.putWord(i_0_);
        }
        if (i_1_ == 2) {
            FrameBuffer.outgoingGameBuffer.putFrame(217);
            FrameBuffer.outgoingGameBuffer.putDword(i);
            FrameBuffer.outgoingGameBuffer.putWord(i_0_);
        }
        if (i_1_ == 3) {
            FrameBuffer.outgoingGameBuffer.putFrame(161);
            FrameBuffer.outgoingGameBuffer.putDword(i);
            FrameBuffer.outgoingGameBuffer.putWord(i_0_);
        }
        if (i_1_ == 4) {
            FrameBuffer.outgoingGameBuffer.putFrame(3);
            FrameBuffer.outgoingGameBuffer.putDword(i);
            FrameBuffer.outgoingGameBuffer.putWord(i_0_);
        }
        if (i_1_ == 5) {
            FrameBuffer.outgoingGameBuffer.putFrame(40);
            FrameBuffer.outgoingGameBuffer.putDword(i);
            FrameBuffer.outgoingGameBuffer.putWord(i_0_);
        }
        if (i_1_ == 6) {
            FrameBuffer.outgoingGameBuffer.putFrame(73);
            FrameBuffer.outgoingGameBuffer.putDword(i);
            FrameBuffer.outgoingGameBuffer.putWord(i_0_);
        }
        if (i_1_ == 7) {
            FrameBuffer.outgoingGameBuffer.putFrame(57);
            FrameBuffer.outgoingGameBuffer.putDword(i);
            FrameBuffer.outgoingGameBuffer.putWord(i_0_);
        }
        if (i_1_ == 8) {
            FrameBuffer.outgoingGameBuffer.putFrame(167);
            FrameBuffer.outgoingGameBuffer.putDword(i);
            FrameBuffer.outgoingGameBuffer.putWord(i_0_);
        }
        if (i_1_ == 9) {
            FrameBuffer.outgoingGameBuffer.putFrame(28);
            FrameBuffer.outgoingGameBuffer.putDword(i);
            FrameBuffer.outgoingGameBuffer.putWord(i_0_);
        }
        if (i_1_ == 10) {
            FrameBuffer.outgoingGameBuffer.putFrame(8);
            FrameBuffer.outgoingGameBuffer.putDword(i);
            FrameBuffer.outgoingGameBuffer.putWord(i_0_);
        }
        Widget widget = Class62_Sub2.method1081(i, i_0_, 0);
        if (widget != null && widget.anObjectArray2045 != null) {
            Class39_Sub5_Sub4_Sub4.executeClientScript(i_1_, (widget.anObjectArray2045), null, -105, 0, widget, 0);
        }
    }

    public static void method334(int i) {
        aClass39_Sub5_Sub10_Sub4_606 = null;
        aClass3_612 = null;
        aClass3_613 = null;
        aClass3_611 = null;
        aClass3_615 = null;
        aClass3_608 = null;
        aClass57_610 = null;
        aClass3_614 = null;
        if (i == 1) {
            aClass3_609 = null;
        }
    }

    public static void method335(int i, boolean bool) {
        if ((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301 >> 7
                == Class30.anInt544)
                && (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275 >> 7
                == ArchiveRequest.anInt1407)) {
            Class30.anInt544 = 0;
        }
        int i_3_ = TraversalMap.anInt515;
        if (bool) {
            i_3_ = 1;
        }
        for (int i_4_ = 0; i_3_ > i_4_; i_4_++) {
            Player class39_sub5_sub4_sub4_sub2;
            int i_5_;
            if (bool) {
                class39_sub5_sub4_sub4_sub2 = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
                i_5_ = 33538048;
            } else {
                class39_sub5_sub4_sub4_sub2 = (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[Class1.anIntArray40[i_4_]]);
                i_5_ = Class1.anIntArray40[i_4_] << 14;
            }
            if (class39_sub5_sub4_sub4_sub2 != null
                    && class39_sub5_sub4_sub4_sub2.method510((byte) -77)) {
                class39_sub5_sub4_sub4_sub2.aBoolean2505 = false;
                int i_6_ = class39_sub5_sub4_sub4_sub2.anInt2275 >> 7;
                if ((Class45.aBoolean867 && TraversalMap.anInt515 > 50
                        || TraversalMap.anInt515 > 200)
                        && !bool
                        && (class39_sub5_sub4_sub4_sub2.anInt2317
                        == class39_sub5_sub4_sub4_sub2.anInt2303)) {
                    class39_sub5_sub4_sub4_sub2.aBoolean2505 = true;
                }
                int i_7_ = class39_sub5_sub4_sub4_sub2.anInt2301 >> 7;
                if (i_7_ >= 0 && i_7_ < 104 && i_6_ >= 0 && i_6_ < 104) {
                    if ((class39_sub5_sub4_sub4_sub2.aClass39_Sub5_Sub4_Sub6_2520) == null
                            || (Class2.logicCycle
                            < class39_sub5_sub4_sub4_sub2.anInt2512)
                            || (class39_sub5_sub4_sub4_sub2.anInt2523
                            <= Class2.logicCycle)) {
                        if (((class39_sub5_sub4_sub4_sub2.anInt2301 & 0x7f)
                                == 64)
                                && ((class39_sub5_sub4_sub4_sub2.anInt2275 & 0x7f)
                                == 64)) {
                            if (Canvas_Sub1.anInt15
                                    == ScriptState.anIntArrayArray455[i_7_][i_6_]) {
                                continue;
                            }
                            ScriptState.anIntArrayArray455[i_7_][i_6_] = Canvas_Sub1.anInt15;
                        }
                        class39_sub5_sub4_sub4_sub2.anInt2524 = Class14.method212((class39_sub5_sub4_sub4_sub2.anInt2275),
                                9990, NameTable.height,
                                (class39_sub5_sub4_sub4_sub2.anInt2301));
                        Class44.aClass38_836.method375(NameTable.height,
                                class39_sub5_sub4_sub4_sub2.anInt2301,
                                class39_sub5_sub4_sub4_sub2.anInt2275,
                                class39_sub5_sub4_sub4_sub2.anInt2524, 60,
                                class39_sub5_sub4_sub4_sub2,
                                class39_sub5_sub4_sub4_sub2.anInt2251, i_5_,
                                class39_sub5_sub4_sub4_sub2.aBoolean2298);
                    } else {
                        class39_sub5_sub4_sub4_sub2.aBoolean2505 = false;
                        class39_sub5_sub4_sub4_sub2.anInt2524 = Class14.method212((class39_sub5_sub4_sub4_sub2.anInt2275),
                                9990, NameTable.height,
                                (class39_sub5_sub4_sub4_sub2.anInt2301));
                        Class44.aClass38_836.method406(NameTable.height,
                                class39_sub5_sub4_sub4_sub2.anInt2301,
                                class39_sub5_sub4_sub4_sub2.anInt2275,
                                class39_sub5_sub4_sub4_sub2.anInt2524, 60,
                                class39_sub5_sub4_sub4_sub2,
                                class39_sub5_sub4_sub4_sub2.anInt2251, i_5_,
                                class39_sub5_sub4_sub4_sub2.anInt2511,
                                class39_sub5_sub4_sub4_sub2.anInt2510,
                                class39_sub5_sub4_sub4_sub2.anInt2513,
                                class39_sub5_sub4_sub4_sub2.anInt2522);
                    }
                }
            }
        }
    }

    public static void method336(int i) {
        if (Class39_Sub7.anInt1380 > 1) {
            Class39_Sub7.anInt1380--;
        }
        if (ClientScript.anInt1692 > 0) {
            ClientScript.anInt1692--;
        }
        if (Widget.aBoolean2116) {
            Widget.aBoolean2116 = false;
            Class37.method354((byte) 102);
        } else {
            for (int i_8_ = 0; i_8_ < 100; i_8_++) {
                if (!Bzip2Block.readFrame()) {
                    break;
                }
            }
            if (Class31.state == 30 || Class31.state == 35) {
                if (AbstractImage.aBoolean1000 && Class31.state == 30) {
                    Class46.anInt887 = 0;
                    Class30.anInt541 = 0;
                    while (Class39_Sub5_Sub7.method588(-4)) {
                        /* empty */
                    }
                    for (int i_9_ = 0; Class13.aBooleanArray200.length > i_9_;
                            i_9_++) {
                        Class13.aBooleanArray200[i_9_] = false;
                    }
                }
                BufferedFile.method233(-14, 194,
                        FrameBuffer.outgoingGameBuffer);
                synchronized (Cache.aClass31_123.anObject559) {
                    if (!Huffmans.aBoolean764) {
                        Cache.aClass31_123.anInt561 = 0;
                    } else if (Class46.anInt887 != 0
                            || Cache.aClass31_123.anInt561 >= 40) {
                        FrameBuffer.outgoingGameBuffer.putFrame(133);
                        int i_10_ = 0;
                        FrameBuffer.outgoingGameBuffer.putByte(0);
                        int i_11_ = (FrameBuffer.outgoingGameBuffer.offset);
                        for (int i_12_ = 0;
                                Cache.aClass31_123.anInt561 > i_12_; i_12_++) {
                            if (-i_11_ + (FrameBuffer.outgoingGameBuffer.offset)
                                    >= 240) {
                                break;
                            }
                            i_10_++;
                            int i_13_ = Cache.aClass31_123.anIntArray571[i_12_];
                            if (i_13_ < 0) {
                                i_13_ = 0;
                            } else if (i_13_ > 764) {
                                i_13_ = 764;
                            }
                            int i_14_ = Cache.aClass31_123.anIntArray562[i_12_];
                            if (i_14_ < 0) {
                                i_14_ = 0;
                            } else if (i_14_ > 502) {
                                i_14_ = 502;
                            }
                            int i_15_ = i_13_ + i_14_ * 765;
                            if (Cache.aClass31_123.anIntArray562[i_12_] == -1
                                    && (Cache.aClass31_123.anIntArray571[i_12_]
                                    == -1)) {
                                i_14_ = -1;
                                i_13_ = -1;
                                i_15_ = 524287;
                            }
                            if (Class39_Sub5_Sub9.anInt1807 == i_13_
                                    && i_14_ == FrameBuffer.anInt2159) {
                                if (Class46.anInt882 < 2047) {
                                    Class46.anInt882++;
                                }
                            } else {
                                int i_16_ = i_13_ - Class39_Sub5_Sub9.anInt1807;
                                Class39_Sub5_Sub9.anInt1807 = i_13_;
                                int i_17_ = -FrameBuffer.anInt2159 + i_14_;
                                FrameBuffer.anInt2159 = i_14_;
                                if (Class46.anInt882 < 8 && i_16_ >= -32
                                        && i_16_ <= 31 && i_17_ >= -32
                                        && i_17_ <= 31) {
                                    i_16_ += 32;
                                    i_17_ += 32;
                                    FrameBuffer.outgoingGameBuffer.putWord(((Class46.anInt882 << 12)
                                            - (-(i_16_ << 6) - i_17_)));
                                    Class46.anInt882 = 0;
                                } else if (Class46.anInt882 < 8) {
                                    FrameBuffer.outgoingGameBuffer.putTri(8388608 + (Class46.anInt882
                                            << 19) + i_15_);
                                    Class46.anInt882 = 0;
                                } else {
                                    FrameBuffer.outgoingGameBuffer.putDword(((Class46.anInt882 << 19) - 1073741824
                                            + i_15_));
                                    Class46.anInt882 = 0;
                                }
                            }
                        }
                        FrameBuffer.outgoingGameBuffer.putByteLength((FrameBuffer.outgoingGameBuffer.offset) - i_11_);
                        if (Cache.aClass31_123.anInt561 > i_10_) {
                            Cache.aClass31_123.anInt561 -= i_10_;
                            for (int i_18_ = 0;
                                    Cache.aClass31_123.anInt561 > i_18_;
                                    i_18_++) {
                                Cache.aClass31_123.anIntArray571[i_18_] = (Cache.aClass31_123.anIntArray571[i_10_ + i_18_]);
                                Cache.aClass31_123.anIntArray562[i_18_] = (Cache.aClass31_123.anIntArray562[i_10_ + i_18_]);
                            }
                        } else {
                            Cache.aClass31_123.anInt561 = 0;
                        }
                    }
                }
                if (Class46.anInt887 != 0) {
                    long l = ((-Class30.aLong536
                            + StillGraphic.aLong2331)
                            / 50L);
                    Class30.aLong536 = StillGraphic.aLong2331;
                    if (l > 4095L) {
                        l = 4095L;
                    }
                    int i_19_ = Bzip2Block.anInt1054;
                    int i_20_ = Class39_Sub4.anInt1329;
                    if (i_20_ >= 0) {
                        if (i_20_ > 764) {
                            i_20_ = 764;
                        }
                    } else {
                        i_20_ = 0;
                    }
                    if (i_19_ < 0) {
                        i_19_ = 0;
                    } else if (i_19_ > 502) {
                        i_19_ = 502;
                    }
                    int i_21_ = i_20_ + i_19_ * 765;
                    int i_22_ = 0;
                    if (Class46.anInt887 == 2) {
                        i_22_ = 1;
                    }
                    int i_23_ = (int) l;
                    FrameBuffer.outgoingGameBuffer.putFrame(162);
                    FrameBuffer.outgoingGameBuffer.method827(i_21_ + (i_22_ << 19) + (i_23_ << 20), -334352184);
                }
                if (Class13.aBooleanArray200[96]
                        || Class13.aBooleanArray200[97]
                        || Class13.aBooleanArray200[98]
                        || Class13.aBooleanArray200[99]) {
                    Deque.aBoolean914 = true;
                }
                if (Class39_Sub4.anInt1336 > 0) {
                    Class39_Sub4.anInt1336--;
                }
                if (Deque.aBoolean914 && Class39_Sub4.anInt1336 <= 0) {
                    Class39_Sub4.anInt1336 = 20;
                    Deque.aBoolean914 = false;
                    FrameBuffer.outgoingGameBuffer.putFrame(66);
                    FrameBuffer.outgoingGameBuffer.method832(JSocket.anInt301, (byte) -30);
                    FrameBuffer.outgoingGameBuffer.putWord(anInt605);
                }
                if (!Class43.aBoolean802 != true
                        && !JString.aBoolean1232 == true) {
                    JString.aBoolean1232 = true;
                    FrameBuffer.outgoingGameBuffer.putFrame(207);
                    ArchiveRequest.anInt1408++;
                    FrameBuffer.outgoingGameBuffer.putByte(1);
                }
                if (!Class43.aBoolean802 == true
                        && JString.aBoolean1232 == true) {
                    JString.aBoolean1232 = false;
                    FrameBuffer.outgoingGameBuffer.putFrame(207);
                    ArchiveRequest.anInt1408++;
                    FrameBuffer.outgoingGameBuffer.putByte(0);
                }
                Class23.method271((byte) -30);
                if (Class31.state == 30 || Class31.state == 35) {
                    Varbit.method591((byte) 92);
                    ClientScript.method477((byte) -103);
                    Class39_Sub5_Sub11.anInt1827++;
                    if (Class39_Sub5_Sub11.anInt1827 > 750) {
                        Class37.method354((byte) 48);
                    } else {
                        Class30.method318(0);
                        JString.method91((byte) -126);
                        Class62.method1049((byte) 108);
                        Class45.anInt856++;
                        if (Class4.anInt80 != 0) {
                            Class26.anInt503 += 20;
                            if (Class26.anInt503 >= 400) {
                                Class4.anInt80 = 0;
                            }
                        }
                        if (Class25.anInt459 != 0) {
                            GroundItem.anInt2242++;
                            if (GroundItem.anInt2242 >= 15) {
                                if (Class25.anInt459 == 2) {
                                    Class39_Sub14.aBoolean1520 = true;
                                }
                                if (Class25.anInt459 == 3) {
                                    Class14.aBoolean245 = true;
                                }
                                Class25.anInt459 = 0;
                            }
                        }
                        if (Class30.anInt534 != 0) {
                            Widget.anInt2031++;
                            if ((IsaacPrng.anInt1091
                                    > ClientScript.anInt1702 + 5)
                                    || (IsaacPrng.anInt1091
                                    < ClientScript.anInt1702 - 5)
                                    || (OndemandRequest.anInt1720 + 5
                                    < Class33.anInt599)
                                    || (Class33.anInt599
                                    < OndemandRequest.anInt1720 - 5)) {
                                Cache.aBoolean121 = true;
                            }
                            if (Class30.anInt541 == 0) {
                                if (Class30.anInt534 == 3) {
                                    Class14.aBoolean245 = true;
                                }
                                if (Class30.anInt534 == 2) {
                                    Class39_Sub14.aBoolean1520 = true;
                                }
                                Class30.anInt534 = 0;
                                if (Cache.aBoolean121
                                        && Widget.anInt2031 >= 5) {
                                    Class41.anInt768 = -1;
                                    JString.method95(33);
                                    if (ArchiveWorker.anInt1203 == Class41.anInt768
                                            && (Class14.anInt231
                                            != ArchiveRequest.anInt1403)) {
                                        Widget class39_sub5_sub17 = Class37.getWidget((ArchiveWorker.anInt1203));
                                        int i_24_ = 0;
                                        if (Client.anInt1274 == 1
                                                && (class39_sub5_sub17.anInt2078
                                                == 206)) {
                                            i_24_ = 1;
                                        }
                                        if ((class39_sub5_sub17.anIntArray2087[Class14.anInt231])
                                                <= 0) {
                                            i_24_ = 0;
                                        }
                                        if (!class39_sub5_sub17.method773(0)) {
                                            if (i_24_ != 1) {
                                                class39_sub5_sub17.method760(Class14.anInt231,
                                                        ArchiveRequest.anInt1403,
                                                        7211);
                                            } else {
                                                int i_25_ = ArchiveRequest.anInt1403;
                                                int i_26_ = Class14.anInt231;
                                                while (i_26_ != i_25_) {
                                                    if (i_26_ < i_25_) {
                                                        class39_sub5_sub17.method760(i_25_ - 1, i_25_,
                                                                7211);
                                                        i_25_--;
                                                    } else if (i_25_ < i_26_) {
                                                        class39_sub5_sub17.method760(i_25_ + 1, i_25_,
                                                                7211);
                                                        i_25_++;
                                                    }
                                                }
                                            }
                                        } else {
                                            int i_27_ = ArchiveRequest.anInt1403;
                                            int i_28_ = Class14.anInt231;
                                            class39_sub5_sub17.anIntArray2087[i_28_] = (class39_sub5_sub17.anIntArray2087[i_27_]);
                                            class39_sub5_sub17.anIntArray2073[i_28_] = (class39_sub5_sub17.anIntArray2073[i_27_]);
                                            class39_sub5_sub17.anIntArray2087[i_27_] = -1;
                                            class39_sub5_sub17.anIntArray2073[i_27_] = 0;
                                        }
                                        FrameBuffer.outgoingGameBuffer.putFrame(190);
                                        FrameBuffer.outgoingGameBuffer.putWord(ArchiveRequest.anInt1403);
                                        FrameBuffer.outgoingGameBuffer.putDword(ArchiveWorker.anInt1203);
                                        FrameBuffer.outgoingGameBuffer.putByte(i_24_);
                                        FrameBuffer.outgoingGameBuffer.putWord(Class14.anInt231);
                                    }
                                } else if ((Class45.anInt868 != 1
                                        && !(Class33.method327((Class39_Sub5_Sub11.anInt1841
                                        - 1),
                                        (byte) -128)))
                                        || (Class39_Sub5_Sub11.anInt1841
                                        <= 2)) {
                                    if (Class39_Sub5_Sub11.anInt1841 > 0) {
                                        ScriptState.method278((Class39_Sub5_Sub11.anInt1841) - 1,
                                                1);
                                    }
                                } else {
                                    Class39_Sub5_Sub9.method607(701);
                                }
                                Class46.anInt887 = 0;
                                GroundItem.anInt2242 = 10;
                            }
                        }
                        int i_29_ = 34;
                        if (SubNode.anInt1348 != -1) {
                            Class62.method1050(503, SubNode.anInt1348, 0,
                                    0, -72, i_29_, 765);
                            if (ClientScript.anInt1713 != -1) {
                                Class62.method1050(503,
                                        ClientScript.anInt1713,
                                        0, 0, 95, i_29_, 765);
                            }
                        } else {
                            if (Class39_Sub11.anInt1478 == -1) {
                                if (Class26.anInt485 != -1) {
                                    Class62.method1050(338, Class26.anInt485,
                                            4, 4, -109, i_29_, 516);
                                }
                            } else {
                                Class62.method1050(338,
                                        Class39_Sub11.anInt1478, 4,
                                        4, -108, i_29_, 516);
                            }
                            if (StillGraphic.anInt2338 != -1) {
                                Class62.method1050(466,
                                        (StillGraphic.anInt2338),
                                        553, 205, 89, i_29_, 743);
                            } else if ((Class39_Sub5_Sub14.anIntArray1914[Node.anInt728])
                                    != -1) {
                                Class62.method1050(466,
                                        (Class39_Sub5_Sub14.anIntArray1914[Node.anInt728]),
                                        553, 205, -121, i_29_, 743);
                            }
                            if (Class39_Sub5_Sub14.anInt1912 == -1) {
                                if (IsaacPrng.anInt1095 != -1) {
                                    Class62.method1050(453, IsaacPrng.anInt1095,
                                            17, 357, 79, i_29_,
                                            496);
                                }
                            } else {
                                Class62.method1050(453,
                                        (Class39_Sub5_Sub14.anInt1912),
                                        17, 357, -115, i_29_, 496);
                            }
                        }
                        if (SubNode.anInt1348 == -1) {
                            if (Class39_Sub11.anInt1478 != -1) {
                                Class62.method1050(338,
                                        Class39_Sub11.anInt1478, 4,
                                        4, 68, i_29_ ^ 0xffffffff,
                                        516);
                            } else if (Class26.anInt485 != -1) {
                                Class62.method1050(338, Class26.anInt485, 4, 4,
                                        122, i_29_ ^ 0xffffffff,
                                        516);
                            }
                            if (StillGraphic.anInt2338 != -1) {
                                Class62.method1050(466,
                                        (StillGraphic.anInt2338),
                                        553, 205, 100,
                                        i_29_ ^ 0xffffffff, 743);
                            } else if ((Class39_Sub5_Sub14.anIntArray1914[Node.anInt728])
                                    != -1) {
                                Class62.method1050(466,
                                        (Class39_Sub5_Sub14.anIntArray1914[Node.anInt728]),
                                        553, 205, 105,
                                        i_29_ ^ 0xffffffff, 743);
                            }
                            if (Class39_Sub5_Sub14.anInt1912 != -1) {
                                Class62.method1050(453,
                                        (Class39_Sub5_Sub14.anInt1912),
                                        17, 357, -115,
                                        i_29_ ^ 0xffffffff, 496);
                            } else if (IsaacPrng.anInt1095 != -1) {
                                Class62.method1050(453, IsaacPrng.anInt1095, 17,
                                        357, 125,
                                        i_29_ ^ 0xffffffff, 496);
                            }
                        } else {
                            Class62.method1050(503, SubNode.anInt1348, 0,
                                    0, -81, i_29_ ^ 0xffffffff,
                                    765);
                            if (ClientScript.anInt1713 != -1) {
                                Class62.method1050(503,
                                        ClientScript.anInt1713,
                                        0, 0, 72,
                                        i_29_ ^ 0xffffffff, 765);
                            }
                        }
                        Class62.method1046((byte) 22);
                        if (Class38.anInt682 != -1) {
                            int i_30_ = Class38.anInt677;
                            int i_31_ = Class38.anInt682;
                            boolean bool = Class26.method293(24134, 0, i_30_, 0, 0, 0, 0, true, 0,
                                    (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anIntArray2314[0]),
                                    (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anIntArray2255[0]),
                                    i_31_);
                            if (bool) {
                                Class4.anInt80 = 1;
                                Class26.anInt503 = 0;
                                Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
                                Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
                            }
                            Class38.anInt682 = -1;
                        }
                        if (Class46.anInt887 == 1
                                && OndemandRequest.aClass3_1714 != null) {
                            Class46.anInt887 = 0;
                            OndemandRequest.aClass3_1714 = null;
                            Class14.aBoolean245 = true;
                        }
                        Node.method411(113);
                        if (SubNode.anInt1348 == -1) {
                            Class39_Sub12.method872(50);
                            Class47.method948(-124);
                            Class39_Sub5_Sub5.method569((byte) -123);
                        }
                        if (Class30.anInt541 == 1 || Class46.anInt887 == 1) {
                            Class46_Sub1.anInt1547++;
                        }
                        if (Class39_Sub5_Sub16.anInt1982 == -1
                                && Class39_Sub10.anInt1440 == -1
                                && Class39_Sub5_Sub6.anInt1760 == -1) {
                            if (FrameBuffer.anInt2157 > 0) {
                                FrameBuffer.anInt2157--;
                            }
                        } else if (Class30.anInt548
                                > FrameBuffer.anInt2157) {
                            FrameBuffer.anInt2157++;
                            if (FrameBuffer.anInt2157
                                    == Class30.anInt548) {
                                if (Class39_Sub10.anInt1440 != -1) {
                                    Class39_Sub14.aBoolean1520 = true;
                                }
                                if (Class39_Sub5_Sub16.anInt1982 != -1) {
                                    Class14.aBoolean245 = true;
                                }
                            }
                        }
                        JString.method62(126);
                        if (TraversalMap.aBoolean504) {
                            Projectile.method496(2048);
                        }
                        for (int i_32_ = 0; i_32_ < 5; i_32_++) {
                            Class4.anIntArray75[i_32_]++;
                        }
                        Buffer.method814(103);
                        int i_33_ = Class32.method326(-2);
                        int i_34_ = Class39_Sub5_Sub11.method699((byte) 49);
                        if (i_33_ > ModHook.idleTicks() && i_34_ > ModHook.idleTicks()) {
                            ClientScript.anInt1692 = 250;
                            StillGraphic.method535((byte) 106, 4000);
                            FrameBuffer.outgoingGameBuffer.putFrame(192);
                        }
                        Class41.anInt780++;
                        Class39_Sub4.anInt1335++;
                        FileLoader.anInt1290++;
                        if (FileLoader.anInt1290 > 500) {
                            FileLoader.anInt1290 = 0;
                            int i_35_ = (int) (Math.random() * 8.0);
                            if ((i_35_ & 0x1) == 1) {
                                RuntimeException_Sub1.anInt1216 += Class36.anInt643;
                            }
                            if ((i_35_ & 0x4) == 4) {
                                SubNode.anInt1344 += ArchiveWorker.anInt1211;
                            }
                            if ((i_35_ & 0x2) == 2) {
                                Buffer.anInt1361 += Class39_Sub5_Sub11.anInt1828;
                            }
                        }
                        if (Class39_Sub4.anInt1335 > 500) {
                            Class39_Sub4.anInt1335 = 0;
                            int i_36_ = (int) (Math.random() * 8.0);
                            if ((i_36_ & 0x2) == 2) {
                                Class39_Sub7.anInt1386 += Class37.anInt662;
                            }
                            if ((i_36_ & 0x1) == 1) {
                                ArchiveRequest.anInt1401 += Class46_Sub1.anInt1550;
                            }
                        }
                        if (Class39_Sub7.anInt1386 < -20) {
                            Class37.anInt662 = 1;
                        }
                        if (ArchiveRequest.anInt1401 < -60) {
                            Class46_Sub1.anInt1550 = 2;
                        }
                        if (RuntimeException_Sub1.anInt1216 < -50) {
                            Class36.anInt643 = 2;
                        }
                        if (Buffer.anInt1361 < -55) {
                            Class39_Sub5_Sub11.anInt1828 = 2;
                        }
                        if (Buffer.anInt1361 > 55) {
                            Class39_Sub5_Sub11.anInt1828 = -2;
                        }
                        if (ArchiveRequest.anInt1401 > 60) {
                            Class46_Sub1.anInt1550 = -2;
                        }
                        if (SubNode.anInt1344 < -40) {
                            ArchiveWorker.anInt1211 = 1;
                        }
                        if (Class39_Sub7.anInt1386 > 10) {
                            Class37.anInt662 = -1;
                        }
                        if (SubNode.anInt1344 > 40) {
                            ArchiveWorker.anInt1211 = -1;
                        }
                        if (RuntimeException_Sub1.anInt1216 > 50) {
                            Class36.anInt643 = -2;
                        }
                        if (Class41.anInt780 > 50) {
                            anInt604++;
                            FrameBuffer.outgoingGameBuffer.putFrame(86);
                        }
                        do {
                            try {
                                if (Class37.gameSocket == null
                                        || ((FrameBuffer.outgoingGameBuffer.offset)
                                        <= 0)) {
                                    break;
                                }
                                Class37.gameSocket.write((FrameBuffer.outgoingGameBuffer.payload),
                                        0, (FrameBuffer.outgoingGameBuffer.offset));
                                Class41.anInt780 = 0;
                                FrameBuffer.outgoingGameBuffer.offset = 0;
                            } catch (java.io.IOException ioexception) {
                                Class37.method354((byte) 89);
                                break;
                            }
                            break;
                        } while (false);
                    }
                }
            }
        }
    }

    public abstract void method337(Component component, byte i);

    public abstract int method338(int i);

    public static void method339(FileTable class9, byte i, FileTable class9_37_) {
        Class39_Sub10.aClass9_1424 = class9;
        Buffer.aClass9_1362 = class9_37_;
        JImage.anInt1584 = Buffer.aClass9_1362.getAmountChildren(3);
    }

    public static void method340(Color color, int i, JString class3,
            int i_38_) {
        try {
            Graphics graphics = Class41.aCanvas778.getGraphics();
            if (BufferedFile.aFont351 == null) {
                BufferedFile.aFont351 = new Font("Helvetica", 1, 13);
                Class26.aFontMetrics491 = Class41.aCanvas778.getFontMetrics(BufferedFile.aFont351);
            }
            if (ClientScript.aBoolean1690) {
                ClientScript.aBoolean1690 = false;
                graphics.setColor(Color.black);
                graphics.fillRect(0, 0, IsaacPrng.anInt1087, Deque.anInt919);
            }
            if (color == null) {
                color = new Color(140, 17, 17);
            }
            try {
                if (Class39_Sub11.anImage1464 == null) {
                    Class39_Sub11.anImage1464 = Class41.aCanvas778.createImage(304, 34);
                }
                Graphics graphics_39_ = Class39_Sub11.anImage1464.getGraphics();
                graphics_39_.setColor(color);
                graphics_39_.drawRect(0, 0, 303, 33);
                graphics_39_.fillRect(2, 2, i_38_ * 3, 30);
                graphics_39_.setColor(Color.black);
                graphics_39_.drawRect(1, 1, 301, 31);
                graphics_39_.fillRect(2 + i_38_ * 3, 2, -(i_38_ * 3) + 300,
                        30);
                graphics_39_.setFont(BufferedFile.aFont351);
                graphics_39_.setColor(Color.white);
                class3.draw(graphics_39_, (-class3.getStringWidth(Class26.aFontMetrics491) + 304) / 2, 22);
                graphics.drawImage(Class39_Sub11.anImage1464,
                        IsaacPrng.anInt1087 / 2 - 152,
                        Deque.anInt919 / 2 - 18, null);
            } catch (Exception exception) {
                int i_40_ = IsaacPrng.anInt1087 / 2 - 152;
                int i_41_ = Deque.anInt919 / 2 - 18;
                graphics.setColor(color);
                graphics.drawRect(i_40_, i_41_, 303, 33);
                graphics.fillRect(i_40_ + 2, i_41_ + 2, i_38_ * 3, 30);
                graphics.setColor(Color.black);
                graphics.drawRect(i_40_ + 1, i_41_ + 1, 301, 31);
                graphics.fillRect(i_38_ * 3 + (i_40_ + 2), i_41_ + 2,
                        -(i_38_ * 3) + 300, 30);
                graphics.setFont(BufferedFile.aFont351);
                graphics.setColor(Color.white);
                class3.draw(graphics, (i_40_ + (-class3.getStringWidth(Class26.aFontMetrics491) + 304) / 2), i_41_ + 22);
            }
        } catch (Exception exception) {
            Class41.aCanvas778.repaint();
        }
    }

    public static void method341(boolean bool) {
        Class4.aClass7_70.method134(27392);
        Class39_Sub5_Sub16.aClass7_1975.method134(27392);
    }

    public static int method342(int i, int i_42_) {
        return i & i_42_;
    }

    static {
        anInt605 = 0;
        aClass3_609 = Class39_Sub5_Sub9.createJstring("mapback");
        aClass3_614 = Class39_Sub5_Sub9.createJstring(":duelreq:");
        aClass3_613 = Class39_Sub5_Sub9.createJstring("slide:");
        aClass3_612 = Class39_Sub5_Sub9.createJstring("Report abuse");
        aClass3_615 = aClass3_613;
        aClass3_608 = aClass3_612;
    }
}

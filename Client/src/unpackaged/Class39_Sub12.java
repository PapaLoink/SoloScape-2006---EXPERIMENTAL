package unpackaged;

import jagex.io.BufferedFile;
import jagex.utils.HashTable;
import jagex.utils.Huffmans;
import jagex.graphics.AbstractImage;
import jagex.graphics.sprites.DirectColorSprite;
import jagex.graphics.sprites.IndexedColorSprite;
import jagex.world.actors.Projectile;
import jagex.utils.Node;
import jagex.utils.JString;
import jagex.io.JSocket;
import jagex.world.actors.Npc;
import jagex.world.actors.Player;
import jagex.world.map.TraversalMap;
import jagex.utils.Deque;
import jagex.io.FrameBuffer;
import jagex.io.Buffer;
import jagex.utils.Cache;

/* Class39_Sub12 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Class39_Sub12 extends Node {

    public static JString aClass3_1482 = Class39_Sub5_Sub9.createJstring("scrollbar");
    public static JString aClass3_1483;
    public static JString aClass3_1484;
    public static JString aClass3_1485;
    public int[][] anIntArrayArray1486;
    public static boolean aBoolean1487;
    public static JString aClass3_1488;
    public static boolean aBoolean1489;
    public static int anInt1490;
    public static int[] anIntArray1491 = new int[25];
    public static JString aClass3_1492;
    public static boolean aBoolean1493;
    public int anInt1494;
    public static IndexedColorSprite aClass39_Sub5_Sub10_Sub4_1495;
    public static JString aClass3_1496;
    public int[] anIntArray1497;
    public static boolean aBoolean1498;
    public static JString aClass3_1499;
    public int anInt1500;

    public static void method870(byte i) {
        if (Class26.anInt484 == 0) {
            Class44.aClass38_836 = new Class38(4, 104, 104, Class67.heightMap);
            for (int i_0_ = 0; i_0_ < 4; i_0_++) {
                Class39_Sub5_Sub12.aClass27Array1857[i_0_] = new TraversalMap(104, 104);
            }
            Class39_Sub5_Sub4_Sub2.aClass39_Sub5_Sub10_Sub3_2219 = new DirectColorSprite(512, 512);
            Class39_Sub7.anInt1387 = 5;
            Class39_Sub5_Sub14.aClass3_1918 = Class62.aClass3_1113;
            Class26.anInt484 = 20;
        } else if (Class26.anInt484 == 20) {
            int[] is = new int[9];
            for (int i_1_ = 0; i_1_ < 9; i_1_++) {
                int i_2_ = i_1_ * 32 + 128 + 15;
                int i_3_ = Class39_Sub5_Sub10_Sub2.sineTable[i_2_];
                int i_4_ = i_2_ * 3 + 600;
                is[i_1_] = i_3_ * i_4_ >> 16;
            }
            Class38.method403(is, 500, 2400, 512, 334);
            Class39_Sub7.anInt1387 = 10;
            Class26.anInt484 = 30;
            Class39_Sub5_Sub14.aClass3_1918 = Class39_Sub5_Sub5.aClass3_1741;
        } else {
            if (Class26.anInt484 == 30) {
                Buffer.fileLoader0 = Class39_Sub7.createFileLoader(0, true, true, false);
                Class15.fileLoader1 = Class39_Sub7.createFileLoader(1, true, true, false);
                Class67.fileLoader2 = Class39_Sub7.createFileLoader(2, false, true, true);
                Node.fileLoader3 = Class39_Sub7.createFileLoader(3, true, true, false);
                Node.fileLoader4 = Class39_Sub7.createFileLoader(4, true, true, false);
                JSocket.fileLoader5 = Class39_Sub7.createFileLoader(5, true, true, true);
                Projectile.fileLoader6 = Class39_Sub7.createFileLoader(6, true, false, true);
                Class45.fileLoader7 = Class39_Sub7.createFileLoader(7, true, true, false);
                TraversalMap.fileLoader8 = Class39_Sub7.createFileLoader(8, true, true, false);
                Widget.fileLoader9 = Class39_Sub7.createFileLoader(9, true, true, false);
                Class66.fileLoader10 = Class39_Sub7.createFileLoader(10, true, true, false);
                Class36.fileLoader11 = Class39_Sub7.createFileLoader(11, true, true, false);
                Class33.fileLoader12 = Class39_Sub7.createFileLoader(12, true, true, false);
                Class55.fileLoader13 = Class39_Sub7.createFileLoader(13, false, true, true);
                Class39_Sub7.anInt1387 = 20;
                Class39_Sub5_Sub14.aClass3_1918 = Class47.aClass3_896;
                Class26.anInt484 = 40;
            } else if (Class26.anInt484 == 40) {
                int i_6_ = 0;
                i_6_ += Buffer.fileLoader0.method177(0) * 5 / 100;
                i_6_ += Class15.fileLoader1.method177(0) * 5 / 100;
                i_6_ += Class67.fileLoader2.method177(0) * 5 / 100;
                i_6_ += Node.fileLoader3.method177(0) * 5 / 100;
                i_6_ += Node.fileLoader4.method177(0) * 5 / 100;
                i_6_ += JSocket.fileLoader5.method177(0) * 5 / 100;
                i_6_ += (Projectile.fileLoader6.method177(0)
                        * 5 / 100);
                i_6_ += Class45.fileLoader7.method177(0) * 40 / 100;
                i_6_ += TraversalMap.fileLoader8.method177(0) * 5 / 100;
                i_6_ += (Widget.fileLoader9.method177(0) * 3
                        / 100);
                i_6_ += Class66.fileLoader10.method177(0) * 5 / 100;
                i_6_ += Class36.fileLoader11.method177(0) * 5 / 100;
                i_6_ += Class33.fileLoader12.method177(0) * 5 / 100;
                i_6_ += Class55.fileLoader13.method177(0) * 2 / 100;
                if (i_6_ != 100) {
                    if (i_6_ != 0) {
                        Class39_Sub5_Sub14.aClass3_1918 = (Class39_Sub5_Sub11.method708((new JString[]{JKeyListener.aClass3_623,
                                    AbstractImage.method1007((byte) 71, i_6_),
                                    Class39_Sub5_Sub18.aClass3_2134})));
                    }
                    Class39_Sub7.anInt1387 = 30;
                } else {
                    Class39_Sub7.anInt1387 = 30;
                    Class39_Sub5_Sub14.aClass3_1918 = Class39_Sub13.aClass3_1503;
                    Class26.anInt484 = 45;
                }
            } else if (Class26.anInt484 == 45) {
                if ("lan-websocket".equals(System.getProperty("client.transport"))) {
                    // CheerpJ can block while opening Java MIDI or sampled-audio devices.
                    // Keep the browser experiment silent and retain the normal effect mixer.
                    Class55.aClass62_1251 = null;
                    Class15.anInt274 = 22050;
                    jagex.world.actors.StillGraphic.aBoolean2342 = false;
                    Class20.aClass13_395 = new Class13();
                    Class1.aClass39_Sub1_Sub1_32 = new Class39_Sub1_Sub1();
                    Class13.method191((byte) 121, Class1.aClass39_Sub1_Sub1_32);
                    System.out.println("[local-render] Audio disabled; continuing browser startup.");
                } else {
                    Class66.method1101(0, Class39_Sub5_Sub9.signlink, 32031,
                            !Class45.aBoolean867);
                    Class1.aClass39_Sub1_Sub1_32 = Class65.method1096(false,
                            Class39_Sub5_Sub9.signlink,
                            Class41.aCanvas778, 20697, 22050);
                }
                ClientScript.aClass43_1693 = new Class43(22050, Class15.anInt274);
                Class39_Sub7.anInt1387 = 35;
                Class26.anInt484 = 50;
                Class39_Sub5_Sub14.aClass3_1918 = Class39_Sub5_Sub6.aClass3_1751;
            } else if (Class26.anInt484 == 50) {
                int i_7_ = 0;
                if (Npc.aClass39_Sub5_Sub10_Sub1_2495
                        == null) {
                    Npc.aClass39_Sub5_Sub10_Sub1_2495 = Class46_Sub1.createBitmapFont(TraversalMap.fileLoader8,(Class39_Sub5_Sub14.aClass3_1895),
                            Class66.blankString);
                } else {
                    i_7_++;
                }
                if (Class39_Sub5_Sub14.p12fullFont == null) {
                    Class39_Sub5_Sub14.p12fullFont = Class46_Sub1.createBitmapFont(TraversalMap.fileLoader8, Class39_Sub10.p12_full, Class66.blankString);
                } else {
                    i_7_++;
                }
                if (Class32.aClass39_Sub5_Sub10_Sub1_587 == null) {
                    Class32.aClass39_Sub5_Sub10_Sub1_587 = Class46_Sub1.createBitmapFont(TraversalMap.fileLoader8,(FrameBuffer.b12_full),
                            Class66.blankString);
                } else {
                    i_7_++;
                }
                if (i_7_ < 3) {
                    Class39_Sub5_Sub14.aClass3_1918 = (Class39_Sub5_Sub11.method708(new JString[]{Deque.aClass3_926,
                                AbstractImage.method1007((byte) 71,
                                i_7_ * 100 / 3),
                                Class39_Sub5_Sub18.aClass3_2134}));
                    Class39_Sub7.anInt1387 = 40;
                } else {
                    Class39_Sub7.anInt1387 = 40;
                    Class26.anInt484 = 60;
                    Class39_Sub5_Sub14.aClass3_1918 = Class39_Sub5_Sub7.aClass3_1786;
                }
            } else if (Class26.anInt484 == 60) {
                int i_8_ = BufferedFile.method230(true, Class66.fileLoader10,
                        TraversalMap.fileLoader8);
                int i_9_ = JKeyListener.method345((byte) 64);
                if (i_9_ > i_8_) {
                    Class39_Sub5_Sub14.aClass3_1918 = (Class39_Sub5_Sub11.method708((new JString[]{ClientApplet.aClass3_10,
                                AbstractImage.method1007((byte) 71,
                                i_8_ * 100 / i_9_),
                                Class39_Sub5_Sub18.aClass3_2134})));
                    Class39_Sub7.anInt1387 = 50;
                } else {
                    Class39_Sub7.anInt1387 = 50;
                    Class39_Sub5_Sub14.aClass3_1918 = ArchiveWorker.aClass3_1195;
                    Class39_Sub14.setState(5);
                    Class26.anInt484 = 70;
                }
            } else if (Class26.anInt484 == 70) {
                if (!Class67.fileLoader2.getArchivesLoaded()) {
                    Class39_Sub5_Sub14.aClass3_1918 = (Class39_Sub5_Sub11.method708((new JString[]{Client.aClass3_1277,
                                AbstractImage.method1007((byte) 71,
                                Class67.fileLoader2.method169(125)),
                                Class39_Sub5_Sub18.aClass3_2134})));
                    Class39_Sub7.anInt1387 = 60;
                } else {
                    Class65.method1099((byte) 119, Class67.fileLoader2);
                    Class62_Sub2.method1076(16965, Class67.fileLoader2);
                    Class34.method339(Class45.fileLoader7, (byte) 9,
                            Class67.fileLoader2);
                    ScriptState.setObjectFileLoaders(Class45.aBoolean867, Class45.fileLoader7, Class67.fileLoader2, 64);
                    Class47.setNpcFileLoaders(Class67.fileLoader2, Class45.fileLoader7);
                    Bzip2Block.method1032(Class45.fileLoader7, (byte) -113,
                            Class67.fileLoader2,
                            (Npc.aClass39_Sub5_Sub10_Sub1_2495),
                            HashTable.isMembers);
                    Class67.method1110(Class15.fileLoader1,
                            Class67.fileLoader2,
                            Buffer.fileLoader0, 102);
                    NpcDefinition.method715((byte) -16,
                            Class67.fileLoader2,
                            Class45.fileLoader7);
                    Class33.method328((byte) 80, Class67.fileLoader2);
                    Class46.method937(52, Class67.fileLoader2);
                    NpcDefinition.method714(Class45.fileLoader7,
                            (byte) 64,
                            TraversalMap.fileLoader8,
                            Node.fileLoader3);
                    Class39_Sub5_Sub14.aClass3_1918 = Class39_Sub11.aClass3_1475;
                    Class26.anInt484 = 80;
                    Class39_Sub7.anInt1387 = 60;
                }
            } else if (Class26.anInt484 == 80) {
                int i_10_ = 0;
                if (FileTable.aClass39_Sub5_Sub10_Sub3_141 != null) {
                    i_10_++;
                } else {
                    FileTable.aClass39_Sub5_Sub10_Sub3_141 = Class39_Sub5_Sub9.method599(Class41.aClass3_779,
                            Class66.blankString,
                            (TraversalMap.fileLoader8));
                }
                if (ClientScript.aClass39_Sub5_Sub10_Sub3_1712 != null) {
                    i_10_++;
                } else {
                    ClientScript.aClass39_Sub5_Sub10_Sub3_1712 = Class39_Sub5_Sub9.method599(ArchiveWorker.aClass3_1207,
                            Class66.blankString,
                            (TraversalMap.fileLoader8));
                }
                if (FileLoader.aClass39_Sub5_Sub10_Sub4Array1296 != null) {
                    i_10_++;
                } else {
                    FileLoader.aClass39_Sub5_Sub10_Sub4Array1296 = Class39_Sub5_Sub12.method713((byte) 122,
                            (TraversalMap.fileLoader8),
                            Class66.blankString,
                            Class33.aClass3_597);
                }
                if (Projectile.aClass39_Sub5_Sub10_Sub3Array2205
                        == null) {
                    Projectile.aClass39_Sub5_Sub10_Sub3Array2205 = Class39_Sub7.method852(Class39_Sub7.aClass3_1388,
                            TraversalMap.fileLoader8,
                            Class66.blankString, -30253);
                } else {
                    i_10_++;
                }
                if (Class39_Sub5_Sub16.aClass39_Sub5_Sub10_Sub3Array1992
                        != null) {
                    i_10_++;
                } else {
                    Class39_Sub5_Sub16.aClass39_Sub5_Sub10_Sub3Array1992 = Class39_Sub7.method852(Class39_Sub11.aClass3_1479,
                            TraversalMap.fileLoader8,
                            Class66.blankString, -30253);
                }
                if (Class31.aClass39_Sub5_Sub10_Sub3Array573 != null) {
                    i_10_++;
                } else {
                    Class31.aClass39_Sub5_Sub10_Sub3Array573 = Class39_Sub7.method852(Class39_Sub11.aClass3_1480,
                            TraversalMap.fileLoader8,
                            Class66.blankString, -30253);
                }
                if (Class62.aClass39_Sub5_Sub10_Sub3Array1106 != null) {
                    i_10_++;
                } else {
                    Class62.aClass39_Sub5_Sub10_Sub3Array1106 = Class39_Sub7.method852(Class39_Sub10.aClass3_1422,
                            TraversalMap.fileLoader8,
                            Class66.blankString, -30253);
                }
                if (Class20.aClass39_Sub5_Sub10_Sub3Array392 != null) {
                    i_10_++;
                } else {
                    Class20.aClass39_Sub5_Sub10_Sub3Array392 = Class39_Sub7.method852(Cache.aClass3_115,
                            TraversalMap.fileLoader8,
                            Class66.blankString, -30253);
                }
                if (Widget.aClass39_Sub5_Sub10_Sub3_2113 == null) {
                    Widget.aClass39_Sub5_Sub10_Sub3_2113 = Class39_Sub5_Sub9.method599((Class39_Sub7.aClass3_1384),
                            Class66.blankString,
                            (TraversalMap.fileLoader8));
                } else {
                    i_10_++;
                }
                if (Class36.aClass39_Sub5_Sub10_Sub3Array648 != null) {
                    i_10_++;
                } else {
                    Class36.aClass39_Sub5_Sub10_Sub3Array648 = Class39_Sub7.method852(Class33.aClass3_595,
                            TraversalMap.fileLoader8,
                            Class66.blankString, -30253);
                }
                if (Class47.aClass39_Sub5_Sub10_Sub3Array893 != null) {
                    i_10_++;
                } else {
                    Class47.aClass39_Sub5_Sub10_Sub3Array893 = Class39_Sub7.method852(ScriptState.aClass3_446,
                            TraversalMap.fileLoader8,
                            Class66.blankString, -30253);
                }
                if (Class39_Sub5_Sub16.aClass39_Sub5_Sub10_Sub3Array1986
                        != null) {
                    i_10_++;
                } else {
                    Class39_Sub5_Sub16.aClass39_Sub5_Sub10_Sub3Array1986 = Class39_Sub7.method852(Deque.aClass3_927,
                            TraversalMap.fileLoader8,
                            Class66.blankString, -30253);
                }
                if (Class62_Sub2.aClass39_Sub5_Sub10_Sub4Array1607 == null) {
                    Class62_Sub2.aClass39_Sub5_Sub10_Sub4Array1607 = Class39_Sub5_Sub12.method713((byte) -86,
                            (TraversalMap.fileLoader8),
                            Class66.blankString,
                            aClass3_1482);
                } else {
                    i_10_++;
                }
                if (Class55.aClass39_Sub5_Sub10_Sub4Array1247 != null) {
                    i_10_++;
                } else {
                    Class55.aClass39_Sub5_Sub10_Sub4Array1247 = Class39_Sub5_Sub12.method713((byte) 99,
                            (TraversalMap.fileLoader8),
                            Class66.blankString,
                            (Class39_Sub5_Sub4_Sub2.aClass3_2228));
                }
                if (i_10_ < 14) {
                    Class39_Sub5_Sub14.aClass3_1918 = (Class39_Sub5_Sub11.method708((new JString[]{Class26.aClass3_490,
                                AbstractImage.method1007((byte) 71, i_10_ * 100 / 14),
                                Class39_Sub5_Sub18.aClass3_2134})));
                    Class39_Sub7.anInt1387 = 70;
                } else {
                    int i_11_ = (int) (Math.random() * 21.0) - 10;
                    ClientScript.aClass39_Sub5_Sub10_Sub3_1712.method689();
                    int i_12_ = (int) (Math.random() * 21.0) - 10;
                    int i_13_ = (int) (Math.random() * 21.0) - 10;
                    int i_14_ = (int) (Math.random() * 41.0) - 20;
                    for (int i_15_ = 0;
                            ((Projectile.aClass39_Sub5_Sub10_Sub3Array2205).length
                            > i_15_);
                            i_15_++) {
                        Projectile.aClass39_Sub5_Sub10_Sub3Array2205[i_15_].method667(i_11_ + i_14_, i_14_ + i_13_, i_14_ + i_12_);
                    }
                    FileLoader.aClass39_Sub5_Sub10_Sub4Array1296[0].method693(i_14_ + i_11_, i_13_ + i_14_, i_12_ + i_14_);
                    Class26.anInt484 = 85;
                    Class39_Sub5_Sub14.aClass3_1918 = FileTable.aClass3_148;
                    Class39_Sub7.anInt1387 = 70;
                }
            } else if (Class26.anInt484 == 85) {
                int i_16_ = ArchiveRequest.method861((byte) -63,
                        TraversalMap.fileLoader8);
                int i_17_ = BufferedFile.method236(false);
                if (i_16_ < i_17_) {
                    Class39_Sub5_Sub14.aClass3_1918 = (Class39_Sub5_Sub11.method708((new JString[]{JKeyListener.aClass3_622,
                                AbstractImage.method1007((byte) 71,
                                i_16_ * 100 / i_17_),
                                Class39_Sub5_Sub18.aClass3_2134})));
                    Class39_Sub7.anInt1387 = 80;
                } else {
                    Class39_Sub7.anInt1387 = 80;
                    Class39_Sub5_Sub14.aClass3_1918 = Client.aClass3_1279;
                    Class26.anInt484 = 90;
                }
            } else if (Class26.anInt484 == 90) {
                if (!Widget.fileLoader9.getArchivesLoaded()) {
                    Class39_Sub5_Sub14.aClass3_1918 = (Class39_Sub5_Sub11.method708((new JString[]{ClientScript.aClass3_1689,
                                AbstractImage.method1007((byte) 71,
                                Widget.fileLoader9.method169(79)),
                                Class39_Sub5_Sub18.aClass3_2134})));
                    Class39_Sub7.anInt1387 = 90;
                } else {
                    Class55 class55 = new Class55(Widget.fileLoader9,
                            TraversalMap.fileLoader8, 20, 0.8,
                            Class45.aBoolean867 ? 64 : 128);
                    Class39_Sub5_Sub10_Sub2.method655(class55);
                    Class39_Sub5_Sub10_Sub2.method650(0.8);
                    Class26.anInt484 = 110;
                    Class39_Sub7.anInt1387 = 90;
                    Class39_Sub5_Sub14.aClass3_1918 = HashTable.aClass3_386;
                }
            } else if (Class26.anInt484 == 110) {
                Cache.aClass31_123 = new Class31();
                Class39_Sub5_Sub9.signlink.requestThread(Cache.aClass31_123, 10);
                Class39_Sub5_Sub14.aClass3_1918 = Class2.aClass3_57;
                Class26.anInt484 = 120;
                Class39_Sub7.anInt1387 = 94;
            } else if (Class26.anInt484 == 120) {
                if (!Class66.fileLoader10.method152(22411,
                        Class66.blankString,
                        Cache.aClass3_113)) {
                    Class39_Sub5_Sub14.aClass3_1918 = Class39_Sub5_Sub11.method708((new JString[]{Class14.aClass3_218,
                                (Buffer.aClass3_1366)}));
                    Class39_Sub7.anInt1387 = 96;
                } else {
                    Huffmans huffmans = new Huffmans(Class66.fileLoader10.lookupFile(Cache.aClass3_113, Class66.blankString));
                    Class62_Sub1.setHuffmans(huffmans);
                    Class39_Sub7.anInt1387 = 96;
                    Class39_Sub5_Sub14.aClass3_1918 = Player.aClass3_2528;
                    Class26.anInt484 = 130;
                }
            } else if (Class26.anInt484 == 130) {
                if (!Node.fileLoader3.getArchivesLoaded()) {
                    Class39_Sub5_Sub14.aClass3_1918 = (Class39_Sub5_Sub11.method708((new JString[]{ObjectDefinition.aClass3_1966,
                                AbstractImage.method1007((byte) 71,
                                Node.fileLoader3.method169(71) * 4 / 5),
                                Class39_Sub5_Sub18.aClass3_2134})));
                    Class39_Sub7.anInt1387 = 100;
                } else if (!Class33.fileLoader12.getArchivesLoaded()) {
                    Class39_Sub5_Sub14.aClass3_1918 = (Class39_Sub5_Sub11.method708((new JString[]{ObjectDefinition.aClass3_1966,
                                AbstractImage.method1007((byte) 71,
                                (Class33.fileLoader12.method169(108) / 6
                                + 80)),
                                Class39_Sub5_Sub18.aClass3_2134})));
                    Class39_Sub7.anInt1387 = 100;
                } else if (!Class55.fileLoader13.getArchivesLoaded()) {
                    Class39_Sub5_Sub14.aClass3_1918 = (Class39_Sub5_Sub11.method708((new JString[]{ObjectDefinition.aClass3_1966,
                                AbstractImage.method1007((byte) 71,
                                (Class55.fileLoader13.method169(74) / 20
                                + 96)),
                                Class39_Sub5_Sub18.aClass3_2134})));
                    Class39_Sub7.anInt1387 = 100;
                } else {
                    Class39_Sub7.anInt1387 = 100;
                    Class26.anInt484 = 140;
                    Class39_Sub5_Sub14.aClass3_1918 = Class36.aClass3_634;
                }
            } else if (Class26.anInt484 == 140) {
                Class39_Sub14.setState(10);
            }
        }
    }

    public static void method871(int i) {
        anIntArray1491 = null;
        aClass3_1499 = null;
        aClass3_1484 = null;
        aClass3_1496 = null;
        aClass39_Sub5_Sub10_Sub4_1495 = null;
        aClass3_1483 = null;
        aClass3_1492 = null;
        aClass3_1485 = null;
        aClass3_1488 = null;
        aClass3_1482 = null;
    }

    public static void method872(int i) {
        if (Class55.characterDesignActive) {
            return;
        }
        if (Class39_Sub5_Sub7.minimapState == 0) {
            if (Class46.anInt887 == 1) {
                int i_18_ = Class39_Sub4.anInt1329 - 25 - 550;
                int i_19_ = Bzip2Block.anInt1054 - 4 - 5;
                if (i_18_ >= 0 && i_19_ >= 0 && i_18_ < 146 && i_19_ < 151) {
                    i_18_ -= 73;
                    int i_20_ = Class34.anInt605 + ArchiveRequest.anInt1401 & 0x7ff;
                    int i_21_ = Class39_Sub5_Sub10_Sub2.sineTable[i_20_];
                    i_21_ = (Class39_Sub7.anInt1386 + 256) * i_21_ >> 8;
                    i_19_ -= 75;
                    int i_22_ = Class39_Sub5_Sub10_Sub2.cosineTable[i_20_];
                    i_22_ = i_22_ * (Class39_Sub7.anInt1386 + 256) >> 8;
                    int i_23_ = i_18_ * i_22_ + i_19_ * i_21_ >> 11;
                    int i_24_ = ((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301
                            + i_23_)
                            >> 7);
                    int i_25_ = i_22_ * i_19_ - i_21_ * i_18_ >> 11;
                    int i_26_ = (-i_25_ + (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275)
                            >> 7);
                    boolean bool = Class26.method293(24134, 0, i_26_, 0, 1, 0, 0, true,
                            0,
                            (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anIntArray2314[0]),
                            (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anIntArray2255[0]),
                            i_24_);
                    if (bool) {
                        FrameBuffer.outgoingGameBuffer.putByte(i_18_);
                        FrameBuffer.outgoingGameBuffer.putByte(i_19_);
                        FrameBuffer.outgoingGameBuffer.putWord(Class34.anInt605);
                        FrameBuffer.outgoingGameBuffer.putByte(57);
                        FrameBuffer.outgoingGameBuffer.putByte(ArchiveRequest.anInt1401);
                        FrameBuffer.outgoingGameBuffer.putByte(Class39_Sub7.anInt1386);
                        FrameBuffer.outgoingGameBuffer.putByte(89);
                        FrameBuffer.outgoingGameBuffer.putWord((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301));
                        FrameBuffer.outgoingGameBuffer.putWord((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275));
                        FrameBuffer.outgoingGameBuffer.putByte(CacheIO.anInt99);
                        FrameBuffer.outgoingGameBuffer.putByte(63);
                    }
                }
            }
        }
    }

    public Class39_Sub12(int i, byte[] is) {
        anInt1494 = i;
        Buffer class39_sub6 = new Buffer(is);
        anInt1500 = class39_sub6.getUbyte();
        anIntArrayArray1486 = new int[anInt1500][];
        anIntArray1497 = new int[anInt1500];
        for (int i_27_ = 0; i_27_ < anInt1500; i_27_++) {
            anIntArray1497[i_27_] = class39_sub6.getUbyte();
        }
        for (int i_28_ = 0; anInt1500 > i_28_; i_28_++) {
            anIntArrayArray1486[i_28_] = new int[class39_sub6.getUbyte()];
        }
        for (int i_29_ = 0; i_29_ < anInt1500; i_29_++) {
            for (int i_30_ = 0; i_30_ < anIntArrayArray1486[i_29_].length;
                    i_30_++) {
                anIntArrayArray1486[i_29_][i_30_] = class39_sub6.getUbyte();
            }
        }
    }

    static {
        aClass3_1488 = Class39_Sub5_Sub9.createJstring("Enter name of player to add to list");
        aClass3_1484 = Class39_Sub5_Sub9.createJstring("Sprites geladen)3");
        aClass3_1483 = Class39_Sub5_Sub9.createJstring("Untersuchen");
        aBoolean1487 = false;
        aClass3_1496 = aClass3_1488;
        aBoolean1493 = false;
        aClass3_1492 = Class39_Sub5_Sub9.createJstring("");
        aClass3_1499 = Class39_Sub5_Sub9.createJstring("red:");
        aBoolean1489 = false;
        aClass3_1485 = aClass3_1499;
    }
}

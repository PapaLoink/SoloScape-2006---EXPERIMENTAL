package jagex.io;

/* Class39_Sub6 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import jagex.utils.Cache;
import java.math.BigInteger;
import unpackaged.ArchiveRequest;
import unpackaged.ArchiveWorker;
import unpackaged.Bzip2Block;
import unpackaged.CacheIO;
import unpackaged.Canvas_Sub1;
import unpackaged.Class10;
import unpackaged.Class13;
import unpackaged.Class14;
import unpackaged.Class15;
import unpackaged.Class2;
import unpackaged.Class20;
import unpackaged.Class25;
import unpackaged.Class26;
import unpackaged.Class30;
import unpackaged.Class32;
import unpackaged.Class33;
import unpackaged.Class34;
import unpackaged.Class37;
import unpackaged.Class39_Sub10;
import unpackaged.Class39_Sub11;
import unpackaged.Class39_Sub12;
import unpackaged.Class39_Sub14;
import unpackaged.Class39_Sub4;
import unpackaged.Class39_Sub5_Sub12;
import unpackaged.Class39_Sub5_Sub14;
import unpackaged.Class39_Sub5_Sub16;
import unpackaged.Class39_Sub5_Sub18;
import unpackaged.Class39_Sub5_Sub4_Sub2;
import unpackaged.Class39_Sub5_Sub4_Sub4;
import unpackaged.Class39_Sub5_Sub6;
import unpackaged.Class39_Sub5_Sub7;
import unpackaged.Class39_Sub5_Sub9;
import unpackaged.Class4;
import unpackaged.Class43;
import unpackaged.Class45;
import unpackaged.Class46;
import unpackaged.Class53;
import unpackaged.Class55;
import unpackaged.Class63;
import unpackaged.Class65;
import unpackaged.Class66;
import unpackaged.Class67;
import unpackaged.Class68;
import unpackaged.ClientScript;
import jagex.utils.Deque;
import unpackaged.FileLoader;
import unpackaged.FileTable;
import jagex.utils.HashTable;
import jagex.utils.Huffmans;
import jagex.utils.IsaacPrng;
import jagex.graphics.JImage;
import unpackaged.JMouseListener;
import jagex.utils.JString;
import unpackaged.NameTable;
import jagex.utils.Node;
import jagex.world.actors.Npc;
import unpackaged.OndemandRequest;
import jagex.world.actors.Projectile;
import unpackaged.RuntimeException_Sub1;
import jagex.world.actors.StillGraphic;
import jagex.utils.SubNode;
import unpackaged.Varbit;
import unpackaged.Widget;

public class Buffer extends Node {

    public static int anInt1353 = 0;
    public static JString aClass3_1354;
    public static int[] anIntArray1355;
    public static JString aClass3_1356 = Class39_Sub5_Sub9.createJstring("An");
    public int offset;
    public static FileLoader fileLoader0;
    public static JString aClass3_1359;
    public static JString aClass3_1360;
    public static int anInt1361 = 0;
    public static FileTable aClass9_1362;
    public static JString aClass3_1363;
    public static int anInt1364;
    public static JString aClass3_1365;
    public static JString aClass3_1366;
    public byte[] payload;
    public static int anInt1368;
    public static JString aClass3_1369;
    public static JString aClass3_1370;

    public void method781(byte[] is, int i, int i_0_, int i_1_) {
        for (int i_2_ = i_1_; i_2_ < i + i_1_; i_2_++) {
            is[i_2_] = (byte) (payload[offset++] - 128);
        }
    }

    public static void method782(byte i) {
        aClass3_1365 = null;
        aClass3_1369 = null;
        aClass9_1362 = null;
        anIntArray1355 = null;
        fileLoader0 = null;
        aClass3_1360 = null;
        aClass3_1354 = null;
        aClass3_1366 = null;
        aClass3_1363 = null;
        aClass3_1359 = null;
        aClass3_1370 = null;
        aClass3_1356 = null;
    }

    public void method783(int i, int i_3_, byte[] dest, int i_4_) {
        for (int i_5_ = i_4_ + i - 1; i_5_ >= i_4_; i_5_--) {
            dest[i_5_] = payload[offset++];
        }
    }

    public int getUbyte() {
        return payload[offset++] & 0xff;
    }

    public void method785(int i, int i_6_) {
        payload[offset++] = (byte) -i_6_;
    }

    public void putWordLe(int value) {
        payload[offset++] = (byte) value;
        payload[offset++] = (byte) (value >> 8);
    }

    public static void method787(int i) {
        RuntimeException_Sub1.aClass7_1220.method134(27392);
    }

    public int method788(byte i) {
        return payload[offset++] - 128 & 0xff;
    }

    public void putDwordLe(int value) {
        payload[offset++] = (byte) value;
        payload[offset++] = (byte) (value >> 8);
        payload[offset++] = (byte) (value >> 16);
        payload[offset++] = (byte) (value >> 24);
    }

    public void putBytes(byte[] src, int off, int len) {
        for (int i = off; i < len + off; i++) {
            payload[offset++] = src[i];
        }
    }

    public int putPayloadChecksum(int startOff) {
        int checksum = Class25.calculateChecksum(payload, startOff, offset);
        putDword(checksum);
        return checksum;
    }

    public int method792(boolean bool) {
        offset += 2;
        int i = ((payload[offset - 2] << 8 & 0xff00)
                + (payload[offset - 1] - 128 & 0xff));
        if (i > 32767) {
            i -= 65536;
        }
        return i;
    }

    public int getUtri(byte i) {
        offset += 3;
        return ((payload[offset - 2] << 8 & 0xff00)
                + ((payload[offset - 3] << 16 & 0xff0000)
                + (payload[offset - 1] & 0xff)));
    }

    public int getWordLe(byte i) {
        offset += 2;
        int i_14_ = (((payload[offset - 1] & 0xff) << 8)
                + (payload[offset - 2] & 0xff));
        if (i_14_ > 32767) {
            i_14_ -= 65536;
        }
        return i_14_;
    }

    public long getQword() {
        long l = (long) getDword() & 0xffffffffL;
        long l_15_ = (long) getDword() & 0xffffffffL;
        return l_15_ + (l << 32);
    }

    public void method796(int i, int i_16_) {
        payload[offset++] = (byte) (i_16_ + 128);
        payload[offset++] = (byte) (i_16_ >> 8);
    }

    public void method797(boolean bool, int i) {
        payload[offset++] = (byte) (-i + 128);
    }

    public int method798(int i) {
        int i_17_ = payload[offset++];
        int i_18_ = 0;
        for (/**/; i_17_ < 0; i_17_ = payload[offset++]) {
            i_18_ = (i_18_ | i_17_ & 0x7f) << 7;
        }
        return i_17_ | i_18_;
    }

    public byte method799(int i) {
        return (byte) (128 - payload[offset++]);
    }

    public int getUword() {
        offset += 2;
        return ((payload[offset - 1] & 0xff)
                + ((payload[offset - 2] & 0xff) << 8));
    }

    public int getUwordLe() {
        offset += 2;
        return ((payload[offset - 1] << 8 & 0xff00)
                + (payload[offset - 2] & 0xff));
    }

    public byte getByte() {
        return payload[offset++];
    }

    public void putQword(long l) {
        payload[offset++] = (byte) (int) (l >> 56);
        payload[offset++] = (byte) (int) (l >> 48);
        payload[offset++] = (byte) (int) (l >> 40);
        payload[offset++] = (byte) (int) (l >> 32);
        payload[offset++] = (byte) (int) (l >> 24);
        payload[offset++] = (byte) (int) (l >> 16);
        payload[offset++] = (byte) (int) (l >> 8);
        payload[offset++] = (byte) (int) l;
    }

    public int method804(int i) {
        return -payload[offset++] + 128 & 0xff;
    }

    public void putByte(int i_20_) {
        payload[offset++] = (byte) i_20_;
    }

    public void putTri(int i_21_) {
        payload[offset++] = (byte) (i_21_ >> 16);
        payload[offset++] = (byte) (i_21_ >> 8);
        payload[offset++] = (byte) i_21_;
    }

    public void decipherXtea(int[] keys, int i, int i_22_, int i_23_) {
        int i_24_ = offset;
        int i_25_ = (i_22_ - i_23_) / 8;
        offset = i_23_;
        for (int i_26_ = i; i_25_ > i_26_; i_26_++) {
            int v0 = getDword();
            int i_28_ = 32;
            int counter = -957401312;
            int delta = -1640531527;
            int v1 = getDword();
            while (i_28_-- > 0) {
                v1 -= (keys[(counter & 0x1ff1) >>> 11] + counter
                        ^ v0 + (v0 << 4 ^ v0 >>> 5));
                counter -= delta;
                v0 -= (v1 + (v1 << 4 ^ v1 >>> 5)
                        ^ counter + keys[counter & 0x3]);
            }
            offset -= 8;
            putDword(v0);
            putDword(v1);
        }
        offset = i_24_;
    }

    public void putDword(int i) {
        payload[offset++] = (byte) (i >> 24);
        payload[offset++] = (byte) (i >> 16);
        payload[offset++] = (byte) (i >> 8);
        payload[offset++] = (byte) i;
    }

    public int getWord() {
        offset += 2;
        int i_33_ = ((payload[offset - 1] & 0xff)
                + (payload[offset - 2] << 8 & 0xff00));
        if (i_33_ > 32767) {
            i_33_ -= 65536;
        }
        return i_33_;
    }

    public byte method810(int i) {
        return (byte) -payload[offset++];
    }

    public int getDword() {
        offset += 4;
        return (((payload[offset - 2] & 0xff) << 8)
                + ((payload[offset - 3] & 0xff) << 16)
                + (payload[offset - 4] << 24 & ~0xffffff)
                + (payload[offset - 1] & 0xff));
    }

    public int method812(int i) {
        offset += 4;
        return (((payload[offset - 1] & 0xff) << 8)
                + ((payload[offset - 4] & 0xff) << 16)
                + (((payload[offset - 3] & 0xff) << 24)
                + (payload[offset - 2] & 0xff)));
    }

    public byte method813(byte i) {
        return (byte) (payload[offset++] - 128);
    }

    public static void method814(int i) {
        while (Class39_Sub5_Sub7.method588(-4)) {
            if (unpackaged.GrandExchangeSearch.key(Class15.anInt287, Projectile.anInt2191)) continue;
            if (Class39_Sub11.anInt1478 == -1
                    || Class26.anInt473 != Class39_Sub11.anInt1478) {
                if (!Class39_Sub12.aBoolean1489) {
                    if (Class39_Sub5_Sub4_Sub4.anInt2285 != 1) {
                        if (Class39_Sub5_Sub4_Sub4.anInt2285 == 2) {
                            if (Class15.anInt287 == 85
                                    && Class66.aClass3_1151.getLength() > 0) {
                                Class66.aClass3_1151 = (Class66.aClass3_1151.method59(0, i - 104,
                                        Class66.aClass3_1151.getLength() - 1));
                                Class14.aBoolean245 = true;
                            }
                            if (((unpackaged.GrandExchangeWidgets.searching && Class26.method290(Projectile.anInt2191, -160)) || Class37.method352((byte) 56,
                                    (Projectile.anInt2191))
                                    || Projectile.anInt2191 == 32)
                                    && Class66.aClass3_1151.getLength() < (unpackaged.GrandExchangeWidgets.searching ? 40 : 12)) {
                                Class66.aClass3_1151 = (Class66.aClass3_1151.createConcatChar(Projectile.anInt2191));
                                Class14.aBoolean245 = true;
                            }
                            if (Class15.anInt287 == 84) {
                                if (Class66.aClass3_1151.getLength()
                                        > 0) {
                                    if (!unpackaged.GrandExchangeWidgets.submitSearch(Class66.aClass3_1151)) {
                                        FrameBuffer.outgoingGameBuffer.putFrame(22);
                                        FrameBuffer.outgoingGameBuffer.putQword(Class66.aClass3_1151.encodeBase37());
                                    }
                                }
                                unpackaged.GrandExchangeWidgets.searching = false;
                                Class39_Sub5_Sub4_Sub4.anInt2285 = 0;
                                Class14.aBoolean245 = true;
                            }
                        } else if (Class39_Sub5_Sub4_Sub4.anInt2285 != 3) {
                            if (Class39_Sub5_Sub14.anInt1912 == -1
                                    && SubNode.anInt1348 == -1) {
                                if (Class15.anInt287 == 85
                                        && (Class66.aClass3_1160.getLength()
                                        > 0)) {
                                    Class66.aClass3_1160 = (Class66.aClass3_1160.method59(0, -1, Class66.aClass3_1160.getLength() - 1));
                                    Class14.aBoolean245 = true;
                                }
                                if (Class26.method290((Projectile.anInt2191),
                                        -160)
                                        && (Class66.aClass3_1160.getLength()
                                        < 80)) {
                                    Class66.aClass3_1160 = (Class66.aClass3_1160.createConcatChar(Projectile.anInt2191));
                                    Class14.aBoolean245 = true;
                                }
                                if (Class15.anInt287 == 84
                                        && (Class66.aClass3_1160.getLength()
                                        > 0)) {
                                    if (CacheIO.anInt97 == 2) {
                                        if (Class66.aClass3_1160.isEqual(Class39_Sub5_Sub18.aClass3_2138)) {
                                            Class37.method354((byte) 81);
                                        }
                                        if (Class66.aClass3_1160.isEqual((Class39_Sub5_Sub4_Sub2.aClass3_2213))) {
                                            OndemandRequest.aBoolean1718 = true;
                                        }
                                        if (Class66.aClass3_1160.isEqual(Class32.aClass3_588)) {
                                            OndemandRequest.aBoolean1718 = false;
                                        }
                                        if (Class66.aClass3_1160.isEqual(NameTable.aClass3_185)) {
                                            for (int i_34_ = 0; i_34_ < 4;
                                                    i_34_++) {
                                                for (int i_35_ = 1;
                                                        i_35_ < 103; i_35_++) {
                                                    for (int i_36_ = 1;
                                                            i_36_ < 103; i_36_++) {
                                                        Class39_Sub5_Sub12.aClass27Array1857[i_34_].adjancency[i_35_][i_36_] = 0;
                                                    }
                                                }
                                            }
                                        }
                                        if ((Class66.aClass3_1160.isEqual(Class39_Sub5_Sub18.aClass3_2123))
                                                && (Class39_Sub5_Sub6.mode
                                                == 2)) {
                                            throw new RuntimeException();
                                        }
                                        if (Class66.aClass3_1160.isEqual(ArchiveRequest.aClass3_1414)) {
                                            HashTable.aBoolean361 = true;
                                        }
                                    }
                                    if (unpackaged.ModHook.command(unpackaged.ModHook.typed())) {
                                        /* handled by the client */
                                    } else if (Class66.aClass3_1160.isEqual(Class39_Sub5_Sub9.createJstring("::fps"))) {
                                        unpackaged.FpsDebugOverlay.toggle();
                                    } else if (Class66.aClass3_1160.method65(Class10.aClass3_160, false)) {
                                        FrameBuffer.outgoingGameBuffer.putFrame(174);
                                        FrameBuffer.outgoingGameBuffer.putByte(Class66.aClass3_1160.getLength() - 1);
                                        FrameBuffer.outgoingGameBuffer.putJstr((byte) 81,
                                                Class66.aClass3_1160.method85(i ^ ~0x5e, 2));
                                    } else {
                                        unpackaged.ChatEffects effects = unpackaged.ChatEffects.parse(Class66.aClass3_1160);
                                        int i_37_ = effects.colour;
                                        int i_38_ = effects.animation;
                                        Class66.aClass3_1160 = effects.text;
                                        FrameBuffer.outgoingGameBuffer.putFrame(4);
                                        FrameBuffer.outgoingGameBuffer.putByte(0);
                                        int offset = (FrameBuffer.outgoingGameBuffer.offset);
                                        FrameBuffer.outgoingGameBuffer.putByte(i_37_);
                                        FrameBuffer.outgoingGameBuffer.putByte(i_38_);
                                        Class68.encodeHuffmans((FrameBuffer.outgoingGameBuffer),
                                                Class66.aClass3_1160, -110);
                                        FrameBuffer.outgoingGameBuffer.putByteLength((FrameBuffer.outgoingGameBuffer.offset) - offset);
                                        if (Bzip2Block.anInt1051 == 2) {
                                            Class39_Sub5_Sub4_Sub4.aBoolean2253 = true;
                                            Bzip2Block.anInt1051 = 3;
                                            Projectile.anInt2203++;
                                            FrameBuffer.outgoingGameBuffer.putFrame(76);
                                            FrameBuffer.outgoingGameBuffer.putByte(Bzip2Block.anInt1051);
                                            FrameBuffer.outgoingGameBuffer.putByte(NameTable.anInt177);
                                            FrameBuffer.outgoingGameBuffer.putByte(Cache.anInt118);
                                        }
                                    }
                                    Class14.aBoolean245 = true;
                                    Class66.aClass3_1160 = Class66.blankString;
                                }
                            }
                        } else {
                            if (Class15.anInt287 == 85
                                    && Class66.aClass3_1151.getLength() > 0) {
                                Class66.aClass3_1151 = (Class66.aClass3_1151.method59(0, -1,
                                        (Class66.aClass3_1151.getLength()
                                        - 1)));
                                Class14.aBoolean245 = true;
                            }
                            if (Class26.method290((Projectile.anInt2191),
                                    i ^ ~0xf8)
                                    && Class66.aClass3_1151.getLength() < 40) {
                                Class66.aClass3_1151 = (Class66.aClass3_1151.createConcatChar(Projectile.anInt2191));
                                Class14.aBoolean245 = true;
                            }
                        }
                    } else {
                        if (Class15.anInt287 == 85
                                && Class66.aClass3_1151.getLength() > 0) {
                            Class66.aClass3_1151 = (Class66.aClass3_1151.method59(0, i - 104,
                                    Class66.aClass3_1151.getLength() - 1));
                            Class14.aBoolean245 = true;
                        }
                        if (Class43.method909(Projectile.anInt2191,
                                -86)
                                && Class66.aClass3_1151.getLength() < 10) {
                            Class66.aClass3_1151 = (Class66.aClass3_1151.createConcatChar(Projectile.anInt2191));
                            Class14.aBoolean245 = true;
                        }
                        if (Class15.anInt287 == 84) {
                            if (Class66.aClass3_1151.getLength() > 0) {
                                int i_40_ = 0;
                                if (Class66.aClass3_1151.method87(22415)) {
                                    i_40_ = Class66.aClass3_1151.method76((byte) -71);
                                }
                                FrameBuffer.outgoingGameBuffer.putFrame(74);
                                FrameBuffer.outgoingGameBuffer.putDword(i_40_);
                            }
                            Class39_Sub5_Sub4_Sub4.anInt2285 = 0;
                            Class14.aBoolean245 = true;
                        }
                    }
                } else {
                    if (Class15.anInt287 == 85
                            && Class66.aClass3_1154.getLength() > 0) {
                        Class66.aClass3_1154 = (Class66.aClass3_1154.method59(0, -1,
                                Class66.aClass3_1154.getLength() - 1));
                        Class14.aBoolean245 = true;
                    }
                    if (Class26.method290(Projectile.anInt2191,
                            i ^ ~0xf8)
                            && Class66.aClass3_1154.getLength() < 80) {
                        Class66.aClass3_1154 = (Class66.aClass3_1154.createConcatChar(Projectile.anInt2191));
                        Class14.aBoolean245 = true;
                    }
                    if (Class15.anInt287 == 84) {
                        Class14.aBoolean245 = true;
                        Class39_Sub12.aBoolean1489 = false;
                        if (Class15.anInt277 == 1) {
                            long l = Class66.aClass3_1154.encodeBase37();
                            Class68.method1111(0, l);
                        }
                        if (Class15.anInt277 == 2 && Class4.anInt62 > 0) {
                            long l = Class66.aClass3_1154.encodeBase37();
                            Class20.method246(l, (byte) -123);
                        }
                        if (Class15.anInt277 == 3
                                && Class66.aClass3_1154.getLength() > 0) {
                            FrameBuffer.outgoingGameBuffer.putFrame(50);
                            FrameBuffer.outgoingGameBuffer.putByte(0);
                            int i_41_ = (FrameBuffer.outgoingGameBuffer.offset);
                            FrameBuffer.outgoingGameBuffer.putQword(Huffmans.aLong752);
                            Class68.encodeHuffmans((FrameBuffer.outgoingGameBuffer),
                                    Class66.aClass3_1154, -126);
                            FrameBuffer.outgoingGameBuffer.putByteLength(-i_41_ + (FrameBuffer.outgoingGameBuffer.offset));
                            if (NameTable.anInt177 == 2) {
                                Class39_Sub5_Sub4_Sub4.aBoolean2253 = true;
                                Projectile.anInt2203++;
                                NameTable.anInt177 = 1;
                                FrameBuffer.outgoingGameBuffer.putFrame(76);
                                FrameBuffer.outgoingGameBuffer.putByte(Bzip2Block.anInt1051);
                                FrameBuffer.outgoingGameBuffer.putByte(NameTable.anInt177);
                                FrameBuffer.outgoingGameBuffer.putByte(Cache.anInt118);
                            }
                        }
                        if (Class15.anInt277 == 4 && Class15.amountIgnores < 100) {
                            long l = Class66.aClass3_1154.encodeBase37();
                            Class65.method1097(0, l);
                        }
                        if (Class15.anInt277 == 5 && Class15.amountIgnores > 0) {
                            long l = Class66.aClass3_1154.encodeBase37();
                            HashTable.method237(l, (byte) -58);
                        }
                    }
                }
            } else {
                if (Class15.anInt287 == 85
                        && Class66.aClass3_1163.getLength() > 0) {
                    Class66.aClass3_1163 = (Class66.aClass3_1163.method59(0, -1,
                            Class66.aClass3_1163.getLength() - 1));
                }
                if ((Class37.method352((byte) 56,
                        Projectile.anInt2191)
                        || Projectile.anInt2191 == 32)
                        && Class66.aClass3_1163.getLength() < 12) {
                    Class66.aClass3_1163 = Class66.aClass3_1163.createConcatChar((Projectile.anInt2191));
                }
            }
        }
        if (i != 103) {
            aClass3_1366 = null;
        }
    }

    public int method815(byte i) {
        return -payload[offset++] & 0xff;
    }

    public void putByteLength(int length) {
        payload[-length + offset - 1] = (byte) length;
    }

    public int getSmartA() {
        int i_43_ = payload[offset] & 0xff;
        if (i_43_ < 128) {
            return getUbyte() - 64;
        }
        return getUword() - 49152;
    }

    public int method818(int i) {
        offset += 2;
        return ((payload[offset - 1] - 128 & 0xff)
                + (payload[offset - 2] << 8 & 0xff00));
    }

    public void method819(int i, int i_44_) {
        payload[offset++] = (byte) (i >> 8);
        payload[offset++] = (byte) i;
        payload[offset++] = (byte) (i >> 24);
        payload[offset++] = (byte) (i >> 16);
    }

    public int method820(int i) {
        offset += 2;
        int i_45_ = ((payload[offset + i] << 8 & 0xff00)
                + (payload[offset - 2] - 128 & 0xff));
        if (i_45_ > 32767) {
            i_45_ -= 65536;
        }
        return i_45_;
    }

    public int method821() {
        offset += 4;
        return ((payload[offset - 4] & 0xff)
                + (((payload[offset - 2] & 0xff) << 16)
                + (payload[offset - 1] << 24 & ~0xffffff))
                + ((payload[offset - 3] & 0xff) << 8));
    }

    public int method822(byte i) {
        offset += 3;
        return ((payload[offset - 2] & 0xff)
                + (payload[offset - 1] << 8 & 0xff00)
                + (payload[offset - 3] << 16 & 0xff0000));
    }

    public static void method823(int i) {
        if (ClientScript.aBoolean1690) {
            ClientScript.aBoolean1690 = false;
            Varbit.method590(false);
            Class14.aBoolean245 = true;
            Class39_Sub5_Sub4_Sub4.aBoolean2253 = true;
            Class39_Sub14.aBoolean1520 = true;
            IsaacPrng.aBoolean1089 = true;
        }
        Class39_Sub5_Sub7.method589((byte) 101);
        if (Class39_Sub12.aBoolean1493 && Class37.anInt653 == 1) {
            Class39_Sub14.aBoolean1520 = true;
        }
        if (StillGraphic.anInt2338 != -1) {
            boolean bool = Class39_Sub4.method459(StillGraphic.anInt2338,
                    (byte) 120);
            if (bool) {
                Class39_Sub14.aBoolean1520 = true;
            }
        }
        if (Class25.anInt459 == 2) {
            Class39_Sub14.aBoolean1520 = true;
        }
        if (Class30.anInt534 == 2) {
            Class39_Sub14.aBoolean1520 = true;
        }
        if (Class39_Sub14.aBoolean1520) {
            Class39_Sub14.aBoolean1520 = false;
            RuntimeException_Sub1.method1124((byte) 44);
        }
        unpackaged.GrandExchangeSearch.mouseScreen(IsaacPrng.anInt1091, Class33.anInt599);
        if (Class39_Sub5_Sub14.anInt1912 == -1 && !unpackaged.GrandExchangeWidgets.searching) {
            Class65.aClass39_Sub5_Sub17_1136.anInt1994 = -Node.anInt741 - 77 + Deque.anInt912;
            if (IsaacPrng.anInt1091 > 17 && IsaacPrng.anInt1091 < 560
                    && Class33.anInt599 > 332) {
                Class39_Sub4.method456((byte) 121, 463, IsaacPrng.anInt1091 - 17,
                        Class33.anInt599 - 357, -1, 77, 0,
                        Deque.anInt912,
                        Class65.aClass39_Sub5_Sub17_1136);
            }
            int i_46_ = (-Class65.aClass39_Sub5_Sub17_1136.anInt1994
                    + (Deque.anInt912 - 77));
            if (i_46_ < 0) {
                i_46_ = 0;
            }
            if (i_46_ > Deque.anInt912 - 77) {
                i_46_ = Deque.anInt912 - 77;
            }
            if (Node.anInt741 != i_46_) {
                Class14.aBoolean245 = true;
                Node.anInt741 = i_46_;
            }
        }
        if (Class39_Sub5_Sub14.anInt1912 == -1
                && Class39_Sub5_Sub4_Sub4.anInt2285 == 3) {
            Class65.aClass39_Sub5_Sub17_1136.anInt1994 = Class39_Sub14.anInt1511;
            int i_47_ = Class67.anInt1184 * 14 + 7;
            if (IsaacPrng.anInt1091 > 17 && IsaacPrng.anInt1091 < 560
                    && Class33.anInt599 > 332) {
                Class39_Sub4.method456((byte) 121, 463, IsaacPrng.anInt1091 - 17,
                        Class33.anInt599 - 357, -1, 77, 0,
                        i_47_,
                        Class65.aClass39_Sub5_Sub17_1136);
            }
            int i_48_ = Class65.aClass39_Sub5_Sub17_1136.anInt1994;
            if (i_48_ < 0) {
                i_48_ = 0;
            }
            if (i_47_ - 77 < i_48_) {
                i_48_ = i_47_ - 77;
            }
            if (i_48_ != Class39_Sub14.anInt1511) {
                Class39_Sub14.anInt1511 = i_48_;
                Class14.aBoolean245 = true;
            }
        }
        if (i != (Class39_Sub5_Sub14.anInt1912 ^ 0xffffffff)) {
            boolean bool = Class39_Sub4.method459(Class39_Sub5_Sub14.anInt1912,
                    (byte) 115);
            if (bool) {
                Class14.aBoolean245 = true;
            }
        }
        if (Class25.anInt459 == 3) {
            Class14.aBoolean245 = true;
        }
        if (Class30.anInt534 == 3) {
            Class14.aBoolean245 = true;
        }
        if (OndemandRequest.aClass3_1714 != null) {
            Class14.aBoolean245 = true;
        }
        if (Class39_Sub12.aBoolean1493 && Class37.anInt653 == 2) {
            Class14.aBoolean245 = true;
        }
        if (Class14.aBoolean245) {
            Class14.aBoolean245 = false;
            Class32.method325((byte) -85);
        }
        Widget.method761((byte) 45);
        if (ClientScript.anInt1703 != -1) {
            IsaacPrng.aBoolean1089 = true;
        }
        if (IsaacPrng.aBoolean1089) {
            if (ClientScript.anInt1703 != -1
                    && ClientScript.anInt1703 == Node.anInt728) {
                ClientScript.anInt1703 = -1;
                FrameBuffer.outgoingGameBuffer.putFrame(65);
                FrameBuffer.outgoingGameBuffer.putByte(Node.anInt728);
            }
            NameTable.aBoolean183 = true;
            IsaacPrng.aBoolean1089 = false;
            Class39_Sub5_Sub16.method748(Class39_Sub5_Sub14.anIntArray1914,
                    (StillGraphic.anInt2338
                    == -1),
                    (byte) 82,
                    (Class2.logicCycle % 20 >= 10
                    ? ClientScript.anInt1703 : -1),
                    Node.anInt728);
        }
        if (Class39_Sub5_Sub4_Sub4.aBoolean2253) {
            NameTable.aBoolean183 = true;
            Class39_Sub5_Sub4_Sub4.aBoolean2253 = false;
            ArchiveWorker.method1119(Cache.anInt118, Bzip2Block.anInt1051,
                    (Class39_Sub5_Sub14.p12fullFont),
                    41, NameTable.anInt177);
        }
        Npc.method520(-118, Class45.anInt856,
                Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301,
                Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275,
                NameTable.height);
        Class45.anInt856 = 0;
    }

    public JString getJstr() {
        int i_49_ = offset;
        while (payload[offset++] != 0) {
            /* empty */
        }
        return HashTable.createJstring(payload, i_49_,
                offset + (-i_49_ - 1));
    }

    public void getBytes(byte[] dest, int off, int len) {
        for (int i = off; i < off + len; i++) {
            dest[i] = payload[offset++];
        }
    }

    public int getSmartB() {
        int i_52_ = payload[offset] & 0xff;
        if (i_52_ >= 128) {
            return getUword() - 32768;
        }
        return getUbyte();
    }

    public void method827(int i, int i_53_) {
        payload[offset++] = (byte) (i >> 16);
        payload[offset++] = (byte) (i >> 24);
        payload[offset++] = (byte) i;
        payload[offset++] = (byte) (i >> i_53_);
    }

    public void applyRsa(BigInteger publicExponent, BigInteger modulus, int i) {
        int i_55_ = offset;
        offset = 0;
        byte[] is = new byte[i_55_];
        getBytes(is, 0, i_55_);
        BigInteger biginteger_56_ = new BigInteger(is);
        BigInteger biginteger_57_ = biginteger_56_.modPow(publicExponent, modulus);
        byte[] is_58_ = biginteger_57_.toByteArray();
        offset = 0;
        putByte(is_58_.length);
        putBytes(is_58_, 0, is_58_.length);
    }

    public Buffer(int i) {
        payload = Class4.getByteArray((byte) 68, i);
        offset = 0;
    }

    public Buffer(byte[] is) {
        payload = is;
        offset = 0;
    }

    public int method829() {
        offset += 4;
        return ((payload[offset - 3] & 0xff)
                + (payload[offset - 4] << 8 & 0xff00)
                + ((payload[offset - 1] << 16 & 0xff0000)
                + ((payload[offset - 2] & 0xff) << 24)));
    }

    public void putSmartB(int value) {
        if (value >= 0 && value < 128) {
            putByte(value);
        } else if (value >= 0 && value < 32768) {
            putWord(value + 32768);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void putWord(int value) {
        payload[offset++] = (byte) (value >> 8);
        payload[offset++] = (byte) value;
    }

    public void method832(int i, byte i_61_) {
        payload[offset++] = (byte) (i >> 8);
        payload[offset++] = (byte) (i + 128);
    }

    public int method833(byte i) {
        offset += 2;
        return (((payload[offset - 1] & 0xff) << 8) + (payload[offset - 2] - 128 & 0xff));
    }

    public void putJstr(byte i, JString jstr) {
        offset += jstr.method83(40, 0, payload, offset, jstr.getLength());
        payload[offset++] = (byte) 0;
    }

    static {
        aClass3_1360 = Class39_Sub5_Sub9.createJstring("(U(Y");
        aClass3_1354 = Class39_Sub5_Sub9.createJstring("Unable to find ");
        aClass3_1359 = Class39_Sub5_Sub9.createJstring("lila:");
        anInt1364 = 0;
        aClass3_1363 = Class39_Sub5_Sub9.createJstring("Benutzeroberfl-=che geladen)3");
        aClass3_1366 = Class39_Sub5_Sub9.createJstring("0(U");
        aClass3_1365 = aClass3_1354;
        aClass3_1370 = Class39_Sub5_Sub9.createJstring("null");
        aClass3_1369 = Class39_Sub5_Sub9.createJstring("T");
        anInt1368 = 0;
    }
}

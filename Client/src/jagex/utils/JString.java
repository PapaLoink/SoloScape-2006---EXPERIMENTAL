package jagex.utils;

/* Class3 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import jagex.io.JSocket;
import jagex.world.actors.Npc;
import jagex.world.actors.Player;
import jagex.io.FrameBuffer;
import jagex.io.Buffer;
import jagex.utils.Cache;
import jagex.utils.ArrayUtils;
import java.awt.FontMetrics;
import java.awt.Graphics;
import unpackaged.ArchiveWorker;
import jagex.io.BufferedFile;
import unpackaged.Class12;
import unpackaged.Class13;
import unpackaged.Class14;
import unpackaged.Class25;
import unpackaged.Class30;
import unpackaged.Class33;
import unpackaged.Class34;
import unpackaged.Class36;
import unpackaged.Class39_Sub10;
import unpackaged.Class39_Sub11;
import unpackaged.Class39_Sub13;
import unpackaged.Class39_Sub14;
import unpackaged.Class39_Sub4;
import unpackaged.Class39_Sub5_Sub11;
import unpackaged.Class39_Sub5_Sub14;
import unpackaged.Class39_Sub5_Sub16;
import unpackaged.Class39_Sub5_Sub4;
import unpackaged.Class39_Sub5_Sub6;
import unpackaged.Class39_Sub5_Sub9;
import unpackaged.Class4;
import unpackaged.Class41;
import unpackaged.Class43;
import unpackaged.Class48;
import unpackaged.Class53;
import unpackaged.Class55;
import unpackaged.Class67;
import unpackaged.ClientScript;
import jagex.world.actors.GroundItem;
import unpackaged.Interface1;
import unpackaged.ItemDefinition;
import unpackaged.JKeyListener;
import unpackaged.NameTable;
import jagex.world.actors.Projectile;
import unpackaged.RuntimeException_Sub1;
import jagex.world.actors.StillGraphic;
import unpackaged.Widget;

public class JString implements Interface1 {

    public boolean aBoolean1226 = true;
    public static Cache aClass7_1227;
    public int length;
    public static int anInt1229 = 0;
    public int hash;
    public static int anInt1231 = -1;
    public static boolean aBoolean1232 = true;
    public byte[] bytes;
    public static JString aClass3_1234;
    public static JString aClass3_1235;
    public static FrameBuffer aClass39_Sub6_Sub1_1236;
    public static JString aClass3_1237;
    public static JString aClass3_1238;
    public static int[] anIntArray1239;
    public static int anInt1240;
    /*synthetic*/ public static Class aClass1241;

    public static void method55(int i, JString class3, int i_0_,
            JString class3_1_, byte i_2_, int i_3_,
            int i_4_) {
        if (Class39_Sub5_Sub11.anInt1841 < 500) {
            if (class3_1_.getLength() <= 0) {
                Class33.aClass3Array601[Class39_Sub5_Sub11.anInt1841] = class3;
            } else {
                Class33.aClass3Array601[Class39_Sub5_Sub11.anInt1841] = Class39_Sub5_Sub11.method708((new JString[]{class3,
                            Class53.aClass3_953,
                            class3_1_}));
            }
            JKeyListener.anIntArray621[Class39_Sub5_Sub11.anInt1841] = i_4_;
            NameTable.anIntArray176[Class39_Sub5_Sub11.anInt1841] = i_3_;
            Class12.anIntArray196[Class39_Sub5_Sub11.anInt1841] = i;
            Class43.anIntArray820[Class39_Sub5_Sub11.anInt1841] = i_0_;
            Class39_Sub5_Sub11.anInt1841++;
        }
    }

    public int getHashCode() {
        int i_5_ = 0;
        for (int i_6_ = 0; i_6_ < length; i_6_++) {
            i_5_ = (bytes[i_6_] & 0xff) + ((i_5_ << 5) - i_5_);
        }
        return i_5_;
    }

    public JString method57(int i) {
        long l = method94(0);
        synchronized (aClass1241 != null ? aClass1241
                : (aClass1241 = method98("jagex.utils.JString"))) {
            if (Cache.aClass19_106 != null) {
                for (Class39_Sub4 class39_sub4 = ((Class39_Sub4) Cache.aClass19_106.fetch(l));
                        class39_sub4 != null;
                        class39_sub4 = (Class39_Sub4) Cache.aClass19_106.lookupLast()) {
                    if (isEqual(class39_sub4.aClass3_1325)) {
                        return class39_sub4.aClass3_1325;
                    }
                }
            } else {
                Cache.aClass19_106 = new HashTable(4096);
            }
            Class39_Sub4 class39_sub4 = new Class39_Sub4();
            aBoolean1226 = false;
            class39_sub4.aClass3_1325 = this;
            Cache.aClass19_106.put(l, class39_sub4);
        }
        return this;
    }

    public JString method58(boolean bool) {
        JString class3_7_ = new JString();
        class3_7_.length = length;
        class3_7_.bytes = new byte[length];
        boolean bool_8_ = bool;
        for (int i = 0; i < length; i++) {
            byte i_9_ = bytes[i];
            if (i_9_ >= 97 && i_9_ <= 122
                    || i_9_ >= -32 && i_9_ <= -2 && i_9_ != -9) {
                if (bool_8_) {
                    i_9_ -= 32;
                }
                bool_8_ = false;
            } else if (i_9_ >= 65 && i_9_ <= 90
                    || i_9_ >= -64 && i_9_ <= -34 && i_9_ != -41) {
                if (!bool_8_) {
                    i_9_ += 32;
                }
                bool_8_ = false;
            }
            if (i_9_ == 46 || i_9_ == 33 || i_9_ == 63) {
                bool_8_ = true;
            }
            class3_7_.bytes[i] = i_9_;
        }
        return class3_7_;
    }

    public JString method59(int i, int i_10_, int i_11_) {
        JString class3_12_ = new JString();
        class3_12_.bytes = new byte[-i + i_11_];
        class3_12_.length = i_11_ - i;
        ArrayUtils.arrayCopy(bytes, i, class3_12_.bytes, 0,
                class3_12_.length);
        return class3_12_;
    }

    public static boolean method60(int i, JString class3) {
        if (i != 21469) {
            method95(18);
        }
        if (class3 == null) {
            return false;
        }
        for (int i_13_ = 0; Class4.anInt62 > i_13_; i_13_++) {
            if (class3.method97(Projectile.aClass3Array2188[i_13_],
                    i - 21535)) {
                return true;
            }
        }
        if (class3.method97((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.aClass3_2521),
                -66)) {
            return true;
        }
        return false;
    }

    public boolean method61(boolean bool, int i) {
        if (bool != true) {
            return false;
        }
        if (i < 1 || i > 36) {
            i = 10;
        }
        boolean bool_14_ = false;
        boolean bool_15_ = false;
        int i_16_ = 0;
        for (int i_17_ = 0; i_17_ < length; i_17_++) {
            int i_18_ = bytes[i_17_] & 0xff;
            if (i_17_ == 0) {
                if (i_18_ == 45) {
                    bool_14_ = true;
                    continue;
                }
                if (i_18_ == 43) {
                    continue;
                }
            }
            if (i_18_ >= 48 && i_18_ <= 57) {
                i_18_ -= 48;
            } else if (i_18_ >= 65 && i_18_ <= 90) {
                i_18_ -= 55;
            } else {
                if (i_18_ < 97 || i_18_ > 122) {
                    return false;
                }
                i_18_ -= 87;
            }
            if (i_18_ >= i) {
                return false;
            }
            if (bool_14_) {
                i_18_ = -i_18_;
            }
            int i_19_ = i_18_ + i_16_ * i;
            if (i_16_ != i_19_ / i) {
                return false;
            }
            i_16_ = i_19_;
            bool_15_ = true;
        }
        return bool_15_;
    }

    public String toString() {
        throw new RuntimeException();
    }

    public static void method62(int i) {
        if (!Class13.aBooleanArray200[98]) {
            if (!Class13.aBooleanArray200[99]) {
                Class41.anInt773 /= 2;
            } else {
                Class41.anInt773 += (-Class41.anInt773 - 12) / 2;
            }
        } else {
            Class41.anInt773 += (-Class41.anInt773 + 12) / 2;
        }
        if (!Class13.aBooleanArray200[96]) {
            if (!Class13.aBooleanArray200[97]) {
                Class36.anInt642 /= 2;
            } else {
                Class36.anInt642 += (-Class36.anInt642 + 24) / 2;
            }
        } else {
            Class36.anInt642 += (-24 - Class36.anInt642) / 2;
        }
        Class34.anInt605 = Class36.anInt642 / 2 + Class34.anInt605 & 0x7ff;
        JSocket.anInt301 += Class41.anInt773 / 2;
        if (JSocket.anInt301 < 128) {
            JSocket.anInt301 = 128;
        }
        if (JSocket.anInt301 > 383) {
            JSocket.anInt301 = 383;
        }
        int i_20_ = (RuntimeException_Sub1.anInt1216
                + Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301);
        int i_21_ = (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275
                + Buffer.anInt1361);
        if (Class41.anInt765 - i_20_ < -500 || -i_20_ + Class41.anInt765 > 500
                || Class39_Sub10.anInt1431 - i_21_ < -500
                || -i_21_ + Class39_Sub10.anInt1431 > 500) {
            Class41.anInt765 = i_20_;
            Class39_Sub10.anInt1431 = i_21_;
        }
        if (Class41.anInt765 != i_20_) {
            Class41.anInt765 += (i_20_ - Class41.anInt765) / 16;
        }
        if (i_21_ != Class39_Sub10.anInt1431) {
            Class39_Sub10.anInt1431 += (i_21_ - Class39_Sub10.anInt1431) / 16;
        }
        int i_22_ = Class41.anInt765 >> 7;
        int i_23_ = Class39_Sub10.anInt1431 >> 7;
        int i_24_ = Class14.method212(Class39_Sub10.anInt1431, 9990,
                NameTable.height, Class41.anInt765);
        int i_25_ = 0;
        if (i_22_ > 3 && i_23_ > 3 && i_22_ < 100 && i_23_ < 100) {
            for (int i_26_ = i_22_ - 4; i_26_ <= i_22_ + 4; i_26_++) {
                for (int i_27_ = i_23_ - 4; i_27_ <= i_23_ + 4; i_27_++) {
                    int i_28_ = NameTable.height;
                    if (i_28_ < 3
                            && (Class55.tileFlags[1][i_26_][i_27_]
                            & 0x2) == 2) {
                        i_28_++;
                    }
                    int i_29_ = i_24_ - (Class67.heightMap[i_28_][i_26_][i_27_]);
                    if (i_25_ < i_29_) {
                        i_25_ = i_29_;
                    }
                }
            }
        }
        int i_30_ = i_25_ * 192;
        if (i_30_ > 98048) {
            i_30_ = 98048;
        }
        if (i_30_ < 32768) {
            i_30_ = 32768;
        }
        if (i_30_ > Cache.anInt101) {
            Cache.anInt101 += (i_30_ - Cache.anInt101) / 24;
        } else if (i_30_ < Cache.anInt101) {
            Cache.anInt101 += (-Cache.anInt101 + i_30_) / 80;
        }
    }

    public JString method63(int i, byte i_31_, JString class3_32_) {
        if (!aBoolean1226) {
            throw new IllegalArgumentException();
        }
        if (i > length) {
            throw new IllegalArgumentException();
        }
        hash = 0;
        if (bytes.length < i + class3_32_.length) {
            int i_33_;
            for (i_33_ = 1; class3_32_.length + i > i_33_; i_33_ += i_33_) {
                /* empty */
            }
            byte[] is = new byte[i_33_];
            ArrayUtils.arrayCopy(bytes, 0, is, 0, length);
            bytes = is;
        }
        ArrayUtils.arrayCopy(class3_32_.bytes, 0, bytes, i,
                class3_32_.length);
        if (i_31_ != 84) {
            method93((byte) -32, null, -24);
        }
        if (i + class3_32_.length > length) {
            length = i + class3_32_.length;
        }
        return this;
    }

    public static void method64(byte i) {
        aClass3_1234 = null;
        aClass3_1238 = null;
        aClass3_1235 = null;
        aClass39_Sub6_Sub1_1236 = null;
        aClass7_1227 = null;
        anIntArray1239 = null;
        aClass3_1237 = null;
    }

    public boolean method65(JString class3_34_, boolean bool) {
        if (class3_34_.length > length) {
            return false;
        }
        for (int i = 0; i < class3_34_.length; i++) {
            if (bytes[i] != class3_34_.bytes[i]) {
                return false;
            }
        }
        return true;
    }

    public int getStringWidth(FontMetrics fontMetrics) {
        String string;
        try {
            string = new String(bytes, 0, length, "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException unsupportedencodingexception) {
            string = new String(bytes, 0, length);
        }
        return fontMetrics.stringWidth(string);
    }

    public int method67(int i, byte i_35_) {
        boolean bool = false;
        boolean bool_36_ = false;
        int i_37_ = 0;
        if (i_35_ < 72) {
            method59(89, -118, 87);
        }
        if (i < 1 || i > 36) {
            i = 10;
        }
        for (int i_38_ = 0; i_38_ < length; i_38_++) {
            int i_39_ = bytes[i_38_] & 0xff;
            if (i_38_ == 0) {
                if (i_39_ == 45) {
                    bool_36_ = true;
                    continue;
                }
                if (i_39_ == 43) {
                    continue;
                }
            }
            if (i_39_ >= 48 && i_39_ <= 57) {
                i_39_ -= 48;
            } else if (i_39_ < 65 || i_39_ > 90) {
                if (i_39_ >= 97 && i_39_ <= 122) {
                    i_39_ -= 87;
                } else {
                    throw new NumberFormatException();
                }
            } else {
                i_39_ -= 55;
            }
            if (i_39_ >= i) {
                throw new NumberFormatException();
            }
            if (bool_36_) {
                i_39_ = -i_39_;
            }
            int i_40_ = i_39_ + i * i_37_;
            if (i_40_ / i != i_37_) {
                throw new NumberFormatException();
            }
            i_37_ = i_40_;
            bool = true;
        }
        if (!bool) {
            throw new NumberFormatException();
        }
        return i_37_;
    }

    public int findChar(int ch) {
        byte value = (byte) ch;
        for (int i = 0; i < length; i++) {
            if (value == bytes[i]) {
                return i;
            }
        }
        return -1;
    }

    public JString method69(byte i) {
        int i_44_ = 0;
        int i_45_ = length;
        for (/**/; length > i_44_; i_44_++) {
            if ((bytes[i_44_] < 0 || bytes[i_44_] > 32)
                    && (bytes[i_44_] & 0xff) != 160) {
                break;
            }
        }
        for (/**/;
                i_44_ < i_45_ && ((bytes[i_45_ - 1] >= 0
                && bytes[i_45_ - 1] <= 32)
                || (bytes[i_45_ - 1] & 0xff) == 160);
                i_45_--) {
            /* empty */
        }
        if (i_44_ == 0 && length == i_45_) {
            return this;
        }
        JString jstr = new JString();
        jstr.length = -i_44_ + i_45_;
        jstr.bytes = new byte[jstr.length];
        for (int i_47_ = 0; i_47_ < jstr.length; i_47_++) {
            jstr.bytes[i_47_] = bytes[i_47_ + i_44_];
        }
        return jstr;
    }

    public int getLength() {
        return length;
    }

    public JString concatChar(int ch) {
        if (ch <= 0 || ch > 255) {
            throw new IllegalArgumentException("invalid char");
        }
        if (!aBoolean1226) {
            throw new IllegalArgumentException();
        }
        hash = 0;
        if (length == bytes.length) {
            int i_49_;
            for (i_49_ = 1; length >= i_49_; i_49_ += i_49_) {
                /* empty */
            }
            byte[] is = new byte[i_49_];
            ArrayUtils.arrayCopy(bytes, 0, is, 0, length);
            bytes = is;
        }
        bytes[length++] = (byte) ch;
        return this;
    }

    public long encodeBase37() {
        long l = 0L;
        for (int i = 0; length > i; i++) {
            if (i >= 12) {
                break;
            }
            l *= 37L;
            byte b = bytes[i];
            if (b < 65 || b > 90) {
                if (b >= 97 && b <= 122) {
                    l += (long) (b + 1 - 97);
                } else if (b >= 48 && b <= 57) {
                    l += (long) (b - 21);
                }
            } else {
                l += (long) (1 + b - 65);
            }
        }
        for (/**/; l % 37L == 0L && l != 0L; l /= 37L) {
            /* empty */
        }
        return l;
    }

    public JString formatUsername() {
        JString jstr = new JString();
        jstr.length = length;
        jstr.bytes = new byte[length];
        boolean capitalize = true;
        for (int i_53_ = 0; i_53_ < length; i_53_++) {
            byte i_54_ = bytes[i_53_];
            if (i_54_ == 95) {
                jstr.bytes[i_53_] = (byte) 32;
                capitalize = true;
            } else if (i_54_ >= 97 && i_54_ <= 122 && capitalize) {
                jstr.bytes[i_53_] = (byte) (i_54_ - 32);
                capitalize = false;
            } else {
                jstr.bytes[i_53_] = i_54_;
                capitalize = false;
            }
        }
        return jstr;
    }

    public JString formatPassword() {
        JString jstr = new JString();
        jstr.length = length;
        jstr.bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            jstr.bytes[i] = (byte) 42;
        }
        return jstr;
    }

    public JString method75() {
        if (!aBoolean1226) {
            throw new IllegalArgumentException();
        }
        hash = 0;
        if (bytes.length != length) {
            byte[] is = new byte[length];
            ArrayUtils.arrayCopy(bytes, 0, is, 0, length);
            bytes = is;
        }
        return this;
    }

    public int method76(byte i) {
        return method67(10, (byte) 84);
    }

    public JString method77() {
        JString str = new JString();
        str.length = length;
        str.bytes = new byte[length];
        for (int i_58_ = 0; length > i_58_; i_58_++) {
            byte i_59_ = bytes[i_58_];
            if (i_59_ >= 65 && i_59_ <= 90 || i_59_ >= -64 && i_59_ <= -34 && i_59_ != -41) {
                i_59_ += 32;
            }
            str.bytes[i_58_] = i_59_;
        }
        return str;
    }

    public byte[] method78(int i) {
        byte[] is = new byte[length];
        ArrayUtils.arrayCopy(bytes, 0, is, 0, length);
        return is;
    }

    public void draw(Graphics graphics, int positionX, int positionY) {
        String string;
        try {
            string = new String(bytes, 0, length, "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException unsupportedencodingexception) {
            string = new String(bytes, 0, length);
        }
        graphics.drawString(string, positionX, positionY);
    }

    public int method80(int i, JString class3_62_) {
        return method93((byte) 72, class3_62_, 0);
    }

    public JString method81(int i) {
        JString class3_63_ = new JString();
        class3_63_.bytes = new byte[12];
        int i_64_ = 0;
        class3_63_.length = 0;
        for (int i_65_ = 0; i_65_ < length; i_65_++) {
            if (bytes[i_65_] < 65 || bytes[i_65_] > 90) {
                if (bytes[i_65_] >= 97 && bytes[i_65_] <= 122
                        || (bytes[i_65_] >= 48
                        && bytes[i_65_] <= 57)) {
                    class3_63_.bytes[i_64_++] = bytes[i_65_];
                    class3_63_.length = i_64_;
                } else if (i_64_ > 0) {
                    class3_63_.bytes[i_64_++] = (byte) 95;
                }
            } else {
                class3_63_.bytes[i_64_++] = (byte) (bytes[i_65_] - 65 + 97);
                class3_63_.length = i_64_;
            }
            if (i_64_ == 12) {
                break;
            }
        }
        return class3_63_;
    }

    public JString method82(int i, JString class3_66_) {
        if (!aBoolean1226) {
            throw new IllegalArgumentException();
        }
        hash = 0;
        if (bytes.length < class3_66_.length + length) {
            int i_67_;
            for (i_67_ = 1; class3_66_.length + length > i_67_;
                    i_67_ += i_67_) {
                /* empty */
            }
            byte[] is = new byte[i_67_];
            ArrayUtils.arrayCopy(bytes, 0, is, 0, length);
            bytes = is;
        }
        ArrayUtils.arrayCopy(class3_66_.bytes, 0, bytes,
                length, class3_66_.length);
        length += class3_66_.length;
        return this;
    }

    public int method83(int i, int i_68_, byte[] is, int i_69_, int i_70_) {
        ArrayUtils.arrayCopy(bytes, i_68_, is, i_69_, -i_68_ + i_70_);
        return -i_68_ + i_70_;
    }

    @Override
    public boolean equals(Object object) {
        throw new RuntimeException();
    }

    public boolean method84(JString class3_71_, int i) {
        if (length < class3_71_.length) {
            return false;
        }
        int i_72_ = length - class3_71_.length;
        for (int i_73_ = i; i_73_ < class3_71_.length; i_73_++) {
            if (bytes[i_73_ + i_72_]
                    != class3_71_.bytes[i_73_]) {
                return false;
            }
        }
        return true;
    }

    public JString method85(int i, int i_74_) {
        return method59(i_74_, -1, length);
    }

    public JString createConcatChar(int ch) {
        if (ch <= 0 || ch > 255) {
            throw new IllegalArgumentException("invalid char");
        }
        JString str = new JString();
        str.bytes = new byte[length + 1];
        str.length = length + 1;
        ArrayUtils.arrayCopy(bytes, 0, str.bytes, 0, length);
        str.bytes[length] = (byte) ch;
        return str;
    }

    public int hashCode() {
        return getHashCode();
    }

    public boolean method87(int i) {
        if (i != 22415) {
            method61(true, -17);
        }
        return method61(true, 10);
    }

    public static void printArgsUsage() {
        System.out.println("Usage: worldid, [live/office/local], [live/rc/wip], [lowmem/highmem], [free/members], [english/german]");
        System.exit(1);
    }

    public boolean isEqual(JString compareStr) {
        if (compareStr == null) {
            return false;
        }
        if (compareStr.length != length) {
            return false;
        }
        if (!aBoolean1226 || !compareStr.aBoolean1226) {
            if (hash == 0) {
                hash = getHashCode();
                if (hash == 0) {
                    hash = 1;
                }
            }
            if (compareStr.hash == 0) {
                compareStr.hash = compareStr.getHashCode();
                if (compareStr.hash == 0) {
                    compareStr.hash = 1;
                }
            }
            if (compareStr.hash != hash) {
                return false;
            }
        }
        for (int i = 0; i < length; i++) {
            if (bytes[i] != compareStr.bytes[i]) {
                return false;
            }
        }
        return true;
    }

    public int charAt(int position) {
        return bytes[position] & 0xff;
    }

    public static void method91(byte i) {
        for (int i_80_ = 0; i_80_ < ArchiveWorker.anInt1210; i_80_++) {
            int i_81_ = Class39_Sub5_Sub4.anIntArray1734[i_80_];
            Npc class39_sub5_sub4_sub4_sub1 = (GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_81_]);
            if (class39_sub5_sub4_sub4_sub1 != null) {
                ClientScript.method480((byte) 116,
                        class39_sub5_sub4_sub4_sub1,
                        (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492.anInt1870));
            }
        }
    }

    public int method92(byte i, int i_82_) {
        return findChar(i_82_);
    }

    public int method93(byte i, JString class3_83_, int i_84_) {
        int[] is = new int[256];
        int[] is_85_ = new int[class3_83_.length];
        int[] is_86_ = new int[class3_83_.length];
        for (int i_87_ = 0; i_87_ < is.length; i_87_++) {
            is[i_87_] = class3_83_.length;
        }
        for (int i_88_ = 1; i_88_ <= class3_83_.length; i_88_++) {
            is_86_[i_88_ - 1] = (class3_83_.length << 1) - i_88_;
            is[Class34.method342(class3_83_.bytes[i_88_ - 1], 255)] = -i_88_ + class3_83_.length;
        }
        int i_89_ = class3_83_.length + 1;
        int i_90_ = class3_83_.length;
        while (i_90_ > 0) {
            is_85_[i_90_ - 1] = i_89_;
            for (/**/;
                    (i_89_ <= class3_83_.length
                    && (class3_83_.bytes[i_89_ - 1]
                    != class3_83_.bytes[i_90_ - 1]));
                    i_89_ = is_85_[i_89_ - 1]) {
                if (is_86_[i_89_ - 1] >= -i_90_ + class3_83_.length) {
                    is_86_[i_89_ - 1] = class3_83_.length - i_90_;
                }
            }
            i_90_--;
            i_89_--;
        }
        int i_91_ = i_89_;
        i_89_ = class3_83_.length + 1 - i_91_;
        int i_92_ = 1;
        int i_93_ = 0;
        int i_94_ = 1;
        while (i_94_ <= i_89_) {
            is_85_[i_94_ - 1] = i_93_;
            for (/**/;
                    i_93_ >= 1 && (class3_83_.bytes[i_93_ - 1]
                    != class3_83_.bytes[i_94_ - 1]);
                    i_93_ = is_85_[i_93_ - 1]) {
                /* empty */
            }
            i_94_++;
            i_93_++;
        }
        while (i_91_ < class3_83_.length) {
            for (int i_95_ = i_92_; i_95_ <= i_91_; i_95_++) {
                if (class3_83_.length + i_91_ - i_95_ <= is_86_[i_95_ - 1]) {
                    is_86_[i_95_ - 1] = i_91_ + class3_83_.length - i_95_;
                }
            }
            i_92_ = i_91_ + 1;
            i_91_ = i_89_ + i_91_ - is_85_[i_89_ - 1];
            i_89_ = is_85_[i_89_ - 1];
        }
        int i_96_ = i_84_ + class3_83_.length - 1;
        if (i < 6) {
            aClass39_Sub6_Sub1_1236 = null;
        }
        int i_97_;
        for (/**/; i_96_ < length;
                i_96_ += Math.max(is[bytes[i_96_] & 0xff],
                        is_86_[i_97_])) {
            for (i_97_ = class3_83_.length - 1;
                    i_97_ >= 0 && (class3_83_.bytes[i_97_]
                    == bytes[i_96_]);
                    i_97_--) {
                i_96_--;
            }
            if (i_97_ == -1) {
                return i_96_ + 1;
            }
        }
        return -1;
    }

    public long method94(int i) {
        long l = (long) i;
        for (int i_98_ = 0; length > i_98_; i_98_++) {
            l = (long) (bytes[i_98_] & 0xff) + (l << 5) + -l;
        }
        return l;
    }

    public static void method95(int i) {
        if (Class30.anInt534 == 0) {
            Class39_Sub5_Sub11.anInt1841 = 1;
            JKeyListener.anIntArray621[0] = 1002;
            Class33.aClass3Array601[0] = Player.aClass3_2531;
            if (SubNode.anInt1348 != -1) {
                Class48.anInt904 = -1;
                Projectile.anInt2197 = -1;
                Class25.method283(0, IsaacPrng.anInt1091, Class33.anInt599,
                        (byte) 124, 0, 0, SubNode.anInt1348,
                        765, 503);
                Class39_Sub10.anInt1417 = Class48.anInt904;
                Class39_Sub5_Sub6.anInt1760 = Projectile.anInt2197;
            } else {
                BufferedFile.method235((byte) 126);
                Projectile.anInt2197 = -1;
                Class48.anInt904 = -1;
                if (IsaacPrng.anInt1091 > 4 && Class33.anInt599 > 4
                        && IsaacPrng.anInt1091 < 516 && Class33.anInt599 < 338) {
                    if (Class39_Sub11.anInt1478 != -1) {
                        Class25.method283(4, IsaacPrng.anInt1091,
                                Class33.anInt599, (byte) 126, 4, 0,
                                Class39_Sub11.anInt1478, 516, 338);
                    } else {
                        Class39_Sub13.method875(-65);
                    }
                }
                Class39_Sub5_Sub6.anInt1760 = Projectile.anInt2197;
                Class39_Sub10.anInt1417 = Class48.anInt904;
                Projectile.anInt2197 = -1;
                Class48.anInt904 = -1;
                boolean bool = false;
                if (IsaacPrng.anInt1091 > 553 && Class33.anInt599 > 205
                        && IsaacPrng.anInt1091 < 743 && Class33.anInt599 < 466) {
                    if (StillGraphic.anInt2338 == -1) {
                        if (Class39_Sub5_Sub14.anIntArray1914[Node.anInt728]
                                != -1) {
                            Class25.method283(553, IsaacPrng.anInt1091,
                                    Class33.anInt599, (byte) 123,
                                    205, 1,
                                    (Class39_Sub5_Sub14.anIntArray1914[Node.anInt728]),
                                    743, 466);
                        }
                    } else {
                        Class25.method283(553, IsaacPrng.anInt1091,
                                Class33.anInt599, (byte) 114, 205, 1,
                                StillGraphic.anInt2338,
                                743, 466);
                    }
                }
                if (Class12.anInt194 != Class48.anInt904) {
                    Class39_Sub14.aBoolean1520 = true;
                    Class12.anInt194 = Class48.anInt904;
                }
                Class48.anInt904 = -1;
                if (Projectile.anInt2197
                        != Class39_Sub10.anInt1440) {
                    Class39_Sub10.anInt1440 = Projectile.anInt2197;
                    Class39_Sub14.aBoolean1520 = true;
                }
                Projectile.anInt2197 = -1;
                if (unpackaged.GrandExchangeWidgets.searching || (IsaacPrng.anInt1091 > 17 && Class33.anInt599 > 357
                        && IsaacPrng.anInt1091 < 496 && Class33.anInt599 < 453)) {
                    if (unpackaged.GrandExchangeWidgets.searching) {
                        unpackaged.GrandExchangeSearch.menuScreen(IsaacPrng.anInt1091, Class33.anInt599);
                    } else if (Class39_Sub5_Sub14.anInt1912 == -1) {
                        if (IsaacPrng.anInt1095 == -1) {
                            if (Class33.anInt599 < 434
                                    && IsaacPrng.anInt1091 < 426) {
                                ItemDefinition.method471(-2,
                                        (Class33.anInt599
                                        - 357),
                                        (IsaacPrng.anInt1091
                                        - 17));
                            }
                        } else {
                            Class25.method283(17, IsaacPrng.anInt1091,
                                    Class33.anInt599, (byte) 112,
                                    357, 3, IsaacPrng.anInt1095, 496,
                                    453);
                        }
                    } else {
                        Class25.method283(17, IsaacPrng.anInt1091,
                                Class33.anInt599, (byte) 123, 357, 2,
                                Class39_Sub5_Sub14.anInt1912, 496,
                                453);
                    }
                }
                if ((Class39_Sub5_Sub14.anInt1912 != -1
                        || IsaacPrng.anInt1095 != -1)
                        && Widget.anInt2071 != Class48.anInt904) {
                    Widget.anInt2071 = Class48.anInt904;
                    Class14.aBoolean245 = true;
                }
                if ((Class39_Sub5_Sub14.anInt1912 != -1
                        || IsaacPrng.anInt1095 != -1)
                        && (Projectile.anInt2197
                        != Class39_Sub5_Sub16.anInt1982)) {
                    Class14.aBoolean245 = true;
                    Class39_Sub5_Sub16.anInt1982 = Projectile.anInt2197;
                }
                while (!bool) {
                    bool = true;
                    for (int i_99_ = 0;
                            i_99_ < Class39_Sub5_Sub11.anInt1841 - 1; i_99_++) {
                        if (JKeyListener.anIntArray621[i_99_] < 1000
                                && JKeyListener.anIntArray621[i_99_ + 1] > 1000) {
                            bool = false;
                            JString class3 = Class33.aClass3Array601[i_99_];
                            Class33.aClass3Array601[i_99_] = Class33.aClass3Array601[i_99_ + 1];
                            Class33.aClass3Array601[i_99_ + 1] = class3;
                            int i_100_ = JKeyListener.anIntArray621[i_99_];
                            JKeyListener.anIntArray621[i_99_] = JKeyListener.anIntArray621[i_99_ + 1];
                            JKeyListener.anIntArray621[i_99_ + 1] = i_100_;
                            i_100_ = Class12.anIntArray196[i_99_];
                            Class12.anIntArray196[i_99_] = Class12.anIntArray196[i_99_ + 1];
                            Class12.anIntArray196[i_99_ + 1] = i_100_;
                            i_100_ = Class43.anIntArray820[i_99_];
                            Class43.anIntArray820[i_99_] = Class43.anIntArray820[i_99_ + 1];
                            Class43.anIntArray820[i_99_ + 1] = i_100_;
                            i_100_ = NameTable.anIntArray176[i_99_];
                            NameTable.anIntArray176[i_99_] = NameTable.anIntArray176[i_99_ + 1];
                            NameTable.anIntArray176[i_99_ + 1] = i_100_;
                        }
                    }
                }
                unpackaged.ModHook.shiftDropMenu();
            }
        }
    }

    public int method96(int i, JString class3_101_) {
        int i_102_;
        if (class3_101_.length >= length) {
            i_102_ = length;
        } else {
            i_102_ = class3_101_.length;
        }
        for (int i_103_ = 0; i_103_ < i_102_; i_103_++) {
            if (class3_101_.bytes[i_103_] > bytes[i_103_]) {
                return -1;
            }
            if (bytes[i_103_] > class3_101_.bytes[i_103_]) {
                return 1;
            }
        }
        if (class3_101_.length > length) {
            return -1;
        }
        if (length > class3_101_.length) {
            return 1;
        }
        return 0;
    }

    public boolean method97(JString class3_104_, int i) {
        if (class3_104_ == null) {
            return false;
        }
        if (class3_104_.length != length) {
            return false;
        }
        for (int i_105_ = 0; length > i_105_; i_105_++) {
            byte i_106_ = bytes[i_105_];
            byte i_107_ = class3_104_.bytes[i_105_];
            if (i_107_ >= 65 && i_107_ <= 90
                    || i_107_ >= -64 && i_107_ <= -34 && i_107_ != -41) {
                i_107_ += 32;
            }
            if (i_106_ >= 65 && i_106_ <= 90
                    || i_106_ >= -64 && i_106_ <= -34 && i_106_ != -41) {
                i_106_ += 32;
            }
            if (i_107_ != i_106_) {
                return false;
            }
        }
        return true;
    }

    /*synthetic*/ public static Class method98(String string) {
        try {
            return Class.forName(string);
        } catch (ClassNotFoundException classnotfoundexception) {
            throw new NoClassDefFoundError(classnotfoundexception.getMessage());
        }
    }

    static {
        aClass7_1227 = new Cache(64);
        aClass3_1235 = Class39_Sub5_Sub9.createJstring("Unexpected server response");
        aClass3_1234 = aClass3_1235;
        aClass39_Sub6_Sub1_1236 = new FrameBuffer(5000);
        aClass3_1237 = Class39_Sub5_Sub9.createJstring("@cya@");
        anIntArray1239 = new int[4000];
        aClass3_1238 = Class39_Sub5_Sub9.createJstring("Schrifts-=tze geladen)3");
    }
}

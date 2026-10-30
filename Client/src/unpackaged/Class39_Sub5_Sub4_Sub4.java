package unpackaged;

/* Class39_Sub5_Sub4_Sub4 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import jagex.graphics.BitmapFont;
import jagex.utils.Huffmans;
import jagex.world.actors.GroundItem;
import jagex.graphics.JImage;
import jagex.graphics.AbstractImage;
import jagex.world.actors.StillGraphic;
import jagex.world.actors.Projectile;
import jagex.utils.SubNode;
import jagex.utils.Node;
import jagex.utils.IsaacPrng;
import jagex.utils.JString;
import jagex.io.JSocket;
import jagex.world.actors.Npc;
import jagex.world.actors.Player;
import jagex.world.map.TraversalMap;
import jagex.utils.Queue;
import jagex.utils.Deque;
import jagex.io.FrameBuffer;
import jagex.io.Buffer;
import jagex.utils.Cache;
import java.util.Date;

public abstract class Class39_Sub5_Sub4_Sub4 extends Class39_Sub5_Sub4 {

    public int anInt2250;
    public int anInt2251;
    public int anInt2252;
    public static boolean aBoolean2253 = false;
    public int anInt2254;
    public int[] anIntArray2255 = new int[10];
    public int anInt2256;
    public int anInt2257;
    public int[] anIntArray2258;
    public int anInt2259;
    public int anInt2260;
    public int anInt2261;
    public int anInt2262;
    public int anInt2263;
    public int anInt2264;
    public int anInt2265;
    public int anInt2266;
    public int anInt2267;
    public int anInt2268;
    public int anInt2269;
    public int anInt2270;
    public int anInt2271;
    public int anInt2272;
    public int[] anIntArray2273;
    public int anInt2274;
    public int anInt2275;
    public int ipX, ipY, ipSaveX, ipSaveY;                  // Interp: position at the previous tick, and the real one during a blended draw
    public boolean ipBlended;
    public int anInt2276;
    public int anInt2277;
    public static JString aClass3_2278 = Class39_Sub5_Sub9.createJstring("Name des Gegenstands eingeben:");
    public int anInt2279;
    public int anInt2280;
    public static JString aClass3_2281;
    public int anInt2282;
    public int anInt2283;
    public boolean[] aBooleanArray2284;
    public static int anInt2285 = 0;
    public int[] anIntArray2286;
    public int anInt2287;
    public int anInt2288;
    public static JString aClass3_2289;
    public int anInt2290;
    public int anInt2291;
    public int anInt2292;
    public int anInt2293;
    public int anInt2294;
    public JString aClass3_2295;
    public int anInt2296;
    public int anInt2297;
    public boolean aBoolean2298;
    public static int anInt2299;
    public int anInt2300;
    public int anInt2301;
    public static JString aClass3_2302;
    public int anInt2303;
    public int anInt2304;
    public int anInt2305;
    public int anInt2306;
    public static JString aClass3_2307;
    public int anInt2308;
    public static JString aClass3_2309;
    public static JString aClass3_2310;
    public int anInt2311;
    public int anInt2312;
    public static int anInt2313;
    public int[] anIntArray2314;
    public static int anInt2315;
    public int anInt2316;
    public int anInt2317;
    public int anInt2318;
    public static JString aClass3_2319;

    public static void method507(int i) {
        aClass3_2278 = null;
        aClass3_2319 = null;
        aClass3_2281 = null;
        aClass3_2289 = null;
        aClass3_2302 = null;
        aClass3_2307 = null;
        aClass3_2309 = null;
        aClass3_2310 = null;
    }

    public void method508(int i, boolean bool, byte i_0_, int i_1_) {
        if (anInt2268 != -1
                && Class62_Sub1.method1064(anInt2268, (byte) 54).anInt1837 == 1) {
            anInt2268 = -1;
        }
        if (!bool) {
            int i_2_ = -anIntArray2314[0] + i_1_;
            int i_3_ = -anIntArray2255[0] + i;
            if (i_2_ >= -8 && i_2_ <= 8 && i_3_ >= -8 && i_3_ <= 8) {
                if (anInt2312 < 9) {
                    anInt2312++;
                }
                for (int i_4_ = anInt2312; i_4_ > 0; i_4_--) {
                    anIntArray2314[i_4_] = anIntArray2314[i_4_ - 1];
                    anIntArray2255[i_4_] = anIntArray2255[i_4_ - 1];
                    aBooleanArray2284[i_4_] = aBooleanArray2284[i_4_ - 1];
                }
                anIntArray2314[0] = i_1_;
                aBooleanArray2284[0] = false;
                anIntArray2255[0] = i;
                return;
            }
        }
        anIntArray2314[0] = i_1_;
        anInt2312 = 0;
        anInt2254 = 0;
        anIntArray2255[0] = i;
        anInt2274 = 0;
        anInt2301 = anIntArray2314[0] * 128 + anInt2297 * 64;
        anInt2275 = anIntArray2255[0] * 128 + anInt2297 * 64;
    }

    public static void method509(byte i) {
        if (IsaacPrng.aBoolean1100 && i == 39) {
            Widget class39_sub5_sub17 = Class62_Sub2.method1081(Class41.anInt775, Class31.anInt570,
                    i ^ 0x27);
            if (class39_sub5_sub17 != null
                    && class39_sub5_sub17.anObjectArray2004 != null) {
                executeClientScript(0, class39_sub5_sub17.anObjectArray2004, null, 111,
                        0, class39_sub5_sub17, 0);
            }
            IsaacPrng.aBoolean1100 = false;
        }
    }

    public boolean method510(byte i) {
        return false;
    }

    public static void method511(byte i) {
        Class30.aLong536 = 0L;
        Cache.aClass31_123.anInt561 = 0;
        Class46.anInt882 = 0;
        Class39_Sub5_Sub4_Sub2.anInt2220 = 0;
        OndemandRequest.anInt1724 = 0;
        ArchiveRequest.anInt1408 = 0;
        Class39_Sub12.anInt1490 = 0;
        Class39_Sub5_Sub16.anInt1976 = 0;
        Class4.anInt65 = 0;
        Class4.anInt60 = 0;
        Class26.anInt482 = 0;
        Class39_Sub11.anInt1473 = 0;
        if (i != 95) {
            method507(-79);
        }
        Class39_Sub5_Sub12.anInt1848 = 0;
        Class39_Sub5_Sub6.anInt1759 = 0;
        Class34.anInt604 = 0;
        Projectile.anInt2203 = 0;
        JString.aBoolean1232 = true;
        Class43.aBoolean802 = true;
        FileLoader.method174(-122);
        FrameBuffer.outgoingGameBuffer.offset = 0;
        Class39_Sub12.aBoolean1493 = false;
        Class39_Sub5_Sub11.anInt1827 = 0;
        ScriptState.anInt448 = -1;
        Class4.frameId = -1;
        JMouseListener.anInt787 = 0;
        FileLoader.anInt1283 = -1;
        Class39_Sub5_Sub11.gameBuffer.offset = 0;
        Class39_Sub7.anInt1380 = 0;
        Class63.anInt1117 = -1;
        Class39_Sub5_Sub11.anInt1841 = 0;
        ClientScript.anInt1692 = 0;
        StillGraphic.method535((byte) 61, 0);
        for (int i_5_ = 0; i_5_ < 100; i_5_++) {
            Class2.aClass3Array52[i_5_] = null;
        }
        ArchiveRequest.anInt1411 = -1;
        Class13.anInt208 = 0;
        Class34.anInt605 = (int) (Math.random() * 20.0) - 10 & 0x7ff;
        ArchiveWorker.anInt1210 = 0;
        RuntimeException_Sub1.anInt1216 = (int) (Math.random() * 100.0) - 50;
        Class39_Sub7.anInt1386 = (int) (Math.random() * 30.0) - 20;
        ArchiveRequest.anInt1401 = (int) (Math.random() * 120.0) - 60;
        SubNode.anInt1344 = (int) (Math.random() * 80.0) - 40;
        ArchiveRequest.anInt1407 = 0;
        Projectile.anInt2180 = 0;
        IsaacPrng.aBoolean1100 = false;
        TraversalMap.anInt515 = 0;
        Class30.anInt544 = 0;
        Buffer.anInt1361 = (int) (Math.random() * 110.0) - 55;
        Class39_Sub5_Sub7.minimapState = 0;
        for (int i_6_ = 0; i_6_ < 2048; i_6_++) {
            Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_6_] = null;
            IsaacPrng.aClass39_Sub6Array1104[i_6_] = null;
        }
        for (int i_7_ = 0; i_7_ < 32768; i_7_++) {
            GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_7_] = null;
        }
        Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109 = Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[2047] = new Player();
        Class23.projectiles.removeAll(i ^ ~0x5d);
        RuntimeException_Sub1.aClass49_1217.removeAll(i - 98);
        for (int i_8_ = 0; i_8_ < 4; i_8_++) {
            for (int i_9_ = 0; i_9_ < 104; i_9_++) {
                for (int i_10_ = 0; i_10_ < 104; i_10_++) {
                    Class20.groundItems[i_8_][i_9_][i_10_] = null;
                }
            }
        }
        Class15.aClass49_278 = new Deque();
        Class14.anInt232 = 0;
        Class4.anInt62 = 0;
        for (int i_11_ = 0; ObjectDefinition.anInt1923 > i_11_; i_11_++) {
            Class39_Sub5_Sub16 class39_sub5_sub16 = Class62.method1056(i_11_, 111);
            if (class39_sub5_sub16 != null
                    && class39_sub5_sub16.anInt1978 == 0) {
                Class39_Sub5_Sub4_Sub2.queuedStateValues[i_11_] = 0;
                Class66.stateValues[i_11_] = 0;
            }
        }
        Class62_Sub2.method1084((byte) -87, IsaacPrng.anInt1095);
        IsaacPrng.anInt1095 = -1;
        Class62_Sub2.method1084((byte) 107, Class39_Sub5_Sub14.anInt1912);
        Class39_Sub5_Sub14.anInt1912 = -1;
        Class62_Sub2.method1084((byte) -121, Class39_Sub11.anInt1478);
        Class39_Sub11.anInt1478 = -1;
        Class62_Sub2.method1084((byte) -122, SubNode.anInt1348);
        SubNode.anInt1348 = -1;
        Class62_Sub2.method1084((byte) -45, ClientScript.anInt1713);
        ClientScript.anInt1713 = -1;
        Class62_Sub2.method1084((byte) 107, StillGraphic.anInt2338);
        StillGraphic.anInt2338 = -1;
        Class62_Sub2.method1084((byte) -108, Class26.anInt485);
        Node.anInt728 = 3;
        ClientScript.anInt1703 = -1;
        OndemandRequest.aClass3_1714 = null;
        Class39_Sub10.anInt1420 = -1;
        anInt2285 = 0;
        Class26.anInt485 = -1;
        Class39_Sub12.aBoolean1489 = false;
        Class39_Sub12.aBoolean1493 = false;
        Huffmans.anInt756 = 0;
        ClientScript.aClass45_1705.method925(null, false, new int[5],
                i ^ 0x30, -1);
        for (int i_12_ = 0; i_12_ < 5; i_12_++) {
            Class39_Sub14.aClass3Array1519[i_12_] = null;
            RuntimeException_Sub1.aBooleanArray1225[i_12_] = false;
        }
        ArchiveWorker.method1121(-116);
        AbstractImage.aBoolean1000 = true;
    }

    public static int method512(int i, int i_13_) {
        return (int) (Math.log((double) i_13_ * 0.00390625) * 868.5889638065036
                + 0.5);
    }

    public void method513(int i, int i_14_, int i_15_, int i_16_) {
        for (int i_17_ = 0; i_17_ < 4; i_17_++) {
            if (i_14_ >= anIntArray2286[i_17_]) {
                anIntArray2273[i_17_] = i_15_;
                anIntArray2258[i_17_] = i_16_;
                anIntArray2286[i_17_] = i_14_ + 70;
                break;
            }
        }
    }

    public static void executeClientScript(int i, Object[] params, Widget class39_sub5_sub17, int i_18_, int i_19_, Widget class39_sub5_sub17_20_, int i_21_) {
        int scriptId = ((Integer) params[0]).intValue();
        DeveloperToolsClient.script(scriptId, params);
        ClientScript currentClientScript = Class1.getClientScript(scriptId);
        if (currentClientScript != null) {
            int intStackPointer = 0;
            int strStackPointer = 0;
            int offset = -1;
            int[] intOperands = currentClientScript.intOperands;
            int[] opcodes = currentClientScript.opcodes;
            int opcode = -1;
            try {
                JMouseListener.strVariables = new JString[currentClientScript.amountStrVars];
                JSocket.intVariables = new int[currentClientScript.amountIntVars];
                int i_28_ = 0;
                int i_29_ = 0;
                for (int i_30_ = 1; params.length > i_30_; i_30_++) {
                    if (!(params[i_30_] instanceof Integer)) {
                        if (params[i_30_] instanceof JString) {
                            JMouseListener.strVariables[i_29_++] = (JString) params[i_30_];
                        }
                    } else {
                        int value = ((Integer) params[i_30_]).intValue();
                        if (value == -2147483647) {
                            value = i_19_;
                        }
                        if (value == -2147483646) {
                            value = i_21_;
                        }
                        if (value == -2147483645) {
                            value = (class39_sub5_sub17_20_ == null ? -1  : class39_sub5_sub17_20_.anInt2084);
                        }
                        if (value == -2147483644) {
                            value = i;
                        }
                        if (value == -2147483643) {
                            value = (class39_sub5_sub17_20_ == null ? -1
                                    : class39_sub5_sub17_20_.anInt2102);
                        }
                        if (value == -2147483642) {
                            value = (class39_sub5_sub17 != null ? class39_sub5_sub17.anInt2084 : -1);
                        }
                        if (value == -2147483641) {
                            value = (class39_sub5_sub17 != null ? class39_sub5_sub17.anInt2102 : -1);
                        }
                        JSocket.intVariables[i_28_++] = value;
                    }
                }
                for (;;) {
                    opcode = opcodes[++offset];
                    if (opcode < 100) {
                        if (opcode == 0) {
                            Class39_Sub7.intStack[intStackPointer++] = intOperands[offset];
                            continue;
                        }
                        if (opcode == 1) {
                            int i_32_ = intOperands[offset];
                            Class39_Sub7.intStack[intStackPointer++] = Class66.stateValues[i_32_];
                            continue;
                        }
                        if (opcode == 2) {
                            int id = intOperands[offset];
                            Class66.stateValues[id] = Class39_Sub7.intStack[--intStackPointer];
                            continue;
                        }
                        if (opcode == 3) {
                            Varbit.globalStrVars[strStackPointer++] = currentClientScript.strOperands[offset];
                            continue;
                        }
                        if (opcode == 6) {
                            offset += intOperands[offset];
                            continue;
                        }
                        if (opcode == 7) {
                            intStackPointer -= 2;
                            if (Class39_Sub7.intStack[intStackPointer + 1] != Class39_Sub7.intStack[intStackPointer]) {
                                offset += intOperands[offset];
                            }
                            continue;
                        }
                        if (opcode == 8) {
                            intStackPointer -= 2;
                            if (Class39_Sub7.intStack[intStackPointer] == Class39_Sub7.intStack[intStackPointer + 1]) {
                                offset += intOperands[offset];
                            }
                            continue;
                        }
                        if (opcode == 9) {
                            intStackPointer -= 2;
                            if (Class39_Sub7.intStack[intStackPointer]  < Class39_Sub7.intStack[intStackPointer + 1]) {
                                offset += intOperands[offset];
                            }
                            continue;
                        }
                        if (opcode == 10) {
                            intStackPointer -= 2;
                            if (Class39_Sub7.intStack[intStackPointer + 1] < Class39_Sub7.intStack[intStackPointer]) {
                                offset += intOperands[offset];
                            }
                            continue;
                        }
                        if (opcode == 21) {
                            if (Class39_Sub11.scriptStatePointer != 0) {
                                ScriptState scriptState = (Class39_Sub4.scriptStateStack[--Class39_Sub11.scriptStatePointer]);
                                JSocket.intVariables = scriptState.intVariables;
                                JMouseListener.strVariables = scriptState.strVariables;
                                currentClientScript = scriptState.clientScript;
                                offset = scriptState.offset;
                                intOperands = currentClientScript.intOperands;
                                opcodes = currentClientScript.opcodes;
                                continue;
                            }
                            break;
                        }
                        if (opcode == 25) {
                            int id = intOperands[offset];
                            Class39_Sub7.intStack[intStackPointer++] = Class44.getVarbitValue(id);
                            continue;
                        }
                        if (opcode == 27) {
                            int id = intOperands[offset];
                            Class45.setVarbitValue(Class39_Sub7.intStack[--intStackPointer], id);
                            continue;
                        }
                        if (opcode == 31) {
                            intStackPointer -= 2;
                            if (Class39_Sub7.intStack[intStackPointer + 1] >= Class39_Sub7.intStack[intStackPointer]) {
                                offset += intOperands[offset];
                            }
                            continue;
                        }
                        if (opcode == 32) {
                            intStackPointer -= 2;
                            if (Class39_Sub7.intStack[intStackPointer]  >= Class39_Sub7.intStack[intStackPointer + 1]) {
                                offset += intOperands[offset];
                            }
                            continue;
                        }
                        if (opcode == 33) {
                            Class39_Sub7.intStack[intStackPointer++] = JSocket.intVariables[intOperands[offset]];
                            continue;
                        }
                        if (opcode == 34) {
                            JSocket.intVariables[intOperands[offset]] = Class39_Sub7.intStack[--intStackPointer];
                            continue;
                        }
                        if (opcode == 35) {
                            Varbit.globalStrVars[strStackPointer++] = JMouseListener.strVariables[intOperands[offset]];
                            continue;
                        }
                        if (opcode == 36) {
                            JMouseListener.strVariables[intOperands[offset]] = Varbit.globalStrVars[--strStackPointer];
                            continue;
                        }
                        if (opcode == 37) {
                            int i_36_ = intOperands[offset];
                            strStackPointer -= i_36_;
                            JString class3 = JImage.method1013(i_36_,
                                    (Varbit.globalStrVars), strStackPointer);
                            Varbit.globalStrVars[strStackPointer++] = class3;
                            continue;
                        }
                        if (opcode == 38) {
                            intStackPointer--;
                            continue;
                        }
                        if (opcode == 39) {
                            strStackPointer--;
                            continue;
                        }
                        if (opcode == 40) {
                            int i_37_ = intOperands[offset];
                            ClientScript clientScript = Class1.getClientScript(i_37_);
                            int[] intVars = new int[clientScript.amountIntVars];
                            JString[] strVars = new JString[clientScript.amountStrVars];
                            for (int i_40_ = 0;
                                    i_40_ < clientScript.amountIntParams;
                                    i_40_++) {
                                intVars[i_40_] = (Class39_Sub7.intStack[(-clientScript.amountIntParams + (intStackPointer + i_40_))]);
                            }
                            for (int i_41_ = 0;
                                    i_41_ < clientScript.amountStrParams;
                                    i_41_++) {
                                strVars[i_41_] = (Varbit.globalStrVars[(strStackPointer - (clientScript.amountStrParams - i_41_))]);
                            }
                            strStackPointer -= clientScript.amountStrParams;
                            intStackPointer -= clientScript.amountIntParams;
                            ScriptState scriptState = new ScriptState();
                            scriptState.offset = offset;
                            offset = -1;
                            scriptState.intVariables = JSocket.intVariables;
                            scriptState.strVariables = JMouseListener.strVariables;
                            scriptState.clientScript = currentClientScript;
                            Class39_Sub4.scriptStateStack[Class39_Sub11.scriptStatePointer++] = scriptState;
                            JMouseListener.strVariables = strVars;
                            JSocket.intVariables = intVars;
                            currentClientScript = clientScript;
                            opcodes = currentClientScript.opcodes;
                            intOperands = currentClientScript.intOperands;
                            continue;
                        }
                        if (opcode == 42) {
                            Class39_Sub7.intStack[intStackPointer++] = AbstractImage.globalIntVars[intOperands[offset]];
                            continue;
                        }
                        if (opcode == 43) {
                            AbstractImage.globalIntVars[intOperands[offset]] = Class39_Sub7.intStack[--intStackPointer];
                            continue;
                        }
                        if (opcode == 44) {
                            int arrayId = intOperands[offset] >> 16;
                            int initFlag = intOperands[offset] & 0xffff;
                            int size = Class39_Sub7.intStack[--intStackPointer];
                            if (size < 0 || size > 5000) {
                                throw new RuntimeException();
                            }
                            int initValue = -1;
                            if (initFlag == 'i') {
                                initValue = 0;
                            }
                            ScriptState.arraySizes[arrayId] = size;
                            for (int i_46_ = 0; size > i_46_; i_46_++) {
                                Class30.globalArrays[arrayId][i_46_] = initValue;
                            }
                            continue;
                        }
                        if (opcode == 45) {
                            int arrayId = intOperands[offset];
                            int index = Class39_Sub7.intStack[--intStackPointer];
                            if (index < 0 || index >= ScriptState.arraySizes[arrayId]) {
                                throw new RuntimeException();
                            }
                            Class39_Sub7.intStack[intStackPointer++] = Class30.globalArrays[arrayId][index];
                            continue;
                        }
                        if (opcode == 46) {
                            int i_49_ = intOperands[offset];
                            intStackPointer -= 2;
                            int i_50_ = Class39_Sub7.intStack[intStackPointer];
                            if (i_50_ < 0
                                    || ScriptState.arraySizes[i_49_] <= i_50_) {
                                throw new RuntimeException();
                            }
                            Class30.globalArrays[i_49_][i_50_] = Class39_Sub7.intStack[intStackPointer + 1];
                            continue;
                        }
                    }
                    boolean bool;
                    if (intOperands[offset] != 1) {
                        bool = false;
                    } else {
                        bool = true;
                    }
                    if (opcode >= 1000) {
                        if ((opcode < 1000 || opcode >= 1100) && (opcode < 2000 || opcode >= 2100)) {
                            if ((opcode < 1100 || opcode >= 1200) && (opcode < 2100 || opcode >= 2200)) {
                                if ((opcode < 1200 || opcode >= 1300) && (opcode < 2200 || opcode >= 2300)) {
                                    if (opcode >= 1300 && opcode < 1400 || opcode >= 2300 && opcode < 2400) {
                                        Widget widget;
                                        if (opcode < 2000) {
                                            widget = (!bool ? (Class44.aClass39_Sub5_Sub17_841) : (Class43.aClass39_Sub5_Sub17_810));
                                        } else {
                                            opcode -= 1000;
                                            widget = (Class37.getWidget((Class39_Sub7.intStack[--intStackPointer])));
                                        }
                                        if (opcode == 1300) {
                                            int i_52_ = ((Class39_Sub7.intStack[--intStackPointer]) - 1);
                                            if (i_52_ < 0 || i_52_ > 9) {
                                                strStackPointer--;
                                            } else {
                                                widget.method756(1, (Varbit.globalStrVars[--strStackPointer]), i_52_);
                                            }
                                        } else if (opcode == 1301) {
                                            intStackPointer -= 2;
                                            int i_53_ = (Class39_Sub7.intStack[intStackPointer + 1]);
                                            int i_54_ = (Class39_Sub7.intStack[intStackPointer]);
                                            widget.aClass39_Sub5_Sub17_2040 = (Class62_Sub2.method1081(i_54_, i_53_, 0));
                                        } else if (opcode == 1302) {
                                            widget.aBoolean2108 = (Class39_Sub7.intStack[--intStackPointer]) == 1;
                                        } else if (opcode == 1303) {
                                            widget.anInt2056 = (Class39_Sub7.intStack[--intStackPointer]);
                                        } else if (opcode == 1304) {
                                            widget.anInt2008 = (Class39_Sub7.intStack[--intStackPointer]);
                                        } else if (opcode == 1305) {
                                            widget.aClass3_2065 = (Varbit.globalStrVars[--strStackPointer]);
                                        } else {
                                            if (opcode != 1306) {
                                                break;
                                            }
                                            widget.aClass3_2023 = (Varbit.globalStrVars[--strStackPointer]);
                                        }
                                    } else if ((opcode < 1400 || opcode >= 1500)
                                            && (opcode < 2400
                                            || opcode >= 2500)) {
                                        if (opcode >= 1600) {
                                            if (opcode < 1700) {
                                                Widget class39_sub5_sub17_55_ = (!bool
                                                        ? (Class44.aClass39_Sub5_Sub17_841)
                                                        : (Class43.aClass39_Sub5_Sub17_810));
                                                if (opcode == 1600) {
                                                    Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_55_.anInt2064);
                                                } else if (opcode == 1601) {
                                                    Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_55_.anInt1994);
                                                } else if (opcode == 1602) {
                                                    Varbit.globalStrVars[strStackPointer++] = (class39_sub5_sub17_55_.aClass3_2029);
                                                } else if (opcode == 1603) {
                                                    Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_55_.anInt2020);
                                                } else {
                                                    if (opcode != 1604) {
                                                        break;
                                                    }
                                                    Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_55_.anInt2095);
                                                }
                                            } else if (opcode < 1800) {
                                                Widget class39_sub5_sub17_56_ = (bool
                                                        ? (Class43.aClass39_Sub5_Sub17_810)
                                                        : (Class44.aClass39_Sub5_Sub17_841));
                                                if (opcode == 1700) {
                                                    Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_56_.anInt1997);
                                                } else if (opcode == 1701) {
                                                    if ((class39_sub5_sub17_56_.anInt1997)
                                                            == -1) {
                                                        Class39_Sub7.intStack[intStackPointer++] = 0;
                                                    } else {
                                                        Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_56_.anInt2096);
                                                    }
                                                } else {
                                                    if (opcode != 1702) {
                                                        break;
                                                    }
                                                    Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_56_.anInt2102);
                                                }
                                            } else if (opcode >= 2600) {
                                                if (opcode >= 2700) {
                                                    if (opcode < 2800) {
                                                        Widget class39_sub5_sub17_57_ = (Class37.getWidget((Class39_Sub7.intStack[--intStackPointer])));
                                                        if (opcode == 2700) {
                                                            Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_57_.anInt1997);
                                                        } else {
                                                            if (opcode != 2701) {
                                                                break;
                                                            }
                                                            if ((class39_sub5_sub17_57_.anInt1997)
                                                                    != -1) {
                                                                Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_57_.anInt2096);
                                                            } else {
                                                                Class39_Sub7.intStack[intStackPointer++] = 0;
                                                            }
                                                        }
                                                    } else if (opcode >= 3200) {
                                                        if (opcode >= 3300) {
                                                            if (opcode < 3400) {
                                                                if (opcode
                                                                        == 3300) {
                                                                    Class39_Sub7.intStack[intStackPointer++] = (Class2.logicCycle);
                                                                } else if (opcode
                                                                        == 3301) {
                                                                    intStackPointer -= 2;
                                                                    int i_58_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_59_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (Class39_Sub11.method869(86,
                                                                            i_59_,
                                                                            i_58_));
                                                                } else if (opcode
                                                                        == 3302) {
                                                                    intStackPointer -= 2;
                                                                    int i_60_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_61_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (JImage.method1018(i_60_,
                                                                            16485,
                                                                            i_61_));
                                                                } else if (opcode
                                                                        == 3303) {
                                                                    intStackPointer -= 2;
                                                                    int i_62_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    int i_63_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (GroundItem.method505(i_62_,
                                                                            (byte) -124,
                                                                            i_63_));
                                                                } else if (opcode
                                                                        == 3304) {
                                                                    int i_64_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (Class65.method1098((byte) 102,
                                                                            i_64_).anInt1746);
                                                                } else if (opcode
                                                                        == 3305) {
                                                                    int i_65_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (Class31.anIntArray555[i_65_]);
                                                                } else {
                                                                    if (opcode
                                                                            != 3306) {
                                                                        break;
                                                                    }
                                                                    int i_66_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (Class39_Sub12.anIntArray1491[i_66_]);
                                                                }
                                                            } else if (opcode
                                                                    < 4100) {
                                                                if (opcode
                                                                        == 4000) {
                                                                    intStackPointer -= 2;
                                                                    int i_67_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_68_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (i_68_
                                                                            + i_67_);
                                                                } else if (opcode
                                                                        == 4001) {
                                                                    intStackPointer -= 2;
                                                                    int i_69_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_70_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (i_69_
                                                                            - i_70_);
                                                                } else if (opcode
                                                                        == 4002) {
                                                                    intStackPointer -= 2;
                                                                    int i_71_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_72_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (i_72_
                                                                            * i_71_);
                                                                } else if (opcode
                                                                        == 4003) {
                                                                    intStackPointer -= 2;
                                                                    int i_73_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_74_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (i_73_
                                                                            / i_74_);
                                                                } else if (opcode
                                                                        == 4004) {
                                                                    int i_75_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (int) (Math.random() * (double) i_75_);
                                                                } else if (opcode
                                                                        == 4005) {
                                                                    int i_76_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (int) (Math.random() * (double) (i_76_ + 1));
                                                                } else if (opcode
                                                                        == 4006) {
                                                                    intStackPointer -= 5;
                                                                    int i_77_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_78_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 2)]);
                                                                    int i_79_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    int i_80_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 3)]);
                                                                    int i_81_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 4)]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (i_77_
                                                                            + ((i_79_
                                                                            - i_77_)
                                                                            * (-i_78_ + i_81_)
                                                                            / (-i_78_ + i_80_)));
                                                                } else if (opcode
                                                                        == 4007) {
                                                                    intStackPointer -= 2;
                                                                    int i_82_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_83_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (i_82_
                                                                            + (i_83_
                                                                            * i_82_
                                                                            / 100));
                                                                } else if (opcode
                                                                        == 4008) {
                                                                    intStackPointer -= 2;
                                                                    int i_84_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_85_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (Queue.or(1 << i_85_,
                                                                            i_84_));
                                                                } else if (opcode
                                                                        == 4009) {
                                                                    intStackPointer -= 2;
                                                                    int i_86_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_87_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (Class34.method342(-(1
                                                                            << i_87_) - 1,
                                                                            i_86_));
                                                                } else if (opcode
                                                                        == 4010) {
                                                                    intStackPointer -= 2;
                                                                    int i_88_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    int i_89_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = ((Class34.method342(i_89_,
                                                                            1 << i_88_)) != 0
                                                                            ? 1
                                                                            : 0);
                                                                } else if (opcode
                                                                        == 4011) {
                                                                    intStackPointer -= 2;
                                                                    int i_90_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    int i_91_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (i_91_
                                                                            % i_90_);
                                                                } else if (opcode
                                                                        == 4012) {
                                                                    intStackPointer -= 2;
                                                                    int i_92_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    int i_93_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    if (i_93_
                                                                            != 0) {
                                                                        Class39_Sub7.intStack[intStackPointer++] = (int) Math.pow((double) i_93_, (double) i_92_);
                                                                    } else {
                                                                        Class39_Sub7.intStack[intStackPointer++] = 0;
                                                                    }
                                                                } else if (opcode
                                                                        == 4013) {
                                                                    intStackPointer -= 2;
                                                                    int i_94_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_95_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    if (i_94_
                                                                            == 0) {
                                                                        Class39_Sub7.intStack[intStackPointer++] = 0;
                                                                    } else if (i_95_
                                                                            == 0) {
                                                                        Class39_Sub7.intStack[intStackPointer++] = 2147483647;
                                                                    } else {
                                                                        Class39_Sub7.intStack[intStackPointer++] = (int) Math.pow((double) i_94_, 1.0 / (double) i_95_);
                                                                    }
                                                                } else if (opcode
                                                                        == 4014) {
                                                                    intStackPointer -= 2;
                                                                    int i_96_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    int i_97_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (Class34.method342(i_96_,
                                                                            i_97_));
                                                                } else {
                                                                    if (opcode
                                                                            != 4015) {
                                                                        break;
                                                                    }
                                                                    intStackPointer -= 2;
                                                                    int i_98_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_99_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (Queue.or(i_98_,
                                                                            i_99_));
                                                                }
                                                            } else if (opcode
                                                                    >= 4200) {
                                                                if (opcode
                                                                        >= 4300) {
                                                                    break;
                                                                }
                                                                if (opcode
                                                                        == 4200) {
                                                                    int i_100_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                    Varbit.globalStrVars[strStackPointer++] = (Class26.getItemDefinition(i_100_).aClass3_1661);
                                                                } else if (opcode
                                                                        == 4201) {
                                                                    intStackPointer -= 2;
                                                                    int i_101_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_102_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    ItemDefinition class39_sub5_sub1 = (Class26.getItemDefinition(i_101_));
                                                                    if ((i_102_
                                                                            >= 1)
                                                                            && (i_102_
                                                                            <= 5)
                                                                            && (class39_sub5_sub1.aClass3Array1672[i_102_ - 1]) != null) {
                                                                        Varbit.globalStrVars[strStackPointer++] = class39_sub5_sub1.aClass3Array1672[i_102_ - 1];
                                                                    } else {
                                                                        Varbit.globalStrVars[strStackPointer++] = Class39_Sub12.aClass3_1492;
                                                                    }
                                                                } else if (opcode
                                                                        == 4202) {
                                                                    intStackPointer -= 2;
                                                                    int i_103_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                    int i_104_ = (Class39_Sub7.intStack[(intStackPointer
                                                                            + 1)]);
                                                                    ItemDefinition class39_sub5_sub1 = (Class26.getItemDefinition(i_103_));
                                                                    if ((i_104_
                                                                            < 1)
                                                                            || (i_104_
                                                                            > 5)
                                                                            || (class39_sub5_sub1.aClass3Array1657[i_104_ - 1]) == null) {
                                                                        Varbit.globalStrVars[strStackPointer++] = Class39_Sub12.aClass3_1492;
                                                                    } else {
                                                                        Varbit.globalStrVars[strStackPointer++] = class39_sub5_sub1.aClass3Array1657[i_104_ - 1];
                                                                    }
                                                                } else if (opcode
                                                                        == 4203) {
                                                                    int i_105_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = (Class26.getItemDefinition(i_105_).anInt1686);
                                                                } else if (opcode
                                                                        == 4204) {
                                                                    int i_106_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = ((Class26.getItemDefinition(i_106_).anInt1662) == 1
                                                                            ? 1
                                                                            : 0);
                                                                } else if (opcode
                                                                        == 4205) {
                                                                    int i_107_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                    ItemDefinition class39_sub5_sub1 = (Class26.getItemDefinition(i_107_));
                                                                    if (((class39_sub5_sub1.anInt1644)
                                                                            == -1)
                                                                            && (class39_sub5_sub1.anInt1651
                                                                            >= 0)) {
                                                                        Class39_Sub7.intStack[intStackPointer++] = class39_sub5_sub1.anInt1651;
                                                                    } else {
                                                                        Class39_Sub7.intStack[intStackPointer++] = i_107_;
                                                                    }
                                                                } else if (opcode
                                                                        == 4206) {
                                                                    int i_108_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                    ItemDefinition class39_sub5_sub1 = (Class26.getItemDefinition(i_108_));
                                                                    if (((class39_sub5_sub1.anInt1644)
                                                                            >= 0)
                                                                            && (class39_sub5_sub1.anInt1651
                                                                            >= 0)) {
                                                                        Class39_Sub7.intStack[intStackPointer++] = class39_sub5_sub1.anInt1651;
                                                                    } else {
                                                                        Class39_Sub7.intStack[intStackPointer++] = i_108_;
                                                                    }
                                                                } else {
                                                                    if (opcode
                                                                            != 4207) {
                                                                        break;
                                                                    }
                                                                    int i_109_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                    Class39_Sub7.intStack[intStackPointer++] = ((Class26.getItemDefinition(i_109_).aBoolean1653)
                                                                            ? 1
                                                                            : 0);
                                                                }
                                                            } else if (opcode
                                                                    == 4100) {
                                                                JString class3 = (Varbit.globalStrVars[--strStackPointer]);
                                                                int i_110_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                Varbit.globalStrVars[strStackPointer++] = (Class39_Sub5_Sub11.method708((new JString[]{class3,
                                                                            (AbstractImage.method1007((byte) 71,
                                                                            i_110_))})));
                                                            } else if (opcode
                                                                    == 4101) {
                                                                strStackPointer -= 2;
                                                                JString class3 = (Varbit.globalStrVars[strStackPointer]);
                                                                JString class3_111_ = (Varbit.globalStrVars[(strStackPointer
                                                                        + 1)]);
                                                                Varbit.globalStrVars[strStackPointer++] = (Class39_Sub5_Sub11.method708((new JString[]{class3,
                                                                            class3_111_})));
                                                            } else if (opcode
                                                                    == 4102) {
                                                                JString class3 = (Varbit.globalStrVars[--strStackPointer]);
                                                                int i_112_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                Varbit.globalStrVars[strStackPointer++] = (Class39_Sub5_Sub11.method708((new JString[]{class3,
                                                                            (Npc.method519(10,
                                                                            true,
                                                                            i_112_))})));
                                                            } else if (opcode
                                                                    == 4103) {
                                                                JString class3 = (Varbit.globalStrVars[--strStackPointer]);
                                                                Varbit.globalStrVars[strStackPointer++] = (class3.method77());
                                                            } else if (opcode
                                                                    == 4104) {
                                                                int i_113_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                long l = (((long) i_113_
                                                                        + 11745L)
                                                                        * 86400000L);
                                                                JMouseListener.aCalendar786.setTime(new Date(l));
                                                                int i_114_ = (JMouseListener.aCalendar786.get(5));
                                                                int i_115_ = (JMouseListener.aCalendar786.get(2));
                                                                int i_116_ = (JMouseListener.aCalendar786.get(1));
                                                                Varbit.globalStrVars[strStackPointer++] = (Class39_Sub5_Sub11.method708((new JString[]{(AbstractImage.method1007((byte) 71,
                                                                            i_114_)),
                                                                            (Class12.aClass3_191),
                                                                            (Class43.aClass3Array808[i_115_]),
                                                                            (Class12.aClass3_191),
                                                                            (AbstractImage.method1007((byte) 71,
                                                                            i_116_))})));
                                                            } else if (opcode
                                                                    == 4105) {
                                                                strStackPointer -= 2;
                                                                JString class3 = (Varbit.globalStrVars[strStackPointer]);
                                                                JString class3_117_ = (Varbit.globalStrVars[(strStackPointer
                                                                        + 1)]);
                                                                if (((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.aClass45_2516)
                                                                        == null)
                                                                        || !(Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.aClass45_2516.aBoolean854)) {
                                                                    Varbit.globalStrVars[strStackPointer++] = class3;
                                                                } else {
                                                                    Varbit.globalStrVars[strStackPointer++] = class3_117_;
                                                                }
                                                            } else if (opcode
                                                                    == 4106) {
                                                                int i_118_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                                Varbit.globalStrVars[strStackPointer++] = (AbstractImage.method1007((byte) 71,
                                                                        i_118_));
                                                            } else if (opcode
                                                                    == 4107) {
                                                                strStackPointer -= 2;
                                                                Class39_Sub7.intStack[intStackPointer++] = (Varbit.globalStrVars[strStackPointer].method96(10445,
                                                                        (Varbit.globalStrVars[(strStackPointer
                                                                        + 1)])));
                                                            } else if (opcode
                                                                    == 4108) {
                                                                intStackPointer -= 2;
                                                                int i_119_ = (Class39_Sub7.intStack[(intStackPointer
                                                                        + 1)]);
                                                                int i_120_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                JString class3 = (Varbit.globalStrVars[--strStackPointer]);
                                                                byte[] is_121_ = (Class55.fileLoader13.lookupFile(i_119_,
                                                                        0));
                                                                BitmapFont class39_sub5_sub10_sub1 = (new BitmapFont(is_121_));
                                                                Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub10_sub1.method634(class3,
                                                                        i_120_));
                                                            } else {
                                                                if (opcode
                                                                        != 4109) {
                                                                    break;
                                                                }
                                                                intStackPointer -= 2;
                                                                JString class3 = (Varbit.globalStrVars[--strStackPointer]);
                                                                int i_122_ = (Class39_Sub7.intStack[intStackPointer]);
                                                                int i_123_ = (Class39_Sub7.intStack[(intStackPointer
                                                                        + 1)]);
                                                                byte[] is_124_ = (Class55.fileLoader13.lookupFile(i_123_,
                                                                        0));
                                                                BitmapFont class39_sub5_sub10_sub1 = (new BitmapFont(is_124_));
                                                                Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub10_sub1.method631(class3,
                                                                        i_122_));
                                                            }
                                                        } else if (opcode
                                                                == 3200) {
                                                            intStackPointer -= 3;
                                                            TraversalMap.method296((Class39_Sub7.intStack[intStackPointer + 2]),
                                                                    (Class39_Sub7.intStack[intStackPointer + 1]),
                                                                    true,
                                                                    (Class39_Sub7.intStack[intStackPointer]));
                                                        } else if (opcode
                                                                == 3201) {
                                                            Class45.method916((Class39_Sub7.intStack[--intStackPointer]),
                                                                    -44);
                                                        } else {
                                                            if (opcode != 3202) {
                                                                break;
                                                            }
                                                            intStackPointer -= 2;
                                                            Class37.method351(1,
                                                                    (Class39_Sub7.intStack[intStackPointer + 1]),
                                                                    (Class39_Sub7.intStack[intStackPointer]));
                                                        }
                                                    } else if (opcode == 3100) {
                                                        JString class3 = (Varbit.globalStrVars[--strStackPointer]);
                                                        JMouseListener.method902((Class39_Sub12.aClass3_1492),
                                                                class3, false, 0);
                                                    } else if (opcode == 3101) {
                                                        intStackPointer -= 2;
                                                        Class46_Sub1.method944((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109),
                                                                (Class39_Sub7.intStack[intStackPointer]),
                                                                -12,
                                                                (Class39_Sub7.intStack[intStackPointer + 1]));
                                                    } else if (opcode == 3102) {
                                                        int i_125_ = (Class39_Sub7.intStack[--intStackPointer]);
                                                        if (i_125_ >= 0
                                                                && (i_125_
                                                                < (Class39_Sub5_Sub14.anIntArray1914).length)
                                                                && ((Class39_Sub5_Sub14.anIntArray1914[i_125_])
                                                                != -1)) {
                                                            IsaacPrng.aBoolean1089 = true;
                                                            Class39_Sub14.aBoolean1520 = true;
                                                            Node.anInt728 = i_125_;
                                                        }
                                                    } else {
                                                        if (opcode != 3103) {
                                                            break;
                                                        }
                                                        Class55.method999(31121);
                                                    }
                                                } else {
                                                    Widget class39_sub5_sub17_126_ = (Class37.getWidget((Class39_Sub7.intStack[--intStackPointer])));
                                                    if (opcode == 2600) {
                                                        Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_126_.anInt2064);
                                                    } else if (opcode == 2601) {
                                                        Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_126_.anInt1994);
                                                    } else if (opcode == 2602) {
                                                        Varbit.globalStrVars[strStackPointer++] = (class39_sub5_sub17_126_.aClass3_2029);
                                                    } else if (opcode == 2603) {
                                                        Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_126_.anInt2020);
                                                    } else {
                                                        if (opcode != 2604) {
                                                            break;
                                                        }
                                                        Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_126_.anInt2095);
                                                    }
                                                }
                                            } else {
                                                Widget class39_sub5_sub17_127_ = (Class37.getWidget((Class39_Sub7.intStack[--intStackPointer])));
                                                if (opcode == 2500) {
                                                    Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_127_.anInt2091);
                                                } else if (opcode == 2501) {
                                                    Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_127_.anInt2021);
                                                } else if (opcode == 2502) {
                                                    Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_127_.quadWidth);
                                                } else if (opcode == 2503) {
                                                    Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_127_.quadHeight);
                                                } else if (opcode == 2504) {
                                                    Class39_Sub7.intStack[intStackPointer++] = (!(class39_sub5_sub17_127_.aBoolean2055)
                                                            ? 0 : 1);
                                                } else {
                                                    if (opcode != 2505) {
                                                        break;
                                                    }
                                                    Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_127_.anInt2050);
                                                }
                                            }
                                        } else {
                                            Widget class39_sub5_sub17_128_ = (!bool
                                                    ? (Class44.aClass39_Sub5_Sub17_841)
                                                    : (Class43.aClass39_Sub5_Sub17_810));
                                            if (opcode == 1500) {
                                                Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_128_.anInt2091);
                                            } else if (opcode == 1501) {
                                                Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_128_.anInt2021);
                                            } else if (opcode == 1502) {
                                                Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_128_.quadWidth);
                                            } else if (opcode == 1503) {
                                                Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_128_.quadHeight);
                                            } else if (opcode == 1504) {
                                                Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_128_.aBoolean2055) ? 1 : 0;
                                            } else {
                                                if (opcode != 1505) {
                                                    break;
                                                }
                                                Class39_Sub7.intStack[intStackPointer++] = (class39_sub5_sub17_128_.anInt2050);
                                            }
                                        }
                                    } else {
                                        Widget class39_sub5_sub17_129_;
                                        if (opcode >= 2000) {
                                            class39_sub5_sub17_129_ = (Class37.getWidget((Class39_Sub7.intStack[--intStackPointer])));
                                            opcode -= 1000;
                                        } else {
                                            class39_sub5_sub17_129_ = (!bool
                                                    ? (Class44.aClass39_Sub5_Sub17_841)
                                                    : (Class43.aClass39_Sub5_Sub17_810));
                                        }
                                        JString class3 = (Varbit.globalStrVars[--strStackPointer]);
                                        Object[] objects_130_ = (new Object[class3.getLength() + 1]);
                                        for (int i_131_ = objects_130_.length - 1;
                                                i_131_ >= 1; i_131_--) {
                                            if (class3.charAt(i_131_ - 1)
                                                    == 115) {
                                                objects_130_[i_131_] = (Varbit.globalStrVars[--strStackPointer]);
                                            } else {
                                                objects_130_[i_131_] = (new Integer(Class39_Sub7.intStack[--intStackPointer]));
                                            }
                                        }
                                        objects_130_[0] = new Integer(Class39_Sub7.intStack[--intStackPointer]);
                                        if (opcode == 1406) {
                                            class39_sub5_sub17_129_.anObjectArray2004 = objects_130_;
                                        }
                                        if (opcode == 1412) {
                                            class39_sub5_sub17_129_.anObjectArray2075 = objects_130_;
                                        }
                                        if (opcode == 1416) {
                                            class39_sub5_sub17_129_.anObjectArray2099 = objects_130_;
                                        }
                                        if (opcode == 1408) {
                                            class39_sub5_sub17_129_.anObjectArray2016 = objects_130_;
                                        }
                                        if (opcode == 1400) {
                                            class39_sub5_sub17_129_.anObjectArray2035 = objects_130_;
                                        }
                                        if (opcode == 1411) {
                                            class39_sub5_sub17_129_.anObjectArray2044 = objects_130_;
                                        }
                                        if (opcode == 1401) {
                                            class39_sub5_sub17_129_.anObjectArray2080 = objects_130_;
                                        }
                                        if (opcode == 1403) {
                                            class39_sub5_sub17_129_.anObjectArray2019 = objects_130_;
                                        }
                                        if (opcode == 1414) {
                                            class39_sub5_sub17_129_.anObjectArray2061 = objects_130_;
                                        }
                                        if (opcode == 1410) {
                                            class39_sub5_sub17_129_.anObjectArray2088 = objects_130_;
                                        }
                                        if (opcode == 1404) {
                                            class39_sub5_sub17_129_.anObjectArray2100 = objects_130_;
                                        }
                                        if (opcode == 1415) {
                                            class39_sub5_sub17_129_.anObjectArray2062 = objects_130_;
                                        }
                                        if (opcode == 1417) {
                                            class39_sub5_sub17_129_.anObjectArray2039 = objects_130_;
                                        }
                                        if (opcode == 1405) {
                                            class39_sub5_sub17_129_.anObjectArray2018 = objects_130_;
                                        }
                                        class39_sub5_sub17_129_.aBoolean2047 = true;
                                        if (opcode == 1413) {
                                            class39_sub5_sub17_129_.anObjectArray2097 = objects_130_;
                                        }
                                        if (opcode == 1407) {
                                            class39_sub5_sub17_129_.anObjectArray2101 = objects_130_;
                                        }
                                        if (opcode == 1402) {
                                            class39_sub5_sub17_129_.anObjectArray2085 = objects_130_;
                                        }
                                        if (opcode == 1409) {
                                            class39_sub5_sub17_129_.anObjectArray2045 = objects_130_;
                                        }
                                    }
                                } else {
                                    Widget class39_sub5_sub17_132_;
                                    if (opcode < 2000) {
                                        class39_sub5_sub17_132_ = (bool
                                                ? (Class43.aClass39_Sub5_Sub17_810)
                                                : (Class44.aClass39_Sub5_Sub17_841));
                                    } else {
                                        class39_sub5_sub17_132_ = (Class37.getWidget((Class39_Sub7.intStack[--intStackPointer])));
                                        opcode -= 1000;
                                    }
                                    Deque.method967(class39_sub5_sub17_132_,
                                            (byte) -22);
                                    if (opcode == 1200) {
                                        intStackPointer -= 2;
                                        int i_133_ = (Class39_Sub7.intStack[intStackPointer]);
                                        int i_134_ = (Class39_Sub7.intStack[intStackPointer + 1]);
                                        class39_sub5_sub17_132_.anInt1997 = i_133_;
                                        class39_sub5_sub17_132_.anInt2096 = i_134_;
                                    } else if (opcode == 1201) {
                                        class39_sub5_sub17_132_.anInt2009 = 2;
                                        class39_sub5_sub17_132_.anInt2026 = (Class39_Sub7.intStack[--intStackPointer]);
                                    } else {
                                        if (opcode != 1202) {
                                            break;
                                        }
                                        class39_sub5_sub17_132_.anInt2009 = 3;
                                        class39_sub5_sub17_132_.anInt2026 = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.aClass45_2516.method928(124);
                                    }
                                }
                            } else {
                                Widget class39_sub5_sub17_135_;
                                if (opcode < 2000) {
                                    class39_sub5_sub17_135_ = (!bool
                                            ? Class44.aClass39_Sub5_Sub17_841
                                            : Class43.aClass39_Sub5_Sub17_810);
                                } else {
                                    opcode -= 1000;
                                    class39_sub5_sub17_135_ = Class37.getWidget((Class39_Sub7.intStack[--intStackPointer]));
                                }
                                Deque.method967(class39_sub5_sub17_135_,
                                        (byte) -22);
                                if (opcode == 1100) {
                                    intStackPointer -= 2;
                                    class39_sub5_sub17_135_.anInt2064 = Class39_Sub7.intStack[intStackPointer];
                                    class39_sub5_sub17_135_.anInt1994 = (Class39_Sub7.intStack[intStackPointer + 1]);
                                } else if (opcode == 1101) {
                                    class39_sub5_sub17_135_.activeQuadColor = Class39_Sub7.intStack[--intStackPointer];
                                } else if (opcode == 1102) {
                                    class39_sub5_sub17_135_.drawSolidQuad = (Class39_Sub7.intStack[--intStackPointer]
                                            == 1);
                                } else if (opcode == 1103) {
                                    class39_sub5_sub17_135_.anInt2030 = Class39_Sub7.intStack[--intStackPointer];
                                } else if (opcode == 1104) {
                                    class39_sub5_sub17_135_.anInt2083 = Class39_Sub7.intStack[--intStackPointer];
                                } else if (opcode == 1105) {
                                    class39_sub5_sub17_135_.anInt2093 = Class39_Sub7.intStack[--intStackPointer];
                                } else if (opcode == 1106) {
                                    class39_sub5_sub17_135_.anInt2051 = Class39_Sub7.intStack[--intStackPointer];
                                } else if (opcode == 1107) {
                                    class39_sub5_sub17_135_.aBoolean2014 = (Class39_Sub7.intStack[--intStackPointer]
                                            == 1);
                                } else if (opcode == 1108) {
                                    class39_sub5_sub17_135_.anInt2009 = 1;
                                    class39_sub5_sub17_135_.anInt2026 = Class39_Sub7.intStack[--intStackPointer];
                                } else if (opcode == 1109) {
                                    intStackPointer -= 6;
                                    class39_sub5_sub17_135_.anInt2072 = Class39_Sub7.intStack[intStackPointer];
                                    class39_sub5_sub17_135_.anInt2058 = (Class39_Sub7.intStack[intStackPointer + 1]);
                                    class39_sub5_sub17_135_.anInt2098 = (Class39_Sub7.intStack[intStackPointer + 2]);
                                    class39_sub5_sub17_135_.anInt2011 = (Class39_Sub7.intStack[intStackPointer + 3]);
                                    class39_sub5_sub17_135_.anInt2007 = (Class39_Sub7.intStack[intStackPointer + 4]);
                                    class39_sub5_sub17_135_.anInt2074 = (Class39_Sub7.intStack[intStackPointer + 5]);
                                } else if (opcode == 1110) {
                                    class39_sub5_sub17_135_.anInt2103 = Class39_Sub7.intStack[--intStackPointer];
                                } else if (opcode == 1111) {
                                    class39_sub5_sub17_135_.aBoolean2081 = (Class39_Sub7.intStack[--intStackPointer]
                                            == 1);
                                } else if (opcode == 1112) {
                                    class39_sub5_sub17_135_.aClass3_2029 = (Varbit.globalStrVars[--strStackPointer]);
                                } else if (opcode == 1113) {
                                    class39_sub5_sub17_135_.anInt2105 = Class39_Sub7.intStack[--intStackPointer];
                                } else if (opcode == 1114) {
                                    intStackPointer -= 3;
                                    class39_sub5_sub17_135_.anInt2032 = Class39_Sub7.intStack[intStackPointer];
                                    class39_sub5_sub17_135_.anInt1996 = (Class39_Sub7.intStack[intStackPointer + 1]);
                                    class39_sub5_sub17_135_.anInt2036 = (Class39_Sub7.intStack[intStackPointer + 2]);
                                } else if (opcode == 1115) {
                                    class39_sub5_sub17_135_.aBoolean2059 = (Class39_Sub7.intStack[--intStackPointer]
                                            == 1);
                                } else if (opcode == 1116) {
                                    class39_sub5_sub17_135_.anInt2022 = Class39_Sub7.intStack[--intStackPointer];
                                } else if (opcode == 1117) {
                                    class39_sub5_sub17_135_.anInt2003 = Class39_Sub7.intStack[--intStackPointer];
                                } else if (opcode == 1118) {
                                    class39_sub5_sub17_135_.aBoolean2107 = (Class39_Sub7.intStack[--intStackPointer]
                                            == 1);
                                } else if (opcode == 1119) {
                                    class39_sub5_sub17_135_.aBoolean2038 = (Class39_Sub7.intStack[--intStackPointer]
                                            == 1);
                                } else {
                                    if (opcode != 1120) {
                                        break;
                                    }
                                    intStackPointer -= 2;
                                    class39_sub5_sub17_135_.anInt2020 = Class39_Sub7.intStack[intStackPointer];
                                    class39_sub5_sub17_135_.anInt2095 = (Class39_Sub7.intStack[intStackPointer + 1]);
                                }
                            }
                        } else {
                            Widget class39_sub5_sub17_136_;
                            if (opcode < 2000) {
                                class39_sub5_sub17_136_ = (bool ? Class43.aClass39_Sub5_Sub17_810
                                        : Class44.aClass39_Sub5_Sub17_841);
                            } else {
                                opcode -= 1000;
                                class39_sub5_sub17_136_ = Class37.getWidget((Class39_Sub7.intStack[--intStackPointer]));
                            }
                            Deque.method967(class39_sub5_sub17_136_,
                                    (byte) -22);
                            if (opcode == 1000) {
                                intStackPointer -= 2;
                                class39_sub5_sub17_136_.anInt2091 = Class39_Sub7.intStack[intStackPointer];
                                class39_sub5_sub17_136_.anInt2021 = Class39_Sub7.intStack[intStackPointer + 1];
                            } else if (opcode == 1001) {
                                intStackPointer -= 2;
                                class39_sub5_sub17_136_.quadWidth = Class39_Sub7.intStack[intStackPointer];
                                class39_sub5_sub17_136_.quadHeight = Class39_Sub7.intStack[intStackPointer + 1];
                            } else {
                                if (opcode != 1003) {
                                    break;
                                }
                                class39_sub5_sub17_136_.aBoolean2055 = (Class39_Sub7.intStack[--intStackPointer]
                                        == 1);
                            }
                        }
                    } else if (opcode == 100) {
                        intStackPointer -= 3;
                        int i_137_ = Class39_Sub7.intStack[intStackPointer + 1];
                        int i_138_ = Class39_Sub7.intStack[intStackPointer];
                        int i_139_ = Class39_Sub7.intStack[intStackPointer + 2];
                        if (i_137_ == 0) {
                            throw new RuntimeException();
                        }
                        Widget class39_sub5_sub17_140_ = Class37.getWidget(i_138_);
                        if ((class39_sub5_sub17_140_.aClass39_Sub5_Sub17Array2025)
                                == null) {
                            class39_sub5_sub17_140_.aClass39_Sub5_Sub17Array2025 = new Widget[i_139_ + 1];
                        }
                        if ((class39_sub5_sub17_140_.aClass39_Sub5_Sub17Array2025).length
                                <= i_139_) {
                            Widget[] class39_sub5_sub17s = new Widget[i_139_ + 1];
                            for (int i_141_ = 0;
                                    ((class39_sub5_sub17_140_.aClass39_Sub5_Sub17Array2025).length
                                    > i_141_);
                                    i_141_++) {
                                class39_sub5_sub17s[i_141_] = (class39_sub5_sub17_140_.aClass39_Sub5_Sub17Array2025[i_141_]);
                            }
                            class39_sub5_sub17_140_.aClass39_Sub5_Sub17Array2025 = class39_sub5_sub17s;
                        }
                        Widget class39_sub5_sub17_142_ = new Widget();
                        class39_sub5_sub17_142_.anInt2050 = class39_sub5_sub17_142_.anInt2084 = class39_sub5_sub17_140_.anInt2084;
                        class39_sub5_sub17_142_.aBoolean2013 = true;
                        class39_sub5_sub17_142_.anInt2102 = i_139_;
                        class39_sub5_sub17_142_.type = i_137_;
                        class39_sub5_sub17_140_.aClass39_Sub5_Sub17Array2025[i_139_] = class39_sub5_sub17_142_;
                        if (bool) {
                            Class43.aClass39_Sub5_Sub17_810 = class39_sub5_sub17_142_;
                        } else {
                            Class44.aClass39_Sub5_Sub17_841 = class39_sub5_sub17_142_;
                        }
                        Deque.method967(class39_sub5_sub17_140_, (byte) -22);
                    } else if (opcode == 101) {
                        Widget class39_sub5_sub17_143_ = (!bool ? Class44.aClass39_Sub5_Sub17_841
                                : Class43.aClass39_Sub5_Sub17_810);
                        Widget class39_sub5_sub17_144_ = Class37.getWidget((class39_sub5_sub17_143_.anInt2084));
                        class39_sub5_sub17_144_.aClass39_Sub5_Sub17Array2025[class39_sub5_sub17_143_.anInt2102] = null;
                        Deque.method967(class39_sub5_sub17_144_, (byte) -22);
                    } else if (opcode == 102) {
                        Widget class39_sub5_sub17_145_ = Class37.getWidget((Class39_Sub7.intStack[--intStackPointer]));
                        class39_sub5_sub17_145_.aClass39_Sub5_Sub17Array2025 = null;
                        Deque.method967(class39_sub5_sub17_145_, (byte) -22);
                    } else {
                        if (opcode != 200) {
                            break;
                        }
                        intStackPointer -= 2;
                        int i_146_ = Class39_Sub7.intStack[intStackPointer];
                        int i_147_ = Class39_Sub7.intStack[intStackPointer + 1];
                        Widget class39_sub5_sub17_148_ = Class62_Sub2.method1081(i_146_, i_147_, 0);
                        if (class39_sub5_sub17_148_ == null || i_147_ == -1) {
                            Class39_Sub7.intStack[intStackPointer++] = 0;
                        } else {
                            Class39_Sub7.intStack[intStackPointer++] = 1;
                            if (!bool) {
                                Class44.aClass39_Sub5_Sub17_841 = class39_sub5_sub17_148_;
                            } else {
                                Class43.aClass39_Sub5_Sub17_810 = class39_sub5_sub17_148_;
                            }
                        }
                    }
                }
            } catch (Exception exception) {
                Class39_Sub7.method849(exception, 64,
                        ("CS2 - scr:"
                        + currentClientScript.hash + " op:"
                        + opcode));
            }
        }
    }

    public void method515(int i) {
        anInt2312 = i;
        anInt2254 = 0;
    }

    public static void method516(Class38 class38, byte i, int i_149_,
            TraversalMap[] class27s, byte[] is, int i_150_) {
        Buffer class39_sub6 = new Buffer(is);
        int mapDebugPlacements = 0;
        int i_151_ = -1;
        for (;;) {
            int i_152_ = class39_sub6.getSmartB();
            if (i_152_ == 0) {
                break;
            }
            i_151_ += i_152_;
            int i_153_ = 0;
            for (;;) {
                int i_154_ = class39_sub6.getSmartB();
                if (i_154_ == 0) {
                    break;
                }
                i_153_ += i_154_ - 1;
                int i_155_ = i_153_ & 0x3f;
                int i_156_ = class39_sub6.getUbyte();
                int i_157_ = i_153_ >> 12;
                int i_158_ = i_156_ >> 2;
                int i_159_ = i_153_ >> 6 & 0x3f;
                int i_160_ = i_156_ & 0x3;
                int i_161_ = i_155_ + i_150_;
                int i_162_ = i_149_ + i_159_;
                if (i_162_ > 0 && i_161_ > 0 && i_162_ < 103 && i_161_ < 103) {
                    TraversalMap class27 = null;
                    int i_163_ = i_157_;
                    if ((Class55.tileFlags[1][i_162_][i_161_]
                            & 0x2)
                            == 2) {
                        i_163_--;
                    }
                    if (i_163_ >= 0) {
                        class27 = class27s[i_163_];
                    }
                    mapDebugPlacements++;
                    Class62_Sub1.method1059(i_158_, class38, i_161_, i_151_,
                            i_157_, i_162_, class27, true,
                            i_160_);
                }
            }
        }
        System.out.println("[MAPDBG] parsed landscape bytes=" + is.length
                + " baseX=" + i_149_ + " baseY=" + i_150_
                + " inBoundsPlacements=" + mapDebugPlacements);
    }

    public static void method517(byte i) {
        synchronized (Class4.archiveWorkerLock) {
            if (Class23.archiveWorkerKeepAlive != 0) {
                Class23.archiveWorkerKeepAlive = 1;
                try {
                    Class4.archiveWorkerLock.wait();
                } catch (InterruptedException interruptedexception) {
                    /* empty */
                }
            }
        }
    }

    public void method518(int i, int i_164_, boolean bool) {
        int i_165_ = anIntArray2255[0];
        int i_166_ = anIntArray2314[0];
        if ((i_164_ ^ 0xffffffff) == i) {
            i_166_--;
            i_165_++;
        }
        if (anInt2268 != -1
                && Class62_Sub1.method1064(anInt2268, (byte) 54).anInt1837 == 1) {
            anInt2268 = -1;
        }
        if (anInt2312 < 9) {
            anInt2312++;
        }
        if (i_164_ == 1) {
            i_165_++;
        }
        for (int i_167_ = anInt2312; i_167_ > 0; i_167_--) {
            anIntArray2314[i_167_] = anIntArray2314[i_167_ - 1];
            anIntArray2255[i_167_] = anIntArray2255[i_167_ - 1];
            aBooleanArray2284[i_167_] = aBooleanArray2284[i_167_ - 1];
        }
        aBooleanArray2284[0] = bool;
        if (i_164_ == 2) {
            i_165_++;
            i_166_++;
        }
        if (i_164_ == 3) {
            i_166_--;
        }
        if (i_164_ == 4) {
            i_166_++;
        }
        if (i_164_ == 5) {
            i_166_--;
            i_165_--;
        }
        if (i_164_ == 6) {
            i_165_--;
        }
        if (i_164_ == 7) {
            i_166_++;
            i_165_--;
        }
        anIntArray2314[0] = i_166_;
        anIntArray2255[0] = i_165_;
    }

    public Class39_Sub5_Sub4_Sub4() {
        anInt2252 = -1000;
        anInt2254 = 0;
        anInt2250 = 32;
        anInt2264 = -1;
        anInt2257 = -1;
        anInt2271 = 0;
        anInt2267 = 0;
        anInt2259 = 100;
        anInt2260 = -1;
        anInt2262 = -1;
        anInt2282 = -1;
        anInt2263 = -1;
        anIntArray2286 = new int[4];
        anInt2268 = -1;
        anInt2293 = -1;
        anInt2265 = 0;
        anInt2291 = 0;
        anInt2276 = 0;
        aClass3_2295 = null;
        aBoolean2298 = false;
        anInt2296 = 0;
        anInt2303 = -1;
        anIntArray2273 = new int[4];
        anInt2304 = 0;
        anInt2306 = 0;
        anIntArray2258 = new int[4];
        anInt2283 = 0;
        anInt2274 = 0;
        aBooleanArray2284 = new boolean[10];
        anInt2312 = 0;
        anInt2305 = 0;
        anInt2300 = 0;
        anIntArray2314 = new int[10];
        anInt2290 = 0;
        anInt2280 = -1;
        anInt2270 = -1;
        anInt2308 = 200;
        anInt2317 = -1;
        anInt2316 = 0;
        anInt2297 = 1;
        anInt2311 = 0;
    }

    static {
        aClass3_2281 = Class39_Sub5_Sub9.createJstring("Use");
        aClass3_2302 = Class39_Sub5_Sub9.createJstring("Unerwartete Antwort vom Anmelde)2Server)3");
        aClass3_2307 = Class39_Sub5_Sub9.createJstring("Members object");
        aClass3_2309 = Class39_Sub5_Sub9.createJstring("Registrierter Benutzer");
        anInt2299 = 0;
        anInt2313 = 255;
        aClass3_2319 = Class39_Sub5_Sub9.createJstring("(X");
        aClass3_2310 = aClass3_2281;
        aClass3_2289 = aClass3_2307;
    }
}

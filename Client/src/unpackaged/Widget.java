package unpackaged;

import jagex.graphics.BitmapFont;
import jagex.io.BufferedFile;
import jagex.graphics.DrawingArea;
import jagex.utils.HashTable;
import jagex.utils.Huffmans;
import jagex.world.actors.GroundItem;
import jagex.graphics.JImage;
import jagex.graphics.AbstractImage;
import jagex.graphics.sprites.DirectColorSprite;
import jagex.world.actors.Projectile;
import jagex.utils.SubNode;
import jagex.utils.JString;
import jagex.io.JSocket;
import jagex.world.actors.Npc;
import jagex.world.actors.Player;
import jagex.world.map.TraversalMap;
import jagex.utils.Queue;
import jagex.utils.Deque;
import jagex.io.Buffer;
import jagex.utils.Cache;

/* Class39_Sub5_Sub17 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Widget extends SubNode
{
    public DirectColorSprite runtimeSprite;
    /** Bundled sprite shown while the widget's condition is true (IF1 activegraphic). */
    public DirectColorSprite runtimeActiveSprite;
    public int anInt1994;
    public static Cache varbitCache;
    public int anInt1996;
    public int anInt1997;
    public int anInt1998;
    public int anInt1999 = 0;
    public int anInt2000;
    public int[] conditionOpcodes;
    public static JString aClass3_2002;
    public int anInt2003;
    public Object[] anObjectArray2004;
    public static int[] anIntArray2005;
    public static int[] anIntArray2006;
    public int anInt2007;
    public int anInt2008;
    public int anInt2009;
    public int anInt2010;
    public int anInt2011 = 0;
    public int anInt2012;
    public boolean aBoolean2013;
    public boolean aBoolean2014;
    public int quadHeight;
    public Object[] anObjectArray2016;
    public int[] conditionValues;
    public Object[] anObjectArray2018;
    public Object[] anObjectArray2019;
    public int anInt2020;
    public int anInt2021;
    public int anInt2022;
    public JString aClass3_2023;
    public int anInt2024;
    public Widget[] aClass39_Sub5_Sub17Array2025;
    public int anInt2026;
    public JString[] aClass3Array2027;
    public int[] anIntArray2028;
    public JString aClass3_2029;
    public int anInt2030;
    public static int anInt2031;
    public int anInt2032;
    public Object[] anObjectArray2033;
    public int anInt2034;
    public Object[] anObjectArray2035;
    public int anInt2036;
    public int[] anIntArray2037;
    public boolean aBoolean2038;
    public Object[] anObjectArray2039;
    public Widget aClass39_Sub5_Sub17_2040;
    public int anInt2041;
    public boolean aBoolean2042;
    public JString[] aClass3Array2043;
    public Object[] anObjectArray2044;
    public Object[] anObjectArray2045;
    public static JString aClass3_2046;
    public boolean aBoolean2047;
    public JString aClass3_2048;
    public int anInt2049;
    public int anInt2050;
    public int anInt2051;
    public int anInt2052;
    public int[] anIntArray2053;
    public int anInt2054;
    public boolean aBoolean2055;
    public int anInt2056;
    public int anInt2057;
    public int anInt2058;
    public boolean aBoolean2059;
    public static FileLoader fileLoader9;
    public Object[] anObjectArray2061;
    public Object[] anObjectArray2062;
    public static int anInt2063;
    public int anInt2064;
    public JString aClass3_2065;
    public JString aClass3_2066;
    public int inactiveQuadColor;
    public JString aClass3_2068;
    public int anInt2069;
    public static JString aClass3_2070
	= Class39_Sub5_Sub9.createJstring("oder benutzen Sie eine andere Welt)3");
    public static int anInt2071;
    public int anInt2072;
    public int[] anIntArray2073;
    public int anInt2074;
    public Object[] anObjectArray2075;
    public boolean drawSolidQuad;
    public int activeQuadColor;
    public int anInt2078;
    public int anInt2079;
    public Object[] anObjectArray2080;
    public boolean aBoolean2081;
    public int anInt2082;
    public int anInt2083;
    public int anInt2084;
    public Object[] anObjectArray2085;
    public int anInt2086;
    public int[] anIntArray2087;
    public Object[] anObjectArray2088;
    public int anInt2089;
    public int anInt2090;
    public int anInt2091;
    public int[][] scriptOpcodes;
    public int anInt2093;
    public int type;
    public int anInt2095;
    public int anInt2096;
    public Object[] anObjectArray2097;
    public int anInt2098;
    public Object[] anObjectArray2099;
    public Object[] anObjectArray2100;
    public Object[] anObjectArray2101;
    public int anInt2102;
    public int anInt2103;
    public int quadWidth;
    public int anInt2105;
    public boolean aBoolean2106;
    public boolean aBoolean2107;
    public boolean aBoolean2108;
    public static int[] anIntArray2109;
    public static JString aClass3_2110;
    public static int[] anIntArray2111;
    public static int[] anIntArray2112;
    public static DirectColorSprite aClass39_Sub5_Sub10_Sub3_2113;
    public static AbstractImage aClass57_2114;
    public static AbstractImage aClass57_2115;
    public static boolean aBoolean2116;
    public static JString aClass3_2117;
    
    public boolean method750(int i) {
	if ((anInt2049 >> 21 & 0x1) == 0)
	    return false;
	return true;
    }
    
    public int method751(byte i) {
	return anInt2049 >> 11 & 0x3f;
    }
    
    public BitmapFont getFont() {
        if ((anInt2084 >>> 16) >= 500 && (anInt2084 >>> 16) <= 504) {
            if (anInt2105 == 496 && Class32.aClass39_Sub5_Sub10_Sub1_587 != null)
                return Class32.aClass39_Sub5_Sub10_Sub1_587;
            if (anInt2105 == 494 && Npc.aClass39_Sub5_Sub10_Sub1_2495 != null)
                return Npc.aClass39_Sub5_Sub10_Sub1_2495;
            return Class39_Sub5_Sub14.p12fullFont;
        }
	Class39_Sub5_Sub12.aBoolean1856 = false;
	if (anInt2105 == -1)
	    return null;
	BitmapFont font = ((BitmapFont) Deque.fontCache.get((long) anInt2105));
	if (font != null)
	    return font;
	font = Queue.createBitmapFont(Class37.aClass9_658, anInt2105, 0);
	if (font != null)
	    Deque.fontCache.put(font, (long) anInt2105, (byte) -78);
	else
	    Class39_Sub5_Sub12.aBoolean1856 = true;
	return font;
    }
    
    public boolean method753(byte i) {
	if ((anInt2049 >> 28 & 0x1) == 0)
	    return false;
	return true;
    }
    
    public boolean method754(int i, boolean bool) {
	if (bool) {
	    if (anInt2049 != 0)
		return false;
	    if (type == 0)
		return false;
	}
	return aBoolean2055;
    }
    
    public void decodeOldFormat(Buffer buffer) {
	aBoolean2013 = false;
	type = buffer.getUbyte();
	anInt2089 = buffer.getUbyte();
	anInt2078 = buffer.getUword();
	anInt2090 = anInt2091 = buffer.getWord();
	anInt2024 = anInt2021 = buffer.getWord();
	quadWidth = buffer.getUword();
	quadHeight = buffer.getUword();
	anInt2030 = buffer.getUbyte();
	anInt2050 = buffer.getUword();
	if (anInt2050 == 65535)
	    anInt2050 = -1;
	else
	    anInt2050 = anInt2050 + (anInt2084 & ~0xffff);
	anInt2057 = buffer.getUword();
	if (anInt2057 == 65535)
	    anInt2057 = -1;
	int i_0_ = buffer.getUbyte();
	if (i_0_ > 0) {
	    conditionOpcodes = new int[i_0_];
	    conditionValues = new int[i_0_];
	    for (int i_1_ = 0; i_0_ > i_1_; i_1_++) {
		conditionOpcodes[i_1_] = buffer.getUbyte();
		conditionValues[i_1_] = buffer.getUword();
	    }
	}
	int i_2_ = buffer.getUbyte();
	if (i_2_ > 0) {
	    scriptOpcodes = new int[i_2_][];
	    for (int i_3_ = 0; i_2_ > i_3_; i_3_++) {
		int i_4_ = buffer.getUword();
		scriptOpcodes[i_3_] = new int[i_4_];
		for (int i_5_ = 0; i_4_ > i_5_; i_5_++) {
		    scriptOpcodes[i_3_][i_5_]
			= buffer.getUword();
		    if (scriptOpcodes[i_3_][i_5_] == 65535)
			scriptOpcodes[i_3_][i_5_] = -1;
		}
	    }
	}
	if (type == 0) {
	    anInt2095 = buffer.getUword();
	    aBoolean2055 = buffer.getUbyte() == 1;
	}
	if (type == 1) {
	    buffer.getUword();
	    buffer.getUbyte();
	}
	if (type == 2) {
	    anIntArray2087 = new int[quadWidth * quadHeight];
	    anIntArray2073 = new int[quadHeight * quadWidth];
	    int i_6_ = buffer.getUbyte();
	    if (i_6_ == 1)
		anInt2049 |= 0x10000000;
	    int i_7_ = buffer.getUbyte();
	    if (i_7_ == 1)
		anInt2049 |= 0x40000000;
	    int i_8_ = buffer.getUbyte();
	    if (i_8_ == 1)
		anInt2049 |= ~0x7fffffff;
	    int i_9_ = buffer.getUbyte();
	    if (i_9_ == 1)
		anInt2049 |= 0x20000000;
	    anInt2000 = buffer.getUbyte();
	    anInt2010 = buffer.getUbyte();
	    anIntArray2037 = new int[20];
	    anIntArray2028 = new int[20];
	    anIntArray2053 = new int[20];
	    for (int i_10_ = 0; i_10_ < 20; i_10_++) {
		int i_11_ = buffer.getUbyte();
		if (i_11_ == 1) {
		    anIntArray2028[i_10_] = buffer.getWord();
		    anIntArray2037[i_10_] = buffer.getWord();
		    anIntArray2053[i_10_] = buffer.getDword();
		} else
		    anIntArray2053[i_10_] = -1;
	    }
	    aClass3Array2043 = new JString[5];
	    for (int i_12_ = 0; i_12_ < 5; i_12_++) {
		JString class3 = buffer.getJstr();
		if (class3.getLength() > 0) {
		    aClass3Array2043[i_12_] = class3;
		    anInt2049 |= 1 << i_12_ + 23;
		}
	    }
	}
	if (type == 3)
	    drawSolidQuad = buffer.getUbyte() == 1;
	if (type == 4 || type == 1) {
	    anInt2032 = buffer.getUbyte();
	    anInt1996 = buffer.getUbyte();
	    anInt2036 = buffer.getUbyte();
	    anInt2105 = buffer.getUword();
	    if (anInt2105 == 65535)
		anInt2105 = -1;
	    aBoolean2059 = buffer.getUbyte() == 1;
	}
	if (type == 4) {
	    aClass3_2029 = buffer.getJstr();
	    aClass3_2048 = buffer.getJstr();
	}
	if (type == 1 || type == 3 || type == 4)
	    activeQuadColor = buffer.getDword();
	if (type == 3 || type == 4) {
	    inactiveQuadColor = buffer.getDword();
	    anInt2041 = buffer.getDword();
	    anInt2086 = buffer.getDword();
	}
	if (type == 5) {
	    anInt2093 = buffer.getDword();
	    anInt2034 = buffer.getDword();
	}
	if (type == 6) {
	    anInt2009 = 1;
	    anInt2026 = buffer.getUword();
	    anInt2082 = 1;
	    if (anInt2026 == 65535)
		anInt2026 = -1;
	    anInt2054 = buffer.getUword();
	    if (anInt2054 == 65535)
		anInt2054 = -1;
	    anInt2103 = buffer.getUword();
	    if (anInt2103 == 65535)
		anInt2103 = -1;
	    anInt2052 = buffer.getUword();
	    if (anInt2052 == 65535)
		anInt2052 = -1;
	    anInt2074 = buffer.getUword();
	    anInt2098 = buffer.getUword();
	    anInt2011 = buffer.getUword();
	}
	if (type == 7) {
	    anIntArray2073 = new int[quadHeight * quadWidth];
	    anIntArray2087 = new int[quadHeight * quadWidth];
	    anInt2032 = buffer.getUbyte();
	    anInt2105 = buffer.getUword();
	    if (anInt2105 == 65535)
		anInt2105 = -1;
	    aBoolean2059 = buffer.getUbyte() == 1;
	    activeQuadColor = buffer.getDword();
	    anInt2000 = buffer.getWord();
	    anInt2010 = buffer.getWord();
	    int i_13_ = buffer.getUbyte();
	    aClass3Array2043 = new JString[5];
	    if (i_13_ == 1)
		anInt2049 |= 0x40000000;
	    for (int i_14_ = 0; i_14_ < 5; i_14_++) {
		JString jstr = buffer.getJstr();
		if (jstr.getLength() > 0) {
		    aClass3Array2043[i_14_] = jstr;
		    anInt2049 |= 1 << 23 + i_14_;
		}
	    }
	}
	if (type == 8)
	    aClass3_2029 = buffer.getJstr();
	if (anInt2089 == 2 || type == 2) {
	    aClass3_2023 = buffer.getJstr();
	    aClass3_2066 = buffer.getJstr();
	    int i_15_ = buffer.getUword() & 0x3f;
	    anInt2049 |= i_15_ << 11;
	}
	if (anInt2089 == 1 || anInt2089 == 4 || anInt2089 == 5
	    || anInt2089 == 6) {
	    aClass3_2068 = buffer.getJstr();
	    if (aClass3_2068.getLength() == 0) {
		if (anInt2089 == 1)
		    aClass3_2068 = Class23.aClass3_437;
		if (anInt2089 == 4)
		    aClass3_2068 = HashTable.aClass3_383;
		if (anInt2089 == 5)
		    aClass3_2068 = HashTable.aClass3_383;
		if (anInt2089 == 6)
		    aClass3_2068 = Class46.aClass3_874;
	    }
	}
	if (anInt2089 == 1 || anInt2089 == 4 || anInt2089 == 5)
	    anInt2049 |= 0x400000;
	if (anInt2089 == 6)
	    anInt2049 |= 0x1;
	anInt1998 = anInt2049;
    }
    
    public void method756(int i, JString class3, int i_16_) {
	if (aClass3Array2027 == null || i_16_ >= aClass3Array2027.length) {
	    JString[] class3s = new JString[i_16_ + 1];
	    if (aClass3Array2027 != null) {
		for (int i_17_ = 0; i_17_ < aClass3Array2027.length; i_17_++)
		    class3s[i_17_] = aClass3Array2027[i_17_];
	    }
	    aClass3Array2027 = class3s;
	}
	aClass3Array2027[i_16_] = class3;
    }
    
    public void decodeNewFormat(Buffer buffer) {
	buffer.getUbyte();
	aBoolean2013 = true;
	type = buffer.getUbyte();
	anInt2078 = buffer.getUword();
	anInt2090 = anInt2091 = buffer.getWord();
	anInt2024 = anInt2021 = buffer.getWord();
	quadWidth = buffer.getUword();
	if (type != 9)
	    quadHeight = buffer.getUword();
	else
	    quadHeight = buffer.getWord();
	anInt2050 = buffer.getUword();
	if (anInt2050 == 65535)
	    anInt2050 = -1;
	else
	    anInt2050 = anInt2050 + (anInt2084 & ~0xffff);
	aBoolean2055 = buffer.getUbyte() == 1;
	if (type == 0) {
	    anInt2020 = buffer.getUword();
	    anInt2095 = buffer.getUword();
	}
	if (type == 5) {
	    anInt2093 = buffer.getDword();
	    anInt2051 = buffer.getUword();
	    aBoolean2014 = buffer.getUbyte() == 1;
	    anInt2030 = buffer.getUbyte();
	    anInt2022 = buffer.getUbyte();
	    anInt2003 = buffer.getDword();
	    aBoolean2107 = buffer.getUbyte() == 1;
	    aBoolean2038 = buffer.getUbyte() == 1;
	}
	if (type == 6) {
	    anInt2009 = 1;
	    anInt2026 = buffer.getUword();
	    if (anInt2026 == 65535)
		anInt2026 = -1;
	    anInt2072 = buffer.getWord();
	    anInt2058 = buffer.getWord();
	    anInt2098 = buffer.getUword();
	    anInt2011 = buffer.getUword();
	    anInt2007 = buffer.getUword();
	    anInt2074 = buffer.getUword();
	    anInt2103 = buffer.getUword();
	    if (anInt2103 == 65535)
		anInt2103 = -1;
	    aBoolean2081 = buffer.getUbyte() == 1;
	}
	if (type == 4) {
	    anInt2105 = buffer.getUword();
	    if (anInt2105 == 65535)
		anInt2105 = -1;
	    aClass3_2029 = buffer.getJstr();
	    anInt2036 = buffer.getUbyte();
	    anInt2032 = buffer.getUbyte();
	    anInt1996 = buffer.getUbyte();
	    aBoolean2059 = buffer.getUbyte() == 1;
	    activeQuadColor = buffer.getDword();
	}
	if (type == 3) {
	    activeQuadColor = buffer.getDword();
	    drawSolidQuad = buffer.getUbyte() == 1;
	    anInt2030 = buffer.getUbyte();
	}
	if (type == 9) {
	    anInt2083 = buffer.getUbyte();
	    activeQuadColor = buffer.getDword();
	}
	anInt2049 = anInt1998 = buffer.getUtri((byte) -62);
	aClass3_2065 = buffer.getJstr();
	int i = buffer.getUbyte();
	if (i > 0) {
	    aClass3Array2027 = new JString[i];
	    for (int i_18_ = 0; i > i_18_; i_18_++)
		aClass3Array2027[i_18_] = buffer.getJstr();
	}
	anInt2056 = buffer.getUbyte();
	anInt2008 = buffer.getUbyte();
	aBoolean2108 = buffer.getUbyte() == 1;
	aClass3_2023 = buffer.getJstr();
	anObjectArray2033 = getScriptParams(buffer);
	anObjectArray2019 = getScriptParams(buffer);
	anObjectArray2100 = getScriptParams(buffer);
	anObjectArray2004 = getScriptParams(buffer);
	anObjectArray2099 = getScriptParams(buffer);
	anObjectArray2101 = getScriptParams(buffer);
	anObjectArray2061 = getScriptParams(buffer);
	anObjectArray2062 = getScriptParams(buffer);
	anObjectArray2016 = getScriptParams(buffer);
	anObjectArray2045 = getScriptParams(buffer);
	anObjectArray2075 = getScriptParams(buffer);
	anObjectArray2035 = getScriptParams(buffer);
	anObjectArray2044 = getScriptParams(buffer);
	anObjectArray2085 = getScriptParams(buffer);
	anObjectArray2080 = getScriptParams(buffer);
	anObjectArray2018 = getScriptParams(buffer);
	anObjectArray2088 = getScriptParams(buffer);
	anObjectArray2097 = getScriptParams(buffer);
	anObjectArray2039 = getScriptParams(buffer);
    }
    
    public boolean method758(int i) {
	if ((anInt2049 >> 31 & 0x1) == 0)
	    return false;
	return true;
    }
    
    public DirectColorSprite method759(int i, int i_19_) {
	Class39_Sub5_Sub12.aBoolean1856 = false;
	if ((i ^ 0xffffffff) > i_19_ || i >= anIntArray2053.length)
	    return null;
	int i_20_ = anIntArray2053[i];
	if (i_20_ == -1)
	    return null;
	DirectColorSprite class39_sub5_sub10_sub3
	    = ((DirectColorSprite)
	       Class62_Sub1.aClass7_1587.get((long) i_20_));
	if (class39_sub5_sub10_sub3 != null)
	    return class39_sub5_sub10_sub3;
	class39_sub5_sub10_sub3
	    = Class45.method923(Class37.aClass9_658, 0, i_20_, false);
	if (class39_sub5_sub10_sub3 == null)
	    Class39_Sub5_Sub12.aBoolean1856 = true;
	else
	    Class62_Sub1.aClass7_1587.put(class39_sub5_sub10_sub3,
						(long) i_20_, (byte) -114);
	return class39_sub5_sub10_sub3;
    }
    
    public void method760(int i, int i_21_, int i_22_) {
	int i_23_ = anIntArray2087[i_21_];
	anIntArray2087[i_21_] = anIntArray2087[i];
	anIntArray2087[i] = i_23_;
	i_23_ = anIntArray2073[i_21_];
	anIntArray2073[i_21_] = anIntArray2073[i];
	anIntArray2073[i] = i_23_;
    }
    
    public static void method761(byte i) {
	if (i != 45)
	    aBoolean2116 = true;
	Varbit.method597(103);
	if (Class39_Sub5_Sub7.minimapState == 2) {
	    byte[] is = Class47.aClass39_Sub5_Sub10_Sub4_891.index;
	    int i_24_ = is.length;
	    int[] is_25_ = DrawingArea.buffer;
	    for (int i_26_ = 0; i_24_ > i_26_; i_26_++) {
		if (is[i_26_] == 0)
		    is_25_[i_26_] = 0;
	    }
	    FileTable.aClass39_Sub5_Sub10_Sub3_141.method688(0, 0, 33, 33, 25, 25,
							  Class34.anInt605,
							  256,
							  (Class46_Sub1
							   .anIntArray1564),
							  anIntArray2109);
	    Class66.method1104(false);
	} else {
	    int i_27_ = Class34.anInt605 + ArchiveRequest.anInt1401 & 0x7ff;
	    int i_28_
		= 464 - Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275 / 32;
	    int i_29_
		= 48 + Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301 / 32;
	    Class39_Sub5_Sub4_Sub2.aClass39_Sub5_Sub10_Sub3_2219.method688
		(25, 5, 146, 151, i_29_, i_28_, i_27_,
		 Class39_Sub7.anInt1386 + 256, BufferedFile.anIntArray345,
		 Class32.anIntArray586);
	    for (int i_30_ = 0; i_30_ < Deque.anInt915; i_30_++) {
		i_29_
		    = (Class46_Sub1.anIntArray1549[i_30_] * 4
		       - Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301 / 32
		       + 2);
		i_28_ = -(Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275
			  / 32) + 2 + anIntArray2111[i_30_] * 4;
		Player.method528
		    (i_28_, Class31.aClass39_Sub5_Sub10_Sub3Array560[i_30_],
		     i_29_, 10064);
	    }
	    for (int i_31_ = 0; i_31_ < 104; i_31_++) {
		for (int i_32_ = 0; i_32_ < 104; i_32_++) {
		    Deque class49 = (Class20.groundItems
				       [NameTable.height][i_31_][i_32_]);
		    if (class49 != null) {
			i_29_ = -((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				   .anInt2301)
				  / 32) + (i_31_ * 4 + 2);
			i_28_ = -((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				   .anInt2275)
				  / 32) + 2 + i_32_ * 4;
			Player.method528
			    (i_28_,
			     (Class39_Sub5_Sub16
			      .aClass39_Sub5_Sub10_Sub3Array1986[0]),
			     i_29_, 10064);
		    }
		}
	    }
	    for (int i_33_ = 0; i_33_ < ArchiveWorker.anInt1210; i_33_++) {
		Npc class39_sub5_sub4_sub4_sub1
		    = (GroundItem
		       .aClass39_Sub5_Sub4_Sub4_Sub1Array2241
		       [Class39_Sub5_Sub4.anIntArray1734[i_33_]]);
		if (class39_sub5_sub4_sub4_sub1 != null
		    && class39_sub5_sub4_sub4_sub1.method510((byte) -77)) {
		    NpcDefinition class39_sub5_sub13
			= class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492;
		    if (class39_sub5_sub13.anIntArray1878 != null)
			class39_sub5_sub13
			    = class39_sub5_sub13.method721(5585);
		    if (class39_sub5_sub13 != null
			&& class39_sub5_sub13.aBoolean1888
			&& class39_sub5_sub13.aBoolean1886) {
			i_29_ = (-((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				    .anInt2301)
				   / 32)
				 + class39_sub5_sub4_sub4_sub1.anInt2301 / 32);
			i_28_ = (class39_sub5_sub4_sub4_sub1.anInt2275 / 32
				 - (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				    .anInt2275) / 32);
			Player.method528
			    (i_28_,
			     (Class39_Sub5_Sub16
			      .aClass39_Sub5_Sub10_Sub3Array1986[1]),
			     i_29_, 10064);
		    }
		}
	    }
            ChristmasEventMinimap.draw();
            for (int i_34_ = 0; i_34_ < TraversalMap.anInt515; i_34_++) {
		Player class39_sub5_sub4_sub4_sub2
		    = (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211
		       [Class1.anIntArray40[i_34_]]);
		if (class39_sub5_sub4_sub4_sub2 != null
		    && class39_sub5_sub4_sub4_sub2.method510((byte) -77)) {
		    i_29_
			= (class39_sub5_sub4_sub4_sub2.anInt2301 / 32
			   - (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301
			      / 32));
		    i_28_
			= (class39_sub5_sub4_sub4_sub2.anInt2275 / 32
			   - (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275
			      / 32));
		    boolean bool = false;
		    long l = class39_sub5_sub4_sub4_sub2.aClass3_2521
				 .encodeBase37();
		    for (int i_35_ = 0; i_35_ < Class4.anInt62; i_35_++) {
			if (ClientApplet.aLongArray2[i_35_] == l
			    && (Player.anIntArray2533
				[i_35_]) != 0) {
			    bool = true;
			    break;
			}
		    }
		    boolean bool_36_ = false;
		    if (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2526 != 0
			&& class39_sub5_sub4_sub4_sub2.anInt2526 != 0
			&& (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2526
			    == class39_sub5_sub4_sub4_sub2.anInt2526))
			bool_36_ = true;
		    if (bool)
			Player.method528
			    (i_28_,
			     (Class39_Sub5_Sub16
			      .aClass39_Sub5_Sub10_Sub3Array1986[3]),
			     i_29_, 10064);
		    else if (bool_36_)
			Player.method528
			    (i_28_,
			     (Class39_Sub5_Sub16
			      .aClass39_Sub5_Sub10_Sub3Array1986[4]),
			     i_29_, i + 10019);
		    else
			Player.method528
			    (i_28_,
			     (Class39_Sub5_Sub16
			      .aClass39_Sub5_Sub10_Sub3Array1986[2]),
			     i_29_, i + 10019);
		}
	    }
	    if (JMouseListener.anInt787 != 0 && Class2.logicCycle % 20 < 10) {
		if (JMouseListener.anInt787 == 1 && Class30.anInt542 >= 0
		    && ((GroundItem
			 .aClass39_Sub5_Sub4_Sub4_Sub1Array2241).length
			> Class30.anInt542)) {
		    Npc class39_sub5_sub4_sub4_sub1
			= (GroundItem
			   .aClass39_Sub5_Sub4_Sub4_Sub1Array2241
			   [Class30.anInt542]);
		    if (class39_sub5_sub4_sub4_sub1 != null) {
			i_28_ = (class39_sub5_sub4_sub4_sub1.anInt2275 / 32
				 - (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				    .anInt2275) / 32);
			i_29_ = (-((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				    .anInt2301)
				   / 32)
				 + class39_sub5_sub4_sub4_sub1.anInt2301 / 32);
			Projectile.method494
			    (13552,
			     Class36.aClass39_Sub5_Sub10_Sub3Array648[1],
			     i_29_, i_28_);
		    }
		}
		if (JMouseListener.anInt787 == 2) {
		    i_28_
			= ((Class25.anInt471 - JKeyListener.anInt618) * 4 + 2
			   - (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275
			      / 32));
		    i_29_
			= ((-Class65.anInt1145 + JString.anInt1229) * 4 + 2
			   - (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301
			      / 32));
		    Projectile.method494
			(i + 13507,
			 Class36.aClass39_Sub5_Sub10_Sub3Array648[1], i_29_,
			 i_28_);
		}
		if (JMouseListener.anInt787 == 10 && Class34.anInt607 >= 0
		    && (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211.length
			> Class34.anInt607)) {
		    Player class39_sub5_sub4_sub4_sub2
			= (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211
			   [Class34.anInt607]);
		    if (class39_sub5_sub4_sub4_sub2 != null) {
			i_29_ = (class39_sub5_sub4_sub4_sub2.anInt2301 / 32
				 - (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				    .anInt2301) / 32);
			i_28_ = (class39_sub5_sub4_sub4_sub2.anInt2275 / 32
				 - (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				    .anInt2275) / 32);
			Projectile.method494
			    (13552,
			     Class36.aClass39_Sub5_Sub10_Sub3Array648[1],
			     i_29_, i_28_);
		    }
		}
	    }
	    if (Class30.anInt544 != 0) {
		i_29_ = -(Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301
			  / 32) + (Class30.anInt544 * 4 + 2);
		i_28_
		    = (ArchiveRequest.anInt1407 * 4
		       - Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275 / 32
		       + 2);
		Player.method528
		    (i_28_, Class36.aClass39_Sub5_Sub10_Sub3Array648[0], i_29_,
		     10064);
	    }
	    DrawingArea.drawQuad(97, 78, 3, 3, 16777215);
	    FileTable.aClass39_Sub5_Sub10_Sub3_141.method688(0, 0, 33, 33, 25, 25,
							  Class34.anInt605,
							  256,
							  (Class46_Sub1
							   .anIntArray1564),
							  anIntArray2109);
	    Class66.method1104(false);
	}
    }
    
    public JString method762(boolean bool, int i) {
	if (method751((byte) -19) == 0)
	    return null;
	if (i != 64)
	    method753((byte) -56);
	if (aClass3_2023 == null
	    || aClass3_2023.method69((byte) -45).getLength() == 0) {
	    if (bool)
		return TraversalMap.aClass3_509;
	    return null;
	}
	return aClass3_2023;
    }
    
    public static void method763(int i) {
	if (i == -4322) {
	    for (int i_37_ = 0; i_37_ < JImage.anInt1586; i_37_++) {
		int i_38_ = ArchiveRequest.anIntArray1400[i_37_];
		Npc class39_sub5_sub4_sub4_sub1
		    = (GroundItem
		       .aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_38_]);
		int i_39_ = Class39_Sub5_Sub11.gameBuffer
				.getUbyte();
		if ((i_39_ & 0x2) != 0) {
		    class39_sub5_sub4_sub4_sub1.anInt2316
			= Class39_Sub5_Sub11.gameBuffer
			      .getUword();
		    class39_sub5_sub4_sub4_sub1.anInt2300
			= Class39_Sub5_Sub11.gameBuffer
			      .getUwordLe();
		}
		if ((i_39_ & 0x20) != 0) {
		    int i_40_ = Class39_Sub5_Sub11.gameBuffer
				    .method833((byte) 127);
		    int i_41_ = Class39_Sub5_Sub11.gameBuffer
				    .method788((byte) -98);
		    if (i_40_ == 65535)
			i_40_ = -1;
		    if (class39_sub5_sub4_sub4_sub1.anInt2268 == i_40_
			&& i_40_ != -1) {
			int i_42_ = (Class62_Sub1.method1064(i_40_, (byte) 54)
				     .anInt1830);
			if (i_42_ == 1) {
			    class39_sub5_sub4_sub4_sub1.anInt2291 = 0;
			    class39_sub5_sub4_sub4_sub1.anInt2305 = i_41_;
			    class39_sub5_sub4_sub4_sub1.anInt2265 = 0;
			    class39_sub5_sub4_sub4_sub1.anInt2311 = 0;
			}
			if (i_42_ == 2)
			    class39_sub5_sub4_sub4_sub1.anInt2291 = 0;
		    } else if (i_40_ == -1
			       || class39_sub5_sub4_sub4_sub1.anInt2268 == -1
			       || ((Class62_Sub1.method1064(i_40_, (byte) 54)
				    .anInt1826)
				   >= (Class62_Sub1.method1064
				       (class39_sub5_sub4_sub4_sub1.anInt2268,
					(byte) 54)
				       .anInt1826))) {
			class39_sub5_sub4_sub4_sub1.anInt2305 = i_41_;
			class39_sub5_sub4_sub4_sub1.anInt2311 = 0;
			class39_sub5_sub4_sub4_sub1.anInt2291 = 0;
			class39_sub5_sub4_sub4_sub1.anInt2254
			    = class39_sub5_sub4_sub4_sub1.anInt2312;
			class39_sub5_sub4_sub4_sub1.anInt2265 = 0;
			class39_sub5_sub4_sub4_sub1.anInt2268 = i_40_;
		    }
		}
		if ((i_39_ & 0x40) != 0) {
		    int i_43_ = Class39_Sub5_Sub11.gameBuffer
				    .method788((byte) -98);
		    int i_44_ = Class39_Sub5_Sub11.gameBuffer
				    .method788((byte) 91);
		    class39_sub5_sub4_sub4_sub1
			.method513(i + 4241, Class2.logicCycle, i_43_, i_44_);
		    class39_sub5_sub4_sub4_sub1.anInt2252
			= Class2.logicCycle + 300;
		    class39_sub5_sub4_sub4_sub1.anInt2318
			= Class39_Sub5_Sub11.gameBuffer
			      .getUbyte();
		    class39_sub5_sub4_sub4_sub1.anInt2269
			= Class39_Sub5_Sub11.gameBuffer
			      .method815((byte) -109);
		}
		if ((i_39_ & 0x4) != 0) {
		    class39_sub5_sub4_sub4_sub1.anInt2270
			= Class39_Sub5_Sub11.gameBuffer
			      .method833((byte) 124);
		    int i_45_ = Class39_Sub5_Sub11.gameBuffer
				    .method812(i ^ ~0x2ef905f9);
		    class39_sub5_sub4_sub4_sub1.anInt2288 = i_45_ >> 16;
		    class39_sub5_sub4_sub4_sub1.anInt2276 = 0;
		    if (class39_sub5_sub4_sub4_sub1.anInt2270 == 65535)
			class39_sub5_sub4_sub4_sub1.anInt2270 = -1;
		    class39_sub5_sub4_sub4_sub1.anInt2304 = 0;
		    class39_sub5_sub4_sub4_sub1.anInt2272
			= (i_45_ & 0xffff) + Class2.logicCycle;
		    if (Class2.logicCycle < class39_sub5_sub4_sub4_sub1.anInt2272)
			class39_sub5_sub4_sub4_sub1.anInt2276 = -1;
		}
		if ((i_39_ & 0x10) != 0) {
		    class39_sub5_sub4_sub4_sub1.anInt2260
			= Class39_Sub5_Sub11.gameBuffer
			      .getUwordLe();
		    if (class39_sub5_sub4_sub4_sub1.anInt2260 == 65535)
			class39_sub5_sub4_sub4_sub1.anInt2260 = -1;
		}
		if ((i_39_ & 0x80) != 0) {
		    int i_46_ = Class39_Sub5_Sub11.gameBuffer
				    .method804(i + 4438);
		    int i_47_ = Class39_Sub5_Sub11.gameBuffer
				    .method804(100);
		    class39_sub5_sub4_sub4_sub1
			.method513(i + 4425, Class2.logicCycle, i_46_, i_47_);
		    class39_sub5_sub4_sub4_sub1.anInt2252
			= Class2.logicCycle + 300;
		    class39_sub5_sub4_sub4_sub1.anInt2318
			= Class39_Sub5_Sub11.gameBuffer
			      .getUbyte();
		    class39_sub5_sub4_sub4_sub1.anInt2269
			= Class39_Sub5_Sub11.gameBuffer
			      .method804(51);
		}
		if ((i_39_ & 0x1) != 0) {
		    class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
			= ArchiveWorker.getNpcDefinition(Class39_Sub5_Sub11
						 .gameBuffer
						 .getUwordLe());
		    class39_sub5_sub4_sub4_sub1.anInt2317
			= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
			   .anInt1887);
		    class39_sub5_sub4_sub4_sub1.anInt2280
			= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
			   .anInt1875);
		    class39_sub5_sub4_sub4_sub1.anInt2282
			= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
			   .anInt1879);
		    class39_sub5_sub4_sub4_sub1.anInt2264
			= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
			   .anInt1861);
		    class39_sub5_sub4_sub4_sub1.anInt2262
			= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
			   .anInt1877);
		    class39_sub5_sub4_sub4_sub1.anInt2263
			= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
			   .anInt1872);
		    class39_sub5_sub4_sub4_sub1.anInt2250
			= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
			   .anInt1882);
		    class39_sub5_sub4_sub4_sub1.anInt2257
			= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
			   .anInt1863);
		    class39_sub5_sub4_sub4_sub1.anInt2297
			= (class39_sub5_sub4_sub4_sub1.aClass39_Sub5_Sub13_2492
			   .anInt1870);
		}
		if ((i_39_ & 0x8) != 0) {
		    class39_sub5_sub4_sub4_sub1.aClass3_2295
			= Class39_Sub5_Sub11.gameBuffer
			      .getJstr();
		    class39_sub5_sub4_sub4_sub1.anInt2259 = 100;
		}
	    }
	}
    }
    
    public static DirectColorSprite[] method764(int i) {
	DirectColorSprite[] class39_sub5_sub10_sub3s
	    = new DirectColorSprite[JSocket.anInt302];
	for (int i_48_ = 0; i_48_ < JSocket.anInt302; i_48_++) {
	    DirectColorSprite class39_sub5_sub10_sub3
		= (class39_sub5_sub10_sub3s[i_48_]
		   = new DirectColorSprite());
	    class39_sub5_sub10_sub3.anInt2475 = Class13.anInt203;
	    class39_sub5_sub10_sub3.anInt2477 = Class39_Sub5_Sub12.anInt1854;
	    class39_sub5_sub10_sub3.anInt2473
		= Class46_Sub1.anIntArray1548[i_48_];
	    class39_sub5_sub10_sub3.anInt2472
		= SubNode.anIntArray1352[i_48_];
	    class39_sub5_sub10_sub3.width
		= Class39_Sub5_Sub9.anIntArray1799[i_48_];
	    class39_sub5_sub10_sub3.height
		= Class39_Sub14.anIntArray1512[i_48_];
	    int i_49_ = (class39_sub5_sub10_sub3.height
			 * class39_sub5_sub10_sub3.width);
	    byte[] is = TraversalMap.aByteArrayArray517[i_48_];
	    class39_sub5_sub10_sub3.anIntArray2476 = new int[i_49_];
	    for (int i_50_ = 0; i_49_ > i_50_; i_50_++)
		class39_sub5_sub10_sub3.anIntArray2476[i_50_]
		    = Class39_Sub11.anIntArray1460[Class34.method342(is[i_50_],
								     255)];
	}
	RuntimeException_Sub1.method1123();
	return class39_sub5_sub10_sub3s;
    }
    
    public static void method765(byte i) {
	aClass57_2115 = null;
	fileLoader9 = null;
	anIntArray2109 = null;
	anIntArray2006 = null;
	aClass3_2070 = null;
	anIntArray2111 = null;
	aClass3_2117 = null;
	anIntArray2005 = null;
	aClass3_2110 = null;
	aClass57_2114 = null;
	varbitCache = null;
	aClass3_2046 = null;
	anIntArray2112 = null;
	aClass3_2002 = null;
	aClass39_Sub5_Sub10_Sub3_2113 = null;
    }
    
    public Object[] getScriptParams(Buffer buffer) {
	int amountParams = buffer.getUbyte();
	if (amountParams == 0)
	    return null;
	Object[] params = new Object[amountParams];
	for (int i = 0; i < amountParams; i++) {
	    int type = buffer.getUbyte();
	    if (type != 0) {
		if (type == 1)
		    params[i] = buffer.getJstr();
	    } else
		params[i] = new Integer(buffer.getDword());
	}
	aBoolean2047 = true;
	return params;
    }
    
    public Widget method767(byte i) {
	int i_54_ = (anInt2049 & 0xed60d) >> 17;
	if (i_54_ == 0)
	    return null;
	Widget class39_sub5_sub17_55_ = this;
	for (int i_56_ = 0; i_56_ < i_54_; i_56_++) {
	    class39_sub5_sub17_55_
		= Class37.getWidget(class39_sub5_sub17_55_.anInt2050);
	    if (class39_sub5_sub17_55_ == null)
		return null;
	}
	return class39_sub5_sub17_55_;
    }
    
    public boolean method768(int i) {
	if ((anInt2049 & 0x1) == 0)
	    return false;
	return true;
    }
    
    public boolean method769(int i) {
	if ((anInt2049 & 0x542c2685) >> 30 == 0)
	    return false;
	return true;
    }
    
    public boolean method770(int i, int i_57_) {
	if (i != -12755)
	    return true;
	if ((anInt2049 >> i_57_ + 1 & 0x1) == 0)
	    return false;
	return true;
    }
    
    public JString method771(boolean bool, byte i, int i_58_) {
	int i_59_ = 102 % ((i + 12) / 52);
	if (!method770(-12755, i_58_))
	    return null;
	if (aClass3Array2027 == null || aClass3Array2027.length <= i_58_
	    || aClass3Array2027[i_58_] == null
	    || (aClass3Array2027[i_58_].method69((byte) -45).getLength()
		== 0)) {
	    if (bool)
		return (Class39_Sub5_Sub11.method708
			(new JString[] { Class31.aClass3_558,
					AbstractImage.method1007((byte) 71, i_58_) }));
	    return null;
	}
	return aClass3Array2027[i_58_];
    }
    
    public boolean method772(boolean bool) {
	if ((anInt2049 & 0x1c8f57) >> 20 == 0)
	    return false;
	return true;
    }
    
    public boolean method773(int i) {
	if ((anInt2049 >> 29 & 0x1) == 0)
	    return false;
	return true;
    }
    
    public DirectColorSprite method774(int i, boolean bool) {
	Class39_Sub5_Sub12.aBoolean1856 = false;
        if (bool && runtimeActiveSprite != null) return runtimeActiveSprite;
        if (runtimeSprite != null) return runtimeSprite;
	int i_60_;
	if (!bool)
	    i_60_ = anInt2093;
	else
	    i_60_ = anInt2034;
	if (i == i_60_)
	    return null;
	long l = (((long) anInt2022 << 36) + (long) i_60_
		  - (-((aBoolean2107 ? 1L : 0L) << 38)
		     + (-((aBoolean2038 ? 1L : 0L) << 39)
			- ((long) anInt2003 << 40))));
	DirectColorSprite class39_sub5_sub10_sub3
	    = ((DirectColorSprite)
	       Class62_Sub1.aClass7_1587.get(l));
	if (class39_sub5_sub10_sub3 != null)
	    return class39_sub5_sub10_sub3;
	class39_sub5_sub10_sub3
	    = Class45.method923(Class37.aClass9_658, 0, i_60_, false);
	if (class39_sub5_sub10_sub3 == null) {
	    Class39_Sub5_Sub12.aBoolean1856 = true;
	    return null;
	}
	if (aBoolean2107)
	    class39_sub5_sub10_sub3.method671();
	if (aBoolean2038)
	    class39_sub5_sub10_sub3.method681();
	if (anInt2022 > 0)
	    class39_sub5_sub10_sub3.method673(anInt2022);
	if (anInt2022 >= 1)
	    class39_sub5_sub10_sub3.method683(1);
	if (anInt2022 >= 2)
	    class39_sub5_sub10_sub3.method683(16777215);
	if (anInt2003 != 0)
	    class39_sub5_sub10_sub3.method672(anInt2003);
	Class62_Sub1.aClass7_1587.put(class39_sub5_sub10_sub3, l,
					    (byte) 97);
	return class39_sub5_sub10_sub3;
    }
    
    public Model method775
	(int i, int i_61_, boolean bool, Class45 class45,
	 Class39_Sub5_Sub11 class39_sub5_sub11) {
	Class39_Sub5_Sub12.aBoolean1856 = false;
	int i_62_;
	int i_63_;
	if (bool) {
	    i_62_ = anInt2054;
	    i_63_ = anInt2082;
	} else {
	    i_62_ = anInt2026;
	    i_63_ = anInt2009;
	}
	if (i_63_ == 0)
	    return null;
	if (i_63_ == 1 && i_62_ == -1)
	    return null;
	Model class39_sub5_sub4_sub6
	    = ((Model)
	       Class2.aClass7_58.get((long) ((i_63_ << 16) + i_62_)));
	if (class39_sub5_sub4_sub6 == null) {
	    if (i_63_ == 1) {
		class39_sub5_sub4_sub6
		    = Model.getModel(Huffmans.aClass9_760,
						       i_62_, 0);
		if (class39_sub5_sub4_sub6 == null) {
		    Class39_Sub5_Sub12.aBoolean1856 = true;
		    return null;
		}
		class39_sub5_sub4_sub6.method553();
		class39_sub5_sub4_sub6.method548(64, 768, -50, -10, -50, true);
	    }
	    if (i_63_ == 2) {
		class39_sub5_sub4_sub6
		    = ArchiveWorker.getNpcDefinition(i_62_).method718(0);
		if (class39_sub5_sub4_sub6 == null) {
		    Class39_Sub5_Sub12.aBoolean1856 = true;
		    return null;
		}
		class39_sub5_sub4_sub6.method553();
		class39_sub5_sub4_sub6.method548(64, 768, -50, -10, -50, true);
	    }
	    if (i_63_ == 3) {
		if (class45 == null)
		    return null;
		class39_sub5_sub4_sub6 = class45.method924(1);
		if (class39_sub5_sub4_sub6 == null) {
		    Class39_Sub5_Sub12.aBoolean1856 = true;
		    return null;
		}
		class39_sub5_sub4_sub6.method553();
		class39_sub5_sub4_sub6.method548(64, 768, -50, -10, -50, true);
	    }
	    if (i_63_ == 4) {
		ItemDefinition class39_sub5_sub1
		    = Class26.getItemDefinition(i_62_);
		class39_sub5_sub4_sub6
		    = class39_sub5_sub1.method468(10, false, (byte) -113);
		if (class39_sub5_sub4_sub6 == null) {
		    Class39_Sub5_Sub12.aBoolean1856 = true;
		    return null;
		}
		class39_sub5_sub4_sub6.method553();
		class39_sub5_sub4_sub6.method548((class39_sub5_sub1.anInt1647
						  + 64),
						 (class39_sub5_sub1.anInt1673
						  + 768),
						 -50, -10, -50, true);
	    }
	    Class2.aClass7_58.put(class39_sub5_sub4_sub6,
					(long) (i_62_ + (i_63_ << 16)),
					(byte) 116);
	}
	if (class39_sub5_sub11 != null)
	    class39_sub5_sub4_sub6
		= class39_sub5_sub11.method706(-2058442128,
					       class39_sub5_sub4_sub6, i_61_);
	return class39_sub5_sub4_sub6;
    }
    
    public Widget() {
	anInt1997 = -1;
	anInt2010 = 0;
	anInt2003 = 0;
	anInt1996 = 0;
	anInt2020 = 0;
	aClass3_2029 = Class48.aClass3_905;
	anInt2008 = 0;
	anInt2041 = 0;
	anInt2022 = 0;
	anInt1994 = 0;
	anInt2036 = 0;
	aClass3_2023 = Class48.aClass3_905;
	anInt2024 = 0;
	anInt2032 = 0;
	aBoolean2042 = false;
	quadHeight = 0;
	anInt2050 = -1;
	anInt2007 = 0;
	aBoolean2013 = false;
	aClass3_2065 = Class48.aClass3_905;
	aBoolean2059 = false;
	anInt2030 = 0;
	aBoolean2047 = false;
	anInt2057 = -1;
	inactiveQuadColor = 0;
	anInt2064 = 0;
	anInt2052 = -1;
	anInt2074 = 100;
	anInt2000 = 0;
	drawSolidQuad = false;
	anInt2079 = 0;
	anInt2078 = 0;
	activeQuadColor = 0;
	anInt2051 = 0;
	anInt2049 = 0;
	anInt2054 = -1;
	anInt2090 = 0;
	anInt2084 = -1;
	anInt2056 = 0;
	aClass3_2066 = Class48.aClass3_905;
	anInt1998 = 0;
	anInt2034 = -1;
	anInt2058 = 0;
	anInt2009 = 1;
	anInt2082 = 1;
	anInt2012 = -1;
	anInt2086 = 0;
	anInt2096 = 0;
	anInt2095 = 0;
	anInt2089 = 0;
	aClass3_2048 = Class48.aClass3_905;
	aBoolean2055 = false;
	anInt2072 = 0;
	anInt2026 = -1;
	anInt2093 = -1;
	anInt2021 = 0;
	aClass3_2068 = Class23.aClass3_437;
	anInt2091 = 0;
	aBoolean2081 = false;
	anInt2069 = 0;
	anInt2098 = 0;
	anInt2103 = -1;
	aClass39_Sub5_Sub17_2040 = null;
	anInt2105 = -1;
	aBoolean2014 = false;
	quadWidth = 0;
	aBoolean2106 = false;
	anInt2102 = -1;
	anInt2083 = 1;
	aBoolean2108 = false;
    }
    
    static {
	aClass3_2046 = Class39_Sub5_Sub9.createJstring("Accept challenge");
	aClass3_2002 = aClass3_2046;
	anInt2031 = 0;
	anInt2071 = -1;
	anIntArray2005 = new int[] { 1, 2, 4, 8 };
	varbitCache = new Cache(64);
	anIntArray2112 = new int[4000];
	aClass3_2110 = Class39_Sub5_Sub9.createJstring(" )2> @whi@");
	anIntArray2111 = new int[1000];
	aBoolean2116 = false;
	aClass3_2117 = Class39_Sub5_Sub9.createJstring("(U3");
    }
}

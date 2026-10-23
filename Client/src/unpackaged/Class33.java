package unpackaged;

import jagex.utils.Node;
import jagex.utils.JString;
import jagex.utils.Queue;
import jagex.utils.Cache;

/* Class33 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Class33
{
    public static JString aClass3_594;
    public static JString aClass3_595
	= Class39_Sub5_Sub9.createJstring("mapmarker");
    public static byte[][][] aByteArrayArrayArray596;
    public static JString aClass3_597
	= Class39_Sub5_Sub9.createJstring("mapscene");
    public static int[] anIntArray598 = { 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2,
					  2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3 };
    public static int anInt599 = 0;
    public static FileLoader[] fileLoaders = new FileLoader[256];
    public static JString[] aClass3Array601;
    public static int[][] anIntArrayArray602;
    public static FileLoader fileLoader12;
    
    public static boolean method327(int i, byte i_0_) {
	if (i < 0)
	    return false;
	int i_1_ = JKeyListener.anIntArray621[i];
	if (i_1_ >= 2000)
	    i_1_ -= 2000;
	if (i_1_ == 31)
	    return true;
	return false;
    }
    
    public static void method328(byte i, FileTable class9) {
	Queue.aClass9_971 = class9;
    }
    
    public static void method329(int i) {
	aClass3_595 = null;
	fileLoader12 = null;
	aClass3_594 = null;
	anIntArray598 = null;
	aClass3Array601 = null;
	anIntArrayArray602 = null;
	fileLoaders = null;
	aClass3_597 = null;
	aByteArrayArrayArray596 = null;
    }
    
    public static int method330(int i) {
	if (ModHook.noRoofs) return NameTable.height;
	int i_2_ = 3;
	if (Class43.anInt799 < 310) {
	    int i_3_ = Class39_Sub11.anInt1470 >> 7;
	    int i_4_ = Node.anInt742 >> 7;
	    int i_5_ = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275 >> 7;
	    if ((Class55.tileFlags[NameTable.height][i_3_][i_4_]
		 & 0x4)
		!= 0)
		i_2_ = NameTable.height;
	    int i_6_ = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301 >> 7;
	    int i_7_;
	    if (i_4_ < i_5_)
		i_7_ = i_5_ - i_4_;
	    else
		i_7_ = i_4_ - i_5_;
	    int i_8_;
	    if (i_6_ > i_3_)
		i_8_ = i_6_ - i_3_;
	    else
		i_8_ = i_3_ - i_6_;
	    if (i_7_ >= i_8_) {
		int i_9_ = i_8_ * 65536 / i_7_;
		int i_10_ = 32768;
		while (i_4_ != i_5_) {
		    if (i_5_ <= i_4_) {
			if (i_5_ < i_4_)
			    i_4_--;
		    } else
			i_4_++;
		    i_10_ += i_9_;
		    if (((Class55.tileFlags[NameTable.height]
			  [i_3_][i_4_])
			 & 0x4)
			!= 0)
			i_2_ = NameTable.height;
		    if (i_10_ >= 65536) {
			if (i_6_ > i_3_)
			    i_3_++;
			else if (i_3_ > i_6_)
			    i_3_--;
			i_10_ -= 65536;
			if (((Class55.tileFlags
			      [NameTable.height][i_3_][i_4_])
			     & 0x4)
			    != 0)
			    i_2_ = NameTable.height;
		    }
		}
	    } else {
		int i_11_ = i_7_ * 65536 / i_8_;
		int i_12_ = 32768;
		while (i_6_ != i_3_) {
		    if (i_6_ > i_3_)
			i_3_++;
		    else if (i_6_ < i_3_)
			i_3_--;
		    if (((Class55.tileFlags[NameTable.height]
			  [i_3_][i_4_])
			 & 0x4)
			!= 0)
			i_2_ = NameTable.height;
		    i_12_ += i_11_;
		    if (i_12_ >= 65536) {
			i_12_ -= 65536;
			if (i_4_ >= i_5_) {
			    if (i_5_ < i_4_)
				i_4_--;
			} else
			    i_4_++;
			if (((Class55.tileFlags
			      [NameTable.height][i_3_][i_4_])
			     & 0x4)
			    != 0)
			    i_2_ = NameTable.height;
		    }
		}
	    }
	}
	if (((Class55.tileFlags[NameTable.height]
	      [Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2301 >> 7]
	      [Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anInt2275 >> 7])
	     & 0x4)
	    != 0)
	    i_2_ = NameTable.height;
	return i_2_;
    }
    
    public static void method331(int i) {
	for (Class39_Sub11 class39_sub11
		 = (Class39_Sub11) Class15.aClass49_278.getFirst();
	     class39_sub11 != null;
	     class39_sub11 = ((Class39_Sub11)
			      Class15.aClass49_278.getNext())) {
	    if (class39_sub11.anInt1456 == -1) {
		class39_sub11.anInt1476 = 0;
		Class66.method1106(1, class39_sub11);
	    } else
		class39_sub11.unlinkDeque();
	}
	if (i != -1)
	    aByteArrayArrayArray596 = null;
    }
    
    static {
	aClass3_594 = Class39_Sub5_Sub9.createJstring("Fallen lassen");
	anIntArrayArray602 = new int[104][104];
	aClass3Array601 = new JString[500];
    }
}

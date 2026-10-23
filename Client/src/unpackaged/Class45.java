package unpackaged;

import jagex.world.actors.GroundItem;
import jagex.graphics.JImage;
import jagex.graphics.sprites.DirectColorSprite;
import jagex.graphics.sprites.IndexedColorSprite;
import jagex.world.actors.Projectile;
import jagex.utils.JString;
import jagex.io.JSocket;
import jagex.utils.Queue;
import jagex.io.Buffer;
import jagex.utils.Cache;

/* Class45 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Class45
{
    public int[] anIntArray848;
    public long aLong849;
    public long aLong850;
    public int[] anIntArray851;
    public int anInt852;
    public static Cache clientScriptCache;
    public boolean aBoolean854;
    public static int anInt855;
    public static int anInt856 = 0;
    public static int[] anIntArray857;
    public static JString aClass3_858;
    public static JString aClass3_859;
    public static JString aClass3_860;
    public static volatile boolean aBoolean861;
    public static boolean aBoolean862;
    public static JString aClass3_863;
    public static JString aClass3_864;
    public static JString aClass3_865;
    public static IndexedColorSprite aClass39_Sub5_Sub10_Sub4_866;
    public static boolean aBoolean867;
    public static int anInt868;
    public static int anInt869;
    public static FileLoader fileLoader7;
    public static JString aClass3_871;
    
    public static void method916(int i, int i_0_) {
	if (i == -1 && JSocket.anInt313 == 0)
	    Class41.method892((byte) 94);
	else if (i != -1 && i != ArchiveWorker.anInt1205
		 && Class39_Sub5_Sub4_Sub4.anInt2313 != 0
		 && JSocket.anInt313 == 0)
	    Class41.method894(Projectile.fileLoader6, i, 0,
			      false, 10, 0, -75,
			      Class39_Sub5_Sub4_Sub4.anInt2313);
	ArchiveWorker.anInt1205 = i;
    }

    /** The music tab's LOOP toggle (config 19). */
    public static boolean loopMusic() {
	return Class66.stateValues != null && Class66.stateValues.length > 19
	    && Class66.stateValues[19] == 1;
    }

    // Songs play once; LOOP replays the current one after a short silence.
    private static final long LOOP_GAP_MS = 2500L;
    private static long songEndedAt = -1L;

    /** Game thread, once per cycle: replays the current song LOOP_GAP_MS after it ends while LOOP is on. */
    public static void refreshMusicLoop() {
	if (!loopMusic() || ArchiveWorker.anInt1205 == -1
	    || Class39_Sub5_Sub4_Sub4.anInt2313 == 0 || JSocket.anInt313 != 0
	    || Class55.aClass62_1251 == null || Class39_Sub5_Sub5.aBoolean1749
	    || Class39_Sub5_Sub6.aByteArray1768 != null
	    || Class55.aClass62_1251.isPlaying()) {
	    songEndedAt = -1L;
	    return;
	}
	long now = System.currentTimeMillis();
	if (songEndedAt < 0L) {
	    songEndedAt = now;
	} else if (now - songEndedAt >= LOOP_GAP_MS) {
	    songEndedAt = -1L;
	    Class41.method894(Projectile.fileLoader6, ArchiveWorker.anInt1205, 0,
			      false, 10, 0, -75,
			      Class39_Sub5_Sub4_Sub4.anInt2313);
	}
    }
    
    public static void sleep(long l) {
	if (l > 0L) {
	    if (l % 10L == 0L) {
		Class50.method974(l - 1L, (byte) -44);
		Class50.method974(1L, (byte) -44);
	    } else
		Class50.method974(l, (byte) -44);
	}
    }
    
    public void method918(int i, boolean bool) {
	if (aBoolean854 != bool)
	    method925(null, bool, anIntArray851, 91, -1);
    }
    
    public void method919(int i, boolean bool, int i_1_) {
	if (i_1_ != 1 || !aBoolean854) {
	    int i_2_ = anIntArray848[NameTable.anIntArray179[i_1_]];
	    if (i != i_2_) {
		i_2_ -= 256;
		Class39_Sub5_Sub6 class39_sub5_sub6;
		do {
		    if (!bool) {
			if (--i_2_ < 0)
			    i_2_ = JImage.anInt1584 - 1;
		    } else {
			i_2_++;
			if (JImage.anInt1584 <= i_2_)
			    i_2_ = 0;
		    }
		    class39_sub5_sub6 = FileLoader.method172(i_2_, 3);
		} while (class39_sub5_sub6 == null
			 || class39_sub5_sub6.aBoolean1754
			 || (i_1_ + (!aBoolean854 ? 0 : 7)
			     != class39_sub5_sub6.anInt1758));
		anIntArray848[NameTable.anIntArray179[i_1_]] = i_2_ + 256;
		method926(true);
	    }
	}
    }
    
    public static void method920(int i) {
	aClass3_871 = null;
	fileLoader7 = null;
	aClass3_860 = null;
	clientScriptCache = null;
	aClass3_863 = null;
	aClass3_865 = null;
	anIntArray857 = null;
	aClass3_864 = null;
	aClass3_859 = null;
	aClass39_Sub5_Sub10_Sub4_866 = null;
	aClass3_858 = null;
    }
    
    public void method921(Buffer class39_sub6, int i) {
	class39_sub6.putByte(aBoolean854 ? 1 : 0);
	for (int i_3_ = 0; i_3_ < 7; i_3_++) {
	    int i_4_ = anIntArray848[NameTable.anIntArray179[i_3_]];
	    if (i_4_ == 0)
		class39_sub6.putByte(-1);
	    else
		class39_sub6.putByte(i_4_ - 256);
	}
	for (int i_5_ = 0; i_5_ < 5; i_5_++)
	    class39_sub6.putByte(anIntArray851[i_5_]);
    }
    
    public Model method922
	(int i, Class39_Sub5_Sub11 class39_sub5_sub11,
	 Class39_Sub5_Sub11 class39_sub5_sub11_6_, int i_7_, int i_8_) {
	if (anInt852 != -1)
	    return (ArchiveWorker.getNpcDefinition(anInt852).method725
		    (i_7_, 65535, class39_sub5_sub11, class39_sub5_sub11_6_,
		     i_8_));
	long l = aLong849;
	int[] is = anIntArray848;
	if (i != -27537)
	    method924(53);
	if (class39_sub5_sub11_6_ != null
	    && (class39_sub5_sub11_6_.anInt1825 >= 0
		|| class39_sub5_sub11_6_.anInt1836 >= 0)) {
	    is = new int[12];
	    for (int i_9_ = 0; i_9_ < 12; i_9_++)
		is[i_9_] = anIntArray848[i_9_];
	    if (class39_sub5_sub11_6_.anInt1825 >= 0) {
		l += (long) (class39_sub5_sub11_6_.anInt1825 - anIntArray848[5]
			     << 8);
		is[5] = class39_sub5_sub11_6_.anInt1825;
	    }
	    if (class39_sub5_sub11_6_.anInt1836 >= 0) {
		l += (long) ((-anIntArray848[3]
			      + class39_sub5_sub11_6_.anInt1836)
			     << 16);
		is[3] = class39_sub5_sub11_6_.anInt1836;
	    }
	}
	Model class39_sub5_sub4_sub6
	    = ((Model)
	       RuntimeException_Sub1.aClass7_1220.get(l));
	if (class39_sub5_sub4_sub6 == null) {
	    boolean bool = false;
	    for (int i_10_ = 0; i_10_ < 12; i_10_++) {
		int i_11_ = is[i_10_];
		if (i_11_ >= 256 && i_11_ < 512
		    && !FileLoader.method172(i_11_ - 256, 3).method577(0))
		    bool = true;
		if (i_11_ >= 512 && !Class26.getItemDefinition(i_11_ - 512)
					 .method463(aBoolean854, -2583))
		    bool = true;
	    }
	    if (bool) {
		if (aLong850 != -1L)
		    class39_sub5_sub4_sub6
			= ((Model)
			   RuntimeException_Sub1.aClass7_1220
			       .get(aLong850));
		if (class39_sub5_sub4_sub6 == null)
		    return null;
	    }
	    if (class39_sub5_sub4_sub6 == null) {
		Model[] class39_sub5_sub4_sub6s
		    = new Model[12];
		int i_12_ = 0;
		for (int i_13_ = 0; i_13_ < 12; i_13_++) {
		    int i_14_ = is[i_13_];
		    if (i_14_ >= 256 && i_14_ < 512) {
			Model class39_sub5_sub4_sub6_15_
			    = FileLoader.method172(i_14_ - 256, 3)
				  .method580(114);
			if (class39_sub5_sub4_sub6_15_ != null)
			    class39_sub5_sub4_sub6s[i_12_++]
				= class39_sub5_sub4_sub6_15_;
		    }
		    if (i_14_ >= 512) {
			Model class39_sub5_sub4_sub6_16_
			    = Class26.getItemDefinition(i_14_ - 512)
				  .method474(aBoolean854, i ^ ~0x6bfa);
			if (class39_sub5_sub4_sub6_16_ != null)
			    class39_sub5_sub4_sub6s[i_12_++]
				= class39_sub5_sub4_sub6_16_;
		    }
		}
		class39_sub5_sub4_sub6
		    = new Model(class39_sub5_sub4_sub6s,
						 i_12_);
		for (int i_17_ = 0; i_17_ < 5; i_17_++) {
		    if (anIntArray851[i_17_] != 0) {
			class39_sub5_sub4_sub6.method563
			    ((GroundItem.anIntArrayArray2233[i_17_]
			      [0]),
			     (GroundItem.anIntArrayArray2233[i_17_]
			      [anIntArray851[i_17_]]));
			if (i_17_ == 1)
			    class39_sub5_sub4_sub6.method563
				(Class46_Sub1.anIntArray1555[0],
				 (Class46_Sub1.anIntArray1555
				  [anIntArray851[i_17_]]));
		    }
		}
		class39_sub5_sub4_sub6.method553();
		class39_sub5_sub4_sub6.method548(64, 850, -30, -50, -30, true);
		RuntimeException_Sub1.aClass7_1220
		    .put(class39_sub5_sub4_sub6, l, (byte) -9);
		aLong850 = l;
	    }
	}
	Model class39_sub5_sub4_sub6_18_;
	if (class39_sub5_sub11_6_ != null || class39_sub5_sub11 != null) {
	    if (class39_sub5_sub11_6_ != null && class39_sub5_sub11 != null)
		class39_sub5_sub4_sub6_18_
		    = class39_sub5_sub11_6_.method701(i_7_,
						      class39_sub5_sub4_sub6,
						      class39_sub5_sub11,
						      i - 248981919, i_8_);
	    else if (class39_sub5_sub11_6_ == null)
		class39_sub5_sub4_sub6_18_
		    = class39_sub5_sub11.method703(class39_sub5_sub4_sub6,
						   i_8_, true);
	    else
		class39_sub5_sub4_sub6_18_
		    = class39_sub5_sub11_6_.method703(class39_sub5_sub4_sub6,
						      i_7_, true);
	} else
	    return class39_sub5_sub4_sub6;
	return class39_sub5_sub4_sub6_18_;
    }
    
    public static DirectColorSprite method923(FileTable class9, int i,
						    int i_19_, boolean bool) {
	if (bool)
	    method923(null, -91, -12, true);
	if (!Client.decodeBitmapFont(class9, i_19_, i))
	    return null;
	return Class62_Sub2.method1074(!bool);
    }
    
    public Model method924(int i) {
	if (anInt852 != -1)
	    return ArchiveWorker.getNpcDefinition(anInt852).method718(0);
	boolean bool = false;
	for (int i_20_ = 0; i_20_ < 12; i_20_++) {
	    int i_21_ = anIntArray848[i_20_];
	    if (i_21_ >= 256 && i_21_ < 512
		&& !FileLoader.method172(i_21_ - 256, 3)
			.method581((byte) -21))
		bool = true;
	    if (i_21_ >= 512
		&& !Class26.getItemDefinition(i_21_ - 512).method466(111,
								  aBoolean854))
		bool = true;
	}
	if (bool)
	    return null;
	if (i != 1)
	    aBoolean867 = false;
	int i_22_ = 0;
	Model[] class39_sub5_sub4_sub6s
	    = new Model[12];
	for (int i_23_ = 0; i_23_ < 12; i_23_++) {
	    int i_24_ = anIntArray848[i_23_];
	    if (i_24_ >= 256 && i_24_ < 512) {
		Model class39_sub5_sub4_sub6
		    = FileLoader.method172(i_24_ - 256, 3)
			  .method576(i ^ ~0x1);
		if (class39_sub5_sub4_sub6 != null)
		    class39_sub5_sub4_sub6s[i_22_++] = class39_sub5_sub4_sub6;
	    }
	    if (i_24_ >= 512) {
		Model class39_sub5_sub4_sub6
		    = Class26.getItemDefinition(i_24_ - 512)
			  .method469((byte) 117, aBoolean854);
		if (class39_sub5_sub4_sub6 != null)
		    class39_sub5_sub4_sub6s[i_22_++] = class39_sub5_sub4_sub6;
	    }
	}
	Model class39_sub5_sub4_sub6
	    = new Model(class39_sub5_sub4_sub6s, i_22_);
	for (int i_25_ = 0; i_25_ < 5; i_25_++) {
	    if (anIntArray851[i_25_] != 0) {
		class39_sub5_sub4_sub6.method563((GroundItem
						  .anIntArrayArray2233[i_25_]
						  [0]),
						 (GroundItem
						  .anIntArrayArray2233[i_25_]
						  [anIntArray851[i_25_]]));
		if (i_25_ == 1)
		    class39_sub5_sub4_sub6.method563((Class46_Sub1
						      .anIntArray1555[0]),
						     (Class46_Sub1
						      .anIntArray1555
						      [anIntArray851[i_25_]]));
	    }
	}
	return class39_sub5_sub4_sub6;
    }
    
    public void method925(int[] is, boolean bool, int[] is_26_, int i,
			  int i_27_) {
	if (is == null) {
	    is = new int[12];
	    for (int i_28_ = 0; i_28_ < 7; i_28_++) {
		for (int i_29_ = 0; i_29_ < JImage.anInt1584; i_29_++) {
		    Class39_Sub5_Sub6 class39_sub5_sub6
			= FileLoader.method172(i_29_, 3);
		    if (class39_sub5_sub6 != null
			&& !class39_sub5_sub6.aBoolean1754
			&& (class39_sub5_sub6.anInt1758
			    == (bool ? 7 : 0) + i_28_)) {
			is[NameTable.anIntArray179[i_28_]] = i_29_ + 256;
			break;
		    }
		}
	    }
	}
	aBoolean854 = bool;
	anIntArray851 = is_26_;
	anIntArray848 = is;
	anInt852 = i_27_;
	method926(true);
    }
    
    public void method926(boolean bool) {
	long l = aLong849;
	aLong849 = 0L;
	int i = anIntArray848[5];
	int i_30_ = anIntArray848[9];
	anIntArray848[5] = i_30_;
	anIntArray848[9] = i;
	for (int i_31_ = 0; i_31_ < 12; i_31_++) {
	    aLong849 <<= 4;
	    if (anIntArray848[i_31_] >= 256)
		aLong849 += (long) (anIntArray848[i_31_] - 256);
	}
	if (anIntArray848[0] >= 256)
	    aLong849 += (long) (anIntArray848[0] - 256 >> 4);
	if (bool != true)
	    aClass3_863 = null;
	if (anIntArray848[1] >= 256)
	    aLong849 += (long) (anIntArray848[1] - 256 >> 8);
	for (int i_32_ = 0; i_32_ < 5; i_32_++) {
	    aLong849 <<= 3;
	    aLong849 += (long) anIntArray851[i_32_];
	}
	anIntArray848[9] = i_30_;
	aLong849 <<= 1;
	Class45 class45_33_ = this;
	class45_33_.aLong849
	    = class45_33_.aLong849 + (long) (aBoolean854 ? 1 : 0);
	anIntArray848[5] = i;
	if (l != 0L && aLong849 != l)
	    RuntimeException_Sub1.aClass7_1220.method131(l, -123);
    }
    
    public static int method927(int i, int i_34_, int i_35_, byte i_36_) {
	if ((Class55.tileFlags[i_35_][i_34_][i] & 0x8) != 0)
	    return 0;
	if (i_35_ > 0
	    && (Class55.tileFlags[1][i_34_][i] & 0x2) != 0)
	    return i_35_ - 1;
	return i_35_;
    }
    
    public int method928(int i) {
	if (anInt852 == -1)
	    return (anIntArray848[1]
		    + ((anIntArray848[11] << 5)
		       + ((anIntArray848[8] << 10) + (anIntArray848[0] << 15)))
		    + ((anIntArray851[0] << 25) + (anIntArray851[4] << 20)));
	return ArchiveWorker.getNpcDefinition(anInt852).id + 305419896;
    }
    
    public static void setVarbitValue(int value, int id) {
	Varbit varbit
	    = Class25.getVarbit(id);
	int i_39_ = varbit.anInt1793;
	int settingId = varbit.settingId;
	int i_41_ = varbit.anInt1794;
	int i_42_ = ClientScript.bitMasks[-i_39_ + i_41_];
	if (value < 0 || value > i_42_)
	    value = 0;
	i_42_ <<= i_39_;
	Class66.stateValues[settingId]
	    = Queue.or(Class34.method342(i_42_ ^ 0xffffffff,
						  (Class66.stateValues
						   [settingId])),
				Class34.method342(i_42_, value << i_39_));
    }
    
    public static RuntimeException_Sub1 method930(Throwable throwable,
						  String string) {
	RuntimeException_Sub1 runtimeexception_sub1;
	if (!(throwable instanceof RuntimeException_Sub1))
	    runtimeexception_sub1
		= new RuntimeException_Sub1(throwable, string);
	else {
	    runtimeexception_sub1 = (RuntimeException_Sub1) throwable;
	    runtimeexception_sub1.aString1215 += ' ' + (String) string;
	}
	return runtimeexception_sub1;
    }
    
    public void method931(boolean bool, int i, int i_43_) {
	int i_44_ = anIntArray851[i_43_];
	if (bool) {
	    i_44_++;
	    if (GroundItem.anIntArrayArray2233[i_43_].length
		<= i_44_)
		i_44_ = 0;
	} else if (--i_44_ < 0)
	    i_44_
		= GroundItem.anIntArrayArray2233[i_43_].length - 1;
	anIntArray851[i_43_] = i_44_;
	method926(true);
    }
    
    static {
	anInt855 = 500;
	clientScriptCache = new Cache(128);
	aClass3_860 = Class39_Sub5_Sub9.createJstring("System)2Update in: ");
	anIntArray857 = new int[5];
	aBoolean862 = false;
	aBoolean861 = true;
	aClass3_863 = Class39_Sub5_Sub9.createJstring("Enter amount:");
	aClass3_858 = Class39_Sub5_Sub9.createJstring("Offline");
	aClass3_859 = aClass3_863;
	anInt868 = 0;
	aBoolean867 = false;
	aClass3_864 = aClass3_858;
	aClass3_871
	    = (Class39_Sub5_Sub9.createJstring
	       ("You need a members account to login to this world)3"));
	aClass3_865 = aClass3_871;
    }
}

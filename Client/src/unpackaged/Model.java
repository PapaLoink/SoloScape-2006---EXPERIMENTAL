package unpackaged;

import jagex.io.Buffer;

/* Class39_Sub5_Sub4_Sub6 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Model extends Class39_Sub5_Sub4
{
    public int[] anIntArray2354;
    public int anInt2355 = 0;
    public int[] anIntArray2356;
    public int[] anIntArray2357;
    public int anInt2358;
    public int[] anIntArray2359;
    public int[][] anIntArrayArray2360;
    public Class48[] aClass48Array2361;
    public int anInt2362;
    public int[] anIntArray2363;
    public int[] anIntArray2364;
    public int[] anIntArray2365;
    public int anInt2366 = 0;
    public int anInt2367;
    public int[] anIntArray2368;
    public int[] anIntArray2369;
    public int[] anIntArray2370;
    public int[] anIntArray2371;
    public int anInt2372;
    public int[] anIntArray2373;
    public boolean aBoolean2374 = false;
    public int[] anIntArray2375;
    public int[] anIntArray2376;
    public int anInt2377;
    public int[] anIntArray2378;
    public int[] anIntArray2379;
    public Class48[] aClass48Array2380;
    public int anInt2381;
    public static Model aClass39_Sub5_Sub4_Sub6_2382
	= new Model();
    public int anInt2383;
    public int anInt2384 = 0;
    public int[] anIntArray2385;
    public int anInt2386;
    public int[] anIntArray2387;
    public int[][] anIntArrayArray2388;
    public static Model aClass39_Sub5_Sub4_Sub6_2389;
    public static int[] anIntArray2390 = new int[1];
    public static int[] anIntArray2391;
    public static int[] anIntArray2392;
    public static int[] anIntArray2393;
    public static int[] anIntArray2394;
    public static int[] anIntArray2395;
    public static int anInt2396;
    public static int anInt2397;
    public static boolean aBoolean2398;
    public static int[] anIntArray2399;
    public static int[] anIntArray2400;
    public static int[] anIntArray2401;
    public static int[] anIntArray2402;
    public static int[] anIntArray2403;
    public static boolean[] aBooleanArray2404;
    public static int[] anIntArray2405;
    public static int[] anIntArray2406;
    public static int anInt2407;
    public static int[] anIntArray2408;
    public static int[] anIntArray2409;
    public static int[][] anIntArrayArray2410;
    public static int[] anIntArray2411;
    public static boolean[] aBooleanArray2412;
    public static int anInt2413;
    public static int[] anIntArray2414;
    public static int[][] anIntArrayArray2415;
    public static int anInt2416;
    public static int[] anIntArray2417;
    public static int[] anIntArray2418;
    public static int[] anIntArray2419;
    public static int anInt2420;
    public static int[] anIntArray2421;
    public static int[] anIntArray2422;
    
    public boolean method537(int i, int i_0_, int i_1_, int i_2_, int i_3_, int i_4_, int i_5_, int i_6_) {
	if (i_0_ < i_1_ && i_0_ < i_2_ && i_0_ < i_3_)
	    return false;
	if (i_0_ > i_1_ && i_0_ > i_2_ && i_0_ > i_3_)
	    return false;
	if (i < i_4_ && i < i_5_ && i < i_6_)
	    return false;
	if (i > i_4_ && i > i_5_ && i > i_6_)
	    return false;
	return true;
    }
    
    public void method538(Class39_Sub5_Sub12 class39_sub5_sub12, int i,
			  Class39_Sub5_Sub12 class39_sub5_sub12_7_, int i_8_,
			  int[] is) {
	if (i != -1) {
	    if (is == null || i_8_ == -1)
		method557(class39_sub5_sub12, i);
	    else {
		aClass48Array2361 = null;
		anInt2381 = 0;
		Class51 class51 = class39_sub5_sub12.aClass51Array1855[i];
		Class51 class51_9_
		    = class39_sub5_sub12_7_.aClass51Array1855[i_8_];
		Class39_Sub12 class39_sub12 = class51.aClass39_Sub12_938;
		anInt2420 = 0;
		anInt2416 = 0;
		anInt2396 = 0;
		int i_10_ = 0;
		int i_11_ = is[i_10_++];
		for (int i_12_ = 0; i_12_ < class51.anInt948; i_12_++) {
		    int i_13_;
		    for (i_13_ = class51.anIntArray945[i_12_]; i_13_ > i_11_;
			 i_11_ = is[i_10_++]) {
			/* empty */
		    }
		    if (i_13_ != i_11_
			|| class39_sub12.anIntArray1497[i_13_] == 0)
			method545(class39_sub12.anIntArray1497[i_13_],
				  class39_sub12.anIntArrayArray1486[i_13_],
				  class51.anIntArray942[i_12_],
				  class51.anIntArray946[i_12_],
				  class51.anIntArray944[i_12_]);
		}
		anInt2420 = 0;
		anInt2416 = 0;
		anInt2396 = 0;
		i_10_ = 0;
		i_11_ = is[i_10_++];
		for (int i_14_ = 0; i_14_ < class51_9_.anInt948; i_14_++) {
		    int i_15_;
		    for (i_15_ = class51_9_.anIntArray945[i_14_];
			 i_15_ > i_11_; i_11_ = is[i_10_++]) {
			/* empty */
		    }
		    if (i_15_ == i_11_
			|| class39_sub12.anIntArray1497[i_15_] == 0)
			method545(class39_sub12.anIntArray1497[i_15_],
				  class39_sub12.anIntArrayArray1486[i_15_],
				  class51_9_.anIntArray942[i_14_],
				  class51_9_.anIntArray946[i_14_],
				  class51_9_.anIntArray944[i_14_]);
		}
	    }
	}
    }
    
    public void method539(int i, int i_16_, int i_17_) {
	int i_18_ = anInt2377 >> 16;
	int i_19_ = anInt2377 << 16 >> 16;
	method560(i_18_, i_19_, i, i_16_, i_17_);
    }
    
    public void render(int i, int i_20_, int i_21_, int i_22_, int i_23_,
			  int i_24_, int i_25_, int i_26_) {
	if (anInt2381 != 2 && anInt2381 != 1)
	    method568();
	int i_27_ = Class39_Sub5_Sub10_Sub2.anInt2462;
	int i_28_ = Class39_Sub5_Sub10_Sub2.anInt2467;
	int i_29_ = anIntArray2394[i];
	int i_30_ = anIntArray2418[i];
	int i_31_ = anIntArray2394[i_20_];
	int i_32_ = anIntArray2418[i_20_];
	int i_33_ = anIntArray2394[i_21_];
	int i_34_ = anIntArray2418[i_21_];
	int i_35_ = anIntArray2394[i_22_];
	int i_36_ = anIntArray2418[i_22_];
	int i_37_ = i_24_ * i_35_ + i_25_ * i_36_ >> 16;
	for (int i_38_ = 0; i_38_ < anInt2367; i_38_++) {
	    int i_39_ = anIntArray2376[i_38_];
	    int i_40_ = anIntArray2356[i_38_];
	    int i_41_ = anIntArray2359[i_38_];
	    if (i_21_ != 0) {
		int i_42_ = i_40_ * i_33_ + i_39_ * i_34_ >> 16;
		i_40_ = i_40_ * i_34_ - i_39_ * i_33_ >> 16;
		i_39_ = i_42_;
	    }
	    if (i != 0) {
		int i_43_ = i_40_ * i_30_ - i_41_ * i_29_ >> 16;
		i_41_ = i_40_ * i_29_ + i_41_ * i_30_ >> 16;
		i_40_ = i_43_;
	    }
	    if (i_20_ != 0) {
		int i_44_ = i_41_ * i_31_ + i_39_ * i_32_ >> 16;
		i_41_ = i_41_ * i_32_ - i_39_ * i_31_ >> 16;
		i_39_ = i_44_;
	    }
	    i_39_ += i_23_;
	    i_40_ += i_24_;
	    i_41_ += i_25_;
	    int i_45_ = i_40_ * i_36_ - i_41_ * i_35_ >> 16;
	    i_41_ = i_40_ * i_35_ + i_41_ * i_36_ >> 16;
	    i_40_ = i_45_;
	    anIntArray2405[i_38_] = i_41_ - i_37_;
	    anIntArray2395[i_38_] = i_27_ + (i_39_ * Resizable.zoom) / i_26_;
	    anIntArray2403[i_38_] = i_28_ + (i_40_ * Resizable.zoom) / i_26_;
	    if (anInt2355 > 0) {
		anIntArray2392[i_38_] = i_39_;
		anIntArray2421[i_38_] = i_40_;
		anIntArray2414[i_38_] = i_41_;
	    }
	}
	try {
	    method546(false, false, 0);
	} catch (Exception exception) {
	    /* empty */
	}
    }
    
    public static Model getModel(FileTable fileTable, int parent, int child) {
	byte[] is = fileTable.lookupFile(parent, child);
	if (is == null)
	    return null;
	return new Model(is);
    }
    
    public void method542() {
	if (anInt2381 != 1) {
	    anInt2381 = 1;
	    anInt1726 = 0;
	    anInt2362 = 0;
	    anInt2386 = 0;
	    for (int i = 0; i < anInt2367; i++) {
		int i_47_ = anIntArray2376[i];
		int i_48_ = anIntArray2356[i];
		int i_49_ = anIntArray2359[i];
		if (-i_48_ > anInt1726)
		    anInt1726 = -i_48_;
		if (i_48_ > anInt2362)
		    anInt2362 = i_48_;
		int i_50_ = i_47_ * i_47_ + i_49_ * i_49_;
		if (i_50_ > anInt2386)
		    anInt2386 = i_50_;
	    }
	    anInt2386 = (int) (Math.sqrt((double) anInt2386) + 0.99);
	    anInt2358 = (int) (Math.sqrt((double) (anInt2386 * anInt2386
						   + anInt1726 * anInt1726))
			       + 0.99);
	    anInt2372 = (anInt2358
			 + (int) (Math.sqrt((double) (anInt2386 * anInt2386
						      + anInt2362 * anInt2362))
				  + 0.99));
	}
    }
    
    public void method543() {
	aClass48Array2361 = null;
	anInt2381 = 0;
	for (int i = 0; i < anInt2367; i++)
	    anIntArray2359[i] = -anIntArray2359[i];
	for (int i = 0; i < anInt2366; i++) {
	    int i_51_ = anIntArray2385[i];
	    anIntArray2385[i] = anIntArray2368[i];
	    anIntArray2368[i] = i_51_;
	}
    }
    
    public Model method544
	(boolean bool, Model class39_sub5_sub4_sub6_52_,
	 int[] is) {
	class39_sub5_sub4_sub6_52_.anInt2367 = anInt2367;
	class39_sub5_sub4_sub6_52_.anInt2366 = anInt2366;
	class39_sub5_sub4_sub6_52_.anInt2355 = anInt2355;
	if (class39_sub5_sub4_sub6_52_.anIntArray2376 == null
	    || class39_sub5_sub4_sub6_52_.anIntArray2376.length < anInt2367) {
	    class39_sub5_sub4_sub6_52_.anIntArray2376
		= new int[anInt2367 + 100];
	    class39_sub5_sub4_sub6_52_.anIntArray2356
		= new int[anInt2367 + 100];
	    class39_sub5_sub4_sub6_52_.anIntArray2359
		= new int[anInt2367 + 100];
	}
	for (int i = 0; i < anInt2367; i++) {
	    class39_sub5_sub4_sub6_52_.anIntArray2376[i] = anIntArray2376[i];
	    class39_sub5_sub4_sub6_52_.anIntArray2356[i] = anIntArray2356[i];
	    class39_sub5_sub4_sub6_52_.anIntArray2359[i] = anIntArray2359[i];
	}
	if (bool)
	    class39_sub5_sub4_sub6_52_.anIntArray2379 = anIntArray2379;
	else {
	    class39_sub5_sub4_sub6_52_.anIntArray2379 = is;
	    if (anIntArray2379 == null) {
		for (int i = 0; i < anInt2366; i++)
		    class39_sub5_sub4_sub6_52_.anIntArray2379[i] = 0;
	    } else {
		for (int i = 0; i < anInt2366; i++)
		    class39_sub5_sub4_sub6_52_.anIntArray2379[i]
			= anIntArray2379[i];
	    }
	}
	class39_sub5_sub4_sub6_52_.anIntArray2378 = anIntArray2378;
	class39_sub5_sub4_sub6_52_.anIntArray2371 = anIntArray2371;
	class39_sub5_sub4_sub6_52_.anIntArray2365 = anIntArray2365;
	class39_sub5_sub4_sub6_52_.anInt2384 = anInt2384;
	class39_sub5_sub4_sub6_52_.anIntArrayArray2360 = anIntArrayArray2360;
	class39_sub5_sub4_sub6_52_.anIntArrayArray2388 = anIntArrayArray2388;
	class39_sub5_sub4_sub6_52_.anIntArray2385 = anIntArray2385;
	class39_sub5_sub4_sub6_52_.anIntArray2363 = anIntArray2363;
	class39_sub5_sub4_sub6_52_.anIntArray2368 = anIntArray2368;
	class39_sub5_sub4_sub6_52_.anIntArray2354 = anIntArray2354;
	class39_sub5_sub4_sub6_52_.anIntArray2369 = anIntArray2369;
	class39_sub5_sub4_sub6_52_.anIntArray2364 = anIntArray2364;
	class39_sub5_sub4_sub6_52_.anIntArray2370 = anIntArray2370;
	class39_sub5_sub4_sub6_52_.anIntArray2373 = anIntArray2373;
	class39_sub5_sub4_sub6_52_.anIntArray2387 = anIntArray2387;
	class39_sub5_sub4_sub6_52_.aBoolean2374 = aBoolean2374;
	class39_sub5_sub4_sub6_52_.anInt2381 = 0;
	return class39_sub5_sub4_sub6_52_;
    }
    
    public void method545(int i, int[] is, int i_53_, int i_54_, int i_55_) {
	int i_56_ = is.length;
	if (i == 0) {
	    int i_57_ = 0;
	    anInt2420 = 0;
	    anInt2416 = 0;
	    anInt2396 = 0;
	    for (int i_58_ = 0; i_58_ < i_56_; i_58_++) {
		int i_59_ = is[i_58_];
		if (i_59_ < anIntArrayArray2388.length) {
		    int[] is_60_ = anIntArrayArray2388[i_59_];
		    for (int i_61_ = 0; i_61_ < is_60_.length; i_61_++) {
			int i_62_ = is_60_[i_61_];
			anInt2420 += anIntArray2376[i_62_];
			anInt2416 += anIntArray2356[i_62_];
			anInt2396 += anIntArray2359[i_62_];
			i_57_++;
		    }
		}
	    }
	    if (i_57_ > 0) {
		anInt2420 = anInt2420 / i_57_ + i_53_;
		anInt2416 = anInt2416 / i_57_ + i_54_;
		anInt2396 = anInt2396 / i_57_ + i_55_;
	    } else {
		anInt2420 = i_53_;
		anInt2416 = i_54_;
		anInt2396 = i_55_;
	    }
	} else if (i == 1) {
	    for (int i_63_ = 0; i_63_ < i_56_; i_63_++) {
		int i_64_ = is[i_63_];
		if (i_64_ < anIntArrayArray2388.length) {
		    int[] is_65_ = anIntArrayArray2388[i_64_];
		    for (int i_66_ = 0; i_66_ < is_65_.length; i_66_++) {
			int i_67_ = is_65_[i_66_];
			anIntArray2376[i_67_] += i_53_;
			anIntArray2356[i_67_] += i_54_;
			anIntArray2359[i_67_] += i_55_;
		    }
		}
	    }
	} else if (i == 2) {
	    for (int i_68_ = 0; i_68_ < i_56_; i_68_++) {
		int i_69_ = is[i_68_];
		if (i_69_ < anIntArrayArray2388.length) {
		    int[] is_70_ = anIntArrayArray2388[i_69_];
		    for (int i_71_ = 0; i_71_ < is_70_.length; i_71_++) {
			int i_72_ = is_70_[i_71_];
			anIntArray2376[i_72_] -= anInt2420;
			anIntArray2356[i_72_] -= anInt2416;
			anIntArray2359[i_72_] -= anInt2396;
			int i_73_ = (i_53_ & 0xff) * 8;
			int i_74_ = (i_54_ & 0xff) * 8;
			int i_75_ = (i_55_ & 0xff) * 8;
			if (i_75_ != 0) {
			    int i_76_ = anIntArray2394[i_75_];
			    int i_77_ = anIntArray2418[i_75_];
			    int i_78_ = ((anIntArray2356[i_72_] * i_76_
					  + anIntArray2376[i_72_] * i_77_)
					 >> 16);
			    anIntArray2356[i_72_]
				= (anIntArray2356[i_72_] * i_77_
				   - anIntArray2376[i_72_] * i_76_) >> 16;
			    anIntArray2376[i_72_] = i_78_;
			}
			if (i_73_ != 0) {
			    int i_79_ = anIntArray2394[i_73_];
			    int i_80_ = anIntArray2418[i_73_];
			    int i_81_ = ((anIntArray2356[i_72_] * i_80_
					  - anIntArray2359[i_72_] * i_79_)
					 >> 16);
			    anIntArray2359[i_72_]
				= (anIntArray2356[i_72_] * i_79_
				   + anIntArray2359[i_72_] * i_80_) >> 16;
			    anIntArray2356[i_72_] = i_81_;
			}
			if (i_74_ != 0) {
			    int i_82_ = anIntArray2394[i_74_];
			    int i_83_ = anIntArray2418[i_74_];
			    int i_84_ = ((anIntArray2359[i_72_] * i_82_
					  + anIntArray2376[i_72_] * i_83_)
					 >> 16);
			    anIntArray2359[i_72_]
				= (anIntArray2359[i_72_] * i_83_
				   - anIntArray2376[i_72_] * i_82_) >> 16;
			    anIntArray2376[i_72_] = i_84_;
			}
			anIntArray2376[i_72_] += anInt2420;
			anIntArray2356[i_72_] += anInt2416;
			anIntArray2359[i_72_] += anInt2396;
		    }
		}
	    }
	} else if (i == 3) {
	    for (int i_85_ = 0; i_85_ < i_56_; i_85_++) {
		int i_86_ = is[i_85_];
		if (i_86_ < anIntArrayArray2388.length) {
		    int[] is_87_ = anIntArrayArray2388[i_86_];
		    for (int i_88_ = 0; i_88_ < is_87_.length; i_88_++) {
			int i_89_ = is_87_[i_88_];
			anIntArray2376[i_89_] -= anInt2420;
			anIntArray2356[i_89_] -= anInt2416;
			anIntArray2359[i_89_] -= anInt2396;
			anIntArray2376[i_89_]
			    = anIntArray2376[i_89_] * i_53_ / 128;
			anIntArray2356[i_89_]
			    = anIntArray2356[i_89_] * i_54_ / 128;
			anIntArray2359[i_89_]
			    = anIntArray2359[i_89_] * i_55_ / 128;
			anIntArray2376[i_89_] += anInt2420;
			anIntArray2356[i_89_] += anInt2416;
			anIntArray2359[i_89_] += anInt2396;
		    }
		}
	    }
	} else if (i == 5 && anIntArrayArray2360 != null
		   && anIntArray2379 != null) {
	    for (int i_90_ = 0; i_90_ < i_56_; i_90_++) {
		int i_91_ = is[i_90_];
		if (i_91_ < anIntArrayArray2360.length) {
		    int[] is_92_ = anIntArrayArray2360[i_91_];
		    for (int i_93_ = 0; i_93_ < is_92_.length; i_93_++) {
			int i_94_ = is_92_[i_93_];
			anIntArray2379[i_94_] += i_53_ * 8;
			if (anIntArray2379[i_94_] < 0)
			    anIntArray2379[i_94_] = 0;
			if (anIntArray2379[i_94_] > 255)
			    anIntArray2379[i_94_] = 255;
		    }
		}
	    }
	}
    }
    
    public void method546(boolean bool, boolean bool_95_, int i) {
	if (anInt2372 < 1600) {
	    for (int i_96_ = 0; i_96_ < anInt2372; i_96_++)
		anIntArray2409[i_96_] = 0;
	    for (int i_97_ = 0; i_97_ < anInt2366; i_97_++) {
		if (anIntArray2378 == null || anIntArray2378[i_97_] != -1) {
		    int i_98_ = anIntArray2385[i_97_];
		    int i_99_ = anIntArray2363[i_97_];
		    int i_100_ = anIntArray2368[i_97_];
		    int i_101_ = anIntArray2395[i_98_];
		    int i_102_ = anIntArray2395[i_99_];
		    int i_103_ = anIntArray2395[i_100_];
		    if (bool && (i_101_ == -5000 || i_102_ == -5000
				 || i_103_ == -5000)) {
			int i_104_ = anIntArray2392[i_98_];
			int i_105_ = anIntArray2392[i_99_];
			int i_106_ = anIntArray2392[i_100_];
			int i_107_ = anIntArray2421[i_98_];
			int i_108_ = anIntArray2421[i_99_];
			int i_109_ = anIntArray2421[i_100_];
			int i_110_ = anIntArray2414[i_98_];
			int i_111_ = anIntArray2414[i_99_];
			int i_112_ = anIntArray2414[i_100_];
			i_104_ -= i_105_;
			i_106_ -= i_105_;
			i_107_ -= i_108_;
			i_109_ -= i_108_;
			i_110_ -= i_111_;
			i_112_ -= i_111_;
			int i_113_ = i_107_ * i_112_ - i_110_ * i_109_;
			int i_114_ = i_110_ * i_106_ - i_104_ * i_112_;
			int i_115_ = i_104_ * i_109_ - i_107_ * i_106_;
			if (i_105_ * i_113_ + i_108_ * i_114_ + i_111_ * i_115_
			    > 0) {
			    aBooleanArray2404[i_97_] = true;
			    int i_116_ = ((anIntArray2405[i_98_]
					   + anIntArray2405[i_99_]
					   + anIntArray2405[i_100_]) / 3
					  + anInt2358);
			    anIntArrayArray2415[i_116_]
				[anIntArray2409[i_116_]++]
				= i_97_;
			}
		    } else {
			if (bool_95_ && method537(anInt2397, anInt2407,
						  anIntArray2403[i_98_],
						  anIntArray2403[i_99_],
						  anIntArray2403[i_100_],
						  i_101_, i_102_, i_103_)) {
			    anIntArray2408[anInt2413++] = i;
			    bool_95_ = false;
			}
			if (((i_101_ - i_102_) * (anIntArray2403[i_100_]
						  - anIntArray2403[i_99_])
			     - ((anIntArray2403[i_98_] - anIntArray2403[i_99_])
				* (i_103_ - i_102_)))
			    > 0) {
			    aBooleanArray2404[i_97_] = false;
			    if (i_101_ < 0 || i_102_ < 0 || i_103_ < 0
				|| i_101_ > Class39_Sub5_Sub10_Sub2.anInt2461
				|| i_102_ > Class39_Sub5_Sub10_Sub2.anInt2461
				|| i_103_ > Class39_Sub5_Sub10_Sub2.anInt2461)
				aBooleanArray2412[i_97_] = true;
			    else
				aBooleanArray2412[i_97_] = false;
			    int i_117_ = ((anIntArray2405[i_98_]
					   + anIntArray2405[i_99_]
					   + anIntArray2405[i_100_]) / 3
					  + anInt2358);
			    anIntArrayArray2415[i_117_]
				[anIntArray2409[i_117_]++]
				= i_97_;
			}
		    }
		}
	    }
	    if (anIntArray2365 == null) {
		for (int i_118_ = anInt2372 - 1; i_118_ >= 0; i_118_--) {
		    int i_119_ = anIntArray2409[i_118_];
		    if (i_119_ > 0) {
			int[] is = anIntArrayArray2415[i_118_];
			for (int i_120_ = 0; i_120_ < i_119_; i_120_++)
			    method552(is[i_120_]);
		    }
		}
	    } else {
		for (int i_121_ = 0; i_121_ < 12; i_121_++) {
		    anIntArray2417[i_121_] = 0;
		    anIntArray2391[i_121_] = 0;
		}
		for (int i_122_ = anInt2372 - 1; i_122_ >= 0; i_122_--) {
		    int i_123_ = anIntArray2409[i_122_];
		    if (i_123_ > 0) {
			int[] is = anIntArrayArray2415[i_122_];
			for (int i_124_ = 0; i_124_ < i_123_; i_124_++) {
			    int i_125_ = is[i_124_];
			    int i_126_ = anIntArray2365[i_125_];
			    int i_127_ = anIntArray2417[i_126_]++;
			    anIntArrayArray2410[i_126_][i_127_] = i_125_;
			    if (i_126_ < 10)
				anIntArray2391[i_126_] += i_122_;
			    else if (i_126_ == 10)
				anIntArray2422[i_127_] = i_122_;
			    else
				anIntArray2399[i_127_] = i_122_;
			}
		    }
		}
		int i_128_ = 0;
		if (anIntArray2417[1] > 0 || anIntArray2417[2] > 0)
		    i_128_ = ((anIntArray2391[1] + anIntArray2391[2])
			      / (anIntArray2417[1] + anIntArray2417[2]));
		int i_129_ = 0;
		if (anIntArray2417[3] > 0 || anIntArray2417[4] > 0)
		    i_129_ = ((anIntArray2391[3] + anIntArray2391[4])
			      / (anIntArray2417[3] + anIntArray2417[4]));
		int i_130_ = 0;
		if (anIntArray2417[6] > 0 || anIntArray2417[8] > 0)
		    i_130_ = ((anIntArray2391[6] + anIntArray2391[8])
			      / (anIntArray2417[6] + anIntArray2417[8]));
		int i_131_ = 0;
		int i_132_ = anIntArray2417[10];
		int[] is = anIntArrayArray2410[10];
		int[] is_133_ = anIntArray2422;
		if (i_131_ == i_132_) {
		    i_131_ = 0;
		    i_132_ = anIntArray2417[11];
		    is = anIntArrayArray2410[11];
		    is_133_ = anIntArray2399;
		}
		int i_134_;
		if (i_131_ < i_132_)
		    i_134_ = is_133_[i_131_];
		else
		    i_134_ = -1000;
		for (int i_135_ = 0; i_135_ < 10; i_135_++) {
		    while (i_135_ == 0) {
			if (i_134_ <= i_128_)
			    break;
			method552(is[i_131_++]);
			if (i_131_ == i_132_
			    && is != anIntArrayArray2410[11]) {
			    i_131_ = 0;
			    i_132_ = anIntArray2417[11];
			    is = anIntArrayArray2410[11];
			    is_133_ = anIntArray2399;
			}
			if (i_131_ < i_132_)
			    i_134_ = is_133_[i_131_];
			else
			    i_134_ = -1000;
		    }
		    while (i_135_ == 3) {
			if (i_134_ <= i_129_)
			    break;
			method552(is[i_131_++]);
			if (i_131_ == i_132_
			    && is != anIntArrayArray2410[11]) {
			    i_131_ = 0;
			    i_132_ = anIntArray2417[11];
			    is = anIntArrayArray2410[11];
			    is_133_ = anIntArray2399;
			}
			if (i_131_ < i_132_)
			    i_134_ = is_133_[i_131_];
			else
			    i_134_ = -1000;
		    }
		    while (i_135_ == 5 && i_134_ > i_130_) {
			method552(is[i_131_++]);
			if (i_131_ == i_132_
			    && is != anIntArrayArray2410[11]) {
			    i_131_ = 0;
			    i_132_ = anIntArray2417[11];
			    is = anIntArrayArray2410[11];
			    is_133_ = anIntArray2399;
			}
			if (i_131_ < i_132_)
			    i_134_ = is_133_[i_131_];
			else
			    i_134_ = -1000;
		    }
		    int i_136_ = anIntArray2417[i_135_];
		    int[] is_137_ = anIntArrayArray2410[i_135_];
		    for (int i_138_ = 0; i_138_ < i_136_; i_138_++)
			method552(is_137_[i_138_]);
		}
		while (i_134_ != -1000) {
		    method552(is[i_131_++]);
		    if (i_131_ == i_132_ && is != anIntArrayArray2410[11]) {
			i_131_ = 0;
			is = anIntArrayArray2410[11];
			i_132_ = anIntArray2417[11];
			is_133_ = anIntArray2399;
		    }
		    if (i_131_ < i_132_)
			i_134_ = is_133_[i_131_];
		    else
			i_134_ = -1000;
		}
	    }
	}
    }
    
    public Model method547(boolean bool) {
	if (!bool && anIntArray2406.length < anInt2366)
	    anIntArray2406 = new int[anInt2366 + 100];
	return method544(bool, aClass39_Sub5_Sub4_Sub6_2389, anIntArray2406);
    }
    
    public void method548(int i, int i_139_, int i_140_, int i_141_,
			  int i_142_, boolean bool) {
	int i_143_
	    = (int) Math.sqrt((double) (i_140_ * i_140_ + i_141_ * i_141_
					+ i_142_ * i_142_));
	int i_144_ = i_139_ * i_143_ >> 8;
	if (anIntArray2354 == null) {
	    anIntArray2354 = new int[anInt2366];
	    anIntArray2369 = new int[anInt2366];
	    anIntArray2364 = new int[anInt2366];
	}
	if (aClass48Array2361 == null) {
	    aClass48Array2361 = new Class48[anInt2367];
	    for (int i_145_ = 0; i_145_ < anInt2367; i_145_++)
		aClass48Array2361[i_145_] = new Class48();
	}
	for (int i_146_ = 0; i_146_ < anInt2366; i_146_++) {
	    int i_147_ = anIntArray2385[i_146_];
	    int i_148_ = anIntArray2363[i_146_];
	    int i_149_ = anIntArray2368[i_146_];
	    int i_150_ = anIntArray2376[i_148_] - anIntArray2376[i_147_];
	    int i_151_ = anIntArray2356[i_148_] - anIntArray2356[i_147_];
	    int i_152_ = anIntArray2359[i_148_] - anIntArray2359[i_147_];
	    int i_153_ = anIntArray2376[i_149_] - anIntArray2376[i_147_];
	    int i_154_ = anIntArray2356[i_149_] - anIntArray2356[i_147_];
	    int i_155_ = anIntArray2359[i_149_] - anIntArray2359[i_147_];
	    int i_156_ = i_151_ * i_155_ - i_154_ * i_152_;
	    int i_157_ = i_152_ * i_153_ - i_155_ * i_150_;
	    int i_158_;
	    for (i_158_ = i_150_ * i_154_ - i_153_ * i_151_;
		 (i_156_ > 8192 || i_157_ > 8192 || i_158_ > 8192
		  || i_156_ < -8192 || i_157_ < -8192 || i_158_ < -8192);
		 i_158_ >>= 1) {
		i_156_ >>= 1;
		i_157_ >>= 1;
	    }
	    int i_159_
		= (int) Math.sqrt((double) (i_156_ * i_156_ + i_157_ * i_157_
					    + i_158_ * i_158_));
	    if (i_159_ <= 0)
		i_159_ = 1;
	    i_156_ = i_156_ * 256 / i_159_;
	    i_157_ = i_157_ * 256 / i_159_;
	    i_158_ = i_158_ * 256 / i_159_;
	    if (anIntArray2378 == null
		|| (anIntArray2378[i_146_] & 0x1) == 0) {
		Class48 class48 = aClass48Array2361[i_147_];
		class48.anInt901 += i_156_;
		class48.anInt903 += i_157_;
		class48.anInt900 += i_158_;
		class48.anInt908++;
		class48 = aClass48Array2361[i_148_];
		class48.anInt901 += i_156_;
		class48.anInt903 += i_157_;
		class48.anInt900 += i_158_;
		class48.anInt908++;
		class48 = aClass48Array2361[i_149_];
		class48.anInt901 += i_156_;
		class48.anInt903 += i_157_;
		class48.anInt900 += i_158_;
		class48.anInt908++;
	    } else {
		int i_160_ = i + (i_140_ * i_156_ + i_141_ * i_157_
				  + i_142_ * i_158_) / (i_144_ + i_144_ / 2);
		anIntArray2354[i_146_]
		    = method566(anIntArray2371[i_146_], i_160_,
				anIntArray2378[i_146_]);
	    }
	}
	if (bool)
	    method560(i, i_144_, i_140_, i_141_, i_142_);
	else {
	    aClass48Array2380 = new Class48[anInt2367];
	    for (int i_161_ = 0; i_161_ < anInt2367; i_161_++) {
		Class48 class48 = aClass48Array2361[i_161_];
		Class48 class48_162_
		    = aClass48Array2380[i_161_] = new Class48();
		class48_162_.anInt901 = class48.anInt901;
		class48_162_.anInt903 = class48.anInt903;
		class48_162_.anInt900 = class48.anInt900;
		class48_162_.anInt908 = class48.anInt908;
	    }
	    anInt2377 = (i << 16) + (i_144_ & 0xffff);
	}
    }
    
    public void method549(int i, int i_163_, int i_164_, int i_165_,
			  int i_166_, int i_167_, int i_168_) {
	if (anInt2381 != 2 && anInt2381 != 1)
	    method568();
	int i_169_ = Class39_Sub5_Sub10_Sub2.anInt2462;
	int i_170_ = Class39_Sub5_Sub10_Sub2.anInt2467;
	int i_171_ = anIntArray2394[i];
	int i_172_ = anIntArray2418[i];
	int i_173_ = anIntArray2394[i_163_];
	int i_174_ = anIntArray2418[i_163_];
	int i_175_ = anIntArray2394[i_164_];
	int i_176_ = anIntArray2418[i_164_];
	int i_177_ = anIntArray2394[i_165_];
	int i_178_ = anIntArray2418[i_165_];
	int i_179_ = i_167_ * i_177_ + i_168_ * i_178_ >> 16;
	for (int i_180_ = 0; i_180_ < anInt2367; i_180_++) {
	    int i_181_ = anIntArray2376[i_180_];
	    int i_182_ = anIntArray2356[i_180_];
	    int i_183_ = anIntArray2359[i_180_];
	    if (i_164_ != 0) {
		int i_184_ = i_182_ * i_175_ + i_181_ * i_176_ >> 16;
		i_182_ = i_182_ * i_176_ - i_181_ * i_175_ >> 16;
		i_181_ = i_184_;
	    }
	    if (i != 0) {
		int i_185_ = i_182_ * i_172_ - i_183_ * i_171_ >> 16;
		i_183_ = i_182_ * i_171_ + i_183_ * i_172_ >> 16;
		i_182_ = i_185_;
	    }
	    if (i_163_ != 0) {
		int i_186_ = i_183_ * i_173_ + i_181_ * i_174_ >> 16;
		i_183_ = i_183_ * i_174_ - i_181_ * i_173_ >> 16;
		i_181_ = i_186_;
	    }
	    i_181_ += i_166_;
	    i_182_ += i_167_;
	    i_183_ += i_168_;
	    int i_187_ = i_182_ * i_178_ - i_183_ * i_177_ >> 16;
	    i_183_ = i_182_ * i_177_ + i_183_ * i_178_ >> 16;
	    i_182_ = i_187_;
	    anIntArray2405[i_180_] = i_183_ - i_179_;
	    anIntArray2395[i_180_] = i_169_ + (i_181_ * Resizable.zoom) / i_183_;
	    anIntArray2403[i_180_] = i_170_ + (i_182_ * Resizable.zoom) / i_183_;
	    if (anInt2355 > 0) {
		anIntArray2392[i_180_] = i_181_;
		anIntArray2421[i_180_] = i_182_;
		anIntArray2414[i_180_] = i_183_;
	    }
	}
	try {
	    method546(false, false, 0);
	} catch (Exception exception) {
	    /* empty */
	}
    }
    
    public static void method550() {
	aClass39_Sub5_Sub4_Sub6_2382 = null;
	anIntArray2390 = null;
	aClass39_Sub5_Sub4_Sub6_2389 = null;
	anIntArray2406 = null;
	aBooleanArray2412 = null;
	aBooleanArray2404 = null;
	anIntArray2395 = null;
	anIntArray2403 = null;
	anIntArray2405 = null;
	anIntArray2392 = null;
	anIntArray2421 = null;
	anIntArray2414 = null;
	anIntArray2409 = null;
	anIntArrayArray2415 = null;
	anIntArray2417 = null;
	anIntArrayArray2410 = null;
	anIntArray2422 = null;
	anIntArray2399 = null;
	anIntArray2391 = null;
	anIntArray2419 = null;
	anIntArray2393 = null;
	anIntArray2411 = null;
	anIntArray2408 = null;
	anIntArray2394 = null;
	anIntArray2418 = null;
	anIntArray2401 = null;
	anIntArray2400 = null;
	anIntArray2402 = null;
    }
    
    public void method551() {
	aClass48Array2361 = null;
	anInt2381 = 0;
	for (int i = 0; i < anInt2367; i++) {
	    int i_188_ = anIntArray2359[i];
	    anIntArray2359[i] = anIntArray2376[i];
	    anIntArray2376[i] = -i_188_;
	}
    }
    
    public void method552(int i) {
	if (aBooleanArray2404[i])
	    method558(i);
	else {
	    int i_189_ = anIntArray2385[i];
	    int i_190_ = anIntArray2363[i];
	    int i_191_ = anIntArray2368[i];
	    Class39_Sub5_Sub10_Sub2.aBoolean2470 = aBooleanArray2412[i];
	    if (anIntArray2379 == null)
		Class39_Sub5_Sub10_Sub2.anInt2468 = 0;
	    else
		Class39_Sub5_Sub10_Sub2.anInt2468 = anIntArray2379[i];
	    int i_192_;
	    if (anIntArray2378 == null)
		i_192_ = 0;
	    else
		i_192_ = anIntArray2378[i] & 0x3;
	    if (i_192_ == 0)
		Class39_Sub5_Sub10_Sub2.method660(anIntArray2403[i_189_],
						  anIntArray2403[i_190_],
						  anIntArray2403[i_191_],
						  anIntArray2395[i_189_],
						  anIntArray2395[i_190_],
						  anIntArray2395[i_191_],
						  anIntArray2354[i],
						  anIntArray2369[i],
						  anIntArray2364[i]);
	    else if (i_192_ == 1)
		Class39_Sub5_Sub10_Sub2.method662(anIntArray2403[i_189_],
						  anIntArray2403[i_190_],
						  anIntArray2403[i_191_],
						  anIntArray2395[i_189_],
						  anIntArray2395[i_190_],
						  anIntArray2395[i_191_],
						  (anIntArray2401
						   [anIntArray2354[i]]));
	    else if (i_192_ == 2) {
		int i_193_ = anIntArray2378[i] >> 2;
		int i_194_ = anIntArray2370[i_193_];
		int i_195_ = anIntArray2373[i_193_];
		int i_196_ = anIntArray2387[i_193_];
		Class39_Sub5_Sub10_Sub2.method665
		    (anIntArray2403[i_189_], anIntArray2403[i_190_],
		     anIntArray2403[i_191_], anIntArray2395[i_189_],
		     anIntArray2395[i_190_], anIntArray2395[i_191_],
		     anIntArray2354[i], anIntArray2369[i], anIntArray2364[i],
		     anIntArray2392[i_194_], anIntArray2392[i_195_],
		     anIntArray2392[i_196_], anIntArray2421[i_194_],
		     anIntArray2421[i_195_], anIntArray2421[i_196_],
		     anIntArray2414[i_194_], anIntArray2414[i_195_],
		     anIntArray2414[i_196_], anIntArray2371[i]);
	    } else if (i_192_ == 3) {
		int i_197_ = anIntArray2378[i] >> 2;
		int i_198_ = anIntArray2370[i_197_];
		int i_199_ = anIntArray2373[i_197_];
		int i_200_ = anIntArray2387[i_197_];
		Class39_Sub5_Sub10_Sub2.method665
		    (anIntArray2403[i_189_], anIntArray2403[i_190_],
		     anIntArray2403[i_191_], anIntArray2395[i_189_],
		     anIntArray2395[i_190_], anIntArray2395[i_191_],
		     anIntArray2354[i], anIntArray2354[i], anIntArray2354[i],
		     anIntArray2392[i_198_], anIntArray2392[i_199_],
		     anIntArray2392[i_200_], anIntArray2421[i_198_],
		     anIntArray2421[i_199_], anIntArray2421[i_200_],
		     anIntArray2414[i_198_], anIntArray2414[i_199_],
		     anIntArray2414[i_200_], anIntArray2371[i]);
	    }
	}
    }
    
    public void method553() {
	if (anIntArray2357 != null) {
	    int[] is = new int[256];
	    int i = 0;
	    for (int i_201_ = 0; i_201_ < anInt2367; i_201_++) {
		int i_202_ = anIntArray2357[i_201_];
		is[i_202_]++;
		if (i_202_ > i)
		    i = i_202_;
	    }
	    anIntArrayArray2388 = new int[i + 1][];
	    for (int i_203_ = 0; i_203_ <= i; i_203_++) {
		anIntArrayArray2388[i_203_] = new int[is[i_203_]];
		is[i_203_] = 0;
	    }
	    for (int i_204_ = 0; i_204_ < anInt2367; i_204_++) {
		int i_205_ = anIntArray2357[i_204_];
		anIntArrayArray2388[i_205_][is[i_205_]++] = i_204_;
	    }
	    anIntArray2357 = null;
	}
	if (anIntArray2375 != null) {
	    int[] is = new int[256];
	    int i = 0;
	    for (int i_206_ = 0; i_206_ < anInt2366; i_206_++) {
		int i_207_ = anIntArray2375[i_206_];
		is[i_207_]++;
		if (i_207_ > i)
		    i = i_207_;
	    }
	    anIntArrayArray2360 = new int[i + 1][];
	    for (int i_208_ = 0; i_208_ <= i; i_208_++) {
		anIntArrayArray2360[i_208_] = new int[is[i_208_]];
		is[i_208_] = 0;
	    }
	    for (int i_209_ = 0; i_209_ < anInt2366; i_209_++) {
		int i_210_ = anIntArray2375[i_209_];
		anIntArrayArray2360[i_210_][is[i_210_]++] = i_209_;
	    }
	    anIntArray2375 = null;
	}
    }
    
    public void method554(int i, int i_211_, int i_212_) {
	anInt2381 = 0;
	for (int i_213_ = 0; i_213_ < anInt2367; i_213_++) {
	    anIntArray2376[i_213_] += i;
	    anIntArray2356[i_213_] += i_211_;
	    anIntArray2359[i_213_] += i_212_;
	}
    }
    
    public void method555() {
	aClass48Array2361 = null;
	anInt2381 = 0;
	for (int i = 0; i < anInt2367; i++) {
	    int i_214_ = anIntArray2376[i];
	    anIntArray2376[i] = anIntArray2359[i];
	    anIntArray2359[i] = -i_214_;
	}
    }
    
    public int method556(Model class39_sub5_sub4_sub6_215_,
			 int i) {
	int i_216_ = -1;
	int i_217_ = class39_sub5_sub4_sub6_215_.anIntArray2376[i];
	int i_218_ = class39_sub5_sub4_sub6_215_.anIntArray2356[i];
	int i_219_ = class39_sub5_sub4_sub6_215_.anIntArray2359[i];
	for (int i_220_ = 0; i_220_ < anInt2367; i_220_++) {
	    if (i_217_ == anIntArray2376[i_220_]
		&& i_218_ == anIntArray2356[i_220_]
		&& i_219_ == anIntArray2359[i_220_]) {
		i_216_ = i_220_;
		break;
	    }
	}
	if (i_216_ == -1) {
	    anIntArray2376[anInt2367] = i_217_;
	    anIntArray2356[anInt2367] = i_218_;
	    anIntArray2359[anInt2367] = i_219_;
	    if (class39_sub5_sub4_sub6_215_.anIntArray2357 != null)
		anIntArray2357[anInt2367]
		    = class39_sub5_sub4_sub6_215_.anIntArray2357[i];
	    i_216_ = anInt2367++;
	}
	return i_216_;
    }
    
    public void method488(int i, int i_221_, int i_222_, int i_223_,
			  int i_224_, int i_225_, int i_226_, int i_227_,
			  int i_228_) {
	if (anInt2381 != 1)
	    method542();
	int i_229_ = i_227_ * i_224_ - i_225_ * i_223_ >> 16;
	int i_230_ = i_226_ * i_221_ + i_229_ * i_222_ >> 16;
	int i_231_ = anInt2386 * i_222_ >> 16;
	int i_232_ = i_230_ + i_231_;
	if (i_232_ > 50 && i_230_ < 14000) {
	    int i_233_ = i_227_ * i_223_ + i_225_ * i_224_ >> 16;
	    int i_234_ = (i_233_ - anInt2386) * Resizable.zoom;
	    if (i_234_ / i_232_ < Class39_Sub5_Sub10_Sub2.anInt2457) {
		int i_235_ = (i_233_ + anInt2386) * Resizable.zoom;
		if (i_235_ / i_232_ > Class39_Sub5_Sub10_Sub2.anInt2453) {
		    int i_236_ = i_226_ * i_222_ - i_229_ * i_221_ >> 16;
		    int i_237_ = anInt2386 * i_221_ >> 16;
		    int i_238_ = (i_236_ + i_237_) * Resizable.zoom;
		    if (i_238_ / i_232_ > Class39_Sub5_Sub10_Sub2.anInt2464) {
			int i_239_ = i_237_ + (anInt1726 * i_222_ >> 16);
			int i_240_ = (i_236_ - i_239_) * Resizable.zoom;
			if (i_240_ / i_232_
			    < Class39_Sub5_Sub10_Sub2.anInt2454) {
			    int i_241_ = i_231_ + (anInt1726 * i_221_ >> 16);
			    boolean bool = false;
			    boolean bool_242_ = false;
			    if (i_230_ - i_241_ <= 50)
				bool_242_ = true;
			    boolean bool_243_ = bool_242_ || anInt2355 > 0;
			    boolean bool_244_ = false;
			    if (i_228_ > 0 && aBoolean2398) {
				int i_245_ = i_230_ - i_231_;
				if (i_245_ <= 50)
				    i_245_ = 50;
				if (i_233_ > 0) {
				    i_234_ /= i_232_;
				    i_235_ /= i_245_;
				} else {
				    i_235_ /= i_232_;
				    i_234_ /= i_245_;
				}
				if (i_236_ > 0) {
				    i_240_ /= i_232_;
				    i_238_ /= i_245_;
				} else {
				    i_238_ /= i_232_;
				    i_240_ /= i_245_;
				}
				int i_246_
				    = (anInt2397
				       - Class39_Sub5_Sub10_Sub2.anInt2462);
				int i_247_
				    = (anInt2407
				       - Class39_Sub5_Sub10_Sub2.anInt2467);
				if (i_246_ > i_234_ && i_246_ < i_235_
				    && i_247_ > i_240_ && i_247_ < i_238_) {
				    if (aBoolean2374)
					anIntArray2408[anInt2413++] = i_228_;
				    else
					bool_244_ = true;
				}
			    }
			    int i_248_ = Class39_Sub5_Sub10_Sub2.anInt2462;
			    int i_249_ = Class39_Sub5_Sub10_Sub2.anInt2467;
			    int i_250_ = 0;
			    int i_251_ = 0;
			    if (i != 0) {
				i_250_ = anIntArray2394[i];
				i_251_ = anIntArray2418[i];
			    }
			    for (int i_252_ = 0; i_252_ < anInt2367;
				 i_252_++) {
				int i_253_ = anIntArray2376[i_252_];
				int i_254_ = anIntArray2356[i_252_];
				int i_255_ = anIntArray2359[i_252_];
				if (i != 0) {
				    int i_256_
					= (i_255_ * i_250_ + i_253_ * i_251_
					   >> 16);
				    i_255_ = (i_255_ * i_251_ - i_253_ * i_250_
					      >> 16);
				    i_253_ = i_256_;
				}
				i_253_ += i_225_;
				i_254_ += i_226_;
				i_255_ += i_227_;
				int i_257_
				    = i_255_ * i_223_ + i_253_ * i_224_ >> 16;
				i_255_
				    = i_255_ * i_224_ - i_253_ * i_223_ >> 16;
				i_253_ = i_257_;
				i_257_
				    = i_254_ * i_222_ - i_255_ * i_221_ >> 16;
				i_255_
				    = i_254_ * i_221_ + i_255_ * i_222_ >> 16;
				i_254_ = i_257_;
				anIntArray2405[i_252_] = i_255_ - i_230_;
				if (i_255_ >= 50) {
				    anIntArray2395[i_252_]
					= i_248_ + (i_253_ * Resizable.zoom) / i_255_;
				    anIntArray2403[i_252_]
					= i_249_ + (i_254_ * Resizable.zoom) / i_255_;
				} else {
				    anIntArray2395[i_252_] = -5000;
				    bool = true;
				}
				if (bool_243_) {
				    anIntArray2392[i_252_] = i_253_;
				    anIntArray2421[i_252_] = i_254_;
				    anIntArray2414[i_252_] = i_255_;
				}
			    }
			    try {
				method546(bool, bool_244_, i_228_);
			    } catch (Exception exception) {
				/* empty */
			    }
			}
		    }
		}
	    }
	}
    }
    
    public void method557(Class39_Sub5_Sub12 class39_sub5_sub12, int i) {
	if (anIntArrayArray2388 != null && i != -1) {
	    aClass48Array2361 = null;
	    anInt2381 = 0;
	    Class51 class51 = class39_sub5_sub12.aClass51Array1855[i];
	    Class39_Sub12 class39_sub12 = class51.aClass39_Sub12_938;
	    anInt2420 = 0;
	    anInt2416 = 0;
	    anInt2396 = 0;
	    for (int i_258_ = 0; i_258_ < class51.anInt948; i_258_++) {
		int i_259_ = class51.anIntArray945[i_258_];
		method545(class39_sub12.anIntArray1497[i_259_],
			  class39_sub12.anIntArrayArray1486[i_259_],
			  class51.anIntArray942[i_258_],
			  class51.anIntArray946[i_258_],
			  class51.anIntArray944[i_258_]);
	    }
	}
    }
    
    public void method558(int i) {
	int i_260_ = Class39_Sub5_Sub10_Sub2.anInt2462;
	int i_261_ = Class39_Sub5_Sub10_Sub2.anInt2467;
	int i_262_ = 0;
	int i_263_ = anIntArray2385[i];
	int i_264_ = anIntArray2363[i];
	int i_265_ = anIntArray2368[i];
	int i_266_ = anIntArray2414[i_263_];
	int i_267_ = anIntArray2414[i_264_];
	int i_268_ = anIntArray2414[i_265_];
	if (anIntArray2379 == null)
	    Class39_Sub5_Sub10_Sub2.anInt2468 = 0;
	else
	    Class39_Sub5_Sub10_Sub2.anInt2468 = anIntArray2379[i];
	if (i_266_ >= 50) {
	    anIntArray2419[i_262_] = anIntArray2395[i_263_];
	    anIntArray2393[i_262_] = anIntArray2403[i_263_];
	    anIntArray2411[i_262_++] = anIntArray2354[i];
	} else {
	    int i_269_ = anIntArray2392[i_263_];
	    int i_270_ = anIntArray2421[i_263_];
	    int i_271_ = anIntArray2354[i];
	    if (i_268_ >= 50) {
		int i_272_ = (50 - i_266_) * anIntArray2400[i_268_ - i_266_];
		anIntArray2419[i_262_]
		    = (i_260_
		       + ((i_269_ + ((anIntArray2392[i_265_] - i_269_) * i_272_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2393[i_262_]
		    = (i_261_
		       + ((i_270_ + ((anIntArray2421[i_265_] - i_270_) * i_272_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2411[i_262_++]
		    = i_271_ + ((anIntArray2364[i] - i_271_) * i_272_ >> 16);
	    }
	    if (i_267_ >= 50) {
		int i_273_ = (50 - i_266_) * anIntArray2400[i_267_ - i_266_];
		anIntArray2419[i_262_]
		    = (i_260_
		       + ((i_269_ + ((anIntArray2392[i_264_] - i_269_) * i_273_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2393[i_262_]
		    = (i_261_
		       + ((i_270_ + ((anIntArray2421[i_264_] - i_270_) * i_273_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2411[i_262_++]
		    = i_271_ + ((anIntArray2369[i] - i_271_) * i_273_ >> 16);
	    }
	}
	if (i_267_ >= 50) {
	    anIntArray2419[i_262_] = anIntArray2395[i_264_];
	    anIntArray2393[i_262_] = anIntArray2403[i_264_];
	    anIntArray2411[i_262_++] = anIntArray2369[i];
	} else {
	    int i_274_ = anIntArray2392[i_264_];
	    int i_275_ = anIntArray2421[i_264_];
	    int i_276_ = anIntArray2369[i];
	    if (i_266_ >= 50) {
		int i_277_ = (50 - i_267_) * anIntArray2400[i_266_ - i_267_];
		anIntArray2419[i_262_]
		    = (i_260_
		       + ((i_274_ + ((anIntArray2392[i_263_] - i_274_) * i_277_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2393[i_262_]
		    = (i_261_
		       + ((i_275_ + ((anIntArray2421[i_263_] - i_275_) * i_277_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2411[i_262_++]
		    = i_276_ + ((anIntArray2354[i] - i_276_) * i_277_ >> 16);
	    }
	    if (i_268_ >= 50) {
		int i_278_ = (50 - i_267_) * anIntArray2400[i_268_ - i_267_];
		anIntArray2419[i_262_]
		    = (i_260_
		       + ((i_274_ + ((anIntArray2392[i_265_] - i_274_) * i_278_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2393[i_262_]
		    = (i_261_
		       + ((i_275_ + ((anIntArray2421[i_265_] - i_275_) * i_278_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2411[i_262_++]
		    = i_276_ + ((anIntArray2364[i] - i_276_) * i_278_ >> 16);
	    }
	}
	if (i_268_ >= 50) {
	    anIntArray2419[i_262_] = anIntArray2395[i_265_];
	    anIntArray2393[i_262_] = anIntArray2403[i_265_];
	    anIntArray2411[i_262_++] = anIntArray2364[i];
	} else {
	    int i_279_ = anIntArray2392[i_265_];
	    int i_280_ = anIntArray2421[i_265_];
	    int i_281_ = anIntArray2364[i];
	    if (i_267_ >= 50) {
		int i_282_ = (50 - i_268_) * anIntArray2400[i_267_ - i_268_];
		anIntArray2419[i_262_]
		    = (i_260_
		       + ((i_279_ + ((anIntArray2392[i_264_] - i_279_) * i_282_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2393[i_262_]
		    = (i_261_
		       + ((i_280_ + ((anIntArray2421[i_264_] - i_280_) * i_282_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2411[i_262_++]
		    = i_281_ + ((anIntArray2369[i] - i_281_) * i_282_ >> 16);
	    }
	    if (i_266_ >= 50) {
		int i_283_ = (50 - i_268_) * anIntArray2400[i_266_ - i_268_];
		anIntArray2419[i_262_]
		    = (i_260_
		       + ((i_279_ + ((anIntArray2392[i_263_] - i_279_) * i_283_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2393[i_262_]
		    = (i_261_
		       + ((i_280_ + ((anIntArray2421[i_263_] - i_280_) * i_283_
				    >> 16))
			  * Resizable.zoom) / 50);
		anIntArray2411[i_262_++]
		    = i_281_ + ((anIntArray2354[i] - i_281_) * i_283_ >> 16);
	    }
	}
	int i_284_ = anIntArray2419[0];
	int i_285_ = anIntArray2419[1];
	int i_286_ = anIntArray2419[2];
	int i_287_ = anIntArray2393[0];
	int i_288_ = anIntArray2393[1];
	int i_289_ = anIntArray2393[2];
	Class39_Sub5_Sub10_Sub2.aBoolean2470 = false;
	if (i_262_ == 3) {
	    if (i_284_ < 0 || i_285_ < 0 || i_286_ < 0
		|| i_284_ > Class39_Sub5_Sub10_Sub2.anInt2461
		|| i_285_ > Class39_Sub5_Sub10_Sub2.anInt2461
		|| i_286_ > Class39_Sub5_Sub10_Sub2.anInt2461)
		Class39_Sub5_Sub10_Sub2.aBoolean2470 = true;
	    int i_290_;
	    if (anIntArray2378 == null)
		i_290_ = 0;
	    else
		i_290_ = anIntArray2378[i] & 0x3;
	    if (i_290_ == 0)
		Class39_Sub5_Sub10_Sub2.method660(i_287_, i_288_, i_289_,
						  i_284_, i_285_, i_286_,
						  anIntArray2411[0],
						  anIntArray2411[1],
						  anIntArray2411[2]);
	    else if (i_290_ == 1)
		Class39_Sub5_Sub10_Sub2.method662(i_287_, i_288_, i_289_,
						  i_284_, i_285_, i_286_,
						  (anIntArray2401
						   [anIntArray2354[i]]));
	    else if (i_290_ == 2) {
		int i_291_ = anIntArray2378[i] >> 2;
		int i_292_ = anIntArray2370[i_291_];
		int i_293_ = anIntArray2373[i_291_];
		int i_294_ = anIntArray2387[i_291_];
		Class39_Sub5_Sub10_Sub2.method665
		    (i_287_, i_288_, i_289_, i_284_, i_285_, i_286_,
		     anIntArray2411[0], anIntArray2411[1], anIntArray2411[2],
		     anIntArray2392[i_292_], anIntArray2392[i_293_],
		     anIntArray2392[i_294_], anIntArray2421[i_292_],
		     anIntArray2421[i_293_], anIntArray2421[i_294_],
		     anIntArray2414[i_292_], anIntArray2414[i_293_],
		     anIntArray2414[i_294_], anIntArray2371[i]);
	    } else if (i_290_ == 3) {
		int i_295_ = anIntArray2378[i] >> 2;
		int i_296_ = anIntArray2370[i_295_];
		int i_297_ = anIntArray2373[i_295_];
		int i_298_ = anIntArray2387[i_295_];
		Class39_Sub5_Sub10_Sub2.method665
		    (i_287_, i_288_, i_289_, i_284_, i_285_, i_286_,
		     anIntArray2354[i], anIntArray2354[i], anIntArray2354[i],
		     anIntArray2392[i_296_], anIntArray2392[i_297_],
		     anIntArray2392[i_298_], anIntArray2421[i_296_],
		     anIntArray2421[i_297_], anIntArray2421[i_298_],
		     anIntArray2414[i_296_], anIntArray2414[i_297_],
		     anIntArray2414[i_298_], anIntArray2371[i]);
	    }
	}
	if (i_262_ == 4) {
	    if (i_284_ < 0 || i_285_ < 0 || i_286_ < 0
		|| i_284_ > Class39_Sub5_Sub10_Sub2.anInt2461
		|| i_285_ > Class39_Sub5_Sub10_Sub2.anInt2461
		|| i_286_ > Class39_Sub5_Sub10_Sub2.anInt2461
		|| anIntArray2419[3] < 0
		|| anIntArray2419[3] > Class39_Sub5_Sub10_Sub2.anInt2461)
		Class39_Sub5_Sub10_Sub2.aBoolean2470 = true;
	    int i_299_;
	    if (anIntArray2378 == null)
		i_299_ = 0;
	    else
		i_299_ = anIntArray2378[i] & 0x3;
	    if (i_299_ == 0) {
		Class39_Sub5_Sub10_Sub2.method660(i_287_, i_288_, i_289_,
						  i_284_, i_285_, i_286_,
						  anIntArray2411[0],
						  anIntArray2411[1],
						  anIntArray2411[2]);
		Class39_Sub5_Sub10_Sub2.method660(i_287_, i_289_,
						  anIntArray2393[3], i_284_,
						  i_286_, anIntArray2419[3],
						  anIntArray2411[0],
						  anIntArray2411[2],
						  anIntArray2411[3]);
	    } else if (i_299_ == 1) {
		int i_300_ = anIntArray2401[anIntArray2354[i]];
		Class39_Sub5_Sub10_Sub2.method662(i_287_, i_288_, i_289_,
						  i_284_, i_285_, i_286_,
						  i_300_);
		Class39_Sub5_Sub10_Sub2.method662(i_287_, i_289_,
						  anIntArray2393[3], i_284_,
						  i_286_, anIntArray2419[3],
						  i_300_);
	    } else if (i_299_ == 2) {
		int i_301_ = anIntArray2378[i] >> 2;
		int i_302_ = anIntArray2370[i_301_];
		int i_303_ = anIntArray2373[i_301_];
		int i_304_ = anIntArray2387[i_301_];
		Class39_Sub5_Sub10_Sub2.method665
		    (i_287_, i_288_, i_289_, i_284_, i_285_, i_286_,
		     anIntArray2411[0], anIntArray2411[1], anIntArray2411[2],
		     anIntArray2392[i_302_], anIntArray2392[i_303_],
		     anIntArray2392[i_304_], anIntArray2421[i_302_],
		     anIntArray2421[i_303_], anIntArray2421[i_304_],
		     anIntArray2414[i_302_], anIntArray2414[i_303_],
		     anIntArray2414[i_304_], anIntArray2371[i]);
		Class39_Sub5_Sub10_Sub2.method665
		    (i_287_, i_289_, anIntArray2393[3], i_284_, i_286_,
		     anIntArray2419[3], anIntArray2411[0], anIntArray2411[2],
		     anIntArray2411[3], anIntArray2392[i_302_],
		     anIntArray2392[i_303_], anIntArray2392[i_304_],
		     anIntArray2421[i_302_], anIntArray2421[i_303_],
		     anIntArray2421[i_304_], anIntArray2414[i_302_],
		     anIntArray2414[i_303_], anIntArray2414[i_304_],
		     anIntArray2371[i]);
	    } else if (i_299_ == 3) {
		int i_305_ = anIntArray2378[i] >> 2;
		int i_306_ = anIntArray2370[i_305_];
		int i_307_ = anIntArray2373[i_305_];
		int i_308_ = anIntArray2387[i_305_];
		Class39_Sub5_Sub10_Sub2.method665
		    (i_287_, i_288_, i_289_, i_284_, i_285_, i_286_,
		     anIntArray2354[i], anIntArray2354[i], anIntArray2354[i],
		     anIntArray2392[i_306_], anIntArray2392[i_307_],
		     anIntArray2392[i_308_], anIntArray2421[i_306_],
		     anIntArray2421[i_307_], anIntArray2421[i_308_],
		     anIntArray2414[i_306_], anIntArray2414[i_307_],
		     anIntArray2414[i_308_], anIntArray2371[i]);
		Class39_Sub5_Sub10_Sub2.method665
		    (i_287_, i_289_, anIntArray2393[3], i_284_, i_286_,
		     anIntArray2419[3], anIntArray2354[i], anIntArray2354[i],
		     anIntArray2354[i], anIntArray2392[i_306_],
		     anIntArray2392[i_307_], anIntArray2392[i_308_],
		     anIntArray2421[i_306_], anIntArray2421[i_307_],
		     anIntArray2421[i_308_], anIntArray2414[i_306_],
		     anIntArray2414[i_307_], anIntArray2414[i_308_],
		     anIntArray2371[i]);
	    }
	}
    }
    
    public void method559(int i, int i_309_, int i_310_) {
	aClass48Array2361 = null;
	anInt2381 = 0;
	for (int i_311_ = 0; i_311_ < anInt2367; i_311_++) {
	    anIntArray2376[i_311_] = anIntArray2376[i_311_] * i / 128;
	    anIntArray2356[i_311_] = anIntArray2356[i_311_] * i_309_ / 128;
	    anIntArray2359[i_311_] = anIntArray2359[i_311_] * i_310_ / 128;
	}
    }
    
    public void method560(int i, int i_312_, int i_313_, int i_314_,
			  int i_315_) {
	for (int i_316_ = 0; i_316_ < anInt2366; i_316_++) {
	    int i_317_ = anIntArray2385[i_316_];
	    int i_318_ = anIntArray2363[i_316_];
	    int i_319_ = anIntArray2368[i_316_];
	    if (anIntArray2378 == null) {
		int i_320_ = anIntArray2371[i_316_];
		Class48 class48 = aClass48Array2361[i_317_];
		int i_321_ = i + ((i_313_ * class48.anInt901
				   + i_314_ * class48.anInt903
				   + i_315_ * class48.anInt900)
				  / (i_312_ * class48.anInt908));
		anIntArray2354[i_316_] = method566(i_320_, i_321_, 0);
		class48 = aClass48Array2361[i_318_];
		i_321_ = i + ((i_313_ * class48.anInt901
			       + i_314_ * class48.anInt903
			       + i_315_ * class48.anInt900)
			      / (i_312_ * class48.anInt908));
		anIntArray2369[i_316_] = method566(i_320_, i_321_, 0);
		class48 = aClass48Array2361[i_319_];
		i_321_ = i + ((i_313_ * class48.anInt901
			       + i_314_ * class48.anInt903
			       + i_315_ * class48.anInt900)
			      / (i_312_ * class48.anInt908));
		anIntArray2364[i_316_] = method566(i_320_, i_321_, 0);
	    } else if ((anIntArray2378[i_316_] & 0x1) == 0) {
		int i_322_ = anIntArray2371[i_316_];
		int i_323_ = anIntArray2378[i_316_];
		Class48 class48 = aClass48Array2361[i_317_];
		int i_324_ = i + ((i_313_ * class48.anInt901
				   + i_314_ * class48.anInt903
				   + i_315_ * class48.anInt900)
				  / (i_312_ * class48.anInt908));
		anIntArray2354[i_316_] = method566(i_322_, i_324_, i_323_);
		class48 = aClass48Array2361[i_318_];
		i_324_ = i + ((i_313_ * class48.anInt901
			       + i_314_ * class48.anInt903
			       + i_315_ * class48.anInt900)
			      / (i_312_ * class48.anInt908));
		anIntArray2369[i_316_] = method566(i_322_, i_324_, i_323_);
		class48 = aClass48Array2361[i_319_];
		i_324_ = i + ((i_313_ * class48.anInt901
			       + i_314_ * class48.anInt903
			       + i_315_ * class48.anInt900)
			      / (i_312_ * class48.anInt908));
		anIntArray2364[i_316_] = method566(i_322_, i_324_, i_323_);
	    }
	}
	aClass48Array2361 = null;
	aClass48Array2380 = null;
	anIntArray2357 = null;
	anIntArray2375 = null;
	if (anIntArray2378 != null) {
	    for (int i_325_ = 0; i_325_ < anInt2366; i_325_++) {
		if ((anIntArray2378[i_325_] & 0x2) == 2)
		    return;
	    }
	}
	anIntArray2371 = null;
    }
    
    public int method561() {
	method542();
	return anInt2386;
    }
    
    public void method562() {
	aClass48Array2361 = null;
	anInt2381 = 0;
	for (int i = 0; i < anInt2367; i++) {
	    anIntArray2376[i] = -anIntArray2376[i];
	    anIntArray2359[i] = -anIntArray2359[i];
	}
    }
    
    public void method563(int i, int i_326_) {
	for (int i_327_ = 0; i_327_ < anInt2366; i_327_++) {
	    if (anIntArray2371[i_327_] == i)
		anIntArray2371[i_327_] = i_326_;
	}
    }
    
    public void method564() {
	if (anInt2381 != 3) {
	    anInt2381 = 3;
	    anInt1726 = 0;
	    anInt2362 = 0;
	    anInt2386 = 999999;
	    anInt2372 = -999999;
	    anInt2358 = -99999;
	    anInt2383 = 99999;
	    for (int i = 0; i < anInt2367; i++) {
		int i_328_ = anIntArray2376[i];
		int i_329_ = anIntArray2356[i];
		int i_330_ = anIntArray2359[i];
		if (i_328_ < anInt2386)
		    anInt2386 = i_328_;
		if (i_328_ > anInt2372)
		    anInt2372 = i_328_;
		if (i_330_ < anInt2383)
		    anInt2383 = i_330_;
		if (i_330_ > anInt2358)
		    anInt2358 = i_330_;
		if (-i_329_ > anInt1726)
		    anInt1726 = -i_329_;
		if (i_329_ > anInt2362)
		    anInt2362 = i_329_;
	    }
	}
    }
    
    public void method565(int i) {
	aClass48Array2361 = null;
	anInt2381 = 0;
	int i_331_ = anIntArray2394[i];
	int i_332_ = anIntArray2418[i];
	for (int i_333_ = 0; i_333_ < anInt2367; i_333_++) {
	    int i_334_ = ((anIntArray2356[i_333_] * i_332_
			   - anIntArray2359[i_333_] * i_331_)
			  >> 16);
	    anIntArray2359[i_333_] = (anIntArray2356[i_333_] * i_331_
				      + anIntArray2359[i_333_] * i_332_) >> 16;
	    anIntArray2356[i_333_] = i_334_;
	}
    }
    
    public static int method566(int i, int i_335_, int i_336_) {
	if ((i_336_ & 0x2) == 2) {
	    if (i_335_ < 0)
		i_335_ = 0;
	    else if (i_335_ > 127)
		i_335_ = 127;
	    i_335_ = anIntArray2402[i_335_];
	    return i_335_;
	}
	i_335_ = i_335_ * (i & 0x7f) >> 7;
	if (i_335_ < 2)
	    i_335_ = 2;
	else if (i_335_ > 126)
	    i_335_ = 126;
	return (i & 0xff80) + i_335_;
    }
    
    public Model method567(boolean bool) {
	if (!bool && anIntArray2390.length < anInt2366)
	    anIntArray2390 = new int[anInt2366 + 100];
	return method544(bool, aClass39_Sub5_Sub4_Sub6_2382, anIntArray2390);
    }
    
    public Model(byte[] is) {
	anInt2367 = 0;
	Buffer class39_sub6 = new Buffer(is);
	Buffer class39_sub6_337_ = new Buffer(is);
	Buffer class39_sub6_338_ = new Buffer(is);
	Buffer class39_sub6_339_ = new Buffer(is);
	Buffer class39_sub6_340_ = new Buffer(is);
	class39_sub6.offset = is.length - 18;
	int i = class39_sub6.getUword();
	int i_341_ = class39_sub6.getUword();
	int i_342_ = class39_sub6.getUbyte();
	int i_343_ = class39_sub6.getUbyte();
	int i_344_ = class39_sub6.getUbyte();
	int i_345_ = class39_sub6.getUbyte();
	int i_346_ = class39_sub6.getUbyte();
	int i_347_ = class39_sub6.getUbyte();
	int i_348_ = class39_sub6.getUword();
	int i_349_ = class39_sub6.getUword();
	int i_350_ = class39_sub6.getUword();
	int i_351_ = class39_sub6.getUword();
	int i_352_ = 0;
	int i_353_ = i_352_;
	i_352_ += i;
	int i_354_ = i_352_;
	i_352_ += i_341_;
	int i_355_ = i_352_;
	if (i_344_ == 255)
	    i_352_ += i_341_;
	else
	    i_355_ = -i_344_ - 1;
	int i_356_ = i_352_;
	if (i_346_ == 1)
	    i_352_ += i_341_;
	else
	    i_356_ = -1;
	int i_357_ = i_352_;
	if (i_343_ == 1)
	    i_352_ += i_341_;
	else
	    i_357_ = -1;
	int i_358_ = i_352_;
	if (i_347_ == 1)
	    i_352_ += i;
	else
	    i_358_ = -1;
	int i_359_ = i_352_;
	if (i_345_ == 1)
	    i_352_ += i_341_;
	else
	    i_359_ = -1;
	int i_360_ = i_352_;
	i_352_ += i_351_;
	int i_361_ = i_352_;
	i_352_ += i_341_ * 2;
	int i_362_ = i_352_;
	i_352_ += i_342_ * 6;
	int i_363_ = i_352_;
	i_352_ += i_348_;
	int i_364_ = i_352_;
	i_352_ += i_349_;
	int i_365_ = i_352_;
	i_352_ += i_350_;
	anInt2367 = i;
	anInt2366 = i_341_;
	anInt2355 = i_342_;
	anIntArray2376 = new int[i];
	anIntArray2356 = new int[i];
	anIntArray2359 = new int[i];
	anIntArray2385 = new int[i_341_];
	anIntArray2363 = new int[i_341_];
	anIntArray2368 = new int[i_341_];
	anIntArray2370 = new int[i_342_];
	anIntArray2373 = new int[i_342_];
	anIntArray2387 = new int[i_342_];
	if (i_358_ >= 0)
	    anIntArray2357 = new int[i];
	if (i_357_ >= 0)
	    anIntArray2378 = new int[i_341_];
	if (i_355_ >= 0)
	    anIntArray2365 = new int[i_341_];
	else
	    anInt2384 = -i_355_ - 1;
	if (i_359_ >= 0)
	    anIntArray2379 = new int[i_341_];
	if (i_356_ >= 0)
	    anIntArray2375 = new int[i_341_];
	anIntArray2371 = new int[i_341_];
	class39_sub6.offset = i_353_;
	class39_sub6_337_.offset = i_363_;
	class39_sub6_338_.offset = i_364_;
	class39_sub6_339_.offset = i_365_;
	class39_sub6_340_.offset = i_358_;
	int i_366_ = 0;
	int i_367_ = 0;
	int i_368_ = 0;
	for (int i_369_ = 0; i_369_ < i; i_369_++) {
	    int i_370_ = class39_sub6.getUbyte();
	    int i_371_ = 0;
	    if ((i_370_ & 0x1) != 0)
		i_371_ = class39_sub6_337_.getSmartA();
	    int i_372_ = 0;
	    if ((i_370_ & 0x2) != 0)
		i_372_ = class39_sub6_338_.getSmartA();
	    int i_373_ = 0;
	    if ((i_370_ & 0x4) != 0)
		i_373_ = class39_sub6_339_.getSmartA();
	    anIntArray2376[i_369_] = i_366_ + i_371_;
	    anIntArray2356[i_369_] = i_367_ + i_372_;
	    anIntArray2359[i_369_] = i_368_ + i_373_;
	    i_366_ = anIntArray2376[i_369_];
	    i_367_ = anIntArray2356[i_369_];
	    i_368_ = anIntArray2359[i_369_];
	    if (anIntArray2357 != null)
		anIntArray2357[i_369_]
		    = class39_sub6_340_.getUbyte();
	}
	class39_sub6.offset = i_361_;
	class39_sub6_337_.offset = i_357_;
	class39_sub6_338_.offset = i_355_;
	class39_sub6_339_.offset = i_359_;
	class39_sub6_340_.offset = i_356_;
	for (int i_374_ = 0; i_374_ < i_341_; i_374_++) {
	    anIntArray2371[i_374_] = class39_sub6.getUword();
	    if (anIntArray2378 != null)
		anIntArray2378[i_374_]
		    = class39_sub6_337_.getUbyte();
	    if (anIntArray2365 != null)
		anIntArray2365[i_374_]
		    = class39_sub6_338_.getUbyte();
	    if (anIntArray2379 != null)
		anIntArray2379[i_374_]
		    = class39_sub6_339_.getUbyte();
	    if (anIntArray2375 != null)
		anIntArray2375[i_374_]
		    = class39_sub6_340_.getUbyte();
	}
	class39_sub6.offset = i_360_;
	class39_sub6_337_.offset = i_354_;
	int i_375_ = 0;
	int i_376_ = 0;
	int i_377_ = 0;
	int i_378_ = 0;
	for (int i_379_ = 0; i_379_ < i_341_; i_379_++) {
	    int i_380_ = class39_sub6_337_.getUbyte();
	    if (i_380_ == 1) {
		i_375_ = class39_sub6.getSmartA() + i_378_;
		i_378_ = i_375_;
		i_376_ = class39_sub6.getSmartA() + i_378_;
		i_378_ = i_376_;
		i_377_ = class39_sub6.getSmartA() + i_378_;
		i_378_ = i_377_;
		anIntArray2385[i_379_] = i_375_;
		anIntArray2363[i_379_] = i_376_;
		anIntArray2368[i_379_] = i_377_;
	    }
	    if (i_380_ == 2) {
		i_376_ = i_377_;
		i_377_ = class39_sub6.getSmartA() + i_378_;
		i_378_ = i_377_;
		anIntArray2385[i_379_] = i_375_;
		anIntArray2363[i_379_] = i_376_;
		anIntArray2368[i_379_] = i_377_;
	    }
	    if (i_380_ == 3) {
		i_375_ = i_377_;
		i_377_ = class39_sub6.getSmartA() + i_378_;
		i_378_ = i_377_;
		anIntArray2385[i_379_] = i_375_;
		anIntArray2363[i_379_] = i_376_;
		anIntArray2368[i_379_] = i_377_;
	    }
	    if (i_380_ == 4) {
		int i_381_ = i_375_;
		i_375_ = i_376_;
		i_376_ = i_381_;
		i_377_ = class39_sub6.getSmartA() + i_378_;
		i_378_ = i_377_;
		anIntArray2385[i_379_] = i_375_;
		anIntArray2363[i_379_] = i_376_;
		anIntArray2368[i_379_] = i_377_;
	    }
	}
	class39_sub6.offset = i_362_;
	for (int i_382_ = 0; i_382_ < i_342_; i_382_++) {
	    anIntArray2370[i_382_] = class39_sub6.getUword();
	    anIntArray2373[i_382_] = class39_sub6.getUword();
	    anIntArray2387[i_382_] = class39_sub6.getUword();
	}
    }
    
    public void method568() {
	if (anInt2381 != 2) {
	    anInt2381 = 2;
	    anInt2386 = 0;
	    for (int i = 0; i < anInt2367; i++) {
		int i_383_ = anIntArray2376[i];
		int i_384_ = anIntArray2356[i];
		int i_385_ = anIntArray2359[i];
		int i_386_
		    = i_383_ * i_383_ + i_385_ * i_385_ + i_384_ * i_384_;
		if (i_386_ > anInt2386)
		    anInt2386 = i_386_;
	    }
	    anInt2386 = (int) (Math.sqrt((double) anInt2386) + 0.99);
	    anInt2358 = anInt2386;
	    anInt2372 = anInt2386 + anInt2386;
	}
    }
    
    public Model() {
	anInt2367 = 0;
    }
    
    public Model
	(Model[] class39_sub5_sub4_sub6s, int i) {
	anInt2367 = 0;
	boolean bool = false;
	boolean bool_387_ = false;
	boolean bool_388_ = false;
	boolean bool_389_ = false;
	anInt2367 = 0;
	anInt2366 = 0;
	anInt2355 = 0;
	anInt2384 = -1;
	for (int i_390_ = 0; i_390_ < i; i_390_++) {
	    Model class39_sub5_sub4_sub6_391_
		= class39_sub5_sub4_sub6s[i_390_];
	    if (class39_sub5_sub4_sub6_391_ != null) {
		anInt2367 += class39_sub5_sub4_sub6_391_.anInt2367;
		anInt2366 += class39_sub5_sub4_sub6_391_.anInt2366;
		anInt2355 += class39_sub5_sub4_sub6_391_.anInt2355;
		bool = (bool
			| class39_sub5_sub4_sub6_391_.anIntArray2378 != null);
		if (class39_sub5_sub4_sub6_391_.anIntArray2365 != null)
		    bool_387_ = true;
		else {
		    if (anInt2384 == -1)
			anInt2384 = class39_sub5_sub4_sub6_391_.anInt2384;
		    if (anInt2384 != class39_sub5_sub4_sub6_391_.anInt2384)
			bool_387_ = true;
		}
		bool_388_
		    = (bool_388_
		       | class39_sub5_sub4_sub6_391_.anIntArray2379 != null);
		bool_389_
		    = (bool_389_
		       | class39_sub5_sub4_sub6_391_.anIntArray2375 != null);
	    }
	}
	anIntArray2376 = new int[anInt2367];
	anIntArray2356 = new int[anInt2367];
	anIntArray2359 = new int[anInt2367];
	anIntArray2357 = new int[anInt2367];
	anIntArray2385 = new int[anInt2366];
	anIntArray2363 = new int[anInt2366];
	anIntArray2368 = new int[anInt2366];
	anIntArray2370 = new int[anInt2355];
	anIntArray2373 = new int[anInt2355];
	anIntArray2387 = new int[anInt2355];
	if (bool)
	    anIntArray2378 = new int[anInt2366];
	if (bool_387_)
	    anIntArray2365 = new int[anInt2366];
	if (bool_388_)
	    anIntArray2379 = new int[anInt2366];
	if (bool_389_)
	    anIntArray2375 = new int[anInt2366];
	anIntArray2371 = new int[anInt2366];
	anInt2367 = 0;
	anInt2366 = 0;
	anInt2355 = 0;
	int i_392_ = 0;
	for (int i_393_ = 0; i_393_ < i; i_393_++) {
	    Model class39_sub5_sub4_sub6_394_
		= class39_sub5_sub4_sub6s[i_393_];
	    if (class39_sub5_sub4_sub6_394_ != null) {
		for (int i_395_ = 0;
		     i_395_ < class39_sub5_sub4_sub6_394_.anInt2366;
		     i_395_++) {
		    if (bool) {
			if (class39_sub5_sub4_sub6_394_.anIntArray2378 == null)
			    anIntArray2378[anInt2366] = 0;
			else {
			    int i_396_ = (class39_sub5_sub4_sub6_394_
					  .anIntArray2378[i_395_]);
			    if ((i_396_ & 0x2) == 2)
				i_396_ += i_392_ << 2;
			    anIntArray2378[anInt2366] = i_396_;
			}
		    }
		    if (bool_387_) {
			if (class39_sub5_sub4_sub6_394_.anIntArray2365 == null)
			    anIntArray2365[anInt2366]
				= class39_sub5_sub4_sub6_394_.anInt2384;
			else
			    anIntArray2365[anInt2366]
				= (class39_sub5_sub4_sub6_394_.anIntArray2365
				   [i_395_]);
		    }
		    if (bool_388_) {
			if (class39_sub5_sub4_sub6_394_.anIntArray2379 == null)
			    anIntArray2379[anInt2366] = 0;
			else
			    anIntArray2379[anInt2366]
				= (class39_sub5_sub4_sub6_394_.anIntArray2379
				   [i_395_]);
		    }
		    if (bool_389_
			&& class39_sub5_sub4_sub6_394_.anIntArray2375 != null)
			anIntArray2375[anInt2366]
			    = (class39_sub5_sub4_sub6_394_.anIntArray2375
			       [i_395_]);
		    anIntArray2371[anInt2366]
			= class39_sub5_sub4_sub6_394_.anIntArray2371[i_395_];
		    anIntArray2385[anInt2366]
			= method556(class39_sub5_sub4_sub6_394_,
				    (class39_sub5_sub4_sub6_394_.anIntArray2385
				     [i_395_]));
		    anIntArray2363[anInt2366]
			= method556(class39_sub5_sub4_sub6_394_,
				    (class39_sub5_sub4_sub6_394_.anIntArray2363
				     [i_395_]));
		    anIntArray2368[anInt2366]
			= method556(class39_sub5_sub4_sub6_394_,
				    (class39_sub5_sub4_sub6_394_.anIntArray2368
				     [i_395_]));
		    anInt2366++;
		}
		for (int i_397_ = 0;
		     i_397_ < class39_sub5_sub4_sub6_394_.anInt2355;
		     i_397_++) {
		    anIntArray2370[anInt2355]
			= method556(class39_sub5_sub4_sub6_394_,
				    (class39_sub5_sub4_sub6_394_.anIntArray2370
				     [i_397_]));
		    anIntArray2373[anInt2355]
			= method556(class39_sub5_sub4_sub6_394_,
				    (class39_sub5_sub4_sub6_394_.anIntArray2373
				     [i_397_]));
		    anIntArray2387[anInt2355]
			= method556(class39_sub5_sub4_sub6_394_,
				    (class39_sub5_sub4_sub6_394_.anIntArray2387
				     [i_397_]));
		    anInt2355++;
		}
		i_392_ += class39_sub5_sub4_sub6_394_.anInt2355;
	    }
	}
    }
    
    public Model
	(Model[] class39_sub5_sub4_sub6s, int i,
	 boolean bool) {
	anInt2367 = 0;
	boolean bool_398_ = false;
	boolean bool_399_ = false;
	boolean bool_400_ = false;
	boolean bool_401_ = false;
	anInt2367 = 0;
	anInt2366 = 0;
	anInt2355 = 0;
	anInt2384 = -1;
	for (int i_402_ = 0; i_402_ < i; i_402_++) {
	    Model class39_sub5_sub4_sub6_403_
		= class39_sub5_sub4_sub6s[i_402_];
	    if (class39_sub5_sub4_sub6_403_ != null) {
		anInt2367 += class39_sub5_sub4_sub6_403_.anInt2367;
		anInt2366 += class39_sub5_sub4_sub6_403_.anInt2366;
		anInt2355 += class39_sub5_sub4_sub6_403_.anInt2355;
		bool_398_
		    = (bool_398_
		       | class39_sub5_sub4_sub6_403_.anIntArray2378 != null);
		if (class39_sub5_sub4_sub6_403_.anIntArray2365 != null)
		    bool_399_ = true;
		else {
		    if (anInt2384 == -1)
			anInt2384 = class39_sub5_sub4_sub6_403_.anInt2384;
		    if (anInt2384 != class39_sub5_sub4_sub6_403_.anInt2384)
			bool_399_ = true;
		}
		bool_400_
		    = (bool_400_
		       | class39_sub5_sub4_sub6_403_.anIntArray2379 != null);
		bool_401_
		    = (bool_401_
		       | class39_sub5_sub4_sub6_403_.anIntArray2371 != null);
	    }
	}
	anIntArray2376 = new int[anInt2367];
	anIntArray2356 = new int[anInt2367];
	anIntArray2359 = new int[anInt2367];
	anIntArray2385 = new int[anInt2366];
	anIntArray2363 = new int[anInt2366];
	anIntArray2368 = new int[anInt2366];
	anIntArray2354 = new int[anInt2366];
	anIntArray2369 = new int[anInt2366];
	anIntArray2364 = new int[anInt2366];
	anIntArray2370 = new int[anInt2355];
	anIntArray2373 = new int[anInt2355];
	anIntArray2387 = new int[anInt2355];
	if (bool_398_)
	    anIntArray2378 = new int[anInt2366];
	if (bool_399_)
	    anIntArray2365 = new int[anInt2366];
	if (bool_400_)
	    anIntArray2379 = new int[anInt2366];
	if (bool_401_)
	    anIntArray2371 = new int[anInt2366];
	anInt2367 = 0;
	anInt2366 = 0;
	anInt2355 = 0;
	int i_404_ = 0;
	for (int i_405_ = 0; i_405_ < i; i_405_++) {
	    Model class39_sub5_sub4_sub6_406_
		= class39_sub5_sub4_sub6s[i_405_];
	    if (class39_sub5_sub4_sub6_406_ != null) {
		int i_407_ = anInt2367;
		for (int i_408_ = 0;
		     i_408_ < class39_sub5_sub4_sub6_406_.anInt2367;
		     i_408_++) {
		    anIntArray2376[anInt2367]
			= class39_sub5_sub4_sub6_406_.anIntArray2376[i_408_];
		    anIntArray2356[anInt2367]
			= class39_sub5_sub4_sub6_406_.anIntArray2356[i_408_];
		    anIntArray2359[anInt2367]
			= class39_sub5_sub4_sub6_406_.anIntArray2359[i_408_];
		    anInt2367++;
		}
		for (int i_409_ = 0;
		     i_409_ < class39_sub5_sub4_sub6_406_.anInt2366;
		     i_409_++) {
		    anIntArray2385[anInt2366]
			= (class39_sub5_sub4_sub6_406_.anIntArray2385[i_409_]
			   + i_407_);
		    anIntArray2363[anInt2366]
			= (class39_sub5_sub4_sub6_406_.anIntArray2363[i_409_]
			   + i_407_);
		    anIntArray2368[anInt2366]
			= (class39_sub5_sub4_sub6_406_.anIntArray2368[i_409_]
			   + i_407_);
		    anIntArray2354[anInt2366]
			= class39_sub5_sub4_sub6_406_.anIntArray2354[i_409_];
		    anIntArray2369[anInt2366]
			= class39_sub5_sub4_sub6_406_.anIntArray2369[i_409_];
		    anIntArray2364[anInt2366]
			= class39_sub5_sub4_sub6_406_.anIntArray2364[i_409_];
		    if (bool_398_) {
			if (class39_sub5_sub4_sub6_406_.anIntArray2378 == null)
			    anIntArray2378[anInt2366] = 0;
			else {
			    int i_410_ = (class39_sub5_sub4_sub6_406_
					  .anIntArray2378[i_409_]);
			    if ((i_410_ & 0x2) == 2)
				i_410_ += i_404_ << 2;
			    anIntArray2378[anInt2366] = i_410_;
			}
		    }
		    if (bool_399_) {
			if (class39_sub5_sub4_sub6_406_.anIntArray2365 == null)
			    anIntArray2365[anInt2366]
				= class39_sub5_sub4_sub6_406_.anInt2384;
			else
			    anIntArray2365[anInt2366]
				= (class39_sub5_sub4_sub6_406_.anIntArray2365
				   [i_409_]);
		    }
		    if (bool_400_) {
			if (class39_sub5_sub4_sub6_406_.anIntArray2379 == null)
			    anIntArray2379[anInt2366] = 0;
			else
			    anIntArray2379[anInt2366]
				= (class39_sub5_sub4_sub6_406_.anIntArray2379
				   [i_409_]);
		    }
		    if (bool_401_
			&& class39_sub5_sub4_sub6_406_.anIntArray2371 != null)
			anIntArray2371[anInt2366]
			    = (class39_sub5_sub4_sub6_406_.anIntArray2371
			       [i_409_]);
		    anInt2366++;
		}
		for (int i_411_ = 0;
		     i_411_ < class39_sub5_sub4_sub6_406_.anInt2355;
		     i_411_++) {
		    anIntArray2370[anInt2355]
			= (class39_sub5_sub4_sub6_406_.anIntArray2370[i_411_]
			   + i_407_);
		    anIntArray2373[anInt2355]
			= (class39_sub5_sub4_sub6_406_.anIntArray2373[i_411_]
			   + i_407_);
		    anIntArray2387[anInt2355]
			= (class39_sub5_sub4_sub6_406_.anIntArray2387[i_411_]
			   + i_407_);
		    anInt2355++;
		}
		i_404_ += class39_sub5_sub4_sub6_406_.anInt2355;
	    }
	}
    }
    
    public Model
	(Model class39_sub5_sub4_sub6_412_, boolean bool,
	 boolean bool_413_, boolean bool_414_) {
	anInt2367 = 0;
	anInt2367 = class39_sub5_sub4_sub6_412_.anInt2367;
	anInt2366 = class39_sub5_sub4_sub6_412_.anInt2366;
	anInt2355 = class39_sub5_sub4_sub6_412_.anInt2355;
	if (bool) {
	    anIntArray2376 = class39_sub5_sub4_sub6_412_.anIntArray2376;
	    anIntArray2356 = class39_sub5_sub4_sub6_412_.anIntArray2356;
	    anIntArray2359 = class39_sub5_sub4_sub6_412_.anIntArray2359;
	} else {
	    anIntArray2376 = new int[anInt2367];
	    anIntArray2356 = new int[anInt2367];
	    anIntArray2359 = new int[anInt2367];
	    for (int i = 0; i < anInt2367; i++) {
		anIntArray2376[i]
		    = class39_sub5_sub4_sub6_412_.anIntArray2376[i];
		anIntArray2356[i]
		    = class39_sub5_sub4_sub6_412_.anIntArray2356[i];
		anIntArray2359[i]
		    = class39_sub5_sub4_sub6_412_.anIntArray2359[i];
	    }
	}
	if (bool_413_)
	    anIntArray2371 = class39_sub5_sub4_sub6_412_.anIntArray2371;
	else {
	    anIntArray2371 = new int[anInt2366];
	    for (int i = 0; i < anInt2366; i++)
		anIntArray2371[i]
		    = class39_sub5_sub4_sub6_412_.anIntArray2371[i];
	}
	if (bool_414_)
	    anIntArray2379 = class39_sub5_sub4_sub6_412_.anIntArray2379;
	else {
	    anIntArray2379 = new int[anInt2366];
	    if (class39_sub5_sub4_sub6_412_.anIntArray2379 == null) {
		for (int i = 0; i < anInt2366; i++)
		    anIntArray2379[i] = 0;
	    } else {
		for (int i = 0; i < anInt2366; i++)
		    anIntArray2379[i]
			= class39_sub5_sub4_sub6_412_.anIntArray2379[i];
	    }
	}
	anIntArray2357 = class39_sub5_sub4_sub6_412_.anIntArray2357;
	anIntArray2375 = class39_sub5_sub4_sub6_412_.anIntArray2375;
	anIntArray2378 = class39_sub5_sub4_sub6_412_.anIntArray2378;
	anIntArray2385 = class39_sub5_sub4_sub6_412_.anIntArray2385;
	anIntArray2363 = class39_sub5_sub4_sub6_412_.anIntArray2363;
	anIntArray2368 = class39_sub5_sub4_sub6_412_.anIntArray2368;
	anIntArray2365 = class39_sub5_sub4_sub6_412_.anIntArray2365;
	anInt2384 = class39_sub5_sub4_sub6_412_.anInt2384;
	anIntArray2370 = class39_sub5_sub4_sub6_412_.anIntArray2370;
	anIntArray2373 = class39_sub5_sub4_sub6_412_.anIntArray2373;
	anIntArray2387 = class39_sub5_sub4_sub6_412_.anIntArray2387;
	aBoolean2374 = class39_sub5_sub4_sub6_412_.aBoolean2374;
    }
    
    public Model
	(Model class39_sub5_sub4_sub6_415_, boolean bool,
	 boolean bool_416_) {
	anInt2367 = 0;
	anInt2367 = class39_sub5_sub4_sub6_415_.anInt2367;
	anInt2366 = class39_sub5_sub4_sub6_415_.anInt2366;
	anInt2355 = class39_sub5_sub4_sub6_415_.anInt2355;
	if (bool) {
	    anIntArray2356 = new int[anInt2367];
	    for (int i = 0; i < anInt2367; i++)
		anIntArray2356[i]
		    = class39_sub5_sub4_sub6_415_.anIntArray2356[i];
	} else
	    anIntArray2356 = class39_sub5_sub4_sub6_415_.anIntArray2356;
	if (bool_416_) {
	    anIntArray2354 = new int[anInt2366];
	    anIntArray2369 = new int[anInt2366];
	    anIntArray2364 = new int[anInt2366];
	    for (int i = 0; i < anInt2366; i++) {
		anIntArray2354[i]
		    = class39_sub5_sub4_sub6_415_.anIntArray2354[i];
		anIntArray2369[i]
		    = class39_sub5_sub4_sub6_415_.anIntArray2369[i];
		anIntArray2364[i]
		    = class39_sub5_sub4_sub6_415_.anIntArray2364[i];
	    }
	    anIntArray2378 = new int[anInt2366];
	    if (class39_sub5_sub4_sub6_415_.anIntArray2378 == null) {
		for (int i = 0; i < anInt2366; i++)
		    anIntArray2378[i] = 0;
	    } else {
		for (int i = 0; i < anInt2366; i++)
		    anIntArray2378[i]
			= class39_sub5_sub4_sub6_415_.anIntArray2378[i];
	    }
	    aClass48Array2361 = new Class48[anInt2367];
	    for (int i = 0; i < anInt2367; i++) {
		Class48 class48 = aClass48Array2361[i] = new Class48();
		Class48 class48_417_
		    = class39_sub5_sub4_sub6_415_.aClass48Array2361[i];
		class48.anInt901 = class48_417_.anInt901;
		class48.anInt903 = class48_417_.anInt903;
		class48.anInt900 = class48_417_.anInt900;
		class48.anInt908 = class48_417_.anInt908;
	    }
	    aClass48Array2380 = class39_sub5_sub4_sub6_415_.aClass48Array2380;
	} else {
	    anIntArray2354 = class39_sub5_sub4_sub6_415_.anIntArray2354;
	    anIntArray2369 = class39_sub5_sub4_sub6_415_.anIntArray2369;
	    anIntArray2364 = class39_sub5_sub4_sub6_415_.anIntArray2364;
	    anIntArray2378 = class39_sub5_sub4_sub6_415_.anIntArray2378;
	}
	anIntArray2376 = class39_sub5_sub4_sub6_415_.anIntArray2376;
	anIntArray2359 = class39_sub5_sub4_sub6_415_.anIntArray2359;
	anIntArray2371 = class39_sub5_sub4_sub6_415_.anIntArray2371;
	anIntArray2379 = class39_sub5_sub4_sub6_415_.anIntArray2379;
	anIntArray2365 = class39_sub5_sub4_sub6_415_.anIntArray2365;
	anInt2384 = class39_sub5_sub4_sub6_415_.anInt2384;
	anIntArray2385 = class39_sub5_sub4_sub6_415_.anIntArray2385;
	anIntArray2363 = class39_sub5_sub4_sub6_415_.anIntArray2363;
	anIntArray2368 = class39_sub5_sub4_sub6_415_.anIntArray2368;
	anIntArray2370 = class39_sub5_sub4_sub6_415_.anIntArray2370;
	anIntArray2373 = class39_sub5_sub4_sub6_415_.anIntArray2373;
	anIntArray2387 = class39_sub5_sub4_sub6_415_.anIntArray2387;
	aBoolean2374 = class39_sub5_sub4_sub6_415_.aBoolean2374;
	anInt2377 = class39_sub5_sub4_sub6_415_.anInt2377;
    }
    
    static {
	aClass39_Sub5_Sub4_Sub6_2389 = new Model();
	anIntArray2391 = new int[12];
	anIntArray2394 = Class39_Sub5_Sub10_Sub2.sineTable;
	anIntArray2393 = new int[10];
	aBooleanArray2404 = new boolean[4096];
	anIntArrayArray2410 = new int[12][2000];
	anIntArray2400 = Class39_Sub5_Sub10_Sub2.anIntArray2455;
	anInt2407 = 0;
	anIntArray2405 = new int[4096];
	anIntArray2403 = new int[4096];
	anIntArray2409 = new int[1600];
	anIntArray2408 = new int[1000];
	anIntArray2399 = new int[2000];
	anIntArray2411 = new int[10];
	aBooleanArray2412 = new boolean[4096];
	anIntArray2401 = Class39_Sub5_Sub10_Sub2.anIntArray2452;
	anIntArray2402 = new int[128];
	anInt2397 = 0;
	anIntArray2395 = new int[4096];
	aBoolean2398 = false;
	anIntArray2406 = new int[1];
	anIntArray2392 = new int[4096];
	anIntArray2419 = new int[10];
	anIntArray2418 = Class39_Sub5_Sub10_Sub2.cosineTable;
	anIntArrayArray2415 = new int[1600][512];
	anInt2413 = 0;
	anIntArray2414 = new int[4096];
	anIntArray2421 = new int[4096];
	anIntArray2422 = new int[2000];
	anIntArray2417 = new int[12];
	int i = 0;
	int i_418_ = 248;
	while (i < 9)
	    anIntArray2402[i++] = 255;
	while (i < 16) {
	    anIntArray2402[i++] = i_418_;
	    i_418_ -= 8;
	}
	while (i < 32) {
	    anIntArray2402[i++] = i_418_;
	    i_418_ -= 4;
	}
	while (i < 64) {
	    anIntArray2402[i++] = i_418_;
	    i_418_ -= 2;
	}
	while (i < 128)
	    anIntArray2402[i++] = i_418_--;
    }
}

package unpackaged;

import jagex.utils.Deque;

/* Class38 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Class38
{
    public Class39_Sub14[][][] aClass39_Sub14ArrayArrayArray665;
    public static int[] anIntArray666;
    public static int anInt667;
    public static int[] anIntArray668 = { -45, 45, 45, -45 };
    public static int anInt669;
    public static Deque aClass49_670;
    public static int anInt671;
    public static int anInt672;
    public static int anInt673;
    public static int anInt674;
    public static int anInt675 = 0;
    public int anInt676;
    public static int anInt677;
    public int anInt678 = 0;
    public static int anInt679;
    public static int[] anIntArray680;
    public static int anInt681;
    public static int anInt682;
    public static int anInt683;
    public static boolean aBoolean684;
    public static int anInt685;
    public static int anInt686;
    public static int anInt687;
    public static Class26[][] aClass26ArrayArray688;
    public int anInt689;
    public Class10[] aClass10Array690;
    public static Class26[] aClass26Array691;
    public static int anInt692;
    public static int anInt693;
    public int[][][] anIntArrayArrayArray694;
    public static int anInt695;
    public int anInt696;
    public static int anInt697;
    public static Class10[] aClass10Array698;
    public int[][][] anIntArrayArrayArray699;
    public static int anInt700;
    public static int[] anIntArray701;
    public static int anInt702;
    public static boolean aBoolean703;
    public static int anInt704;
    public static int[] anIntArray705;
    public int anInt706;
    public int anInt707;
    public int[][] anIntArrayArray708;
    public static int[] anIntArray709;
    public static int anInt710;
    public static int anInt711;
    public static final int DR = 52;
    /** Ground tiles are drawn while every corner is at least this far in front of the camera (2006: 50); lower so close zoom has no holes. */
    static final int GROUND_NEAR = 16;
    public static boolean[][][][] aBooleanArrayArrayArrayArray712;
    public static int[] anIntArray713;
    public static int anInt714;
    public static int[] anIntArray715;
    public int[] anIntArray716;
    public static int[] anIntArray717;
    public static int[] anIntArray718;
    public static int anInt719;
    public static int[] anIntArray720;
    public int[] anIntArray721;
    public static int anInt722;
    public int[][] anIntArrayArray723;
    public static int[] anIntArray724;
    public static boolean[][] aBooleanArrayArray725;
    public static int anInt726;
    
    public void method355(int i, int i_0_) {
	aBoolean703 = true;
	anInt675 = i;
	anInt697 = i_0_;
	anInt682 = -1;
	anInt677 = -1;
    }
    
    public Class36 method356(int i, int i_1_, int i_2_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_1_][i_2_];
	if (class39_sub14 == null)
	    return null;
	return class39_sub14.aClass36_1528;
    }
    
    public boolean method357(int i, int i_3_, int i_4_, int i_5_, int i_6_,
			     int i_7_) {
	if (i_3_ == i_4_ && i_5_ == i_6_) {
	    if (!method376(i, i_3_, i_5_))
		return false;
	    int i_8_ = i_3_ << 7;
	    int i_9_ = i_5_ << 7;
	    if (method371(i_8_ + 1,
			  anIntArrayArrayArray699[i][i_3_][i_5_] - i_7_,
			  i_9_ + 1)
		&& method371(i_8_ + 128 - 1,
			     anIntArrayArrayArray699[i][i_3_ + 1][i_5_] - i_7_,
			     i_9_ + 1)
		&& method371(i_8_ + 128 - 1,
			     (anIntArrayArrayArray699[i][i_3_ + 1][i_5_ + 1]
			      - i_7_),
			     i_9_ + 128 - 1)
		&& method371(i_8_ + 1,
			     anIntArrayArrayArray699[i][i_3_][i_5_ + 1] - i_7_,
			     i_9_ + 128 - 1))
		return true;
	    return false;
	}
	for (int i_10_ = i_3_; i_10_ <= i_4_; i_10_++) {
	    for (int i_11_ = i_5_; i_11_ <= i_6_; i_11_++) {
		if (anIntArrayArrayArray694[i][i_10_][i_11_] == -anInt695)
		    return false;
	    }
	}
	int i_12_ = (i_3_ << 7) + 1;
	int i_13_ = (i_5_ << 7) + 2;
	int i_14_ = anIntArrayArrayArray699[i][i_3_][i_5_] - i_7_;
	if (!method371(i_12_, i_14_, i_13_))
	    return false;
	int i_15_ = (i_4_ << 7) - 1;
	if (!method371(i_15_, i_14_, i_13_))
	    return false;
	int i_16_ = (i_6_ << 7) - 1;
	if (!method371(i_12_, i_14_, i_16_))
	    return false;
	if (!method371(i_15_, i_14_, i_16_))
	    return false;
	return true;
    }
    
    public Class44 method358(int i, int i_17_, int i_18_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_17_][i_18_];
	if (class39_sub14 == null)
	    return null;
	return class39_sub14.aClass44_1530;
    }
    
    public int method359(int i, int i_19_, int i_20_, int i_21_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_19_][i_20_];
	if (class39_sub14 == null)
	    return -1;
	if (class39_sub14.aClass36_1528 != null
	    && class39_sub14.aClass36_1528.anInt637 == i_21_)
	    return class39_sub14.aClass36_1528.anInt644 & 0xff;
	if (class39_sub14.aClass44_1530 != null
	    && class39_sub14.aClass44_1530.anInt842 == i_21_)
	    return class39_sub14.aClass44_1530.anInt844 & 0xff;
	if (class39_sub14.aClass50_1539 != null
	    && class39_sub14.aClass50_1539.anInt935 == i_21_)
	    return class39_sub14.aClass50_1539.anInt932 & 0xff;
	for (int i_22_ = 0; i_22_ < class39_sub14.anInt1538; i_22_++) {
	    if (class39_sub14.aClass10Array1524[i_22_].anInt157 == i_21_)
		return class39_sub14.aClass10Array1524[i_22_].anInt171 & 0xff;
	}
	return -1;
    }
    
    public void method360(Class10 class10) {
	for (int i = class10.anInt151; i <= class10.anInt174; i++) {
	    for (int i_23_ = class10.anInt158; i_23_ <= class10.anInt162;
		 i_23_++) {
		Class39_Sub14 class39_sub14 = (aClass39_Sub14ArrayArrayArray665
					       [class10.anInt159][i][i_23_]);
		if (class39_sub14 != null) {
		    for (int i_24_ = 0; i_24_ < class39_sub14.anInt1538;
			 i_24_++) {
			if (class39_sub14.aClass10Array1524[i_24_]
			    == class10) {
			    class39_sub14.anInt1538--;
			    for (int i_25_ = i_24_;
				 i_25_ < class39_sub14.anInt1538; i_25_++) {
				class39_sub14.aClass10Array1524[i_25_]
				    = (class39_sub14.aClass10Array1524
				       [i_25_ + 1]);
				class39_sub14.anIntArray1532[i_25_]
				    = class39_sub14.anIntArray1532[i_25_ + 1];
			    }
			    class39_sub14.aClass10Array1524[(class39_sub14
							     .anInt1538)]
				= null;
			    break;
			}
		    }
		    class39_sub14.anInt1535 = 0;
		    for (int i_26_ = 0; i_26_ < class39_sub14.anInt1538;
			 i_26_++)
			class39_sub14.anInt1535
			    |= class39_sub14.anIntArray1532[i_26_];
		}
	    }
	}
    }
    
    public void method361(Model class39_sub5_sub4_sub6, int i,
			  int i_27_, int i_28_, int i_29_, int i_30_) {
	boolean bool = true;
	int i_31_ = i_27_;
	int i_32_ = i_27_ + i_29_;
	int i_33_ = i_28_ - 1;
	int i_34_ = i_28_ + i_30_;
	for (int i_35_ = i; i_35_ <= i + 1; i_35_++) {
	    if (i_35_ != anInt689) {
		for (int i_36_ = i_31_; i_36_ <= i_32_; i_36_++) {
		    if (i_36_ >= 0 && i_36_ < anInt706) {
			for (int i_37_ = i_33_; i_37_ <= i_34_; i_37_++) {
			    if (i_37_ >= 0 && i_37_ < anInt696
				&& (!bool || i_36_ >= i_32_ || i_37_ >= i_34_
				    || i_37_ < i_28_ && i_36_ != i_27_)) {
				Class39_Sub14 class39_sub14
				    = (aClass39_Sub14ArrayArrayArray665[i_35_]
				       [i_36_][i_37_]);
				if (class39_sub14 != null) {
				    int i_38_
					= (((anIntArrayArrayArray699[i_35_]
					     [i_36_][i_37_])
					    + (anIntArrayArrayArray699[i_35_]
					       [i_36_ + 1][i_37_])
					    + (anIntArrayArrayArray699[i_35_]
					       [i_36_][i_37_ + 1])
					    + (anIntArrayArrayArray699[i_35_]
					       [i_36_ + 1][i_37_ + 1])) / 4
					   - ((anIntArrayArrayArray699[i]
					       [i_27_][i_28_])
					      + (anIntArrayArrayArray699[i]
						 [i_27_ + 1][i_28_])
					      + (anIntArrayArrayArray699[i]
						 [i_27_][i_28_ + 1])
					      + (anIntArrayArrayArray699[i]
						 [i_27_ + 1][i_28_ + 1])) / 4);
				    Class36 class36
					= class39_sub14.aClass36_1528;
				    if (class36 != null) {
					if (class36.aClass39_Sub5_Sub4_646
					    instanceof Model) {
					    Model class39_sub5_sub4_sub6_39_
						= ((Model)
						   (class36
						    .aClass39_Sub5_Sub4_646));
					    if ((class39_sub5_sub4_sub6_39_
						 .aClass48Array2361)
						!= null)
						method372
						    (class39_sub5_sub4_sub6,
						     class39_sub5_sub4_sub6_39_,
						     ((i_36_ - i_27_) * 128
						      + (1 - i_29_) * 64),
						     i_38_,
						     ((i_37_ - i_28_) * 128
						      + (1 - i_30_) * 64),
						     bool);
					}
					if (class36.aClass39_Sub5_Sub4_641
					    instanceof Model) {
					    Model class39_sub5_sub4_sub6_40_
						= ((Model)
						   (class36
						    .aClass39_Sub5_Sub4_641));
					    if ((class39_sub5_sub4_sub6_40_
						 .aClass48Array2361)
						!= null)
						method372
						    (class39_sub5_sub4_sub6,
						     class39_sub5_sub4_sub6_40_,
						     ((i_36_ - i_27_) * 128
						      + (1 - i_29_) * 64),
						     i_38_,
						     ((i_37_ - i_28_) * 128
						      + (1 - i_30_) * 64),
						     bool);
					}
				    }
				    for (int i_41_ = 0;
					 i_41_ < class39_sub14.anInt1538;
					 i_41_++) {
					Class10 class10
					    = (class39_sub14.aClass10Array1524
					       [i_41_]);
					if (class10 != null
					    && (class10.aClass39_Sub5_Sub4_154
						instanceof Model)) {
					    Model class39_sub5_sub4_sub6_42_
						= ((Model)
						   (class10
						    .aClass39_Sub5_Sub4_154));
					    if ((class39_sub5_sub4_sub6_42_
						 .aClass48Array2361)
						!= null) {
						int i_43_
						    = (class10.anInt174
						       - class10.anInt151 + 1);
						int i_44_
						    = (class10.anInt162
						       - class10.anInt158 + 1);
						method372
						    (class39_sub5_sub4_sub6,
						     class39_sub5_sub4_sub6_42_,
						     ((class10.anInt151
						       - i_27_) * 128
						      + (i_43_ - i_29_) * 64),
						     i_38_,
						     ((class10.anInt158
						       - i_28_) * 128
						      + (i_44_ - i_30_) * 64),
						     bool);
					    }
					}
				    }
				}
			    }
			}
		    }
		}
		i_31_--;
		bool = false;
	    }
	}
    }
    
    public void method362(int i, int i_45_, int i_46_, int i_47_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_45_][i_46_];
	if (class39_sub14 != null)
	    aClass39_Sub14ArrayArrayArray665[i][i_45_][i_46_].anInt1545
		= i_47_;
    }
    
    public int method363(int i, int i_48_, int i_49_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_48_][i_49_];
	if (class39_sub14 == null || class39_sub14.aClass44_1530 == null)
	    return 0;
	return class39_sub14.aClass44_1530.anInt842;
    }
    
    public void method364(int i, int i_50_, int i_51_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_50_][i_51_];
	if (class39_sub14 != null)
	    class39_sub14.aClass50_1539 = null;
    }
    
    public void method365(int i, int i_52_, int i_53_, int i_54_,
			  Class39_Sub5_Sub4 class39_sub5_sub4, int i_55_,
			  int i_56_, int i_57_, int i_58_, int i_59_,
			  int i_60_) {
	if (class39_sub5_sub4 != null) {
	    Class44 class44 = new Class44();
	    class44.anInt842 = i_59_;
	    class44.anInt844 = i_60_;
	    class44.anInt834 = i_52_ * 128 + 64 + i_57_;
	    class44.anInt845 = i_53_ * 128 + 64 + i_58_;
	    class44.anInt832 = i_54_;
	    class44.aClass39_Sub5_Sub4_846 = class39_sub5_sub4;
	    class44.anInt831 = i_55_;
	    class44.anInt840 = i_56_;
	    for (int i_61_ = i; i_61_ >= 0; i_61_--) {
		if (aClass39_Sub14ArrayArrayArray665[i_61_][i_52_][i_53_]
		    == null)
		    aClass39_Sub14ArrayArrayArray665[i_61_][i_52_][i_53_]
			= new Class39_Sub14(i_61_, i_52_, i_53_);
	    }
	    aClass39_Sub14ArrayArrayArray665[i][i_52_][i_53_].aClass44_1530
		= class44;
	}
    }
    
    public void method366(int i, int i_62_, int i_63_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_62_][i_63_];
	if (class39_sub14 != null)
	    class39_sub14.aClass44_1530 = null;
    }
    
    public void method367(int i, int i_64_, int i_65_, int i_66_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_64_][i_65_];
	if (class39_sub14 != null) {
	    Class44 class44 = class39_sub14.aClass44_1530;
	    if (class44 != null) {
		int i_67_ = i_64_ * 128 + 64;
		int i_68_ = i_65_ * 128 + 64;
		class44.anInt834
		    = i_67_ + (class44.anInt834 - i_67_) * i_66_ / 16;
		class44.anInt845
		    = i_68_ + (class44.anInt845 - i_68_) * i_66_ / 16;
	    }
	}
    }
    
    public void method368(int i, int i_69_, int i_70_, int i_71_, int i_72_,
			  int i_73_, int i_74_, int i_75_, int i_76_,
			  int i_77_, int i_78_, int i_79_, int i_80_,
			  int i_81_, int i_82_, int i_83_, int i_84_,
			  int i_85_, int i_86_, int i_87_) {
	if (i_71_ == 0) {
	    Class32 class32
		= new Class32(i_78_, i_79_, i_80_, i_81_, -1, i_86_, false);
	    for (int i_88_ = i; i_88_ >= 0; i_88_--) {
		if (aClass39_Sub14ArrayArrayArray665[i_88_][i_69_][i_70_]
		    == null)
		    aClass39_Sub14ArrayArrayArray665[i_88_][i_69_][i_70_]
			= new Class39_Sub14(i_88_, i_69_, i_70_);
	    }
	    aClass39_Sub14ArrayArrayArray665[i][i_69_][i_70_].aClass32_1542
		= class32;
	} else if (i_71_ == 1) {
	    Class32 class32
		= new Class32(i_82_, i_83_, i_84_, i_85_, i_73_, i_87_,
			      (i_74_ == i_75_ && i_74_ == i_76_
			       && i_74_ == i_77_));
	    for (int i_89_ = i; i_89_ >= 0; i_89_--) {
		if (aClass39_Sub14ArrayArrayArray665[i_89_][i_69_][i_70_]
		    == null)
		    aClass39_Sub14ArrayArrayArray665[i_89_][i_69_][i_70_]
			= new Class39_Sub14(i_89_, i_69_, i_70_);
	    }
	    aClass39_Sub14ArrayArrayArray665[i][i_69_][i_70_].aClass32_1542
		= class32;
	} else {
	    Class17 class17
		= new Class17(i_71_, i_72_, i_73_, i_69_, i_70_, i_74_, i_75_,
			      i_76_, i_77_, i_78_, i_79_, i_80_, i_81_, i_82_,
			      i_83_, i_84_, i_85_, i_86_, i_87_);
	    for (int i_90_ = i; i_90_ >= 0; i_90_--) {
		if (aClass39_Sub14ArrayArrayArray665[i_90_][i_69_][i_70_]
		    == null)
		    aClass39_Sub14ArrayArrayArray665[i_90_][i_69_][i_70_]
			= new Class39_Sub14(i_90_, i_69_, i_70_);
	    }
	    aClass39_Sub14ArrayArrayArray665[i][i_69_][i_70_].aClass17_1531
		= class17;
	}
    }
    
    public static boolean method369(int i, int i_91_, int i_92_) {
	int i_93_ = i_92_ * anInt671 + i * anInt693 >> 16;
	int i_94_ = i_92_ * anInt693 - i * anInt671 >> 16;
	int i_95_ = i_91_ * anInt685 + i_94_ * anInt687 >> 16;
	int i_96_ = i_91_ * anInt687 - i_94_ * anInt685 >> 16;
	if (i_95_ < GROUND_NEAR || i_95_ > 9000)
	    return false;
	int i_97_ = anInt726 + (i_93_ * Resizable.zoom) / i_95_;
	int i_98_ = anInt722 + (i_96_ * Resizable.zoom) / i_95_;
	if (i_97_ < anInt714 || i_97_ > anInt719 || i_98_ < anInt711
	    || i_98_ > anInt710)
	    return false;
	return true;
    }
    
    public void method370(int i, int i_99_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[0][i][i_99_];
	for (int i_100_ = 0; i_100_ < 3; i_100_++) {
	    Class39_Sub14 class39_sub14_101_
		= (aClass39_Sub14ArrayArrayArray665[i_100_][i][i_99_]
		   = aClass39_Sub14ArrayArrayArray665[i_100_ + 1][i][i_99_]);
	    if (class39_sub14_101_ != null) {
		class39_sub14_101_.anInt1546--;
		for (int i_102_ = 0; i_102_ < class39_sub14_101_.anInt1538;
		     i_102_++) {
		    Class10 class10
			= class39_sub14_101_.aClass10Array1524[i_102_];
		    if ((class10.anInt157 >> 29 & 0x3) == 2
			&& class10.anInt151 == i && class10.anInt158 == i_99_)
			class10.anInt159--;
		}
	    }
	}
	if (aClass39_Sub14ArrayArrayArray665[0][i][i_99_] == null)
	    aClass39_Sub14ArrayArrayArray665[0][i][i_99_]
		= new Class39_Sub14(0, i, i_99_);
	aClass39_Sub14ArrayArrayArray665[0][i][i_99_].aClass39_Sub14_1540
	    = class39_sub14;
	aClass39_Sub14ArrayArrayArray665[3][i][i_99_] = null;
    }
    
    public boolean method371(int i, int i_103_, int i_104_) {
	for (int i_105_ = 0; i_105_ < anInt673; i_105_++) {
	    Class26 class26 = aClass26Array691[i_105_];
	    if (class26.anInt475 == 1) {
		int i_106_ = class26.anInt495 - i;
		if (i_106_ > 0) {
		    int i_107_
			= class26.anInt481 + (class26.anInt488 * i_106_ >> 8);
		    int i_108_
			= class26.anInt501 + (class26.anInt479 * i_106_ >> 8);
		    int i_109_
			= class26.anInt494 + (class26.anInt498 * i_106_ >> 8);
		    int i_110_
			= class26.anInt477 + (class26.anInt476 * i_106_ >> 8);
		    if (i_104_ >= i_107_ && i_104_ <= i_108_
			&& i_103_ >= i_109_ && i_103_ <= i_110_)
			return true;
		}
	    } else if (class26.anInt475 == 2) {
		int i_111_ = i - class26.anInt495;
		if (i_111_ > 0) {
		    int i_112_
			= class26.anInt481 + (class26.anInt488 * i_111_ >> 8);
		    int i_113_
			= class26.anInt501 + (class26.anInt479 * i_111_ >> 8);
		    int i_114_
			= class26.anInt494 + (class26.anInt498 * i_111_ >> 8);
		    int i_115_
			= class26.anInt477 + (class26.anInt476 * i_111_ >> 8);
		    if (i_104_ >= i_112_ && i_104_ <= i_113_
			&& i_103_ >= i_114_ && i_103_ <= i_115_)
			return true;
		}
	    } else if (class26.anInt475 == 3) {
		int i_116_ = class26.anInt481 - i_104_;
		if (i_116_ > 0) {
		    int i_117_
			= class26.anInt495 + (class26.anInt497 * i_116_ >> 8);
		    int i_118_
			= class26.anInt499 + (class26.anInt492 * i_116_ >> 8);
		    int i_119_
			= class26.anInt494 + (class26.anInt498 * i_116_ >> 8);
		    int i_120_
			= class26.anInt477 + (class26.anInt476 * i_116_ >> 8);
		    if (i >= i_117_ && i <= i_118_ && i_103_ >= i_119_
			&& i_103_ <= i_120_)
			return true;
		}
	    } else if (class26.anInt475 == 4) {
		int i_121_ = i_104_ - class26.anInt481;
		if (i_121_ > 0) {
		    int i_122_
			= class26.anInt495 + (class26.anInt497 * i_121_ >> 8);
		    int i_123_
			= class26.anInt499 + (class26.anInt492 * i_121_ >> 8);
		    int i_124_
			= class26.anInt494 + (class26.anInt498 * i_121_ >> 8);
		    int i_125_
			= class26.anInt477 + (class26.anInt476 * i_121_ >> 8);
		    if (i >= i_122_ && i <= i_123_ && i_103_ >= i_124_
			&& i_103_ <= i_125_)
			return true;
		}
	    } else if (class26.anInt475 == 5) {
		int i_126_ = i_103_ - class26.anInt494;
		if (i_126_ > 0) {
		    int i_127_
			= class26.anInt495 + (class26.anInt497 * i_126_ >> 8);
		    int i_128_
			= class26.anInt499 + (class26.anInt492 * i_126_ >> 8);
		    int i_129_
			= class26.anInt481 + (class26.anInt488 * i_126_ >> 8);
		    int i_130_
			= class26.anInt501 + (class26.anInt479 * i_126_ >> 8);
		    if (i >= i_127_ && i <= i_128_ && i_104_ >= i_129_
			&& i_104_ <= i_130_)
			return true;
		}
	    }
	}
	return false;
    }
    
    public void method372(Model class39_sub5_sub4_sub6,
			  Model class39_sub5_sub4_sub6_131_,
			  int i, int i_132_, int i_133_, boolean bool) {
	class39_sub5_sub4_sub6_131_.method564();
	anInt707++;
	int i_134_ = 0;
	int[] is = class39_sub5_sub4_sub6_131_.anIntArray2376;
	int i_135_ = class39_sub5_sub4_sub6_131_.anInt2367;
	for (int i_136_ = 0; i_136_ < class39_sub5_sub4_sub6.anInt2367;
	     i_136_++) {
	    Class48 class48 = class39_sub5_sub4_sub6.aClass48Array2361[i_136_];
	    Class48 class48_137_
		= class39_sub5_sub4_sub6.aClass48Array2380[i_136_];
	    if (class48_137_.anInt908 != 0) {
		int i_138_
		    = class39_sub5_sub4_sub6.anIntArray2356[i_136_] - i_132_;
		if (i_138_ <= class39_sub5_sub4_sub6_131_.anInt2362) {
		    int i_139_
			= class39_sub5_sub4_sub6.anIntArray2376[i_136_] - i;
		    if (i_139_ >= class39_sub5_sub4_sub6_131_.anInt2386
			&& i_139_ <= class39_sub5_sub4_sub6_131_.anInt2372) {
			int i_140_
			    = (class39_sub5_sub4_sub6.anIntArray2359[i_136_]
			       - i_133_);
			if (i_140_ >= class39_sub5_sub4_sub6_131_.anInt2383
			    && (i_140_
				<= class39_sub5_sub4_sub6_131_.anInt2358)) {
			    for (int i_141_ = 0; i_141_ < i_135_; i_141_++) {
				Class48 class48_142_
				    = (class39_sub5_sub4_sub6_131_
				       .aClass48Array2361[i_141_]);
				Class48 class48_143_
				    = (class39_sub5_sub4_sub6_131_
				       .aClass48Array2380[i_141_]);
				if (i_139_ == is[i_141_]
				    && i_140_ == (class39_sub5_sub4_sub6_131_
						  .anIntArray2359[i_141_])
				    && i_138_ == (class39_sub5_sub4_sub6_131_
						  .anIntArray2356[i_141_])
				    && class48_143_.anInt908 != 0) {
				    class48.anInt901 += class48_143_.anInt901;
				    class48.anInt903 += class48_143_.anInt903;
				    class48.anInt900 += class48_143_.anInt900;
				    class48.anInt908 += class48_143_.anInt908;
				    class48_142_.anInt901
					+= class48_137_.anInt901;
				    class48_142_.anInt903
					+= class48_137_.anInt903;
				    class48_142_.anInt900
					+= class48_137_.anInt900;
				    class48_142_.anInt908
					+= class48_137_.anInt908;
				    i_134_++;
				    anIntArray721[i_136_] = anInt707;
				    anIntArray716[i_141_] = anInt707;
				}
			    }
			}
		    }
		}
	    }
	}
	if (i_134_ >= 3 && bool) {
	    for (int i_144_ = 0; i_144_ < class39_sub5_sub4_sub6.anInt2366;
		 i_144_++) {
		if (anIntArray721[(class39_sub5_sub4_sub6.anIntArray2385
				   [i_144_])] == anInt707
		    && anIntArray721[(class39_sub5_sub4_sub6.anIntArray2363
				      [i_144_])] == anInt707
		    && anIntArray721[(class39_sub5_sub4_sub6.anIntArray2368
				      [i_144_])] == anInt707)
		    class39_sub5_sub4_sub6.anIntArray2378[i_144_] = -1;
	    }
	    for (int i_145_ = 0;
		 i_145_ < class39_sub5_sub4_sub6_131_.anInt2366; i_145_++) {
		if (anIntArray716[(class39_sub5_sub4_sub6_131_.anIntArray2385
				   [i_145_])] == anInt707
		    && anIntArray716[(class39_sub5_sub4_sub6_131_
				      .anIntArray2363[i_145_])] == anInt707
		    && anIntArray716[(class39_sub5_sub4_sub6_131_
				      .anIntArray2368[i_145_])] == anInt707)
		    class39_sub5_sub4_sub6_131_.anIntArray2378[i_145_] = -1;
	    }
	}
    }
    
    public void method373(int i, int i_146_, int i_147_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_146_][i_147_];
	if (class39_sub14 != null)
	    class39_sub14.aClass67_1541 = null;
    }
    
    public Class10 method374(int i, int i_148_, int i_149_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_148_][i_149_];
	if (class39_sub14 == null)
	    return null;
	for (int i_150_ = 0; i_150_ < class39_sub14.anInt1538; i_150_++) {
	    Class10 class10 = class39_sub14.aClass10Array1524[i_150_];
	    if ((class10.anInt157 >> 29 & 0x3) == 2
		&& class10.anInt151 == i_148_ && class10.anInt158 == i_149_)
		return class10;
	}
	return null;
    }
    
    public boolean method375(int i, int i_151_, int i_152_, int i_153_,
			     int i_154_, Class39_Sub5_Sub4 class39_sub5_sub4,
			     int i_155_, int i_156_, boolean bool) {
	if (class39_sub5_sub4 == null)
	    return true;
	int i_157_ = i_151_ - i_154_;
	int i_158_ = i_152_ - i_154_;
	int i_159_ = i_151_ + i_154_;
	int i_160_ = i_152_ + i_154_;
	if (bool) {
	    if (i_155_ > 640 && i_155_ < 1408)
		i_160_ += 128;
	    if (i_155_ > 1152 && i_155_ < 1920)
		i_159_ += 128;
	    if (i_155_ > 1664 || i_155_ < 384)
		i_158_ -= 128;
	    if (i_155_ > 128 && i_155_ < 896)
		i_157_ -= 128;
	}
	i_157_ /= 128;
	i_158_ /= 128;
	i_159_ /= 128;
	i_160_ /= 128;
	return method386(i, i_157_, i_158_, i_159_ - i_157_ + 1,
			 i_160_ - i_158_ + 1, i_151_, i_152_, i_153_,
			 class39_sub5_sub4, i_155_, true, i_156_, 0);
    }
    
    public boolean method376(int i, int i_161_, int i_162_) {
	int i_163_ = anIntArrayArrayArray694[i][i_161_][i_162_];
	if (i_163_ == -anInt695)
	    return false;
	if (i_163_ == anInt695)
	    return true;
	int i_164_ = i_161_ << 7;
	int i_165_ = i_162_ << 7;
	if (method371(i_164_ + 1, anIntArrayArrayArray699[i][i_161_][i_162_],
		      i_165_ + 1)
	    && method371(i_164_ + 128 - 1,
			 anIntArrayArrayArray699[i][i_161_ + 1][i_162_],
			 i_165_ + 1)
	    && method371(i_164_ + 128 - 1,
			 anIntArrayArrayArray699[i][i_161_ + 1][i_162_ + 1],
			 i_165_ + 128 - 1)
	    && method371(i_164_ + 1,
			 anIntArrayArrayArray699[i][i_161_][i_162_ + 1],
			 i_165_ + 128 - 1)) {
	    anIntArrayArrayArray694[i][i_161_][i_162_] = anInt695;
	    return true;
	}
	anIntArrayArrayArray694[i][i_161_][i_162_] = -anInt695;
	return false;
    }
    
    public boolean method377(int i, int i_166_, int i_167_, int i_168_) {
	if (!method376(i, i_166_, i_167_))
	    return false;
	int i_169_ = i_166_ << 7;
	int i_170_ = i_167_ << 7;
	int i_171_ = anIntArrayArrayArray699[i][i_166_][i_167_] - 1;
	int i_172_ = i_171_ - 120;
	int i_173_ = i_171_ - 230;
	int i_174_ = i_171_ - 238;
	if (i_168_ < 16) {
	    if (i_168_ == 1) {
		if (i_169_ > anInt686) {
		    if (!method371(i_169_, i_171_, i_170_))
			return false;
		    if (!method371(i_169_, i_171_, i_170_ + 128))
			return false;
		}
		if (i > 0) {
		    if (!method371(i_169_, i_172_, i_170_))
			return false;
		    if (!method371(i_169_, i_172_, i_170_ + 128))
			return false;
		}
		if (!method371(i_169_, i_173_, i_170_))
		    return false;
		if (!method371(i_169_, i_173_, i_170_ + 128))
		    return false;
		return true;
	    }
	    if (i_168_ == 2) {
		if (i_170_ < anInt702) {
		    if (!method371(i_169_, i_171_, i_170_ + 128))
			return false;
		    if (!method371(i_169_ + 128, i_171_, i_170_ + 128))
			return false;
		}
		if (i > 0) {
		    if (!method371(i_169_, i_172_, i_170_ + 128))
			return false;
		    if (!method371(i_169_ + 128, i_172_, i_170_ + 128))
			return false;
		}
		if (!method371(i_169_, i_173_, i_170_ + 128))
		    return false;
		if (!method371(i_169_ + 128, i_173_, i_170_ + 128))
		    return false;
		return true;
	    }
	    if (i_168_ == 4) {
		if (i_169_ < anInt686) {
		    if (!method371(i_169_ + 128, i_171_, i_170_))
			return false;
		    if (!method371(i_169_ + 128, i_171_, i_170_ + 128))
			return false;
		}
		if (i > 0) {
		    if (!method371(i_169_ + 128, i_172_, i_170_))
			return false;
		    if (!method371(i_169_ + 128, i_172_, i_170_ + 128))
			return false;
		}
		if (!method371(i_169_ + 128, i_173_, i_170_))
		    return false;
		if (!method371(i_169_ + 128, i_173_, i_170_ + 128))
		    return false;
		return true;
	    }
	    if (i_168_ == 8) {
		if (i_170_ > anInt702) {
		    if (!method371(i_169_, i_171_, i_170_))
			return false;
		    if (!method371(i_169_ + 128, i_171_, i_170_))
			return false;
		}
		if (i > 0) {
		    if (!method371(i_169_, i_172_, i_170_))
			return false;
		    if (!method371(i_169_ + 128, i_172_, i_170_))
			return false;
		}
		if (!method371(i_169_, i_173_, i_170_))
		    return false;
		if (!method371(i_169_ + 128, i_173_, i_170_))
		    return false;
		return true;
	    }
	}
	if (!method371(i_169_ + 64, i_174_, i_170_ + 64))
	    return false;
	if (i_168_ == 16) {
	    if (!method371(i_169_, i_173_, i_170_ + 128))
		return false;
	    return true;
	}
	if (i_168_ == 32) {
	    if (!method371(i_169_ + 128, i_173_, i_170_ + 128))
		return false;
	    return true;
	}
	if (i_168_ == 64) {
	    if (!method371(i_169_ + 128, i_173_, i_170_))
		return false;
	    return true;
	}
	if (i_168_ == 128) {
	    if (!method371(i_169_, i_173_, i_170_))
		return false;
	    return true;
	}
	return true;
    }
    
    public void method378() {
	for (int i = 0; i < anInt689; i++) {
	    for (int i_175_ = 0; i_175_ < anInt706; i_175_++) {
		for (int i_176_ = 0; i_176_ < anInt696; i_176_++)
		    aClass39_Sub14ArrayArrayArray665[i][i_175_][i_176_] = null;
	    }
	}
	for (int i = 0; i < anInt683; i++) {
	    for (int i_177_ = 0; i_177_ < anIntArray701[i]; i_177_++)
		aClass26ArrayArray688[i][i_177_] = null;
	    anIntArray701[i] = 0;
	}
	for (int i = 0; i < anInt676; i++)
	    aClass10Array690[i] = null;
	anInt676 = 0;
	for (int i = 0; i < aClass10Array698.length; i++)
	    aClass10Array698[i] = null;
    }
    
    public int method379(int i, int i_178_, int i_179_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_178_][i_179_];
	if (class39_sub14 == null || class39_sub14.aClass36_1528 == null)
	    return 0;
	return class39_sub14.aClass36_1528.anInt637;
    }
    
    public void method380(int i, int i_180_, int i_181_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_180_][i_181_];
	if (class39_sub14 != null)
	    class39_sub14.aClass36_1528 = null;
    }
    
    public void method381(int i, int i_182_, int i_183_, int i_184_,
			  Class39_Sub5_Sub4 class39_sub5_sub4, int i_185_,
			  int i_186_) {
	if (class39_sub5_sub4 != null) {
	    Class50 class50 = new Class50();
	    class50.aClass39_Sub5_Sub4_933 = class39_sub5_sub4;
	    class50.anInt937 = i_182_ * 128 + 64;
	    class50.anInt930 = i_183_ * 128 + 64;
	    class50.anInt929 = i_184_;
	    class50.anInt935 = i_185_;
	    class50.anInt932 = i_186_;
	    if (aClass39_Sub14ArrayArrayArray665[i][i_182_][i_183_] == null)
		aClass39_Sub14ArrayArrayArray665[i][i_182_][i_183_]
		    = new Class39_Sub14(i, i_182_, i_183_);
	    aClass39_Sub14ArrayArrayArray665[i][i_182_][i_183_].aClass50_1539
		= class50;
	}
    }
    
    public void method382(int i, int i_187_, int i_188_, int i_189_,
			  int i_190_, int i_191_) {
	if (i < 0)
	    i = 0;
	else if (i >= anInt706 * 128)
	    i = anInt706 * 128 - 1;
	if (i_188_ < 0)
	    i_188_ = 0;
	else if (i_188_ >= anInt696 * 128)
	    i_188_ = anInt696 * 128 - 1;
	anInt695++;
	anInt685 = Model.anIntArray2394[i_189_];
	anInt687 = Model.anIntArray2418[i_189_];
	anInt671 = Model.anIntArray2394[i_190_];
	anInt693 = Model.anIntArray2418[i_190_];
	aBooleanArrayArray725 = (aBooleanArrayArrayArrayArray712
				 [(i_189_ - 128) / 32][i_190_ / 64]);
	anInt686 = i;
	anInt669 = i_187_;
	anInt702 = i_188_;
	anInt704 = i / 128;
	anInt674 = i_188_ / 128;
	anInt679 = i_191_;
	anInt667 = anInt704 - DR;
	if (anInt667 < 0)
	    anInt667 = 0;
	anInt692 = anInt674 - DR;
	if (anInt692 < 0)
	    anInt692 = 0;
	anInt681 = anInt704 + DR;
	if (anInt681 > anInt706)
	    anInt681 = anInt706;
	anInt672 = anInt674 + DR;
	if (anInt672 > anInt696)
	    anInt672 = anInt696;
	method399();
	anInt700 = 0;
	for (int i_192_ = anInt678; i_192_ < anInt689; i_192_++) {
	    Class39_Sub14[][] class39_sub14s
		= aClass39_Sub14ArrayArrayArray665[i_192_];
	    for (int i_193_ = anInt667; i_193_ < anInt681; i_193_++) {
		for (int i_194_ = anInt692; i_194_ < anInt672; i_194_++) {
		    Class39_Sub14 class39_sub14
			= class39_sub14s[i_193_][i_194_];
		    if (class39_sub14 != null) {
			if ((class39_sub14.anInt1545 > i_191_ && !ModHook.keepAbove(i_193_, i_194_))
			    || (!(aBooleanArrayArray725[i_193_ - anInt704 + DR]
				  [i_194_ - anInt674 + DR])
				&& (anIntArrayArrayArray699[i_192_][i_193_]
				    [i_194_]) - i_187_ < 2000)) {
			    class39_sub14.aBoolean1525 = false;
			    class39_sub14.aBoolean1537 = false;
			    class39_sub14.anInt1536 = 0;
			} else {
			    class39_sub14.aBoolean1525 = true;
			    class39_sub14.aBoolean1537 = true;
			    if (class39_sub14.anInt1538 > 0)
				class39_sub14.aBoolean1529 = true;
			    else
				class39_sub14.aBoolean1529 = false;
			    anInt700++;
			}
		    }
		}
	    }
	}
	for (int i_195_ = anInt678; i_195_ < anInt689; i_195_++) {
	    Class39_Sub14[][] class39_sub14s
		= aClass39_Sub14ArrayArrayArray665[i_195_];
	    for (int i_196_ = -DR; i_196_ <= 0; i_196_++) {
		int i_197_ = anInt704 + i_196_;
		int i_198_ = anInt704 - i_196_;
		if (i_197_ >= anInt667 || i_198_ < anInt681) {
		    for (int i_199_ = -DR; i_199_ <= 0; i_199_++) {
			int i_200_ = anInt674 + i_199_;
			int i_201_ = anInt674 - i_199_;
			if (i_197_ >= anInt667) {
			    if (i_200_ >= anInt692) {
				Class39_Sub14 class39_sub14
				    = class39_sub14s[i_197_][i_200_];
				if (class39_sub14 != null
				    && class39_sub14.aBoolean1525)
				    method389(class39_sub14, true);
			    }
			    if (i_201_ < anInt672) {
				Class39_Sub14 class39_sub14
				    = class39_sub14s[i_197_][i_201_];
				if (class39_sub14 != null
				    && class39_sub14.aBoolean1525)
				    method389(class39_sub14, true);
			    }
			}
			if (i_198_ < anInt681) {
			    if (i_200_ >= anInt692) {
				Class39_Sub14 class39_sub14
				    = class39_sub14s[i_198_][i_200_];
				if (class39_sub14 != null
				    && class39_sub14.aBoolean1525)
				    method389(class39_sub14, true);
			    }
			    if (i_201_ < anInt672) {
				Class39_Sub14 class39_sub14
				    = class39_sub14s[i_198_][i_201_];
				if (class39_sub14 != null
				    && class39_sub14.aBoolean1525)
				    method389(class39_sub14, true);
			    }
			}
			if (anInt700 == 0) {
			    aBoolean703 = false;
			    return;
			}
		    }
		}
	    }
	}
	for (int i_202_ = anInt678; i_202_ < anInt689; i_202_++) {
	    Class39_Sub14[][] class39_sub14s
		= aClass39_Sub14ArrayArrayArray665[i_202_];
	    for (int i_203_ = -DR; i_203_ <= 0; i_203_++) {
		int i_204_ = anInt704 + i_203_;
		int i_205_ = anInt704 - i_203_;
		if (i_204_ >= anInt667 || i_205_ < anInt681) {
		    for (int i_206_ = -DR; i_206_ <= 0; i_206_++) {
			int i_207_ = anInt674 + i_206_;
			int i_208_ = anInt674 - i_206_;
			if (i_204_ >= anInt667) {
			    if (i_207_ >= anInt692) {
				Class39_Sub14 class39_sub14
				    = class39_sub14s[i_204_][i_207_];
				if (class39_sub14 != null
				    && class39_sub14.aBoolean1525)
				    method389(class39_sub14, false);
			    }
			    if (i_208_ < anInt672) {
				Class39_Sub14 class39_sub14
				    = class39_sub14s[i_204_][i_208_];
				if (class39_sub14 != null
				    && class39_sub14.aBoolean1525)
				    method389(class39_sub14, false);
			    }
			}
			if (i_205_ < anInt681) {
			    if (i_207_ >= anInt692) {
				Class39_Sub14 class39_sub14
				    = class39_sub14s[i_205_][i_207_];
				if (class39_sub14 != null
				    && class39_sub14.aBoolean1525)
				    method389(class39_sub14, false);
			    }
			    if (i_208_ < anInt672) {
				Class39_Sub14 class39_sub14
				    = class39_sub14s[i_205_][i_208_];
				if (class39_sub14 != null
				    && class39_sub14.aBoolean1525)
				    method389(class39_sub14, false);
			    }
			}
			if (anInt700 == 0) {
			    aBoolean703 = false;
			    return;
			}
		    }
		}
	    }
	}
	aBoolean703 = false;
    }
    
    public boolean method383(int i, int i_209_, int i_210_, int i_211_,
			     int i_212_, int i_213_,
			     Class39_Sub5_Sub4 class39_sub5_sub4, int i_214_,
			     int i_215_, int i_216_) {
	if (class39_sub5_sub4 == null)
	    return true;
	int i_217_ = i_209_ * 128 + i_212_ * 64;
	int i_218_ = i_210_ * 128 + i_213_ * 64;
	return method386(i, i_209_, i_210_, i_212_, i_213_, i_217_, i_218_,
			 i_211_, class39_sub5_sub4, i_214_, false, i_215_,
			 i_216_);
    }
    
    public int method384(int i, int i_219_, int i_220_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_219_][i_220_];
	if (class39_sub14 == null)
	    return 0;
	for (int i_221_ = 0; i_221_ < class39_sub14.anInt1538; i_221_++) {
	    Class10 class10 = class39_sub14.aClass10Array1524[i_221_];
	    if ((class10.anInt157 >> 29 & 0x3) == 2
		&& class10.anInt151 == i_219_ && class10.anInt158 == i_220_)
		return class10.anInt157;
	}
	return 0;
    }
    
    public static int method385(int i, int i_222_) {
	i_222_ = (127 - i_222_) * (i & 0x7f) >> 7;
	if (i_222_ < 2)
	    i_222_ = 2;
	else if (i_222_ > 126)
	    i_222_ = 126;
	return (i & 0xff80) + i_222_;
    }
    
    public boolean method386(int i, int i_223_, int i_224_, int i_225_,
			     int i_226_, int i_227_, int i_228_, int i_229_,
			     Class39_Sub5_Sub4 class39_sub5_sub4, int i_230_,
			     boolean bool, int i_231_, int i_232_) {
	for (int i_233_ = i_223_; i_233_ < i_223_ + i_225_; i_233_++) {
	    for (int i_234_ = i_224_; i_234_ < i_224_ + i_226_; i_234_++) {
		if (i_233_ < 0 || i_234_ < 0 || i_233_ >= anInt706
		    || i_234_ >= anInt696)
		    return false;
		Class39_Sub14 class39_sub14
		    = aClass39_Sub14ArrayArrayArray665[i][i_233_][i_234_];
		if (class39_sub14 != null && class39_sub14.anInt1538 >= 5)
		    return false;
	    }
	}
	Class10 class10 = new Class10();
	class10.anInt157 = i_231_;
	class10.anInt171 = i_232_;
	class10.anInt159 = i;
	class10.anInt156 = i_227_;
	class10.anInt152 = i_228_;
	class10.anInt153 = i_229_;
	class10.aClass39_Sub5_Sub4_154 = class39_sub5_sub4;
	class10.anInt169 = i_230_;
	class10.anInt151 = i_223_;
	class10.anInt158 = i_224_;
	class10.anInt174 = i_223_ + i_225_ - 1;
	class10.anInt162 = i_224_ + i_226_ - 1;
	for (int i_235_ = i_223_; i_235_ < i_223_ + i_225_; i_235_++) {
	    for (int i_236_ = i_224_; i_236_ < i_224_ + i_226_; i_236_++) {
		int i_237_ = 0;
		if (i_235_ > i_223_)
		    i_237_++;
		if (i_235_ < i_223_ + i_225_ - 1)
		    i_237_ += 4;
		if (i_236_ > i_224_)
		    i_237_ += 8;
		if (i_236_ < i_224_ + i_226_ - 1)
		    i_237_ += 2;
		for (int i_238_ = i; i_238_ >= 0; i_238_--) {
		    if ((aClass39_Sub14ArrayArrayArray665[i_238_][i_235_]
			 [i_236_])
			== null)
			aClass39_Sub14ArrayArrayArray665[i_238_][i_235_]
			    [i_236_]
			    = new Class39_Sub14(i_238_, i_235_, i_236_);
		}
		Class39_Sub14 class39_sub14
		    = aClass39_Sub14ArrayArrayArray665[i][i_235_][i_236_];
		class39_sub14.aClass10Array1524[class39_sub14.anInt1538]
		    = class10;
		class39_sub14.anIntArray1532[class39_sub14.anInt1538] = i_237_;
		class39_sub14.anInt1535 |= i_237_;
		class39_sub14.anInt1538++;
	    }
	}
	if (bool)
	    aClass10Array690[anInt676++] = class10;
	return true;
    }
    
    public void method387(int[] is, int i, int i_239_, int i_240_, int i_241_,
			  int i_242_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i_240_][i_241_][i_242_];
	if (class39_sub14 != null) {
	    Class32 class32 = class39_sub14.aClass32_1542;
	    if (class32 != null) {
		int i_243_ = class32.anInt584;
		if (i_243_ != 0) {
		    for (int i_244_ = 0; i_244_ < 4; i_244_++) {
			is[i] = i_243_;
			is[i + 1] = i_243_;
			is[i + 2] = i_243_;
			is[i + 3] = i_243_;
			i += i_239_;
		    }
		}
	    } else {
		Class17 class17 = class39_sub14.aClass17_1531;
		if (class17 != null) {
		    int i_245_ = class17.anInt320;
		    int i_246_ = class17.anInt316;
		    int i_247_ = class17.anInt328;
		    int i_248_ = class17.anInt324;
		    int[] is_249_ = anIntArrayArray708[i_245_];
		    int[] is_250_ = anIntArrayArray723[i_246_];
		    int i_251_ = 0;
		    if (i_247_ != 0) {
			for (int i_252_ = 0; i_252_ < 4; i_252_++) {
			    is[i] = (is_249_[is_250_[i_251_++]] == 0 ? i_247_
				     : i_248_);
			    is[i + 1] = (is_249_[is_250_[i_251_++]] == 0
					 ? i_247_ : i_248_);
			    is[i + 2] = (is_249_[is_250_[i_251_++]] == 0
					 ? i_247_ : i_248_);
			    is[i + 3] = (is_249_[is_250_[i_251_++]] == 0
					 ? i_247_ : i_248_);
			    i += i_239_;
			}
		    } else {
			for (int i_253_ = 0; i_253_ < 4; i_253_++) {
			    if (is_249_[is_250_[i_251_++]] != 0)
				is[i] = i_248_;
			    if (is_249_[is_250_[i_251_++]] != 0)
				is[i + 1] = i_248_;
			    if (is_249_[is_250_[i_251_++]] != 0)
				is[i + 2] = i_248_;
			    if (is_249_[is_250_[i_251_++]] != 0)
				is[i + 3] = i_248_;
			    i += i_239_;
			}
		    }
		}
	    }
	}
    }
    
    public static void method388(int i, int i_254_, int i_255_, int i_256_,
				 int i_257_, int i_258_, int i_259_,
				 int i_260_) {
	Class26 class26 = new Class26();
	class26.anInt480 = i_255_ / 128;
	class26.anInt500 = i_256_ / 128;
	class26.anInt487 = i_257_ / 128;
	class26.anInt502 = i_258_ / 128;
	class26.anInt493 = i_254_;
	class26.anInt495 = i_255_;
	class26.anInt499 = i_256_;
	class26.anInt481 = i_257_;
	class26.anInt501 = i_258_;
	class26.anInt494 = i_259_;
	class26.anInt477 = i_260_;
	aClass26ArrayArray688[i][anIntArray701[i]++] = class26;
    }
    
    public void method389(Class39_Sub14 class39_sub14, boolean bool) {
	aClass49_670.offerLast(class39_sub14);
	for (;;) {
	    Class39_Sub14 class39_sub14_261_
		= (Class39_Sub14) aClass49_670.pollFirst();
	    if (class39_sub14_261_ == null)
		break;
	    if (class39_sub14_261_.aBoolean1537) {
		int i = class39_sub14_261_.anInt1526;
		int i_262_ = class39_sub14_261_.anInt1544;
		int i_263_ = class39_sub14_261_.anInt1546;
		int i_264_ = class39_sub14_261_.anInt1534;
		Class39_Sub14[][] class39_sub14s
		    = aClass39_Sub14ArrayArrayArray665[i_263_];
		if (class39_sub14_261_.aBoolean1525) {
		    if (bool) {
			if (i_263_ > 0) {
			    Class39_Sub14 class39_sub14_265_
				= (aClass39_Sub14ArrayArrayArray665[i_263_ - 1]
				   [i][i_262_]);
			    if (class39_sub14_265_ != null
				&& class39_sub14_265_.aBoolean1537)
				continue;
			}
			if (i <= anInt704 && i > anInt667) {
			    Class39_Sub14 class39_sub14_266_
				= class39_sub14s[i - 1][i_262_];
			    if (class39_sub14_266_ != null
				&& class39_sub14_266_.aBoolean1537
				&& (class39_sub14_266_.aBoolean1525
				    || ((class39_sub14_261_.anInt1535 & 0x1)
					== 0)))
				continue;
			}
			if (i >= anInt704 && i < anInt681 - 1) {
			    Class39_Sub14 class39_sub14_267_
				= class39_sub14s[i + 1][i_262_];
			    if (class39_sub14_267_ != null
				&& class39_sub14_267_.aBoolean1537
				&& (class39_sub14_267_.aBoolean1525
				    || ((class39_sub14_261_.anInt1535 & 0x4)
					== 0)))
				continue;
			}
			if (i_262_ <= anInt674 && i_262_ > anInt692) {
			    Class39_Sub14 class39_sub14_268_
				= class39_sub14s[i][i_262_ - 1];
			    if (class39_sub14_268_ != null
				&& class39_sub14_268_.aBoolean1537
				&& (class39_sub14_268_.aBoolean1525
				    || ((class39_sub14_261_.anInt1535 & 0x8)
					== 0)))
				continue;
			}
			if (i_262_ >= anInt674 && i_262_ < anInt672 - 1) {
			    Class39_Sub14 class39_sub14_269_
				= class39_sub14s[i][i_262_ + 1];
			    if (class39_sub14_269_ != null
				&& class39_sub14_269_.aBoolean1537
				&& (class39_sub14_269_.aBoolean1525
				    || ((class39_sub14_261_.anInt1535 & 0x2)
					== 0)))
				continue;
			}
		    } else
			bool = true;
		    class39_sub14_261_.aBoolean1525 = false;
		    if (class39_sub14_261_.aClass39_Sub14_1540 != null) {
			Class39_Sub14 class39_sub14_270_
			    = class39_sub14_261_.aClass39_Sub14_1540;
			if (class39_sub14_270_.aClass32_1542 != null) {
			    if (!method376(0, i, i_262_))
				method394(class39_sub14_270_.aClass32_1542, 0,
					  anInt685, anInt687, anInt671,
					  anInt693, i, i_262_);
			} else if (class39_sub14_270_.aClass17_1531 != null
				   && !method376(0, i, i_262_))
			    method392(class39_sub14_270_.aClass17_1531,
				      anInt685, anInt687, anInt671, anInt693,
				      i, i_262_);
			Class36 class36 = class39_sub14_270_.aClass36_1528;
			if (class36 != null)
			    class36.aClass39_Sub5_Sub4_646.method488
				(0, anInt685, anInt687, anInt671, anInt693,
				 class36.anInt636 - anInt686,
				 class36.anInt651 - anInt669,
				 class36.anInt638 - anInt702,
				 class36.anInt637);
			for (int i_271_ = 0;
			     i_271_ < class39_sub14_270_.anInt1538; i_271_++) {
			    Class10 class10
				= class39_sub14_270_.aClass10Array1524[i_271_];
			    if (class10 != null)
				class10.aClass39_Sub5_Sub4_154.method488
				    (class10.anInt169, anInt685, anInt687,
				     anInt671, anInt693,
				     class10.anInt156 - anInt686,
				     class10.anInt153 - anInt669,
				     class10.anInt152 - anInt702,
				     class10.anInt157);
			}
		    }
		    boolean bool_272_ = false;
		    if (class39_sub14_261_.aClass32_1542 != null) {
			if (!method376(i_264_, i, i_262_)) {
			    bool_272_ = true;
			    method394(class39_sub14_261_.aClass32_1542, i_264_,
				      anInt685, anInt687, anInt671, anInt693,
				      i, i_262_);
			}
		    } else if (class39_sub14_261_.aClass17_1531 != null
			       && !method376(i_264_, i, i_262_)) {
			bool_272_ = true;
			method392(class39_sub14_261_.aClass17_1531, anInt685,
				  anInt687, anInt671, anInt693, i, i_262_);
		    }
		    int i_273_ = 0;
		    int i_274_ = 0;
		    Class36 class36 = class39_sub14_261_.aClass36_1528;
		    Class44 class44 = class39_sub14_261_.aClass44_1530;
		    if (class36 != null || class44 != null) {
			if (anInt704 == i)
			    i_273_++;
			else if (anInt704 < i)
			    i_273_ += 2;
			if (anInt674 == i_262_)
			    i_273_ += 3;
			else if (anInt674 > i_262_)
			    i_273_ += 6;
			i_274_ = anIntArray717[i_273_];
			class39_sub14_261_.anInt1523 = anIntArray709[i_273_];
		    }
		    if (class36 != null) {
			if ((class36.anInt632 & anIntArray713[i_273_]) != 0) {
			    if (class36.anInt632 == 16) {
				class39_sub14_261_.anInt1536 = 3;
				class39_sub14_261_.anInt1527
				    = anIntArray720[i_273_];
				class39_sub14_261_.anInt1533
				    = 3 - class39_sub14_261_.anInt1527;
			    } else if (class36.anInt632 == 32) {
				class39_sub14_261_.anInt1536 = 6;
				class39_sub14_261_.anInt1527
				    = anIntArray718[i_273_];
				class39_sub14_261_.anInt1533
				    = 6 - class39_sub14_261_.anInt1527;
			    } else if (class36.anInt632 == 64) {
				class39_sub14_261_.anInt1536 = 12;
				class39_sub14_261_.anInt1527
				    = anIntArray715[i_273_];
				class39_sub14_261_.anInt1533
				    = 12 - class39_sub14_261_.anInt1527;
			    } else {
				class39_sub14_261_.anInt1536 = 9;
				class39_sub14_261_.anInt1527
				    = anIntArray724[i_273_];
				class39_sub14_261_.anInt1533
				    = 9 - class39_sub14_261_.anInt1527;
			    }
			} else
			    class39_sub14_261_.anInt1536 = 0;
			if ((class36.anInt632 & i_274_) != 0
			    && !method377(i_264_, i, i_262_, class36.anInt632))
			    class36.aClass39_Sub5_Sub4_646.method488
				(0, anInt685, anInt687, anInt671, anInt693,
				 class36.anInt636 - anInt686,
				 class36.anInt651 - anInt669,
				 class36.anInt638 - anInt702,
				 class36.anInt637);
			if ((class36.anInt649 & i_274_) != 0
			    && !method377(i_264_, i, i_262_, class36.anInt649))
			    class36.aClass39_Sub5_Sub4_641.method488
				(0, anInt685, anInt687, anInt671, anInt693,
				 class36.anInt636 - anInt686,
				 class36.anInt651 - anInt669,
				 class36.anInt638 - anInt702,
				 class36.anInt637);
		    }
		    if (class44 != null
			&& !method396(i_264_, i, i_262_,
				      (class44.aClass39_Sub5_Sub4_846
				       .anInt1726))) {
			if ((class44.anInt831 & i_274_) != 0)
			    class44.aClass39_Sub5_Sub4_846.method488
				(class44.anInt840, anInt685, anInt687,
				 anInt671, anInt693,
				 class44.anInt834 - anInt686,
				 class44.anInt832 - anInt669,
				 class44.anInt845 - anInt702,
				 class44.anInt842);
			else if ((class44.anInt831 & 0x300) != 0) {
			    int i_275_ = class44.anInt834 - anInt686;
			    int i_276_ = class44.anInt832 - anInt669;
			    int i_277_ = class44.anInt845 - anInt702;
			    int i_278_ = class44.anInt840;
			    int i_279_;
			    if (i_278_ == 1 || i_278_ == 2)
				i_279_ = -i_275_;
			    else
				i_279_ = i_275_;
			    int i_280_;
			    if (i_278_ == 2 || i_278_ == 3)
				i_280_ = -i_277_;
			    else
				i_280_ = i_277_;
			    if ((class44.anInt831 & 0x100) != 0
				&& i_280_ < i_279_) {
				int i_281_ = i_275_ + anIntArray666[i_278_];
				int i_282_ = i_277_ + anIntArray705[i_278_];
				class44.aClass39_Sub5_Sub4_846.method488
				    (i_278_ * 512 + 256, anInt685, anInt687,
				     anInt671, anInt693, i_281_, i_276_,
				     i_282_, class44.anInt842);
			    }
			    if ((class44.anInt831 & 0x200) != 0
				&& i_280_ > i_279_) {
				int i_283_ = i_275_ + anIntArray668[i_278_];
				int i_284_ = i_277_ + anIntArray680[i_278_];
				class44.aClass39_Sub5_Sub4_846.method488
				    (i_278_ * 512 + 1280 & 0x7ff, anInt685,
				     anInt687, anInt671, anInt693, i_283_,
				     i_276_, i_284_, class44.anInt842);
			    }
			}
		    }
		    if (bool_272_) {
			Class50 class50 = class39_sub14_261_.aClass50_1539;
			if (class50 != null)
			    class50.aClass39_Sub5_Sub4_933.method488
				(0, anInt685, anInt687, anInt671, anInt693,
				 class50.anInt937 - anInt686,
				 class50.anInt929 - anInt669,
				 class50.anInt930 - anInt702,
				 class50.anInt935);
			Class67 class67 = class39_sub14_261_.aClass67_1541;
			if (class67 != null && class67.anInt1181 == 0) {
			    if (class67.aClass39_Sub5_Sub4_1182 != null)
				class67.aClass39_Sub5_Sub4_1182.method488
				    (0, anInt685, anInt687, anInt671, anInt693,
				     class67.anInt1168 - anInt686,
				     class67.anInt1171 - anInt669,
				     class67.anInt1180 - anInt702,
				     class67.anInt1166);
			    if (class67.aClass39_Sub5_Sub4_1183 != null)
				class67.aClass39_Sub5_Sub4_1183.method488
				    (0, anInt685, anInt687, anInt671, anInt693,
				     class67.anInt1168 - anInt686,
				     class67.anInt1171 - anInt669,
				     class67.anInt1180 - anInt702,
				     class67.anInt1166);
			    if (class67.aClass39_Sub5_Sub4_1177 != null)
				class67.aClass39_Sub5_Sub4_1177.method488
				    (0, anInt685, anInt687, anInt671, anInt693,
				     class67.anInt1168 - anInt686,
				     class67.anInt1171 - anInt669,
				     class67.anInt1180 - anInt702,
				     class67.anInt1166);
			}
		    }
		    int i_285_ = class39_sub14_261_.anInt1535;
		    if (i_285_ != 0) {
			if (i < anInt704 && (i_285_ & 0x4) != 0) {
			    Class39_Sub14 class39_sub14_286_
				= class39_sub14s[i + 1][i_262_];
			    if (class39_sub14_286_ != null
				&& class39_sub14_286_.aBoolean1537)
				aClass49_670.offerLast(class39_sub14_286_);
			}
			if (i_262_ < anInt674 && (i_285_ & 0x2) != 0) {
			    Class39_Sub14 class39_sub14_287_
				= class39_sub14s[i][i_262_ + 1];
			    if (class39_sub14_287_ != null
				&& class39_sub14_287_.aBoolean1537)
				aClass49_670.offerLast(class39_sub14_287_);
			}
			if (i > anInt704 && (i_285_ & 0x1) != 0) {
			    Class39_Sub14 class39_sub14_288_
				= class39_sub14s[i - 1][i_262_];
			    if (class39_sub14_288_ != null
				&& class39_sub14_288_.aBoolean1537)
				aClass49_670.offerLast(class39_sub14_288_);
			}
			if (i_262_ > anInt674 && (i_285_ & 0x8) != 0) {
			    Class39_Sub14 class39_sub14_289_
				= class39_sub14s[i][i_262_ - 1];
			    if (class39_sub14_289_ != null
				&& class39_sub14_289_.aBoolean1537)
				aClass49_670.offerLast(class39_sub14_289_);
			}
		    }
		}
		if (class39_sub14_261_.anInt1536 != 0) {
		    boolean bool_290_ = true;
		    for (int i_291_ = 0; i_291_ < class39_sub14_261_.anInt1538;
			 i_291_++) {
			if ((class39_sub14_261_.aClass10Array1524[i_291_]
			     .anInt175) != anInt695
			    && ((class39_sub14_261_.anIntArray1532[i_291_]
				 & class39_sub14_261_.anInt1536)
				== class39_sub14_261_.anInt1527)) {
			    bool_290_ = false;
			    break;
			}
		    }
		    if (bool_290_) {
			Class36 class36 = class39_sub14_261_.aClass36_1528;
			if (!method377(i_264_, i, i_262_, class36.anInt632))
			    class36.aClass39_Sub5_Sub4_646.method488
				(0, anInt685, anInt687, anInt671, anInt693,
				 class36.anInt636 - anInt686,
				 class36.anInt651 - anInt669,
				 class36.anInt638 - anInt702,
				 class36.anInt637);
			class39_sub14_261_.anInt1536 = 0;
		    }
		}
		if (class39_sub14_261_.aBoolean1529) {
		    try {
			int i_292_ = class39_sub14_261_.anInt1538;
			class39_sub14_261_.aBoolean1529 = false;
			int i_293_ = 0;
		    while_10_:
			for (int i_294_ = 0; i_294_ < i_292_; i_294_++) {
			    Class10 class10
				= class39_sub14_261_.aClass10Array1524[i_294_];
			    if (class10.anInt175 != anInt695) {
				for (int i_295_ = class10.anInt151;
				     i_295_ <= class10.anInt174; i_295_++) {
				    for (int i_296_ = class10.anInt158;
					 i_296_ <= class10.anInt162;
					 i_296_++) {
					Class39_Sub14 class39_sub14_297_
					    = class39_sub14s[i_295_][i_296_];
					if (class39_sub14_297_.aBoolean1525) {
					    class39_sub14_261_.aBoolean1529
						= true;
					    continue while_10_;
					}
					if (class39_sub14_297_.anInt1536
					    != 0) {
					    int i_298_ = 0;
					    if (i_295_ > class10.anInt151)
						i_298_++;
					    if (i_295_ < class10.anInt174)
						i_298_ += 4;
					    if (i_296_ > class10.anInt158)
						i_298_ += 8;
					    if (i_296_ < class10.anInt162)
						i_298_ += 2;
					    if ((i_298_ & (class39_sub14_297_
							   .anInt1536))
						== (class39_sub14_261_
						    .anInt1533)) {
						class39_sub14_261_.aBoolean1529
						    = true;
						continue while_10_;
					    }
					}
				    }
				}
				aClass10Array698[i_293_++] = class10;
				int i_299_ = anInt704 - class10.anInt151;
				int i_300_ = class10.anInt174 - anInt704;
				if (i_300_ > i_299_)
				    i_299_ = i_300_;
				int i_301_ = anInt674 - class10.anInt158;
				int i_302_ = class10.anInt162 - anInt674;
				if (i_302_ > i_301_)
				    class10.anInt161 = i_299_ + i_302_;
				else
				    class10.anInt161 = i_299_ + i_301_;
			    }
			}
			while (i_293_ > 0) {
			    int i_303_ = -50;
			    int i_304_ = -1;
			    for (int i_305_ = 0; i_305_ < i_293_; i_305_++) {
				Class10 class10 = aClass10Array698[i_305_];
				if (class10.anInt175 != anInt695) {
				    if (class10.anInt161 > i_303_) {
					i_303_ = class10.anInt161;
					i_304_ = i_305_;
				    } else if (class10.anInt161 == i_303_) {
					int i_306_
					    = class10.anInt156 - anInt686;
					int i_307_
					    = class10.anInt152 - anInt702;
					int i_308_ = ((aClass10Array698[i_304_]
						       .anInt156)
						      - anInt686);
					int i_309_ = ((aClass10Array698[i_304_]
						       .anInt152)
						      - anInt702);
					if (i_306_ * i_306_ + i_307_ * i_307_
					    > (i_308_ * i_308_
					       + i_309_ * i_309_))
					    i_304_ = i_305_;
				    }
				}
			    }
			    if (i_304_ == -1)
				break;
			    Class10 class10 = aClass10Array698[i_304_];
			    class10.anInt175 = anInt695;
			    if (!method357(i_264_, class10.anInt151,
					   class10.anInt174, class10.anInt158,
					   class10.anInt162,
					   (class10.aClass39_Sub5_Sub4_154
					    .anInt1726)))
				class10.aClass39_Sub5_Sub4_154.method488
				    (class10.anInt169, anInt685, anInt687,
				     anInt671, anInt693,
				     class10.anInt156 - anInt686,
				     class10.anInt153 - anInt669,
				     class10.anInt152 - anInt702,
				     class10.anInt157);
			    for (int i_310_ = class10.anInt151;
				 i_310_ <= class10.anInt174; i_310_++) {
				for (int i_311_ = class10.anInt158;
				     i_311_ <= class10.anInt162; i_311_++) {
				    Class39_Sub14 class39_sub14_312_
					= class39_sub14s[i_310_][i_311_];
				    if (class39_sub14_312_.anInt1536 != 0)
					aClass49_670.offerLast
					    (class39_sub14_312_);
				    else if ((i_310_ != i || i_311_ != i_262_)
					     && (class39_sub14_312_
						 .aBoolean1537))
					aClass49_670.offerLast
					    (class39_sub14_312_);
				}
			    }
			}
			if (class39_sub14_261_.aBoolean1529)
			    continue;
		    } catch (Exception exception) {
			class39_sub14_261_.aBoolean1529 = false;
		    }
		}
		if (class39_sub14_261_.aBoolean1537
		    && class39_sub14_261_.anInt1536 == 0) {
		    if (i <= anInt704 && i > anInt667) {
			Class39_Sub14 class39_sub14_313_
			    = class39_sub14s[i - 1][i_262_];
			if (class39_sub14_313_ != null
			    && class39_sub14_313_.aBoolean1537)
			    continue;
		    }
		    if (i >= anInt704 && i < anInt681 - 1) {
			Class39_Sub14 class39_sub14_314_
			    = class39_sub14s[i + 1][i_262_];
			if (class39_sub14_314_ != null
			    && class39_sub14_314_.aBoolean1537)
			    continue;
		    }
		    if (i_262_ <= anInt674 && i_262_ > anInt692) {
			Class39_Sub14 class39_sub14_315_
			    = class39_sub14s[i][i_262_ - 1];
			if (class39_sub14_315_ != null
			    && class39_sub14_315_.aBoolean1537)
			    continue;
		    }
		    if (i_262_ >= anInt674 && i_262_ < anInt672 - 1) {
			Class39_Sub14 class39_sub14_316_
			    = class39_sub14s[i][i_262_ + 1];
			if (class39_sub14_316_ != null
			    && class39_sub14_316_.aBoolean1537)
			    continue;
		    }
		    class39_sub14_261_.aBoolean1537 = false;
		    anInt700--;
		    Class67 class67 = class39_sub14_261_.aClass67_1541;
		    if (class67 != null && class67.anInt1181 != 0) {
			if (class67.aClass39_Sub5_Sub4_1182 != null)
			    class67.aClass39_Sub5_Sub4_1182.method488
				(0, anInt685, anInt687, anInt671, anInt693,
				 class67.anInt1168 - anInt686,
				 (class67.anInt1171 - anInt669
				  - class67.anInt1181),
				 class67.anInt1180 - anInt702,
				 class67.anInt1166);
			if (class67.aClass39_Sub5_Sub4_1183 != null)
			    class67.aClass39_Sub5_Sub4_1183.method488
				(0, anInt685, anInt687, anInt671, anInt693,
				 class67.anInt1168 - anInt686,
				 (class67.anInt1171 - anInt669
				  - class67.anInt1181),
				 class67.anInt1180 - anInt702,
				 class67.anInt1166);
			if (class67.aClass39_Sub5_Sub4_1177 != null)
			    class67.aClass39_Sub5_Sub4_1177.method488
				(0, anInt685, anInt687, anInt671, anInt693,
				 class67.anInt1168 - anInt686,
				 (class67.anInt1171 - anInt669
				  - class67.anInt1181),
				 class67.anInt1180 - anInt702,
				 class67.anInt1166);
		    }
		    if (class39_sub14_261_.anInt1523 != 0) {
			Class44 class44 = class39_sub14_261_.aClass44_1530;
			if (class44 != null
			    && !method396(i_264_, i, i_262_,
					  (class44.aClass39_Sub5_Sub4_846
					   .anInt1726))) {
			    if ((class44.anInt831
				 & class39_sub14_261_.anInt1523)
				!= 0)
				class44.aClass39_Sub5_Sub4_846.method488
				    (class44.anInt840, anInt685, anInt687,
				     anInt671, anInt693,
				     class44.anInt834 - anInt686,
				     class44.anInt832 - anInt669,
				     class44.anInt845 - anInt702,
				     class44.anInt842);
			    else if ((class44.anInt831 & 0x300) != 0) {
				int i_317_ = class44.anInt834 - anInt686;
				int i_318_ = class44.anInt832 - anInt669;
				int i_319_ = class44.anInt845 - anInt702;
				int i_320_ = class44.anInt840;
				int i_321_;
				if (i_320_ == 1 || i_320_ == 2)
				    i_321_ = -i_317_;
				else
				    i_321_ = i_317_;
				int i_322_;
				if (i_320_ == 2 || i_320_ == 3)
				    i_322_ = -i_319_;
				else
				    i_322_ = i_319_;
				if ((class44.anInt831 & 0x100) != 0
				    && i_322_ >= i_321_) {
				    int i_323_
					= i_317_ + anIntArray666[i_320_];
				    int i_324_
					= i_319_ + anIntArray705[i_320_];
				    class44.aClass39_Sub5_Sub4_846.method488
					(i_320_ * 512 + 256, anInt685,
					 anInt687, anInt671, anInt693, i_323_,
					 i_318_, i_324_, class44.anInt842);
				}
				if ((class44.anInt831 & 0x200) != 0
				    && i_322_ <= i_321_) {
				    int i_325_
					= i_317_ + anIntArray668[i_320_];
				    int i_326_
					= i_319_ + anIntArray680[i_320_];
				    class44.aClass39_Sub5_Sub4_846.method488
					(i_320_ * 512 + 1280 & 0x7ff, anInt685,
					 anInt687, anInt671, anInt693, i_325_,
					 i_318_, i_326_, class44.anInt842);
				}
			    }
			}
			Class36 class36 = class39_sub14_261_.aClass36_1528;
			if (class36 != null) {
			    if ((class36.anInt649
				 & class39_sub14_261_.anInt1523) != 0
				&& !method377(i_264_, i, i_262_,
					      class36.anInt649))
				class36.aClass39_Sub5_Sub4_641.method488
				    (0, anInt685, anInt687, anInt671, anInt693,
				     class36.anInt636 - anInt686,
				     class36.anInt651 - anInt669,
				     class36.anInt638 - anInt702,
				     class36.anInt637);
			    if ((class36.anInt632
				 & class39_sub14_261_.anInt1523) != 0
				&& !method377(i_264_, i, i_262_,
					      class36.anInt632))
				class36.aClass39_Sub5_Sub4_646.method488
				    (0, anInt685, anInt687, anInt671, anInt693,
				     class36.anInt636 - anInt686,
				     class36.anInt651 - anInt669,
				     class36.anInt638 - anInt702,
				     class36.anInt637);
			}
		    }
		    if (i_263_ < anInt689 - 1) {
			Class39_Sub14 class39_sub14_327_
			    = (aClass39_Sub14ArrayArrayArray665[i_263_ + 1][i]
			       [i_262_]);
			if (class39_sub14_327_ != null
			    && class39_sub14_327_.aBoolean1537)
			    aClass49_670.offerLast(class39_sub14_327_);
		    }
		    if (i < anInt704) {
			Class39_Sub14 class39_sub14_328_
			    = class39_sub14s[i + 1][i_262_];
			if (class39_sub14_328_ != null
			    && class39_sub14_328_.aBoolean1537)
			    aClass49_670.offerLast(class39_sub14_328_);
		    }
		    if (i_262_ < anInt674) {
			Class39_Sub14 class39_sub14_329_
			    = class39_sub14s[i][i_262_ + 1];
			if (class39_sub14_329_ != null
			    && class39_sub14_329_.aBoolean1537)
			    aClass49_670.offerLast(class39_sub14_329_);
		    }
		    if (i > anInt704) {
			Class39_Sub14 class39_sub14_330_
			    = class39_sub14s[i - 1][i_262_];
			if (class39_sub14_330_ != null
			    && class39_sub14_330_.aBoolean1537)
			    aClass49_670.offerLast(class39_sub14_330_);
		    }
		    if (i_262_ > anInt674) {
			Class39_Sub14 class39_sub14_331_
			    = class39_sub14s[i][i_262_ - 1];
			if (class39_sub14_331_ != null
			    && class39_sub14_331_.aBoolean1537)
			    aClass49_670.offerLast(class39_sub14_331_);
		    }
		}
	    }
	}
    }
    
    public void method390(int i) {
	anInt678 = i;
	for (int i_332_ = 0; i_332_ < anInt706; i_332_++) {
	    for (int i_333_ = 0; i_333_ < anInt696; i_333_++) {
		if (aClass39_Sub14ArrayArrayArray665[i][i_332_][i_333_]
		    == null)
		    aClass39_Sub14ArrayArrayArray665[i][i_332_][i_333_]
			= new Class39_Sub14(i, i_332_, i_333_);
	    }
	}
    }
    
    public void method391(int i, int i_334_, int i_335_, int i_336_,
			  Class39_Sub5_Sub4 class39_sub5_sub4, int i_337_,
			  Class39_Sub5_Sub4 class39_sub5_sub4_338_,
			  Class39_Sub5_Sub4 class39_sub5_sub4_339_) {
	Class67 class67 = new Class67();
	class67.aClass39_Sub5_Sub4_1177 = class39_sub5_sub4;
	class67.anInt1168 = i_334_ * 128 + 64;
	class67.anInt1180 = i_335_ * 128 + 64;
	class67.anInt1171 = i_336_;
	class67.anInt1166 = i_337_;
	class67.aClass39_Sub5_Sub4_1182 = class39_sub5_sub4_338_;
	class67.aClass39_Sub5_Sub4_1183 = class39_sub5_sub4_339_;
	int i_340_ = 0;
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_334_][i_335_];
	if (class39_sub14 != null) {
	    for (int i_341_ = 0; i_341_ < class39_sub14.anInt1538; i_341_++) {
		if (((class39_sub14.aClass10Array1524[i_341_].anInt171 & 0x100)
		     == 256)
		    && ((class39_sub14.aClass10Array1524[i_341_]
			 .aClass39_Sub5_Sub4_154)
			instanceof Model)) {
		    Model class39_sub5_sub4_sub6
			= ((Model)
			   (class39_sub14.aClass10Array1524[i_341_]
			    .aClass39_Sub5_Sub4_154));
		    class39_sub5_sub4_sub6.method542();
		    if (class39_sub5_sub4_sub6.anInt1726 > i_340_)
			i_340_ = class39_sub5_sub4_sub6.anInt1726;
		}
	    }
	}
	class67.anInt1181 = i_340_;
	if (aClass39_Sub14ArrayArrayArray665[i][i_334_][i_335_] == null)
	    aClass39_Sub14ArrayArrayArray665[i][i_334_][i_335_]
		= new Class39_Sub14(i, i_334_, i_335_);
	aClass39_Sub14ArrayArrayArray665[i][i_334_][i_335_].aClass67_1541
	    = class67;
    }
    
    public void method392(Class17 class17, int i, int i_342_, int i_343_,
			  int i_344_, int i_345_, int i_346_) {
	int i_347_ = class17.anIntArray334.length;
	for (int i_348_ = 0; i_348_ < i_347_; i_348_++) {
	    int i_349_ = class17.anIntArray334[i_348_] - anInt686;
	    int i_350_ = class17.anIntArray325[i_348_] - anInt669;
	    int i_351_ = class17.anIntArray335[i_348_] - anInt702;
	    int i_352_ = i_351_ * i_343_ + i_349_ * i_344_ >> 16;
	    i_351_ = i_351_ * i_344_ - i_349_ * i_343_ >> 16;
	    i_349_ = i_352_;
	    i_352_ = i_350_ * i_342_ - i_351_ * i >> 16;
	    i_351_ = i_350_ * i + i_351_ * i_342_ >> 16;
	    i_350_ = i_352_;
	    if (i_351_ < GROUND_NEAR)
		return;
	    if (class17.anIntArray327 != null) {
		Class17.anIntArray321[i_348_] = i_349_;
		Class17.anIntArray330[i_348_] = i_350_;
		Class17.anIntArray322[i_348_] = i_351_;
	    }
	    Class17.anIntArray326[i_348_]
		= Class39_Sub5_Sub10_Sub2.anInt2462 + (i_349_ * Resizable.zoom) / i_351_;
	    Class17.anIntArray333[i_348_]
		= Class39_Sub5_Sub10_Sub2.anInt2467 + (i_350_ * Resizable.zoom) / i_351_;
	}
	Class39_Sub5_Sub10_Sub2.anInt2468 = 0;
	i_347_ = class17.anIntArray314.length;
	for (int i_353_ = 0; i_353_ < i_347_; i_353_++) {
	    int i_354_ = class17.anIntArray314[i_353_];
	    int i_355_ = class17.anIntArray323[i_353_];
	    int i_356_ = class17.anIntArray329[i_353_];
	    int i_357_ = Class17.anIntArray326[i_354_];
	    int i_358_ = Class17.anIntArray326[i_355_];
	    int i_359_ = Class17.anIntArray326[i_356_];
	    int i_360_ = Class17.anIntArray333[i_354_];
	    int i_361_ = Class17.anIntArray333[i_355_];
	    int i_362_ = Class17.anIntArray333[i_356_];
	    if (((i_357_ - i_358_) * (i_362_ - i_361_)
		 - (i_360_ - i_361_) * (i_359_ - i_358_))
		> 0) {
		Class39_Sub5_Sub10_Sub2.aBoolean2470 = false;
		if (i_357_ < 0 || i_358_ < 0 || i_359_ < 0
		    || i_357_ > Class39_Sub5_Sub10_Sub2.anInt2461
		    || i_358_ > Class39_Sub5_Sub10_Sub2.anInt2461
		    || i_359_ > Class39_Sub5_Sub10_Sub2.anInt2461)
		    Class39_Sub5_Sub10_Sub2.aBoolean2470 = true;
		if (aBoolean703
		    && method397(anInt675, anInt697, i_360_, i_361_, i_362_,
				 i_357_, i_358_, i_359_)) {
		    anInt682 = i_345_;
		    anInt677 = i_346_;
		}
		if (class17.anIntArray327 == null
		    || class17.anIntArray327[i_353_] == -1) {
		    if (class17.anIntArray332[i_353_] != 12345678)
			Class39_Sub5_Sub10_Sub2.method660
			    (i_360_, i_361_, i_362_, i_357_, i_358_, i_359_,
			     class17.anIntArray332[i_353_],
			     class17.anIntArray319[i_353_],
			     class17.anIntArray318[i_353_]);
		} else if (!aBoolean684) {
		    if (class17.aBoolean315)
			Class39_Sub5_Sub10_Sub2.method665
			    (i_360_, i_361_, i_362_, i_357_, i_358_, i_359_,
			     class17.anIntArray332[i_353_],
			     class17.anIntArray319[i_353_],
			     class17.anIntArray318[i_353_],
			     Class17.anIntArray321[0],
			     Class17.anIntArray321[1],
			     Class17.anIntArray321[3],
			     Class17.anIntArray330[0],
			     Class17.anIntArray330[1],
			     Class17.anIntArray330[3],
			     Class17.anIntArray322[0],
			     Class17.anIntArray322[1],
			     Class17.anIntArray322[3],
			     class17.anIntArray327[i_353_]);
		    else
			Class39_Sub5_Sub10_Sub2.method665
			    (i_360_, i_361_, i_362_, i_357_, i_358_, i_359_,
			     class17.anIntArray332[i_353_],
			     class17.anIntArray319[i_353_],
			     class17.anIntArray318[i_353_],
			     Class17.anIntArray321[i_354_],
			     Class17.anIntArray321[i_355_],
			     Class17.anIntArray321[i_356_],
			     Class17.anIntArray330[i_354_],
			     Class17.anIntArray330[i_355_],
			     Class17.anIntArray330[i_356_],
			     Class17.anIntArray322[i_354_],
			     Class17.anIntArray322[i_355_],
			     Class17.anIntArray322[i_356_],
			     class17.anIntArray327[i_353_]);
		} else {
		    int i_363_
			= Class39_Sub5_Sub10_Sub2.anInterface2_2458
			      .method7(105, class17.anIntArray327[i_353_]);
		    Class39_Sub5_Sub10_Sub2.method660
			(i_360_, i_361_, i_362_, i_357_, i_358_, i_359_,
			 method385(i_363_, class17.anIntArray332[i_353_]),
			 method385(i_363_, class17.anIntArray319[i_353_]),
			 method385(i_363_, class17.anIntArray318[i_353_]));
		}
	    }
	}
    }
    
    public void method393() {
	for (int i = 0; i < anInt676; i++) {
	    Class10 class10 = aClass10Array690[i];
	    method360(class10);
	    aClass10Array690[i] = null;
	}
	anInt676 = 0;
    }
    
    public void method394(Class32 class32, int i, int i_364_, int i_365_,
			  int i_366_, int i_367_, int i_368_, int i_369_) {
	int i_371_;
	int i_370_ = i_371_ = (i_368_ << 7) - anInt686;
	int i_373_;
	int i_372_ = i_373_ = (i_369_ << 7) - anInt702;
	int i_375_;
	int i_374_ = i_375_ = i_370_ + 128;
	int i_377_;
	int i_376_ = i_377_ = i_372_ + 128;
	int i_378_ = anIntArrayArrayArray699[i][i_368_][i_369_] - anInt669;
	int i_379_ = anIntArrayArrayArray699[i][i_368_ + 1][i_369_] - anInt669;
	int i_380_
	    = anIntArrayArrayArray699[i][i_368_ + 1][i_369_ + 1] - anInt669;
	int i_381_ = anIntArrayArrayArray699[i][i_368_][i_369_ + 1] - anInt669;
	int i_382_ = i_372_ * i_366_ + i_370_ * i_367_ >> 16;
	i_372_ = i_372_ * i_367_ - i_370_ * i_366_ >> 16;
	i_370_ = i_382_;
	i_382_ = i_378_ * i_365_ - i_372_ * i_364_ >> 16;
	i_372_ = i_378_ * i_364_ + i_372_ * i_365_ >> 16;
	i_378_ = i_382_;
	if (i_372_ >= GROUND_NEAR) {
	    i_382_ = i_373_ * i_366_ + i_374_ * i_367_ >> 16;
	    i_373_ = i_373_ * i_367_ - i_374_ * i_366_ >> 16;
	    i_374_ = i_382_;
	    i_382_ = i_379_ * i_365_ - i_373_ * i_364_ >> 16;
	    i_373_ = i_379_ * i_364_ + i_373_ * i_365_ >> 16;
	    i_379_ = i_382_;
	    if (i_373_ >= GROUND_NEAR) {
		i_382_ = i_376_ * i_366_ + i_375_ * i_367_ >> 16;
		i_376_ = i_376_ * i_367_ - i_375_ * i_366_ >> 16;
		i_375_ = i_382_;
		i_382_ = i_380_ * i_365_ - i_376_ * i_364_ >> 16;
		i_376_ = i_380_ * i_364_ + i_376_ * i_365_ >> 16;
		i_380_ = i_382_;
		if (i_376_ >= GROUND_NEAR) {
		    i_382_ = i_377_ * i_366_ + i_371_ * i_367_ >> 16;
		    i_377_ = i_377_ * i_367_ - i_371_ * i_366_ >> 16;
		    i_371_ = i_382_;
		    i_382_ = i_381_ * i_365_ - i_377_ * i_364_ >> 16;
		    i_377_ = i_381_ * i_364_ + i_377_ * i_365_ >> 16;
		    i_381_ = i_382_;
		    if (i_377_ >= GROUND_NEAR) {
			int i_383_ = (Class39_Sub5_Sub10_Sub2.anInt2462
				      + (i_370_ * Resizable.zoom) / i_372_);
			int i_384_ = (Class39_Sub5_Sub10_Sub2.anInt2467
				      + (i_378_ * Resizable.zoom) / i_372_);
			int i_385_ = (Class39_Sub5_Sub10_Sub2.anInt2462
				      + (i_374_ * Resizable.zoom) / i_373_);
			int i_386_ = (Class39_Sub5_Sub10_Sub2.anInt2467
				      + (i_379_ * Resizable.zoom) / i_373_);
			int i_387_ = (Class39_Sub5_Sub10_Sub2.anInt2462
				      + (i_375_ * Resizable.zoom) / i_376_);
			int i_388_ = (Class39_Sub5_Sub10_Sub2.anInt2467
				      + (i_380_ * Resizable.zoom) / i_376_);
			int i_389_ = (Class39_Sub5_Sub10_Sub2.anInt2462
				      + (i_371_ * Resizable.zoom) / i_377_);
			int i_390_ = (Class39_Sub5_Sub10_Sub2.anInt2467
				      + (i_381_ * Resizable.zoom) / i_377_);
			Class39_Sub5_Sub10_Sub2.anInt2468 = 0;
			if (((i_387_ - i_389_) * (i_386_ - i_390_)
			     - (i_388_ - i_390_) * (i_385_ - i_389_))
			    > 0) {
			    Class39_Sub5_Sub10_Sub2.aBoolean2470 = false;
			    if (i_387_ < 0 || i_389_ < 0 || i_385_ < 0
				|| i_387_ > Class39_Sub5_Sub10_Sub2.anInt2461
				|| i_389_ > Class39_Sub5_Sub10_Sub2.anInt2461
				|| i_385_ > Class39_Sub5_Sub10_Sub2.anInt2461)
				Class39_Sub5_Sub10_Sub2.aBoolean2470 = true;
			    if (aBoolean703
				&& method397(anInt675, anInt697, i_388_,
					     i_390_, i_386_, i_387_, i_389_,
					     i_385_)) {
				anInt682 = i_368_;
				anInt677 = i_369_;
			    }
			    if (class32.anInt579 == -1) {
				if (class32.anInt580 != 12345678)
				    Class39_Sub5_Sub10_Sub2.method660
					(i_388_, i_390_, i_386_, i_387_,
					 i_389_, i_385_, class32.anInt580,
					 class32.anInt582, class32.anInt576);
			    } else if (!aBoolean684) {
				if (class32.aBoolean574)
				    Class39_Sub5_Sub10_Sub2.method665
					(i_388_, i_390_, i_386_, i_387_,
					 i_389_, i_385_, class32.anInt580,
					 class32.anInt582, class32.anInt576,
					 i_370_, i_374_, i_371_, i_378_,
					 i_379_, i_381_, i_372_, i_373_,
					 i_377_, class32.anInt579);
				else
				    Class39_Sub5_Sub10_Sub2.method665
					(i_388_, i_390_, i_386_, i_387_,
					 i_389_, i_385_, class32.anInt580,
					 class32.anInt582, class32.anInt576,
					 i_375_, i_371_, i_374_, i_380_,
					 i_381_, i_379_, i_376_, i_377_,
					 i_373_, class32.anInt579);
			    } else {
				int i_391_
				    = Class39_Sub5_Sub10_Sub2
					  .anInterface2_2458
					  .method7(105, class32.anInt579);
				Class39_Sub5_Sub10_Sub2.method660
				    (i_388_, i_390_, i_386_, i_387_, i_389_,
				     i_385_,
				     method385(i_391_, class32.anInt580),
				     method385(i_391_, class32.anInt582),
				     method385(i_391_, class32.anInt576));
			    }
			}
			if (((i_383_ - i_385_) * (i_390_ - i_386_)
			     - (i_384_ - i_386_) * (i_389_ - i_385_))
			    > 0) {
			    Class39_Sub5_Sub10_Sub2.aBoolean2470 = false;
			    if (i_383_ < 0 || i_385_ < 0 || i_389_ < 0
				|| i_383_ > Class39_Sub5_Sub10_Sub2.anInt2461
				|| i_385_ > Class39_Sub5_Sub10_Sub2.anInt2461
				|| i_389_ > Class39_Sub5_Sub10_Sub2.anInt2461)
				Class39_Sub5_Sub10_Sub2.aBoolean2470 = true;
			    if (aBoolean703
				&& method397(anInt675, anInt697, i_384_,
					     i_386_, i_390_, i_383_, i_385_,
					     i_389_)) {
				anInt682 = i_368_;
				anInt677 = i_369_;
			    }
			    if (class32.anInt579 == -1) {
				if (class32.anInt575 != 12345678)
				    Class39_Sub5_Sub10_Sub2.method660
					(i_384_, i_386_, i_390_, i_383_,
					 i_385_, i_389_, class32.anInt575,
					 class32.anInt576, class32.anInt582);
			    } else if (!aBoolean684)
				Class39_Sub5_Sub10_Sub2.method665
				    (i_384_, i_386_, i_390_, i_383_, i_385_,
				     i_389_, class32.anInt575,
				     class32.anInt576, class32.anInt582,
				     i_370_, i_374_, i_371_, i_378_, i_379_,
				     i_381_, i_372_, i_373_, i_377_,
				     class32.anInt579);
			    else {
				int i_392_
				    = Class39_Sub5_Sub10_Sub2
					  .anInterface2_2458
					  .method7(105, class32.anInt579);
				Class39_Sub5_Sub10_Sub2.method660
				    (i_384_, i_386_, i_390_, i_383_, i_385_,
				     i_389_,
				     method385(i_392_, class32.anInt575),
				     method385(i_392_, class32.anInt576),
				     method385(i_392_, class32.anInt582));
			    }
			}
		    }
		}
	    }
	}
    }
    
    public void method395(int i, int i_393_, int i_394_) {
	for (int i_395_ = 0; i_395_ < anInt689; i_395_++) {
	    for (int i_396_ = 0; i_396_ < anInt706; i_396_++) {
		for (int i_397_ = 0; i_397_ < anInt696; i_397_++) {
		    Class39_Sub14 class39_sub14
			= (aClass39_Sub14ArrayArrayArray665[i_395_][i_396_]
			   [i_397_]);
		    if (class39_sub14 != null) {
			Class36 class36 = class39_sub14.aClass36_1528;
			if (class36 != null
			    && (class36.aClass39_Sub5_Sub4_646
				instanceof Model)) {
			    Model class39_sub5_sub4_sub6
				= ((Model)
				   class36.aClass39_Sub5_Sub4_646);
			    if (class39_sub5_sub4_sub6.aClass48Array2361
				!= null) {
				method361(class39_sub5_sub4_sub6, i_395_,
					  i_396_, i_397_, 1, 1);
				if (class36.aClass39_Sub5_Sub4_641
				    instanceof Model) {
				    Model class39_sub5_sub4_sub6_398_
					= ((Model)
					   class36.aClass39_Sub5_Sub4_641);
				    if ((class39_sub5_sub4_sub6_398_
					 .aClass48Array2361)
					!= null) {
					method361(class39_sub5_sub4_sub6_398_,
						  i_395_, i_396_, i_397_, 1,
						  1);
					method372(class39_sub5_sub4_sub6,
						  class39_sub5_sub4_sub6_398_,
						  0, 0, 0, false);
					class39_sub5_sub4_sub6_398_
					    .method539(i, i_393_, i_394_);
				    }
				}
				class39_sub5_sub4_sub6.method539(i, i_393_,
								 i_394_);
			    }
			}
			for (int i_399_ = 0; i_399_ < class39_sub14.anInt1538;
			     i_399_++) {
			    Class10 class10
				= class39_sub14.aClass10Array1524[i_399_];
			    if (class10 != null
				&& (class10.aClass39_Sub5_Sub4_154
				    instanceof Model)) {
				Model class39_sub5_sub4_sub6
				    = ((Model)
				       class10.aClass39_Sub5_Sub4_154);
				if (class39_sub5_sub4_sub6.aClass48Array2361
				    != null) {
				    method361(class39_sub5_sub4_sub6, i_395_,
					      i_396_, i_397_,
					      (class10.anInt174
					       - class10.anInt151 + 1),
					      (class10.anInt162
					       - class10.anInt158 + 1));
				    class39_sub5_sub4_sub6.method539(i, i_393_,
								     i_394_);
				}
			    }
			}
			Class50 class50 = class39_sub14.aClass50_1539;
			if (class50 != null
			    && (class50.aClass39_Sub5_Sub4_933
				instanceof Model)) {
			    Model class39_sub5_sub4_sub6
				= ((Model)
				   class50.aClass39_Sub5_Sub4_933);
			    if (class39_sub5_sub4_sub6.aClass48Array2361
				!= null) {
				method401(class39_sub5_sub4_sub6, i_395_,
					  i_396_, i_397_);
				class39_sub5_sub4_sub6.method539(i, i_393_,
								 i_394_);
			    }
			}
		    }
		}
	    }
	}
    }
    
    public boolean method396(int i, int i_400_, int i_401_, int i_402_) {
	if (!method376(i, i_400_, i_401_))
	    return false;
	int i_403_ = i_400_ << 7;
	int i_404_ = i_401_ << 7;
	if (method371(i_403_ + 1,
		      anIntArrayArrayArray699[i][i_400_][i_401_] - i_402_,
		      i_404_ + 1)
	    && method371(i_403_ + 128 - 1,
			 (anIntArrayArrayArray699[i][i_400_ + 1][i_401_]
			  - i_402_),
			 i_404_ + 1)
	    && method371(i_403_ + 128 - 1,
			 (anIntArrayArrayArray699[i][i_400_ + 1][i_401_ + 1]
			  - i_402_),
			 i_404_ + 128 - 1)
	    && method371(i_403_ + 1,
			 (anIntArrayArrayArray699[i][i_400_][i_401_ + 1]
			  - i_402_),
			 i_404_ + 128 - 1))
	    return true;
	return false;
    }
    
    public boolean method397(int i, int i_405_, int i_406_, int i_407_,
			     int i_408_, int i_409_, int i_410_, int i_411_) {
	if (i_405_ < i_406_ && i_405_ < i_407_ && i_405_ < i_408_)
	    return false;
	if (i_405_ > i_406_ && i_405_ > i_407_ && i_405_ > i_408_)
	    return false;
	if (i < i_409_ && i < i_410_ && i < i_411_)
	    return false;
	if (i > i_409_ && i > i_410_ && i > i_411_)
	    return false;
	int i_412_ = ((i_405_ - i_406_) * (i_410_ - i_409_)
		      - (i - i_409_) * (i_407_ - i_406_));
	int i_413_ = ((i_405_ - i_408_) * (i_409_ - i_411_)
		      - (i - i_411_) * (i_406_ - i_408_));
	int i_414_ = ((i_405_ - i_407_) * (i_411_ - i_410_)
		      - (i - i_410_) * (i_408_ - i_407_));
	if (i_412_ * i_414_ > 0 && i_414_ * i_413_ > 0)
	    return true;
	return false;
    }
    
    public void method398(int i, int i_415_, int i_416_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_415_][i_416_];
	if (class39_sub14 != null) {
	    for (int i_417_ = 0; i_417_ < class39_sub14.anInt1538; i_417_++) {
		Class10 class10 = class39_sub14.aClass10Array1524[i_417_];
		if ((class10.anInt157 >> 29 & 0x3) == 2
		    && class10.anInt151 == i_415_
		    && class10.anInt158 == i_416_) {
		    method360(class10);
		    break;
		}
	    }
	}
    }
    
    public void method399() {
	int i = anIntArray701[anInt679];
	Class26[] class26s = aClass26ArrayArray688[anInt679];
	anInt673 = 0;
	for (int i_418_ = 0; i_418_ < i; i_418_++) {
	    Class26 class26 = class26s[i_418_];
	    if (class26.anInt493 == 1) {
		int i_419_ = class26.anInt480 - anInt704 + DR;
		if (i_419_ >= 0 && i_419_ <= 2 * DR) {
		    int i_420_ = class26.anInt487 - anInt674 + DR;
		    if (i_420_ < 0)
			i_420_ = 0;
		    int i_421_ = class26.anInt502 - anInt674 + DR;
		    if (i_421_ > 2 * DR)
			i_421_ = 2 * DR;
		    boolean bool = false;
		    while (i_420_ <= i_421_) {
			if (aBooleanArrayArray725[i_419_][i_420_++]) {
			    bool = true;
			    break;
			}
		    }
		    if (bool) {
			int i_422_ = anInt686 - class26.anInt495;
			if (i_422_ > 32)
			    class26.anInt475 = 1;
			else {
			    if (i_422_ >= -32)
				continue;
			    class26.anInt475 = 2;
			    i_422_ = -i_422_;
			}
			class26.anInt488
			    = (class26.anInt481 - anInt702 << 8) / i_422_;
			class26.anInt479
			    = (class26.anInt501 - anInt702 << 8) / i_422_;
			class26.anInt498
			    = (class26.anInt494 - anInt669 << 8) / i_422_;
			class26.anInt476
			    = (class26.anInt477 - anInt669 << 8) / i_422_;
			aClass26Array691[anInt673++] = class26;
		    }
		}
	    } else if (class26.anInt493 == 2) {
		int i_423_ = class26.anInt487 - anInt674 + DR;
		if (i_423_ >= 0 && i_423_ <= 2 * DR) {
		    int i_424_ = class26.anInt480 - anInt704 + DR;
		    if (i_424_ < 0)
			i_424_ = 0;
		    int i_425_ = class26.anInt500 - anInt704 + DR;
		    if (i_425_ > 2 * DR)
			i_425_ = 2 * DR;
		    boolean bool = false;
		    while (i_424_ <= i_425_) {
			if (aBooleanArrayArray725[i_424_++][i_423_]) {
			    bool = true;
			    break;
			}
		    }
		    if (bool) {
			int i_426_ = anInt702 - class26.anInt481;
			if (i_426_ > 32)
			    class26.anInt475 = 3;
			else {
			    if (i_426_ >= -32)
				continue;
			    class26.anInt475 = 4;
			    i_426_ = -i_426_;
			}
			class26.anInt497
			    = (class26.anInt495 - anInt686 << 8) / i_426_;
			class26.anInt492
			    = (class26.anInt499 - anInt686 << 8) / i_426_;
			class26.anInt498
			    = (class26.anInt494 - anInt669 << 8) / i_426_;
			class26.anInt476
			    = (class26.anInt477 - anInt669 << 8) / i_426_;
			aClass26Array691[anInt673++] = class26;
		    }
		}
	    } else if (class26.anInt493 == 4) {
		int i_427_ = class26.anInt494 - anInt669;
		if (i_427_ > 128) {
		    int i_428_ = class26.anInt487 - anInt674 + DR;
		    if (i_428_ < 0)
			i_428_ = 0;
		    int i_429_ = class26.anInt502 - anInt674 + DR;
		    if (i_429_ > 2 * DR)
			i_429_ = 2 * DR;
		    if (i_428_ <= i_429_) {
			int i_430_ = class26.anInt480 - anInt704 + DR;
			if (i_430_ < 0)
			    i_430_ = 0;
			int i_431_ = class26.anInt500 - anInt704 + DR;
			if (i_431_ > 2 * DR)
			    i_431_ = 2 * DR;
			boolean bool = false;
		    while_11_:
			for (int i_432_ = i_430_; i_432_ <= i_431_; i_432_++) {
			    for (int i_433_ = i_428_; i_433_ <= i_429_;
				 i_433_++) {
				if (aBooleanArrayArray725[i_432_][i_433_]) {
				    bool = true;
				    break while_11_;
				}
			    }
			}
			if (bool) {
			    class26.anInt475 = 5;
			    class26.anInt497
				= (class26.anInt495 - anInt686 << 8) / i_427_;
			    class26.anInt492
				= (class26.anInt499 - anInt686 << 8) / i_427_;
			    class26.anInt488
				= (class26.anInt481 - anInt702 << 8) / i_427_;
			    class26.anInt479
				= (class26.anInt501 - anInt702 << 8) / i_427_;
			    aClass26Array691[anInt673++] = class26;
			}
		    }
		}
	    }
	}
    }
    
    public Class50 method400(int i, int i_434_, int i_435_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_434_][i_435_];
	if (class39_sub14 == null || class39_sub14.aClass50_1539 == null)
	    return null;
	return class39_sub14.aClass50_1539;
    }
    
    public void method401(Model class39_sub5_sub4_sub6, int i,
			  int i_436_, int i_437_) {
	if (i_436_ < anInt706) {
	    Class39_Sub14 class39_sub14
		= aClass39_Sub14ArrayArrayArray665[i][i_436_ + 1][i_437_];
	    if (class39_sub14 != null && class39_sub14.aClass50_1539 != null
		&& (class39_sub14.aClass50_1539.aClass39_Sub5_Sub4_933
		    instanceof Model)) {
		Model class39_sub5_sub4_sub6_438_
		    = ((Model)
		       class39_sub14.aClass50_1539.aClass39_Sub5_Sub4_933);
		if (class39_sub5_sub4_sub6_438_.aClass48Array2361 != null)
		    method372(class39_sub5_sub4_sub6,
			      class39_sub5_sub4_sub6_438_, 128, 0, 0, true);
	    }
	}
	if (i_437_ < anInt706) {
	    Class39_Sub14 class39_sub14
		= aClass39_Sub14ArrayArrayArray665[i][i_436_][i_437_ + 1];
	    if (class39_sub14 != null && class39_sub14.aClass50_1539 != null
		&& (class39_sub14.aClass50_1539.aClass39_Sub5_Sub4_933
		    instanceof Model)) {
		Model class39_sub5_sub4_sub6_439_
		    = ((Model)
		       class39_sub14.aClass50_1539.aClass39_Sub5_Sub4_933);
		if (class39_sub5_sub4_sub6_439_.aClass48Array2361 != null)
		    method372(class39_sub5_sub4_sub6,
			      class39_sub5_sub4_sub6_439_, 0, 0, 128, true);
	    }
	}
	if (i_436_ < anInt706 && i_437_ < anInt696) {
	    Class39_Sub14 class39_sub14
		= aClass39_Sub14ArrayArrayArray665[i][i_436_ + 1][i_437_ + 1];
	    if (class39_sub14 != null && class39_sub14.aClass50_1539 != null
		&& (class39_sub14.aClass50_1539.aClass39_Sub5_Sub4_933
		    instanceof Model)) {
		Model class39_sub5_sub4_sub6_440_
		    = ((Model)
		       class39_sub14.aClass50_1539.aClass39_Sub5_Sub4_933);
		if (class39_sub5_sub4_sub6_440_.aClass48Array2361 != null)
		    method372(class39_sub5_sub4_sub6,
			      class39_sub5_sub4_sub6_440_, 128, 0, 128, true);
	    }
	}
	if (i_436_ < anInt706 && i_437_ > 0) {
	    Class39_Sub14 class39_sub14
		= aClass39_Sub14ArrayArrayArray665[i][i_436_ + 1][i_437_ - 1];
	    if (class39_sub14 != null && class39_sub14.aClass50_1539 != null
		&& (class39_sub14.aClass50_1539.aClass39_Sub5_Sub4_933
		    instanceof Model)) {
		Model class39_sub5_sub4_sub6_441_
		    = ((Model)
		       class39_sub14.aClass50_1539.aClass39_Sub5_Sub4_933);
		if (class39_sub5_sub4_sub6_441_.aClass48Array2361 != null)
		    method372(class39_sub5_sub4_sub6,
			      class39_sub5_sub4_sub6_441_, 128, 0, -128, true);
	    }
	}
    }
    
    public static void method402() {
	aClass10Array698 = null;
	anIntArray666 = null;
	anIntArray705 = null;
	anIntArray668 = null;
	anIntArray680 = null;
	anIntArray701 = null;
	aClass26ArrayArray688 = null;
	aClass26Array691 = null;
	aClass49_670 = null;
	anIntArray717 = null;
	anIntArray713 = null;
	anIntArray709 = null;
	anIntArray720 = null;
	anIntArray718 = null;
	anIntArray715 = null;
	anIntArray724 = null;
	aBooleanArrayArrayArrayArray712 = null;
	aBooleanArrayArray725 = null;
    }
    
    public static void method403(int[] is, int i, int i_442_, int i_443_,
				 int i_444_) {
	anInt714 = 0;
	anInt711 = 0;
	anInt719 = i_443_;
	anInt710 = i_444_;
	anInt726 = i_443_ / 2;
	anInt722 = i_444_ / 2;
	boolean[][][][] bools = new boolean[9][32][2 * DR + 3][2 * DR + 3];
	for (int i_445_ = 128; i_445_ <= 384; i_445_ += 32) {
	    for (int i_446_ = 0; i_446_ < 2048; i_446_ += 64) {
		anInt685 = Model.anIntArray2394[i_445_];
		anInt687 = Model.anIntArray2418[i_445_];
		anInt671 = Model.anIntArray2394[i_446_];
		anInt693 = Model.anIntArray2418[i_446_];
		int i_447_ = (i_445_ - 128) / 32;
		int i_448_ = i_446_ / 64;
		for (int i_449_ = -DR - 1; i_449_ <= DR + 1; i_449_++) {
		    for (int i_450_ = -DR - 1; i_450_ <= DR + 1; i_450_++) {
			int i_451_ = i_449_ * 128;
			int i_452_ = i_450_ * 128;
			boolean bool = false;
			for (int zs = 0; zs < 6 && !bool; zs++) {
			    int camH = is[i_447_] * (new int[] { 35, 50, 65, 100, 140, 175 })[zs] / 100;
			    for (int i_453_ = -i; i_453_ <= i_442_;
				 i_453_ += 128) {
				if (method369(i_451_, camH + i_453_,
					      i_452_)) {
				    bool = true;
				    break;
				}
			    }
			}
			bools[i_447_][i_448_][i_449_ + DR + 1][i_450_ + DR + 1]
			    = bool;
		    }
		}
	    }
	}
	for (int i_454_ = 0; i_454_ < 8; i_454_++) {
	    for (int i_455_ = 0; i_455_ < 32; i_455_++) {
		for (int i_456_ = -DR; i_456_ < DR; i_456_++) {
		    for (int i_457_ = -DR; i_457_ < DR; i_457_++) {
			boolean bool = false;
		    while_12_:
			for (int i_458_ = -1; i_458_ <= 1; i_458_++) {
			    for (int i_459_ = -1; i_459_ <= 1; i_459_++) {
				if (bools[i_454_][i_455_]
				    [i_456_ + i_458_ + DR + 1]
				    [i_457_ + i_459_ + DR + 1]) {
				    bool = true;
				    break while_12_;
				}
				if (bools[i_454_][(i_455_ + 1) % 31]
				    [i_456_ + i_458_ + DR + 1]
				    [i_457_ + i_459_ + DR + 1]) {
				    bool = true;
				    break while_12_;
				}
				if (bools[i_454_ + 1][i_455_]
				    [i_456_ + i_458_ + DR + 1]
				    [i_457_ + i_459_ + DR + 1]) {
				    bool = true;
				    break while_12_;
				}
				if (bools[i_454_ + 1][(i_455_ + 1) % 31]
				    [i_456_ + i_458_ + DR + 1]
				    [i_457_ + i_459_ + DR + 1]) {
				    bool = true;
				    break while_12_;
				}
			    }
			}
			aBooleanArrayArrayArrayArray712[i_454_][i_455_]
			    [i_456_ + DR][i_457_ + DR]
			    = bool;
		    }
		}
	    }
	}
    }
    
    public int method404(int i, int i_460_, int i_461_) {
	Class39_Sub14 class39_sub14
	    = aClass39_Sub14ArrayArrayArray665[i][i_460_][i_461_];
	if (class39_sub14 == null || class39_sub14.aClass50_1539 == null)
	    return 0;
	return class39_sub14.aClass50_1539.anInt935;
    }
    
    public void method405(int i, int i_462_, int i_463_, int i_464_,
			  Class39_Sub5_Sub4 class39_sub5_sub4,
			  Class39_Sub5_Sub4 class39_sub5_sub4_465_, int i_466_,
			  int i_467_, int i_468_, int i_469_) {
	if (class39_sub5_sub4 != null || class39_sub5_sub4_465_ != null) {
	    Class36 class36 = new Class36();
	    class36.anInt637 = i_468_;
	    class36.anInt644 = i_469_;
	    class36.anInt636 = i_462_ * 128 + 64;
	    class36.anInt638 = i_463_ * 128 + 64;
	    class36.anInt651 = i_464_;
	    class36.aClass39_Sub5_Sub4_646 = class39_sub5_sub4;
	    class36.aClass39_Sub5_Sub4_641 = class39_sub5_sub4_465_;
	    class36.anInt632 = i_466_;
	    class36.anInt649 = i_467_;
	    for (int i_470_ = i; i_470_ >= 0; i_470_--) {
		if (aClass39_Sub14ArrayArrayArray665[i_470_][i_462_][i_463_]
		    == null)
		    aClass39_Sub14ArrayArrayArray665[i_470_][i_462_][i_463_]
			= new Class39_Sub14(i_470_, i_462_, i_463_);
	    }
	    aClass39_Sub14ArrayArrayArray665[i][i_462_][i_463_].aClass36_1528
		= class36;
	}
    }
    
    public boolean method406(int i, int i_471_, int i_472_, int i_473_,
			     int i_474_, Class39_Sub5_Sub4 class39_sub5_sub4,
			     int i_475_, int i_476_, int i_477_, int i_478_,
			     int i_479_, int i_480_) {
	if (class39_sub5_sub4 == null)
	    return true;
	return method386(i, i_477_, i_478_, i_479_ - i_477_ + 1,
			 i_480_ - i_478_ + 1, i_471_, i_472_, i_473_,
			 class39_sub5_sub4, i_475_, true, i_476_, 0);
    }
    
    public Class38(int i, int i_481_, int i_482_, int[][][] is) {
	anInt676 = 0;
	aClass10Array690 = new Class10[5000];
	anInt707 = 0;
	anIntArrayArray708
	    = new int[][] { new int[16],
			    { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			    { 1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1 },
			    { 1, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0 },
			    { 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1 },
			    { 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
			    { 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1 },
			    { 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0 },
			    { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0 },
			    { 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1 },
			    { 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0 },
			    { 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1 },
			    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1,
			      1 } };
	anIntArrayArray723
	    = (new int[][]
	       { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
		 { 12, 8, 4, 0, 13, 9, 5, 1, 14, 10, 6, 2, 15, 11, 7, 3 },
		 { 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 },
		 { 3, 7, 11, 15, 2, 6, 10, 14, 1, 5, 9, 13, 0, 4, 8, 12 } });
	anIntArray716 = new int[10000];
	anIntArray721 = new int[10000];
	anInt689 = i;
	anInt706 = i_481_;
	anInt696 = i_482_;
	aClass39_Sub14ArrayArrayArray665
	    = new Class39_Sub14[i][i_481_][i_482_];
	anIntArrayArrayArray694 = new int[i][i_481_ + 1][i_482_ + 1];
	anIntArrayArrayArray699 = is;
	method378();
    }
    
    static {
	anIntArray666 = new int[] { 53, -53, -53, 53 };
	anInt677 = -1;
	anInt679 = 0;
	anInt673 = 0;
	aBoolean684 = true;
	aClass10Array698 = new Class10[100];
	anIntArray680 = new int[] { 45, 45, -45, -45 };
	anInt682 = -1;
	anInt697 = 0;
	aClass26Array691 = new Class26[500];
	anInt700 = 0;
	anIntArray705 = new int[] { -53, -53, 53, 53 };
	aBoolean703 = false;
	anInt683 = 4;
	aClass26ArrayArray688 = new Class26[anInt683][500];
	anIntArray701 = new int[anInt683];
	aClass49_670 = new Deque();
	anIntArray715 = new int[] { 0, 4, 4, 8, 0, 0, 8, 0, 0 };
	aBooleanArrayArrayArrayArray712 = new boolean[8][32][2 * DR + 1][2 * DR + 1];
	anIntArray709 = new int[] { 76, 8, 137, 4, 0, 1, 38, 2, 19 };
	anIntArray717 = new int[] { 19, 55, 38, 155, 255, 110, 137, 205, 76 };
	anIntArray713 = new int[] { 160, 192, 80, 96, 0, 144, 80, 48, 160 };
	anIntArray718 = new int[] { 2, 0, 0, 2, 0, 0, 0, 4, 4 };
	anIntArray720 = new int[] { 0, 0, 2, 0, 0, 2, 1, 1, 0 };
	anIntArray724 = new int[] { 1, 1, 0, 0, 0, 8, 0, 0, 8 };
    }
}

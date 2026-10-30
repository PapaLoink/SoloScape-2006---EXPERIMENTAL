package unpackaged;

import jagex.graphics.DrawingArea;

/* Class39_Sub5_Sub10_Sub2 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Class39_Sub5_Sub10_Sub2 extends DrawingArea
{
    public static int[] anIntArray2452;
    public static int anInt2453;
    public static int anInt2454;
    public static int[] anIntArray2455;
    public static int[] cosineTable = new int[2048];
    public static int anInt2457;
    public static Interface2 anInterface2_2458;
    public static boolean aBoolean2459;
    public static int[] anIntArray2460;
    public static int anInt2461;
    public static int anInt2462;
    public static int anInt2463;
    public static int anInt2464;
    public static boolean aBoolean2465;
    public static int[] anIntArray2466;
    public static int anInt2467;
    public static int anInt2468;
    public static int[] sineTable;
    public static boolean aBoolean2470;
    public static boolean aBoolean2471;
    
    public static int method649(int i, int i_0_) {
	i_0_ = (127 - i_0_) * (i & 0x7f) >> 7;
	if (i_0_ < 2)
	    i_0_ = 2;
	else if (i_0_ > 126)
	    i_0_ = 126;
	return (i & 0xff80) + i_0_;
    }
    
    public static void method650(double d) {
	method651(d, 0, 512);
    }
    
    public static void method651(double d, int i, int i_1_) {
	d += Math.random() * 0.03 - 0.015;
	int i_2_ = i * 128;
	for (int i_3_ = i; i_3_ < i_1_; i_3_++) {
	    double d_4_ = (double) (i_3_ >> 3) / 64.0 + 0.0078125;
	    double d_5_ = (double) (i_3_ & 0x7) / 8.0 + 0.0625;
	    for (int i_6_ = 0; i_6_ < 128; i_6_++) {
		double d_7_ = (double) i_6_ / 128.0;
		double d_8_ = d_7_;
		double d_9_ = d_7_;
		double d_10_ = d_7_;
		if (d_5_ != 0.0) {
		    double d_11_;
		    if (d_7_ < 0.5)
			d_11_ = d_7_ * (d_5_ + 1.0);
		    else
			d_11_ = d_7_ + d_5_ - d_7_ * d_5_;
		    double d_12_ = d_7_ * 2.0 - d_11_;
		    double d_13_ = d_4_ + 0.3333333333333333;
		    if (d_13_ > 1.0)
			d_13_--;
		    double d_14_ = d_4_;
		    double d_15_ = d_4_ - 0.3333333333333333;
		    if (d_15_ < 0.0)
			d_15_++;
		    if (d_13_ * 6.0 < 1.0)
			d_8_ = d_12_ + (d_11_ - d_12_) * 6.0 * d_13_;
		    else if (d_13_ * 2.0 < 1.0)
			d_8_ = d_11_;
		    else if (d_13_ * 3.0 < 2.0)
			d_8_ = d_12_ + ((d_11_ - d_12_)
					* (0.6666666666666666 - d_13_) * 6.0);
		    else
			d_8_ = d_12_;
		    if (d_14_ * 6.0 < 1.0)
			d_9_ = d_12_ + (d_11_ - d_12_) * 6.0 * d_14_;
		    else if (d_14_ * 2.0 < 1.0)
			d_9_ = d_11_;
		    else if (d_14_ * 3.0 < 2.0)
			d_9_ = d_12_ + ((d_11_ - d_12_)
					* (0.6666666666666666 - d_14_) * 6.0);
		    else
			d_9_ = d_12_;
		    if (d_15_ * 6.0 < 1.0)
			d_10_ = d_12_ + (d_11_ - d_12_) * 6.0 * d_15_;
		    else if (d_15_ * 2.0 < 1.0)
			d_10_ = d_11_;
		    else if (d_15_ * 3.0 < 2.0)
			d_10_ = d_12_ + ((d_11_ - d_12_)
					 * (0.6666666666666666 - d_15_) * 6.0);
		    else
			d_10_ = d_12_;
		}
		int i_16_ = (int) (d_8_ * 256.0);
		int i_17_ = (int) (d_9_ * 256.0);
		int i_18_ = (int) (d_10_ * 256.0);
		int i_19_ = (i_16_ << 16) + (i_17_ << 8) + i_18_;
		i_19_ = method663(i_19_, d);
		if (i_19_ == 0)
		    i_19_ = 1;
		anIntArray2452[i_2_++] = i_19_;
	    }
	}
    }
    
    public static void method652() {
	anIntArray2460 = null;
	anIntArray2452 = null;
	anInterface2_2458 = null;
	anIntArray2466 = null;
	anIntArray2455 = null;
	sineTable = null;
	cosineTable = null;
    }
    
    public static int[] method653(int i, int i_20_, int i_21_, int i_22_,
				  int[] is) {
	anInt2461 = i_21_ - i;
	anInt2463 = i_22_ - i_20_;
	if (is == null) {
	    int i_23_ = anInt2463;
	    if (i_23_ == 0)
		i_23_++;
	    anIntArray2460 = new int[i_23_];
	    for (int i_24_ = 0; i_24_ < i_23_; i_24_++)
		anIntArray2460[i_24_]
		    = (i_20_ + i_24_) * DrawingArea.bufferWidth + i;
	} else
	    anIntArray2460 = is;
	method654();
	return anIntArray2460;
    }
    
    public static void method654() {
	anInt2462 = anInt2461 / 2;
	anInt2467 = anInt2463 / 2;
	anInt2453 = -anInt2462;
	anInt2457 = anInt2461 - anInt2462;
	anInt2464 = -anInt2467;
	anInt2454 = anInt2463 - anInt2467;
    }
    
    public static void method655(Interface2 interface2) {
	anInterface2_2458 = interface2;
    }
    
    public static void method656(int[] is, int i, int i_25_, int i_26_,
				 int i_27_, int i_28_, int i_29_, int i_30_) {
	if (aBoolean2471) {
	    int i_31_;
	    if (aBoolean2470) {
		if (i_28_ - i_27_ > 3)
		    i_31_ = (i_30_ - i_29_) / (i_28_ - i_27_);
		else
		    i_31_ = 0;
		if (i_28_ > anInt2461)
		    i_28_ = anInt2461;
		if (i_27_ < 0) {
		    i_29_ -= i_27_ * i_31_;
		    i_27_ = 0;
		}
		if (i_27_ >= i_28_)
		    return;
		i += i_27_;
		i_26_ = i_28_ - i_27_ >> 2;
		i_31_ <<= 2;
	    } else {
		if (i_27_ >= i_28_)
		    return;
		i += i_27_;
		i_26_ = i_28_ - i_27_ >> 2;
		if (i_26_ > 0)
		    i_31_ = (i_30_ - i_29_) * anIntArray2466[i_26_] >> 15;
		else
		    i_31_ = 0;
	    }
	    if (anInt2468 == 0) {
		while (--i_26_ >= 0) {
		    i_25_ = anIntArray2452[i_29_ >> 8];
		    i_29_ += i_31_;
		    is[i++] = i_25_;
		    is[i++] = i_25_;
		    is[i++] = i_25_;
		    is[i++] = i_25_;
		}
		i_26_ = i_28_ - i_27_ & 0x3;
		if (i_26_ > 0) {
		    i_25_ = anIntArray2452[i_29_ >> 8];
		    do
			is[i++] = i_25_;
		    while (--i_26_ > 0);
		}
	    } else {
		int i_32_ = anInt2468;
		int i_33_ = 256 - anInt2468;
		while (--i_26_ >= 0) {
		    i_25_ = anIntArray2452[i_29_ >> 8];
		    i_29_ += i_31_;
		    i_25_ = (((i_25_ & 0xff00ff) * i_33_ >> 8 & 0xff00ff)
			     + ((i_25_ & 0xff00) * i_33_ >> 8 & 0xff00));
		    is[i++]
			= (i_25_ + ((is[i] & 0xff00ff) * i_32_ >> 8 & 0xff00ff)
			   + ((is[i] & 0xff00) * i_32_ >> 8 & 0xff00));
		    is[i++]
			= (i_25_ + ((is[i] & 0xff00ff) * i_32_ >> 8 & 0xff00ff)
			   + ((is[i] & 0xff00) * i_32_ >> 8 & 0xff00));
		    is[i++]
			= (i_25_ + ((is[i] & 0xff00ff) * i_32_ >> 8 & 0xff00ff)
			   + ((is[i] & 0xff00) * i_32_ >> 8 & 0xff00));
		    is[i++]
			= (i_25_ + ((is[i] & 0xff00ff) * i_32_ >> 8 & 0xff00ff)
			   + ((is[i] & 0xff00) * i_32_ >> 8 & 0xff00));
		}
		i_26_ = i_28_ - i_27_ & 0x3;
		if (i_26_ > 0) {
		    i_25_ = anIntArray2452[i_29_ >> 8];
		    i_25_ = (((i_25_ & 0xff00ff) * i_33_ >> 8 & 0xff00ff)
			     + ((i_25_ & 0xff00) * i_33_ >> 8 & 0xff00));
		    do
			is[i++]
			    = (i_25_
			       + ((is[i] & 0xff00ff) * i_32_ >> 8 & 0xff00ff)
			       + ((is[i] & 0xff00) * i_32_ >> 8 & 0xff00));
		    while (--i_26_ > 0);
		}
	    }
	} else if (i_27_ < i_28_) {
	    int i_34_ = (i_30_ - i_29_) / (i_28_ - i_27_);
	    if (aBoolean2470) {
		if (i_28_ > anInt2461)
		    i_28_ = anInt2461;
		if (i_27_ < 0) {
		    i_29_ -= i_27_ * i_34_;
		    i_27_ = 0;
		}
		if (i_27_ >= i_28_)
		    return;
	    }
	    i += i_27_;
	    i_26_ = i_28_ - i_27_;
	    if (anInt2468 == 0) {
		do {
		    is[i++] = anIntArray2452[i_29_ >> 8];
		    i_29_ += i_34_;
		} while (--i_26_ > 0);
	    } else {
		int i_35_ = anInt2468;
		int i_36_ = 256 - anInt2468;
		do {
		    i_25_ = anIntArray2452[i_29_ >> 8];
		    i_29_ += i_34_;
		    i_25_ = (((i_25_ & 0xff00ff) * i_36_ >> 8 & 0xff00ff)
			     + ((i_25_ & 0xff00) * i_36_ >> 8 & 0xff00));
		    is[i++]
			= (i_25_ + ((is[i] & 0xff00ff) * i_35_ >> 8 & 0xff00ff)
			   + ((is[i] & 0xff00) * i_35_ >> 8 & 0xff00));
		} while (--i_26_ > 0);
	    }
	}
    }
    
    public static int[] method657(int[] is) {
	return method653(DrawingArea.areaOffsetX,
			 DrawingArea.areaOffsetY,
			 DrawingArea.areaWidth,
			 DrawingArea.areaHeight, is);
    }
    
    public static void method658(int[] is, int i, int i_37_, int i_38_,
				 int i_39_, int i_40_) {
	if (aBoolean2470) {
	    if (i_40_ > anInt2461)
		i_40_ = anInt2461;
	    if (i_39_ < 0)
		i_39_ = 0;
	}
	if (i_39_ < i_40_) {
	    i += i_39_;
	    i_38_ = i_40_ - i_39_ >> 2;
	    if (anInt2468 == 0) {
		while (--i_38_ >= 0) {
		    is[i++] = i_37_;
		    is[i++] = i_37_;
		    is[i++] = i_37_;
		    is[i++] = i_37_;
		}
		i_38_ = i_40_ - i_39_ & 0x3;
		while (--i_38_ >= 0)
		    is[i++] = i_37_;
	    } else {
		int i_41_ = anInt2468;
		int i_42_ = 256 - anInt2468;
		i_37_ = (((i_37_ & 0xff00ff) * i_42_ >> 8 & 0xff00ff)
			 + ((i_37_ & 0xff00) * i_42_ >> 8 & 0xff00));
		while (--i_38_ >= 0) {
		    is[i++]
			= (i_37_ + ((is[i] & 0xff00ff) * i_41_ >> 8 & 0xff00ff)
			   + ((is[i] & 0xff00) * i_41_ >> 8 & 0xff00));
		    is[i++]
			= (i_37_ + ((is[i] & 0xff00ff) * i_41_ >> 8 & 0xff00ff)
			   + ((is[i] & 0xff00) * i_41_ >> 8 & 0xff00));
		    is[i++]
			= (i_37_ + ((is[i] & 0xff00ff) * i_41_ >> 8 & 0xff00ff)
			   + ((is[i] & 0xff00) * i_41_ >> 8 & 0xff00));
		    is[i++]
			= (i_37_ + ((is[i] & 0xff00ff) * i_41_ >> 8 & 0xff00ff)
			   + ((is[i] & 0xff00) * i_41_ >> 8 & 0xff00));
		}
		i_38_ = i_40_ - i_39_ & 0x3;
		while (--i_38_ >= 0)
		    is[i++]
			= (i_37_ + ((is[i] & 0xff00ff) * i_41_ >> 8 & 0xff00ff)
			   + ((is[i] & 0xff00) * i_41_ >> 8 & 0xff00));
	    }
	}
    }
    
    public static void method659(int i, int i_43_) {
	int i_44_ = anIntArray2460[0];
	int i_45_ = i_44_ / DrawingArea.bufferWidth;
	int i_46_ = i_44_ - i_45_ * DrawingArea.bufferWidth;
	anInt2462 = i - i_46_;
	anInt2467 = i_43_ - i_45_;
	anInt2453 = -anInt2462;
	anInt2457 = anInt2461 - anInt2462;
	anInt2464 = -anInt2467;
	anInt2454 = anInt2463 - anInt2467;
    }
    
    public static void method660(int i, int i_47_, int i_48_, int i_49_,
				 int i_50_, int i_51_, int i_52_, int i_53_,
				 int i_54_) {
	if (Gpu.capturing) {                               // GPU rendering: recorded, drawn by OpenGL at the end of the world
	    Gpu.gouraud(i, i_47_, i_48_, i_49_, i_50_, i_51_, i_52_, i_53_, i_54_);
	    return;
	}
	int i_55_ = 0;
	int i_56_ = 0;
	if (i_47_ != i) {
	    i_55_ = (i_50_ - i_49_ << 16) / (i_47_ - i);
	    i_56_ = (i_53_ - i_52_ << 15) / (i_47_ - i);
	}
	int i_57_ = 0;
	int i_58_ = 0;
	if (i_48_ != i_47_) {
	    i_57_ = (i_51_ - i_50_ << 16) / (i_48_ - i_47_);
	    i_58_ = (i_54_ - i_53_ << 15) / (i_48_ - i_47_);
	}
	int i_59_ = 0;
	int i_60_ = 0;
	if (i_48_ != i) {
	    i_59_ = (i_49_ - i_51_ << 16) / (i - i_48_);
	    i_60_ = (i_52_ - i_54_ << 15) / (i - i_48_);
	}
	if (i <= i_47_ && i <= i_48_) {
	    if (i < anInt2463) {
		if (i_47_ > anInt2463)
		    i_47_ = anInt2463;
		if (i_48_ > anInt2463)
		    i_48_ = anInt2463;
		if (i_47_ < i_48_) {
		    i_51_ = i_49_ <<= 16;
		    i_54_ = i_52_ <<= 15;
		    if (i < 0) {
			i_51_ -= i_59_ * i;
			i_49_ -= i_55_ * i;
			i_54_ -= i_60_ * i;
			i_52_ -= i_56_ * i;
			i = 0;
		    }
		    i_50_ <<= 16;
		    i_53_ <<= 15;
		    if (i_47_ < 0) {
			i_50_ -= i_57_ * i_47_;
			i_53_ -= i_58_ * i_47_;
			i_47_ = 0;
		    }
		    if (i != i_47_ && i_59_ < i_55_
			|| i == i_47_ && i_59_ > i_57_) {
			i_48_ -= i_47_;
			i_47_ -= i;
			i = anIntArray2460[i];
			while (--i_47_ >= 0) {
			    method656(DrawingArea.buffer, i, 0,
				      0, i_51_ >> 16, i_49_ >> 16, i_54_ >> 7,
				      i_52_ >> 7);
			    i_51_ += i_59_;
			    i_49_ += i_55_;
			    i_54_ += i_60_;
			    i_52_ += i_56_;
			    i += DrawingArea.bufferWidth;
			}
			while (--i_48_ >= 0) {
			    method656(DrawingArea.buffer, i, 0,
				      0, i_51_ >> 16, i_50_ >> 16, i_54_ >> 7,
				      i_53_ >> 7);
			    i_51_ += i_59_;
			    i_50_ += i_57_;
			    i_54_ += i_60_;
			    i_53_ += i_58_;
			    i += DrawingArea.bufferWidth;
			}
		    } else {
			i_48_ -= i_47_;
			i_47_ -= i;
			i = anIntArray2460[i];
			while (--i_47_ >= 0) {
			    method656(DrawingArea.buffer, i, 0,
				      0, i_49_ >> 16, i_51_ >> 16, i_52_ >> 7,
				      i_54_ >> 7);
			    i_51_ += i_59_;
			    i_49_ += i_55_;
			    i_54_ += i_60_;
			    i_52_ += i_56_;
			    i += DrawingArea.bufferWidth;
			}
			while (--i_48_ >= 0) {
			    method656(DrawingArea.buffer, i, 0,
				      0, i_50_ >> 16, i_51_ >> 16, i_53_ >> 7,
				      i_54_ >> 7);
			    i_51_ += i_59_;
			    i_50_ += i_57_;
			    i_54_ += i_60_;
			    i_53_ += i_58_;
			    i += DrawingArea.bufferWidth;
			}
		    }
		} else {
		    i_50_ = i_49_ <<= 16;
		    i_53_ = i_52_ <<= 15;
		    if (i < 0) {
			i_50_ -= i_59_ * i;
			i_49_ -= i_55_ * i;
			i_53_ -= i_60_ * i;
			i_52_ -= i_56_ * i;
			i = 0;
		    }
		    i_51_ <<= 16;
		    i_54_ <<= 15;
		    if (i_48_ < 0) {
			i_51_ -= i_57_ * i_48_;
			i_54_ -= i_58_ * i_48_;
			i_48_ = 0;
		    }
		    if (i != i_48_ && i_59_ < i_55_
			|| i == i_48_ && i_57_ > i_55_) {
			i_47_ -= i_48_;
			i_48_ -= i;
			i = anIntArray2460[i];
			while (--i_48_ >= 0) {
			    method656(DrawingArea.buffer, i, 0,
				      0, i_50_ >> 16, i_49_ >> 16, i_53_ >> 7,
				      i_52_ >> 7);
			    i_50_ += i_59_;
			    i_49_ += i_55_;
			    i_53_ += i_60_;
			    i_52_ += i_56_;
			    i += DrawingArea.bufferWidth;
			}
			while (--i_47_ >= 0) {
			    method656(DrawingArea.buffer, i, 0,
				      0, i_51_ >> 16, i_49_ >> 16, i_54_ >> 7,
				      i_52_ >> 7);
			    i_51_ += i_57_;
			    i_49_ += i_55_;
			    i_54_ += i_58_;
			    i_52_ += i_56_;
			    i += DrawingArea.bufferWidth;
			}
		    } else {
			i_47_ -= i_48_;
			i_48_ -= i;
			i = anIntArray2460[i];
			while (--i_48_ >= 0) {
			    method656(DrawingArea.buffer, i, 0,
				      0, i_49_ >> 16, i_50_ >> 16, i_52_ >> 7,
				      i_53_ >> 7);
			    i_50_ += i_59_;
			    i_49_ += i_55_;
			    i_53_ += i_60_;
			    i_52_ += i_56_;
			    i += DrawingArea.bufferWidth;
			}
			while (--i_47_ >= 0) {
			    method656(DrawingArea.buffer, i, 0,
				      0, i_49_ >> 16, i_51_ >> 16, i_52_ >> 7,
				      i_54_ >> 7);
			    i_51_ += i_57_;
			    i_49_ += i_55_;
			    i_54_ += i_58_;
			    i_52_ += i_56_;
			    i += DrawingArea.bufferWidth;
			}
		    }
		}
	    }
	} else if (i_47_ <= i_48_) {
	    if (i_47_ < anInt2463) {
		if (i_48_ > anInt2463)
		    i_48_ = anInt2463;
		if (i > anInt2463)
		    i = anInt2463;
		if (i_48_ < i) {
		    i_49_ = i_50_ <<= 16;
		    i_52_ = i_53_ <<= 15;
		    if (i_47_ < 0) {
			i_49_ -= i_55_ * i_47_;
			i_50_ -= i_57_ * i_47_;
			i_52_ -= i_56_ * i_47_;
			i_53_ -= i_58_ * i_47_;
			i_47_ = 0;
		    }
		    i_51_ <<= 16;
		    i_54_ <<= 15;
		    if (i_48_ < 0) {
			i_51_ -= i_59_ * i_48_;
			i_54_ -= i_60_ * i_48_;
			i_48_ = 0;
		    }
		    if (i_47_ != i_48_ && i_55_ < i_57_
			|| i_47_ == i_48_ && i_55_ > i_59_) {
			i -= i_48_;
			i_48_ -= i_47_;
			i_47_ = anIntArray2460[i_47_];
			while (--i_48_ >= 0) {
			    method656(DrawingArea.buffer, i_47_,
				      0, 0, i_49_ >> 16, i_50_ >> 16,
				      i_52_ >> 7, i_53_ >> 7);
			    i_49_ += i_55_;
			    i_50_ += i_57_;
			    i_52_ += i_56_;
			    i_53_ += i_58_;
			    i_47_ += DrawingArea.bufferWidth;
			}
			while (--i >= 0) {
			    method656(DrawingArea.buffer, i_47_,
				      0, 0, i_49_ >> 16, i_51_ >> 16,
				      i_52_ >> 7, i_54_ >> 7);
			    i_49_ += i_55_;
			    i_51_ += i_59_;
			    i_52_ += i_56_;
			    i_54_ += i_60_;
			    i_47_ += DrawingArea.bufferWidth;
			}
		    } else {
			i -= i_48_;
			i_48_ -= i_47_;
			i_47_ = anIntArray2460[i_47_];
			while (--i_48_ >= 0) {
			    method656(DrawingArea.buffer, i_47_,
				      0, 0, i_50_ >> 16, i_49_ >> 16,
				      i_53_ >> 7, i_52_ >> 7);
			    i_49_ += i_55_;
			    i_50_ += i_57_;
			    i_52_ += i_56_;
			    i_53_ += i_58_;
			    i_47_ += DrawingArea.bufferWidth;
			}
			while (--i >= 0) {
			    method656(DrawingArea.buffer, i_47_,
				      0, 0, i_51_ >> 16, i_49_ >> 16,
				      i_54_ >> 7, i_52_ >> 7);
			    i_49_ += i_55_;
			    i_51_ += i_59_;
			    i_52_ += i_56_;
			    i_54_ += i_60_;
			    i_47_ += DrawingArea.bufferWidth;
			}
		    }
		} else {
		    i_51_ = i_50_ <<= 16;
		    i_54_ = i_53_ <<= 15;
		    if (i_47_ < 0) {
			i_51_ -= i_55_ * i_47_;
			i_50_ -= i_57_ * i_47_;
			i_54_ -= i_56_ * i_47_;
			i_53_ -= i_58_ * i_47_;
			i_47_ = 0;
		    }
		    i_49_ <<= 16;
		    i_52_ <<= 15;
		    if (i < 0) {
			i_49_ -= i_59_ * i;
			i_52_ -= i_60_ * i;
			i = 0;
		    }
		    if (i_55_ < i_57_) {
			i_48_ -= i;
			i -= i_47_;
			i_47_ = anIntArray2460[i_47_];
			while (--i >= 0) {
			    method656(DrawingArea.buffer, i_47_,
				      0, 0, i_51_ >> 16, i_50_ >> 16,
				      i_54_ >> 7, i_53_ >> 7);
			    i_51_ += i_55_;
			    i_50_ += i_57_;
			    i_54_ += i_56_;
			    i_53_ += i_58_;
			    i_47_ += DrawingArea.bufferWidth;
			}
			while (--i_48_ >= 0) {
			    method656(DrawingArea.buffer, i_47_,
				      0, 0, i_49_ >> 16, i_50_ >> 16,
				      i_52_ >> 7, i_53_ >> 7);
			    i_49_ += i_59_;
			    i_50_ += i_57_;
			    i_52_ += i_60_;
			    i_53_ += i_58_;
			    i_47_ += DrawingArea.bufferWidth;
			}
		    } else {
			i_48_ -= i;
			i -= i_47_;
			i_47_ = anIntArray2460[i_47_];
			while (--i >= 0) {
			    method656(DrawingArea.buffer, i_47_,
				      0, 0, i_50_ >> 16, i_51_ >> 16,
				      i_53_ >> 7, i_54_ >> 7);
			    i_51_ += i_55_;
			    i_50_ += i_57_;
			    i_54_ += i_56_;
			    i_53_ += i_58_;
			    i_47_ += DrawingArea.bufferWidth;
			}
			while (--i_48_ >= 0) {
			    method656(DrawingArea.buffer, i_47_,
				      0, 0, i_50_ >> 16, i_49_ >> 16,
				      i_53_ >> 7, i_52_ >> 7);
			    i_49_ += i_59_;
			    i_50_ += i_57_;
			    i_52_ += i_60_;
			    i_53_ += i_58_;
			    i_47_ += DrawingArea.bufferWidth;
			}
		    }
		}
	    }
	} else if (i_48_ < anInt2463) {
	    if (i > anInt2463)
		i = anInt2463;
	    if (i_47_ > anInt2463)
		i_47_ = anInt2463;
	    if (i < i_47_) {
		i_50_ = i_51_ <<= 16;
		i_53_ = i_54_ <<= 15;
		if (i_48_ < 0) {
		    i_50_ -= i_57_ * i_48_;
		    i_51_ -= i_59_ * i_48_;
		    i_53_ -= i_58_ * i_48_;
		    i_54_ -= i_60_ * i_48_;
		    i_48_ = 0;
		}
		i_49_ <<= 16;
		i_52_ <<= 15;
		if (i < 0) {
		    i_49_ -= i_55_ * i;
		    i_52_ -= i_56_ * i;
		    i = 0;
		}
		if (i_57_ < i_59_) {
		    i_47_ -= i;
		    i -= i_48_;
		    i_48_ = anIntArray2460[i_48_];
		    while (--i >= 0) {
			method656(DrawingArea.buffer, i_48_, 0,
				  0, i_50_ >> 16, i_51_ >> 16, i_53_ >> 7,
				  i_54_ >> 7);
			i_50_ += i_57_;
			i_51_ += i_59_;
			i_53_ += i_58_;
			i_54_ += i_60_;
			i_48_ += DrawingArea.bufferWidth;
		    }
		    while (--i_47_ >= 0) {
			method656(DrawingArea.buffer, i_48_, 0,
				  0, i_50_ >> 16, i_49_ >> 16, i_53_ >> 7,
				  i_52_ >> 7);
			i_50_ += i_57_;
			i_49_ += i_55_;
			i_53_ += i_58_;
			i_52_ += i_56_;
			i_48_ += DrawingArea.bufferWidth;
		    }
		} else {
		    i_47_ -= i;
		    i -= i_48_;
		    i_48_ = anIntArray2460[i_48_];
		    while (--i >= 0) {
			method656(DrawingArea.buffer, i_48_, 0,
				  0, i_51_ >> 16, i_50_ >> 16, i_54_ >> 7,
				  i_53_ >> 7);
			i_50_ += i_57_;
			i_51_ += i_59_;
			i_53_ += i_58_;
			i_54_ += i_60_;
			i_48_ += DrawingArea.bufferWidth;
		    }
		    while (--i_47_ >= 0) {
			method656(DrawingArea.buffer, i_48_, 0,
				  0, i_49_ >> 16, i_50_ >> 16, i_52_ >> 7,
				  i_53_ >> 7);
			i_50_ += i_57_;
			i_49_ += i_55_;
			i_53_ += i_58_;
			i_52_ += i_56_;
			i_48_ += DrawingArea.bufferWidth;
		    }
		}
	    } else {
		i_49_ = i_51_ <<= 16;
		i_52_ = i_54_ <<= 15;
		if (i_48_ < 0) {
		    i_49_ -= i_57_ * i_48_;
		    i_51_ -= i_59_ * i_48_;
		    i_52_ -= i_58_ * i_48_;
		    i_54_ -= i_60_ * i_48_;
		    i_48_ = 0;
		}
		i_50_ <<= 16;
		i_53_ <<= 15;
		if (i_47_ < 0) {
		    i_50_ -= i_55_ * i_47_;
		    i_53_ -= i_56_ * i_47_;
		    i_47_ = 0;
		}
		if (i_57_ < i_59_) {
		    i -= i_47_;
		    i_47_ -= i_48_;
		    i_48_ = anIntArray2460[i_48_];
		    while (--i_47_ >= 0) {
			method656(DrawingArea.buffer, i_48_, 0,
				  0, i_49_ >> 16, i_51_ >> 16, i_52_ >> 7,
				  i_54_ >> 7);
			i_49_ += i_57_;
			i_51_ += i_59_;
			i_52_ += i_58_;
			i_54_ += i_60_;
			i_48_ += DrawingArea.bufferWidth;
		    }
		    while (--i >= 0) {
			method656(DrawingArea.buffer, i_48_, 0,
				  0, i_50_ >> 16, i_51_ >> 16, i_53_ >> 7,
				  i_54_ >> 7);
			i_50_ += i_55_;
			i_51_ += i_59_;
			i_53_ += i_56_;
			i_54_ += i_60_;
			i_48_ += DrawingArea.bufferWidth;
		    }
		} else {
		    i -= i_47_;
		    i_47_ -= i_48_;
		    i_48_ = anIntArray2460[i_48_];
		    while (--i_47_ >= 0) {
			method656(DrawingArea.buffer, i_48_, 0,
				  0, i_51_ >> 16, i_49_ >> 16, i_54_ >> 7,
				  i_52_ >> 7);
			i_49_ += i_57_;
			i_51_ += i_59_;
			i_52_ += i_58_;
			i_54_ += i_60_;
			i_48_ += DrawingArea.bufferWidth;
		    }
		    while (--i >= 0) {
			method656(DrawingArea.buffer, i_48_, 0,
				  0, i_51_ >> 16, i_50_ >> 16, i_54_ >> 7,
				  i_53_ >> 7);
			i_50_ += i_55_;
			i_51_ += i_59_;
			i_53_ += i_56_;
			i_54_ += i_60_;
			i_48_ += DrawingArea.bufferWidth;
		    }
		}
	    }
	}
    }
    
    public static int[] method661() {
	return anIntArray2460;
    }
    
    public static void method662(int i, int i_61_, int i_62_, int i_63_,
				 int i_64_, int i_65_, int i_66_) {
	if (Gpu.capturing) {
	    Gpu.flat(i, i_61_, i_62_, i_63_, i_64_, i_65_, i_66_);
	    return;
	}
	int i_67_ = 0;
	if (i_61_ != i)
	    i_67_ = (i_64_ - i_63_ << 16) / (i_61_ - i);
	int i_68_ = 0;
	if (i_62_ != i_61_)
	    i_68_ = (i_65_ - i_64_ << 16) / (i_62_ - i_61_);
	int i_69_ = 0;
	if (i_62_ != i)
	    i_69_ = (i_63_ - i_65_ << 16) / (i - i_62_);
	if (i <= i_61_ && i <= i_62_) {
	    if (i < anInt2463) {
		if (i_61_ > anInt2463)
		    i_61_ = anInt2463;
		if (i_62_ > anInt2463)
		    i_62_ = anInt2463;
		if (i_61_ < i_62_) {
		    i_65_ = i_63_ <<= 16;
		    if (i < 0) {
			i_65_ -= i_69_ * i;
			i_63_ -= i_67_ * i;
			i = 0;
		    }
		    i_64_ <<= 16;
		    if (i_61_ < 0) {
			i_64_ -= i_68_ * i_61_;
			i_61_ = 0;
		    }
		    if (i != i_61_ && i_69_ < i_67_
			|| i == i_61_ && i_69_ > i_68_) {
			i_62_ -= i_61_;
			i_61_ -= i;
			i = anIntArray2460[i];
			while (--i_61_ >= 0) {
			    method658(DrawingArea.buffer, i,
				      i_66_, 0, i_65_ >> 16, i_63_ >> 16);
			    i_65_ += i_69_;
			    i_63_ += i_67_;
			    i += DrawingArea.bufferWidth;
			}
			while (--i_62_ >= 0) {
			    method658(DrawingArea.buffer, i,
				      i_66_, 0, i_65_ >> 16, i_64_ >> 16);
			    i_65_ += i_69_;
			    i_64_ += i_68_;
			    i += DrawingArea.bufferWidth;
			}
		    } else {
			i_62_ -= i_61_;
			i_61_ -= i;
			i = anIntArray2460[i];
			while (--i_61_ >= 0) {
			    method658(DrawingArea.buffer, i,
				      i_66_, 0, i_63_ >> 16, i_65_ >> 16);
			    i_65_ += i_69_;
			    i_63_ += i_67_;
			    i += DrawingArea.bufferWidth;
			}
			while (--i_62_ >= 0) {
			    method658(DrawingArea.buffer, i,
				      i_66_, 0, i_64_ >> 16, i_65_ >> 16);
			    i_65_ += i_69_;
			    i_64_ += i_68_;
			    i += DrawingArea.bufferWidth;
			}
		    }
		} else {
		    i_64_ = i_63_ <<= 16;
		    if (i < 0) {
			i_64_ -= i_69_ * i;
			i_63_ -= i_67_ * i;
			i = 0;
		    }
		    i_65_ <<= 16;
		    if (i_62_ < 0) {
			i_65_ -= i_68_ * i_62_;
			i_62_ = 0;
		    }
		    if (i != i_62_ && i_69_ < i_67_
			|| i == i_62_ && i_68_ > i_67_) {
			i_61_ -= i_62_;
			i_62_ -= i;
			i = anIntArray2460[i];
			while (--i_62_ >= 0) {
			    method658(DrawingArea.buffer, i,
				      i_66_, 0, i_64_ >> 16, i_63_ >> 16);
			    i_64_ += i_69_;
			    i_63_ += i_67_;
			    i += DrawingArea.bufferWidth;
			}
			while (--i_61_ >= 0) {
			    method658(DrawingArea.buffer, i,
				      i_66_, 0, i_65_ >> 16, i_63_ >> 16);
			    i_65_ += i_68_;
			    i_63_ += i_67_;
			    i += DrawingArea.bufferWidth;
			}
		    } else {
			i_61_ -= i_62_;
			i_62_ -= i;
			i = anIntArray2460[i];
			while (--i_62_ >= 0) {
			    method658(DrawingArea.buffer, i,
				      i_66_, 0, i_63_ >> 16, i_64_ >> 16);
			    i_64_ += i_69_;
			    i_63_ += i_67_;
			    i += DrawingArea.bufferWidth;
			}
			while (--i_61_ >= 0) {
			    method658(DrawingArea.buffer, i,
				      i_66_, 0, i_63_ >> 16, i_65_ >> 16);
			    i_65_ += i_68_;
			    i_63_ += i_67_;
			    i += DrawingArea.bufferWidth;
			}
		    }
		}
	    }
	} else if (i_61_ <= i_62_) {
	    if (i_61_ < anInt2463) {
		if (i_62_ > anInt2463)
		    i_62_ = anInt2463;
		if (i > anInt2463)
		    i = anInt2463;
		if (i_62_ < i) {
		    i_63_ = i_64_ <<= 16;
		    if (i_61_ < 0) {
			i_63_ -= i_67_ * i_61_;
			i_64_ -= i_68_ * i_61_;
			i_61_ = 0;
		    }
		    i_65_ <<= 16;
		    if (i_62_ < 0) {
			i_65_ -= i_69_ * i_62_;
			i_62_ = 0;
		    }
		    if (i_61_ != i_62_ && i_67_ < i_68_
			|| i_61_ == i_62_ && i_67_ > i_69_) {
			i -= i_62_;
			i_62_ -= i_61_;
			i_61_ = anIntArray2460[i_61_];
			while (--i_62_ >= 0) {
			    method658(DrawingArea.buffer, i_61_,
				      i_66_, 0, i_63_ >> 16, i_64_ >> 16);
			    i_63_ += i_67_;
			    i_64_ += i_68_;
			    i_61_ += DrawingArea.bufferWidth;
			}
			while (--i >= 0) {
			    method658(DrawingArea.buffer, i_61_,
				      i_66_, 0, i_63_ >> 16, i_65_ >> 16);
			    i_63_ += i_67_;
			    i_65_ += i_69_;
			    i_61_ += DrawingArea.bufferWidth;
			}
		    } else {
			i -= i_62_;
			i_62_ -= i_61_;
			i_61_ = anIntArray2460[i_61_];
			while (--i_62_ >= 0) {
			    method658(DrawingArea.buffer, i_61_,
				      i_66_, 0, i_64_ >> 16, i_63_ >> 16);
			    i_63_ += i_67_;
			    i_64_ += i_68_;
			    i_61_ += DrawingArea.bufferWidth;
			}
			while (--i >= 0) {
			    method658(DrawingArea.buffer, i_61_,
				      i_66_, 0, i_65_ >> 16, i_63_ >> 16);
			    i_63_ += i_67_;
			    i_65_ += i_69_;
			    i_61_ += DrawingArea.bufferWidth;
			}
		    }
		} else {
		    i_65_ = i_64_ <<= 16;
		    if (i_61_ < 0) {
			i_65_ -= i_67_ * i_61_;
			i_64_ -= i_68_ * i_61_;
			i_61_ = 0;
		    }
		    i_63_ <<= 16;
		    if (i < 0) {
			i_63_ -= i_69_ * i;
			i = 0;
		    }
		    if (i_67_ < i_68_) {
			i_62_ -= i;
			i -= i_61_;
			i_61_ = anIntArray2460[i_61_];
			while (--i >= 0) {
			    method658(DrawingArea.buffer, i_61_,
				      i_66_, 0, i_65_ >> 16, i_64_ >> 16);
			    i_65_ += i_67_;
			    i_64_ += i_68_;
			    i_61_ += DrawingArea.bufferWidth;
			}
			while (--i_62_ >= 0) {
			    method658(DrawingArea.buffer, i_61_,
				      i_66_, 0, i_63_ >> 16, i_64_ >> 16);
			    i_63_ += i_69_;
			    i_64_ += i_68_;
			    i_61_ += DrawingArea.bufferWidth;
			}
		    } else {
			i_62_ -= i;
			i -= i_61_;
			i_61_ = anIntArray2460[i_61_];
			while (--i >= 0) {
			    method658(DrawingArea.buffer, i_61_,
				      i_66_, 0, i_64_ >> 16, i_65_ >> 16);
			    i_65_ += i_67_;
			    i_64_ += i_68_;
			    i_61_ += DrawingArea.bufferWidth;
			}
			while (--i_62_ >= 0) {
			    method658(DrawingArea.buffer, i_61_,
				      i_66_, 0, i_64_ >> 16, i_63_ >> 16);
			    i_63_ += i_69_;
			    i_64_ += i_68_;
			    i_61_ += DrawingArea.bufferWidth;
			}
		    }
		}
	    }
	} else if (i_62_ < anInt2463) {
	    if (i > anInt2463)
		i = anInt2463;
	    if (i_61_ > anInt2463)
		i_61_ = anInt2463;
	    if (i < i_61_) {
		i_64_ = i_65_ <<= 16;
		if (i_62_ < 0) {
		    i_64_ -= i_68_ * i_62_;
		    i_65_ -= i_69_ * i_62_;
		    i_62_ = 0;
		}
		i_63_ <<= 16;
		if (i < 0) {
		    i_63_ -= i_67_ * i;
		    i = 0;
		}
		if (i_68_ < i_69_) {
		    i_61_ -= i;
		    i -= i_62_;
		    i_62_ = anIntArray2460[i_62_];
		    while (--i >= 0) {
			method658(DrawingArea.buffer, i_62_,
				  i_66_, 0, i_64_ >> 16, i_65_ >> 16);
			i_64_ += i_68_;
			i_65_ += i_69_;
			i_62_ += DrawingArea.bufferWidth;
		    }
		    while (--i_61_ >= 0) {
			method658(DrawingArea.buffer, i_62_,
				  i_66_, 0, i_64_ >> 16, i_63_ >> 16);
			i_64_ += i_68_;
			i_63_ += i_67_;
			i_62_ += DrawingArea.bufferWidth;
		    }
		} else {
		    i_61_ -= i;
		    i -= i_62_;
		    i_62_ = anIntArray2460[i_62_];
		    while (--i >= 0) {
			method658(DrawingArea.buffer, i_62_,
				  i_66_, 0, i_65_ >> 16, i_64_ >> 16);
			i_64_ += i_68_;
			i_65_ += i_69_;
			i_62_ += DrawingArea.bufferWidth;
		    }
		    while (--i_61_ >= 0) {
			method658(DrawingArea.buffer, i_62_,
				  i_66_, 0, i_63_ >> 16, i_64_ >> 16);
			i_64_ += i_68_;
			i_63_ += i_67_;
			i_62_ += DrawingArea.bufferWidth;
		    }
		}
	    } else {
		i_63_ = i_65_ <<= 16;
		if (i_62_ < 0) {
		    i_63_ -= i_68_ * i_62_;
		    i_65_ -= i_69_ * i_62_;
		    i_62_ = 0;
		}
		i_64_ <<= 16;
		if (i_61_ < 0) {
		    i_64_ -= i_67_ * i_61_;
		    i_61_ = 0;
		}
		if (i_68_ < i_69_) {
		    i -= i_61_;
		    i_61_ -= i_62_;
		    i_62_ = anIntArray2460[i_62_];
		    while (--i_61_ >= 0) {
			method658(DrawingArea.buffer, i_62_,
				  i_66_, 0, i_63_ >> 16, i_65_ >> 16);
			i_63_ += i_68_;
			i_65_ += i_69_;
			i_62_ += DrawingArea.bufferWidth;
		    }
		    while (--i >= 0) {
			method658(DrawingArea.buffer, i_62_,
				  i_66_, 0, i_64_ >> 16, i_65_ >> 16);
			i_64_ += i_67_;
			i_65_ += i_69_;
			i_62_ += DrawingArea.bufferWidth;
		    }
		} else {
		    i -= i_61_;
		    i_61_ -= i_62_;
		    i_62_ = anIntArray2460[i_62_];
		    while (--i_61_ >= 0) {
			method658(DrawingArea.buffer, i_62_,
				  i_66_, 0, i_65_ >> 16, i_63_ >> 16);
			i_63_ += i_68_;
			i_65_ += i_69_;
			i_62_ += DrawingArea.bufferWidth;
		    }
		    while (--i >= 0) {
			method658(DrawingArea.buffer, i_62_,
				  i_66_, 0, i_65_ >> 16, i_64_ >> 16);
			i_64_ += i_67_;
			i_65_ += i_69_;
			i_62_ += DrawingArea.bufferWidth;
		    }
		}
	    }
	}
    }
    
    public static int method663(int i, double d) {
	double d_70_ = (double) (i >> 16) / 256.0;
	double d_71_ = (double) (i >> 8 & 0xff) / 256.0;
	double d_72_ = (double) (i & 0xff) / 256.0;
	d_70_ = Math.pow(d_70_, d);
	d_71_ = Math.pow(d_71_, d);
	d_72_ = Math.pow(d_72_, d);
	int i_73_ = (int) (d_70_ * 256.0);
	int i_74_ = (int) (d_71_ * 256.0);
	int i_75_ = (int) (d_72_ * 256.0);
	return (i_73_ << 16) + (i_74_ << 8) + i_75_;
    }
    
    public static void method664(int[] is, int[] is_76_, int i, int i_77_,
				 int i_78_, int i_79_, int i_80_, int i_81_,
				 int i_82_, int i_83_, int i_84_, int i_85_,
				 int i_86_, int i_87_, int i_88_) {
	if (i_79_ < i_80_) {
	    int i_89_;
	    int i_90_;
	    if (aBoolean2470) {
		i_90_ = (i_82_ - i_81_) / (i_80_ - i_79_);
		if (i_80_ > anInt2461)
		    i_80_ = anInt2461;
		if (i_79_ < 0) {
		    i_81_ -= i_79_ * i_90_;
		    i_79_ = 0;
		}
		if (i_79_ >= i_80_)
		    return;
		i_89_ = i_80_ - i_79_ >> 3;
		i_90_ <<= 12;
		i_81_ <<= 9;
	    } else {
		if (i_80_ - i_79_ > 7) {
		    i_89_ = i_80_ - i_79_ >> 3;
		    i_90_ = (i_82_ - i_81_) * anIntArray2466[i_89_] >> 6;
		} else {
		    i_89_ = 0;
		    i_90_ = 0;
		}
		i_81_ <<= 9;
	    }
	    i_78_ += i_79_;
	    if (aBoolean2465) {
		int i_91_ = 0;
		int i_92_ = 0;
		int i_93_ = i_79_ - anInt2462;
		i_83_ += (i_86_ >> 3) * i_93_;
		i_84_ += (i_87_ >> 3) * i_93_;
		i_85_ += (i_88_ >> 3) * i_93_;
		int i_94_ = i_85_ >> 12;
		if (i_94_ != 0) {
		    i = i_83_ / i_94_;
		    i_77_ = i_84_ / i_94_;
		    if (i < 0)
			i = 0;
		    else if (i > 4032)
			i = 4032;
		}
		i_83_ += i_86_;
		i_84_ += i_87_;
		i_85_ += i_88_;
		i_94_ = i_85_ >> 12;
		if (i_94_ != 0) {
		    i_91_ = i_83_ / i_94_;
		    i_92_ = i_84_ / i_94_;
		    if (i_91_ < 7)
			i_91_ = 7;
		    else if (i_91_ > 4032)
			i_91_ = 4032;
		}
		int i_95_ = i_91_ - i >> 3;
		int i_96_ = i_92_ - i_77_ >> 3;
		i += (i_81_ & 0x600000) >> 3;
		int i_97_ = i_81_ >> 23;
		if (aBoolean2459) {
		    while (i_89_-- > 0) {
			is[i_78_++]
			    = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_;
			i += i_95_;
			i_77_ += i_96_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_;
			i += i_95_;
			i_77_ += i_96_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_;
			i += i_95_;
			i_77_ += i_96_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_;
			i += i_95_;
			i_77_ += i_96_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_;
			i += i_95_;
			i_77_ += i_96_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_;
			i += i_95_;
			i_77_ += i_96_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_;
			i += i_95_;
			i_77_ += i_96_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_;
			i = i_91_;
			i_77_ = i_92_;
			i_83_ += i_86_;
			i_84_ += i_87_;
			i_85_ += i_88_;
			i_94_ = i_85_ >> 12;
			if (i_94_ != 0) {
			    i_91_ = i_83_ / i_94_;
			    i_92_ = i_84_ / i_94_;
			    if (i_91_ < 7)
				i_91_ = 7;
			    else if (i_91_ > 4032)
				i_91_ = 4032;
			}
			i_95_ = i_91_ - i >> 3;
			i_96_ = i_92_ - i_77_ >> 3;
			i_81_ += i_90_;
			i += (i_81_ & 0x600000) >> 3;
			i_97_ = i_81_ >> 23;
		    }
		    i_89_ = i_80_ - i_79_ & 0x7;
		    while (i_89_-- > 0) {
			is[i_78_++]
			    = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_;
			i += i_95_;
			i_77_ += i_96_;
		    }
		} else {
		    while (i_89_-- > 0) {
			int i_98_;
			if ((i_98_
			     = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_)
			    != 0)
			    is[i_78_] = i_98_;
			i_78_++;
			i += i_95_;
			i_77_ += i_96_;
			if ((i_98_
			     = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_)
			    != 0)
			    is[i_78_] = i_98_;
			i_78_++;
			i += i_95_;
			i_77_ += i_96_;
			if ((i_98_
			     = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_)
			    != 0)
			    is[i_78_] = i_98_;
			i_78_++;
			i += i_95_;
			i_77_ += i_96_;
			if ((i_98_
			     = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_)
			    != 0)
			    is[i_78_] = i_98_;
			i_78_++;
			i += i_95_;
			i_77_ += i_96_;
			if ((i_98_
			     = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_)
			    != 0)
			    is[i_78_] = i_98_;
			i_78_++;
			i += i_95_;
			i_77_ += i_96_;
			if ((i_98_
			     = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_)
			    != 0)
			    is[i_78_] = i_98_;
			i_78_++;
			i += i_95_;
			i_77_ += i_96_;
			if ((i_98_
			     = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_)
			    != 0)
			    is[i_78_] = i_98_;
			i_78_++;
			i += i_95_;
			i_77_ += i_96_;
			if ((i_98_
			     = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_)
			    != 0)
			    is[i_78_] = i_98_;
			i_78_++;
			i = i_91_;
			i_77_ = i_92_;
			i_83_ += i_86_;
			i_84_ += i_87_;
			i_85_ += i_88_;
			i_94_ = i_85_ >> 12;
			if (i_94_ != 0) {
			    i_91_ = i_83_ / i_94_;
			    i_92_ = i_84_ / i_94_;
			    if (i_91_ < 7)
				i_91_ = 7;
			    else if (i_91_ > 4032)
				i_91_ = 4032;
			}
			i_95_ = i_91_ - i >> 3;
			i_96_ = i_92_ - i_77_ >> 3;
			i_81_ += i_90_;
			i += (i_81_ & 0x600000) >> 3;
			i_97_ = i_81_ >> 23;
		    }
		    i_89_ = i_80_ - i_79_ & 0x7;
		    while (i_89_-- > 0) {
			int i_99_;
			if ((i_99_
			     = is_76_[(i_77_ & 0xfc0) + (i >> 6)] >>> i_97_)
			    != 0)
			    is[i_78_] = i_99_;
			i_78_++;
			i += i_95_;
			i_77_ += i_96_;
		    }
		}
	    } else {
		int i_100_ = 0;
		int i_101_ = 0;
		int i_102_ = i_79_ - anInt2462;
		i_83_ += (i_86_ >> 3) * i_102_;
		i_84_ += (i_87_ >> 3) * i_102_;
		i_85_ += (i_88_ >> 3) * i_102_;
		int i_103_ = i_85_ >> 14;
		if (i_103_ != 0) {
		    i = i_83_ / i_103_;
		    i_77_ = i_84_ / i_103_;
		    if (i < 0)
			i = 0;
		    else if (i > 16256)
			i = 16256;
		}
		i_83_ += i_86_;
		i_84_ += i_87_;
		i_85_ += i_88_;
		i_103_ = i_85_ >> 14;
		if (i_103_ != 0) {
		    i_100_ = i_83_ / i_103_;
		    i_101_ = i_84_ / i_103_;
		    if (i_100_ < 7)
			i_100_ = 7;
		    else if (i_100_ > 16256)
			i_100_ = 16256;
		}
		int i_104_ = i_100_ - i >> 3;
		int i_105_ = i_101_ - i_77_ >> 3;
		i += i_81_ & 0x600000;
		int i_106_ = i_81_ >> 23;
		if (aBoolean2459) {
		    while (i_89_-- > 0) {
			is[i_78_++]
			    = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_;
			i += i_104_;
			i_77_ += i_105_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_;
			i += i_104_;
			i_77_ += i_105_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_;
			i += i_104_;
			i_77_ += i_105_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_;
			i += i_104_;
			i_77_ += i_105_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_;
			i += i_104_;
			i_77_ += i_105_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_;
			i += i_104_;
			i_77_ += i_105_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_;
			i += i_104_;
			i_77_ += i_105_;
			is[i_78_++]
			    = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_;
			i = i_100_;
			i_77_ = i_101_;
			i_83_ += i_86_;
			i_84_ += i_87_;
			i_85_ += i_88_;
			i_103_ = i_85_ >> 14;
			if (i_103_ != 0) {
			    i_100_ = i_83_ / i_103_;
			    i_101_ = i_84_ / i_103_;
			    if (i_100_ < 7)
				i_100_ = 7;
			    else if (i_100_ > 16256)
				i_100_ = 16256;
			}
			i_104_ = i_100_ - i >> 3;
			i_105_ = i_101_ - i_77_ >> 3;
			i_81_ += i_90_;
			i += i_81_ & 0x600000;
			i_106_ = i_81_ >> 23;
		    }
		    i_89_ = i_80_ - i_79_ & 0x7;
		    while (i_89_-- > 0) {
			is[i_78_++]
			    = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_;
			i += i_104_;
			i_77_ += i_105_;
		    }
		} else {
		    while (i_89_-- > 0) {
			int i_107_;
			if ((i_107_
			     = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_)
			    != 0)
			    is[i_78_] = i_107_;
			i_78_++;
			i += i_104_;
			i_77_ += i_105_;
			if ((i_107_
			     = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_)
			    != 0)
			    is[i_78_] = i_107_;
			i_78_++;
			i += i_104_;
			i_77_ += i_105_;
			if ((i_107_
			     = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_)
			    != 0)
			    is[i_78_] = i_107_;
			i_78_++;
			i += i_104_;
			i_77_ += i_105_;
			if ((i_107_
			     = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_)
			    != 0)
			    is[i_78_] = i_107_;
			i_78_++;
			i += i_104_;
			i_77_ += i_105_;
			if ((i_107_
			     = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_)
			    != 0)
			    is[i_78_] = i_107_;
			i_78_++;
			i += i_104_;
			i_77_ += i_105_;
			if ((i_107_
			     = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_)
			    != 0)
			    is[i_78_] = i_107_;
			i_78_++;
			i += i_104_;
			i_77_ += i_105_;
			if ((i_107_
			     = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_)
			    != 0)
			    is[i_78_] = i_107_;
			i_78_++;
			i += i_104_;
			i_77_ += i_105_;
			if ((i_107_
			     = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_)
			    != 0)
			    is[i_78_] = i_107_;
			i_78_++;
			i = i_100_;
			i_77_ = i_101_;
			i_83_ += i_86_;
			i_84_ += i_87_;
			i_85_ += i_88_;
			i_103_ = i_85_ >> 14;
			if (i_103_ != 0) {
			    i_100_ = i_83_ / i_103_;
			    i_101_ = i_84_ / i_103_;
			    if (i_100_ < 7)
				i_100_ = 7;
			    else if (i_100_ > 16256)
				i_100_ = 16256;
			}
			i_104_ = i_100_ - i >> 3;
			i_105_ = i_101_ - i_77_ >> 3;
			i_81_ += i_90_;
			i += i_81_ & 0x600000;
			i_106_ = i_81_ >> 23;
		    }
		    i_89_ = i_80_ - i_79_ & 0x7;
		    while (i_89_-- > 0) {
			int i_108_;
			if ((i_108_
			     = is_76_[(i_77_ & 0x3f80) + (i >> 7)] >>> i_106_)
			    != 0)
			    is[i_78_] = i_108_;
			i_78_++;
			i += i_104_;
			i_77_ += i_105_;
		    }
		}
	    }
	}
    }
    
    public static void method665
	(int i, int i_109_, int i_110_, int i_111_, int i_112_, int i_113_,
	 int i_114_, int i_115_, int i_116_, int i_117_, int i_118_,
	 int i_119_, int i_120_, int i_121_, int i_122_, int i_123_,
	 int i_124_, int i_125_, int i_126_) {
	int[] is = anInterface2_2458.method8(i_126_, -9039);
	if (is == null) {
	    int i_127_ = anInterface2_2458.method7(105, i_126_);
	    method660(i, i_109_, i_110_, i_111_, i_112_, i_113_,
		      method649(i_127_, i_114_), method649(i_127_, i_115_),
		      method649(i_127_, i_116_));
	} else {
	    aBoolean2465 = anInterface2_2458.method10(-11819, i_126_);
	    aBoolean2459 = anInterface2_2458.method9((byte) -50, i_126_);
	    if (Gpu.capturing) {
		Gpu.textured(i, i_109_, i_110_, i_111_, i_112_, i_113_, i_114_, i_115_, i_116_,
			     i_117_, i_118_, i_119_, i_120_, i_121_, i_122_, i_123_, i_124_, i_125_,
			     i_126_, is, aBoolean2465, aBoolean2459);
		return;
	    }
	    i_118_ = i_117_ - i_118_;
	    i_121_ = i_120_ - i_121_;
	    i_124_ = i_123_ - i_124_;
	    i_119_ -= i_117_;
	    i_122_ -= i_120_;
	    i_125_ -= i_123_;
	    int i_128_ = i_119_ * i_120_ - i_122_ * i_117_ << 14;
	    int i_129_ = i_122_ * i_123_ - i_125_ * i_120_ << 8;
	    int i_130_ = i_125_ * i_117_ - i_119_ * i_123_ << 5;
	    int i_131_ = i_118_ * i_120_ - i_121_ * i_117_ << 14;
	    int i_132_ = i_121_ * i_123_ - i_124_ * i_120_ << 8;
	    int i_133_ = i_124_ * i_117_ - i_118_ * i_123_ << 5;
	    int i_134_ = i_121_ * i_119_ - i_118_ * i_122_ << 14;
	    int i_135_ = i_124_ * i_122_ - i_121_ * i_125_ << 8;
	    int i_136_ = i_118_ * i_125_ - i_124_ * i_119_ << 5;
	    int i_137_ = 0;
	    int i_138_ = 0;
	    if (i_109_ != i) {
		i_137_ = (i_112_ - i_111_ << 16) / (i_109_ - i);
		i_138_ = (i_115_ - i_114_ << 16) / (i_109_ - i);
	    }
	    int i_139_ = 0;
	    int i_140_ = 0;
	    if (i_110_ != i_109_) {
		i_139_ = (i_113_ - i_112_ << 16) / (i_110_ - i_109_);
		i_140_ = (i_116_ - i_115_ << 16) / (i_110_ - i_109_);
	    }
	    int i_141_ = 0;
	    int i_142_ = 0;
	    if (i_110_ != i) {
		i_141_ = (i_111_ - i_113_ << 16) / (i - i_110_);
		i_142_ = (i_114_ - i_116_ << 16) / (i - i_110_);
	    }
	    if (i <= i_109_ && i <= i_110_) {
		if (i < anInt2463) {
		    if (i_109_ > anInt2463)
			i_109_ = anInt2463;
		    if (i_110_ > anInt2463)
			i_110_ = anInt2463;
		    if (i_109_ < i_110_) {
			i_113_ = i_111_ <<= 16;
			i_116_ = i_114_ <<= 16;
			if (i < 0) {
			    i_113_ -= i_141_ * i;
			    i_111_ -= i_137_ * i;
			    i_116_ -= i_142_ * i;
			    i_114_ -= i_138_ * i;
			    i = 0;
			}
			i_112_ <<= 16;
			i_115_ <<= 16;
			if (i_109_ < 0) {
			    i_112_ -= i_139_ * i_109_;
			    i_115_ -= i_140_ * i_109_;
			    i_109_ = 0;
			}
			int i_143_ = i - anInt2467;
			i_128_ += i_130_ * i_143_;
			i_131_ += i_133_ * i_143_;
			i_134_ += i_136_ * i_143_;
			if (i != i_109_ && i_141_ < i_137_
			    || i == i_109_ && i_141_ > i_139_) {
			    i_110_ -= i_109_;
			    i_109_ -= i;
			    i = anIntArray2460[i];
			    while (--i_109_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i, i_113_ >> 16,
					  i_111_ >> 16, i_116_ >> 8,
					  i_114_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_113_ += i_141_;
				i_111_ += i_137_;
				i_116_ += i_142_;
				i_114_ += i_138_;
				i += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			    while (--i_110_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i, i_113_ >> 16,
					  i_112_ >> 16, i_116_ >> 8,
					  i_115_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_113_ += i_141_;
				i_112_ += i_139_;
				i_116_ += i_142_;
				i_115_ += i_140_;
				i += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			} else {
			    i_110_ -= i_109_;
			    i_109_ -= i;
			    i = anIntArray2460[i];
			    while (--i_109_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i, i_111_ >> 16,
					  i_113_ >> 16, i_114_ >> 8,
					  i_116_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_113_ += i_141_;
				i_111_ += i_137_;
				i_116_ += i_142_;
				i_114_ += i_138_;
				i += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			    while (--i_110_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i, i_112_ >> 16,
					  i_113_ >> 16, i_115_ >> 8,
					  i_116_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_113_ += i_141_;
				i_112_ += i_139_;
				i_116_ += i_142_;
				i_115_ += i_140_;
				i += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			}
		    } else {
			i_112_ = i_111_ <<= 16;
			i_115_ = i_114_ <<= 16;
			if (i < 0) {
			    i_112_ -= i_141_ * i;
			    i_111_ -= i_137_ * i;
			    i_115_ -= i_142_ * i;
			    i_114_ -= i_138_ * i;
			    i = 0;
			}
			i_113_ <<= 16;
			i_116_ <<= 16;
			if (i_110_ < 0) {
			    i_113_ -= i_139_ * i_110_;
			    i_116_ -= i_140_ * i_110_;
			    i_110_ = 0;
			}
			int i_144_ = i - anInt2467;
			i_128_ += i_130_ * i_144_;
			i_131_ += i_133_ * i_144_;
			i_134_ += i_136_ * i_144_;
			if (i != i_110_ && i_141_ < i_137_
			    || i == i_110_ && i_139_ > i_137_) {
			    i_109_ -= i_110_;
			    i_110_ -= i;
			    i = anIntArray2460[i];
			    while (--i_110_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i, i_112_ >> 16,
					  i_111_ >> 16, i_115_ >> 8,
					  i_114_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_112_ += i_141_;
				i_111_ += i_137_;
				i_115_ += i_142_;
				i_114_ += i_138_;
				i += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			    while (--i_109_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i, i_113_ >> 16,
					  i_111_ >> 16, i_116_ >> 8,
					  i_114_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_113_ += i_139_;
				i_111_ += i_137_;
				i_116_ += i_140_;
				i_114_ += i_138_;
				i += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			} else {
			    i_109_ -= i_110_;
			    i_110_ -= i;
			    i = anIntArray2460[i];
			    while (--i_110_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i, i_111_ >> 16,
					  i_112_ >> 16, i_114_ >> 8,
					  i_115_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_112_ += i_141_;
				i_111_ += i_137_;
				i_115_ += i_142_;
				i_114_ += i_138_;
				i += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			    while (--i_109_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i, i_111_ >> 16,
					  i_113_ >> 16, i_114_ >> 8,
					  i_116_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_113_ += i_139_;
				i_111_ += i_137_;
				i_116_ += i_140_;
				i_114_ += i_138_;
				i += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			}
		    }
		}
	    } else if (i_109_ <= i_110_) {
		if (i_109_ < anInt2463) {
		    if (i_110_ > anInt2463)
			i_110_ = anInt2463;
		    if (i > anInt2463)
			i = anInt2463;
		    if (i_110_ < i) {
			i_111_ = i_112_ <<= 16;
			i_114_ = i_115_ <<= 16;
			if (i_109_ < 0) {
			    i_111_ -= i_137_ * i_109_;
			    i_112_ -= i_139_ * i_109_;
			    i_114_ -= i_138_ * i_109_;
			    i_115_ -= i_140_ * i_109_;
			    i_109_ = 0;
			}
			i_113_ <<= 16;
			i_116_ <<= 16;
			if (i_110_ < 0) {
			    i_113_ -= i_141_ * i_110_;
			    i_116_ -= i_142_ * i_110_;
			    i_110_ = 0;
			}
			int i_145_ = i_109_ - anInt2467;
			i_128_ += i_130_ * i_145_;
			i_131_ += i_133_ * i_145_;
			i_134_ += i_136_ * i_145_;
			if (i_109_ != i_110_ && i_137_ < i_139_
			    || i_109_ == i_110_ && i_137_ > i_141_) {
			    i -= i_110_;
			    i_110_ -= i_109_;
			    i_109_ = anIntArray2460[i_109_];
			    while (--i_110_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i_109_, i_111_ >> 16,
					  i_112_ >> 16, i_114_ >> 8,
					  i_115_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_111_ += i_137_;
				i_112_ += i_139_;
				i_114_ += i_138_;
				i_115_ += i_140_;
				i_109_ += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			    while (--i >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i_109_, i_111_ >> 16,
					  i_113_ >> 16, i_114_ >> 8,
					  i_116_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_111_ += i_137_;
				i_113_ += i_141_;
				i_114_ += i_138_;
				i_116_ += i_142_;
				i_109_ += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			} else {
			    i -= i_110_;
			    i_110_ -= i_109_;
			    i_109_ = anIntArray2460[i_109_];
			    while (--i_110_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i_109_, i_112_ >> 16,
					  i_111_ >> 16, i_115_ >> 8,
					  i_114_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_111_ += i_137_;
				i_112_ += i_139_;
				i_114_ += i_138_;
				i_115_ += i_140_;
				i_109_ += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			    while (--i >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i_109_, i_113_ >> 16,
					  i_111_ >> 16, i_116_ >> 8,
					  i_114_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_111_ += i_137_;
				i_113_ += i_141_;
				i_114_ += i_138_;
				i_116_ += i_142_;
				i_109_ += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			}
		    } else {
			i_113_ = i_112_ <<= 16;
			i_116_ = i_115_ <<= 16;
			if (i_109_ < 0) {
			    i_113_ -= i_137_ * i_109_;
			    i_112_ -= i_139_ * i_109_;
			    i_116_ -= i_138_ * i_109_;
			    i_115_ -= i_140_ * i_109_;
			    i_109_ = 0;
			}
			i_111_ <<= 16;
			i_114_ <<= 16;
			if (i < 0) {
			    i_111_ -= i_141_ * i;
			    i_114_ -= i_142_ * i;
			    i = 0;
			}
			int i_146_ = i_109_ - anInt2467;
			i_128_ += i_130_ * i_146_;
			i_131_ += i_133_ * i_146_;
			i_134_ += i_136_ * i_146_;
			if (i_137_ < i_139_) {
			    i_110_ -= i;
			    i -= i_109_;
			    i_109_ = anIntArray2460[i_109_];
			    while (--i >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i_109_, i_113_ >> 16,
					  i_112_ >> 16, i_116_ >> 8,
					  i_115_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_113_ += i_137_;
				i_112_ += i_139_;
				i_116_ += i_138_;
				i_115_ += i_140_;
				i_109_ += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			    while (--i_110_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i_109_, i_111_ >> 16,
					  i_112_ >> 16, i_114_ >> 8,
					  i_115_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_111_ += i_141_;
				i_112_ += i_139_;
				i_114_ += i_142_;
				i_115_ += i_140_;
				i_109_ += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			} else {
			    i_110_ -= i;
			    i -= i_109_;
			    i_109_ = anIntArray2460[i_109_];
			    while (--i >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i_109_, i_112_ >> 16,
					  i_113_ >> 16, i_115_ >> 8,
					  i_116_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_113_ += i_137_;
				i_112_ += i_139_;
				i_116_ += i_138_;
				i_115_ += i_140_;
				i_109_ += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			    while (--i_110_ >= 0) {
				method664(DrawingArea.buffer,
					  is, 0, 0, i_109_, i_112_ >> 16,
					  i_111_ >> 16, i_115_ >> 8,
					  i_114_ >> 8, i_128_, i_131_, i_134_,
					  i_129_, i_132_, i_135_);
				i_111_ += i_141_;
				i_112_ += i_139_;
				i_114_ += i_142_;
				i_115_ += i_140_;
				i_109_ += DrawingArea.bufferWidth;
				i_128_ += i_130_;
				i_131_ += i_133_;
				i_134_ += i_136_;
			    }
			}
		    }
		}
	    } else if (i_110_ < anInt2463) {
		if (i > anInt2463)
		    i = anInt2463;
		if (i_109_ > anInt2463)
		    i_109_ = anInt2463;
		if (i < i_109_) {
		    i_112_ = i_113_ <<= 16;
		    i_115_ = i_116_ <<= 16;
		    if (i_110_ < 0) {
			i_112_ -= i_139_ * i_110_;
			i_113_ -= i_141_ * i_110_;
			i_115_ -= i_140_ * i_110_;
			i_116_ -= i_142_ * i_110_;
			i_110_ = 0;
		    }
		    i_111_ <<= 16;
		    i_114_ <<= 16;
		    if (i < 0) {
			i_111_ -= i_137_ * i;
			i_114_ -= i_138_ * i;
			i = 0;
		    }
		    int i_147_ = i_110_ - anInt2467;
		    i_128_ += i_130_ * i_147_;
		    i_131_ += i_133_ * i_147_;
		    i_134_ += i_136_ * i_147_;
		    if (i_139_ < i_141_) {
			i_109_ -= i;
			i -= i_110_;
			i_110_ = anIntArray2460[i_110_];
			while (--i >= 0) {
			    method664(DrawingArea.buffer, is, 0,
				      0, i_110_, i_112_ >> 16, i_113_ >> 16,
				      i_115_ >> 8, i_116_ >> 8, i_128_, i_131_,
				      i_134_, i_129_, i_132_, i_135_);
			    i_112_ += i_139_;
			    i_113_ += i_141_;
			    i_115_ += i_140_;
			    i_116_ += i_142_;
			    i_110_ += DrawingArea.bufferWidth;
			    i_128_ += i_130_;
			    i_131_ += i_133_;
			    i_134_ += i_136_;
			}
			while (--i_109_ >= 0) {
			    method664(DrawingArea.buffer, is, 0,
				      0, i_110_, i_112_ >> 16, i_111_ >> 16,
				      i_115_ >> 8, i_114_ >> 8, i_128_, i_131_,
				      i_134_, i_129_, i_132_, i_135_);
			    i_112_ += i_139_;
			    i_111_ += i_137_;
			    i_115_ += i_140_;
			    i_114_ += i_138_;
			    i_110_ += DrawingArea.bufferWidth;
			    i_128_ += i_130_;
			    i_131_ += i_133_;
			    i_134_ += i_136_;
			}
		    } else {
			i_109_ -= i;
			i -= i_110_;
			i_110_ = anIntArray2460[i_110_];
			while (--i >= 0) {
			    method664(DrawingArea.buffer, is, 0,
				      0, i_110_, i_113_ >> 16, i_112_ >> 16,
				      i_116_ >> 8, i_115_ >> 8, i_128_, i_131_,
				      i_134_, i_129_, i_132_, i_135_);
			    i_112_ += i_139_;
			    i_113_ += i_141_;
			    i_115_ += i_140_;
			    i_116_ += i_142_;
			    i_110_ += DrawingArea.bufferWidth;
			    i_128_ += i_130_;
			    i_131_ += i_133_;
			    i_134_ += i_136_;
			}
			while (--i_109_ >= 0) {
			    method664(DrawingArea.buffer, is, 0,
				      0, i_110_, i_111_ >> 16, i_112_ >> 16,
				      i_114_ >> 8, i_115_ >> 8, i_128_, i_131_,
				      i_134_, i_129_, i_132_, i_135_);
			    i_112_ += i_139_;
			    i_111_ += i_137_;
			    i_115_ += i_140_;
			    i_114_ += i_138_;
			    i_110_ += DrawingArea.bufferWidth;
			    i_128_ += i_130_;
			    i_131_ += i_133_;
			    i_134_ += i_136_;
			}
		    }
		} else {
		    i_111_ = i_113_ <<= 16;
		    i_114_ = i_116_ <<= 16;
		    if (i_110_ < 0) {
			i_111_ -= i_139_ * i_110_;
			i_113_ -= i_141_ * i_110_;
			i_114_ -= i_140_ * i_110_;
			i_116_ -= i_142_ * i_110_;
			i_110_ = 0;
		    }
		    i_112_ <<= 16;
		    i_115_ <<= 16;
		    if (i_109_ < 0) {
			i_112_ -= i_137_ * i_109_;
			i_115_ -= i_138_ * i_109_;
			i_109_ = 0;
		    }
		    int i_148_ = i_110_ - anInt2467;
		    i_128_ += i_130_ * i_148_;
		    i_131_ += i_133_ * i_148_;
		    i_134_ += i_136_ * i_148_;
		    if (i_139_ < i_141_) {
			i -= i_109_;
			i_109_ -= i_110_;
			i_110_ = anIntArray2460[i_110_];
			while (--i_109_ >= 0) {
			    method664(DrawingArea.buffer, is, 0,
				      0, i_110_, i_111_ >> 16, i_113_ >> 16,
				      i_114_ >> 8, i_116_ >> 8, i_128_, i_131_,
				      i_134_, i_129_, i_132_, i_135_);
			    i_111_ += i_139_;
			    i_113_ += i_141_;
			    i_114_ += i_140_;
			    i_116_ += i_142_;
			    i_110_ += DrawingArea.bufferWidth;
			    i_128_ += i_130_;
			    i_131_ += i_133_;
			    i_134_ += i_136_;
			}
			while (--i >= 0) {
			    method664(DrawingArea.buffer, is, 0,
				      0, i_110_, i_112_ >> 16, i_113_ >> 16,
				      i_115_ >> 8, i_116_ >> 8, i_128_, i_131_,
				      i_134_, i_129_, i_132_, i_135_);
			    i_112_ += i_137_;
			    i_113_ += i_141_;
			    i_115_ += i_138_;
			    i_116_ += i_142_;
			    i_110_ += DrawingArea.bufferWidth;
			    i_128_ += i_130_;
			    i_131_ += i_133_;
			    i_134_ += i_136_;
			}
		    } else {
			i -= i_109_;
			i_109_ -= i_110_;
			i_110_ = anIntArray2460[i_110_];
			while (--i_109_ >= 0) {
			    method664(DrawingArea.buffer, is, 0,
				      0, i_110_, i_113_ >> 16, i_111_ >> 16,
				      i_116_ >> 8, i_114_ >> 8, i_128_, i_131_,
				      i_134_, i_129_, i_132_, i_135_);
			    i_111_ += i_139_;
			    i_113_ += i_141_;
			    i_114_ += i_140_;
			    i_116_ += i_142_;
			    i_110_ += DrawingArea.bufferWidth;
			    i_128_ += i_130_;
			    i_131_ += i_133_;
			    i_134_ += i_136_;
			}
			while (--i >= 0) {
			    method664(DrawingArea.buffer, is, 0,
				      0, i_110_, i_113_ >> 16, i_112_ >> 16,
				      i_116_ >> 8, i_115_ >> 8, i_128_, i_131_,
				      i_134_, i_129_, i_132_, i_135_);
			    i_112_ += i_137_;
			    i_113_ += i_141_;
			    i_115_ += i_138_;
			    i_116_ += i_142_;
			    i_110_ += DrawingArea.bufferWidth;
			    i_128_ += i_130_;
			    i_131_ += i_133_;
			    i_134_ += i_136_;
			}
		    }
		}
	    }
	}
    }
    
    public static void method666(int i, int i_149_, int i_150_) {
	aBoolean2470
	    = (i < 0 || i > anInt2461 || i_149_ < 0 || i_149_ > anInt2461
	       || i_150_ < 0 || i_150_ > anInt2461);
    }
    
    static {
	anIntArray2455 = new int[2048];
	aBoolean2459 = false;
	anIntArray2466 = new int[512];
	anInt2468 = 0;
	sineTable = new int[2048];
	aBoolean2465 = false;
	aBoolean2470 = false;
	anIntArray2452 = new int[65536];
	aBoolean2471 = true;
	for (int i = 1; i < 512; i++)
	    anIntArray2466[i] = 32768 / i;
	for (int i = 1; i < 2048; i++)
	    anIntArray2455[i] = 65536 / i;
	for (int i = 0; i < 2048; i++) {
	    sineTable[i]
		= (int) (Math.sin((double) i * 0.0030679615) * 65536.0);
	    cosineTable[i]
		= (int) (Math.cos((double) i * 0.0030679615) * 65536.0);
	}
    }
}

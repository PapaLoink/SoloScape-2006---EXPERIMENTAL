package unpackaged;

import jagex.graphics.BitmapFont;
import jagex.graphics.DrawingArea;
import jagex.graphics.AbstractImage;
import jagex.world.actors.StillGraphic;
import jagex.utils.Node;
import jagex.utils.IsaacPrng;
import jagex.utils.JString;
import jagex.utils.Queue;
import jagex.utils.Deque;
import jagex.utils.Cache;

/* Class32 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Class32
{
    public boolean aBoolean574 = true;
    public int anInt575;
    public int anInt576;
    public static JString aClass3_577;
    public static JString aClass3_578
	= Class39_Sub5_Sub9.createJstring("Passwort: ");
    public int anInt579;
    public int anInt580;
    public static JString aClass3_581
	= Class39_Sub5_Sub9.createJstring("Private chat");
    public int anInt582;
    public static Queue regularRequestsQueue;
    public int anInt584;
    public static AbstractImage aClass57_585;
    public static int[] anIntArray586;
    public static BitmapFont aClass39_Sub5_Sub10_Sub1_587;
    public static JString aClass3_588;
    public static int anInt589;
    public static int anInt590;
    public static JString aClass3_591;
    public static JString aClass3_592;
    public static JString aClass3_593;
    
    public static int method323(int i, int i_0_, int i_1_, int i_2_) {
	int i_3_ = i_1_ / i;
	int i_4_ = i_2_ / i;
	int i_5_ = i - 1 & i_1_;
	int i_6_ = i_2_ & i - 1;
	int i_7_ = Class39_Sub14.method878(-26, i_4_, i_3_);
	int i_8_ = Class39_Sub14.method878(-26, i_4_, i_3_ + 1);
	int i_9_ = Class39_Sub14.method878(-26, i_4_ + 1, i_3_);
	int i_10_ = Class39_Sub14.method878(-26, i_4_ + 1, i_3_ + 1);
	int i_11_ = Class39_Sub7.method851(i_5_, i_8_, i_7_, i, -247888528);
	int i_12_ = Class39_Sub7.method851(i_5_, i_10_, i_9_, i, -247888528);
	return Class39_Sub7.method851(i_6_, i_12_, i_11_, i, -247888528);
    }
    
    public static void method324(int i) {
	aClass3_588 = null;
	anIntArray586 = null;
	aClass3_581 = null;
	aClass3_591 = null;
	regularRequestsQueue = null;
	aClass3_593 = null;
	aClass39_Sub5_Sub10_Sub1_587 = null;
	aClass3_578 = null;
	aClass3_592 = null;
	aClass57_585 = null;
	aClass3_577 = null;
    }
    
    public static void method325(byte i) {
	Class39_Sub5_Sub14.aBoolean1908 = true;
	IsaacPrng.method1043(-1540585334);
	if (!Class39_Sub12.aBoolean1489) {
	    if (Class39_Sub5_Sub4_Sub4.anInt2285 != 1) {
                if (GrandExchangeWidgets.searching) {
                    GrandExchangeSearch.draw();
                } else if (Class39_Sub5_Sub4_Sub4.anInt2285 == 2) {
		    aClass39_Sub5_Sub10_Sub1_587.method629
                        (StillGraphic.aClass3_2340, 239, 40, 0);
		    aClass39_Sub5_Sub10_Sub1_587.method629
			(Class39_Sub5_Sub11.method708((new JString[]
						       { Class66.aClass3_1151,
							 (ScriptState
							  .aClass3_456) })),
			 239, 60, 128);
		} else if (Class39_Sub5_Sub4_Sub4.anInt2285 == 3) {
		    if (Class66.aClass3_1159 != Class66.aClass3_1151) {
			Class2.method52(Class66.aClass3_1151, false);
			Class66.aClass3_1159 = Class66.aClass3_1151;
		    }
		    BitmapFont class39_sub5_sub10_sub1
			= Class39_Sub5_Sub14.p12fullFont;
		    DrawingArea.setDimensions(0, 0, 463, 77);
		    for (int i_13_ = 0; i_13_ < Class67.anInt1184; i_13_++) {
			int i_14_
			    = i_13_ * 14 + (18 - Class39_Sub14.anInt1511);
			if (i_14_ > 0 && i_14_ < 110)
			    class39_sub5_sub10_sub1.method629
				(Class46_Sub1.aClass3Array1552[i_13_], 239,
				 i_14_, 0);
		    }
		    DrawingArea.resetDimensions();
		    if (Class67.anInt1184 > 5)
			Class4.method102(463, Class39_Sub14.anInt1511,
					 Class67.anInt1184 * 14 + 7, 0, 18734,
					 77);
		    if (Class66.aClass3_1151.getLength() != 0) {
			if (Class67.anInt1184 == 0)
			    aClass39_Sub5_Sub10_Sub1_587
				.method629(Bzip2Block.aClass3_1050, 239, 40, 0);
		    } else
			aClass39_Sub5_Sub10_Sub1_587
			    .method629(IsaacPrng.aClass3_1098, 239, 40, 255);
		    class39_sub5_sub10_sub1.method629
			(Class39_Sub5_Sub11.method708((new JString[]
						       { Class66.aClass3_1151,
							 (ScriptState
							  .aClass3_456) })),
			 239, 90, 0);
		    DrawingArea.drawHorizontalLine(0, 77, 479, 0);
		} else if (OndemandRequest.aClass3_1714 != null) {
		    aClass39_Sub5_Sub10_Sub1_587.method625((OndemandRequest
							    .aClass3_1714),
							   10, 20, 459, 40, 0,
							   false, 1, 1, 0);
		    aClass39_Sub5_Sub10_Sub1_587.method629((ClientScript
							    .aClass3_1701),
							   239, 80, 128);
		} else if (Class39_Sub5_Sub14.anInt1912 == -1) {
		    if (IsaacPrng.anInt1095 != -1) {
			boolean bool
			    = Deque.method955(96, 479, 0, IsaacPrng.anInt1095,
						0, -1, 3);
			if (!bool)
			    Class14.aBoolean245 = true;
		    } else {
			BitmapFont class39_sub5_sub10_sub1
			    = Class39_Sub5_Sub14.p12fullFont;
			DrawingArea.setDimensions(0, 0, 463, 77);
			int i_15_ = 0;
			for (int i_16_ = 0; i_16_ < 100; i_16_++) {
			    if (Class2.aClass3Array52[i_16_] != null) {
				int i_17_ = Client.anIntArray1268[i_16_];
				JString class3
				    = Class39_Sub11.aClass3Array1462[i_16_];
				int i_18_
				    = -(i_15_ * 14) + Node.anInt741 + 70;
				int i_19_ = 0;
				if (class3 != null
				    && class3.method65(Class37.aClass3_661,
						       false)) {
				    class3 = class3.method85(-58, 5);
				    i_19_ = 1;
				}
				if (class3 != null
				    && class3.method65(Class53.aClass3_959,
						       false)) {
				    class3 = class3.method85(-58, 5);
				    i_19_ = 2;
				}
				if (i_17_ == 0) {
				    i_15_++;
				    if (i_18_ > 0 && i_18_ < 110)
					class39_sub5_sub10_sub1.method635
					    (Class2.aClass3Array52[i_16_], 4,
					     i_18_, 0, false);
				}
				if ((i_17_ == 1 || i_17_ == 2)
				    && (i_17_ == 1 || Bzip2Block.anInt1051 == 0
					|| (Bzip2Block.anInt1051 == 1
					    && JString.method60(21469,
							       class3)))) {
				    if (i_18_ > 0 && i_18_ < 110) {
					int i_20_ = 4;
					if (i_19_ == 1) {
					    Class55
						.aClass39_Sub5_Sub10_Sub4Array1247
						[0]
						.method695(i_20_, i_18_ - 12);
					    i_20_ += 14;
					}
					if (i_19_ == 2) {
					    Class55
						.aClass39_Sub5_Sub10_Sub4Array1247
						[1]
						.method695(i_20_, i_18_ - 12);
					    i_20_ += 14;
					}
					class39_sub5_sub10_sub1.method647
					    ((Class39_Sub5_Sub11.method708
					      ((new JString[]
						{ class3, (OndemandRequest
							   .aClass3_1723) }))),
					     i_20_, i_18_, 0);
					i_20_ += class39_sub5_sub10_sub1
						     .method637(class3) + 8;
					class39_sub5_sub10_sub1.method647
					    (Class2.aClass3Array52[i_16_],
					     i_20_, i_18_, 255);
				    }
				    i_15_++;
				}
				if ((i_17_ == 3 || i_17_ == 7)
				    && Class2.anInt53 == 0
				    && (i_17_ == 7 || NameTable.anInt177 == 0
					|| (NameTable.anInt177 == 1
					    && JString.method60(21469,
							       class3)))) {
				    i_15_++;
				    if (i_18_ > 0 && i_18_ < 110) {
					int i_21_ = 4;
					class39_sub5_sub10_sub1.method647
					    (Class39_Sub5_Sub16.aClass3_1991,
					     i_21_, i_18_, 0);
					i_21_ += (class39_sub5_sub10_sub1
						      .method637
						  (Class39_Sub5_Sub16
						   .aClass3_1991));
					i_21_ += class39_sub5_sub10_sub1
						     .method645(32);
					if (i_19_ == 1) {
					    Class55
						.aClass39_Sub5_Sub10_Sub4Array1247
						[0]
						.method695(i_21_, i_18_ - 12);
					    i_21_ += 14;
					}
					if (i_19_ == 2) {
					    Class55
						.aClass39_Sub5_Sub10_Sub4Array1247
						[1]
						.method695(i_21_, i_18_ - 12);
					    i_21_ += 14;
					}
					class39_sub5_sub10_sub1.method647
					    ((Class39_Sub5_Sub11.method708
					      ((new JString[]
						{ class3, (OndemandRequest
							   .aClass3_1723) }))),
					     i_21_, i_18_, 0);
					i_21_ += class39_sub5_sub10_sub1
						     .method637(class3) + 8;
					class39_sub5_sub10_sub1.method647
					    (Class2.aClass3Array52[i_16_],
					     i_21_, i_18_, 8388608);
				    }
				}
				if (i_17_ == 4
				    && (Cache.anInt118 == 0
					|| (Cache.anInt118 == 1
					    && JString.method60(21469,
							       class3)))) {
				    if (i_18_ > 0 && i_18_ < 110)
					class39_sub5_sub10_sub1.method647
					    ((Class39_Sub5_Sub11.method708
					      ((new JString[]
						{ class3,
						  Class62_Sub2.aClass3_1605,
						  (Class2.aClass3Array52
						   [i_16_]) }))),
					     4, i_18_, 8388736);
				    i_15_++;
				}
				if (i_17_ == 5 && Class2.anInt53 == 0
				    && NameTable.anInt177 < 2) {
				    if (i_18_ > 0 && i_18_ < 110)
					class39_sub5_sub10_sub1.method647
					    (Class2.aClass3Array52[i_16_], 4,
					     i_18_, 8388608);
				    i_15_++;
				}
				if (i_17_ == 6 && Class2.anInt53 == 0
				    && NameTable.anInt177 < 2) {
				    if (i_18_ > 0 && i_18_ < 110) {
					class39_sub5_sub10_sub1.method647
					    ((Class39_Sub5_Sub11.method708
					      ((new JString[]
						{ Class39_Sub14.aClass3_1514,
						  Class62_Sub2.aClass3_1605,
						  class3,
						  (OndemandRequest
						   .aClass3_1723) }))),
					     4, i_18_, 0);
					class39_sub5_sub10_sub1.method647
					    (Class2.aClass3Array52[i_16_],
					     (class39_sub5_sub10_sub1.method637
					      (Class39_Sub5_Sub11.method708
					       ((new JString[]
						 { Class39_Sub14.aClass3_1514,
						   Class62_Sub2.aClass3_1605,
						   class3 })))) + 12,
					     i_18_, 8388608);
				    }
				    i_15_++;
				}
				if (i_17_ == 8
				    && (Cache.anInt118 == 0
					|| (Cache.anInt118 == 1
					    && JString.method60(21469,
							       class3)))) {
				    i_15_++;
				    if (i_18_ > 0 && i_18_ < 110)
					class39_sub5_sub10_sub1.method647
					    ((Class39_Sub5_Sub11.method708
					      ((new JString[]
						{ class3,
						  Class62_Sub2.aClass3_1605,
						  (Class2.aClass3Array52
						   [i_16_]) }))),
					     4, i_18_, 8270336);
				}
			    }
			}
			DrawingArea.resetDimensions();
			Deque.anInt912 = i_15_ * 14 + 7;
			if (Deque.anInt912 < 78)
			    Deque.anInt912 = 78;
			Class4.method102(463,
					 (-Node.anInt741 + Deque.anInt912
					  - 77),
					 Deque.anInt912, 0, 18734, 77);
			JString class3;
			if (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109 != null
			    && (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				.aClass3_2521) != null)
			    class3 = (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				      .aClass3_2521);
			else
			    class3 = Class39_Sub5_Sub14.aClass3_1897;
			class39_sub5_sub10_sub1.method647
			    ((Class39_Sub5_Sub11.method708
			      (new JString[] { class3,
					      OndemandRequest.aClass3_1723 })),
			     4, 90, 0);
			boolean promptShown = ModHook.promptShown();
			class39_sub5_sub10_sub1.method647
			    (promptShown ? ModHook.promptText()
				 : Class39_Sub5_Sub11.method708((new JString[]
							   { (Class66
							      .aClass3_1160),
							     (ScriptState
							      .aClass3_456) })),
			     (class39_sub5_sub10_sub1.method637
			      (Class39_Sub5_Sub11.method708
			       (new JString[] { class3, Class31.aClass3_556 }))) + 6,
			     90, promptShown ? ModHook.PROMPT_COLOUR : 255);
			DrawingArea.drawHorizontalLine(0, 77, 479, 0);
		    }
		} else {
		    boolean bool
			= Deque.method955(96, 479, 0,
					    Class39_Sub5_Sub14.anInt1912, 0,
					    -1, 2);
		    if (!bool)
			Class14.aBoolean245 = true;
		}
	    } else {
		aClass39_Sub5_Sub10_Sub1_587.method629(Class45.aClass3_859,
						       239, 40, 0);
		aClass39_Sub5_Sub10_Sub1_587.method629
		    (Class39_Sub5_Sub11.method708((new JString[]
						   { Class66.aClass3_1151,
						     ScriptState.aClass3_456 })),
		     239, 60, 128);
	    }
	} else {
	    aClass39_Sub5_Sub10_Sub1_587.method629(Class66.aClass3_1150, 239,
						   40, 0);
	    aClass39_Sub5_Sub10_Sub1_587.method629
		(Class39_Sub5_Sub11.method708((new JString[]
					       { Class66.aClass3_1154,
						 ScriptState.aClass3_456 })),
		 239, 60, 128);
	}
	int i_22_ = 32 % ((24 - i) / 50);
	if (Class39_Sub12.aBoolean1493 && Class37.anInt653 == 2)
	    Class1.method49(-53);
	CacheIO.method127(17);
	if (GrandExchangeWidgets.searching)
	    jagex.world.map.TraversalMap.aClass57_514.method1006(0);
    }
    
    public Class32(int i, int i_23_, int i_24_, int i_25_, int i_26_,
		   int i_27_, boolean bool) {
	anInt584 = i_27_;
	anInt576 = i_23_;
	anInt580 = i_24_;
	aBoolean574 = bool;
	anInt582 = i_25_;
	anInt575 = i;
	anInt579 = i_26_;
    }
    
    public static int method326(int i) {
	return FileLoader.anInt1302++;
    }
    
    static {
	aClass3_577 = aClass3_581;
	regularRequestsQueue = new Queue();
	aClass3_588 = Class39_Sub5_Sub9.createJstring("::fpsoff");
	aClass3_593 = Class39_Sub5_Sub9.createJstring("_");
	aClass3_591
	    = Class39_Sub5_Sub9
		  .createJstring("Enter name of player to delete from list");
	aClass3_592 = aClass3_591;
	anInt589 = (int) (Math.random() * 17.0) - 8;
    }
}

package unpackaged;

import jagex.utils.HashTable;
import jagex.utils.Huffmans;
import jagex.world.actors.GroundItem;
import jagex.graphics.AbstractImage;
import jagex.world.actors.Projectile;
import jagex.utils.Node;
import jagex.utils.IsaacPrng;
import jagex.utils.JString;
import jagex.world.actors.Npc;
import jagex.world.actors.Player;
import jagex.world.map.TraversalMap;
import jagex.utils.Deque;
import jagex.io.FrameBuffer;
import jagex.io.Buffer;
import jagex.utils.Cache;

/* Class24 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class ScriptState
{
    public static Class39_Sub1 aClass39_Sub1_439;
    public int[] intVariables;
    public static JString aClass3_441;
    public static JString aClass3_442;
    public static JString aClass3_443;
    public static JString aClass3_444;
    public static JString aClass3_445;
    public static JString aClass3_446;
    public static JString aClass3_447
	= Class39_Sub5_Sub9.createJstring(" has logged out)3");
    public static int anInt448;
    public static int[] arraySizes;
    public JString[] strVariables;
    public ClientScript clientScript;
    public static JString aClass3_452;
    public int offset = -1;
    public static int anInt454;
    public static int[][] anIntArrayArray455;
    public static JString aClass3_456;
    
    public static void method275(int i) {
	ArchiveWorker.aClass46_1206.method938((byte) -111);
	for (int i_0_ = 0; i_0_ < 32; i_0_++)
	    Class39_Sub7.aLongArray1374[i_0_] = 0L;
	for (int i_1_ = 0; i_1_ < 32; i_1_++)
	    Class67.aLongArray1172[i_1_] = 0L;
	Class2.anInt50 = 0;
    }
    
    public static void setObjectFileLoaders(boolean bool, FileTable class9, FileTable class9_2_,
				 int i) {
	Deque.objectFileLoader = class9_2_;
	JMouseListener.aBoolean785 = bool;
	Npc.modelFileLoader = class9;
    }
    
    public static void method277(boolean bool) {
	Class62_Sub1.aClass7_1587.method134(27392);
	Class2.aClass7_58.method134(27392);
	Deque.fontCache.method134(27392);
    }
    
    public static void method278(int i, int i_3_) {
	if (i >= 0) {
	    int i_4_ = Class12.anIntArray196[i];
	    int i_5_ = Class43.anIntArray820[i];
	    int i_6_ = JKeyListener.anIntArray621[i];
	    if (i_6_ >= 2000)
		i_6_ -= 2000;
	    int i_7_ = NameTable.anIntArray176[i];
	    if (i_6_ == 20 && GrandExchangeWidgets.click(i_5_)) return;
	    if (Class39_Sub5_Sub4_Sub4.anInt2285 != 0 && i_6_ != 1002) {
		Class14.aBoolean245 = true;
		Class39_Sub5_Sub4_Sub4.anInt2285 = 0;
	    }
	    if (i_6_ == 32) {
		FrameBuffer.outgoingGameBuffer.putFrame(182);
		FrameBuffer.outgoingGameBuffer.putWordLe(i_7_);
		FrameBuffer.outgoingGameBuffer.putWord(i_4_);
		FrameBuffer.outgoingGameBuffer
		    .putDwordLe(i_5_);
		Class39_Sub5_Sub5.anInt1739 = i_4_;
		Class65.anInt1137 = i_5_;
		Class25.anInt459 = 2;
		GroundItem.anInt2242 = 0;
		if (i_5_ >> 16 == Class39_Sub11.anInt1478)
		    Class25.anInt459 = 1;
		if (i_5_ >> 16 == Class39_Sub5_Sub14.anInt1912)
		    Class25.anInt459 = 3;
	    }
	    if (i_6_ == 1005) {
		Npc.method526(i_7_, i_4_, false, i_5_);
		FrameBuffer.outgoingGameBuffer.putFrame(120);
		FrameBuffer.outgoingGameBuffer
		    .putWord(JKeyListener.anInt618 + i_5_);
		FrameBuffer.outgoingGameBuffer
		    .putWord(Class65.anInt1145 + i_4_);
		FrameBuffer.outgoingGameBuffer
		    .putWord((i_7_ & 0x1ffff835) >> 14);
	    }
	    if (i_6_ == 12
		&& Npc.method526(i_7_, i_4_, false,
							 i_5_)) {
		FrameBuffer.outgoingGameBuffer.putFrame(78);
		FrameBuffer.outgoingGameBuffer
		    .method827(Class41.anInt775, -334352184);
		FrameBuffer.outgoingGameBuffer
		    .putWord(i_4_ + Class65.anInt1145);
		FrameBuffer.outgoingGameBuffer
		    .putWord(Class31.anInt570);
		FrameBuffer.outgoingGameBuffer
		    .putWordLe(JKeyListener.anInt618 + i_5_);
		FrameBuffer.outgoingGameBuffer
		    .method832(i_7_ >> 14 & 0x7fff, (byte) -30);
	    }
	    if (i_6_ == 8 && Class39_Sub10.anInt1420 == -1) {
		Bzip2Block.method1035((byte) 4, i_4_, i_5_);
		JString.anInt1231 = i_4_;
		Class39_Sub10.anInt1420 = i_5_;
	    }
	    if (i_6_ == 40) {
		boolean bool
		    = Class26.method293(i_3_ ^ 0x5e47, 0, i_5_, 0, 2, 0, 0,
					false, 0,
					(Cache
					 .aClass39_Sub5_Sub4_Sub4_Sub2_109
					 .anIntArray2314[0]),
					(Cache
					 .aClass39_Sub5_Sub4_Sub4_Sub2_109
					 .anIntArray2255[0]),
					i_4_);
		if (!bool)
		    bool = Class26.method293(24134, 0, i_5_, 0, 2, 1, 1, false,
					     0,
					     (Cache
					      .aClass39_Sub5_Sub4_Sub4_Sub2_109
					      .anIntArray2314[0]),
					     (Cache
					      .aClass39_Sub5_Sub4_Sub4_Sub2_109
					      .anIntArray2255[0]),
					     i_4_);
		Class4.anInt80 = 2;
		Class26.anInt503 = 0;
		Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
		Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
		FrameBuffer.outgoingGameBuffer.putFrame(149);
		FrameBuffer.outgoingGameBuffer
		    .method796(-21994, Class65.anInt1145 + i_4_);
		FrameBuffer.outgoingGameBuffer
		    .method796(-21994, JKeyListener.anInt618 + i_5_);
		FrameBuffer.outgoingGameBuffer.putWord(i_7_);
	    }
	    if (i_6_ == 31 || i_6_ == 21 || i_6_ == 2 || i_6_ == 50) {
		JString class3 = Class33.aClass3Array601[i];
		int i_8_
		    = class3.method80(22938, Class39_Sub5_Sub4.aClass3_1728);
		if (i_8_ != -1) {
		    long l = class3.method85(-58, i_8_ + 5).method69
				 ((byte) -45).encodeBase37();
		    if (i_6_ == 31)
			Class68.method1111(i_3_ - 1, l);
		    if (i_6_ == 21)
			Class65.method1097(0, l);
		    if (i_6_ == 2)
			Class20.method246(l, (byte) -123);
		    if (i_6_ == 50)
			HashTable.method237(l, (byte) -97);
		}
	    }
	    if (i_6_ == 38) {
		boolean bool
		    = Class26.method293(24134, 0, i_5_, 0, 2, 0, 0, false, 0,
					(Cache
					 .aClass39_Sub5_Sub4_Sub4_Sub2_109
					 .anIntArray2314[0]),
					(Cache
					 .aClass39_Sub5_Sub4_Sub4_Sub2_109
					 .anIntArray2255[0]),
					i_4_);
		if (!bool)
		    bool = Class26.method293(24134, 0, i_5_, 0, 2, 1, 1, false,
					     0,
					     (Cache
					      .aClass39_Sub5_Sub4_Sub4_Sub2_109
					      .anIntArray2314[0]),
					     (Cache
					      .aClass39_Sub5_Sub4_Sub4_Sub2_109
					      .anIntArray2255[0]),
					     i_4_);
		Class4.anInt80 = 2;
		Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
		Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
		Class26.anInt503 = 0;
		FrameBuffer.outgoingGameBuffer.putFrame(252);
		FrameBuffer.outgoingGameBuffer
		    .method796(i_3_ - 21995, i_7_);
		FrameBuffer.outgoingGameBuffer
		    .method796(-21994, Class65.anInt1145 + i_4_);
		FrameBuffer.outgoingGameBuffer
		    .method796(i_3_ ^ ~0x55e8, JKeyListener.anInt618 + i_5_);
	    }
	    if (i_6_ == 28) {
		FrameBuffer.outgoingGameBuffer.putFrame(0);
		FrameBuffer.outgoingGameBuffer.putWordLe(i_4_);
		FrameBuffer.outgoingGameBuffer.putWord(i_7_);
		FrameBuffer.outgoingGameBuffer
		    .putDwordLe(i_5_);
		GroundItem.anInt2242 = 0;
		Class65.anInt1137 = i_5_;
		Class39_Sub5_Sub5.anInt1739 = i_4_;
		Class25.anInt459 = 2;
		if (Class39_Sub11.anInt1478 == i_5_ >> 16)
		    Class25.anInt459 = 1;
		if (Class39_Sub5_Sub14.anInt1912 == i_5_ >> 16)
		    Class25.anInt459 = 3;
	    }
	    if (i_6_ == 35) {
		FrameBuffer.outgoingGameBuffer.putFrame(144);
		FrameBuffer.outgoingGameBuffer.method796(-21994,
								    i_4_);
		FrameBuffer.outgoingGameBuffer.putDword(i_5_);
		FrameBuffer.outgoingGameBuffer.putWordLe(i_7_);
		Class39_Sub5_Sub5.anInt1739 = i_4_;
		GroundItem.anInt2242 = 0;
		Class65.anInt1137 = i_5_;
		Class25.anInt459 = 2;
		if (Class39_Sub11.anInt1478 == i_5_ >> 16)
		    Class25.anInt459 = 1;
		if (i_5_ >> 16 == Class39_Sub5_Sub14.anInt1912)
		    Class25.anInt459 = 3;
	    }
	    if (i_6_ == 14) {
		Widget class39_sub5_sub17
		    = Class62_Sub2.method1081(i_5_, i_4_, 0);
		if (class39_sub5_sub17 != null) {
		    Class39_Sub5_Sub4_Sub4.method509((byte) 39);
		    Class25.method281(class39_sub5_sub17.method751((byte) -19),
				      i_4_, i_5_, true);
		    Class39_Sub14.aBoolean1520 = true;
		    Class13.anInt208 = 0;
		    Client.aClass3_1273
			= class39_sub5_sub17.method762(HashTable.aBoolean361,
						       64);
		    if (Client.aClass3_1273 == null)
			Client.aClass3_1273 = Class41.aClass3_774;
		    if (class39_sub5_sub17.aBoolean2013)
			Class14.aClass3_216
			    = (Class39_Sub5_Sub11.method708
			       ((new JString[]
				 { class39_sub5_sub17.aClass3_2065,
				   Class39_Sub5_Sub4.aClass3_1728 })));
		    else
			Class14.aClass3_216
			    = (Class39_Sub5_Sub11.method708
			       ((new JString[]
				 { Class41.aClass3_783,
				   class39_sub5_sub17.aClass3_2066,
				   Class39_Sub5_Sub4.aClass3_1728 })));
		    if (Class41.anInt776 == 16
			&& !class39_sub5_sub17.aBoolean2013) {
			Node.anInt728 = 3;
			IsaacPrng.aBoolean1089 = true;
			Class39_Sub14.aBoolean1520 = true;
		    }
		}
	    } else {
		if (i_6_ == 42) {
		    Npc class39_sub5_sub4_sub4_sub1
			= (GroundItem
			   .aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_7_]);
		    if (class39_sub5_sub4_sub4_sub1 != null) {
			Class26.method293
			    (i_3_ + 24133, 0,
			     class39_sub5_sub4_sub4_sub1.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub1.anIntArray2314[0]);
			Class4.anInt80 = 2;
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class26.anInt503 = 0;
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(154);
			FrameBuffer.outgoingGameBuffer
			    .putWord(i_7_);
		    }
		}
		if (i_6_ == 47) {
		    Npc class39_sub5_sub4_sub4_sub1
			= (GroundItem
			   .aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_7_]);
		    if (class39_sub5_sub4_sub4_sub1 != null) {
			Class26.method293
			    (24134, 0,
			     class39_sub5_sub4_sub4_sub1.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub1.anIntArray2314[0]);
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class26.anInt503 = 0;
			Class4.anInt80 = 2;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(146);
			FrameBuffer.outgoingGameBuffer
			    .method819(Class39_Sub10.anInt1430, -1);
			FrameBuffer.outgoingGameBuffer
			    .putWord(Class63.anInt1128);
			FrameBuffer.outgoingGameBuffer
			    .putWord(Class23.anInt428);
			FrameBuffer.outgoingGameBuffer
			    .method796(-21994, i_7_);
		    }
		}
		if (i_6_ == 53) {
		    Npc class39_sub5_sub4_sub4_sub1
			= (GroundItem
			   .aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_7_]);
		    if (class39_sub5_sub4_sub4_sub1 != null) {
			Class26.method293
			    (24134, 0,
			     class39_sub5_sub4_sub4_sub1.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub1.anIntArray2314[0]);
			Class26.anInt503 = 0;
			Class4.anInt80 = 2;
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(222);
			FrameBuffer.outgoingGameBuffer
			    .method796(i_3_ - 21995, i_7_);
		    }
		}
		if (i_6_ == 4) {
		    boolean bool
			= Class26.method293(i_3_ + 24133, 0, i_5_, 0, 2, 0, 0,
					    false, 0,
					    (Cache
					     .aClass39_Sub5_Sub4_Sub4_Sub2_109
					     .anIntArray2314[0]),
					    (Cache
					     .aClass39_Sub5_Sub4_Sub4_Sub2_109
					     .anIntArray2255[0]),
					    i_4_);
		    if (!bool)
			bool = (Class26.method293
				(24134, 0, i_5_, 0, 2, 1, 1, false, 0,
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2314[0]),
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2255[0]),
				 i_4_));
		    Class4.anInt80 = 2;
		    Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
		    Class26.anInt503 = 0;
		    Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
		    FrameBuffer.outgoingGameBuffer
			.putFrame(114);
		    FrameBuffer.outgoingGameBuffer
			.putWordLe(Class63.anInt1128);
		    FrameBuffer.outgoingGameBuffer
			.method832(i_5_ + JKeyListener.anInt618, (byte) -30);
		    FrameBuffer.outgoingGameBuffer
			.putDword(Class39_Sub10.anInt1430);
		    FrameBuffer.outgoingGameBuffer
			.method832(i_4_ + Class65.anInt1145, (byte) -30);
		    FrameBuffer.outgoingGameBuffer
			.putWordLe(i_7_);
		    FrameBuffer.outgoingGameBuffer
			.method796(-21994, Class23.anInt428);
		}
		if (i_6_ == 49) {
		    FrameBuffer.outgoingGameBuffer
			.putFrame(171);
		    FrameBuffer.outgoingGameBuffer
			.method832(i_4_, (byte) -30);
		    FrameBuffer.outgoingGameBuffer
			.putDword(i_5_);
		    FrameBuffer.outgoingGameBuffer
			.putWordLe(i_7_);
		    Class65.anInt1137 = i_5_;
		    Class25.anInt459 = 2;
		    GroundItem.anInt2242 = 0;
		    Class39_Sub5_Sub5.anInt1739 = i_4_;
		    if (Class39_Sub11.anInt1478 == i_5_ >> 16)
			Class25.anInt459 = 1;
		    if (Class39_Sub5_Sub14.anInt1912 == i_5_ >> 16)
			Class25.anInt459 = 3;
		}
		if (i_6_ == 26) {
		    FrameBuffer.outgoingGameBuffer
			.putFrame(188);
		    FrameBuffer.outgoingGameBuffer.method796(-21994,
									i_4_);
		    FrameBuffer.outgoingGameBuffer.method796(-21994,
									i_7_);
		    FrameBuffer.outgoingGameBuffer.method819(i_5_,
									-1);
		    Class39_Sub5_Sub5.anInt1739 = i_4_;
		    Class25.anInt459 = 2;
		    if (Class39_Sub11.anInt1478 == i_5_ >> 16)
			Class25.anInt459 = 1;
		    if (Class39_Sub5_Sub14.anInt1912 == i_5_ >> 16)
			Class25.anInt459 = 3;
		    Class65.anInt1137 = i_5_;
		    GroundItem.anInt2242 = 0;
		}
		if (i_6_ == 11) {
		    boolean bool
			= Class26.method293(i_3_ ^ 0x5e47, 0, i_5_, 0, 2, 0, 0,
					    false, 0,
					    (Cache
					     .aClass39_Sub5_Sub4_Sub4_Sub2_109
					     .anIntArray2314[0]),
					    (Cache
					     .aClass39_Sub5_Sub4_Sub4_Sub2_109
					     .anIntArray2255[0]),
					    i_4_);
		    if (!bool)
			bool = (Class26.method293
				(24134, 0, i_5_, 0, 2, 1, 1, false, 0,
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2314[0]),
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2255[0]),
				 i_4_));
		    Class26.anInt503 = 0;
		    Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
		    Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
		    Class4.anInt80 = 2;
		    FrameBuffer.outgoingGameBuffer
			.putFrame(38);
		    FrameBuffer.outgoingGameBuffer
			.putWordLe(i_7_);
		    FrameBuffer.outgoingGameBuffer
			.method832(JKeyListener.anInt618 + i_5_, (byte) -30);
		    FrameBuffer.outgoingGameBuffer
			.putWord(Class65.anInt1145 + i_4_);
		}
		if (i_6_ == 43) {
		    Npc.method526(i_7_, i_4_, false,
							  i_5_);
		    FrameBuffer.outgoingGameBuffer
			.putFrame(69);
		    FrameBuffer.outgoingGameBuffer
			.putWordLe((i_7_ & 0x1fffcb21) >> 14);
		    FrameBuffer.outgoingGameBuffer
			.putWord(i_5_ + JKeyListener.anInt618);
		    FrameBuffer.outgoingGameBuffer
			.putWord(Class65.anInt1145 + i_4_);
		}
		if (i_6_ == 48) {
		    Npc class39_sub5_sub4_sub4_sub1
			= (GroundItem
			   .aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_7_]);
		    if (class39_sub5_sub4_sub4_sub1 != null) {
			Class26.method293
			    (i_3_ + 24133, 0,
			     class39_sub5_sub4_sub4_sub1.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub1.anIntArray2314[0]);
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class26.anInt503 = 0;
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class4.anInt80 = 2;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(200);
			FrameBuffer.outgoingGameBuffer
			    .putWordLe(Class31.anInt570);
			FrameBuffer.outgoingGameBuffer
			    .putDwordLe(Class41.anInt775);
			FrameBuffer.outgoingGameBuffer
			    .putWord(i_7_);
		    }
		}
		if (i_6_ == 58) {
		    Player class39_sub5_sub4_sub4_sub2
			= Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_7_];
		    if (class39_sub5_sub4_sub4_sub2 != null) {
			Class26.method293
			    (24134, 0,
			     class39_sub5_sub4_sub4_sub2.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub2.anIntArray2314[0]);
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class26.anInt503 = 0;
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class4.anInt80 = 2;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(104);
			FrameBuffer.outgoingGameBuffer
			    .method827(Class39_Sub10.anInt1430, -334352184);
			FrameBuffer.outgoingGameBuffer
			    .method796(i_3_ ^ ~0x55e8, Class23.anInt428);
			FrameBuffer.outgoingGameBuffer
			    .putWordLe(i_7_);
			FrameBuffer.outgoingGameBuffer
			    .method796(i_3_ ^ ~0x55e8, Class63.anInt1128);
		    }
		}
		if (i_6_ == 45
		    && Npc.method526(i_7_, i_4_, false,
							     i_5_)) {
		    FrameBuffer.outgoingGameBuffer
			.putFrame(195);
		    FrameBuffer.outgoingGameBuffer
			.method796(i_3_ - 21995, i_5_ + JKeyListener.anInt618);
		    FrameBuffer.outgoingGameBuffer
			.putWordLe((i_7_ & 0x1ffffabb) >> 14);
		    FrameBuffer.outgoingGameBuffer
			.method832(Class63.anInt1128, (byte) -30);
		    FrameBuffer.outgoingGameBuffer
			.putWordLe(Class23.anInt428);
		    FrameBuffer.outgoingGameBuffer
			.method796(i_3_ - 21995, i_4_ + Class65.anInt1145);
		    FrameBuffer.outgoingGameBuffer
			.putDwordLe(Class39_Sub10.anInt1430);
		}
		if (i_6_ == 1004) {
		    Widget class39_sub5_sub17
			= Class37.getWidget(i_5_);
		    if (class39_sub5_sub17 != null
			&& class39_sub5_sub17.anIntArray2073[i_4_] >= 100000)
			JMouseListener.method902
			    (Class66.blankString,
			     (Class39_Sub5_Sub11.method708
			      ((new JString[]
				{ AbstractImage.method1007((byte) 71,
						     (class39_sub5_sub17
						      .anIntArray2073[i_4_])),
				  Class30.aClass3_543,
				  (Class26.getItemDefinition(i_7_)
				   .aClass3_1661) }))),
			     false, 0);
		    else {
			OndemandRequest.anInt1724++;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(219);
			FrameBuffer.outgoingGameBuffer
			    .method832(i_7_, (byte) -30);
		    }
		    Class39_Sub5_Sub5.anInt1739 = i_4_;
		    Class25.anInt459 = 2;
		    if (Class39_Sub11.anInt1478 == i_5_ >> 16)
			Class25.anInt459 = 1;
		    if (i_5_ >> 16 == Class39_Sub5_Sub14.anInt1912)
			Class25.anInt459 = 3;
		    Class65.anInt1137 = i_5_;
		    GroundItem.anInt2242 = 0;
		}
		if (i_6_ == 25) {
		    FrameBuffer.outgoingGameBuffer
			.putFrame(221);
		    FrameBuffer.outgoingGameBuffer
			.method827(i_5_, -334352184);
		    FrameBuffer.outgoingGameBuffer.method796(-21994,
									i_4_);
		    FrameBuffer.outgoingGameBuffer
			.putWord(i_7_);
		    Class65.anInt1137 = i_5_;
		    Class25.anInt459 = 2;
		    GroundItem.anInt2242 = 0;
		    Class39_Sub5_Sub5.anInt1739 = i_4_;
		    if (i_5_ >> 16 == Class39_Sub11.anInt1478)
			Class25.anInt459 = 1;
		    if (Class39_Sub5_Sub14.anInt1912 == i_5_ >> 16)
			Class25.anInt459 = 3;
		}
		if (i_6_ == 6) {
		    Npc.method526(i_7_, i_4_, false,
							  i_5_);
		    FrameBuffer.outgoingGameBuffer
			.putFrame(202);
		    FrameBuffer.outgoingGameBuffer
			.putWord(i_7_ >> 14 & 0x7fff);
		    FrameBuffer.outgoingGameBuffer
			.method832(Class65.anInt1145 + i_4_, (byte) -30);
		    FrameBuffer.outgoingGameBuffer
			.method796(i_3_ ^ ~0x55e8, JKeyListener.anInt618 + i_5_);
		}
		if (i_6_ == 19) {
		    FrameBuffer.outgoingGameBuffer
			.putFrame(178);
		    FrameBuffer.outgoingGameBuffer.method796(-21994,
									i_4_);
		    FrameBuffer.outgoingGameBuffer
			.method832(i_7_, (byte) -30);
		    FrameBuffer.outgoingGameBuffer
			.method819(i_5_, i_3_ - 2);
		    Class25.anInt459 = 2;
		    if (i_5_ >> 16 == Class39_Sub11.anInt1478)
			Class25.anInt459 = 1;
		    GroundItem.anInt2242 = 0;
		    if (i_5_ >> 16 == Class39_Sub5_Sub14.anInt1912)
			Class25.anInt459 = 3;
		    Class39_Sub5_Sub5.anInt1739 = i_4_;
		    Class65.anInt1137 = i_5_;
		}
		if (i_6_ == 33) {
		    if (!Class39_Sub12.aBoolean1493)
			Class44.aClass38_836.method355(Resizable.vx(Class39_Sub4.anInt1329),
						       Resizable.vy(Bzip2Block.anInt1054));
		    else
			Class44.aClass38_836.method355(Resizable.vx(i_4_), Resizable.vy(i_5_));
		}
		if (i_6_ == 24) {
		    Class62_Sub2.method1084((byte) -35, IsaacPrng.anInt1095);
		    IsaacPrng.anInt1095 = -1;
		    Class14.aBoolean245 = true;
		}
		if (i_6_ == 34) {
		    boolean bool
			= Class26.method293(24134, 0, i_5_, 0, 2, 0, 0, false,
					    0,
					    (Cache
					     .aClass39_Sub5_Sub4_Sub4_Sub2_109
					     .anIntArray2314[0]),
					    (Cache
					     .aClass39_Sub5_Sub4_Sub4_Sub2_109
					     .anIntArray2255[0]),
					    i_4_);
		    if (!bool)
			bool = (Class26.method293
				(i_3_ + 24133, 0, i_5_, 0, 2, 1, 1, false, 0,
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2314[0]),
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2255[0]),
				 i_4_));
		    Class4.anInt80 = 2;
		    Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
		    Class26.anInt503 = 0;
		    Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
		    FrameBuffer.outgoingGameBuffer
			.putFrame(85);
		    FrameBuffer.outgoingGameBuffer
			.method832(i_5_ + JKeyListener.anInt618, (byte) -30);
		    FrameBuffer.outgoingGameBuffer
			.method796(i_3_ ^ ~0x55e8, Class65.anInt1145 + i_4_);
		    FrameBuffer.outgoingGameBuffer
			.method796(i_3_ ^ ~0x55e8, i_7_);
		}
		if (i_6_ == 51)
		    Class34.method333(i_5_, i_4_, i_7_, 1);
		if (i_6_ == 1) {
		    Player class39_sub5_sub4_sub4_sub2
			= Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_7_];
		    if (class39_sub5_sub4_sub4_sub2 != null) {
			Class26.method293
			    (24134, 0,
			     class39_sub5_sub4_sub4_sub2.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub2.anIntArray2314[0]);
			Class26.anInt503 = 0;
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class4.anInt80 = 2;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(206);
			FrameBuffer.outgoingGameBuffer
			    .putWord(i_7_);
		    }
		}
		if (i_6_ == 10) {
		    JString class3 = Class33.aClass3Array601[i];
		    int i_9_ = class3.method80(22938,
					       Class39_Sub5_Sub4.aClass3_1728);
		    if (i_9_ != -1) {
			long l = class3.method85(i_3_ - 59, i_9_ + 5).method69
				     ((byte) -45).encodeBase37();
			int i_10_ = -1;
			for (int i_11_ = 0; Class4.anInt62 > i_11_; i_11_++) {
			    if (l == ClientApplet.aLongArray2[i_11_]) {
				i_10_ = i_11_;
				break;
			    }
			}
			if (i_10_ != -1 && (Player
					    .anIntArray2533[i_10_]) > 0) {
			    Class14.aBoolean245 = true;
			    Class39_Sub12.aBoolean1489 = true;
			    Class15.anInt277 = 3;
			    Class39_Sub5_Sub4_Sub4.anInt2285 = 0;
			    Class66.aClass3_1154 = Class66.blankString;
			    Huffmans.aLong752 = ClientApplet.aLongArray2[i_10_];
			    Class66.aClass3_1150
				= (Class39_Sub5_Sub11.method708
				   (new JString[] { Class41.aClass3_781,
						   (Projectile
						    .aClass3Array2188
						    [i_10_]) }));
			}
		    }
		}
		if (i_3_ != 1)
		    method278(87, -70);
		if (i_6_ == 36) {
		    FrameBuffer.outgoingGameBuffer
			.putFrame(54);
		    FrameBuffer.outgoingGameBuffer
			.putDword(i_5_);
		    Class39_Sub5_Sub12.anInt1848++;
		    Widget class39_sub5_sub17
			= Class37.getWidget(i_5_);
		    if (class39_sub5_sub17.scriptOpcodes != null
			&& class39_sub5_sub17.scriptOpcodes[0][0] == 5) {
			int i_12_
			    = class39_sub5_sub17.scriptOpcodes[0][1];
			Class66.stateValues[i_12_]
			    = -Class66.stateValues[i_12_] + 1;
			Node.method408(i_12_, 1);
			Class39_Sub14.aBoolean1520 = true;
		    }
		}
		if (i_6_ == 1001) {
		    Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
		    Class26.anInt503 = 0;
		    Class4.anInt80 = 2;
		    Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
		    Npc class39_sub5_sub4_sub4_sub1
			= (GroundItem
			   .aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_7_]);
		    if (class39_sub5_sub4_sub4_sub1 != null) {
			NpcDefinition class39_sub5_sub13
			    = (class39_sub5_sub4_sub4_sub1
			       .aClass39_Sub5_Sub13_2492);
			if (class39_sub5_sub13.anIntArray1878 != null)
			    class39_sub5_sub13
				= class39_sub5_sub13.method721(5585);
			if (class39_sub5_sub13 != null) {
			    FrameBuffer.outgoingGameBuffer
				.putFrame(158);
			    FrameBuffer.outgoingGameBuffer.putWord
				(class39_sub5_sub13.id);
			}
		    }
		}
		if (i_6_ == 15 || i_6_ == 37) {
		    JString class3 = Class33.aClass3Array601[i];
		    int i_13_
			= class3.method80(22938,
					  Class39_Sub5_Sub4.aClass3_1728);
		    if (i_13_ != -1) {
			class3 = class3.method85(-58, i_13_ + 5)
				     .method69((byte) -45);
			JString class3_14_
			    = class3.method81(-32769).formatUsername();
			boolean bool = false;
			for (int i_15_ = 0; i_15_ < TraversalMap.anInt515;
			     i_15_++) {
			    Player class39_sub5_sub4_sub4_sub2
				= (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211
				   [Class1.anIntArray40[i_15_]]);
			    if (class39_sub5_sub4_sub4_sub2 != null
				&& (class39_sub5_sub4_sub4_sub2.aClass3_2521
				    != null)
				&& class39_sub5_sub4_sub4_sub2.aClass3_2521
				       .method97(class3_14_, -66)) {
				bool = true;
				Class26.method293
				    (24134, 0,
				     (class39_sub5_sub4_sub4_sub2
				      .anIntArray2255[0]),
				     0, 2, 1, 1, false, 0,
				     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				      .anIntArray2314[0]),
				     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				      .anIntArray2255[0]),
				     (class39_sub5_sub4_sub4_sub2
				      .anIntArray2314[0]));
				if (i_6_ == 15) {
				    FrameBuffer
					.outgoingGameBuffer
					.putFrame(101);
				    Class39_Sub5_Sub16.anInt1976++;
				    FrameBuffer
					.outgoingGameBuffer.method796
					(-21994, Class1.anIntArray40[i_15_]);
				}
				if (i_6_ == 37) {
				    FrameBuffer
					.outgoingGameBuffer
					.putFrame(11);
				    FrameBuffer
					.outgoingGameBuffer.method796
					(-21994, Class1.anIntArray40[i_15_]);
				    Class39_Sub5_Sub4_Sub2.anInt2220++;
				}
				break;
			    }
			}
			if (!bool)
			    JMouseListener.method902(Class66.blankString,
					      (Class39_Sub5_Sub11.method708
					       ((new JString[]
						 { Buffer.aClass3_1365,
						   class3_14_ }))),
					      false, 0);
		    }
		}
		if (i_6_ == 27) {
		    boolean bool
			= Class26.method293(i_3_ + 24133, 0, i_5_, 0, 2, 0, 0,
					    false, 0,
					    (Cache
					     .aClass39_Sub5_Sub4_Sub4_Sub2_109
					     .anIntArray2314[0]),
					    (Cache
					     .aClass39_Sub5_Sub4_Sub4_Sub2_109
					     .anIntArray2255[0]),
					    i_4_);
		    if (!bool)
			bool = (Class26.method293
				(24134, 0, i_5_, 0, 2, 1, 1, false, 0,
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2314[0]),
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2255[0]),
				 i_4_));
		    Class26.anInt503 = 0;
		    Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
		    Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
		    Class4.anInt80 = 2;
		    FrameBuffer.outgoingGameBuffer
			.putFrame(64);
		    FrameBuffer.outgoingGameBuffer
			.method796(i_3_ ^ ~0x55e8, i_7_);
		    FrameBuffer.outgoingGameBuffer
			.method827(Class41.anInt775, -334352184);
		    FrameBuffer.outgoingGameBuffer
			.putWord(JKeyListener.anInt618 + i_5_);
		    FrameBuffer.outgoingGameBuffer
			.method796(i_3_ - 21995, Class65.anInt1145 + i_4_);
		    FrameBuffer.outgoingGameBuffer
			.method796(i_3_ ^ ~0x55e8, Class31.anInt570);
		}
		if (i_6_ == 16) {
		    FrameBuffer.outgoingGameBuffer
			.putFrame(29);
		    FrameBuffer.outgoingGameBuffer.method819(i_5_,
									-1);
		    FrameBuffer.outgoingGameBuffer
			.putWord(i_7_);
		    FrameBuffer.outgoingGameBuffer
			.method832(i_4_, (byte) -30);
		    Class25.anInt459 = 2;
		    Class39_Sub5_Sub5.anInt1739 = i_4_;
		    GroundItem.anInt2242 = 0;
		    Class65.anInt1137 = i_5_;
		    if (i_5_ >> 16 == Class39_Sub11.anInt1478)
			Class25.anInt459 = 1;
		    if (i_5_ >> 16 == Class39_Sub5_Sub14.anInt1912)
			Class25.anInt459 = 3;
		}
		if (i_6_ == 7) {
		    Player class39_sub5_sub4_sub4_sub2
			= Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_7_];
		    if (class39_sub5_sub4_sub4_sub2 != null) {
			Class26.method293
			    (24134, 0,
			     class39_sub5_sub4_sub4_sub2.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub2.anIntArray2314[0]);
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class4.anInt80 = 2;
			Class26.anInt503 = 0;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(236);
			FrameBuffer.outgoingGameBuffer
			    .method832(i_7_, (byte) -30);
			FrameBuffer.outgoingGameBuffer
			    .method832(Class31.anInt570, (byte) -30);
			FrameBuffer.outgoingGameBuffer
			    .putDwordLe(Class41.anInt775);
		    }
		}
		if (i_6_ == 18) {
		    FrameBuffer.outgoingGameBuffer
			.putFrame(147);
		    FrameBuffer.outgoingGameBuffer
			.method832(Class23.anInt428, (byte) -30);
		    FrameBuffer.outgoingGameBuffer
			.putWordLe(Class63.anInt1128);
		    FrameBuffer.outgoingGameBuffer
			.putWordLe(i_7_);
		    FrameBuffer.outgoingGameBuffer
			.putDwordLe(i_5_);
		    FrameBuffer.outgoingGameBuffer
			.method827(Class39_Sub10.anInt1430, -334352184);
		    FrameBuffer.outgoingGameBuffer
			.putWord(i_4_);
		    GroundItem.anInt2242 = 0;
		    Class65.anInt1137 = i_5_;
		    Class25.anInt459 = 2;
		    if (i_5_ >> 16 == Class39_Sub11.anInt1478)
			Class25.anInt459 = 1;
		    if (i_5_ >> 16 == Class39_Sub5_Sub14.anInt1912)
			Class25.anInt459 = 3;
		    Class39_Sub5_Sub5.anInt1739 = i_4_;
		}
		if (i_6_ == 55) {
		    Npc.method526(i_7_, i_4_, false,
							  i_5_);
		    FrameBuffer.outgoingGameBuffer
			.putFrame(47);
		    FrameBuffer.outgoingGameBuffer
			.method832(JKeyListener.anInt618 + i_5_, (byte) -30);
		    FrameBuffer.outgoingGameBuffer
			.method796(-21994, i_7_ >> 14 & 0x7fff);
		    FrameBuffer.outgoingGameBuffer
			.method832(Class65.anInt1145 + i_4_, (byte) -30);
		}
		if (i_6_ == 46) {
		    Npc class39_sub5_sub4_sub4_sub1
			= (GroundItem
			   .aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_7_]);
		    if (class39_sub5_sub4_sub4_sub1 != null) {
			Class26.method293
			    (24134, 0,
			     class39_sub5_sub4_sub4_sub1.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub1.anIntArray2314[0]);
			Class26.anInt503 = 0;
			Class4.anInt80 = 2;
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(87);
			FrameBuffer.outgoingGameBuffer
			    .putWord(i_7_);
		    }
		}
		if (i_6_ == 13) {
		    Npc class39_sub5_sub4_sub4_sub1
			= (GroundItem
			   .aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_7_]);
		    if (class39_sub5_sub4_sub4_sub1 != null) {
			Class26.method293
			    (24134, 0,
			     class39_sub5_sub4_sub4_sub1.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub1.anIntArray2314[0]);
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class26.anInt503 = 0;
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class4.anInt80 = 2;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(89);
			FrameBuffer.outgoingGameBuffer
			    .method796(-21994, i_7_);
		    }
		}
		if (i_6_ == 56) {
		    Player class39_sub5_sub4_sub4_sub2
			= Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_7_];
		    if (class39_sub5_sub4_sub4_sub2 != null) {
			Class26.method293
			    (24134, 0,
			     class39_sub5_sub4_sub4_sub2.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub2.anIntArray2314[0]);
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class26.anInt503 = 0;
			Class39_Sub5_Sub16.anInt1976++;
			Class4.anInt80 = 2;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(101);
			FrameBuffer.outgoingGameBuffer
			    .method796(i_3_ - 21995, i_7_);
		    }
		}
		if (i_6_ == 20) {
		    boolean bool = true;
		    Widget class39_sub5_sub17
			= Class37.getWidget(i_5_);
		    if (class39_sub5_sub17.anInt2078 > 0)
			bool = Class48.method951(class39_sub5_sub17,
						 (byte) -36);
		    if (bool) {
			FrameBuffer.outgoingGameBuffer
			    .putFrame(54);
			FrameBuffer.outgoingGameBuffer
			    .putDword(i_5_);
			Class39_Sub5_Sub12.anInt1848++;
			if (i_5_ == ((269 << 16) | 99)) {
			    Class55.characterDesignActive = false;
			}
		    }
		}
		if (i_6_ == 57) {
		    Npc.method526(i_7_, i_4_, false,
							  i_5_);
		    FrameBuffer.outgoingGameBuffer
			.putFrame(245);
		    FrameBuffer.outgoingGameBuffer
			.putWord(i_4_ + Class65.anInt1145);
		    FrameBuffer.outgoingGameBuffer
			.putWord(i_7_ >> 14 & 0x7fff);
		    FrameBuffer.outgoingGameBuffer
			.method832(JKeyListener.anInt618 + i_5_, (byte) -30);
		}
		if (i_6_ == 41) {
		    Class39_Sub5_Sub12.anInt1848++;
		    FrameBuffer.outgoingGameBuffer
			.putFrame(54);
		    FrameBuffer.outgoingGameBuffer
			.putDword(i_5_);
		    Widget class39_sub5_sub17
			= Class37.getWidget(i_5_);
		    if (class39_sub5_sub17.scriptOpcodes != null
			&& class39_sub5_sub17.scriptOpcodes[0][0] == 5) {
			int i_16_
			    = class39_sub5_sub17.scriptOpcodes[0][1];
			if (Class66.stateValues[i_16_]
			    != class39_sub5_sub17.conditionValues[0]) {
			    Class66.stateValues[i_16_]
				= class39_sub5_sub17.conditionValues[0];
			    Node.method408(i_16_, i_3_);
			    Class39_Sub14.aBoolean1520 = true;
			}
		    }
		}
		if (i_6_ == 1003) {
		    OndemandRequest.anInt1724++;
		    Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
		    Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
		    Class4.anInt80 = 2;
		    Class26.anInt503 = 0;
		    FrameBuffer.outgoingGameBuffer
			.putFrame(219);
		    FrameBuffer.outgoingGameBuffer
			.method832(i_7_, (byte) -30);
		}
		if (i_6_ == 17) {
		    Npc class39_sub5_sub4_sub4_sub1
			= (GroundItem
			   .aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_7_]);
		    if (class39_sub5_sub4_sub4_sub1 != null) {
			Class26.method293
			    (24134, 0,
			     class39_sub5_sub4_sub4_sub1.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub1.anIntArray2314[0]);
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class4.anInt80 = 2;
			Class26.anInt503 = 0;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(224);
			FrameBuffer.outgoingGameBuffer
			    .putWord(i_7_);
		    }
		}
		if (i_6_ == 22) {
		    Player class39_sub5_sub4_sub4_sub2
			= Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_7_];
		    if (class39_sub5_sub4_sub4_sub2 != null) {
			Class26.method293
			    (24134, 0,
			     class39_sub5_sub4_sub4_sub2.anIntArray2255[0], 0,
			     2, 1, 1, false, 0,
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2314[0]),
			     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			      .anIntArray2255[0]),
			     class39_sub5_sub4_sub4_sub2.anIntArray2314[0]);
			Class4.anInt80 = 2;
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class26.anInt503 = 0;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(169);
			FrameBuffer.outgoingGameBuffer
			    .method796(i_3_ ^ ~0x55e8, i_7_);
		    }
		}
		if (i_6_ == 30) {
		    FrameBuffer.outgoingGameBuffer
			.putFrame(113);
		    FrameBuffer.outgoingGameBuffer
			.method827(i_5_, -334352184);
		    FrameBuffer.outgoingGameBuffer
			.putWordLe(i_7_);
		    FrameBuffer.outgoingGameBuffer.method796(-21994,
									i_4_);
		    Class65.anInt1137 = i_5_;
		    Class39_Sub5_Sub5.anInt1739 = i_4_;
		    GroundItem.anInt2242 = 0;
		    Class25.anInt459 = 2;
		    if (i_5_ >> 16 == Class39_Sub11.anInt1478)
			Class25.anInt459 = 1;
		    if (Class39_Sub5_Sub14.anInt1912 == i_5_ >> 16)
			Class25.anInt459 = 3;
		}
		if (i_6_ == 23) {
		    Class39_Sub5_Sub4_Sub4.method509((byte) 39);
		    Class13.anInt208 = 1;
		    Class63.anInt1128 = i_7_;
		    Class23.anInt428 = i_4_;
		    Class39_Sub14.aBoolean1520 = true;
		    Class39_Sub10.anInt1430 = i_5_;
		    Class39_Sub10.aClass3_1436
			= (Class39_Sub5_Sub11.method708
			   ((new JString[]
			     { FrameBuffer.aClass3_2147,
			       Class26.getItemDefinition(i_7_).aClass3_1661,
			       Class39_Sub5_Sub4.aClass3_1728 })));
		    if (Class39_Sub10.aClass3_1436 == null)
			Class39_Sub10.aClass3_1436 = Class36.aClass3_633;
		} else {
		    if (i_6_ == 3) {
			boolean bool
			    = (Class26.method293
			       (24134, 0, i_5_, 0, 2, 0, 0, false, 0,
				(Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				 .anIntArray2314[0]),
				(Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				 .anIntArray2255[0]),
				i_4_));
			if (!bool)
			    bool = (Class26.method293
				    (24134, 0, i_5_, 0, 2, 1, 1, false, 0,
				     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				      .anIntArray2314[0]),
				     (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				      .anIntArray2255[0]),
				     i_4_));
			Class26.anInt503 = 0;
			Class4.anInt80 = 2;
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(136);
			FrameBuffer.outgoingGameBuffer
			    .method832(i_4_ + Class65.anInt1145, (byte) -30);
			FrameBuffer.outgoingGameBuffer
			    .putWordLe(i_7_);
			FrameBuffer.outgoingGameBuffer
			    .putWord(i_5_ + JKeyListener.anInt618);
		    }
		    if (i_6_ == 29)
			Class55.method999(31121);
		    if (i_6_ == 39) {
			FrameBuffer.outgoingGameBuffer
			    .putFrame(145);
			FrameBuffer.outgoingGameBuffer
			    .putDword(Class41.anInt775);
			FrameBuffer.outgoingGameBuffer
			    .putDword(i_5_);
			FrameBuffer.outgoingGameBuffer
			    .method832(Class31.anInt570, (byte) -30);
			FrameBuffer.outgoingGameBuffer
			    .method796(-21994, i_4_);
		    }
		    if (i_6_ == 9) {
			JString class3 = Class33.aClass3Array601[i];
			int i_17_
			    = class3.method80(22938,
					      Class39_Sub5_Sub4.aClass3_1728);
			if (i_17_ != -1) {
			    if (Class39_Sub11.anInt1478 == -1) {
				Class55.method999(31121);
				if (Class48.anInt907 != -1) {
				    Class66.aClass3_1163
					= class3.method85
					      (i_3_ ^ ~0x38, i_17_ + 5)
					      .method69((byte) -45);
				    Class39_Sub12.aBoolean1487 = false;
				    Class26.anInt473
					= Class39_Sub11.anInt1478
					= Class48.anInt907;
				}
			    } else
				JMouseListener.method902(Class66.blankString,
						  Class31.aClass3_572, false,
						  0);
			}
		    }
		    if (i_6_ == 54) {
			FrameBuffer.outgoingGameBuffer
			    .putFrame(48);
			FrameBuffer.outgoingGameBuffer
			    .method832(i_4_, (byte) -30);
			FrameBuffer.outgoingGameBuffer
			    .method819(i_5_, -1);
			FrameBuffer.outgoingGameBuffer
			    .putWordLe(i_7_);
			Class25.anInt459 = 2;
			Class65.anInt1137 = i_5_;
			Class39_Sub5_Sub5.anInt1739 = i_4_;
			if (Class39_Sub11.anInt1478 == i_5_ >> 16)
			    Class25.anInt459 = 1;
			if (Class39_Sub5_Sub14.anInt1912 == i_5_ >> 16)
			    Class25.anInt459 = 3;
			GroundItem.anInt2242 = 0;
		    }
		    if (i_6_ == 1006) {
			Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			Class4.anInt80 = 2;
			Class26.anInt503 = 0;
			FrameBuffer.outgoingGameBuffer
			    .putFrame(36);
			FrameBuffer.outgoingGameBuffer
			    .putWordLe(i_7_ >> 14 & 0x7fff);
		    }
		    if (i_6_ == 5) {
			FrameBuffer.outgoingGameBuffer
			    .putFrame(243);
			FrameBuffer.outgoingGameBuffer
			    .putDword(i_5_);
			FrameBuffer.outgoingGameBuffer
			    .method832(i_4_, (byte) -30);
			FrameBuffer.outgoingGameBuffer
			    .method827(Class41.anInt775, -334352184);
			FrameBuffer.outgoingGameBuffer
			    .method796(i_3_ - 21995, i_7_);
			FrameBuffer.outgoingGameBuffer
			    .putWordLe(Class31.anInt570);
			Class39_Sub5_Sub5.anInt1739 = i_4_;
			Class25.anInt459 = 2;
			Class65.anInt1137 = i_5_;
			if (i_5_ >> 16 == Class39_Sub11.anInt1478)
			    Class25.anInt459 = 1;
			GroundItem.anInt2242 = 0;
			if (i_5_ >> 16 == Class39_Sub5_Sub14.anInt1912)
			    Class25.anInt459 = 3;
		    }
		    if (i_6_ == 52) {
			Player class39_sub5_sub4_sub4_sub2
			    = (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211
			       [i_7_]);
			if (class39_sub5_sub4_sub4_sub2 != null) {
			    Class26.method293
				(24134, 0,
				 class39_sub5_sub4_sub4_sub2.anIntArray2255[0],
				 0, 2, 1, 1, false, 0,
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2314[0]),
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2255[0]),
				 (class39_sub5_sub4_sub4_sub2.anIntArray2314
				  [0]));
			    Class4.anInt80 = 2;
			    Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			    Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			    Class26.anInt503 = 0;
			    FrameBuffer.outgoingGameBuffer
				.putFrame(229);
			    FrameBuffer.outgoingGameBuffer
				.method832(i_7_, (byte) -30);
			}
		    }
		    if (i_6_ == 44) {
			Player class39_sub5_sub4_sub4_sub2
			    = (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211
			       [i_7_]);
			if (class39_sub5_sub4_sub4_sub2 != null) {
			    Class26.method293
				(24134, 0,
				 class39_sub5_sub4_sub4_sub2.anIntArray2255[0],
				 0, 2, 1, 1, false, 0,
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2314[0]),
				 (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				  .anIntArray2255[0]),
				 (class39_sub5_sub4_sub4_sub2.anIntArray2314
				  [0]));
			    Class62_Sub1.anInt1590 = Class39_Sub4.anInt1329;
			    Class4.anInt80 = 2;
			    Class26.anInt503 = 0;
			    Class39_Sub5_Sub4_Sub2.anInt2220++;
			    Class62_Sub2.anInt1596 = Bzip2Block.anInt1054;
			    FrameBuffer.outgoingGameBuffer
				.putFrame(11);
			    FrameBuffer.outgoingGameBuffer
				.method796(-21994, i_7_);
			}
		    }
		    if (Class13.anInt208 != 0) {
			Class39_Sub14.aBoolean1520 = true;
			Class13.anInt208 = 0;
		    }
		    if (IsaacPrng.aBoolean1100) {
			Class39_Sub5_Sub4_Sub4.method509((byte) 39);
			Class39_Sub14.aBoolean1520 = true;
		    }
		}
	    }
	}
    }
    
    public static int method279(int i, boolean bool, int i_18_) {
	if (i < i_18_) {
	    int i_19_ = i;
	    i = i_18_;
	    i_18_ = i_19_;
	}
	int i_20_;
	for (/**/; i_18_ != 0; i_18_ = i_20_) {
	    i_20_ = i % i_18_;
	    i = i_18_;
	}
	return i;
    }
    
    public static void method280(boolean bool) {
	aClass39_Sub1_439 = null;
	aClass3_444 = null;
	aClass3_452 = null;
	aClass3_456 = null;
	aClass3_443 = null;
	aClass3_445 = null;
	arraySizes = null;
	aClass3_442 = null;
	anIntArrayArray455 = null;
	aClass3_446 = null;
	aClass3_441 = null;
	aClass3_447 = null;
    }
    
    static {
	aClass3_445
	    = Class39_Sub5_Sub9
		  .createJstring("Benutzen Sie bitte eine andere Welt)3");
	aClass3_446 = Class39_Sub5_Sub9.createJstring("cross");
	aClass3_452
	    = Class39_Sub5_Sub9.createJstring("Ung-Ultige Session)2ID)3");
	aClass3_441
	    = Class39_Sub5_Sub9.createJstring("Lade Freunde)2Liste)3)3)3");
	aClass3_444 = Class39_Sub5_Sub9.createJstring("Lade Wordpack )2 ");
	arraySizes = new int[5];
	aClass3_442 = aClass3_447;
	aClass3_443 = Class39_Sub5_Sub9.createJstring("(U1");
	anInt448 = 0;
	anIntArrayArray455 = new int[104][104];
	aClass3_456 = Class39_Sub5_Sub9.createJstring("(Z");
    }
}

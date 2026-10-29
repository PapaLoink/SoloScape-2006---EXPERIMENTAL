package jagex.utils;

import jagex.utils.IsaacPrng;
import jagex.utils.JString;
import jagex.io.JSocket;
import jagex.io.FrameBuffer;
import jagex.utils.Cache;
import unpackaged.ArchiveRequest;
import unpackaged.ArchiveWorker;
import unpackaged.Bzip2Block;
import unpackaged.Canvas_Sub1;
import unpackaged.Class1;
import unpackaged.Class12;
import unpackaged.Class14;
import unpackaged.Class2;
import unpackaged.Class30;
import unpackaged.Class32;
import unpackaged.Class33;
import unpackaged.Class37;
import unpackaged.Class39_Sub11;
import unpackaged.Class39_Sub12;
import unpackaged.Class39_Sub14;
import unpackaged.Class39_Sub4;
import unpackaged.Class39_Sub5_Sub10_Sub2;
import unpackaged.Class39_Sub5_Sub11;
import unpackaged.Class39_Sub5_Sub14;
import unpackaged.Class39_Sub5_Sub4_Sub4;
import unpackaged.Class39_Sub5_Sub7;
import unpackaged.Class39_Sub5_Sub9;
import unpackaged.Class41;
import unpackaged.Class43;
import unpackaged.Class45;
import unpackaged.Class46;
import unpackaged.Class53;
import unpackaged.Class55;
import unpackaged.Class62;
import unpackaged.Class66;
import unpackaged.Class68;
import unpackaged.Client;
import unpackaged.ClientScript;
import jagex.graphics.DrawingArea;
import unpackaged.FileLoader;
import unpackaged.FileTable;
import unpackaged.JKeyListener;
import unpackaged.NpcDefinition;
import unpackaged.OndemandRequest;
import jagex.world.actors.Projectile;
import unpackaged.ScriptState;
import jagex.world.actors.StillGraphic;
import unpackaged.Widget;

/* Class39 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Node
{
    public static JString aClass3_727;
    public static int anInt728 = 3;
    public static JString aClass3_729
	= Class39_Sub5_Sub9.createJstring(" from your ignore list first");
    public Node prevNode;
    public static JString aClass3_731;
    public static FileLoader fileLoader4;
    public long hash;
    public Node nextNode;
    public static JString aClass3_735;
    public static int mSectorX;
    public static int anInt737;
    public static FileLoader fileLoader3;
    public static FileTable aClass9_739;
    public static JString aClass3_740;
    public static int anInt741 = 0;
    public static int anInt742;
    public static JString aClass3_743;
    
    public static void method407(boolean bool) {
	JString class3 = null;
	for (int i = 0; Class39_Sub5_Sub11.anInt1841 > i; i++) {
	    if (Class33.aClass3Array601[i]
		    .method80(22938, FrameBuffer.aClass3_2147)
		!= -1) {
		class3
		    = (Class33.aClass3Array601[i].method85
		       (-58,
			Class33.aClass3Array601[i]
			    .method80(22938, FrameBuffer.aClass3_2147)));
		break;
	    }
	}
	if (class3 == null)
	    Class1.method49(118);
	else {
	    int i = Widget.anInt2063;
	    if (i < 0)
		i = 0;
	    int i_0_ = Class39_Sub5_Sub7.anInt1784;
	    int i_1_ = Huffmans.anInt757;
	    int i_2_ = Class14.anInt227;
	    int i_3_ = 6116423;
	    if (i_2_ > 190)
		i_2_ = 190;
	    DrawingArea.drawQuad(i, i_0_, i_2_, i_1_, i_3_);
	    DrawingArea.drawQuad(i + 1, i_0_ + 1, i_2_ - 2, 16, 0);
	    DrawingArea.drawQuadOutline(i + 1, i_0_ + 18, i_2_ - 2, i_1_ - 19,
					 0);
	    Class32.aClass39_Sub5_Sub10_Sub1_587
		.method635(class3, i + 3, i_0_ + 14, i_3_, bool);
	    int i_4_ = Class33.anInt599;
	    int i_5_ = IsaacPrng.anInt1091;
	    if (Class37.anInt653 == 0) {
		i_5_ = unpackaged.Resizable.vx(i_5_);
		i_4_ = unpackaged.Resizable.vy(i_4_);
	    }
	    if (Class37.anInt653 == 1) {
		i_4_ -= 205;
		i_5_ -= 553;
	    }
	    if (Class37.anInt653 == 2) {
		i_4_ -= 357;
		i_5_ -= 17;
	    }
	    for (int i_6_ = 0; i_6_ < Class39_Sub5_Sub11.anInt1841; i_6_++) {
		int i_7_
		    = (i_0_
		       - (-((Class39_Sub5_Sub11.anInt1841 + (-i_6_ - 1)) * 15)
			  - 31));
		int i_8_ = 16777215;
		JString class3_9_ = Class33.aClass3Array601[i_6_];
		if (class3_9_.method84(class3, 0)) {
		    class3_9_ = class3_9_.method59(0, -1,
						   (class3_9_.getLength()
						    - class3.getLength()));
		    if (class3_9_.method84(Class53.aClass3_953, 0))
			class3_9_
			    = class3_9_.method59(0, -1,
						 (class3_9_.getLength()
						  - Class53.aClass3_953
							.getLength()));
		}
		if (i < i_5_ && i + i_2_ > i_5_ && i_7_ - 13 < i_4_
		    && i_7_ + 3 > i_4_)
		    i_8_ = 16776960;
		Class32.aClass39_Sub5_Sub10_Sub1_587
		    .method635(class3_9_, i + 3, i_7_, i_8_, true);
	    }
	}
    }
    
    public static void method408(int id, int i_10_) {
	Canvas_Sub1.method41((byte) 127);
	int i_11_ = Class62.method1056(id, i_10_ ^ ~0x52).anInt1978;
	if (i_11_ != 0) {
	    int i_12_ = Class66.stateValues[id];
	    if (i_11_ == 1) {
		if (i_12_ == 1) {
		    Class39_Sub5_Sub10_Sub2.method650(0.9);
		    ((Class55) Class39_Sub5_Sub10_Sub2.anInterface2_2458)
			.method1003(0.9, false);
		}
		if (i_12_ == 2) {
		    Class39_Sub5_Sub10_Sub2.method650(0.8);
		    ((Class55) Class39_Sub5_Sub10_Sub2.anInterface2_2458)
			.method1003(0.8, false);
		}
		if (i_12_ == 3) {
		    Class39_Sub5_Sub10_Sub2.method650(0.7);
		    ((Class55) Class39_Sub5_Sub10_Sub2.anInterface2_2458)
			.method1003(0.7, false);
		}
		if (i_12_ == 4) {
		    Class39_Sub5_Sub10_Sub2.method650(0.6);
		    ((Class55) Class39_Sub5_Sub10_Sub2.anInterface2_2458)
			.method1003(0.6, false);
		}
		Class66.method1100(i_10_ + 123);
		ClientScript.aBoolean1690 = true;
	    }
	    if (i_11_ == 3) {
		int i_13_ = 0;
		if (i_12_ == 0)
		    i_13_ = 255;
		if (i_12_ == 1)
		    i_13_ = 192;
		if (i_12_ == 2)
		    i_13_ = 128;
		if (i_12_ == 3)
		    i_13_ = 64;
		if (i_12_ == 4)
		    i_13_ = 0;
		if (i_13_ != Class39_Sub5_Sub4_Sub4.anInt2313) {
		    if (Class39_Sub5_Sub4_Sub4.anInt2313 != 0
			|| ArchiveWorker.anInt1205 == -1) {
			if (i_13_ == 0) {
			    Class41.method892((byte) 94);
			    JSocket.anInt313 = 0;
			} else
			    Class46.method935(i_13_, 64);
		    } else {
			Class41.method891(i_10_ ^ 0x23, 0, 0, false,
					  (Projectile
					   .fileLoader6),
					  i_13_, ArchiveWorker.anInt1205);
			JSocket.anInt313 = 0;
		    }
		    Class39_Sub5_Sub4_Sub4.anInt2313 = i_13_;
		}
	    }
	    if (i_11_ == 6)
		Class68.anInt1190 = i_12_;
	    if (i_10_ != 1)
		aClass3_731 = null;
	    if (i_11_ == 5)
		Class45.anInt868 = i_12_;
	    if (i_11_ == 10) {
		if (i_12_ == 0)
		    FileTable.anInt149 = 127;
		if (i_12_ == 1)
		    FileTable.anInt149 = 96;
		if (i_12_ == 2)
		    FileTable.anInt149 = 64;
		if (i_12_ == 3)
		    FileTable.anInt149 = 32;
		if (i_12_ == 4)
		    FileTable.anInt149 = 0;
	    }
	    if (i_11_ == 9)
		Client.anInt1274 = i_12_;
	    if (i_11_ == 8) {
		Class2.anInt53 = i_12_;
		Class14.aBoolean245 = true;
	    }
	    if (i_11_ == 4) {
		if (i_12_ == 0)
		    Class39_Sub5_Sub9.anInt1810 = 127;
		if (i_12_ == 1)
		    Class39_Sub5_Sub9.anInt1810 = 96;
		if (i_12_ == 2)
		    Class39_Sub5_Sub9.anInt1810 = 64;
		if (i_12_ == 3)
		    Class39_Sub5_Sub9.anInt1810 = 32;
		if (i_12_ == 4)
		    Class39_Sub5_Sub9.anInt1810 = 0;
	    }
	}
    }
    
    public static void method409(boolean bool) {
	aClass3_740 = null;
	aClass9_739 = null;
	fileLoader4 = null;
	aClass3_731 = null;
	aClass3_743 = null;
	fileLoader3 = null;
	aClass3_735 = null;
	aClass3_727 = null;
	aClass3_729 = null;
    }
    
    public static void method410(int i, boolean bool) {
	StillGraphic.method530(-92, i);
    }
    
    public static void method411(int i) {
	if (Class30.anInt534 == 0
	    && NpcDefinition.aClass39_Sub5_Sub17_1864 == null) {
	    int i_14_ = Class46.anInt887;
	    if (IsaacPrng.aBoolean1100 && Class39_Sub4.anInt1329 >= 516
		&& Bzip2Block.anInt1054 >= 160 && Class39_Sub4.anInt1329 <= 765
		&& Bzip2Block.anInt1054 <= 205)
		i_14_ = 0;
	    if (!Class39_Sub12.aBoolean1493) {
		if (i_14_ == 1 && Class39_Sub5_Sub11.anInt1841 > 0) {
		    int i_15_ = (JKeyListener.anIntArray621
				 [Class39_Sub5_Sub11.anInt1841 - 1]);
		    if (i_15_ == 35 || i_15_ == 30 || i_15_ == 26
			|| i_15_ == 25 || i_15_ == 49 || i_15_ == 28
			|| i_15_ == 16 || i_15_ == 54 || i_15_ == 32
			|| i_15_ == 19 || i_15_ == 23 || i_15_ == 1004) {
			int i_16_ = (Class12.anIntArray196
				     [Class39_Sub5_Sub11.anInt1841 - 1]);
			int i_17_ = (Class43.anIntArray820
				     [Class39_Sub5_Sub11.anInt1841 - 1]);
			Widget class39_sub5_sub17
			    = Class37.getWidget(i_17_);
			if (class39_sub5_sub17.method753((byte) 114)
			    || class39_sub5_sub17.method773(0)) {
			    Cache.aBoolean121 = false;
			    Widget.anInt2031 = 0;
			    ArchiveWorker.anInt1203 = i_17_;
			    OndemandRequest.anInt1720 = Bzip2Block.anInt1054;
			    Class30.anInt534 = 2;
			    if (i_17_ >> 16 == Class39_Sub11.anInt1478)
				Class30.anInt534 = 1;
			    if (Class39_Sub5_Sub14.anInt1912 == i_17_ >> 16)
				Class30.anInt534 = 3;
			    ArchiveRequest.anInt1403 = i_16_;
			    ClientScript.anInt1702
				= Class39_Sub4.anInt1329;
			    return;
			}
		    }
		}
		if (i_14_ == 1
		    && (Class45.anInt868 == 1
			|| Class33.method327(Class39_Sub5_Sub11.anInt1841 - 1,
					     (byte) -128))
		    && Class39_Sub5_Sub11.anInt1841 > 2)
		    i_14_ = 2;
		if (i_14_ == 1 && Class39_Sub5_Sub11.anInt1841 > 0)
		    ScriptState.method278(Class39_Sub5_Sub11.anInt1841 - 1, 1);
		if (i_14_ == 2 && Class39_Sub5_Sub11.anInt1841 > 0)
		    Class39_Sub5_Sub9.method607(701);
	    } else {
		if (i_14_ != 1) {
		    int i_18_ = Class33.anInt599;
		    int i_19_ = IsaacPrng.anInt1091;
		    if (Class37.anInt653 == 0) {
			i_18_ = unpackaged.Resizable.vy(i_18_);
			i_19_ = unpackaged.Resizable.vx(i_19_);
		    }
		    if (Class37.anInt653 == 1) {
			i_18_ -= 205;
			i_19_ -= 553;
		    }
		    if (Class37.anInt653 == 2) {
			i_19_ -= 17;
			i_18_ -= 357;
		    }
		    if (Widget.anInt2063 - 10 > i_19_
			|| (Class14.anInt227 + Widget.anInt2063
			    + 10) < i_19_
			|| i_18_ < Class39_Sub5_Sub7.anInt1784 - 10
			|| (Huffmans.anInt757 + Class39_Sub5_Sub7.anInt1784 + 10
			    < i_18_)) {
			Class39_Sub12.aBoolean1493 = false;
			if (Class37.anInt653 == 1)
			    Class39_Sub14.aBoolean1520 = true;
			if (Class37.anInt653 == 2)
			    Class14.aBoolean245 = true;
		    }
		}
		if (i_14_ == 1) {
		    int i_20_ = Class39_Sub5_Sub7.anInt1784;
		    int i_21_ = Widget.anInt2063;
		    int i_22_ = Class14.anInt227;
		    int i_23_ = Class39_Sub4.anInt1329;
		    int i_24_ = Bzip2Block.anInt1054;
		    int i_25_ = -1;
		    if (Class37.anInt653 == 0) {
			i_24_ = unpackaged.Resizable.vy(i_24_);
			i_23_ = unpackaged.Resizable.vx(i_23_);
		    }
		    if (Class37.anInt653 == 1) {
			i_23_ -= 553;
			i_24_ -= 205;
		    }
		    if (Class37.anInt653 == 2) {
			i_24_ -= 357;
			i_23_ -= 17;
		    }
		    for (int i_26_ = 0; Class39_Sub5_Sub11.anInt1841 > i_26_;
			 i_26_++) {
			int i_27_ = (i_20_ + 31
				     + (-i_26_ + (Class39_Sub5_Sub11.anInt1841
						  - 1)) * 15);
			if (i_23_ > i_21_ && i_23_ < i_22_ + i_21_
			    && i_27_ - 13 < i_24_ && i_27_ + 3 > i_24_)
			    i_25_ = i_26_;
		    }
		    if (i_25_ != -1)
			ScriptState.method278(i_25_, 1);
		    if (Class37.anInt653 == 2)
			Class14.aBoolean245 = true;
		    Class39_Sub12.aBoolean1493 = false;
		    if (Class37.anInt653 == 1)
			Class39_Sub14.aBoolean1520 = true;
		}
	    }
	    int i_28_ = 29 % ((36 - i) / 45);
	}
    }
    
    public static int method412(int i, int i_29_, byte i_30_) {
	if (i_29_ == -1)
	    return 12345678;
	i = i * (i_29_ & 0x7f) / 128;
	if (i >= 2) {
	    if (i > 126)
		i = 126;
	} else
	    i = 2;
	return i + (i_29_ & 0xff80);
    }
    
    public void unlinkDeque() {
	if (prevNode != null) {
	    prevNode.nextNode = nextNode;
	    nextNode.prevNode = prevNode;
	    prevNode = null;
	    nextNode = null;
	}
    }
    
    public boolean isLinked() {
	if (prevNode == null)
	    return false;
	return true;
    }
    
    static {
	anInt737 = (int) (Math.random() * 33.0) - 16;
	aClass3_727 = aClass3_729;
	aClass3_740 = Class39_Sub5_Sub9.createJstring("Titelbild geladen)3");
	aClass3_735
	    = Class39_Sub5_Sub9.createJstring("nicht hergestellt werden)3");
	aClass3_731 = Class39_Sub5_Sub9.createJstring("welle2:");
	aClass3_743 = Class39_Sub5_Sub9.createJstring(":0");
    }
}

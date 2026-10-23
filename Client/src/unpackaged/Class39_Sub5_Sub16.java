package unpackaged;

/* Class39_Sub5_Sub16 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
import jagex.io.BufferedFile;
import jagex.graphics.sprites.DirectColorSprite;
import jagex.graphics.sprites.IndexedColorSprite;
import jagex.utils.SubNode;
import jagex.utils.IsaacPrng;
import jagex.utils.JString;
import jagex.world.actors.Npc;
import jagex.utils.Queue;
import jagex.io.Buffer;
import jagex.utils.Cache;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.zip.CRC32;

public class Class39_Sub5_Sub16 extends SubNode
{
    public static Cache aClass7_1975 = new Cache(30);
    public static int anInt1976;
    public static int[] anIntArray1977;
    public int anInt1978 = 0;
    public static FileTable wigetFileLoader;
    public static CRC32 crc;
    public static JString aClass3_1981;
    public static int anInt1982;
    public static JString aClass3_1983
	= Class39_Sub5_Sub9.createJstring(" is already on your friend list");
    public static int[] anIntArray1984;
    public static long aLong1985;
    public static DirectColorSprite[] aClass39_Sub5_Sub10_Sub3Array1986;
    public static JString aClass3_1987;
    public static IndexedColorSprite aClass39_Sub5_Sub10_Sub4_1988;
    public static byte[][] aByteArrayArray1989;
    public static JString aClass3_1990;
    public static JString aClass3_1991;
    public static DirectColorSprite[] aClass39_Sub5_Sub10_Sub3Array1992;
    public static JString aClass3_1993;
    
    public void decodeOpcode(Buffer buffer, int opcode) {
	if (opcode == 5)
	    anInt1978 = buffer.getUword();
    }
    
    public static int method744(KeyEvent keyevent, int i) {
	int i_2_ = keyevent.getKeyChar();
	if (i_2_ <= 0 || i_2_ >= 256)
	    i_2_ = -1;
	return i_2_;
    }
    
    public void decode(Buffer buffer) {
	for (;;) {
	    int opcode = buffer.getUbyte();
	    if (opcode == 0)
		break;
	    decodeOpcode(buffer, opcode);
	}
    }
    
    public static void method746(byte i, Component component) {
	component.removeKeyListener(Npc.aClass35_2499);
	component
	    .removeFocusListener(Npc.aClass35_2499);
    }
    
    public static void method747(int i) {
	Class39_Sub5_Sub4_Sub2.anIntArray2231 = null;
	Class14.anIntArray239 = null;
	Class39_Sub14.aByteArrayArrayArray1516 = null;
	Class20.anIntArrayArray391 = null;
	IsaacPrng.aByteArrayArrayArray1081 = null;
	Class39_Sub5_Sub9.anIntArray1809 = null;
	anIntArray1984 = null;
	Class33.aByteArrayArrayArray596 = null;
	ItemDefinition.aByteArrayArrayArray1671 = null;
	ArchiveRequest.anIntArrayArrayArray1412 = null;
	NameTable.anIntArray187 = null;
	Class67.aByteArrayArrayArray1179 = null;
    }
    
    public static void method748(int[] is, boolean bool, byte i, int i_4_,
				 int i_5_) {
	NpcDefinition.aClass57_1867.method1006(10);
	Queue.aClass39_Sub5_Sub10_Sub4_973.method695(0, 0);
	if (bool) {
	    if (is[i_5_] != -1) {
		if (i_5_ == 0)
		    Class68.aClass39_Sub5_Sub10_Sub4_1188.method695(22, 10);
		if (i_5_ == 1)
		    ObjectDefinition.aClass39_Sub5_Sub10_Sub4_1927
			.method695(54, 8);
		if (i_5_ == 2)
		    ObjectDefinition.aClass39_Sub5_Sub10_Sub4_1927
			.method695(82, 8);
		if (i_5_ == 3)
		    Npc
			.aClass39_Sub5_Sub10_Sub4_2500.method695(110, 8);
		if (i_5_ == 4)
		    Class65.aClass39_Sub5_Sub10_Sub4_1146.method695(153, 8);
		if (i_5_ == 5)
		    Class65.aClass39_Sub5_Sub10_Sub4_1146.method695(181, 8);
		if (i_5_ == 6)
		    JKeyListener.aClass39_Sub5_Sub10_Sub4_629.method695(209, 9);
	    }
	    if (is[0] != -1 && i_4_ != 0)
		drawSideIcon(0, 29, 13);
	    if (is[1] != -1 && i_4_ != 1)
		drawSideIcon(1, 53, 11);
	    if (is[2] != -1 && i_4_ != 2)
		drawSideIcon(2, 82, 11);
	    if (is[3] != -1 && i_4_ != 3)
		drawSideIcon(3, 115, 12);
	    if (is[4] != -1 && i_4_ != 4)
		drawSideIcon(4, 153, 13);
	    if (is[5] != -1 && i_4_ != 5)
		drawSideIcon(5, 180, 11);
	    if (is[6] != -1 && i_4_ != 6)
		drawSideIcon(6, 208, 13);
	}
	BufferedFile.aClass57_346.method1006(10);
	Class68.aClass39_Sub5_Sub10_Sub4_1189.method695(0, 0);
	if (bool) {
	    if (is[i_5_] != -1) {
		if (i_5_ == 7)
		    Class4.aClass39_Sub5_Sub10_Sub4_67.method695(42, 0);
		if (i_5_ == 8)
		    aClass39_Sub5_Sub10_Sub4_1988.method695(74, 0);
		if (i_5_ == 9)
		    aClass39_Sub5_Sub10_Sub4_1988.method695(102, 0);
		if (i_5_ == 10)
		    Class34.aClass39_Sub5_Sub10_Sub4_606.method695(130, 1);
		if (i_5_ == 11)
		    Class39_Sub12.aClass39_Sub5_Sub10_Sub4_1495.method695(173,
									  0);
		if (i_5_ == 12)
		    Class39_Sub12.aClass39_Sub5_Sub10_Sub4_1495.method695(201,
									  0);
		if (i_5_ == 13)
		    Class30.aClass39_Sub5_Sub10_Sub4_539.method695(229, 0);
	    }
	    if (is[8] != -1 && i_4_ != 8)
		drawSideIcon(7, 74, 2);
	    if (is[9] != -1 && i_4_ != 9)
		drawSideIcon(8, 102, 3);
	    if (is[10] != -1 && i_4_ != 10)
		drawSideIcon(9, 137, 4);
	    if (is[11] != -1 && i_4_ != 11)
		drawSideIcon(10, 174, 2);
	    if (is[12] != -1 && i_4_ != 12)
		drawSideIcon(11, 201, 2);
	    if (is[13] != -1 && i_4_ != 13)
		drawSideIcon(12, 226, 2);
	}
	try {
	    Graphics graphics = Class41.aCanvas778.getGraphics();
	    NpcDefinition.aClass57_1867.draw(graphics, 516,
							160);
	    GrandExchangeSearch.drawBehindSearch(BufferedFile.aClass57_346, graphics, 496, 466);
	} catch (Exception exception) {
	    Class41.aCanvas778.repaint();
	}
    }
    
    public static void method749(int i) {
	aClass7_1975 = null;
	aClass39_Sub5_Sub10_Sub4_1988 = null;
	aClass3_1991 = null;
	aClass39_Sub5_Sub10_Sub3Array1992 = null;
	crc = null;
	aClass3_1990 = null;
	aClass3_1981 = null;
	wigetFileLoader = null;
	aClass3_1983 = null;
	anIntArray1977 = null;
	anIntArray1984 = null;
	aByteArrayArray1989 = null;
	aClass3_1993 = null;
	aClass39_Sub5_Sub10_Sub3Array1986 = null;
	aClass3_1987 = null;
    }
    
    static {
	anInt1982 = -1;
	aClass3_1981 = aClass3_1983;
	crc = new CRC32();
	aClass3_1990
	    = Class39_Sub5_Sub9
		  .createJstring("oberen Rand der Webseite ausw-=hlen)3");
	aClass3_1993 = Class39_Sub5_Sub9.createJstring("blaugr-Un:");
	aClass3_1987 = Class39_Sub5_Sub9.createJstring("From");
	aClass3_1991 = aClass3_1987;
    }
    /** Sidebar icon slot index at (x, y): the 2004 icon with the classic interface setting, else the cache icon. */
    private static void drawSideIcon(int index, int x, int y) {
	if (!ClassicInterface.drawSideIcon(index, x, y))
	    Class55.aClass39_Sub5_Sub10_Sub4Array1243[index].method695(x, y);
    }

}

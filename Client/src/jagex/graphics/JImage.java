package jagex.graphics;

/* Class57_Sub2 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
import jagex.graphics.AbstractImage;
import jagex.graphics.sprites.IndexedColorSprite;
import jagex.utils.IsaacPrng;
import jagex.utils.JString;
import jagex.world.actors.Player;
import jagex.world.map.TraversalMap;
import jagex.io.FrameBuffer;
import jagex.io.Buffer;
import jagex.utils.Cache;
import jagex.utils.ArrayUtils;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import unpackaged.ArchiveRequest;
import unpackaged.ArchiveWorker;
import unpackaged.Class1;
import unpackaged.Class10;
import unpackaged.Class14;
import unpackaged.Class2;
import unpackaged.Class20;
import unpackaged.Class25;
import unpackaged.Class30;
import unpackaged.Class39_Sub12;
import unpackaged.Class39_Sub13;
import unpackaged.Class39_Sub14;
import unpackaged.Class39_Sub5_Sub11;
import unpackaged.Class39_Sub5_Sub18;
import unpackaged.Class39_Sub5_Sub6;
import unpackaged.Class39_Sub5_Sub7;
import unpackaged.Class39_Sub5_Sub9;
import unpackaged.Class39_Sub7;
import unpackaged.Class4;
import unpackaged.Class43;
import unpackaged.Class53;
import unpackaged.Class55;
import unpackaged.Class63;
import unpackaged.Class65;
import unpackaged.Class66;
import unpackaged.Class68;
import unpackaged.Client;
import unpackaged.FileLoader;
import unpackaged.FileTable;
import jagex.world.actors.GroundItem;
import jagex.utils.Huffmans;
import unpackaged.JKeyListener;
import unpackaged.JMouseListener;
import unpackaged.ScriptState;
import unpackaged.WebClientBridge;

public class JImage extends AbstractImage
    implements ImageProducer, ImageObserver
{
    public ImageConsumer anImageConsumer1571;
    public static JString aClass3_1572 = Class39_Sub5_Sub9.createJstring("On");
    public static int anInt1573;
    public static JString aClass3_1574;
    public ColorModel colorModel;
    public static AbstractImage aClass57_1576;
    public static JString aClass3_1577;
    public static JString aClass3_1578;
    public static JString aClass3_1579
	= Class39_Sub5_Sub9.createJstring("@or3@");
    public static JString aClass3_1580;
    public static Buffer updateTableBuffer;
    public static JString aClass3_1582;
    public static JString aClass3_1583;
    public static int anInt1584;
    public static JString aClass3_1585;
    public static int anInt1586;
    
    public static int method1011(int i, int i_0_, int i_1_, byte i_2_) {
	int i_3_ = -i_0_ + 256;
	return (((i_0_ * (i_1_ & 0xff00ff) + (i & 0xff00ff) * i_3_ & ~0xff00ff)
		 + (i_0_ * (i_1_ & 0xff00) + i_3_ * (i & 0xff00) & 0xff0000))
		>> 8);
    }
    
    public void requestTopDownLeftRightResend(ImageConsumer imageconsumer) {
	/* empty */
    }
    
    public void startProduction(ImageConsumer imageconsumer) {
	addConsumer(imageconsumer);
    }
    
    public static void method1012(int i) {
	if (JMouseListener.anInt787 == 2) {
	    Class4.method104(Class66.anInt1158 * 2,
			     (Class39_Sub5_Sub18.anInt2124
			      + (-JKeyListener.anInt618 + Class25.anInt471 << 7)),
			     -119,
			     ((-Class65.anInt1145 + JString.anInt1229 << 7)
			      + Class43.anInt823));
	    if (Class14.anInt224 > -1 && Class2.logicCycle % 20 < 10)
		Class20.aClass39_Sub5_Sub10_Sub3Array392[0]
		    .method670(Class14.anInt224 - 12, Class53.anInt962 - 28);
	}
    }
    
    public boolean imageUpdate(Image image, int i, int i_4_, int i_5_,
			       int i_6_, int i_7_) {
	return true;
    }
    
    public synchronized void addConsumer(ImageConsumer imageconsumer) {
	anImageConsumer1571 = imageconsumer;
	imageconsumer.setDimensions(width, height);
	imageconsumer.setProperties(null);
	imageconsumer.setColorModel(colorModel);
	imageconsumer.setHints(14);
    }
    
    public static JString method1013(int i, JString[] class3s,
				    int i_9_) {
	int i_10_ = 0;
	for (int i_11_ = 0; i_11_ < i; i_11_++) {
	    if (class3s[i_9_ + i_11_] == null)
		class3s[i_9_ + i_11_] = Class68.nullJstr;
	    i_10_ += class3s[i_11_ + i_9_].length;
	}
	int i_12_ = 0;
	byte[] is = new byte[i_10_];
	for (int i_13_ = 0; i > i_13_; i_13_++) {
	    JString class3 = class3s[i_13_ + i_9_];
	    ArrayUtils.arrayCopy(class3.bytes, 0, is, i_12_,
			      class3.length);
	    i_12_ += class3.length;
	}
	JString class3 = new JString();
	class3.length = i_10_;
	class3.bytes = is;
	return class3;
    }
    
    public static void method1014(int i, byte i_14_) {
	if (i_14_ != 99)
	    method1012(-85);
	if (Class55.aClass62_1251 != null) {
	    if (ArchiveRequest.anInt1415 != 0) {
		if (Class39_Sub5_Sub6.aByteArray1768 != null)
		    Class39_Sub5_Sub18.anInt2120 = i;
	    } else if (GroundItem.anInt2239 >= 0) {
		GroundItem.anInt2239 = i;
		Class55.aClass62_1251.method1055(0, i, i_14_ - 99);
	    }
	}
    }
    
    public static IndexedColorSprite[] method1015
	(int i, int i_15_, int i_16_, FileTable class9) {
	if (!Client.decodeBitmapFont(class9, i_15_, i_16_))
	    return null;
	return Class10.method179((byte) 68);
    }
    
    public synchronized void method1016(byte i) {
	if (i != -66)
	    anImageConsumer1571 = null;
	if (anImageConsumer1571 != null) {
	    anImageConsumer1571.setPixels(0, 0, width, height,
					  colorModel, buffer, 0,
					  width);
	    anImageConsumer1571.imageComplete(2);
	}
    }
    
    public static void method1017(byte i) {
	Class30.anInt544 = 0;
	Huffmans.frameSize = 0;
	ScriptState.anInt448 = -1;
	Class39_Sub12.aBoolean1493 = false;
	Class39_Sub5_Sub11.anInt1841 = 0;
	if (i == 74) {
	    FileLoader.anInt1283 = -1;
	    FrameBuffer.outgoingGameBuffer.offset = 0;
	    Class39_Sub5_Sub7.minimapState = 0;
	    Class63.anInt1117 = -1;
	    Class4.frameId = -1;
	    Class39_Sub5_Sub11.anInt1827 = 0;
	    Class39_Sub7.anInt1380 = 0;
	    Class39_Sub5_Sub11.gameBuffer.offset = 0;
	    for (int i_17_ = 0;
		 Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211.length > i_17_;
		 i_17_++) {
		if (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_17_]
		    != null)
		    Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_17_]
			.anInt2260
			= -1;
	    }
	    for (int i_18_ = 0;
		 ((GroundItem
		   .aClass39_Sub5_Sub4_Sub4_Sub1Array2241).length
		  > i_18_);
		 i_18_++) {
		if ((GroundItem
		     .aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_18_])
		    != null)
		    GroundItem
			.aClass39_Sub5_Sub4_Sub4_Sub1Array2241[i_18_].anInt2260
			= -1;
	    }
	    ArchiveWorker.method1121(i ^ ~0x3b);
	    Class39_Sub14.setState(30);
	}
    }
    
    public void setComponent(Component component,int i, int i_19_) {
	height = i_19_;
	buffer = new int[i * i_19_ + 1];
	width = i;
	colorModel = new DirectColorModel(32, 16711680, 65280, 255);
	image = component.createImage(this);
	    method1016((byte) -66);
	    component.prepareImage(image, this);
	    method1016((byte) -66);
	    component.prepareImage(image, this);
	    method1016((byte) -66);
	    component.prepareImage(image, this);
	    this.method1006(10);
	
    }
    
    public static int method1018(int i, int i_21_, int i_22_) {
	Class39_Sub13 class39_sub13
	    = ((Class39_Sub13)
	       Class14.aClass19_213.fetch((long) i));
	if (class39_sub13 == null)
	    return 0;
	if (i_22_ < 0 || class39_sub13.anIntArray1504.length <= i_22_)
	    return 0;
	return class39_sub13.anIntArray1504[i_22_];
    }
    
    public void draw(Graphics graphics, int i_23_, int i_24_) {
	unpackaged.XpOverlay.paintBuffer(buffer, width, height, i_23_, i_24_);
	if (unpackaged.Resizable.capture(this, buffer, width, height, i_23_, i_24_)) return;
	method1016((byte) -66);
	WebClientBridge.blit(buffer, width, height, i_23_, i_24_);
	java.awt.Shape clip0 = unpackaged.ModHook.beforeBlit(graphics);
	graphics.drawImage(image, i_23_, i_24_, this);
	unpackaged.ModHook.afterBlitRestore(graphics, clip0);
	unpackaged.ModHook.afterBlit(graphics, i_23_, i_24_, width, height);
    }
    
    public static void method1019(byte i) {
	while (Class39_Sub5_Sub11.gameBuffer
		   .getRemainingBits(Huffmans.frameSize)
	       >= 11) {
	    int i_25_
		= Class39_Sub5_Sub11.gameBuffer.getBits(11);
	    if (i_25_ == 2047)
		break;
	    boolean bool = false;
	    if (Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_25_] == null) {
		bool = true;
		Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_25_]
		    = new Player();
		if (IsaacPrng.aClass39_Sub6Array1104[i_25_] != null)
		    Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_25_]
			.parseAppearance
			(IsaacPrng.aClass39_Sub6Array1104[i_25_]);
	    }
	    Class1.anIntArray40[TraversalMap.anInt515++] = i_25_;
	    Player class39_sub5_sub4_sub4_sub2
		= Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211[i_25_];
	    class39_sub5_sub4_sub4_sub2.anInt2290 = Class2.logicCycle;
	    int i_26_
		= Class39_Sub5_Sub11.gameBuffer.getBits(1);
	    if (i_26_ == 1)
		ArchiveRequest.anIntArray1400[anInt1586++] = i_25_;
	    int i_27_
		= Class39_Sub5_Sub11.gameBuffer.getBits(5);
	    if (i_27_ > 15)
		i_27_ -= 32;
	    int i_28_
		= Class39_Sub5_Sub11.gameBuffer.getBits(1);
	    int i_29_ = (Client.anIntArray1276
			 [Class39_Sub5_Sub11.gameBuffer
			      .getBits(3)]);
	    if (bool)
		class39_sub5_sub4_sub4_sub2.anInt2251 = i_29_;
	    int i_30_
		= Class39_Sub5_Sub11.gameBuffer.getBits(5);
	    if (i_30_ > 15)
		i_30_ -= 32;
	    class39_sub5_sub4_sub4_sub2.method508
		((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109.anIntArray2255[0]
		  + i_27_),
		 i_28_ == 1, (byte) 36,
		 i_30_ + (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
			  .anIntArray2314[0]));
	}
	Class39_Sub5_Sub11.gameBuffer.finishBitAccess();
    }
    
    public synchronized boolean isConsumer(ImageConsumer imageconsumer) {
	if (imageconsumer != anImageConsumer1571)
	    return false;
	return true;
    }
    
    public static void method1020(boolean bool) {
	aClass3_1579 = null;
	aClass3_1572 = null;
	aClass3_1578 = null;
	aClass3_1574 = null;
	aClass57_1576 = null;
	updateTableBuffer = null;
	aClass3_1582 = null;
	aClass3_1583 = null;
	aClass3_1580 = null;
	aClass3_1585 = null;
	aClass3_1577 = null;
    }
    
    public synchronized void removeConsumer(ImageConsumer imageconsumer) {
	if (anImageConsumer1571 == imageconsumer)
	    anImageConsumer1571 = null;
    }
    
    static {
	aClass3_1574 = aClass3_1572;
	aClass3_1577
	    = (Class39_Sub5_Sub9.createJstring
	       ("Moderator)2Option: Spieler f-Ur 48 Stunden stumm schalten: <ON>"));
	aClass3_1578 = Class39_Sub5_Sub9.createJstring("Privater Chat");
	aClass3_1582 = Class39_Sub5_Sub9.createJstring("@yel@*V");
	aClass3_1583 = Class39_Sub5_Sub9.createJstring("Fertigkeit)2");
	anInt1573 = 0;
	aClass3_1580 = Class39_Sub5_Sub9.createJstring("flash1:");
	anInt1586 = 0;
	aClass3_1585 = aClass3_1580;
    }
}

package unpackaged;

import jagex.graphics.DrawingArea;
import jagex.utils.HashTable;
import jagex.graphics.JImage;
import jagex.graphics.AbstractImage;
import jagex.world.actors.Projectile;
import jagex.utils.SubNode;
import jagex.utils.Node;
import jagex.utils.IsaacPrng;
import jagex.utils.JString;
import jagex.io.JSocket;
import jagex.world.actors.Npc;
import jagex.world.map.TraversalMap;
import jagex.io.Buffer;
import jagex.utils.Cache;

/* Class39_Sub5_Sub7 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public class Class39_Sub5_Sub7 extends SubNode
{
    public static int minimapState = 0;
    public static JString aClass3_1777;
    public static AbstractImage aClass57_1778;
    public static volatile int anInt1779;
    public static JString aClass3_1780;
    public static int anInt1781;
    public static JString aClass3_1782
	= Class39_Sub5_Sub9.createJstring("Loaded fonts");
    public static int anInt1783;
    public static int anInt1784;
    public byte[] aByteArray1785;
    public static JString aClass3_1786;
    public static JString aClass3_1787;
    public static JString aClass3_1788;
    public static int[] anIntArray1789;
    
    public static void method584(Class39_Sub5_Sub4_Sub4 class39_sub5_sub4_sub4,
				 byte i) {
	if (Class2.logicCycle == class39_sub5_sub4_sub4.anInt2261
	    || class39_sub5_sub4_sub4.anInt2268 == -1
	    || class39_sub5_sub4_sub4.anInt2305 != 0
	    || (class39_sub5_sub4_sub4.anInt2311 + 1
		> (Class62_Sub1.method1064
		   (class39_sub5_sub4_sub4.anInt2268, (byte) 54).anIntArray1831
		   [class39_sub5_sub4_sub4.anInt2265]))) {
	    int i_0_ = (class39_sub5_sub4_sub4.anInt2261
			- class39_sub5_sub4_sub4.anInt2256);
	    int i_1_ = Class2.logicCycle - class39_sub5_sub4_sub4.anInt2256;
	    int i_2_ = (class39_sub5_sub4_sub4.anInt2297 * 64
			+ class39_sub5_sub4_sub4.anInt2287 * 128);
	    int i_3_ = (class39_sub5_sub4_sub4.anInt2297 * 64
			+ class39_sub5_sub4_sub4.anInt2266 * 128);
	    int i_4_ = (class39_sub5_sub4_sub4.anInt2297 * 64
			+ class39_sub5_sub4_sub4.anInt2277 * 128);
	    class39_sub5_sub4_sub4.anInt2275
		= (i_4_ * i_1_ + (i_0_ - i_1_) * i_3_) / i_0_;
	    int i_5_ = (class39_sub5_sub4_sub4.anInt2279 * 128
			+ class39_sub5_sub4_sub4.anInt2297 * 64);
	    class39_sub5_sub4_sub4.anInt2301
		= (i_2_ * (-i_1_ + i_0_) + i_1_ * i_5_) / i_0_;
	}
	class39_sub5_sub4_sub4.anInt2274 = 0;
	if (class39_sub5_sub4_sub4.anInt2292 == 0)
	    class39_sub5_sub4_sub4.anInt2294 = 1024;
	if (class39_sub5_sub4_sub4.anInt2292 == 1)
	    class39_sub5_sub4_sub4.anInt2294 = 1536;
	if (class39_sub5_sub4_sub4.anInt2292 == 2)
	    class39_sub5_sub4_sub4.anInt2294 = 0;
	if (class39_sub5_sub4_sub4.anInt2292 == 3)
	    class39_sub5_sub4_sub4.anInt2294 = 512;
	class39_sub5_sub4_sub4.anInt2251 = class39_sub5_sub4_sub4.anInt2294;
    }
    
    public static Widget method585
	(int i, int i_6_, int i_7_, Widget[] class39_sub5_sub17s,
	 int i_8_, int i_9_, int i_10_, Widget class39_sub5_sub17,
	 int i_11_, int i_12_, int i_13_, int i_14_) {
	if (i_11_ > i_6_ || i_8_ < i_12_ || i_6_ >= i_7_ || i_10_ <= i_8_)
	    return null;
	for (int i_15_ = 0; i_15_ < class39_sub5_sub17s.length; i_15_++) {
	    Widget class39_sub5_sub17_16_
		= class39_sub5_sub17s[i_15_];
	    if (class39_sub5_sub17_16_ != null
		&& i == class39_sub5_sub17_16_.anInt2050
		&& !class39_sub5_sub17_16_.method754(-59, HashTable.aBoolean361)
		&& class39_sub5_sub17_16_ != class39_sub5_sub17) {
		int i_17_ = class39_sub5_sub17_16_.anInt2021 + i_12_ - i_14_;
		int i_18_ = class39_sub5_sub17_16_.anInt2091 - (-i_11_ + i_9_);
		if (class39_sub5_sub17_16_.type == 0) {
		    Widget class39_sub5_sub17_19_
			= method585(class39_sub5_sub17_16_.anInt2084, i_6_,
				    i_18_ + class39_sub5_sub17_16_.quadWidth,
				    class39_sub5_sub17s, i_8_,
				    class39_sub5_sub17_16_.anInt2064,
				    class39_sub5_sub17_16_.quadHeight + i_17_,
				    class39_sub5_sub17, i_18_, i_17_, -64,
				    class39_sub5_sub17_16_.anInt1994);
		    if (class39_sub5_sub17_19_ != null)
			return class39_sub5_sub17_19_;
		    if (class39_sub5_sub17_16_.aClass39_Sub5_Sub17Array2025
			!= null) {
			class39_sub5_sub17_19_
			    = method585(class39_sub5_sub17_16_.anInt2084, i_6_,
					(i_18_
					 + class39_sub5_sub17_16_.quadWidth),
					(class39_sub5_sub17_16_
					 .aClass39_Sub5_Sub17Array2025),
					i_8_, class39_sub5_sub17_16_.anInt2064,
					(class39_sub5_sub17_16_.quadHeight
					 + i_17_),
					class39_sub5_sub17, i_18_, i_17_, -50,
					class39_sub5_sub17_16_.anInt1994);
			if (class39_sub5_sub17_19_ != null)
			    return class39_sub5_sub17_19_;
		    }
		}
		if (class39_sub5_sub17_16_.method772(false) && i_18_ <= i_6_
		    && i_8_ >= i_17_
		    && i_6_ < class39_sub5_sub17_16_.quadWidth + i_18_
		    && class39_sub5_sub17_16_.quadHeight + i_17_ > i_8_)
		    return class39_sub5_sub17_16_;
	    }
	}
	return null;
    }
    
    public static int method586(boolean bool, boolean bool_20_, int i) {
	int i_21_ = i;
	if (bool_20_)
	    i_21_ += Class1.pendingPriorityRequests + Class39_Sub5_Sub6.queuedPriorityRequests;
	if (bool)
	    i_21_ += Class41.queuedRegularRequests + Canvas_Sub1.pendingRegularRequests;
	return i_21_;
    }
    
    public static void method587(int i) {
	aClass3_1777 = null;
	aClass3_1787 = null;
	aClass3_1780 = null;
	anIntArray1789 = null;
	aClass3_1786 = null;
	aClass57_1778 = null;
	aClass3_1788 = null;
	aClass3_1782 = null;
    }
    
    public static boolean method588(int i) {
	synchronized (Npc.aClass35_2499) {
	    if (Class39_Sub5_Sub4.anInt1731 == Buffer.anInt1368)
		return false;
	    Class15.anInt287 = Class46.anIntArray883[Buffer.anInt1368];
	    Projectile.anInt2191
		= Class46_Sub1.anIntArray1558[Buffer.anInt1368];
	    Buffer.anInt1368 = Buffer.anInt1368 + 1 & 0x7f;
	    return true;
	}
    }
    
    public Class39_Sub5_Sub7(byte[] is) {
	aByteArray1785 = is;
    }
    
    public static void method589(byte i) {
	Canvas_Sub1.anInt15++;
	Class34.method335(-18587, true);
	Class1.method45(-11, true);
	Class34.method335(-18587, false);
	Class1.method45(-11, false);
	Class50.updateProjectiles();
	Class62_Sub2.method1072(6255);
	if (!TraversalMap.aBoolean504) {
	    int i_22_ = Interp.pitch();                      // camera blended between ticks at frame rates above 50
	    if (Cache.anInt101 / 256 > i_22_)
		i_22_ = Cache.anInt101 / 256;
	    int cameraDistance = ModHook.zoomDist(i_22_ * 3 + 600);
	    // browser pinch zoom (WebClientBridge) moves the camera only while a pinch offset is set,
	    // so its 450-2600 clamp never cuts into the wheel zoom range
	    if (WebClientBridge.cameraDistanceOffset() != 0)
		cameraDistance = WebClientBridge.cameraDistance(cameraDistance);
	    int i_23_ = SubNode.anInt1344 + Interp.yaw() & 0x7ff;
	    if (Class44.aBooleanArray837[4]
		&& Class2.anIntArray49[4] + 128 > i_22_)
		i_22_ = Class2.anIntArray49[4] + 128;
	    ClientApplet.method26
		(1000, Interp.camX(), i_23_, Interp.camY(),
		 cameraDistance, i_22_,
		 Class14.method212((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				    .anInt2275),
				   9990, NameTable.height,
				   (Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				    .anInt2301)) - 50);
	}
	int i_24_;
	if (TraversalMap.aBoolean504)
	    i_24_ = Class23.method274((byte) 108);
	else
	    i_24_ = Class33.method330(113);
	int i_25_ = Class39_Sub11.anInt1470;
	int i_26_ = Class39_Sub10.anInt1437;
	int i_27_ = Node.anInt742;
	int i_28_ = Class43.anInt799;
	int i_29_ = Class39_Sub5_Sub4_Sub4.anInt2315;
	for (int i_30_ = 0; i_30_ < 5; i_30_++) {
	    if (Class44.aBooleanArray837[i_30_]) {
		int i_31_
		    = (int) (((double) (Class13.anIntArray197[i_30_] * 2 + 1)
			      * Math.random())
			     - (double) Class13.anIntArray197[i_30_]
			     + (Math.sin((double) Class4.anIntArray75[i_30_]
					 * ((double) (Class45.anIntArray857
						      [i_30_])
					    / 100.0))
				* (double) Class2.anIntArray49[i_30_]));
		if (i_30_ == 0)
		    Class39_Sub11.anInt1470 += i_31_;
		if (i_30_ == 3)
		    Class39_Sub5_Sub4_Sub4.anInt2315
			= Class39_Sub5_Sub4_Sub4.anInt2315 + i_31_ & 0x7ff;
		if (i_30_ == 2)
		    Node.anInt742 += i_31_;
		if (i_30_ == 4) {
		    Class43.anInt799 += i_31_;
		    if (Class43.anInt799 < 128)
			Class43.anInt799 = 128;
		    if (Class43.anInt799 > 383)
			Class43.anInt799 = 383;
		}
		if (i_30_ == 1)
		    Class39_Sub10.anInt1437 += i_31_;
	    }
	}
	ClientScript.method481(-6414);
	Resizable.zoom = Resizable.sceneZoom;              // the world (and its overhead text) uses the window-scaled projection
	Model.anInt2397 = Resizable.vx(IsaacPrng.anInt1091);
	Model.anInt2407 = Resizable.vy(Class33.anInt599);
	Model.anInt2413 = 0;
	Model.aBoolean2398 = true;
	boolean gpu = Gpu.begin();                           // GPU setting: the world's triangles are drawn by OpenGL
	if (!gpu || !Gpu.coversView())                       // the GPU picture replaces every pixel: no need to clear first
	    DrawingArea.reset();
	try {
	    Class44.aClass38_836.method382(Class39_Sub11.anInt1470,
					   Class39_Sub10.anInt1437,
					   Node.anInt742, Class43.anInt799,
					   Class39_Sub5_Sub4_Sub4.anInt2315,
					   i_24_);
	} finally {
	    if (gpu) Gpu.end();
	}
	Class44.aClass38_836.method393();
	Class1.method48(false);
	JImage.method1012(0);
	((Class55) Class39_Sub5_Sub10_Sub2.anInterface2_2458)
	    .method1004(Class45.anInt856, false);
	ChristmasEventSnow.drawSnowfall();
	Resizable.zoom = 512;                                // interfaces, item and chathead models keep the 2006 scale
	Canvas_Sub1.method39(-32024);
	if (AbstractImage.aBoolean1000 && method586(false, true, 0) == 0)
	    AbstractImage.aBoolean1000 = false;
	if (AbstractImage.aBoolean1000) {
	    ClientScript.method481(-6414);
	    DrawingArea.reset();
	    Class26.method294(false, NameTable.aClass3_190, null, -77);
	}
	Class46.method940((byte) -103);
	Class39_Sub10.anInt1437 = i_26_;
	Class39_Sub11.anInt1470 = i_25_;
	Node.anInt742 = i_27_;
	Class39_Sub5_Sub4_Sub4.anInt2315 = i_29_;
	Class43.anInt799 = i_28_;
        BrowserClientBootstrap.sceneRendered();
    }
    
    static {
	anInt1779 = 0;
	aClass3_1788 = Class39_Sub5_Sub9.createJstring("glow3:");
	aClass3_1787 = aClass3_1788;
	aClass3_1780
	    = Class39_Sub5_Sub9.createJstring("Welcome to RuneScape");
	aClass3_1777 = aClass3_1780;
	aClass3_1786 = aClass3_1782;
	anInt1783 = 0;
    }
}

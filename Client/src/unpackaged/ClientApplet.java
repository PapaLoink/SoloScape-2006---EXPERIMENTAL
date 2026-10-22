package unpackaged;

/* Applet_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
import jagex.io.BufferedFile;
import jagex.utils.HashTable;
import jagex.graphics.AbstractImage;
import jagex.utils.Node;
import jagex.utils.IsaacPrng;
import jagex.utils.JString;
import jagex.world.actors.Player;
import jagex.utils.Queue;
import jagex.utils.Deque;
import jagex.io.Buffer;
import jagex.utils.Cache;
import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;

public abstract class ClientApplet extends Applet implements Runnable, FocusListener, WindowListener {
    public static byte encryptionKey = 0;
    public static long[] aLongArray2 = new long[200];
    public static Buffer odBuffer;
    public static JString aClass3_4 = Class39_Sub5_Sub9.createJstring("m");
    public boolean errorPageDisplayed = false;
    public static JString aClass3_6;
    public static JString aClass3_7;
    public static int anInt8;
    public static JString aClass3_9;
    public static JString aClass3_10;
    public static int anInt11;
    public static boolean aBoolean12;
    
    public void windowClosing(WindowEvent windowevent) {
	destroy();
    }
    
    public void start() {
	if (this == Class25.anApplet_Sub1_465 && !Class62.aBoolean1112)
	    Class15.aLong281 = 0L;
    }
    
    public String getParameter(String string) {
	if (Class10.frame != null)
	    return null;
	if (Class39_Sub5_Sub9.signlink != null
	    && Class39_Sub5_Sub9.signlink.anApplet416 != this)
	    return Class39_Sub5_Sub9.signlink.anApplet416
		       .getParameter(string);
	return super.getParameter(string);
    }
    
    public void focusGained(FocusEvent focusevent) {
	Class45.aBoolean861 = true;
	ClientScript.aBoolean1690 = true;
    }
    
    public void run() {
	try {
	    if (Signlink.javaVendor != null) {
		String string = Signlink.javaVendor.toLowerCase();
		if (string.indexOf("sun") == -1
		    && string.indexOf("apple") == -1) {
		    if (string.indexOf("ibm") != -1
			&& (Signlink.javaVersion == null
			    || Signlink.javaVersion.equals("1.4.2"))) {
			displayErrorPage("wrongjava");
			return;
		    }
		} else {
		    String string_0_ = Signlink.javaVersion;
		    if (string_0_.equals("1.1") || string_0_.startsWith("1.1.")
			|| string_0_.equals("1.2")
			|| string_0_.startsWith("1.2.")) {
			displayErrorPage("wrongjava");
			return;
		    }
		    Class39_Sub5_Sub6.anInt1773 = 5;
		}
	    }
	    if (Class39_Sub5_Sub9.signlink.anApplet416 != null) {
		Method method = Signlink.aMethod417;
		if (method != null) {
		    try {
			method.invoke((Class39_Sub5_Sub9.signlink
				       .anApplet416),
				      new Object[] { Boolean.TRUE });
		    } catch (Throwable throwable) {
			/* empty */
		    }
		}
	    }
	    method28(54);
	    Class34.aClass57_610
		= Queue.method994(Class41.aCanvas778, IsaacPrng.anInt1087,
				    Deque.anInt919, (byte) -120);
	    method20(false);
	    ArchiveWorker.aClass46_1206 = AbstractImage.method1009((byte) 89);
	    ArchiveWorker.aClass46_1206.method938((byte) -50);
	    while (Class15.aLong281 == 0L
		   || Class2.getSystemTime() < Class15.aLong281) {
		Class2.anInt50
		    = ArchiveWorker.aClass46_1206.method939(4,
						      (Class39_Sub5_Sub6
						       .anInt1773),
						      Class15.anInt276);
		for (int i = 0; Class2.anInt50 > i; i++)
		    method16(-1);
		method30((byte) 90);
	    }
	} catch (Exception exception) {
	    Class39_Sub7.method849(exception, 64, null);
	    displayErrorPage("crash");
	}
	method27((byte) 77);
    }
    
    public void windowDeiconified(WindowEvent windowevent) {
	/* empty */
    }
    
    public void method16(int i) {
	long l = Class2.getSystemTime();
	long l_1_ = Class67.aLongArray1172[Class39_Sub5_Sub7.anInt1781];
	if (l_1_ != 0L && l_1_ < l) {
	    /* empty */
	}
	Class67.aLongArray1172[Class39_Sub5_Sub7.anInt1781] = l;
	Class39_Sub5_Sub7.anInt1781 = Class39_Sub5_Sub7.anInt1781 - i & 0x1f;
	synchronized (this) {
	    Class43.aBoolean802 = Class45.aBoolean861;
	}
	method33((byte) 19);
    }
    
    public void method17(int i, int i_2_, int i_3_, int i_4_, int i_5_) {
	try {
	    if (Class25.anApplet_Sub1_465 != null) {
		Class62_Sub1.anInt1595++;
		if (Class62_Sub1.anInt1595 >= 3)
		    displayErrorPage("alreadyloaded");
		else
		    getAppletContext().showDocument(getDocumentBase());
	    } else {
		ArchiveWorker.anInt1198 = i_4_;
		Deque.anInt919 = i_2_;
		IsaacPrng.anInt1087 = i_5_;
		Class25.anApplet_Sub1_465 = this;
		if (Class39_Sub5_Sub9.signlink == null)
		    Cache.aClass21_108 = Class39_Sub5_Sub9.signlink = new Signlink(false, this, InetAddress.getByName(getCodeBase().getHost()), i_3_, null, 0);
		Class39_Sub5_Sub9.signlink.requestThread(this, 1);
	    }
	} catch (Exception exception) {
	    Class39_Sub7.method849(exception, 64, null);
	    displayErrorPage("crash");
	}
    }
    
    public void windowIconified(WindowEvent windowevent) {
	/* empty */
    }
    
    public static void method18(Signlink class21) {
	Cache.aClass21_108 = Class39_Sub5_Sub9.signlink = class21;
    }
    
    public void displayErrorPage(String string) {
	if (!errorPageDisplayed) {
	    errorPageDisplayed = true;
	    System.out.println("error_game_" + string);
	    try {
		getAppletContext().showDocument(new URL(getCodeBase(), ("error_game_" + string + ".ws")));
	    } catch (Exception exception) {
		/* empty */
	    }
	}
    }
    
    public abstract void method20(boolean bool);
    
    public AppletContext getAppletContext() {
	if (Class10.frame != null)
	    return null;
	if (Class39_Sub5_Sub9.signlink != null
	    && Class39_Sub5_Sub9.signlink.anApplet416 != this)
	    return Class39_Sub5_Sub9.signlink.anApplet416
		       .getAppletContext();
	return super.getAppletContext();
    }
    
    public static void method21(byte i) {
	odBuffer = null;
	aLongArray2 = null;
	aClass3_4 = null;
	aClass3_7 = null;
	aClass3_9 = null;
	aClass3_10 = null;
	aClass3_6 = null;
    }
    
    public abstract void method22(int i);
    
    public void method23(int i, InetAddress inetaddress, String string,
			 int i_6_, int i_7_, int i_8_, int i_9_, int i_10_) {
	try {
	    Class25.anApplet_Sub1_465 = this;
	    ArchiveWorker.anInt1198 = i;
	    Deque.anInt919 = i_7_;
	    IsaacPrng.anInt1087 = i_6_;
	    Class10.frame = new Frame();
	    boolean background = Boolean.getBoolean("client.background")
		&& Integer.getInteger("web.bridge.port", -1) > 0;
	    if (background) {
		// Keep a native peer for software rendering without a second game window.
		Class10.frame.setType(java.awt.Window.Type.UTILITY);
		Class10.frame.setFocusableWindowState(false);
		Class10.frame.setLocation(-10000, -10000);
	    }
	    Class10.frame.setTitle("Jagex");
	    Class10.frame.setResizable(true);
	    Class10.frame.setLayout(null);
	    Class10.frame.setBackground(Color.BLACK);
	    Class10.frame.addComponentListener(new ComponentAdapter() {
		public void componentResized(ComponentEvent event) {
		    positionGameCanvas();
		}
	    });
	    Class10.frame.addWindowListener(this);
	    Class10.frame.setVisible(true);
	    if (!background) Class10.frame.toFront();
	    Insets insets = Class10.frame.getInsets();
	    Class10.frame.setSize(insets.right + (insets.left + i_6_), insets.top + i_7_ + insets.bottom);
	    Class10.frame.setMinimumSize(new Dimension(Class10.frame.getWidth(), Class10.frame.getHeight()));
	    if (!background) ModHook.init(Class10.frame, this);
	    Cache.aClass21_108 = Class39_Sub5_Sub9.signlink = new Signlink(true, null, inetaddress, i_10_, string, i_8_);
	    Class39_Sub5_Sub9.signlink.requestThread(this, 1);
	} catch (Exception exception) {
	    Class39_Sub7.method849(exception, 64, null);
	}
    }
    
    @Override
    public URL getCodeBase() {
	if (Class10.frame != null)
	    return null;
	if (Class39_Sub5_Sub9.signlink != null
	    && this != Class39_Sub5_Sub9.signlink.anApplet416)
	    return Class39_Sub5_Sub9.signlink.anApplet416.getCodeBase();
	return super.getCodeBase();
    }
    
    public static void method24(int i, int i_11_, byte i_12_, int i_13_,
				NpcDefinition class39_sub5_sub13) {
	if (Class39_Sub5_Sub11.anInt1841 < 400) {
	    if (class39_sub5_sub13.anIntArray1878 != null)
		class39_sub5_sub13 = class39_sub5_sub13.method721(5585);
	    if (class39_sub5_sub13 != null
		&& class39_sub5_sub13.aBoolean1886) {
		JString class3 = class39_sub5_sub13.aClass3_1881;
		if (class39_sub5_sub13.anInt1859 != 0)
		    class3 = (Class39_Sub5_Sub11.method708
			      ((new JString[]
				{ class3,
				  (Class39_Sub5_Sub6.method579
				   ((Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109
				     .anInt2506),
				    class39_sub5_sub13.anInt1859, false)),
				  Class39_Sub5_Sub18.aClass3_2118,
				  Client.aClass3_1270,
				  AbstractImage.method1007((byte) 71,
						     (class39_sub5_sub13
						      .anInt1859)),
				  Queue.aClass3_984 })));
		if (Class13.anInt208 != 1) {
		    if (IsaacPrng.aBoolean1100) {
			if ((Class41.anInt776 & 0x2) == 2)
			    JString.method55(i, Client.aClass3_1273, i_13_,
					    (Class39_Sub5_Sub11.method708
					     ((new JString[]
					       { Class14.aClass3_216,
						 (Class39_Sub5_Sub9
						  .aClass3_1817),
						 class3 }))),
					    (byte) -116, i_11_, 48);
		    } else {
			JString[] class3s = class39_sub5_sub13.aClass3Array1866;
			if (Class45.aBoolean862)
			    class3s = BufferedFile.method225((byte) 127, class3s);
			if (class3s != null) {
			    for (int i_14_ = 4; i_14_ >= 0; i_14_--) {
				if (class3s[i_14_] != null
				    && !class3s[i_14_].method97(aClass3_7,
								-66)) {
				    int i_15_ = 0;
				    if (i_14_ == 0)
					i_15_ = 42;
				    if (i_14_ == 1)
					i_15_ = 17;
				    if (i_14_ == 2)
					i_15_ = 13;
				    if (i_14_ == 3)
					i_15_ = 53;
				    if (i_14_ == 4)
					i_15_ = 46;
				    JString.method55(i, class3s[i_14_], i_13_,
						    (Class39_Sub5_Sub11
							 .method708
						     ((new JString[]
						       { HashTable.aClass3_368,
							 class3 }))),
						    (byte) -73, i_11_, i_15_);
				}
			    }
			}
			if (class3s != null) {
			    for (int i_16_ = 4; i_16_ >= 0; i_16_--) {
				if (class3s[i_16_] != null
				    && class3s[i_16_].method97(aClass3_7,
							       -66)) {
				    int i_17_ = 0;
				    int i_18_ = 0;
				    if ((Cache
					 .aClass39_Sub5_Sub4_Sub4_Sub2_109
					 .anInt2506)
					< class39_sub5_sub13.anInt1859)
					i_18_ = 2000;
				    if (i_16_ == 0)
					i_17_ = i_18_ + 42;
				    if (i_16_ == 1)
					i_17_ = i_18_ + 17;
				    if (i_16_ == 2)
					i_17_ = i_18_ + 13;
				    if (i_16_ == 3)
					i_17_ = 53 + i_18_;
				    if (i_16_ == 4)
					i_17_ = i_18_ + 46;
				    JString.method55(i, class3s[i_16_], i_13_,
						    (Class39_Sub5_Sub11
							 .method708
						     ((new JString[]
						       { HashTable.aClass3_368,
							 class3 }))),
						    (byte) -127, i_11_, i_17_);
				}
			    }
			}
			JString.method55(i, RuntimeException_Sub1.aClass3_1224,
					i_13_,
					(Class39_Sub5_Sub11.method708
					 (new JString[] { HashTable.aClass3_368,
							 class3 })),
					(byte) -15, i_11_, 1001);
		    }
		} else
		    JString.method55(i, Class39_Sub5_Sub4_Sub4.aClass3_2310,
				    i_13_,
				    (Class39_Sub5_Sub11.method708
				     ((new JString[]
				       { Class39_Sub10.aClass3_1436,
					 Class39_Sub5_Sub9.aClass3_1817,
					 class3 }))),
				    (byte) -64, i_11_, 47);
	    }
	}
    }
    
    public static void method25
	(Player class39_sub5_sub4_sub4_sub2, byte i,
	 int i_19_, int i_20_) {
	if ((i_20_ & 0x40) != 0) {
	    int i_21_ = Class39_Sub5_Sub11.gameBuffer
			    .method788((byte) 48);
	    byte[] is = new byte[i_21_];
	    Buffer class39_sub6 = new Buffer(is);
	    Class39_Sub5_Sub11.gameBuffer.method781(is, i_21_,
								 -25923, 0);
	    IsaacPrng.aClass39_Sub6Array1104[i_19_] = class39_sub6;
	    class39_sub5_sub4_sub4_sub2.parseAppearance(class39_sub6);
	}
	if ((i_20_ & 0x100) != 0) {
	    class39_sub5_sub4_sub4_sub2.anInt2287
		= Class39_Sub5_Sub11.gameBuffer.method804(39);
	    class39_sub5_sub4_sub4_sub2.anInt2266
		= Class39_Sub5_Sub11.gameBuffer
		      .method815((byte) 109);
	    class39_sub5_sub4_sub4_sub2.anInt2279
		= Class39_Sub5_Sub11.gameBuffer.method804(50);
	    class39_sub5_sub4_sub4_sub2.anInt2277
		= Class39_Sub5_Sub11.gameBuffer.method804(40);
	    class39_sub5_sub4_sub4_sub2.anInt2256
		= Class39_Sub5_Sub11.gameBuffer
		      .method833((byte) 127) + Class2.logicCycle;
	    class39_sub5_sub4_sub4_sub2.anInt2261
		= Class39_Sub5_Sub11.gameBuffer
		      .method833((byte) 125) + Class2.logicCycle;
	    class39_sub5_sub4_sub4_sub2.anInt2292
		= Class39_Sub5_Sub11.gameBuffer
		      .method815((byte) -122);
	    class39_sub5_sub4_sub4_sub2.method515(0);
	}
	if ((i_20_ & 0x200) != 0) {
	    class39_sub5_sub4_sub4_sub2.anInt2270
		= Class39_Sub5_Sub11.gameBuffer.getUword();
	    int i_22_ = Class39_Sub5_Sub11.gameBuffer
			    .method812(788075800);
	    class39_sub5_sub4_sub4_sub2.anInt2272
		= (i_22_ & 0xffff) + Class2.logicCycle;
	    if (class39_sub5_sub4_sub4_sub2.anInt2270 == 65535)
		class39_sub5_sub4_sub4_sub2.anInt2270 = -1;
	    class39_sub5_sub4_sub4_sub2.anInt2304 = 0;
	    class39_sub5_sub4_sub4_sub2.anInt2276 = 0;
	    class39_sub5_sub4_sub4_sub2.anInt2288 = i_22_ >> 16;
	    if (Class2.logicCycle < class39_sub5_sub4_sub4_sub2.anInt2272)
		class39_sub5_sub4_sub4_sub2.anInt2276 = -1;
	}
	if ((i_20_ & 0x1) != 0) {
	    class39_sub5_sub4_sub4_sub2.aClass3_2295
		= Class39_Sub5_Sub11.gameBuffer
		      .getJstr();
	    if (class39_sub5_sub4_sub4_sub2.aClass3_2295.charAt(0)
		!= 126) {
		if (class39_sub5_sub4_sub4_sub2
		    == Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109)
		    JMouseListener.method902(class39_sub5_sub4_sub4_sub2.aClass3_2521,
				      class39_sub5_sub4_sub4_sub2.aClass3_2295,
				      false, 2);
	    } else {
		class39_sub5_sub4_sub4_sub2.aClass3_2295
		    = class39_sub5_sub4_sub4_sub2.aClass3_2295.method85(-58,
									1);
		JMouseListener.method902(class39_sub5_sub4_sub4_sub2.aClass3_2521,
				  class39_sub5_sub4_sub4_sub2.aClass3_2295,
				  false, 2);
	    }
	    class39_sub5_sub4_sub4_sub2.anInt2271 = 0;
	    class39_sub5_sub4_sub4_sub2.anInt2259 = 150;
	    class39_sub5_sub4_sub4_sub2.anInt2296 = 0;
	}
	if ((i_20_ & 0x10) != 0) {
	    class39_sub5_sub4_sub4_sub2.anInt2260
		= Class39_Sub5_Sub11.gameBuffer
		      .method833((byte) 119);
	    if (class39_sub5_sub4_sub4_sub2.anInt2260 == 65535)
		class39_sub5_sub4_sub4_sub2.anInt2260 = -1;
	}
	if ((i_20_ & 0x2) != 0) {
	    class39_sub5_sub4_sub4_sub2.anInt2316
		= Class39_Sub5_Sub11.gameBuffer
		      .method833((byte) 126);
	    class39_sub5_sub4_sub4_sub2.anInt2300
		= Class39_Sub5_Sub11.gameBuffer.getUwordLe();
	}
	if ((i_20_ & 0x8) != 0) {
	    int i_23_ = Class39_Sub5_Sub11.gameBuffer
			    .method833((byte) 121);
	    int i_24_ = Class39_Sub5_Sub11.gameBuffer
			    .method815((byte) -10);
	    if (i_23_ == 65535)
		i_23_ = -1;
	    Class46_Sub1.method944(class39_sub5_sub4_sub4_sub2, i_23_, -8,
				   i_24_);
	}
	if ((i_20_ & 0x4) != 0) {
	    int i_25_
		= Class39_Sub5_Sub11.gameBuffer.getUword();
	    int i_26_ = Class39_Sub5_Sub11.gameBuffer
			    .getUbyte();
	    int i_27_ = Class39_Sub5_Sub11.gameBuffer
			    .method788((byte) -117);
	    int i_28_ = Class39_Sub5_Sub11.gameBuffer.offset;
	    if (class39_sub5_sub4_sub4_sub2.aClass3_2521 != null
		&& class39_sub5_sub4_sub4_sub2.aClass45_2516 != null) {
		long l = class39_sub5_sub4_sub4_sub2.aClass3_2521
			     .encodeBase37();
		boolean bool = false;
		if (i_26_ <= 1) {
		    for (int i_29_ = 0; Class15.amountIgnores > i_29_; i_29_++) {
			if (Class39_Sub5_Sub9.ignoreUsernames[i_29_] == l) {
			    bool = true;
			    break;
			}
		    }
		}
		if (!bool && Class36.anInt630 == 0) {
		    Player.aClass39_Sub6_2515.offset
			= 0;
		    Class39_Sub5_Sub11.gameBuffer.method783
			(i_27_, 2914, (Player
				       .aClass39_Sub6_2515.payload), 0);
		    Player.aClass39_Sub6_2515.offset
			= 0;
		    JString class3
			= Class63.decodeHuffmans
			      (Player.aClass39_Sub6_2515)
			      .method58(true);
		    class39_sub5_sub4_sub4_sub2.aClass3_2295
			= class3.method69((byte) -45);
		    class39_sub5_sub4_sub4_sub2.anInt2259 = 150;
		    class39_sub5_sub4_sub4_sub2.anInt2296 = i_25_ >> 8;
		    class39_sub5_sub4_sub4_sub2.anInt2271 = i_25_ & 0xff;
		    if (i_26_ == 2 || i_26_ == 3)
			JMouseListener.method902((Class39_Sub5_Sub11.method708
					   ((new JString[]
					     { Class53.aClass3_959,
					       (class39_sub5_sub4_sub4_sub2
						.aClass3_2521) }))),
					  class3, false, 1);
		    else if (i_26_ != 1)
			JMouseListener.method902((class39_sub5_sub4_sub4_sub2
					   .aClass3_2521),
					  class3, false, 2);
		    else
			JMouseListener.method902((Class39_Sub5_Sub11.method708
					   ((new JString[]
					     { Class37.aClass3_661,
					       (class39_sub5_sub4_sub4_sub2
						.aClass3_2521) }))),
					  class3, false, 1);
		}
	    }
	    Class39_Sub5_Sub11.gameBuffer.offset
		= i_28_ + i_27_;
	}
	if ((i_20_ & 0x400) != 0) {
	    int i_30_ = Class39_Sub5_Sub11.gameBuffer
			    .getUbyte();
	    int i_31_
		= Class39_Sub5_Sub11.gameBuffer.method804(68);
	    class39_sub5_sub4_sub4_sub2.method513(-89, Class2.logicCycle, i_30_,
						  i_31_);
	    class39_sub5_sub4_sub4_sub2.anInt2252 = Class2.logicCycle + 300;
	    class39_sub5_sub4_sub4_sub2.anInt2318
		= Class39_Sub5_Sub11.gameBuffer
		      .method788((byte) 80);
	    class39_sub5_sub4_sub4_sub2.anInt2269
		= Class39_Sub5_Sub11.gameBuffer
		      .getUbyte();
	}
	if ((i_20_ & 0x20) != 0) {
	    int i_32_ = Class39_Sub5_Sub11.gameBuffer
			    .method815((byte) 102);
	    int i_33_ = Class39_Sub5_Sub11.gameBuffer
			    .getUbyte();
	    class39_sub5_sub4_sub4_sub2.method513(-116, Class2.logicCycle, i_32_,
						  i_33_);
	    class39_sub5_sub4_sub4_sub2.anInt2252 = Class2.logicCycle + 300;
	    class39_sub5_sub4_sub4_sub2.anInt2318
		= Class39_Sub5_Sub11.gameBuffer
		      .method815((byte) 124);
	    class39_sub5_sub4_sub4_sub2.anInt2269
		= Class39_Sub5_Sub11.gameBuffer
		      .method815((byte) 105);
	}
    }
    
    public void update(Graphics graphics) {
	paint(graphics);
    }
    
    public void windowOpened(WindowEvent windowevent) {
	/* empty */
    }
    
    public static void method26(int i, int i_34_, int i_35_, int i_36_,
				int i_37_, int i_38_, int i_39_) {
	int i_40_ = -i_38_ + 2048 & 0x7ff;
	int i_41_ = -i_35_ + 2048 & 0x7ff;
	int i_42_ = 0;
	int i_43_ = 0;
	int i_44_ = i_37_;
	if (i_40_ != 0) {
	    int i_45_ = Model.anIntArray2394[i_40_];
	    int i_46_ = Model.anIntArray2418[i_40_];
	    int i_47_ = i_46_ * i_42_ - i_44_ * i_45_ >> 16;
	    i_44_ = i_45_ * i_42_ + i_44_ * i_46_ >> 16;
	    i_42_ = i_47_;
	}
	if (i_41_ != 0) {
	    int i_48_ = Model.anIntArray2394[i_41_];
	    int i_49_ = Model.anIntArray2418[i_41_];
	    int i_50_ = i_44_ * i_48_ + i_49_ * i_43_ >> 16;
	    i_44_ = -(i_48_ * i_43_) + i_49_ * i_44_ >> 16;
	    i_43_ = i_50_;
	}
	Node.anInt742 = -i_44_ + i_36_;
	Class43.anInt799 = i_38_;
	Class39_Sub10.anInt1437 = -i_42_ + i_39_;
	Class39_Sub5_Sub4_Sub4.anInt2315 = i_35_;
	Class39_Sub11.anInt1470 = i_34_ - i_43_;
    }
    
    public synchronized void method27(byte i) {
	if (i <= 56)
	    encryptionKey = (byte) -114;
	if (!Class62.aBoolean1112) {
	    Class62.aBoolean1112 = true;
	    try {
		Class41.aCanvas778.removeFocusListener(this);
	    } catch (Exception exception) {
		/* empty */
	    }
	    try {
		method22(4258);
	    } catch (Exception exception) {
		/* empty */
	    }
	    if (Class10.frame != null) {
		try {
		    System.exit(0);
		} catch (Throwable throwable) {
		    /* empty */
		}
	    }
	    if (Class39_Sub5_Sub9.signlink != null) {
		try {
		    Class39_Sub5_Sub9.signlink.method257((byte) 117);
		} catch (Exception exception) {
		    /* empty */
		}
	    }
	    method31(503);
	}
    }
    
    private synchronized void positionGameCanvas() {
	if (Class10.frame == null || Class41.aCanvas778 == null)
	    return;
	Insets insets = Class10.frame.getInsets();
	int width = Class10.frame.getWidth() - insets.left - insets.right;
	int height = Class10.frame.getHeight() - insets.top - insets.bottom;
	int dock = ModPanel.dockWidth();
	if (ModPanel.inst != null)
	{
	    ModPanel.inst.setBounds(insets.left + width - dock, insets.top, dock, height);
	    ModPanel.inst.doLayout();
	    ModPanel.inst.validate();
	    ModPanel.inst.repaint();
	}
	// The canvas fills everything left of the side bar; Canvas_Sub1 fits the game picture inside it.
	Class41.aCanvas778.setBounds(insets.left, insets.top,
	    Math.max(1, width - dock), Math.max(1, height));
	ClientScript.aBoolean1690 = true;
    }

    public void relayoutCanvas() {
	positionGameCanvas();
    }

    public synchronized void method28(int i) {
	Container container;
	if (Class10.frame != null)
	    container = Class10.frame;
	else
	    container = Class39_Sub5_Sub9.signlink.anApplet416;
	if (Class41.aCanvas778 != null) {
	    Class41.aCanvas778.removeFocusListener(this);
	    container.remove(Class41.aCanvas778);
	}
	Class41.aCanvas778 = new Canvas_Sub1(this);
	ModHook.app = Class41.aCanvas778;
	ModHook.hookWheel(Class41.aCanvas778);
	Class41.aCanvas778.setBackground(Color.BLACK);
	container.add(Class41.aCanvas778);
	Class41.aCanvas778.setSize(IsaacPrng.anInt1087, Deque.anInt919);
	Class41.aCanvas778.setVisible(true);
	if (Class10.frame != null) {
	    positionGameCanvas();
	} else
	    Class41.aCanvas778.setLocation(0, 0);
	Class41.aCanvas778.addFocusListener(this);
	Class41.aCanvas778.requestFocus();
	ClientScript.aBoolean1690 = true;
	Class39_Sub7.aBoolean1373 = false;
	Class41.aLong782 = Class2.getSystemTime();
    }
    
    public synchronized void paint(Graphics graphics) {
	if (this == Class25.anApplet_Sub1_465 && !Class62.aBoolean1112) {
	    ClientScript.aBoolean1690 = true;
	    if (Signlink.javaVersion != null
		&& Signlink.javaVersion.startsWith("1.5")
		&& Class2.getSystemTime() - Class41.aLong782 > 1000L) {
		Rectangle rectangle = graphics.getClipBounds();
		if (rectangle == null
		    || (IsaacPrng.anInt1087 <= rectangle.width
			&& Deque.anInt919 <= rectangle.height))
		    Class39_Sub7.aBoolean1373 = true;
	    }
	}
    }
    
    public void stop() {
	if (this == Class25.anApplet_Sub1_465 && !Class62.aBoolean1112)
	    Class15.aLong281 = Class2.getSystemTime() + 4000L;
    }
    
    public boolean method29(int i) {
	String string = getDocumentBase().getHost().toLowerCase();
	if (string.endsWith("jagex.com"))
	    return true;
	if (string.endsWith("runescape.com"))
	    return true;
	if (string.endsWith("127.0.0.1"))
	    return true;
	for (/**/;
	     (string.length() > 0 && string.charAt(string.length() - 1) >= '0'
	      && string.charAt(string.length() - 1) <= '9');
	     string = string.substring(0, string.length() - 1)) {
	    /* empty */
	}
	if (string.endsWith("192.168.1."))
	    return true;
	displayErrorPage("invalidhost");
	return false;
    }
    
    public void destroy() {
	if (this == Class25.anApplet_Sub1_465 && !Class62.aBoolean1112) {
	    Class15.aLong281 = Class2.getSystemTime();
	    Class45.sleep(5000L);
	    Cache.aClass21_108 = null;
	    method27((byte) 92);
	}
    }
    
    public void windowClosed(WindowEvent windowevent) {
	/* empty */
    }
    
    public void windowDeactivated(WindowEvent windowevent) {
	/* empty */
    }
    
    public void method30(byte i) {
	if (i < 67)
	    method21((byte) 122);
	long l = Class2.getSystemTime();
	long l_51_ = Class39_Sub7.aLongArray1374[JMouseListener.anInt793];
	Class39_Sub7.aLongArray1374[JMouseListener.anInt793] = l;
	if (l_51_ != 0L && l_51_ < l) {
	    int i_52_ = (int) (l + -l_51_);
	    Class39_Sub5_Sub18.anInt2119 = (32000 + (i_52_ >> 1)) / i_52_;
	}
	JMouseListener.anInt793 = JMouseListener.anInt793 + 1 & 0x1f;
	if (Class45.anInt855++ > 50) {
	    Class45.anInt855 -= 50;
	    ClientScript.aBoolean1690 = true;
	    Class41.aCanvas778.setVisible(true);
	    if (Class10.frame == null) {
		Class41.aCanvas778.setSize(IsaacPrng.anInt1087, Deque.anInt919);
		Class41.aCanvas778.setLocation(0, 0);
	    } else {
		positionGameCanvas();
	    }
	}
	method32(true);
	WebClientBridge.publishFrame();
    }
    
    public abstract void method31(int i);
    
    public abstract void init();
    
    public URL getDocumentBase() {
	if (Class10.frame != null)
	    return null;
	if (Class39_Sub5_Sub9.signlink != null
	    && this != Class39_Sub5_Sub9.signlink.anApplet416)
	    return Class39_Sub5_Sub9.signlink.anApplet416
		       .getDocumentBase();
	return super.getDocumentBase();
    }
    
    public void focusLost(FocusEvent focusevent) {
	Class45.aBoolean861 = false;
    }
    
    public void windowActivated(WindowEvent windowevent) {
	/* empty */
    }
    
    public abstract void method32(boolean bool);
    
    public abstract void method33(byte i);
    
    static {
	odBuffer = new Buffer(8);
	aClass3_6
	    = Class39_Sub5_Sub9.createJstring("Loading title screen )2 ");
	aClass3_10 = aClass3_6;
	aClass3_9 = Class39_Sub5_Sub9.createJstring("Attack");
	aClass3_7 = aClass3_9;
    }
}

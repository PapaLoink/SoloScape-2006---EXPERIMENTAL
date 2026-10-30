package unpackaged;

/* Canvas_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
import jagex.utils.Huffmans;
import jagex.graphics.AbstractImage;
import jagex.world.actors.StillGraphic;
import jagex.utils.Node;
import jagex.utils.JString;
import jagex.io.JSocket;
import jagex.utils.Deque;
import jagex.io.Buffer;
import jagex.utils.Cache;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import jagex.utils.IsaacPrng;

public class Canvas_Sub1 extends Canvas
{
    public static JString aClass3_13;
    public static JString aClass3_14
	= Class39_Sub5_Sub9.createJstring("white:");
    // Browser touch input waits for rendered scene picking on another thread.
    public static volatile int anInt15;
    public static JString aClass3_16;
    public static JString aClass3_17
	= (Class39_Sub5_Sub9.createJstring
	   ("und die Schaltfl-=che (WSpielkonto erstellen(W am"));
    public static JString aClass3_18;
    public static JString aClass3_19;
    public static JString aClass3_20;
    public static int pendingRegularRequests;
    public Component aComponent22;
    public static JString aClass3_23;
    public static int anInt24;
    
    public void update(Graphics graphics) {
	aComponent22.update(graphics);
    }

    @Override
    public Graphics getGraphics() {
	return scaleGameGraphics(super.getGraphics());
    }

    /** Unscaled graphics for the whole canvas (margins, buttons). */
    Graphics rawGraphics() {
	return super.getGraphics();
    }

    Graphics scaleGameGraphics(Graphics graphics) {
	if (graphics instanceof Graphics2D && IsaacPrng.anInt1087 > 0 && Deque.anInt919 > 0) {
	    Graphics2D graphics2d = (Graphics2D) graphics;
	    double[] m = ModHook.geom(getWidth(), getHeight());
	    graphics2d.translate(m[2], m[3]);
	    graphics2d.scale(m[0], m[1]);
	    graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	}
	return graphics;
    }

    private MouseEvent gameMouseEvent(MouseEvent event) {
	if (Resizable.active()) {
	    int[] p = Resizable.map(event.getX(), event.getY(), getWidth(), getHeight());
	    return new MouseEvent(this, event.getID(), event.getWhen(),
		event.getModifiersEx(), p[0], p[1], event.getXOnScreen(), event.getYOnScreen(),
		event.getClickCount(), event.isPopupTrigger(), event.getButton());
	}
	double[] m = ModHook.geom(getWidth(), getHeight());
	int x = (int) Math.floor((event.getX() - m[2]) / m[0]);
	int y = (int) Math.floor((event.getY() - m[3]) / m[1]);
	x = Math.max(0, Math.min(IsaacPrng.anInt1087 - 1, x));
	y = Math.max(0, Math.min(Deque.anInt919 - 1, y));
	return new MouseEvent(this, event.getID(), event.getWhen(),
	    event.getModifiersEx(), x, y, event.getXOnScreen(), event.getYOnScreen(),
	    event.getClickCount(), event.isPopupTrigger(), event.getButton());
    }

    @Override
    protected void processMouseEvent(MouseEvent event) {
	if (ModHook.mouse(event, this)) { event.consume(); return; }
	MouseEvent gameEvent = gameMouseEvent(event);
	super.processMouseEvent(gameEvent);
	if (gameEvent.isConsumed()) event.consume();
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent event) {
	if (ModHook.mouse(event, this)) { event.consume(); return; }
	MouseEvent gameEvent = gameMouseEvent(event);
	super.processMouseMotionEvent(gameEvent);
	if (gameEvent.isConsumed()) event.consume();
    }

    // The web bridge supplies coordinates in the original game resolution.
    int toCanvasX(int x) {
	double[] m = ModHook.geom(getWidth(), getHeight());
	return (int) Math.ceil(m[2] + x * m[0]);
    }

    int toCanvasY(int y) {
	double[] m = ModHook.geom(getWidth(), getHeight());
	return (int) Math.ceil(m[3] + y * m[1]);
    }
    
    public static void method39(int i) {
	Cache.method130((byte) 54);
	if (Class4.anInt80 == 1)
	    Class47.aClass39_Sub5_Sub10_Sub3Array893
		[Class26.anInt503 / 100].method670
		(Resizable.vx(Class62_Sub1.anInt1590) - 8,
		 Resizable.vy(Class62_Sub2.anInt1596) - 8);
	if (Class4.anInt80 == 2)
	    Class47.aClass39_Sub5_Sub10_Sub3Array893
		[Class26.anInt503 / 100 + 4].method670
		(Resizable.vx(Class62_Sub1.anInt1590) - 8,
		 Resizable.vy(Class62_Sub2.anInt1596) - 8);
	if (i == -32024) {
	    if (Class26.anInt485 != -1) {
		Class39_Sub4.method459(Class26.anInt485, (byte) 127);
		Deque.method955(Resizable.iy() + 334, Resizable.ix() + 512, Resizable.ix(), Class26.anInt485, Resizable.iy(), -1, 4);
	    }
	    if (Class39_Sub11.anInt1478 != -1) {
		Class39_Sub4.method459(Class39_Sub11.anInt1478, (byte) 112);
		Deque.method955(Resizable.iy() + 334, Resizable.ix() + 512, Resizable.ix(), Class39_Sub11.anInt1478, Resizable.iy(), -1,
				  0);
	    }
	    Class39_Sub5_Sub4.method487(false);
	    if (!Class39_Sub12.aBoolean1493) {
		JString.method95(i ^ ~0x7d26);
		Class31.method320(-23401);
	    } else if (Class37.anInt653 == 0)
		Class1.method49(-104);
	    if (Huffmans.anInt756 == 1)
		Widget.aClass39_Sub5_Sub10_Sub3_2113
		    .method670(472, 296);
            if (OndemandRequest.aBoolean1718) {
                FpsDebugOverlay.draw();
                // Avoid the old redrawn-panel notices obscuring the diagnostics.
                Class44.aBoolean833 = false;
                Class39_Sub5_Sub14.aBoolean1908 = false;
                NameTable.aBoolean183 = false;
            }
	    // Client setting: small yellow FPS counter at the top left of the game view. It is drawn into the
	    // game picture itself, so it scales with it when stretched or fullscreen.
	    jagex.graphics.BitmapFont small = jagex.world.actors.Npc.aClass39_Sub5_Sub10_Sub1_2495;   // p11_full
	    if (ModHook.showFps && small != null) {
		JString fps = Class39_Sub5_Sub9.createJstring("Fps:" + Class39_Sub5_Sub18.anInt2119);
		// Below the "Walk here" hover text, which the game draws along the top edge.
		small.method647(fps, 5, 32, 0);
		small.method647(fps, 4, 31, 16776960);
	    }
	    if (Class39_Sub7.anInt1380 != 0) {
		int i_4_ = Class39_Sub7.anInt1380 / 50;
		int i_5_ = i_4_ / 60;
		i_4_ %= 60;
		if (i_4_ < 10)
		    Class39_Sub5_Sub14.p12fullFont.method647
			((Class39_Sub5_Sub11.method708
			  (new JString[] { Class39_Sub4.aClass3_1326,
					  AbstractImage.method1007((byte) 71, i_5_),
					  Node.aClass3_743,
					  AbstractImage.method1007((byte) 71,
							     i_4_) })),
			 4, 329, 16776960);
		else
		    Class39_Sub5_Sub14.p12fullFont.method647
			((Class39_Sub5_Sub11.method708
			  (new JString[] { Class39_Sub4.aClass3_1326,
					  AbstractImage.method1007((byte) 71, i_5_),
					  OndemandRequest.aClass3_1723,
					  AbstractImage.method1007((byte) 71,
							     i_4_) })),
			 4, 329, 16776960);
	    }
	}
    }
    
    public void paint(Graphics graphics) {
	aComponent22.paint(graphics);
    }
    
    public static ObjectDefinition method40(int i, byte i_6_) {
	ObjectDefinition class39_sub5_sub15
	    = ((ObjectDefinition)
	       NameTable.aClass7_184.get((long) i));
	if (class39_sub5_sub15 != null)
	    return class39_sub5_sub15;
	byte[] is = Deque.objectFileLoader.lookupFile(6, i);
	class39_sub5_sub15 = new ObjectDefinition();
	class39_sub5_sub15.anInt1931 = i;
	if (is != null)
	    class39_sub5_sub15.decode(new Buffer(is));
	class39_sub5_sub15.method739(false);
	if (class39_sub5_sub15.aBoolean1953) {
	    class39_sub5_sub15.aBoolean1949 = false;
	    class39_sub5_sub15.aBoolean1928 = false;
	}
	NameTable.aClass7_184.put(class39_sub5_sub15, (long) i,
				      (byte) 127);
	return class39_sub5_sub15;
    }
    
    public static void method41(byte i) {
	for (Class39_Sub10 class39_sub10
		 = (Class39_Sub10) Class4.aClass49_74.getFirst();
	     class39_sub10 != null;
	     class39_sub10
		 = (Class39_Sub10) Class4.aClass49_74.getNext()) {
	    if (class39_sub10.aClass39_Sub5_Sub15_1428 != null)
		class39_sub10.method864(-67);
	}
    }
    
    public Canvas_Sub1(Component component) {
	aComponent22 = component;
    }
    
    public static void method42(int i) {
	aClass3_20 = null;
	aClass3_16 = null;
	aClass3_23 = null;
	aClass3_17 = null;
	aClass3_13 = null;
	aClass3_14 = null;
	aClass3_18 = null;
	aClass3_19 = null;
    }
    
    static {
	aClass3_13 = aClass3_14;
	aClass3_20 = Class39_Sub5_Sub9.createJstring("Cabbage");
	pendingRegularRequests = 0;
	aClass3_18
	    = Class39_Sub5_Sub9
		  .createJstring("Zu viele Verbindungen von Ihrer Adresse)3");
	anInt15 = 0;
	aClass3_23
	    = Class39_Sub5_Sub9.createJstring("Login server offline)3");
	aClass3_19 = Class39_Sub5_Sub9.createJstring("Neuer Benutzer");
	aClass3_16 = aClass3_23;
	anInt24 = 0;
    }
}

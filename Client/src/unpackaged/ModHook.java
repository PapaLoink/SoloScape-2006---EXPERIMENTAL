package unpackaged;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Properties;
import jagex.io.FrameBuffer;
import jagex.utils.Cache;
import jagex.utils.JString;
import jagex.utils.SubNode;
import jagex.world.actors.Player;

/**
 * Client extras: docked side bar, stretch-to-fill, fullscreen, WASD camera with the
 * "Press Enter to begin talking" line, music mute button.
 */
public final class ModHook {
    static Component app;               // the game canvas
    static Frame frame;
    static ClientApplet shell;
    static volatile boolean wasd = true;
    static volatile boolean stretch = false;
    static volatile boolean panelVisible = true;
    static volatile boolean muted = false;
    static boolean fullscreen = false;
    private static boolean chatFocus;
    private static final Properties SET = new Properties();
    static final int BASE_W = 765, BASE_H = 503;
    static final String PH = "Press Enter to begin talking";

    // ------------------------------------------------------------------ start
    // ---- camera zoom: distance percent, 70 = 30% closer, 160 = 60% farther
    public static int zoomPct = 100;
    public static int zoomDist(int d) {
        return (int) ((long) d * zoomPct / 100);
    }
    public static void hookWheel(Component c) {
        c.addMouseWheelListener(e -> {
            int step = e.getWheelRotation() * 10;
            if (step == 0) return;
            // only zoom when the pointer is over the 3D view and no interface (bank, shop, scroll...) covers it
            try {
                int mx = jagex.utils.IsaacPrng.anInt1091, my = Class33.anInt599;
                boolean overView = Class31.state == 30 && mx > 4 && mx < 516 && my > 4 && my < 338;
                if (!overView || Class39_Sub11.anInt1478 != -1 || SubNode.anInt1348 != -1) return;
            } catch (Throwable t) { return; }
            zoomPct = Math.max(65, Math.min(175, zoomPct + step));
        });
    }

    public static void init(Frame f, ClientApplet self) {
        if (frame != null) return;
        frame = f;
        shell = self;
        loadSettings();
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                ModPanel.install(f);
                Insets in = f.getInsets();
                f.setMinimumSize(new Dimension(in.left + in.right + BASE_W + 53, in.top + in.bottom + BASE_H));
                f.setExtendedState(Frame.MAXIMIZED_BOTH);
                relayout();
            } catch (Throwable t) { t.printStackTrace(); }
        });
        new javax.swing.Timer(250, e -> { overlayTick(); loginTick(); if (!inGame()) { postCutoffSent = false; classicUiSent = false; } }).start();
    }

    // ---- remembered login (./login.dat, base64 of the raw field bytes)
    static boolean loginPrefilled, loginLoaded;
    /** When off, the login is neither prefilled nor saved, and any saved login is deleted. */
    static volatile boolean rememberLogin = true;

    static void setRememberLogin(boolean b) {
        rememberLogin = b;
        if (!b) { try { new File("./login.dat").delete(); } catch (Throwable t) { } savedUser = ""; savedPass = ""; }
        saveSettings();
    }
    static String savedUser = "", savedPass = "";
    static byte[] rawBytes(JString j) {
        if (j == null || j.bytes == null) return new byte[0];
        return java.util.Arrays.copyOf(j.bytes, Math.min(j.length, j.bytes.length));
    }
    static JString fromRaw(byte[] b) {
        JString j = new JString();
        j.bytes = b.clone();
        j.length = b.length;
        return j;
    }
    static void loginTick() {
        try {
            if (!loginLoaded) {
                loginLoaded = true;
                File f = new File("./login.dat");
                if (f.exists()) {
                    String[] p = new String(java.util.Base64.getDecoder().decode(java.nio.file.Files.readAllBytes(f.toPath())), "ISO-8859-1").split("\n", -1);
                    if (p.length >= 2) { savedUser = p[0]; savedPass = p[1]; }
                } else loginPrefilled = true;
            }
            if (!rememberLogin) return;
            JString u = Class39_Sub5_Sub14.aClass3_1897, pw = Class39_Sub5_Sub14.aClass3_1905;
            if (u == null || pw == null) return;
            int st = Class31.state;
            if (st == 10 || (!loginPrefilled && st >= 10 && st < 30)) {
                // title screen (also after logging out): refill the remembered login if the fields are blank
                loginPrefilled = true;
                if (!savedUser.isEmpty() && u.getLength() == 0 && pw.getLength() == 0) {
                    Class39_Sub5_Sub14.aClass3_1897 = fromRaw(savedUser.getBytes("ISO-8859-1"));
                    Class39_Sub5_Sub14.aClass3_1905 = fromRaw(savedPass.getBytes("ISO-8859-1"));
                }
            } else if (st == 30 && u.getLength() > 0) {
                String nu = new String(rawBytes(u), "ISO-8859-1"), np = new String(rawBytes(pw), "ISO-8859-1");
                if (!nu.equals(savedUser) || !np.equals(savedPass)) {
                    java.nio.file.Files.write(new File("./login.dat").toPath(),
                        java.util.Base64.getEncoder().encode((nu + "\n" + np).getBytes("ISO-8859-1")));
                    savedUser = nu; savedPass = np;
                }
            }
        } catch (Throwable t) { }
    }

    // idle logout: 0 = default 90 s, 1 = 5 min, 2 = 15 min, 3 = 30 min, 4 = never (ticks are 20 ms)
    public static int idleChoice = 0;
    public static final String[] IDLE_NAMES = {"Default (90 seconds)", "5 minutes", "15 minutes", "30 minutes", "Never"};
    public static int idleTicks() {
        switch (idleChoice) {
            case 1: return 15000;
            case 2: return 45000;
            case 3: return 90000;
            case 4: return Integer.MAX_VALUE;
            default: return 4500;
        }
    }
    static void setIdle(int c) { idleChoice = c; saveSettings(); }

    // post-2006 content (God Wars Dungeon, Grand Exchange): the server is told on login and whenever this changes
    public static volatile boolean postCutoff;
    private static volatile boolean postCutoffSent;
    static void setPostCutoff(boolean b) { postCutoff = b; postCutoffSent = false; saveSettings(); }

    // classic interface: 2004 tab icons and Game Options / Player controls tabs (see ClassicInterface)
    private static volatile boolean classicUiSent;
    static void setClassicUi(boolean b) {
        ClassicInterface.enabled = b;
        classicUiSent = false;
        ClientScript.aBoolean1690 = true;
        saveSettings();
    }

    /** Game thread, once per logged-in cycle: sends server-side settings as :: commands when needed. */
    public static void serverSettingsTick() {
        if (!inGame()) return;
        if (!postCutoffSent) {
            postCutoffSent = true;
            sendCommand(postCutoff ? "postcutoff on" : "postcutoff off");
        }
        if (!classicUiSent) {
            classicUiSent = true;
            sendCommand(ClassicInterface.enabled ? "classicui on" : "classicui off");
        }
    }

    private static void sendCommand(String text) {
        FrameBuffer.outgoingGameBuffer.putFrame(174);
        JString cmd = Class39_Sub5_Sub9.createJstring(text);
        FrameBuffer.outgoingGameBuffer.putByte(cmd.getLength() + 1);
        FrameBuffer.outgoingGameBuffer.putJstr((byte) 81, cmd);
    }

    static void loadSettings() {
        try (InputStream in = new FileInputStream("./modsettings.properties")) { SET.load(in); } catch (Throwable t) { }
        if (SET.getProperty("wasd") != null) wasd = SET.getProperty("wasd").equals("1");
        lockChatAfterSend = "1".equals(SET.getProperty("chatlock"));
        stretch = "1".equals(SET.getProperty("stretch"));
        panelVisible = !"0".equals(SET.getProperty("panel"));
        muted = "1".equals(SET.getProperty("mute"));
        XpOverlay.enabled = !"0".equals(SET.getProperty("xpdrops"));
        noRoofs = "1".equals(SET.getProperty("noroofs"));
        rememberLogin = !"0".equals(SET.getProperty("remember"));
        try { idleChoice = Math.max(0, Math.min(4, Integer.parseInt(SET.getProperty("idle", "0")))); } catch (Exception e) { idleChoice = 0; }
        postCutoff = "1".equals(SET.getProperty("postcutoff"));
        ClassicInterface.enabled = "1".equals(SET.getProperty("classicui"));
    }

    static void saveSettings() {
        SET.setProperty("wasd", wasd ? "1" : "0");
        SET.setProperty("chatlock", lockChatAfterSend ? "1" : "0");
        SET.setProperty("stretch", stretch ? "1" : "0");
        SET.setProperty("panel", panelVisible ? "1" : "0");
        SET.setProperty("mute", muted ? "1" : "0");
        SET.setProperty("xpdrops", XpOverlay.enabled ? "1" : "0");
        SET.setProperty("noroofs", noRoofs ? "1" : "0");
        SET.setProperty("remember", rememberLogin ? "1" : "0");
        SET.setProperty("idle", Integer.toString(idleChoice));
        SET.setProperty("postcutoff", postCutoff ? "1" : "0");
        SET.setProperty("classicui", ClassicInterface.enabled ? "1" : "0");
        try (OutputStream out = new FileOutputStream("./modsettings.properties")) { SET.store(out, "client settings"); } catch (Throwable t) { }
    }

    /** When set, the scene only draws up to the player's own floor, so roofs and upper floors are never drawn. */
    public static volatile boolean noRoofs;

    /** WASD mode: lock the chatbox again after Enter sends a line. */
    static volatile boolean lockChatAfterSend;
    static void setLockChatAfterSend(boolean b) { lockChatAfterSend = b; saveSettings(); }

    static boolean isWasd() { return wasd; }
    static void setWasd(boolean b) { wasd = b; chatFocus = false; saveSettings(); }
    static void setStretch(boolean b) { stretch = b; saveSettings(); relayout(); refresh(); }
    static void focusGame() { try { if (app != null) app.requestFocusInWindow(); } catch (Throwable t) { } }

    // ------------------------------------------------------------ game state
    static boolean inGame() { return Class31.state == 30; }

    /** True on the title / login screens only (before the world starts loading). The mute button only applies here. */
    static boolean onLoginScreen() { return Class31.state < 25; }

    /** {worldX, worldY} of the local player, or null when not in game. */
    static int[] playerPos() {
        try {
            Player me = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
            if (!inGame() || me == null) return null;
            return new int[]{(me.anInt2301 >> 7) + Class65.anInt1145, (me.anInt2275 >> 7) + JKeyListener.anInt618};
        } catch (Throwable t) { return null; }
    }

    static String playerName() {
        try {
            Player me = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
            return me == null ? "" : DeveloperToolsClient.text(me.aClass3_2521);
        } catch (Throwable t) { return ""; }
    }

    /** Own skill data from the client: {current, base, xp}. */
    static int[][] mySkills() {
        try {
            int[][] r = new int[21][3];
            int[] base = Class39_Sub12.anIntArray1491, cur = Class31.anIntArray555, xp = Class39_Sub14.anIntArray1543;
            if (base == null || cur == null || xp == null) return null;
            for (int i = 0; i < 21; i++) { r[i][0] = cur[i]; r[i][1] = base[i]; r[i][2] = xp[i]; }
            return r;
        } catch (Throwable t) { return null; }
    }

    /** Shows a line in the chat box. */
    static void note(String s) {
        try { JMouseListener.method902(Class66.blankString, Class39_Sub5_Sub9.createJstring(s), false, 0); } catch (Throwable t) { }
    }

    // ----------------------------------------------------------------- music
    /** True while the track that is playing was started on the title/login screen. */
    private static volatile boolean titleTrack;

    /** Called just before the game starts a new MIDI track. */
    public static void trackStarting() { titleTrack = onLoginScreen(); }

    public static boolean midiMuted(javax.sound.midi.MidiMessage msg) {
        try {
            // the title music keeps playing for a moment after the state leaves the login screen: keep it silent
            if (!muted || !(onLoginScreen() || titleTrack)) return false;
            byte[] b = msg.getMessage();
            return b.length >= 3 && (b[0] & 0xF0) == 0x90 && b[2] != 0;
        } catch (Throwable t) { return false; }
    }

    static void setMuted(boolean m) {
        muted = m; saveSettings();
        if (m) silence();
        paintButtons();
    }

    private static void silence() {
        try {
            javax.sound.midi.Receiver r = Class62_Sub1_Sub1.reciever;
            if (r == null) return;
            for (int ch = 0; ch < 16; ch++) {
                javax.sound.midi.ShortMessage m = new javax.sound.midi.ShortMessage();
                m.setMessage(javax.sound.midi.ShortMessage.CONTROL_CHANGE, ch, 120, 0); r.send(m, -1L);
                m = new javax.sound.midi.ShortMessage();
                m.setMessage(javax.sound.midi.ShortMessage.CONTROL_CHANGE, ch, 123, 0); r.send(m, -1L);
            }
        } catch (Throwable t) { }
    }

    // ------------------------------------------------------- window / layout
    private static java.awt.Rectangle savedBounds;
    private static int savedState;

    /** Same window: toggles between normal (title bar) and borderless covering the whole screen. */
    static void setFullscreen(boolean on) {
        try {
            if (frame == null || on == fullscreen) return;
            fullscreen = on;
            ModPanel.closeTools();
            if (on) {
                savedState = frame.getExtendedState();
                savedBounds = frame.getBounds();
                java.awt.Rectangle sb = frame.getGraphicsConfiguration().getBounds();
                frame.dispose();
                frame.setUndecorated(true);
                frame.setExtendedState(Frame.NORMAL);
                frame.setBounds(sb);
                frame.setVisible(true);
                frame.setAlwaysOnTop(true);
            } else {
                frame.setAlwaysOnTop(false);
                frame.dispose();
                frame.setUndecorated(false);
                if (savedBounds != null) frame.setBounds(savedBounds);
                frame.setVisible(true);
                if ((savedState & Frame.MAXIMIZED_BOTH) != 0) frame.setExtendedState(savedState);
            }
            frame.validate();
            javax.swing.Timer tm = new javax.swing.Timer(300, e -> { relayout(); refresh(); focusGame(); });
            tm.setRepeats(false); tm.start();
        } catch (Throwable t) { t.printStackTrace(); }
    }

    static void setPanelVisible(boolean v) {
        panelVisible = v;
        saveSettings();
        if (ModPanel.inst != null) ModPanel.setCollapsed(!v);
        relayout();
        refresh();
        focusGame();
    }

    static void relayout() {
        try { if (shell != null) shell.relayoutCanvas(); } catch (Throwable t) { }
    }

    /** Black-fills the whole canvas and asks the game to redraw everything. */
    static void refresh() {
        try {
            if (app instanceof Canvas_Sub1) {
                Graphics g = ((Canvas_Sub1) app).rawGraphics();
                if (g != null) { g.setColor(Color.BLACK); g.fillRect(0, 0, app.getWidth(), app.getHeight()); g.dispose(); }
            }
            ClientScript.aBoolean1690 = true;
            paintButtons();
        } catch (Throwable t) { }
    }

    // ---------------------------------------------------------------- geometry
    /** {scaleX, scaleY, offsetX, offsetY} for drawing the 765x503 game into a canvas of w x h. */
    static double[] geom(int w, int h) {
        if (w <= 0 || h <= 0) return new double[]{1, 1, 0, 0};
        if (stretch) return new double[]{w / (double) BASE_W, h / (double) BASE_H, 0, 0};
        double s = Math.min(w / (double) BASE_W, h / (double) BASE_H);
        return new double[]{s, s, Math.max(0, (w - BASE_W * s) / 2.0), Math.max(0, (h - BASE_H * s) / 2.0)};
    }

    // ------------------------------------------------------------------- WASD
    private static boolean inputPromptOpen() {
        return Class39_Sub12.aBoolean1489
            || (Class39_Sub5_Sub4_Sub4.anInt2285 >= 1 && Class39_Sub5_Sub4_Sub4.anInt2285 <= 3);
    }

    private static boolean dialogueOpen() {
        return Class39_Sub5_Sub14.anInt1912 != -1 || SubNode.anInt1348 != -1;
    }

    public static String typed() { return DeveloperToolsClient.text(Class66.aClass3_1160); }

    static void clearTyped() {
        Class66.aClass3_1160 = Class66.blankString;
        Class14.aBoolean245 = true;
    }

    /** True while the chat line should show the "Press Enter to begin talking" prompt. */
    public static boolean promptShown() {
        return wasd && inGame() && !chatFocus && !inputPromptOpen() && !dialogueOpen();
    }

    /** Colour of the prompt text (dark red). */
    public static final int PROMPT_COLOUR = 0x800000;

    public static JString promptText() { return Class39_Sub5_Sub9.createJstring(PH); }

    private static int arrowFor(int code) {
        switch (code) {
            case KeyEvent.VK_A: return KeyEvent.VK_LEFT;
            case KeyEvent.VK_D: return KeyEvent.VK_RIGHT;
            case KeyEvent.VK_W: return KeyEvent.VK_UP;
            case KeyEvent.VK_S: return KeyEvent.VK_DOWN;
        }
        return 0;
    }

    private static KeyEvent remap(KeyEvent e, int arrow) {
        return new KeyEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), arrow, KeyEvent.CHAR_UNDEFINED);
    }

    /** Returns the event the game should see, or null to swallow it. */
    public static KeyEvent keyPressed(KeyEvent e) {
        try {
            int code = e.getKeyCode();
            if (code == KeyEvent.VK_F11) { final boolean on = !fullscreen; javax.swing.SwingUtilities.invokeLater(() -> setFullscreen(on)); return null; }
            if (code == KeyEvent.VK_F9) { javax.swing.SwingUtilities.invokeLater(ModPanel::toggleAction); return null; }
            if (!wasd || !inGame() || inputPromptOpen()) { chatFocus = false; return e; }
            if (dialogueOpen()) {
                chatFocus = false;
                int a = arrowFor(code);
                if (a != 0) return remap(e, a);
                return code == KeyEvent.VK_ENTER ? null : e;
            }
            if (chatFocus) {
                if (code == KeyEvent.VK_BACK_SPACE && typed().isEmpty()) { chatFocus = false; Class14.aBoolean245 = true; return null; }
                if (code == KeyEvent.VK_ESCAPE) {
                    boolean empty = typed().isEmpty();
                    clearTyped();
                    if (empty) chatFocus = false;
                    return null;
                }
                if (code == KeyEvent.VK_ENTER && lockChatAfterSend) {
                    // Send the line, then hand the keys back to the WASD camera.
                    boolean empty = typed().isEmpty();
                    chatFocus = false;
                    Class14.aBoolean245 = true;
                    return empty ? null : e;
                }
                return e;                                   // Enter sends the line; chat stays focused
            }
            if (code == KeyEvent.VK_ENTER) { chatFocus = true; clearTyped(); return null; }
            int a = arrowFor(code);
            if (a != 0) return remap(e, a);
            if (code == KeyEvent.VK_BACK_SPACE || code == KeyEvent.VK_DELETE) return null;
            char ch = e.getKeyChar();
            if (ch >= 32 && ch < 127) return null;          // typing is off outside chat focus
            return e;
        } catch (Throwable t) { return e; }
    }

    public static KeyEvent keyReleased(KeyEvent e) {
        try {
            int code = e.getKeyCode();
            if (wasd && inGame()) {
                int a = arrowFor(code);
                if (a != 0) return remap(e, a);
            }
        } catch (Throwable t) { }
        return e;
    }

    /** Handles "::wasd" typed in the chat line. Returns true when the line was consumed. */
    public static boolean command(String line) {
        String lt = line.trim().toLowerCase();
        if (lt.startsWith("::zoom")) {
            try { zoomPct = Math.max(65, Math.min(175, Integer.parseInt(lt.substring(6).trim()))); } catch (Exception e) { zoomPct = 100; }
            clearTyped();
            note("Zoom set to " + zoomPct + "%. The mouse wheel also zooms.");
            return true;
        }
        if (lt.startsWith("::xptest")) {
            XpOverlay.test(lt.length() > 8 ? lt.substring(8).trim() : "");
            clearTyped();
            return true;
        }
        if (!lt.equals("::wasd")) return false;
        boolean on = !wasd;
        setWasd(on);
        clearTyped();
        note(on ? "WASD camera mode is now on." : "WASD camera mode is now off. Type ::wasd again to turn it on.");
        return true;
    }

    // -------------------------------------------------- on-screen buttons
    // 0 = side bar toggle (top right), 1 = mute music (bottom right, login screen only),
    // 2 = widen the world map (middle right, only while the map is open).
    static final int SW = 36, SH = 28, PAD = 4, MW = 72, MH = 58, MRIGHT = 22, MBOTTOM = 20;
    private static int kw(int kind) { return kind != 1 ? SW : MW; }
    private static int kh(int kind) { return kind != 1 ? SH : MH; }
    private static boolean active(int kind) { return kind == 0 || (kind == 1 ? onLoginScreen() : "map".equals(ModPanel.openKey)); }
    private static boolean state(int kind) { return kind == 0 ? (ModPanel.openKey != null || panelVisible) : kind == 2 ? ModPanel.mapWide : muted; }

    private static int[] screenRect(int kind, int w, int h) {
        if (kind == 0) return new int[]{w - SW, PAD, SW, SH};
        if (kind == 2) return new int[]{w - SW, h / 2 - SH / 2, SW, SH};
        int x = w - MW - MRIGHT, y = h - MH - MBOTTOM;
        double[] m = geom(w, h);
        double gr = m[2] + BASE_W * m[0], gb = m[3] + BASE_H * m[1];
        boolean overlap = x < gr && x + MW > m[2] && y < gb && y + MH > m[3];
        boolean inside = x >= m[2] && x + MW <= gr && y >= m[3] && y + MH <= gb;
        if (overlap && !inside) { x = (int) Math.min(x, gr - MW - MRIGHT); y = (int) Math.min(y, gb - MH - MBOTTOM); }
        return new int[]{x, y, MW, MH};
    }

    private static final java.util.Map<String, BufferedImage> GL = new java.util.HashMap<>();
    private static BufferedImage glyph(int kind, boolean st, int w, int h) {
        String key = kind + "" + st + w + "x" + h;
        BufferedImage im = GL.get(key);
        if (im != null) return im;
        im = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (kind == 1) {
            int t = Math.max(1, (int) Math.round(Math.min(w / (double) MW, h / (double) MH) * 2));
            g.setColor(new Color(0x7a6a44)); g.fillRect(0, 0, w, h);
            g.setColor(new Color(0x1c1812)); g.fillRect(t, t, w - 2 * t, h - 2 * t);
            g.setColor(new Color(0x3a3020)); g.fillRect(2 * t, 2 * t, w - 4 * t, h - 4 * t);
        } else {
            g.setColor(new Color(0x3a3020)); g.fillRect(0, 0, w, h);
            g.setColor(new Color(0x7a6a44)); g.drawRect(0, 0, w - 1, h - 1);
        }
        g.setColor(new Color(0xf0e2b8));
        double k = Math.min(w / (double) kw(kind), h / (double) kh(kind));
        g.translate(w / 2.0, h / 2.0); g.scale(k * (kind == 1 ? 2.0 : 1.0), k * (kind == 1 ? 2.0 : 1.0));
        g.setStroke(new BasicStroke(1.8f));
        if (kind != 1) {
            int d = st ? 1 : -1;
            g.fillPolygon(new int[]{-4 * d, -4 * d, 4 * d}, new int[]{-8, 8, 0}, 3);
        } else {
            g.setColor(new Color(0xf0e2b8));
            g.fillPolygon(new int[]{-10, -5, 0, 0, -5, -10}, new int[]{-3, -3, -8, 8, 3, 3}, 6);
            if (st) { g.setColor(new Color(0xe01c1c)); g.setStroke(new BasicStroke(2.4f)); g.drawLine(3, -5, 10, 5); g.drawLine(10, -5, 3, 5); }
            else { g.drawArc(-1, -5, 8, 10, -60, 120); g.drawArc(0, -9, 14, 18, -60, 120); }
        }
        g.dispose();
        GL.put(key, im);
        return im;
    }

    /** Draws the buttons in canvas pixel coordinates; g must be at the canvas' base transform. */
    private static int[] muteGhost;
    private static void drawButtons(Graphics g, int w, int h) {
        for (int kind = 0; kind < 3; kind++) {
            if (!active(kind)) {
                // The login screen's mute button was painted straight onto the canvas; wipe it once when it goes away.
                if (kind == 1 && muteGhost != null) {
                    g.setColor(Color.BLACK); g.fillRect(muteGhost[0], muteGhost[1], muteGhost[2], muteGhost[3]); muteGhost = null;
                }
                continue;
            }
            int[] r = screenRect(kind, w, h);
            if (kind == 1) muteGhost = r;
            g.drawImage(glyph(kind, state(kind), r[2], r[3]), r[0], r[1], null);
        }
    }

    /** Keeps the game's picture from painting over the buttons (they would flicker). Returns the old clip. */
    public static Shape beforeBlit(Graphics g0) {
        try {
            if (!(app instanceof Canvas_Sub1) || !(g0 instanceof Graphics2D)) return null;
            Canvas_Sub1 c = (Canvas_Sub1) app;
            Graphics2D g = (Graphics2D) g0;
            Shape old = g.getClip();
            int w = c.getWidth(), h = c.getHeight();
            double[] m = geom(w, h);
            java.awt.geom.Area area = new java.awt.geom.Area(old != null ? old : new Rectangle(-100000, -100000, 200000, 200000));
            for (int kind = 0; kind < 3; kind++) {
                if (!active(kind)) continue;
                int[] r = screenRect(kind, w, h);
                area.subtract(new java.awt.geom.Area(new java.awt.geom.Rectangle2D.Double(
                    (r[0] - m[2]) / m[0], (r[1] - m[3]) / m[1], r[2] / m[0], r[3] / m[1])));
            }
            g.setClip(area);
            return old == null ? NOCLIP : old;
        } catch (Throwable t) { return null; }
    }
    private static final Shape NOCLIP = new Rectangle(0, 0, 0, 0);
    public static void afterBlitRestore(Graphics g0, Shape old) {
        try { if (old != null) ((Graphics2D) g0).setClip(old == NOCLIP ? null : old); } catch (Throwable t) { }
    }

    /** Called straight after the game draws a picture: puts the buttons back on top of it. */
    public static void afterBlit(Graphics g0, int bx, int by, int bw, int bh) {
        try {
        } catch (Throwable t) { }
        try {
            if (!(app instanceof Canvas_Sub1) || !(g0 instanceof Graphics2D)) return;
            Canvas_Sub1 c = (Canvas_Sub1) app;
            double[] m = geom(c.getWidth(), c.getHeight());
            Graphics2D g = (Graphics2D) g0.create();
            g.scale(1.0 / m[0], 1.0 / m[1]);
            g.translate(-m[2], -m[3]);
            g.setClip(null);
            drawButtons(g, c.getWidth(), c.getHeight());
            g.dispose();
        } catch (Throwable t) { }
    }

    static void paintButtons() {
        try {
            if (!(app instanceof Canvas_Sub1)) return;
            Canvas_Sub1 c = (Canvas_Sub1) app;
            Graphics g = c.rawGraphics();
            if (g == null) return;
            drawButtons(g, c.getWidth(), c.getHeight());
            g.dispose();
        } catch (Throwable t) { }
    }

    private static void overlayTick() { wipeMargins(); paintButtons(); }

    /** In game (not stretched) the game never paints outside its own area, so stale login-screen art there is wiped. */
    private static void wipeMargins() {
        try {
            if (stretch || Class31.state < 25 || !(app instanceof Canvas_Sub1)) return;
            Canvas_Sub1 c = (Canvas_Sub1) app;
            int w = c.getWidth(), h = c.getHeight();
            if (w <= 0 || h <= 0) return;
            double[] m = geom(w, h);
            Graphics g = c.rawGraphics();
            if (g == null) return;
            java.awt.geom.Area a = new java.awt.geom.Area(new Rectangle(0, 0, w, h));
            a.subtract(new java.awt.geom.Area(new Rectangle((int) Math.floor(m[2]), (int) Math.floor(m[3]),
                (int) Math.ceil(BASE_W * m[0]) + 1, (int) Math.ceil(BASE_H * m[1]) + 1)));
            for (int kind = 0; kind < 3; kind++) {
                if (!active(kind)) continue;
                int[] r = screenRect(kind, w, h);
                a.subtract(new java.awt.geom.Area(new Rectangle(r[0], r[1], r[2], r[3])));
            }
            g.setClip(a);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);
            g.dispose();
        } catch (Throwable t) { }
    }

    /** Clicks on the on-screen buttons never reach the game. */
    public static boolean mouse(MouseEvent e, Component c) {
        try {
            for (int kind = 0; kind < 3; kind++) {
                if (!active(kind)) continue;
                int[] r = screenRect(kind, c.getWidth(), c.getHeight());
                if (e.getX() >= r[0] && e.getX() < r[0] + r[2] && e.getY() >= r[1] && e.getY() < r[1] + r[3]) {
                    final int k = kind;
                    if (e.getID() == MouseEvent.MOUSE_PRESSED)
                        javax.swing.SwingUtilities.invokeLater(() -> { if (k == 0) ModPanel.toggleAction(); else if (k == 2) ModPanel.toggleMapWide(); else setMuted(!muted); });
                    return true;
                }
            }
        } catch (Throwable t) { }
        return false;
    }
}

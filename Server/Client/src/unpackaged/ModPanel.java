package unpackaged;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

/** Docked slim icon bar on the right; each icon opens a tool window. */
final class ModPanel extends JPanel {
    static ModPanel inst;
    static final int WIDTH = 53, DRAWER = 340;
    static final Color BG = new Color(0x1c1812), FG = new Color(0xe8dcb8), ACC = new Color(0xc8a050);
    static final Color TAN = new Color(0xd8cba0), INK = new Color(0x2b2418), EDGE = new Color(0x5a4a2a);

    static void toggleAction() {
        if (openKey != null) closeTools(); else ModHook.setPanelVisible(!ModHook.panelVisible);
        ModHook.focusGame();
    }

    static void install(Frame f) {
        if (inst != null) return;
        try {
            inst = new ModPanel();
            f.add(inst);
            setCollapsed(!ModHook.panelVisible);
            ModHook.relayout();
        } catch (Throwable t) { t.printStackTrace(); }
    }

    /** Width the docked panel currently needs (0 when hidden). */
    static int dockWidth() {
        if (inst == null || !inst.isVisible()) return 0;
        return inst.getPreferredSize().width;
    }

    // ---------------------------------------------------------------- side drawer (same window)
    private static final Map<String, JComponent> cache = new HashMap<>();
    static String openKey;
    private static JPanel bar;
    static final int TOG = 0;

    static void setCollapsed(boolean c) {
        if (inst == null) return;
        if (c) closeTools();
        bar.setVisible(!c); inst.setVisible(!c);
        inst.setPreferredSize(new Dimension(TOG + (c ? 0 : WIDTH), 100));
        ModHook.relayout();
        ModHook.refresh();
    }
    private static JPanel drawer;
    static boolean mapWide = false;

    private static int instWidth(String key) {
        int base = TOG + WIDTH + ("map".equals(key) ? 560 : DRAWER);
        if (!"map".equals(key) || !mapWide || inst == null || inst.getParent() == null) return base;
        int total = inst.getParent().getWidth();
        return Math.max(base, Math.min((int) (total * 0.68), total - 400));
    }

    static void toggleMapWide() {
        try {
            if (!"map".equals(openKey)) return;
            mapWide = !mapWide;
            inst.setPreferredSize(new Dimension(instWidth("map"), 100));
            ModHook.relayout();
            drawer.revalidate(); drawer.repaint();
            ModHook.refresh();
            ModHook.focusGame();
        } catch (Throwable t) { t.printStackTrace(); }
    }

    static void closeTools() {
        openKey = null; mapWide = false;
        if (drawer != null && inst != null) {
            drawer.removeAll(); drawer.setVisible(false);
            inst.setPreferredSize(new Dimension(TOG + (bar.isVisible() ? WIDTH : 0), 100));
            ModHook.relayout();
            ModHook.refresh();
        }
    }

    static void skin(Component c) {
        if (c instanceof JPanel || c instanceof JCheckBox || c instanceof JLabel || c instanceof JScrollPane) {
            if (c instanceof JComponent && !(c instanceof JScrollPane)) { ((JComponent) c).setOpaque(true); }
            c.setBackground(TAN);
            if (c instanceof JLabel || c instanceof JCheckBox) c.setForeground(INK);
        }
        if (c instanceof JButton) { c.setBackground(new Color(0xb9a874)); c.setForeground(INK); ((JButton) c).setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EDGE), BorderFactory.createEmptyBorder(2, 8, 2, 8))); }
        if (c instanceof JTable) { JTable jt = (JTable) c; jt.getTableHeader().setBackground(new Color(0xb9a874)); jt.setBackground(TAN); jt.setForeground(INK); jt.setGridColor(new Color(0xb9a874)); jt.setOpaque(true); if (jt.getParent() instanceof JViewport) jt.getParent().setBackground(TAN); }
        if (c instanceof Container) for (Component k : ((Container) c).getComponents()) skin(k);
    }

    static void openTool(String key, String title, int w, int h, java.util.function.Supplier<JComponent> make) {
        try {
            if (key.equals(openKey)) { closeTools(); ModHook.focusGame(); return; }
            JComponent body = cache.get(key);
            if (body == null) { body = make.get(); skin(body); cache.put(key, body); }
            drawer.removeAll();
            JPanel head = new JPanel(new BorderLayout());
            head.setBackground(new Color(0x2b2418));
            head.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 6));
            JLabel t = new JLabel(title); t.setForeground(FG); t.setFont(t.getFont().deriveFont(Font.BOLD, 13f));
            JLabel x = new JLabel("X"); x.setForeground(FG); x.setFont(x.getFont().deriveFont(Font.BOLD, 13f));
            x.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            x.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { closeTools(); ModHook.focusGame(); } });
            head.add(t, BorderLayout.WEST); head.add(x, BorderLayout.EAST);
            drawer.add(head, BorderLayout.NORTH);
            drawer.add(body, BorderLayout.CENTER);
            drawer.setVisible(true);
            openKey = key;
            if (!"map".equals(key)) mapWide = false;
            inst.setPreferredSize(new Dimension(instWidth(key), 100));
            ModHook.relayout();
            drawer.revalidate(); drawer.repaint();
            ModHook.refresh();
        } catch (Throwable t) { t.printStackTrace(); }
    }

    // ---------------------------------------------------------------- icon bar
    private interface Painter { void paint(Graphics2D g); }

    private static final class Icon extends JComponent {
        final Painter painter; final String tip; final Runnable action; boolean hover;
        Icon(String tip, Painter p, Runnable a) {
            this.painter = p; this.tip = tip; this.action = a;
            setToolTipText(tip);
            setPreferredSize(new Dimension(WIDTH, 40));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                public void mouseClicked(MouseEvent e) { action.run(); ModHook.focusGame(); }
            });
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(0.5f);
        }
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hover) { g.setColor(new Color(0x3a3020)); g.fillRect(2, 2, getWidth() - 4, getHeight() - 4); }
            g.translate((getWidth() - 24) / 2, (getHeight() - 24) / 2); g.scale(1.2, 1.2);
            g.setColor(hover ? new Color(0xfff0c0) : FG);
            g.setStroke(new BasicStroke(1.6f));
            painter.paint(g);
            g.dispose();
        }
    }

    private static void line(Graphics2D g, int a, int b, int c, int d) { g.drawLine(a, b, c, d); }

    private static final Painter TROPHY = g -> { g.drawPolyline(new int[]{4, 4, 16, 16}, new int[]{2, 9, 9, 2}, 4); g.drawLine(4, 2, 16, 2); g.drawLine(10, 9, 10, 15); g.drawLine(5, 18, 15, 18); g.drawLine(10, 15, 10, 18); g.drawArc(0, 3, 6, 5, 90, 180); g.drawArc(14, 3, 6, 5, 90, -180); };
    private static final Painter DOC = g -> { g.drawRect(3, 1, 13, 18); g.drawLine(6, 6, 13, 6); g.drawLine(6, 10, 13, 10); g.drawLine(6, 14, 11, 14); };
    private static final Painter BARS = g -> { g.drawLine(1, 19, 19, 19); g.fillRect(3, 11, 3, 8); g.fillRect(8, 6, 3, 13); g.fillRect(13, 1, 3, 18); };
    private static final Painter CALC = g -> { g.drawRect(3, 1, 13, 18); g.drawRect(6, 4, 7, 3); for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++) g.fillRect(6 + c * 3, 10 + r * 3, 2, 2); };
    private static final Painter MAP = g -> { g.drawPolyline(new int[]{1, 7, 13, 19, 19, 13, 7, 1, 1}, new int[]{4, 2, 4, 2, 16, 18, 16, 18, 4}, 9); g.drawLine(7, 2, 7, 16); g.drawLine(13, 4, 13, 18); };
    private static final Painter INFO = g -> { g.drawOval(1, 1, 17, 17); g.fillRect(9, 8, 2, 7); g.fillRect(9, 4, 2, 2); };
    private static final Painter CAMERA = g -> { g.drawRect(1, 5, 18, 12); g.drawOval(6, 7, 8, 8); g.drawLine(6, 5, 8, 2); g.drawLine(8, 2, 12, 2); g.drawLine(12, 2, 14, 5); };
    private static final Painter PIN = g -> { g.drawLine(10, 10, 10, 19); g.drawLine(5, 10, 15, 10); g.drawLine(6, 10, 6, 3); g.drawLine(14, 10, 14, 3); g.drawLine(5, 3, 15, 3); };
    private static final Painter FULL = g -> { g.drawPolyline(new int[]{1, 1, 6}, new int[]{6, 1, 1}, 3); g.drawPolyline(new int[]{14, 19, 19}, new int[]{1, 1, 6}, 3); g.drawPolyline(new int[]{1, 1, 6}, new int[]{14, 19, 19}, 3); g.drawPolyline(new int[]{14, 19, 19}, new int[]{19, 19, 14}, 3); };
    private static final Painter STRETCH = g -> { g.drawRect(1, 3, 17, 13); g.drawLine(5, 14, 14, 5); g.drawLine(9, 5, 14, 5); g.drawLine(14, 5, 14, 10); };
    private static final Painter ARROW_R = g -> { g.drawPolyline(new int[]{6, 14, 6}, new int[]{3, 10, 17}, 3); };
    private static final Painter SPEAKER = g -> { g.drawPolyline(new int[]{2, 6, 11, 11, 6, 2, 2}, new int[]{7, 7, 2, 18, 13, 13, 7}, 7); if (ModHook.muted) { g.drawLine(14, 6, 19, 14); g.drawLine(19, 6, 14, 14); } else { g.drawArc(10, 5, 6, 10, -60, 120); g.drawArc(11, 2, 9, 16, -60, 120); } };
    private static final Painter GEAR = g -> { g.drawOval(5, 5, 10, 10); g.drawOval(8, 8, 4, 4); for (int i = 0; i < 8; i++) { double a = i * Math.PI / 4; g.drawLine((int) (10 + 6 * Math.cos(a)), (int) (10 + 6 * Math.sin(a)), (int) (10 + 9 * Math.cos(a)), (int) (10 + 9 * Math.sin(a))); } };

    private static Component gap(int h) { JComponent c = (JComponent) Box.createRigidArea(new Dimension(WIDTH, h)); c.setAlignmentX(0.5f); return c; }

    private ModPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setPreferredSize(new Dimension(WIDTH, 100));
        JPanel top = new JPanel(new GridLayout(0, 1)); top.setBackground(BG);
        top.add(new Icon("Hiscores", TROPHY, () -> openTool("hs", "Hiscores", 380, 480, HiscoreView::new)));
        top.add(new Icon("Wiki", DOC, () -> openTool("wiki", "Wiki", 300, 160, WikiView::new)));
        top.add(new Icon("My stats", BARS, () -> openTool("stats", "My stats", 300, 520, StatsView::new)));
        top.add(new Icon("Level calculator", CALC, () -> openTool("calc", "Level calculator", 300, 240, CalcView::new)));
        top.add(new Icon("World map", MAP, () -> openTool("map", "World map", 560, 480, MapView::new)));
        top.add(new Icon("Help", INFO, () -> openTool("help", "Help", 320, 200, HelpView::new)));
        JPanel bot = new JPanel(new GridLayout(0, 1)); bot.setBackground(BG);
        bot.add(new Icon("Stretch to fill window", STRETCH, () -> ModHook.setStretch(!ModHook.stretch)));
        bot.add(new Icon("Screenshot", CAMERA, ModPanel::screenshot));
        bot.add(new Icon("Keep window on top", PIN, () -> { Window w = ModHook.frame; w.setAlwaysOnTop(!w.isAlwaysOnTop()); }));
        bot.add(new Icon("Fullscreen (F11)", FULL, () -> ModHook.setFullscreen(!ModHook.fullscreen)));
        bot.add(new Icon("Settings", GEAR, () -> openTool("opt", "Settings", 340, 280, OptionsView::new)));
        bar = new JPanel(new BorderLayout()); bar.setBackground(BG);
        bar.setPreferredSize(new Dimension(WIDTH, 100));
        bar.add(top, BorderLayout.NORTH); bar.add(bot, BorderLayout.SOUTH);
        drawer = new JPanel(new BorderLayout());
        drawer.setBackground(TAN);
        drawer.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2, new Color(0x4a3c22)));
        drawer.setVisible(false);
        JPanel right = new JPanel(new BorderLayout()); right.setBackground(BG);
        right.add(bar, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
        add(drawer, BorderLayout.CENTER);
    }

    static void screenshot() {
        try {
            Component app = ModHook.app;
            Point p = app.getLocationOnScreen();
            Robot r = new Robot();
            BufferedImage img = r.createScreenCapture(new Rectangle(p.x, p.y, app.getWidth(), app.getHeight()));
            File dir = new File("./screenshots"); dir.mkdirs();
            File f = new File(dir, "shot-" + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".png");
            javax.imageio.ImageIO.write(img, "png", f);
            ModHook.note("Screenshot saved to screenshots/" + f.getName());
        } catch (Throwable t) { t.printStackTrace(); }
    }

    // ---------------------------------------------------------------- stats / calc / help
    static long xpFor(int lvl) { double t = 0; for (int i = 1; i < lvl; i++) t += Math.floor(i + 300 * Math.pow(2, i / 7.0)); return (long) Math.floor(t / 4); }

    static final class StatsView extends JPanel {
        private final DefaultTableModel m = new DefaultTableModel(new Object[]{"Skill", "Level", "XP"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        StatsView() {
            super(new BorderLayout());
            JTable t = new JTable(m); t.setFocusable(false);
            add(new JScrollPane(t), BorderLayout.CENTER);
            Runnable up = () -> {
                int[][] my = ModHook.mySkills();
                m.setRowCount(0);
                if (my == null) { m.addRow(new Object[]{"(log in first)", "", ""}); return; }
                int tl = 0; long tx = 0;
                for (int i = 0; i < 21; i++) { m.addRow(new Object[]{SKILLS[i], my[i][1], my[i][2]}); tl += my[i][1]; tx += my[i][2]; }
                m.addRow(new Object[]{"Total", tl, tx});
            };
            up.run();
            new javax.swing.Timer(1000, e -> { if (isShowing()) up.run(); }).start();
        }
    }

    static final class CalcView extends JPanel {
        CalcView() {
            super(new GridLayout(0, 1, 4, 4));
            setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            JComboBox<String> sk = new JComboBox<>(SKILLS); sk.setFocusable(false);
            JSpinner tgt = new JSpinner(new SpinnerNumberModel(99, 2, 99, 1));
            JLabel cur = new JLabel(" "), need = new JLabel(" ");
            Runnable up = () -> {
                int[][] my = ModHook.mySkills(); int i = sk.getSelectedIndex(); int t = (Integer) tgt.getValue();
                if (my == null) { cur.setText("Log in first to see your current xp."); need.setText("XP for level " + t + ": " + xpFor(t)); return; }
                cur.setText("Current: level " + my[i][1] + ", " + my[i][2] + " xp");
                need.setText("XP for level " + t + ": " + xpFor(t) + "   (remaining: " + Math.max(0, xpFor(t) - my[i][2]) + ")");
            };
            sk.addActionListener(e -> up.run()); tgt.addChangeListener(e -> up.run());
            add(new JLabel("Skill")); add(sk); add(new JLabel("Target level")); add(tgt); add(cur); add(need);
            up.run();
        }
    }

    static final class HelpView extends JPanel {
        private static final String[][] CMDS = {
            {"Items", "", ""},
            {"", "::sitem ID [N]", "spawn an item (N of it)"}, {"", "::empty", "empty your inventory"}, {"", "::money", "1,000,000 coins"},
            {"", "::food / ::nfood", "stacks of food"}, {"", "::nbones", "bones"}, {"", "::runes", "10,000 of each rune"},
            {"", "::arrows", "10,000 of each arrow"}, {"", "::bank", "open your bank"},
            {"Stats", "", ""},
            {"", "::master", "all stats to maximum"}, {"", "::setlevel S L", "set skill S to level L"}, {"", "::god", "toggle god mode"},
            {"", "::run", "refill run energy"}, {"", "::irun", "toggle infinite run"}, {"", "::statsme", "show your account stats"},
            {"Teleport & magic", "", ""},
            {"", "::tele X Y [H]", "teleport to coordinates"}, {"", "::home", "go home, normal spellbook"},
            {"", "::modern", "normal spellbook"}, {"", "::ancient", "ancient spellbook"},
            {"Quests", "", ""},
            {"", "::quests", "complete all quests"}, {"", "::rquests", "reset all quests"}, {"", "::lquest NAME", "search quests, shows IDs"},
            {"", "::cquest ID", "complete one quest"}, {"", "::rquest ID", "reset one quest"},
            {"Other", "", ""},
            {"", "::skiptask", "skip current progressive task"}, {"", "::bot / ::sbot", "progressive bot controls"},
            {"", "::players", "players online"}, {"", "::ms", "toggle movement system"}, {"", "::pw OLD NEW", "change password"},
            {"", "::wasd", "toggle WASD camera mode"},
        };
        private static List<String[]> load(String res) {
            List<String[]> out = new ArrayList<>();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(ModPanel.class.getResourceAsStream(res), StandardCharsets.UTF_8))) {
                String l; r.readLine();
                while ((l = r.readLine()) != null) { int t = l.indexOf('\t'); if (t > 0) out.add(new String[]{l.substring(0, t), l.substring(t + 1)}); }
            } catch (Throwable t) { }
            return out;
        }
        private final JTextField search = new JTextField();
        private final JComboBox<String> mode = new JComboBox<>(new String[]{"Commands", "Items", "NPCs"});
        private final JEditorPane html = new JEditorPane("text/html", "");
        private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Name"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        private final JTable table = new JTable(model);
        private final CardLayout cards = new CardLayout();
        private final JPanel center = new JPanel(cards);
        private final JLabel status = new JLabel(" ");
        private List<String[]> items, npcs;

        HelpView() {
            super(new BorderLayout());
            JPanel top = new JPanel(new BorderLayout(4, 0));
            top.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            mode.setFocusable(false);
            top.add(mode, BorderLayout.WEST);
            top.add(search, BorderLayout.CENTER);
            add(top, BorderLayout.NORTH);
            html.setEditable(false); html.setFocusable(false); html.setBackground(TAN);
            html.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            JScrollPane hs = new JScrollPane(html); hs.setBorder(null); hs.getViewport().setBackground(TAN);
            table.setFocusable(false);
            table.getColumnModel().getColumn(0).setPreferredWidth(50);
            table.getColumnModel().getColumn(0).setMaxWidth(80);
            JScrollPane ts = new JScrollPane(table); ts.setBorder(null);
            center.add(hs, "c"); center.add(ts, "t");
            add(center, BorderLayout.CENTER);
            status.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            add(status, BorderLayout.SOUTH);
            mode.addActionListener(e -> refresh());
            search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            });
            refresh();
        }

        private void refresh() {
            String q = search.getText().trim().toLowerCase();
            int m = mode.getSelectedIndex();
            if (m == 0) { cards.show(center, "c"); html.setText(commands(q)); html.setCaretPosition(0); status.setText(" "); return; }
            cards.show(center, "t");
            if (m == 1 && items == null) items = load("/assets/mod/ids/items.txt");
            if (m == 2 && npcs == null) npcs = load("/assets/mod/ids/npcs.txt");
            List<String[]> src = m == 1 ? items : npcs;
            model.setRowCount(0);
            int n = 0, total = 0;
            for (String[] r : src) {
                if (q.isEmpty() || r[0].equals(q) || r[1].toLowerCase().contains(q) || r[0].startsWith(q)) {
                    total++;
                    if (n < 600) { model.addRow(r); n++; }
                }
            }
            status.setText(total + " match" + (total == 1 ? "" : "es") + (total > n ? " (showing first " + n + ", type more to narrow)" : ""));
        }

        private String commands(String q) {
            StringBuilder h = new StringBuilder("<html><body style='font-family:SansSerif;font-size:11px;color:#2b2418;background-color:#d8cba0'>");
            if (q.isEmpty()) h.append("<b>Keys</b><br>F11 fullscreen, F9 show/hide this bar<br>WASD turn the camera, Enter opens chat,<br>Esc / Backspace on an empty line goes back to WASD<br>Mouse wheel zooms the game view<br><br><b>Commands</b> (type in the chat box)<br>");
            h.append("<table cellpadding=1>");
            String sec = ""; boolean secShown = false; int shown = 0;
            for (String[] r : CMDS) {
                if (!r[0].isEmpty()) { sec = r[0]; secShown = false; continue; }
                if (!q.isEmpty() && !(r[1].toLowerCase().contains(q) || r[2].toLowerCase().contains(q) || sec.toLowerCase().contains(q))) continue;
                if (!secShown) { h.append("<tr><td colspan=2><br><u><b>" + sec.replace("&", "&amp;") + "</b></u></td></tr>"); secShown = true; }
                h.append("<tr><td valign=top><b>" + r[1] + "</b></td><td>" + r[2] + "</td></tr>"); shown++;
            }
            h.append("</table>");
            if (q.isEmpty()) h.append("<br><b>Skill numbers (::setlevel)</b><br>0 Attack, 1 Defence, 2 Strength, 3 Hitpoints, 4 Ranged, 5 Prayer, 6 Magic, 7 Cooking, 8 Woodcutting, 9 Fletching, 10 Fishing, 11 Firemaking, 12 Crafting, 13 Smithing, 14 Mining, 15 Herblore, 16 Agility, 17 Thieving, 18 Slayer, 19 Farming, 20 Runecrafting<br><br>Screenshots are saved in the screenshots folder next to the client.");
            else if (shown == 0) h.append("<br>No commands match.");
            return h.append("</body></html>").toString();
        }
    }

    static JButton button(String t, ActionListener a) {
        JButton b = new JButton(t);
        b.setFocusable(false);
        b.addActionListener(e -> { a.actionPerformed(e); ModHook.focusGame(); });
        return b;
    }

    // ------------------------------------------------------------------- map
    /** The live 2006 world map served by the local server (region sprites + every online player). */
    static final class MapView extends JPanel {
        private static final int PX = 2, X0 = 2048, ZTOP = 4288, MW = 1984 * PX, MH = 1792 * PX, R = 64 * PX;
        private static final Map<Integer, BufferedImage> CACHE = new LinkedHashMap<Integer, BufferedImage>(128, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<Integer, BufferedImage> e) { return size() > 200; }
        };
        private static final BufferedImage NONE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        private static final java.util.concurrent.ExecutorService LOADER = java.util.concurrent.Executors.newFixedThreadPool(3, r -> { Thread t = new Thread(r); t.setDaemon(true); return t; });
        private static final java.util.Set<Integer> LOADING = new java.util.HashSet<>();

        private double cx = (3222 - X0 + 0.5) * PX, cz = (ZTOP - 3218 - 0.5) * PX, scale = 1.0;
        private boolean follow = true;
        private int[] me;
        private volatile List<Object[]> others = new ArrayList<>();
        private volatile boolean serverOk = true;
        private Point drag; private double dcx, dcz;
        private final JLabel info = new JLabel(" ");
        private final JLabel zoomLbl = new JLabel("100%");
        private final JToggleButton followBtn = new JToggleButton("Follow me", true);
        private final JCheckBox names = new JCheckBox("Show names", true);
        private final JComponent view;

        private volatile BufferedImage hd;
        private volatile int hdState;

        private void loadHd() {
            hdState = 1;
            Thread t = new Thread(() -> {
                try {
                    HttpURLConnection c = (HttpURLConnection) new URL("http://" + host() + ":43597/map/image").openConnection();
                    c.setConnectTimeout(1500); c.setReadTimeout(30000);
                    if (c.getResponseCode() == 200) {
                        try (InputStream in = new BufferedInputStream(c.getInputStream())) {
                            BufferedImage im = javax.imageio.ImageIO.read(in);
                            if (im != null) { hd = im; hdState = 2; }
                        }
                    }
                } catch (Throwable ex) { }
                if (hd == null) hdState = 3;
                SwingUtilities.invokeLater(() -> { if (view != null) view.repaint(); });
            }, "mod-map-image");
            t.setDaemon(true); t.start();
        }

        private static String host() { return System.getProperty("hiscores.host", "127.0.0.1"); }

        private BufferedImage region(int rid) {
            synchronized (CACHE) {
                BufferedImage im = CACHE.get(rid);
                if (im != null) return im == NONE ? null : im;
                if (LOADING.contains(rid)) return null;
                LOADING.add(rid);
            }
            LOADER.execute(() -> {
                BufferedImage im = NONE;
                boolean retry = false;
                try {
                    HttpURLConnection c = (HttpURLConnection) new URL("http://" + host() + ":43597/map/sprite?id=" + rid).openConnection();
                    c.setConnectTimeout(1500); c.setReadTimeout(5000);
                    if (c.getResponseCode() == 200) {
                        try (InputStream in = c.getInputStream()) {
                            BufferedImage raw = javax.imageio.ImageIO.read(in);
                            if (raw != null) {
                                BufferedImage out = new BufferedImage(raw.getWidth(), raw.getHeight(), BufferedImage.TYPE_INT_ARGB);
                                for (int y = 0; y < raw.getHeight(); y++) for (int x = 0; x < raw.getWidth(); x++) {
                                    int rgb = raw.getRGB(x, y) & 0xffffff;
                                    out.setRGB(x, y, rgb == 0xff00ff ? 0 : (0xff000000 | rgb));
                                }
                                im = out;
                            }
                        }
                    }
                } catch (Throwable t) { retry = true; }
                synchronized (CACHE) { if (retry) CACHE.remove(rid); else CACHE.put(rid, im); LOADING.remove(rid); }
                SwingUtilities.invokeLater(() -> { if (view != null) view.repaint(); });
            });
            return null;
        }

        MapView() {
            super(new BorderLayout());
            view = new JComponent() {
                protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0;
                    int w = getWidth(), h = getHeight();
                    g.setColor(Color.BLACK); g.fillRect(0, 0, w, h);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, scale >= 1.0 ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    double x1 = cx - w / 2.0 / scale, z1 = cz - h / 2.0 / scale, x2 = cx + w / 2.0 / scale, z2 = cz + h / 2.0 / scale;
                    if (hdState == 0) loadHd();
                    BufferedImage big = hd;
                    if (big != null) {
                        double sx1 = Math.max(0, x1), sy1 = Math.max(0, z1), sx2 = Math.min(big.getWidth(), x2), sy2 = Math.min(big.getHeight(), z2);
                        if (sx2 > sx1 && sy2 > sy1) {
                            g.drawImage(big, (int) Math.round((sx1 - x1) * scale), (int) Math.round((sy1 - z1) * scale), (int) Math.round((sx2 - x1) * scale), (int) Math.round((sy2 - z1) * scale),
                                (int) sx1, (int) sy1, (int) Math.ceil(sx2), (int) Math.ceil(sy2), null);
                        }
                    } else if (hdState == 1) {
                        g.setColor(Color.WHITE); g.drawString("Loading map...", 10, 40);
                    }
                    if (big == null && hdState == 3) {
                    int c0 = Math.max(0, (int) Math.floor(x1 / R)), c1 = Math.min(MW / R - 1, (int) Math.floor(x2 / R));
                    int r0 = Math.max(0, (int) Math.floor(z1 / R)), r1 = Math.min(MH / R - 1, (int) Math.floor(z2 / R));
                    for (int r = r0; r <= r1; r++) for (int c = c0; c <= c1; c++) {
                        int rx = X0 / 64 + c, ry = ZTOP / 64 - 1 - r;
                        BufferedImage im = region((rx << 8) | ry);
                        if (im == null) continue;
                        int dx1 = (int) Math.floor((c * R - x1) * scale), dy1 = (int) Math.floor((r * R - z1) * scale);
                        int dx2 = (int) Math.ceil(((c + 1) * R - x1) * scale), dy2 = (int) Math.ceil(((r + 1) * R - z1) * scale);
                        g.drawImage(im, dx1, dy1, dx2, dy2, 0, 0, im.getWidth(), im.getHeight(), null);
                    }
                    }
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Font font = new Font("SansSerif", Font.BOLD, 12);
                    g.setFont(font);
                    String mine = ModHook.playerName();
                    for (Object[] o : others) {
                        if ((Integer) o[3] != 0) continue;
                        String nm = (String) o[0];
                        if (mine != null && nm.equalsIgnoreCase(mine)) continue;
                        marker(g, (Integer) o[1], (Integer) o[2], ((Integer) o[4]) == 1, nm, x1, z1, false);
                    }
                    if (me != null) marker(g, me[0], me[1], false, mine == null ? "" : mine, x1, z1, true);
                    if (!serverOk) {
                        g.setColor(Color.WHITE);
                        g.drawString("Live map unavailable - is the server running?", 10, 20);
                    }
                }

                private void marker(Graphics2D g, int wx, int wy, boolean bot, String name, double x1, double z1, boolean self) {
                    if (wx < X0 || wx >= X0 + MW / PX || wy < ZTOP - MH / PX || wy >= ZTOP) return;
                    int px = (int) Math.round(((wx - X0 + 0.5) * PX - x1) * scale), py = (int) Math.round(((ZTOP - wy - 0.5) * PX - z1) * scale);
                    if (px < -30 || py < -30 || px > getWidth() + 30 || py > getHeight() + 30) return;
                    int rad = self ? 6 : (bot ? 4 : 5);
                    g.setColor(new Color(0, 0, 0, 190)); g.fillOval(px - rad - 1, py - rad - 1, rad * 2 + 2, rad * 2 + 2);
                    g.setColor(self ? new Color(255, 60, 60) : (bot ? new Color(255, 170, 45) : new Color(70, 235, 110)));
                    g.fillOval(px - rad, py - rad, rad * 2, rad * 2);
                    g.setColor(Color.WHITE); g.drawOval(px - rad, py - rad, rad * 2, rad * 2);
                    if (!names.isSelected() || name.isEmpty()) return;
                    FontMetrics fm = g.getFontMetrics();
                    int tx = px - fm.stringWidth(name) / 2, ty = py - rad - 4;
                    g.setColor(Color.BLACK);
                    g.drawString(name, tx - 1, ty); g.drawString(name, tx + 1, ty); g.drawString(name, tx, ty - 1); g.drawString(name, tx, ty + 1);
                    g.setColor(Color.WHITE); g.drawString(name, tx, ty);
                }
            };
            view.setPreferredSize(new Dimension(400, 400));
            MouseAdapter ma = new MouseAdapter() {
                public void mousePressed(MouseEvent e) { drag = e.getPoint(); dcx = cx; dcz = cz; }
                public void mouseReleased(MouseEvent e) { drag = null; }
                public void mouseDragged(MouseEvent e) {
                    if (drag == null) return;
                    cx = dcx - (e.getX() - drag.x) / scale; cz = dcz - (e.getY() - drag.y) / scale;
                    setFollow(false); clamp(); hover(e); view.repaint();
                }
                public void mouseMoved(MouseEvent e) { hover(e); }
                public void mouseWheelMoved(MouseWheelEvent e) { zoomBy(e.getWheelRotation() < 0 ? 1.25 : 0.8); }
            };
            view.addMouseListener(ma); view.addMouseMotionListener(ma); view.addMouseWheelListener(ma);
            JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            names.setFocusable(false);
            names.addActionListener(e -> { view.repaint(); ModHook.focusGame(); });
            bar.add(names);
            bar.add(button("-", e -> zoomBy(0.8)));
            bar.add(zoomLbl);
            bar.add(button("+", e -> zoomBy(1.25)));
            followBtn.setFocusable(false);
            followBtn.addActionListener(e -> { follow = followBtn.isSelected(); if (follow) update(); ModHook.focusGame(); });
            bar.add(followBtn);
            bar.add(info);
            add(view, BorderLayout.CENTER); add(bar, BorderLayout.SOUTH);
            new javax.swing.Timer(500, e -> update()).start();
            Thread poll = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        if (view.isShowing()) fetchPlayers();
                    } catch (InterruptedException e) { return; }
                }
            }, "mod-map-players");
            poll.setDaemon(true); poll.start();
            update();
        }

        private void fetchPlayers() {
            List<Object[]> out = new ArrayList<>();
            try {
                HttpURLConnection c = (HttpURLConnection) new URL("http://" + host() + ":43597/players").openConnection();
                c.setConnectTimeout(1500); c.setReadTimeout(3000);
                try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        String[] p = line.split("\t");
                        if (p.length < 5) continue;
                        out.add(new Object[]{p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]), Integer.parseInt(p[4])});
                    }
                }
                serverOk = true;
            } catch (Throwable t) { serverOk = false; }
            others = out;
            SwingUtilities.invokeLater(() -> view.repaint());
        }

        private void setFollow(boolean f) { follow = f; followBtn.setSelected(f); }

        private void zoomBy(double f) {
            scale = Math.max(0.25, Math.min(3.0, scale * f));
            zoomLbl.setText(Math.round(scale * 100) + "%");
            clamp(); view.repaint();
        }

        private void clamp() {
            cx = Math.max(0, Math.min(MW, cx)); cz = Math.max(0, Math.min(MH, cz));
        }

        private void hover(MouseEvent e) {
            double mx = cx + (e.getX() - view.getWidth() / 2.0) / scale, mz = cz + (e.getY() - view.getHeight() / 2.0) / scale;
            info.setText((int) Math.floor(mx / PX) + X0 + ", " + (ZTOP - 1 - (int) Math.floor(mz / PX)));
        }

        private void update() {
            try {
                if (!isShowing() && !view.isShowing()) return;
                int[] p = ModHook.playerPos();
                me = p;
                if (p != null && follow) { cx = (p[0] - X0 + 0.5) * PX; cz = (ZTOP - p[1] - 0.5) * PX; }
                view.repaint();
            } catch (Throwable t) { }
        }
    }

    // --------------------------------------------------------------- options
    static final class OptionsView extends JPanel {
        OptionsView() {
            super(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.NORTHWEST; c.insets = new Insets(6, 8, 2, 8); c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            JCheckBox wasd = new JCheckBox("WASD camera (Enter to chat, Esc to cancel)", ModHook.isWasd());
            wasd.setFocusable(false);
            wasd.addActionListener(e -> { ModHook.setWasd(wasd.isSelected()); ModHook.focusGame(); });
            add(wasd, c); c.gridy++;
            JCheckBox stretch = new JCheckBox("Stretch game to fill window", ModHook.stretch);
            stretch.setFocusable(false);
            stretch.addActionListener(e -> { ModHook.setStretch(stretch.isSelected()); ModHook.focusGame(); });
            add(stretch, c); c.gridy++;
            add(new JLabel("Log out when idle for:"), c); c.gridy++;
            JComboBox<String> idle = new JComboBox<>(ModHook.IDLE_NAMES);
            idle.setSelectedIndex(ModHook.idleChoice);
            idle.setFocusable(false);
            idle.addActionListener(e -> { ModHook.setIdle(idle.getSelectedIndex()); ModHook.focusGame(); });
            add(idle, c); c.gridy++;
            add(button("Hide the side bar (F9 brings it back)", e -> ModHook.setPanelVisible(false)), c); c.gridy++;
            add(button("Forget saved login", e -> {
                try { new File("./login.dat").delete(); ModHook.savedUser = ""; ModHook.savedPass = ""; } catch (Throwable t) { }
                JOptionPane.showMessageDialog(this, "Saved login removed.");
            }), c); c.gridy++;
            JLabel l = new JLabel("<html><br>Keys: F11 fullscreen, F9 show/hide panel.<br>WASD turn the camera; Enter opens chat.</html>");
            add(l, c); c.gridy++;
            c.weighty = 1;
            add(new JLabel(), c);
        }
    }

    // -------------------------------------------------------------- hiscores
    static final String[] SKILLS = {"Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecrafting"};

    static final class Row {
        String name; int total; long totalXp; int[] lvl = new int[21]; long[] xp = new long[21];
    }

    static final class HiscoreView extends JPanel {
        private final JComboBox<String> skill = new JComboBox<>();
        private final DefaultTableModel model = new DefaultTableModel(new Object[]{"#", "Player", "Level", "XP"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        private final JTable table = new JTable(model);
        private final JLabel status = new JLabel(" ");
        private List<Row> rows = new ArrayList<>();

        HiscoreView() {
            super(new BorderLayout());
            skill.addItem("Overall");
            for (String s : SKILLS) skill.addItem(s);
            skill.setFocusable(false);
            skill.addActionListener(e -> fill());
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
            top.add(skill);
            top.add(button("Refresh", e -> fetch()));
            add(top, BorderLayout.NORTH);
            table.setFocusable(false);
            table.setAutoCreateRowSorter(false);
            table.getColumnModel().getColumn(0).setPreferredWidth(32);
            table.getColumnModel().getColumn(1).setPreferredWidth(120);
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    String n = String.valueOf(model.getValueAt(r, 1));
                    boolean mine = n.equalsIgnoreCase(ModHook.playerName());
                    comp.setFont(comp.getFont().deriveFont(mine ? Font.BOLD : Font.PLAIN));
                    if (!sel) comp.setBackground(mine ? new Color(0xeadb94) : TAN);
                    return comp;
                }
            });
            add(new JScrollPane(table), BorderLayout.CENTER);
            status.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            add(status, BorderLayout.SOUTH);
            status.setText("Loading...");
            addHierarchyListener(e -> { if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) fetch(); });
            Thread th = new Thread(() -> {
                while (true) {
                    try { Thread.sleep(10000); if (isShowing()) fetch(); } catch (InterruptedException e) { return; }
                }
            }, "mod-hiscores");
            th.setDaemon(true);
            th.start();
        }

        private void fetch() {
            new Thread(() -> {
                List<Row> out = new ArrayList<>();
                String msg = "";
                try {
                    String host = System.getProperty("hiscores.host", "127.0.0.1");
                    HttpURLConnection c = (HttpURLConnection) new URL("http://" + host + ":43597/hiscores").openConnection();
                    c.setConnectTimeout(1500); c.setReadTimeout(3000);
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = r.readLine()) != null) {
                            String[] p = line.split("\t");
                            if (p.length < 3 + 42) continue;
                            Row row = new Row();
                            row.name = p[0]; row.total = Integer.parseInt(p[1]); row.totalXp = (long) Double.parseDouble(p[2]);
                            for (int i = 0; i < 21; i++) { row.lvl[i] = Integer.parseInt(p[3 + i]); row.xp[i] = (long) Double.parseDouble(p[24 + i]); }
                            out.add(row);
                        }
                    }
                    msg = out.size() + " players (online now, including bots)";
                } catch (Throwable t) {
                    msg = "Hiscores unavailable (server not updated?)";
                    int[][] my = ModHook.mySkills();
                    if (my != null) {
                        Row row = new Row();
                        row.name = ModHook.playerName();
                        for (int i = 0; i < 21; i++) { row.lvl[i] = my[i][1]; row.xp[i] = my[i][2]; row.total += my[i][1]; row.totalXp += my[i][2]; }
                        out.add(row);
                    }
                }
                final String m = msg;
                SwingUtilities.invokeLater(() -> { rows = out; status.setText(m); fill(); });
            }, "mod-hiscores-fetch").start();
        }

        private void fill() {
            int sk = skill.getSelectedIndex() - 1;
            List<Row> sorted = new ArrayList<>(rows);
            sorted.sort((a, b) -> {
                if (sk < 0) return a.total != b.total ? Integer.compare(b.total, a.total) : Long.compare(b.totalXp, a.totalXp);
                return a.xp[sk] != b.xp[sk] ? Long.compare(b.xp[sk], a.xp[sk]) : Integer.compare(b.lvl[sk], a.lvl[sk]);
            });
            model.setRowCount(0);
            int i = 1;
            for (Row r : sorted) model.addRow(new Object[]{i++, r.name, sk < 0 ? r.total : r.lvl[sk], sk < 0 ? r.totalXp : r.xp[sk]});
        }
    }

    // ------------------------------------------------------------------ wiki
    static final class WikiView extends JPanel {
        WikiView() {
            super(new BorderLayout());
            JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
            p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            p.add(new JLabel("LostHQ 2004 guides"));
            p.add(button("Open LostHQ (online)", e -> open("https://2004.losthq.rs/")));
            add(p, BorderLayout.NORTH);
        }

        static void open(String url) {
            try { Desktop.getDesktop().browse(new URI(url)); } catch (Throwable t) { t.printStackTrace(); }
        }
    }
}

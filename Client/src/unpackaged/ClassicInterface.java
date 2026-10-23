package unpackaged;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import jagex.graphics.sprites.DirectColorSprite;
import jagex.utils.HashTable;

/**
 * The "Classic interface" setting: the 2004 (revision 274) tab icons and the Game Options and
 * Player controls tabs, built from LostCity's layouts in assets/classic. The server swaps the
 * native options and emote tabs for groups 506 and 507 while the setting is on.
 */
public final class ClassicInterface {
    public static final int OPTIONS_GROUP = 506, CONTROLS_GROUP = 507;
    public static volatile boolean enabled;

    // Fonts in cache 443 (index 8): p11_full, p12_full, b12_full, q8_full.
    private static final Map<String, Integer> FONTS = new HashMap<String, Integer>();
    static {
        FONTS.put("p11_full", 494); FONTS.put("p12_full", 495);
        FONTS.put("b12_full", 496); FONTS.put("q8_full", 497);
    }

    // Revision 274 varp names used by the layouts; the ids are the same in cache 443.
    private static final Map<String, Integer> VARPS = new HashMap<String, Integer>();
    static {
        VARPS.put("option_brightness", 166); VARPS.put("option_music", 168); VARPS.put("option_sounds", 169);
        VARPS.put("option_mouse", 170); VARPS.put("option_chat", 171); VARPS.put("option_nodef", 172);
        VARPS.put("option_run", 173); VARPS.put("option_pm", 287);
        VARPS.put("com_mode", 43); VARPS.put("attackstyle_magic", 108);
        VARPS.put("sa_energy", 300); VARPS.put("sa_attack", 301);
    }

    // Cell size of each LostCity sprite sheet (sprites/meta/*.opt).
    private static final Map<String, int[]> SHEET_CELLS = new HashMap<String, int[]>();
    static {
        SHEET_CELLS.put("sideicons", new int[]{30, 30}); SHEET_CELLS.put("miscgraphics", new int[]{36, 36});
        SHEET_CELLS.put("miscgraphics2", new int[]{36, 36}); SHEET_CELLS.put("miscgraphics3", new int[]{25, 25});
        SHEET_CELLS.put("combatboxes", new int[]{72, 36}); SHEET_CELLS.put("combaticons", new int[]{34, 24});
        SHEET_CELLS.put("combaticons2", new int[]{34, 24}); SHEET_CELLS.put("combaticons3", new int[]{34, 24});
        SHEET_CELLS.put("steelborder", new int[]{25, 30}); SHEET_CELLS.put("prayeron", new int[]{30, 30});
    }

    // Models named in the layouts (LostCity pack/model.pack); model_com_N is model N. All exist in cache 443.
    private static final Map<String, Integer> MODELS = new HashMap<String, Integer>();
    static {
        MODELS.put("inv_axe", 2778); MODELS.put("inv_book_fancy", 2543);
    }

    // Classic combat tabs (groups 508-523) and the native 443 combat tab each replaces.
    // combat_whip is a 2004-style tab for the whip (2005), generated from combat_polearm.
    public static final int FIRST_COMBAT_GROUP = 508, LAST_COMBAT_GROUP = 524;
    private static final String[] COMBAT_LAYOUTS = {"combat_unarmed", "combat_axe", "combat_blunt", "combat_crossbow",
        "combat_bow", "combat_claw", "combat_hacksword", "combat_heavysword", "combat_pickaxe", "combat_polearm",
        "combat_scythe", "combat_spear", "combat_spiked", "combat_stabsword", "combat_staff_2", "combat_thrown",
        "combat_whip"};
    private static final int[] NATIVE_COMBAT_GROUPS = {92, 75, 76, 77, 79, 81, 78, 82, 83, 84, 86, 87, 88, 89, 90, 91, 93};
    private static Map<String, Map<String, Integer>> combatChildren;

    // Emotes added after 2004, appended to the Player controls scroll list in 443 emote-tab order.
    // Children start at 52 (button, label, button, label...); the server maps them the same way.
    private static final int EXTRA_EMOTE_FIRST_CHILD = 52;
    private static final String[] EXTRA_EMOTES = {
        "Shrug", "Jump\\nfor Joy", "Yawn", "Jig", "Spin", "Head-\\nbang", "Blow\\nKiss", "Panic",
        "Rasp-\\nberry", "Salute", "Goblin\\nBow", "Goblin\\nSalute", "Slap\\nHead", "Stamp", "Flap",
        "Idea", "Zombie\\nWalk", "Zombie\\nDance", "Scared", "Rabbit\\nHop", "Skill\\nCape"
    };
    private static final int[] EMOTE_COLUMNS = {3, 45, 86, 127};
    private static final int EMOTE_ROW_HEIGHT = 30, EMOTE_FIRST_EXTRA_ROW_Y = 123;

    private static final Map<String, DirectColorSprite[]> sheets = new HashMap<String, DirectColorSprite[]>();

    private ClassicInterface() { }

    // ------------------------------------------------------------------ sprites

    /** Cells of a LostCity sprite sheet, row by row; magenta is transparent. */
    private static DirectColorSprite[] sheet(String name, int cellWidth, int cellHeight) {
        DirectColorSprite[] cached = sheets.get(name);
        if (cached != null) return cached;
        BufferedImage image;
        try (InputStream in = ClassicInterface.class.getResourceAsStream("/assets/classic/" + name + ".png")) {
            if (in == null) throw new IOException("Missing classic interface asset " + name);
            image = ImageIO.read(in);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        int columns = image.getWidth() / cellWidth, rows = image.getHeight() / cellHeight;
        DirectColorSprite[] cells = new DirectColorSprite[columns * rows];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                DirectColorSprite sprite = new DirectColorSprite(cellWidth, cellHeight);
                image.getRGB(column * cellWidth, row * cellHeight, cellWidth, cellHeight, sprite.anIntArray2476, 0, cellWidth);
                for (int i = 0; i < sprite.anIntArray2476.length; i++) {
                    int rgb = sprite.anIntArray2476[i] & 0xffffff;
                    boolean clear = (sprite.anIntArray2476[i] >>> 24) == 0 || rgb == 0xff00ff;
                    sprite.anIntArray2476[i] = clear ? 0 : Math.max(1, rgb);
                }
                cells[row * columns + column] = sprite;
            }
        }
        sheets.put(name, cells);
        return cells;
    }

    private static DirectColorSprite graphic(String reference) {
        String[] parts = reference.split(",");
        int cell = Integer.parseInt(parts[1].trim());
        String name = parts[0].trim();
        int[] size = SHEET_CELLS.get(name);
        if (size == null) throw new IllegalArgumentException("Unknown classic sprite sheet " + name);
        return sheet(name, size[0], size[1])[cell];
    }

    /** Draws the 2004 icon for sidebar icon slot index (0-12) when the setting is on. */
    public static boolean drawSideIcon(int index, int x, int y) {
        if (!enabled) return false;
        try {
            DirectColorSprite[] icons = sheet("sideicons", 30, 30);
            if (index < 0 || index >= icons.length) return false;
            icons[index].method670(x, y);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    // ------------------------------------------------------------------ tabs

    /** Groups built at runtime here; they have no cache archive to load or unload. */
    public static boolean isRuntimeGroup(int group) {
        return group == OPTIONS_GROUP || group == CONTROLS_GROUP
                || group >= FIRST_COMBAT_GROUP && group <= LAST_COMBAT_GROUP;
    }

    /** Supplies groups 506-523 to the widget loader. */
    public static boolean load(int group) {
        if (!isRuntimeGroup(group)) return false;
        if (Class62_Sub1.widgets.length <= LAST_COMBAT_GROUP) {
            Class62_Sub1.widgets = Arrays.copyOf(Class62_Sub1.widgets, LAST_COMBAT_GROUP + 1);
            Class39_Sub5_Sub4.widgetsLoaded = Arrays.copyOf(Class39_Sub5_Sub4.widgetsLoaded, LAST_COMBAT_GROUP + 1);
        }
        if (Class62_Sub1.widgets[group] != null && Class39_Sub5_Sub4.widgetsLoaded[group]) return true;
        if (group >= FIRST_COMBAT_GROUP) return loadCombat(group);
        List<Map<String, String>> components = parse(group == OPTIONS_GROUP ? "options" : "controls");
        if (group == CONTROLS_GROUP) addLaterEmotes(components);
        int size = 0;
        for (Map<String, String> c : components) size = Math.max(size, child(c.get("@name")) + 1);
        Widget[] widgets = new Widget[size];
        for (Map<String, String> c : components) {
            int child = child(c.get("@name"));
            widgets[child] = build(group, child, c, null);
        }
        // Unused ids stay as hidden empty layers so every slot is safe to draw.
        for (int i = 0; i < widgets.length; i++) {
            if (widgets[i] != null) continue;
            Widget w = widgets[i] = new Widget();
            w.anInt2084 = group << 16 | i;
            w.aBoolean2055 = true;
        }
        if (group == CONTROLS_GROUP) applyEmoteLocks(widgets);
        Class62_Sub1.widgets[group] = widgets;
        Class39_Sub5_Sub4.widgetsLoaded[group] = true;
        return true;
    }

    /** Component name -> child for each combat layout, from assets/classic/combat_ids.tsv (shared with the server). */
    private static Map<String, Integer> combatIds(String layout) {
        if (combatChildren == null) {
            Map<String, Map<String, Integer>> all = new HashMap<String, Map<String, Integer>>();
            try (InputStream in = ClassicInterface.class.getResourceAsStream("/assets/classic/combat_ids.tsv")) {
                if (in == null) throw new IOException("Missing classic combat id table");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String[] f = line.split("\t");
                    Map<String, Integer> ids = all.get(f[2]);
                    if (ids == null) all.put(f[2], ids = new HashMap<String, Integer>());
                    ids.put(f[4], Integer.parseInt(f[5]));
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            combatChildren = all;
        }
        return combatChildren.get(layout);
    }

    /** A 2004 combat tab, plus the native tab's combat level text in its 443 position. */
    private static boolean loadCombat(int group) {
        String layout = COMBAT_LAYOUTS[group - FIRST_COMBAT_GROUP];
        Map<String, Integer> ids = combatIds(layout);
        List<Map<String, String>> components = parse("combat/" + layout);
        int size = 0;
        for (int child : ids.values()) size = Math.max(size, child + 1);
        Widget[] widgets = new Widget[size + 1];
        for (Map<String, String> c : components) {
            int child = ids.get(c.get("@name"));
            widgets[child] = build(group, child, c, ids);
        }
        widgets[size] = combatLevel(group, size, NATIVE_COMBAT_GROUPS[group - FIRST_COMBAT_GROUP]);
        // The combat level sits at its native spot (y 25). These layouts have a tighter header, so give
        // them the 2h sword's: "Weapon:" and the name at y 10, "Choose Attack Style" at y 40.
        if (layout.equals("combat_polearm") || layout.equals("combat_spear") || layout.equals("combat_whip")
                || layout.equals("combat_staff_2")) {
            for (String header : new String[]{"com_1", "name"}) {
                Widget w = widgets[ids.get(header)];
                w.anInt2024 = w.anInt2021 = 10;
            }
            Widget title = widgets[ids.get("com_3")];
            title.anInt2024 = title.anInt2021 = 40;
        }
        for (int i = 0; i < widgets.length; i++) {
            if (widgets[i] != null) continue;
            Widget w = widgets[i] = new Widget();
            w.anInt2084 = group << 16 | i;
            w.aBoolean2055 = true;
        }
        Class62_Sub1.widgets[group] = widgets;
        Class39_Sub5_Sub4.widgetsLoaded[group] = true;
        return true;
    }

    /** Copy of the native combat tab's "Combat Lvl: %1" text (script opcode 8), kept where 443 draws it. */
    private static Widget combatLevel(int group, int child, int nativeGroup) {
        Widget w = new Widget();
        w.anInt2084 = group << 16 | child;
        w.aBoolean2055 = true;
        try {
            if (!jagex.io.JSocket.loadWidget(nativeGroup) || Class62_Sub1.widgets[nativeGroup] == null) return w;
        } catch (RuntimeException unavailable) {
            return w; // no native tab to copy from: the classic tab still works, without the level
        }
        for (Widget level : Class62_Sub1.widgets[nativeGroup]) {
            if (level == null || level.type != 4 || level.scriptOpcodes == null || level.scriptOpcodes[0][0] != 8) continue;
            w.type = 4;
            w.anInt2050 = -1;
            w.anInt2057 = -1;
            w.anInt2090 = w.anInt2091 = level.anInt2090;
            w.anInt2024 = w.anInt2021 = level.anInt2024;
            w.quadWidth = level.quadWidth;
            w.quadHeight = level.quadHeight;
            w.anInt2032 = level.anInt2032;
            w.anInt1996 = level.anInt1996;
            w.anInt2036 = level.anInt2036;
            w.anInt2105 = level.anInt2105;
            w.aBoolean2059 = level.aBoolean2059;
            w.aClass3_2029 = level.aClass3_2029;
            w.aClass3_2048 = level.aClass3_2048;
            w.activeQuadColor = level.activeQuadColor;
            w.inactiveQuadColor = level.inactiveQuadColor;
            w.scriptOpcodes = level.scriptOpcodes;
            w.aBoolean2055 = false;
            break;
        }
        return w;
    }

    // Unlock tests copied from the 443 emote tab (group 464): {varp, bit}, or {-1, varbit} for "varbit > 6".
    private static final int GOBLIN = -1;
    private static final int[][] MIME_LOCKS = {{313, 0}, {313, 1}, {313, 2}, {313, 3}}; // Glass Wall, Glass Box, Climb Rope, Lean
    private static final int[][] MIME_CHILDREN = {{41, 45, 46}, {42, 47, 48}, {43, 49, 50}, {44, 51}};
    // Extra emote index -> lock; null = always available.
    private static final int[][] EXTRA_LOCKS = {
        null, null, null, null, null, null, null, null, null, null,
        {GOBLIN, 532}, {GOBLIN, 532},                    // Goblin Bow, Goblin Salute (The Lost Tribe)
        {802, 1}, {802, 3}, {802, 0}, {802, 2},          // Slap Head, Stamp, Flap, Idea (Stronghold of Security)
        {313, 6}, {313, 5},                              // Zombie Walk, Zombie Dance (Gravedigger)
        {313, 4}, {313, 7}, {313, 8}                     // Scared, Rabbit Hop, Skill Cape
    };
    private static final int LOCKED_LABEL = 0x7A7A7A, UNLOCKED_LABEL = 0xFFFF00;
    private static DirectColorSprite lockedButton;

    /** Locked emotes: greyed-out stone and grey label until the unlock config is set ("Greyed out" style). */
    private static void applyEmoteLocks(Widget[] widgets) {
        for (int i = 0; i < MIME_LOCKS.length; i++) {
            for (int child : MIME_CHILDREN[i]) lock(widgets[child], MIME_LOCKS[i]);
        }
        for (int i = 0; i < EXTRA_LOCKS.length; i++) {
            if (EXTRA_LOCKS[i] == null) continue;
            lock(widgets[EXTRA_EMOTE_FIRST_CHILD + i * 2], EXTRA_LOCKS[i]);
            lock(widgets[EXTRA_EMOTE_FIRST_CHILD + i * 2 + 1], EXTRA_LOCKS[i]);
        }
    }

    private static void lock(Widget w, int[] test) {
        if (test[0] == GOBLIN) {
            w.scriptOpcodes = new int[][]{{14, test[1], 0}};
            w.conditionOpcodes = new int[]{3};
            w.conditionValues = new int[]{6};
        } else {
            w.scriptOpcodes = new int[][]{{13, test[0], test[1], 0}};
            w.conditionOpcodes = new int[]{1};
            w.conditionValues = new int[]{1};
        }
        if (w.type == 5) {
            w.runtimeActiveSprite = w.runtimeSprite;
            w.runtimeSprite = lockedButton();
        } else if (w.type == 4) {
            w.activeQuadColor = LOCKED_LABEL;
            w.inactiveQuadColor = UNLOCKED_LABEL;
        }
    }

    private static DirectColorSprite lockedButton() {
        if (lockedButton != null) return lockedButton;
        DirectColorSprite normal = graphic("miscgraphics2,0");
        DirectColorSprite grey = new DirectColorSprite(normal.width, normal.height);
        for (int i = 0; i < grey.anIntArray2476.length; i++) {
            int rgb = normal.anIntArray2476[i];
            if (rgb == 0) continue;
            int y = (int) (((rgb >> 16 & 255) * 0.3 + (rgb >> 8 & 255) * 0.59 + (rgb & 255) * 0.11) * 0.7);
            grey.anIntArray2476[i] = Math.max(1, y << 16 | y << 8 | y);
        }
        return lockedButton = grey;
    }

    private static int child(String name) {
        return Integer.parseInt(name.substring("com_".length()));
    }

    private static List<Map<String, String>> parse(String file) {
        List<Map<String, String>> components = new ArrayList<Map<String, String>>();
        try (InputStream in = ClassicInterface.class.getResourceAsStream("/assets/classic/" + file + ".if")) {
            if (in == null) throw new IOException("Missing classic interface layout " + file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            Map<String, String> current = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("[") && line.endsWith("]")) {
                    current = new LinkedHashMap<String, String>();
                    current.put("@name", line.substring(1, line.length() - 1));
                    components.add(current);
                } else if (current != null && line.indexOf('=') > 0) {
                    current.put(line.substring(0, line.indexOf('=')), line.substring(line.indexOf('=') + 1));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return components;
    }

    /** Later emotes as extra rows below the 2004 ones, in the same stone-button style. */
    private static void addLaterEmotes(List<Map<String, String>> components) {
        int rows = (EXTRA_EMOTES.length + EMOTE_COLUMNS.length - 1) / EMOTE_COLUMNS.length;
        for (Map<String, String> c : components) {
            if (c.get("@name").equals("com_40")) {
                c.put("scroll", Integer.toString(EMOTE_FIRST_EXTRA_ROW_Y + rows * EMOTE_ROW_HEIGHT));
            }
        }
        for (int i = 0; i < EXTRA_EMOTES.length; i++) {
            int x = EMOTE_COLUMNS[i % EMOTE_COLUMNS.length];
            int y = EMOTE_FIRST_EXTRA_ROW_Y + (i / EMOTE_COLUMNS.length) * EMOTE_ROW_HEIGHT;
            Map<String, String> button = new LinkedHashMap<String, String>();
            button.put("@name", "com_" + (EXTRA_EMOTE_FIRST_CHILD + i * 2));
            button.put("layer", "com_40"); button.put("type", "graphic"); button.put("buttontype", "normal");
            button.put("x", Integer.toString(x)); button.put("y", Integer.toString(y));
            button.put("width", "36"); button.put("height", "25"); button.put("graphic", "miscgraphics2,0");
            components.add(button);
            boolean twoLines = EXTRA_EMOTES[i].contains("\\n");
            Map<String, String> label = new LinkedHashMap<String, String>();
            label.put("@name", "com_" + (EXTRA_EMOTE_FIRST_CHILD + i * 2 + 1));
            label.put("layer", "com_40"); label.put("type", "text"); label.put("center", "yes");
            // Two pixels of margin either side so lines just wider than the button don't wrap again.
            label.put("x", Integer.toString(x - 2)); label.put("y", Integer.toString(y + (twoLines ? 1 : 7)));
            label.put("width", "40"); label.put("height", twoLines ? "22" : "11");
            label.put("font", "p11_full"); label.put("shadowed", "yes");
            label.put("text", EXTRA_EMOTES[i]); label.put("colour", "0xFFFF00");
            components.add(label);
        }
    }

    private static int number(Map<String, String> c, String key, int otherwise) {
        String value = c.get(key);
        if (value == null) return otherwise;
        value = value.trim();
        return value.startsWith("0x") ? (int) Long.parseLong(value.substring(2), 16) : Integer.parseInt(value);
    }

    /** Child of a component named in the same layout: the combat id table, or com_N for the options and controls tabs. */
    private static int resolve(Map<String, Integer> ids, String name) {
        if (ids == null) return child(name);
        Integer child = ids.get(name);
        if (child == null) throw new IllegalArgumentException("Unknown classic component " + name);
        return child;
    }

    // IF1 script comparisons (Huffmans.parseClientScript): 1 equal, 2 less than, 3 greater than, 4 not equal.
    private static int comparison(String name) {
        if (name.equals("eq")) return 1;
        if (name.equals("lt")) return 2;
        if (name.equals("gt")) return 3;
        if (name.equals("ne")) return 4;
        throw new IllegalArgumentException("Unsupported classic comparison " + name);
    }

    /** IF1 opcodes for one LostCity script op; consecutive ops are added together by the interpreter. */
    private static int[] scriptOp(String op) {
        String[] p = op.split(",");
        for (int i = 0; i < p.length; i++) p[i] = p[i].trim();
        if (p[0].equals("pushvar")) return new int[]{5, varp(p[1])};
        if (p[0].equals("testbit")) return new int[]{13, varp(p[1]), Integer.parseInt(p[2])};
        if (p[0].equals("runenergy")) return new int[]{11};
        if (p[0].equals("runweight")) return new int[]{12};
        throw new IllegalArgumentException("Unsupported classic script op " + op);
    }

    private static int varp(String name) {
        Integer id = VARPS.get(name);
        if (id == null) throw new IllegalArgumentException("Unknown classic varp " + name);
        return id;
    }

    private static int model(String name) {
        if (name.startsWith("model_com_")) return Integer.parseInt(name.substring("model_com_".length()));
        Integer id = MODELS.get(name);
        if (id == null) throw new IllegalArgumentException("Unknown classic model " + name);
        return id;
    }

    private static Widget build(int group, int child, Map<String, String> c, Map<String, Integer> ids) {
        Widget w = new Widget();
        String type = c.get("type");
        w.type = "layer".equals(type) ? 0 : "inv".equals(type) ? 2 : "rect".equals(type) ? 3 : "text".equals(type) ? 4
                : "graphic".equals(type) ? 5 : "model".equals(type) ? 6 : "invtext".equals(type) ? 7 : 0;
        w.anInt2084 = group << 16 | child;
        String layer = c.get("layer");
        w.anInt2050 = layer == null ? -1 : (group << 16 | resolve(ids, layer));
        String over = c.get("overlayer");
        w.anInt2057 = over == null ? -1 : resolve(ids, over);
        w.anInt2090 = w.anInt2091 = number(c, "x", 0);
        w.anInt2024 = w.anInt2021 = number(c, "y", 0);
        w.quadWidth = number(c, "width", 0);
        w.quadHeight = number(c, "height", 0);
        w.aBoolean2055 = "yes".equals(c.get("hide"));
        String button = c.get("buttontype");
        w.anInt2089 = button == null ? 0 : "normal".equals(button) ? 1 : "target".equals(button) ? 2
                : "close".equals(button) ? 3 : "toggle".equals(button) ? 4 : "select".equals(button) ? 5 : "pause".equals(button) ? 6 : 0;

        // scriptN is a comparison ("gt,99") of the sum of scriptNop1..scriptNopK.
        List<int[]> scripts = new ArrayList<int[]>();
        List<int[]> conditions = new ArrayList<int[]>();
        for (int n = 1; c.containsKey("script" + n + "op1") || c.containsKey("script" + n); n++) {
            List<Integer> ops = new ArrayList<Integer>();
            for (int k = 1; c.containsKey("script" + n + "op" + k); k++) {
                for (int value : scriptOp(c.get("script" + n + "op" + k))) ops.add(value);
            }
            ops.add(0);
            int[] script = new int[ops.size()];
            for (int i = 0; i < script.length; i++) script[i] = ops.get(i);
            scripts.add(script);
            String condition = c.get("script" + n);
            if (condition != null) {
                String[] parts = condition.split(",");
                conditions.add(new int[]{comparison(parts[0].trim()), Integer.parseInt(parts[1].trim())});
            }
        }
        if (!scripts.isEmpty()) w.scriptOpcodes = scripts.toArray(new int[0][]);
        if (!conditions.isEmpty()) {
            w.conditionOpcodes = new int[conditions.size()];
            w.conditionValues = new int[conditions.size()];
            for (int i = 0; i < conditions.size(); i++) {
                w.conditionOpcodes[i] = conditions.get(i)[0];
                w.conditionValues[i] = conditions.get(i)[1];
            }
        }

        if (w.type == 0) w.anInt2095 = number(c, "scroll", 0);
        if (w.type == 3) {
            w.drawSolidQuad = "yes".equals(c.get("fill"));
            w.activeQuadColor = number(c, "colour", 0);
            w.inactiveQuadColor = number(c, "activecolour", 0);
            w.anInt2041 = number(c, "overcolour", 0);
            w.anInt2086 = number(c, "activeovercolour", 0);
        }
        if (w.type == 4) {
            w.anInt2032 = "yes".equals(c.get("center")) ? 1 : 0;
            w.anInt1996 = 0;
            w.anInt2036 = 0;
            w.anInt2105 = FONTS.get(c.containsKey("font") ? c.get("font") : "p12_full");
            w.aBoolean2059 = "yes".equals(c.get("shadowed"));
            String text = c.containsKey("text") ? c.get("text") : "";
            w.aClass3_2029 = GrandExchangeWidgets.literal(text);
            w.aClass3_2048 = GrandExchangeWidgets.literal(c.containsKey("activetext") ? c.get("activetext") : "");
            w.activeQuadColor = number(c, "colour", 0);
            w.inactiveQuadColor = number(c, "activecolour", 0);
            w.anInt2041 = number(c, "overcolour", 0);
            w.anInt2086 = number(c, "activeovercolour", 0);
        }
        if (w.type == 5) {
            if (c.containsKey("graphic")) w.runtimeSprite = graphic(c.get("graphic"));
            if (c.containsKey("activegraphic")) w.runtimeActiveSprite = graphic(c.get("activegraphic"));
        }
        if (w.type == 6) {
            // Model fields as decoded for IF1 type 6 in Widget.decodeOldFormat.
            w.anInt2009 = 1;
            w.anInt2026 = c.containsKey("model") ? model(c.get("model")) : -1;
            w.anInt2082 = 1;
            w.anInt2054 = c.containsKey("activemodel") ? model(c.get("activemodel")) : -1;
            w.anInt2103 = -1;
            w.anInt2052 = -1;
            w.anInt2074 = number(c, "zoom", 100);
            w.anInt2098 = number(c, "xan", 0);
            w.anInt2011 = number(c, "yan", 0);
        }
        // Right-click option: the layout's own, else the default for the button type (Widget.decodeOldFormat).
        if (c.containsKey("option")) w.aClass3_2068 = GrandExchangeWidgets.literal(c.get("option"));
        else if (w.anInt2089 == 1) w.aClass3_2068 = Class23.aClass3_437;
        else if (w.anInt2089 == 4 || w.anInt2089 == 5) w.aClass3_2068 = HashTable.aClass3_383;
        else if (w.anInt2089 == 6) w.aClass3_2068 = Class46.aClass3_874;
        if (w.anInt2089 == 1 || w.anInt2089 == 4 || w.anInt2089 == 5) w.anInt2049 |= 0x400000;
        if (w.anInt2089 == 6) w.anInt2049 |= 0x1;
        w.anInt1998 = w.anInt2049;
        return w;
    }
}

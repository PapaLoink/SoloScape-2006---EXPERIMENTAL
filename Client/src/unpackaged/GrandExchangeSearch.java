package unpackaged;

import java.awt.Graphics;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import jagex.graphics.AbstractImage;
import jagex.graphics.BitmapFont;
import jagex.graphics.DrawingArea;
import jagex.graphics.sprites.DirectColorSprite;
import jagex.io.FrameBuffer;
import jagex.io.Buffer;
import jagex.utils.JString;
import jagex.utils.Queue;

/** Revision-530 interface 389 rendered over the complete fixed-mode chat panel. */
public final class GrandExchangeSearch {
    private static final int RESULT = 0x7fff0000;
    private static final int CLOSE = RESULT | 0xffff;
    private static final int WIDTH = 516;
    private static final int HEIGHT = 130;
    private static final int SCREEN_X = 0;
    private static final int SCREEN_Y = 338;
    private static final int LIST_HEIGHT = 108;
    private static final int ROW_HEIGHT = 11;
    private static final List<Integer> results = new ArrayList<Integer>();
    private static final Widget scrollbar = new Widget();
    private static AbstractImage panel;
    private static String lastQuery = "";
    private static int hovered = -1;
    private static final java.util.Map<Integer, String> developerNames = new java.util.HashMap<Integer, String>();

    private static void ensurePanel() {
        if (panel == null)
            panel = Queue.method994(Class41.aCanvas778, WIDTH, HEIGHT, (byte) -111);
    }

    public static boolean containsScreen(int x, int y) {
        return x >= SCREEN_X && x < SCREEN_X + WIDTH && y >= SCREEN_Y && y < SCREEN_Y + HEIGHT;
    }

    public static void drawToScreen(Graphics graphics) {
        ensurePanel();
        panel.draw(graphics, SCREEN_X, SCREEN_Y);
    }

    /** Keep direct frame updates from exposing backgrounds beneath the search. */
    public static void drawBehindSearch(AbstractImage image, Graphics graphics, int x, int y) {
        if (!GrandExchangeWidgets.searching) {
            image.draw(graphics, x, y);
            return;
        }
        // Use child graphics so callers retain their clip and canvas scaling.
        int right = SCREEN_X + WIDTH, bottom = SCREEN_Y + HEIGHT;
        int[][] regions = {
            {x, y, image.width, Math.max(0, Math.min(y + image.height, SCREEN_Y) - y)},
            {x, Math.max(y, bottom), image.width, Math.max(0, y + image.height - Math.max(y, bottom))},
            {x, Math.max(y, SCREEN_Y), Math.max(0, Math.min(x + image.width, SCREEN_X) - x),
                Math.max(0, Math.min(y + image.height, bottom) - Math.max(y, SCREEN_Y))},
            {Math.max(x, right), Math.max(y, SCREEN_Y), Math.max(0, x + image.width - Math.max(x, right)),
                Math.max(0, Math.min(y + image.height, bottom) - Math.max(y, SCREEN_Y))}
        };
        for (int[] region : regions) {
            if (region[2] == 0 || region[3] == 0) continue;
            Graphics behind = graphics.create();
            try {
                behind.clipRect(region[0], region[1], region[2], region[3]);
                image.draw(behind, x, y);
            } finally {
                behind.dispose();
            }
        }
    }

    public static void mouseScreen(int x, int y) { mouse(x - SCREEN_X, y - SCREEN_Y); }
    public static void menuScreen(int x, int y) { menu(x - SCREEN_X, y - SCREEN_Y); }

    static void open() {
        lastQuery = null;
        scrollbar.quadWidth = 440;
        scrollbar.anInt1994 = 0;
        hovered = -1;
        update(Class66.aClass3_1151);
    }

    static void close() {
        hovered = -1;
        ClientScript.aBoolean1690 = true;
        Class14.aBoolean245 = true;
    }

    private static String string(JString value) {
        return new String(value.bytes, 0, value.length, Charset.forName("windows-1252"));
    }

    public static String name(int id) {
        if (!developerPicker()) return string(Class26.getItemDefinition(id).aClass3_1661);
        String name = developerNames.get(id);
        if (name != null) return name;
        // Read the original name: the ordinary item loader masks members items on free worlds.
        byte[] data = Class31.itemFileLoader.lookupFile(10, id);
        ItemDefinition item = new ItemDefinition();
        if (data != null) item.decode(0, new Buffer(data));
        name = item.aClass3_1661 == null ? "null" : string(item.aClass3_1661);
        if (item.anInt1644 != -1 && item.anInt1651 != id) name = name(item.anInt1651) + " (noted)";
        developerNames.put(id, name);
        return name;
    }

    static boolean developerPicker() { return Class39_Sub11.anInt1478 == 505; }

    static boolean hasSearchInterface() {
        return Class39_Sub11.anInt1478 == 500 || developerPicker();
    }

    static int itemLimit() {
        return Math.min(Class37.anInt663, developerPicker() ? 32768 : 11884);
    }

    static boolean isSearchable(int id) {
        // Removed broken weapons and their noted/crafting forms.
        if (id == 11235 || id == 11236 || (id >= 11686 && id <= 11715)) return false;
        if (id < 0 || id >= itemLimit()) return false;
        ItemDefinition item = Class26.getItemDefinition(id);
        if (!developerPicker() && item.aClass3_1661 == null) return false;
        String name = name(id).trim();
        if (name.isEmpty() || name.equalsIgnoreCase("null") || name.equalsIgnoreCase("null (noted)")) return false;
        return developerPicker() || (GrandExchangeCatalog.contains(id) && item.anInt1644 == -1 && id != 995);
    }

    private static void dismiss() {
        boolean developer = developerPicker();
        GrandExchangeWidgets.cancelSearch();
        if (developer) Class55.method999(31121);
    }

    public static void update(JString value) {
        String text = string(value).trim().toLowerCase(Locale.ROOT);
        if (!text.equals(lastQuery)) {
            lastQuery = text;
            results.clear();
            scrollbar.anInt1994 = 0;
            hovered = -1;
            if (!text.isEmpty()) {
                String[] terms = text.split("\\s+");
                for (int id = 0; id < itemLimit(); id++) {
                    if (!isSearchable(id)) continue;
                    String name = name(id).toLowerCase(Locale.ROOT);
                    if (name.equals("null")) continue;
                    boolean match = true;
                    for (String term : terms) if (!name.contains(term)) { match = false; break; }
                    if (match) results.add(id);
                }
                Collections.sort(results, new Comparator<Integer>() {
                    public int compare(Integer a, Integer b) { return name(a).compareToIgnoreCase(name(b)); }
                });
            }
        }
        Class14.aBoolean245 = true;
    }

    public static int resultCount() { return results.size(); }

    static boolean acceptsInput(Widget w) {
        int group = w.anInt2084 >>> 16;
        return group < 500 || group > 504 || !w.aBoolean2055;
    }

    public static void selectFirst() { if (!results.isEmpty()) select(results.get(0)); }

    public static void select(int id) {
        if (!GrandExchangeWidgets.searching || !hasSearchInterface() || !isSearchable(id)) return;
        FrameBuffer.outgoingGameBuffer.putFrame(19);
        FrameBuffer.outgoingGameBuffer.putWord(id);
        if (!developerPicker()) GrandExchangeWidgets.cancelSearch();
    }

    static boolean click(int hash) {
        if (!GrandExchangeWidgets.searching || (hash & 0xffff0000) != RESULT) return false;
        if (hash == CLOSE) dismiss();
        else {
            int index = hash & 0xffff;
            if (index < results.size()) select(results.get(index));
        }
        return true;
    }

    private static int rowAt(int x, int y) {
        if (x < 60 || x >= 500 || y < 0 || y >= LIST_HEIGHT) return -1;
        int row = (y + scrollbar.anInt1994 - 3) / ROW_HEIGHT;
        return y + scrollbar.anInt1994 >= 3 && row < results.size() ? row : -1;
    }

    public static void menu(int x, int y) {
        if (!GrandExchangeWidgets.searching) return;
        int row = rowAt(x, y);
        if (row >= 0) JString.method55(0, GrandExchangeWidgets.literal("Select"), RESULT | row,
                GrandExchangeWidgets.literal(name(results.get(row))), (byte) -51, 0, 20);
        if (x >= 500 && x < 516 && y >= 111 && y < 127)
            JString.method55(0, GrandExchangeWidgets.literal("Close"), CLOSE, Class66.blankString, (byte) -51, 0, 20);
    }

    public static void mouse(int x, int y) {
        if (!GrandExchangeWidgets.searching) return;
        if (!hasSearchInterface()) { GrandExchangeWidgets.cancelSearch(); return; }
        int height = Math.max(LIST_HEIGHT, results.size() * ROW_HEIGHT + 7);
        if (height > LIST_HEIGHT)
            Class39_Sub4.method456((byte) 121, 500, x, y, 3, LIST_HEIGHT, 0, height, scrollbar);
        scrollbar.anInt1994 = Math.max(0, Math.min(height - LIST_HEIGHT, scrollbar.anInt1994));
        int row = rowAt(x, y);
        if (hovered != row) { hovered = row; Class14.aBoolean245 = true; }
    }

    /** Draws revision-530 interface 389 into its own full-size chat-panel buffer. */
    public static void draw() {
        ensurePanel();
        panel.method1006(0);
        BitmapFont small = jagex.world.actors.Npc.aClass39_Sub5_Sub10_Sub1_2495;
        if (small == null) small = Class39_Sub5_Sub14.p12fullFont;
        GrandExchangeAssets.sprite("search-full").method670(0, 0);
        GrandExchangeAssets.sprite("1139-0").method670(10, 8);
        GrandExchangeAssets.sprite("1154-0").method670(6, 113);
        GrandExchangeAssets.sprite("831-0").method670(500, 111);
        if (hovered >= 0 && hovered < results.size()) {
            DirectColorSprite icon = FrameBuffer.method841(results.get(hovered), 0, false, 1, 68, 1);
            if (icon != null) icon.method670(12, 10);
        }
        DrawingArea.setDimensions(60, 0, 500, LIST_HEIGHT);
        if (lastQuery.isEmpty()) {
            Class32.aClass39_Sub5_Sub10_Sub1_587.method629(
                    GrandExchangeWidgets.literal(developerPicker() ? "Developer Item Search" : "Grand Exchange Item Search"), 280, 27, 0xa05a00);
            small.method629(GrandExchangeWidgets.literal("To search for an item, start by typing part of its name."), 280, 56, 0xa05a00);
            small.method629(GrandExchangeWidgets.literal(developerPicker()
                    ? "Click a result to add one item to your inventory for free."
                    : "Then, simply select the item you want from the results on display."), 280, 71, 0xa05a00);
        } else if (results.isEmpty()) {
            small.method629(GrandExchangeWidgets.literal("No matching items found."), 280, 56, 0xa05a00);
        } else {
            int first = scrollbar.anInt1994 / ROW_HEIGHT;
            int last = Math.min(results.size(), first + (LIST_HEIGHT + ROW_HEIGHT - 1) / ROW_HEIGHT + 1);
            for (int i = first; i < last; i++) {
                if (i == hovered) DrawingArea.drawQuadOverlay(60,
                        3 + i * ROW_HEIGHT - scrollbar.anInt1994, 440, ROW_HEIGHT, 0x665b46, 64);
                small.method635(GrandExchangeWidgets.literal(name(results.get(i))), 64,
                        12 + i * ROW_HEIGHT - scrollbar.anInt1994, 0xa05a00, false);
            }
        }
        DrawingArea.resetDimensions();
        small.method635(GrandExchangeWidgets.literal(string(Class66.aClass3_1151) + "*"), 27, 123, 0xffffff, true);
        int height = results.size() * ROW_HEIGHT + 7;
        if (height > LIST_HEIGHT) Class4.method102(500, scrollbar.anInt1994, height, 0, 18734, LIST_HEIGHT);
    }

    public static boolean key(int key, int character) {
        if (!GrandExchangeWidgets.searching) return false;
        if (!hasSearchInterface()) { GrandExchangeWidgets.cancelSearch(); return false; }
        // AWT Escape (27) maps to client key 0.
        if (key == 0) { dismiss(); return true; }
        if (key == 84) { GrandExchangeWidgets.submitSearch(Class66.aClass3_1151); return true; }
        if (key == 85 && Class66.aClass3_1151.length > 0)
            Class66.aClass3_1151 = Class66.aClass3_1151.method59(0, -1, Class66.aClass3_1151.length - 1);
        else if (key == 98) scrollbar.anInt1994 = Math.max(0, scrollbar.anInt1994 - ROW_HEIGHT);
        else if (key == 99) scrollbar.anInt1994 = Math.min(Math.max(0, results.size() * ROW_HEIGHT + 7 - LIST_HEIGHT), scrollbar.anInt1994 + ROW_HEIGHT);
        else if (Class26.method290(character, -160) && Class66.aClass3_1151.length < 40)
            Class66.aClass3_1151 = Class66.aClass3_1151.createConcatChar(character);
        update(Class66.aClass3_1151);
        return true;
    }
}

package unpackaged;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** XP drops and a level-progress card in the top right of the game viewport. */
public final class XpOverlay {
    static boolean enabled = true;
    static final String[] NAMES = {"Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecrafting"};
    static final int[] COLS = {0xd04030, 0x4070d0, 0x30a050, 0xe05050, 0x60a030, 0x50c0d0, 0x9060d0, 0xb07030, 0x40a040, 0x30a0a0, 0x5080e0, 0xe08020, 0xc0a040, 0x909090, 0x806040, 0x30b060, 0x4060c0, 0x7050a0, 0xa04040, 0x70b030, 0xd0c040};
    private static final long DROP_MS = 2600, CARD_MS = 9000;

    private static final class Drop { int skill; long amount; long start; }
    private static final List<Drop> drops = new ArrayList<Drop>();
    private static long[] lastXp = null;
    private static int lastSkill = -1;
    private static long lastGain = 0, lastSpawn = 0;
    private static long sessionXp = 0;
    private static int levelUpSkill = -1; private static long levelUpAt = 0;

    static long xpFor(int lvl) { double t = 0; for (int i = 1; i < lvl; i++) t += Math.floor(i + 300 * Math.pow(2, i / 7.0)); return (long) Math.floor(t / 4); }
    static int levelOf(long xp) { int l = 1; while (l < 99 && xpFor(l + 1) <= xp) l++; return l; }

    private static void poll() {
        if (!ModHook.inGame()) { lastXp = null; drops.clear(); lastSkill = -1; return; }
        int[][] my = ModHook.mySkills();
        if (my == null) return;
        long now = System.currentTimeMillis();
        long[] cur = new long[21];
        long tot = 0;
        for (int i = 0; i < 21; i++) { cur[i] = my[i][2]; tot += cur[i]; }
        if (lastXp == null) { if (tot > 0) lastXp = cur; return; }   // baseline once stats have arrived
        for (int i = 0; i < 21; i++) {
            long d = cur[i] - lastXp[i];
            if (d > 0) {
                Drop dr = new Drop(); dr.skill = i; dr.amount = d;
                dr.start = Math.max(now, lastSpawn + 450); lastSpawn = dr.start;
                if (drops.size() < 12) drops.add(dr);
                lastSkill = i; lastGain = now; sessionXp += d;
                if (levelOf(cur[i]) > levelOf(lastXp[i])) { levelUpSkill = i; levelUpAt = now; }
            }
        }
        lastXp = cur;
    }

    private static int[] shareBuf; private static java.awt.image.BufferedImage shareImg;
    /** Draws into the game's own pixel buffer just before it is shown, so the overlay never flickers. */
    public static void paintBuffer(int[] buf, int w, int h, int bx, int by) {
        try {
            if (bx != 4 || by != 4 || w != 512 || h != 334 || buf == null || buf.length < w * h) return;
            if (shareImg == null || shareBuf != buf) {
                java.awt.image.DataBufferInt db = new java.awt.image.DataBufferInt(buf, w * h);
                java.awt.image.WritableRaster r = java.awt.image.Raster.createPackedRaster(db, w, h, w, new int[]{0xff0000, 0xff00, 0xff}, null);
                shareImg = new java.awt.image.BufferedImage(new java.awt.image.DirectColorModel(24, 0xff0000, 0xff00, 0xff), r, false, null);
                shareBuf = buf;
            }
            Graphics2D g = shareImg.createGraphics();
            g.translate(-bx, -by);
            paint(g, bx, by, w, h);
            g.dispose();
        } catch (Throwable t) { }
    }

    /** Paints into the game picture's coordinates (765x503). x,y,w,h = the blitted region. */
    static void paint(Graphics2D g0, int bx, int by, int bw, int bh) {
        try {
            // only the main 3D viewport blit
            Rectangle vp = new Rectangle(4, 4, 512, 334);
            Rectangle blit = new Rectangle(bx, by, bw, bh);
            if (!blit.contains(vp) && !blit.intersects(new Rectangle(380, 4, 136, 120))) return;
            poll();
            if (!enabled || !ModHook.inGame()) return;
            if (Class39_Sub11.anInt1478 != -1 || jagex.utils.SubNode.anInt1348 != -1) return;
            long now = System.currentTimeMillis();
            Graphics2D g = (Graphics2D) g0.create();
            g.setClip(vp.intersection(blit));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int right = 510;

            // level progress card
            int[][] my = ModHook.mySkills();
            if (lastSkill >= 0 && my != null && now - lastGain < CARD_MS) {
                float fade = (float) Math.min(1.0, (CARD_MS - (now - lastGain)) / 1200.0);
                int s = lastSkill, lvl = my[s][1];
                long xp = my[s][2];
                long a = xpFor(lvl), b = xpFor(lvl + 1);
                double pct = lvl >= 99 ? 1.0 : Math.max(0, Math.min(1, (xp - a) / (double) (b - a)));
                int cw = 150, ch = 40, cx = right - cw, cy = 8;
                composite(g, 0.78f * fade);
                g.setColor(new Color(0x1c1710));
                g.fillRoundRect(cx, cy, cw, ch, 8, 8);
                g.setColor(new Color(COLS[s]));
                g.fillRoundRect(cx, cy, 5, ch, 4, 4);
                composite(g, fade);
                g.setColor(new Color(0x6b5a38));
                g.drawRoundRect(cx, cy, cw, ch, 8, 8);
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                boolean up = levelUpSkill == s && now - levelUpAt < 5000;
                text(g, NAMES[s] + "  " + (up ? "LEVEL UP! " : "Lvl ") + lvl, cx + 11, cy + 14, up ? 0xffe040 : 0xf0e2b8);
                g.setFont(new Font("SansSerif", Font.PLAIN, 10));
                String rem = lvl >= 99 ? "Max level" : fmt(b - xp) + " xp to level " + (lvl + 1);
                text(g, rem, cx + 11, cy + 26, 0xd8d0b8);
                int bx2 = cx + 11, by2 = cy + 31, bw2 = cw - 20;
                g.setColor(new Color(0x00000));
                g.fillRect(bx2, by2, bw2, 5);
                g.setColor(new Color(COLS[s]).brighter());
                g.fillRect(bx2, by2, (int) Math.round(bw2 * pct), 5);
                g.setColor(new Color(0x8a7a52));
                g.drawRect(bx2, by2, bw2, 5);
                g.setFont(new Font("SansSerif", Font.BOLD, 10));
                String ps = (int) Math.floor(pct * 100) + "%";
                text(g, ps, cx + cw - 8 - g.getFontMetrics().stringWidth(ps), cy + 14, 0xf0e2b8);
            }

            // xp drops, rising from below the card
            for (int i = drops.size() - 1; i >= 0; i--) {
                Drop d = drops.get(i);
                long age = now - d.start;
                if (age < 0) continue;
                if (age > DROP_MS) { drops.remove(i); continue; }
                double t = age / (double) DROP_MS;
                float alpha = (float) (t < 0.75 ? 1.0 : 1.0 - (t - 0.75) / 0.25);
                int y = (int) (120 - 66 * t);
                composite(g, alpha);
                g.setFont(new Font("SansSerif", Font.BOLD, 13));
                String s = "+" + fmt(d.amount) + " xp";
                int w = g.getFontMetrics().stringWidth(s);
                g.setColor(new Color(COLS[d.skill]));
                g.fillOval(right - w - 22, y - 9, 9, 9);
                text(g, s, right - w - 8, y, 0xffffff);
                g.setFont(new Font("SansSerif", Font.PLAIN, 9));
                text(g, NAMES[d.skill], right - w - 22 - 6 - g.getFontMetrics().stringWidth(NAMES[d.skill]), y, 0xc8c0a8);
            }
            g.dispose();
        } catch (Throwable t) { }
    }

    /** Test helper: pretends the player had less xp so the next poll sees a gain. */
    static void test(String arg) {
        if (lastXp == null) return;
        int sk = 0; long n = 83;
        try { String[] p = arg.split(" "); if (p.length > 0 && p[0].length() > 0) sk = Integer.parseInt(p[0]) % 21; if (p.length > 1) n = Long.parseLong(p[1]); } catch (Exception e) { }
        lastXp[sk] -= n;
    }

    private static void composite(Graphics2D g, float a) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, a))));
    }

    private static void text(Graphics2D g, String s, int x, int y, int rgb) {
        g.setColor(Color.BLACK); g.drawString(s, x + 1, y + 1);
        g.setColor(new Color(rgb)); g.drawString(s, x, y);
    }

    private static String fmt(long n) { return String.format("%,d", n); }
}

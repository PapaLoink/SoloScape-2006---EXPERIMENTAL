package unpackaged;

import jagex.io.JSocket;
import jagex.utils.Cache;
import jagex.world.actors.GroundItem;

/**
 * Blending between the last two game ticks for FramePacer's in-between frames. The camera is blended where
 * the scene draw reads it (Class39_Sub5_Sub7.method589), so the live values the logic and the mouse drag
 * write are never touched. Characters' fine positions are swapped for blended ones only for the length of
 * a draw and put back afterwards. A jump of more than 2 tiles (teleport, new arrival) is not blended.
 */
public final class Interp {
    static float alpha = 1f;
    static boolean on;
    private static int yaw0, yaw1, pitch0, pitch1, camX0, camX1, camY0, camY1;

    private Interp() { }

    static void beforeTicks() {
        yaw0 = Class34.anInt605;
        pitch0 = JSocket.anInt301;
        camX0 = Class41.anInt765;
        camY0 = Class39_Sub10.anInt1431;
        Class39_Sub5_Sub4_Sub4 me = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
        if (me != null) mark(me);
        Class39_Sub5_Sub4_Sub4[] players = Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211;
        if (players != null) for (Class39_Sub5_Sub4_Sub4 m : players) if (m != null) mark(m);
        Class39_Sub5_Sub4_Sub4[] npcs = GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241;
        if (npcs != null) for (Class39_Sub5_Sub4_Sub4 m : npcs) if (m != null) mark(m);
    }

    private static void mark(Class39_Sub5_Sub4_Sub4 m) {
        m.ipX = m.anInt2275;
        m.ipY = m.anInt2301;
    }

    static void afterTicks() {
        yaw1 = Class34.anInt605;
        pitch1 = JSocket.anInt301;
        camX1 = Class41.anInt765;
        camY1 = Class39_Sub10.anInt1431;
    }

    /** How far the tick moved a value, still to come at this point between ticks. */
    private static int behind(int moved) {
        return Math.round(moved * (1f - alpha));
    }

    static int yaw() {
        if (!on) return Class34.anInt605;
        return Class34.anInt605 - behind(((yaw1 - yaw0 + 1024) & 0x7ff) - 1024) & 0x7ff;
    }

    static int pitch() {
        return on ? JSocket.anInt301 - behind(pitch1 - pitch0) : JSocket.anInt301;
    }

    static int camX() {
        int d = camX1 - camX0;
        return on && Math.abs(d) <= 500 ? Class41.anInt765 - behind(d) : Class41.anInt765;
    }

    static int camY() {
        int d = camY1 - camY0;
        return on && Math.abs(d) <= 500 ? Class39_Sub10.anInt1431 - behind(d) : Class39_Sub10.anInt1431;
    }

    static void apply() {
        on = FramePacer.active() && alpha < 1f;
        if (!on) return;
        Class39_Sub5_Sub4_Sub4 me = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
        if (me != null) blend(me);
        Class39_Sub5_Sub4_Sub4[] players = Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211;
        if (players != null) for (Class39_Sub5_Sub4_Sub4 m : players) if (m != null && m != me) blend(m);
        Class39_Sub5_Sub4_Sub4[] npcs = GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241;
        if (npcs != null) for (Class39_Sub5_Sub4_Sub4 m : npcs) if (m != null) blend(m);
    }

    private static void blend(Class39_Sub5_Sub4_Sub4 m) {
        int x = m.anInt2275, y = m.anInt2301;
        m.ipSaveX = x;
        m.ipSaveY = y;
        m.ipBlended = true;
        int dx = x - m.ipX, dy = y - m.ipY;
        if (dx > 256 || dx < -256 || dy > 256 || dy < -256) return;
        m.anInt2275 = x - behind(dx);
        m.anInt2301 = y - behind(dy);
    }

    static void restore() {
        if (!on) return;
        on = false;
        Class39_Sub5_Sub4_Sub4 me = Cache.aClass39_Sub5_Sub4_Sub4_Sub2_109;
        if (me != null) unblend(me);
        Class39_Sub5_Sub4_Sub4[] players = Class14.aClass39_Sub5_Sub4_Sub4_Sub2Array211;
        if (players != null) for (Class39_Sub5_Sub4_Sub4 m : players) if (m != null) unblend(m);
        Class39_Sub5_Sub4_Sub4[] npcs = GroundItem.aClass39_Sub5_Sub4_Sub4_Sub1Array2241;
        if (npcs != null) for (Class39_Sub5_Sub4_Sub4 m : npcs) if (m != null) unblend(m);
    }

    private static void unblend(Class39_Sub5_Sub4_Sub4 m) {
        if (!m.ipBlended) return;
        m.ipBlended = false;
        m.anInt2275 = m.ipSaveX;
        m.anInt2301 = m.ipSaveY;
    }
}

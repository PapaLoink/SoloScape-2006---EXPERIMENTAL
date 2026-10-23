package com.rs2.model.quest.impl;

import com.rs2.ServerSettings;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.player.Player;
import com.rs2.model.task.TickTask;

/** Stone circle entrance to the thieves' cave (Monk's Friend): ladder appears when you stand inside the circle. */
public final class Ring {
    static final int DOWN = 1754, UP = 1755;
    // stone circles (centre x, y). The ladder appears at the centre.
    static final int[][] CIRCLES = { {2561, 3222} };
    static final int RADIUS = 2;
    // inside the cave
    static final int ARR_X = 2561, ARR_Y = 9621;
    static final int UP_X = 2561, UP_Y = 9622;

    static final java.util.Map<Player, Long> lastPos = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Long>());
    static final java.util.Map<Player, Long> shown = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Long>());
    static final java.util.Map<Player, Integer> warned = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Integer>());
    static final java.util.Map<Player, Integer> hello = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Integer>());
    private static boolean started, caveLadder;
    static final java.util.Map<Player, Integer> lastCircle = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Integer>());

    public static synchronized void ensure() {
        if (started) return;
        try {
            World.scheduleTickTask(new TickTask(2) {
                @Override public void execute() { try { Ring.scan(); } catch (Throwable t) { t.printStackTrace(); } }
            });
            started = true;
        } catch (Throwable t) {
            if (!retrying) {
                retrying = true;
                Thread th = new Thread(() -> {
                    for (int n = 0; n < 120 && !started; n++) {
                        try { Thread.sleep(2000); } catch (InterruptedException e) { return; }
                        ensure();
                    }
                });
                th.setDaemon(true);
                th.start();
            }
        }
    }
    private static boolean retrying;

    private static int bs() {
        return ServerSettings.placeholderObjectId;
    }

    private static void spawn(int id, int x, int y, int ticks) {
        new DynamicObject(id, x, y, 0, 0, 10, bs(), ticks);
    }

    static void scan() {
        Player[] ps = World.getPlayers();
        if (ps == null) return;
        for (Player p : ps) {
            if (p == null) continue;
            int st = p.getQuestState(MonksFriendQuest.ID, true);
            Position pos = p.getPosition();
            if (st == 0) continue;   // quest not started (or reset): no ladder
            if (pos.getPlane() != 0) continue;
            for (int k = 0; k < CIRCLES.length; k++) {
                int cx = CIRCLES[k][0], cy = CIRCLES[k][1];
                if (Math.abs(pos.getX() - cx) <= RADIUS && Math.abs(pos.getY() - cy) <= RADIUS) {
                    if (ObjectManager.findDynamicObjectByIdAt(DOWN, cx, cy, 0) == null) {
                        spawn(DOWN, cx, cy, 30);
                        p.packetSender.sendGameMessage("A ladder appears in the middle of the stone circle.");
                    }
                    lastCircle.put(p, k);
                }
            }
        }
    }

    static boolean click(Player p, int id, int x, int y) {
        if (id == DOWN) {
            for (int[] c : CIRCLES) if (c[0] == x && c[1] == y) {
                if (p.getQuestState(MonksFriendQuest.ID, true) == 0) return true;
                p.moveTo(new Position(ARR_X, ARR_Y, 0));
                return true;
            }
        }
        if (id == UP && x == UP_X && y == UP_Y) {
            Integer k = lastCircle.get(p);
            int[] c = CIRCLES[k == null ? 0 : k];
            p.moveTo(new Position(c[0], c[1] - 1, 0));
            return true;
        }
        return false;
    }
}

package com.rs2.model.npc;

import com.rs2.model.Position;
import com.rs2.model.World;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * NPCs sorted into 16x16-tile cells once per tick, so a collision check looks only at NPCs nearby instead of
 * every NPC in the world (with ~2,300 NPCs and ~150 bots that scan took most of each 600 ms tick).
 * Callers still compare current positions exactly: an NPC moves at most a couple of tiles in a tick, so the
 * 3x3 cells around a point always hold everything that can be within collision distance of it.
 */
final class NpcGrid {
    private static final HashMap<Long, ArrayList<Npc>> CELLS = new HashMap<Long, ArrayList<Npc>>();
    private static int builtTick = Integer.MIN_VALUE;

    private NpcGrid() { }

    private static long key(int plane, int cellX, int cellY) {
        return (long) plane << 40 | (long) (cellX & 0xfffff) << 20 | cellY & 0xfffff;
    }

    private static void rebuild() {
        for (ArrayList<Npc> cell : CELLS.values()) cell.clear();
        for (Npc npc : World.getNpcs()) {
            if (npc == null) continue;
            Position p = npc.getPosition();
            long k = key(p.getPlane(), p.getX() >> 4, p.getY() >> 4);
            ArrayList<Npc> cell = CELLS.get(k);
            if (cell == null) CELLS.put(k, cell = new ArrayList<Npc>());
            cell.add(npc);
        }
        builtTick = World.tickCount;
    }

    /** Fills out with the NPCs that were within about 16 tiles of (x, y) on this plane at the start of the tick. */
    static void near(int plane, int x, int y, ArrayList<Npc> out) {
        if (builtTick != World.tickCount) rebuild();
        out.clear();
        int cx = x >> 4, cy = y >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                ArrayList<Npc> cell = CELLS.get(key(plane, cx + dx, cy + dy));
                if (cell != null) out.addAll(cell);
            }
        }
    }
}

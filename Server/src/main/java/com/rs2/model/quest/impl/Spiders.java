package com.rs2.model.quest.impl;

/** Lost City cellar spiders (Clock Tower black-cog room), from 274 map m40_150. */
public final class Spiders {
    private static boolean done;
    // local (x,y) in region 40,150 -> world x=2560+lx, y=9600+ly ; npc 59 (giant spider)
    private static final int[][] SP = {
        {45,37},{45,39},{46,35},{46,46},{47,37},{47,41},{47,44},{47,48},{48,35},{48,42},{49,39},{49,43}};

    // {npc id, local x, local y, level} from the 274 map (everything except the spiders above)
    private static final int[][] OTHER = {{102,2,57,0},{101,2,59,0},{101,3,61,0},{282,4,9,0},{100,4,53,0},{100,5,55,0},{282,6,5,0},{100,6,26,0},{122,6,32,0},{100,7,53,0},{47,8,20,0},{283,9,6,0},{100,9,33,0},{102,11,53,0},{47,13,12,0},{122,13,26,0},{88,13,30,0},{47,19,31,0},{224,19,55,0},{47,20,0,0},{47,20,14,0},{47,20,20,0},{47,20,26,0},{224,21,56,0},{224,21,59,0},{47,23,32,0},{224,23,59,0},{47,24,25,0},{47,24,37,0},{224,24,56,0},{114,25,8,0},{114,26,14,0},{224,26,59,0},{224,27,56,0},{114,28,6,0},{47,29,44,0},{224,29,54,0},{224,29,58,0},{47,30,1,0},{47,30,38,0},{47,31,1,0},{47,31,21,0},{224,31,56,0},{224,31,59,0},{47,34,36,0},{47,34,44,0},{47,37,4,0},{101,39,26,0},{101,41,27,0},{101,45,21,0},{47,47,15,0},{100,47,21,0},{47,48,28,0},{101,50,20,0},{47,54,51,0},{47,54,56,0},{47,55,47,0},{47,55,61,0},{47,56,33,0},{47,58,30,0}};

    public static void start() {
        Thread th = new Thread(() -> {
            for (int t = 0; t < 600 && !done; t++) {
                try { Thread.sleep(1000); } catch (InterruptedException e) { return; }
                try {
                    Object[] n = com.rs2.model.World.getNpcs();
                    int c = 0;
                    if (n != null) for (Object a : n) if (a != null) c++;
                    if (c < 500) continue;
                    spawn();
                    done = true;
                } catch (Throwable e) { }
            }
        }, "mod-spiders");
        th.setDaemon(true);
        th.start();
    }

    private static void spawn() {
        for (int[] s : SP) com.rs2.model.GameplayHelper.spawnNpc(59, 2560 + s[0], 9600 + s[1], 0, 1);
        for (int[] s : OTHER) com.rs2.model.GameplayHelper.spawnNpc(s[0], 2560 + s[1], 9600 + s[2], s[3], 1);
    }
}

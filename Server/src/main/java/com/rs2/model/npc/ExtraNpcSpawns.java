package com.rs2.model.npc;

import com.rs2.model.GameplayHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Loads additional NPC spawns from data/npcs/extra_spawns.csv (columns: npc_id,name,x,y,plane,walk).
 * The spawns come from the LostCityRS/Content May 2006 maps; see docs/audit/ for how they were chosen.
 * Delete or empty the file to turn them off.
 */
public final class ExtraNpcSpawns {
    private ExtraNpcSpawns() {}

    public static void load() {
        File file = new File("./data/npcs/extra_spawns.csv");
        if (!file.exists()) return;
        int count = 0, skipped = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] a = line.split(",");
                try {
                    int id = Integer.parseInt(a[0].trim());
                    int x = Integer.parseInt(a[a.length - 4].trim());
                    int y = Integer.parseInt(a[a.length - 3].trim());
                    int plane = Integer.parseInt(a[a.length - 2].trim());
                    int walk = Integer.parseInt(a[a.length - 1].trim());
                    if (!NpcDefinition.isDefined(id)) { skipped++; continue; }
                    GameplayHelper.spawnNpc(id, x, y, plane, walk);
                    count++;
                } catch (Exception e) {
                    skipped++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("[ExtraNpcSpawns] spawned " + count + " NPCs (" + skipped + " skipped)");
    }
}

package com.rs2.model.quest.impl;

/** Starts the world-side parts of the Monk's Friend, Clock Tower, Sheep Herder, Plague City and Biohazard quests, and loads the extra NPC spawns. */
public final class QuestExtras {
    private QuestExtras() {}

    public static void start() {
        try {
            Ring.ensure();
            Spiders.start();
            SheepHerderQuest.Ensure.start();
            PlagueCityQuest.spawnWorld();
            BiohazardQuest.setup();
            com.rs2.model.npc.ExtraNpcSpawns.load();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

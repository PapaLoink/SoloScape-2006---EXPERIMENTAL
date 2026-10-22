package com.rs2.model.quest.impl;

/** Starts the world-side parts of the Monk's Friend, Clock Tower and Sheep Herder quests. */
public final class QuestExtras {
    private QuestExtras() {}

    public static void start() {
        try {
            Ring.ensure();
            Spiders.start();
            SheepHerderQuest.Ensure.start();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

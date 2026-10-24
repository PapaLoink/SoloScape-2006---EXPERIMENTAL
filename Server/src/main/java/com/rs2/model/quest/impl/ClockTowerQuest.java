package com.rs2.model.quest.impl;

import com.rs2.model.quest.QuestScript;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.Player;
import com.rs2.model.Position;

/** Clock Tower (quest 23), ported from the 274 LostCity scripts (quest_cog). */
public final class ClockTowerQuest extends QuestScript {
    public static final int ID = 23;
    private static final int KOJO = 223;

    // items
    private static final int WHITE = 20, BLACK = 21, BLUE = 22, RED = 23, POISON = 24;
    private static final int BUCKET_WATER = 1929, BUCKET = 1925, ICE_GLOVES = 1580, COINS = 995;
    // objects
    private static final int SP_RED = 29, SP_BLACK = 30, SP_WHITE = 31, SP_BLUE = 32;
    private static final int TROUGH = 40, RAT_GATE = 39;
    // bits in bI[ID]
    private static final int B_BLUE = 1, B_BLACK = 2, B_WHITE = 4, B_RED = 8, B_RATDOOR = 16, B_COOLED = 32;
    private static final int ALL = B_BLUE | B_BLACK | B_WHITE | B_RED;

    private final QuestDlg[] kojo = new QuestDlg[3];
    private final java.util.Map<Player, Object[]> active = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Object[]>());

    public ClockTowerQuest(int n) {
        super(ID);
        super.setQuestPointReward(1);
        build();
        for (QuestDlg d : kojo) if (d != null) d.validate();
    }

    // ---------------------------------------------------------------- helpers
    private static boolean has(Player p, int item) { return p.getInventoryManager().containsItem(item); }
    private static boolean hasCog(Player p) { return has(p, WHITE) || has(p, BLACK) || has(p, BLUE) || has(p, RED); }
    private static int bits(Player p) { return p.questProgressFlags[ID]; }
    private static void setBit(Player p, int b) { p.questProgressFlags[ID] = p.questProgressFlags[ID] | b; }
    private static int placed(Player p) {
        int b = bits(p), n = 0;
        if ((b & B_BLUE) != 0) n++;
        if ((b & B_BLACK) != 0) n++;
        if ((b & B_WHITE) != 0) n++;
        if ((b & B_RED) != 0) n++;
        return n;
    }
    /** Chat message, wrapped onto further lines at word boundaries so nothing is cut off. */
    private static void msg(Player p, String s) {
        final int max = 72;
        while (s.length() > max) {
            int cut = s.lastIndexOf(' ', max);
            if (cut <= 0) cut = max;
            p.packetSender.sendGameMessage(s.substring(0, cut));
            s = s.substring(cut).trim();
        }
        p.packetSender.sendGameMessage(s);
    }

    // --------------------------------------------------------------- dialogue
    private void build() {
        kojo[0] = new QuestDlg()
            .player("Hello monk.")
            .npc("Hello adventurer. My name is Brother Kojo. Do you happen to know the time?")
            .player("No, sorry, I don't.")
            .npc("Exactly! This clock tower has recently broken down, and without it nobody can tell the correct time. I must fix it before the town people become too angry!")
            .npc("I don't suppose you could assist me in the repairs? I'll pay you for your help.")
            .label("opts")
            .options("OK old monk, what can I do?", "ok", "How much reward are we talking?", "reward", "Not now old monk.", "notnow")
            .label("reward")
            .player("So... how much reward are we talking then?")
            .npc("Well, I'm only a monk so I'm not exactly rich, but I assure you I will give you a fair reward for the time spent assisting me in repairing the clock.")
            .go("opts")
            .label("notnow")
            .npc("OK then. Come back and let me know if you change your mind.")
            .end()
            .label("ok")
            .npc("Oh, thank you kind sir! In the cellar below, you'll find four cogs. They're too heavy for me, but you should be able to carry them one at a time.")
            .npc("I know one goes on each floor... but I can't exactly remember which goes where specifically. Oh well, I'm sure you can figure it out fairly easily.")
            .player("Well, I'll do my best.")
            .act(p -> this.startQuest(p))
            .npc("Thank you again! And remember to be careful, the cellar is full of strange beasts!")
            .end();

        kojo[2] = new QuestDlg()
            .jumpIf(p -> placed(p) == 4, "all")
            .jumpIf(p -> placed(p) == 3, "three")
            .jumpIf(p -> placed(p) == 2, "two")
            .jumpIf(p -> placed(p) == 1, "one")
            .player("Hello again.")
            .npc("Oh hello, are you having trouble? The cogs are in the four rooms below us. Place one cog on a pole on each of the four tower levels.")
            .player("Right, gotcha. I'll do that then.")
            .end()
            .label("one")
            .player("I've placed a cog!")
            .npc("That's great. Come see me when you've done the other three.")
            .end()
            .label("two")
            .player("Two down!")
            .npc("Two to go.")
            .end()
            .label("three")
            .npc("One left.")
            .end()
            .label("all")
            .player("I have replaced all the cogs!")
            .npc("Really..? Wait, listen! Well done, well done! Yes yes yes, you've done it! You ARE clever!")
            .npc("The townsfolk will all be able to know the correct time now! Thank you so much for all of your help! And as promised, here is your reward!")
            .act(p -> this.awardCompletionRewards(p))
            .endKeep();

        kojo[1] = new QuestDlg()
            .player("Hello again Brother Kojo.")
            .npc("Oh hello there traveller. You've done a grand job with the clock. It's just like new.")
            .end();
    }

    @Override
    public final boolean handleNpcDialogue(Player p, int npc, int step, int option, int stage) {
        if (npc != KOJO) return false;
        QuestDlg d;
        if (step <= 1) {
            if (stage < 0 || stage >= kojo.length || kojo[stage] == null) return false;
            d = kojo[stage];
            active.put(p, new Object[]{npc, d});
        } else {
            Object[] cur = active.get(p);
            if (cur == null || ((Integer) cur[0]) != npc) return false;
            d = (QuestDlg) cur[1];
        }
        return d.run(p, step, option);
    }

    // ---------------------------------------------------------------- pickups
    /** Called when a player tries to pick up a ground item. Return true to block it. */
    @Override
    public boolean handleGroundItemInteraction(Player p, int item, int stage) {
        if (item < WHITE || item > RED) return false;
        if (stage == 0) { msg(p, "You must speak to Brother Kojo to begin this quest."); return true; }
        if (stage == 1) { msg(p, "You have already completed this quest."); return true; }
        if (hasCog(p)) { msg(p, "The cogs are too heavy to carry more than one at a time."); return true; }
        // LostCity cogs.rs2: the black cog needs ice gloves, or water poured on it first (use the bucket on the cog).
        if (item == BLACK && (bits(p) & B_COOLED) == 0) {
            if (p.getEquipmentManager().getItemIdAtSlot(9) == ICE_GLOVES) {
                setBit(p, B_COOLED);
                p.getDialogueManager().showTwoLineStatement("The ice gloves cool down the cog.", "You can carry it now.");
            } else {
                p.getDialogueManager().showTwoLineStatement("The cog is red hot from flames.", "You cannot pick it up.");
                return true;
            }
        }
        return false;
    }

    // --------------------------------------------------- use item on ground item
    /** Bucket of water on the hot black cog (cog spawn at 2613,9639); LostCity accepts only a bucket. */
    public static void useOnGround(Player p, int item) {
        if (item != BUCKET_WATER) return;
        if (p.getPosition().getPlane() != 0 || Math.abs(p.getPosition().getX() - 2613) > 3 || Math.abs(p.getPosition().getY() - 9639) > 3) return;
        int st = p.getQuestState(ID, true);
        if (st == 0) { msg(p, "You must speak to Brother Kojo to begin this quest."); return; }
        if (st == 1) { msg(p, "You have already completed this quest."); return; }
        if (hasCog(p)) { msg(p, "The cogs are too heavy to carry more than one at a time."); return; }
        p.getInventoryManager().removeItem(new ItemStack(BUCKET_WATER, 1));
        p.getInventoryManager().addItem(new ItemStack(BUCKET, 1));
        setBit(p, B_COOLED);
        p.getDialogueManager().showTwoLineStatement("You pour water over the cog.", "It quickly cools down enough to take.");
    }

    // ------------------------------------------------------- use item on object
    @Override
    public boolean handleItemOnObject(Player p, int item, int obj, int stage) {
        if (item == POISON && obj == TROUGH) {
            p.getInventoryManager().removeItem(new ItemStack(POISON, 1));
            setBit(p, B_RATDOOR);
            msg(p, "The rats swarm towards the poisoned food...");
            msg(p, "...and devour it hungrily.");
            msg(p, "You see them smashing against the gates in panic.");
            msg(p, "They seem to be dying.");
            poisonRats(p.getPosition());
            return true;
        }
        boolean cog = item >= WHITE && item <= RED;
        boolean spindle = obj >= 25 && obj <= 32;
        if (!cog || !spindle) return false;
        if (stage == 0 || stage == 1) return false;
        int bit = 0;
        if (item == RED && obj == SP_RED) bit = B_RED;
        else if (item == BLACK && obj == SP_BLACK) bit = B_BLACK;
        else if (item == WHITE && obj == SP_WHITE) bit = B_WHITE;
        else if (item == BLUE && obj == SP_BLUE) bit = B_BLUE;
        if (bit == 0) { msg(p, "The cog doesn't seem to fit."); return true; }
        if ((bits(p) & bit) != 0) { msg(p, "You have already placed a cog here."); return true; }
        setBit(p, bit);
        msg(p, "The cog fits perfectly.");
        p.getInventoryManager().removeItem(new ItemStack(item, 1));
        return true;
    }

    // ----------------------------------------------------------- poisoned rats
    // LostCity cog_dungeon_rat: each cage rat within 10 tiles runs to the gate, then back into the cage, and dies.
    private static final int CAGE_RAT = 224;
    private static final Position RAT_GATE_SPOT = new Position(2586, 9654, 0), RAT_DEATH_SPOT = new Position(2579, 9656, 0);

    private static void poisonRats(Position trough) {
        for (com.rs2.model.npc.Npc npc : com.rs2.model.World.getNpcs()) {
            if (npc == null || npc.getNpcId() != CAGE_RAT || npc.isDead() || !npc.isActive()) continue;
            if (npc.getPosition().getPlane() != trough.getPlane()
                    || com.rs2.util.GameUtil.getDistance(npc.getPosition(), trough) > 10) continue;
            dieOfPoison(npc);
        }
    }

    private static void dieOfPoison(final com.rs2.model.npc.Npc npc) {
        final com.rs2.model.npc.NpcMovementMode mode = npc.getMovementMode();
        npc.setMovementMode(com.rs2.model.npc.NpcMovementMode.STATIONARY);   // npc_setmode(none)
        com.rs2.model.combat.CombatManager.stopCombat(npc);
        npc.queuePathTo(RAT_GATE_SPOT, true);
        final int dieAt = 4 + 6 + com.rs2.util.GameUtil.randomInt(2);        // 4 ticks, then 6-7 more
        com.rs2.model.World.scheduleTickTask(new com.rs2.model.task.TickTask(1, true) {
            private int tick;
            @Override public void execute() {
                tick++;
                if (tick == 4) {
                    npc.queuePathTo(RAT_DEATH_SPOT, true);
                } else if (tick == dieAt) {
                    npc.setDead(true);
                    npc.getUpdateState().setAnimation(npc.getDeathAnimationId());
                } else if (tick > dieAt) {
                    stop();
                    npc.setMovementMode(mode);
                    // npc_del: gone without a drop, back after its normal respawn time.
                    com.rs2.model.combat.CombatManager.finishDeath(npc, null, false);
                }
            }
        });
    }

    // ------------------------------------------------------------- object clicks
    /** Option 1 on an object. */
    @Override
    public boolean handleFirstObjectAction(Player p, int id, int x, int y, int stage) {
        if ((id == 33 || id == 35) && x == 2591 && y == 9661) { pullLever(p); lever(x, y, 33, 35, 3, true); return true; }
        if ((id == 34 || id == 36) && x == 2593 && y == 9661) { pullLever(p); lever(x, y, 34, 36, 3, false); return true; }
        if (id == 1586 && x == 2575 && y == 9631) {
            msg(p, "You push the wall and it swings open.");
            int px = p.getPosition().getX();
            p.moveTo(new Position(px <= x ? x + 1 : x, p.getPosition().getY() == y ? y : y, p.getPosition().getPlane()));
            return true;
        }
        if (id == RAT_GATE) {
            if ((bits(p) & B_RATDOOR) == 0) { msg(p, "This door does not seem to be openable."); return true; }
            p.getDialogueManager().showTwoLineStatement("The death throes of the rats seem to have shaken the door",
                    "loose of its hinges. You pick it up and go through.");
            // LostCity open_and_close_door2: the gate swings open, you walk through, and it closes behind you.
            int plane = p.getPosition().getPlane();
            com.rs2.model.objects.WorldObject gate = com.rs2.model.skill.SkillActionHelper.findWorldObjectById(RAT_GATE, x, y, plane);
            int closed = gate == null ? 0 : gate.getOrientation(), type = gate == null ? 0 : gate.getType();
            p.packetSender.passThroughDoor(RAT_GATE, x, y, plane, (closed + 1) & 3, closed, type,
                    p.getPosition().getX() >= x ? -1 : 1, 0);
            p.packetSender.sendSoundEffect(GRATE_OPEN_SOUND, 1, 0);
            return true;
        }
        return false;
    }

    private static boolean gateOpen;
    private static int gateToken;
    private static int clipA, clipB;

    private static void clipAdd(int x, int y, int mask) {
        try {
            java.lang.reflect.Method m = Class.forName("com.rs2.util.path.WalkingCollisionMap").getDeclaredMethod("setTileFlag", int.class, int.class, int.class, int.class);
            m.setAccessible(true);
            m.invoke(null, x, y, 0, mask);
        } catch (Throwable t) { t.printStackTrace(); }
    }

    private static synchronized void openGate() {
        if (gateOpen) return;
        gateOpen = true;
        clipA = com.rs2.util.path.WalkingCollisionMap.getTileFlags(2595, 9657, 0);
        clipB = com.rs2.util.path.WalkingCollisionMap.getTileFlags(2596, 9657, 0);
        // Lost City: loc_del(gate) + loc_add(gate x+1, loc_1541, angle 3)
        new com.rs2.model.objects.DynamicObject(3550, 2595, 9657, 0, 2, 0, 37, 500);
        new com.rs2.model.objects.DynamicObject(1541, 2596, 9657, 0, 3, 0, 3550, 500);
        // the engine does not drop the gate's walking block by itself
        com.rs2.model.objects.ObjectManager.removeObjectCollision(37, 2595, 9657, 0, 2, 0);
        final int tok = ++gateToken;
        Thread th = new Thread(() -> {
            try { Thread.sleep(500 * 600L); } catch (InterruptedException e) { return; }
            synchronized (ClockTowerQuest.class) { if (gateOpen && tok == gateToken) restoreGate(); }
        }, "mod-gate");
        th.setDaemon(true);
        th.start();
    }

    private static synchronized void closeGate() {
        if (!gateOpen) return;
        com.rs2.model.objects.DynamicObject g1 = com.rs2.model.objects.ObjectManager.findDynamicObjectAt(2595, 9657, 0);
        if (g1 != null) g1.remainingTicks = 1;
        com.rs2.model.objects.DynamicObject g2 = com.rs2.model.objects.ObjectManager.findDynamicObjectAt(2596, 9657, 0);
        if (g2 != null) g2.remainingTicks = 1;
        restoreGate();
    }

    private static void restoreGate() {
        gateOpen = false;
        gateToken++;
        Thread th = new Thread(() -> {
            try { Thread.sleep(1800); } catch (InterruptedException e) { }
            com.rs2.model.objects.ObjectManager.removeObjectCollision(1541, 2596, 9657, 0, 3, 0);
            clipAdd(2595, 9657, clipA);
            clipAdd(2596, 9657, clipB);
        }, "mod-gate-close");
        th.setDaemon(true);
        th.start();
    }

    // LostCity seq human_leverdown and synths lever / grate_open (274 ids; the sender maps them to 443).
    private static final int LEVER_ANIM = 834, LEVER_SOUND = 319, GRATE_OPEN_SOUND = 332;

    private static void pullLever(Player p) {
        p.getUpdateState().setAnimation(LEVER_ANIM);
        p.packetSender.sendSoundEffect(LEVER_SOUND, 1, 0);
    }

    /** Rat-cage levers (Lost City quest_cog_gates_and_levers). Lever A also opens the cage gate. */
    private static void lever(int x, int y, int up, int down, int face, boolean gate) {
        com.rs2.model.objects.DynamicObject cur = com.rs2.model.objects.ObjectManager.findDynamicObjectAt(x, y, 0);
        if (cur != null) {
            cur.remainingTicks = 1;
            if (gate) closeGate();
            return;
        }
        new com.rs2.model.objects.DynamicObject(down, x, y, 0, face, 4, up, 500);
        if (gate) openGate();
    }

    // ----------------------------------------------------------------- journal
    @Override
    public final String[] buildQuestJournal(Player p, int s) {
        String start = "I spoke to Brother Kojo at the Clock Tower south of Ardougne and agreed to help him repair the clock.";
        String[] top = {"I spoke to Brother Kojo at the Clock Tower South of", "Ardougne and agreed to help him repair the clock."};
        int b = bits(p);
        switch (s) {
            case 0: return new String[]{"I can start this quest by talking to Brother Kojo at the", "Clock Tower which is located South of Ardougne"};
            case 1: return new String[]{"Quest Completed!", "", "You were awarded:", "1 Quest Point", "500 Coins"};
            default: {
                if (placed(p) == 4) return new String[]{top[0], top[1], "I have placed all four cogs successfully on the spindles.", "I should speak to Brother Kojo and claim my reward"};
                return new String[]{
                    top[0], top[1],
                    "To repair the clock I need to find the four coloured cogs",
                    "and place them on the four correctly coloured spindles.",
                    (b & B_BLUE) != 0 ? "I have successfully placed the Blue Cog on its spindle" : "I haven't placed the Blue Cog on its spindle yet",
                    (b & B_BLACK) != 0 ? "I have successfully placed the Black Cog on its spindle" : "I haven't placed the Black Cog on its spindle yet",
                    (b & B_WHITE) != 0 ? "I have successfully placed the White Cog on its spindle" : "I haven't placed the White Cog on its spindle yet",
                    (b & B_RED) != 0 ? "I have successfully placed the Red Cog on its spindle" : "I haven't placed the Red Cog on its spindle yet"};
            }
        }
    }

    // -------------------------------------------------------------- completion
    @Override
    public final void awardCompletionRewards(Player p) {
        super.markQuestComplete(p);
        super.showQuestCompleteInterface(p);
        p.packetSender.sendInterfaceText("1 Quest Point", 12150);
        p.packetSender.sendInterfaceText("500 Coins", 12151);
        p.packetSender.sendInterfaceText("", 12152);
        p.packetSender.sendInterfaceText("", 12153);
        p.packetSender.sendInterfaceText("", 12154);
        p.packetSender.sendInterfaceText("", 12155);
        p.getInventoryManager().addItem(new ItemStack(COINS, 500));
        p.packetSender.sendInterfaceModel(QuestScript.usesLegacyCompletionInterface() ? 6161 : 12145, 250, COINS);
        p.packetSender.showInterface(QuestScript.usesLegacyCompletionInterface() ? 1689 : 12140);
        p.deferLevelUpInterfaces = false;
    }
}

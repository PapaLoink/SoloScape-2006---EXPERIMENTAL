package com.rs2.model.quest.impl;

import com.rs2.model.quest.QuestScript;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.player.Player;
import com.rs2.model.task.TickTask;
import com.rs2.model.Position;

/** Sheep Herder (quest 84), ported from Lost City 274's scripts. */
public final class SheepHerderQuest extends QuestScript {
    public static final int ID = 84;
    // stages: 0 = not started, 1 = complete, 2 = told to get a suit from Orbon, 3 = disposing of the sheep
    static final int S_ORBON = 2, S_DISPOSE = 3;
    static final int HALGRIVE = 289, ORBON = 290, BRUMTY = 291;
    static final int[] SHEEP = {2345, 2346, 2347, 2348};          // plaguesheep_1..4
    static final int[] BONES = {280, 281, 282, 283};              // sheepbonesa..d
    static final int[] BIT = {1, 4, 7, 10};                       // sheepherdervar start bits
    static final int[][] PEN = {{2596, 3362}, {2597, 3363}, {2597, 3360}, {2596, 3359}};
    static final int[][][] HOME = {
        {{2609, 3344}, {2610, 3343}, {2610, 3345}},
        {{2621, 3367}, {2622, 3366}, {2623, 3367}},
        {{2560, 3389}, {2561, 3388}, {2561, 3390}},
        {{2610, 3391}, {2611, 3390}, {2612, 3391}}};
    static final int COINS = 995, PROD = 278, FEED = 279, JACKET = 284, TROUSERS = 285;
    static final int GATE_L = 166, GATE_R = 167, FURNACE = 165;
    static final int[][] GATES = {{GATE_L, 2594, 3362}, {GATE_R, 2594, 3361}};
    static final int ANIM_PROD = 799, ANIM_FURNACE = 899;

    // zones (inclusive): in pen, pen gate
    static boolean inPen(int x, int y) { return x >= 2595 && x <= 2609 && y >= 3351 && y <= 3364; }
    static boolean inGate(int x, int y) { return x >= 2592 && x <= 2594 && y >= 3360 && y <= 3363; }

    // ------------------------------------------------------------------ state
    private static int bits(Player p) { return p.questProgressFlags[ID]; }
    private static int range(Player p, int from, int to) { return (bits(p) >> from) & ((1 << (to - from + 1)) - 1); }
    private static void setRange(Player p, int val, int from, int to) {
        int mask = ((1 << (to - from + 1)) - 1) << from;
        p.questProgressFlags[ID] = (p.questProgressFlags[ID] & ~mask) | ((val << from) & mask);
    }
    private static int sheepState(Player p, int t) { return range(p, BIT[t], BIT[t] + 2); }
    private static boolean has(Player p, int item) { return p.getInventoryManager().containsItem(item); }
    private static int count(Player p, int item) { try { return p.getInventoryManager().getItemAmount(item); } catch (Throwable t) { return has(p, item) ? 1 : 0; } }
    private static void give(Player p, int item, int n) { p.getInventoryManager().addItem(new ItemStack(item, n)); }
    private static void take(Player p, int item, int n) { p.getInventoryManager().removeItem(new ItemStack(item, n)); }
    private static boolean worn(Player p, int item) { return p.getEquipmentManager().containsItem(item); }
    private static boolean suit(Player p) { return worn(p, JACKET) && worn(p, TROUSERS); }
    private static boolean haveSuit(Player p) {
        return (has(p, JACKET) || worn(p, JACKET)) && (has(p, TROUSERS) || worn(p, TROUSERS));
    }
    private static boolean allDone(Player p) { return range(p, 0, 12) == 7020; }
    private void stage(Player p, int s) { p.setQuestState(ID, s); }

    // -------------------------------------------------------------- dialogues
    private final java.util.Map<Player, Object[]> active = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Object[]>());
    private final QuestDlg[] halgrive = new QuestDlg[4], orbon = new QuestDlg[4], brumty = new QuestDlg[4];
    private final QuestDlg dNoSuit, dNoProd, dWrongTool, dProdWrong, dNotStarted, dDone, dInPen, dBonesHave, dDisposed,
        dOutside, dAlready, dPoison, dRestricted, dNoGateSuit;

    private static String t(String s) { return s.replace("|", " "); }

    public static SheepHerderQuest INSTANCE;

    public SheepHerderQuest(int n) {
        super(ID);
        INSTANCE = this;
        super.setQuestPointReward(4);   // OSRS wiki: 4 quest points
        buildHalgrive();
        buildOrbon();
        buildBrumty();
        dNotStarted = new QuestDlg().mes("The sheep looks extremely ill and you haven't any reason to interfere with it.").end();
        dDone = new QuestDlg().mes("There's no need for you to do that. You've already completed the Sheep Herder quest.").end();
        dNoSuit = new QuestDlg().player("The sheep looks extremely unwell. I don't want to touch it without a full protective suit.").end();
        dNoProd = new QuestDlg().player("I'm not prodding a sickly-looking sheep with my hands!")
            .player("I'll need to wield an appropriate tool.").end();
        dWrongTool = new QuestDlg().player("I don't want to attack the sheep. I need to make it move!")
            .player("I'll need to wield an appropriate tool.").end();
        dProdWrong = new QuestDlg().player("I think I'd do better to equip the cattleprod before I prod the sheep.").end();
        dInPen = new QuestDlg().mes("The sheep is already in the enclosure. You don't need to prod it.").end();
        dBonesHave = new QuestDlg().mes("You've already got bones from a sheep like this. You don't need to fetch another one now.").end();
        dDisposed = new QuestDlg().mes("You have already disposed of this sheep; there's no need to prod it again.").end();
        dOutside = new QuestDlg().player("I don't think I should kill the sheep outside the enclosure. The councillor was worried that it might spread the disease.").end();
        dAlready = new QuestDlg().player("I've already disposed of this sheep like this. I should find another.").end();
        dRestricted = new QuestDlg().mes("This is a restricted area. You cannot enter without protective clothing.").end();
        dNoGateSuit = new QuestDlg().player("It doesn't look very safe in there. I'm not going in without decent protective clothing.").end();
        dPoison = new QuestDlg()
            .act(p -> poisonStart(p))
            .mes("You feed the poisoned food to the sheep. It happily eats it all up. You watch as the sheep collapses dead onto the floor.")
            .act(p -> poisonKill(p))
            .end();
        for (QuestDlg d : new QuestDlg[]{dNoSuit, dNoProd, dWrongTool, dProdWrong, dNotStarted, dDone, dInPen, dBonesHave, dDisposed,
            dOutside, dAlready, dPoison, dRestricted, dNoGateSuit}) d.validate();
        for (QuestDlg[] set : new QuestDlg[][]{halgrive, orbon, brumty}) for (QuestDlg d : set) if (d != null) d.validate();
    }

    private void buildHalgrive() {
        QuestDlg more = new QuestDlg()
            .player("Hello. How are you?")
            .npc("I've been better.")
            .options("What's wrong?", "wrong", "That's life for you.", "life")
            .label("life")
            .player("That's life for you.")
            .npc("That's what concerns me... There might not be much life around here soon.")
            .end()
            .label("wrong")
            .player("What's wrong?")
            .npc(t("You may or may not be aware, but a plague has spread across West Ardougne. Now, so far, our efforts to contain it have been largely successful, for the most part."))
            .npc(t("However, four sheep recently escaped from a farm near the city. When they were found, we noticed that they were strangely discoloured, so we asked the mourners to examine them."))
            .npc("They believe that the sheep have become infected with the plague.")
            .npc("As the councillor responsible for public health and safety here in East Ardougne, I am looking for someone to herd these sheep into a safe enclosure, kill them quickly and cleanly and then dispose of the remains hygienically in a special incinerator.")
            .npc("Unfortunately nobody wants to risk catching the plague, and I am unable to find someone willing to undertake this mission for me.")
            .options("I can do that for you.", "can", "That's not a job for me.", "not")
            .label("not")
            .player("That's not a job for me.")
            .npc("I do not blame you adventurer. It is not particularly pleasant work. My problem remains however. If you know anyone suitable please send them this way; time is of the essence.")
            .end()
            .label("can")
            .player("I can do that for you.")
            .npc("Y-you will??? That is excellent news! Head to the enclosure we have set up on Farmer Brumty's land to the north of the city; the four infected sheep should still be somewhere in that vicinity.")
            .npc("Before you will be allowed to enter the enclosure, however, you must ensure you have some kind of protective clothing to prevent contagion.")
            .player("Where can I find some protective clothing then?")
            .npc("Doctor Orbon wears it when conducting mercy missions to the infected parts of the city. You should be able to find him in the chapel just north of here.")
            .npc("Please also take this poisoned sheep feed; we believe poisoning the sheep will minimise the risk of airborne contamination, and is of course also more humane to the sheep.")
            .act(p -> give(p, FEED, 1))
            .mes("The councillor gives you some poisoned sheep feed.")
            // startQuest sets stage 2 (S_ORBON) and turns the quest list entry yellow.
            .act(p -> { if (p.getQuestState(ID, true) == 0) this.startQuest(p); })
            .player("How will I know which sheep are infected?")
            .npc("The poor creatures have developed strangely discoloured wool and flesh. You should have no trouble spotting them.")
            .end();
        halgrive[0] = more;

        halgrive[1] = new QuestDlg()
            .player("Hello again sir.")
            .npc("Well hello again adventurer! How are you today?")
            .player("Fine thank you. And yourself?")
            .npc("Much better now I don't have to worry about those sheep.")
            .end();

        halgrive[2] = new QuestDlg()
            .npc("Please... please find and dispose of those four diseased sheep as quickly as possible. Every second the risk of contamination grows!")
            .npc("If you need some more protective clothing, speak to Doctor Orbon in the chapel just north of here.")
            .jumpIf(p -> !has(p, FEED), "feed")
            .player("I'll do my best sir.")
            .end()
            .label("feed")
            .player("Some more sheep poison would be appreciated...")
            .npc("Certainly adventurer. Please hurry!")
            .act(p -> give(p, FEED, 1))
            .mes("The councillor gives you some more poisoned sheep feed.")
            .end();

        halgrive[3] = new QuestDlg()
            .jumpIf(p -> allDone(p), "done")
            .jumpIf(p -> !has(p, FEED), "feed")
            .npc("Have you managed to find and dispose of those four sheep yet?")
            .player("Uh... yeah... not quite just yet...")
            .npc("Not quite's not good enough. It is vital you catch, kill, and incinerate all four sheep as quickly as possible. Each second they are free, the risk of further contagion increases.")
            .player("Ok, I'll get to it.")
            .end()
            .label("feed")
            .player("Some more sheep poison would be appreciated...")
            .npc("Certainly adventurer. Please hurry!")
            .act(p -> give(p, FEED, 1))
            .mes("The councillor gives you some more poisoned sheep feed.")
            .end()
            .label("done")
            .npc("Have you managed to find and dispose of those four sheep yet?")
            .player("Yes, I have.")
            .npc("Excellent work adventurer! Please let me reimburse you the 100 gold it cost you to purchase your protective clothing.")
            .npc("And in recognition of your service to the public health of Ardougne please accept this further 3000 coins as a reward.")
            .act(p -> this.awardCompletionRewards(p))
            .endKeep();
    }

    private void buildOrbon() {
        // purchase block shared
        orbon[0] = new QuestDlg()
            .player("Hello.")
            .npc("How do you feel? No heavy flu, shivers, nausea or loss of appetite?")
            .player("Uh... no... I feel fine...")
            .npc("How about nightmares then? Are you experiencing any problems with especially scary nightmares?")
            .player("No... not since I was young.")
            .npc("Good, good, I had to make sure. This plague really has an incredible fast contagion rate. It spreads faster than the common cold!")
            .options("The plague? Tell me more.", "more", "Ok, I'll be careful.", "careful")
            .label("careful")
            .player("Ok, I'll be careful.")
            .npc("You do that traveller. You do not want to become infected by the plague.")
            .end()
            .label("more")
            .player("The plague? Tell me more.")
            .npc("It came from the west somewhere. It is extremely quick spreading, and utterly deadly.")
            .player("What are the symptoms?")
            .npc("It varies slightly from case to case, but usually abnormal nightmares and flu-like symptoms, progressing as you succumb more.")
            .npc("Eventually it results in the presence of a thick black tar-like substance secreted from the nose and eyes.")
            .npc("Unfortunately if you reach this stage of infection then no one can save you.")
            .end();

        orbon[1] = new QuestDlg()
            .npc("Well hello again. I hear you managed to dispose of those sheep. Good work.")
            .end();

        orbon[2] = new QuestDlg()
            .player("Hi Doctor.")
            .player("I need to acquire some protective clothing so I can dispose of some diseased sheep.")
            .npc("Protective clothing? I'm afraid I only have the one suit which I made myself to prevent infection from any contaminated patients I treat.")
            .npc("I suppose I could sell you this one and make myself another, but it would cost you at least 100 gold so that I could afford a replacement.")
            .options("Ok, I'll take it.", "take", "Sorry doc, that's too much.", "toomuch")
            .label("toomuch")
            .player("Sorry doc, that's too much.")
            .npc("I'm afraid it will cost me that much to replace. I cannot possibly sell it for less.")
            .end()
            .label("take")
            .player("Ok, I'll take it.");
        buy(orbon[2]);

        orbon[3] = new QuestDlg()
            .player("Hello again.")
            .npc("Did you dispose of those infected sheep yet?")
            .player("Not yet.")
            .npc("You should hurry, they could infect the entire town in a matter of a few days!")
            .jumpIf(p -> !haveSuit(p), "nosuit")
            .end()
            .label("nosuit")
            .npc("I notice you do not appear to have your protective clothing with you. Would you care to buy some more? Same price as before.")
            .options("Ok, I'll take it.", "take", "No, I don't need anymore.", "no")
            .label("no")
            .player("No, I don't need anymore.")
            .npc("I suggest you go and retrieve your protective suit then. Trust me, you do not want to be infected.")
            .end()
            .label("take")
            .player("Ok, I'll take it.");
        buy(orbon[3]);
    }

    private void buy(QuestDlg d) {
        d.jumpIf(p -> count(p, COINS) < 100, "poor")
         .act(p -> {
             take(p, COINS, 100);
             give(p, JACKET, 1);
             give(p, TROUSERS, 1);
             if (p.getQuestState(ID, true) == S_ORBON) stage(p, S_DISPOSE);
         })
         .mes("You give Doctor Orbon 100 coins. He hands over a protective suit.")
         .npc("These should protect you from infection.")
         .end()
         .label("poor")
         .player("Oops, sorry, I don't have enough money.")
         .npc("That's ok. Just don't go near those sheep. When you can find the money come back here.")
         .end();
    }

    private void buildBrumty() {
        brumty[0] = new QuestDlg()
            .player("Hello there.")
            .npc("I have all the bad luck. My sheep all run off somewhere, and then those mourners tell me they're infected!")
            .player("Well, I hope things start to look up for you.")
            .end();
        brumty[1] = new QuestDlg()
            .player("Hello there. I'm sorry about your sheep...")
            .npc("That's ok, it had to be done for the sake of the town.")
            .end();
        brumty[2] = new QuestDlg()
            .player("Hello there. I've spoken to Councillor Halgrive. He has asked me to round up your sheep and slaughter them.")
            .npc("Well, I always knew someone would have to. Make sure you're careful about it; there should be a cattleprod in the barn you can use to herd up the sheep.")
            .end();
        brumty[3] = new QuestDlg()
            .player("Hello.")
            .npc("Hello adventurer. Be careful herding those sheep; I don't think they've wandered far, but if you touch them you risk getting infected by the plague. I suggest")
            .npc("you use the cattle prod from the barn over there to herd them, that way you won't have to touch them directly and risk infection.")
            .end();
    }

    // ---------------------------------------------------------------- journal
    @Override
    public final String[] buildQuestJournal(Player p, int s) {
        java.util.ArrayList<String> o = new java.util.ArrayList<String>();
        if (s == 0) {
            add(o, "I can start this quest by speaking to Councillor Halgrive near to the Zoo in East Ardougne.");
            return o.toArray(new String[0]);
        }
        add(o, "Councillor Halgrive asked me to dispose of four plague-bearing sheep just north of Ardougne and I accepted.");
        add(o, "He gave me some poisoned sheep feed to do this.");
        if (s == S_ORBON) {
            o.add("");
            add(o, "I need to get some protective clothing from Dr Orbon who is in the church just north of Ardougne Zoo");
        } else {
            o.add("");
            add(o, "I bought some protective clothing from Dr Orbon in the chapel north of Ardougne Zoo. I could now kill the sheep.");
        }
        if (s == 1) {
            o.add("");
            add(o, "I equipped a prod and then I used it to herd the diseased sheep to a pen where I could safely kill them and incinerate their bones.");
            o.add("");
            add(o, "I returned to let Councillor Halgrive know that the plagued sheep were no more and claimed my reward.");
            o.add("");
            o.add("QUEST COMPLETE!");
        } else if (s == S_DISPOSE) {
            String[] ord = {"first", "second", "third", "fourth"};
            if (allDone(p)) {
                o.add("");
                add(o, "I equipped a prod and then I used it to herd the diseased sheep to a pen where I could safely kill them and incinerate their bones.");
                o.add("");
                add(o, "I should return to Councillor Halgrive to collect the reward he has promised me for my hard work.");
            } else {
                for (int k = 0; k < 4; k++) {
                    o.add("");
                    int st = sheepState(p, k);
                    if (st == 6) add(o, "I have killed the " + ord[k] + " sheep and incinerated its bones.");
                    else if (st > 0) {
                        if (has(p, BONES[k])) add(o, "I have killed the " + ord[k] + " sheep. Now I must incinerate its bones.");
                        else add(o, "I have herded the " + ord[k] + " sheep to the pen. Now I must kill it safely.");
                    } else add(o, "I must find the " + ord[k] + " sheep and herd it to the pen.");
                }
            }
        }
        return o.toArray(new String[0]);
    }

    private static void add(java.util.List<String> o, String s) { for (String l : QuestDlg.wrap(s, 56)) o.add(l); }

    // ------------------------------------------------------------- completion
    @Override
    public final void awardCompletionRewards(Player p) {
        super.markQuestComplete(p);
        super.showQuestCompleteInterface(p);
        give(p, COINS, 3100);
        p.packetSender.sendInterfaceText("4 Quest Points", 12150);
        p.packetSender.sendInterfaceText("3100 Coins", 12151);
        p.packetSender.sendInterfaceText("", 12152);
        p.packetSender.sendInterfaceText("", 12153);
        p.packetSender.sendInterfaceText("", 12154);
        p.packetSender.sendInterfaceText("", 12155);
        p.packetSender.sendInterfaceModel(QuestScript.usesLegacyCompletionInterface() ? 6161 : 12145, 250, COINS);
        p.packetSender.showInterface(QuestScript.usesLegacyCompletionInterface() ? 1689 : 12140);
        p.deferLevelUpInterfaces = false;
    }

    // --------------------------------------------------------------- dialogue
    @Override
    public final boolean handleNpcDialogue(Player p, int npc, int step, int option, int stage) {
        Ensure.start();
        dbg("A npc=" + npc + " step=" + step + " stage=" + stage);
        if (step > 1 || sheepIdx(npc) >= 0 || npc == HALGRIVE) dbg("dlg npc=" + npc + " step=" + step + " opt=" + option + " stage=" + stage + " active=" + (active.get(p) != null));
        QuestDlg d;
        if (step <= 1) {
            QuestDlg[] set = npc == HALGRIVE ? halgrive : npc == ORBON ? orbon : npc == BRUMTY ? brumty : null;
            if (set != null) {
                if (stage < 0 || stage >= set.length || set[stage] == null) return false;
                d = set[stage];
            } else if (sheepIdx(npc) >= 0) {
                d = prod(p, npc);
                if (d == null) return true;      // handled without a dialogue
            } else return false;
            if (set == null) active.put(p, new Object[]{npc, d, Boolean.TRUE});
            else active.put(p, new Object[]{npc, d});
        } else {
            Object[] cur = active.get(p);
            if (cur == null) return false;
            boolean loose = cur.length > 2;
            if (((Integer) cur[0]) != npc && !(loose && npc == 0)) return false;
            d = (QuestDlg) cur[1];
        }
        return d.run(p, step <= 1 ? 1 : step, option);
    }

    static void dbg(String m) { if (System.getProperty("rs2.test") != null) System.err.println("SH " + m); }

    static int sheepIdx(int npc) { for (int k = 0; k < 4; k++) if (SHEEP[k] == npc) return k; return -1; }

    // ----------------------------------------------------- prod / poison logic
    private final java.util.Map<Player, Npc> target = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Npc>());

    private static Npc nearest(Player p, int npcId) {
        Npc best = null; int bd = 99;
        Npc[] all = com.rs2.model.World.getNpcs();
        if (all == null) return null;
        Position pp = p.getPosition();
        for (Npc n : all) {
            if (n == null || n.getNpcId() != npcId) continue;
            Position np = n.getPosition();
            if (np.getPlane() != pp.getPlane()) continue;
            int d = Math.max(Math.abs(np.getX() - pp.getX()), Math.abs(np.getY() - pp.getY()));
            if (d < bd) { bd = d; best = n; }
        }
        return bd <= 4 ? best : null;
    }

    /** Returns the dialogue to show, or null if the click was handled with no dialogue. */
    private QuestDlg prod(Player p, int npc) {
        int t = sheepIdx(npc);
        int stage = p.getQuestState(ID, true);
        if (stage == 0) return dNotStarted;
        if (stage == 1) return dDone;
        if (!suit(p)) return dNoSuit;
        int wp = p.getEquipmentManager().getItemIdAtSlot(3);
        if (wp <= 0) return dNoProd;
        if (wp != PROD) return dWrongTool;
        Npc sheep = nearest(p, npc);
        if (sheep == null) return null;
        Position sp = sheep.getPosition();
        if (inPen(sp.getX(), sp.getY())) return dInPen;
        if (has(p, BONES[t])) return dBonesHave;
        if (sheepState(p, t) == 6) return dDisposed;
        p.getUpdateState().setAnimation(ANIM_PROD);
        sheep.getUpdateState().setForcedTextAndMarkUpdated("BAAAAA!");
        // LostCity npc_setmode(none): a prodded sheep stops wandering until its next "Baa".
        sheep.setMovementMode(com.rs2.model.npc.NpcMovementMode.STATIONARY);
        Position pp = p.getPosition();
        int dx = Integer.signum(sp.getX() - pp.getX()), dy = Integer.signum(sp.getY() - pp.getY());
        int nx = sp.getX() + dx, ny = sp.getY() + dy;
        if (inGate(nx, ny)) {
            sheep.moveTo(new Position(2595, 3361, sp.getPlane()));
            final Npc s = sheep; final int tt = t; final Player pl = p;
            Sheep.later(2, () -> {
                s.moveTo(new Position(PEN[tt][0], PEN[tt][1], 0));
                // LostCity re-adds the sheep at its pen spot, so it wanders around there, not back toward home.
                Sheep.wanderAround(s, PEN[tt][0], PEN[tt][1]);
                Sheep.expire(s, 150);
                Sheep.respawnHome(tt, 60);
                setRange(pl, sheepState(pl, tt) | 1, BIT[tt], BIT[tt] + 2);
            });
            p.packetSender.sendGameMessage("The sheep obligingly jumps over the gate and into the enclosure.");
        } else {
            // LostCity npc_walk: the sheep walks one tile away from the player. If a fence or wall blocks that
            // tile, it tries the nearest other directions, so a sheep stuck in a corner moves to an open tile.
            int[][] tries = {{dx, dy}, {dx, 0}, {0, dy}, {-dy, dx}, {dy, -dx}};
            for (int[] d : tries) {
                if (d[0] == 0 && d[1] == 0) continue;
                int tx = sp.getX() + d[0], ty = sp.getY() + d[1];
                if (!inGate(tx, ty) && Sheep.walkable(sp.getX(), sp.getY(), tx, ty) && Sheep.walk(sheep, tx, ty, true)) break;
            }
        }
        return null;
    }

    @Override
    public final boolean handleItemOnNpc(Player p, int npc, int item, int slot) {
        dbg("item " + item + " on npc " + npc);
        int t = sheepIdx(npc);
        if (t < 0) return false;
        if (item == PROD) { active.put(p, new Object[]{npc, dProdWrong, Boolean.TRUE}); return dProdWrong.run(p, 1, 0); }
        if (item != FEED) return false;
        Npc sheep = nearest(p, npc);
        if (sheep == null) return false;
        Position sp = sheep.getPosition();
        QuestDlg d;
        if (!suit(p)) d = dNoSuit;                  // feeding it is as close as prodding it
        else if (!inPen(sp.getX(), sp.getY())) d = dOutside;
        else {
            int st = sheepState(p, t);
            if (st != 6 && !has(p, BONES[t])) { target.put(p, sheep); d = dPoison; }
            else d = dAlready;
        }
        active.put(p, new Object[]{npc, d, Boolean.TRUE});
        return d.run(p, 1, 0);
    }

    private void poisonStart(Player p) {
        int t = -1;
        Npc s = target.get(p);
        if (s != null) t = sheepIdx(s.getNpcId());
        if (t >= 0) setRange(p, 2, BIT[t], BIT[t] + 2);
    }

    private void poisonKill(Player p) {
        final Npc s = target.get(p);
        if (s == null) return;
        final int t = sheepIdx(s.getNpcId());
        if (t < 0) return;
        final Position pos = s.getPosition();
        s.getUpdateState().setAnimation(Sheep.deathAnim(s));
        Sheep.later(3, () -> {
            Sheep.remove(s);
            Sheep.drop(BONES[t], pos.getX(), pos.getY(), pos.getPlane());
        });
    }

    // ------------------------------------------------------- furnace and gates
    @Override
    public final boolean handleItemOnObject(Player p, int item, int obj, int slot) {
        dbg("item " + item + " on obj " + obj);
        if (obj != FURNACE) return false;
        int t = -1;
        for (int k = 0; k < 4; k++) if (BONES[k] == item) t = k;
        if (t < 0) return false;
        Position pp = p.getPosition();
        if (Math.abs(pp.getX() - 2606) > 4 || Math.abs(pp.getY() - 3361) > 4) return false;
        final int tt = t; final Player pl = p;
        p.getUpdateState().setAnimation(ANIM_FURNACE);
        p.packetSender.sendSoundEffect(FURNACE_SOUND, 1, 0);
        p.packetSender.sendGameMessage("You put the remains into the furnace.");
        take(p, item, 1);
        Sheep.later(3, () -> {
            pl.packetSender.sendGameMessage("The remains burn to dust.");
            setRange(pl, 6, BIT[tt], BIT[tt] + 2);
        });
        return true;
    }

    @Override
    public boolean handleFirstObjectAction(Player p, int id, int x, int y, int stage) {
        if (id == GATE_L || id == GATE_R || id == FURNACE) dbg("obj click " + id + " @" + x + "," + y + " stage=" + stage);
        if ((id != GATE_L && id != GATE_R) || x != 2594) return false;
        Position pp = p.getPosition();
        if (Math.abs(pp.getX() - x) > 2 || Math.abs(pp.getY() - y) > 2) return false;
        boolean entering = pp.getX() <= x;          // LostCity check_axis: the suit is only checked going in
        QuestDlg d = null;
        if (entering && stage == 0) d = dRestricted;
        else if (entering && !suit(p)) d = dNoGateSuit;
        if (d != null) {
            active.put(p, new Object[]{id, d, Boolean.TRUE});
            return d.run(p, 1, 0);
        }
        openGate(p);
        // Walk through rather than LostCity's p_teleport; gateApproach has already put the player in front of it.
        p.packetSender.queueRelativeMovementStep(entering ? 1 : -1, 0, true);
        return true;
    }

    /**
     * The pen gate sits on the east edge of x 2594: it is used from the gate tile (outside) or the tile east of it
     * (inside), in line with the half clicked. Returns that tile, or null for other objects.
     */
    public static Position gateApproach(Player p, int id, int x, int y) {
        if ((id != GATE_L && id != GATE_R) || x != 2594) return null;
        return new Position(p.getPosition().getX() <= x ? x : x + 1, y, p.getPosition().getPlane());
    }

    // LostCity loc inviswall and the swung-open gate pieces inacfishinggateclosedl/r; synths door_open and furnace.
    static final int INVISWALL = 83, OPEN_GATE_L = 49, OPEN_GATE_R = 50, DOOR_OPEN_SOUND = 318, FURNACE_SOUND = 469;

    /**
     * LostCity open_sheepherder_gate: for 3 ticks both gate halves become invisible walls and the left half shows
     * the swung-open gate inside the pen. Visual only; the player is moved through, as LostCity teleports them.
     */
    private static void openGate(Player p) {
        for (int[] g : GATES) {
            com.rs2.model.objects.WorldObject gate = com.rs2.model.skill.SkillActionHelper.findWorldObjectById(g[0], g[1], g[2], 0);
            if (gate == null || com.rs2.model.objects.ObjectManager.findDynamicObjectAt(g[1], g[2], 0) != null) continue;
            int angle = gate.getOrientation(), shape = gate.getType();
            new com.rs2.model.objects.DynamicObject(INVISWALL, g[1], g[2], 0, angle, shape, g[0], 3, false);
            if (g[0] == GATE_L) {
                new com.rs2.model.objects.DynamicObject(OPEN_GATE_L, g[1] + 1, g[2], 0, 1, shape, Ensure.bs(), 3, false);
                new com.rs2.model.objects.DynamicObject(OPEN_GATE_R, g[1] + 2, g[2], 0, 1, shape, Ensure.bs(), 3, false);
            }
        }
        p.packetSender.sendSoundEffect(DOOR_OPEN_SOUND, 1, 0);
    }

    /** Sheep-side helpers. */
    static final class Sheep {
        static final java.util.List<Object[]> timers = new java.util.ArrayList<Object[]>();
        static int tick;

        static synchronized void later(int ticks, Runnable r) {
            timers.add(new Object[]{tick + ticks, r});
        }
        static void expire(final Npc n, int ticks) { later(ticks, () -> remove(n)); }
        /** Moves the NPC out of everyone's view (so clients drop it) and then deletes it a couple of ticks later. */
        static void remove(final Npc n) {
            try {
                n.moveTo(new Position(3170, 3170, 0));
                later(3, () -> { try { com.rs2.model.World.unregisterNpc(n); } catch (Throwable t) { t.printStackTrace(); } });
            } catch (Throwable t) { t.printStackTrace(); }
        }

        static Npc spawn(int id, int x, int y) {
            try {
                com.rs2.model.GameplayHelper.spawnNpc(id, x, y, 0, 1);
                Npc r = null;
                Npc[] all = com.rs2.model.World.getNpcs();
                for (Npc n : all) if (n != null && n.getNpcId() == id && n.getPosition().getX() == x && n.getPosition().getY() == y) r = n;
                if (r != null) wanderAround(r, x, y);
                return r;
            } catch (Throwable t) { t.printStackTrace(); return null; }
        }

        /** LostCity wanderrange=3: the sheep wanders within 3 tiles of (x, y). */
        static void wanderAround(Npc n, int x, int y) {
            n.setMovementMode(com.rs2.model.npc.NpcMovementMode.ROAMING);
            n.setSpawnPosition(new Position(x, y, 0));
            n.setSpawnMinPosition(new Position(x - 3, y - 3));
            n.setSpawnMaxPosition(new Position(x + 3, y + 3));
        }
        static void respawnHome(final int t, int ticks) {
            later(ticks, () -> {
                int[] h = HOME[t][(int) (Math.random() * 3)];
                if (nearby(SHEEP[t], h[0], h[1]) == null) spawn(SHEEP[t], h[0], h[1]);
            });
        }
        static Npc nearby(int id, int x, int y) {
            Npc[] all = com.rs2.model.World.getNpcs();
            if (all != null) for (Npc n : all) if (n != null && n.getNpcId() == id && n.getPosition().getX() == x && n.getPosition().getY() == y) return n;
            return null;
        }
        static boolean solid(int x, int y, int plane) {
            return (com.rs2.util.path.WalkingCollisionMap.getTileFlags(x, y, plane) & (0x100 | 0x40000 | 0x200000)) != 0;
        }

        /** Like LostCity, sheep go anywhere collision allows; only the gate lets them into (or out of) the pen. */
        static boolean walkable(int x, int y, int nx, int ny) {
            return inPen(nx, ny) == inPen(x, y);
        }
        static boolean isPlagueSheep(int id) { return sheepIdx(id) >= 0; }

        /**
         * Walks the sheep straight toward a tile, respecting fences and walls. With wholeWay it only moves if
         * the tile itself is reachable that way (a prod step); otherwise it goes as far as it can (heading home).
         */
        static boolean walk(Npc n, int x, int y, boolean wholeWay) {
            int plane = n.getPosition().getPlane();
            com.rs2.model.path.PathResult path = new com.rs2.model.path.DirectPathStrategy().buildPath(n, new Position(x, y, plane), true);
            // The straight-line check only sees walls, so also refuse a tile filled by scenery such as a bush.
            int fx = n.getPosition().getX(), fy = n.getPosition().getY();
            boolean solid = solid(x, y, plane)
                    || (x != fx && y != fy && (solid(x, fy, plane) || solid(fx, y, plane)));   // no cutting a corner
            if (wholeWay && (!path.isSuccessful() || solid)) return false;
            n.getMovementQueue().clear();
            while (!path.getSteps().isEmpty()) {
                com.rs2.model.path.PathStep s = (com.rs2.model.path.PathStep) path.getSteps().poll();
                n.getMovementQueue().addStep(new Position(s.getX(), s.getY(), plane));
            }
            n.getMovementQueue().removeFirstStep();
            return true;
        }
        /**
         * LostCity plays npc_param(death_anim): the NPC's own death animation. That is 253 for the 2004 sheep model,
         * but the 443 sheep is a newer model (5336), and 253 plays wrongly on it, so use the definition's value.
         */
        static int deathAnim(Npc n) {
            int anim = n.getDeathAnimationId();
            return anim > 0 ? anim : DEATH;
        }
        static final int DEATH = 5336;

        static void drop(int item, int x, int y, int z) {
            try {
                com.rs2.model.ground.GroundItem gi = new com.rs2.model.ground.GroundItem(new ItemStack(item, 1), new Position(x, y, z), false);
                com.rs2.model.ground.GroundItemManager.getInstance().spawn(gi);
            } catch (Throwable t) { t.printStackTrace(); }
        }
    }

    /** World set-up: spawns Farmer Brumty and the diseased sheep, and makes the cattleprod wieldable, once the world is up. */
    static final class Ensure {
        static boolean started, done;
        static synchronized void start() {
            if (started) return;
            started = true;
            Thread th = new Thread(() -> {
                for (int t = 0; t < 600 && !done; t++) {
                    try { Thread.sleep(1000); } catch (InterruptedException e) { return; }
                    try {
                        Object[] n = com.rs2.model.World.getNpcs();
                        int c = 0;
                        if (n != null) for (Object a : n) if (a != null) c++;
                        if (c < 500) continue;
                        setup();
                        done = true;
                    } catch (Throwable e) { e.printStackTrace(); }
                }
            }, "mod-sheepherder");
            th.setDaemon(true);
            th.start();
        }

        static int bs() {
            return com.rs2.ServerSettings.placeholderObjectId;
        }

        static void setup() {
            try {   // make the cattleprod wieldable (weapon slot 3)
                java.lang.reflect.Field f = com.rs2.model.item.ItemDefinition.class.getDeclaredField("equipmentSlot");
                f.setAccessible(true);
                f.setInt(com.rs2.model.item.ItemDefinition.forId(PROD), 3);
            } catch (Throwable t) { t.printStackTrace(); }
            com.rs2.model.GameplayHelper.spawnNpc(BRUMTY, 2600, 3358, 0, 0);
            for (int t = 0; t < 4; t++) for (int[] h : HOME[t]) Sheep.spawn(SHEEP[t], h[0], h[1]);
            // The cattleprod in the barn (2604, 3357) comes from Item spawn.dat, as in LostCity's map; no extra drop here.
            com.rs2.model.World.scheduleTickTask(new TickTask(1) {
                int n;
                @Override public void execute() {
                    try {
                        java.util.List<Object[]> run = new java.util.ArrayList<Object[]>();
                        synchronized (Sheep.class) {
                            Sheep.tick++;
                            for (java.util.Iterator<Object[]> it = Sheep.timers.iterator(); it.hasNext();) {
                                Object[] e = it.next();
                                if ((Integer) e[0] <= Sheep.tick) { run.add(e); it.remove(); }
                            }
                        }
                        for (Object[] e : run) try { ((Runnable) e[1]).run(); } catch (Throwable t) { t.printStackTrace(); }
                        // LostCity ai_timer: each diseased sheep says "Baa" every 10-30 ticks (about 1 in 20 per tick)
                        // and goes back to its normal wandering (npc_setmode(null)). Wandering stays within 3 tiles
                        // of where it started, so a sheep left alone drifts back there.
                        for (Npc s : com.rs2.model.World.getNpcs()) {
                            if (s != null && Sheep.isPlagueSheep(s.getNpcId()) && com.rs2.util.GameUtil.randomInt(20) == 0) {
                                s.getUpdateState().setForcedTextAndMarkUpdated("Baa");
                                s.setMovementMode(com.rs2.model.npc.NpcMovementMode.ROAMING);
                            }
                        }
                    } catch (Throwable t) { t.printStackTrace(); }
                }
            });
        }
    }
}

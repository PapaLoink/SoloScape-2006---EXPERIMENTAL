package com.rs2.model.quest.impl;

import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.combat.CombatManager;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestScript;
import com.rs2.net.packet.ZonePacket;
import java.util.ArrayList;
import java.util.List;

/**
 * Biohazard (quest 21), ported from Lost City 274's quest_biohazard scripts and the Ardougne, Varrock and
 * combat training camp area scripts it uses (elena, jerico, omart, kilron, nurse_sarah, mourner, doors,
 * king_lathas, chemist, the three errand boys, guidor, guidors_wife, east_gate, combat_training_camp).
 */
public final class BiohazardQuest extends QuestScript {
    public static final int ID = 21;

    // LostCity %biohazard values. Our quest state is that value + 1, and 1 means complete (LostCity 16).
    static final int NOT_STARTED = 0, STARTED = 1, SPOKEN_JERICO = 2, USED_BIRDFEED = 3, RELEASED_PIGEONS = 4,
        CLIMBED_LADDER = 5, POISONED_STEW = 6, FOUND_DISTILLATOR = 7, GIVEN_DISTILLATOR = 10, SPOKEN_CHEMIST = 12,
        FOUND_SECRET = 14, REPORTED_ELENA = 15, COMPLETE = 16;
    // %bioerrand bits
    static final int HOPS_CORRECT = 1, CHANCY_CORRECT = 2, DEVINCI_CORRECT = 3, HOPS_GIVEN = 4, CHANCY_GIVEN = 5,
        DEVINCI_GIVEN = 6, HOPS_WRONG = 7, CHANCY_WRONG = 8, DEVINCI_WRONG = 9;
    // %biodummy (training camp dummy hits) lives in bits 16-19 of the same flags word
    static final int DUMMY_SHIFT = 16;

    // npcs
    static final int ELENA = 3209, JERICO = 366, OMART = 350, KILRON = 349, NURSE = 373, M_STEW1 = 371, M_STEW2 = 370,
        M_STEW3 = 369, M_ARMED = 357, M_TOWER = 372, LATHAS = 364, CHEMIST = 367, ARTIST1 = 336, ARTIST2 = 337,
        GAMBLER1 = 338, GAMBLER2 = 339, DRUNK1 = 340, DRUNK2 = 341, WIFE = 342, GUIDOR = 343, BIOGUARD = 368,
        TRAINER1 = 344, TRAINER2 = 345, TRAINER3 = 346, PLAGUE_CITY = 71;
    // pseudo npc id that opens a plain message box
    static final int BOX = 6401;
    // items
    static final int ETHENEA = 415, HONEY = 416, BROLINE = 417, SAMPLE = 418, PAPER = 419, DISTILLATOR = 420,
        BIRDFEED = 422, KEY = 423, PIGEONS = 424, CAGE = 425, PRIEST_GOWN = 426, PRIEST_ROBE = 428,
        DOCTOR_GOWN = 430, ROTTEN_APPLE = 1984;
    // locs
    static final int GUIDOR_DOOR = 2032, STEW_DOOR_UP = 2034, STEW_DOOR = 2036, DUMMY = 2038, TRAIN_GATE_L = 2039,
        TRAIN_GATE_R = 2041, CAULDRON = 2043, CITY_DOOR_L = 2048, CITY_DOOR_R = 2049, VARROCK_GATE_L = 2050,
        VARROCK_GATE_R = 2051, JERICO_CUP = 2056, JERICO_CUP_OPEN = 2057, HQ_GATE_L = 2058, HQ_GATE_R = 2060,
        NURSE_CUP = 2062, NURSE_CUP_OPEN = 2063, CRATE = 2064, LADDER = 2065, LADDER_WALL = 2066, WATCHTOWER = 2067,
        FENCE = 2068, ELENA_DOOR = 2054;
    // synths and spotanims
    static final int DOOR_OPEN = 318, GRATE_OPEN = 332, COFFIN_OPEN = 329, PIGEON_LAUNCH = 72;

    private final java.util.Map<Player, Object[]> active = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Object[]>());
    private final java.util.Map<Player, Object[]> pending = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Object[]>());

    public BiohazardQuest(int n) {
        super(ID);
        super.setQuestPointReward(3);
    }

    // ------------------------------------------------------------ helpers
    private static QuestDlg D() { return new QuestDlg(); }
    static int lc(Player p) { int s = p.getQuestState(ID, true); return s == 1 ? COMPLETE : s <= 0 ? NOT_STARTED : s - 1; }
    private void set(Player p, int v) {
        if (v == STARTED && p.getQuestState(ID, true) == 0) { this.startQuest(p); return; }
        p.setQuestState(ID, v + 1);
    }
    private static boolean bit(Player p, int b) { return (p.questProgressFlags[ID] & (1 << b)) != 0; }
    private static void setBit(Player p, int b) { p.questProgressFlags[ID] |= 1 << b; }
    private static void clearBit(Player p, int b) { p.questProgressFlags[ID] &= ~(1 << b); }
    private static int dummyHits(Player p) { return (p.questProgressFlags[ID] >> DUMMY_SHIFT) & 15; }
    private static void setDummyHits(Player p, int n) { p.questProgressFlags[ID] = (p.questProgressFlags[ID] & ~(15 << DUMMY_SHIFT)) | (n << DUMMY_SHIFT); }

    private static boolean has(Player p, int item) { return p.getInventoryManager().containsItem(item); }
    private static boolean owns(Player p, int item) { return p.ownsItem(item); }
    private static void take(Player p, int item) { p.getInventoryManager().removeItem(new ItemStack(item, 1)); }
    private static void give(Player p, int item) { p.getInventoryManager().addItem(new ItemStack(item, 1)); }
    private static boolean worn(Player p, int item) { return p.getEquipmentManager().containsItem(item); }
    private static boolean priest(Player p) { return worn(p, PRIEST_GOWN) && worn(p, PRIEST_ROBE); }
    private static int freeSlots(Player p) { return p.getInventoryManager().getContainer().getFreeSlots(); }
    private static void msg(Player p, String s) { p.packetSender.sendGameMessage(s); }
    private static void close(Player p) { p.packetSender.closeInterfaces(); }

    /**
     * LostCity player_teleport_normal and spirit_tree: every plague sample carried in the inventory is destroyed on
     * arrival. Banked samples, and Elena's three vials, are unaffected.
     */
    public static void teleportCrossing(Player p) {
        int n = p.getInventoryManager().getItemAmount(SAMPLE);
        if (n <= 0) return;
        p.getInventoryManager().removeItem(new ItemStack(SAMPLE, n));
        msg(p, "The plague sample is too delicate...it disintegrates in the crossing.");
    }

    /** Opens a scripted dialogue (door, item, object...) with the given npc's chathead through the dialogue engine. */
    private void say(Player p, int npc, QuestDlg d) {
        d.validate();
        pending.put(p, new Object[]{npc, d});
        DialogueManager.startDialogue(p, npc);
    }
    private void box(Player p, String text) { say(p, BOX, D().mes(text).end()); }

    /** LostCity p_delay: runs the action after the given number of game ticks. */
    private static void later(final Player p, int ticks, final Runnable r) {
        com.rs2.model.task.CycleEventHandler.getInstance().schedule(p, new com.rs2.model.task.CycleEvent() {
            public void execute(com.rs2.model.task.CycleEventContainer c) { c.stop(); r.run(); }
            public void onStop() { }
        }, ticks);
    }

    /** Nearest active npc with this id within r tiles on the player's plane, or null (LostCity npc_find). */
    private static Npc npcNear(Player p, int id, int r) {
        Position q = p.getPosition();
        Npc best = null; int bd = 1 << 30;
        for (Npc n : World.getNpcs()) {
            if (n == null || !n.isActive() || n.isDead() || n.getNpcId() != id) continue;
            Position np = n.getPosition();
            if (np.getPlane() != q.getPlane()) continue;
            int d = Math.max(Math.abs(np.getX() - q.getX()), Math.abs(np.getY() - q.getY()));
            if (d <= r && d < bd) { bd = d; best = n; }
        }
        return best;
    }

    /** LostCity ~npc_retaliate: the npc the player is talking to attacks them. */
    private static void retaliate(Player p, int id) {
        Npc n = npcNear(p, id, 5);
        if (n != null) CombatManager.startCombat(n, p);
    }

    private static boolean near(Player p, int x, int y, int plane, int r) {
        Position q = p.getPosition();
        return q.getPlane() == plane && Math.abs(q.getX() - x) <= r && Math.abs(q.getY() - y) <= r;
    }

    // ---------------------------------------------------------------- doors
    private static final int[][] OPEN = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};   // door_open offset per loc angle

    /** LostCity check_axis: true when the player stands on the door's own tile side ("entering"). */
    private static boolean onLocSide(Player p, int x, int y, int angle) {
        int px = p.getPosition().getX(), py = p.getPosition().getY();
        switch (angle) {
            case 0: return px >= x;
            case 1: return py <= y;
            case 2: return px <= x;
            default: return py >= y;
        }
    }

    /** LostCity open_and_close_door2: the door opens, the player walks through it and it closes behind them. */
    private static void throughDoor(final Player p, final int id, final int x, final int y, final int angle) {
        int[] o = OPEN[angle];
        int px = p.getPosition().getX(), py = p.getPosition().getY(), pl = p.getPosition().getPlane();
        boolean onA = px == x && py == y, onB = px == x + o[0] && py == y + o[1];
        if (onA || onB) {
            int dx = onA ? o[0] : -o[0], dy = onA ? o[1] : -o[1];
            p.packetSender.passThroughDoor(id, x, y, pl, (angle + 1) & 3, angle, 0, dx, dy);
            return;
        }
        int tx = onLocSide(p, x, y, angle) ? x : x + o[0], ty = onLocSide(p, x, y, angle) ? y : y + o[1];
        walkThen(p, tx, ty, () -> throughDoor(p, id, x, y, angle));
    }

    /**
     * A double gate whose two halves stand on tiles a and b along the gate line. The player passes through the
     * half in line with them: dx/dy is the step from the gate's own side to the far side.
     */
    private static void throughGate(final Player p, final int idA, final int ax, final int ay, final int idB, final int bx, final int by,
                                    final int angle, final int sound) {
        int[] o = OPEN[angle];
        int px = p.getPosition().getX(), py = p.getPosition().getY();
        boolean side = onLocSide(p, ax, ay, angle);
        int lx = o[0] != 0 ? ax : Math.max(Math.min(ax, bx), Math.min(Math.max(ax, bx), px));
        int ly = o[1] != 0 ? ay : Math.max(Math.min(ay, by), Math.min(Math.max(ay, by), py));
        int sx = side ? lx : lx + o[0], sy = side ? ly : ly + o[1];
        if (px != sx || py != sy) { walkThen(p, sx, sy, () -> throughGate(p, idA, ax, ay, idB, bx, by, angle, sound)); return; }
        p.packetSender.openDoubleDoorPair(idA, ax, ay, idB, bx, by);
        p.packetSender.sendSoundEffect(sound, 1, 0);
        p.packetSender.queueRelativeMovementStep(side ? o[0] : -o[0], side ? o[1] : -o[1], true);
    }

    /** Walks to a tile, then runs the action. Does nothing if the tile can't be reached. */
    private static void walkThen(final Player p, final int tx, final int ty, final Runnable then) {
        p.getMovementQueue().clear();
        if (!com.rs2.util.path.PathFinder.findPath(p, tx, ty, false, 0, 0)) return;
        com.rs2.model.task.CycleEventHandler.getInstance().schedule(p, new com.rs2.model.task.CycleEvent() {
            int waited = 0;
            public void execute(com.rs2.model.task.CycleEventContainer c) {
                boolean there = p.getPosition().getX() == tx && p.getPosition().getY() == ty;
                if (there && !p.isMoving()) { c.stop(); then.run(); }
                else if (++waited > 25 || (!p.isMoving() && !there)) c.stop();
            }
            public void onStop() { }
        }, 1);
    }

    // ------------------------------------------------------------ dialogues
    @Override
    public final boolean handleNpcDialogue(Player p, int npc, int step, int option, int stage) {
        QuestDlg d;
        if (step <= 1) {
            Object[] pd = pending.remove(p);
            if (pd != null && ((Integer) pd[0]) == npc) d = (QuestDlg) pd[1];
            else {
                d = talk(p, npc);
                if (d == null) return false;
                d.validate();
            }
            active.put(p, new Object[]{npc, d});
        } else {
            Object[] cur = active.get(p);
            if (cur == null || ((Integer) cur[0]) != npc) return false;
            d = (QuestDlg) cur[1];
        }
        return d.run(p, step <= 1 ? 1 : step, option);
    }

    private QuestDlg talk(Player p, int npc) {
        switch (npc) {
            case ELENA: return elena(p);
            case JERICO: return jerico(p);
            case OMART: return omart(p);
            case KILRON: return kilron(p);
            case NURSE: return nurse(p);
            case M_STEW1: return stew1(p);
            case M_STEW2: return stew2(p);
            case M_STEW3: return stew3(p);
            case LATHAS: return lathas(p);
            case CHEMIST: return chemist(p);
            case DRUNK1: return hops(p);
            case DRUNK2: return hops2(p);
            case GAMBLER1: return chancy(p);
            case GAMBLER2: return chancy2(p);
            case ARTIST1: return devinci(p);
            case ARTIST2: return devinci2(p);
            case GUIDOR: return guidor(p);
            case WIFE: return wife(p);
            case BIOGUARD: return D().npc("<p,neutral>Please don't disturb me, I've got to keep any eye out for suspicious individuals.").end();
            case TRAINER1: return D().player("<p,happy>Hello there.").npc("<p,angry>What do you want - leave us be!").end();
            case TRAINER2: return D().player("<p,happy>Hello.")
                .npc("<p,neutral>Well hello brave warrior. These ogres have been terrorising the area, they've eaten four of our children this week alone.")
                .player("<p,angry>Brutes!")
                .npc("<p,neutral>So we decided to use them for target practice. A fair punishment.")
                .player("<p,neutral>Indeed.").end();
            case TRAINER3: return D().player("<p,happy>Hello.").npc("<p,neutral>Hello soldier.")
                .player("<p,confused>I'm more of an adventurer really.")
                .npc("<p,neutral>In this day and age we're all soldiers. No time to waste gassing, Fight! Fight! Fight!").end();
            default: return null;
        }
    }

    private static QuestDlg note(String m) { return D().act(q -> msg(q, m)).end(); }

    // ---------------------------------------------------------------- Elena
    private QuestDlg elena(Player p) {
        if (p.getQuestState(PLAGUE_CITY, true) != 1) return null;   // still being rescued in Plague City
        switch (lc(p)) {
            case COMPLETE: return D()
                .player("<p,happy>Hello Elena.").npc("<p,happy>Hey, how are you?")
                .player("<p,happy>Good thanks, yourself?")
                .npc("<p,happy>Not bad, let me know when you hear from King Lathas again.")
                .player("<p,happy>Will do.").end();
            case REPORTED_ELENA: return D()
                .player("<p,happy>Hello Elena.").npc("<p,neutral>You must go see King Lathas immediately!").end();
            case FOUND_SECRET: return D()
                .npc("<p,happy>You're back! So what did Guidor say?")
                .player("<p,confused>Nothing.").npc("<p,confused>What?")
                .player("<p,confused>He said that there is no plague.")
                .npc("<p,quiz>So what, this thing has all been a big hoax?")
                .player("<p,neutral>Or maybe we're about to uncover something huge.")
                .npc("<p,neutral>Then I think this thing may be bigger than both of us.")
                .player("<p,quiz>What do you mean?")
                .npc("<p,neutral>I mean you need to go right to the top... You need to see the King of East Ardougne!")
                .act(q -> set(q, REPORTED_ELENA)).end();
            case GIVEN_DISTILLATOR: case SPOKEN_CHEMIST: return D()
                .npc("<p,quiz>What are you doing back here?")
                .optionsQuiet("I just find it hard to say goodbye sometimes.", "bye",
                    "I'm afraid I've lost some of the stuff that you gave me...", "lost",
                    "I've forgotten what I need to do.", "forgot")
                .label("bye").player("<p,sad>I just find it hard to say goodbye sometimes.")
                .npc("<p,confused>Yes... I have feelings for you too...").npc("<p,angry>Now get back to work!").end()
                .label("lost").player("<p,sad>I'm afraid I've lost some of the stuff that you gave me...")
                .npc("<p,neutral>That's alright, I've got plenty.")
                .act(q -> {
                    for (int item : new int[]{HONEY, ETHENEA, BROLINE, SAMPLE}) if (!has(q, item)) give(q, item);
                    msg(q, "Elena replaces your items.");
                })
                .npc("<p,neutral>Ok, so that's your colourless Ethenea... Some highly toxic sulphuric broline... And some bog-standard liquid honey...")
                .player("<p,happy>Great, I'll be on my way.").end()
                .label("forgot").player("<p,sad>I've forgotten what I need to do.")
                .npc("<p,neutral>Go to Rimmington and get some touch paper from the chemist. Use his errand boys to smuggle the vials into Varrock.")
                .npc("<p,neutral>Then collect the samples and take them to Guidor, my old mentor.")
                .player("<p,neutral>Ok, I'll get to it.").end();
            case FOUND_DISTILLATOR: return D()
                .npc("<p,quiz>So, have you managed to retrieve my distillator?")
                .jumpIf(q -> has(q, DISTILLATOR), "yes")
                .player("<p,sad>I'm afraid not.")
                .npc("<p,sad>Oh, you haven't... People may be dying even as we speak.").end()
                .label("yes").player("<p,happy>Yes, here it is!")
                .npc("<p,happy>You have? That's great! Now can you pass me those reaction agents please?")
                .act(q -> msg(q, "You hand Elena the distillator and an assortment of vials."))
                .player("<p,neutral>Those look pretty fancy.")
                .npc("<p,neutral>Well, yes and no. The liquid honey isn't worth much, but the others are. Especially this colourless ethenea. Be careful with the sulphuric broline, it's highly poisonous.")
                .player("<p,neutral>You're not kidding, I can smell it from here!")
                .act(q -> { close(q); later(q, 2, () -> handOver(q)); })
                .end();
            case CLIMBED_LADDER: case POISONED_STEW: return D()
                .player("<p,happy>Hello again.").npc("<p,quiz>You're back, did you find the distillator?")
                .player("<p,sad>I'm afraid not.")
                .npc("<p,sad>I can't test the samples without the distillator. Please don't give up until you find it.").end();
            case RELEASED_PIGEONS: return D()
                .player("<p,happy>Elena, I've distracted the guards at the watchtower.")
                .npc("<p,neutral>Yes, I saw. Quickly meet with Jerico's friends and cross the wall before the pigeons fly off.").end();
            case SPOKEN_JERICO: return D()
                .player("<p,happy>Hello Elena, I've spoken to Jerico.").npc("<p,quiz>Was he able to help?")
                .player("<p,neutral>He has two friends who will help me cross the wall, but first I need to distract the watch tower.")
                .npc("<p,confused>Hmm, could be tricky.").end();
            case STARTED: return D()
                .player("<p,happy>Hello Elena.").npc("<p,happy>Hello brave adventurer. Any luck finding my distillator?")
                .player("<p,sad>No, I'm afraid not.")
                .npc("<p,neutral>Speak to Jerico, he will help you to cross the wall. He lives next to the chapel.").end();
            case NOT_STARTED: return D()
                .player("<p,happy>Good day to you Elena.")
                .npc("<p,happy>You too, thanks for freeing me.")
                .npc("<p,sad>It's just a shame the mourners confiscated my equipment.")
                .player("<p,quiz>What did they take?")
                .npc("<p,neutral>My distillator. I can't test any plague samples without it. They're holding it in the mourner quarters in West Ardougne.")
                .npc("<p,neutral>I must somehow retrieve that distillator if I am to find a cure for this awful affliction.")
                .optionsQuiet("I'll try to retrieve it for you.", "try", "Well, good luck.", "luck")
                .label("luck").player("<p,neutral>Well, good luck.").npc("<p,confused>Thanks traveller.").end()
                .label("try").player("<p,neutral>I'll try to retrieve it for you.")
                .npc("<p,neutral>I was hoping you would say that. Unfortunately they discovered the tunnel and filled it in. We need another way over the wall.")
                .player("<p,quiz>Any ideas?")
                .act(q -> set(q, STARTED))
                .npc("<p,neutral>My father's friend Jerico is in communication with West Ardougne. He might be able to help us, he lives next to the chapel.").end();
            default: return D().end();   // used_birdfeed intentionally has no dialogue
        }
    }

    /** Elena tests the agents, sends the player to Guidor and hands over the vials (LostCity skips any that don't fit). */
    private void handOver(Player p) {
        msg(p, "Elena puts the agents through the distillator.");
        say(p, ELENA, D()
            .npc("<p,confused>I don't understand... the touch paper hasn't changed colour at all...")
            .npc("<p,neutral>You'll need to go and see my old mentor Guidor. He lives in Varrock. Take these vials and this sample to him.")
            .act(q -> {
                close(q);
                set(q, GIVEN_DISTILLATOR);
                take(q, DISTILLATOR);
                for (int item : new int[]{HONEY, ETHENEA, BROLINE, SAMPLE}) if (freeSlots(q) > 1) give(q, item);
                msg(q, "Elena gives you three vials and a sample in a tin container.");
                later(q, 2, () -> say(q, ELENA, D()
                    .npc("<p,neutral>But first you'll need some more touch paper. Go and see the chemist in Rimmington.")
                    .npc("<p,neutral>Just don't get into any fights, and be careful who you speak to.")
                    .npc("<p,neutral>Those vials are fragile, and plague carriers don't tend to be too popular.").end()));
            }).end());
    }

    // ---------------------------------------------------------------- Jerico
    private QuestDlg jerico(Player p) {
        int s = lc(p);
        if (s >= POISONED_STEW) return note("Jerico is busy looking for his bird feed.");
        switch (s) {
            case CLIMBED_LADDER: return D()
                .player("<p,happy>Hello again Jerico.")
                .npc("<p,neutral>So you've returned traveller. Did you get what you wanted?")
                .player("<p,neutral>Not yet.")
                .npc("<p,neutral>Omart will be waiting by the wall, in case you need to cross again.").end();
            case RELEASED_PIGEONS: return D()
                .player("<p,happy>Hello there.")
                .npc("<p,neutral>The guards are distracted by the birds, you must go now, quickly traveller.").end();
            case SPOKEN_JERICO: return D()
                .player("<p,happy>Hello Jerico, I need someway to distract the watch tower, any ideas?")
                .npc("<p,neutral>Hmmm. Nothing springs to mind.")
                .optionsQuiet("Maybe you could shout and scream, and call them away?", "shout",
                    "Maybe I could use your messenger pigeons to distract them?", "pigeons",
                    "Maybe if I'm really quiet they won't notice me?", "quiet",
                    "I can't think of anything either.", "nothing")
                .label("shout").player("<p,quiz>Maybe you could shout and scream, and call them away?")
                .npc("<p,neutral>So they chase after me?").player("<p,neutral>Yes. How quickly can you run?")
                .npc("<p,neutral>No. I don't like this idea.").end()
                .label("pigeons").player("<p,quiz>Maybe I could use your messenger pigeons to distract them?")
                .npc("<p,neutral>You might have some luck with that idea. The pigeons are around the back of my house if you want to try that.")
                .player("<p,happy>Ok, maybe I'll give it a go.").end()
                .label("quiet").player("<p,quiz>Maybe if I'm really quiet they won't notice me?")
                .npc("<p,quiz>And what stops them seeing you?").player("<p,quiz>Well... perhaps I wait till nightfall?")
                .npc("<p,quiz>There's no time for that.").end()
                .label("nothing").player("<p,quiz>I can't think of anything either.")
                .npc("<p,neutral>That's too bad.").end();
            case STARTED: return D()
                .player("<p,happy>Hello Jerico.")
                .npc("<p,neutral>Hello, I've been expecting you. Elena tells me you need to cross the wall.")
                .player("<p,neutral>That's right.")
                .npc("<p,neutral>My messenger pigeons help me communicate with friends over the wall.")
                .act(q -> set(q, SPOKEN_JERICO))
                .npc("<p,neutral>I have arranged for two friends to aid you with a rope ladder. Omart is waiting for you at the southend of the wall.")
                .npc("<p,shifty>But be careful, if the mourners catch you the punishment will be severe.")
                .player("<p,neutral>Thanks Jerico.").end();
            case NOT_STARTED: return D()
                .player("<p,happy>Hello.").npc("<p,quiz>Can I help you?").player("<p,neutral>Just passing by.").end();
            default: return D().end();
        }
    }

    // ------------------------------------------------------ Omart and Kilron
    private QuestDlg omart(Player p) {
        switch (lc(p)) {
            case FOUND_DISTILLATOR: case CLIMBED_LADDER: case POISONED_STEW: return D()
                .player("<p,happy>Hello Omart.")
                .npc("<p,happy>Hello traveller. The guards are still distracted if you wish to cross the wall.")
                .optionsQuiet("Ok, lets do it.", "go", "I'll be back soon.", "soon")
                .label("go").player("<p,neutral>Ok, lets do it.").act(q -> climb(q, 2556, 3267)).end()
                .label("soon").player("<p,neutral>I'll be back soon.")
                .npc("<p,neutral>Don't take too long, those mourners will soon be rid of those birds.").end();
            case RELEASED_PIGEONS: return D()
                .npc("<p,happy>Well done, the guards are having real trouble with those birds. You must go now traveller, it's your only chance.")
                .act(q -> {
                    close(q);
                    msg(q, "Omart calls to his associate.");
                    later(q, 5, () -> say(q, OMART, D().npc("<p,neutral>Kilron!").act(r -> {
                        close(r);
                        msg(r, "He throws one end of the rope ladder over the wall.");
                        later(r, 3, () -> say(r, OMART, D()
                            .npc("<p,neutral>You must go now traveller.")
                            .optionsQuiet("Ok, lets do it.", "go", "I'll be back soon.", "soon")
                            .label("go").player("<p,neutral>Ok, lets do it.").act(t -> climb(t, 2556, 3267)).end()
                            .label("soon").player("<p,neutral>I'll be back soon.")
                            .npc("<p,neutral>Don't take too long, those mourners will soon be rid of those birds.").end()));
                    }).end()));
                }).end();
            case SPOKEN_JERICO: case USED_BIRDFEED: return D()
                .player("<p,neutral>Omart, Jerico said you might be able to help me.")
                .npc("<p,neutral>He informed me of your problem traveller. I would be glad to help, I have a rope ladder and my associate, Kilron, is waiting on the other side.")

                .player("<p,happy>Good stuff.")
                .npc("<p,sad>Unfortunately we can't risk it with the watch tower so close. So first we need to distract the guards in the tower.")
                .player("<p,quiz>How?")
                .npc("<p,neutral>Try asking Jerico, if he's not too busy with his pigeons. I'll be waiting here for you.").end();
            case NOT_STARTED: case STARTED: return D()
                .player("<p,happy>Hello there.").npc("<p,happy>Hello.").player("<p,quiz>How are you?").npc("<p,neutral>Fine thanks.").end();
            default: return D()
                .player("<p,happy>Hello Omart.")
                .npc("<p,neutral>Hello adventurer. I'm afraid it's too risky to use the ladder again, but I believe that Edmond's working on another tunnel.").end();
        }
    }

    private QuestDlg kilron(Player p) {
        if (lc(p) >= CLIMBED_LADDER) return D()
            .player("<p,happy>Hello Kilron.")
            .npc("<p,neutral>Hello traveller. Do you need to go back over?")
            .optionsQuiet("Not yet Kilron.", "no", "Yes I do.", "yes")
            .label("no").player("<p,neutral>Not yet Kilron.").npc("<p,neutral>Okay, just give me the word.").end()
            .label("yes").player("<p,neutral>Yes I do.").npc("<p,shifty>Okay, quickly now!").act(q -> climb(q, 2559, 3267)).end();
        return D().player("<p,happy>Hello there.").npc("<p,happy>Hello.").player("<p,quiz>How are you?").npc("<p,neutral>Busy.").end();
    }

    /** LostCity biohazard_climb_ladder: the rope ladder shows on the wall for 5 ticks and the player drops down after 3. */
    private void climb(final Player p, final int x, final int y) {
        close(p);
        msg(p, "You climb up the rope ladder...");
        int[][] walls = {{2557, 3267, 3}, {2558, 3267, 1}};
        for (int[] w : walls) if (ObjectManager.findDynamicObjectAt(w[0], w[1], 0) == null)
            new DynamicObject(LADDER, w[0], w[1], 0, w[2], 10, LADDER_WALL, 5, false);
        if (lc(p) == RELEASED_PIGEONS) set(p, CLIMBED_LADDER);
        later(p, 3, () -> { p.moveTo(new Position(x, y, 0)); msg(p, "and drop down on the other side."); });
    }

    // ------------------------------------------------------------ West Ardougne
    private QuestDlg nurse(Player p) {
        int s = lc(p);
        if (s == POISONED_STEW) return D()
            .player("<p,happy>Hello nurse.").npc("<p,happy>Oh hello there.")
            .npc("<p,neutral>I'm afraid I can't stop and talk, a group of mourners have become ill with food poisoning. I need to go over and see what I can do.")
            .player("<p,shifty>Hmmm, strange that!").end();
        if (s == CLIMBED_LADDER) return D()
            .player("<p,happy>Hello nurse.").npc("<p,sad>I don't know how much longer I can cope here.")
            .player("<p,quiz>What? Is the plague getting to you?")
            .npc("<p,confused>No, strangely enough the people here don't seem to be affected. It's just the awful living conditions that is making people ill.")
            .player("<p,confused>I was under the impression that everyone here was affected.")
            .npc("<p,confused>Me too, but that doesn't seem to be the case.").end();
        return D()
            .player("<p,happy>Hello there.").npc("<p,happy>Hello my dear, how are you feeling?")
            .player("<p,happy>I'm okay thanks.")
            .npc("<p,happy>Well in that case I'd better get back to work. Take care.")
            .player("<p,happy>You too.").end();
    }

    private QuestDlg stew1(Player p) {
        if (lc(p) != POISONED_STEW) return note("The mourner doesn't feel like talking.");
        return D()
            .player("<p,neutral>Hello there.")
            .npc("<p,sad>Oh dear oh dear. I feel terrible, I think it was the stew.")
            .player("<p,neutral>You should be more careful with your ingredients.")
            .jumpIf(q -> worn(q, DOCTOR_GOWN), "doc")
            .npc("<p,sad>I need a doctor. The nurse's hut is to the south west. Go now and bring us a doctor, that's an order.").end()
            .label("doc")
            .npc("<p,sad>There is one mourner who's really sick. They're resting upstairs. You should see to them first.")
            .player("<p,neutral>Okay, I'll see what I can do.").end();
    }

    private QuestDlg stew3(Player p) {
        if (lc(p) >= COMPLETE) return D().end();
        if (!worn(p, DOCTOR_GOWN)) return D()
            .npc("<p,angry>How did you get in here? This is a restricted area!")
            .act(q -> { close(q); retaliate(q, M_STEW3); }).end();
        return D()
            .player("<p,neutral>Hello there.")
            .npc("<p,sad>Hello Doc, I feel terrible. I think it was the stew.")
            .player("<p,neutral>Be more careful with your ingredients next time.").end();
    }

    private QuestDlg stew2(Player p) {
        if (lc(p) != POISONED_STEW || !worn(p, DOCTOR_GOWN))
            return D().act(q -> { msg(q, "The mourner is sick."); later(q, 3, () -> msg(q, "He doesn't feel like talking.")); }).end();
        return D()
            .player("<p,neutral>Hello there.")
            .npc("<p,sad>You're here at last! I don't know what I've eaten but I feel like I'm on death's door.")
            .player("<p,neutral>Hmm... interesting, sounds like food poisoning.")
            .npc("<p,sad>Yes, I'd figured that out already. What can you give me to help.")
            .optionsQuiet("Just hold your breath and count to ten.", "breath",
                "The best I can do is pray for you.", "pray",
                "There's nothing I can do, it's fatal.", "fatal")
            .label("breath").player("<p,neutral>Just hold your breath and count to ten.")
            .npc("<p,confused>What? How will that help? What kind of doctor are you?")
            .player("<p,shifty>Erm... I'm new, I just started.")
            .npc("<p,angry>You're no doctor!").act(q -> { close(q); retaliate(q, M_STEW2); }).end()
            .label("pray").player("<p,neutral>The best I can do is pray for you.")
            .npc("<p,angry>Pray for me? You're no doctor... You're an imposter!").act(q -> { close(q); retaliate(q, M_STEW2); }).end()
            .label("fatal").player("<p,neutral>There's nothing I can do, it's fatal.")
            .npc("<p,shock>No, I'm too young to die! I've never even had a girlfriend.")
            .player("<p,neutral>That's life for you.")
            .npc("<p,confused>Wait a minute, where's your equipment?")
            .player("<p,shifty>It's erm... at home.")
            .npc("<p,angry>You're no doctor!").act(q -> { close(q); retaliate(q, M_STEW2); }).end();
    }

    /** LostCity ai_queue3 for mournerstew2: whoever killed the sick mourner finds the key on him. */
    @Override
    public boolean handleNpcKill(Player p, int npcId, int stage) {
        if (npcId != M_STEW2 || has(p, KEY)) return false;
        msg(p, "You search the mourner...");
        msg(p, "and find a key.");
        give(p, KEY);
        return false;
    }

    // ---------------------------------------------------------------- King Lathas
    private QuestDlg lathas(Player p) {
        if (lc(p) != REPORTED_ELENA) return note("The king is too busy to talk.");   // Underground Pass is not on this server yet
        return D()
            .player("<p,quiz>I assume that you are the King of East Ardougne?")
            .npc("<p,angry>You assume correctly, but where do you get such impertinence.")
            .player("<p,neutral>I get it from finding out that the plague is a hoax.")
            .npc("<p,confused>A hoax? I've never heard such a ridiculous thing...")
            .player("<p,neutral>I have evidence, from Guidor of Varrock.")
            .npc("<p,neutral>Ah... I see. Well then you are right about the plague. But I did it for the good of my people.")
            .player("<p,quiz>When is it ever good to lie to people like that?")
            .npc("<p,sad>When it protects them from a far greater danger, a fear too big to fathom.")
            .optionsQuiet("I don't understand...", "why", "Well I've wasted enough of my time here.", "waste")
            .label("waste").player("<p,angry>I've wasted enough of my time here.")
            .npc("<p,neutral>No time is ever wasted, thanks for all you've done.").end()
            .label("why").player("<p,confused>I don't understand...")
            .npc("<p,sad>Their King, Tyras, journeyed out to the West on a voyage of discovery. But he was captured by the Dark Lord.")
            .npc("<p,sad>The Dark Lord agreed to spare his life, but only on one condition... That he would drink from the Chalice of Eternity.")
            .player("<p,quiz>So what happened?")
            .npc("<p,sad>The chalice corrupted him. He joined forces with the Dark Lord, the embodiment of pure evil, banished all those years ago...")
            .npc("<p,sad>And so I erected this wall, not just to protect my people, but to protect all the people of RuneScape.")
            .npc("<p,sad>Now, with the King of West Ardougne, the Dark Lord has an ally on the inside.")
            .npc("<p,sad>So I'm sorry that I lied about the plague. I just hope that you can understand my reasons.")
            .player("<p,quiz>Well at least I know now, but what can we do about it?")
            .npc("<p,neutral>Nothing at the moment, I'm waiting for my scouts to come back. They will tell us how we can get through the mountains.")
            .npc("<p,quiz>When this happens, can I count on your support?")
            .player("<p,neutral>Absolutely!")
            .npc("<p,happy>Thank the gods! I give you permission to use my training area.")
            .npc("<p,happy>It's located just to the north west of Ardougne, there you can prepare for the challenge ahead.")
            .player("<p,quiz>Ok. There's just one thing I don't understand, how do you know so much about King Tyras?")
            .npc("<p,sad>How could I not do? He was my brother.")
            .act(q -> this.awardCompletionRewards(q))
            .endKeep();
    }

    // ---------------------------------------------------------------- Rimmington
    private QuestDlg chemist(Player p) {
        switch (lc(p)) {
            case SPOKEN_CHEMIST: case FOUND_SECRET: return D()
                .player("<p,happy>Hello again.").npc("<p,quiz>Oh hello, do you need more touch paper?")
                .jumpIf(q -> owns(q, PAPER), "have")
                .player("<p,happy>Yes please.").npc("<p,happy>Ok, here you go.")
                .act(q -> { give(q, PAPER); msg(q, "The chemist gives you some touch paper."); }).end()
                .label("have").player("<p,happy>No, I just wanted to say hello.")
                .npc("<p,confused>Oh.. ok then... hello.").player("<p,happy>Hi.").end();
            case GIVEN_DISTILLATOR: return D()
                .npc("<p,neutral>Sorry, I'm afraid we're just closing now. You'll have to come back another time.")
                .jumpIf(q -> !has(q, SAMPLE), "out")
                .optionsQuiet("This can't wait, I'm carrying a plague sample.", "wait", "It's ok, I'm Elena's friend.", "friend")
                .label("wait").player("<p,neutral>This can't wait, I'm carrying a plague sample that desperately needs analysis.").go("takes")
                .label("friend").player("<p,neutral>It's ok, I'm Elena's friend.")
                .npc("<p,neutral>Oh, well that's different then. Must be pretty important to come all this way.")
                .npc("<p,quiz>How's everyone doing there anyway? Wasn't there some plague scare?")
                .optionsQuiet("I need some more touch paper for this plague sample.", "plague",
                    "I just need some touch paper for a guy called Guidor.", "guidor")
                .label("plague").player("<p,neutral>That's why I'm here. I need some more touch paper for this plague sample.").go("takes")
                .label("takes")
                .act(q -> take(q, SAMPLE))
                .npc("<p,shock>You idiot! A plague sample should be confined to a lab! I'm taking it off you. I'm afraid it's the only responsible thing to do.")
                .act(q -> msg(q, "He takes the plague sample from you.")).end()
                .label("guidor").player("<p,neutral>Who knows... I just need some touch paper for a guy called Guidor.")
                .npc("<p,neutral>Guidor? This one's on me then... the poor guy. Sorry for the interrogation.")
                .npc("<p,shifty>It's just that there've been rumours of a " + (p.getGender() == 0 ? "man" : "woman") + " travelling with the plague on " + (p.getGender() == 0 ? "him" : "her") + ".")
                .npc("<p,sad>They're even doing spot checks in Varrock. It's a pharmaceutical disaster!")
                .player("<p,neutral>Oh right... so am I going to be ok carrying these three vials with me?")
                .npc("<p,neutral>With touch paper as well? You're asking for trouble. You'd better use my errand boys, outside. Give them a vial each.")
                .npc("<p,neutral>They're not the most reliable people in the world. One's a painter, one's a gambler, and one's a drunk. Still if you pay peanuts you'll get monkeys, right?")
                .npc("<p,neutral>It's better than entering Varrock with half a laboratory in your napsack.")
                .player("<p,happy>Ok, thanks for your help. I know Elena appreciates it.")
                .npc("<p,neutral>Yes well don't stand around here gassing. You'd better hurry if you want to see Guidor... He won't be around for much longer.")
                .act(q -> { set(q, SPOKEN_CHEMIST); give(q, PAPER); msg(q, "He gives you the touch paper."); }).end()
                .label("out").act(q -> msg(q, "The chemist is busy at the moment.")).end();
            default: return note("The chemist is busy at the moment.");
        }
    }

    // The three errand boys: give one a vial in Rimmington, collect it from his twin in Varrock.
    private QuestDlg giveVial(QuestDlg d, String label, int item, String itemName, int givenBit, int resultBit, String[] after) {
        d.label(label)
            .jumpIf(q -> !has(q, item), label + "_none")
            .act(q -> { take(q, item); setBit(q, givenBit); setBit(q, resultBit); msg(q, "You give him the vial of " + itemName + "."); });
        for (int k = 0; k < after.length; k++) {
            if (after[k].startsWith("P:")) d.player(after[k].substring(2)); else d.npc(after[k]);
        }
        d.end().label(label + "_none").mes("You can't give him what you don't have.").end();
        return d;
    }

    private QuestDlg hops(Player p) {
        if (bit(p, HOPS_GIVEN)) return D().npc("<p,neutral>I suppose I'd better get going. I'll meet you at the Dancing Donkey Inn.").end();
        if (lc(p) != SPOKEN_CHEMIST) return note("He isn't in a fit state to talk.");
        String[] after = {"P:<p,neutral>Ok, I'll see you in Varrock.", "<p,happy>Sure, I'm a regular at the Dancing Donkey Inn as it happens."};
        QuestDlg d = D()
            .player("<p,neutral>Hi, I've got something for you to take to Varrock.")
            .npc("<p,bored>Sounds like pretty thirsty work.")
            .player("<p,neutral>Well, there's an Inn in Varrock if you're desperate.")
            .npc("<p,shifty>Don't worry, I'm a pretty resourceful fellow you know.")
            .optionsQuiet("You give him the vial of ethenea...", "e", "You give him the vial of liquid honey...", "h",
                "You give him the vial of sulphuric broline...", "b");
        giveVial(d, "e", ETHENEA, "ethenea", HOPS_GIVEN, HOPS_WRONG, after);
        giveVial(d, "h", HONEY, "liquid honey", HOPS_GIVEN, HOPS_WRONG, after);
        giveVial(d, "b", BROLINE, "sulphuric broline", HOPS_GIVEN, HOPS_CORRECT, after);
        return d;
    }

    private QuestDlg hops2(Player p) {
        if (bit(p, HOPS_CORRECT)) return D()
            .player("<p,happy>Hello, how was your journey?").npc("<p,neutral>Pretty thirst-inducing actually...")
            .player("<p,confused>Please tell me that you haven't drunk the contents...")
            .npc("<p,shock>Oh the gods no! What do you take me for?")
            .npc("<p,neutral>Here's your vial anyway.")
            .act(q -> { close(q); msg(q, "He gives you the vial of sulphuric broline."); give(q, BROLINE); clearBit(q, HOPS_GIVEN); clearBit(q, HOPS_CORRECT);
                later(q, 2, () -> say(q, DRUNK2, D().player("<p,neutral>Thanks, I'll let you get your drink now.").end())); })
            .end();
        if (bit(p, HOPS_WRONG)) return D()
            .player("<p,happy>Hello, how was your journey?").npc("<p,neutral>Pretty thirst-inducing actually...")
            .player("<p,confused>Please tell me that you haven't drunk the contents...")
            .act(q -> { clearBit(q, HOPS_GIVEN); clearBit(q, HOPS_WRONG); })
            .npc("<p,shifty>Of course I can tell you that I haven't drunk the contents...")
            .npc("<p,shifty>But I'd be lying. Sorry about that me old mucker, can I get you a drink?")
            .player("<p,angry>No, I think you've done enough for now.").end();
        return note("He isn't in a fit state to talk.");
    }

    private QuestDlg chancy(Player p) {
        if (bit(p, CHANCY_GIVEN)) return D().npc("<p,neutral>Look, I've got your vial but I'm not taking two. I always like to play the percentages.").end();
        if (lc(p) != SPOKEN_CHEMIST) return note("Chancy doesn't feel like talking.");
        String[] after = {"P:<p,neutral>Right. I'll see you later in the Dancing Donkey Inn.", "<p,happy>Be lucky!"};
        QuestDlg d = D()
            .player("<p,neutral>Hello, I've got a vial for you to take to Varrock.")
            .npc("<p,shock>Tssch... that chemist asks for a lot for the wages he pays.")
            .player("<p,neutral>Maybe you should ask him for more money.")
            .npc("<p,shifty>Nah... I just use my initiative here and there.")
            .optionsQuiet("You give him the vial of ethenea...", "e", "You give him the vial of liquid honey...", "h",
                "You give him the vial of sulphuric broline...", "b");
        giveVial(d, "e", ETHENEA, "ethenea", CHANCY_GIVEN, CHANCY_WRONG, after);
        giveVial(d, "h", HONEY, "liquid honey", CHANCY_GIVEN, CHANCY_CORRECT, after);
        giveVial(d, "b", BROLINE, "sulphuric broline", CHANCY_GIVEN, CHANCY_WRONG, after);
        return d;
    }

    private QuestDlg chancy2(Player p) {
        if (bit(p, CHANCY_CORRECT)) return D()
            .player("<p,happy>Hi, thanks for doing that.").npc("<p,happy>No problem.")
            .act(q -> { close(q); msg(q, "He gives you the vial of liquid honey."); give(q, HONEY); clearBit(q, CHANCY_GIVEN); clearBit(q, CHANCY_CORRECT);
                later(q, 2, () -> say(q, GAMBLER2, D()
                    .npc("<p,sad>Next time give me something more valuable... I couldn't get anything for this on the blackmarket.")
                    .player("<p,neutral>That was the idea.").end())); })
            .end();
        if (bit(p, CHANCY_WRONG)) return D()
            .player("<p,happy>Hi, thanks for doing that.").npc("<p,happy>No problem. I've got some money for you actually.")
            .player("<p,confused>What do you mean?")
            .npc("<p,shifty>Well, it turns out that potion you gave me, was quite valuable...")
            .player("<p,shock>What?")
            .act(q -> { clearBit(q, CHANCY_GIVEN); clearBit(q, CHANCY_WRONG); })
            .npc("<p,shifty>I know that I probably shouldn't have sold it... but some friends and I were having a little wager, the odds were just too good!")
            .player("<p,angry>You sold my vial and gambled with the money?!")
            .npc("<p,shifty>Actually yes... but praise be to Saradoming because I won! So all's well that ends well right?")
            .optionsQuiet("No! Nothing could be further from the truth!", "no", "You have no idea what you have just done!", "idea")
            .label("no").player("<p,angry>No! Nothing could be further from the truth!").npc("<p,neutral>Well, there's no pleasing some people.").end()
            .label("idea").player("<p,angry>You have no idea what you have just done!").npc("<p,neutral>Ignorance is bliss I'm afraid.").end();
        return note("Chancy doesn't feel like talking.");
    }

    private QuestDlg devinci(Player p) {
        if (bit(p, DEVINCI_GIVEN)) return D().npc("<p,neutral>Oh, it's you again. Please don't distract me now, I'm contemplating the sublime.").end();
        if (lc(p) != SPOKEN_CHEMIST) return note("Da Vinci does not feel sufficiently moved to talk.");
        String[] after = {"P:<p,neutral>Ok, we're meeting at the Dancing Donkey in Varrock right?.", "<p,happy>That's right."};
        QuestDlg d = D()
            .player("<p,neutral>Hello, I hear you're an errand boy for the chemist.")
            .npc("<p,quiz>Well that's my job yes. But I don't necessarily define my identity in such black and white terms.")
            .player("<p,neutral>Good for you. Now can you take a vial to Varrock for me?")
            .npc("<p,shifty>Go on then.")
            .optionsQuiet("You give him the vial of ethenea...", "e", "You give him the vial of liquid honey...", "h",
                "You give him the vial of sulphuric broline...", "b");
        giveVial(d, "e", ETHENEA, "ethenea", DEVINCI_GIVEN, DEVINCI_CORRECT, after);
        giveVial(d, "h", HONEY, "liquid honey", DEVINCI_GIVEN, DEVINCI_WRONG, after);
        giveVial(d, "b", BROLINE, "sulphuric broline", DEVINCI_GIVEN, DEVINCI_WRONG, after);
        return d;
    }

    private QuestDlg devinci2(Player p) {
        if (bit(p, DEVINCI_CORRECT)) return D()
            .npc("<p,happy>Hello again. I hope your journey was as pleasant as mine.")
            .player("<p,happy>Well, as they say, it's always sunny in RuneScape.")
            .npc("<p,happy>Ok, here it is.")
            .act(q -> { msg(q, "He gives you the vial of ethenea."); give(q, ETHENEA); clearBit(q, DEVINCI_GIVEN); clearBit(q, DEVINCI_CORRECT); })
            .player("<p,happy>Thanks, you've been a big help.").end();
        if (bit(p, DEVINCI_WRONG)) return D()
            .npc("<p,happy>Hello again. I hope your journey was as pleasant as mine.")
            .player("<p,happy>Yep. Anyway, I'll take the package off you now.")
            .npc("<p,quiz>Package? That's a funny way to describe a liquid of such exquisite beauty!")
            .act(q -> { clearBit(q, DEVINCI_GIVEN); clearBit(q, DEVINCI_WRONG); })
            .optionsQuiet("I'm getting a bad feeling about this.", "bad", "Just give me the stuff now please.", "now")
            .label("bad").player("<p,sad>I'm getting a bad feeling about this.").go("still")
            .label("now").player("<p,neutral>Just give me the stuff now please.")
            .label("still").player("<p,quiz>You do still have it don't you?")
            .npc("<p,happy>Absolutely. It's just not stored in a vial anymore.")
            .player("<p,confused>What?")
            .npc("<p,happy>Instead it has been liberated. It now gleams from the canvas of my latest epic: The Majesty of Varrock!")
            .player("<p,sad>That's great. Thanks to you I'll have to walk back to East Ardougne to get another vial.")
            .npc("<p,happy>Well you can't put a price on art.").end();
        return note("Da Vinci does not feel sufficiently moved to talk.");
    }

    // ---------------------------------------------------------------- Guidor
    private QuestDlg wife(Player p) {
        int s = lc(p);
        if (s >= REPORTED_ELENA) return D()
            .player("<p,neutral>Hello.")
            .npc("<p,sad>Oh hello, I can't chat now. I have to keep an eye on my husband. He's very ill!")
            .player("<p,sad>I'm sorry to hear that!").end();
        if (s == FOUND_SECRET) return D()
            .player("<p,neutral>Hello again.").npc("<p,sad>Hello there. I fear Guidor may not be long for this world!").end();
        if (s == SPOKEN_CHEMIST) {
            if (priest(p)) return D()
                .npc("<p,sad>Father, thank heavens you're here! My husband is very ill! Perhaps you could read him his final rites?")
                .player("<p,neutral>I'll see what I can do.").end();
            return D()
                .player("<p,neutral>Hello, I'm a friend of Elena, here to see Guidor.")
                .npc("<p,sad>I'm afraid...(she sobs)...that Guidor is not long for this world! So I'm not letting people see him now.")
                .player("<p,neutral>I'm really sorry to hear about Guidor.")
                .player("<p,angry>But I do have some very important business to attend to!")
                .npc("<p,angry>You heartless rogue! What could be more important than Guidor's life? A life spent well, if not always wisely... I just hope that Saradomin shows mercy on his soul!")
                .player("<p,confused>Guidor is a religious man?")
                .npc("<p,sad>Oh god no! But I am! If only I could get him to see a priest!").end();
        }
        return D().npc("<p,sad>Oh dear! Oh dear! I don't have time to chat!").end();
    }

    private QuestDlg guidor(Player p) {
        int s = lc(p);
        if (s == FOUND_SECRET || s == REPORTED_ELENA || s == COMPLETE) return D()
            .player("<p,happy>Hello again Guidor.")
            .npc("<p,neutral>Well, hello traveller. I still can't understand why they would lie about the plague.")
            .player("<p,neutral>It's strange, anyway how are you doing?")
            .npc("<p,neutral>I'm hanging in there.").player("<p,neutral>Good for you.").end();
        if (s != SPOKEN_CHEMIST) return D().end();
        return D()
            .player("<p,neutral>Hello, you must be Guidor. I understand that you are unwell.")
            .npc("<p,angry>Is my wife asking priests to visit me now? I'm a man of science for god's sake.")
            .npc("<p,neutral>Ever since she heard rumors of a plague carrier travelling from Ardougne she's kept me under house arrest.")
            .npc("<p,sad>Of course she means well, and I am quite frail now... So what brings you here?")
            .optionsQuiet("I've come to ask your assistance in stopping a plague.", "plague",
                "I was just going to bless your room and I've done that now.", "bless")
            .label("bless").player("<p,neutral>Oh, nothing, I was just going to bless your room and I've done that now. Goodbye.").end()
            .label("plague").player("<p,neutral>Well it's funny you should ask actually... I've come to ask your assistance in stopping a plague that could kill thousands.")
            .npc("<p,shock>So you're the plague carrier!")
            .optionsQuiet("No! Well, yes...", "yes", "I've been sent by your old pupil Elena.", "elena")
            .label("yes").player("<p,neutral>No! Well, yes... but not exactly. It's contained in a sealed unit from Elena.").go("elena2")
            .label("elena").player("<p,neutral>I've been sent by your old pupil Elena, she's trying to halt the virus.")
            .label("elena2").npc("<p,quiz>Elena eh?")
            .player("<p,neutral>Yes, she wants you to analyse it. You might be the only one who can help.")
            .npc("<p,happy>Right then, sounds like we'd better get to work!")
            .jumpIf(q -> !has(q, SAMPLE), "nosample")
            .player("<p,happy>I have the plague sample.")
            .npc("<p,neutral>Now I'll be needing some liquid honey, some sulphuric broline, and then...")
            .player("<p,happy>... some ethenea?").npc("<p,happy>Indeed!")
            .jumpIf(q -> !has(q, BROLINE) || !has(q, HONEY) || !has(q, ETHENEA), "noreagents")
            .jumpIf(q -> !has(q, PAPER), "nopaper")
            .act(q -> { for (int item : new int[]{BROLINE, HONEY, ETHENEA, SAMPLE, PAPER}) take(q, item); set(q, FOUND_SECRET); })
            .npc("<p,neutral>Now I'll just apply these to the sample and... I don't get it... the touch paper has remained the same.")
            .optionsQuiet("That's why Elena wanted you to do it.", "why", "So what does that mean exactly?", "mean")
            .label("why").player("<p,neutral>That's why Elena wanted you to do it, because she wasn't sure what was happening.")
            .npc("<p,neutral>Well that's just it, nothing has happened.").go("result")
            .label("mean").player("<p,quiz>So what does that mean exactly?")
            .label("result").npc("<p,neutral>I don't know what this sample is, but it certainly isn't toxic.")
            .player("<p,quiz>So what about the plague?")
            .npc("<p,angry>Don't you understand? There is no Plague!")
            .npc("<p,neutral>I'm very sorry, I can see that you've worked very hard for this... ... but it seems that someone has been lying to you.")
            .npc("<p,confused>The only question is... ... why?").end()
            .label("nosample").npc("<p,neutral>Seems like you don't actually HAVE the plague sample. It's a long way to come empty-handed... and quite a long way back to.").end()
            .label("noreagents").npc("<p,neutral>Look, I need all three reagents to test the plague sample. Come back when you've got them.").end()
            .label("nopaper").npc("<p,neutral>Oh. You don't have any touch paper, and so I won't be able to help after all.").end();
    }

    // ---------------------------------------------------------------- objects
    @Override
    public boolean handleFirstObjectAction(Player p, int id, int x, int y, int stage) {
        int s = lc(p);
        switch (id) {
            case JERICO_CUP: case NURSE_CUP: {
                ObjectManager.getInstance().removeDynamicObjectAt(x, y, p.getPosition().getPlane(), 0);
                ObjectManager.getInstance().addDynamicObject(new DynamicObject(id + 1, x, y, p.getPosition().getPlane(), 1, 10, id, 999999999), false);
                return true;
            }
            case JERICO_CUP_OPEN: {
                say(p, BOX, D()
                    .item(BIRDFEED, "The cupboard is full of birdfeed.")
                    .jumpIf(q -> lc(q) < SPOKEN_JERICO, "early")
                    .jumpIf(q -> has(q, BIRDFEED) || lc(q) >= USED_BIRDFEED, "enough")
                    .jumpIf(q -> q.ownsItem(BIRDFEED), "bank")   // not in the inventory, so it's in the bank
                    .player("<p,neutral>Mmm, birdfeed! Now what could I do with that?")
                    .act(q -> give(q, BIRDFEED)).end()
                    .label("early").player("<p,neutral>I guess pigeons really love this stuff.").end()
                    .label("enough").player("<p,neutral>I don't need any more birdfeed.").end()
                    .label("bank").player("<p,neutral>I shouldn't take any more of Jerico's birdfeed. I've already got enough in the bank.").end());
                return true;
            }
            case NURSE_CUP_OPEN: {
                msg(p, "You search the cupboard...");
                if (!owns(p, DOCTOR_GOWN) && s >= POISONED_STEW) { msg(p, "and find a doctors' gown."); give(p, DOCTOR_GOWN); }
                else msg(p, "but you find nothing of interest.");
                return true;
            }
            case WATCHTOWER: {
                if (npcNear(p, M_TOWER, 5) != null) say(p, M_TOWER, D()
                    .npc("<p,neutral>Keep away civilian.").player("<p,neutral>What's it to you?")
                    .npc("<p,neutral>This tower's here for your protection.").end());
                return true;
            }
            case STEW_DOOR: {
                int angle = y == 3320 ? 1 : 3;
                if (!onLocSide(p, x, y, angle)) { throughDoor(p, id, x, y, angle); return true; }
                if (s == POISONED_STEW || s == FOUND_DISTILLATOR) {
                    if (worn(p, DOCTOR_GOWN)) say(p, M_ARMED, D().npc("<p,neutral>In you go doc.").act(q -> { close(q); throughDoor(q, id, x, y, angle); }).end());
                    else say(p, M_ARMED, D().npc("<p,neutral>Stay away from there.").player("<p,quiz>Why?")
                        .npc("<p,neutral>Several mourners are ill with food poisoning, we're waiting for a doctor.").end());
                } else if (s >= CLIMBED_LADDER) box(p, "The door is locked. You can hear the mourners eating... You need to distract them from their stew.");
                else say(p, M_ARMED, D().npc("<p,angry>Who are you? Go away!").end());
                return true;
            }
            case STEW_DOOR_UP: {
                if (!onLocSide(p, x, y, 0) && !worn(p, DOCTOR_GOWN)) { msg(p, "The mourner is refusing to open the door."); return true; }
                throughDoor(p, id, x, y, 0);
                return true;
            }
            case HQ_GATE_L: case HQ_GATE_R: {
                if (onLocSide(p, x, y, 2) && !has(p, KEY)) {
                    msg(p, "The gate is locked.");
                    later(p, 3, () -> msg(p, "You need a key."));
                    return true;
                }
                throughGate(p, HQ_GATE_L, 2551, 3326, HQ_GATE_R, 2551, 3325, 2, GRATE_OPEN);
                return true;
            }
            case CRATE: {
                msg(p, "You search the crate.");
                later(p, 3, () -> {
                    if (owns(p, DISTILLATOR) || lc(p) > FOUND_DISTILLATOR) { msg(p, "It's empty."); return; }
                    if (lc(p) == POISONED_STEW) set(p, FOUND_DISTILLATOR);
                    give(p, DISTILLATOR);
                    msg(p, "and find Elena's distillator.");
                });
                return true;
            }
            case FENCE: return climbFence(p, x, y);
            case CITY_DOOR_L: case CITY_DOOR_R: {
                msg(p, "You pull on the large wooden doors...");
                if (s != COMPLETE) { later(p, 1, () -> msg(p, "...But they will not open.")); return true; }
                msg(p, "...You open them and walk through.");
                p.packetSender.sendSoundEffect(COFFIN_OPEN, 1, 0);
                int px = p.getPosition().getX();
                p.packetSender.queueRelativeMovementStep(px < 2557 ? 2559 - px : 2556 - px, 0, true);
                return true;
            }
            case ELENA_DOOR: {   // LostCity area_ardougne_east elenadoor2: locked from both sides until Plague City is done
                if (p.getQuestState(PLAGUE_CITY, true) != 1) { msg(p, "The door is locked."); return true; }
                throughDoor(p, id, x, y, 3);
                return true;
            }
            case GUIDOR_DOOR: {
                if (!onLocSide(p, x, y, 2)) { throughDoor(p, id, x, y, 2); return true; }
                if (s < SPOKEN_CHEMIST) {
                    say(p, BOX, D().player("<p,neutral>That's someone's bedroom. I'm not going in there without a reason.").end());
                    return true;
                }
                if (priest(p)) { msg(p, "Guidor's wife allows you to go in."); throughDoor(p, id, x, y, 2); return true; }
                msg(p, "Guidor's wife refuses to let you enter.");
                if (npcNear(p, WIFE, 5) != null) say(p, WIFE, D()
                    .npc("<p,sad>Please leave my husband alone. He's very sick, and I don't want anyone bothering him.")
                    .player("<p,quiz>I'm sorry to hear that. Is there anything I can do?")
                    .npc("<p,sad>Thank you, but I just want him to see a priest.")
                    .player("<p,neutral>A priest? Hmmm...").end());
                return true;
            }
            case VARROCK_GATE_L: case VARROCK_GATE_R:
                return varrockGate(p, x, y);
            case TRAIN_GATE_L: case TRAIN_GATE_R: {
                if (s != COMPLETE) {
                    say(p, TRAINER1, D().npc("<p,neutral>This is a restricted area. You can only enter under the authority of King Lathas.").end());
                    return true;
                }
                throughGate(p, TRAIN_GATE_L, 2517, 3356, TRAIN_GATE_R, 2518, 3356, 1, GRATE_OPEN);
                return true;
            }
            case DUMMY: {
                if (dummyHits(p) > 5) { msg(p, "There's no more to learn from hitting this dummy."); return true; }
                setDummyHits(p, dummyHits(p) + 1);
                p.getSkillManager().addQuestExperience(0, 50.0);
                p.getUpdateState().setAnimation(422);
                msg(p, "You hit the dummy.");
                return true;
            }
            default: return false;
        }
    }

    @Override
    public boolean handleSecondObjectAction(Player p, int id, int x, int y, int stage) {
        if (id != JERICO_CUP_OPEN && id != NURSE_CUP_OPEN) return false;
        ObjectManager.getInstance().removeDynamicObjectAt(x, y, p.getPosition().getPlane(), 0);
        return true;
    }

    /** LostCity mournerstewfence: climbs over the fence into the mourners' yard (2541,3331, wall on the east edge). */
    private boolean climbFence(final Player p, int x, int y) {
        boolean west = onLocSide(p, x, y, 2);
        final Position start = new Position(west ? x : x + 1, y, 0);
        final int dx = west ? 1 : -1;
        if (p.getPosition().getX() != start.getX() || p.getPosition().getY() != start.getY()) p.moveTo(start);
        later(p, 1, () -> p.packetSender.queueRelativeMovementStep(dx, 0, true));
        return true;
    }

    /** LostCity east_gate: the guard searches anyone heading east into Varrock while carrying the quest vials. */
    private boolean varrockGate(final Player p, int x, int y) {
        if (x != 3264 || (y != 3405 && y != 3406)) return false;
        int s = lc(p);
        if (p.getPosition().getX() >= 3264 || s < GIVEN_DISTILLATOR || s >= FOUND_SECRET || npcNear(p, BIOGUARD, 2) == null) return false;
        say(p, BIOGUARD, D()
            .npc("<p,neutral>Halt. I need to conduct a search on you. There have been reports of someone bringing a virus into Varrock.")
            .act(q -> { close(q); msg(q, "The guard searches you."); later(q, 3, () -> confiscate(q, new int[]{ETHENEA, HONEY, BROLINE}, 0)); })
            .end());
        return true;
    }

    private void confiscate(final Player p, final int[] items, final int k) {
        if (k >= items.length) {
            say(p, BIOGUARD, D().npc("<p,neutral>You may now pass.").act(q -> {
                close(q);
                q.packetSender.queueRelativeMovementStep(1, 0, true);
                q.packetSender.openDoubleDoorPair(VARROCK_GATE_L, 3264, 3405, VARROCK_GATE_R, 3264, 3406);
                q.packetSender.sendSoundEffect(DOOR_OPEN, 1, 0);
            }).end());
            return;
        }
        if (!has(p, items[k])) { confiscate(p, items, k + 1); return; }
        String[] names = {"ethenea", "liquid honey", "sulphuric broline"};
        take(p, items[k]);
        msg(p, "He takes the vial of " + names[k] + " from you.");
        later(p, 3, () -> confiscate(p, items, k + 1));
    }

    // ------------------------------------------------------------ items
    @Override
    public boolean handleItemOnObject(Player p, int item, int obj, int stage) {
        int s = lc(p);
        if (obj == WATCHTOWER) {
            if (s == SPOKEN_JERICO && item == BIRDFEED) {
                take(p, BIRDFEED);
                msg(p, "You throw a hand full of seeds onto the watch tower");
                later(p, 2, () -> { set(p, USED_BIRDFEED); msg(p, "The mourners do not seem to notice"); });
            } else msg(p, "Nothing interesting happens.");
            return true;
        }
        if (obj == CAULDRON && item == ROTTEN_APPLE) {
            msg(p, "You place the rotten apple in the pot...");
            take(p, ROTTEN_APPLE);
            later(p, 3, () -> {
                if (lc(p) == CLIMBED_LADDER) {
                    set(p, POISONED_STEW);
                    msg(p, "and it quickly dissolves into the stew.");
                    later(p, 2, () -> msg(p, "That wasn't very nice."));
                } else msg(p, "That wasn't very nice.");
            });
            return true;
        }
        if ((obj == HQ_GATE_L || obj == HQ_GATE_R) && near(p, 2551, 3325, 1, 2)) {
            if (item != KEY) { msg(p, "Nothing interesting happens."); return true; }
            msg(p, "The key fits the gate.");
            later(p, 3, () -> throughGate(p, HQ_GATE_L, 2551, 3326, HQ_GATE_R, 2551, 3325, 2, GRATE_OPEN));
            return true;
        }
        return false;
    }

    @Override
    public boolean handleInventoryItemFirstOption(Player p, int interfaceId, int item, int stage) {
        if (item != PIGEONS) return false;
        Position q = p.getPosition();
        boolean zone = q.getPlane() == 0 && q.getX() >= 2559 && q.getX() <= 2565 && q.getY() >= 3299 && q.getY() <= 3307;
        if (lc(p) == USED_BIRDFEED && zone) {
            msg(p, "The pigeons fly towards the watch tower");
            take(p, PIGEONS);
            give(p, CAGE);
            set(p, RELEASED_PIGEONS);
            // LostCity coord_projectile(coord, dest, biopigeon_launch, 16, endheight, delay, 20, length, 10, 20)
            int[][] flights = {{2559, 3303, 130, 25, 21}, {2560, 3304, 125, 15, 31}, {2561, 3305, 120, 5, 41}};
            for (int[] f : flights) {
                int dist = Math.max(Math.abs(f[0] - q.getX()), Math.abs(f[1] - q.getY()));
                ZonePacket.sendProjectile(p, q, PIGEON_LAUNCH, (byte) (f[0] - q.getX()), (byte) (f[1] - q.getY()), 0,
                    f[3], f[3] + f[4] + dist * 20, 16, f[2], 20, 10);
            }
            later(p, 4, () -> msg(p, "The mourners frantically try to scare them away"));
            return true;
        }
        msg(p, "You open the cage");
        msg(p, "The pigeons don't want to leave");
        return true;
    }

    // ---------------------------------------------------------------- journal
    @Override
    public final String[] buildQuestJournal(Player p, int state) {
        int s = lc(p);
        StringBuilder t = new StringBuilder();
        if (s == NOT_STARTED) {
            t.append("@dbl@I can start this quest by speaking to @dre@Elena @dbl@who is in @dre@East|Ardougne");
            return t.toString().split("\\|");
        }
        t.append("@str@I've spoken to Elena, the Mourners stole her distillator|");
        if (s > STARTED) t.append("|@str@I've spoken to Jerico about getting into West Ardougne|");
        else t.append("|@dbl@I need to talk to @dre@Jerico @dbl@about getting over the wall and|into @dre@West Ardougne");
        if (s == SPOKEN_JERICO) {
            t.append("|@dre@Omart @dbl@will be able to get me over the wall");
            t.append("|@dbl@He's waiting at the @dre@South @dbl@end of the wall");
        }
        if (s == USED_BIRDFEED) t.append("|@dbl@I've chucked some birdfeed onto the Watch Tower");
        if (s == RELEASED_PIGEONS) {
            t.append("|@dbl@The Watch Tower is now surrounded by flapping pigeons!");
            t.append("|@dbl@Maybe I can sneak over the wall while the mourners are|distracted");
        }
        if (s >= CLIMBED_LADDER) {
            t.append("|@str@I've crossed the wall into West Ardougne");
            t.append("|@str@Omart and Kilron will stay to help me out again|");
            if (s < FOUND_DISTILLATOR) t.append("|@dbl@Somewhere in this city is @dre@Elena's distillator @dbl@- I must find|it and return it to her|");
        }
        if (s == POISONED_STEW) {
            t.append("|@dbl@I have rather unkindly poisoned the mourners' stew by|putting rotten apples into it!");
            t.append("|@dbl@At least the mourners aren't very nice people, so I don't|feel bad about this");
        }
        if (s > FOUND_DISTILLATOR) t.append("|@str@I've found Elena's Distillator and returned it to her|");
        else if (s == FOUND_DISTILLATOR) {
            t.append("|@str@I managed to find Elena's distillator");
            if (has(p, DISTILLATOR)) t.append("|@dbl@Now I can return the @dre@distillator @dbl@to @dre@Elena");
            else {
                t.append("|@dbl@I seem to have mislaid the @dre@distillator");
                t.append("|@dbl@I had better try to find another, or @dre@Elena @dbl@will be upset");
            }
        }
        if (s > GIVEN_DISTILLATOR) t.append("|@str@Elena gave me some chemicals to take to Guidor|");
        else if (s == GIVEN_DISTILLATOR) {
            t.append("|@dbl@Elena's asked me to take the following items to @dre@Guidor @dbl@who|lives in @dre@Varrock");
            t.append("|@dre@Plague Sample @dbl@from @dre@Elena");
            if (has(p, SAMPLE)) t.append("|@str@Elena gave me the Plague Sample");
            t.append("|@dre@Liquid Honey");
            if (has(p, HONEY)) t.append("|@str@I have the Liquid Honey");
            t.append("|@dre@Sulphuric Broline");
            if (has(p, BROLINE)) t.append("|@str@I have the Sulphuric Broline");
            t.append("|@dre@Ethenea");
            if (has(p, ETHENEA)) t.append("|@str@I have the Ethenea");
            t.append("|@dre@Touch Paper @dbl@from the @dre@Chemist @dbl@in @dre@Rimmington");
        }
        if (s > SPOKEN_CHEMIST) t.append("|@str@I've given all the items to Guidor|");
        else if (s == SPOKEN_CHEMIST)
            t.append("|@dbl@The Varrock guards are out looking for someone carrying|suspicious materials. I must smuggle the items to @dre@Guidor|@dbl@secretly");
        if (s == FOUND_SECRET) {
            t.append("|@dre@Guidor's @dbl@findings were very interesting");
            t.append("|@dbl@Apparently there is no @dre@Plague@dbl@!");
            t.append("|@dbl@I should see what Elena thinks of this");
        }
        if (s == REPORTED_ELENA) {
            t.append("|@str@I've told Elena Guidor's findings, she was horrified!|");
            t.append("|@dbl@I need to confront the @dre@King of East Ardougne");
        }
        if (s == COMPLETE) {
            t.append("|@str@I've spoken to King Lathas, he admits the Plague was fake!|");
            t.append("|@str@I have been given permission to use the Royal Training|@str@Gounds which are located North West of Ardougne|");
            t.append("|@red@QUEST COMPLETE!");
        }
        return t.toString().split("\\|", -1);
    }

    // ------------------------------------------------------------- completion
    @Override
    public final void awardCompletionRewards(Player p) {
        super.markQuestComplete(p);
        super.showQuestCompleteInterface(p);
        p.getSkillManager().addQuestExperience(17, 1250.0);
        p.packetSender.sendInterfaceText("3 Quest Points", 12150);
        p.packetSender.sendInterfaceText("1250 Thieving XP", 12151);
        p.packetSender.sendInterfaceText("Use of King Lathas'", 12152);
        p.packetSender.sendInterfaceText("Combat Training Camp", 12153);
        p.packetSender.sendInterfaceText("", 12154);
        p.packetSender.sendInterfaceText("", 12155);
        p.packetSender.sendInterfaceModel(QuestScript.usesLegacyCompletionInterface() ? 6161 : 12145, 250, DISTILLATOR);
        p.packetSender.showInterface(QuestScript.usesLegacyCompletionInterface() ? 1689 : 12140);
        p.deferLevelUpInterfaces = false;
    }

    /** World set-up: makes the doctors' gown wearable (LostCity wearpos=torso). */
    public static void setup() {
        try {
            java.lang.reflect.Field f = com.rs2.model.item.ItemDefinition.class.getDeclaredField("equipmentSlot");
            f.setAccessible(true);
            f.setInt(com.rs2.model.item.ItemDefinition.forId(DOCTOR_GOWN), 4);
        } catch (Throwable t) { t.printStackTrace(); }
    }
}

package com.rs2.model.quest.impl;

import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestScript;
import java.util.ArrayList;
import java.util.List;

/**
 * Plague City (quest 71). Dialogue and flow follow the 2004scape (LostCityRS) "quest_elena" scripts.
 * Edmond -> gas mask -> mud patch -> sewers (Edmond follows) -> rope on grill -> West Ardougne -> Jethick ->
 * Rehnisons -> Milli -> mourners -> clerk/Bravek -> hangover cure -> warrant -> plague house -> Elena -> Edmond.
 */
public final class PlagueCityQuest extends QuestScript {
    public static final int ID = 71;

    // npcs
    private static final int ALRENA = 710, BRAVEK = 711, CLERK = 713, EDMOND = 714, MOURNER_HEAD = 716,
        TED = 721, MARTHA = 722, BILLY = 723, MILLI = 724, JETHICK = 725, ELENA = 3215;
    // pseudo npc ids used only to open scripted message boxes
    private static final int BOX = 6401;
    // stages (same order as the 2004scape varp)
    private static final int S_STARTED = 2, S_GASMASK = 3, S_MUD0 = 4, S_MUD4 = 8, S_TUNNEL = 9, S_ROPE = 10, S_PIPE = 11,
        S_PIC = 12, S_BOOK = 13, S_MARTHA = 14, S_MILLI = 15, S_HOUSE = 16, S_CLERK = 17, S_BRAVEK = 18, S_CURED = 19,
        S_FREED = 20, S_DONE = 1;
    // items
    private static final int SPADE = 952, ROPE = 954, BUCKET = 1925, BUCKET_WATER = 1929, MILK = 1927, DUST = 1975,
        CHOC_MILK = 1977, SNAPE = 231, CURE = 1504, WARRANT = 1503, NOTE = 1508, KEY = 1507, BOOK = 1509,
        PICTURE = 1510, MASK = 1506, DWELL = 2126, SCROLL = 1505;
    // objects
    private static final int MUD_PATCH = 2531, PIPE = 2542, GRILL = 2342, MUD_PILE = 2533, MANHOLE = 2543, SINK = 874,
        CUPBOARD = 2524, BARREL = 2530, PH_DOOR = 2535, STAIRS_DOWN = 2522, STAIRS_UP = 2523, CELL_DOOR = 2526,
        BRAVEK_DOOR = 2528, REHN_DOOR = 2537;

    private final java.util.Map<Player, Object[]> active = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Object[]>());
    private final java.util.Map<Player, Object[]> pending = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Object[]>());

    private static final int N = 24;
    private final QuestDlg[] edmond = new QuestDlg[N], alrena = new QuestDlg[N], jethick = new QuestDlg[N],
        martha = new QuestDlg[N], milli = new QuestDlg[N], mournerE = new QuestDlg[N], mournerW = new QuestDlg[N],
        mournerH = new QuestDlg[N], clerk = new QuestDlg[N], bravek = new QuestDlg[N], elena = new QuestDlg[N];

    public PlagueCityQuest(int n) {
        super(ID);
        super.setQuestPointReward(1);
        buildEdmond(); buildAlrena(); buildJethick(); buildRehnisons(); buildMourners(); buildClerk(); buildBravek(); buildElena();
        for (QuestDlg[] set : new QuestDlg[][]{edmond, alrena, jethick, martha, milli, mournerE, mournerW, mournerH, clerk, bravek, elena})
            for (QuestDlg d : set) if (d != null) d.validate();
    }

    // ------------------------------------------------------------ helpers
    private static QuestDlg D() { return new QuestDlg(); }
    private static void stage(Player p, int s) { p.setQuestState(ID, s); }
    private static int st(Player p) { return p.getQuestState(ID, true); }
    private static boolean done(Player p) { return st(p) == S_DONE; }
    /** stage ordering where "complete" counts as the highest stage. */
    private static int ord(Player p) { int s = st(p); return s == S_DONE ? 99 : s; }
    private static boolean has(Player p, int item) { return p.getInventoryManager().containsItem(item); }
    private static void take(Player p, int item) { p.getInventoryManager().removeItem(new ItemStack(item, 1)); }
    private static void give(Player p, int item) { p.getInventoryManager().addItem(new ItemStack(item, 1)); }
    private static void msg(Player p, String s) { p.packetSender.sendGameMessage(s); }
    private static boolean wearingMask(Player p) { return p.getEquipmentManager().getItemIdAtSlot(0) == MASK; }
    private static void setAll(QuestDlg[] set, int from, int to, QuestDlg d) { for (int k = from; k <= to; k++) set[k] = d; }
    private static void bump(Player p, int from, int to) { if (st(p) == from) stage(p, to); }
    private static boolean east(Player p) { return p.getPosition().getX() >= 2557; }

    /** Opens a one-off scripted dialogue (door, pipe, item...) through the normal dialogue engine. */
    private void say(Player p, int npc, QuestDlg d) {
        d.validate();
        pending.put(p, new Object[]{npc, d});
        DialogueManager.startDialogue(p, npc);
    }

    private void box(Player p, String text) { say(p, BOX, D().mes(text).end()); }

    /** id of the nearest active npc whose id is within [lo,hi] and within r tiles of the player, or -1. */
    private static int npcNear(Player p, int lo, int hi, int r) {
        Position q = p.getPosition();
        int best = -1, bd = 1 << 30;
        Npc[] all = World.getNpcs();
        for (Npc n : all) {
            if (n == null || !n.isActive() || n.isDead()) continue;
            int id = n.getNpcId();
            if (id < lo || id > hi) continue;
            Position np = n.getPosition();
            if (np.getPlane() != q.getPlane()) continue;
            int d = Math.max(Math.abs(np.getX() - q.getX()), Math.abs(np.getY() - q.getY()));
            if (d <= r && d < bd) { bd = d; best = id; }
        }
        return best;
    }

    // ---------------------------------------------------------------- Edmond
    private void buildEdmond() {
        edmond[0] = D()
            .player("Hello old man.")
            .mes("The man looks upset.")
            .player("What's wrong?")
            .npc("I've got to find my daughter. I pray that she is still alive...")
            .options("What's happened to her?", "happened", "Well, good luck finding her.", "luck")
            .label("luck").end()
            .label("happened")
            .npc("Elena's a missionary and a healer. Three weeks ago she managed to cross the Ardougne wall.")
            .npc("No one's allowed to cross the wall in case they spread the plague, but after hearing the screams of suffering, she felt she had to help.")
            .npc("She said she'd be gone for a few days, but we've heard nothing since.")
            .options("Tell me more about the plague.", "plague", "Can I help find her?", "help", "I'm sorry, I have to go.", "sorry")
            .label("plague")
            .npc("The mourners can tell you more than me, they're the only ones allowed to cross the border. I do know the plague is a horrible way to go... That's why Elena felt she had to go help.")
            .options("Can I help find her?", "help", "I'm sorry, I have to go.", "sorry")
            .label("sorry")
            .npc("Okay, then, goodbye.")
            .end()
            .label("help")
            .npc("Really, would you? I've been working on a plan to get over the wall, but I'm too old and tired to carry it through.")
            .npc("If you're going over, the first thing you'll need is protection. My wife made a special gasmask for Elena with dwellberries rubbed into it.")
            .npc("Dwellberries help repel the virus. We need some more though...")
            .player("Where can I find these dwellberries?")
            .act(p -> this.startQuest(p))
            .npc("The only place I know is McGrubor's Wood to the north.")
            .player("Okay, I'll go get some.")
            .end();

        edmond[S_STARTED] = D()
            .player("Hello Edmond.")
            .npc("Have you got the dwellberries?")
            .jumpIf(p -> has(p, DWELL), "yes")
            .player("Sorry, I'm afraid not.")
            .npc("You'll probably find them in McGrubor's Wood to the north.")
            .end()
            .label("yes")
            .player("Yes, I have some here.")
            .npc("Take them to my wife Alrena.")
            .end();

        QuestDlg dig = D()
            .player("Hi Edmond, I've got the gas mask now.")
            .npc("Good stuff, now for the digging. Beneath are the Ardougne sewers. There you'll find access to West Ardougne.")
            .act(p -> bump(p, S_GASMASK, S_MUD0))
            .npc("The problem is the soil is rock hard. You'll need to pour on some buckets of water to soften it up. I'll keep an eye out for the mourners.")
            .end();
        setAll(edmond, S_GASMASK, S_MUD4, dig);

        edmond[S_TUNNEL] = D()
            .player("Edmond, I can't get through to West Ardougne! There's an iron grill blocking my way. I can't pull it off alone.")
            .npc("If you get some rope you could tie to the grill, then we could both pull it from here.")
            .end();
        edmond[S_ROPE] = D()
            .player("I've tied the other end of this rope to the grill.")
            .mes("Edmond gets a good grip on the rope. Together you tug the rope. You hear a clunk as you both fly backwards...")
            .npc("That's done the job.")
            .npc("Remember to always wear the gasmask, otherwise you'll die over there for certain... Please bring my Elena back safe and sound.")
            .act(p -> bump(p, S_ROPE, S_PIPE))
            .end();
        QuestDlg report = D()
            .player("Hello.")
            .npc("Have you found Elena yet?")
            .player("Not yet, it's a big city over there.")
            .npc("I hope it's not too late.")
            .end();
        setAll(edmond, S_PIPE, S_CURED, report);
        edmond[S_FREED] = D()
            .npc("Thank you, thank you! Elena beat you back by minutes. Now I said I'd give you a reward. What can I give you as a reward I wonder?")
            .npc("Here take this magic scroll, I have little use for it but it may help you.")
            .npc("Now I'd recommend you go and see Elena. She'll want to thank you herself. She lives in the house opposite ours.")
            .act(p -> this.awardCompletionRewards(p))
            .endKeep();
        edmond[S_DONE] = D()
            .player("Hello there.")
            .npc("Ah hello. Thank you again for rescuing my daughter.")
            .jumpIf(p -> has(p, SCROLL), "bye")
            .options("Do you have any more of those scrolls?", "more", "No problem.", "bye")
            .label("more")
            .jumpIf(p -> p.ownsItem(SCROLL), "bank")
            .act(p -> give(p, SCROLL))
            .npc("Yes, here you go.")
            .end()
            .label("bank")
            .npc("But you've already got one! It's in your bank.")
            .end()
            .label("bye")
            .player("No problem.")
            .end();
    }

    // ---------------------------------------------------------------- Alrena
    private void buildAlrena() {
        alrena[0] = D()
            .player("Hello Madam.")
            .npc("Oh, hello there.")
            .player("Are you ok?")
            .npc("Not too bad... I've just got some troubles on my mind...")
            .end();
        alrena[S_STARTED] = D()
            .player("Hello, Edmond has asked me to help find your daughter.")
            .npc("Yes he told me. I've begun making your special gas mask, but I need some dwellberries to finish it.")
            .jumpIf(p -> has(p, DWELL), "have")
            .player("I'll try to get some.")
            .npc("The best place to look is in McGrubor's Wood to the north.")
            .end()
            .label("have")
            .player("Yes I've got some here.")
            .act(p -> take(p, DWELL))
            .mes("You give the dwellberries to Alrena.")
            .mes("Alrena crushes the berries into a smooth paste. She then smears the paste over a strange mask.")
            .act(p -> { stage(p, S_GASMASK); give(p, MASK); })
            .npc("There we go, all done. While in West Ardougne you must wear this at all times, or you'll never make it back.")
            .mes("Alrena has given you a gas mask.")
            .npc("While you two are digging, I'll make a spare mask. I'll hide it in the cupboard incase the mourners come in.")
            .end();
        alrena[S_GASMASK] = D()
            .player("Hello Alrena.")
            .npc("Hello darling, how's that tunnel coming along?")
            .player("We're getting there...")
            .npc("Well I'm sure you're quicker than Edmond.")
            .player("I just need to soften the soil and then we'll start digging.")
            .npc("If you lose your protective clothing, I've made a spare set. They're hidden in the cupboard incase the mourners come in.")
            .end();
        setAll(alrena, S_MUD0, S_MUD4 - 1, alrena[S_GASMASK]);
        alrena[S_MUD4] = D()
            .player("Hello again Alrena.")
            .npc("How's the tunnel going?")
            .player("I'm getting there.")
            .npc("One of the mourners has been sniffling around asking questions about you and Edmond, you should keep an eye out for him.")
            .player("Ok, thanks Alrena.")
            .end();
        alrena[S_TUNNEL] = D()
            .player("Hello Alrena.")
            .npc("Hi, have you managed to get through to West Ardougne?")
            .player("Not yet, but I should be going soon.")
            .npc("Make sure you wear your mask while you are over there! I can't think of a worse way to die.")
            .end();
        QuestDlg any = D()
            .player("Hello Alrena.")
            .npc("Hello, any word on Elena?")
            .player("Not yet I'm afraid.")
            .end();
        setAll(alrena, S_ROPE, S_CURED, any);
        QuestDlg thanks = D()
            .npc("Thank you for rescuing my daughter! Elena has told me of your bravery entering a house that could have been plague infected. I can't thank you enough!")
            .end();
        alrena[S_FREED] = thanks;
        alrena[S_DONE] = thanks;
    }

    // --------------------------------------------------------------- Jethick
    private void buildJethick() {
        QuestDlg greet = D()
            .npc("Hello, I don't recognise you. We don't get many newcomers around here.")
            .end();
        for (int k = 0; k < N; k++) jethick[k] = greet;
        QuestDlg old = D()
            .npc("Hello. We don't get many newcomers around here.")
            .end();
        setAll(jethick, S_BOOK, S_FREED, old);
        jethick[S_DONE] = old;
        QuestDlg ask = D()
            .npc("Hello, I don't recognise you. We don't get many newcomers around here.")
            .options("I'm looking for a woman from East Ardougne.", "woman", "So who's in charge here?", "charge")
            .label("charge")
            .npc("Well, King Tyras has wandered off in to the west kingdom. He doesn't care about the mess he's left here. The city warder Bravek is in charge at the moment. He's not much better.")
            .end()
            .label("woman")
            .npc("East Ardougnian women are easier to find in East Ardougne. Not many would come to West Ardougne to find one. Any particular woman you have in mind?")
            .player("Yes, a lady called Elena.")
            .npc("What does she look like?")
            .jumpIf(p -> has(p, PICTURE), "pic")
            .player("Um... brown hair, in her twenties...")
            .npc("Hmmm, that doesn't narrow it down a huge amount... I'll need to know more than that.")
            .end()
            .label("pic")
            .mes("You show the picture to Jethick.")
            .act(p -> bump(p, S_PIPE, S_PIC))
            .npc("Ah yes, I recognise her. She was over here to help aid plague victims.")
            .npc("I think she is staying over with the Rehnison family. They live in the small timbered building at the far north side of town.")
            .npc("I've not seen her around here in a while mind you.")
            .jumpIf(p -> has(p, BOOK), "fin")
            .npc("I don't suppose you could run me a little errand while you're over there? I borrowed this book from them, can you return it?")
            .act(p -> give(p, BOOK))
            .mes("Jethick gives you a book.")
            .label("fin")
            .end();
        jethick[S_PIPE] = ask;
        jethick[S_PIC] = ask;
    }

    // ----------------------------------------------------------- Rehnisons
    private void buildRehnisons() {
        martha[S_BOOK] = D()
            .player("Hi, I hear a woman called Elena is staying here.")
            .npc("Yes she was staying here, but slightly over a week ago she was getting ready to go back.")
            .act(p -> bump(p, S_BOOK, S_MARTHA))
            .npc("However she never managed to leave. My daughter Milli was playing near the west wall when she saw some shadowy figures jump out and grab her. Milli is upstairs if you wish to speak to her.")
            .end();
        martha[S_MARTHA] = martha[S_BOOK];
        QuestDlg luck = D()
            .npc("Any luck with finding Elena yet?")
            .player("Not yet...")
            .npc("I wish you luck, she did a lot for us.")
            .end();
        setAll(martha, S_MILLI, S_CURED, luck);
        QuestDlg safe = D()
            .npc("Any luck finding Elena yet?")
            .player("Yes, she is safe at home now.")
            .npc("That's good to hear, she helped us a lot.")
            .end();
        martha[S_FREED] = safe;
        martha[S_DONE] = safe;

        milli[S_BOOK] = D()
            .player("Hello.")
            .mes("Milli ignores you.")
            .end();
        milli[S_MARTHA] = D()
            .player("Hello. Your parents say you saw what happened to Elena...")
            .npc("*sniff* Yes I was near the south east corner when I saw Elena walking by.")
            .npc("I was about to run to greet her when some men jumped out. Shoved a sack over her head and dragged her into a building.")
            .act(p -> bump(p, S_MARTHA, S_MILLI))
            .player("Which building?")
            .npc("It was the mossy windowless building in that south east corner of West Ardougne.")
            .end();
        QuestDlg still = D()
            .npc("Have you found Elena yet?")
            .player("No... I am still looking.")
            .npc("I hope you find her... She was nice.")
            .end();
        setAll(milli, S_MILLI, S_CURED, still);
        QuestDlg ok = D()
            .npc("Have you found Elena yet?")
            .player("Yes, she's safe at home now.")
            .npc("I hope she comes and visits sometime.")
            .player("Maybe.")
            .end();
        milli[S_FREED] = ok;
        milli[S_DONE] = ok;
    }

    // -------------------------------------------------------------- mourners
    /** Dialogue shared by every mourner: labels are appended where needed. */
    private void buildMourners() {
        // ---- East Ardougne mourner (near Edmond's house)
        mournerE[0] = D()
            .player("Hello there.")
            .npc("Do you have a problem traveller?")
            .player("No, I just wondered why you're wearing that outfit... Is it fancy dress?")
            .npc("No! It's for protection.")
            .player("Protection from what?")
            .npc("The plague of course...")
            .end();
        mournerE[S_STARTED] = D()
            .player("Hello.")
            .npc("What do you want?")
            .options("Who are you?", "who", "Nothing, just being polite.", "polite")
            .label("polite")
            .npc("Hmmm, ok then. Be on your way.")
            .end()
            .label("who")
            .npc("I'm a mourner. It's my job to help heal the plague victims of West Ardougne and to make sure the disease is contained.")
            .player("Very noble of you.")
            .npc("If you come down with any symptoms such as flu or nightmares let me know immediately.")
            .end();
        mournerE[S_GASMASK] = D()
            .player("Hello.")
            .npc("Are you ok?")
            .player("Yes, I'm fine thanks.")
            .npc("Have you experienced any plague symptoms?")
            .options("What are the symptoms?", "symp", "No, I feel fine.", "fine", "Where did the plague come from?", "origin")
            .label("symp")
            .npc("First you'll come down with heavy flu, this is usually followed by horrifying nightmares.")
            .player("I used to have nightmares when I was younger.")
            .npc("Not like these I assure you. Soon after a thick black liquid will seep from your nose and eyes.")
            .player("Yuck!")
            .npc("When it gets to that stage there's nothing we can do for you.")
            .end()
            .label("fine")
            .npc("Well if you take a turn for the worse let me know straight away.")
            .player("Can you cure it then?")
            .npc("No... But you will have to be treated.")
            .player("Treated?")
            .npc("We have to take measures to contain the disease. That's why you must let us know immediately if you take a turn for the worse.")
            .end()
            .label("origin")
            .npc("It all started when King Tyras of West Ardougne came back from one of his visits to the lands west of here.")
            .npc("Some of his men must have unknowingly caught it out there and brought it back with them")
            .end();
        QuestDlg dig = D()
            .player("Hello.")
            .npc("What are you up to with old man Edmond?")
            .player("Nothing, we've just been chatting.")
            .npc("What about his daughter?")
            .player("Oh, you know about that then?")
            .npc("We know about everything that goes on in Ardougne. We have to if we are to contain the plague.")
            .player("Have you see his daughter recently?")
            .npc("I imagine she's caught the plague. Either way she won't be allowed out of West Ardougne, the risk is too great.")
            .end();
        setAll(mournerE, S_MUD0, S_MUD4, dig);
        mournerE[S_TUNNEL] = D()
            .player("Hello there.")
            .npc("Been digging have we?")
            .player("What do you mean?")
            .npc("Your hands are covered in mud.")
            .player("Oh that...")
            .npc("Funny, you don't look like the gardening type.")
            .player("Oh no, I love gardening! It's my favorite pastime.")
            .end();
        mournerE[S_ROPE] = D()
            .player("Hello there.")
            .npc("What are you up to?")
            .player("What do you mean?")
            .npc("You and that Edmond fella, you're looking very suspicious.")
            .player("We're just gardening. Have you heard any news about West Ardougne?")
            .npc("Just the usual, everyone's sick or dying. I'm furious at King Tyras for bringing this plague to our lands.")
            .end();
        QuestDlg dflt = D()
            .player("Hello.")
            .npc("What are you up to?")
            .player("Nothing.")
            .npc("I don't trust you.")
            .player("You don't have to.")
            .npc("If I find you attempting to cross the wall I'll make sure you never return.")
            .end();
        setAll(mournerE, S_PIPE, S_DONE, dflt);
        mournerE[S_DONE] = dflt;
        mournerE[S_FREED] = dflt;

        // ---- West Ardougne mourners (plague house guards etc.)
        QuestDlg west = D()
            .npc("Hmmm, how did you get over here? You're not one of this rabble. Ah well, you'll have to stay. Can't risk you going back now.")
            .jumpIf(p -> ord(p) >= S_FREED, "two")
            .options("So what's a mourner?", "mourner", "I haven't got the plague though...", "plague", "I'm looking for a woman named Elena.", "elena")
            .label("two")
            .options("So what's a mourner?", "mourner", "I haven't got the plague though...", "plague")
            .label("mourner")
            .npc("We're working for King Lathas of East Ardougne. He has tasked us with containing the accursed plague sweeping West Ardougne.")
            .npc("We also do our best to ease these people's suffering. We're nicknamed mourners because we spend a lot of time at plague victim funerals, no one else is allowed to risk attending.")
            .npc("It's a demanding job, and we get little thanks from the people here.")
            .end()
            .label("plague")
            .npc("Can't risk you being a carrier. That protective clothing you have isn't regulation issue. It won't meet safety standards.")
            .end()
            .label("elena")
            .npc("Ah yes, I've heard of her. A healer I believe. She must be mad coming over here voluntarily.")
            .npc("I hear rumours she has probably caught the plague now. Very tragic, a stupid waste of life.")
            .end();
        for (int k = 0; k < N; k++) mournerW[k] = west;

        // ---- Head mourner
        QuestDlg head = D()
            .npc("How did you get into West Ardougne? Ah well you'll have to stay, can't risk you spreading the plague outside.")
            .jumpIf(p -> { int o = ord(p); return o >= S_HOUSE && o <= S_CURED; }, "four")
            .jumpIf(p -> ord(p) >= S_FREED, "two")
            .options("So what's a mourner?", "mourner", "I haven't got the plague though...", "plague", "I'm looking for a woman named Elena.", "elena")
            .label("four")
            .options("I need clearance to enter a plague house.", "clear", "So what's a mourner?", "mourner", "I haven't got the plague though...", "plague", "I'm looking for a woman named Elena.", "elena")
            .label("two")
            .options("So what's a mourner?", "mourner", "I haven't got the plague though...", "plague")
            .label("mourner")
            .npc("We're working for King Lathas of East Ardougne. He has tasked us with containing the accursed plague sweeping West Ardougne.")
            .npc("We also do our best to ease these people's suffering. We're nicknamed mourners because we spend a lot of time at plague victim funerals, no one else is allowed to risk attending.")
            .npc("It's a demanding job, and we get little thanks from the people here.")
            .end()
            .label("plague")
            .npc("Can't risk you being a carrier. That protective clothing you have isn't regulation issue. It won't meet safety standards.")
            .end()
            .label("elena")
            .npc("Ah yes, I've heard of her. A healer I believe. She must be mad coming over here voluntarily.")
            .npc("I hear rumours she has probably caught the plague now. Very tragic, a stupid waste of life.")
            .end()
            .label("clear")
            .player("It's in the south east corner of West Ardougne.")
            .npc("You must be nuts, absolutely not!")
            .options("There's a kidnap victim inside!", "kid", "I've got a gas mask though...", "mask", "Yes, I'm utterly crazy.", "crazy")
            .label("kid")
            .npc("Well they're as good as dead then, no point in trying to save them.")
            .end()
            .label("mask")
            .npc("It's not regulation. Anyway you're not properly trained to deal with the plague.")
            .player("How do I get trained?")
            .npc("It requires a strict 18 months of training.")
            .player("I don't have that sort of time.")
            .end()
            .label("crazy")
            .npc("You're wasting my time, I have a lot of work to do!")
            .end();
        for (int k = 0; k < N; k++) mournerH[k] = head;
    }

    // ----------------------------------------------------------------- clerk
    private void buildClerk() {
        QuestDlg basic = D()
            .npc("Hello, welcome to the Civic Office of West Ardougne. How can I help you?")
            .options("Who is through that door?", "door", "I'm just looking thanks.", "look")
            .label("look").end()
            .label("door")
            .npc("The city warder Bravek is in there.")
            .player("Can I go in?")
            .jumpIf(p -> ord(p) >= S_BRAVEK, "sure")
            .npc("He has asked not to be disturbed.")
            .end()
            .label("sure")
            .npc("I suppose so.")
            .end();
        for (int k = 0; k < N; k++) clerk[k] = basic;
        QuestDlg house = D()
            .npc("Hello, welcome to the Civic Office of West Ardougne. How can I help you?")
            .options("I need permission to enter a plague house.", "perm", "Who is through that door?", "door", "I'm just looking thanks.", "look")
            .label("look")
            .end()
            .label("perm")
            .npc("Rather you than me! Well, the mourners normally deal with that stuff, you should speak to them. Their headquarters are right near the city gate.")
            .options("I'll try asking them then.", "try", "Surely you don't let them run everything for you?", "surely", "This is urgent though!", "urgent")
            .label("try").end()
            .label("surely")
            .npc("Well, they do know what they're doing here. If they did start doing something badly Bravek, the city warder, would have the power to override. I can't see that happening though.")
            .options("I'll try asking them then.", "try", "Can I speak to Bravek anyway?", "anyway")
            .label("anyway")
            .npc("He has asked not to be disturbed.")
            .options("This is urgent though!", "urgent", "Ok I will leave him alone.", "try")
            .label("door")
            .npc("The city warder Bravek is in there.")
            .player("Can I go in?")
            .npc("He has asked not to be disturbed.")
            .options("This is urgent though!", "urgent", "Ok I will leave him alone.", "try")
            .label("urgent")
            .npc("I'll see what I can do I suppose.")
            .npc("Mr Bravek there's a person here who really needs to speak to you.")
            .act(p -> bump(p, S_HOUSE, S_CLERK))
            .npc("I suppose they can come in then. If they keep it short.")
            .end();
        clerk[S_HOUSE] = house;
        QuestDlg see = D()
            .npc("Bravek will see you now but keep it short!")
            .player("Thanks, I won't take much of his time.")
            .end();
        clerk[S_CLERK] = see;
    }

    // ---------------------------------------------------------------- Bravek
    private void buildBravek() {
        bravek[S_CLERK] = D()
            .npc("My head hurts! I'll speak to you another day...")
            .options("This is really important though!", "imp", "Ok, goodbye.", "bye")
            .label("bye").end()
            .label("imp")
            .npc("I can't possibly speak to you with my head spinning like this... I went a bit heavy on the drink again last night.")
            .npc("Curse my herbalist, she made the best hang over cures. Darn inconvenient of her catching the plague.")
            .options("Ok, goodbye.", "bye", "You shouldn't drink so much then!", "drink", "Do you know what is in the cure?", "cure")
            .label("drink")
            .npc("Well position of responsibility are hard, I need something to take my mind off things... Especially with the problems this place has.")
            .options("Ok, goodbye", "bye", "Do you know what is in the cure?", "cure", "I don't think drink is the solution.", "nodrink")
            .label("nodrink")
            .npc("*uurgh* My head still hurts too much to think straight. Oh for one of Trudi's hangover cures...")
            .end()
            .label("cure")
            .npc("Hmmm let me think... Ouch! Thinking isn't clever. Ah here, she did scribble it down for me.")
            .act(p -> { give(p, NOTE); stage(p, S_BRAVEK); })
            .mes("Bravek hands you a tatty piece of paper.")
            .end();
        bravek[S_BRAVEK] = D()
            .npc("*uurgh* My head still hurts too much to think straight. Oh for one of Trudi's hangover cures...")
            .jumpIf(p -> has(p, CURE), "give")
            .end()
            .label("give")
            .player("Try this.")
            .act(p -> { take(p, CURE); stage(p, S_CURED); })
            .mes("You give Bravek the hangover cure. Bravek gulps down the foul-looking liquid.")
            .npc("Ooh that's much better! Thanks, that's the clearest my head has felt in a month. Ah now, what was it you wanted me to do for you?")
            .player("I need to rescue a kidnap victim called Elena. She's being held in a plague house, I need permission to enter.")
            .go("mourners")
            .label("mourners")
            .npc("Well the mourners deal with that sort of thing...")
            .options("Ok, I'll go speak to them.", "speak", "Is that all anyone says around here?", "allsay", "They won't listen to me!", "wont")
            .label("speak").end()
            .label("allsay")
            .npc("Well, they know best about plague issues.")
            .options("Don't you want to take an interest in it at all?", "interest", "They won't listen to me!", "wont")
            .label("interest")
            .npc("Nope I don't wish to take a deep interest in plagues. That stuff is too scary for me!")
            .options("I see why people say you're a weak leader.", "weak", "Okay, I'll talk to the mourners.", "speak", "They won't listen to me!", "wont")
            .label("weak")
            .npc("Bah, people always criticise their leaders but delegating is the only way to lead. I delegate all plague issues to the mourners.")
            .player("This whole city is a plague issue!")
            .end()
            .label("wont")
            .player("They say I'm not properly equipped to go in the house, though I do have a very effective gas mask.")
            .npc("Hmmm, well I guess they're not taking the issue of a kidnapping seriously enough. They do go a bit far sometimes.")
            .npc("I've heard of Elena, she has helped us a lot... Okay, I'll give you this warrant to enter the house.")
            .act(p -> give(p, WARRANT))
            .mes("Bravek hands you a warrant.")
            .end();
        // stage "cured": same menu again if the player left without the warrant
        QuestDlg after = D()
            .npc("Thanks again for the hangover cure.")
            .jumpIf(p -> has(p, WARRANT), "have")
            .npc("Ah now what was it you wanted me to do for you?")
            .player("I need to rescue Elena. She's now a kidnap victim! She's being held in a plague house, I need permission to enter.")
            .npc("Well the mourners deal with that sort of thing...")
            .options("Ok, I'll go speak to them.", "speak", "Is that all anyone says around here?", "allsay", "They won't listen to me!", "wont")
            .label("speak").end()
            .label("allsay")
            .npc("Well, they know best about plague issues.")
            .options("Don't you want to take an interest in it at all?", "interest", "They won't listen to me!", "wont")
            .label("interest")
            .npc("Nope I don't wish to take a deep interest in plagues. That stuff is too scary for me!")
            .options("I see why people say you're a weak leader.", "weak", "Okay, I'll talk to the mourners.", "speak", "They won't listen to me!", "wont")
            .label("weak")
            .npc("Bah, people always criticise their leaders but delegating is the only way to lead. I delegate all plague issues to the mourners.")
            .player("This whole city is a plague issue!")
            .end()
            .label("wont")
            .player("They say I'm not properly equipped to go in the house, though I do have a very effective gas mask.")
            .npc("Hmmm, well I guess they're not taking the issue of a kidnapping seriously enough. They do go a bit far sometimes.")
            .npc("I've heard of Elena, she has helped us a lot... Okay, I'll give you this warrant to enter the house.")
            .act(p -> give(p, WARRANT))
            .mes("Bravek hands you a warrant.")
            .end()
            .label("have")
            .player("Not a problem, happy to help out.")
            .npc("I'm just having a little drop of whisky, then I'll feel really good.")
            .end();
        bravek[S_CURED] = after;
        QuestDlg thanks = D()
            .npc("Thanks again for the hangover cure.")
            .player("Not a problem, happy to help out.")
            .npc("I'm just having a little drop of whisky, then I'll feel really good.")
            .end();
        bravek[S_FREED] = thanks;
        bravek[S_DONE] = thanks;
    }

    // ----------------------------------------------------------------- Elena
    private void buildElena() {
        QuestDlg free = D()
            .player("Hi, you're free to go! Your kidnappers don't seem to be about right now.")
            .npc("Thank you, being kidnapped was so inconvenient. I was on my way back to East Ardougne with some samples, I want to see if I can diagnose a cure for this plague.")
            .player("Well you can leave via the manhole in the middle of the city.")
            .act(p -> { if (st(p) >= S_STARTED) stage(p, S_FREED); })
            .npc("Go and see my father, I'll make sure he adequately rewards you.")
            .end();
        for (int k = 0; k < N; k++) elena[k] = free;
        elena[S_DONE] = D().mes("You have already rescued Elena.").end();
    }

    // -------------------------------------------------------------- journal
    private static void j(List<String> out, boolean done, String text) {
        for (String l : QuestDlg.wrap(text, 50)) out.add((done ? "@str@" : "") + l);
    }

    @Override
    public final String[] buildQuestJournal(Player p, int s) {
        List<String> o = new ArrayList<>();
        int c = s == S_DONE ? 99 : s;
        if (c == 0) {
            j(o, false, "I can start this quest by speaking to Edmond who is in the north-west of East Ardougne.");
            j(o, false, "There aren't any requirements for this Quest.");
            return o.toArray(new String[0]);
        }
        j(o, true, "I've spoken to Edmond, he's asked me to help find his daughter Elena.");
        if (c == S_STARTED) { o.add(""); j(o, false, "I need to get some Dwellberries for Alrena so she can make me a Gasmask. I'm told I'll find some in McGrubor's wood."); }
        if (c >= S_GASMASK) { o.add(""); j(o, true, "Alrena has given me a Gasmask to protect me from the plague while in West Ardougne. She's making a spare which will be in the cupboard."); }
        if (c == S_GASMASK) { o.add(""); j(o, false, "I need to talk to Edmond about getting into West Ardougne."); }
        if (c >= S_MUD0) { o.add(""); j(o, true, "I've spoken to Edmond about getting into West Ardougne."); }
        if (c >= S_MUD4) {
            o.add(""); j(o, true, "I've softened the ground in Edmond's garden enough to dig.");
            if (c == S_MUD4) { o.add(""); j(o, false, "Now I need to dig a tunnel into West Ardougne."); }
        } else if (c >= S_MUD0) { o.add(""); j(o, false, "I need to use buckets of water to soften the ground in Edmond's garden before I can dig into it."); }
        if (c >= S_TUNNEL) {
            o.add(""); j(o, true, "I've dug a tunnel into the sewers.");
            if (c == S_TUNNEL) { o.add(""); j(o, false, "I need to find a way out of the sewers and into West Ardougne. Edmond says a rope might help with the grill."); }
        }
        if (c >= S_PIPE) {
            o.add(""); j(o, true, "I've managed to clear the way into West Ardougne.");
            if (c == S_PIPE) { o.add(""); j(o, false, "I should look for someone in West Ardougne who has seen Elena."); }
        } else if (c == S_ROPE) { o.add(""); j(o, false, "I've tied a rope to the sewer grill but I need Edmond's help to pull it off."); }
        if (c > S_BOOK) { o.add(""); j(o, true, "I've spoken to Jethick, he thinks Elena was staying with the Rehnison Family, in a timber house to the north of the city."); }
        else if (c >= S_PIC) { o.add(""); j(o, false, "I've spoken to Jethick, he thinks Elena was staying with the Rehnison Family, in a timber house to the north of the city."); }
        if (c == S_MARTHA) { o.add(""); j(o, false, "The Rehnisons' daughter Milli may have seen Elena."); }
        if (c >= S_MILLI) { o.add(""); j(o, true, "I've spoken to Milli about Elena, she says Elena was taken into one of the Plague Houses."); }
        if (c == S_MILLI) { o.add(""); j(o, false, "I need to get access to the Plague House. Milli said it was to the south of the city."); }
        else if (c == S_HOUSE || c == S_CLERK) { o.add(""); j(o, false, "I need clearance from either the Head Mourner or Bravek to get into the Plague House."); }
        if (c >= S_BRAVEK) { o.add(""); j(o, true, "Bravek might give me clearance if I make his Hangover Cure."); }
        if (c == S_BRAVEK) { o.add(""); j(o, false, "I need to bring Bravek the Hangover Cure when I work it out."); }
        if (c >= S_CURED) {
            o.add(""); j(o, true, "I've given Bravek the Hangover Cure and he has given me a warrant to enter the plague House.");
            if (c == S_CURED) { o.add(""); j(o, false, "I need to get into the Plague House and find Elena."); }
        }
        if (c >= S_FREED) { o.add(""); j(o, true, "I've freed Elena from the Plague House."); }
        if (c == S_FREED) { o.add(""); j(o, false, "I should go and talk to Edmond again."); }
        if (c == 99) {
            o.add(""); j(o, true, "I've spoken to Edmond and he thanked me for rescuing his daughter Elena. He's given me a spell to learn as a reward.");
            o.add(""); o.add("QUEST COMPLETE!");
        }
        return o.toArray(new String[0]);
    }

    @Override
    public final void awardCompletionRewards(Player p) {
        for (int it : new int[]{PICTURE, KEY, WARRANT, NOTE, BOOK}) take(p, it);
        give(p, SCROLL);
        super.markQuestComplete(p);
        super.showQuestCompleteInterface(p);
        p.packetSender.sendInterfaceText("1 Quest Point", 12150);
        p.packetSender.sendInterfaceText("2425 Mining XP", 12151);
        p.packetSender.sendInterfaceText("Ardougne Teleport scroll", 12152);
        p.packetSender.sendInterfaceText("", 12153);
        p.packetSender.sendInterfaceText("", 12154);
        p.packetSender.sendInterfaceText("", 12155);
        p.getSkillManager().addQuestExperience(14, 2425.0);
        p.packetSender.sendInterfaceModel(QuestScript.usesLegacyCompletionInterface() ? 6161 : 12145, 250, MASK);
        p.packetSender.showInterface(QuestScript.usesLegacyCompletionInterface() ? 1689 : 12140);
        p.deferLevelUpInterfaces = false;
    }

    // -------------------------------------------------------------- dialogue
    @Override
    public final boolean handleNpcDialogue(Player p, int npc, int step, int option, int stage) {
        QuestDlg d;
        if (step <= 1) {
            Object[] pd = pending.remove(p);
            if (pd != null && ((Integer) pd[0]) == npc) {
                d = (QuestDlg) pd[1];
                active.put(p, pd);
            } else {
                QuestDlg[] set = setFor(p, npc);
                if (set == null || stage < 0 || stage >= set.length || set[stage] == null) return false;
                d = set[stage];
                active.put(p, new Object[]{npc, d});
            }
        } else {
            Object[] cur = active.get(p);
            if (cur == null || ((Integer) cur[0]) != npc) return false;
            d = (QuestDlg) cur[1];
        }
        return d.run(p, step, option);
    }

    private QuestDlg[] setFor(Player p, int npc) {
        switch (npc) {
            case EDMOND: return edmond;
            case ALRENA: return alrena;
            case JETHICK: return jethick;
            case TED: case MARTHA: return martha;
            case MILLI: return milli;
            case CLERK: return clerk;
            case BRAVEK: return bravek;
            case ELENA: return elena;
            case BILLY: return null;
            case MOURNER_HEAD: return mournerH;
            case 717: case 718: case 719: return east(p) ? mournerE : mournerW;
            default: return null;
        }
    }

    // ---------------------------------------------------------- item on item
    @Override
    public boolean handleItemOnItem(Player p, int a, int b, int stage) {
        if (stage < S_BRAVEK && stage != S_DONE) return false;
        if ((a == DUST && b == MILK) || (a == MILK && b == DUST)) {
            take(p, DUST); take(p, MILK); give(p, CHOC_MILK);
            msg(p, "You mix the chocolate into the bucket.");
            return true;
        }
        if ((a == SNAPE && b == CHOC_MILK) || (a == CHOC_MILK && b == SNAPE)) {
            take(p, SNAPE); take(p, CHOC_MILK); give(p, CURE);
            msg(p, "You mix the snape grass into the bucket. It looks like a hangover cure.");
            return true;
        }
        return false;
    }

    // --------------------------------------------------------- item on object
    private static boolean near(Player p, int x, int y, int r) {
        Position q = p.getPosition();
        return q.getPlane() == 0 && Math.abs(q.getX() - x) <= r && Math.abs(q.getY() - y) <= r;
    }

    @Override
    public boolean handleItemOnObject(Player p, int item, int obj, int stage) {
        if (item == BUCKET && obj == SINK && near(p, 2573, 3334, 6)) {
            take(p, BUCKET); give(p, BUCKET_WATER);
            msg(p, "You fill the bucket from the sink.");
            return true;
        }
        if (obj == MUD_PATCH && near(p, 2566, 3332, 6)) {
            if (item == BUCKET_WATER) return pour(p, stage);
            if (item == SPADE) return dig(p, stage);
        }
        if ((obj == PIPE || obj == GRILL) && item == ROPE && inSewerRoom(p)) {
            if (stage == S_TUNNEL) {
                take(p, ROPE);
                stage(p, S_ROPE);
                box(p, "You tie the end of the rope to the sewer pipe's grill and hold the other end in your hand.");
            } else msg(p, "Nothing interesting happens.");
            return true;
        }
        if (obj == CELL_DOOR && item == KEY) return cellDoor(p, true);
        return false;
    }

    private static boolean inSewerRoom(Player p) {
        Position q = p.getPosition();
        return q.getX() >= 2505 && q.getX() <= 2525 && q.getY() >= 9730 && q.getY() <= 9770;
    }

    private boolean pour(Player p, int stage) {
        if (stage < S_MUD0 && stage != S_DONE) { box(p, "You see no reason to do that at the moment."); return true; }
        if (stage >= S_MUD4 || stage == S_DONE) { box(p, "You don't need to pour any more water. The soil is soft enough already."); return true; }
        take(p, BUCKET_WATER); give(p, BUCKET);
        stage(p, stage + 1);
        if (stage + 1 == S_MUD4) box(p, "You pour water onto the soil. The soil is now soft enough to dig into.");
        else box(p, "You pour the water onto the soil. The soil softens slightly.");
        return true;
    }

    private boolean dig(Player p, int stage) {
        if (stage < S_MUD4 && stage != S_DONE) { box(p, "You dig the soil... The ground is rather hard."); return true; }
        if (stage < S_TUNNEL) stage(p, S_TUNNEL);
        say(p, BOX, D()
            .mes("You dig deep into the soft soil... Suddenly it crumbles away!")
            .act(q -> q.moveTo(new Position(2516, 9757, 0)))
            .mes("You fall through... ...you land in the sewer. Edmond follows you down the hole.")
            .end());
        return true;
    }

    @Override
    public boolean handleInventoryItemFirstOption(Player p, int interfaceId, int item, int stage) {
        if (item == SPADE && near(p, 2566, 3332, 3)) return dig(p, stage);
        if (item == NOTE) {
            say(p, BOX, D().mes("Hangover cure: mix chocolate dust into a bucket of milk, then add a little snape grass.").end());
            return true;
        }
        if (item == SCROLL) {
            take(p, SCROLL);
            if (stage != S_DONE) { box(p, "The scroll bursts into flame."); return true; }
            say(p, BOX, D()
                .mes("You memorise what is written on the scroll.")
                .mes("You can now cast the Ardougne Teleport spell provided you have the required runes and magic level.")
                .end());
            return true;
        }
        return false;
    }

    // ----------------------------------------------------------------- doors
    /** Moves the player across a door wall at (x,y) with the given orientation. */
    private static void cross(Player p, int x, int y, int orient) {
        int px = p.getPosition().getX(), py = p.getPosition().getY(), pl = p.getPosition().getPlane();
        int tx = px, ty = py;
        switch (orient) {
            case 0: tx = px >= x ? x - 1 : x; ty = y; break;
            case 1: ty = py <= y ? y + 1 : y; tx = x; break;
            case 2: tx = px <= x ? x + 1 : x; ty = y; break;
            default: ty = py >= y ? y - 1 : y; tx = x; break;
        }
        p.moveTo(new Position(tx, ty, pl));
    }

    private boolean cellDoor(Player p, boolean usedKey) {
        int s = st(p);
        if (s == S_FREED || s == S_DONE) { cross(p, 2539, 9672, 2); return true; }
        if (has(p, KEY)) {
            say(p, BOX, D().mes("You unlock the door.").act(q -> cross(q, 2539, 9672, 2)).end());
            return true;
        }
        QuestDlg locked = D().mes("The door is locked.").end();
        if (npcNear(p, ELENA, ELENA, 5) >= 0) {
            locked = D()
                .mes("The door is locked.")
                .npc("Hey get me out of here please!")
                .player("I would do but I don't have a key.")
                .npc("I think there may be one around somewhere. I'm sure I heard them stashing it somewhere.")
                .options("Have you caught the plague?", "plague", "Okay, I'll look for it.", "ok")
                .label("ok").end()
                .label("plague")
                .npc("No, I have none of the symptoms.")
                .player("Strange, I was told this house was plague infected.")
                .npc("I suppose that was a cover up by the kidnappers.")
                .end();
            say(p, ELENA, locked);
        } else say(p, BOX, locked);
        return true;
    }

    private boolean plagueHouseDoor(Player p, int x, int y) {
        boolean inside = p.getPosition().getY() >= y;
        int s = st(p);
        if (inside || s == S_FREED || s == S_DONE) { cross(p, x, y, 3); return true; }
        int m = npcNear(p, 717, 719, 14);
        if (s == S_CURED && has(p, WARRANT) && m >= 0) {
            say(p, m, D()
                .npc("I'd stand away from there. That black cross means that house has been touched by the plague.")
                .player("I have a warrant from Bravek to enter here.")
                .npc("This is highly irregular. Please wait while I speak to the Head Mourner.")
                .act(q -> cross(q, x, y, 3))
                .mes("You wait until the mourner's back is turned and sneak into the building.")
                .end());
            return true;
        }
        if (s == S_MILLI && m >= 0) {
            say(p, m, D().mes("The door won't open. You notice a black cross on the door.").go("m").label("m").npc("I'd stand away from there. That black cross means that house has been touched by the plague.")
                .options("But I think a kidnap victim is in here.", "kid", "I fear not a mere plague.", "fear", "Thanks for the warning.", "thanks")
                .label("thanks").end()
                .label("kid")
                .npc("Sounds unlikely, even kidnappers wouldn't go in there. Even if someone is in there, they're probably dead by now.")
                .options("Good point.", "thanks", "I want to check anyway.", "check")
                .label("check")
                .npc("You don't have clearance to go in there.")
                .go("clear")
                .label("fear")
                .npc("That's irrelevant. You don't have clearance to go in there.")
                .label("clear")
                .player("How do I get clearance?")
                .npc("Well you'd need to apply to the head mourner, or I suppose Bravek the city warder.")
                .act(q -> bump(q, S_MILLI, S_HOUSE))
                .npc("I wouldn't get your hopes up though.")
                .end());
            return true;
        }
        if (m >= 0) {
            say(p, m, D().mes("The door won't open. You notice a black cross on the door.")
                .npc("I'd stand away from there. That black cross means that house has been touched by the plague.").end());
        } else box(p, "The door won't open. You notice a black cross on the door.");
        return true;
    }

    private boolean rehnisonDoor(Player p, int x, int y) {
        int s = st(p);
        boolean inside = p.getPosition().getY() > y; // north wall: the house is north of the door tile
        if (inside || s >= S_BOOK || s == S_DONE) { cross(p, x, y, 1); return true; }
        if (npcNear(p, TED, TED, 7) < 0) { msg(p, "The door is locked."); return true; }
        if (s == S_PIC && has(p, BOOK)) {
            say(p, TED, D()
                .npc("Go away. We don't want any.")
                .player("I'm a friend of Jethick's, I have come to return a book he borrowed.")
                .npc("Oh... Why didn't you say, come in then.")
                .act(q -> { stage(q, S_BOOK); take(q, BOOK); cross(q, x, y, 1); })
                .mes("You hand the book to Ted as you enter.")
                .npc("Thanks, I've been missing that.")
                .end());
        } else say(p, TED, D().npc("Go away. We don't want any.").end());
        return true;
    }

    // ---------------------------------------------------------- object clicks
    @Override
    public boolean handleFirstObjectAction(Player p, int id, int x, int y, int stage) {
        if (id == MUD_PILE) {
            msg(p, "You climb up the mud pile.");
            p.moveTo(new Position(2566, 3332, 0));
            return true;
        }
        if (id == PIPE || (id == GRILL && Math.abs(x - 2514) <= 2 && Math.abs(y - 9737) <= 2)) {
            if (stage == S_TUNNEL || stage == S_ROPE) { box(p, "The grill is too secure. You can't pull it off alone."); return true; }
            if (stage >= S_PIPE || stage == S_DONE) {
                if (wearingMask(p)) {
                    msg(p, "You climb through the sewer pipe.");
                    p.moveTo(new Position(2529, 3304, 0));
                } else if (stage == S_DONE) box(p, "You should wear your gas mask before entering West Ardougne.");
                else if (npcNear(p, EDMOND, EDMOND, 20) >= 0) say(p, EDMOND, D().npc("I can't let you enter the city without your gas mask on.").end());
                return true;
            }
            msg(p, "Nothing interesting happens.");
            return true;
        }
        if (id == MANHOLE && (x == 2529 && y == 3303 || x == 2516 && y == 3322)) {
            if (stage < S_PIPE && stage != S_DONE) return false;
            msg(p, "You climb down the manhole into the sewers.");
            p.moveTo(new Position(2516, 9740, 0));
            return true;
        }
        if (id == CUPBOARD && x == 2574 && y == 3334) {
            if ((stage < S_GASMASK && stage != S_DONE) || p.ownsItem(MASK)) { msg(p, "You search the cupboard but you find nothing."); return true; }
            give(p, MASK);
            box(p, "You find a protective mask.");
            return true;
        }
        if (id == BARREL && x == 2534 && y == 3268) {
            if (stage == S_CURED && !has(p, KEY)) {
                give(p, KEY);
                box(p, "You find a small key in the barrel.");
            } else msg(p, "The barrel is empty.");
            return true;
        }
        if (id == PH_DOOR && ((x == 2533 && y == 3272) || (x == 2540 && y == 3273))) return plagueHouseDoor(p, x, y);
        if (id == STAIRS_DOWN && x == 2536 && y == 3268) {
            msg(p, "You walk down the stairs...");
            p.moveTo(new Position(2537, 9671, 0));
            return true;
        }
        if (id == STAIRS_UP && x == 2536 && y == 9671) {
            msg(p, "You walk up the stairs...");
            p.moveTo(new Position(2536, 3269, 0));
            return true;
        }
        if (id == CELL_DOOR && x == 2539 && y == 9672) return cellDoor(p, false);
        if (id == REHN_DOOR && x == 2531 && y == 3328) return rehnisonDoor(p, x, y);
        if (id == BRAVEK_DOOR && x == 2530 && y == 3314) {
            if (stage < S_CLERK && stage != S_DONE && stage < S_CLERK) {
                say(p, BRAVEK, D().npc("Go away, I'm busy! I'm... Umm... In a meeting!").end());
                return true;
            }
            cross(p, x, y, 0);
            return true;
        }
        return false;
    }

    /** Spawns Milli (upstairs in the Rehnison house), Elena (cell) and Edmond (who waits in the sewer). */
    public static void spawnWorld() {
        try {
            GameplayHelper.spawnNpc(MILLI, 2530, 3332, 1, 0);
            GameplayHelper.spawnNpc(ELENA, 2541, 9671, 0, 0);
            GameplayHelper.spawnNpc(EDMOND, 2516, 9741, 0, 0);
        } catch (Throwable t) { t.printStackTrace(); }
    }
}

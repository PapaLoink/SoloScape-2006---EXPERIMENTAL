package com.rs2.model.quest.impl;

import com.rs2.model.quest.QuestScript;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.player.Player;

public final class MonksFriendQuest extends QuestScript {
    public static final int ID = 63;
    private static final int OMAD = 279, CEDRIC = 280;
    private static final int S_OMAD = 2, S_BLANKET = 3, S_FIND_CEDRIC = 4, S_WATER = 5, S_GAVE_WATER = 6, S_WOOD = 7, S_FIXED = 8;
    private static final int BLANKET = 90, JUG = 1937, LOGS = 1511, LAW = 563;

    private final QuestDlg[] omad = new QuestDlg[10];
    private final QuestDlg[] cedric = new QuestDlg[10];

    public MonksFriendQuest(int n) {
        super(ID);
        super.setQuestPointReward(1);
        buildOmad();
        buildCedric();
        for (QuestDlg d : omad) if (d != null) d.validate();
        for (QuestDlg d : cedric) if (d != null) d.validate();
    }

    private void stage(Player p, int s) { p.setQuestState(ID, s); }

    private static boolean has(Player p, int item) { return p.getInventoryManager().containsItem(item); }
    private static void take(Player p, int item) { p.getInventoryManager().removeItem(new ItemStack(item, 1)); }

    // ---------------------------------------------------------------- Omad
    private void buildOmad() {
        // not started
        omad[0] = new QuestDlg()
            .player("<p,quiz>Hello there, What's wrong?")
            .npc("<p,sad>*yawn*...oh, hello...*yawn* I'm sorry! I'm just so tired! I haven't slept in a week!")
            .options("<p,quiz>Why can't you sleep, what's wrong?", "why", "<p,angry>Sorry! I'm too busy to hear your problems!", "busy")
            .label("busy").end()
            .label("why")
            .npc("<p,sad>It's brother Androe's son! With his constant: Waaaaaah! Waaaaaaaaah! Androe said it's natural, but it's so annoying!")
            .player("<p,happy>I suppose that's what kids do.")
            .npc("<p,sad>He was fine, up until last week! Thieves broke in! They stole his favourite sleeping blanket!")
            .npc("<p,sad>Now he won't rest until it's returned... ...and that means neither can I!")
            .options("<p,quiz>Can I help at all?", "help", "<p,neutral>I'm sorry to hear that! I hope you find his blanket.", "sorry")
            .label("sorry").end()
            .label("help")
            .npc("<p,neutral>Please do. We won't be able to help you as we are peaceful men but we would be grateful for your help!")
            .player("<p,neutral>Where are they?")
            .act(p -> this.startQuest(p))
            .npc("<p,neutral>They hide in a secret cave in the forest. It's hidden under a ring of stones. Please, bring back the blanket!")
            .end();

        // started: looking for blanket
        omad[S_OMAD] = new QuestDlg()
            .player("<p,neutral>Hello.")
            .npc("<p,sad>*yawn*...oh, hello again...*yawn*")
            .npc("<p,sad>Please tell me you have the blanket.")
            .jumpIf(p -> has(p, BLANKET), "have")
            .player("<p,sad>I'm afraid not.")
            .npc("<p,sad>I need some sleep!")
            .end()
            .label("have")
            .player("<p,happy>Yes! I've recovered it from the clutches of the evil thieves!")
            .act(p -> take(p, BLANKET))
            .item(BLANKET, "You hand the monk the childs blanket.")
            .npc("<p,happy>Really, that's excellent, well done! Maybe now I will be able to get some rest.")
            .act(p -> stage(p, S_BLANKET))
            .npc("<p,neutral>*yawn*..I'm off to bed! Farewell brave traveller!")
            .end();

        // blanket returned: organise party
        omad[S_BLANKET] = new QuestDlg()
            .player("<p,neutral>Hello, how are you?")
            .npc("<p,happy>Much better now I'm sleeping well! Now I can organise the party.")
            .player("<p,happy>Ooh! What party?")
            .npc("<p,happy>The son of Brother Androe's birthday party. He's going to be one year old!")
            .player("<p,happy>That's sweet!")
            .npc("<p,happy>It's also a great excuse for a drink!")
            .npc("<p,neutral>We just need Brother Cedric to return with the wine.")
            .options("<p,quiz>Who's Brother Cedric?", "who", "<p,happy>Enjoy it! I'll see you soon!", "enjoy")
            .label("enjoy").npc("<p,neutral>Take care traveller.").end()
            .label("who")
            .npc("<p,neutral>Cedric is a member of the order too. We sent him out three days ago to collect wine. But he didn't return!")
            .npc("<p,angry>He most probably got drunk and lost in the forest!")
            .npc("<p,quiz>I don't suppose you could look for him?")
            .options("<p,neutral>I've no time for that, sorry.", "notime", "<p,quiz>Where should I look?", "where", "<p,happy>Can I come to the party?", "come")
            .label("notime").npc("<p,sad>Okay traveller, take care.").end()
            .label("come").npc("<p,neutral>Of course, but we need the wine first.").end()
            .label("where")
            .npc("<p,happy>Oh, he won't be far. Probably out in the forest.")
            .player("<p,neutral>Ok, I'll go and find him.")
            .act(p -> stage(p, S_FIND_CEDRIC))
            .end();

        // looking for Cedric / finding water
        omad[S_FIND_CEDRIC] = new QuestDlg()
            .player("<p,neutral>Hello brother Omad.")
            .npc("<p,neutral>Hello adventurer, have you found Brother Cedric?")
            .player("<p,neutral>Not yet.")
            .npc("<p,sad>Well, keep looking, we need that wine!")
            .end();
        omad[S_WATER] = new QuestDlg()
            .player("<p,neutral>Hello brother Omad.")
            .npc("<p,sad>Hello adventurer, have you found Brother Cedric?")
            .player("<p,happy>Yes I've seen him, he's a bit drunk!")
            .npc("<p,happy>Well, try your best to get him back here!")
            .end();

        // water given / fixing cart
        omad[S_GAVE_WATER] = new QuestDlg()
            .player("<p,neutral>Hello again brother Omad.")
            .npc("<p,quiz>Hello adventurer, where's Brother Cedric?")
            .player("<p,neutral>He's having a bit of trouble with his cart.")
            .npc("<p,sad>Hmmm! Maybe you could help?")
            .end();
        omad[S_WOOD] = omad[S_GAVE_WATER];

        // cart fixed: finish
        omad[S_FIXED] = new QuestDlg()
            .player("<p,happy>Hi Omad, Brother Cedric is on his way!")
            .npc("<p,happy>Good! Good! Now we can party!")
            .npc("<p,happy>I have little to repay you with but please! Take these Rune Stones.")
            .item(LAW, "Brother Omad gives you 8 Law Runes.")
            .player("<p,happy>Thanks Brother Omad!")
            .npc("<p,happy>OK, let's party!")
            .act(p -> this.startParty(p, true))
            .end();

        // complete: Omad throws the party again (OSRS wiki, Monk's Friend rewards; not in the 2004 LostCity scripts)
        omad[1] = new QuestDlg()
            .npc("<p,drunk>Dum dee do la la! *hiccup* That was some party!")
            .act(p -> this.startParty(p, false))
            .end();
    }

    // -------------------------------------------------------------- Cedric
    private void buildCedric() {
        QuestDlg dflt = new QuestDlg()
            .player("<p,neutral>Hello.")
            .npc("<p,drunk>Honey, money, woman and wine!")
            .player("<p,quiz>Are you ok?")
            .npc("<p,drunk>Yesshh...hic up...beautiful!")
            .player("<p,neutral>Take care old monk.")
            .npc("<p,drunk>La..di..da..hic..up!")
            .mes("The old monk has had too much to drink.")
            .end();
        for (int k = 0; k < cedric.length; k++) cedric[k] = dflt;

        cedric[S_FIND_CEDRIC] = new QuestDlg()
            .player("<p,quiz>Brother Cedric are you okay?")
            .npc("<p,drunk>Yeesshhh, I'm very, very drunk..hic..up..")
            .player("<p,neutral>Brother Omad needs the wine for the party.")
            .npc("<p,sad>Oh dear, oh dear, I knew I had to do something!")
            .npc("<p,sad>Pleashhh, find me a jug of water. Once I'm sober I'll 'elp you take the wine back.")
            .act(p -> stage(p, S_WATER))
            .end();

        cedric[S_WATER] = new QuestDlg()
            .player("<p,quiz>Are you okay?")
            .npc("<p,drunk>Hic up! Oh my head! I need a jug of water.")
            .jumpIf(p -> has(p, JUG), "water")
            .player("<p,neutral>I'll see if I can get some.")
            .npc("<p,drunk>Thanks! *hic*")
            .end()
            .label("water")
            .player("<p,neutral>Cedric! Here, drink! I have some water.")
            .npc("<p,drunk>Good stuff, my head's spinning!")
            .act(p -> { Npc c = nearest(p, CEDRIC, 10); if (c != null) c.getUpdateState().setForcedText("Gulp...gulp!"); })
            .act(p -> take(p, JUG))
            .item(JUG, "You hand the monk a jug of water.")
            .npc("<p,neutral>Aah! That's better!")
            .act(p -> stage(p, S_GAVE_WATER));
        cart(cedric[S_WATER]);

        cedric[S_GAVE_WATER] = new QuestDlg();
        cart(cedric[S_GAVE_WATER]);

        cedric[S_WOOD] = new QuestDlg()
            .npc("<p,quiz>Did you manage to get some wood?")
            .jumpIf(p -> has(p, LOGS), "logs")
            .player("<p,neutral>Not yet, I'm afraid.")
            .end()
            .label("logs")
            .act(p -> take(p, LOGS))
            .item(LOGS, "You hand Cedric some logs.")
            .player("<p,happy>Here you go!")
            .npc("<p,happy>Well done! Now I'll fix this cart. You head back to Brother Omad and tell him I'll be there soon.")
            .player("<p,happy>Ok! I'll see you later!")
            .act(p -> stage(p, S_FIXED))
            .end();

        cedric[S_FIXED] = new QuestDlg()
            .player("<p,neutral>Hello Cedric.")
            .npc("<p,neutral>Hi, I'm almost done here. Could you tell Omad that I'll be back soon?")
            .end();

        cedric[1] = new QuestDlg()
            .npc("<p,happy>Brother Omad sends you his thanks! He won't be in a fit state to thank you in person.")
            .end();
    }

    private void cart(QuestDlg d) {
        d.npc("<p,neutral>Now I just need to fix this cart and we can go party.")
         .npc("<p,quiz>Could you help?")
         .options("<p,angry>No, I've helped enough monks today!", "no", "<p,happy>Yes, I'd be happy to!", "yes")
         .label("no").npc("<p,sad>In that case I'd better drink more wine! It helps me think.").end()
         .label("yes")
         .act(p -> stage(p, S_WOOD))
         .npc("<p,happy>Excellent, I just need some wood.")
         .player("<p,happy>OK, I'll see what I can find.")
         .end();
    }

    // ------------------------------------------------------------ journal
    @Override
    // Text and colours from LostCity quest_drunkmonk drunkmonk_journal.rs2.
    public final String[] buildQuestJournal(Player p, int s) {
        String a1 = "@str@Brother Omad asked me to recover a child's blanket.";
        String a2 = "@str@I found the secret cave and gave back the blanket.";
        String c1 = "@str@I found Brother Cedric in the forest south of Ardougne. I";
        switch (s) {
            case 0: return new String[]{"@dbl@I can start this quest by speaking to @dre@Brother Omad@dbl@ in the",
                    "@dre@Monastery@dbl@ south of @dre@Ardougne"};
            case S_OMAD: return new String[]{"@dre@Brother Omad@dbl@ asked me to recover a @dre@child's blanket@dbl@.",
                    "@dbl@I need to find a @dre@secret cave@dbl@ that is hidden under a @dre@ring of",
                    "@dre@stones@dbl@ in the @dre@forest@dbl@ south of @dre@Ardougne"};
            case S_BLANKET: return new String[]{a1, a2};
            case S_FIND_CEDRIC: return new String[]{a1, a2, "", "@dbl@I agreed to find @dre@Brother Cedric@dbl@. He is somewhere in the",
                    "@dre@forest@dbl@ south of @dre@Ardougne"};
            case S_WATER: return new String[]{a1, a2, "", "@str@I found Brother Cedric in the forest south of Ardougne.",
                    "@dbl@I need to take him a @dre@jug of water"};
            case S_GAVE_WATER: case S_WOOD: return new String[]{a1, a2, "", c1, "@str@sobered him up.",
                    "@dbl@He needs some @dre@wood@dbl@ for his cart"};
            case S_FIXED: return new String[]{a1, a2, "", c1, "@str@sobered him up and I helped him fix his cart.",
                    "@dre@Brother Cedric@dbl@ said that I should tell @dre@Brother Omad@dbl@ that", "@dbl@he is on the way."};
            case 1: return new String[]{a1, a2, "", c1, "@str@sobered him up and I helped him fix his cart.", "",
                    "@str@I had a party with the Monks. There were party balloons", "@str@and we danced the night away!", "",
                    "@red@QUEST COMPLETE!"};
            default: return null;
        }
    }

    // ---------------------------------------------------------------- party
    // LostCity quest_drunkmonk [label,drunkmonk_party]: ticks follow its p_delay calls (p_delay(n) waits n+1 ticks).
    private static final int MONK = 281, DANCE = 866, BALLOON_FIRST = 115, BALLOON_COLOURS = 7, BALLOONS_PER_DROP = 4;
    private static final int PARTY_TICKS = 33;

    /** The party; finishQuest awards the quest the first time, later parties are just for fun. */
    private void startParty(Player p, boolean finishQuest) {
        final Npc omad = nearest(p, OMAD, 10);
        p.setActionLocked(true);
        if (omad != null) omad.getUpdateState().setFaceEntity(p.getEncodedIndex());
        com.rs2.model.World.scheduleTickTask(new com.rs2.model.task.TickTask(1, true) {
            private int tick;
            @Override public void execute() {
                // Logging out mid-party leaves the quest at the last step, so Omad starts the party again.
                if (p.getIndex() < 0 || com.rs2.model.World.getPlayers()[p.getIndex()] != p) { stop(); return; }
                switch (tick) {
                    case 0: dance(omad, "Party!"); break;
                    case 1: case 2: case 4: case 5: dropBalloons(); break;
                    case 3: dance(p, "Woop!"); dropBalloons(); break;
                    case 6: dance(omad, "Let's boogie!"); break;
                    case 9: dance(p, "Oh baby!"); break;
                    case 12: dance(nearest(p, MONK, 5), "GO!"); break;
                    case 15: dance(omad, "Get down!"); break;
                    case 18: dance(p, "Feel the rhythm!"); break;
                    case 21: dance(nearest(p, MONK, 5), "Dance!"); break;
                    case 24: dance(omad, "Oh my!"); break;
                    case 27: dance(p, "Watch me go!"); break;
                    case 30: dance(nearest(p, MONK, 5), "You go!"); break;
                    case PARTY_TICKS:
                        stop();
                        p.setActionLocked(false);
                        if (finishQuest) awardCompletionRewards(p);
                        return;
                }
                tick++;
            }
        });
    }

    private static void dance(com.rs2.model.Entity e, String text) {
        if (e == null) return;
        e.getUpdateState().setAnimation(DANCE);
        e.getUpdateState().setForcedText(text);
    }

    private static Npc nearest(Player p, int id, int range) {
        Npc best = null;
        int bestDistance = range + 1;
        for (Npc npc : com.rs2.model.World.getNpcs()) {
            if (npc == null || npc.getNpcId() != id || npc.getPosition().getPlane() != p.getPosition().getPlane()) continue;
            int distance = com.rs2.util.GameUtil.getDistance(p.getPosition(), npc.getPosition());
            if (distance < bestDistance) { best = npc; bestDistance = distance; }
        }
        return best;
    }

    /** Four balloons on free monastery floor, from the two LostCity drop areas; they stay for 200 ticks. */
    private static void dropBalloons() {
        for (int i = 0; i < BALLOONS_PER_DROP; i++) {
            int x, y;
            if (com.rs2.util.GameUtil.randomInt(3) > 0) {
                x = 2603 + com.rs2.util.GameUtil.randomInt(8); y = 3207 + com.rs2.util.GameUtil.randomInt(5);
            } else {
                x = 2605 + com.rs2.util.GameUtil.randomInt(3); y = 3212 + com.rs2.util.GameUtil.randomInt(7);
            }
            if (com.rs2.util.path.WalkingCollisionMap.getTileFlags(x, y, 0) != 0
                    || com.rs2.model.objects.ObjectManager.findDynamicObjectAt(x, y, 0) != null) continue;
            new com.rs2.model.objects.DynamicObject(BALLOON_FIRST + com.rs2.util.GameUtil.randomInt(BALLOON_COLOURS),
                    x, y, 0, com.rs2.util.GameUtil.randomInt(4), 10,
                    com.rs2.ServerSettings.placeholderObjectId, 200, false);
        }
    }

    // ----------------------------------------------------------- completion
    @Override
    public final void awardCompletionRewards(Player p) {
        super.markQuestComplete(p);
        super.showQuestCompleteInterface(p);
        p.packetSender.sendInterfaceText("1 Quest Point", 12150);
        p.packetSender.sendInterfaceText("2000 Woodcutting XP", 12151);
        p.packetSender.sendInterfaceText("8 Law Runes", 12152);
        p.packetSender.sendInterfaceText("", 12153);
        p.packetSender.sendInterfaceText("", 12154);
        p.packetSender.sendInterfaceText("", 12155);
        p.getSkillManager().addQuestExperience(8, 2000.0);
        p.getInventoryManager().addItem(new ItemStack(LAW, 8));
        p.packetSender.sendInterfaceModel(QuestScript.usesLegacyCompletionInterface() ? 6161 : 12145, 250, LAW);
        p.packetSender.showInterface(QuestScript.usesLegacyCompletionInterface() ? 1689 : 12140);
        p.deferLevelUpInterfaces = false;
    }

    // ------------------------------------------------------------ dialogue
    private final java.util.Map<Player, Object[]> active = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Object[]>());

    @Override
    public boolean handleFirstObjectAction(Player p, int id, int x, int y, int stage) {
        return Ring.click(p, id, x, y);
    }

    @Override
    public final boolean handleNpcDialogue(Player p, int npc, int step, int option, int stage) {
        Ring.ensure();
        QuestDlg[] set = npc == OMAD ? omad : npc == CEDRIC ? cedric : null;
        if (set == null) return false;
        QuestDlg d;
        if (step <= 1) {
            if (stage < 0 || stage >= set.length || set[stage] == null) return false;
            d = set[stage];
            active.put(p, new Object[]{npc, d});   // keep the same script even if the quest stage changes mid-conversation
        } else {
            Object[] cur = active.get(p);
            if (cur == null || ((Integer) cur[0]) != npc) return false;
            d = (QuestDlg) cur[1];
        }
        return d.run(p, step, option);
    }
}

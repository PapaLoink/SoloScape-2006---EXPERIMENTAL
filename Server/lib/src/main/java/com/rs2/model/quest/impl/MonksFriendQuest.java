package com.rs2.model.quest.impl;

import com.rs2.model.quest.QuestScript;
import com.rs2.model.item.ItemStack;
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
            .player("Hello there, What's wrong?")
            .npc("*yawn*...oh, hello...*yawn* I'm sorry! I'm just so tired! I haven't slept in a week!")
            .options("Why can't you sleep, what's wrong?", "why", "Sorry! I'm too busy to hear your problems!", "busy")
            .label("busy").end()
            .label("why")
            .npc("It's brother Androe's son! With his constant: Waaaaaah! Waaaaaaaaah! Androe said it's natural, but it's so annoying!")
            .player("I suppose that's what kids do.")
            .npc("He was fine, up until last week! Thieves broke in! They stole his favourite sleeping blanket!")
            .npc("Now he won't rest until it's returned... ...and that means neither can I!")
            .options("Can I help at all?", "help", "I'm sorry to hear that! I hope you find his blanket.", "sorry")
            .label("sorry").end()
            .label("help")
            .npc("Please do. We won't be able to help you as we are peaceful men but we would be grateful for your help!")
            .player("Where are they?")
            .act(p -> this.startQuest(p))
            .npc("They hide in a secret cave in the forest. It's hidden under a ring of stones. Please, bring back the blanket!")
            .end();

        // started: looking for blanket
        omad[S_OMAD] = new QuestDlg()
            .player("Hello.")
            .npc("*yawn*...oh, hello again...*yawn*")
            .npc("Please tell me you have the blanket.")
            .jumpIf(p -> has(p, BLANKET), "have")
            .player("I'm afraid not.")
            .npc("I need some sleep!")
            .end()
            .label("have")
            .player("Yes! I've recovered it from the clutches of the evil thieves!")
            .act(p -> take(p, BLANKET))
            .mes("You hand the monk the child's blanket.")
            .npc("Really, that's excellent, well done! Maybe now I will be able to get some rest.")
            .act(p -> stage(p, S_BLANKET))
            .npc("*yawn*..I'm off to bed! Farewell brave traveller!")
            .end();

        // blanket returned: organise party
        omad[S_BLANKET] = new QuestDlg()
            .player("Hello, how are you?")
            .npc("Much better now I'm sleeping well! Now I can organise the party.")
            .player("Ooh! What party?")
            .npc("The son of Brother Androe's birthday party. He's going to be one year old!")
            .player("That's sweet!")
            .npc("It's also a great excuse for a drink!")
            .npc("We just need Brother Cedric to return with the wine.")
            .options("Who's Brother Cedric?", "who", "Enjoy it! I'll see you soon!", "enjoy")
            .label("enjoy").npc("Take care traveller.").end()
            .label("who")
            .npc("Cedric is a member of the order too. We sent him out three days ago to collect wine. But he didn't return!")
            .npc("He most probably got drunk and lost in the forest!")
            .npc("I don't suppose you could look for him?")
            .options("I've no time for that, sorry.", "notime", "Where should I look?", "where", "Can I come to the party?", "come")
            .label("notime").npc("Okay traveller, take care.").end()
            .label("come").npc("Of course, but we need the wine first.").end()
            .label("where")
            .npc("Oh, he won't be far. Probably out in the forest.")
            .player("Ok, I'll go and find him.")
            .act(p -> stage(p, S_FIND_CEDRIC))
            .end();

        // looking for Cedric / finding water
        omad[S_FIND_CEDRIC] = new QuestDlg()
            .player("Hello brother Omad.")
            .npc("Hello adventurer, have you found Brother Cedric?")
            .player("Not yet.")
            .npc("Well, keep looking, we need that wine!")
            .end();
        omad[S_WATER] = new QuestDlg()
            .player("Hello brother Omad.")
            .npc("Hello adventurer, have you found Brother Cedric?")
            .player("Yes I've seen him, he's a bit drunk!")
            .npc("Well, try your best to get him back here!")
            .end();

        // water given / fixing cart
        omad[S_GAVE_WATER] = new QuestDlg()
            .player("Hello again brother Omad.")
            .npc("Hello adventurer, where's Brother Cedric?")
            .player("He's having a bit of trouble with his cart.")
            .npc("Hmmm! Maybe you could help?")
            .end();
        omad[S_WOOD] = omad[S_GAVE_WATER];

        // cart fixed: finish
        omad[S_FIXED] = new QuestDlg()
            .player("Hi Omad, Brother Cedric is on his way!")
            .npc("Good! Good! Now we can party!")
            .npc("I have little to repay you with but please! Take these Rune Stones.")
            .mes("Brother Omad gives you 8 Law Runes.")
            .player("Thanks Brother Omad!")
            .npc("OK, let's party!")
            .act(p -> this.awardCompletionRewards(p))
            .endKeep();

        // complete
        omad[1] = new QuestDlg()
            .npc("Dum dee do la la! *hiccup* That was some party!")
            .end();
    }

    // -------------------------------------------------------------- Cedric
    private void buildCedric() {
        QuestDlg dflt = new QuestDlg()
            .player("Hello.")
            .npc("Honey, money, woman and wine!")
            .player("Are you ok?")
            .npc("Yesshh...hic up...beautiful!")
            .player("Take care old monk.")
            .npc("La..di..da..hic..up!")
            .mes("The old monk has had too much to drink.")
            .end();
        for (int k = 0; k < cedric.length; k++) cedric[k] = dflt;

        cedric[S_FIND_CEDRIC] = new QuestDlg()
            .player("Brother Cedric are you okay?")
            .npc("Yeesshhh, I'm very, very drunk..hic..up..")
            .player("Brother Omad needs the wine for the party.")
            .npc("Oh dear, oh dear, I knew I had to do something!")
            .npc("Pleashhh, find me a jug of water. Once I'm sober I'll 'elp you take the wine back.")
            .act(p -> stage(p, S_WATER))
            .end();

        cedric[S_WATER] = new QuestDlg()
            .player("Are you okay?")
            .npc("Hic up! Oh my head! I need a jug of water.")
            .jumpIf(p -> has(p, JUG), "water")
            .player("I'll see if I can get some.")
            .npc("Thanks! *hic*")
            .end()
            .label("water")
            .player("Cedric! Here, drink! I have some water.")
            .npc("Good stuff, my head's spinning!")
            .act(p -> take(p, JUG))
            .mes("You hand the monk a jug of water.")
            .npc("Aah! That's better!")
            .act(p -> stage(p, S_GAVE_WATER));
        cart(cedric[S_WATER]);

        cedric[S_GAVE_WATER] = new QuestDlg();
        cart(cedric[S_GAVE_WATER]);

        cedric[S_WOOD] = new QuestDlg()
            .npc("Did you manage to get some wood?")
            .jumpIf(p -> has(p, LOGS), "logs")
            .player("Not yet, I'm afraid.")
            .end()
            .label("logs")
            .mes("You hand Cedric some logs.")
            .act(p -> take(p, LOGS))
            .player("Here you go!")
            .npc("Well done! Now I'll fix this cart. You head back to Brother Omad and tell him I'll be there soon.")
            .player("Ok! I'll see you later!")
            .act(p -> stage(p, S_FIXED))
            .end();

        cedric[S_FIXED] = new QuestDlg()
            .player("Hello Cedric.")
            .npc("Hi, I'm almost done here. Could you tell Omad that I'll be back soon?")
            .end();

        cedric[1] = new QuestDlg()
            .npc("Brother Omad sends you his thanks! He won't be in a fit state to thank you in person.")
            .end();
    }

    private void cart(QuestDlg d) {
        d.npc("Now I just need to fix this cart and we can go party.")
         .npc("Could you help?")
         .options("No, I've helped enough monks today!", "no", "Yes, I'd be happy to!", "yes")
         .label("no").npc("In that case I'd better drink more wine! It helps me think.").end()
         .label("yes")
         .act(p -> stage(p, S_WOOD))
         .npc("Excellent, I just need some wood.")
         .player("OK, I'll see what I can find.")
         .end();
    }

    // ------------------------------------------------------------ journal
    @Override
    public final String[] buildQuestJournal(Player p, int s) {
        String a1 = "Brother Omad asked me to recover a child's blanket.";
        String a2 = "I found the secret cave and gave back the blanket.";
        String c1 = "I found Brother Cedric in the forest south of Ardougne.";
        switch (s) {
            case 0: return new String[]{"I can start this quest by speaking to Brother Omad in the", "Monastery south of Ardougne"};
            case S_OMAD: return new String[]{a1, "I need to find a secret cave that is hidden under a ring of", "stones in the forest south of Ardougne"};
            case S_BLANKET: return new String[]{a1, a2};
            case S_FIND_CEDRIC: return new String[]{a1, a2, "", "I agreed to find Brother Cedric. He is somewhere in the", "forest south of Ardougne"};
            case S_WATER: return new String[]{a1, a2, "", c1, "I need to take him a jug of water"};
            case S_GAVE_WATER: case S_WOOD: return new String[]{a1, a2, "", c1 + " I", "sobered him up.", "He needs some wood for his cart"};
            case S_FIXED: return new String[]{a1, a2, "", c1 + " I", "sobered him up and I helped him fix his cart.", "Brother Cedric said that I should tell Brother Omad that", "he is on the way."};
            case 1: return new String[]{"Quest Completed!", "", "You were awarded:", "1 Quest Point", "2000 Woodcutting XP", "8 Law Runes"};
            default: return null;
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

package com.rs2.model.quest.impl;

import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.combat.hit.HitType;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestScript;

/**
 * Priest in Peril (quest 72), ported from Lost City 274's quest_priestperil scripts and the scripts it uses in
 * area_mausoleum (drezel, gates, holy_barrier, monuments, temple_guardian, well) and area_varrock (king_roald).
 */
public final class PriestInPerilQuest extends QuestScript {
    public static final int ID = 72;

    // LostCity %priestperil values. Our quest state is that value + 1, and 1 means complete (LostCity 60).
    static final int NOT_STARTED = 0, STARTED = 1, AGREE_TO_KILL_DOG = 2, KILLED_DOG = 3, RETURN_TO_DREZEL = 4,
        FIND_DREZEL_KEY = 5, UNLOCKED_DREZEL = 6, POURED_BLESSED_WATER = 7, MEET_IN_MAUSOLEUM = 8,
        BEGIN_BRING_ESSENCE = 10, END_BRING_ESSENCE = 60, COMPLETE = 60, ACCESS_HOLY_BARRIER = 61;
    // %priestperilvar lives in the flags word: bits 0-6 swapped monuments, bit 21 layout made, bits 22-28 layout seed.
    // Bit 29 stands in for LostCity's post-quest value 61 (Drezel has warned the player about Morytania).
    static final int LAYOUT_MADE = 21, SEED_SHIFT = 22, ACCESS_BIT = 29;
    // Raw quest state while the last essence has been handed over but the reward has not been given yet.
    static final int REWARD_PENDING = END_BRING_ESSENCE + 1;

    // npcs
    static final int ROALD = 648, MONK3 = 1046, DOG = 1047, DREZEL = 1048, DREZEL2 = 1049;
    // items
    static final int UNLIT_CANDLE = 36, FEATHER = 314, TINDERBOX = 590, ESSENCE = 1436, ESSENCE_NOTE = 1437,
        NEEDLE = 1733, BUCKET = 1925, BUCKET_WATER = 1929, POT = 1931, HAMMER = 2347, GOLD_KEY = 2944, IRON_KEY = 2945,
        GOLD_TINDERBOX = 2946, GOLD_CANDLE = 2947, GOLD_POT = 2948, GOLD_HAMMER = 2949, GOLD_FEATHER = 2950,
        GOLD_NEEDLE = 2951, WOLFBANE = 2952, MURKY_WATER = 2953, BLESSED_WATER = 2954,
        ARRAV_SHIELD1 = 763, ARRAV_SHIELD2 = 765, ARRAV_CERTIFICATE = 769;
    // locs
    static final int BARRIER = 3443, UNDERGROUND_DOOR1 = 3444, UNDERGROUND_DOOR2 = 3445, PRISON_DOOR = 3463,
        COFFIN = 3480, WELL = 3485, TEMPLE_DOOR_L = 3489, TEMPLE_DOOR_R = 3490, GRAVE_FIRST = 3493, GRAVE_LAST = 3499;
    // synths, seqs and spotanims
    static final int DOOR_OPEN = 318, GRATE_OPEN = 332, WELL_FILL = 1039, THROW_BUCKET = 1077, OPEN_CUPBOARD = 538, PICKUP_TABLE = 832,
        COFFIN_SPELL = 272;

    // monument contents, indexed by LostCity content id 1-7 (enum monument_graves_*)
    private static final int[] GOLD_ITEM = {0, GOLD_NEEDLE, GOLD_FEATHER, IRON_KEY, GOLD_POT, GOLD_TINDERBOX, GOLD_HAMMER, GOLD_CANDLE};
    private static final int[] PLAIN_ITEM = {0, NEEDLE, FEATHER, GOLD_KEY, POT, TINDERBOX, HAMMER, UNLIT_CANDLE};
    private static final String[] SHORT_NAME = {"", "needle", "feather", "key", "pot", "tinderbox", "hammer", "candle"};
    private static final String[] MONUMENT_TEXT = {"",
        "Saradomin is the\\nneedle that binds\\nour lives\\ntogether.",
        "Saradomin is the\\ndelicate touch\\nthat brushes us\\nwith love.",
        "Saradomin is the\\nkey that unlocks\\nthe mysteries\\nof life.",
        "Saradomin is the\\nvessel that\\nkeeps us safe\\nfrom harm.",
        "Saradomin is the\\nspark that lights\\nthe fire in\\nour hearts.",
        "Saradomin is the\\nhammer that\\ncrushes evil\\neverywhere.",
        "Saradomin is the\\nlight that shines\\nthoughout\\nour lives."};
    // priestperil_gravemonument: the model and text components of the monument screen
    static final int MONUMENT_IF = 8016, MONUMENT_MODEL = 8111, MONUMENT_TEXT_ID = 8112;

    private final java.util.Map<Player, Object[]> active = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Object[]>());
    private final java.util.Map<Player, Object[]> pending = java.util.Collections.synchronizedMap(new java.util.WeakHashMap<Player, Object[]>());

    public PriestInPerilQuest(int n) {
        super(ID);
        super.setQuestPointReward(1);
    }

    // ------------------------------------------------------------ helpers
    private static QuestDlg D() { return new QuestDlg(); }
    static int lc(Player p) {
        int s = p.getQuestState(ID, true);
        if (s == 1) return bit(p, ACCESS_BIT) ? ACCESS_HOLY_BARRIER : COMPLETE;
        return s <= 0 ? NOT_STARTED : s - 1;
    }
    private void set(Player p, int v) {
        if (v == STARTED && p.getQuestState(ID, true) == 0) { this.startQuest(p); return; }
        if (v == ACCESS_HOLY_BARRIER) { setBit(p, ACCESS_BIT); return; }
        p.setQuestState(ID, v + 1);
    }
    private static boolean bit(Player p, int b) { return (p.questProgressFlags[ID] & (1 << b)) != 0; }
    private static void setBit(Player p, int b) { p.questProgressFlags[ID] |= 1 << b; }
    private static int seed(Player p) { return (p.questProgressFlags[ID] >> SEED_SHIFT) & 127; }

    private static boolean has(Player p, int item) { return p.getInventoryManager().containsItem(item); }
    private static int count(Player p, int item) { return p.getInventoryManager().getItemAmount(item); }
    private static void take(Player p, int item) { p.getInventoryManager().removeItem(new ItemStack(item, 1)); }
    private static void give(Player p, int item) { p.getInventoryManager().addOrDropItem(new ItemStack(item, 1)); }
    private static void msg(Player p, String s) { p.packetSender.sendGameMessage(s); }
    private static void close(Player p) { p.packetSender.closeInterfaces(); }
    private static void nothing(Player p) { msg(p, "Nothing interesting happens."); }
    private static String name(int item) { return ItemDefinition.forId(item).getName(); }
    private static int plane(Player p) { return p.getPosition().getPlane(); }

    /** Opens a scripted dialogue (door, item, object...) with the given npc's chathead through the dialogue engine. */
    private void say(Player p, int npc, QuestDlg d) {
        d.validate();
        pending.put(p, new Object[]{npc, d});
        DialogueManager.startDialogue(p, npc);
    }

    /** Nearest active npc with this id within r tiles on the player's plane, or null (LostCity npc_find). */
    private static Npc npcNear(Player p, int id, int r) {
        Position q = p.getPosition();
        for (Npc n : World.getNpcs()) {
            if (n == null || !n.isActive() || n.isDead() || n.getNpcId() != id) continue;
            Position np = n.getPosition();
            if (np.getPlane() == q.getPlane() && Math.abs(np.getX() - q.getX()) <= r && Math.abs(np.getY() - q.getY()) <= r) return n;
        }
        return null;
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

    // ------------------------------------------------------------ doors
    /** LostCity check_axis for the temple doors (east wall of x 3408): true while the player is outside. */
    private static boolean outsideTemple(Player p) { return p.getPosition().getX() <= 3408; }

    /** LostCity open_and_close_double_door2: the player walks up to the half in line with them and through. */
    private static void throughTempleDoors(final Player p) {
        boolean outside = outsideTemple(p);
        int ty = Math.max(3488, Math.min(3489, p.getPosition().getY()));
        int tx = outside ? 3408 : 3409;
        if (p.getPosition().getX() != tx || p.getPosition().getY() != ty) { walkThen(p, tx, ty, () -> throughTempleDoors(p)); return; }
        p.packetSender.openDoubleDoorPair(TEMPLE_DOOR_L, TEMPLE_DOOR_R, 3408, 3489, 3408, 3488, 0);
        p.packetSender.sendSoundEffect(DOOR_OPEN, 1, 0);
        p.packetSender.queueRelativeMovementStep(outside ? 1 : -1, 0, true);
    }

    /**
     * LostCity open_and_close_metal_gate for a single door: (ox, oy) is the step from the door's own tile to the
     * tile on the other side of its wall (LostCity door_open for the door's angle). The player walks to the door's
     * tile on their side, then crosses. As in LostCity the open door stands on that far tile, turned a quarter, for
     * 3 ticks; it must not go on the tile beside the doorway, where it would replace that tile's wall (the
     * railing of Drezel's cell).
     */
    private static void throughDoor(final Player p, final int id, final int x, final int y, final int ox, final int oy) {
        int px = p.getPosition().getX(), py = p.getPosition().getY();
        boolean far = (px - x) * ox + (py - y) * oy > 0;
        int tx = far ? x + ox : x, ty = far ? y + oy : y;
        if (px != tx || py != ty) { walkThen(p, tx, ty, () -> throughDoor(p, id, x, y, ox, oy)); return; }
        int angle = ox < 0 ? 0 : oy > 0 ? 1 : ox > 0 ? 2 : 3;
        int z = plane(p);
        new DynamicObject(com.rs2.ServerSettings.placeholderObjectId, x, y, z, angle, 0, id, 3, false);
        new DynamicObject(id, x + ox, y + oy, z, (angle + 1) & 3, 0, com.rs2.ServerSettings.placeholderObjectId, 3, false);
        p.packetSender.sendSoundEffect(GRATE_OPEN, 1, 0);
        p.packetSender.queueRelativeMovementStep(far ? -ox : ox, far ? -oy : oy, true);
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
            case ROALD: return roald(p);
            case DREZEL: return drezel(p);
            case DREZEL2: return drezel2(p);
            default: return null;
        }
    }

    // ---------------------------------------------------------------- King Roald
    private QuestDlg roald(Player p) {
        // The Shield of Arrav reward is Shield of Arrav's own dialogue.
        if (has(p, ARRAV_CERTIFICATE) || has(p, ARRAV_SHIELD1) && has(p, ARRAV_SHIELD2)) return null;
        int s = lc(p);
        QuestDlg d = D().player("<p,happy>Greetings, your majesty.");
        if (s >= COMPLETE) {
            return d.npc("<p,neutral>Do you have anything of importance to say?")
                .player("<p,neutral>...Not really.")
                .npc("<p,neutral>You will have to excuse me, then. I am very busy as I have a kingdom to run!").end();
        }
        if (s == NOT_STARTED) {
            return d.npc("<p,quiz>Well hello there. What do you want?")
                .player("<p,neutral>I'm looking for a quest!")
                .npc("<p,neutral>A quest you say? Hmm, what an odd request to make of the king. It's funny you should mention it though, as there is something you can do for me.")
                .npc("<p,quiz>Are you aware of the temple east of here. It stands on the River Salve and guards the entrance to the lands of Morytania?")
                .player("<p,confused>No, I don't think I know it...")
                .npc("<p,neutral>Hmm, how strange that you don't. Well anyway, it has been some days since last I heard from Drezel, the priest who lives there.")
                .npc("<p,neutral>Be a sport and go make sure that nothing untoward has happened to the silly old codger for me, would you?")
                .optionsQuiet("Sure.", "start", "No, that sounds boring.", "abandon")
                .label("start")
                .act(q -> set(q, STARTED))
                .player("<p,neutral>Sure. I don't have anything better to do right now.")
                .npc("<p,happy>Many thanks adventurer! I would have sent one of my squires but they wanted payment for it!").end()
                .label("abandon")
                .player("<p,neutral>No. That sounds boring.")
                .npc("<p,neutral>Yes, I dare say it does. I wouldn't even have mentioned it had you not seemed to be looking for something to do anyway.").end();
        }
        if (s == STARTED) {
            return d.npc("<p,quiz>Well hello there. What do you want?")
                .npc("<p,neutral>You have news of Drezel for me?")
                .player("<p,quiz>Where am I supposed to go again?")
                .npc("<p,neutral>The temple east of here where Drezel lives. It lies south of the cliffs, at the source of the River Salve.")
                .npc("<p,neutral>Don't worry, you can't miss it.").end();
        }
        if (s == AGREE_TO_KILL_DOG) {
            return d.npc("<p,neutral>You have news of Drezel for me?")
                .player("<p,confused>Well... I went to the temple like you asked me to... and I spoke to someone inside...")
                .npc("<p,neutral>Ah, well that must have been Drezel then. What did he say?")
                .player("<p,confused>Well... he seemed to be having some kind of trouble. He asked for my help.")
                .npc("<p,neutral>Well I expect you to offer him your full assistance in whatever he needs.")
                .player("<p,neutral>Well... okay then.").end();
        }
        if (s == KILLED_DOG) {
            return d.npc("<p,quiz>Well hello there. What do you want?")
                .npc("<p,neutral>You have news of Drezel for me?")
                .player("<p,happy>Yeah, I spoke to the guys at the temple and they said they were being bothered by that dog in the crypt, so I went and killed it for them. No problem.")
                .npc("<p,shock>YOU DID WHAT???")
                .npc("<p,angry>Are you mentally deficient??? That guard dog was protecting the route to Morytania! Without it we could be in severe peril of attack!")
                .player("<p,sad>Did I make a mistake?")
                .npc("<p,angry>YES YOU DID!!!!! You need to get there right now and find out what is happening! Before it is too late for us all!")
                .player("<p,sad>B-but Drezel TOLD me to...!")
                .npc("<p,angry>No, you absolute cretin! Obviously some fiend has done something to Drezel and tricked your feeble intellect into helping them kill that guard dog!")
                .act(q -> set(q, RETURN_TO_DREZEL))
                .npc("<p,angry>You get back there and do whatever is necessary to safeguard my kingdom from attack, or I will see you beheaded for high treason!")
                .player("<p,sad>Y-yes your highness.").end();
        }
        if (s == RETURN_TO_DREZEL) {
            return d.npc("<p,angry>YOU! Why haven't you ensured the border with Morytania is secure yet?")
                .player("<p,sad>Okay, okay... I'm going, I'm going... There's no need to shout...")
                .npc("<p,angry>NO NEED TO SHOUT???")
                .npc("<p,angry>Listen, and listen well, and see if your puny mind can comprehend this: if the border is not protected, then we are all at the mercy of the vampires!")
                .npc("<p,angry>I would say that me shouting at you for your incompetence is the LEAST of your worries right now. NOW GO!").end();
        }
        if (s == FIND_DREZEL_KEY) {
            return d.npc("<p,quiz>You have news of Drezel for me?")
                .player("<p,sad>I do indeed, sire. He has been imprisoned by some Zamorakian monks.")
                .npc("<p,angry>What? This is wholly unacceptable! I order you to do all that you can to free Drezel immediately!")
                .player("<p,neutral>I was doing that anyway.")
                .npc("<p,neutral>Ah, I see. In that case keep up the good work.").end();
        }
        if (s == UNLOCKED_DREZEL) {
            return d.npc("<p,quiz>Have you freed Drezel yet?")
                .player("<p,neutral>Well, I found the key to his cell and unlocked it, but there's a vampire in there stopping him leaving.")
                .npc("<p,shock>A vampire? Well, I order you to do something about it at once!")
                .player("<p,neutral>Yeah, I was planning on doing that anyway.")
                .npc("<p,neutral>Good work! Always a place for quick thinkers in my kingdom!").end();
        }
        if (s == POURED_BLESSED_WATER) {
            return d.npc("<p,quiz>Have you freed Drezel yet?")
                .player("<p,neutral>Yes sire.")
                .npc("<p,quiz>Aha! Good work adventurer! What news of the border defences against Morytania?")
                .player("<p,confused>I, uh, don't know about that...")
                .npc("<p,angry>Well get back and find out at once! This is a matter of national security and if I find out we are vulnerable I will hold you personally responsible!")
                .player("<p,neutral>Yes sire.").end();
        }
        // MEET_IN_MAUSOLEUM up to the last essence (LostCity has no line for the unused value 9)
        return d.npc("<p,quiz>Have you ensured Misthalin's border is fully secured?")
            .player("<p,neutral>Not yet. I'm working on it though.")
            .npc("<p,neutral>Good, good.").end();
    }

    // ---------------------------------------------------------------- temple doors
    private QuestDlg knock(Player p) {
        int s = lc(p);
        if (s == STARTED) {
            return D().box("You knock at the door...You hear a voice from inside. @dbl@Who are you|and what do you want?")
                .player("<p,quiz>Ummmm.....")
                .optionsQuiet("Roald sent me to check on Drezel.", "drezel", "Hi, I just moved in next door...", "moved",
                    "I hear this place is of historic interest.", "historic", "The council sent me to check your pipes.", "pipes")
                .label("drezel")
                .player("<p,happy>Roald sent me to check on Drezel.")
                .box("@dbl@(Psst... Hey... Who's Roald? Who's Drezel?)@dre@(Uh... isn't Drezel that|dude upstairs? Oh, wait, Roald's the King of Varrock right?)@dbl@(He is???|Aw man... Hey, you deal with this okay?) He's just coming! Wait a|second!@dre@ Hello, my name is Drevil. @dbl@(Drezel!) @dre@I mean Drezel. How can|I help?")
                .player("<p,neutral>Well, as I say, the King sent me to make sure everything's okay with you.")
                .box("@dre@And, uh, what would you do if everything wasn't okay with me?")
                .player("<p,neutral>I'm not sure. Ask you what help you need I suppose.")
                .box("@dre@Ah, good, well, I don't think... @dbl@(Psst... hey... the dog!) @dre@OH! Yes, of|course! Will you do me a favour adventurer?")
                .optionsQuiet("Sure.", "help", "Nope.", "nope")
                .label("help")
                .player("<p,neutral>Sure. I'm a helpful person!")
                .act(q -> set(q, AGREE_TO_KILL_DOG))
                .box("@dre@HAHAHAHA! Really? Thanks buddy!|You see that mausoleum out there?|There's a horrible big dog underneath it|that I'd like you to kill for me!")
                .box("@dre@It's been really bugging me!|Barking all the time and stuff!|Please kill it for me buddy!")
                .player("<p,happy>Okey-dokey, one dead dog coming up.").end()
                .label("nope")
                .player("<p,neutral>Nope. Something about all this is very suspicious...")
                .box("@dre@Get lost then! I have important things to do, as sure as my name is|Dibzil. @dbl@(Drezel!)").end()
                .label("moved")
                .player("<p,happy>Hi, I just moved in next door...")
                .player("<p,quiz>Can I borrow a cup of coffee?")
                .box("@dbl@What next door? What's coffee? Who ARE you?").end()
                .label("historic")
                .player("<p,happy>I hear this place is of historic interest.")
                .player("<p,quiz>Can I come in and have a wander around? Possibly look at some antiques or buy something from your gift shop?")
                .box("@dbl@(Pssst... Hey... Is this place of historic interest?) @dre@(I dunno. I guess it|might be. Does it matter?) @dbl@(I suppose not.) Clear off! You can't come|in!").end()
                .label("pipes")
                .player("<p,happy>The council sent me to check your pipes.")
                .box("@dbl@They did? Ummm.... (Psst... Are there any pipes in here, you|reckon?) @dre@(I dunno... I don't think so...) @dbl@We don't have any thanks!|Bye!").end();
        }
        if (s == AGREE_TO_KILL_DOG) {
            return D().box("You knock at the door...You hear a voice from inside.@dbl@You again?|@dbl@What do you want now?")
                .player("<p,quiz>What am I supposed to be doing again?")
                .box("@dbl@Ah! I want you to go kill the horrible dog in the mausoleum for me!|You can use the entrance out there. You'll do this for good old|Delzig, won't ya buddy? @dre@(Drezel!) @dbl@*cough* for good old Drezel, right|buddy?").end();
        }
        if (s == KILLED_DOG) {
            return D().box("You knock at the door...You hear a voice from inside.@dbl@You again?|@dbl@What do you want now?")
                .player("<p,happy>I killed that dog for you.")
                .box("@dbl@HAHAHAHAHA! Really? Hey, that's great! @dre@Yeah thanks a lot buddy!|HAHAHAHAHAHA")
                .player("<p,quiz>What's so funny?")
                .box("@dbl@HAHAHAHA nothing buddy!|@dbl@We're just so grateful to you! HAHAHA|@dre@Yeah, maybe you should go tell the King|what a great job you did buddy! HAHAHA").end();
        }
        return D().box("You knock at the door...You hear a voice from inside. @dbl@Who are you|and what do you want?")
            .player("<p,quiz>Ummmm.....")
            .optionsQuiet("Hi, I just moved in next door...", "moved", "I hear this place is of historic interest.", "historic",
                "The council sent me to check your pipes.", "pipes")
            .label("moved")
            .player("<p,happy>Hi, I just moved in next door...")
            .player("<p,quiz>Can I borrow a cup of coffee?")
            .box("@dbl@What next door? What's coffee? Who ARE you?").end()
            .label("historic")
            .player("<p,happy>I hear this place is of historic interest.")
            .player("<p,quiz>Can I come in and have a wander around? Possibly look at some antiques or buy something from your gift shop?")
            .box("@dbl@(Pssst... Hey... Is this place of historic interest?) @dre@(I dunno. I guess it|might be. Does it matter?) @dbl@(I suppose not.) Clear off! You can't come|in!").end()
            .label("pipes")
            .player("<p,happy>The council sent me to check your pipes.")
            .box("@dbl@They did? Ummm.... (Psst... Are there any pipes in here, you|reckon?) @dre@(I dunno... I don't think so...) @dbl@We don't have any thanks!|Bye!").end();
    }

    // ---------------------------------------------------------------- Drezel in his cell
    private QuestDlg drezel(Player p) {
        int s = lc(p);
        if (s == UNLOCKED_DREZEL) {
            QuestDlg d = D().player("<p,neutral>The key fitted the lock! You're free to leave now!")
                .npc("<p,happy>Well excellent work adventurer! Unfortunately, as you know, I cannot risk waking that vampire in the coffin.");
            if (has(p, BLESSED_WATER)) {
                return d.player("<p,quiz>I have some blessed water from the Salve in this bucket. Do you think it would help against that vampire?")
                    .npc("<p,happy>Yes! Great idea! If his coffin is doused in the blessed water he will be unable to leave it! Use it on his coffin, quickly!").end();
            }
            if (has(p, MURKY_WATER)) return blessWater(d);
            return d.player("<p,neutral>Do you have any ideas about dealing with the vampire?")
                .npc("<p,neutral>Well, the water of the Salve should still have enough power to work against the vampire despite what those Zamorakians might have done to it...")
                .npc("<p,neutral>Maybe you should try and get hold of some from somewhere?").end();
        }
        if (s == POURED_BLESSED_WATER) {
            return D().player("<p,neutral>I poured the blessed water over the vampires coffin. I think that should trap him in there long enough for you to escape.")
                .act(q -> set(q, MEET_IN_MAUSOLEUM))
                .npc("<p,neutral>Excellent work adventurer! I am free at last! Let me ensure that evil vampire is trapped for good. I will meet you down by the monument.")
                .npc("<p,neutral>Look for me down there. I need to assess what damage has been done to our holy barrier by those evil Zamorakians!").end();
        }
        if (s >= MEET_IN_MAUSOLEUM) {
            return D().npc("<p,neutral>I will meet you downstairs by the monuments. I must see what damage has been done. Meet me there, I fear I may require more of your assistance.")
                .player("<p,neutral>Okay.").end();
        }
        return null;
    }

    /** LostCity drezel_bless_water. */
    private static QuestDlg blessWater(QuestDlg d) {
        return d.player("<p,neutral>I have some water from the Salve. It seems to have been desecrated though. Do you think you could bless it for me?")
            .npc("<p,neutral>Yes, good thinking adventurer! Give it to me, I will bless it!")
            .act(q -> {
                if (!has(q, MURKY_WATER)) return;
                msg(q, "The priest blesses the water for you.");
                take(q, MURKY_WATER);
                give(q, BLESSED_WATER);
            }).end();
    }

    /** LostCity oploc2 on pip_prisondoor: talking to Drezel through the bars. */
    private QuestDlg cellTalk(Player p) {
        int s = lc(p);
        if (s == RETURN_TO_DREZEL) {
            return D().player("<p,neutral>Hello.")
                .npc("<p,shock>Oh! You do not appear to be one of those Zamorakians who imprisoned me here! Who are you and why are you here?")
                .player("<p,neutral>My name's " + p.getUsername() + ". King Roald sent me to find out what was going on at the temple. I take it you are Drezel?")
                .npc("<p,happy>That is right! Oh, praise be to Saradomin! All is not yet lost! I feared that when those Zamorakians attack this place and imprisoned")
                .npc("<p,happy>me up here, Misthalin would be doomed! If they should manage to desecrate the holy river Salve, we would be defenceless against Morytania!")
                .player("<p,neutral>How is a river a good defence then?")
                .npc("<p,happy>Well, it is a long tale, and I am not sure we have time!")
                .optionsQuiet("Tell me anyway.", "anyway", "You're right, we don't.", "dont")
                .label("dont")
                .player("<p,neutral>You're right, we don't. It doesn't matter anyway.")
                .npc("<p,neutral>Well, let's just say if we cannot undo whatever damage has been done here, the entire land is in grave peril!").end()
                .label("anyway")
                .player("<p,neutral>Tell me anyway. I'd like to know the full facts before acting any further.")
                .npc("<p,neutral>Ah, Saradomin has granted you wisdom I see. Well, the story of the river Salve and of how it protects Misthalin is the story of this temple,")
                .npc("<p,neutral>and of the seven warrior priests who died here long ago, from whom I am descended. Once long ago Misthalin did not have the borders that")
                .npc("<p,neutral>it currently does. This entire area, as far West as Varrock itself was under the control of an evil god. There was frequent skirmishing")
                .npc("<p,neutral>along the borders, as the brave heroes of Varrock fought to keep the evil creatures that now are trapped on the eastern side of the River Salve from over running")
                .npc("<p,neutral>the human encampments, who worshipped Saradomin. Then one day, Saradomin himself appeared to one of our mighty heroes, whose name has been forgotten by history,")
                .npc("<p,neutral>and told him that should we be able to take the pass that this temple now stands in, Saradomin would use his power to bless this river, and make it")
                .npc("<p,neutral>impassable to all creatures with evil in their hearts. This unknown hero grouped together all of the mightiest fighters whose hearts were pure")
                .npc("<p,neutral>that he could find, and the seven of them rode here to make a final stand. The enemies swarmed across the Salve but they did not yield.")
                .npc("<p,neutral>For ten days and nights they fought, never sleeping, never eating, fuelled by their desire to make the world a better place for humans to live.")
                .npc("<p,neutral>On the eleventh day they were to be joined by reinforcements from a neighbouring encampment, but when those reinforcements arrived all they found")
                .npc("<p,neutral>were the bodies of these seven brave but unknown heroes, surrounded by the piles of the dead creatures of evil that had tried to defeat them.")
                .npc("<p,neutral>The men were saddened at the loss of such pure and mighty warriors, yet their sacrifice had not been in vain; for the water of the Salve")
                .npc("<p,neutral>had indeed been filled with the power of Saradomin, and the evil creatures of Morytania were trapped beyond the river banks forever, by their own evil.")
                .npc("<p,neutral>In memory of this brave sacrifice my ancestors built this temple so that the land would always be free of the evil creatures")
                .npc("<p,neutral>who wish to destroy it, and laid the bodies of those brave warriors in tombs of honour below this temple with golden gifts on the tombs as marks of respect.")
                .npc("<p,neutral>They also built a statue on the river mouth so that all who might try and cross into Misthalin from Morytania would know that these lands are protected")
                .npc("<p,neutral>by the glory of Saradomin and that good will always defeat evil, no matter how the odds are stacked against them.")
                .player("<p,quiz>Ok. I can see how the river protects the border, but I can't see how anything could affect that from this temple.")
                .npc("<p,neutral>Well, as much as it saddens me to say so adventurer, Lord Saradomin's presence has not been felt on the land for many years now, and even")
                .npc("<p,neutral>though all true Saradominists know that he watches over us, his power upon the land is not as strong as it once was.")
                .npc("<p,neutral>I fear that should those Zamorakians somehow pollute the Salve and desecrate his blessings, his power might not be able to stop")
                .npc("<p,neutral>the army of evil that lurks to the east, longing for the opportunity to invade and destroy us all!")
                .npc("<p,quiz>So, what do you say adventurer? Will you aid me and all of Misthalin in foiling this Zamorakian plot?")
                .optionsQuiet("Yes.", "yes", "No.", "no")
                .label("no")
                .player("<p,neutral>No.")
                .npc("<p,sad>Oooooh... I knew it was too good to be true... Then leave me to my fate villain, there's no need to taunt me as well as keeping me imprisoned.").end()
                .label("yes")
                .player("<p,neutral>Yes, of course. Any threat to Misthalin must be neutralised immediately. So what can I do to help?")
                .npc("<p,neutral>Well, the immediate problem is that I am trapped in this cell. I know that the key to free me is nearby, for none of the Zamorakians")
                .npc("<p,neutral>who imprisoned me here were ever gone for long periods of time. Should you find the key however, as you may have noticed,")
                .npc("<p,neutral>there is a vampire in that coffin over there. I do not know how they managed to find it, but it is one of the ones that somehow")
                .npc("<p,neutral>survived the battle here all those years ago, and is by now quite, quite, mad. It has been trapped on this side of the river for centuries,")
                .npc("<p,neutral>and as those fiendish Zamorakians pointed out to me with delight, as a descendant of one of those who trapped it here, it will recognise")
                .npc("<p,neutral>the smell of my blood should I come anywhere near it. It will of course then wake up and kill me, very probably slowly and painfully.")
                .player("<p,quiz>Maybe I could kill it somehow then while it is asleep?")
                .npc("<p,neutral>No adventurer, I do not think it would be wise for you to wake it at all. As I say, it is little more than a wild animal, and must")
                .npc("<p,neutral>be extremely powerful to have survived until today. I suspect your best chance would be to incapacitate it somehow.")
                .act(q -> set(q, FIND_DREZEL_KEY))
                .player("<p,neutral>Okay, find the key to your cell, and do something about the vampire.")
                .npc("<p,neutral>When you have done both of those I will be able to inspect the damage which those Zamorakians have done to the purity of the Salve.")
                .npc("<p,neutral>Depending on the severity of the damage, I may require further assistance from you in restoring its purity.")
                .player("<p,neutral>Okay, well first thing's first; let's get you out of here.").end();
        }
        // FIND_DREZEL_KEY
        QuestDlg d = D().npc("<p,neutral>How goes it adventurer? Any luck in finding the key to the cell or a way of stopping the vampire yet?");
        if (has(p, IRON_KEY)) {
            return d.player("<p,neutral>I have this key I took from one of the monuments underground.")
                .npc("<p,happy>Excellent work adventurer! Quickly, try it on the door, and see if it will free me!").end();
        }
        if (has(p, GOLD_KEY)) {
            return d.player("<p,neutral>I have this key I took from one of those Zamorakian monks!")
                .npc("<p,happy>Excellent work adventurer! Quickly, try it on the door, and see if it will free me!").end();
        }
        return d.player("<p,neutral>No, not yet...")
            .npc("<p,happy>Well don't give up adventurer! That key MUST be around here somewhere! I know none of those Zamorakians ever got very far from this building!")
            .player("<p,neutral>How do you know that?")
            .npc("<p,happy>I could hear them laughing about some gullible fool that they tricked into killing the guard dog at the monument.")
            .player("<p,neutral>Oh.")
            .npc("<p,happy>Honestly, what kind of idiot would go around killing things just because a stranger told them to? What kind of oafish, numb-skulled, dim-witted,")
            .player("<p,neutral>Okay, OKAY, I get the picture!").end();
    }

    // ---------------------------------------------------------------- Drezel by the monuments
    private QuestDlg drezel2(Player p) {
        if (p.getQuestState(ID, true) == REWARD_PENDING) return D().act(this::awardCompletionRewards).end();
        int s = lc(p);
        if (s == MEET_IN_MAUSOLEUM) {
            return D().npc("<p,neutral>Ah, " + p.getUsername() + ". I see you finally made it down here. Things are worse than I feared. I'm not sure if I will be able to repair the damage.")
                .player("<p,quiz>Why, what's happened?")
                .npc("<p,sad>From what I can tell, after you killed the guard dog who protected the entrance to the monuments, those Zamorakians forced the door into the main chamber")
                .npc("<p,sad>and have used some kind of evil potion upon the well which leads to the source of the river Salve. As they have done this at the very mouth of the river")
                .npc("<p,sad>it will spread along the entire river, disrupting the blessing placed upon it and allowing the evil creatures of Morytania to invade at their leisure.")
                .player("<p,shock>What can we do to prevent that?")
                .npc("<p,neutral>Well, as you can see, I have placed a holy barrier on the entrance to this room from the South, but it is not very powerful and requires me to remain")
                .npc("<p,neutral>here focussing upon it to keep it intact. Should an attack come, they would be able to break this defence very quickly indeed. What we need to do is")
                .npc("<p,neutral>find some kind of way of removing or counteracting the evil magic that has been put into the river source at the well, so that the river will flow pure once again.")
                .player("<p,quiz>Couldn't you bless the river to purify it? Like you did with the water I took from the well?")
                .npc("<p,sad>No, that would not work, the power I have from Saradomin is not great enough to cleanse an entire river of this foul Zamorakian pollutant.")
                .npc("<p,neutral>I have only one idea how we could possibly cleanse the river.")
                .player("<p,quiz>What's that?")
                .npc("<p,neutral>I have heard rumours recently that Mages have found some secret ore that absorbs magic into it and allows them to create runes.")
                .npc("<p,neutral>Should you be able to collect enough of this ore, it is possible it will soak up the evil potion that has been poured into the river, and purify it.")
                .player("<p,quiz>Kind of like a filter? Okay, I guess it's worth a try. How many should I get?")
                .act(q -> set(q, BEGIN_BRING_ESSENCE))
                .npc("<p,neutral>Well I have no knowledge of these ores other than speculation and gossip, but if the things I hear are true around fifty should be sufficient for the task.").end();
        }
        if (s >= BEGIN_BRING_ESSENCE && s < END_BRING_ESSENCE) {
            boolean first = s == BEGIN_BRING_ESSENCE;
            if (has(p, ESSENCE)) {
                return giveEssence(D().player(first ? "<p,happy>I brought you some Rune Essence." : "<p,happy>I brought you some more essences.")
                    .npc(first ? "<p,neutral>Quickly, give it to me!" : "<p,neutral>Quickly, give them to me!"));
            }
            if (has(p, ESSENCE_NOTE)) return essenceNotes();
            if (first) {
                return D().player("<p,quiz>What am I supposed to do again?")
                    .npc("<p,neutral>Bring me fifty Rune Essence so that I can undo the damage done by those Zamorakians.").end();
            }
            return D().player("<p,quiz>How many more essences do I need to bring you?")
                .npc("<p,neutral>I need " + (END_BRING_ESSENCE - s) + " more").end();
        }
        if (s == COMPLETE) return p.ownsItem(WOLFBANE) ? accessBarrier(p) : reclaimWolfbane();
        if (s == ACCESS_HOLY_BARRIER) return p.ownsItem(WOLFBANE) ? postQuest() : reclaimWolfbane();
        return null;
    }

    /** LostCity drezel_give_blankrunes: hands over up to the essences still needed. */
    private QuestDlg giveEssence(QuestDlg d) {
        return d.act(q -> {
                int s = lc(q);
                if (!has(q, ESSENCE) || s < BEGIN_BRING_ESSENCE || s >= END_BRING_ESSENCE) { nothing(q); return; }
                int given = Math.min(END_BRING_ESSENCE - s, count(q, ESSENCE));
                q.getInventoryManager().removeItem(new ItemStack(ESSENCE, given));
                s += given;
                q.setQuestState(ID, s == END_BRING_ESSENCE ? REWARD_PENDING : s + 1);
                // LostCity counts the loop variable down to 0 before choosing, so this is always the plural
                msg(q, "You give the priest your blank runes.");
            })
            .jumpIf(q -> q.getQuestState(ID, true) == REWARD_PENDING, "finish")
            .act(PriestInPerilQuest::close).end()
            .label("finish")
            .npc("<p,happy>Excellent! That should do it! I will bless these stones and place them within the well, and Misthalin should be protected once more!")
            .npc("<p,happy>Please take this dagger; it has been handed down within my family for generations and is filled with the power of Saradomin. You will find that")
            .npc("<p,happy>it has the power to prevent werewolves from adopting their wolf form in combat as long as you have it equipped.")
            .act(q -> { if (q.getQuestState(ID, true) == REWARD_PENDING) awardCompletionRewards(q); }).endKeep();
    }

    /** LostCity drezel_blankrune_cert. */
    private static QuestDlg essenceNotes() {
        return D().player("<p,happy>I brought you some Rune Essence.")
            .npc("<p,confused>You have brought me notes saying 'I promise to pay the bearer on demand the listed number of Rune Essence.' How is that supposed to help me?").end();
    }

    /** LostCity drezel_access_holy_barrier. */
    private QuestDlg accessBarrier(Player p) {
        return D().player("<p,quiz>So can I pass through that barrier now?")
            .npc("<p,neutral>Ah, " + p.getUsername() + ". For all the assistance you have given both myself and Misthalin in your actions, I cannot let you pass without warning you.")
            .npc("<p,neutral>Morytania is an evil land, filled with creatures and monsters more terrifying than any you have yet encountered. Although I will pray for you,")
            .npc("<p,neutral>You should take some basic precautions before heading over the Salve into it. The first place you will come across is the Werewolf trading post.")
            .npc("<p,neutral>In many ways Werewolves are like you and me, except never forget that they are evil vicious beasts at heart. The dagger I have given you is named 'Wolfbane'")
            .npc("<p,neutral>and it is a holy relic that prevents the werewolf people from changing form. I suggest if you battle with them that you keep it always equipped, for their")
            .npc("<p,neutral>wolf form is incredibly powerful, and would savage you very quickly. Please adventurer, promise me this: I should hate for you to die foolishly.")
            .player("<p,neutral>Okay, I will keep it equipped whenever I fight werewolves.")
            .act(q -> set(q, ACCESS_HOLY_BARRIER)).end();
    }

    /** LostCity reclaim_wolfbane_dagger. */
    private static QuestDlg reclaimWolfbane() {
        return D().player("I've lost my wolfbane dagger.")
            .act(q -> give(q, WOLFBANE))
            .npc("<p,neutral>Yes, I know! Luckily for you it washed up on the banks of the Salve earlier! Here, take it again, but please try and be more careful this time.")
            .npc("<p,neutral>It's a family heirloom after all!")
            .player("<p,neutral>Thanks for that!").end();
    }

    /**
     * LostCity drezel_post_priestperil_dialogue, %druidspirit not started. Nature Spirit is not on this server, so the
     * conversation stops where accepting it would begin the quest.
     */
    private static QuestDlg postQuest() {
        return D().npc("<p,neutral>Greetings again adventurer. How go your travels in Moryania? Is it as evil as I have heard?")
            .optionsQuiet("Well, I'm going to look around a bit more.", "look", "Is there anything else interesting to do around here?", "interesting")
            .label("look")
            .player("<p,neutral>Well, I'm going to look around a bit more.")
            .npc("<p,neutral>Well, that sounds like a good idea. Don't get into any trouble though!").end()
            .label("interesting")
            .player("<p,quiz>Is there anything else interesting to do around here?")
            .npc("<p,neutral>Well, not a great deal... but there is something you could do for me if you're interested. Though it is quite dangerous.")
            .optionsQuiet("Sorry, not interested...", "no", "Well, what is it, I may be able to help?", "what")
            .label("no")
            .player("<p,neutral>Sorry, not interested.")
            .npc("<p,neutral>Well, I understand. You must be busy.").end()
            .label("what")
            .player("<p,neutral>Well, what is it, I may be able to help?")
            .npc("<p,neutral>There's a man called Filliman who lives in Mort Myre, I wonder if you could look for him? The swamps of Mort Myre are dangerous though, they're infested with Ghasts!").end();
    }

    /** LostCity drezel_cant_access_holy_barrier (Drezel calls out when the player touches the barrier too early). */
    private QuestDlg barrierRefused(Player p) {
        if (npcNear(p, DREZEL2, 20) == null) return null;
        if (lc(p) < COMPLETE) {
            return D().npc("<p,angry>STOP!")
                .player("<p,quiz>Can't I go through here?")
                .npc("<p,neutral>No, you cannot! It is taking all of my willpower to hold that barrier in place. You must restore the sanctity of the Salve as soon as possible!").end();
        }
        return D().npc("<p,angry>STOP!")
            .player("<p,quiz>Can't I go through here?")
            .npc("<p,angry>Yes, now the Salve is restored you may, but speak to me first, as I have advice for you before you pass through.").end();
    }

    // ---------------------------------------------------------------- objects
    @Override
    public boolean handleFirstObjectAction(Player p, int id, int x, int y, int stage) {
        int s = lc(p);
        if (id == TEMPLE_DOOR_L || id == TEMPLE_DOOR_R) {
            if (x != 3408 || plane(p) != 0) return false;
            if (outsideTemple(p) && s < RETURN_TO_DREZEL) { msg(p, "This door is securely locked from inside."); return true; }
            throughTempleDoors(p);
            return true;
        }
        if (id == PRISON_DOOR) {
            if (s >= UNLOCKED_DREZEL) throughDoor(p, PRISON_DOOR, x, y, 1, 0);
            else msg(p, "The cell door is locked shut.");
            return true;
        }
        if (id == UNDERGROUND_DOOR1 || id == UNDERGROUND_DOOR2) {
            if (s < (id == UNDERGROUND_DOOR1 ? FIND_DREZEL_KEY : MEET_IN_MAUSOLEUM)) { msg(p, "This door is locked shut."); return true; }
            throughDoor(p, id, x, y, id == UNDERGROUND_DOOR2 ? 1 : 0, id == UNDERGROUND_DOOR2 ? 0 : -1);
            return true;
        }
        if (id == BARRIER) {
            if (s == ACCESS_HOLY_BARRIER) {
                p.moveTo(new Position(3423, 3485, 0));
                msg(p, "You pass through the holy barrier.");
            } else {
                QuestDlg d = barrierRefused(p);
                if (d != null) say(p, DREZEL2, d);
            }
            return true;
        }
        if (id == COFFIN) {
            say(p, DREZEL, s >= POURED_BLESSED_WATER
                ? D().player("<p,confused>The vampire should be dealt with. I'd still best not risk it though.").end()
                : D().player("<p,confused>It sounds like there's something alive inside it. I don't think it would be a very good idea to open it...").end());
            return true;
        }
        if (id == WELL) {
            say(p, DREZEL, D().box(s < COMPLETE
                ? "You look down the well and see the filthy polluted water of the River Salve moving slowly along."
                : "You look down the well and see the fresh water of the River Salve moving swiftly along.").end());
            return true;
        }
        if (id >= GRAVE_FIRST && id <= GRAVE_LAST) {
            studyMonument(p, id - GRAVE_FIRST + 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleSecondObjectAction(Player p, int id, int x, int y, int stage) {
        int s = lc(p);
        if (id == TEMPLE_DOOR_L || id == TEMPLE_DOOR_R) {
            if (x != 3408 || plane(p) != 0) return false;
            if (outsideTemple(p)) say(p, DREZEL, knock(p));
            else throughTempleDoors(p);
            return true;
        }
        if (id == PRISON_DOOR) {
            if (npcNear(p, DREZEL, 15) == null) return true;
            if (s == RETURN_TO_DREZEL || s == FIND_DREZEL_KEY) say(p, DREZEL, cellTalk(p));
            else if (s >= UNLOCKED_DREZEL) throughDoor(p, PRISON_DOOR, x, y, 1, 0);
            else nothing(p);
            return true;
        }
        if (id >= GRAVE_FIRST && id <= GRAVE_LAST) {
            takeFromMonument(p, id - GRAVE_FIRST + 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleItemOnObject(Player p, int item, int obj, int stage) {
        int s = lc(p);
        if (obj == PRISON_DOOR) {
            if (item == IRON_KEY && npcNear(p, DREZEL, 15) != null) {
                if (s >= UNLOCKED_DREZEL) {
                    say(p, DREZEL, D().npc("<p,shock>Aargh! What are you trying to lock me back in here for?").end());
                    return true;
                }
                set(p, UNLOCKED_DREZEL);
                take(p, IRON_KEY);
                say(p, DREZEL, D().npc("<p,happy>Oh! Thank you! You have found the key!").act(q -> msg(q, "You unlock the cell door.")).end());
                return true;
            }
            if (item == GOLD_KEY) { say(p, DREZEL, D().box("The key is a similar size to the lock, but does not fit.").end()); return true; }
            nothing(p);
            return true;
        }
        if (obj == COFFIN) {
            if (item == BLESSED_WATER) {
                if (s < POURED_BLESSED_WATER) set(p, POURED_BLESSED_WATER);
                msg(p, "You pour the blessed water over the coffin...");
                p.packetSender.sendStillGraphicToNearbyPlayers(COFFIN_SPELL, 3413, 3486, 2, 0);
                p.packetSender.sendStillGraphicToNearbyPlayers(COFFIN_SPELL, 3413, 3487, 2, 0);
                p.getUpdateState().setAnimation(THROW_BUCKET);
                take(p, BLESSED_WATER);
                give(p, BUCKET);
                return true;
            }
            if (item == BUCKET_WATER) {
                say(p, DREZEL, D().player("<p,confused>I don't think pouring normal water on the coffin is going to help...").end());
                return true;
            }
            if (item == MURKY_WATER) {
                say(p, DREZEL, D().player("<p,confused>This water doesn't look particularly holy to me... I think I'd better check with the priest first.").end());
                return true;
            }
            nothing(p);
            return true;
        }
        if (obj == WELL) {
            if (item != BUCKET) { nothing(p); return true; }
            msg(p, "You fill the bucket from the well.");
            p.getUpdateState().setAnimation(PICKUP_TABLE);
            take(p, BUCKET);
            give(p, MURKY_WATER);
            p.packetSender.sendSoundEffect(WELL_FILL, 1, 0);
            return true;
        }
        if (obj >= GRAVE_FIRST && obj <= GRAVE_LAST) {
            switchMonumentItem(p, obj - GRAVE_FIRST + 1, item);
            return true;
        }
        return false;
    }

    // ---------------------------------------------------------------- monuments
    /** Which of the seven gold items lies on this grave (LostCity: (seed + grave * 17) mod 7, plus 1). */
    private static int content(Player p, int grave) { return (seed(p) + grave * 17) % 7 + 1; }
    private static boolean swapped(Player p, int grave) { return bit(p, grave - 1); }

    private void studyMonument(Player p, int grave) {
        if (lc(p) >= MEET_IN_MAUSOLEUM) { msg(p, "A monument dedicated to the fallen."); return; }
        if (!bit(p, LAYOUT_MADE)) {
            int layout = new java.util.Random().nextInt(100);
            p.questProgressFlags[ID] = (p.questProgressFlags[ID] & ~(127 << SEED_SHIFT)) | (layout << SEED_SHIFT);
            setBit(p, LAYOUT_MADE);
        }
        int c = content(p, grave);
        p.packetSender.sendInterfaceModel(MONUMENT_MODEL, 250, swapped(p, grave) ? PLAIN_ITEM[c] : GOLD_ITEM[c]);
        p.packetSender.sendInterfaceText(MONUMENT_TEXT[c], MONUMENT_TEXT_ID);
        p.packetSender.showInterface(MONUMENT_IF);
    }

    private void takeFromMonument(Player p, int grave) {
        if (lc(p) >= MEET_IN_MAUSOLEUM) { msg(p, "It would be wrong to dishonour this monument."); return; }
        int c = content(p, grave);
        String item = swapped(p, grave) ? name(PLAIN_ITEM[c]) : SHORT_NAME[c];
        msg(p, "You try and take the " + item.toLowerCase() + " from the monument...");
        msg(p, "A holy power prevents you stealing from the monument!");
        p.applyDirectHit(1 + new java.util.Random().nextInt(5), HitType.NORMAL);
    }

    private void switchMonumentItem(Player p, int grave, int used) {
        if (lc(p) >= MEET_IN_MAUSOLEUM) { msg(p, "It would be wrong to dishonour this monument."); return; }
        int c = content(p, grave);
        int gold = GOLD_ITEM[c];
        if (!swapped(p, grave) || gold == IRON_KEY) {
            if (used == GOLD_KEY && gold == IRON_KEY && p.ownsItem(IRON_KEY)) {
                msg(p, "You have already swapped the golden key for the iron key.");
                return;
            }
            if (used == IRON_KEY && gold == IRON_KEY) {
                say(p, DREZEL, D().player("<p,quiz>I think this key is more useful to me right now than the Golden key is.").end());
                return;
            }
            if (used == PLAIN_ITEM[c]) {
                msg(p, "You swap the " + name(used) + " for the " + name(gold) + ".");
                p.getUpdateState().setAnimation(OPEN_CUPBOARD);
                take(p, used);
                give(p, gold);
                setBit(p, grave - 1);
                return;
            }
        } else {
            if (used == gold) {
                say(p, DREZEL, D().player("<p,confused>You know... I think I'd rather keep the valuable solid gold " + SHORT_NAME[c] + ".").end());
                return;
            }
            if (used == PLAIN_ITEM[c]) {
                say(p, DREZEL, D().box("You have already taken the Golden " + SHORT_NAME[c] + ".").end());
                return;
            }
        }
        nothing(p);
    }

    // ---------------------------------------------------------------- npcs
    @Override
    public boolean handleItemOnNpc(Player p, int npc, int item, int stage) {
        if (npc == DREZEL) {
            if (item == MURKY_WATER) say(p, DREZEL, blessWater(D()));
            else nothing(p);
            return true;
        }
        if (npc == DREZEL2) {
            int s = lc(p);
            if (s < BEGIN_BRING_ESSENCE || s >= END_BRING_ESSENCE) { nothing(p); return true; }
            if (item == ESSENCE) say(p, DREZEL2, giveEssence(D()));
            else if (item == ESSENCE_NOTE) say(p, DREZEL2, essenceNotes());
            else nothing(p);
            return true;
        }
        return false;
    }

    /** LostCity priestperilguarddog_attack: only while Drezel's "favour" is outstanding. */
    @Override
    public boolean canAttackNpc(Player p, int npc, int stage) {
        if (npc != DOG) return true;
        int s = lc(p);
        if (s < AGREE_TO_KILL_DOG || s == KILLED_DOG) { msg(p, "You have no reason to attack a helpless dog!"); return false; }
        if (s > KILLED_DOG) { msg(p, "I'd better not make the King mad at me again."); return false; }
        return true;
    }

    @Override
    public boolean handleNpcKill(Player p, int npc, int stage) {
        if (npc == DOG && lc(p) == AGREE_TO_KILL_DOG) set(p, KILLED_DOG);
        return false;
    }

    /** LostCity ai_queue3 for priestperilevilmonk3: the golden key drops for its attacker until Drezel is free. */
    @Override
    public boolean handleNpcDeathDrop(Player p, int npc, Position pos, int stage) {
        if (npc != MONK3 || lc(p) >= UNLOCKED_DREZEL) return false;
        GroundItemManager.getInstance().spawn(new GroundItem(new ItemStack(GOLD_KEY, 1), p, pos));
        return false;
    }

    // ---------------------------------------------------------------- journal
    @Override
    public final String[] buildQuestJournal(Player p, int state) {
        int s = lc(p);
        if (s == NOT_STARTED) {
            return "@dbl@I can start this quest by speaking to @dre@King Roald@dbl@ in @dre@Varrock Palace||@dbl@I must be able to defeat a @dre@level 30 enemy".split("\\|", -1);
        }
        StringBuilder t = new StringBuilder("@str@I spoke to King Roald who asked me to investigate why his|@str@friend Priest Drezel has stopped communicating with him.|");
        if (s == STARTED) {
            t.append("|@dre@Drezel@dbl@ lives in a @dre@temple@dbl@ to the @dre@east@dbl@ of Varrock Palace. I should head there and @dre@investigate@dbl@ what's happened to him");
            return t.toString().split("\\|", -1);
        }
        t.append("@str@I headed to the temple where Drezel lives, but it was all|@str@locked shut. I spoke through the locked door to Drezel.|");
        if (s == AGREE_TO_KILL_DOG) {
            t.append("@dbl@He told me that there was an annoying @dre@dog@dbl@ below the temple, and has asked me to @dre@kill it@dbl@ for him");
            return t.toString().split("\\|", -1);
        }
        t.append("@str@He told me that there was an annoying dog below the|@str@temple, and asked me to kill it, which I did easily.|");
        if (s == KILLED_DOG) {
            t.append("|@dbl@I should tell @dre@King Roald@dbl@ everything's fine with @dre@Drezel@dbl@ now I have killed that @dre@dog@dbl@ for him, and claim my @dre@reward");
            return t.toString().split("\\|", -1);
        }
        t.append("@str@When I told Roald what I had done, he was furious. The|@str@person who told me to kill the dog wasn't Drezel at all!|");
        if (s == RETURN_TO_DREZEL) {
            t.append("|@dbl@I must return to the @dre@temple@dbl@ and find out what happened to the real @dre@Drezel@dbl@, or the King will have me executed!");
            return t.toString().split("\\|", -1);
        }
        t.append("@str@I returned to the temple and found the real Drezel locked|@str@in a makeshift cell upstairs, guarded by a vampire.|");
        if (s == FIND_DREZEL_KEY) {
            t.append("|@dbl@I need to find the @dre@key@dbl@ to his cell and free him!|");
            return t.toString().split("\\|", -1);
        }
        if (s == UNLOCKED_DREZEL) {
            t.append("@str@I used a key from the monument to open the cell door|");
            t.append("@dbl@but I still have to do something about that @dre@vampire||");
            return t.toString().split("\\|", -1);
        }
        if (s == POURED_BLESSED_WATER) {
            t.append("@str@I used a key from the monument to open the cell door and|@str@used Holy Water to trap the vampire in his coffin.|");
            t.append("@dbl@I should speak to @dre@Drezel@dbl@ again");
            return t.toString().split("\\|", -1);
        }
        t.append("@str@I used a key from the monument to open the cell door and|@str@and used Holy Water to trap the vampire in his coffin.|");
        if (s < BEGIN_BRING_ESSENCE) {
            t.append("@dbl@I should head downstairs to the @dre@monument@dbl@ like @dre@Drezel@dbl@ asked me to, and assess what @dre@damage@dbl@ has been done");
            return t.toString().split("\\|", -1);
        }
        t.append("@str@I followed Drezel downstairs only to find that the Salve|@str@had been contaminated and now needed purifying.|");
        if (s < END_BRING_ESSENCE) {
            t.append("@dbl@I need to bring Drezel @dre@fifty rune essences@dbl@ to undo the damage done by the Zamorakians and @dre@purify the Salve");
            return t.toString().split("\\|", -1);
        }
        t.append("@str@I brought Drezel fifty rune essences and the contaminants|@str@were dissolved from the Salve, and Drezel rewarded me|");
        t.append("@str@for all of my help with an ancient holy weapon to fight|@str@with.||");
        t.append("@red@QUEST COMPLETE!");
        return t.toString().split("\\|", -1);
    }

    // ------------------------------------------------------------- completion
    /** LostCity queue priestperil_complete: the Wolfbane dagger and 1,406 Prayer xp. */
    @Override
    public final void awardCompletionRewards(Player p) {
        super.markQuestComplete(p);
        super.showQuestCompleteInterface(p);
        give(p, WOLFBANE);
        p.getSkillManager().addQuestExperience(5, 1406.0);
        p.packetSender.sendInterfaceText("1 Quest Point", 12150);
        p.packetSender.sendInterfaceText("1406 Prayer XP", 12151);
        p.packetSender.sendInterfaceText("Wolfbane dagger", 12152);
        p.packetSender.sendInterfaceText("Route to Canifis", 12153);
        p.packetSender.sendInterfaceText("", 12154);
        p.packetSender.sendInterfaceText("", 12155);
        p.packetSender.sendInterfaceModel(QuestScript.usesLegacyCompletionInterface() ? 6161 : 12145, 250, WOLFBANE);
        p.packetSender.showInterface(QuestScript.usesLegacyCompletionInterface() ? 1689 : 12140);
        p.deferLevelUpInterfaces = false;
    }
}

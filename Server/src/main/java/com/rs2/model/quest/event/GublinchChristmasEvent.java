package com.rs2.model.quest.event;

import com.rs2.ServerSettings;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.combat.AttackStyleDefinition;
import com.rs2.model.combat.ProjectileTiming;
import com.rs2.model.c.ProjectileDefinition;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.NpcMovementMode;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestHook;
import com.rs2.model.task.TickTask;
import com.rs2.model.skill.woodcutting.WoodcuttingHandler;
import com.rs2.util.GameUtil;
import com.rs2.util.path.PathFinder;
import com.rs2.util.path.WalkingCollisionMap;
import com.rs2.util.path.ProjectileCollisionMap;

/** The 2006 Christmas rescue. Slot 5 is part of the existing saved event-state array. */
public final class GublinchChristmasEvent extends QuestHook {
    public static final int STATE_SLOT = 5;
    public static final int SHANTY_CLAWS = 828;
    public static final int SNOWBALL = 10501;
    private static final int GUBLINCH_HITS_TO_DEFEAT = 3;
    private static final int[] SNOWBALL_RANGED_LEVELS = {1, 10, 20, 30, 40, 50, 60, 69, 70};
    // Hit chances in basis points: 10,000 is 100%.
    private static final int[] SNOWBALL_HIT_CHANCES = {5900, 6442, 7044, 7646, 8248, 8850, 9452, 9994, 10000};
    private static final String GUBLINCH_HITS_ATTRIBUTE = "christmasSnowballHits";
    private static final String GUBLINCH_FLIGHT_ATTRIBUTE = "christmasSnowballInFlight";
    private static final String SNOWBALL_THROW_ATTRIBUTE = "christmasSnowballThrow";
    private static final int CLIENT_CYCLES_PER_TICK = 30;
    private static final int SNOWBALL_ANIMATION_DELAY_CYCLES = 10; // 200 ms.
    // Let the throwing motion wind up before displaying the airborne projectile.
    private static final int SNOWBALL_LAUNCH_DELAY_CYCLES = 75; // 1,500 ms.
    private static final int SNOWBALL_IMPACT_TICK = 3;
    // Round the requested five-second interval up to whole 600 ms game ticks.
    private static final int PELT_INTERVAL_TICKS = 9;
    private static final int PELT_RANGE = 6;
    private static final String REINDEER_EMOTE_ATTRIBUTE = "christmasReindeerEmote";
    // Both native sequences last 220 client cycles (4.4 s); round up to whole ticks.
    private static final int REINDEER_EMOTE_TICKS = 8;
    // Original Gublinch snowball-making sequence in the revision 443 cache.
    private static final int MAKE_SNOWBALL_ANIMATION = 5067;
    // Sequence 5067 lasts 150 client frames (3 seconds): five 600 ms server ticks.
    private static final int MAKE_SNOWBALL_TICKS = 5;
    private static final int LARGE_SNOW_PILE = 19030;
    private static final int SMALL_SNOW_PILE = 19035;
    // Recreation estimate supplied by the user, not a verified 2006 regrowth value.
    private static final int SNOW_REGROW_MIN_TICKS = 6;
    private static final int SNOW_REGROW_MAX_TICKS = 10;
    private static final ProjectileDefinition SNOWBALL_PROJECTILE =
            new ProjectileDefinition(861, ProjectileTiming.STANDARD.copy()
                    .setStartDelay(SNOWBALL_LAUNCH_DELAY_CYCLES));
    public static final int SHARDS = 10506;
    public static final int REINDEER_HAT = 10507;
    public static final int WINTUMBER_TREE = 10508;
    public static final int CAGE = 19036;
    public static final int ENTRANCE = 19039;
    public static final int LADDER = 19040;
    private static final int STARTED = 1 << 10;
    private static final int COMPLETE = 1 << 11;
    private static final int TREE_CLAIMED = 1 << 12;
    private static final int ALL_CAGES = (1 << 10) - 1;
    private static final int DOCK_X = 2904, DOCK_Y = 3186;
    private static final int ENTRY_X = 2843, ENTRY_Y = 3141;
    private static final int[][] SNOW_PILES = {
        {2841, 3147}, {2844, 3147}, {2840, 3150}, {2842, 3153}
    };
    private static final boolean[] SNOW_PILE_COLLECTING = new boolean[SNOW_PILES.length];
    private static boolean shantiesScheduled;
    // The 15 positions on the RuneScape wiki's Gublinch NPC map of the lair
    // (https://runescape.wiki/w/Gublinch). Normal NPC pathfinding clips their wandering.
    private static final int[][] GUBLINCH_SPAWNS = {
        {3147, 5328}, {3147, 5355}, {3148, 5321}, {3149, 5366}, {3153, 5341},
        {3161, 5359}, {3164, 5341}, {3171, 5338}, {3171, 5353}, {3177, 5365},
        {3184, 5340}, {3187, 5353}, {3189, 5366}, {3191, 5325}, {3191, 5334}
    };
    // Freed children on the dock, from the RuneScape wiki's Jack, Jill and Jeff NPC maps.
    private static final int JACK_X = 2904, JACK_Y = 3178, JILL_X = 2907, JILL_Y = 3174, JEFF_X = 2906, JEFF_Y = 3172;
    private static final int[][] CAGES = {
        {2907, 3183}, {2907, 3184}, {2907, 3185},
        {2904, 3188}, {2904, 3189}, {2904, 3190}, {2904, 3191},
        {2907, 3188}, {2907, 3190}, {2907, 3189}
    };

    // Full sequence reviewed on the child mesh: repeated jumps with both arms raised.
    private static final int CHILD_CELEBRATION_ANIMATION = 3854;
    private static boolean childPlayScheduled;

    public GublinchChristmasEvent() { super(STATE_SLOT); }

    @Override public void initialize() {
        // Permanent single-player content, independent of the calendar-based rare drops.
        if (ServerSettings.cacheVersion != 443) return;
        spawnOnce(SHANTY_CLAWS, DOCK_X, DOCK_Y);
        if (!shantiesScheduled) {
            shantiesScheduled = true;
            World.scheduleTickTask(new TickTask(10, false) {
                private int verse;
                @Override public void execute() {
                    for (Npc npc : World.getNpcs()) {
                        if (npc != null && npc.getOriginalNpcId() == SHANTY_CLAWS
                                && npc.getSpawnPosition().equals(new Position(DOCK_X, DOCK_Y, 0))) {
                            npc.getUpdateState().setForcedText(GublinchChristmasDialogue.SHANTIES[verse]);
                        }
                    }
                    verse = (verse + 1) % GublinchChristmasDialogue.SHANTIES.length;
                }
            });
        }
        placeObject(ENTRANCE, ENTRY_X, ENTRY_Y);
        for (int i = 0; i < SNOW_PILES.length; i++)
            placeObject(LARGE_SNOW_PILE, SNOW_PILES[i][0], SNOW_PILES[i][1]);
        for (int[] cage : CAGES) placeObject(CAGE, cage[0], cage[1]);
        for (int[] tile : GUBLINCH_SPAWNS) {
            spawnOnce(5017, tile[0], tile[1]);
        }
        // Native animated scenery 20100 at (3160,5363) already contains Jeff at
        // (3161,5363), Jill at (3163,5363), and Jack at (3165,5363), plus the logs
        // and furnace. Separate dungeon NPCs duplicate those original children.
        removeDungeonChildCopies();
        spawnOnce(5046, JACK_X, JACK_Y);
        spawnOnce(5047, JILL_X, JILL_Y);
        spawnOnce(5048, JEFF_X, JEFF_Y);
        for (Npc npc : World.getNpcs()) {
            if (npc == null || npc.getOriginalNpcId() < 5046 || npc.getOriginalNpcId() > 5048) continue;
            Position spawn = npc.getSpawnPosition();
            npc.setSpawnMinPosition(new Position(spawn.getX() - 5, spawn.getY() - 5));
            // Roaming samples the upper bound exclusively: include the full five-tile radius.
            npc.setSpawnMaxPosition(new Position(spawn.getX() + 6, spawn.getY() + 6));
        }
        if (!childPlayScheduled) {
            childPlayScheduled = true;
            World.scheduleTickTask(new RescuedChildPlayTask());
        }
        if (!snowMeltScheduled) {
            snowMeltScheduled = true;
            // A scan rather than a movement hook, so teleporting away also melts them.
            World.scheduleTickTask(new TickTask(2, false) {
                @Override public void execute() {
                    for (Player player : World.getPlayers()) {
                        if (player != null && !onKaramja(player.getPosition())) meltSnowballs(player);
                    }
                }
            });
        }
    }

    private static boolean snowMeltScheduled;

    /**
     * Snowballs "disappeared as soon as they left Karamja" (RuneScape wiki). Banked ones are kept.
     * The bounds are approximate: the island without Crandor, its dungeons, and the gublinch lair.
     */
    static boolean onKaramja(Position p) {
        int x = p.getX(), y = p.getY();
        if (x >= 2688 && x <= 2975 && y >= 2880 && y <= 3215) return true;
        if (x >= 2620 && x <= 2975 && y >= 9400 && y <= 9660) return true;
        return x >= 3135 && x <= 3200 && y >= 5310 && y <= 5375;
    }

    private static void meltSnowballs(Player player) {
        int carried = player.getInventoryManager().getItemAmount(SNOWBALL);
        int wielded = player.getEquipmentManager().getContainer().getItemAmount(SNOWBALL);
        if (carried == 0 && wielded == 0) return;
        if (carried > 0) player.getInventoryManager().removeItem(new ItemStack(SNOWBALL, carried));
        if (wielded > 0) {
            player.getEquipmentManager().removeItem(new ItemStack(SNOWBALL, wielded));
            player.setAppearanceUpdateRequired(true);
        }
        // Recreation: the sources say the snowballs vanished but give no message.
        player.packetSender.sendGameMessage("Your snowballs melt away.");
    }

    /** Recreation of the documented dock dancing; original choreography is unverified. */
    private static final class RescuedChildPlayTask extends TickTask {
        private int tick = -1;

        RescuedChildPlayTask() { super(1, false); }

        @Override public void execute() {
            tick = (tick + 1) % 40;
            for (Npc npc : World.getNpcs()) {
                if (npc == null || !npc.isActive() || npc.isDead()) continue;
                int child = npc.getOriginalNpcId() - 5046;
                if (child < 0 || child > 2) continue;
                // Offset each child so they play independently rather than in unison.
                int phase = (tick - child * 5 + 40) % 40;
                if (phase == 0 || phase == 20) {
                    npc.getMovementQueue().clear();
                    npc.setMovementMode(NpcMovementMode.STATIONARY);
                    npc.getUpdateState().setAnimation(CHILD_CELEBRATION_ANIMATION);
                } else if (phase == 7 || phase == 27) {
                    // Sequence 3854 lasts 186 client cycles (3.72 seconds): allow seven ticks.
                    int animation = CHILD_CELEBRATION_ANIMATION;
                    if (npc.getUpdateState().getAnimationId() == animation)
                        npc.getUpdateState().setAnimation(-1);
                    npc.setMovementMode(NpcMovementMode.ROAMING);
                }
            }
        }
    }

    private static void placeObject(int id, int x, int y) {
        if (ObjectManager.findDynamicObjectAt(x, y, 0) != null) return;
        int orientation = id == CAGE ? cageOrientation(x) : id == ENTRANCE ? 2 : 0;
        if (id == ENTRANCE) {
            LoadedWorldObject tree = WorldObjectLookup.findObjectByIdAt(1303, x, y, 0);
            if (tree != null) {
                WalkingCollisionMap.removeObjectCollision(1303, x, y, 0, tree.getOrientation(), tree.getType());
                ProjectileCollisionMap.removeObjectCollision(1303, x, y, 0, tree.getOrientation(), tree.getType());
            }
        }
        // New scenery has no old object to restore; register its own collision.
        new DynamicObject(id, x, y, 0, orientation, 10, -1, 999999, false);
        WalkingCollisionMap.addObjectCollision(id, x, y, 0, orientation, 10, false);
        ProjectileCollisionMap.addObjectCollision(id, x, y, 0, orientation, 10, false);
    }

    // The cage model opens south at rotation 0, unlike wall orientation values.
    private static int cageOrientation(int x) { return x == 2907 ? 1 : 3; }

    private static void spawnOnce(int id, int x, int y) {
        boolean roaming = isGublinch(id) || id >= 5046 && id <= 5048;
        for (Npc npc : World.getNpcs()) {
            if (npc != null && npc.getOriginalNpcId() == id
                    && npc.getSpawnPosition().equals(new Position(x, y, 0))) {
                if (roaming) npc.setMovementMode(NpcMovementMode.ROAMING);
                return;
            }
        }
        GameplayHelper.spawnNpc(id, x, y, 0, roaming ? 1 : id == SHANTY_CLAWS ? 3 : 4);
    }

    private static void removeDungeonChildCopies() {
        for (Npc npc : World.getNpcs()) {
            if (npc != null && npc.getOriginalNpcId() >= 5023 && npc.getOriginalNpcId() <= 5025)
                World.unregisterNpc(npc);
        }
    }

    public static boolean isGublinch(int id) { return id >= 5017 && id <= 5019; }
    public static boolean isComplete(Player player) { return (player.questHookStates[STATE_SLOT] & COMPLETE) != 0; }
    static boolean started(Player player) { return (player.questHookStates[STATE_SLOT] & STARTED) != 0; }
    static boolean membersReward(Player player) { return player.isMember() && !ServerSettings.freeToPlayWorld; }
    static void start(Player player) { player.questHookStates[STATE_SLOT] |= STARTED; }
    static int cagesRemaining(Player player) { return 10 - Integer.bitCount(player.questHookStates[STATE_SLOT] & ALL_CAGES); }
    static boolean treeClaimed(Player player) {
        // Remember rewards held by accounts completed before separate handoffs were added.
        if (player.ownsItem(WINTUMBER_TREE)) player.questHookStates[STATE_SLOT] |= TREE_CLAIMED;
        return (player.questHookStates[STATE_SLOT] & TREE_CLAIMED) != 0;
    }
    static boolean giveReward(Player player, int item) {
        if (cagesRemaining(player) != 0 || item == WINTUMBER_TREE && !membersReward(player)) return false;
        if (!player.ownsItem(item) && !player.getInventoryManager().addItem(new ItemStack(item, 1))) return false;
        player.questHookStates[STATE_SLOT] |= item == REINDEER_HAT ? COMPLETE : TREE_CLAIMED;
        return true;
    }

    public static boolean isNpcVisible(Player player, int id) {
        // The dungeon children are part of scenery 20100. Hide old NPC copies
        // on every update too: live reload does not rerun initialize().
        if (id >= 5023 && id <= 5025) return false;
        if (id >= 5046 && id <= 5048) return isComplete(player);
        return true;
    }

    private static int cageIndex(int x, int y) {
        for (int i = 0; i < CAGES.length; i++) if (CAGES[i][0] == x && CAGES[i][1] == y) return i;
        return -1;
    }

    public static int cageDisplayId(Player player, int id, int x, int y, int plane) {
        int cage = cageIndex(x, y);
        return id == CAGE && plane == 0 && cage >= 0
                && (player.questHookStates[STATE_SLOT] & (1 << cage)) != 0 ? 19037 : id;
    }

    /** Filled cages have the same geometry and server position as the empty cage. */
    public static boolean isCageAppearance(int id, int x, int y, int plane) {
        return id == 19037 && plane == 0 && cageIndex(x, y) >= 0;
    }

    @Override public boolean handleFirstNpcAction(Player player, int id, int state) {
        if (id == SHANTY_CLAWS || (id == 970 && isComplete(player))
                || id >= 5023 && id <= 5025 || id >= 5046 && id <= 5048) {
            return DialogueManager.startDialogue(player, id);
        }
        return false;
    }

    @Override public boolean handleNpcDialogue(Player player, int id, int step, int option, int state) {
        if (id == 970 && isComplete(player)) {
            say(player, reclaimRewards(player) ? "Diango returns any missing Christmas rewards."
                    : "Make room in your inventory for your Christmas rewards.");
        } else if (GublinchChristmasDialogue.handle(player, id, step, option)) {
            return true;
        } else if (id >= 5023 && id <= 5025) {
            say(player, "Please help us! The gublinch make us stoke their furnaces.");
        } else return false;
        return true;
    }

    @Override public boolean handleItemOnNpc(Player player, int npcId, int item, int state) {
        if (npcId != SHANTY_CLAWS) return false;
        return DialogueManager.startDialogue(player, item == SHARDS
                ? GublinchChristmasDialogue.ITEM_SHARDS : GublinchChristmasDialogue.ITEM_OTHER);
    }

    @Override public boolean handleSecondObjectAction(Player player, int id, int x, int y, int state) {
        if (id != ENTRANCE || x != ENTRY_X || y != ENTRY_Y || player.getPosition().getPlane() != 0) return false;
        return DialogueManager.startDialogue(player, GublinchChristmasDialogue.SEARCH_ENTRANCE);
    }

    private static void say(Player player, String... lines) {
        player.getDialogueManager().showNpcDialogue(lines, 588);
        player.getDialogueManager().finishDialogue();
    }

    /** Reserve enough room for the entire handoff; never drop a reward or duplicate an owned one. */
    public static boolean reclaimRewards(Player player) {
        if ((player.questHookStates[STATE_SLOT] & ALL_CAGES) != ALL_CAGES) return false;
        boolean hat = !player.ownsItem(REINDEER_HAT);
        boolean tree = membersReward(player) && !player.ownsItem(WINTUMBER_TREE);
        if (player.getInventoryManager().getContainer().getFreeSlots() < (hat ? 1 : 0) + (tree ? 1 : 0)) return false;
        if (hat) player.getInventoryManager().addItem(new ItemStack(REINDEER_HAT, 1));
        if (tree) {
            player.getInventoryManager().addItem(new ItemStack(WINTUMBER_TREE, 1));
            player.questHookStates[STATE_SLOT] |= TREE_CLAIMED;
        }
        return true;
    }

    @Override public boolean handleFirstObjectAction(Player player, int id, int x, int y, int state) {
        if ((id == 19030 || id == 19031 || id >= 19033 && id <= 19035)
                && isSnowPile(x, y) && player.getPosition().getPlane() == 0) {
            if (player.isActionLocked()) return true;
            final int pile = snowPileIndex(x, y);
            if (!started(player) || isComplete(player)) {
                player.packetSender.sendGameMessage("Speak to Shanty Claws on the Musa Point dock first.");
            } else if (SNOW_PILE_COLLECTING[pile]
                    || ObjectManager.findDynamicObjectByIdAt(LARGE_SNOW_PILE, x, y, 0) == null) {
                player.packetSender.sendGameMessage("There isn't enough snow here yet.");
            } else if (player.getInventoryManager().addItem(new ItemStack(SNOWBALL, 3))) {
                SNOW_PILE_COLLECTING[pile] = true;
                player.setActionLocked(true);
                player.getUpdateState().setAnimation(MAKE_SNOWBALL_ANIMATION);
                player.packetSender.sendGameMessage("You make three snowballs.");
                World.scheduleTickTask(new TickTask(MAKE_SNOWBALL_TICKS, false) {
                    @Override public void execute() {
                        stop();
                        player.setActionLocked(false);
                        SNOW_PILE_COLLECTING[pile] = false;
                        depleteSnowPile(pile);
                    }
                });
            }
            return true;
        }
        if (id == ENTRANCE && x == ENTRY_X && y == ENTRY_Y && player.getPosition().getPlane() == 0) {
            if (!started(player)) player.packetSender.sendGameMessage("Speak to Shanty Claws on the Musa Point dock first.");
            else if ((state & ALL_CAGES) == ALL_CAGES) DialogueManager.startDialogue(player, GublinchChristmasDialogue.CAVE_EMPTY);
            else AttackStyleDefinition.startDelayedObjectMove(player, new Position(3168, 5320, 0));
            return true;
        }
        if (id == LADDER && x == 3168 && y == 5319 && player.getPosition().getPlane() == 0) {
            AttackStyleDefinition.startDelayedObjectMove(player, new Position(ENTRY_X + 1, ENTRY_Y + 2, 0));
            return true;
        }
        return false;
    }

    private static boolean isSnowPile(int x, int y) {
        return snowPileIndex(x, y) >= 0;
    }

    private static int snowPileIndex(int x, int y) {
        for (int i = 0; i < SNOW_PILES.length; i++) {
            if (SNOW_PILES[i][0] == x && SNOW_PILES[i][1] == y) return i;
        }
        return -1;
    }

    private static void depleteSnowPile(int pile) {
        int x = SNOW_PILES[pile][0], y = SNOW_PILES[pile][1];
        if (ObjectManager.findDynamicObjectByIdAt(LARGE_SNOW_PILE, x, y, 0) == null) return;
        final DynamicObject small = replaceSnowPile(pile, SMALL_SNOW_PILE);
        int regrowTicks = SNOW_REGROW_MIN_TICKS
                + GameUtil.randomInt(SNOW_REGROW_MAX_TICKS - SNOW_REGROW_MIN_TICKS + 1);
        World.scheduleTickTask(new TickTask(regrowTicks, false) {
            @Override public void execute() {
                stop();
                if (ObjectManager.findDynamicObjectAt(x, y, 0) == small) {
                    replaceSnowPile(pile, LARGE_SNOW_PILE);
                }
            }
        });
    }

    private static DynamicObject replaceSnowPile(int pile, int id) {
        int x = SNOW_PILES[pile][0], y = SNOW_PILES[pile][1];
        DynamicObject old = ObjectManager.findDynamicObjectAt(x, y, 0);
        ObjectManager.getInstance().removeDynamicObjectAt(x, y, 0, 10);
        // Retire old viewer references so a later refresh cannot undo the replacement.
        for (Player viewer : World.getPlayers()) {
            if (viewer != null) viewer.visibleDynamicObjects.remove(old);
        }
        // These native variants share a tile footprint; retain the pile's collision.
        return new DynamicObject(id, x, y, 0, 0, 10, LARGE_SNOW_PILE, 999999, false);
    }

    public static boolean useShardsOnCage(Player player, int item, int object, int x, int y, int plane) {
        if (item != SHARDS || (object != CAGE && object != 19037) || plane != 0) return false;
        int cage = cageIndex(x, y);
        if (cage < 0) return false;
        int bit = 1 << cage;
        if (!started(player)) player.packetSender.sendGameMessage("Speak to Shanty Claws first.");
        else if ((player.questHookStates[STATE_SLOT] & bit) != 0) player.packetSender.sendGameMessage("This cage already contains a gublinch.");
        else if (player.getInventoryManager().removeItem(new ItemStack(SHARDS, 1))) {
            player.questHookStates[STATE_SLOT] |= bit;
            player.packetSender.sendObjectCreate(19037, x, y, plane, cageOrientation(x), 10);
            player.packetSender.sendGameMessage("You place the shards in the cage.");
            player.packetSender.sendGameMessage("The gublinch rattles the cage doors, but cannot escape.");
            if ((player.questHookStates[STATE_SLOT] & ALL_CAGES) == ALL_CAGES) {
                player.packetSender.sendGameMessage("Jack, Jill and Jeff escape! Speak to Shanty Claws for your rewards.");
            }
        }
        return true;
    }

    @Override public boolean canAttackNpc(Player player, int id, int state) {
        if (!isGublinch(id)) return true;
        player.packetSender.sendGameMessage("Wield snowballs and use Pelt to freeze the gublinch.");
        return false;
    }

    public static void startPelting(Player player, Npc npc) {
        final int sequence = player.nextActionSequence();
        // Pelt owns its approach queue; generic NPC following walks to melee range.
        com.rs2.model.EntityTargetMovement.clearMovementTarget(player);
        player.getMovementQueue().clear();
        World.scheduleTickTask(new TickTask(1, true) {
            private int cooldown;
            @Override public void execute() {
                if (!player.isCurrentActionSequence(sequence) || !npc.isActive() || npc.isDead()
                        || !isGublinch(npc.getNpcId()) || npc.getIndex() < 0
                        || World.getNpcs()[npc.getIndex()] != npc
                        || player.getPosition().getPlane() != npc.getPosition().getPlane()
                        || !started(player) || isComplete(player)
                        || npc.getOwnerPlayer() != null && npc.getOwnerPlayer() != player) {
                    stop();
                    return;
                }
                if (cooldown > 0) cooldown--;
                if (player.getEquipmentManager().getItemIdAtSlot(3) != SNOWBALL) {
                    player.packetSender.sendGameMessage("You must wield snowballs to pelt the gublinch.");
                    stop();
                    return;
                }
                if (!canPeltFrom(player.getPosition(), npc.getPosition())) {
                    approachPeltRange(player, npc);
                    return;
                }
                // Remove excess approach steps before running can carry us closer.
                player.getMovementQueue().clear();
                player.getUpdateState().setFaceEntity(npc.getEncodedIndex());
                if (cooldown > 0) return;
                if (!pelt(player, npc)) stop();
                else cooldown = PELT_INTERVAL_TICKS;
            }

            @Override public void stop() {
                super.stop();
                // A later walk/click owns its own queue and must not be cancelled.
                if (player.isCurrentActionSequence(sequence)) {
                    player.getMovementQueue().clear();
                    if (player.getInteractionTarget() == npc) player.setInteractionTarget(null);
                }
            }
        });
    }

    private static boolean canPeltFrom(Position from, Position target) {
        return from.getPlane() == target.getPlane()
                && GameUtil.getDistance(from, target) > 0
                && GameUtil.getDistance(from, target) <= PELT_RANGE
                && GameUtil.hasClearPath(from, target, true);
    }

    private static void approachPeltRange(Player player, Npc npc) {
        player.getMovementQueue().clear();
        if (!PathFinder.findPathToAdjacent(player, npc.getPosition().getX(), npc.getPosition().getY(),
                Math.max(1, npc.getSize()), Math.max(1, npc.getSize()), true)) return;
        // Keep the collision-aware route only as far as its first clear throwing tile.
        // Trimming the queue also prevents a running second step from overshooting.
        boolean reached = false;
        java.util.Iterator steps = player.getMovementQueue().getSteps().iterator();
        while (steps.hasNext()) {
            Position step = (Position) steps.next();
            if (reached) steps.remove();
            else reached = canPeltFrom(new Position(step.getX(), step.getY(),
                    player.getPosition().getPlane()), npc.getPosition());
        }
    }

    /** Returns true while the same target needs another snowball, including after a miss. */
    public static boolean pelt(Player player, Npc npc) {
        return pelt(player, npc, GameUtil.randomInt(10000));
    }

    private static boolean pelt(Player player, Npc npc, int accuracyRoll) {
        if (!started(player)) {
            player.packetSender.sendGameMessage("Speak to Shanty Claws on the Musa Point dock first.");
            return false;
        }
        if (isComplete(player) || !isGublinch(npc.getNpcId()) || npc.isDead()
                || npc.getIndex() < 0 || World.getNpcs()[npc.getIndex()] != npc) return false;
        // One outstanding projectile per target prevents overlapping hits or extra ammo use.
        if (Boolean.TRUE.equals(npc.getAttributes().get(GUBLINCH_FLIGHT_ATTRIBUTE))) return true;
        if (!player.getEquipmentManager().removeItem(new ItemStack(SNOWBALL, 1))) {
            player.packetSender.sendGameMessage("You must wield snowballs to pelt the gublinch.");
            return false;
        }
        final boolean hit = snowballHits(player.getSkillManager().getCurrentLevels()[4], accuracyRoll);
        final Object throwToken = new Object();
        player.getAttributes().put(SNOWBALL_THROW_ATTRIBUTE, throwToken);
        player.getUpdateState().setAnimation(5063, SNOWBALL_ANIMATION_DELAY_CYCLES);
        player.setAppearanceUpdateRequired(true);
        // This cache effect is animated with the throwing hand; the sequence
        // hides the equipped ball while this effect carries it through the throw.
        player.getUpdateState().setGraphic(860, SNOWBALL_ANIMATION_DELAY_CYCLES);
        player.getUpdateState().setFacePosition(npc.getPosition().copy());
        // Use the landing tile rather than a client NPC index: the final throw
        // removes that NPC, and its index can be reused before the ball lands.
        WoodcuttingHandler projectile = new WoodcuttingHandler(player.getPosition().copy(), player.getSize(),
                npc.getPosition().copy(), 0, SNOWBALL_PROJECTILE)
                .setEndDelay(SNOWBALL_IMPACT_TICK * CLIENT_CYCLES_PER_TICK);
        projectile.sendProjectileToNearbyPlayers();
        Integer previousHits = (Integer) npc.getAttributes().get(GUBLINCH_HITS_ATTRIBUTE);
        // Returning to the normal form after warming up starts a fresh freeze cycle.
        final int hits = npc.getNpcId() == 5017 || previousHits == null ? 1 : previousHits + 1;
        final int targetIndex = npc.getIndex();
        npc.getAttributes().put(GUBLINCH_FLIGHT_ATTRIBUTE, Boolean.TRUE);
        // Arrival, reaction and animation completion all share tick 3 (1,800 ms).
        int impactTicks = projectile.getEndDelay() / CLIENT_CYCLES_PER_TICK;
        World.scheduleTickTask(new TickTask(impactTicks, false) {
            @Override public void execute() {
                stop();
                finishSnowballThrow(player, throwToken);
                npc.getAttributes().remove(GUBLINCH_FLIGHT_ATTRIBUTE);
                if (!npc.isActive() || npc.isDead() || targetIndex < 0
                        || World.getNpcs()[targetIndex] != npc || !isGublinch(npc.getNpcId())) return;
                if (hit) {
                    applySnowballImpact(player, npc, hits);
                } else {
                    player.packetSender.sendGameMessage("Your snowball misses the gublinch.");
                }
            }
        });
        return !hit || hits < GUBLINCH_HITS_TO_DEFEAT;
    }

    private static boolean snowballHits(int rangedLevel, int accuracyRoll) {
        return accuracyRoll < snowballHitChance(rangedLevel);
    }

    /** Interpolate the supplied Ranged table, rounded to the nearest basis point. */
    private static int snowballHitChance(int rangedLevel) {
        if (rangedLevel <= SNOWBALL_RANGED_LEVELS[0]) return SNOWBALL_HIT_CHANCES[0];
        for (int i = 1; i < SNOWBALL_RANGED_LEVELS.length; i++) {
            if (rangedLevel <= SNOWBALL_RANGED_LEVELS[i]) {
                int span = SNOWBALL_RANGED_LEVELS[i] - SNOWBALL_RANGED_LEVELS[i - 1];
                int offset = rangedLevel - SNOWBALL_RANGED_LEVELS[i - 1];
                return SNOWBALL_HIT_CHANCES[i - 1] + (int) Math.round(
                        (SNOWBALL_HIT_CHANCES[i] - SNOWBALL_HIT_CHANCES[i - 1])
                                * (double) offset / span);
            }
        }
        return 10000;
    }

    private static void finishSnowballThrow(Player player, Object throwToken) {
        // An older flight must not stop a newer throw or a different action's animation.
        if (player.getAttributes().get(SNOWBALL_THROW_ATTRIBUTE) != throwToken) return;
        player.getAttributes().remove(SNOWBALL_THROW_ATTRIBUTE);
        if (player.getUpdateState().getAnimationId() == 5063) {
            player.getUpdateState().setAnimation(-1);
        }
        if (player.getUpdateState().getGraphicId() == 860) {
            player.getUpdateState().setGraphic(-1);
        }
    }

    private static void applySnowballImpact(Player player, Npc npc, int hits) {
        npc.getAttributes().put(GUBLINCH_HITS_ATTRIBUTE, hits);
        if (hits < GUBLINCH_HITS_TO_DEFEAT) {
            // First successful hit chills, second freezes, third shatters.
            npc.transformToNpcId(5017 + Math.min(hits, 2), 100);
            return;
        }
        final Position dropTile = npc.getPosition().copy();
        Position spawn = npc.getSpawnPosition().copy();
        // Local NPC lists retain references after unregistering. Mark this
        // instance inactive so the next update sends its removal immediately.
        npc.setActive(false);
        World.unregisterNpc(npc);
        // NPC removals are serialized at the end of this tick. A ground-item
        // create is sent immediately, so wait until the next tick to show shards.
        World.scheduleTickTask(new TickTask(1, false) {
            @Override public void execute() {
                stop();
                GroundItemManager.getInstance().spawn(
                        new GroundItem(new ItemStack(SHARDS, 1), dropTile, false, player));
            }
        });
        // The throw has completed at impact; stop following the defeated target.
        if (player.getInteractionTarget() == npc) {
            player.getUpdateState().setFaceEntity(65535);
            player.setInteractionTarget(null);
            player.getMovementQueue().clear();
        }
        World.scheduleTickTask(new TickTask(10, false) {
            @Override public void execute() { spawnOnce(5017, spawn.getX(), spawn.getY()); stop(); }
        });
    }

    @Override public boolean handleInventoryItemFirstOption(Player player, int id, int slot, int state) {
        if (id != WINTUMBER_TREE) return false;
        player.packetSender.sendGameMessage("Keep this tree for your house garden. Player-owned houses are not available yet.");
        return true;
    }

    public static void operateHat(Player player) {
        if (!player.getEquipmentManager().containsItem(REINDEER_HAT) || player.isActionLocked()
                || player.getAttributes().containsKey(REINDEER_EMOTE_ATTRIBUTE)) return;
        final Object emote = new Object();
        player.getAttributes().put(REINDEER_EMOTE_ATTRIBUTE, emote);
        player.getUpdateState().setAnimation(5059);
        player.getUpdateState().setGraphic(859);
        player.packetSender.sendGameMessage("You act like a reindeer.");
        World.scheduleTickTask(new TickTask(REINDEER_EMOTE_TICKS, false) {
            @Override public void execute() {
                stop();
                if (player.getAttributes().get(REINDEER_EMOTE_ATTRIBUTE) != emote) return;
                player.getAttributes().remove(REINDEER_EMOTE_ATTRIBUTE);
                if (player.getUpdateState().getAnimationId() == 5059)
                    player.getUpdateState().setAnimation(-1);
                if (player.getUpdateState().getGraphicId() == 859)
                    player.getUpdateState().setGraphic(-1);
            }
        });
    }
}

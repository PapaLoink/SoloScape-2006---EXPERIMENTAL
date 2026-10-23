import com.rs2.ServerSettings;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.cache.js5.ConfigReader;
import com.rs2.cache.js5.Definitions;
import com.rs2.cache.js5.Js5CacheStore;
import java.io.File;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.interaction.NpcActionRouter;
import com.rs2.model.interaction.RoutedNpcActionTask;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.NpcMovementMode;
import com.rs2.model.npc.NpcUpdateTask;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.player.CharacterFileRecord;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.quest.QuestEventRegistry;
import com.rs2.model.quest.event.GublinchChristmasEvent;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.task.TickTask;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.net.IsaacCipher;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.ClientPackets;
import com.rs2.net.packet.InterfaceBridge;
import com.rs2.net.packet.handler.InterfaceActionPacketHandler;
import com.rs2.net.packet.handler.MovementPacketHandler;
import java.nio.ByteBuffer;
import com.rs2.util.CharacterFileManager;
import com.rs2.util.path.WalkingCollisionMap;
import com.rs2.util.path.ProjectileCollisionMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.net.InetSocketAddress;
import java.io.DataInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class GublinchChristmasEventChecks {
    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        ServerSettings.membershipRequirementMode = 1;
        ServerSettings.freeToPlayWorld = false;
        // Check real assets too: a later revision's sequence ID can be sent
        // successfully by the server while producing no animation in the client.
        byte[] snowballSequence = Definitions.readGroup(12).get(5067);
        check(snowballSequence != null, "Snowball-making sequence missing from 443 cache");
        ConfigReader sequence = new ConfigReader(snowballSequence);
        int opcode;
        while ((opcode = sequence.readUnsignedByte()) != 1) {
            if (opcode == 2 || opcode == 6 || opcode == 7) sequence.skip(2);
            else if (opcode == 3) sequence.skip(sequence.readUnsignedByte());
            else if (opcode == 4) { /* Boolean flag. */ }
            else if (opcode >= 5 && opcode <= 11) sequence.skip(1);
            else throw new AssertionError("Unexpected snowball sequence opcode " + opcode);
        }
        int frameCount = sequence.readUnsignedByte();
        check(frameCount > 0, "Snowball-making sequence is empty");
        int snowballDuration = 0;
        for (int i = 0; i < frameCount; i++) snowballDuration += sequence.readUnsignedShort();
        check(snowballDuration == 150, "Snowball collection lock must match the cache animation duration");
        int[] frames = new int[frameCount];
        for (int i = 0; i < frameCount; i++) frames[i] = sequence.readUnsignedShort();
        for (int i = 0; i < frameCount; i++) frames[i] |= sequence.readUnsignedShort() << 16;
        try (Js5CacheStore cache = new Js5CacheStore(new File("cache"))) {
            for (int frame : frames) {
                check(cache.readFiles(0, frame >>> 16).get(frame & 65535) != null,
                        "Snowball-making frame missing from 443 cache");
            }
        }
        QuestDefinition.loadDefinitions();
        InterfaceDefinition.loadDefinitions();
        ItemDefinition.loadDefinitions();
        NpcDefinition.loadDefinitions();
        WorldObjectLookup.loadWorldObjects();
        WalkingCollisionMap.loadCollisionMaps();
        ProjectileCollisionMap.loadCollisionMaps();
        QuestEventRegistry.initializeEventHooks();
        checkGublinchSpawns();
        check(WorldObjectLookup.findObjectByIdAt(20100,3160,5363,0) != null,
                "Original animated children are missing from their cache location");
        for (Npc npc : World.getNpcs())
            check(npc == null || npc.getOriginalNpcId() < 5023 || npc.getOriginalNpcId() > 5025,
                    "Separate dungeon NPC duplicates an original scenery child");
        int[][] cages = {{2907,3183}, {2907,3184}, {2907,3185},
                {2904,3188}, {2904,3189}, {2904,3190}, {2904,3191},
                {2907,3188}, {2907,3190}, {2907,3189}};
        for (int[] tile : cages) {
            check(ObjectManager.findDynamicObjectByIdAt(19036, tile[0], tile[1], 0) != null,
                    "Missing cage at " + tile[0] + "," + tile[1]);
            check(ObjectManager.findDynamicObjectByIdAt(19036,tile[0],tile[1],0).getWorldObject().getOrientation()
                    == (tile[0] == 2907 ? 1 : 3), "Cage faces the wrong direction");
            int adjacentX = tile[0] == 2904 ? 2905 : 2906;
            check(WalkingCollisionMap.canTravelBetween(2905,3186,adjacentX,tile[1],0,1,1),
                    "Cage cannot be reached at " + tile[0] + "," + tile[1]);
        }
        check(ObjectManager.findDynamicObjectByIdAt(19036,2907,3191,0) == null,
                "Removed cage still exists");
        check((WalkingCollisionMap.getTileFlags(2924,3184,0) & 0x200000) != 0,
                "Incorrect platform was not removed");
        check(ItemDefinition.forId(10501).getEquipmentSlot() == 3, "Snowball must wield in weapon slot");
        check(ItemDefinition.forId(10506).getEquipmentSlot() == -1, "Shards must not be wearable");
        check(ItemDefinition.forId(10508).getEquipmentSlot() == -1, "Tree must not be wearable");
        check(ItemDefinition.forId(10507).getEquipmentSlot() == 0, "Hat must wear in head slot");
        check(ItemDefinition.forId(10507).isUntradeable() && ItemDefinition.forId(10508).isUntradeable(), "Rewards must be untradeable");
        check(QuestEventRegistry.getEventHook(5) instanceof GublinchChristmasEvent, "Event must register");
        int npcCount = countNpcs();
        int scheduledTasks = World.getTaskScheduler().getTasks().size();
        // Reinitialization must also clear copies left by the previous event implementation.
        com.rs2.model.GameplayHelper.spawnNpc(5023,3162,5338,0,4);
        com.rs2.model.GameplayHelper.spawnNpc(5024,3164,5338,0,4);
        com.rs2.model.GameplayHelper.spawnNpc(5025,3166,5338,0,4);
        QuestEventRegistry.getEventHook(5).initialize();
        check(countNpcs() == npcCount, "Initialization duplicated NPCs or retained old child copies");
        check(WorldObjectLookup.findObjectByIdAt(20100,3160,5363,0) != null,
                "Removing child copies also removed their original scenery");
        check(World.getTaskScheduler().getTasks().size() == scheduledTasks, "Initialization duplicated shanties");
        checkGublinchSpawns();
        checkShanties();
        checkChildPlaying();
        checkCharosDialogue();
        for (Npc npc : World.getNpcs()) {
            if (npc != null && (GublinchChristmasEvent.isGublinch(npc.getNpcId())
                    || npc.getNpcId() == 828 || npc.getNpcId() >= 5023 && npc.getNpcId() <= 5025
                    || npc.getNpcId() >= 5046 && npc.getNpcId() <= 5048)) {
                Position tile = npc.getPosition();
                if (npc.getNpcId() == 828) {
                    check(tile.equals(new Position(2904,3186,0)), "Shanty Claws is at the wrong position");
                    check(npc.getFacingDirection() == 3, "Shanty Claws must face south");
                }
                if (npc.getNpcId() >= 5046 && npc.getNpcId() <= 5048) {
                    // Jack, Jill and Jeff dock spawns from the RuneScape wiki NPC maps.
                    int expectedX = npc.getNpcId() == 5046 ? 2904 : npc.getNpcId() == 5047 ? 2907 : 2906;
                    int expectedY = npc.getNpcId() == 5046 ? 3178 : npc.getNpcId() == 5047 ? 3174 : 3172;
                    check(npc.getSpawnPosition().equals(new Position(expectedX, expectedY, 0)),
                            "Rescued child has the wrong spawn: " + npc.getNpcId());
                    check(npc.getMovementMode() == NpcMovementMode.ROAMING,
                            "Rescued child cannot wander: " + npc.getNpcId());
                    Position spawn = npc.getSpawnPosition();
                    check(npc.getSpawnMinPosition().equals(new Position(spawn.getX() - 5, spawn.getY() - 5))
                                    && npc.getSpawnMaxPosition().equals(new Position(spawn.getX() + 6, spawn.getY() + 6)),
                            "Rescued child wandering must stay within five tiles of its spawn");
                }
                check((WalkingCollisionMap.getTileFlags(tile.getX(), tile.getY(), 0) & 0x200100) == 0,
                        "Event NPC spawned on blocked terrain: " + tile);
            }
        }
        ObjectManager.getInstance().processObjects();
        check(ObjectManager.findDynamicObjectByIdAt(19039, 2843, 3141, 0) != null, "Entrance expired");
        check(ObjectManager.findDynamicObjectByIdAt(19039, 2856, 3162, 0) == null, "Old eastern entrance remains");
        check((WalkingCollisionMap.getTileFlags(2844,3143,0) & 0x200100) == 0,
                "Cave exit returns onto blocked terrain");
        check(WalkingCollisionMap.canTravelBetween(2844,3143,2842,3143,0,1,1)
                        && WalkingCollisionMap.canTravelBetween(2842,3143,2842,3144,0,1,1),
                "Player cannot walk away from the cave exit");
        Player player = player(1);
        World.getPlayers()[1] = player;
        Player other = player(2);
        World.getPlayers()[2] = other;
        for (int id = 5023; id <= 5025; id++) {
            check(!GublinchChristmasEvent.isNpcVisible(player, id),
                    "Old dungeon child copy remains visible before the rescue: " + id);
        }
        player.setPosition(new Position(2844, 3140, 0));
        check(player.getQuestManager().handleFirstObjectAction(19039, 2843, 3141), "Entrance not routed");
        check(player.getPosition().getY() == 3140, "Entered before speaking to Shanty");
        check(!GublinchChristmasEvent.reclaimRewards(player), "Unstarted account acquired rewards");
        check(player.getQuestManager().handleFirstNpcAction(828), "Shanty dialogue not routed");
        check(player.questHookStates[5] == 0, "Opening the introduction started the event before Continue");
        int staleContinue = continueWidget(player);
        walkAway(player,99);
        continueDialogue(player,staleContinue);
        check(player.questHookStates[5] == 0 && player.getDialogueManager().isDialogueInactive(),
                "Walking away or a stale Continue advanced the introduction");
        player.getQuestManager().handleFirstNpcAction(828);
        continueDialogue(player);
        check(player.questHookStates[5] == 0, "Event started before the instructions were acknowledged");
        staleContinue = continueWidget(player);
        walkAway(player,80);
        continueDialogue(player,staleContinue);
        check(player.questHookStates[5] == 0, "Minimap walking advanced the introduction");
        player.getQuestManager().handleFirstNpcAction(828);
        continueDialogue(player);
        continueDialogue(player);
        advanceToMenu(player, 80);
        check(player.questHookStates[5] == 1024, "Event did not start");
        checkQuestions(player, 80, 5);
        check(player.getQuestManager().handleSecondObjectAction(19039, 2843, 3141), "Entrance search not routed");
        finishConversation(player);
        check(player.questHookStates[5] == 1024, "Searching entrance changed progress");
        check(player.getQuestManager().handleItemOnNpc(828,10506), "Using shards on Shanty not routed");
        check(player.getDialogueManager().getDialogueStep() / 100 == 18, "Wrong shard-use conversation");
        finishConversation(player);
        check(player.getQuestManager().handleItemOnNpc(828,590), "Using another item on Shanty not routed");
        check(player.getDialogueManager().getDialogueStep() / 100 == 19, "Wrong item-use conversation");
        finishConversation(player);
        check(!GublinchChristmasEvent.isNpcVisible(player, 5046), "Rescued child visible too early");
        checkSharedSnowPiles(player);
        check(ObjectManager.findDynamicObjectByIdAt(19030,2841,3143,0) == null
                && ObjectManager.findDynamicObjectByIdAt(19031,2844,3143,0) == null, "Old snow piles remain");
        check(!player.getQuestManager().handleFirstObjectAction(19030,2841,3143), "Old snow pile position still accepted");
        player.getQuestManager().handleFirstObjectAction(19030, 2841, 3147);
        check(player.getInventoryManager().getItemAmount(10501) == 3, "Snow must give three balls");
        TickTask collection = (TickTask) World.getTaskScheduler().getTasks().get(
                World.getTaskScheduler().getTasks().size() - 1);
        for (int tick = 0; tick < 5; tick++) collection.tick();
        checkClimb(player, 19039, 2843, 3141, new Position(3168, 5320, 0));
        Npc target = gublinch();
        Position targetTile = target.getPosition().copy();
        check(NpcActionRouter.resolve(target.getDefinition(), 1) == NpcActionRouter.Action.PELT, "Pelt cache action not routed");
        check(!GublinchChristmasEvent.pelt(player, target), "Pelt worked without wielding balls");
        player.getInventoryManager().removeItem(new ItemStack(10501, 3));
        player.getEquipmentManager().getContainer().setItem(3, new ItemStack(10501, 3));
        player.setPosition(new Position(targetTile.getX() + 1, targetTile.getY(), 0));
        player.getLastKnownRegionPosition().set(player.getPosition());
        player.refreshLocalViewArea();
        new RoutedNpcActionTask(player, player.nextActionSequence(), target, 1, NpcActionRouter.Action.PELT).execute();
        TickTask pelting = (TickTask) World.getTaskScheduler().getTasks().get(
                World.getTaskScheduler().getTasks().size() - 1);
        check(pelting.getIntervalTicks() == 1,
                "Pelting pursuit must check movement every tick");
        // Exercise the scheduled action: first shot is immediate, then ticks 9 and 18.
        for (int elapsed = 0; elapsed <= 24; elapsed++) {
            advanceEventTicks(1);
            int shots = Math.min(3, 1 + elapsed / 9);
            check(player.getEquipmentManager().getContainer().getItemAmount(10501) == 3 - shots,
                    "Automatic pelting threw early or late at elapsed tick " + elapsed);
        }
        check(player.getEquipmentManager().getContainer().getItemAmount(10501) == 0, "Pelt did not consume three balls");
        GroundItem shards = GroundItemManager.findVisibleItem(player, 10506, targetTile);
        check(shards != null, "Three successful hits did not drop shards");
        check(GroundItemManager.findVisibleItem(other, 10506, targetTile) == null, "Shards leaked to another account");
        check(!GublinchChristmasEvent.pelt(player, target), "Removed target accepted another pelt");
        checkClimb(player, 19040, 3168, 5319, new Position(2844, 3143, 0));
        player.getInventoryManager().addItem(new ItemStack(10506, 11));
        player.getQuestManager().handleFirstNpcAction(828);
        check(player.getDialogueManager().getDialogueStep() / 100 == 3, "Shards-in-inventory reminder missing");
        advanceToMenu(player, 80);
        check(!GublinchChristmasEvent.useShardsOnCage(player,10506,19036,2907,3191,0),
                "Removed cage accepted shards");
        check(GublinchChristmasEvent.useShardsOnCage(player, 10506, 19036, 2907, 3183, 0), "Cage not handled");
        int left = player.getInventoryManager().getItemAmount(10506);
        player.getQuestManager().handleFirstNpcAction(828);
        check(player.getDialogueManager().getDialogueStep() / 100 == 4, "Caged-gublinch reminder missing");
        advanceToMenu(player, 80);
        GublinchChristmasEvent.useShardsOnCage(player, 10506, 19037, 2907, 3183, 0);
        check(player.getInventoryManager().getItemAmount(10506) == left, "Duplicate cage consumed shards");
        check(GublinchChristmasEvent.cageDisplayId(player, 19036, 2907, 3183, 0) == 19037, "Filled cage appearance missing");
        check(GublinchChristmasEvent.cageDisplayId(other, 19036, 2907, 3183, 0) == 19036, "Cage progress leaked to another account");
        check(SkillActionHelper.findWorldObjectById(19037, 2907, 3183, 0) != null, "Filled cage cannot receive item use");
        persistence(player);
        check(!GublinchChristmasEvent.useShardsOnCage(player, 10506, 19036, 3100, 3100, 0), "Unrelated cage accepted shards");
        for (int i = 1; i < 9; i++) GublinchChristmasEvent.useShardsOnCage(player, 10506, 19036, cages[i][0], cages[i][1], 0);
        check(!GublinchChristmasEvent.isNpcVisible(player, 5046), "Children escaped before all ten required cages were filled");
        GublinchChristmasEvent.useShardsOnCage(player, 10506, 19036, cages[9][0], cages[9][1], 0);
        check(GublinchChristmasEvent.cageDisplayId(player,19036,2907,3188,0) == 19037,
                "New required cage did not record progress");
        check(!GublinchChristmasEvent.isNpcVisible(player, 5046) && !GublinchChristmasEvent.isNpcVisible(player, 5023),
                "Dock children appeared before the completion handoff");
        player.getInventoryManager().getContainer().clear();
        player.getInventoryManager().addItem(new ItemStack(554, 1));
        player.getInventoryManager().addItem(new ItemStack(555, 1));
        for (int i = 0; i < 25; i++) player.getInventoryManager().addItem(new ItemStack(590, 1));
        check(player.getInventoryManager().getContainer().getFreeSlots() == 1, "Full-inventory fixture wrong: " + player.getInventoryManager().getContainer().getFreeSlots());
        check(player.isMember() && !ServerSettings.freeToPlayWorld, "Members fixture wrong");
        player.getQuestManager().handleFirstNpcAction(828);
        check(!GublinchChristmasEvent.isComplete(player) && !player.ownsItem(10507), "Reward granted before Continue");
        finishConversation(player);
        check(GublinchChristmasEvent.isComplete(player) && player.ownsItem(10507) && !player.ownsItem(10508),
                "One free slot must award the hat and defer the tree");
        for (int id = 5046; id <= 5048; id++) {
            check(GublinchChristmasEvent.isNpcVisible(player, id), "Completed player cannot see child " + id);
            check(!GublinchChristmasEvent.isNpcVisible(other, id), "Dock children leaked to unfinished player");
        }
        player.getInventoryManager().removeItem(new ItemStack(590, 1));
        player.getQuestManager().handleFirstNpcAction(828);
        finishConversation(player);
        check(GublinchChristmasEvent.isComplete(player) && player.ownsItem(10507) && player.ownsItem(10508), "Rewards/completion missing");
        player.getQuestManager().handleFirstNpcAction(828);
        advanceToMenu(player, 90);
        checkQuestions(player, 90, 3);
        check(player.getInventoryManager().getItemAmount(10507) == 1, "Repeated dialogue duplicated hat");
        persistence(player);
        player.getInventoryManager().removeItem(new ItemStack(10507, 1));
        player.getQuestManager().handleFirstNpcAction(970);
        check(player.getInventoryManager().getItemAmount(10507) == 1, "Diango did not recover hat");
        player.getInventoryManager().removeItem(new ItemStack(10507, 1));
        player.getBankContainer().addToTab(new ItemStack(10507, 1), 0);
        player.getQuestManager().handleFirstNpcAction(970);
        check(player.getInventoryManager().getItemAmount(10507) == 0, "Diango duplicated banked hat");
        player.getBankContainer().clear();
        player.getQuestManager().handleFirstNpcAction(970);
        player.getInventoryManager().removeItem(new ItemStack(10508, 1));
        ServerSettings.freeToPlayWorld = true;
        GublinchChristmasEvent.reclaimRewards(player);
        check(!player.ownsItem(10508), "Free world awarded members tree");
        ServerSettings.freeToPlayWorld = false;
        player.getQuestManager().handleFirstNpcAction(970);
        check(player.ownsItem(10508), "Members tree recovery failed");
        player.getEquipmentManager().getContainer().setItem(0, new ItemStack(10507, 1));
        player.setActionLocked(false);
        GublinchChristmasEvent.operateHat(player);
        check(player.getUpdateState().getAnimationId() == 5059, "Hat operation animation missing");
        checkHatEquipmentOperation();
        checkSnowballsAgainstNormalNpc();
        checkRewardBranches(other);
        checkPeltVisuals();
        checkGublinchRespawns();
        checkSnowballAccuracy();
        checkPeltingRangeAndPursuit();
        System.out.println("Gublinch Christmas checks passed: dungeon population, roaming, respawns, routing, terrain, snowballs, pelting, private shards, cages, rescue, rewards, reclaim and persistence.");
        System.exit(0);
    }

    private static void checkPeltingRangeAndPursuit() throws Exception {
        // Find a real, straight cave corridor long enough to approach and follow.
        Position origin = null;
        int dx = 0, dy = 0;
        search: for (int x = 3158; x < 3198; x++) for (int y = 5320; y < 5374; y++) {
            for (int direction = 0; direction < 2; direction++) {
                int sx = direction == 0 ? 1 : 0, sy = direction == 0 ? 0 : 1;
                boolean clear = true;
                for (int step = 0; step <= 12; step++) {
                    int tx = x + sx * step, ty = y + sy * step;
                    if ((WalkingCollisionMap.getTileFlags(tx, ty, 0) & 0x200100) != 0
                            || step > 0 && !WalkingCollisionMap.canTravelBetween(
                                    tx - sx, ty - sy, tx, ty, 0, 1, 1)) {
                        clear = false;
                        break;
                    }
                }
                if (clear && com.rs2.util.GameUtil.hasClearPath(
                        new Position(x, y, 0), new Position(x + sx * 12, y + sy * 12, 0), true)) {
                    origin = new Position(x, y, 0);
                    dx = sx; dy = sy;
                    break search;
                }
            }
        }
        check(origin != null, "No real cave corridor for ranged pursuit checks");
        Player thrower = player(10);
        thrower.questHookStates[5] = 1 << 10;
        thrower.setPosition(origin.copy());
        thrower.getLastKnownRegionPosition().set(origin);
        thrower.refreshLocalViewArea();
        thrower.getMovementQueue().setRunning(true);
        thrower.getEquipmentManager().getContainer().setItem(3, new ItemStack(10501, 6));
        Npc target = new Npc(5017);
        target.moveTo(new Position(origin.getX() + dx * 10, origin.getY() + dy * 10, 0));
        target.setSpawnPosition(target.getPosition().copy());
        World.registerNpc(target);
        try {
            // Use the actual Pelt click; it must immediately suppress melee following.
            normalNpcAttack(thrower, target);
            check(thrower.getMovementTarget() == null, "Pelt click retained melee following");
            advanceEventTicks(1);
            check(thrower.getEquipmentManager().getContainer().getItemAmount(10501) == 6,
                    "Out-of-range click threw before approaching");
            check(!thrower.getMovementQueue().getSteps().isEmpty(), "Pelt click did not approach");
            Position end = (Position) thrower.getMovementQueue().getSteps().getLast();
            check(com.rs2.util.GameUtil.getDistance(end, target.getPosition()) == 6,
                    "Approach did not end at maximum throwing range");
            for (int tick = 0; tick < 8
                    && thrower.getEquipmentManager().getContainer().getItemAmount(10501) == 6; tick++) {
                thrower.getTargetMovement().process();
                thrower.getMovementQueue().process();
                advanceEventTicks(1);
            }
            check(thrower.getEquipmentManager().getContainer().getItemAmount(10501) == 5
                            && com.rs2.util.GameUtil.getDistance(thrower.getPosition(), target.getPosition()) == 6,
                    "Running approach overshot range or failed to throw");
            check(thrower.getMovementQueue().getSteps().size() <= 1, "Approach kept moving after throwing");

            target.moveTo(new Position(origin.getX() + dx * 12, origin.getY() + dy * 12, 0));
            advanceEventTicks(1);
            check(!thrower.getMovementQueue().getSteps().isEmpty(), "Escaping target was not followed");
            end = (Position) thrower.getMovementQueue().getSteps().getLast();
            check(com.rs2.util.GameUtil.getDistance(end, target.getPosition()) == 6,
                    "Pursuit chased closer than maximum throwing range");
            thrower.getMovementQueue().process();
            check(com.rs2.util.GameUtil.getDistance(thrower.getPosition(), target.getPosition()) == 6,
                    "Running pursuit overshot maximum throwing range");
            for (int elapsed = 2; elapsed <= 9; elapsed++) {
                advanceEventTicks(1);
                int expected = elapsed < 9 ? 5 : 4;
                check(thrower.getEquipmentManager().getContainer().getItemAmount(10501) == expected,
                        "Pursuit reset the throw cadence or failed to resume automatically");
            }

            // A new player command cancels pursuit without deleting the new walk.
            thrower.nextActionSequence();
            thrower.getMovementQueue().addStep(new Position(
                    thrower.getPosition().getX() - dx, thrower.getPosition().getY() - dy, 0));
            int steps = thrower.getMovementQueue().getSteps().size();
            advanceEventTicks(1);
            check(thrower.getMovementQueue().getSteps().size() == steps,
                    "Old pelting action erased a newer movement command");
            advanceEventTicks(10);
            check(thrower.getEquipmentManager().getContainer().getItemAmount(10501) == 4,
                    "Cancelled pursuit kept throwing");
        } finally {
            thrower.nextActionSequence();
            World.unregisterNpc(target);
        }
    }

    private static void checkSnowballsAgainstNormalNpc() throws Exception {
        Player attacker = player(9);
        Npc target = new Npc(1);
        target.moveTo(new Position(2919, 3175, 0));
        target.setSpawnPosition(target.getPosition().copy());
        World.registerNpc(target);
        try {
            check("Attack".equalsIgnoreCase(target.getDefinition().getAction(1)),
                    "Normal NPC attack fixture changed");
            attacker.getEquipmentManager().getContainer().setItem(3, new ItemStack(10501, 6));
            normalNpcAttack(attacker, target);
            check(attacker.getEquipmentManager().getItemIdAtSlot(3) != 10501,
                    "Normal NPC attack retained equipped snowballs");
            check(attacker.getInventoryManager().getItemAmount(10501) == 6,
                    "Normal NPC attack consumed or lost snowballs");
            check(attacker.getCombatTarget() == target && attacker.getAttackRange() == 1,
                    "Normal NPC attack did not continue with unarmed combat");
            check(attacker.getUpdateState().getAnimationId() != 5063
                            && attacker.getUpdateState().getGraphicId() != 860,
                    "Normal NPC attack started a snowball throw");
            com.rs2.model.combat.CombatManager.stopCombat(attacker);

            // No safe inventory destination: retain the entire stack and refuse combat.
            attacker.getInventoryManager().getContainer().clear();
            for (int slot = 0; slot < 28; slot++)
                attacker.getInventoryManager().getContainer().setItem(slot, new ItemStack(590, 1));
            attacker.getEquipmentManager().getContainer().setItem(3, new ItemStack(10501, 6));
            normalNpcAttack(attacker, target);
            check(attacker.getEquipmentManager().getContainer().getItemAmount(10501) == 6
                            && attacker.getInventoryManager().getItemAmount(10501) == 0,
                    "Full inventory lost equipped snowballs");
            check(attacker.getCombatTarget() == null, "Full inventory allowed snowball combat");
        } finally {
            com.rs2.model.combat.CombatManager.stopCombat(attacker);
            World.unregisterNpc(target);
        }
    }

    private static void normalNpcAttack(Player player, Npc target) {
        ByteBuffer payload = ByteBuffer.allocate(2);
        payload.putShort((short)target.getIndex()).flip();
        new com.rs2.net.packet.handler.NpcInteractionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.NPC_OPTION_2, 2, PacketBuffer.wrapReader(payload)));
    }

    private static void checkHatEquipmentOperation() throws Exception {
        Player wearer = player(8);
        wearer.setSidebarInterfaceId(4, 1644);
        wearer.getEquipmentManager().getContainer().setItem(0, new ItemStack(10507, 1));
        // Exercise the native equipment Operate packet, not a direct helper call.
        try (ServerSocketChannel listener = ServerSocketChannel.open()) {
            listener.bind(new InetSocketAddress("127.0.0.1", 0));
            try (SocketChannel sender = SocketChannel.open(listener.getLocalAddress());
                 SocketChannel receiver = listener.accept()) {
                receiver.socket().setSoTimeout(3000);
                Field socket = Player.class.getDeclaredField("socketChannel");
                socket.setAccessible(true);
                socket.set(wearer, sender);
                wearer.setOutboundCipher(new IsaacCipher(new int[4]));
                hatOperatePacket(wearer, 1, 10507);
                check(wearer.getUpdateState().getAnimationId() != 5059,
                        "Wrong equipment slot activated the hat");
                hatOperatePacket(wearer, 0, 10501);
                check(wearer.getUpdateState().getAnimationId() != 5059,
                        "Mismatched item activated the hat");
                wearer.setActionLocked(true);
                hatOperatePacket(wearer, 0, 10507);
                check(wearer.getUpdateState().getAnimationId() != 5059,
                        "Locked player activated the hat");
                wearer.setActionLocked(false);
                hatOperatePacket(wearer, 0, 10507);
                check(wearer.getUpdateState().getAnimationId() == 5059
                                && wearer.getUpdateState().getGraphicId() == 859,
                        "Native equipment Operate did not start the reindeer emote");
                DataInputStream input = new DataInputStream(receiver.socket().getInputStream());
                IsaacCipher decoder = new IsaacCipher(new int[4]);
                check(((input.readUnsignedByte() - decoder.nextInt()) & 255) == 157,
                        "Reindeer emote did not send a chatbox game message");
                byte[] message = new byte[input.readUnsignedByte()];
                input.readFully(message);
                check(message[message.length - 1] == 0
                                && new String(message, 0, message.length - 1, "ISO-8859-1")
                                        .equals("You act like a reindeer."),
                        "Reindeer emote chat text or punctuation changed");
                check(wearer.getEquipmentManager().getItemIdAtSlot(0) == 10507,
                        "Operating the hat unequipped it");
                TickTask finish = (TickTask) World.getTaskScheduler().getTasks().get(
                        World.getTaskScheduler().getTasks().size() - 1);
                check(finish.getIntervalTicks() == 8, "Reindeer replay lock must cover the 4.4-second emote");
                for (int tick = 0; tick < 8; tick++) {
                    wearer.getUpdateState().reset();
                    int tasks = World.getTaskScheduler().getTasks().size();
                    for (int click = 0; click < 5; click++) hatOperatePacket(wearer, 0, 10507);
                    check(!wearer.getUpdateState().isAnimationUpdateRequired()
                                    && !wearer.getUpdateState().isGraphicUpdateRequired(),
                            "Repeated Operate restarted the reindeer animation or effect");
                    check(World.getTaskScheduler().getTasks().size() == tasks && input.available() == 0,
                            "Repeated Operate scheduled extra releases or spammed chat");
                    check(finish.isActive(), "Reindeer lock ended before the animation completed");
                    finish.tick();
                }
                check(!finish.isActive() && wearer.getUpdateState().getAnimationId() == -1
                                && wearer.getUpdateState().getGraphicId() == -1,
                        "Reindeer animation/effect did not finish cleanly");
                hatOperatePacket(wearer, 0, 10507);
                check(wearer.getUpdateState().getAnimationId() == 5059
                                && wearer.getUpdateState().getGraphicId() == 859,
                        "Reindeer emote could not replay after completion");
                check(((input.readUnsignedByte() - decoder.nextInt()) & 255) == 157,
                        "Replayed emote did not send its chat message");
                byte[] replayMessage = new byte[input.readUnsignedByte()];
                input.readFully(replayMessage);
                check(new String(replayMessage, 0, replayMessage.length - 1, "ISO-8859-1")
                                .equals("You act like a reindeer."),
                        "Replayed emote message changed");
                TickTask replayFinish = (TickTask) World.getTaskScheduler().getTasks().get(
                        World.getTaskScheduler().getTasks().size() - 1);
                wearer.getUpdateState().setAnimation(855);
                wearer.getUpdateState().setGraphic(86);
                for (int tick = 0; tick < 8; tick++) replayFinish.tick();
                check(wearer.getUpdateState().getAnimationId() == 855
                                && wearer.getUpdateState().getGraphicId() == 86,
                        "Old reindeer cleanup erased a different action's animation");

            }
        }
    }

    private static void hatOperatePacket(Player player, int slot, int item) {
        int widget = 387 << 16 | 25;
        ByteBuffer payload = ByteBuffer.allocate(8);
        // Native option 2: inverse-middle widget, little-endian item, add/little slot.
        payload.put((byte)(widget >> 16)).put((byte)(widget >> 24))
                .put((byte)widget).put((byte)(widget >> 8))
                .put((byte)item).put((byte)(item >> 8))
                .put((byte)(slot + 128)).put((byte)(slot >> 8)).flip();
        new com.rs2.net.packet.handler.ItemActionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.WIDGET_ITEM_OPTION_2, 8, PacketBuffer.wrapReader(payload)));
    }

    private static void checkSharedSnowPiles(Player collector) throws Exception {
        int[][] tiles = {{2841,3147}, {2844,3147}, {2840,3150}, {2842,3153}};
        Player peer = player(6);
        Player lateViewer = player(7);
        peer.questHookStates[5] = 1 << 10;
        Player previousPeer = World.getPlayers()[6], previousViewer = World.getPlayers()[7];
        World.getPlayers()[6] = peer;
        try {
            for (int[] tile : tiles) {
                int x = tile[0], y = tile[1];
                peer.setPosition(new Position(x + 1, y, 0));
                peer.getLastKnownRegionPosition().set(peer.getPosition());
                peer.refreshLocalViewArea();
                ObjectManager.getInstance().refreshDynamicObjectsForPlayer(peer);
                DynamicObject large = ObjectManager.findDynamicObjectByIdAt(19030, x, y, 0);
                check(large != null, "Snow pile did not start large at " + x + "," + y);
                int collision = WalkingCollisionMap.getTileFlags(x, y, 0);
                // A failed inventory addition must not reserve or deplete the pile.
                for (int slot = 0; slot < 28; slot++)
                    peer.getInventoryManager().getContainer().setItem(slot, new ItemStack(590, 1));
                peer.getQuestManager().handleFirstObjectAction(19030, x, y);
                check(!peer.isActionLocked()
                                && ObjectManager.findDynamicObjectAt(x, y, 0) == large,
                        "Full inventory reserved or depleted the shared pile");
                for (int slot = 0; slot < 28; slot++)
                    peer.getInventoryManager().getContainer().setItem(slot, null);

                check(collector.getQuestManager().handleFirstObjectAction(19030, x, y),
                        "Snow pile collection was not routed");
                check(collector.getInventoryManager().getItemAmount(10501) == 3,
                        "Collection did not award three snowballs");
                check(collector.getUpdateState().getAnimationId() == 5067 && collector.isActionLocked(),
                        "Collection did not retain its five-tick animation lock");
                TickTask collection = (TickTask) World.getTaskScheduler().getTasks().get(
                        World.getTaskScheduler().getTasks().size() - 1);
                for (int tick = 0; tick < 5; tick++) {
                    collector.getQuestManager().handleFirstObjectAction(19030, x, y);
                    peer.getQuestManager().handleFirstObjectAction(19030, x, y);
                    check(collector.getInventoryManager().getItemAmount(10501) == 3
                                    && peer.getInventoryManager().getItemAmount(10501) == 0
                                    && !peer.isActionLocked(),
                            "Another player or repeated click collected a reserved pile");
                    check(ObjectManager.findDynamicObjectAt(x, y, 0) == large,
                            "Large pile shrank before collection completed");
                    collection.tick();
                }
                check(!collector.isActionLocked() && !collection.isActive(),
                        "Collection failed to unlock");
                DynamicObject small = ObjectManager.findDynamicObjectByIdAt(19035, x, y, 0);
                check(small != null && small.owner == null, "Collection did not create shared small snow");
                check(!peer.visibleDynamicObjects.contains(large) && peer.visibleDynamicObjects.contains(small),
                        "Existing viewer retained stale large-pile state");
                ObjectManager.getInstance().refreshDynamicObjectsForPlayer(peer);
                check(peer.visibleDynamicObjects.contains(small), "Refresh lost the depleted pile");

                // A player arriving after depletion sees the same unavailable object.
                lateViewer.setPosition(peer.getPosition().copy());
                lateViewer.getLastKnownRegionPosition().set(lateViewer.getPosition());
                lateViewer.refreshLocalViewArea();
                World.getPlayers()[7] = lateViewer;
                ObjectManager.getInstance().refreshDynamicObjectsForPlayer(lateViewer);
                check(lateViewer.visibleDynamicObjects.contains(small),
                        "Late viewer did not see the shared depleted pile");
                TickTask regrowth = (TickTask) World.getTaskScheduler().getTasks().get(
                        World.getTaskScheduler().getTasks().size() - 1);
                int delay = regrowth.getIntervalTicks();
                check(delay >= 6 && delay <= 10, "Regrowth estimate escaped six-to-ten tick range");
                for (int elapsed = 1; elapsed < delay; elapsed++) {
                    // Test both real small-object clicks and stale large-object clicks.
                    collector.getQuestManager().handleFirstObjectAction(19035, x, y);
                    peer.getQuestManager().handleFirstObjectAction(19030, x, y);
                    check(collector.getInventoryManager().getItemAmount(10501) == 3
                                    && peer.getInventoryManager().getItemAmount(10501) == 0,
                            "Small pile or stale large-pile click awarded snowballs");
                    regrowth.tick();
                    check(ObjectManager.findDynamicObjectAt(x, y, 0) == small,
                            "Pile regrew before the selected delay");
                }
                regrowth.tick();
                DynamicObject regrown = ObjectManager.findDynamicObjectByIdAt(19030, x, y, 0);
                check(regrown != null && regrown != large && !regrowth.isActive(),
                        "Small pile did not regrow at the selected delay");
                check(!peer.visibleDynamicObjects.contains(small)
                                && !lateViewer.visibleDynamicObjects.contains(small)
                                && lateViewer.visibleDynamicObjects.contains(regrown),
                        "Regrowth did not update existing and late viewers");
                check(WalkingCollisionMap.getTileFlags(x, y, 0) == collision,
                        "Snow-pile size change altered walking collision");
                collector.getInventoryManager().removeItem(new ItemStack(10501, 3));
            }
        } finally {
            World.getPlayers()[6] = previousPeer;
            World.getPlayers()[7] = previousViewer;
        }
    }

    private static void checkGublinchSpawns() {
        // Flood-fill real collision through the cave's winding corridors.
        Set<String> reachable = new HashSet<String>();
        ArrayDeque<Position> pending = new ArrayDeque<Position>();
        pending.add(new Position(3168, 5320, 0));
        reachable.add("3168,5320");
        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};
        while (!pending.isEmpty()) {
            Position from = pending.remove();
            for (int[] direction : directions) {
                int x = from.getX() + direction[0], y = from.getY() + direction[1];
                if (x < 3136 || x > 3199 || y < 5312 || y > 5375 || reachable.contains(x + "," + y)) continue;
                if (WalkingCollisionMap.canTravelBetween(from.getX(), from.getY(), x, y, 0, 1, 1)) {
                    reachable.add(x + "," + y);
                    pending.add(new Position(x, y, 0));
                }
            }
        }
        Set<String> anchors = new HashSet<String>();
        int west = 0, east = 0, north = 0;
        for (Npc npc : World.getNpcs()) {
            if (npc == null || !GublinchChristmasEvent.isGublinch(npc.getOriginalNpcId())) continue;
            Position spawn = npc.getSpawnPosition();
            String key = spawn.getX() + "," + spawn.getY();
            check(anchors.add(key), "Duplicate Gublinch spawn at " + spawn);
            check(spawn.getPlane() == 0 && reachable.contains(key), "Gublinch unreachable from ladder: " + spawn);
            check(npc.getMovementMode() == NpcMovementMode.ROAMING, "Gublinch cannot wander: " + spawn);
            int destinations = 0;
            for (int[] direction : directions) {
                int x = spawn.getX() + direction[0], y = spawn.getY() + direction[1];
                if (x >= npc.getSpawnMinPosition().getX() && x < npc.getSpawnMaxPosition().getX()
                        && y >= npc.getSpawnMinPosition().getY() && y < npc.getSpawnMaxPosition().getY()
                        && WalkingCollisionMap.canTravelBetween(spawn.getX(), spawn.getY(), x, y, 0, 1, 1))
                    destinations++;
            }
            check(destinations >= 2, "Gublinch has no room to wander: " + spawn);
            if (spawn.getX() < 3155) west++;
            if (spawn.getX() > 3180) east++;
            if (spawn.getY() >= 5355) north++;
        }
        check(anchors.size() == 15, "Expected fifteen dungeon Gublinch, got " + anchors.size());
        check(west >= 4 && east >= 4 && north >= 4, "Gublinch do not cover both loops and the northern chamber");
    }

    private static void checkGublinchRespawns() throws Exception {
        // Finish the earlier single-target check's pending respawn first.
        advanceEventTicks(12);
        checkGublinchSpawns();
        Player thrower = player(3);
        thrower.questHookStates[5] = 1 << 10;
        thrower.getEquipmentManager().getContainer().setItem(3, new ItemStack(10501, 48));
        Set<String> anchors = new HashSet<String>();
        for (Npc npc : World.getNpcs().clone()) {
            if (npc == null || !GublinchChristmasEvent.isGublinch(npc.getOriginalNpcId())) continue;
            Position spawn = npc.getSpawnPosition();
            anchors.add(spawn.getX() + "," + spawn.getY());
            int index = npc.getIndex();
            thrower.setPosition(new Position(npc.getPosition().getX() + 1, npc.getPosition().getY(), 0));
            check(GublinchChristmasEvent.pelt(thrower, npc), "First snowball failed");
            advanceEventTicks(3);
            check(GublinchChristmasEvent.pelt(thrower, npc), "Second snowball failed");
            advanceEventTicks(3);
            check(npc.isActive() && World.getNpcs()[index] == npc,
                    "Gublinch must survive two successful hits");
            check(!GublinchChristmasEvent.pelt(thrower, npc), "Third successful shot did not finish pelting");
            check(npc.isActive(), "Third throw removed the target before impact");
            advanceEventTicks(3);
            check(World.getNpcs()[index] != npc, "Frozen Gublinch was not removed");
            check(!npc.isActive(), "Removed Gublinch remains visible in local NPC lists");
        }
        advanceEventTicks(12);
        checkGublinchSpawns();
        for (Npc npc : World.getNpcs()) {
            if (npc != null && GublinchChristmasEvent.isGublinch(npc.getOriginalNpcId())) {
                Position spawn = npc.getSpawnPosition();
                check(anchors.remove(spawn.getX() + "," + spawn.getY()) && npc.getNpcId() == 5017,
                        "Gublinch respawned at another anchor or remained frozen");
            }
        }
        check(anchors.isEmpty(), "Gublinch respawns missing");
    }

    private static void checkSnowballAccuracy() throws Exception {
        Method chance = GublinchChristmasEvent.class.getDeclaredMethod("snowballHitChance", int.class);
        Method roll = GublinchChristmasEvent.class.getDeclaredMethod("snowballHits", int.class, int.class);
        Method pelt = GublinchChristmasEvent.class.getDeclaredMethod("pelt", Player.class, Npc.class, int.class);
        chance.setAccessible(true);
        roll.setAccessible(true);
        pelt.setAccessible(true);
        int[][] table = {
            {1,5900}, {10,6442}, {20,7044}, {30,7646}, {40,8248}, {50,8850},
            {60,9452}, {69,9994}, {70,10000}, {99,10000},
            {0,5900}, {5,6141}, {15,6743}, {65,9753}
        };
        for (int[] row : table) {
            check((Integer) chance.invoke(null, row[0]) == row[1],
                    "Incorrect snowball hit chance at Ranged " + row[0]);
            int hits = 0;
            for (int value = 0; value < 10000; value++) {
                if ((Boolean) roll.invoke(null, row[0], value)) hits++;
            }
            check(hits == row[1], "Accuracy roll has an off-by-one at Ranged " + row[0]);
        }

        advanceEventTicks(12);
        Npc target = gublinch();
        Position tile = target.getPosition().copy();
        Player thrower = player(5);
        thrower.questHookStates[5] = 1 << 10;
        thrower.getSkillManager().getCurrentLevels()[4] = 1;
        thrower.setPosition(new Position(tile.getX() + 1, tile.getY(), 0));
        thrower.getLastKnownRegionPosition().set(thrower.getPosition());
        thrower.refreshLocalViewArea();
        thrower.getEquipmentManager().getContainer().setItem(3, new ItemStack(10501, 6));

        // Miss before each successful hit, including the potentially lethal third hit.
        // Misses must neither advance nor reset the normal/chilled/frozen progression.
        int successfulHits = 0;
        for (int shot = 0; shot < 6; shot++) {
            boolean hit = (shot & 1) != 0;
            int previousForm = target.getNpcId();
            boolean keepPelting = (Boolean) pelt.invoke(null, thrower, target, hit ? 0 : 9999);
            if (hit) successfulHits++;
            check(keepPelting == (successfulHits < 3), "Miss stopped automatic pelting");
            check(thrower.getEquipmentManager().getContainer().getItemAmount(10501) == 5 - shot,
                    "A missed shot did not consume exactly one snowball");
            advanceEventTicks(2);
            check(target.isActive() && target.getNpcId() == previousForm,
                    "Accuracy result was applied before tick 3");
            advanceEventTicks(1);
            if (successfulHits < 3) {
                check(target.isActive() && target.getNpcId() == 5017 + successfulHits,
                        "Miss advanced or reset the Gublinch freeze stage");
                check(GroundItemManager.findVisibleItem(thrower, 10506, tile) == null,
                        "Misses caused a premature shard drop");
            } else {
                check(!target.isActive() && GroundItemManager.findVisibleItem(thrower, 10506, tile) == null,
                        "Fatal hit must remove the Gublinch before showing shards");
                advanceEventTicks(1);
                check(GroundItemManager.findVisibleItem(thrower, 10506, tile) != null,
                        "Shards did not appear one tick after removal");
            }
            check(thrower.getUpdateState().getAnimationId() == -1
                            && thrower.getUpdateState().getGraphicId() == -1,
                    "Misses changed the tick-3 animation completion");
        }
    }

    private static void advanceEventTicks(int count) {
        for (int i = 0; i < count; i++) {
            for (Object task : new ArrayList<Object>(World.getTaskScheduler().getTasks())) {
                TickTask tick = (TickTask) task;
                if (tick.isActive()) tick.tick();
            }
        }
    }

    private static void checkPeltVisuals() throws Exception {
        ConfigReader graphic = new ConfigReader(Definitions.readGroup(13).get(861));
        check(graphic.readUnsignedByte() == 1 && graphic.readUnsignedShort() == 20502,
                "Snowball projectile uses the wrong cache model");
        check(graphic.readUnsignedByte() == 2 && graphic.readUnsignedShort() == 5065,
                "Snowball projectile uses the wrong cache sequence");
        ConfigReader hand = new ConfigReader(Definitions.readGroup(13).get(860));
        check(hand.readUnsignedByte() == 1 && hand.readUnsignedShort() == 20502
                        && hand.readUnsignedByte() == 2 && hand.readUnsignedShort() == 5064,
                "Hand effect must retain the matching native throw sequence");
        advanceEventTicks(12);
        Npc target = gublinch();
        Position tile = target.getPosition().copy();
        Player thrower = player(3);
        thrower.questHookStates[5] = 1 << 10;
        thrower.setPosition(new Position(tile.getX() + 1, tile.getY(), 0));
        thrower.getLastKnownRegionPosition().set(thrower.getPosition());
        thrower.refreshLocalViewArea();
        thrower.getEquipmentManager().getContainer().setItem(3, new ItemStack(10501, 3));
        check(!thrower.shouldHideEquipmentItemInAppearance(3),
                "Equipped snowball must remain visible between throws");
        check(!thrower.shouldHideEquipmentItemInAppearance(5),
                "Snowball must not hide the shield slot");
        check(thrower.getStandAnimation() == 808 && thrower.getWalkAnimation() == 819
                && thrower.getRunAnimation() == 824, "Snowball must use player-safe movement animations");
        Player observer = player(4);
        observer.setPosition(thrower.getPosition().copy());
        observer.getLastKnownRegionPosition().set(observer.getPosition());
        observer.refreshLocalViewArea();
        // Capture actual zone packets over loopback, independently of pelting's
        // return value and the server's animation state.
        try (ServerSocketChannel listener = ServerSocketChannel.open()) {
            listener.bind(new InetSocketAddress("127.0.0.1", 0));
            try (SocketChannel sender = SocketChannel.open(listener.getLocalAddress());
                 SocketChannel receiver = listener.accept()) {
                receiver.socket().setSoTimeout(2000);
                Field socket = Player.class.getDeclaredField("socketChannel");
                socket.setAccessible(true);
                socket.set(observer, sender);
                World.getPlayers()[4] = observer;
                observer.getLocalNpcs().add(target);
                IsaacCipher decoder = new IsaacCipher(new int[4]);
                DataInputStream input = new DataInputStream(receiver.socket().getInputStream());
                for (int hit = 0; hit < 3; hit++) {
                    // The fixed timeline must hold at short, medium and maximum range.
                    int distance = new int[]{1, 3, 6}[hit];
                    thrower.setPosition(new Position(tile.getX() + distance, tile.getY(), 0));
                    thrower.getLastKnownRegionPosition().set(thrower.getPosition());
                    thrower.refreshLocalViewArea();
                    int previousForm = target.getNpcId();
                    check(GublinchChristmasEvent.pelt(thrower, target) == (hit < 2), "Pelt progression wrong");
                    check(thrower.getEquipmentManager().getContainer().getItemAmount(10501) == 2 - hit,
                            "Each successful throw must consume exactly one snowball");
                    check(target.isActive() && target.getNpcId() == previousForm,
                            "Gublinch reacted before the projectile arrived");
                    check(GroundItemManager.findVisibleItem(thrower, 10506, tile) == null,
                            "Shards appeared before the final projectile arrived");
                    check(GublinchChristmasEvent.pelt(thrower, target)
                                    && thrower.getEquipmentManager().getContainer().getItemAmount(10501) == 2 - hit,
                            "An overlapping throw consumed extra ammo");
                    byte[] packet = new byte[19];
                    input.readFully(packet);
                    check(((packet[0] & 255) - decoder.nextInt() & 255) == 82, "Projectile zone base missing");
                    check(((packet[3] & 255) - decoder.nextInt() & 255) == 101, "Snowball projectile packet missing");
                    check(packet[5] == -distance && packet[6] == 0, "Snowball flies to the wrong tile");
                    check(packet[7] == 0 && packet[8] == 0, "Snowball follows a removed/reused NPC index");
                    check(((packet[9] & 255) << 8 | packet[10] & 255) == 861, "Hand effect was used as the flying projectile");
                    check(thrower.getUpdateState().getGraphicId() == 860, "Animated snowball in the throwing hand missing");
                    check(thrower.getUpdateState().getAnimationId() == 5063,
                            "Throw animation was cancelled on the final launch");
                    check(thrower.getUpdateState().getAnimationDelay() == 10
                                    && (thrower.getUpdateState().getGraphicDelay() & 65535) == 10,
                            "Throw animation and hand effect must share a 200 ms delay");
                    int startCycle = (packet[13] & 255) << 8 | packet[14] & 255;
                    int endCycle = (packet[15] & 255) << 8 | packet[16] & 255;
                    check(startCycle == 75 && endCycle == 90,
                            "Snowball must wait until 1,500 ms to launch and arrive at 1,800 ms at every range");
                    check(com.rs2.model.combat.ProjectileTiming.STANDARD.getStartDelay() == 44,
                            "Snowball timing changed other projectiles");
                    advanceEventTicks(2);
                    check(target.isActive() && target.getNpcId() == previousForm,
                            "Hit was applied before tick 3");
                    check(thrower.getUpdateState().getAnimationId() == 5063
                                    && thrower.getUpdateState().getGraphicId() == 860,
                            "Throw animation or hand effect ended before tick 3");
                    advanceEventTicks(1);
                    if (hit < 2) {
                        check(target.isActive() && target.getNpcId() == 5017 + Math.min(hit + 1, 2),
                                "Gublinch did not react when the snowball arrived");
                    } else {
                        check(!target.isActive() && GroundItemManager.findVisibleItem(thrower, 10506, tile) == null,
                                "Final impact must remove the Gublinch before its drop appears");
                    }
                    check(thrower.getUpdateState().getAnimationId() == -1
                                    && thrower.getUpdateState().getGraphicId() == -1,
                            "Throw animation and hand effect must finish together on tick 3");
                }
                check(!target.isActive(), "Frozen target remains active");
                check(thrower.getInteractionTarget() == null && thrower.getMovementQueue().getSteps().size() <= 1,
                        "Player still follows the removed target");
                NpcUpdateTask.updatePlayer(observer);
                check(!observer.getLocalNpcs().contains(target), "Frozen target survives the next NPC update");
                byte[] header = new byte[3];
                input.readFully(header);
                check(((header[0] & 255) - decoder.nextInt() & 255) == 238, "NPC update missing");
                byte[] update = new byte[(header[1] & 255) << 8 | header[2] & 255];
                input.readFully(update);
                check((update[0] & 255) == 1 && (update[1] & 0xe0) == 0xe0,
                        "Final hit did not serialize immediate NPC removal");
                check(GroundItemManager.findVisibleItem(thrower, 10506, tile) == null,
                        "Shards appeared before the NPC removal update was delivered");
                // Even if the removed instance moves, loot stays on its death tile.
                target.moveTo(new Position(tile.getX() + 2, tile.getY(), 0));
                advanceEventTicks(1);
                GroundItem drop = GroundItemManager.findVisibleItem(thrower, 10506, tile);
                check(drop != null && drop.getItem().getAmount() == 1,
                        "Private shard did not appear on the death tile the following tick");
                check(GroundItemManager.findVisibleItem(observer, 10506, tile) == null,
                        "Delayed shard leaked to a different player");
                advanceEventTicks(1);
                check(GroundItemManager.findVisibleItem(thrower, 10506, tile) == drop
                                && drop.getItem().getAmount() == 1,
                        "Delayed drop task created duplicate shards");
            } finally {
                World.getPlayers()[4] = null;
            }
        }
    }

    private static void checkClimb(Player player, int id, int x, int y, Position destination) {
        Position before = player.getPosition().copy();
        check(player.getQuestManager().handleFirstObjectAction(id, x, y), "Climb not routed: " + id);
        check(player.getUpdateState().getAnimationId() == 828, "Climb animation missing: " + id);
        check(player.isActionLocked(), "Climb did not lock actions: " + id);
        check(player.getPosition().equals(before), "Climb moved before animation: " + id);
        CycleEventHandler events = CycleEventHandler.getInstance();
        events.process();
        check(player.getPosition().equals(before), "Climb delay skipped: " + id);
        events.process();
        check(player.getPosition().equals(destination), "Climb destination wrong: " + id);
        check(!player.isActionLocked(), "Climb left actions locked: " + id);
        check(player.getUpdateState().getAnimationId() == 65535, "Climb animation not cleared: " + id);
        events.process();
        check(!player.isActionLocked(), "Climb continuation left actions locked: " + id);
    }

    private static void continueDialogue(Player player) {
        continueDialogue(player,continueWidget(player));
    }
    private static int continueWidget(Player player) {
        int group = InterfaceBridge.translateGroup(player.getOpenInterfaceId());
        int child = group >= 241 && group <= 244 ? group - 238 : group >= 64 && group <= 67 ? group - 61
                : group == 210 ? 1 : group >= 211 && group <= 214 ? group - 209 : group == 102 ? 3 : group == 249 ? 2 : -1;
        check(child >= 0,"Missing Continue widget for group " + group);
        return group << 16 | child;
    }
    private static void advanceToMenu(Player player, int step) {
        for (int i = 0; i < 100 && player.getDialogueManager().getDialogueStep() != step; i++) {
            check(!player.getDialogueManager().isDialogueInactive(), "Conversation ended before menu " + step);
            continueDialogue(player);
        }
        check(player.getDialogueManager().getDialogueStep() == step, "Dialogue never reached menu " + step);
    }
    private static void finishConversation(Player player) {
        for (int i = 0; i < 100 && !player.getDialogueManager().isDialogueInactive(); i++) continueDialogue(player);
        check(player.getDialogueManager().isDialogueInactive(), "Conversation did not end");
    }
    private static void checkQuestions(Player player, int menu, int count) {
        for (int option = 1; option < count; option++) {
            selectOption(player, menu, option);
            advanceToMenu(player, menu);
        }
        selectOption(player, menu, count);
        finishConversation(player);
    }
    private static void selectOption(Player player, int menu, int option) {
        continueDialogue(player, InterfaceBridge.translate((menu == 80 ? 2493 : 2470) + option));
    }
    private static void checkCharosDialogue() throws Exception {
        java.io.PrintStream original = System.out;
        java.io.ByteArrayOutputStream trace = new java.io.ByteArrayOutputStream();
        boolean debug = ServerSettings.debugModeEnabled;
        try {
            System.setOut(new java.io.PrintStream(trace, true, "UTF-8"));
            ServerSettings.debugModeEnabled = true;
            Player player = player(3);
            player.getQuestManager().handleFirstNpcAction(828);
            continueDialogue(player);
            check(InterfaceBridge.translateGroup(player.getOpenInterfaceId()) == 243, "Greeting must use three body lines");
            check(trace.toString("UTF-8").contains("text='Raise the mainsail and hoist the anchor...' Eh! Oh, the | "
                    + "names 'Shanty'; 'Shanty Claws' to a landlubber like ye. | "
                    + "And oi've some terrible news for yer deck-swabbin' ears."), "Greeting body lines changed");
            walkAway(player, 99);
            player.setGender(1);
            player.getEquipmentManager().getContainer().setItem(12, new ItemStack(4202, 1));
            player.getQuestManager().handleFirstNpcAction(828);
            continueDialogue(player);
            check(trace.toString("UTF-8").contains("fellow werewolf!"), "Ring of Charos greeting missing");
            check(player.questHookStates[5] == 0, "Charos greeting started rescue early");
            advanceToMenu(player, 80);
            selectOption(player, 80, 2);
            advanceToMenu(player, 80);
            check(trace.toString("UTF-8").contains("lass"), "Female address was not substituted");
            selectOption(player, 80, 4);
            advanceToMenu(player, 80);
            check(trace.toString("UTF-8").contains("Certainly, Qa_Christmas_3."), "Player name was not substituted");
            } finally {
            ServerSettings.debugModeEnabled = debug;
            System.setOut(original);
        }
    }
    private static void checkChildPlaying() throws Exception {
        TickTask play = null;
        for (Object scheduled : World.getTaskScheduler().getTasks()) {
            TickTask task = (TickTask) scheduled;
            if (task.getClass().getSimpleName().equals("RescuedChildPlayTask")) {
                check(play == null, "Initialization duplicated the child play controller");
                play = task;
            }
        }
        check(play != null, "Rescued children have no play controller");
        // Verify the native child rig, celebration, and reindeer sequences, not just file existence.
        try (Js5CacheStore cache = new Js5CacheStore(new File("cache"))) {
            for (int animation : new int[]{3854, 189, 195, 5059, 5060}) {
                ConfigReader sequence = new ConfigReader(Definitions.readGroup(12).get(animation));
                int opcode;
                while ((opcode = sequence.readUnsignedByte()) != 1) {
                    if (opcode == 2 || opcode == 6 || opcode == 7) sequence.skip(2);
                    else if (opcode == 3) sequence.skip(sequence.readUnsignedByte());
                    else if (opcode == 4) { /* Boolean flag. */ }
                    else if (opcode >= 5 && opcode <= 11) sequence.skip(1);
                    else throw new AssertionError("Unexpected child emote sequence opcode " + opcode);
                }
                int count = sequence.readUnsignedByte(), duration = 0;
                for (int frame = 0; frame < count; frame++) duration += sequence.readUnsignedShort();
                check(duration == (animation == 3854 ? 186 : animation == 189 ? 40 : animation == 195 ? 42 : 220),
                        "Emote pause/replay lock no longer matches the native duration");
                int[] frames = new int[count];
                for (int frame = 0; frame < count; frame++) frames[frame] = sequence.readUnsignedShort();
                for (int frame = 0; frame < count; frame++) frames[frame] |= sequence.readUnsignedShort() << 16;
                for (int frame : frames) {
                    byte[] bytes = cache.readFiles(0, frame >>> 16).get(frame & 65535);
                    check(bytes != null, "Emote frame missing from 2006 cache");
                    if (animation == 3854 || animation == 189 || animation == 195) {
                        int skeleton = (bytes[0] & 255) << 8 | bytes[1] & 255;
                        check(skeleton == 319,
                                "Child animation uses the adult skeleton and will stretch the model");
                    }
                }
            }
        }
        boolean[] celebrated = new boolean[3];
        int[] firstCelebration = {-1, -1, -1};
        for (int tick = 0; tick < 40; tick++) {
            play.execute();
            for (Npc npc : World.getNpcs()) {
                if (npc == null) continue;
                int child = npc.getOriginalNpcId() - 5046;
                if (child < 0 || child > 2) continue;
                int animation = npc.getUpdateState().getAnimationId();
                check(animation != 866 && animation != 862 && animation != 197
                                && animation != 5018 && animation != 2842,
                        "Incorrect child motion was reintroduced");
                if (animation == 3854) {
                    celebrated[child] = true;
                    if (firstCelebration[child] < 0) firstCelebration[child] = tick;
                    check(npc.getMovementMode() == NpcMovementMode.STATIONARY
                                    && npc.getMovementQueue().getSteps().size() <= 1,
                            "Rescued child kept walking during its emote");
                }
                if (tick == 39)
                    check(npc.getMovementMode() == NpcMovementMode.ROAMING && animation == -1,
                            "Child failed to resume wandering after playing");
            }
        }
        for (int child = 0; child < 3; child++)
            check(celebrated[child] && firstCelebration[child] == child * 5,
                    "Child did not celebrate independently: " + child);
    }

    private static void checkShanties() {
        String[] expected = {"'Sailing on the sea...happy as can be...'", "'...to exotic places...fair winds in our faces...'",
                "'...and we'll sink a drink to our sailor ways...'", "'...polish ye cutlass's makes 'em rust-less...'",
                "'...a brig's too good, so keelhaul him in the mornin'...'"};
        Npc shanty = null;
        for (Npc npc : World.getNpcs()) if (npc != null && npc.getNpcId() == 828) shanty = npc;
        check(shanty != null, "No Shanty for overhead dialogue");
        TickTask song = null;
        for (Object scheduled : World.getTaskScheduler().getTasks()) {
            TickTask task = (TickTask)scheduled;
            if (task.getClass().getEnclosingClass() == GublinchChristmasEvent.class
                    && task.getIntervalTicks() == 10) song = task;
        }
        check(song != null, "No shanty scheduler");
        for (int i = 0; i < 7; i++) {
            song.execute();
            check(expected[i % expected.length].equals(shanty.getUpdateState().getForcedText()), "Shanties out of order");
        }
    }
    private static void checkRewardBranches(Player player) {
        player.questHookStates[5] = 1024 | 1023;
        for (int i = 0; i < 28; i++) player.getInventoryManager().addItem(new ItemStack(590, 1));
        player.getQuestManager().handleFirstNpcAction(828);
        finishConversation(player);
        check(!GublinchChristmasEvent.isComplete(player) && !player.ownsItem(10507), "Full inventory awarded hat");
        player.getInventoryManager().getContainer().clear();
        ServerSettings.freeToPlayWorld = true;
        player.getQuestManager().handleFirstNpcAction(828);
        finishConversation(player);
        check(player.ownsItem(10507) && !player.ownsItem(10508) && GublinchChristmasEvent.isComplete(player), "Free world reward branch wrong");
        ServerSettings.freeToPlayWorld = false;
        player.getQuestManager().handleFirstNpcAction(828);
        finishConversation(player);
        check(player.ownsItem(10508), "Returning on members world did not award tree");
        player.getInventoryManager().removeItem(new ItemStack(10507, 1));
        player.getInventoryManager().removeItem(new ItemStack(10508, 1));
        player.getQuestManager().handleFirstNpcAction(828);
        advanceToMenu(player, 90);
        check(!player.ownsItem(10507) && !player.ownsItem(10508), "Shanty replaced rewards reserved for Diango");
        player.getQuestManager().handleFirstNpcAction(5046);
        finishConversation(player);
        check(player.getQuestManager().handleSecondObjectAction(19039,2843,3141), "Completed search not handled");
        finishConversation(player);
        Position before = player.getPosition().copy();
        player.getQuestManager().handleFirstObjectAction(19039,2843,3141);
        finishConversation(player);
        check(player.getPosition().equals(before), "Completed player reentered cave");
    }
    private static void continueDialogue(Player player,int widget) {
        ByteBuffer payload = ByteBuffer.allocate(6);
        payload.putShort((short)-1).putInt(widget).flip();
        new InterfaceActionPacketHandler().handle(player,
                new IncomingPacket(ClientPackets.WIDGET_SELECT,6,PacketBuffer.wrapReader(payload)));
    }
    private static void walkAway(Player player,int opcode) {
        int x = player.getPosition().getX(), y = player.getPosition().getY();
        ByteBuffer payload = ByteBuffer.allocate(opcode == 80 ? 19 : 5);
        payload.put((byte)(x+128)).put((byte)(x>>8)).put((byte)0).put((byte)y).put((byte)(y>>8));
        if (opcode == 80) payload.put(new byte[14]);
        payload.flip();
        new MovementPacketHandler().handle(player,new IncomingPacket(opcode,payload.remaining(),PacketBuffer.wrapReader(payload)));
    }
    private static int countNpcs() { int count = 0; for (Npc n : World.getNpcs()) if (n != null) count++; return count; }
    private static Npc gublinch() { for (Npc n : World.getNpcs()) if (n != null && n.getNpcId() == 5017) return n; throw new AssertionError("No gublinch spawned"); }
    private static Player player(int index) throws Exception {
        Player player = new Player(null);
        player.setUsername("qa_christmas_" + index);
        SocketChannel transport = SocketChannel.open(); transport.close();
        Field socket = Player.class.getDeclaredField("socketChannel"); socket.setAccessible(true); socket.set(player, transport);
        player.setOutboundCipher(new IsaacCipher(new int[4]));
        player.setIndex(index); player.setEncodedIndex(32768 + index);
        // Existing event/visual fixtures use guaranteed-hit Ranged; accuracy is tested separately.
        player.getSkillManager().getCurrentLevels()[4] = 70;
        player.setQuestState(0, 1); player.setPosition(new Position(2918, 3175, 0)); player.refreshLocalViewArea();
        return player;
    }

    private static void persistence(Player player) throws Exception {
        String username = "qa_gublinch_event";
        Path file = Paths.get("data/characters/" + username + ".dat");
        check(!Files.exists(file), "Test account already exists");
        try {
            player.setUsername(username); player.setPassword("test"); player.lastLoginHostAddress = "127.0.0.1";
            Field host = Player.class.getDeclaredField("hostAddress"); host.setAccessible(true); host.set(player, "127.0.0.1");
            Method writer = CharacterFileManager.class.getDeclaredMethod("writePlayerFile", Player.class); writer.setAccessible(true); writer.invoke(null, player);
            Player loaded = new Player(null); loaded.setUsername(username);
            CharacterFileManager.loadPlayerFromFile("./data/characters/", loaded);
            check(loaded.questHookStates[5] == player.questHookStates[5], "Saved cage/completion state lost");
            Method reader = CharacterFileManager.class.getDeclaredMethod("readCharacterFileRecord", String.class, String.class, boolean.class);
            reader.setAccessible(true);
            CharacterFileRecord record = (CharacterFileRecord)reader.invoke(null, "./data/characters/", username, true);
            check(record.questHookStates[5] == player.questHookStates[5], "Offline record lost event state");
            CharacterFileManager.saveCharacterFileRecord(record);
            CharacterFileManager.loadPlayerFromFile("./data/characters/", loaded);
            check(loaded.questHookStates[5] == player.questHookStates[5], "Offline rewrite lost event state");
        } finally { Files.deleteIfExists(file); }
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}

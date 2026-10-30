package com.rs2.model.quest.impl;

import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.Entity;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.clue.PuzzleBoxHandler;
import com.rs2.model.combat.AttackStyleDefinition;
import com.rs2.model.combat.CombatManager;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestScript;
import com.rs2.model.quest.impl.ApeAtollDungeonHazardDamageTask;
import com.rs2.model.quest.impl.ApeAtollGuardCaptureDialogueTask;
import com.rs2.model.quest.impl.ApeAtollGuardCaptureTask;
import com.rs2.model.quest.impl.ArdougneZooGnomeRescueTask;
import com.rs2.model.quest.impl.ArdougneZooMonkeyRecaptureTask;
import com.rs2.model.quest.impl.AwowogeiAllianceCutsceneStartTask;
import com.rs2.model.quest.impl.CaranockShipyardCutsceneStartTask;
import com.rs2.model.quest.impl.CaranockWaydarCutsceneStartTask;
import com.rs2.model.quest.impl.DaeroBlindfoldHangarReturnTask;
import com.rs2.model.quest.impl.DaeroBlindfoldHangarTravelTask;
import com.rs2.model.quest.impl.DaeroOrdersDecodeTask;
import com.rs2.model.quest.impl.DaeroPuzzleCompletionTeleportTask;
import com.rs2.model.quest.impl.DaeroTrainingTimeSkipTask;
import com.rs2.model.quest.impl.DaeroTrainingXpRewardTask;
import com.rs2.model.quest.impl.KrukAwowogeiEscortTask;
import com.rs2.model.quest.impl.LumdoApeAtollTravelTask;
import com.rs2.model.quest.impl.LumdoCrashIslandReturnTask;
import com.rs2.model.quest.impl.MonkeyAmuletMouldCrateSearchTask;
import com.rs2.model.quest.impl.MonkeyAmuletSmithingTask;
import com.rs2.model.quest.impl.MonkeyDenturesCrateSearchTask;
import com.rs2.model.quest.impl.MonkeyMadnessChapterFourTitleTask;
import com.rs2.model.quest.impl.MonkeyMadnessChapterOneTitleTask;
import com.rs2.model.quest.impl.MonkeyMadnessChapterThreeTitleTask;
import com.rs2.model.quest.impl.MonkeyMadnessChapterTwoTitleTask;
import com.rs2.model.quest.impl.NarnodeOrdersWritingTask;
import com.rs2.model.quest.impl.ShipyardCrateHoleSearchTask;
import com.rs2.model.quest.impl.ShipyardCrateTunnelDescentTask;
import com.rs2.model.quest.impl.ShipyardGateLockpickTask;
import com.rs2.model.quest.impl.WaydarCrashIslandReturnFlightTask;
import com.rs2.model.quest.impl.WaydarHangarReturnTask;
import com.rs2.model.quest.impl.WaydarInitialCrashIslandFlightTask;
import com.rs2.model.quest.impl.WaydarRepeatHangarReturnTask;
import com.rs2.model.quest.impl.ZooknockAmuletEnchantSpellTask;
import com.rs2.model.quest.impl.ZooknockFinalBattleExitTask;
import com.rs2.model.quest.impl.ZooknockGreegreeEnchantSpellTask;
import com.rs2.model.task.TickTask;
import com.rs2.util.GameUtil;
import com.rs2.util.RectangularArea;

public final class MonkeyMadnessQuest
extends QuestScript {
    private final RectangularArea apeAtollGuardPatrolCaptureArea = new RectangularArea(2766, 2793, 2773, 2802, 0);
    private final RectangularArea apeAtollImmediateCaptureArea = new RectangularArea(2762, 2767, 2767, 2772, 0);
    final RectangularArea apeAtollDungeonHazardArea = new RectangularArea(2688, 9087, 2818, 9150, 0);
    final RectangularArea apeAtollDungeonSafeArea = new RectangularArea(2800, 9138, 2812, 9150, 0);
    private final RectangularArea grandTreeKingArea = new RectangularArea(2461, 3491, 2470, 3500, 0);
    private final RectangularArea grandTreeDaeroArea = new RectangularArea(2477, 3483, 2486, 3492, 1);
    private final RectangularArea undergroundHangarArea = new RectangularArea(2572, 4490, 2663, 4535, 0);
    private final RectangularArea apeAtollLumdoArea = new RectangularArea(2883, 2692, 2940, 2744, 0);

    public MonkeyMadnessQuest(int value2) {
        super(62);
        super.setQuestPointReward(3);
    }

    @Override
    public final String[] buildQuestJournal(Player player, int value2) {
        if (value2 == 0) {
            if (player.getQuestState(46) == 1 && player.getQuestState(95) == 1) {
                return new String[]{"In The Grand Tree, I exposed the treachery of King", "Narnode Shareen's then High Tree Guardian, Glough.", "Glough has since been deposed. To start this quest I must", "check how King Shareen is faring.", "", "To complete this quest I need:", "-To be able to defeat a level 195 Jungle Demon"};
            }
            return new String[]{"To start this quest I need to:", String.valueOf(player.getQuestState(46) == 1 ? "@str@" : "") + "-Complete The Grand Tree quest", String.valueOf(player.getQuestState(95) == 1 ? "@str@" : "") + "-Complete The Tree Gnome Village quest", "", "To complete this quest I need:", "-To be able to defeat a level 195 Jungle Demon"};
        }
        if (value2 == 2) {
            if (player.ownsItem(4004)) {
                return new String[]{"I should go investigate the Karamja shipyard."};
            }
            return new String[]{"I should speak with King Narnode."};
        }
        if (value2 == 3) {
            return new String[]{"I should go investigate the Karamja shipyard."};
        }
        if (value2 == 4) {
            if (value2 == 4 && player.ownsItem(4005)) {
                return new String[]{"I should bring Kings orders to Daero who should", "be somewhere in the Grand Tree."};
            }
            return new String[]{"I should report back to King Narnode."};
        }
        if (value2 == 5 || value2 == 6) {
            return new String[]{"I should speak with Daero."};
        }
        if (value2 == 7) {
            return new String[]{"The code to power the hangar needs to be solved."};
        }
        if (value2 == 8) {
            return new String[]{"I should speak with Daero in the hangar."};
        }
        if (value2 == 9) {
            return new String[]{"I should speak with Waydar."};
        }
        if (value2 == 10) {
            return new String[]{"I should go and look for the 10th squad."};
        }
        if (value2 == 11) {
            return new String[]{"I should go and look for the 10th squad."};
        }
        if (value2 == 12) {
            return new String[]{"I should try to escape."};
        }
        if (!(value2 != 13 || this.isProgressFlagSet(player, 18) || this.isProgressFlagSet(player, 2) || this.isProgressFlagSet(player, 6) || this.isProgressFlagSet(player, 12))) {
            return new String[]{"I should find and speak with Zooknock."};
        }
        if (value2 == 13 && this.isProgressFlagSet(player, 2)) {
            return new String[]{"Zooknock told me for the amulet he needs:", String.valueOf(this.isProgressFlagSet(player, 3) ? "@str@" : "") + "Gold bar", String.valueOf(this.isProgressFlagSet(player, 5) ? "@str@" : "") + "Monkey amulet mould", String.valueOf(this.isProgressFlagSet(player, 4) ? "@str@" : "") + "Something to do monkey speech"};
        }
        if (value2 == 13 && this.isProgressFlagSet(player, 12)) {
            return new String[]{"Zooknock told me for the talisman he needs:", String.valueOf(this.isProgressFlagSet(player, 13) ? "@str@" : "") + "Some kind of monkey remains", String.valueOf(this.isProgressFlagSet(player, 14) ? "@str@" : "") + "Authentic magical monkey talisman"};
        }
        if (value2 == 13 && this.isProgressFlagSet(player, 18)) {
            return new String[]{"I should speak with Garkor."};
        }
        if (value2 == 14) {
            return new String[]{"I need to find a way to speak with Awowogei."};
        }
        if (value2 == 15) {
            return new String[]{"I should find and speak with Kruk."};
        }
        if (value2 == 16) {
            return new String[]{"I should speak with Awowogei."};
        }
        if (value2 == 17) {
            return new String[]{"I should bring a monkey from Ardougne Zoo to Awowogei."};
        }
        if (value2 == 18 || value2 == 19) {
            return new String[]{"I should speak with Garkor."};
        }
        if (value2 == 20) {
            return new String[]{"I should prepare myself for battle and then wear", "the squad sigil when ready."};
        }
        if (value2 == 21 || value2 == 22) {
            return new String[]{"I should speak with King Narnode."};
        }
        if (value2 == 1) {
            return new String[]{"Quest Completed!", "", "You were awarded:", "3 Quest Points", "10,000 coins", "3 diamonds", "", this.isProgressFlagSet(player, 17) ? "" : "I should speak with Daero for my training."};
        }
        return null;
    }

    private static boolean hasCompletedGrandTreeAndTreeGnomeVillage(Player player) {
        return player.getQuestState(46) == 1 && player.getQuestState(95) == 1;
    }

    @Override
    public final void awardCompletionRewards(Player player) {
        super.markQuestComplete(player);
        super.showQuestCompleteInterface(player);
        Player player2 = player;
        player2.packetSender.sendInterfaceText("3 Quest Points", 12150);
        player2 = player;
        player2.packetSender.sendInterfaceText("10,000 coins", 12151);
        player2 = player;
        player2.packetSender.sendInterfaceText("3 diamonds", 12152);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 12153);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 12154);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 12155);
        player.getInventoryManager().addOrDropItem(new ItemStack(995, 10000));
        player.getInventoryManager().addOrDropItem(new ItemStack(1601, 3));
        player2 = player;
        player2.packetSender.sendInterfaceModel(QuestScript.usesLegacyCompletionInterface() ? 6161 : 12145, 250, 4033);
        player2 = player;
        player2.packetSender.showInterface(QuestScript.usesLegacyCompletionInterface() ? 1689 : 12140);
        player2 = player;
        player.deferLevelUpInterfaces = false;
    }

    @Override
    public final boolean handleMovementStep(Player player, int value2) {
        // Everything below happens on Ape Atoll (guards at 2743-2802, 2767-2802; dungeon 2688-2818, 9087-9150).
        // This runs on every step of every player and bot, and findByDefinitionId searches all NPCs, so leave
        // straight away anywhere else.
        int x = player.getPosition().getX(), y = player.getPosition().getY();
        if (x < 2688 || x > 2818 || !(y >= 2688 && y <= 2815 || y >= 9087 && y <= 9150)) {
            return false;
        }
        value2 = Npc.scriptedStageCursor;
        if (value2 < 0) {
            return false;
        }
        Object npc = (Npc)Npc.scriptedStageNpcs.get(value2);
        if (this.apeAtollGuardPatrolCaptureArea.containsExclusive(player.getPosition()) && ((Npc)npc).scriptedPathStage < 5 && GameUtil.isNpcLastStepFacingPlayer(player, (Npc)npc)) {
            this.startApeAtollGuardCapture(player);
            return true;
        }
        if (this.apeAtollImmediateCaptureArea.containsExclusive(player.getPosition())) {
            this.startApeAtollGuardCapture(player);
            return true;
        }
        npc = Npc.findByDefinitionId(1454);
        if (GameUtil.isNpcLastStepFacingPlayer(player, (Npc)npc)) {
            this.startApeAtollGuardCapture(player);
            return true;
        }
        if (this.apeAtollDungeonHazardArea.containsExclusive(player.getPosition()) && !this.apeAtollDungeonSafeArea.containsExclusive(player.getPosition())) {
            if (player.activeEnvironmentalHazardId == 1425) {
                return true;
            }
            World.getTaskScheduler().schedule(new ApeAtollDungeonHazardDamageTask(this, 25, player));
            player.activeEnvironmentalHazardId = 1425;
            return true;
        }
        return false;
    }

    @Override
    public final boolean handleItemOnObject(Player objectId, int objectId2, int value2, int value32) {
        if (objectId2 == 4007 && (value2 == 4765 || value2 == 4766) && ((Player)objectId).getInventoryManager().containsItem(4020)) {
            ((Entity)objectId).getUpdateState().setAnimation(899);
            Player player = objectId;
            player.packetSender.sendSoundEffect(469, 1, 0);
            ((Player)objectId).setActionLocked(true);
            World.getTaskScheduler().schedule(new MonkeyAmuletSmithingTask(this, 4, (Player)objectId));
            return true;
        }
        return false;
    }

    @Override
    public final boolean handleFirstObjectAction(Player objectId, int objectId2, int value2, int value32, int value42) {
        if (objectId2 == 4868 || objectId2 == 4869 || objectId2 == 4870) {
            ((Player)objectId).getTeleportManager().startStandardTeleport(2466, 3488, 0, null);
            return true;
        }
        if (objectId2 == 4871 && value42 == 7) {
            PuzzleBoxHandler.openQuestPuzzleBox((Player)objectId);
            return true;
        }
        if (objectId2 == 4771 && value2 == 2802 && value32 == 2765) {
            if (((Player)objectId).getEquipmentManager().getItemIdAtSlot(2) == 4021) {
                DialogueManager.continueDialogue((Player)objectId, 1448, 1, 0);
            }
            return true;
        }
        if (objectId2 == 4787 && value2 == 2719 && value32 == 2766 || objectId2 == 4788 && value2 == 2721 && value32 == 2766) {
            if (((Player)objectId).getEquipmentManager().getItemIdAtSlot(3) == 4024) {
                DialogueManager.continueDialogue((Player)objectId, 1441, 100, 0);
            }
            return true;
        }
        if (objectId2 == 4775 && value2 == 2713 && value32 == 2766) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2712, 2766, 2));
            return true;
        }
        if (objectId2 == 4777 && value2 == 2713 && value32 == 2766) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2712, 2766, 0));
            return true;
        }
        if (objectId2 == 4776 && value2 == 2729 && value32 == 2766) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2730, 2766, 0));
            return true;
        }
        if (objectId2 == 4774 && value2 == 2729 && value32 == 2766) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2730, 2766, 2));
            return true;
        }
        if (objectId2 == 4749) {
            ((Player)objectId).getInventoryManager().addOrDropItem(new ItemStack(1963, 1));
            Player player = objectId;
            player.packetSender.sendGameMessage("You pick a banana.");
            return true;
        }
        if (objectId2 == 4772 && value2 == 2794 && value32 == 2773) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2793, 2773, 1));
            return true;
        }
        if (objectId2 == 4778 && value2 == 2794 && value32 == 2773) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2793, 2773, 0));
            return true;
        }
        if (objectId2 == 4778 && value2 == 2805 && value32 == 2785) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2806, 2785, 0));
            return true;
        }
        if (objectId2 == 4772 && value2 == 2805 && value32 == 2785) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2806, 2785, 1));
            return true;
        }
        if (objectId2 == 4879 && value2 == 2807 && value32 == 2785) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2807, 9201, 0));
            return true;
        }
        if (objectId2 == 4881 && value2 == 2808 && value32 == 9201) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2806, 2785, 0));
            return true;
        }
        if (objectId2 == 4743 && value2 == 2803 && value32 == 2734) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2803, 2733, 2));
            return true;
        }
        if (objectId2 == 4745 && value2 == 2803 && value32 == 2727) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2803, 2725, 0));
            return true;
        }
        if (objectId2 == 4780 && value2 == 2763 && value32 == 2703) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2764, 9103, 0));
            return true;
        }
        if (objectId2 == 4781 && value2 == 2763 && value32 == 9103) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2764, 2703, 0));
            return true;
        }
        if (objectId2 == 4710 && value2 == 2764 && value32 == 2764) {
            Player player = objectId;
            player.packetSender.openSingleDoor(4710, 2764, 2764, ((Entity)objectId).getPosition().getPlane());
            player = objectId;
            player.packetSender.queueRelativeMovementStep(0, ((Entity)objectId).getPosition().getY() < 2764 ? 1 : -1, true);
            return true;
        }
        if (objectId2 == 4800) {
            Player player = objectId;
            player.packetSender.sendGameMessage("Theres no time for that now.");
            return true;
        }
        if (objectId2 == 4728 && value2 == 2798 && value32 == 9169) {
            AttackStyleDefinition.startDelayedObjectMove((Player)objectId, new Position(2769, 2764, 0));
            return true;
        }
        if (objectId2 == 4799) {
            if (((Entity)objectId).getPosition().getY() < 2796 && (value42 == 1 || value42 >= 12)) {
                ((Player)objectId).setActionLocked(true);
                Player player = objectId;
                player.packetSender.sendGameMessage("You attempt to pick the lock.");
                boolean enabled = GameUtil.randomInt(3) == 0;
                World.getTaskScheduler().schedule(new ShipyardGateLockpickTask(this, 3, (Player)objectId, enabled));
            }
            return true;
        }
        return false;
    }

    @Override
    public final boolean handleContextDialogue(int value13, Player player, int value22, int value32, int value42, int value52, int value62, int value72) {
        if (value22 == 4715) {
            if (value32 == 1) {
                player.setActionLocked(true);
                Object value8 = player;
                ((Player)value8).packetSender.sendGameMessage("You search the crate.");
                World.getTaskScheduler().schedule(new MonkeyDenturesCrateSearchTask(this, 2, player));
                return true;
            }
            if (value32 == 2) {
                player.getDialogueManager().showTwoOptionsWithTitle("Do you wish to take one?", "Yes", "No");
                return true;
            }
            if (value32 == 3) {
                if (value42 == 1) {
                    player.getInventoryManager().addOrDropItem(new ItemStack(4006, 1));
                    Player player2 = player;
                    player2.packetSender.closeInterfaces();
                    return true;
                }
                Player player3 = player;
                player3.packetSender.closeInterfaces();
                return true;
            }
        }
        if (value22 == 4714) {
            if (value32 == 1) {
                player.setActionLocked(true);
                Object value9 = player;
                ((Player)value9).packetSender.sendGameMessage("You search the crate.");
                World.getTaskScheduler().schedule(new ShipyardCrateHoleSearchTask(this, 2, player));
                return true;
            }
            if (value32 == 2) {
                player.getDialogueManager().showTwoOptionsWithTitle("Would you like to go down?", "Yes, I'm sure.", "No, not yet.");
                return true;
            }
            if (value32 == 3) {
                if (value42 == 1) {
                    player.getDialogueManager().showOneLineStatement("You begin to lower yourself into the hole...");
                    return true;
                }
                Player player4 = player;
                player4.packetSender.closeInterfaces();
                return true;
            }
            if (value32 == 4) {
                player.setActionLocked(true);
                Object value10 = player;
                ((Player)value10).packetSender.showInterface(8677);
                World.getTaskScheduler().schedule(new ShipyardCrateTunnelDescentTask(this, 5, player));
                return true;
            }
        }
        if (value22 == 4724) {
            if (value32 == 1) {
                player.setActionLocked(true);
                Object value11 = player;
                ((Player)value11).packetSender.sendGameMessage("You search the crate.");
                World.getTaskScheduler().schedule(new MonkeyAmuletMouldCrateSearchTask(this, 2, player));
                return true;
            }
            if (value32 == 2) {
                player.getDialogueManager().showTwoOptionsWithTitle("Do you wish to take one?", "Yes", "No");
                return true;
            }
            if (value32 == 3) {
                if (value42 == 1) {
                    player.getInventoryManager().addOrDropItem(new ItemStack(4020, 1));
                    Player player5 = player;
                    player5.packetSender.closeInterfaces();
                    return true;
                }
                Player player6 = player;
                player6.packetSender.closeInterfaces();
                return true;
            }
        }
        if (value22 == 4871 && value72 == 7 && value32 == 100) {
            player.setActionLocked(true);
            Object value12 = player;
            ((Player)value12).packetSender.showInterface(8677);
            value12 = player;
            ((Player)value12).packetSender.sendGameMessage("As you slide the final piece into place you begin to hear a low rumbling");
            value12 = player;
            ((Player)value12).packetSender.sendGameMessage("sound...");
            player.setQuestState(this.getQuestId(), 8);
            World.getTaskScheduler().schedule(new DaeroPuzzleCompletionTeleportTask(this, 5, player));
            player.getDialogueManager().finishDialogue();
            return true;
        }
        return false;
    }

    @Override
    public final boolean handleCombatDeath(Entity entity, Entity entity2, int value2) {
        if (entity2.isNpc() && ((Npc)(entity2 = (Npc)entity2)).getNpcId() == 1472) {
            if (!entity.isPlayer()) {
                ((Npc)entity2).setCurrentHitpoints(((Npc)entity2).getMaxHitpoints() / 5);
                if (((Npc)entity2).getOwnerPlayer() != null) {
                    entity = ((Npc)entity2).getOwnerPlayer();
                    ((Player)entity).packetSender.sendGameMessage("The demon regenerates.");
                }
                return true;
            }
            entity = (Player)entity;
            if (value2 == 20) {
                ((Player)entity).setQuestState(this.getQuestId(), 21);
                entity2 = ((Player)entity).findTemporaryCutsceneNpc(1412);
                if (entity2 != null) {
                    ((Player)entity).packetSender.sendEntityHintIcon(1, entity2.getIndex());
                }
            }
        }
        return false;
    }

    private static void showCaranockShipyardCutsceneTitle(Player player) {
        Player player2 = player;
        player2.packetSender.sendInterfaceText("", 3026);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3027);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3028);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3029);
        player2 = player;
        player2.packetSender.sendInterfaceText("@cya@Meanwhile, far away in Karamja...", 3030);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3031);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3032);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3033);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3034);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3035);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3036);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3037);
        player2 = player;
        player2.packetSender.showInterface(3023);
    }

    private static void showCaranockWaydarCutsceneTitle(Player player) {
        Player player2 = player;
        player2.packetSender.sendInterfaceText("", 3026);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3027);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3028);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3029);
        player2 = player;
        player2.packetSender.sendInterfaceText("@cya@Meanwhile, somewhere far below the Ape Atoll...", 3030);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3031);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3032);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3033);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3034);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3035);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3036);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3037);
        player2 = player;
        player2.packetSender.showInterface(3023);
    }

    private static void showAwowogeiAllianceCutsceneTitle(Player player) {
        Player player2 = player;
        player2.packetSender.sendInterfaceText("", 3026);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3027);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3028);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3029);
        player2 = player;
        player2.packetSender.sendInterfaceText("@cya@Somewhere far below the Ape Atoll...", 3030);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3031);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3032);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3033);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3034);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3035);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3036);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 3037);
        player2 = player;
        player2.packetSender.showInterface(3023);
    }

    private void startApeAtollGuardCapture(Player player2) {
        if (((Player)player2).getEquipmentManager().getItemIdAtSlot(3) == 4024) {
            return;
        }
        if (((Player)player2).actionSucceeded) {
            return;
        }
        ((Player)player2).actionSucceeded = true;
        Player player = player2;
        player.packetSender.showWalkableInterface(8677);
        ((Entity)player2).getUpdateState().setAnimation(2304);
        ((Player)player2).setActionLocked(true);
        World.getTaskScheduler().schedule(new ApeAtollGuardCaptureTask(this, 3, (Player)player2));
    }

    @Override
    public final int getQuestDamageOverride(Entity entity2, Entity entity, int value2) {
        if (entity.isPlayer() && ((Entity)entity2).isNpc()) {
            entity = (Player)entity;
            Npc npc = (Npc)entity2;
            if (npc.getNpcId() == 1456) {
                ++((Player)entity).pendingGameMode;
                if (((Player)entity).actionSucceeded) {
                    CombatManager.stopCombat((Entity)entity2);
                }
                if (((Player)entity).pendingGameMode >= 10 && !((Player)entity).actionSucceeded) {
                    ((Player)entity).actionSucceeded = true;
                    entity2 = entity;
                    ((Player)entity2).packetSender.showWalkableInterface(8677);
                    entity.getUpdateState().setAnimation(2304);
                    ((Player)entity).setActionLocked(true);
                    World.getTaskScheduler().schedule(new ApeAtollGuardCaptureDialogueTask(this, 10, (Player)entity, value2));
                    return 0;
                }
            }
        }
        return -1;
    }

    @Override
    public final boolean handleItemOnNpc(Player player, int npcId, int value2, int value32) {
        if (value32 == 13 && this.isProgressFlagSet(player, 2) && !this.isProgressFlagSet(player, 3) && value2 == 2357 && npcId == 1425) {
            DialogueManager.continueDialogue(player, 1425, 100, 0);
            return true;
        }
        if (value32 == 13 && this.isProgressFlagSet(player, 2) && !this.isProgressFlagSet(player, 4) && value2 == 4006 && npcId == 1425) {
            DialogueManager.continueDialogue(player, 1425, 103, 0);
            return true;
        }
        if (value32 == 13 && this.isProgressFlagSet(player, 2) && !this.isProgressFlagSet(player, 5) && value2 == 4020 && npcId == 1425) {
            DialogueManager.continueDialogue(player, 1425, 104, 0);
            return true;
        }
        if (value32 == 13 && this.isProgressFlagSet(player, 12) && !this.isProgressFlagSet(player, 13) && value2 == 3179 && npcId == 1425) {
            DialogueManager.continueDialogue(player, 1425, 111, 0);
            return true;
        }
        if (value32 == 13 && this.isProgressFlagSet(player, 12) && !this.isProgressFlagSet(player, 14) && value2 == 4023 && npcId == 1425) {
            DialogueManager.continueDialogue(player, 1425, 114, 0);
            return true;
        }
        return false;
    }

    public final boolean isProgressFlagSet(Player player, int value2) {
        if (player.questProgressFlags[this.getQuestId()] == 0) {
            return false;
        }
        return (player.questProgressFlags[this.getQuestId()] & GameUtil.bitFlag(value2)) != 0;
    }

    @Override
    public final boolean handleNpcDialogue(Player player, int npcId, int value2, int value32, int value42) {
        if (npcId == 670 && this.grandTreeKingArea.containsExclusiveOnPlane(player.getPosition())) {
            if (value42 == 0) {
                if (!MonkeyMadnessQuest.hasCompletedGrandTreeAndTreeGnomeVillage(player)) {
                    return false;
                }
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Adventurer! It is good to see you again.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("And you too, King. How fares the tree?", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcTwoLineDialogue("The tree? It is fine, as it has always been since we", "foiled Glough's plans.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Good. What ever did happen to Glough?", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Oh, I forced him to resign. I have now appointed a new", "Head Tree Guardian, Daero. He is learning quickly", "and serves me well.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showPlayerOneLineDialogue("King, you look worried. Is anything the matter?", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcOneLineDialogue("Nothing in particular ... Well actually, yes - there is.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What is it?", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Well, do you remember Glough's ship building facilities", "in Karamja?", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Yes, they were on the eastern coast. What of them?", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showNpcFourLineDialogue("After you defeated Glough's demon I sent an envoy of", "my Royal Guard, the 10th squad, to oversee the", "decommission of the shipyard. They were ordered to", "use force if necessary.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I see ... Were they successful?", 591);
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I ... I don't know. I have heard nothing from them. I", "do not even know if they reached the shipyard!", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showPlayerOneLineDialogue("It is a long way...", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().showNpcThreeLineDialogue("But I need to know what happened. These are elite", "soldiers - their disappearance cannot simply be ignored.", "I cannot wait any longer.", 591);
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().showNpcThreeLineDialogue("And so I ask you: would you visit Glough's old shipyard", "in Karamja and find out if the 10th squad ever", "managed to reach?", 591);
                    return true;
                }
                if (value2 == 17) {
                    player.getDialogueManager().showTwoOptionsWithTitle("Start Monkey Madness?", "Yes", "No");
                    return true;
                }
                if (value2 == 18) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Ok, I'll do it.", 591);
                        this.startQuest(player);
                        player.getDialogueManager().setNextDialogueStep(19);
                        return true;
                    }
                    player.getDialogueManager().finishDialogue();
                    return false;
                }
            }
            if (value42 == 2 && value2 == 1 && !player.ownsItem(4004)) {
                player.getInventoryManager().addOrDropItem(new ItemStack(4004, 1));
                player.getDialogueManager().showNpcOneLineDialogue("Don't lose it this time!", 591);
                player.getDialogueManager().finishDialogue();
                return true;
            }
            if (value42 == 2) {
                if (value2 == 19) {
                    player.getDialogueManager().showItemMessage("Narnode hands you a copy of the Royal Seal.", new ItemStack(4004, 1));
                    player.getInventoryManager().addOrDropItem(new ItemStack(4004, 1));
                    return true;
                }
                if (value2 == 20) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Thank you very much. You may need this Royal Seal", "to identify yourself as my envoy.", 591);
                    return true;
                }
                if (value2 == 21) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Please report to me as soon as you have any", "information.", 591);
                    return true;
                }
                if (value2 == 22) {
                    player.setActionLocked(true);
                    MonkeyMadnessChapterOneTitleTask monkeyMadnessChapterOneTitleTask = new MonkeyMadnessChapterOneTitleTask(this, 2, player);
                    World.getTaskScheduler().schedule(monkeyMadnessChapterOneTitleTask);
                    return false;
                }
            }
            if (value42 == 4) {
                if (value2 == 1 && !player.ownsItem(4005)) {
                    player.getDialogueManager().showNpcOneLineDialogue("Welcome back, adventurer.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Hello. I investigated the shipyard.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcOneLineDialogue("Thank you for doing this. What did you find?", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("I met a Gnome who goes by the name of Caranock. Do", "you recognise it?", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcTwoLineDialogue("The name sounds a little familiar, but it is nobody I", "know personally.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("He calls himself a Gnome Liaison Officer. He seemed a", "little ... odd", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Never mind that - did you find anything out about my", "10th squad?", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("Caranock suggested they were blown off course by", "extreme southerly winds.", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showNpcOneLineDialogue("Do you believe him?", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I don't have any other information right now.", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Very well. I will now prepare some orders. You must", "convey them to my new High Tree Guardian, Daero.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getPacketSender().sendGameMessage("Narnode begins to write strange glyphs on a scroll of paper.");
                    player.setActionLocked(true);
                    NarnodeOrdersWritingTask narnodeOrdersWritingTask = new NarnodeOrdersWritingTask(this, 4, player);
                    World.getTaskScheduler().schedule(narnodeOrdersWritingTask);
                    return false;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showItemMessage("Narnode hands you some handwritten orders.", new ItemStack(4005, 1));
                    player.getInventoryManager().addOrDropItem(new ItemStack(4005, 1));
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Where will I find Daero?", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().showNpcTwoLineDialogue("You will find him attending to business somewhere on", "the Grand Tree.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 21) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("King Narnode!", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Yes? How is the mission going ... it has been quite some", "time since I sent you on your way.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("It's over - it's finally over.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("What do you mean 'over'?", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I mean 'finished.'", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcOneLineDialogue("Yes, alright. Report on what happened.", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showPlayerThreeLineDialogue("With all due respect sir, if I told you, you would not", "believe me. I expect Sergeant Garkor will be sending", "you a full report soon enough.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcOneLineDialogue("And what of my 10th squad?", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showPlayerOneLineDialogue("They all live - we suffered no casualties.", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcOneLineDialogue("'We', " + player.getUsername() + "?", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("I, uh, I'm part of the 10th squad now. I even have the", "sigil.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showItemMessage("You show King Narnode your sigil.", new ItemStack(4035, 1));
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Well, now. It appears I cannot argue with that. Garkor", "obviously thinks highly of you, as do I.", 591);
                    player.getDialogueManager().setNextDialogueStep(1);
                    player.setQuestState(this.getQuestId(), 22);
                    return true;
                }
            }
            if (value42 == 22) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcThreeLineDialogue("No service such as what you have done for me goes", "unrewarded in my kingdom. I personally made a visit", "to the Royal Treasury to withdraw your reward.", 591);
                    return true;
                }
                if (value2 == 2) {
                    if (player.getInventoryManager().getContainer().getFreeSlots() < 4) {
                        player.getDialogueManager().showNpcTwoLineDialogue("You don't have enough space to carry what I want to", "give you!", 591);
                        player.getDialogueManager().finishDialogue();
                    } else {
                        player.getPacketSender().sendGameMessage("King Narnode hands you a huge stack of gold coins and several diamonds!");
                        this.awardCompletionRewards(player);
                    }
                    return true;
                }
            }
            if (value42 == 1 && !this.isProgressFlagSet(player, 17)) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("So you're officially a member of the 10th squad then?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I suppose so...", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcOneLineDialogue("Well then you had better sign up for training.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Training?", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Yes. All members of the Royal Guard must complete a", "mandatory training programme.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Where do I sign up for this?", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcThreeLineDialogue("The High Tree Guardian Daero is in charge of the", "training programme. You should know where to find", "him by now.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
        }
        if (npcId == 675 && value42 == 2) {
            if (value2 == 2) {
                player.getDialogueManager().showNpcOneLineDialogue("Hey you! What are you up to?", 591);
                return true;
            }
            if (value2 == 3) {
                player.getDialogueManager().showPlayerOneLineDialogue("I'm trying to open the gate!", 591);
                return true;
            }
            if (value2 == 4) {
                player.getDialogueManager().showNpcOneLineDialogue("I can see that! Why?", 591);
                return true;
            }
            if (value2 == 5) {
                player.getDialogueManager().showPlayerTwoLineDialogue("I am on a special mission for King Narnode of the", "Gnomes.", 591);
                return true;
            }
            if (value2 == 6) {
                player.getDialogueManager().showNpcTwoLineDialogue("Narnode? I don't believe you. He wouldn't send a", "human!", 591);
                return true;
            }
            if (value2 == 7) {
                player.getDialogueManager().showPlayerOneLineDialogue("Well, he did...", 591);
                return true;
            }
            if (value2 == 8) {
                player.getDialogueManager().showNpcOneLineDialogue("Tough.", 591);
                return true;
            }
            if (value2 == 9) {
                player.getDialogueManager().showPlayerOneLineDialogue("Look, I have the Gnome Royal Seal.", 591);
                return true;
            }
            if (value2 == 10) {
                player.getDialogueManager().showItemMessage("You show the shipyard worker the Royal Seal.", new ItemStack(4004, 1));
                return true;
            }
            if (value2 == 11) {
                player.getDialogueManager().showNpcOneLineDialogue("Wow. I haven't seen one of these since...", 591);
                return true;
            }
            if (value2 == 12) {
                player.getDialogueManager().showPlayerOneLineDialogue("Since when?", 591);
                return true;
            }
            if (value2 == 13) {
                player.getDialogueManager().showNpcOneLineDialogue("Anyway. Please step inside, Sir.", 591);
                player.setQuestState(this.getQuestId(), 3);
                player.getDialogueManager().finishDialogue();
                return true;
            }
        }
        if (npcId == 1427) {
            if (value42 == 3) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Hello!", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("Who are you? Did Glough send you?", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Glough? No. He has been forced to resign by the King.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("Forced to resign??", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("He was plotting to start a war between the Gnomes and", "humankind.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showPlayerFourLineDialogue("Anyway, I am here on a separate mission. I am", "investigating the mysterious disappearance of the 10th", "squad of King Narnode's Royal Guard. They were to", "carry out some work in the area.", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Royal Guard? I know nothing about them. Absolutely", "nothing.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showPlayerOneLineDialogue("You have no idea why they mysteriously disappeared?", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showNpcOneLineDialogue("None whatsoever. What were they here to do?", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showPlayerOneLineDialogue("They were to oversee the decommission of the shipyard.", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Decommission the shipyard ... I see. Well, we have had", "some seriously strong southerly winds of late. They", "may have been blown off course during flight.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I shall see personally to the decommission. You should", "report to the King immediately.", 591);
                    player.setQuestState(this.getQuestId(), 4);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 13) {
                if (value2 == 100) {
                    player.getDialogueManager().showNpcOneLineDialogue("It is good of you to meet me, Waydar.", 591);
                    return true;
                }
                if (value2 == 101) {
                    player.getDialogueManager().setDialogueNpcId(1409);
                    player.getDialogueManager().showNpcTwoLineDialogue("It is good to see you again, Caranock. It is a strange", "island these monkeys inhabit.", 591);
                    return true;
                }
                if (value2 == 102) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Well observed. How have you been keeping yourself", "occupied?", 591);
                    return true;
                }
                if (value2 == 103) {
                    player.getDialogueManager().setDialogueNpcId(1409);
                    player.getDialogueManager().showNpcTwoLineDialogue("I am now a Flight Commander. My duties include", "testing Glough's prototype military glider.", 591);
                    return true;
                }
                if (value2 == 104) {
                    player.getDialogueManager().showNpcTwoLineDialogue("My my. How things have changed somewhat since", "Glough's time... Now, what of the human?", 591);
                    return true;
                }
                if (value2 == 105) {
                    player.getDialogueManager().setDialogueNpcId(1409);
                    player.getDialogueManager().showNpcThreeLineDialogue("The human? Just somebody Narnode appears to have", "taken a fancy to. It is hard to tell why. I suspect the", "human was involved with Glough's fall from grace.", 591);
                    return true;
                }
                if (value2 == 106) {
                    player.getDialogueManager().showNpcThreeLineDialogue("You may be right. Never mind - there are greater", "matters afoot. With Glough gone, it falls to us to", "continue with his plans.", 591);
                    return true;
                }
                if (value2 == 107) {
                    player.getDialogueManager().showNpcOneLineDialogue("Also, the shipyard workers are becoming restless.", 591);
                    return true;
                }
                if (value2 == 108) {
                    player.getDialogueManager().setDialogueNpcId(1409);
                    player.getDialogueManager().showNpcOneLineDialogue("I see. What do you have in mind?", 591);
                    return true;
                }
                if (value2 == 109) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Money for me, Waydar, and promotion for you. As", "you know, the 10th squad of the Royal Guard are", "slightly worse for wear on this island.", 591);
                    return true;
                }
                if (value2 == 110) {
                    player.getDialogueManager().setDialogueNpcId(1409);
                    player.getDialogueManager().showNpcTwoLineDialogue("This I know. But I don't see how it leads to money or", "promotion.", 591);
                    return true;
                }
                if (value2 == 111) {
                    player.getDialogueManager().showNpcThreeLineDialogue("What if they were to die? An entire squad of the Royal", "Guard goes missing in the jungle of Karamja ... We", "could blame it on the humans.", 591);
                    return true;
                }
                if (value2 == 112) {
                    player.getDialogueManager().setDialogueNpcId(1409);
                    player.getDialogueManager().showNpcOneLineDialogue("Narnode would be furious!", 591);
                    return true;
                }
                if (value2 == 113) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Precisely. He might even order an invasion. At the", "very least he'll step up the defence. More orders for", "me, promotion for you.", 591);
                    return true;
                }
                if (value2 == 114) {
                    player.getDialogueManager().setDialogueNpcId(1409);
                    player.getDialogueManager().showNpcTwoLineDialogue("Very clever. It might also serve us well to remind", "Narnode of Bolren's situation.", 591);
                    return true;
                }
                if (value2 == 115) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Ah yes - all that trouble with the Khazard. Last I heard,", "Bolren had retrieved the orbs of protection. Apparently", "some human lent their assistance.", 591);
                    return true;
                }
                if (value2 == 116) {
                    player.getDialogueManager().setDialogueNpcId(1409);
                    player.getDialogueManager().showNpcThreeLineDialogue("Really? Typical meddling human behaviour.", "Nevertheless, it will stoke fires of worry. After all, the", "battle still continues.", 591);
                    return true;
                }
                if (value2 == 117) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I agree. Anyhow, we don't want your human", "wondering as to your whereabouts. When the time is", "right, don't hesitate to ... dispose of it.", 591);
                    return true;
                }
                if (value2 == 118) {
                    player.getDialogueManager().setDialogueNpcId(1409);
                    player.getDialogueManager().showNpcTwoLineDialogue("Understood. Military gliders are after all an untested", "form of transport...", 591);
                    return true;
                }
                if (value2 == 119) {
                    player.getPacketSender().showInterface(8677);
                    MonkeyMadnessChapterThreeTitleTask monkeyMadnessChapterThreeTitleTask = new MonkeyMadnessChapterThreeTitleTask(this, 5, player);
                    World.getTaskScheduler().schedule(monkeyMadnessChapterThreeTitleTask);
                    return true;
                }
            }
        }
        if (npcId == 1407 && this.grandTreeDaeroArea.containsExclusive(player.getPosition())) {
            if (value42 == 1 && !this.isProgressFlagSet(player, 17)) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Good day, High Tree Guardian.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("Hello there. I hear your mission is complete.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerThreeLineDialogue("News travels quickly on this tree. I expect you also", "know that I am to be enrolled in the Royal Guard", "training programme.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("Indeed I do.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerOneLineDialogue("How long does it take?", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcOneLineDialogue("For you, it should hardly take any time at all.", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Then let us begin.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Enthusiasm. I like that. We will first begin with a series", "of exercices designed to increase your strenght and", "stamina.", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showNpcTwoLineDialogue("We then will follow these up by improving your attack", "and defence techniques.", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Let us begin. You must choose what you want to focus", "on:", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showTwoOptions("Focus on increasing strength and stamina...", "Focus on improving attack and defence techniques...");
                    return true;
                }
                if (value2 == 12) {
                    player.getPacketSender().closeInterfaces();
                    player.setActionLocked(true);
                    player.getPacketSender().showWalkableInterface(8677);
                    npcId = this.getQuestId();
                    value2 = value32;
                    DaeroTrainingTimeSkipTask daeroTrainingTimeSkipTask = new DaeroTrainingTimeSkipTask(this, 5, player);
                    DaeroTrainingXpRewardTask daeroTrainingXpRewardTask = new DaeroTrainingXpRewardTask(this, 10, player, value2, npcId);
                    World.getTaskScheduler().schedule(daeroTrainingTimeSkipTask);
                    World.getTaskScheduler().schedule(daeroTrainingXpRewardTask);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 4) {
                if (value2 == 1 && player.getInventoryManager().containsItem(4005)) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Are you Daero?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("Indeed I am - and you are?", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("I am an adventurer. I am currently in the service of", "your King.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I see. You must be the individual who helped defeat my", "predecessor Glough. I hope you'll find me a more", "honest replacement.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("I have been asked to give you orders from King", "Narnode.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcOneLineDialogue("Well hand them over here then.", 591);
                    return true;
                }
                if (value2 == 7 && player.getInventoryManager().containsItem(4005)) {
                    player.getDialogueManager().showItemMessage("You hand King Narnode's orders to Daero.", new ItemStack(4005, 1));
                    player.getInventoryManager().removeItem(new ItemStack(4005, 1));
                    player.setQuestState(this.getQuestId(), 5);
                    player.getDialogueManager().setNextDialogueStep(1);
                    return true;
                }
            }
            if (value42 == 5) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcTwoLineDialogue("It is written in and old military code. Bear with me", "whilst I decode this.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getPacketSender().sendGameMessage("Daero starts decoding the orders.");
                    player.setActionLocked(true);
                    DaeroOrdersDecodeTask daeroOrdersDecodeTask = new DaeroOrdersDecodeTask(this, 4, player);
                    World.getTaskScheduler().schedule(daeroOrdersDecodeTask);
                    return false;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcOneLineDialogue("I hope you feel like a quest adventurer ...", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Why is that?", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcOneLineDialogue("... because you're going to get one.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Tell me what the orders say!", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcFourLineDialogue("Given your recent performance of uncovering and", "neutralising a threat at the very extremes of the", "Gnome hierarchy, the King has decreed that you are to", "undertake a reconnaisance mission.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Where to?", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showNpcFourLineDialogue("You are to be taken far to the south of Karamja,", "further than any Gnome has purposely travelled before.", "You are to investigate Caranock's claim that Garkor's", "squad were blown off course.", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcOneLineDialogue("You must really have impressed the King in the past.", 591);
                    return true;
                }
                if (value2 == 51) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("What lies to the south of Karamja?", 591);
                        player.getDialogueManager().setNextDialogueStep(14);
                        return true;
                    }
                    if (value32 == 2) {
                        player.getDialogueManager().showPlayerOneLineDialogue("How will I travel?", 591);
                        player.getDialogueManager().setNextDialogueStep(17);
                        return true;
                    }
                    if (value32 == 3) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Are you coming with me?", 591);
                        player.getDialogueManager().setNextDialogueStep(18);
                        return true;
                    }
                    if (value32 == 4) {
                        value2 = 11;
                        value32 = 0;
                    }
                }
                if (value2 == 61) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Why did the King send a squad of the Royal Guard?", 591);
                        player.getDialogueManager().setNextDialogueStep(19);
                        return true;
                    }
                    if (value32 == 2) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Who is Garkor?", 591);
                        player.getDialogueManager().setNextDialogueStep(31);
                        return true;
                    }
                    if (value32 == 3) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Why are the 10th squad so famous?", 591);
                        player.getDialogueManager().setNextDialogueStep(23);
                        return true;
                    }
                    if (value32 == 4) {
                        value2 = 11;
                        value32 = 0;
                    }
                }
                if (value2 == 71) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Who is Caranock?", 591);
                        player.getDialogueManager().setNextDialogueStep(26);
                        return true;
                    }
                    if (value32 == 2) {
                        player.getDialogueManager().showPlayerOneLineDialogue("What is a Gnome Liaison Officer?", 591);
                        player.getDialogueManager().setNextDialogueStep(27);
                        return true;
                    }
                    if (value32 == 3) {
                        player.getDialogueManager().showPlayerOneLineDialogue("I'm not so sure about Caranock...", 591);
                        player.getDialogueManager().setNextDialogueStep(28);
                        return true;
                    }
                    if (value32 == 4) {
                        value2 = 11;
                        value32 = 0;
                    }
                }
                if (value2 == 11) {
                    if (!this.isProgressFlagSet(player, 1) || !this.isProgressFlagSet(player, 2)) {
                        player.getDialogueManager().showThreeOptions("Talk about the journey...", "Talk about the 10th squad...", "Talk about Caranock...");
                    } else {
                        player.getDialogueManager().showFourOptions("Talk about the journey...", "Talk about the 10th squad...", "Talk about Caranock...", "Leave...");
                    }
                    player.getDialogueManager().setNextDialogueStep(12);
                    return true;
                }
                if (value2 == 12 && value32 == 1) {
                    value2 = 50;
                    value32 = 0;
                }
                if (value2 == 12 && value32 == 2) {
                    value2 = 60;
                    value32 = 0;
                }
                if (value2 == 12 && value32 == 3) {
                    value2 = 70;
                    value32 = 0;
                }
                if (value2 == 12 && value32 == 4) {
                    value2 = 80;
                    value32 = 0;
                }
                if (value2 == 50) {
                    player.getDialogueManager().showFourOptions("What lies to the south of Karamja?", "How will I travel?", "Are you coming with me?", "Return to previous menu");
                    player.getDialogueManager().setNextDialogueStep(51);
                    return true;
                }
                if (value2 == 60) {
                    player.getDialogueManager().showFourOptions("Why did the King send a squad of the Royal Guard?", "Who is Garkor?", "Why are the 10th squad so famous?", "Return to previous menu");
                    player.getDialogueManager().setNextDialogueStep(61);
                    return true;
                }
                if (value2 == 70) {
                    player.getDialogueManager().showFourOptions("Who is Caranock?", "What is a Gnome Liaison Officer?", "I'm not so sure about Caranock...", "Return to previous menu");
                    player.getDialogueManager().setNextDialogueStep(71);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showNpcTwoLineDialogue("We do not know. Initial reports spoke of a large atoll", "populated by monkeys.", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Monkeys? Like on Karamja?", 591);
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().showNpcOneLineDialogue("From what I have heard, not quite like those monkeys...", 591);
                    player.getDialogueManager().setNextDialogueStep(50);
                    return true;
                }
                if (value2 == 17) {
                    player.getDialogueManager().showNpcThreeLineDialogue("It is my responsibility to make arrangements for your", "mission. We will shortly visit a colleague of mine who", "will be accompanying you.", 591);
                    if (!this.isProgressFlagSet(player, 1)) {
                        int questId = this.getQuestId();
                        player.questProgressFlags[questId] = player.questProgressFlags[questId] + GameUtil.bitFlag(1);
                    }
                    player.getDialogueManager().setNextDialogueStep(50);
                    return true;
                }
                if (value2 == 18) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I am afraid not. I must remain here to safeguard the", "Grand Tree. I will assign a Gnome agent to travel with", "you.", 591);
                    player.getDialogueManager().setNextDialogueStep(50);
                    return true;
                }
                if (value2 == 19) {
                    player.getDialogueManager().showNpcFourLineDialogue("The Royal Guard is composed of particularly elite", "soldiers who have proven themselves in battle. They are", "duty bound to protect the Grand Tree, its King and", "his interests.", 591);
                    return true;
                }
                if (value2 == 20) {
                    player.getDialogueManager().showNpcTwoLineDialogue("In the face of danger, they can more than take care of", "themselves.", 591);
                    return true;
                }
                if (value2 == 21) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("So the King worries we have come across an equally", "formidable foe?", 591);
                    return true;
                }
                if (value2 == 22) {
                    player.getDialogueManager().showNpcOneLineDialogue("He worries about this, yes.", 591);
                    player.getDialogueManager().setNextDialogueStep(60);
                    if (!this.isProgressFlagSet(player, 2)) {
                        int questId2 = this.getQuestId();
                        player.questProgressFlags[questId2] = player.questProgressFlags[questId2] + GameUtil.bitFlag(2);
                    }
                    return true;
                }
                if (value2 == 23) {
                    player.getDialogueManager().showNpcFourLineDialogue("They are, as you humans might say, the best of the", "best. As well as Sergeant Garkor, they have in their", "company a High Mage, two sappers and battle hardened", "foot soldiers.", 591);
                    return true;
                }
                if (value2 == 24) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What is a sapper?", 591);
                    return true;
                }
                if (value2 == 25) {
                    player.getDialogueManager().showNpcTwoLineDialogue("You might consider the role as that of an engineer. Or", "perhaps of a munitions expert.", 591);
                    player.getDialogueManager().setNextDialogueStep(60);
                    return true;
                }
                if (value2 == 26) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I have never heard of him. According to the report you", "made to the King, he is the Gnome Liaison Officer at", "the eastern Karamja shipyard.", 591);
                    player.getDialogueManager().setNextDialogueStep(70);
                    return true;
                }
                if (value2 == 27) {
                    player.getDialogueManager().showNpcFourLineDialogue("It was a position Glough introduced. Gnome Liaison", "Officers are in general responsible for coordinating", "activities between Gnomes and other beings in our", "remote operations.", 591);
                    player.getDialogueManager().setNextDialogueStep(70);
                    return true;
                }
                if (value2 == 28) {
                    player.getDialogueManager().showNpcOneLineDialogue("In what way?", 591);
                    return true;
                }
                if (value2 == 29) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("I do not know. He just seemed a little suspicious. He", "was very keen to see me leave.", 591);
                    return true;
                }
                if (value2 == 30) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I do not know him; he is from before my time. Glough", "would presumably have hand picked him.", 591);
                    player.getDialogueManager().setNextDialogueStep(70);
                    return true;
                }
                if (value2 == 31) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Sergeant Garkor holds the command of the 10th squad.", "As a soldier, he is extremely able. If his men are in", "trouble, he will be tirelessly working to save them.", 591);
                    return true;
                }
                if (value2 == 32) {
                    player.getDialogueManager().showNpcOneLineDialogue("You should aim to make contact with him.", 591);
                    player.getDialogueManager().setNextDialogueStep(60);
                    return true;
                }
                if (value2 == 80) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Let us go then.", 591);
                    player.getDialogueManager().setNextDialogueStep(81);
                    return true;
                }
                if (value2 == 81) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I must introduce you to a colleague of mine who", "will be accompanying you on your mission.", 591);
                    return true;
                }
                if (value2 == 82) {
                    player.getDialogueManager().showTwoOptions("Who is it?", "I work better on my own...");
                    return true;
                }
                if (value2 == 83) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Who is it?", 591);
                        return true;
                    }
                    player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                    player.getDialogueManager().setNextDialogueStep(82);
                    return true;
                }
                if (value2 == 84) {
                    player.getDialogueManager().showNpcOneLineDialogue("His name is Flight Commander Waydar.", 591);
                    return true;
                }
                if (value2 == 85) {
                    player.getDialogueManager().showNpcTwoLineDialogue("We must go now and meet Waydar. For security", "reasons I must ask you to wear a blindfold.", 591);
                    return true;
                }
                if (value2 == 86) {
                    player.getDialogueManager().showItemMessage("You wear the blindfold Daero hands you.", new ItemStack(1025, 1));
                    return true;
                }
                if (value2 == 87) {
                    player.setActionLocked(true);
                    player.getPacketSender().showInterface(8677);
                    npcId = this.getQuestId();
                    DaeroBlindfoldHangarTravelTask daeroBlindfoldHangarTravelTask = new DaeroBlindfoldHangarTravelTask(this, 5, player, npcId);
                    World.getTaskScheduler().schedule(daeroBlindfoldHangarTravelTask);
                    return true;
                }
            }
            if (value42 == 1 || value42 >= 6) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Hello again, adventurer. Would you like to return to", "the hangar?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showTwoOptions("Yes", "No");
                    return true;
                }
                if (value2 == 3) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Yes please.", 591);
                        return true;
                    }
                    player.getDialogueManager().finishDialogue();
                    return false;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showItemMessage("You wear the blindfold Daero hands you.", new ItemStack(1025, 1));
                    return true;
                }
                if (value2 == 5) {
                    player.setActionLocked(true);
                    player.getPacketSender().showInterface(8677);
                    npcId = value42;
                    DaeroBlindfoldHangarReturnTask daeroBlindfoldHangarReturnTask = new DaeroBlindfoldHangarReturnTask(this, 5, player, npcId);
                    World.getTaskScheduler().schedule(daeroBlindfoldHangarReturnTask);
                    return true;
                }
            }
        }
        if (npcId == 1407 && this.undergroundHangarArea.containsExclusive(player.getPosition())) {
            if (value42 == 6) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Welcome, adventurer, to the Underground Military", "Glider Hangar.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Wow! Why would Gnomes need such a place?", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcThreeLineDialogue("We do not, if the truth be told. This hangar was part", "of Glough's contingency planning. Had the attacks by", "land and sea failed, he would have turned to air.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcFourLineDialogue("It is fortunate indeed that you managed to expose him.", "The military gliders in this hangar are a prototype for a", "much refined version of the standard variety we", "currently use.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcOneLineDialogue("Let me introduce you to Flight Commander Waydar.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Flight Commander Waydar, I would like you to meet", String.valueOf(player.getUsername()) + ".", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().setDialogueNpcId(1408);
                    player.getDialogueManager().showNpcOneLineDialogue("Greetings High Tree Guardian.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().setDialogueNpcId(1408);
                    player.getDialogueManager().showNpcOneLineDialogue("And greetings to you too, visitor.", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Not just any old visitor Waydar; this is the person who", "exposed Glough and defeated his demon.", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().setDialogueNpcId(1408);
                    player.getDialogueManager().showNpcOneLineDialogue("I see. Well, there are no more demons left here.", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showNpcOneLineDialogue("Quite. He is now on a secret mission for the King.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showNpcThreeLineDialogue("As you know, the 10th squad went missing during their", "mission to decommission the eastern shipyard of", "Karamja.", 591);
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showNpcTwoLineDialogue("We still do not know what happened, but evidence", "suggests they were blown far off course to the south.", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().setDialogueNpcId(1408);
                    player.getDialogueManager().showNpcTwoLineDialogue("Their standard gliders must have fallen prey to the", "tropical weather.", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().showNpcThreeLineDialogue("When reinitialisation has been completed, you are to fly", "to the south of Karamja with " + player.getUsername() + " and", "accompany him on the mission.", 591);
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().setDialogueNpcId(1408);
                    player.getDialogueManager().showNpcThreeLineDialogue("We are no closer to reinitialising sir - the code is too", "hard. It is likely the only person who could do it is", "Glough.", 591);
                    return true;
                }
                if (value2 == 17) {
                    player.getDialogueManager().showNpcThreeLineDialogue("That Gnome is never stepping foot in this hangar again.", "He always was and still is a menace. Do you", "understand me?", 591);
                    return true;
                }
                if (value2 == 18) {
                    player.getDialogueManager().setDialogueNpcId(1408);
                    player.getDialogueManager().showNpcOneLineDialogue("Yes sir.", 591);
                    return true;
                }
                if (value2 == 19) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Very well. Notify me when you have managed to", "reinitialise.", 591);
                    return true;
                }
                if (value2 == 20) {
                    player.getDialogueManager().showNpcTwoLineDialogue(String.valueOf(player.getUsername()) + ", you will have to wait till reinitialisation is", "complete.", 591);
                    player.setQuestState(this.getQuestId(), 7);
                    player.questProgressFlags[this.getQuestId()] = 0;
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 7) {
                if (value2 == 1) {
                    player.getDialogueManager().showFourOptions("Where is the hangar?", "How do I leave?", "How do I return here later?", "What is reinitialisation?");
                    return true;
                }
                if (value2 == 2) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Where is the hangar?", 591);
                        return true;
                    }
                    if (value32 == 2) {
                        player.getDialogueManager().showPlayerOneLineDialogue("How do I leave?", 591);
                        player.getDialogueManager().setNextDialogueStep(6);
                        return true;
                    }
                    if (value32 == 3) {
                        player.getDialogueManager().showPlayerOneLineDialogue("How do I return later?", 591);
                        player.getDialogueManager().setNextDialogueStep(7);
                        return true;
                    }
                    if (value32 == 4) {
                        player.getDialogueManager().showPlayerOneLineDialogue("What is reinitialisation?", 591);
                        player.getDialogueManager().setNextDialogueStep(4);
                        return true;
                    }
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcFourLineDialogue("We try to ensure the location of the hangar remains a", "secret. If you must know something, then know that it", "lies directly beneath the Gnomeball pitch. However, I", "cannot reveal the location of its entrance.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Glough left a code on the facility which periodically", "needs to be entered if we wish to maintain the supply of", "power.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Without going through this process of reinitialisation, we", "cannot power the mechanism which deploys the military", "gliders.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcTwoLineDialogue("There is a teleporter round the corner. You must use", "this.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Come and find me on the Grand Tree. I will probably", "be at the bar.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 8) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcFourLineDialogue("Well done, adventurer. You have managed to break", "Glough's code. Now the process of reinitialisation is", "complete, you can truly begin your journey into the", "unknown.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I've had some practice in the past.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcOneLineDialogue("You are clearly a man of many talents.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Flight Commander Waydar, now that reinitialisation is", "complete, I order you to fly to the south of Karamja", "with " + player.getUsername() + ".", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().setDialogueNpcId(1408);
                    player.getDialogueManager().showNpcOneLineDialogue("Yes sir.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcTwoLineDialogue("You are to safeguard him on this potentially dangerous", "mission.", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().setDialogueNpcId(1408);
                    player.getDialogueManager().showNpcOneLineDialogue("Understood.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcTwoLineDialogue(String.valueOf(player.getUsername()) + ", speak to Waydar when you are ready to", "leave.", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showNpcOneLineDialogue("And ... good luck.", 591);
                    player.setQuestState(this.getQuestId(), 9);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
        }
        if (npcId == 1408) {
            if (value42 == 6 && value2 == 1) {
                player.getDialogueManager().showNpcOneLineDialogue("Sorry, I am not authorised to talk to you.", 591);
                player.getDialogueManager().finishDialogue();
                return true;
            }
            if (value42 == 7) {
                if (value2 == 1) {
                    player.getDialogueManager().showFourOptions("What are military gliders?", "Why are the gliders folded against the wall?", "When will reinitialisation be completed?", "Where is the reinitialisation code?");
                    return true;
                }
                if (value2 == 2) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("What are military gliders?", 591);
                        return true;
                    }
                    if (value32 == 2) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Why are the gliders folded against the wall?", 591);
                        player.getDialogueManager().setNextDialogueStep(4);
                        return true;
                    }
                    if (value32 == 3) {
                        player.getDialogueManager().showPlayerOneLineDialogue("When will reinitialisation be completed?", 591);
                        player.getDialogueManager().setNextDialogueStep(5);
                        return true;
                    }
                    if (value32 == 4) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Where is the reinitialisation code?", 591);
                        player.getDialogueManager().setNextDialogueStep(7);
                        return true;
                    }
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcFourLineDialogue("As Daero said, they are a prototype for a new version", "of glider. They are reinforced yet are built out of", "lighter materials. They can fly much faster and for", "longer.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcFourLineDialogue("It is the most compact way to store them.", "Unfortunately, they will remain like that until they are", "powered. We can only fully power the hangar when", "reinitialisation has been completed.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I cannot be sure. It is a difficult code; it may happen", "the next instant - then again it may not.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcOneLineDialogue("You could even have a go at solving the code yourself.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcTwoLineDialogue("The code must be entered at the red control panel just", "by the wall over there.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 8 && value2 == 1) {
                player.getDialogueManager().showNpcTwoLineDialogue("Well done on breaking the code. You should speak to", "the High Tree Guardian.", 591);
                player.getDialogueManager().finishDialogue();
                return true;
            }
            if (value42 == 9) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcTwoLineDialogue("You should stock up well on food before our journey. I", "can carry only enough provisions for myself.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I'd be careful of the local fauna too - I've heard the", "bite is far worse than any noise they make.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcOneLineDialogue("Do you wish to fly right now?", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showTwoOptions("Yes", "No");
                    return true;
                }
                if (value2 == 5) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Yes, let's go.", 591);
                        return true;
                    }
                    player.getDialogueManager().finishDialogue();
                    return false;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcOneLineDialogue("As you wish.", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.setActionLocked(true);
                    player.getPacketSender().showInterface(8677);
                    npcId = this.getQuestId();
                    WaydarInitialCrashIslandFlightTask waydarInitialCrashIslandFlightTask = new WaydarInitialCrashIslandFlightTask(this, 5, player, npcId);
                    World.getTaskScheduler().schedule(waydarInitialCrashIslandFlightTask);
                    return true;
                }
            }
            if (value42 == 1 || value42 >= 10) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Shall we return to Crash Island?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showTwoOptions("Yes", "No");
                    return true;
                }
                if (value2 == 3) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Yes, let's go.", 591);
                        return true;
                    }
                    player.getDialogueManager().finishDialogue();
                    return false;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("As you wish.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.setActionLocked(true);
                    player.getPacketSender().showInterface(8677);
                    WaydarCrashIslandReturnFlightTask waydarCrashIslandReturnFlightTask = new WaydarCrashIslandReturnFlightTask(this, 5, player);
                    World.getTaskScheduler().schedule(waydarCrashIslandReturnFlightTask);
                    return true;
                }
            }
        }
        if (npcId == 1419 && this.apeAtollLumdoArea.containsExclusive(player.getPosition())) {
            if (value42 == 10) {
                if (value2 == 1 && !this.isProgressFlagSet(player, 1)) {
                    player.getDialogueManager().showNpcOneLineDialogue("Who are you two?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("We are on a mission for King Narnode Shareen. I am", String.valueOf(player.getUsername()) + " and this is Flight Commander Waydar.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcOneLineDialogue("What business do you have here?", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showPlayerThreeLineDialogue("We are to investigate the disappearance of the 10th", "squad of the Royal Guard. Am I right in suspecting", "that you, Lumdo, are a member of the 10th squad?", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I might be. Do you have any way to prove that you", "are who you say you are?", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I have the Gnome Royal Seal.", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showItemMessage("You show Lumbo the Royal Seal.", new ItemStack(4004, 1));
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcOneLineDialogue("I see. Sorry for my distrust.", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showPlayerOneLineDialogue("So are you Lumbo of the 10th squad?", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcOneLineDialogue("I am indeed -", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("Where are the rest of your squad? Where is your", "Sergeant?", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showNpcOneLineDialogue("Let me begin at the beginning, human.", 591);
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showNpcFourLineDialogue("We were on our way to decommission Glough's old ship", "building facilities in eastern Karamja, as you probably", "know. However, we obviously chose the wrong season to", "fly.", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showNpcThreeLineDialogue("We were one gnome to a glider, so each was extremely", "light. Like leaves in a wind, we were blown south before", "we even landed on that island.", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Did you crash straight here?", 591);
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Yes. The winds drove us into the treetops, which", "destroyed the canvas of our gliders. We dragged what", "remained of the gliders out onto this beach.", 591);
                    return true;
                }
                if (value2 == 17) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What did you do then?", 591);
                    return true;
                }
                if (value2 == 18) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Whilst we were falling, Sergeant Garkor noticed a large", "populated atoll to our west. You cannot see it from", "here, but it is within sailing distance.", 591);
                    return true;
                }
                if (value2 == 19) {
                    player.getDialogueManager().showNpcFourLineDialogue("We spent time gathering enough wood to fashion two", "boats. The Sergeant took the rest of the 10th squad in", "the larger of the boats to explore the island and to", "search for potential glider launch sites.", 591);
                    return true;
                }
                if (value2 == 20) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("Presumably you are to guard the gliders until they", "return?", 591);
                    return true;
                }
                if (value2 == 21) {
                    player.getDialogueManager().showNpcOneLineDialogue("Affirmative.", 591);
                    return true;
                }
                if (value2 == 22) {
                    player.getDialogueManager().showPlayerThreeLineDialogue("You must take us to the island. I have orders from the", "High Tree Guardian to make contact with your", "Sergeant.", 591);
                    return true;
                }
                if (value2 == 23) {
                    player.getDialogueManager().showNpcOneLineDialogue("And I have orders from the Sergeant to stay here.", 591);
                    return true;
                }
                if (value2 == 24) {
                    player.getDialogueManager().showPlayerOneLineDialogue("You will not take me?", 591);
                    return true;
                }
                if (value2 == 25) {
                    player.getDialogueManager().showNpcOneLineDialogue("I will not take orders from you.", 591);
                    if (!this.isProgressFlagSet(player, 1)) {
                        int questId3 = this.getQuestId();
                        player.questProgressFlags[questId3] = player.questProgressFlags[questId3] + GameUtil.bitFlag(1);
                    }
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 1 || value42 >= 11) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Can you take me back to Ape Atoll?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("As you wish.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.setActionLocked(true);
                    player.getPacketSender().showInterface(8677);
                    LumdoApeAtollTravelTask lumdoApeAtollTravelTask = new LumdoApeAtollTravelTask(this, 5, player);
                    World.getTaskScheduler().schedule(lumdoApeAtollTravelTask);
                    return true;
                }
            }
        }
        if (npcId == 1419 && !this.apeAtollLumdoArea.containsExclusive(player.getPosition())) {
            if (value2 == 1) {
                player.getDialogueManager().showPlayerOneLineDialogue("Can you take me back to Crash Island?", 591);
                return true;
            }
            if (value2 == 2) {
                player.getDialogueManager().showNpcOneLineDialogue("As you wish.", 591);
                return true;
            }
            if (value2 == 3) {
                player.setActionLocked(true);
                player.getPacketSender().showInterface(8677);
                LumdoCrashIslandReturnTask lumdoCrashIslandReturnTask = new LumdoCrashIslandReturnTask(this, 5, player);
                World.getTaskScheduler().schedule(lumdoCrashIslandReturnTask);
                return true;
            }
        }
        if (npcId == 1409) {
            if (value42 == 10) {
                if (value2 == 1 && !this.isProgressFlagSet(player, 2)) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Where are we?", 591);
                    return true;
                }
                if (value2 == 1 && this.isProgressFlagSet(player, 2)) {
                    if (!this.isProgressFlagSet(player, 1)) {
                        player.getDialogueManager().showThreeOptions("What shall we do now?", "Do you recognise the gnome on the beach?", "Can you take me back to your kingdom?");
                    } else {
                        player.getDialogueManager().showFourOptions("What shall we do now?", "Do you recognise the gnome on the beach?", "Can you take me back to your kingdom?", "I cannot convince Lumdo to take us to the island...");
                    }
                    player.getDialogueManager().setNextDialogueStep(5);
                    return true;
                }
                if (value2 == 5) {
                    if (value32 == 3) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Can you take me back to your kingdom?", 591);
                        player.getDialogueManager().setNextDialogueStep(24);
                        return true;
                    }
                    if (value32 == 4) {
                        player.getDialogueManager().showPlayerOneLineDialogue("I cannot convince Lumdo to take us to the island...", 591);
                        player.getDialogueManager().setNextDialogueStep(6);
                        return true;
                    }
                    player.getDialogueManager().finishDialogue();
                    return false;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcFourLineDialogue("I am not sure. We appear to have landed where the", "10th squad crashed. The number of gnome gliders is", "correct. Unfortunately for them, it appears that none of", "their gliders survived the collision.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Did our glider survive?", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("Of course.", 591);
                    if (!this.isProgressFlagSet(player, 2)) {
                        int questId4 = this.getQuestId();
                        player.questProgressFlags[questId4] = player.questProgressFlags[questId4] + GameUtil.bitFlag(2);
                    }
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcOneLineDialogue("What is the problem?", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("He claims to be under direct orders from Garkor to", "guard their gliders until the rest return.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcThreeLineDialogue("His zeal in this matter is to be expected. The Royal", "Guard - in particular the 10th squad - are renowned", "for their fierce loyalty.", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Can you do anything?", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I would rather not get involved. My mission is to", "protect you.", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showPlayerOneLineDialogue("You must do something!", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showNpcOneLineDialogue("You are becoming tiresome, human. As you wish.", 591);
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showNpcOneLineDialogue("Foot soldier Lumdo of the 10th squad.", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().setDialogueNpcId(1419);
                    player.getDialogueManager().showNpcOneLineDialogue("Yes?", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I am Flight Commander Waydar. I believe you are", "under direct orders from your Sergeant to guard these", "gliders?", 591);
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().setDialogueNpcId(1419);
                    player.getDialogueManager().showNpcOneLineDialogue("That is correct, Commander.", 591);
                    return true;
                }
                if (value2 == 17) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I need not remind you that I outrank Garkor. As of", "this instant, your orders are to convey the human to", "the atoll and remain there until he needs to return.", 591);
                    return true;
                }
                if (value2 == 18) {
                    player.getDialogueManager().setDialogueNpcId(1419);
                    player.getDialogueManager().showNpcOneLineDialogue("Garkor will not be pleased!", 591);
                    return true;
                }
                if (value2 == 19) {
                    player.getDialogueManager().showNpcOneLineDialogue("Then he can take up his issues with me personally.", 591);
                    return true;
                }
                if (value2 == 20) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Waydar! Will you not accompany me to the island?", 591);
                    return true;
                }
                if (value2 == 21) {
                    player.getDialogueManager().showNpcOneLineDialogue("No. After all, somebody has to look after the gliders.", 591);
                    return true;
                }
                if (value2 == 22) {
                    player.getDialogueManager().showPlayerOneLineDialogue("But it is your mission to protect me!", 591);
                    return true;
                }
                if (value2 == 23) {
                    player.getDialogueManager().showNpcOneLineDialogue("Enough. Return here when you are done.", 591);
                    player.setQuestState(this.getQuestId(), 11);
                    player.questProgressFlags[this.getQuestId()] = 0;
                    return true;
                }
                if (value2 == 24) {
                    player.getDialogueManager().showNpcOneLineDialogue("As you wish.", 591);
                    return true;
                }
                if (value2 == 25) {
                    player.setActionLocked(true);
                    player.getPacketSender().showInterface(8677);
                    WaydarHangarReturnTask waydarHangarReturnTask = new WaydarHangarReturnTask(this, 5, player);
                    World.getTaskScheduler().schedule(waydarHangarReturnTask);
                    return true;
                }
            }
            if (value42 >= 11 || value42 == 1) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Hello again, Adventurer.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("Do you want to fly back to the hangar?", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showTwoOptions("Yes", "No");
                    return true;
                }
                if (value2 == 4) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Yes", 591);
                        player.getDialogueManager().setNextDialogueStep(5);
                        return true;
                    }
                    player.getDialogueManager().finishDialogue();
                    return false;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcOneLineDialogue("As you wish.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.setActionLocked(true);
                    player.getPacketSender().showInterface(8677);
                    WaydarRepeatHangarReturnTask waydarRepeatHangarReturnTask = new WaydarRepeatHangarReturnTask(this, 5, player);
                    World.getTaskScheduler().schedule(waydarRepeatHangarReturnTask);
                    return true;
                }
                if (value2 == 24) {
                    player.setActionLocked(true);
                    MonkeyMadnessQuest.showCaranockShipyardCutsceneTitle(player);
                    player.getPacketSender().showWalkableInterface(8677);
                    CaranockShipyardCutsceneStartTask caranockShipyardCutsceneStartTask = new CaranockShipyardCutsceneStartTask(this, 5, player);
                    World.getTaskScheduler().schedule(caranockShipyardCutsceneStartTask);
                    return true;
                }
            }
        }
        if (npcId == 1470 && value42 == 11) {
            if (value2 == 100) {
                player.getDialogueManager().showNpcOneLineDialogue("The workers are getting restless, Caranock.", 591);
                return true;
            }
            if (value2 == 101) {
                player.getDialogueManager().setDialogueNpcId(1427);
                player.getDialogueManager().showNpcOneLineDialogue("I know...", 591);
                return true;
            }
            if (value2 == 102) {
                player.getDialogueManager().showNpcTwoLineDialogue("All this talk of Glough being replaced doesn't bode well", "for ... how shall I put this ... their morale.", 591);
                return true;
            }
            if (value2 == 103) {
                player.getDialogueManager().setDialogueNpcId(1427);
                player.getDialogueManager().showNpcOneLineDialogue("Look, I know.", 591);
                return true;
            }
            if (value2 == 104) {
                player.getDialogueManager().showNpcTwoLineDialogue("Those are all men with children to feed. Famished", "families. Worried wives. All of us rely on this shipyard.", 591);
                return true;
            }
            if (value2 == 105) {
                player.getDialogueManager().showNpcTwoLineDialogue("If something isn't done soon, there'll be revolt. And I", "won't be able to stop it.", 591);
                return true;
            }
            if (value2 == 106) {
                player.getDialogueManager().setDialogueNpcId(1427);
                player.getDialogueManager().showNpcOneLineDialogue("Stop worrying. I'm working on something -", 591);
                return true;
            }
            if (value2 == 107) {
                player.getDialogueManager().showNpcTwoLineDialogue("What something? You're always working on something.", "All we ever hear is bad news.", 591);
                return true;
            }
            if (value2 == 108) {
                player.getDialogueManager().showNpcThreeLineDialogue("First, Glough disappears. Then news of a missing squad", "of the Royal Guard in our area. And what about that", "human sent by the King?", 591);
                return true;
            }
            if (value2 == 109) {
                player.getDialogueManager().setDialogueNpcId(1427);
                player.getDialogueManager().showNpcFourLineDialogue("The human means nothing. If it becomes too much", "trouble, I will simply have it ... removed. In the", "meantime, let it continue to search for that blasted 10th", "squad.", 591);
                return true;
            }
            if (value2 == 110) {
                player.getDialogueManager().showNpcOneLineDialogue("I am still worried. What am I meant to tell the men?", 591);
                return true;
            }
            if (value2 == 111) {
                player.getDialogueManager().setDialogueNpcId(1427);
                player.getDialogueManager().showNpcTwoLineDialogue("Stop worrying. I'm working on something - Glough left", "a few of his agents in the Gnome airforce.", 591);
                return true;
            }
            if (value2 == 112) {
                player.getDialogueManager().setDialogueNpcId(1427);
                player.getDialogueManager().showNpcTwoLineDialogue("For now, tell your men to continue work on the", "battleships. Give me some time.", 591);
                return true;
            }
            if (value2 == 113) {
                player.getDialogueManager().showNpcTwoLineDialogue("I hope you're right, Caranock, for your sake. My sake.", "For all of our sakes...", 591);
                return true;
            }
            if (value2 == 114) {
                player.getPacketSender().showInterface(8677);
                MonkeyMadnessChapterTwoTitleTask monkeyMadnessChapterTwoTitleTask = new MonkeyMadnessChapterTwoTitleTask(this, 5, player);
                World.getTaskScheduler().schedule(monkeyMadnessChapterTwoTitleTask);
                return true;
            }
        }
        if (npcId == 1413) {
            if (value42 == 11) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Hello?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("Hello - Who are you?", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerThreeLineDialogue("I am in the service of King Narnode Shareen. I have", "been sent here to locate the missing 10th squad of the", "Royal Guard.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("Well we're certainly missing home, I'll give you that.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerOneLineDialogue("How long have you been here?", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Oh, not for too long ... The jail guards keep us", "entertained every now and then too.", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcOneLineDialogue("Isn't that right Bunkdo?", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().setDialogueNpcId(1415);
                    player.getDialogueManager().showNpcTwoLineDialogue("That's right Lumo. Remember the time when they got", "all confused when changing shifts?", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Oh yeah. Trefaji and Aberab - they're as dense as", "bricks. They do give mighty punches though.", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().setDialogueNpcId(1415);
                    player.getDialogueManager().showNpcOneLineDialogue("Indeed. My backside is still a little sore...", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showNpcOneLineDialogue("Your backside??", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().setDialogueNpcId(1415);
                    player.getDialogueManager().showNpcOneLineDialogue("I turned around at the wrong time...", 591);
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().setDialogueNpcId(1417);
                    player.getDialogueManager().showNpcOneLineDialogue("Serves you bloody right too.", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Now, now lads, watch your language. You'll have to", "excuse them, human, they're only soldiers!", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().setDialogueNpcId(1417);
                    player.getDialogueManager().showNpcOneLineDialogue("Excuse me -", 591);
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Oh that's quite alright...", 591);
                    return true;
                }
                if (value2 == 17) {
                    player.getDialogueManager().showNpcOneLineDialogue("So human, what can we do for you?", 591);
                    return true;
                }
                if (value2 == 18) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Well I suppose I ought to help you escape.", 591);
                    return true;
                }
                if (value2 == 19) {
                    player.getDialogueManager().setDialogueNpcId(1415);
                    player.getDialogueManager().showNpcOneLineDialogue("No it's alright laddy, we quite like it in here.", 591);
                    return true;
                }
                if (value2 == 20) {
                    player.getDialogueManager().showNpcOneLineDialogue("Do your best to ignore him...", 591);
                    return true;
                }
                if (value2 == 21) {
                    player.getDialogueManager().showNpcOneLineDialogue("To be fair human, you're not much help stuck in there.", 591);
                    return true;
                }
                if (value2 == 22) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Well how do I get out?", 591);
                    return true;
                }
                if (value2 == 23) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Well, you can try picking the lock on your door.", "Watch out for the guards though.", 591);
                    return true;
                }
                if (value2 == 24) {
                    player.getDialogueManager().showNpcTwoLineDialogue("We've been trying to pick ours but we haven't been", "able to do so yet.", 591);
                    player.setQuestState(this.getQuestId(), 12);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value2 == 100) {
                player.getDialogueManager().showNpcThreeLineDialogue("Look - a newcomer. I'd say let me be the first to", "welcome you to Ape Atoll, but I see you've already met", "the welcoming party...", 591);
                player.getDialogueManager().finishDialogue();
                return true;
            }
        }
        if (npcId == 1420 && (value42 == 12 || value42 == 13)) {
            if (this.isProgressFlagSet(player, 1) && value2 == 1) {
                value2 = 7;
            }
            if (value2 == 1) {
                player.getDialogueManager().showPlayerOneLineDialogue("Hello? Is anybody there?", 591);
                return true;
            }
            if (value2 == 2) {
                player.getDialogueManager().showNpcOneLineDialogue("That depends on who is asking...", 591);
                return true;
            }
            if (value2 == 3) {
                player.getDialogueManager().showPlayerThreeLineDialogue("I am in the service of King Narnode Shareen. I have", "been sent here to locate the missing 10th squad of the", "Royal Guard.", 591);
                return true;
            }
            if (value2 == 4) {
                player.getDialogueManager().showNpcOneLineDialogue("I see. As you can see, this is a dangerous place to be.", 591);
                return true;
            }
            if (value2 == 5) {
                player.getDialogueManager().showPlayerOneLineDialogue("It certainly is ... who are you?", 591);
                return true;
            }
            if (value2 == 6) {
                player.getDialogueManager().showNpcTwoLineDialogue("Karam, 10th squad, Royal Guard. High Assassin at", "your service.", 591);
                return true;
            }
            if (value2 == 7) {
                player.getDialogueManager().showFourOptions("What are you doing here?", "How do you remain unseen?", "How can you be everywhere?", "I'll be back later.");
                player.getDialogueManager().setNextDialogueStep(8);
                if (!this.isProgressFlagSet(player, 1)) {
                    int questId5 = this.getQuestId();
                    player.questProgressFlags[questId5] = player.questProgressFlags[questId5] + GameUtil.bitFlag(1);
                }
                return true;
            }
            if (value2 == 8) {
                if (value32 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What are you doing here?", 591);
                    player.getDialogueManager().setNextDialogueStep(9);
                    return true;
                }
                if (value32 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("How do you remain unseen?", 591);
                    player.getDialogueManager().setNextDialogueStep(10);
                    return true;
                }
                if (value32 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("How can you be everywhere?", 591);
                    player.getDialogueManager().setNextDialogueStep(13);
                    return true;
                }
                if (value32 == 4) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I'll be back later.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value2 == 9) {
                player.getDialogueManager().showNpcTwoLineDialogue("I am thinking of a way to free my friends. In the", "meantime, I am helping those who further my cause.", 591);
                player.getDialogueManager().setNextDialogueStep(7);
                return true;
            }
            if (value2 == 10) {
                player.getDialogueManager().showNpcTwoLineDialogue("I have been gifted by Zooknock, our squad mage. I", "have saved his life on a number of occasions.", 591);
                return true;
            }
            if (value2 == 11) {
                player.getDialogueManager().showPlayerOneLineDialogue("Is there any way I could be like that?", 591);
                return true;
            }
            if (value2 == 12) {
                player.getDialogueManager().showNpcOneLineDialogue("Your best chance is to hide in this grass, human.", 591);
                player.getDialogueManager().setNextDialogueStep(7);
                return true;
            }
            if (value2 == 13) {
                player.getDialogueManager().showNpcOneLineDialogue("I move quickly.", 591);
                player.getDialogueManager().setNextDialogueStep(7);
                return true;
            }
        }
        if (npcId == 1411) {
            if (value42 == 12) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Hello?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcTwoLineDialogue("A fine day you have chosen to visit this hellish island,", "human.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("Good day to you too Sergeant. I've been sent by your", "King Narnode to -", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Investigate the circumstances surrounding the", "mysterious disappearance of my squad. Yes, I know.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerOneLineDialogue("How did you know that?", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcThreeLineDialogue("The King and I are still in communication, albeit", "sporadic. I decided I need a human of your calibre to", "assist me. It is pleasing to see you are still alive.", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Why do you need a human?", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcThreeLineDialogue("There is more going on than meets your eye, human.", "Did you not find it strange that an entire squad be", "sent to decomission a shipyard?", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Well -", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcFourLineDialogue("Indeed. But there are more pressing matters at hand.", "Three of my squad have been captured and placed in", "the jail. They are watched over by somewhat", "overpowering guards.", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Before we can resume our original mission we must", "rescue them.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("I know about the guards - I had to sneak out between", "the change of shifts.", 591);
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Trust me; we too have considered this, but whilst it is", "possible for one, it is near impossible for three.", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showNpcThreeLineDialogue("We have considered many things. I have my squad", "mage and sappers working below us right now. My,", "assassin Karam, is operating in the village itself.", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().showNpcOneLineDialogue("I remain here so that I may overhear Awowogei's plans.", 591);
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Awowogei?", 591);
                    return true;
                }
                if (value2 == 17) {
                    player.getDialogueManager().showNpcFourLineDialogue("The self-proclaimed ruler of the island. I have been", "listening to him for some time now. I believe we will", "incur a minimum of casualties if we have an insider - a", "monkey working for us.", 591);
                    return true;
                }
                if (value2 == 18) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("Have you seen these monkeys? You could never", "convince them to work for you!", 591);
                    return true;
                }
                if (value2 == 19) {
                    player.getDialogueManager().showNpcOneLineDialogue("I wasn't suggesting convincing them, human, but you.", 591);
                    return true;
                }
                if (value2 == 20) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Don't be ridiculous! I'm a human - not a monkey!", 591);
                    return true;
                }
                if (value2 == 21) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Do not be so sceptical ... you humans are considered to", "be quite closely related to monkeys.", 591);
                    return true;
                }
                if (value2 == 22) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Yes, but -", 591);
                    return true;
                }
                if (value2 == 23) {
                    player.getDialogueManager().showNpcFourLineDialogue("Go and see my squad mage, Zooknock. Tell him I have", "asked you to be 'disguised' as a monkey so that you", "may infiltrate the village. As you will see, he is", "something of an expert on the subject.", 591);
                    return true;
                }
                if (value2 == 24) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I can't even communicate with the monkeys!", 591);
                    return true;
                }
                if (value2 == 25) {
                    player.getDialogueManager().showNpcOneLineDialogue("Just go and find my squad mage, human.", 591);
                    player.setQuestState(this.getQuestId(), 13);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 13) {
                if (player.getEquipmentManager().getItemIdAtSlot(3) != 4024) {
                    return false;
                }
                if (value2 == 1) {
                    player.getDialogueManager().showNpcTwoLineDialogue("My my, Zooknock has outdone himself this time. You", "do look very much like a monkey you know.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I know.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcTwoLineDialogue("And by happy coincidence you appear to be just the", "right sort of monkey.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I need you now to seek audience with Awowogei. Claim", "you are an envoy from the monkeys of Karamja and", "are seeking an alliance.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcOneLineDialogue("You must win his trust if we are to succeed.", 591);
                    player.setQuestState(this.getQuestId(), 14);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 >= 14 && value42 < 18) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I need you now to seek audience with Awowogei. Claim", "you are an envoy from the monkeys of Karamja and", "are seeking an alliance.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("You must win his trust if we are to succeed.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 18) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Well done on winning Awowogei's trust. I overheard", "everything from here.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("However, your efforts may be in vain...", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What do you mean?", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Progress in the caves has been slow. Whilst you were", "in Ardougne, Bunkwicket and Waymottin overheard a", "slightly disturbing conversation.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Who was speaking? What was said?", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcOneLineDialogue("Listen closely whilst I narrate the details...", 591);
                    player.setQuestState(this.getQuestId(), 19);
                    return true;
                }
            }
            if (value42 == 19) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What shall we do?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("Zooknock and I have come up with a plan.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What kind of a plan?", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I hope you were listening closely. The teleportation spell", "that was provided will teleport ALL of the 10th squad, no", "matter where we may be.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcTwoLineDialogue("In effect, the spell will break Lumo, Bunkdo and Carado", "out of the jail for us.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("But you will be teleported straight into whatever trap", "they have prepared!", 591);
                    player.getDialogueManager().setNextDialogueStep(8);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Indeed. This is where you come in. Do not forget that", "we are the 10th squad of the Royal Guard, and that we", "are more than capable of holding our own.", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showNpcTwoLineDialogue("With your assistance, we should be able to defeat", "whatever is thrown at us.", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showPlayerOneLineDialogue("But how will I join you?", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Simple. We fool the teleportation spell that you are in", "fact a member of our squad.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What?", 591);
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Zooknock knows Glough's grasp of magic well. He", "believes the spell is linked to the sigils that all of our", "squad carry.", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showNpcTwoLineDialogue("It is these sigils that identify us as a member of the", "squad.", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getInventoryManager().addOrDropItem(new ItemStack(4035, 1));
                    player.getDialogueManager().showItemMessage("Garkor hands you some kind of medallion.", new ItemStack(4035, 1));
                    player.setQuestState(this.getQuestId(), 20);
                    return true;
                }
                if (value2 == 7) {
                    player.setActionLocked(true);
                    MonkeyMadnessQuest.showAwowogeiAllianceCutsceneTitle(player);
                    player.getPacketSender().showWalkableInterface(8677);
                    AwowogeiAllianceCutsceneStartTask awowogeiAllianceCutsceneStartTask = new AwowogeiAllianceCutsceneStartTask(this, 5, player);
                    World.getTaskScheduler().schedule(awowogeiAllianceCutsceneStartTask);
                    return true;
                }
            }
            if (value42 == 20) {
                if (value2 == 1 && !player.ownsItem(4035)) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I lost the sigil.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getInventoryManager().addOrDropItem(new ItemStack(4035, 1));
                    player.getDialogueManager().showItemMessage("Garkor hands you some kind of medallion.", new ItemStack(4035, 1));
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcOneLineDialogue("Try to be more careful this time.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().showNpcOneLineDialogue("Welcome to the 10th squad, " + player.getUsername() + ".", 591);
                    return true;
                }
                if (value2 == 17) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What is it?", 591);
                    return true;
                }
                if (value2 == 18) {
                    player.getDialogueManager().showNpcThreeLineDialogue("It is a replica Waymottin has made of our squad sigils.", "If you wear that when the spell is cast, you will be", "summoned along with the rest of us.", 591);
                    return true;
                }
                if (value2 == 19) {
                    player.getDialogueManager().showNpcThreeLineDialogue("You should prepare. Collect your thoughts and", "belongings and then wear the sigil. Hurry, human, we", "do not wish to enter this fight without you.", 591);
                    return true;
                }
                if (value2 == 20) {
                    player.getDialogueManager().showPlayerOneLineDialogue("All I have to do is wear this sigil?", 591);
                    return true;
                }
                if (value2 == 21) {
                    player.getDialogueManager().showNpcOneLineDialogue("Yes - but do not do so until you are ready.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
        }
        if (npcId == 1412) {
            if (value42 == 20 && (player.getOwnedNpc() == null || player.getOwnedNpc().isDead() || player.getOwnedNpc().getNpcId() != 1472)) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Ready yourself, human: the final battle begins!", 591);
                    return true;
                }
                if (value2 == 2) {
                    Npc npc = new Npc(1472);
                    npc.questOwnerPlayer = player;
                    GameplayHelper.replaceOwnedRoamingNpcAtPosition(player, npc, 2702, 9176, player.getPosition().getPlane(), -1, true, true);
                    npc.getUpdateState().setGraphic(86, 25);
                    for (Object temporaryNpc : player.temporaryCutsceneNpcs) {
                        Npc npc2 = (Npc)temporaryNpc;
                        if (npc2 == null) continue;
                        CombatManager.startCombat(npc2, npc);
                    }
                    return false;
                }
            }
            if (value42 == 21 && !this.isProgressFlagSet(player, 16)) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Well done, human! That was a most impressive display", "of skill.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Thank you.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcThreeLineDialogue("You should report to King Narnode immediately. Tell", "him that the 10th squad still survives and has suffered", "no casualties.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Rest assured, I will do so.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerOneLineDialogue("How do I leave this place?", 591);
                    return true;
                }
                if (value2 == 6) {
                    Npc npc;
                    player.getDialogueManager().showNpcOneLineDialogue("Speak to Zooknock. He will arrange for you to leave.", 591);
                    if (!this.isProgressFlagSet(player, 16)) {
                        int questId6 = this.getQuestId();
                        player.questProgressFlags[questId6] = player.questProgressFlags[questId6] + GameUtil.bitFlag(16);
                    }
                    if ((npc = player.findTemporaryCutsceneNpc(1426)) != null) {
                        player.getPacketSender().sendEntityHintIcon(1, npc.getIndex());
                    }
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
        }
        if (npcId == 1426 && value42 == 21 && this.isProgressFlagSet(player, 16)) {
            if (value2 == 1) {
                player.getDialogueManager().showNpcOneLineDialogue("Well done, human. Bear with me now.", 591);
                return true;
            }
            if (value2 == 2) {
                player.setActionLocked(true);
                player.getPacketSender().showInterface(8677);
                ZooknockFinalBattleExitTask zooknockFinalBattleExitTask = new ZooknockFinalBattleExitTask(this, 5, player);
                World.getTaskScheduler().schedule(zooknockFinalBattleExitTask);
                return true;
            }
        }
        if (npcId == 1428) {
            if (value2 == 100) {
                player.getDialogueManager().showNpcOneLineDialogue("Good evening, Awowogei.", 591);
                return true;
            }
            if (value2 == 101) {
                player.getDialogueManager().setDialogueNpcId(1448);
                player.getDialogueManager().showNpcTwoLineDialogue("It is always dark here, Gnome. Why have you asked to", "see me in private?", 591);
                return true;
            }
            if (value2 == 102) {
                player.getDialogueManager().setDialogueNpcId(1409);
                player.getDialogueManager().showNpcOneLineDialogue("Caranock and I have a suggestion to make.", 591);
                return true;
            }
            if (value2 == 103) {
                player.getDialogueManager().setDialogueNpcId(1448);
                player.getDialogueManager().showNpcOneLineDialogue("Then be quick about it.", 591);
                return true;
            }
            if (value2 == 104) {
                player.getDialogueManager().setDialogueNpcId(1409);
                player.getDialogueManager().showNpcOneLineDialogue("The foot soldiers of the Royal Guard in your jail...", 591);
                return true;
            }
            if (value2 == 105) {
                player.getDialogueManager().showNpcTwoLineDialogue("Would it not be easier if they were somehow just to ...", "die?", 591);
                return true;
            }
            if (value2 == 106) {
                player.getDialogueManager().setDialogueNpcId(1448);
                player.getDialogueManager().showNpcTwoLineDialogue("Why would I want to do that? Your king would declare", "war on my island.", 591);
                return true;
            }
            if (value2 == 107) {
                player.getDialogueManager().showNpcTwoLineDialogue("I can assure you he will not. We will lay the blame at", "the humans' feet.", 591);
                return true;
            }
            if (value2 == 108) {
                player.getDialogueManager().showNpcTwoLineDialogue("Narnode will indeed declare war: not against you, but", "against humankind.", 591);
                return true;
            }
            if (value2 == 109) {
                player.getDialogueManager().setDialogueNpcId(1409);
                player.getDialogueManager().showNpcOneLineDialogue("You are of course welcome to your share of the profits.", 591);
                return true;
            }
            if (value2 == 110) {
                player.getDialogueManager().setDialogueNpcId(1448);
                player.getDialogueManager().showNpcTwoLineDialogue("Intriguing. I have recently secured an alliance with the", "northern monkeys, which may prove useful.", 591);
                return true;
            }
            if (value2 == 111) {
                player.getDialogueManager().setDialogueNpcId(1448);
                player.getDialogueManager().showNpcOneLineDialogue("What would you have me do?", 591);
                return true;
            }
            if (value2 == 112) {
                player.getDialogueManager().showNpcThreeLineDialogue("Kill the foot soldiers and the rest of the 10th squad. My", "superior has sent you a few tricks which may prove", "useful.", 591);
                return true;
            }
            if (value2 == 113) {
                player.getDialogueManager().setDialogueNpcId(1448);
                player.getDialogueManager().showNpcOneLineDialogue("Such as?", 591);
                return true;
            }
            if (value2 == 114) {
                player.getDialogueManager().showNpcTwoLineDialogue("High magic: the ability to summon the entire 10th squad", "to a single location. And -", 591);
                return true;
            }
            if (value2 == 115) {
                player.getDialogueManager().setDialogueNpcId(1448);
                player.getDialogueManager().showNpcOneLineDialogue("Even those who escaped?", 591);
                return true;
            }
            if (value2 == 116) {
                player.getDialogueManager().showNpcTwoLineDialogue("Yes. And of course, you will also receive access to one", "of his 'pets'.", 591);
                return true;
            }
            if (value2 == 117) {
                player.getDialogueManager().showNpcThreeLineDialogue("You must be careful with these, as you have only one", "use of each. Ensure you set your trap well - none", "must survive lest they spread the truth.", 591);
                return true;
            }
            if (value2 == 118) {
                player.getDialogueManager().setDialogueNpcId(1409);
                player.getDialogueManager().showNpcOneLineDialogue("What of my human?", 591);
                return true;
            }
            if (value2 == 119) {
                player.getDialogueManager().setDialogueNpcId(1448);
                player.getDialogueManager().showNpcOneLineDialogue("What human??", 591);
                return true;
            }
            if (value2 == 120) {
                player.getDialogueManager().showNpcThreeLineDialogue("Ignore him. My colleague's official mission was to look", "after a human in the area, but don't worry: it is", "probably dead already.", 591);
                return true;
            }
            if (value2 == 121) {
                player.getDialogueManager().setDialogueNpcId(1448);
                player.getDialogueManager().showNpcOneLineDialogue("I should hope so, for both of your sakes.", 591);
                return true;
            }
            if (value2 == 122) {
                player.getDialogueManager().setDialogueNpcId(1448);
                player.getDialogueManager().showNpcTwoLineDialogue("Very well. I shall let you know when I have dealt with", "the Royal Guard.", 591);
                return true;
            }
            if (value2 == 123) {
                player.getDialogueManager().showNpcOneLineDialogue("Good luck, Awowogei.", 591);
                return true;
            }
            if (value2 == 124) {
                player.getDialogueManager().setDialogueNpcId(1448);
                player.getDialogueManager().showNpcTwoLineDialogue("With access to one of Glough's 'pets', I don't think I'll", "need it...", 591);
                return true;
            }
            if (value2 == 125) {
                player.getPacketSender().showInterface(8677);
                MonkeyMadnessChapterFourTitleTask monkeyMadnessChapterFourTitleTask = new MonkeyMadnessChapterFourTitleTask(this, 5, player);
                World.getTaskScheduler().schedule(monkeyMadnessChapterFourTitleTask);
                return true;
            }
        }
        if (npcId == 1425 && (value42 >= 13 || value42 == 1)) {
            if (value2 == 1 && this.isProgressFlagSet(player, 3) && this.isProgressFlagSet(player, 4) && this.isProgressFlagSet(player, 5)) {
                value2 = 105;
            } else if (value2 == 1 && this.isProgressFlagSet(player, 13) && this.isProgressFlagSet(player, 14)) {
                value2 = 115;
            } else if (value2 == 1 && this.isProgressFlagSet(player, 2)) {
                value2 = 59;
            } else if (value2 == 1 && (this.isProgressFlagSet(player, 6) || this.isProgressFlagSet(player, 15))) {
                value2 = 56;
            } else if (value2 == 1 && this.isProgressFlagSet(player, 12)) {
                value2 = 56;
            }
            if (value2 == 1) {
                player.getDialogueManager().showPlayerOneLineDialogue("Hello?", 591);
                return true;
            }
            if (value2 == 2) {
                player.getDialogueManager().showNpcTwoLineDialogue("A human ... here? What business have you on Ape", "Atoll?", 591);
                return true;
            }
            if (value2 == 3) {
                player.getDialogueManager().showPlayerTwoLineDialogue("I am on a mission for King Narnode Shareen of the", "Gnomes.", 591);
                return true;
            }
            if (value2 == 4) {
                player.getDialogueManager().showPlayerThreeLineDialogue("I have been sent to investigate the whereabouts of the", "10th squad of his Royal Guard, which went missing", "during a recent mission to Karamja.", 591);
                return true;
            }
            if (value2 == 5) {
                player.getDialogueManager().showNpcOneLineDialogue("Well you've found us - what's left of us, that is.", 591);
                return true;
            }
            if (value2 == 6) {
                player.getDialogueManager().showNpcThreeLineDialogue("I am Zooknock, the 10th squad mage. These are", "Bunkwicket and Waymottin, two of our finest sappers.", "I assume you will want to know how we got here?", 591);
                return true;
            }
            if (value2 == 7) {
                player.getDialogueManager().showPlayerOneLineDialogue("Of course.", 591);
                return true;
            }
            if (value2 == 8) {
                player.getDialogueManager().showNpcTwoLineDialogue("Your story first, human. What possessed you to travel", "to this forsaken island?", 591);
                return true;
            }
            if (value2 == 9) {
                player.getDialogueManager().showPlayerThreeLineDialogue("I am currently in the service of your King. I was the", "human who exposed Glough's warfaring plans and", "defeated his demon.", 591);
                return true;
            }
            if (value2 == 10) {
                player.getDialogueManager().showPlayerThreeLineDialogue("As far as I understand, the 10th squad were sent to", "oversee the decomission of Glough's shipyard in east", "Karamja.", 591);
                return true;
            }
            if (value2 == 11) {
                player.getDialogueManager().showPlayerTwoLineDialogue("Rumour has it you were blown off course. The King", "worried as to your fate, and sent me to investigate.", 591);
                return true;
            }
            if (value2 == 12) {
                player.getDialogueManager().showNpcOneLineDialogue("You were sent alone?", 591);
                return true;
            }
            if (value2 == 13) {
                player.getDialogueManager().showPlayerThreeLineDialogue("No, I have been accompanied by Flight Commander", "Waydar. We flew south on a special type of glider and", "landed on a small island to our east.", 591);
                return true;
            }
            if (value2 == 14) {
                player.getDialogueManager().showNpcTwoLineDialogue("The so called Crash Island. We left there one of our", "number, Lumdo, to guard our gliders until our return.", 591);
                return true;
            }
            if (value2 == 15) {
                player.getDialogueManager().showPlayerOneLineDialogue("Yes, we have met. He ferried me across to the atoll.", 591);
                return true;
            }
            if (value2 == 16) {
                player.getDialogueManager().showNpcTwoLineDialogue("He did!? He was explicitly ordered to guard the gliders!", "How did this happen? Who is guarding the gliders now?", 591);
                return true;
            }
            if (value2 == 17) {
                player.getDialogueManager().showPlayerTwoLineDialogue("Waydar ordered him to leave his post. He is guarding", "the gliders himself.", 591);
                return true;
            }
            if (value2 == 18) {
                player.getDialogueManager().showNpcTwoLineDialogue("Flight Commander Waydar you said? For some reason", "that name is familiar...", 591);
                return true;
            }
            if (value2 == 19) {
                player.getDialogueManager().showPlayerOneLineDialogue("So why are you here?", 591);
                return true;
            }
            if (value2 == 20) {
                player.getDialogueManager().showNpcFourLineDialogue("The rumours are correct. We were indeed blown off", "course. Fortunately, we managed to find a small island", "to steer our gliders towards, else we surely would have", "drowned.", 591);
                return true;
            }
            if (value2 == 21) {
                player.getDialogueManager().showPlayerFourLineDialogue("Then you gathered enough wood to fashion two boats.", "Your Sergeant and the rest of the 10th squad took the", "larger boat to this island, leaving Lumdo to guard the", "gliders and the other boat...", 591);
                return true;
            }
            if (value2 == 22) {
                player.getDialogueManager().showNpcOneLineDialogue("Correct. I assume Lumdo told you this?", 591);
                return true;
            }
            if (value2 == 23) {
                player.getDialogueManager().showPlayerOneLineDialogue("Yes. What happened when you landed here?", 591);
                return true;
            }
            if (value2 == 24) {
                player.getDialogueManager().showNpcFourLineDialogue("We split up into several small groups to search the", "island for potential gnome glider launch sites. Whilst we", "knew the island to be inhabited, we did not expect its", "occupants to be quite so ... militant.", 591);
                return true;
            }
            if (value2 == 25) {
                player.getDialogueManager().showPlayerOneLineDialogue("...", 591);
                return true;
            }
            if (value2 == 26) {
                player.getDialogueManager().showNpcFourLineDialogue("Monkeys. Lots of monkeys. They are unlike any other", "type of monkey we've come across. A far cry from the", "usual wild variety, these were armed with high quality", "weaponry and uncanny tactical ability.", 591);
                return true;
            }
            if (value2 == 27) {
                player.getDialogueManager().showNpcTwoLineDialogue("We were overwhelmed in numbers. Some of us", "managed to escape, but the rest were taken captive.", 591);
                return true;
            }
            if (value2 == 28) {
                player.getDialogueManager().showPlayerOneLineDialogue("Who survived?", 591);
                return true;
            }
            if (value2 == 29) {
                player.getDialogueManager().showNpcThreeLineDialogue("Myself, the Sergeant, Bunkwicket and Waymottin here.", "Karam, our assassin, probably managed to escape - he", "usually does.", 591);
                return true;
            }
            if (value2 == 30) {
                player.getDialogueManager().showPlayerOneLineDialogue("And of the rest?", 591);
                return true;
            }
            if (value2 == 31) {
                player.getDialogueManager().showNpcFourLineDialogue("Lumo, Bunkdo and Carado were captured, as I said.", "We believe they are being held in the jail. We are", "working on a way to release them. I have sensed there", "lies a cavern to the north.", 591);
                return true;
            }
            if (value2 == 32) {
                player.getDialogueManager().showNpcTwoLineDialogue("We are attempting to tunnel to this northern cavern", "and then move upwards from there.", 591);
                return true;
            }
            if (value2 == 33) {
                player.getDialogueManager().showPlayerOneLineDialogue("Why don't you just go overground?", 591);
                return true;
            }
            if (value2 == 34) {
                player.getDialogueManager().showNpcTwoLineDialogue("We have considered this, but every entrance seems to", "be excessively guarded.", 591);
                return true;
            }
            if (value2 == 35) {
                player.getDialogueManager().showPlayerOneLineDialogue("I see.", 591);
                return true;
            }
            if (value2 == 36) {
                player.getDialogueManager().showPlayerFourLineDialogue("I have spoken to your Sergeant. He believes that the", "best way to rescue the rest of your squad with the", "minimum of casualties is to have an insider - a monkey", "working for us.", 591);
                return true;
            }
            if (value2 == 37) {
                player.getDialogueManager().showNpcOneLineDialogue("Aha. He wants me to turn you into a monkey.", 591);
                return true;
            }
            if (value2 == 38) {
                player.getDialogueManager().showPlayerOneLineDialogue("Actually, it was more along the lines of a disguise...", 591);
                return true;
            }
            if (value2 == 39) {
                player.getDialogueManager().showNpcTwoLineDialogue("I think you misunderstand, human. Do you know why", "you were sent here?", 591);
                return true;
            }
            if (value2 == 40) {
                player.getDialogueManager().showPlayerOneLineDialogue("King Narnode Shareen asked me to...", 591);
                return true;
            }
            if (value2 == 41) {
                player.getDialogueManager().showNpcThreeLineDialogue("Indeed. However, King Narnode Shareen is still in", "contact with Garkor! You were sent here because", "Garkor specifically asked Narnode for you!", 591);
                return true;
            }
            if (value2 == 42) {
                player.getDialogueManager().showPlayerOneLineDialogue("Why wasn't I told?", 591);
                return true;
            }
            if (value2 == 43) {
                player.getDialogueManager().showNpcTwoLineDialogue("Before you arrived on this island, that information", "would have endangered both yourself and the mission.", 591);
                return true;
            }
            if (value2 == 44) {
                player.getDialogueManager().showPlayerOneLineDialogue("But why a human? Why me?", 591);
                return true;
            }
            if (value2 == 45) {
                player.getDialogueManager().showNpcFourLineDialogue("Garkor had long decided that we need a monkey", "insider. I have the necessary magic to perform the", "shapeshifting spell, but we needed a human to", "transform.", 591);
                return true;
            }
            if (value2 == 46) {
                player.getDialogueManager().showPlayerOneLineDialogue("Why don't you just transform a gnome?", 591);
                return true;
            }
            if (value2 == 47) {
                player.getDialogueManager().showNpcFourLineDialogue("It has been tried in the past, but the results were far", "from ... satisfactory. Although we, like you, are related", "to the monkeys, the link is too weak for a succesful", "transformation. That is why we need you.", 591);
                return true;
            }
            if (value2 == 48) {
                player.getDialogueManager().showPlayerOneLineDialogue("Right. What do I have to do?", 591);
                return true;
            }
            if (value2 == 49) {
                player.getDialogueManager().showNpcThreeLineDialogue("There will be two aspects to your transformation. We", "must first arrange it so that you are able to", "understand and communicate with the monkeys.", 591);
                return true;
            }
            if (value2 == 50) {
                player.getDialogueManager().showNpcTwoLineDialogue("We must also transform your body so that you may", "pass amongst them unnoticed.", 591);
                return true;
            }
            if (value2 == 51) {
                player.getDialogueManager().showNpcThreeLineDialogue("So that the effects of my spells are not permanent, I", "will invest their power into magical items which you", "must find. You can then use them at your will.", 591);
                return true;
            }
            if (value2 == 52) {
                player.getDialogueManager().showPlayerOneLineDialogue("What kind of items?", 591);
                return true;
            }
            if (value2 == 53) {
                player.getDialogueManager().showNpcTwoLineDialogue("For the spells to take full effect, they will have to be in", "some way related to the monkeys.", 591);
                return true;
            }
            if (value2 == 54) {
                player.getDialogueManager().showNpcTwoLineDialogue("I suggest that I invest the ability to communicate with", "the monkeys in an authentic monkey amulet.", 591);
                return true;
            }
            if (value2 == 55) {
                player.getDialogueManager().showNpcTwoLineDialogue("Similarly, the transformation spell should be stored in a", "monkey talisman of some kind.", 591);
                return true;
            }
            if (value2 == 56) {
                String text = "What do we need for the monkey amulet?";
                String text2 = "What do we need for the monkey talisman?";
                if (this.isProgressFlagSet(player, 6)) {
                    text = "I've lost the monkey amulet...";
                }
                if (this.isProgressFlagSet(player, 15)) {
                    text2 = "I've lost the monkey talisman...";
                }
                player.getDialogueManager().showTwoOptions(text, text2);
                player.getDialogueManager().setNextDialogueStep(57);
                return true;
            }
            if (value2 == 57) {
                if (value32 == 1 && this.isProgressFlagSet(player, 6)) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I've lost the monkey amulet...", 591);
                    player.getDialogueManager().setNextDialogueStep(61);
                    return true;
                }
                if (value32 == 1 && !this.isProgressFlagSet(player, 6)) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What do we need for the monkey amulet?", 591);
                    player.getDialogueManager().setNextDialogueStep(58);
                    return true;
                }
                if (value32 == 2 && this.isProgressFlagSet(player, 15)) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I've lost the monkey talisman...", 591);
                    player.getDialogueManager().setNextDialogueStep(72);
                    return true;
                }
                if (value32 == 2 && !this.isProgressFlagSet(player, 15)) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What do we need for the monkey talisman?", 591);
                    player.getDialogueManager().setNextDialogueStep(65);
                    return true;
                }
            }
            if (value2 == 58) {
                player.getDialogueManager().showNpcTwoLineDialogue("We need a gold bar, a monkey amulet mould and", "something to do monkey speech.", 591);
                if (!this.isProgressFlagSet(player, 2)) {
                    int questId7 = this.getQuestId();
                    player.questProgressFlags[questId7] = player.questProgressFlags[questId7] + GameUtil.bitFlag(2);
                }
                return true;
            }
            if (value2 == 59) {
                player.getDialogueManager().showFourOptions("Where do I find a gold bar?", "Where do I find a monkey amulet mould?", "Where do I find something to do with monkey speech?", "I'll be back later.");
                player.getDialogueManager().setNextDialogueStep(60);
                return true;
            }
            if (value2 == 60) {
                if (value32 == 4) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I'll be back later.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                player.getDialogueManager().finishDialogue();
                return false;
            }
            if (value2 == 61) {
                player.getDialogueManager().showNpcOneLineDialogue("Are you sure?", 591);
                return true;
            }
            if (value2 == 62) {
                player.getDialogueManager().showTwoOptions("Yes", "No");
                return true;
            }
            if (value2 == 63) {
                if (value32 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Yes.", 591);
                    player.getDialogueManager().setNextDialogueStep(64);
                    return true;
                }
                player.getDialogueManager().finishDialogue();
                return false;
            }
            if (value2 == 64) {
                player.getDialogueManager().showNpcTwoLineDialogue("Careless human. No matter - bring me the same", "materials again and I will prepare for you another one.", 591);
                if (!this.isProgressFlagSet(player, 2)) {
                    int questId8 = this.getQuestId();
                    player.questProgressFlags[questId8] = player.questProgressFlags[questId8] + GameUtil.bitFlag(2);
                }
                player.getDialogueManager().finishDialogue();
                return true;
            }
            if (value2 == 65) {
                player.getDialogueManager().showNpcTwoLineDialogue("We need some kind of monkey remains as well as an", "authentic magical monkey talisman.", 591);
                if (!this.isProgressFlagSet(player, 12)) {
                    int questId9 = this.getQuestId();
                    player.questProgressFlags[questId9] = player.questProgressFlags[questId9] + GameUtil.bitFlag(12);
                }
                return true;
            }
            if (value2 == 66) {
                player.getDialogueManager().showThreeOptions("Where do I find monkey remains?", "Where do I find a magical monkey talisman?", "I'll be back later.");
                player.getDialogueManager().setNextDialogueStep(67);
                return true;
            }
            if (value2 == 67) {
                if (value32 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Where do I find monkey remains?", 591);
                    player.getDialogueManager().setNextDialogueStep(68);
                    return true;
                }
                if (value32 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Where do I find a magical monkey talisman?", 591);
                    player.getDialogueManager().setNextDialogueStep(71);
                    return true;
                }
                if (value32 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I'll be back later.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value2 == 68) {
                player.getDialogueManager().showNpcThreeLineDialogue("I'll leave that to your better judgement... However, bear", "in mind the type of remain might affect the type of", "monkey you become...", 591);
                return true;
            }
            if (value2 == 69) {
                player.getDialogueManager().showPlayerOneLineDialogue("What if I need to be another type of monkey?", 591);
                return true;
            }
            if (value2 == 70) {
                player.getDialogueManager().showNpcTwoLineDialogue("Then bring me different monkey remains and another", "talisman.", 591);
                player.getDialogueManager().setNextDialogueStep(66);
                return true;
            }
            if (value2 == 71) {
                player.getDialogueManager().showNpcTwoLineDialogue("There ought to be something in the village. I cannot be", "sure, as I have not spent much time there.", 591);
                player.getDialogueManager().setNextDialogueStep(66);
                return true;
            }
            if (value2 == 72) {
                player.getDialogueManager().showNpcTwoLineDialogue("Careless human. No matter - bring me the same", "materials again and I will prepare for you another one.", 591);
                player.getDialogueManager().setNextDialogueStep(65);
                return true;
            }
            if (value2 == 100) {
                player.getDialogueManager().showItemMessage("You hand Zooknock the gold bar.", new ItemStack(2357, 1));
                if (!this.isProgressFlagSet(player, 3)) {
                    int questId10 = this.getQuestId();
                    player.questProgressFlags[questId10] = player.questProgressFlags[questId10] + GameUtil.bitFlag(3);
                }
                player.getInventoryManager().removeItem(new ItemStack(2357, 1));
                return true;
            }
            if (value2 == 101) {
                player.getDialogueManager().showNpcOneLineDialogue("Well done, human.", 591);
                if (this.isProgressFlagSet(player, 3) && this.isProgressFlagSet(player, 4) && this.isProgressFlagSet(player, 5)) {
                    player.getDialogueManager().setNextDialogueStep(105);
                }
                return true;
            }
            if (value2 == 102) {
                String text3 = "We still need: ";
                String text4 = "";
                value32 = 0;
                value42 = 0;
                if (!this.isProgressFlagSet(player, 3)) {
                    text3 = String.valueOf(text3) + "a gold bar";
                    value32 = 1;
                }
                if (!this.isProgressFlagSet(player, 4)) {
                    if (value32 != 0) {
                        text3 = String.valueOf(text3) + ", ";
                        text4 = "something to do monkey speech";
                    } else {
                        text3 = String.valueOf(text3) + "something to do monkey speech";
                    }
                    value32 = 1;
                    value42 = 1;
                }
                if (!this.isProgressFlagSet(player, 5)) {
                    if (value32 != 0) {
                        text3 = String.valueOf(text3) + ", ";
                    }
                    if (value42 != 0) {
                        text4 = "monkey amulet mould";
                    } else {
                        text3 = String.valueOf(text3) + "monkey amulet mould";
                    }
                }
                if (text4.equals("")) {
                    player.getDialogueManager().showNpcOneLineDialogue(text3, 591);
                } else {
                    player.getDialogueManager().showNpcTwoLineDialogue(text3, text4, 591);
                }
                player.getDialogueManager().finishDialogue();
                return true;
            }
            if (value2 == 103) {
                player.getDialogueManager().showItemMessage("You hand Zooknock the magical monkey dentures.", new ItemStack(4006, 1));
                if (!this.isProgressFlagSet(player, 4)) {
                    int questId11 = this.getQuestId();
                    player.questProgressFlags[questId11] = player.questProgressFlags[questId11] + GameUtil.bitFlag(4);
                }
                player.getInventoryManager().removeItem(new ItemStack(4006, 1));
                player.getDialogueManager().setNextDialogueStep(101);
                return true;
            }
            if (value2 == 104) {
                player.getDialogueManager().showItemMessage("You hand Zooknock the monkey amulet mould.", new ItemStack(4020, 1));
                if (!this.isProgressFlagSet(player, 5)) {
                    int questId12 = this.getQuestId();
                    player.questProgressFlags[questId12] = player.questProgressFlags[questId12] + GameUtil.bitFlag(5);
                }
                player.getInventoryManager().removeItem(new ItemStack(4020, 1));
                player.getDialogueManager().setNextDialogueStep(101);
                return true;
            }
            if (value2 == 105) {
                player.getDialogueManager().showNpcThreeLineDialogue("Now listen closely, human. I will cast a spell to enchant", "this gold bar with the power contained in these monkey", "dentures.", 591);
                player.getDialogueManager().setNextDialogueStep(106);
                return true;
            }
            if (value2 == 106) {
                player.getDialogueManager().showNpcFourLineDialogue("You must then smith the gold using the monkey amulet", "mould. However, unless you do this in a place of", "religious significance to the monkeys, the spirits", "contained within will likely depart.", 591);
                return true;
            }
            if (value2 == 107) {
                player.getDialogueManager().showPlayerTwoLineDialogue("Where do I find a place of religious significance to", "monkeys?", 591);
                return true;
            }
            if (value2 == 108) {
                player.getDialogueManager().showNpcTwoLineDialogue("Somewhere in the village. It ought to be obvious. Now", "give me a moment.", 591);
                return true;
            }
            if (value2 == 109) {
                player.setActionLocked(true);
                ZooknockAmuletEnchantSpellTask zooknockAmuletEnchantSpellTask = new ZooknockAmuletEnchantSpellTask(this, 3, player);
                World.getTaskScheduler().schedule(zooknockAmuletEnchantSpellTask);
                return false;
            }
            if (value2 == 110) {
                Npc npc = Npc.findByDefinitionId(1425);
                npc.getUpdateState().setGraphic(160, 0);
                player.getDialogueManager().showTwoItemMessage("Zooknock hands you back the gold bar and the monkey", "amulet mould.", new ItemStack(4007, 1), new ItemStack(4020, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(4007, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(4020, 1));
                if (this.isProgressFlagSet(player, 2)) {
                    int questId13 = this.getQuestId();
                    player.questProgressFlags[questId13] = player.questProgressFlags[questId13] - GameUtil.bitFlag(2);
                }
                if (this.isProgressFlagSet(player, 3)) {
                    int questId14 = this.getQuestId();
                    player.questProgressFlags[questId14] = player.questProgressFlags[questId14] - GameUtil.bitFlag(3);
                }
                if (this.isProgressFlagSet(player, 4)) {
                    int questId15 = this.getQuestId();
                    player.questProgressFlags[questId15] = player.questProgressFlags[questId15] - GameUtil.bitFlag(4);
                }
                if (this.isProgressFlagSet(player, 5)) {
                    int questId16 = this.getQuestId();
                    player.questProgressFlags[questId16] = player.questProgressFlags[questId16] - GameUtil.bitFlag(5);
                }
                if (!this.isProgressFlagSet(player, 6)) {
                    int questId17 = this.getQuestId();
                    player.questProgressFlags[questId17] = player.questProgressFlags[questId17] + GameUtil.bitFlag(6);
                }
                player.getDialogueManager().finishDialogue();
                return true;
            }
            if (value2 == 111) {
                player.getDialogueManager().showItemMessage("You hand Zooknock the monkey remains.", new ItemStack(3179, 1));
                if (!this.isProgressFlagSet(player, 13)) {
                    int questId18 = this.getQuestId();
                    player.questProgressFlags[questId18] = player.questProgressFlags[questId18] + GameUtil.bitFlag(13);
                }
                player.getInventoryManager().removeItem(new ItemStack(3179, 1));
                player.getDialogueManager().setNextDialogueStep(112);
                return true;
            }
            if (value2 == 112) {
                player.getDialogueManager().showNpcOneLineDialogue("Excellent.", 591);
                if (this.isProgressFlagSet(player, 13) && this.isProgressFlagSet(player, 14)) {
                    player.getDialogueManager().setNextDialogueStep(115);
                }
                return true;
            }
            if (value2 == 113) {
                String text5 = "We still need ";
                if (!this.isProgressFlagSet(player, 13)) {
                    text5 = String.valueOf(text5) + "monkey remains.";
                } else if (!this.isProgressFlagSet(player, 14)) {
                    text5 = String.valueOf(text5) + "an authentic magical monkey talisman.";
                }
                player.getDialogueManager().showNpcOneLineDialogue(text5, 591);
                player.getDialogueManager().finishDialogue();
                return true;
            }
            if (value2 == 114) {
                player.getDialogueManager().showItemMessage("You hand Zooknock the monkey talisman.", new ItemStack(4023, 1));
                if (!this.isProgressFlagSet(player, 14)) {
                    int questId19 = this.getQuestId();
                    player.questProgressFlags[questId19] = player.questProgressFlags[questId19] + GameUtil.bitFlag(14);
                }
                player.getInventoryManager().removeItem(new ItemStack(4023, 1));
                player.getDialogueManager().setNextDialogueStep(112);
                return true;
            }
            if (value2 == 115) {
                player.getDialogueManager().showNpcThreeLineDialogue("Bear with me human: I must now cast an extremely", "powerful spell. It is not often we are succesful in", "investing shapeshifting powers within objects.", 591);
                player.getDialogueManager().setNextDialogueStep(116);
                return true;
            }
            if (value2 == 116) {
                player.setActionLocked(true);
                ZooknockGreegreeEnchantSpellTask zooknockGreegreeEnchantSpellTask = new ZooknockGreegreeEnchantSpellTask(this, 3, player);
                World.getTaskScheduler().schedule(zooknockGreegreeEnchantSpellTask);
                return false;
            }
            if (value2 == 117) {
                Npc npc = Npc.findByDefinitionId(1425);
                npc.getUpdateState().setGraphic(160, 0);
                player.getDialogueManager().showThreeLineItemMessage("", "Zooknock hands you back the talisman. It seems to glow", "slightly.", new ItemStack(4024, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(4024, 1));
                if (this.isProgressFlagSet(player, 12)) {
                    int questId20 = this.getQuestId();
                    player.questProgressFlags[questId20] = player.questProgressFlags[questId20] - GameUtil.bitFlag(12);
                }
                if (this.isProgressFlagSet(player, 13)) {
                    int questId21 = this.getQuestId();
                    player.questProgressFlags[questId21] = player.questProgressFlags[questId21] - GameUtil.bitFlag(13);
                }
                if (this.isProgressFlagSet(player, 14)) {
                    int questId22 = this.getQuestId();
                    player.questProgressFlags[questId22] = player.questProgressFlags[questId22] - GameUtil.bitFlag(14);
                }
                if (!this.isProgressFlagSet(player, 15)) {
                    int questId23 = this.getQuestId();
                    player.questProgressFlags[questId23] = player.questProgressFlags[questId23] + GameUtil.bitFlag(15);
                }
                return true;
            }
            if (value2 == 118) {
                player.getDialogueManager().showNpcThreeLineDialogue("I am afraid I have not been able to fully invest my", "powers in that talisman. You may use it, but it will", "continue to draw its energy directly from me.", 591);
                return true;
            }
            if (value2 == 119) {
                player.getDialogueManager().showNpcTwoLineDialogue("The range at which I will be able to sustain it is limited.", "I cannot ensure it will be effective off the atoll.", 591);
                return true;
            }
            if (value2 == 120) {
                player.getDialogueManager().showNpcThreeLineDialogue("Furthermore, you will not be able to attack whilst using", "this, so be careful. Perhaps when I refine my spells I", "could look into making this possible.", 591);
                if (!this.isProgressFlagSet(player, 18)) {
                    int questId24 = this.getQuestId();
                    player.questProgressFlags[questId24] = player.questProgressFlags[questId24] + GameUtil.bitFlag(18);
                } else {
                    player.getDialogueManager().finishDialogue();
                }
                return true;
            }
            if (value2 == 121) {
                player.setActionLocked(true);
                MonkeyMadnessQuest.showCaranockWaydarCutsceneTitle(player);
                player.getPacketSender().showWalkableInterface(8677);
                CaranockWaydarCutsceneStartTask caranockWaydarCutsceneStartTask = new CaranockWaydarCutsceneStartTask(this, 5, player);
                World.getTaskScheduler().schedule(caranockWaydarCutsceneStartTask);
                return true;
            }
        }
        if (npcId == 1452) {
            if (player.getEquipmentManager().getItemIdAtSlot(2) != 4021) {
                return false;
            }
            if (value42 == 13 && !this.isProgressFlagSet(player, 7)) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Hello there little monkey.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("Hello big-big. Aunty told me not to talk to strangers.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Oh I'm not a stranger...", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("You look strange to me!", 591);
                    int questId25 = this.getQuestId();
                    player.questProgressFlags[questId25] = player.questProgressFlags[questId25] + GameUtil.bitFlag(7);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 13 && !this.isProgressFlagSet(player, 8)) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Hello again little monkey.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("You're strange! Go away!", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I'm not a stranger...!", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("Then what are you?", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showFourOptions("I'm a monkey's brother!", "I knew your sister's mother!", "I'm just a monkey lover!", "Well I'll be a monkey's uncle!");
                    return true;
                }
                if (value2 == 6) {
                    if (value32 == 4) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Well I'll be a monkey's uncle!", 591);
                        player.getDialogueManager().setNextDialogueStep(7);
                        return true;
                    }
                    player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                    player.getDialogueManager().setNextDialogueStep(5);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcOneLineDialogue("Uh ah! You do look like my uncle!", 591);
                    int questId26 = this.getQuestId();
                    player.questProgressFlags[questId26] = player.questProgressFlags[questId26] + GameUtil.bitFlag(8);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 13 && !this.isProgressFlagSet(player, 9)) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("You look a lot bigger than last time, Uncle!", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("I've been ... eating more bananas... just like you should", "be!", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcOneLineDialogue("I'm bored.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Why are you bored?", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Aunty told me to pick loads of bananas. She said if, I", "got bananas, she'd give me a new toy!", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showTwoOptions("What kind of toy did Aunty say she'd give you?", "How many bananas did Aunty want?");
                    return true;
                }
                if (value2 == 7) {
                    if (value32 == 2) {
                        player.getDialogueManager().showPlayerOneLineDialogue("How many bananas did Aunty want?", 591);
                        player.getDialogueManager().setNextDialogueStep(8);
                        return true;
                    }
                    player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                    player.getDialogueManager().setNextDialogueStep(6);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Twenty! But I can't count! It's very mean of her, isn't", "it Uncle?", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("Yes, very mean ... do you want me to get the bananas", "for you?", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcOneLineDialogue("Ok! Ook Ook!", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showPlayerOneLineDialogue("But only if you promise to let me borrow your toy...", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showNpcOneLineDialogue("Ok Uncle!", 591);
                    int questId27 = this.getQuestId();
                    player.questProgressFlags[questId27] = player.questProgressFlags[questId27] + GameUtil.bitFlag(9);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 13 && !this.isProgressFlagSet(player, 10)) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Did you get any bananas, Uncle?", 591);
                    return true;
                }
                if (value2 == 2) {
                    if (player.getInventoryManager().containsItem(1963)) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Yes, I have some here.", 591);
                    } else {
                        player.getDialogueManager().showPlayerOneLineDialogue("Not yet.", 591);
                        player.getDialogueManager().finishDialogue();
                    }
                    return true;
                }
                if (value2 == 3) {
                    if (player.getInventoryManager().containsItemAmount(1963, 5)) {
                        player.getDialogueManager().showNpcOneLineDialogue("Wow that's a lot of bananas! Are there twenty?", 591);
                    } else {
                        player.getDialogueManager().showNpcOneLineDialogue("I think she wants some more bananas...", 591);
                        player.getDialogueManager().finishDialogue();
                    }
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Yes, of course there are.", 591);
                    return true;
                }
                if (value2 == 5) {
                    Npc npc = Npc.findByDefinitionId(1454);
                    player.pendingGameMode = npc.scriptedSequenceLoopCount;
                    player.getDialogueManager().showItemMessage("You give the monkey child your bananas.", new ItemStack(1963, 1));
                    player.getInventoryManager().removeItem(new ItemStack(1963, player.getInventoryManager().getItemAmount(1963)));
                    int questId28 = this.getQuestId();
                    player.questProgressFlags[questId28] = player.questProgressFlags[questId28] + GameUtil.bitFlag(10);
                    player.getDialogueManager().setNextDialogueStep(8);
                    return true;
                }
            }
            if (value42 == 13 && !this.isProgressFlagSet(player, 11)) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Has Aunty given you the toy yet?", 591);
                    return true;
                }
                if (value2 == 2) {
                    Npc npc = Npc.findByDefinitionId(1454);
                    if (player.pendingGameMode == npc.scriptedSequenceLoopCount) {
                        player.getDialogueManager().showNpcTwoLineDialogue("No, not yet... she said she's too busy dealing with", "something called humans.", 591);
                        player.getDialogueManager().finishDialogue();
                    } else {
                        player.getDialogueManager().showNpcOneLineDialogue("Yeah - it's really neat!", 591);
                    }
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Can I borrow it now then?", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("But I only just got it!", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Please?", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcOneLineDialogue("Ok then...", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showItemMessage("The monkey child gives you some kind of talisman.", new ItemStack(4023, 1));
                    player.getInventoryManager().addOrDropItem(new ItemStack(4023, 1));
                    int questId29 = this.getQuestId();
                    player.questProgressFlags[questId29] = player.questProgressFlags[questId29] + GameUtil.bitFlag(11);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcOneLineDialogue("Aunty will be so happy!", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (!(value42 != 13 && value42 != 1 || !this.isProgressFlagSet(player, 11) || player.ownsItem(4023) || player.ownsItem(4024))) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I lost your toy, can I get a new one?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("But I only just got a new one!", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Please?", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("Ok then...", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showItemMessage("The monkey child gives you some kind of talisman.", new ItemStack(4023, 1));
                    player.getInventoryManager().addOrDropItem(new ItemStack(4023, 1));
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
        }
        if (npcId == 1441) {
            if (value42 == 15) {
                if (player.getEquipmentManager().getItemIdAtSlot(2) != 4021) {
                    return false;
                }
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Hello?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("What brings you up here, monkey?", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("I have come to seek audience with Awowogei. I am told", "I need your permission to do so.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcTwoLineDialogue("That's right - you do. What business have you on our", "island?", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("I am envoy from the monkeys of Karamja. I have", "come to propose an alliance.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcOneLineDialogue("I see. Very well, you look genuine enough. Follow me.", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.setActionLocked(true);
                    player.getPacketSender().showInterface(8677);
                    npcId = this.getQuestId();
                    KrukAwowogeiEscortTask krukAwowogeiEscortTask = new KrukAwowogeiEscortTask(this, 5, player, npcId);
                    World.getTaskScheduler().schedule(krukAwowogeiEscortTask);
                    return true;
                }
            }
            if (value2 == 100) {
                player.getDialogueManager().showNpcOneLineDialogue("Open the gates! A monkey wishes to pass!", 591);
                return true;
            }
            if (value2 == 101) {
                player.getPacketSender().queueRelativeMovementStep(0, player.getPosition().getY() < 2767 ? 2 : -2, true);
                player.getPacketSender().openDoubleDoorPair(4787, 2719, 2766, 4788, 2721, 2766);
                player.getDialogueManager().finishDialogue();
                return false;
            }
        }
        if (npcId == 1461) {
            if (player.getEquipmentManager().getItemIdAtSlot(2) != 4021) {
                return false;
            }
            if (value42 == 14) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Grrr ... What do you want?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I'd like to speak with Awowogei, please.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Only the Captain of the Monkey Guard or those he", "authorises may enter this building. You will need his", "permission to enter.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Who is the Captain of the Monkey Guard?", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcOneLineDialogue("He goes by the name of Kruk.", 591);
                    player.setQuestState(this.getQuestId(), 15);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 >= 16) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Grrr ... What do you want?", 591);
                    return true;
                }
                if (value2 == 2) {
                    if (player.getPosition().getY() < 2759) {
                        player.getDialogueManager().showPlayerOneLineDialogue("I must speak with Awowogei again.", 591);
                    } else {
                        player.getDialogueManager().showPlayerOneLineDialogue("I would like to leave now.", 591);
                    }
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcOneLineDialogue("As you wish.", 591);
                    return true;
                }
                if (value2 == 4) {
                    if (player.getPosition().getY() < 2759) {
                        player.getPacketSender().queueTwoStepMovement(new Position[]{new Position(2802, 2758), new Position(2802, 2759)}, 2, true);
                    } else {
                        player.getPacketSender().queueTwoStepMovement(new Position[]{new Position(2802, 2758), new Position(2802, 2756)}, 2, true);
                    }
                    return false;
                }
            }
        }
        if (npcId == 1448) {
            if (player.getEquipmentManager().getItemIdAtSlot(2) != 4021) {
                return false;
            }
            if (value42 == 16) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Greetings, Awowogei.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("Greetings, visitor. What brings you to my island?", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("I am an envoy from the monkeys of Karamja. We wish", "to propose an alliance.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I see. Ours is a strong and mighty lineage of monkey,", "visitor. We clearly do not need your offer of an", "alliance.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Awowogei, please consider my offer carefully.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showPlayerThreeLineDialogue("We offer strength in numbers and our island would", "serve as a northern platform for defence. All that we", "ask for in return is peace.", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().setDialogueNpcId(1449);
                    player.getDialogueManager().showNpcTwoLineDialogue("I don't believe him, Awowogei. Never trust a northern", "monkey.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcOneLineDialogue("What is your opinion, Muruwoi?", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().setDialogueNpcId(1450);
                    player.getDialogueManager().showNpcOneLineDialogue("I think he seems trustworthy, sir.", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I must admit, I have always regarded your kind as our", "inferior cousins, visitor.", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showNpcTwoLineDialogue("However, I am well aware that you may have a few", "things to offer.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().setDialogueNpcId(1449);
                    player.getDialogueManager().showNpcOneLineDialogue("Don't listen to him Awowogei!", 591);
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showNpcOneLineDialogue("Be silent, Uwogo.", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I have heard your kind are exceptionally resourceful. I", "wish to put this reputation to the test.", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().showNpcTwoLineDialogue("You must be well aware your kind are hunted and", "trapped almost everywhere.", 591);
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().showNpcTwoLineDialogue("In particular you may have heard of such activities in a", "city known to the humans as Ardougne.", 591);
                    return true;
                }
                if (value2 == 17) {
                    player.getDialogueManager().showNpcTwoLineDialogue("There are several of your kind kept captive there. I", "challenge you to free one and return it to me.", 591);
                    return true;
                }
                if (value2 == 18) {
                    player.getDialogueManager().showPlayerOneLineDialogue("How am I meant to free one of them?", 591);
                    return true;
                }
                if (value2 == 19) {
                    player.getDialogueManager().showNpcOneLineDialogue("This is for you to decide, visitor.", 591);
                    return true;
                }
                if (value2 == 20) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Very well. I will be back later, with one of the captives.", 591);
                    player.setQuestState(this.getQuestId(), 17);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
            if (value42 == 17) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Have you brought with you a captive?", 591);
                    return true;
                }
                if (value2 == 2) {
                    if (player.getInventoryManager().containsItem(4033)) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Yes, I have.", 591);
                    } else {
                        player.getDialogueManager().showPlayerOneLineDialogue("Not yet.", 591);
                        player.getDialogueManager().finishDialogue();
                    }
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcOneLineDialogue("Well done!", 591);
                    player.getInventoryManager().removeItem(new ItemStack(4033, 1));
                    player.setQuestState(this.getQuestId(), 18);
                    player.getDialogueManager().setNextDialogueStep(1);
                    return true;
                }
            }
            if (value42 == 18) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcThreeLineDialogue("You have shown yourself to be very resourceful. You", "have managed to complete an extremely long journey", "remarkably quickly.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Thank you.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcTwoLineDialogue("You are clearly well acquainted with the ways of this", "world. We will talk more on this later.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcTwoLineDialogue("In the meantime, feel free to remain as long as you like", "on my island.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What about the proposed alliance, Awowogei?", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I must think upon it some more and discuss the matter", "with my advisers. We will contact you when we are", "ready.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
        }
        if (npcId == 1469 && value42 == 17) {
            if (value2 == 1) {
                if (!player.isWearingMonkeyDisguise() && player.isInsideArdougneZooMonkeyEnclosure()) {
                    player.getDialogueManager().showNpcOneLineDialogue("My word - what are you doing in there?", 591);
                    player.getDialogueManager().setNextDialogueStep(10);
                    return true;
                }
                if (player.isWearingMonkeyDisguise() && !player.isInsideArdougneZooMonkeyEnclosure()) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Ook Ook!", 591);
                    return true;
                }
            }
            if (value2 == 2) {
                player.getDialogueManager().showNpcThreeLineDialogue("Why do you monkeys keep trying to escape? Good", "thing I've caught you before you got away, you little", "scoundrel.", 591);
                return true;
            }
            if (value2 == 3) {
                player.getDialogueManager().showPlayerOneLineDialogue("Ook!", 591);
                return true;
            }
            if (value2 == 4) {
                player.getDialogueManager().showNpcOneLineDialogue("Let's put you back in your cage where you belong...", 591);
                return true;
            }
            if (value2 == 5) {
                player.getDialogueManager().showPlayerOneLineDialogue("Ok!", 591);
                return true;
            }
            if (value2 == 6) {
                player.getDialogueManager().showNpcOneLineDialogue("What??", 591);
                return true;
            }
            if (value2 == 7) {
                player.getDialogueManager().showPlayerOneLineDialogue("Err ... Ook?", 591);
                return true;
            }
            if (value2 == 8) {
                player.getDialogueManager().showNpcOneLineDialogue("I must be imagining things ... monkeys can't talk.", 591);
                return true;
            }
            if (value2 == 9) {
                player.setActionLocked(true);
                player.getPacketSender().showInterface(8677);
                ArdougneZooMonkeyRecaptureTask ardougneZooMonkeyRecaptureTask = new ArdougneZooMonkeyRecaptureTask(this, 5, player);
                World.getTaskScheduler().schedule(ardougneZooMonkeyRecaptureTask);
                return true;
            }
            if (value2 == 10) {
                player.getDialogueManager().showPlayerTwoLineDialogue("I ... er ... I don't know! One minute I was asleep, and", "the next minute I was here surrounded by monkeys!", 591);
                return true;
            }
            if (value2 == 11) {
                player.getDialogueManager().showNpcOneLineDialogue("Well, don't worry. We'll have you out of there shortly.", 591);
                return true;
            }
            if (value2 == 12) {
                player.setActionLocked(true);
                player.getPacketSender().showInterface(8677);
                ArdougneZooGnomeRescueTask ardougneZooGnomeRescueTask = new ArdougneZooGnomeRescueTask(this, 5, player);
                World.getTaskScheduler().schedule(ardougneZooGnomeRescueTask);
                return true;
            }
            if (value2 == 13) {
                player.getDialogueManager().showPlayerOneLineDialogue("Thank you.", 591);
                return true;
            }
            if (value2 == 14) {
                player.getDialogueManager().showNpcOneLineDialogue("No problem.", 591);
                return true;
            }
        }
        if (npcId == 1463) {
            if (!player.isWearingMonkeyDisguise() || player.ownsItem(4033)) {
                return false;
            }
            if (value42 == 17) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Hello there, little monkey.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showNpcOneLineDialogue("Hello there!", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("How would you like to get out of here?", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("Escape!? It's all I ever think about!", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showPlayerOneLineDialogue("That's convenient. When would you like to leave?", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showNpcOneLineDialogue("Where will you be taking me?", 591);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Erm ... to the happy, sunny jungle of Karamja...", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcOneLineDialogue("Wowee! I was born there you know!", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showPlayerOneLineDialogue("That's nice. Are you ready to go?", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcOneLineDialogue("Yes. Actually, can I bring some of my friends?", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showPlayerOneLineDialogue("No, I only have space for one.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showNpcOneLineDialogue("Pleeeeease?", 591);
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showPlayerOneLineDialogue("No!", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showNpcOneLineDialogue("Pretty pleeeeease?", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().showPlayerOneLineDialogue("No!!", 591);
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().showNpcOneLineDialogue("Pretty please with a banana on top?", 591);
                    return true;
                }
                if (value2 == 17) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("Look, I already said no. If you want to come then", "jump into my backpack.", 591);
                    return true;
                }
                if (value2 == 18) {
                    player.getDialogueManager().showNpcOneLineDialogue("Ook!", 591);
                    return true;
                }
                if (value2 == 19) {
                    if (player.getInventoryManager().getContainer().getFreeSlots() > 0) {
                        if (player.getInteractionTarget() != null && player.getInteractionTarget().isNpc()) {
                            Npc npc = (Npc)player.getInteractionTarget();
                            CombatManager.finishDeath(npc, player, false);
                        }
                        player.getInventoryManager().addOrDropItem(new ItemStack(4033, 1));
                        return false;
                    }
                    player.getDialogueManager().showNpcOneLineDialogue("There is not enough space for me!", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
        }
        return false;
    }
}

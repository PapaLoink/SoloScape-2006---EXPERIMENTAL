package com.rs2.model.quest.impl;

import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.c.ProjectileDefinition;
import com.rs2.model.combat.ProjectileTiming;
import com.rs2.model.cutscene.Cutscene;
import com.rs2.model.cutscene.restlessghost.RestlessGhostCutscene;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestScript;
import com.rs2.model.quest.impl.RestlessGhostCoffinGhostSpawnTask;
import com.rs2.model.skill.woodcutting.WoodcuttingHandler;
import com.rs2.model.task.TickTask;
import com.rs2.util.GameUtil;
import java.util.ArrayList;

public final class RestlessGhostQuest
extends QuestScript {
    public RestlessGhostQuest(int value2) {
        super(12);
        super.setQuestPointReward(1);
    }

    @Override
    public final boolean handleGroundItemInteraction(Player player, int itemId, int value2) {
        if (itemId == 553) {
            if (player.getQuestState(this.getQuestId()) < 4 || player.getQuestState(this.getQuestId()) >= 4 && player.ownsItem(553)) {
                return true;
            }
            if (player.getQuestState(this.getQuestId()) == 4) {
                GameplayHelper.spawnOwnedNpcAdjacentToPlayer(player, new Npc(459), true, false);
                player.setQuestState(this.getQuestId(), 5);
            }
            return false;
        }
        return false;
    }

    @Override
    public final boolean handleItemOnObject(Player objectId, int objectId2, int value2, int value32) {
        if (((Player)objectId).getQuestState(this.getQuestId()) >= 5 && objectId2 == 553 && value2 == 2146 && Npc.findByDefinitionId(457) != null) {
            Player player = (Player)objectId;
            player.packetSender.sendGameMessage("You put the skull in the coffin.");
            ObjectManager.getInstance().removeDynamicObjectAt(3249, 3192, 0, 0);
            ObjectManager.getInstance().addDynamicObject(new DynamicObject(2145, 3249, 3192, 0, 0, 10, 2145, 999999999), true);
            ArrayList<Npc> arrayList = new ArrayList<Npc>();
            arrayList.add(Npc.findByDefinitionId(457));
            Cutscene cutscene = new RestlessGhostCutscene(player, arrayList);
            cutscene.startCutscene();
            return true;
        }
        return false;
    }

    @Override
    public final String[] buildQuestJournal(Player player, int value2) {
        if (value2 == 0) {
            return new String[]{"I can start this quest by speaking to Father Aereck in the", "church next to Lumbridge Castle", "I must be unafraid of a Level 13 Skeleton"};
        }
        if (value2 == 2) {
            return new String[]{"I should find Father Urhney for help to get rid", "of the ghost."};
        }
        if (value2 == 3) {
            return new String[]{"I should talk to the Ghost to find out why it is hunting the", "graveyard crypt."};
        }
        if (value2 == 4) {
            return new String[]{"I should go and search the Wizard's Tower South West of", "Lumbridge for the Ghost's Skull."};
        }
        if (value2 >= 5) {
            return new String[]{"I should bring the skull back to the graveyard crypt."};
        }
        if (value2 == 1) {
            return new String[]{"Quest Completed!", "", "You were awarded:", "1 Quest Point", "1125 Prayer XP"};
        }
        return null;
    }

    @Override
    public final void awardCompletionRewards(Player player) {
        super.markQuestComplete(player);
        super.showQuestCompleteInterface(player);
        Player player2 = player;
        player2.packetSender.sendInterfaceText("1 Quest Point", 12150);
        player2 = player;
        player2.packetSender.sendInterfaceText("1125 Prayer XP", 12151);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 12152);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 12153);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 12154);
        player2 = player;
        player2.packetSender.sendInterfaceText("", 12155);
        player.getSkillManager().addQuestExperience(5, 1125.0);
        player2 = player;
        player2.packetSender.sendInterfaceModel(QuestScript.usesLegacyCompletionInterface() ? 6161 : 12145, 250, 553);
        player2 = player;
        player2.packetSender.showInterface(QuestScript.usesLegacyCompletionInterface() ? 1689 : 12140);
        player2 = player;
        player.deferLevelUpInterfaces = false;
    }

    @Override
    public final boolean handleFirstObjectAction(Player player, int objectId, int value6, int value32, int value42) {
        if (objectId == 2145 && value6 == 3249 && value32 == 3192) {
            if (value42 >= 2) {
                ObjectManager.getInstance().removeDynamicObjectAt(3249, 3192, 0, 0);
                ObjectManager.getInstance().addDynamicObject(new DynamicObject(2146, 3249, 3192, 0, 0, 10, 2145, 999999999), true);
                Player player2 = player;
                player2.packetSender.sendGameMessage("You open the coffin.");
                if (Npc.findByDefinitionId(457) == null) {
                    int value2 = 605;
                    Position position = new Position(3250, 3195, 0);
                    Position position2 = new Position(3250, 3192, 0);
                    player2 = player;
                    Object value5 = this;
                    value42 = GameUtil.getDistance(position2, position);
                    new WoodcuttingHandler(position2, 0, position, 0, new ProjectileDefinition(605, ProjectileTiming.MAGIC)).sendProjectileToNearbyPlayers();
                    World.getTaskScheduler().schedule(new RestlessGhostCoffinGhostSpawnTask((RestlessGhostQuest)value5, value42, player2));
                }
                return true;
            }
            return true;
        }
        if (objectId == 2146 && value6 == 3249 && value32 == 3192) {
            if (value42 >= 2) {
                ObjectManager.getInstance().removeDynamicObjectAt(3249, 3192, 0, 0);
                ObjectManager.getInstance().addDynamicObject(new DynamicObject(2145, 3249, 3192, 0, 0, 10, 2145, 999999999), true);
                Player player3 = player;
                player3.packetSender.sendGameMessage("You close the coffin.");
                return true;
            }
            return true;
        }
        return false;
    }

    @Override
    public final boolean handleNpcDialogue(Player player, int npcId, int value2, int value32, int value42) {
        if (npcId == 456) {
            if (value42 == 0) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Welcome to the church of holy Saradomin.", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showThreeOptions("Who's Saradomin?", "Nice place you've got here.", "I'm looking for a quest!");
                    return true;
                }
                if (value2 == 3) {
                    if (value32 == 3) {
                        player.getDialogueManager().showPlayerOneLineDialogue("I'm looking for a quest.", 591);
                        player.getDialogueManager().setNextDialogueStep(4);
                        return true;
                    }
                    player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                    player.getDialogueManager().setNextDialogueStep(2);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("That's lucky, I need someone to do a quest for me.", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showTwoOptions("Ok, let me help then.", "Sorry, I don't have time right now.");
                    return true;
                }
                if (value2 == 6) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Ok, let me help then.", 591);
                        this.startQuest(player);
                        player.getDialogueManager().setNextDialogueStep(7);
                        return true;
                    }
                    player.getDialogueManager().finishDialogue();
                    return false;
                }
            }
            if (value42 == 2) {
                if (value2 == 7) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Thank you. The problem is, there is a ghost in the", "church graveyard. I would like you to get rid of it.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcTwoLineDialogue("If you need any help, my friend Father Urhney is an", "expert on ghosts.", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I believe he is currently living as a hermit in Lumbridge", "swamp. He has a little shack in the south-east of the", "swamps.", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Exit the graveyard through the south gate to reach the", "swamp. I'm sure if you told him that I sent you he'd", "be willing to help.", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showNpcTwoLineDialogue("My name is Father Aereck by the way. Pleased to", "meet you.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Likewise.", 591);
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Take care travelling through the swamps, I have heard", "they can be quite dangerous.", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I will, thanks.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Have you got rid of the ghost yet?", 591);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showPlayerOneLineDialogue("I can't find Father Urhney at the moment.", 591);
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Well, you can get to the swamp he lives in by going", "south through the cemetery.", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcTwoLineDialogue("You'll have to go right into the eastern depths of the", "swamp, near the coastline. That is where his house is.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
        }
        if (npcId == 458) {
            if (value42 == 2) {
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Go away! I'm meditating!", 614);
                    return true;
                }
                if (value2 == 2) {
                    player.getDialogueManager().showThreeOptions("Well, that's friendly.", "Father Aereck sent me to talk to you.", "I've come to repossess your house.");
                    return true;
                }
                if (value2 == 3) {
                    if (value32 == 2) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Father Aereck sent me to talk to you.", 591);
                        player.getDialogueManager().setNextDialogueStep(4);
                        return true;
                    }
                    player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                    player.getDialogueManager().setNextDialogueStep(2);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I suppose I'd better talk to you then. What problems", "has he got himself into this time?", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showTwoOptions("He's got a ghost haunting his graveyard.", "You mean he gets himself into lots of problems?");
                    return true;
                }
                if (value2 == 6) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("He's got a ghost haunting his graveyard.", 591);
                        player.getDialogueManager().setNextDialogueStep(7);
                        return true;
                    }
                    player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                    player.getDialogueManager().setNextDialogueStep(5);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcOneLineDialogue("Oh, the silly fool.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I leave the town for just five months, and ALREADY he", "can't manage.", 591);
                    return true;
                }
                if (value2 == 9) {
                    player.getDialogueManager().showNpcOneLineDialogue("(sigh)", 591);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Well, I can't go back and exorcise it. I vowed not to", "leave this place. Until I had done a full two years of", "prayer and meditation.", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showNpcOneLineDialogue("Tell you what I can do though; take this amulet.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showItemMessage("Father Urhney hands you an amulet.", new ItemStack(552, 1));
                    player.getInventoryManager().addOrDropItem(new ItemStack(552, 1));
                    player.setQuestState(this.getQuestId(), 3);
                    return true;
                }
            }
            if (value42 >= 3) {
                if (value2 == 13) {
                    player.getDialogueManager().showNpcOneLineDialogue("It is an Amulet of Ghostspeak.", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showNpcThreeLineDialogue("So called, because when you wear it you can speak to", "ghosts. A lot of ghosts are doomed to be ghosts because", "they have left some important task uncompleted.", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Maybe if you know what this task is, you can get rid of", "the ghost. I'm not making any guarantees mind you,", "but it is the best I can do right now.", 591);
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Thank you. I'll give it a try!", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
                if (value2 == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue("Go away! I'm meditating!", 614);
                    return true;
                }
                if (value2 == 2) {
                    if (!player.ownsItem(552)) {
                        player.getDialogueManager().showThreeOptions("Well, that's friendly.", "I've come to repossess your house.", "I've lost the Amulet of Ghostspeak.");
                    } else {
                        player.getDialogueManager().showTwoOptions("Well, that's friendly.", "I've come to repossess your house.");
                    }
                    return true;
                }
                if (value2 == 3) {
                    if (value32 == 3) {
                        player.getDialogueManager().showPlayerOneLineDialogue("I've lost the Amulet of Ghostspeak.", 591);
                        player.getDialogueManager().setNextDialogueStep(4);
                        return true;
                    }
                    player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                    player.getDialogueManager().setNextDialogueStep(2);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showOneLineStatement("Father Urhney sighs.");
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showNpcTwoLineDialogue("How careless can you get? Those things aren't easy to", "come by you know! It's a good job I've got a spare.", 591);
                    return true;
                }
                if (value2 == 6) {
                    player.getDialogueManager().showItemMessage("Father Urhney hands you an amulet.", new ItemStack(552, 1));
                    player.getInventoryManager().addOrDropItem(new ItemStack(552, 1));
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcOneLineDialogue("Be more careful this time.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Ok, I'll try to be.", 591);
                    player.getDialogueManager().finishDialogue();
                    return true;
                }
            }
        }
        if (npcId == 457) {
            if (value42 == 3) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Hello ghost, how are you?", 591);
                    return true;
                }
                if (value2 == 2) {
                    if (player.getEquipmentManager().getContainer().getItemAt(2) != null && player.getEquipmentManager().getContainer().getItemAt(2).getId() == 552) {
                        player.getDialogueManager().showNpcOneLineDialogue("Not very good actually.", 591);
                    } else {
                        player.getDialogueManager().showNpcOneLineDialogue("woooo wooo wooooo woooo.", 591);
                        player.getDialogueManager().finishDialogue();
                    }
                    return true;
                }
                if (value2 == 3) {
                    player.getDialogueManager().showPlayerOneLineDialogue("What's the problem then?", 591);
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcOneLineDialogue("Did you just understand what I said???", 591);
                    return true;
                }
                if (value2 == 5) {
                    player.getDialogueManager().showThreeOptions("Yep, now tell me what the problem is.", "No, you sound like you're speaking nonsense to me.", "Wow, this amulet works!");
                    return true;
                }
                if (value2 == 6) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Yep, now tell me what the problem is.", 591);
                        player.getDialogueManager().setNextDialogueStep(18);
                        return true;
                    }
                    if (value32 == 3) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Wow, this amulet works!", 591);
                        player.getDialogueManager().setNextDialogueStep(7);
                        return true;
                    }
                    player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                    player.getDialogueManager().setNextDialogueStep(5);
                    return true;
                }
                if (value2 == 7) {
                    player.getDialogueManager().showNpcThreeLineDialogue("Oh! It's your amulet that's doing it! I did wonder. I", "don't suppose you can help me? I don't like being a", "ghost.", 591);
                    return true;
                }
                if (value2 == 8) {
                    player.getDialogueManager().showTwoOptions("Yes, ok. Do you know why you're a ghost?", "No, you're scary!");
                    return true;
                }
                if (value2 == 9) {
                    if (value32 == 1) {
                        player.getDialogueManager().showPlayerOneLineDialogue("Yes, ok. Do you know WHY you're a ghost?", 591);
                        player.getDialogueManager().setNextDialogueStep(10);
                        return true;
                    }
                    player.getDialogueManager().showOneLineStatement("This option is currently missing...");
                    player.getDialogueManager().setNextDialogueStep(8);
                    return true;
                }
                if (value2 == 10) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Nope. I just know I can't do much of anything like", "this!", 591);
                    return true;
                }
                if (value2 == 11) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("I've been told a certain task may need to be completed", "so you can rest in peace.", 591);
                    return true;
                }
                if (value2 == 12) {
                    player.getDialogueManager().showNpcThreeLineDialogue("I should think it is probably because a warlock has come", "along and stolen my skull. If you look inside my coffin", "there, you'll find my corpse without a head on it.", 591);
                    return true;
                }
                if (value2 == 13) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Do you know where this warlock might be now?", 591);
                    return true;
                }
                if (value2 == 14) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I think it was one of the warlocks who lives in the big", "tower by the sea south-west from here.", 591);
                    return true;
                }
                if (value2 == 15) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("Ok. I will try and get the skull back for you, then you", "can rest in peace.", 591);
                    return true;
                }
                if (value2 == 16) {
                    player.getDialogueManager().showNpcOneLineDialogue("Ooh, thank you. That would be such a great relief!", 591);
                    return true;
                }
                if (value2 == 17) {
                    player.getDialogueManager().showNpcOneLineDialogue("It is so dull being a ghost...", 591);
                    player.getDialogueManager().finishDialogue();
                    player.setQuestState(this.getQuestId(), 4);
                    return true;
                }
                if (value2 == 18) {
                    player.getDialogueManager().showNpcTwoLineDialogue("WOW! This is INCREDIBLE! I didn't expect anyone", "to ever understand me again!", 591);
                    return true;
                }
                if (value2 == 19) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Ok, Ok, I can understand you!", 591);
                    return true;
                }
                if (value2 == 20) {
                    player.getDialogueManager().showPlayerTwoLineDialogue("But have you any idea WHY you're doomed to be a", "ghost?", 591);
                    return true;
                }
                if (value2 == 21) {
                    player.getDialogueManager().showNpcOneLineDialogue("Well, to be honest... I'm not sure.", 591);
                    player.getDialogueManager().setNextDialogueStep(11);
                    return true;
                }
            }
            if (value42 == 5) {
                if (value2 == 1) {
                    player.getDialogueManager().showPlayerOneLineDialogue("Hello ghost, how are you?", 591);
                    return true;
                }
                if (value2 == 2) {
                    if (player.getEquipmentManager().getContainer().getItemAt(2) != null && player.getEquipmentManager().getContainer().getItemAt(2).getId() == 552) {
                        player.getDialogueManager().showNpcOneLineDialogue("How are you doing finding my skull?", 591);
                    } else {
                        player.getDialogueManager().showNpcOneLineDialogue("woooo wooo wooooo woooo.", 591);
                        player.getDialogueManager().finishDialogue();
                    }
                    return true;
                }
                if (value2 == 3) {
                    if (player.ownsItem(553)) {
                        player.getDialogueManager().showPlayerOneLineDialogue("I have found it!", 591);
                    } else {
                        player.getDialogueManager().showPlayerOneLineDialogue("I haven't found it yet.", 591);
                        player.getDialogueManager().finishDialogue();
                    }
                    return true;
                }
                if (value2 == 4) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Hurrah! Now I can stop being a ghost! You just need", "to put it in my coffin there, and I will be free!", 591);
                    return true;
                }
            }
        }
        return false;
    }
}


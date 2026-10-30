import com.rs2.ServerSettings;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.quest.impl.RomeoAndJulietQuest;
import com.rs2.model.quest.impl.RomeoJulietSupportingNpcs;

/** Regression checks for issue #6. Run from Server with its libraries on the classpath. */
public final class RomeoAndJulietDialogueChecks {
    private static final int QUEST = 13;
    private static final int ROMEO = 639;
    private static final int JULIET = 637;
    private static final int LETTER = 755;
    private static final int POTION = 756;

    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        ServerSettings.freeToPlayWorld = false;
        ServerSettings.membershipRequirementMode = 1;
        QuestDefinition.loadDefinitions();
        InterfaceDefinition.loadDefinitions();
        ItemDefinition.loadDefinitions();
        NpcDefinition.loadDefinitions();
        require(com.rs2.net.packet.AudioIds443.jingle(571) == 120,
                "Original Cadava Potion jingle is missing from the 443 cache");
        require(com.rs2.net.packet.AudioIds443.jingle(572) == 168,
                "Original Romeo Falls in Love jingle is missing from the 443 cache");
        require("Draul Leptoc".equalsIgnoreCase(
                NpcDefinition.forId(RomeoJulietSupportingNpcs.DRAUL).getName()),
                "Draul's revision-443 model is not mapped");
        require("Phillipa".equalsIgnoreCase(
                NpcDefinition.forId(RomeoJulietSupportingNpcs.PHILLIPA).getName()),
                "Phillipa's revision-443 model is not mapped");
        RomeoJulietSupportingNpcs.spawnMissingNpcs();
        com.rs2.model.npc.Npc draul = com.rs2.model.npc.Npc.findByDefinitionId(RomeoJulietSupportingNpcs.DRAUL);
        com.rs2.model.npc.Npc phillipa = com.rs2.model.npc.Npc.findByDefinitionId(RomeoJulietSupportingNpcs.PHILLIPA);
        require(draul != null && draul.getPosition().getPlane() == 0,
                "Draul did not spawn on the mansion's ground floor");
        require(phillipa != null && phillipa.getPosition().getPlane() == 1,
                "Phillipa did not spawn upstairs near Juliet");
        RomeoJulietSupportingNpcs.spawnMissingNpcs();
        require(com.rs2.model.npc.Npc.findByDefinitionId(RomeoJulietSupportingNpcs.PHILLIPA) == phillipa,
                "Repeated spawn registration must not duplicate Phillipa");

        // Both NPCs must respond at every stage, not just when they advance the quest.
        for (int stage = 0; stage <= 8; stage++) {
            Player player = player(stage);
            require(DialogueManager.startDialogue(player, ROMEO),
                    "Romeo untalkable at quest stage " + stage);
            require(DialogueManager.startDialogue(player, JULIET),
                    "Juliet untalkable at quest stage " + stage);
            require(player.getQuestState(QUEST) == stage,
                    "Opening an optional conversation changed stage " + stage);
        }

        // The same must hold when a player still carries the item to hand over.
        Player letterCarrier = player(3);
        letterCarrier.getInventoryManager().addItem(new ItemStack(LETTER));
        require(DialogueManager.startDialogue(letterCarrier, ROMEO),
                "Romeo must accept Juliet's letter");
        require(DialogueManager.startDialogue(letterCarrier, JULIET),
                "Juliet must remind the player to deliver her letter");
        Player potionCarrier = player(7);
        potionCarrier.getInventoryManager().addItem(new ItemStack(POTION));
        require(DialogueManager.startDialogue(potionCarrier, JULIET),
                "Juliet must accept the Cadava potion");

        // All three introductory answers should work instead of a missing-dialogue stub.
        for (int option = 1; option <= 3; option++) {
            Player player = player(0);
            require(DialogueManager.startDialogue(player, ROMEO), "Romeo introduction");
            require(DialogueManager.continueDialogue(player, ROMEO, 2, 0), "Romeo choices");
            require(DialogueManager.continueDialogue(player, ROMEO, 3, option),
                    "Romeo introduction option " + option + " not handled");
            if (option == 1) {
                require(player.getDialogueManager().getDialogueStep() == 9,
                        "Seeing Juliet should continue to Romeo's request");
            } else if (option == 2) {
                require(player.getDialogueManager().isDialogueInactive(),
                        "Declining to help should finish the dialogue");
            }
            require(player.getQuestState(QUEST) == 0,
                    "Introductory option prematurely started the quest");
        }

        // Romeo's question menu loops back, without advancing the quest.
        for (int option = 1; option <= 3; option++) {
            Player player = player(2);
            require(DialogueManager.continueDialogue(player, ROMEO, 19, 0),
                    "Romeo question menu not handled");
            require(DialogueManager.continueDialogue(player, ROMEO, 20, option),
                    "Romeo question option " + option + " not handled");
            if (option == 3) {
                require(player.getDialogueManager().isDialogueInactive(), "Goodbye should exit");
            } else {
                int next = option == 1 ? 21 : 23;
                require(player.getDialogueManager().getDialogueStep() == next - 1,
                        "Romeo question jumps to the wrong branch");
                require(DialogueManager.continueDialogue(player, ROMEO, next, 0),
                        "Romeo answer not implemented");
                require(player.getDialogueManager().getDialogueStep() == 18,
                        "Romeo answer did not return to the question menu");
            }
            require(player.getQuestState(QUEST) == 2,
                    "Asking Romeo questions should not advance the quest");
        }

        // Father Lawrence stage's previously missing choices are all reachable.
        for (int option = 1; option <= 4; option++) {
            Player player = player(4);
            require(DialogueManager.startDialogue(player, ROMEO), "Romeo stage 4 menu");
            require(DialogueManager.continueDialogue(player, ROMEO, 2, option),
                    "Romeo stage 4 option " + option + " not handled");
            if (option == 1 || option == 3) {
                int next = option == 1 ? 32 : 33;
                require(player.getDialogueManager().getDialogueStep() == next - 1,
                        "Romeo stage 4 jumped to the wrong branch");
                require(DialogueManager.continueDialogue(player, ROMEO, next, 0),
                        "Romeo stage 4 answer not implemented");
                require(player.getDialogueManager().getDialogueStep() == 0,
                        "Romeo stage 4 answer did not return to the menu");
            }
            require(player.getQuestState(QUEST) == 4,
                    "Romeo stage 4 chat changed quest progress");
        }

        // Juliet can start the quest, not just Romeo.
        Player julietStart = player(0);
        require(DialogueManager.startDialogue(julietStart, JULIET), "Juliet alternate opening");
        require(DialogueManager.continueDialogue(julietStart, JULIET, 2, 0),
                "Juliet alternate start choices");
        require(DialogueManager.continueDialogue(julietStart, JULIET, 3, 1),
                "Juliet alternate start acceptance");
        require(julietStart.getQuestState(QUEST) == 2, "Juliet did not start the quest");
        require(DialogueManager.continueDialogue(julietStart, JULIET, 1, 0),
                "Juliet did not continue into her letter scene");

        Player julietDecline = player(0);
        DialogueManager.startDialogue(julietDecline, JULIET);
        require(DialogueManager.continueDialogue(julietDecline, JULIET, 3, 2),
                "Juliet refusal not handled");
        require(julietDecline.getQuestState(QUEST) == 0,
                "Refusing Juliet should not start the quest");

        // The Apothecary's questions must work with no berries in inventory.
        for (int option = 1; option <= 4; option++) {
            Player player = player(6);
            require(DialogueManager.startDialogue(player, 638),
                    "Apothecary unavailable without berries");
            require(player.getDialogueManager().getDialogueStep() == 7,
                    "Apothecary did not route to berry question menu");
            require(DialogueManager.continueDialogue(player, 638, 8, 0),
                    "Apothecary question menu missing");
            require(DialogueManager.continueDialogue(player, 638, 9, option),
                    "Apothecary berry option " + option + " not handled");
            if (option < 4) {
                int next = option == 1 ? 10 : option == 2 ? 12 : 14;
                require(player.getDialogueManager().getDialogueStep() == next - 1,
                        "Apothecary berry option jumps to incorrect answer");
                require(DialogueManager.continueDialogue(player, 638, next, 0),
                        "Apothecary berry answer not implemented");
                require(player.getDialogueManager().getDialogueStep() == 7,
                        "Apothecary berry answer did not return to menu");
            } else {
                require(player.getDialogueManager().isDialogueInactive(),
                        "Apothecary goodbye should exit");
            }
            require(player.getQuestState(QUEST) == 6,
                    "Asking about Cadava berries advanced the quest");
        }
        Player potionInHand = player(7);
        potionInHand.getInventoryManager().addItem(new ItemStack(POTION));
        require(DialogueManager.startDialogue(potionInHand, 638),
                "Apothecary unavailable when potion still owned");
        require(potionInHand.getInventoryManager().containsItem(POTION),
                "Repeat dialogue must not consume the potion");
        require(DialogueManager.startDialogue(player(2), RomeoJulietSupportingNpcs.PHILLIPA),
                "Phillipa introduction missing");
        require(DialogueManager.startDialogue(player(7), RomeoJulietSupportingNpcs.DRAUL),
                "Draul Leptoc introduction missing");

        // Drinking a Cadava potion does not complete the quest.
        Player drinkingPlayer = player(7);
        drinkingPlayer.getInventoryManager().addItem(new ItemStack(POTION));
        RomeoAndJulietQuest drinkScript = new RomeoAndJulietQuest(0);
        require(drinkScript.handleInventoryItemFirstOption(drinkingPlayer, 3214, POTION, 7),
                "Drinking the potion was not handled");
        require(!drinkingPlayer.getInventoryManager().containsItem(POTION),
                "Potion was not consumed");
        require(drinkingPlayer.getQuestState(QUEST) == 7,
                "Drinking the potion advanced the quest");

        // Handing over Juliet's letter must keep Romeo available immediately.
        Player deliveringLetter = player(3);
        deliveringLetter.getInventoryManager().addItem(new ItemStack(LETTER));
        RomeoAndJulietQuest script = new RomeoAndJulietQuest(0);
        for (int step = 1; step <= 25; step++) {
            require(script.handleNpcDialogue(deliveringLetter, ROMEO, step, 0,
                    deliveringLetter.getQuestState(QUEST)), "Letter hand-off step " + step);
        }
        require(deliveringLetter.getQuestState(QUEST) == 4, "Letter did not advance to stage 4");
        require(!deliveringLetter.getInventoryManager().containsItem(LETTER),
                "Romeo did not take the letter");
        require(DialogueManager.startDialogue(deliveringLetter, ROMEO),
                "Romeo untalkable after accepting the letter");

        // Likewise Juliet must remain talkable after the potion hand-off.
        Player deliveringPotion = player(7);
        deliveringPotion.getInventoryManager().addItem(new ItemStack(POTION));
        for (int step = 1; step <= 14; step++) {
            require(script.handleNpcDialogue(deliveringPotion, JULIET, step, 0,
                    deliveringPotion.getQuestState(QUEST)), "Potion hand-off step " + step);
        }
        require(deliveringPotion.getQuestState(QUEST) == 8, "Potion did not advance to stage 8");
        require(!deliveringPotion.getInventoryManager().containsItem(POTION),
                "Juliet did not take the potion");
        require(DialogueManager.startDialogue(deliveringPotion, JULIET),
                "Juliet untalkable after accepting the potion");
        require(DialogueManager.startDialogue(deliveringPotion, ROMEO),
                "Romeo not available for quest completion");

        System.out.println("Romeo & Juliet dialogue checks passed");
    }

    private static Player player(int stage) throws Exception {
        Player player = NpcDialogueAudit.player();
        player.setQuestState(QUEST, stage);
        return player;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

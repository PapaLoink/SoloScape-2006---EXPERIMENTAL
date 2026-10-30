package com.rs2.model.quest.impl;

import com.rs2.model.GameplayHelper;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.player.Player;

/** 2006 rework supporting characters, absent from the imported spawn table. */
public final class RomeoJulietSupportingNpcs {
    // The revision-443 cache maps these to 3324/3325; later OSRS uses 5039/5040.
    public static final int DRAUL = 3324;
    public static final int PHILLIPA = 3325;

    private RomeoJulietSupportingNpcs() {
    }

    public static void spawnMissingNpcs() {
        // The live 443 NPC definition must agree with the expected character:
        // a numeric ID from a newer cache alone is not sufficient.
        spawnIfSupported(DRAUL, "Draul", 3158, 3427, 0);
        spawnIfSupported(PHILLIPA, "Phillipa", 3159, 3429, 1);
    }

    private static void spawnIfSupported(int id, String expectedName, int x, int y, int plane) {
        if (!NpcDefinition.isDefined(id)
                || !NpcDefinition.forId(id).getName().toLowerCase().contains(expectedName.toLowerCase())
                || Npc.findByDefinitionId(id) != null) {
            return;
        }
        GameplayHelper.spawnNpc(id, x, y, plane, 0);
    }

    public static boolean handleDialogue(Player player, int npcId, int step, int option, int stage) {
        if (npcId == DRAUL) {
            if (step == 1) {
                if (stage == 7 && player.ownsItem(756)) {
                    player.getDialogueManager().showNpcTwoLineDialogue("What is that strange potion doing in my house?", "You had better leave my daughter alone!", 591);
                } else {
                    player.getDialogueManager().showNpcTwoLineDialogue("Why are you snooping about in my house?", "And do not encourage Juliet to see Romeo!", 591);
                }
                return true;
            }
            if (step == 2) {
                player.getDialogueManager().showPlayerOneLineDialogue("I only wanted to see Juliet.", 591);
                player.getDialogueManager().finishDialogue();
                return true;
            }
            return false;
        }
        if (npcId == PHILLIPA) {
            if (step == 1) {
                if (stage == 1) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Romeo is forever telling me how wonderful I am.", "Juliet would not be pleased to hear it!", 591);
                } else if (stage == 8) {
                    player.getDialogueManager().showNpcTwoLineDialogue("Poor Juliet is resting in the crypt.", "I hope Romeo has not forgotten her.", 591);
                } else if (stage >= 2) {
                    player.getDialogueManager().showNpcTwoLineDialogue("I hope you can help my cousin Juliet.", "Her father will never approve of Romeo.", 591);
                } else {
                    player.getDialogueManager().showNpcTwoLineDialogue("Hello, I am Phillipa, Juliet's cousin.", "I suppose you have come to see her?", 591);
                }
                return true;
            }
            if (step == 2) {
                player.getDialogueManager().showPlayerOneLineDialogue("Thank you for telling me.", 591);
                player.getDialogueManager().finishDialogue();
                return true;
            }
        }
        return false;
    }
}

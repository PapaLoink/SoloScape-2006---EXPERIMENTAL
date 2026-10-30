package com.rs2.model.quest.impl;

import com.rs2.model.cutscene.Cutscene;
import com.rs2.model.cutscene.CutsceneStep;
import com.rs2.model.npc.Npc;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;

/**
 * Server-driven scenes for Romeo & Juliet.
 *
 * Uses the existing cutscene UI and camera pipeline. These are story-beat
 * recreations, NOT frame-exact reproductions of the March 2006 camera path:
 * the original crypt's coordinates and original music track IDs are not
 * verified. In particular, the crypt is never exposed as an explorable area.
 */
public final class RomeoJulietCutscenes extends Cutscene {
    public static final int JULIET_POTION = 0;
    public static final int ROMEO_CRYPT = 1;
    private final int scene;

    public RomeoJulietCutscenes(Player player, int scene) {
        super(player, null);
        this.scene = scene;
    }

    @Override
    public void addCustomSteps() {
        addStep(new Moment(this, 3, 0));
        addStep(new Moment(this, 3, 1));
        addStep(new Moment(this, 4, 2));
        addStep(new Moment(this, 5, 3));
    }

    @Override
    public void setupScene() {
        Player player = getPlayer();
        if (scene == JULIET_POTION) {
            player.packetSender.sendCameraLookAt(3157, 3428, 350, 0, 80);
            player.packetSender.sendCameraPosition(3153, 3425, 510, 0, 80);
        } else {
            // The original crypt is cutscene-only and has no confirmed tile
            // coordinates. Do not move the player into an invented dungeon.
            player.packetSender.sendCameraLookAt(3210, 3425, 350, 0, 80);
            player.packetSender.sendCameraPosition(3206, 3421, 550, 0, 80);
        }
    }

    @Override
    public void startDialogue() {
        Player player = getPlayer();
        // Verified Jagex 2006 jingles: legacy 571/572 translate to
        // revision-443 audio groups 120/168 via AudioIds443.
        player.packetSender.sendMusicJingle(scene == JULIET_POTION ? 571 : 572, 0);
        if (scene == JULIET_POTION) {
            player.getDialogueManager().setDialogueNpcId(637);
            player.getDialogueManager().showNpcOneLineDialogue("I shall drink it now. Please tell Romeo about the crypt.", 591);
            Npc juliet = Npc.findByDefinitionId(637);
            if (juliet != null) juliet.getUpdateState().setAnimation(827);
        } else {
            player.getDialogueManager().setDialogueNpcId(639);
            player.getDialogueManager().showNpcTwoLineDialogue("You came with me, just as you promised.", "Where is Juliet? I cannot bear to look!", 591);
        }
    }

    private void moment(int index) {
        Player player = getPlayer();
        if (scene == JULIET_POTION) {
            switch (index) {
                case 0:
                    player.getDialogueManager().showOneLineStatement("Juliet drinks the Cadava potion.");
                    break;
                case 1:
                    player.getDialogueManager().showOneLineStatement("Juliet falls into a deep, death-like sleep.");
                    break;
                case 2:
                    player.getDialogueManager().showNpcTwoLineDialogue("Oh, Juliet! What have you done?", "We must make arrangements for the crypt!", 591);
                    break;
                case 3:
                    player.getDialogueManager().showOneLineStatement("You should return to Romeo with the news.");
                    break;
                default:
                    break;
            }
        } else {
            switch (index) {
                case 0:
                    player.getDialogueManager().showOneLineStatement("You and Romeo enter the crypt where Juliet rests.");
                    break;
                case 1:
                    player.getDialogueManager().showNpcOneLineDialogue("Who is this lovely lady standing here?", 591);
                    break;
                case 2:
                    player.getDialogueManager().showOneLineStatement("Romeo has noticed Phillipa, Juliet's cousin.");
                    break;
                case 3:
                    player.getDialogueManager().showNpcTwoLineDialogue("Phillipa! At last my heart has found its true love!", "Poor Juliet... now, where were we?", 591);
                    // Delay rewards until the crypt encounter's final beat.
                    if (player.getQuestState(13) == 8) {
                        QuestDefinition.getQuestScript(13).awardCompletionRewards(player);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static final class Moment extends CutsceneStep {
        private final RomeoJulietCutscenes scene;
        private final int index;

        private Moment(RomeoJulietCutscenes scene, int delayTicks, int index) {
            super(scene, delayTicks);
            this.scene = scene;
            this.index = index;
        }

        @Override
        public void executeStep() {
            scene.moment(index);
        }
    }
}

package com.rs2.model.interaction;

import com.rs2.model.EntityTargetMovement;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.interaction.FirstNpcActionTask;
import com.rs2.model.interaction.FirstObjectActionTask;
import com.rs2.model.interaction.FourthNpcActionTask;
import com.rs2.model.interaction.FourthObjectActionTask;
import com.rs2.model.interaction.InteractionType;
import com.rs2.model.interaction.ItemOnNpcTask;
import com.rs2.model.interaction.ItemOnObjectTask;
import com.rs2.model.interaction.SecondNpcActionTask;
import com.rs2.model.interaction.SecondObjectActionTask;
import com.rs2.model.interaction.SpellOnObjectTask;
import com.rs2.model.interaction.ThirdNpcActionTask;
import com.rs2.model.interaction.ThirdObjectActionTask;
import com.rs2.model.npc.Npc;
import com.rs2.model.objects.LoadedWorldObject;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.StileObject;
import com.rs2.model.objects.WorldObject;
import com.rs2.model.objects.WorldObjectLookup;
import com.rs2.model.player.PetManager;
import com.rs2.model.player.Player;
import com.rs2.model.skill.farming.FarmingPatchUtils;
import com.rs2.model.skill.magic.SpellDefinition;
import com.rs2.util.GameUtil;
import com.rs2.util.GameplayTrace;
import com.rs2.util.path.PathFinder;
import com.rs2.util.path.ProjectileCollisionMap;

public final class InteractionDispatcher {
    private static InteractionType currentInteractionType = InteractionType.FIRST_OBJECT;

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void dispatchCurrentInteraction(Player player) {
        switch (currentInteractionType) {
            case FIRST_OBJECT: {
                String text;
                Object value;
                int interactionTargetId = player.getInteractionTargetId();
                int interactionTargetX = player.getInteractionTargetX();
                int interactionTargetY = player.getInteractionTargetY();
                int interactionTargetPlane = player.getInteractionTargetPlane() % 4;
                if (ObjectDefinition.forId(interactionTargetId) != null) {
                    value = ObjectDefinition.forId(interactionTargetId);
                    text = ((ObjectDefinition)value).name.toLowerCase();
                } else {
                    text = "";
                }
                value = text;
                int value2 = player.nextActionSequence();
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("schedule first-object task player=" + GameplayTrace.describe(player) + " seq=" + value2 + " objectId=" + interactionTargetId + " name=" + value + " x=" + interactionTargetX + " y=" + interactionTargetY + " plane=" + interactionTargetPlane);
                }
                if (StileObject.isStile(interactionTargetId)) {
                    World.scheduleTickTask(new StileInteractionTask(player, value2, interactionTargetId, interactionTargetX, interactionTargetY, interactionTargetPlane));
                } else {
                    World.scheduleTickTask(new FirstObjectActionTask(1, true, player, value2, interactionTargetId, interactionTargetX, interactionTargetY, interactionTargetPlane, (String)value));
                }
                return;
            }
            case SECOND_OBJECT: {
                int interactionTargetId2 = player.getInteractionTargetId();
                int interactionTargetX2 = player.getInteractionTargetX();
                int interactionTargetY2 = player.getInteractionTargetY();
                int interactionTargetPlane2 = player.getInteractionTargetPlane();
                int value3 = player.nextActionSequence();
                World.scheduleTickTask(new SecondObjectActionTask(1, true, player, value3, interactionTargetId2, interactionTargetX2, interactionTargetY2, interactionTargetPlane2));
                return;
            }
            case THIRD_OBJECT: {
                int interactionTargetId3 = player.getInteractionTargetId();
                int interactionTargetX3 = player.getInteractionTargetX();
                int interactionTargetY3 = player.getInteractionTargetY();
                int interactionTargetPlane3 = player.getInteractionTargetPlane();
                int value4 = player.nextActionSequence();
                World.scheduleTickTask(new ThirdObjectActionTask(1, true, player, value4, interactionTargetId3, interactionTargetX3, interactionTargetY3, interactionTargetPlane3));
                return;
            }
            case FOURTH_OBJECT: {
                int interactionTargetId4 = player.getInteractionTargetId();
                int interactionTargetX4 = player.getInteractionTargetX();
                int interactionTargetY4 = player.getInteractionTargetY();
                int interactionTargetPlane4 = player.getInteractionTargetPlane();
                int value5 = player.nextActionSequence();
                World.scheduleTickTask(new FourthObjectActionTask(1, true, player, value5, interactionTargetId4, interactionTargetX4, interactionTargetY4, interactionTargetPlane4));
                return;
            }
            case FIRST_NPC: {
                int value6;
                Npc npc = World.getNpcs()[player.getInteractionTargetIndex()];
                int value7 = player.nextActionSequence();
                if (npc == null || !npc.isInteractable()) return;
                int[][] integerValues = PetManager.petItemNpcPairs;
                int index = 0;
                while (index < 6) {
                    int[] integerValues2 = integerValues[index];
                    if (player.getInteractionTargetId() == integerValues2[1]) {
                        player.getPetManager().pickupPet();
                        return;
                    }
                    ++index;
                }
                if (npc.getOwnerPlayer() != null && (npc.getOwnerPlayer() != player || npc.getCombatTarget() != null)) {
                    Player player2 = player;
                    player2.packetSender.sendGameMessage("This npc is not interested in talking with you right now.");
                    return;
                }
                if (npc.isInApeAtoll() && (value6 = GameplayHelper.getNpcShopId(player.getInteractionTargetId())) >= 0 && !player.isWearingMonkeyDisguise()) {
                    Player player3 = player;
                    player3.packetSender.sendGameMessage("This npc is not interested in talking with you right now.");
                    return;
                }
                if (npc.getNpcId() == 1469 && player.isInsideArdougneZooMonkeyEnclosure()) {
                    npc.getUpdateState().setFaceEntity(player.getEncodedIndex());
                    player.setInteractionTarget(npc);
                    player.getUpdateState().setFaceEntity(npc.getEncodedIndex());
                    DialogueManager.startDialogue(player, player.getInteractionTargetId());
                    EntityTargetMovement.clearMovementTarget(player);
                    return;
                }
                World.scheduleTickTask(new FirstNpcActionTask(1, true, player, value7, npc));
                return;
            }
            case SECOND_NPC: {
                Npc npc = World.getNpcs()[player.getInteractionTargetIndex()];
                int value8 = player.nextActionSequence();
                if (npc == null || !npc.isInteractable()) return;
                World.scheduleTickTask(new SecondNpcActionTask(1, true, player, value8, npc));
                return;
            }
            case THIRD_NPC: {
                Npc npc = World.getNpcs()[player.getInteractionTargetIndex()];
                int value9 = player.nextActionSequence();
                if (npc == null || !npc.isInteractable()) return;
                World.scheduleTickTask(new ThirdNpcActionTask(1, true, player, value9, npc));
                return;
            }
            case FOURTH_NPC: {
                Npc npc = World.getNpcs()[player.getInteractionTargetIndex()];
                int value10 = player.nextActionSequence();
                if (npc == null || !npc.isInteractable()) return;
                World.scheduleTickTask(new FourthNpcActionTask(1, true, player, value10, npc));
                return;
            }
            case ITEM_ON_OBJECT: {
                int interactionTargetX5 = player.getInteractionTargetX();
                int interactionTargetY5 = player.getInteractionTargetY();
                int interactionTargetPlane5 = player.getInteractionTargetPlane();
                int interactionTargetId5 = player.getInteractionTargetId();
                int selectedItemId = player.getSelectedItemId();
                int value11 = player.nextActionSequence();
                LoadedWorldObject loadedWorldObject = WorldObjectLookup.findObjectByIdAt(interactionTargetId5, interactionTargetX5, interactionTargetY5, interactionTargetPlane5);
                if (loadedWorldObject == null) {
                    ObjectManager.getInstance();
                    if (ObjectManager.findDynamicObjectAt(interactionTargetX5, interactionTargetY5, interactionTargetPlane5) == null) {
                        if (GameplayTrace.enabled()) {
                            GameplayTrace.log("item-on-object missing-object player=" + GameplayTrace.describe(player) + " itemId=" + selectedItemId + " objectId=" + interactionTargetId5 + " x=" + interactionTargetX5 + " y=" + interactionTargetY5 + " plane=" + interactionTargetPlane5);
                        }
                        return;
                    }
                }
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("schedule item-on-object task player=" + GameplayTrace.describe(player) + " seq=" + value11 + " itemId=" + selectedItemId + " objectId=" + interactionTargetId5 + " x=" + interactionTargetX5 + " y=" + interactionTargetY5 + " plane=" + interactionTargetPlane5);
                }
                World.scheduleTickTask(new ItemOnObjectTask(1, true, player, value11, interactionTargetId5, interactionTargetX5, interactionTargetY5, interactionTargetPlane5, selectedItemId));
                return;
            }
            case ITEM_ON_NPC: {
                int selectedItemId2 = player.getSelectedItemId();
                Npc npc = World.getNpcs()[player.getInteractionTargetIndex()];
                int value12 = player.nextActionSequence();
                int selectedItemSlot = player.getSelectedItemSlot();
                if (npc == null || !npc.isInteractable()) return;
                World.scheduleTickTask(new ItemOnNpcTask(1, true, player, value12, npc, selectedItemId2, selectedItemSlot));
                return;
            }
            case SPELL_ON_OBJECT: {
                SpellDefinition spellDefinition;
                int interactionTargetX6 = player.getInteractionTargetX();
                int interactionTargetY6 = player.getInteractionTargetY();
                int interactionTargetPlane6 = player.getInteractionTargetPlane();
                int interactionTargetId6 = player.getInteractionTargetId();
                int value13 = player.nextActionSequence();
                int interactionSpellButtonId = player.getInteractionSpellButtonId();
                LoadedWorldObject loadedWorldObject = WorldObjectLookup.findObjectByIdAt(interactionTargetId6, interactionTargetX6, interactionTargetY6, interactionTargetPlane6);
                if (loadedWorldObject == null) {
                    ObjectManager.getInstance();
                    if (ObjectManager.findDynamicObjectAt(interactionTargetX6, interactionTargetY6, interactionTargetPlane6) == null) return;
                }
                if ((spellDefinition = (SpellDefinition)((Object)player.getSpellbook().getSpellByButtonId().get(interactionSpellButtonId))) == null) return;
                World.scheduleTickTask(new SpellOnObjectTask(1, true, player, value13, interactionTargetId6, interactionTargetX6, interactionTargetY6, interactionTargetPlane6, spellDefinition));
            }
        }
    }

    public static boolean canReachObjectInteraction(Player player, WorldObject worldObject) {
        if (player == null || worldObject == null) {
            return false;
        }
        if (worldObject.getObjectId() == 2638) {
            return true;
        }

        // Objects with fixed access tiles: reachable once the player stands on the chosen tile.
        Position fixedApproach = com.rs2.model.quest.impl.Ring.ladderApproach(player,
                worldObject.getObjectId(), worldObject.getPosition().getX(), worldObject.getPosition().getY());
        if (fixedApproach == null) {
            fixedApproach = com.rs2.model.quest.impl.ClockTowerQuest.passageApproach(player,
                    worldObject.getObjectId(), worldObject.getPosition().getX(), worldObject.getPosition().getY());
        }
        if (fixedApproach == null) {
            fixedApproach = com.rs2.model.quest.impl.SheepHerderQuest.gateApproach(player,
                    worldObject.getObjectId(), worldObject.getPosition().getX(), worldObject.getPosition().getY());
        }
        if (fixedApproach != null) {
            return player.getPosition().getPlane() == worldObject.getPosition().getPlane()
                    && player.getPosition().getX() == fixedApproach.getX()
                    && player.getPosition().getY() == fixedApproach.getY();
        }

        boolean exactApproachConfigured =
                player.getInteractionTargetId() == worldObject.getObjectId()
                && player.getInteractionTargetX() == worldObject.getPosition().getX()
                && player.getInteractionTargetY() == worldObject.getPosition().getY()
                && player.getInteractionTargetPlane() == worldObject.getPosition().getPlane()
                && (player.interactionApproachX != 0
                    || player.interactionApproachY != 0);
        if (exactApproachConfigured) {
            return player.getPosition().getPlane()
                        == worldObject.getPosition().getPlane()
                    && player.getPosition().getX() == player.interactionApproachX
                    && player.getPosition().getY() == player.interactionApproachY;
        }

        Position[] farmingPatchBounds = FarmingPatchUtils.getInteractionBounds(
                worldObject.getPosition());
        if (farmingPatchBounds != null) {
            Position southWest = farmingPatchBounds[0];
            Position northEast = farmingPatchBounds[1];
            return PathFinder.hasReachedObject(
                    player,
                    southWest.getX(), southWest.getY(),
                    northEast.getX() - southWest.getX() + 1,
                    northEast.getY() - southWest.getY() + 1,
                    10, 0,
                    0);
        }

        ObjectDefinition definition = ObjectDefinition.forId(worldObject.getObjectId());
        if (definition == null) {
            return false;
        }

        return PathFinder.hasReachedObject(
                player,
                worldObject.getPosition().getX(),
                worldObject.getPosition().getY(),
                Math.max(1, definition.width),
                Math.max(1, definition.length),
                worldObject.getType(),
                worldObject.getOrientation(),
                0);
    }

    public static boolean canReachObjectInteraction(Position position, Position position2, WorldObject worldObject) {
        if (worldObject.getObjectId() == 2638) {
            return true;
        }
        ProjectileCollisionMap.removeObjectCollisionForReachability(worldObject.getObjectId(), worldObject.getPosition().getX(), worldObject.getPosition().getY(), worldObject.getPosition().getPlane(), worldObject.getOrientation(), worldObject.getType());
        boolean enabled = GameUtil.hasClearPath(position, position2, false);
        ProjectileCollisionMap.addObjectCollision(worldObject.getObjectId(), worldObject.getPosition().getX(), worldObject.getPosition().getY(), worldObject.getPosition().getPlane(), worldObject.getOrientation(), worldObject.getType(), true);
        return enabled;
    }

    public static void setCurrentInteractionType(InteractionType interactionType) {
        currentInteractionType = interactionType;
    }

    static boolean canReachObjectInteraction(Player player, Position position, WorldObject worldObject) {
        if (worldObject.getObjectId() == 2638) {
            return true;
        }
        ProjectileCollisionMap.removeObjectCollisionForReachability(worldObject.getObjectId(), worldObject.getPosition().getX(), worldObject.getPosition().getY(), worldObject.getPosition().getPlane(), worldObject.getOrientation(), worldObject.getType());
        boolean position2 = GameUtil.hasClearPath(player.getPosition(), position, false);
        ProjectileCollisionMap.addObjectCollision(worldObject.getObjectId(), worldObject.getPosition().getX(), worldObject.getPosition().getY(), worldObject.getPosition().getPlane(), worldObject.getOrientation(), worldObject.getType(), true);
        return position2;
    }
}

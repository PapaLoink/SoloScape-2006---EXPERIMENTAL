package com.rs2.net.packet.handler;

import com.rs2.ServerSettings;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.Position;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.interaction.InteractionDispatcher;
import com.rs2.model.interaction.InteractionType;
import com.rs2.model.interaction.ObjectActionRouter;
import com.rs2.model.item.ItemStack;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.objects.ObjectManager;
import com.rs2.model.objects.WorldObject;
import com.rs2.model.player.Player;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.skill.farming.FarmingPatchUtils;
import com.rs2.net.packet.ByteOrder;
import com.rs2.net.packet.ByteTransform;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.InterfaceBridge;
import com.rs2.net.packet.PacketHandler;
import com.rs2.net.packet.PacketReader;
import com.rs2.net.packet.ClientPackets;
import com.rs2.net.packet.SpellWidgets;
import com.rs2.util.GameplayTrace;
import com.rs2.util.path.PathFinder;

public final class ObjectInteractionPacketHandler
implements PacketHandler {
    @Override
    public final void handle(Player player, IncomingPacket incomingPacket) {
        if (player.isActionLocked() && !(ServerSettings.clientBuild == 443
                && incomingPacket.getOpcode() == ClientPackets.OBJECT_EXAMINE)) {
            return;
        }
        if (ServerSettings.clientBuild == 443
                && ClientPackets.getLength(incomingPacket.getOpcode())
                != ClientPackets.UNMAPPED
                && (ClientPackets.isObjectOption(incomingPacket.getOpcode())
                || incomingPacket.getOpcode() == ClientPackets.OBJECT_EXAMINE
                || incomingPacket.getOpcode() == ClientPackets.ITEM_ON_OBJECT
                || incomingPacket.getOpcode() == ClientPackets.SPELL_ON_OBJECT)) {
            handleRevision443(player, incomingPacket);
            return;
        }
        closeInterfacesUnlessTutorialOverlay(player);
        player.resetInteractionState();
        switch (incomingPacket.getOpcode()) {
            case 192: {
                player.setSelectedItemInterfaceId(incomingPacket.getReader().readSignedShort());
                player.setInteractionTargetId(incomingPacket.getReader().readSignedShort(true, ByteOrder.LITTLE));
                player.setInteractionTargetY(incomingPacket.getReader().readShort(true, ByteTransform.ADD, ByteOrder.LITTLE));
                player.setSelectedItemSlot(incomingPacket.getReader().readSignedShort(ByteOrder.LITTLE));
                player.setInteractionTargetX(incomingPacket.getReader().readShort(true, ByteTransform.ADD, ByteOrder.LITTLE));
                player.setInteractionTargetPlane(player.getPosition().getPlane());
                player.setSelectedItemId(incomingPacket.getReader().readSignedShort());
                sendInteractionDebug(player, "item");
                if (player.getSelectedItemSlot() <= 28) {
                    ItemStack itemStack = player.getInventoryManager().getContainer().getItemAt(player.getSelectedItemSlot());
                    if (itemStack == null || itemStack.getId() != player.getSelectedItemId()) {
                        if (GameplayTrace.enabled()) {
                            GameplayTrace.log("item-on-object invalid-selected-item player=" + GameplayTrace.describe(player) + " interfaceId=" + player.getSelectedItemInterfaceId() + " slot=" + player.getSelectedItemSlot() + " selectedItemId=" + player.getSelectedItemId() + " inventoryItem=" + (itemStack == null ? "null" : itemStack.getId() + ":" + itemStack.getDefinition().getName()) + " objectId=" + player.getInteractionTargetId() + " x=" + player.getInteractionTargetX() + " y=" + player.getInteractionTargetY() + " plane=" + player.getInteractionTargetPlane());
                        }
                        break;
                    }
                    InterfaceDefinition interfaceDefinition = InterfaceDefinition.forId(player.getSelectedItemInterfaceId());
                    if (GameplayTrace.enabled()) {
                        GameplayTrace.log("item-on-object decoded player=" + GameplayTrace.describe(player) + " interfaceId=" + player.getSelectedItemInterfaceId() + " slot=" + player.getSelectedItemSlot() + " itemId=" + player.getSelectedItemId() + " item=" + itemStack.getDefinition().getName() + " objectId=" + player.getInteractionTargetId() + " x=" + player.getInteractionTargetX() + " y=" + player.getInteractionTargetY() + " plane=" + player.getInteractionTargetPlane() + " interfaceOpen=" + player.isInterfaceOpen(interfaceDefinition));
                    }
                    if (player.isInterfaceOpen(interfaceDefinition)) {
                        if (player.getPlayerRights() > 1 && ServerSettings.debugModeEnabled) {
                            System.out.println("item: " + player.getSelectedItemId() + " object: " + player.getInteractionTargetId());
                        }
                        EntityTargetMovement.clearMovementTarget(player);
                        ObjectManager.prepareObjectInteractionMovement(player,
                                player.getInteractionTargetId(),
                                player.getInteractionTargetX(),
                                player.getInteractionTargetY());
                        ObjectInteractionPacketHandler.queueObjectInteractionMovement(player);
                        InteractionDispatcher.setCurrentInteractionType(InteractionType.ITEM_ON_OBJECT);
                        InteractionDispatcher.dispatchCurrentInteraction(player);
                    }
                }
                return;
            }
            case 132: {
                player.setInteractionTargetX(incomingPacket.getReader().readShort(true, ByteTransform.ADD, ByteOrder.LITTLE));
                player.setInteractionTargetId(incomingPacket.getReader().readSignedShort());
                player.setInteractionTargetY(incomingPacket.getReader().readSignedShort(ByteTransform.ADD));
                player.setInteractionTargetPlane(player.getPosition().getPlane());
                sendInteractionDebug(player, "first");
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("object first-click decoded player=" + GameplayTrace.describe(player) + " objectId=" + player.getInteractionTargetId() + " x=" + player.getInteractionTargetX() + " y=" + player.getInteractionTargetY() + " plane=" + player.getInteractionTargetPlane() + " objectType=" + SkillActionHelper.getObjectType(player.getInteractionTargetId(), player.getInteractionTargetX(), player.getInteractionTargetY(), player.getPosition().getPlane()));
                }
                if (player.getPlayerRights() > 1 && ServerSettings.debugModeEnabled) {
                    System.out.println("first click id = " + player.getInteractionTargetId() + " x = " + player.getInteractionTargetX() + " y = " + player.getInteractionTargetY() + " type " + SkillActionHelper.getObjectType(player.getInteractionTargetId(), player.getInteractionTargetX(), player.getInteractionTargetY(), player.getPosition().getPlane()));
                }
                EntityTargetMovement.clearMovementTarget(player);
                ObjectManager.prepareObjectInteractionMovement(player, player.getInteractionTargetId(), player.getInteractionTargetX(), player.getInteractionTargetY());
                ObjectInteractionPacketHandler.queueObjectInteractionMovement(player);
                InteractionDispatcher.setCurrentInteractionType(InteractionType.FIRST_OBJECT);
                InteractionDispatcher.dispatchCurrentInteraction(player);
                return;
            }
            case 252: {
                player.setInteractionTargetId(incomingPacket.getReader().readSignedShort(ByteTransform.ADD, ByteOrder.LITTLE));
                player.setInteractionTargetY(incomingPacket.getReader().readSignedShort(true, ByteOrder.LITTLE));
                player.setInteractionTargetX(incomingPacket.getReader().readSignedShort(ByteTransform.ADD));
                player.setInteractionTargetPlane(player.getPosition().getPlane());
                sendInteractionDebug(player, "second");
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("object second-click decoded player=" + GameplayTrace.describe(player) + " objectId=" + player.getInteractionTargetId() + " x=" + player.getInteractionTargetX() + " y=" + player.getInteractionTargetY() + " plane=" + player.getInteractionTargetPlane());
                }
                if (player.getPlayerRights() > 1 && ServerSettings.debugModeEnabled) {
                    System.out.println("second click id = " + player.getInteractionTargetId() + " x = " + player.getInteractionTargetX() + " y = " + player.getInteractionTargetY());
                }
                EntityTargetMovement.clearMovementTarget(player);
                ObjectManager.prepareObjectInteractionMovement(player, player.getInteractionTargetId(), player.getInteractionTargetX(), player.getInteractionTargetY());
                ObjectInteractionPacketHandler.queueObjectInteractionMovement(player);
                InteractionDispatcher.setCurrentInteractionType(InteractionType.SECOND_OBJECT);
                InteractionDispatcher.dispatchCurrentInteraction(player);
                return;
            }
            case 70: {
                player.setInteractionTargetX(incomingPacket.getReader().readSignedShort(true, ByteOrder.LITTLE));
                player.setInteractionTargetY(incomingPacket.getReader().readSignedShort());
                player.setInteractionTargetId(incomingPacket.getReader().readSignedShort(ByteTransform.ADD, ByteOrder.LITTLE));
                player.setInteractionTargetPlane(player.getPosition().getPlane());
                sendInteractionDebug(player, "third");
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("object third-click decoded player=" + GameplayTrace.describe(player) + " objectId=" + player.getInteractionTargetId() + " x=" + player.getInteractionTargetX() + " y=" + player.getInteractionTargetY() + " plane=" + player.getInteractionTargetPlane());
                }
                if (player.getPlayerRights() > 1 && ServerSettings.debugModeEnabled) {
                    System.out.println("third click id = " + player.getInteractionTargetId() + " x = " + player.getInteractionTargetX() + " y = " + player.getInteractionTargetY());
                }
                EntityTargetMovement.clearMovementTarget(player);
                ObjectManager.prepareObjectInteractionMovement(player, player.getInteractionTargetId(), player.getInteractionTargetX(), player.getInteractionTargetY());
                ObjectInteractionPacketHandler.queueObjectInteractionMovement(player);
                InteractionDispatcher.setCurrentInteractionType(InteractionType.THIRD_OBJECT);
                InteractionDispatcher.dispatchCurrentInteraction(player);
                return;
            }
            case 234: {
                player.setInteractionTargetX(incomingPacket.getReader().readSignedShort(ByteTransform.ADD, ByteOrder.LITTLE));
                player.setInteractionTargetId(incomingPacket.getReader().readSignedShort(ByteTransform.ADD));
                player.setInteractionTargetY(incomingPacket.getReader().readSignedShort(ByteTransform.ADD, ByteOrder.LITTLE));
                player.setInteractionTargetPlane(player.getPosition().getPlane());
                sendInteractionDebug(player, "fourth");
                if (GameplayTrace.enabled()) {
                    GameplayTrace.log("object fourth-click decoded player=" + GameplayTrace.describe(player) + " objectId=" + player.getInteractionTargetId() + " x=" + player.getInteractionTargetX() + " y=" + player.getInteractionTargetY() + " plane=" + player.getInteractionTargetPlane());
                }
                if (player.getPlayerRights() > 1 && ServerSettings.debugModeEnabled) {
                    System.out.println("fourth click id = " + player.getInteractionTargetId() + " x = " + player.getInteractionTargetX() + " y = " + player.getInteractionTargetY());
                }
                EntityTargetMovement.clearMovementTarget(player);
                ObjectManager.prepareObjectInteractionMovement(player, player.getInteractionTargetId(), player.getInteractionTargetX(), player.getInteractionTargetY());
                ObjectInteractionPacketHandler.queueObjectInteractionMovement(player);
                InteractionDispatcher.setCurrentInteractionType(InteractionType.FOURTH_OBJECT);
                InteractionDispatcher.dispatchCurrentInteraction(player);
                return;
            }
            case 35: {
                PacketReader packetReader = incomingPacket.getReader();
                int value = packetReader.readSignedShort(ByteOrder.LITTLE);
                int value2 = packetReader.readSignedShort(ByteTransform.ADD, ByteOrder.BIG);
                int value3 = packetReader.readSignedShort(ByteTransform.ADD, ByteOrder.BIG);
                int value4 = packetReader.readSignedShort(ByteOrder.LITTLE);
                player.setInteractionTargetX(value);
                player.setInteractionTargetId(value4);
                player.setInteractionTargetY(value3);
                player.setInteractionTargetPlane(player.getPosition().getPlane());
                player.setInteractionSpellButtonId(value2);
                sendInteractionDebug(player, "spell");
                if (!SkillActionHelper.isObjectPresent(value4, value, value3, player.getPosition().getPlane())) break;
                EntityTargetMovement.clearMovementTarget(player);
                ObjectManager.prepareObjectInteractionMovement(player, player.getInteractionTargetId(), player.getInteractionTargetX(), player.getInteractionTargetY());
                ObjectInteractionPacketHandler.queueObjectInteractionMovement(player);
                InteractionDispatcher.setCurrentInteractionType(InteractionType.SPELL_ON_OBJECT);
                InteractionDispatcher.dispatchCurrentInteraction(player);
            }
        }
    }

    private static void handleRevision443(Player player, IncomingPacket packet) {
        int opcode = packet.getOpcode();
        int option = ClientPackets.getObjectOption(opcode);
        if (option != -1) {
            int objectId;
            int x;
            int y;
            switch (option) {
                case 1:
                    y = packet.getReader().readSignedShort(ByteTransform.ADD);
                    objectId = packet.getReader().readSignedShort(ByteTransform.ADD, ByteOrder.LITTLE);
                    x = packet.getReader().readSignedShort(ByteTransform.ADD);
                    break;
                case 2:
                    x = packet.getReader().readSignedShort();
                    objectId = packet.getReader().readSignedShort();
                    y = packet.getReader().readSignedShort(ByteTransform.ADD);
                    break;
                case 3:
                    objectId = packet.getReader().readSignedShort(ByteOrder.LITTLE);
                    y = packet.getReader().readSignedShort();
                    x = packet.getReader().readSignedShort();
                    break;
                case 4:
                    objectId = packet.getReader().readSignedShort();
                    x = packet.getReader().readSignedShort(ByteTransform.ADD);
                    y = packet.getReader().readSignedShort(ByteTransform.ADD, ByteOrder.LITTLE);
                    break;
                default:
                    y = packet.getReader().readSignedShort();
                    x = packet.getReader().readSignedShort();
                    objectId = packet.getReader().readSignedShort();
                    break;
            }
            closeInterfacesUnlessTutorialOverlay(player);
            player.resetInteractionState();
            player.setInteractionTargetId(objectId & 0xFFFF);
            player.setInteractionTargetX(x & 0xFFFF);
            player.setInteractionTargetY(y & 0xFFFF);
            player.setInteractionTargetPlane(player.getPosition().getPlane());
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("443 object-option-" + option + " decoded player="
                        + GameplayTrace.describe(player) + " objectId=" + (objectId & 0xFFFF)
                        + " x=" + (x & 0xFFFF) + " y=" + (y & 0xFFFF));
            }
            EntityTargetMovement.clearMovementTarget(player);
            ObjectManager.prepareObjectInteractionMovement(player, player.getInteractionTargetId(),
                    player.getInteractionTargetX(), player.getInteractionTargetY());
            queueObjectInteractionMovement(player);
            InteractionType type = ObjectActionRouter.semanticRoute(ObjectDefinition.forId(objectId & 0xFFFF), option - 1);
            GameplayTrace.logInteraction(player, "object-action route id=" + (objectId & 0xFFFF)
                    + " option=" + option + " semantic=" + type);
            if (type == null) type = ObjectActionRouter.legacyRoute(option);
            if (type != null) {
                InteractionDispatcher.setCurrentInteractionType(type);
                InteractionDispatcher.dispatchCurrentInteraction(player);
            } else if (player.isInteractionDebugEnabled()) {
                player.packetSender.sendGameMessage("443 object option 5 decoded: id="
                        + player.getInteractionTargetId());
            }
            return;
        }

        if (opcode == ClientPackets.OBJECT_EXAMINE) {
            int objectId = packet.getReader().readSignedShort(ByteOrder.LITTLE) & 0xFFFF;
            ObjectDefinition definition = ObjectDefinition.forId(objectId);
            if (definition == null) return;
            player.packetSender.sendGameMessage(definition.getDescription());
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("443 object examine player=" + GameplayTrace.describe(player)
                        + " objectId=" + objectId);
            }
            if (player.isInteractionDebugEnabled()) {
                player.packetSender.sendGameMessage("443 examine object: " + objectId);
            }
            return;
        }

        if (opcode == ClientPackets.ITEM_ON_OBJECT) {
            int y = packet.getReader().readSignedShort(ByteTransform.ADD, ByteOrder.LITTLE) & 0xFFFF;
            int objectId = packet.getReader().readSignedShort(ByteOrder.LITTLE) & 0xFFFF;
            int itemId = packet.getReader().readSignedShort(ByteTransform.ADD) & 0xFFFF;
            int slot = packet.getReader().readSignedShort(ByteOrder.LITTLE) & 0xFFFF;
            int x = packet.getReader().readSignedShort(ByteTransform.ADD, ByteOrder.LITTLE) & 0xFFFF;
            int packedInterface = ClientPackets.readIntLittle(packet.getReader());
            if (slot >= 28) return;
            ItemStack item = player.getInventoryManager().getContainer().getItemAt(slot);
            if (item == null || item.getId() != itemId) return;
            closeInterfacesUnlessTutorialOverlay(player);
            player.resetInteractionState();
            player.setSelectedItemInterfaceId(packedInterface);
            player.setSelectedItemSlot(slot);
            player.setSelectedItemId(itemId);
            player.setInteractionTargetId(objectId);
            player.setInteractionTargetX(x);
            player.setInteractionTargetY(y);
            player.setInteractionTargetPlane(player.getPosition().getPlane());
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("443 item-on-object decoded player=" + GameplayTrace.describe(player)
                        + " interface=" + packedInterface + " slot=" + slot + " itemId=" + itemId
                        + " objectId=" + objectId + " x=" + x + " y=" + y);
            }
            EntityTargetMovement.clearMovementTarget(player);
            ObjectManager.prepareObjectInteractionMovement(player, objectId, x, y);
            queueObjectInteractionMovement(player);
            InteractionDispatcher.setCurrentInteractionType(InteractionType.ITEM_ON_OBJECT);
            InteractionDispatcher.dispatchCurrentInteraction(player);
            return;
        }

        if (opcode == ClientPackets.SPELL_ON_OBJECT) {
            int spellInterface = ClientPackets.readIntInverseMiddle(packet.getReader());
            int x = packet.getReader().readSignedShort() & 0xFFFF;
            int spellChild = packet.getReader().readSignedShort() & 0xFFFF;
            int y = packet.getReader().readSignedShort(ByteOrder.LITTLE) & 0xFFFF;
            int objectId = packet.getReader().readSignedShort(ByteTransform.ADD) & 0xFFFF;
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("443 spell-on-object decoded player=" + GameplayTrace.describe(player)
                        + " spell=" + spellInterface + ":" + spellChild
                        + " objectId=" + objectId + " x=" + x + " y=" + y);
            }
            if (player.isInteractionDebugEnabled()) {
                player.packetSender.sendGameMessage("443 spell-on-object decoded: spell="
                        + spellInterface + ":" + spellChild + " object=" + objectId);
            }
            int legacySpellButton = SpellWidgets.toLegacySpellButton(spellInterface, spellChild);
            if (!SpellWidgets.isSpellWidget(spellInterface)
                    || legacySpellButton == InterfaceBridge.UNMAPPED
                    || !SkillActionHelper.isObjectPresent(objectId, x, y,
                    player.getPosition().getPlane())) return;
            closeInterfacesUnlessTutorialOverlay(player);
            player.resetInteractionState();
            player.setInteractionTargetX(x);
            player.setInteractionTargetY(y);
            player.setInteractionTargetId(objectId);
            player.setInteractionTargetPlane(player.getPosition().getPlane());
            player.setInteractionSpellButtonId(legacySpellButton);
            EntityTargetMovement.clearMovementTarget(player);
            ObjectManager.prepareObjectInteractionMovement(player, objectId, x, y);
            queueObjectInteractionMovement(player);
            InteractionDispatcher.setCurrentInteractionType(InteractionType.SPELL_ON_OBJECT);
            InteractionDispatcher.dispatchCurrentInteraction(player);
        }
    }

    private static void closeInterfacesUnlessTutorialOverlay(Player player) {
        if (player.getQuestState(0) != 1) {
            return;
        }
        player.packetSender.closeInterfaces();
    }

    private static void sendInteractionDebug(Player player, String action) {
        if (!player.isInteractionDebugEnabled()) {
            return;
        }
        int objectId = player.getInteractionTargetId();
        int objectX = player.getInteractionTargetX();
        int objectY = player.getInteractionTargetY();
        int plane = player.getInteractionTargetPlane();
        int type = SkillActionHelper.getObjectType(objectId, objectX, objectY, plane);
        player.getPacketSender().sendGameMessage(
                "Debug " + action + ": id=" + objectId
                + " x=" + objectX + " y=" + objectY
                + " plane=" + plane + " type=" + type);
    }

    public static boolean queueObjectInteractionMovement(Player player) {
        int objectId = player.getInteractionTargetId();
        int objectX = player.getInteractionTargetX();
        int objectY = player.getInteractionTargetY();
        int plane = player.getInteractionTargetPlane();

        if (!SkillActionHelper.isObjectPresent(objectId, objectX, objectY, plane)) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("object movement skipped missing-object player="
                        + GameplayTrace.describe(player)
                        + " objectId=" + objectId
                        + " x=" + objectX + " y=" + objectY + " plane=" + plane);
            }
            return false;
        }

        WorldObject worldObject = SkillActionHelper.findWorldObjectById(
                objectId, objectX, objectY, plane);
        if (worldObject == null) {
            return false;
        }

        return queueObjectInteractionMovement(player, worldObject);
    }

    public static boolean queueObjectInteractionMovement(Player player, WorldObject worldObject) {
        if (player == null || worldObject == null) {
            return false;
        }

        int objectId = worldObject.getObjectId();
        int objectX = worldObject.getPosition().getX();
        int objectY = worldObject.getPosition().getY();
        int plane = worldObject.getPosition().getPlane();

        Position castleWarsSideDoorApproach =
                CastleWarsEngineeringManager.getSideDoorInteractionApproach(
                        player, objectId, objectX, objectY);
        if (castleWarsSideDoorApproach != null) {
            return PathFinder.findPath(player,
                    castleWarsSideDoorApproach.getX(),
                    castleWarsSideDoorApproach.getY(),
                    false, 1, 1);
        }

        Position castleWarsMainDoorApproach =
                CastleWarsEngineeringManager.getMainDoorInteractionApproach(
                        player, objectId, objectX, objectY);
        if (castleWarsMainDoorApproach != null) {
            return PathFinder.findPath(player,
                    castleWarsMainDoorApproach.getX(),
                    castleWarsMainDoorApproach.getY(),
                    false, 1, 1);
        }

        Position castleWarsStairApproach =
                CastleWarsManager.getStairTraversalApproach(
                        player, objectId, objectX, objectY);
        if (castleWarsStairApproach != null) {
            boolean foundPath = PathFinder.findPath(player,
                    castleWarsStairApproach.getX(),
                    castleWarsStairApproach.getY(),
                    false, 1, 1);
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("castle-wars stair movement queued player="
                        + GameplayTrace.describe(player)
                        + " objectId=" + objectId
                        + " object=" + objectX + "," + objectY + "," + plane
                        + " approach=" + GameplayTrace.position(castleWarsStairApproach)
                        + " path=" + foundPath
                        + " steps=" + player.getMovementQueue().getSteps().size());
            }
            return foundPath;
        }

        Position questStairApproach = com.rs2.model.quest.impl.PlagueCityQuest.stairApproach(
                player, objectId, objectX, objectY);
        if (questStairApproach == null) {
            questStairApproach = com.rs2.model.quest.impl.Ring.ladderApproach(
                    player, objectId, objectX, objectY);
        }
        if (questStairApproach == null) {
            questStairApproach = com.rs2.model.quest.impl.ClockTowerQuest.passageApproach(
                    player, objectId, objectX, objectY);
        }
        if (questStairApproach != null) {
            return PathFinder.findPath(player,
                    questStairApproach.getX(), questStairApproach.getY(),
                    false, 1, 1);
        }

        boolean exactApproachConfigured =
                player.getInteractionTargetId() == objectId
                && player.getInteractionTargetX() == objectX
                && player.getInteractionTargetY() == objectY
                && player.getInteractionTargetPlane() == plane
                && player.getPosition().getPlane() == plane
                && (player.interactionApproachX != 0
                    || player.interactionApproachY != 0);
        if (exactApproachConfigured) {
            boolean foundPath = PathFinder.findPath(player,
                    player.interactionApproachX,
                    player.interactionApproachY,
                    false, 1, 1);
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("object exact-approach movement queued player="
                        + GameplayTrace.describe(player)
                        + " objectId=" + objectId
                        + " object=" + objectX + "," + objectY + "," + plane
                        + " approach=" + player.interactionApproachX + ","
                        + player.interactionApproachY + "," + plane
                        + " path=" + foundPath
                        + " steps=" + player.getMovementQueue().getSteps().size());
            }
            return foundPath;
        }

        Position[] farmingPatchBounds = FarmingPatchUtils.getInteractionBounds(
                worldObject.getPosition());
        if (farmingPatchBounds != null) {
            Position southWest = farmingPatchBounds[0];
            Position northEast = farmingPatchBounds[1];
            return PathFinder.findPathToObject(
                    player,
                    southWest.getX(), southWest.getY(),
                    northEast.getX() - southWest.getX() + 1,
                    northEast.getY() - southWest.getY() + 1,
                    10, 0,
                    0,
                    false);
        }

        ObjectDefinition definition = ObjectDefinition.forId(objectId);
        if (definition == null) {
            return false;
        }

        int orientation = worldObject.getOrientation();
        int type = worldObject.getType();
        int width = Math.max(1, definition.width);
        int length = Math.max(1, definition.length);

        boolean foundPath = PathFinder.findPathToObject(
                player,
                objectX, objectY,
                width, length,
                type, orientation,
                0,
                false);

        if (GameplayTrace.enabled()) {
            int routedWidth = definition.getWidthForOrientation(orientation);
            int routedLength = definition.getLengthForOrientation(orientation);
            GameplayTrace.log("object movement queued player="
                    + GameplayTrace.describe(player)
                    + " objectId=" + objectId
                    + " x=" + objectX + " y=" + objectY + " plane=" + plane
                    + " type=" + type + " orientation=" + orientation
                    + " size=" + routedWidth + "x" + routedLength
                    + " path=" + foundPath
                    + " steps=" + player.getMovementQueue().getSteps().size());
        }

        return foundPath;
    }

}

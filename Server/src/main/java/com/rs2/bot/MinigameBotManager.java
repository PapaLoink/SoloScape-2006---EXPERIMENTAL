package com.rs2.bot;

import com.rs2.bot.combat.BotCombatHelper;
import com.rs2.bot.combat.BotCombatLoadoutManager;
import com.rs2.bot.combat.BotPvpCombatHandler;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.Player;
import com.rs2.util.GameUtil;
import com.rs2.util.path.PathFinder;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public final class MinigameBotManager {
    private static final Position CASTLE_WARS_LOBBY = new Position(2440, 3089, 0);
    private static final Set<BotPlayer> spreadWaitingBots =
        Collections.newSetFromMap(new IdentityHashMap<BotPlayer, Boolean>());
    private static final Map<BotPlayer, WaitingSocialState> waitingSocialStates =
        new IdentityHashMap<BotPlayer, WaitingSocialState>();

    private MinigameBotManager() {
    }

    public static void startMinigameBot(BotPlayer botPlayer) {
        randomizeCombatLevels(botPlayer);
        preparePvpLoadout(botPlayer);
        botPlayer.setAutoRetaliate(true);
        placeAtCastleWarsLobby(botPlayer);
        CastleWarsManager.leaveWaitingRoom(botPlayer);
    }

    public static void placeAtCastleWarsLobby(BotPlayer botPlayer) {
        botPlayer.moveTo(new Position(
            CASTLE_WARS_LOBBY.getX(),
            CASTLE_WARS_LOBBY.getY(),
            CASTLE_WARS_LOBBY.getPlane()
        ));
    }

    public static void processMinigameBot(BotPlayer botPlayer) {
        joinCastleWars(botPlayer);

        if (!botPlayer.isRegistered() || botPlayer.isDead()) {
            clearWaitingSocial(botPlayer);
            return;
        }
        if (!CastleWarsManager.isWaitingPlayer(botPlayer) && !CastleWarsManager.isInGame(botPlayer)) {
            spreadWaitingBots.remove(botPlayer);
            clearWaitingSocial(botPlayer);
            CastleWarsBotAi.clear(botPlayer);
            CastleWarsBotRoleAi.clear(botPlayer);
            return;
        }

        if (CastleWarsManager.isInGame(botPlayer)) {
            BotCombatHelper.syncPrimaryMagicAutocast(botPlayer);
            spreadWaitingBots.remove(botPlayer);
            clearWaitingSocial(botPlayer);
            if (CastleWarsBotRoleAi.process(botPlayer)) {
                CastleWarsBotAi.clear(botPlayer);
            } else {
                CastleWarsBotAi.process(botPlayer);
            }
        } else {
            CastleWarsBotAi.clear(botPlayer);
            CastleWarsBotRoleAi.clear(botPlayer);
            processWaitingRoomSocial(botPlayer);
        }

        // This task runs every two game ticks. A 1/180 roll keeps a large
        // Castle Wars population chatty without flooding public chat.
        if (GameUtil.randomInt(180) != 0) {
            return;
        }

        CastleWarsManager.Team team = CastleWarsManager.getTeam(botPlayer);
        if (GameUtil.randomInt(5) == 0 && team != null) {
            CastleWarsBotChat.sayTeam(botPlayer, team);
            return;
        }

        CastleWarsBotChat.sayContextual(botPlayer);
    }

    public static boolean joinCastleWars(BotPlayer botPlayer) {
        if (!botPlayer.isRegistered()) {
            return false;
        }
        if (CastleWarsManager.isInGame(botPlayer)) {
            spreadWaitingBots.remove(botPlayer);
            clearWaitingSocial(botPlayer);
            return true;
        }
        if (CastleWarsManager.isWaitingPlayer(botPlayer)) {
            if (spreadWaitingBots.add(botPlayer)) {
                CastleWarsManager.spreadWaitingPlayer(botPlayer);
            }
            return true;
        }

        prepareCastleWarsEquipment(botPlayer);
        placeAtCastleWarsLobby(botPlayer);
        boolean handled = CastleWarsManager.handleLobbyPortal(botPlayer, CastleWarsManager.GUTHIX_PORTAL_ID);
        if (handled && CastleWarsManager.isWaitingPlayer(botPlayer)) {
            CastleWarsManager.spreadWaitingPlayer(botPlayer);
            spreadWaitingBots.add(botPlayer);
        }
        return handled;
    }

    private static void preparePvpLoadout(BotPlayer botPlayer) {
        BotCombatLoadoutManager.selectCombatStyleFromStats(botPlayer, false);
        BotCombatLoadoutManager.prepareMinigameCombatLoadout(botPlayer);
        prepareCastleWarsEquipment(botPlayer);
        prepareCastleWarsCombatStacks(botPlayer);
        botPlayer.getInventoryManager().refresh();
        botPlayer.getEquipmentManager().refresh();
        botPlayer.getUpdateState().setUpdateRequired(true);
        botPlayer.setAppearanceUpdateRequired(true);
    }

    private static void prepareCastleWarsCombatStacks(BotPlayer botPlayer) {
        if (botPlayer.botPrimaryCombatStyle == BotPvpCombatHandler.RANGED_COMBAT_STYLE) {
            // Refill the compatible arrows chosen with the bow. Rune arrows
            // cannot be fired by the lower-tier bows used by low-level bots.
            ItemStack arrows = botPlayer.getEquipmentManager().getContainer().getItemAt(13);
            if (arrows != null) {
                botPlayer.getEquipmentManager().getContainer().setItem(
                        13, new ItemStack(arrows.getId(), Integer.MAX_VALUE));
            }
            return;
        }
        if (botPlayer.botPrimaryCombatStyle != BotPvpCombatHandler.MAGIC_COMBAT_STYLE) {
            return;
        }

        // Core rune set used by standard and Ancient combat magic.
        int[] runeIds = new int[]{
                554, 555, 556, 557, 558, 559, 560,
                561, 562, 563, 564, 565, 566
        };
        for (int runeId : runeIds) {
            setInventoryStackToMax(botPlayer, runeId);
        }
        BotCombatHelper.syncPrimaryMagicAutocast(botPlayer);

        // Astral runes exist only in later compatible caches.
        if (ItemDefinition.isDefined(9075)) {
            setInventoryStackToMax(botPlayer, 9075);
        }
    }

    private static void setInventoryStackToMax(BotPlayer botPlayer, int itemId) {
        int slot = botPlayer.getInventoryManager().getContainer().indexOfItem(itemId);
        if (slot < 0) {
            slot = botPlayer.getInventoryManager().getContainer().getFirstFreeSlot();
        }
        if (slot >= 0) {
            botPlayer.getInventoryManager().getContainer().setItem(
                    slot, new ItemStack(itemId, Integer.MAX_VALUE));
        }
    }

    private static void prepareCastleWarsEquipment(BotPlayer botPlayer) {
        boolean equipmentChanged = false;
        if (botPlayer.getEquipmentManager().getContainer().getItemAt(0) != null) {
            botPlayer.getEquipmentManager().getContainer().setItem(0, null);
            equipmentChanged = true;
        }
        if (botPlayer.getEquipmentManager().getContainer().getItemAt(1) != null) {
            botPlayer.getEquipmentManager().getContainer().setItem(1, null);
            equipmentChanged = true;
        }

        boolean inventoryChanged = false;
        ItemStack[] inventoryItems = botPlayer.getInventoryManager().getContainer().getItems();
        for (int slot = 0; slot < inventoryItems.length; ++slot) {
            ItemStack item = inventoryItems[slot];
            if (!CastleWarsManager.isAllowedLobbyPortalInventoryItem(botPlayer, item)) {
                botPlayer.getInventoryManager().getContainer().setItem(slot, null);
                inventoryChanged = true;
            }
        }

        if (inventoryChanged) {
            botPlayer.getInventoryManager().refresh();
        }
        if (equipmentChanged) {
            botPlayer.getEquipmentManager().refresh();
        }
        botPlayer.setAppearanceUpdateRequired(true);
    }

    private static void processWaitingRoomSocial(BotPlayer botPlayer) {
        WaitingSocialState state = waitingSocialStates.get(botPlayer);
        if (state == null) {
            state = new WaitingSocialState();
            waitingSocialStates.put(botPlayer, state);
        }

        if (state.followTicks > 0) {
            if (!isValidWaitingSocialTarget(botPlayer, state.followTarget, 10)) {
                stopWaitingFollow(botPlayer, state);
                return;
            }

            if (botPlayer.getMovementTarget() != state.followTarget) {
                botPlayer.setMovementTarget(state.followTarget);
            }
            --state.followTicks;
            if (state.followTicks <= 0) {
                stopWaitingFollow(botPlayer, state);
            }
            return;
        }

        if (state.actionDelay > 0) {
            --state.actionDelay;
            return;
        }
        state.actionDelay = 8 + GameUtil.randomInt(20);

        int action = GameUtil.randomInt(100);
        if (action < 40) {
            wanderWaitingRoom(botPlayer);
            return;
        }
        if (action < 70) {
            faceWaitingPlayer(botPlayer);
            return;
        }
        if (action < 83) {
            startWaitingFollow(botPlayer, state);
            return;
        }
        if (action < 90) {
            CastleWarsBotChat.sayContextual(botPlayer);
        }
    }

    private static void wanderWaitingRoom(BotPlayer botPlayer) {
        Position target = CastleWarsManager.getWaitingRoomWanderTarget(botPlayer, 4);
        if (target == null) {
            return;
        }
        botPlayer.getMovementQueue().setRunning(GameUtil.randomInt(8) == 0);
        PathFinder.findPath(botPlayer, target.getX(), target.getY(), true, 0, 0);
        botPlayer.getMovementQueue().clearMovementActions();
    }

    private static void faceWaitingPlayer(BotPlayer botPlayer) {
        Player target = findNearbyWaitingPlayer(botPlayer, 6, false);
        if (target == null) {
            return;
        }
        botPlayer.getUpdateState().setFacePosition(target.getPosition());
        if (target instanceof BotPlayer && GameUtil.randomInt(2) == 0) {
            target.getUpdateState().setFacePosition(botPlayer.getPosition());
        }
    }

    private static void startWaitingFollow(BotPlayer botPlayer, WaitingSocialState state) {
        Player target = findNearbyWaitingPlayer(botPlayer, 6, true);
        if (target == null) {
            return;
        }
        state.followTarget = target;
        state.followTicks = 2 + GameUtil.randomInt(4);
        botPlayer.getMovementQueue().setRunning(false);
        botPlayer.setMovementTarget(target);
    }

    private static void stopWaitingFollow(BotPlayer botPlayer, WaitingSocialState state) {
        if (state.followTarget != null && botPlayer.getMovementTarget() == state.followTarget) {
            EntityTargetMovement.clearMovementTarget(botPlayer);
            botPlayer.getMovementQueue().clear();
        }
        state.followTarget = null;
        state.followTicks = 0;
        state.actionDelay = 6 + GameUtil.randomInt(12);
    }

    private static Player findNearbyWaitingPlayer(BotPlayer botPlayer, int maxDistance, boolean botsOnly) {
        CastleWarsManager.Team team = CastleWarsManager.getWaitingTeam(botPlayer);
        if (team == null) {
            return null;
        }

        Player selected = null;
        int candidates = 0;
        for (Player player : World.getPlayers()) {
            if (player == null
                    || player == botPlayer
                    || !player.isRegistered()
                    || player.isDead()
                    || (botsOnly && !(player instanceof BotPlayer))
                    || player.getPosition().getPlane() != botPlayer.getPosition().getPlane()
                    || CastleWarsManager.getWaitingTeam(player) != team
                    || !GameUtil.isWithinDistance(botPlayer.getPosition(), player.getPosition(), maxDistance)) {
                continue;
            }

            ++candidates;
            if (GameUtil.randomInt(candidates) == 0) {
                selected = player;
            }
        }
        return selected;
    }

    private static boolean isValidWaitingSocialTarget(BotPlayer botPlayer, Player target, int maxDistance) {
        CastleWarsManager.Team team = CastleWarsManager.getWaitingTeam(botPlayer);
        return target != null
                && target.isRegistered()
                && !target.isDead()
                && team != null
                && CastleWarsManager.getWaitingTeam(target) == team
                && target.getPosition().getPlane() == botPlayer.getPosition().getPlane()
                && GameUtil.isWithinDistance(botPlayer.getPosition(), target.getPosition(), maxDistance);
    }

    private static void clearWaitingSocial(BotPlayer botPlayer) {
        WaitingSocialState state = waitingSocialStates.remove(botPlayer);
        if (state == null) {
            return;
        }
        if (botPlayer.getMovementTarget() != null && botPlayer.getCombatTarget() == null) {
            EntityTargetMovement.clearMovementTarget(botPlayer);
        }
        botPlayer.getMovementQueue().clear();
    }

    private static void randomizeCombatLevels(BotPlayer botPlayer) {
        BotCombatHelper.setBotSkillLevel(botPlayer, 0, 10 + GameUtil.randomInt(90));
        BotCombatHelper.setBotSkillLevel(botPlayer, 1, 10 + GameUtil.randomInt(90));
        BotCombatHelper.setBotSkillLevel(botPlayer, 2, 10 + GameUtil.randomInt(90));
        BotCombatHelper.setBotSkillLevel(botPlayer, 3, 30 + GameUtil.randomInt(70));
        BotCombatHelper.setBotSkillLevel(botPlayer, 4, 10 + GameUtil.randomInt(90));
        BotCombatHelper.setBotSkillLevel(botPlayer, 5, 1 + GameUtil.randomInt(99));
        BotCombatHelper.setBotSkillLevel(botPlayer, 6, 10 + GameUtil.randomInt(90));
        botPlayer.getSkillManager().refreshAllSkills();
    }

    private static final class WaitingSocialState {
        private int actionDelay = 4 + GameUtil.randomInt(12);
        private Player followTarget;
        private int followTicks;
    }
}

package com.rs2.model.interaction;

import com.rs2.ServerSettings;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.GameplayHelper;
import com.rs2.model.World;
import com.rs2.model.combat.CombatAction;
import com.rs2.model.c.ProjectileDefinition;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.gameplay.magetrainingarena.MageTrainingArenaRewardShop;
import com.rs2.model.npc.Npc;
import com.rs2.model.player.BankManager;
import com.rs2.model.player.GrandExchangeManager;
import com.rs2.model.player.Player;
import com.rs2.model.shop.ShopManager;
import com.rs2.model.skill.fishing.FishingSpotDefinition;
import com.rs2.model.skill.runecrafting.RunecraftingHandler;
import com.rs2.model.task.TickTask;
import com.rs2.util.GameUtil;
import com.rs2.util.GameplayTrace;

/** One pending, cancellable NPC operation with the original selected action attached. */
public final class RoutedNpcActionTask extends TickTask {
    private final Player player;
    private final int sequence;
    private final Npc npc;
    private final int npcId;
    private final int slot;
    private final NpcActionRouter.Action action;

    public RoutedNpcActionTask(Player player, int sequence, Npc npc, int slot, NpcActionRouter.Action action) {
        super(1, true);
        this.player = player;
        this.sequence = sequence;
        this.npc = npc;
        this.npcId = npc.getNpcId();
        this.slot = slot;
        this.action = action;
    }

    @Override
    public void execute() {
        if (!player.isCurrentActionSequence(sequence) || npc.isDead() || !npc.isInteractable()
                || npc.getIndex() < 0 || npc.getIndex() >= World.getNpcs().length
                || World.getNpcs()[npc.getIndex()] != npc || npc.getNpcId() != npcId
                || player.getPosition().getPlane() != npc.getPosition().getPlane()
                || NpcActionRouter.resolve(npc.getDefinition(), slot) != action) {
            stop();
            return;
        }
        if (npc.getOwnerPlayer() != null && npc.getOwnerPlayer() != player) {
            reject("This npc is not interested in interacting with you right now.");
            return;
        }
        if (action == NpcActionRouter.Action.PELT) {
            player.setInteractionTarget(npc);
            com.rs2.model.quest.event.GublinchChristmasEvent.startPelting(player, npc);
            stop();
            return;
        }
        if (player.isOverlapping(npc)) return;
        boolean bankCounter = action == NpcActionRouter.Action.BANK && npc.isBanker();
        // Fishing spots occupy water, so they do not require a walkable NPC tile.
        // Counter bankers retain their existing facing-position rule.
        boolean withinReach = bankCounter ? npc.isFacingInteractionPosition(player.getPosition(), 2)
                : player.isWithinReach(npc, 1);
        boolean needsWalkablePath = !bankCounter && action != NpcActionRouter.Action.FISH;
        if (!withinReach || needsWalkablePath
                && !GameUtil.hasClearPath(player.getPosition(), npc.getPosition(), true)) {
            return;
        }
        EntityTargetMovement.clearMovementTarget(player);
        player.setInteractionTarget(npc);
        player.getUpdateState().setFaceEntity(npc.getEncodedIndex());
        npc.getUpdateState().setFaceEntity(player.getEncodedIndex());
        switch (action) {
            case BANK:
                BankManager.openBank(player);
                break;
            case TRADE:
                if (npc.isInApeAtoll() && !player.isWearingMonkeyDisguise()) {
                    reject("This npc is not interested in talking with you right now.");
                    return;
                }
                if ((npcId == 836 || npcId == 2257) && !allowMembers()) return;
                if (npcId == CastleWarsManager.LANTHUS_NPC_ID) {
                    ShopManager.openCastleWarsRewardShop(player);
                } else if (!GameplayHelper.openNpcShop(player, npcId)) {
                    unsupported("No shop is configured for this NPC.");
                    return;
                }
                break;
            case PICKPOCKET:
                if (!"semantic".equals(NpcActionRouter.coverage(npc.getDefinition(), slot))) {
                    unsupported("No pickpocket rewards are configured for this NPC.");
                    return;
                }
                CombatAction.handlePickpocketAttempt(player, npc);
                break;
            case FISH:
                FishingSpotDefinition fishing = FishingSpotDefinition.forNpcIdAndAction(npcId,
                        npc.getDefinition().getAction(slot));
                if (!player.getFishingHandler().handleFishingSpot(npc, fishing)) {
                    unsupported("This fishing spot is not registered at this position.");
                    return;
                }
                break;
            case TELEPORT:
                if (npcId == 2257) {
                    // Retain the members, quest and miniquest gates of the existing Abyss handler.
                    new ThirdNpcActionTask(1, true, player, sequence, npc).execute();
                } else {
                    RunecraftingHandler.startAbyssMageTeleport(player, npc);
                }
                break;
            case TAN:
                GameplayHelper.openTanningInterface(player);
                break;
            case EXCHANGE:
                GrandExchangeManager.openGrandExchange(player);
                break;
            case SHEAR:
                if (!player.getInventoryManager().containsItem(1735)) {
                    reject("You need a pair of shears to shear this sheep.");
                    return;
                }
                ProjectileDefinition.startSheepShearing(player);
                break;
            case HEAL:
                player.getDuelSession().restoreHitpoints();
                break;
            case REWARDS:
                MageTrainingArenaRewardShop.openRewardShop(player);
                break;
            default:
                throw new IllegalStateException("Unexpected routed NPC action " + action);
        }
        GameplayTrace.logInteraction(player, "npc-action invoked npc=" + npcId + " action=" + action);
        stop();
    }

    private boolean allowMembers() {
        if (!player.isMember()) {
            reject("You need a members account to access members content.");
            return false;
        }
        if (ServerSettings.freeToPlayWorld) {
            reject("You need to be in members world to access members content.");
            return false;
        }
        return true;
    }

    private void unsupported(String reason) {
        GameplayTrace.logInteraction(player, "npc-action unhandled npc=" + npcId + " action=" + action + " reason=" + reason);
        if (player.isInteractionDebugEnabled()) player.packetSender.sendGameMessage(reason);
        stop();
    }

    private void reject(String reason) {
        player.packetSender.sendGameMessage(reason);
        GameplayTrace.logInteraction(player, "npc-action rejected npc=" + npcId + " action=" + action + " reason=" + reason);
        stop();
    }
}

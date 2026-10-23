package com.rs2.model.interaction;

import com.rs2.model.World;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.player.Player;
import com.rs2.model.skill.fishing.FishingSpotDefinition;
import com.rs2.model.skill.thieving.PickpocketDefinition;
import com.rs2.model.skill.woodcutting.TreeDefinition;
import com.rs2.model.skill.woodcutting.UndeadTreeCutting;
import com.rs2.util.GameplayTrace;
import java.util.Locale;

/** Translates 443 cache actions to gameplay operations, independently of menu slots. */
public final class NpcActionRouter {
    public enum Action { NONE, TALK, ATTACK, BANK, TRADE, PICKPOCKET, FISH, TELEPORT, TAN, EXCHANGE, SHEAR, WOODCUT, HEAL, REWARDS, PELT, LEGACY }

    private NpcActionRouter() { }

    public static Action resolve(NpcDefinition definition, int slot) {
        String label = definition.getAction(slot);
        if (label == null || label.trim().isEmpty()) return Action.NONE;
        String action = label.trim().toLowerCase(Locale.ROOT);
        if (action.equals("talk-to") || action.equals("talk")) return Action.TALK;
        if (action.equals("attack")) return Action.ATTACK;
        if (action.equals("pelt") && com.rs2.model.quest.event.GublinchChristmasEvent.isGublinch(definition.getId())) return Action.PELT;
        if (action.equals("bank")) return Action.BANK;
        if (action.equals("trade") || action.equals("trade-with")) {
            if (isTanner(definition.getId())) return Action.TAN;
            if (definition.getId() == 3103) return Action.REWARDS;
            return Action.TRADE;
        }
        if (action.equals("pickpocket") || action.equals("pick-pocket")) return Action.PICKPOCKET;
        if (FishingSpotDefinition.forNpcIdAndAction(definition.getId(), label) != null) return Action.FISH;
        if (action.equals("teleport") && supportsTeleport(definition.getId())) return Action.TELEPORT;
        if (action.equals("tan") && isTanner(definition.getId())) return Action.TAN;
        if (action.equals("exchange") && definition.getId() == 3863) return Action.EXCHANGE;
        if (action.equals("shear") && (definition.getId() == 43 || definition.getId() == 1765)) return Action.SHEAR;
        if (action.equals("chop down") && (TreeDefinition.forEntNpcId(definition.getId()) != null
                || UndeadTreeCutting.isUndeadTree(definition.getId()))) return Action.WOODCUT;
        if (action.equals("heal") && (definition.getId() == 960 || definition.getId() == 961
                || definition.getId() == 962)) return Action.HEAL;
        return Action.LEGACY;
    }

    public static boolean supportsTeleport(int id) {
        return id == 171 || id == 300 || id == 462 || id == 844 || id == 553 || id == 2257;
    }

    private static boolean isTanner(int id) {
        return id == 804 || id == 1041 || id == 2824;
    }

    /** A route proves a handler exists; it does not claim that all gameplay has been tested. */
    public static String coverage(NpcDefinition definition, int slot) {
        Action action = resolve(definition, slot);
        if (action == Action.TRADE && definition.getShopId() < 0
                && definition.getId() != CastleWarsManager.LANTHUS_NPC_ID) return "missing-shop";
        if (action == Action.PICKPOCKET) {
            for (PickpocketDefinition pickpocket : PickpocketDefinition.values()) {
                for (String name : pickpocket.getNpcNames()) {
                    if (name.equalsIgnoreCase(definition.getName())) return "semantic";
                }
            }
            return "missing-pickpocket-definition";
        }
        return action == Action.LEGACY ? "review-legacy" : action == Action.NONE ? "no-action" : "semantic";
    }

    /** Returns false only when a specialised legacy action still owns this click. */
    public static boolean dispatch(Player player, Npc npc, int slot) {
        Action action = resolve(npc.getDefinition(), slot);
        GameplayTrace.logInteraction(player, "npc-action route npc=" + npc.getNpcId() + " slot="
                + (slot + 1) + " label=" + npc.getDefinition().getAction(slot) + " route=" + action);
        if (action == Action.LEGACY || action == Action.ATTACK) return false;
        if (action == Action.NONE) return true;
        if (action == Action.PELT) {
            // Claim the approach immediately, before generic NPC following runs.
            new RoutedNpcActionTask(player, player.nextActionSequence(), npc, slot, action).execute();
            return true;
        }
        if (action == Action.TALK || action == Action.WOODCUT) {
            InteractionDispatcher.setCurrentInteractionType(InteractionType.FIRST_NPC);
            InteractionDispatcher.dispatchCurrentInteraction(player);
        } else {
            World.scheduleTickTask(new RoutedNpcActionTask(player, player.nextActionSequence(), npc, slot, action));
        }
        return true;
    }
}

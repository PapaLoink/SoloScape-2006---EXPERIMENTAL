package com.rs2.model.skill.magic;

import com.rs2.bot.combat.BotCombatEscapeHandler;
import com.rs2.model.Position;
import com.rs2.model.combat.hit.HitDefinition;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.skill.magic.MagicSpellAction;
import com.rs2.model.skill.magic.SpellDefinition;
import com.rs2.util.GameUtil;

public final class SelfCastSpellAction
extends MagicSpellAction {
    private final SpellDefinition selfSpell;
    private final Player caster;

    public SelfCastSpellAction(Player player, SpellDefinition spellDefinition, SpellDefinition spellDefinition2, Player player2) {
        super(player, spellDefinition, (byte)0);
        this.selfSpell = spellDefinition2;
        this.caster = player2;
    }

    @Override
    public final boolean prepareCast() {
        switch (this.selfSpell) {
            case BONES_TO_PEACHES: {
                return this.castBonesToFruit(true);
            }
            case BONES_TO_BANANAS: {
                return this.castBonesToFruit(false);
            }
            case CHARGE: {
                if (this.caster.getChargeCooldownTimer().hasElapsed()) {
                    this.caster.activateChargeSpell();
                    this.caster.getAttackDelayTimer().setDelayTicks(this.caster.getAttackDelayTimer().getDelayTicks() + 2);
                    break;
                }
                Player player = this.caster;
                player.packetSender.sendGameMessage("You cannot use this spell yet!");
                return false;
            }
            case LUMBRIDGE_GRAVEYARD_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3241 + GameUtil.randomInclusive(1), 3195 + GameUtil.randomInclusive(1), 0));
            }
            case DRAYNOR_MANOR_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3109 + GameUtil.randomInclusive(1), 3352 + GameUtil.randomInclusive(1), 0));
            }
            case MIND_ALTAR_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2978 + GameUtil.randomInclusive(1), 3506 + GameUtil.randomInclusive(1), 0));
            }
            case SALVE_GRAVEYARD_TELEPORT: {
                if (this.caster.getQuestState(72) != 1) {
                    Object value = QuestDefinition.forId(72);
                    value = ((QuestDefinition)value).getName();
                    Player player = this.caster;
                    player.packetSender.sendGameMessage("You need to complete " + (String)value + " to do this.");
                    return false;
                }
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3431 + GameUtil.randomInclusive(1), 3460 + GameUtil.randomInclusive(1), 0));
            }
            case FENKENSTRAINS_CASTLE_TELEPORT: {
                if (this.caster.getQuestState(72) != 1) {
                    Object value2 = QuestDefinition.forId(72);
                    value2 = ((QuestDefinition)value2).getName();
                    Player player = this.caster;
                    player.packetSender.sendGameMessage("You need to complete " + (String)value2 + " to do this.");
                    return false;
                }
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3545 + GameUtil.randomInclusive(1), 3528 + GameUtil.randomInclusive(1), 0));
            }
            case WEST_ARDOUGNE_TELEPORT: {
                if (this.caster.getPlayerRights() < 2) {
                    Player player = this.caster;
                    player.packetSender.sendGameMessage("This spell has been temporarily disabled!");
                    return false;
                }
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2501 + GameUtil.randomInclusive(1), 3291 + GameUtil.randomInclusive(1), 0));
            }
            case CEMETERY_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2980, 3762 + GameUtil.randomInclusive(1), 0));
            }
            case BARROWS_TELEPORT: {
                if (this.caster.getQuestState(72) != 1) {
                    Object value3 = QuestDefinition.forId(72);
                    value3 = ((QuestDefinition)value3).getName();
                    Player player = this.caster;
                    player.packetSender.sendGameMessage("You need to complete " + (String)value3 + " to do this.");
                    return false;
                }
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3565 + GameUtil.randomInclusive(1), 3314 + GameUtil.randomInclusive(1), 0));
            }
            case NECROMANCY_APE_ATOLL_TELEPORT: {
                if (this.caster.getQuestState(62) != 1) {
                    Object value4 = QuestDefinition.forId(62);
                    value4 = ((QuestDefinition)value4).getName();
                    Player player = this.caster;
                    player.packetSender.sendGameMessage("You need to complete " + (String)value4 + " to do this.");
                    return false;
                }
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2799 + GameUtil.randomInclusive(1), 9212 + GameUtil.randomInclusive(1), 0));
            }
            case VARROCK_TELEPORT: {
                boolean teleportManager = this.caster.getTeleportManager().castSpellbookTeleport(new Position(3213 + GameUtil.randomInclusive(1), 3423 + GameUtil.randomInclusive(1), 0));
                if (this.caster.botEnabled && !teleportManager && this.caster.botCombatState.startsWith("escape")) {
                    this.caster.botCombatState = "tele";
                    BotCombatEscapeHandler.startBotCombatWalkingEscape(this.caster);
                } else if (this.caster.botEnabled && !teleportManager && this.caster.botCombatState.equals("tele")) {
                    this.caster.botCombatState = "run";
                }
                return teleportManager;
            }
            case LUMBRIDGE_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3222 + GameUtil.randomInclusive(1), 3218 + GameUtil.randomInclusive(1), 0));
            }
            case HOME_TELEPORT: {
                return this.caster.getTeleportManager().castHomeTeleport();
            }
            case FALADOR_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2964 + GameUtil.randomInclusive(1), 3378 + GameUtil.randomInclusive(1), 0));
            }
            case CAMELOT_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2757 + GameUtil.randomInclusive(1), 3479 + GameUtil.randomInclusive(1), 0));
            }
            case ARDOUGNE_TELEPORT: {
                if (this.caster.getQuestState(71) != 1) {
                    this.caster.packetSender.sendGameMessage("You need to complete " + QuestDefinition.forId(71).getName() + " to do this.");
                    return false;
                }
                if (!com.rs2.model.quest.impl.PlagueCityQuest.hasReadScroll(this.caster)) {
                    this.caster.packetSender.sendGameMessage("You havn't learnt how to cast this spell yet.");
                    return false;
                }
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2662 + GameUtil.randomInclusive(1), 3305 + GameUtil.randomInclusive(1), 0));
            }
            case WATCHTOWER_TELEPORT: {
                if (this.caster.getQuestState(101) != 1) {
                    Object value6 = QuestDefinition.forId(101);
                    value6 = ((QuestDefinition)value6).getName();
                    Player player = this.caster;
                    player.packetSender.sendGameMessage("You need to complete " + (String)value6 + " to do this.");
                    return false;
                }
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2546 + GameUtil.randomInclusive(1), 3112 + GameUtil.randomInclusive(1), 2));
            }
            case TROLLHEIM_TELEPORT: {
                if (this.caster.getPlayerRights() < 2) {
                    Player player = this.caster;
                    player.packetSender.sendGameMessage("This spell has been temporarily disabled!");
                    return false;
                }
                if (this.caster.getQuestState(31) != 1) {
                    Object value7 = QuestDefinition.forId(31);
                    value7 = ((QuestDefinition)value7).getName();
                    Player player = this.caster;
                    player.packetSender.sendGameMessage("You need to complete " + (String)value7 + " to do this.");
                    return false;
                }
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2910 + GameUtil.randomInclusive(1), 3612 + GameUtil.randomInclusive(1), 0));
            }
            case APE_ATOLL_TELEPORT: {
                if (this.caster.getQuestState(62) != 1) {
                    Object value8 = QuestDefinition.forId(62);
                    value8 = ((QuestDefinition)value8).getName();
                    Player player = this.caster;
                    player.packetSender.sendGameMessage("You need to complete " + (String)value8 + " to do this.");
                    return false;
                }
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2798 + GameUtil.randomInclusive(1), 2798 + GameUtil.randomInclusive(1), 1));
            }
            case PADDEWWA_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3098 + GameUtil.randomInclusive(1), 9884 + GameUtil.randomInclusive(1), 0));
            }
            case SENNTISTEN_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3321 + GameUtil.randomInclusive(1), 3335 + GameUtil.randomInclusive(1), 0));
            }
            case CARRALLANGAR_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3156 + GameUtil.randomInclusive(1), 3666 + GameUtil.randomInclusive(1), 0));
            }
            case KHARYRLL_TELEPORT: {
                if (this.caster.getQuestState(72) != 1) {
                    Object value9 = QuestDefinition.forId(72);
                    value9 = ((QuestDefinition)value9).getName();
                    Player player = this.caster;
                    player.packetSender.sendGameMessage("You need to complete " + (String)value9 + " to do this.");
                    return false;
                }
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3492 + GameUtil.randomInclusive(1), 3471 + GameUtil.randomInclusive(1), 0));
            }
            case LASSAR_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3001 + GameUtil.randomInclusive(1), 3470 + GameUtil.randomInclusive(1), 0));
            }
            case DAREEYAK_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2970 + GameUtil.randomInclusive(1), 3697 + GameUtil.randomInclusive(1), 0));
            }
            case ANNAKARL_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(3287 + GameUtil.randomInclusive(1), 3886 + GameUtil.randomInclusive(1), 0));
            }
            case GHORROCK_TELEPORT: {
                return this.caster.getTeleportManager().castSpellbookTeleport(new Position(2977 + GameUtil.randomInclusive(1), 3873 + GameUtil.randomInclusive(1), 0));
            }
        }
        return true;
    }

    @Override
    public final void applyImpact(HitDefinition hitDefinition) {
    }
}

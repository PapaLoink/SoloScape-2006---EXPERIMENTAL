package com.rs2.model.combat.attack;

import com.rs2.ServerSettings;
import com.rs2.bot.combat.BotCombatEscapeHandler;
import com.rs2.model.Entity;
import com.rs2.model.World;
import com.rs2.model.combat.AttackValidationResult;
import com.rs2.model.combat.CombatAction;
import com.rs2.model.combat.CombatCycleEvent;
import com.rs2.model.combat.CombatManager;
import com.rs2.model.combat.CombatType;
import com.rs2.model.combat.attack.BaseCombatAttack;
import com.rs2.model.combat.attack.CombatAttackState;
import com.rs2.model.combat.effect.CombatEffect;
import com.rs2.model.combat.effect.PoisonEffect;
import com.rs2.model.combat.effect.StatDrainEffect;
import com.rs2.model.combat.hit.HitDefinition;
import com.rs2.model.combat.requirement.CombatRequirement;
import com.rs2.model.combat.requirement.GodStaffRequirement;
import com.rs2.model.combat.requirement.MagicCombatLevelRequirement;
import com.rs2.model.combat.requirement.MagicCombatMembersRequirement;
import com.rs2.model.combat.requirement.MagicCombatRuneRequirement;
import com.rs2.model.gameplay.duel.DuelRule;
import com.rs2.model.npc.Npc;
import com.rs2.model.player.Player;
import com.rs2.model.skill.magic.SpellDefinition;
import com.rs2.model.task.CycleEventContainer;
import com.rs2.util.GameUtil;

public final class MagicCombatAttack
extends BaseCombatAttack {
    private SpellDefinition spell;

    public MagicCombatAttack(Entity entity, Entity entity2, SpellDefinition spellDefinition) {
        super(entity, entity2);
        this.spell = spellDefinition;
    }

    @Override
    public final CombatAttackState getState() {
        if (!this.spell.isCombatSpell() || this.spell.getHitDefinition() == null) {
            return CombatAttackState.a;
        }
        if (ServerSettings.freeToPlayWorld && this.spell.isMembersOnly()) {
            if (this.getAttacker().isPlayer()) {
                Player player = (Player)this.getAttacker();
                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                ((Player)this.getAttacker()).setAutocastSpell(null);
                CombatManager.stopCombat(this.getAttacker());
            }
            return CombatAttackState.a;
        }
        if (this.getAttacker().isPlayer() && DuelRule.NO_MAGIC.isEnabledFor((Player)this.getAttacker())) {
            if (this.getAttacker().isPlayer()) {
                Player player = (Player)this.getAttacker();
                player.packetSender.sendGameMessage("Magic attacks have been disabled during this fight!");
                ((Player)this.getAttacker()).setAutocastSpell(null);
                CombatManager.stopCombat(this.getAttacker());
            }
            return CombatAttackState.a;
        }
        if (this.spell.equals((Object)SpellDefinition.CRUMBLE_UNDEAD) && !Npc.isUndead(this.getTarget())) {
            Player player = (Player)this.getAttacker();
            player.packetSender.sendGameMessage("This spell only affects skeletons, zombies, ghosts and shades.");
            CombatManager.stopCombat(this.getAttacker());
            return CombatAttackState.a;
        }
        if (this.spell.getPrimaryEffect() != null) {
            if (this.spell.getPrimaryEffect() instanceof StatDrainEffect) {
                StatDrainEffect statDrainEffect = (StatDrainEffect)this.spell.getPrimaryEffect();
                int skillId = statDrainEffect.getSkillId();
                if (this.getTarget().isPlayer()) {
                    Player player = (Player)this.getTarget();
                    int skillManager = player.getSkillManager().getBaseLevel(skillId);
                    int skillManager2 = player.getSkillManager().getCurrentLevels()[skillId];
                    if (skillManager2 < skillManager) {
                        if (this.getAttacker().isPlayer()) {
                            Player player2 = (Player)this.getAttacker();
                            player2.packetSender.sendGameMessage("The target is immune to that spell right now!");
                            CombatManager.stopCombat(this.getAttacker());
                        }
                        return CombatAttackState.a;
                    }
                } else {
                    Npc npc = (Npc)this.getTarget();
                    int index = 0;
                    int index2 = 0;
                    if (skillId == 0) {
                        index = npc.getBaseAttackLevel();
                        index2 = npc.getCurrentAttackLevel();
                    } else if (skillId == 2) {
                        index = npc.getBaseStrengthLevel();
                        index2 = npc.getCurrentStrengthLevel();
                    } else if (skillId == 1) {
                        index = npc.getBaseDefenceLevel();
                        index2 = npc.getCurrentDefenceLevel();
                    } else if (skillId == 6) {
                        index = npc.getBaseMagicLevel();
                        index2 = npc.getCurrentMagicLevel();
                    } else if (skillId == 4) {
                        index = npc.getBaseRangedLevel();
                        index2 = npc.getCurrentRangedLevel();
                    }
                    if (index2 < index) {
                        if (this.getAttacker().isPlayer()) {
                            Player player = (Player)this.getAttacker();
                            player.packetSender.sendGameMessage("The target is immune to that spell right now!");
                            CombatManager.stopCombat(this.getAttacker());
                        }
                        return CombatAttackState.a;
                    }
                }
            } else if (!this.getTarget().canApplyCombatEffect(this.spell.getPrimaryEffect())) {
                if (this.getAttacker().isPlayer()) {
                    Player player = (Player)this.getAttacker();
                    player.packetSender.sendGameMessage("The target is immune to that spell right now!");
                    CombatManager.stopCombat(this.getAttacker());
                }
                return CombatAttackState.a;
            }
        }
        return super.getState();
    }

    @Override
    public final CombatType getCombatType() {
        if (!this.spell.isCombatSpell() || this.spell.getHitDefinition() == null) {
            return null;
        }
        return this.spell.getHitDefinition().getAttackStyle().getCombatType();
    }

    @Override
    public final int getAttackRange() {
        return 10;
    }

    @Override
    public final void prepare() {
        if (!this.spell.isCombatSpell() || this.spell.getHitDefinition() == null) {
            return;
        }
        Object runeCosts = this.spell.getRuneCosts();
        int initialValue = -1;
        if (this.spell == SpellDefinition.FLAMES_OF_ZAMORAK) {
            initialValue = 2417;
        } else if (this.spell == SpellDefinition.CLAWS_OF_GUTHIX) {
            initialValue = 2416;
        } else if (this.spell == SpellDefinition.SARADOMIN_STRIKE) {
            initialValue = 2415;
        }
        if (this.spell == SpellDefinition.IBAN_BLAST) {
            initialValue = 1409;
        }
        if (this.spell == SpellDefinition.MAGIC_DART) {
            initialValue = 4170;
        }
        int value = (initialValue != -1 ? 1 : 0) + (runeCosts != null ? 1 : 0) + 1 + (this.spell.isMembersOnly() ? 1 : 0);
        CombatRequirement[] combatRequirementArray = new CombatRequirement[value];
        int index = 0;
        if (this.spell.isMembersOnly()) {
            combatRequirementArray[index++] = new MagicCombatMembersRequirement(this);
        }
        if (runeCosts != null) {
            combatRequirementArray[index++] = new MagicCombatRuneRequirement(this, this.spell);
        }
        combatRequirementArray[index++] = new MagicCombatLevelRequirement(this, 6, this.spell.getRequiredLevel());
        if (initialValue != -1) {
            combatRequirementArray[index] = new GodStaffRequirement(this, 3, initialValue, 1, false);
        }
        if (this.spell.getAnimationId() != -1) {
            this.setAnimationId(this.spell.getAnimationId());
            this.setAttackSoundId(this.spell.getCastSoundId());
        }
        runeCosts = this.spell.getHitDefinition().copy();
        ((HitDefinition)runeCosts).setSpell(this.spell);
        ((HitDefinition)runeCosts).setMaxDamage(CombatManager.calculateSpellMaxHit(this.getAttacker(), this.spell));
        boolean enabled = this.spell == SpellDefinition.SUMMON_ZOMBIE || this.spell == SpellDefinition.CHAOS_ELEMENTAL_DISARM || this.spell == SpellDefinition.CHAOS_ELEMENTAL_RANDOM_TELEPORT;
        CombatEffect combatEffect = this.spell.getSecondaryEffect();
        if (this.spell.getSecondaryEffect() != null && this.spell.getSecondaryEffect() instanceof PoisonEffect && !GameUtil.rollChance(0.125)) {
            combatEffect = null;
        }
        this.setHitDefinitions(new HitDefinition[]{((HitDefinition)runeCosts).enableRandomDamage().enableAccuracyCheck().addEffects(new CombatEffect[]{this.spell.getPrimaryEffect(), combatEffect}).setAlwaysHits(enabled)});
        this.setAttackerGraphic(this.spell.getCastGraphic());
        this.setAttackDelay(5);
        this.setRequirements(combatRequirementArray);
    }

    @Override
    public final int execute(CycleEventContainer cycleEventContainer) {
        Object value;
        Object value2;
        if (this.getAttacker().isPlayer()) {
            value2 = (Player)this.getAttacker();
            if (!((Player)value2).isAutocastEnabled()) {
                this.getAttacker().getMovementQueue().clear();
            }
            if (((Player)value2).getQueuedCombatSpell() == this.spell) {
                ((Player)value2).setQueuedCombatSpell(null);
                cycleEventContainer.stop();
            }
        }
        value2 = this.spell.getHitDefinition().copy();
        ((HitDefinition)value2).setSpell(this.spell);
        if (((HitDefinition)value2).isMultiTargetSpreadEnabled() && ((value = this.spell) == SpellDefinition.SMOKE_BURST || value == SpellDefinition.SHADOW_BURST || value == SpellDefinition.BLOOD_BURST || value == SpellDefinition.ICE_BURST || value == SpellDefinition.SMOKE_BARRAGE || value == SpellDefinition.SHADOW_BARRAGE || value == SpellDefinition.BLOOD_BARRAGE || value == SpellDefinition.ICE_BARRAGE) && this.getAttacker().isInMultiCombatArea()) {
            AttackValidationResult attackValidationResult;
            Entity entity;
            int index = 0;
            Entity[] entityArray = World.getPlayers();
            int length = entityArray.length;
            int index2 = 0;
            while (index2 < length) {
                entity = entityArray[index2];
                if (entity != null && entity != this.getAttacker() && entity != this.getTarget() && GameUtil.isWithinDistance(this.getTarget().getPosition(), entity.getPosition(), 1) && (attackValidationResult = CombatCycleEvent.validateAttack(this.getAttacker(), entity)) == AttackValidationResult.VALID) {
                    if (index > 13) break;
                    ((HitDefinition)value2).setMultiTargetSpreadEnabled(false);
                    ((HitDefinition)value2).enableAccuracyCheck(true);
                    ((HitDefinition)value2).scaleMaxDamage(0.75);
                    ((HitDefinition)value2).setDelay(-1);
                    new CombatAction(this.getAttacker(), entity, (HitDefinition)value2).queue();
                    ++index;
                }
                ++index2;
            }
            entityArray = World.getNpcs();
            length = entityArray.length;
            index2 = 0;
            while (index2 < length) {
                entity = entityArray[index2];
                if (entity != null && entity != this.getTarget() && this.getTarget().isWithinReach(entity, 1) && (attackValidationResult = CombatCycleEvent.validateAttack(this.getAttacker(), entity)) == AttackValidationResult.VALID) {
                    if (index > 13) break;
                    ((HitDefinition)value2).setMultiTargetSpreadEnabled(false);
                    ((HitDefinition)value2).enableAccuracyCheck(true);
                    ((HitDefinition)value2).scaleMaxDamage(0.75);
                    ((HitDefinition)value2).setDelay(-1);
                    new CombatAction(this.getAttacker(), entity, (HitDefinition)value2).queue();
                    ++index;
                }
                ++index2;
            }
        }
        if (this.getAttacker().isPlayer()) {
            value = (Player)this.getAttacker();
            ((Player)value).getSkillManager().addExperience(6, this.spell.getExperience());
        }
        int value3 = super.execute(cycleEventContainer);
        return value3;
    }

    @Override
    public final void onRequirementFailed() {
        if (this.getAttacker().isPlayer()) {
            Player player = (Player)this.getAttacker();
            if (player.botEnabled) {
                if (player.isInWilderness()) {
                    BotCombatEscapeHandler.tryStartBotCombatEscape(player);
                } else if (player.currentBotTask != null) {
                    player.botCombatState = "escape";
                }
            }
            if (player.getQueuedCombatSpell() == this.spell) {
                player.setQueuedCombatSpell(null);
                return;
            }
            if (player.getAutocastSpell() == this.spell) {
                player.setAutocastSpell(null);
            }
        }
    }
}

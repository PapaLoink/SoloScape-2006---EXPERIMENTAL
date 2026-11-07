package com.rs2.model.c;

import com.rs2.model.Entity;
import com.rs2.model.animation.GraphicEffect;
import com.rs2.model.combat.AttackXpMode;
import com.rs2.model.combat.CombatType;
import com.rs2.model.combat.ProjectileTiming;
import com.rs2.model.combat.WeaponProfile;
import com.rs2.model.combat.attack.BaseCombatAttack;
import com.rs2.model.combat.attack.CombatAttack;
import com.rs2.model.npc.combat.NpcCombatDefinition;
import com.rs2.model.player.Player;
import com.rs2.model.skill.magic.SpellDefinition;
import com.rs2.util.GameUtil;

/**
 * The Priest in Peril Monks of Zamorak (LostCity ai_opplayer2 for priestperilevilmonk1-3). One attack in four is a
 * spell; otherwise they punch. Monks 1 and 2 cast the first of their curses whose stat the player still has at full
 * level, and monk 3 zaps for up to 8.
 */
public final class PriestInPerilMonkCombatDefinition
extends NpcCombatDefinition {
    public static final int MONK1 = 1044, MONK2 = 1045, MONK3 = 1046;
    private static final int ATTACK = 0, DEFENCE = 1, STRENGTH = 2;
    // LostCity npc_zap_attack: human_castzap with the zap spotanim at height 92, no projectile
    private static final int CASTZAP = 707, ZAP = 82;

    private final int monk;

    public PriestInPerilMonkCombatDefinition(int monk) {
        this.monk = monk;
    }

    @Override
    public final CombatAttack[] createAttacks(Entity entity, Entity entity2) {
        CombatAttack punch = BaseCombatAttack.createWeaponProfileMeleeAttack(entity, entity2, AttackXpMode.AGGRESSIVE, 0, WeaponProfile.FISTS);
        if (!entity2.isPlayer() || GameUtil.randomInt(4) != 0) {
            return new CombatAttack[]{punch};
        }
        if (this.monk == MONK3) {
            return new CombatAttack[]{BaseCombatAttack.createProjectileAttack(entity, entity2, CombatType.MAGIC, AttackXpMode.MAGIC, 8, 4,
                CASTZAP, new GraphicEffect(ZAP, 92), new GraphicEffect(-1, 0), -1, ProjectileTiming.MAGIC)};
        }
        Player player = (Player)entity2;
        int[] order = this.monk == MONK1 ? new int[]{STRENGTH, ATTACK, DEFENCE} : new int[]{ATTACK, STRENGTH, DEFENCE};
        for (int skill : order) {
            if (player.getSkillManager().getCurrentLevels()[skill] >= player.getSkillManager().getBaseLevel(skill)) {
                SpellDefinition spell = skill == ATTACK ? SpellDefinition.CONFUSE : skill == STRENGTH ? SpellDefinition.WEAKEN : SpellDefinition.CURSE;
                return new CombatAttack[]{BaseCombatAttack.createMagicAttack(entity, entity2, spell)};
            }
        }
        return new CombatAttack[]{punch};
    }
}

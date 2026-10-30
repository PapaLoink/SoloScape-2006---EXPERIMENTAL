package com.rs2.model.combat.special;

import com.rs2.ServerSettings;
import com.rs2.model.Entity;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.animation.GraphicEffect;
import com.rs2.model.combat.AttackValidationResult;
import com.rs2.model.combat.CombatAction;
import com.rs2.model.combat.CombatCycleEvent;
import com.rs2.model.combat.CombatManager;
import com.rs2.model.combat.WeaponProfile;
import com.rs2.model.combat.attack.WeaponCombatAttack;
import com.rs2.model.combat.hit.HitDefinition;
import com.rs2.model.combat.hit.HitType;
import com.rs2.model.gameplay.duel.DuelRule;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.player.Player;
import com.rs2.util.GameUtil;

public abstract class SpecialAttackDefinition {
    private static SpecialAttackDefinition DRAGON_DAGGER = new DragonDaggerSpecialDefinition(25, "drag dagger", "dragon dagger");
    private static SpecialAttackDefinition ABYSSAL_WHIP = new AbyssalWhipSpecialDefinition(50, new String[0]);
    private static SpecialAttackDefinition RUNE_THROWNAXE = new RuneThrownaxeSpecialDefinition(10, new String[0]);
    private static SpecialAttackDefinition DRAGON_LONGSWORD = new DragonLongswordSpecialDefinition(25, new String[0]);
    private static SpecialAttackDefinition DRAGON_SCIMITAR = new DragonScimitarSpecialDefinition(55, new String[0]);
    private static SpecialAttackDefinition RUNE_CLAWS = new RuneClawsSpecialDefinition(25, new String[0]);
    private static SpecialAttackDefinition DRAGON_CLAWS = new DragonClawsSpecialDefinition(50, "dragon claws");
    private static SpecialAttackDefinition DRAGON_MACE = new DragonMaceSpecialDefinition(25, new String[0]);
    private static SpecialAttackDefinition DRAGON_AXE = new DragonAxeSpecialDefinition(100, new String[0]);
    private static SpecialAttackDefinition DARKLIGHT = new DarklightSpecialDefinition(50, new String[0]);
    private static SpecialAttackDefinition DRAGON_SPEAR = new DragonSpearSpecialDefinition(25, "dragon spear", "zamorakian spear");
    private static SpecialAttackDefinition DRAGON_HALBERD = new DragonHalberdSpecialDefinition(30, new String[0]);
    private static SpecialAttackDefinition DRAGON_2H_SWORD = new Dragon2hSwordSpecialDefinition(60, new String[0]);
    private static SpecialAttackDefinition SEERCULL = new SeercullSpecialDefinition(100, new String[0]);
    private static SpecialAttackDefinition DARK_BOW = new DarkBowSpecialDefinition(55, new String[0]);
    private static SpecialAttackDefinition MAGIC_SHORTBOW = new MagicShortbowSpecialDefinition(ServerSettings.cacheVersion < 296 ? 35 : 55, new String[0]);
    private static SpecialAttackDefinition MAGIC_LONGBOW = new MagicLongbowSpecialDefinition(35, new String[0]);
    private static SpecialAttackDefinition DRAGON_BATTLEAXE = new DragonBattleaxeSpecialDefinition(100, new String[0]);
    private static SpecialAttackDefinition EXCALIBUR = new ExcaliburSpecialDefinition(100, new String[0]);
    private static SpecialAttackDefinition BANDOS_GODSWORD = new BandosGodswordSpecialDefinition(50, new String[0]);
    private static SpecialAttackDefinition ARMADYL_GODSWORD = new ArmadylGodswordSpecialDefinition(50, new String[0]);
    private static SpecialAttackDefinition ZAMORAK_GODSWORD = new ZamorakGodswordSpecialDefinition(50, new String[0]);
    private static SpecialAttackDefinition SARADOMIN_GODSWORD = new SaradominGodswordSpecialDefinition(50, new String[0]);
    private static SpecialAttackDefinition SARADOMIN_SWORD = new SaradominSwordSpecialDefinition(100, new String[0]);
    private static SpecialAttackDefinition GRANITE_MAUL = new GraniteMaulSpecialDefinition(50, new String[0]);
    private static final SpecialAttackDefinition[] VALUES;

    static {
        VALUES = new SpecialAttackDefinition[]{DRAGON_DAGGER, ABYSSAL_WHIP, RUNE_THROWNAXE, DRAGON_LONGSWORD, DRAGON_SCIMITAR, RUNE_CLAWS, DRAGON_CLAWS, DRAGON_MACE, DRAGON_AXE, DARKLIGHT, DRAGON_SPEAR, DRAGON_HALBERD, DRAGON_2H_SWORD, SEERCULL, DARK_BOW, MAGIC_SHORTBOW, MAGIC_LONGBOW, DRAGON_BATTLEAXE, EXCALIBUR, BANDOS_GODSWORD, ARMADYL_GODSWORD, ZAMORAK_GODSWORD, SARADOMIN_GODSWORD, SARADOMIN_SWORD, GRANITE_MAUL};
    }

    private final String name;
    private final byte energyCost;
    private String[] weaponNamePatterns;

    protected SpecialAttackDefinition(int energyCost, String ... weaponNamePatterns) {
        this.name = SpecialAttackDefinition.deriveName(this.getClass().getSimpleName());
        this.energyCost = (byte)energyCost;
        this.weaponNamePatterns = weaponNamePatterns;
        if (this.weaponNamePatterns == null || this.weaponNamePatterns.length == 0) {
            this.weaponNamePatterns = new String[]{this.name().toLowerCase().replaceAll("_", " ")};
        }
    }

    private static String deriveName(String simpleName) {
        String baseName = simpleName.endsWith("SpecialDefinition") ? simpleName.substring(0, simpleName.length() - "SpecialDefinition".length()) : simpleName;
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < baseName.length(); ++index) {
            char character = baseName.charAt(index);
            if (index > 0) {
                char previous = baseName.charAt(index - 1);
                if (Character.isDigit(character) && !Character.isDigit(previous)) {
                    builder.append('_');
                } else if (Character.isUpperCase(character) && (Character.isLowerCase(previous) || Character.isDigit(previous))) {
                    builder.append('_');
                }
            }
            builder.append(Character.toUpperCase(character));
        }
        return builder.toString();
    }

    public abstract WeaponCombatAttack createAttack(Player player, Entity target, WeaponProfile weaponProfile);

    public final byte getEnergyCost() {
        return this.energyCost;
    }

    public final String name() {
        return this.name;
    }

    public final int ordinal() {
        for (int index = 0; index < VALUES.length; ++index) {
            if (VALUES[index] == this) {
                return index;
            }
        }
        return -1;
    }

    public final String toString() {
        return this.name;
    }

    public static SpecialAttackDefinition forItem(ItemStack itemStack) {
        if (itemStack == null || ItemDefinition.isRemovedContent(itemStack.getId())) {
            return null;
        }
        String itemName = ItemDefinition.forId(itemStack.getId()).getName().toLowerCase();
        SpecialAttackDefinition[] definitions = SpecialAttackDefinition.values();
        int count = definitions.length;
        int index = 0;
        while (index < count) {
            SpecialAttackDefinition definition = definitions[index];
            String[] patterns = definition.weaponNamePatterns;
            int patternCount = patterns.length;
            int patternIndex = 0;
            while (patternIndex < patternCount) {
                String pattern = patterns[patternIndex];
                if (itemName.contains(pattern.replace("_", " "))) {
                    return definition;
                }
                ++patternIndex;
            }
            ++index;
        }
        return null;
    }

    public static void performDragonBattleaxeSpecial(Player player) {
        if (DuelRule.NO_SPECIAL_ATTACK.isEnabledFor(player)) {
            Player player2 = player;
            player2.packetSender.sendGameMessage("Special attacks have been disabled during this fight!");
            return;
        }
        if (player.getSpecialEnergy() < 100) {
            Player player3 = player;
            player3.packetSender.sendGameMessage("You don't have enough special attack to do that.");
            return;
        }
        player.setSpecialAttackEnabled(!player.isSpecialAttackEnabled());
        Player player4 = player;
        player4.packetSender.refreshSpecialAttackConfig();
        player.getUpdateState().setGraphic(246);
        player.getUpdateState().setAnimation(1056);
        player4 = player;
        player4.packetSender.sendSoundEffect(389, 1, 0);
        player.getUpdateState().setForcedText("Raarrrrrgggggghhhhhhh!");
        player4 = player;
        player4.packetSender.modifySkillLevel(2, (int)((double)player.getSkillManager().getBaseLevel(2) * 0.2), true);
        player4 = player;
        player4.packetSender.modifySkillLevel(0, -((int)((double)player.getSkillManager().getBaseLevel(0) * 0.1)), true);
        player4 = player;
        player4.packetSender.modifySkillLevel(1, -((int)((double)player.getSkillManager().getBaseLevel(1) * 0.1)), true);
        player4 = player;
        player4.packetSender.modifySkillLevel(4, -((int)((double)player.getSkillManager().getBaseLevel(4) * 0.1)), true);
        player4 = player;
        player4.packetSender.modifySkillLevel(6, -((int)((double)player.getSkillManager().getBaseLevel(6) * 0.1)), true);
        player.setSpecialAttackEnabled(false);
        player.setSpecialEnergy(0);
        player4 = player;
        player4.packetSender.refreshSpecialEnergyBar(7511);
        player4 = player;
        player4.packetSender.refreshSpecialAttackConfig();
    }

    public static void performExcaliburSpecial(Player player) {
        if (DuelRule.NO_SPECIAL_ATTACK.isEnabledFor(player)) {
            Player player2 = player;
            player2.packetSender.sendGameMessage("Special attacks have been disabled during this fight!");
            return;
        }
        if (player.getSpecialEnergy() < 100) {
            Player player3 = player;
            player3.packetSender.sendGameMessage("You don't have enough special attack to do that.");
            return;
        }
        player.setSpecialAttackEnabled(!player.isSpecialAttackEnabled());
        Player player4 = player;
        player4.packetSender.refreshSpecialAttackConfig();
        player.getUpdateState().setGraphic(247);
        player.getUpdateState().setAnimation(1057);
        player4 = player;
        player4.packetSender.sendSoundEffect(384, 1, 0);
        player.getUpdateState().setForcedText("For Camelot!");
        player4 = player;
        player4.packetSender.modifySkillLevel(1, 8, true);
        player.setSpecialAttackEnabled(false);
        player.setSpecialEnergy(0);
        player4 = player;
        player4.packetSender.refreshSpecialEnergyBar(7611);
        player4 = player;
        player4.packetSender.refreshSpecialAttackConfig();
    }

    public static void performGraniteMaulSpecial(Player player) {
        if (player.getCombatTarget().isDead() || !EntityTargetMovement.canReachTarget(player, player.getCombatTarget())) {
            return;
        }
        if (player.getCombatTarget().isDoorSupportNpc()) {
            return;
        }
        AttackValidationResult validationResult = CombatCycleEvent.validateAttack(player, player.getCombatTarget());
        if (validationResult != AttackValidationResult.VALID) {
            return;
        }
        if (DuelRule.NO_SPECIAL_ATTACK.isEnabledFor(player)) {
            player.packetSender.sendGameMessage("Special attacks have been disabled during this fight!");
            return;
        }
        if (player.getSpecialEnergy() < 50) {
            player.packetSender.sendGameMessage("You don't have enough special attack to do that.");
            return;
        }
        player.setSpecialAttackEnabled(!player.isSpecialAttackEnabled());
        player.packetSender.refreshSpecialAttackConfig();
        player.getUpdateState().setAnimation(1667);
        player.getUpdateState().setGraphic(new GraphicEffect(337, 100));
        WeaponCombatAttack weaponCombatAttack = new WeaponCombatAttack(player, player.getCombatTarget(), player.getWeaponProfile());
        HitDefinition hitDefinition = new HitDefinition(weaponCombatAttack.getAttackStyle(), HitType.NORMAL, CombatManager.calculateMeleeMaxHit(player, weaponCombatAttack)).enableRandomDamage().enableAccuracyCheck(true);
        new CombatAction(player, player.getCombatTarget(), hitDefinition).queue();
        player.setSpecialAttackEnabled(false);
        player.setSpecialEnergy(player.getSpecialEnergy() - 50);
        player.packetSender.refreshSpecialEnergyBar(7486);
        player.packetSender.refreshSpecialAttackConfig();
    }

    public static void applyHitSpecialEffect(Entity entity, Entity target, HitDefinition hitDefinition, int damage) {
        if (entity == null || !entity.isPlayer()) {
            return;
        }
        Player player = (Player)entity;
        // Ordinary hits also pass through this handler. Keep Tutorial Island's
        // instruction panel visible when the player's arrow reaches the rat.
        if (player.getQuestState(0) == 1 || player.getOpenInterfaceId() != 6179) {
            player.packetSender.closeInterfaces();
        }
        switch (hitDefinition.getSpecialEffectId()) {
            case 1: {
                target.getUpdateState().setGraphic(474);
                if (damage <= 0 || !target.isPlayer()) break;
                Player targetPlayer = (Player)target;
                targetPlayer.packetSender.modifySkillLevel(6, -damage, true);
                return;
            }
            case 2: {
                if (!target.isPlayer()) break;
                Player targetPlayer = (Player)target;
                targetPlayer.getPrayerManager().deactivatePrayer(12);
                targetPlayer.getPrayerManager().deactivatePrayer(13);
                targetPlayer.getPrayerManager().deactivatePrayer(14);
                targetPlayer.setProtectionPrayerDisabledUntil(System.currentTimeMillis() + 5000L);
                targetPlayer.packetSender.sendGameMessage("You have been injured!");
                return;
            }
            case 3: {
                if (damage <= 0 || !target.isPlayer()) break;
                Player targetPlayer = (Player)target;
                targetPlayer.packetSender.modifySkillLevel(1, -damage, true);
                targetPlayer.packetSender.modifySkillLevel(6, -damage, true);
                return;
            }
            case 4: {
                if (damage <= 0 || !target.isPlayer()) break;
                Player targetPlayer = (Player)target;
                targetPlayer.packetSender.modifySkillLevel(0, -((int)((double)targetPlayer.getSkillManager().getBaseLevel(0) * 0.05)), true);
                targetPlayer.packetSender.modifySkillLevel(2, -((int)((double)targetPlayer.getSkillManager().getBaseLevel(2) * 0.05)), true);
                targetPlayer.packetSender.modifySkillLevel(1, -((int)((double)targetPlayer.getSkillManager().getBaseLevel(1) * 0.05)), true);
                return;
            }
            case 5: {
                int playerX = player.getPosition().getX();
                int playerY = player.getPosition().getY();
                int deltaX = 0;
                int deltaY = 0;
                int targetX = target.getPosition().getX();
                int targetY = target.getPosition().getY();
                if (playerX > targetX) {
                    deltaX = -1;
                } else if (playerX < targetX) {
                    deltaX = 1;
                }
                if (playerY > targetY) {
                    deltaY = -1;
                } else if (playerY < targetY) {
                    deltaY = 1;
                }
                boolean canMoveTarget = target.getSize() > 1;
                target.getUpdateState().setGraphicHeight100(254);
                if (canMoveTarget) {
                    if (target.isPlayer()) {
                        Player targetPlayer = (Player)target;
                        targetPlayer.packetSender.queueRelativeMovementStep(deltaX, deltaY, false);
                    } else {
                        Npc npc = (Npc)target;
                        npc.queuePathTo(new Position(npc.getPosition().getX() + deltaX, npc.getPosition().getY() + deltaY), false);
                    }
                }
                target.getStunTimer().setDelayTicks(10);
                target.getStunTimer().reset();
                return;
            }
            case 6: {
                if (!entity.isInMultiCombatArea()) break;
                int queuedTargets = 0;
                Player[] players = World.getPlayers();
                int playerCount = players.length;
                int playerIndex = 0;
                while (playerIndex < playerCount) {
                    Player targetPlayer = players[playerIndex];
                    if (targetPlayer != null && targetPlayer != entity && targetPlayer != entity.getCombatTarget()) {
                        AttackValidationResult validationResult = CombatCycleEvent.validateAttack(entity, targetPlayer);
                        if (GameUtil.getDistance(entity.getPosition(), targetPlayer.getPosition()) <= 1 && validationResult == AttackValidationResult.VALID) {
                            WeaponCombatAttack weaponCombatAttack = new WeaponCombatAttack(player, targetPlayer, player.getWeaponProfile());
                            HitDefinition extraHit = new HitDefinition(weaponCombatAttack.getAttackStyle(), HitType.NORMAL, CombatManager.calculateMeleeMaxHit(player, weaponCombatAttack)).enableRandomDamage().enableAccuracyCheck(true);
                            new CombatAction(entity, targetPlayer, extraHit).queue();
                            if (++queuedTargets > 13) break;
                        }
                    }
                    ++playerIndex;
                }
                Npc[] npcs = World.getNpcs();
                int npcCount = npcs.length;
                int npcIndex = 0;
                while (npcIndex < npcCount) {
                    Npc targetNpc = npcs[npcIndex];
                    if (targetNpc != null && targetNpc != entity.getCombatTarget()) {
                        AttackValidationResult validationResult = CombatCycleEvent.validateAttack(entity, targetNpc);
                        if (entity.isWithinReach(targetNpc, 1) && validationResult == AttackValidationResult.VALID) {
                            WeaponCombatAttack weaponCombatAttack = new WeaponCombatAttack(player, targetNpc, player.getWeaponProfile());
                            HitDefinition extraHit = new HitDefinition(weaponCombatAttack.getAttackStyle(), HitType.NORMAL, CombatManager.calculateMeleeMaxHit(player, weaponCombatAttack)).enableRandomDamage().enableAccuracyCheck(true);
                            new CombatAction(entity, targetNpc, extraHit).queue();
                            if (++queuedTargets > 13) break;
                        }
                    }
                    ++npcIndex;
                }
                return;
            }
            case 7: {
                target.getUpdateState().setGraphic(398);
                entity.heal(damage);
                return;
            }
            case 8: {
                target.getUpdateState().setGraphic(399);
                if (!target.isPlayer()) break;
                Player targetPlayer = (Player)target;
                targetPlayer.addRunEnergyPercent(-4);
                targetPlayer.packetSender.sendRunEnergy();
                return;
            }
            case 9: {
                target.getUpdateState().setGraphicHeight100(400);
                if (!target.isPlayer()) break;
                Player targetPlayer = (Player)target;
                targetPlayer.packetSender.modifySkillLevel(2, -5, false);
                return;
            }
            case 10: {
                target.getUpdateState().setGraphicHeight100(401);
                if (!target.isPlayer()) break;
                Player targetPlayer = (Player)target;
                targetPlayer.packetSender.modifySkillLevel(16, -((int)((double)targetPlayer.getSkillManager().getCurrentLevels()[16] * 0.2)), true);
                return;
            }
            case 11: {
                if (damage <= 0 || !target.isPlayer()) break;
                Player targetPlayer = (Player)target;
                targetPlayer.packetSender.sendGameMessage("You have been drained.");
                int remainder = targetPlayer.packetSender.modifySkillLevelReturningRemainder(1, -damage, true);
                if (remainder <= 0) break;
                if ((remainder = targetPlayer.packetSender.modifySkillLevelReturningRemainder(2, -remainder, true)) <= 0) break;
                if ((remainder = targetPlayer.packetSender.modifySkillLevelReturningRemainder(0, -remainder, true)) <= 0) break;
                if ((remainder = targetPlayer.packetSender.modifySkillLevelReturningRemainder(5, -remainder, true)) <= 0) break;
                if ((remainder = targetPlayer.packetSender.modifySkillLevelReturningRemainder(6, -remainder, true)) <= 0) break;
                targetPlayer.packetSender.modifySkillLevelReturningRemainder(4, -remainder, true);
                return;
            }
            case 12: {
                if (damage <= 0) break;
                int hitpointsRestore = 10;
                int prayerRestore = 5;
                if (damage > 21) {
                    hitpointsRestore = damage / 2;
                    prayerRestore = damage / 4;
                }
                player.packetSender.modifySkillLevelReturningRemainder(3, hitpointsRestore, true);
                player.packetSender.modifySkillLevelReturningRemainder(5, prayerRestore, true);
                return;
            }
            case 13: {
                HitDefinition extraHit = new HitDefinition(ServerSettings.SARADOMIN_SWORD_ATTACK_STYLE, HitType.NORMAL, 16).enableRandomDamage().setAccuracyMultiplier(1.0);
                new CombatAction(entity, target, extraHit).queue();
                return;
            }
            case 14: {
                if (GameUtil.randomInt(100) >= 6) break;
                int maxHit = player.getSkillManager().getCurrentLevels()[4] / 5;
                HitDefinition extraHit = new HitDefinition(ServerSettings.DRAGONFIRE_ATTACK_STYLE, HitType.NORMAL, maxHit).setAccuracyMultiplier(1.0);
                new CombatAction(entity, target, extraHit).queue();
                target.getUpdateState().setGraphic(756);
            }
        }
    }

    public static SpecialAttackDefinition[] values() {
        return VALUES.clone();
    }

    public static SpecialAttackDefinition valueOf(String text2) {
        if (text2 == null) {
            throw new NullPointerException("Name is null");
        }
        SpecialAttackDefinition[] definitions = SpecialAttackDefinition.values();
        int count = definitions.length;
        int index = 0;
        while (index < count) {
            SpecialAttackDefinition definition = definitions[index];
            if (definition.name().equals(text2)) {
                return definition;
            }
            ++index;
        }
        throw new IllegalArgumentException("No enum constant com.rs2.model.combat.special.SpecialAttackDefinition." + text2);
    }
}

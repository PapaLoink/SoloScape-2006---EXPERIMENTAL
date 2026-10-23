package com.rs2.bot.combat;

import com.rs2.ServerSettings;
import com.rs2.bot.ClanWarsBotManager;
import com.rs2.bot.combat.BotCombatHelper;
import com.rs2.bot.combat.BotCombatLoadoutTables;
import com.rs2.bot.combat.BotGroupCombatTickTask;
import com.rs2.bot.combat.BotPvpCombatHandler;
import com.rs2.bot.combat.BotPvpTargetSearchTickTask;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.GameplayHelper;
import com.rs2.model.World;
import com.rs2.model.combat.special.SpecialAttackDefinition;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.item.consumable.FoodDefinition;
import com.rs2.model.item.consumable.PotionHandler;
import com.rs2.model.player.Player;
import com.rs2.model.skill.SkillManager;
import com.rs2.model.skill.magic.SpellDefinition;
import com.rs2.model.skill.magic.Spellbook;
import com.rs2.model.task.TickTask;
import com.rs2.util.GameUtil;
import java.util.ArrayList;

public final class BotCombatLoadoutManager {
    public static void initializeCombatLoadoutTypes() {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        arrayList.add(0);
        arrayList.add(1);
        arrayList.add(2);
        arrayList.add(7);
        arrayList.add(4);
        arrayList.add(6);
        arrayList.add(5);
        if (ItemDefinition.isDefined(6528)) {
            arrayList.add(8);
        }
        if (ItemDefinition.isDefined(6111)) {
            arrayList.add(9);
        }
        if (ItemDefinition.isDefined(4411)) {
            arrayList.add(10);
        }
        int[] integerValues = new int[arrayList.size()];
        int index = 0;
        while (index < arrayList.size()) {
            integerValues[index] = (Integer)arrayList.get(index);
            ++index;
        }
        BotCombatLoadoutTables.enabledCombatLoadoutTypes = integerValues;
    }

    public static void startCombatLoadoutBot(Player player) {
        Player player2;
        Player player3 = player;
        boolean enabled = false;
        player3.botCombatStyle = BotCombatLoadoutTables.enabledCombatLoadoutTypes[GameUtil.randomInt(BotCombatLoadoutTables.enabledCombatLoadoutTypes.length)];
        if (player3.botCombatStyle == 8 || player3.botCombatStyle == 9 || player3.botCombatStyle == 10 || player3.botCombatStyle == 11) {
            enabled = true;
        }
        while (BotCombatHelper.isFreeToPlayWorld() && enabled) {
            player3.botCombatStyle = BotCombatLoadoutTables.enabledCombatLoadoutTypes[GameUtil.randomInt(BotCombatLoadoutTables.enabledCombatLoadoutTypes.length)];
            enabled = player3.botCombatStyle == 8 || player3.botCombatStyle == 9 || player3.botCombatStyle == 10 || player3.botCombatStyle == 11;
        }
        GameplayHelper.resetBotTaskState(player3);
        if (ServerSettings.wildyBotsUseNewGeneration) {
            player2 = player3;
            GameplayHelper.resetBotSkillsToBase(player2);
            int value = ServerSettings.wildyBotsBaseCombatLevel - ServerSettings.wildyBotsCombatLevelSpread;
            if ((value += GameUtil.randomInt((ServerSettings.wildyBotsCombatLevelSpread << 1) + 1)) < 3) {
                value = 3;
            }
            if (value > SkillManager.maxCombatLevel) {
                value = SkillManager.maxCombatLevel;
            }
            while (player2.getCombatLevel() < value) {
                int[] integerValues = new int[6];
                integerValues[1] = 2;
                integerValues[2] = 1;
                integerValues[3] = 4;
                integerValues[4] = 6;
                integerValues[5] = 5;
                int[] integerValues2 = integerValues;
                int value2 = integerValues[GameUtil.randomInt(6)];
                player2.getSkillManager();
                int levelForExperience = SkillManager.getLevelForExperience(player2.getSkillManager().getExperience()[value2]);
                BotCombatHelper.setBotSkillLevel(player2, value2, levelForExperience + 1);
                player2.getSkillManager().getExperience()[3] = BotCombatHelper.calculateBotHitpointsExperience(player2);
                int[] skillManager = player2.getSkillManager().getCurrentLevels();
                player2.getSkillManager();
                skillManager[3] = SkillManager.getLevelForExperience(player2.getSkillManager().getExperience()[3]);
            }
            player2.getSkillManager().refreshAllSkills();
            BotCombatLoadoutManager.selectCombatStyleFromStats(player2, true);
            player2 = player3;
            player2.getInventoryManager().getContainer().clear();
            player2.getEquipmentManager().getContainer().clear();
            if (player2.botCombatStyle == 0) {
                player3 = player2;
                BotCombatLoadoutManager.prepareMeleeLoadout(player3, false);
                BotCombatLoadoutManager.equipGlovesAndBoots(player2);
            } else if (player2.botCombatStyle == 2) {
                BotCombatLoadoutManager.prepareMagicLoadout(player2);
            } else if (player2.botCombatStyle == 1) {
                BotCombatLoadoutManager.prepareRangedLoadout(player2);
                int skillManager2 = player2.getSkillManager().getCurrentLevels()[4] >= 40 ? 1731 : (GameUtil.randomInt(3) == 0 ? 1478 : 1729);
                if (!BotCombatHelper.isFreeToPlayWorld() && player2.getCombatLevel() >= 60 && GameUtil.randomInt(2) == 0) {
                    skillManager2 = 1712;
                }
                player2.getEquipmentManager().getContainer().setItem(2, new ItemStack(skillManager2));
            }
            BotCombatLoadoutManager.equipRandomCape(player2);
            BotCombatLoadoutManager.addCombatSupplies(player2);
            player2.getInventoryManager().refresh();
            player2.getEquipmentManager().refresh();
        } else {
            player2 = player3;
            int value3 = player2.botCombatStyle;
            GameplayHelper.resetBotSkillsToBase(player2);
            switch (value3) {
                case 4: {
                    BotCombatHelper.setBotSkillLevel(player2, 0, 40);
                    BotCombatHelper.setBotSkillLevel(player2, 2, 20 + GameUtil.randomInt(79) + 1);
                    if (GameUtil.randomInt(3) == 0) {
                        BotCombatHelper.setBotSkillLevel(player2, 1, 10);
                    }
                    if (GameUtil.randomInt(4) == 0) break;
                    BotCombatHelper.setBotSkillLevel(player2, 5, GameUtil.randomInt(2) == 0 ? 25 : 31);
                    break;
                }
                case 9: {
                    BotCombatHelper.setBotSkillLevel(player2, 0, 60);
                    BotCombatHelper.setBotSkillLevel(player2, 2, 60 + GameUtil.randomInt(20));
                    if (GameUtil.randomInt(4) != 0) {
                        BotCombatHelper.setBotSkillLevel(player2, 5, GameUtil.randomInt(2) == 0 ? 25 : 25 + GameUtil.randomInt(30));
                    }
                    BotCombatHelper.setBotSkillLevel(player2, 6, GameUtil.randomInt(2) == 0 ? 1 : 25);
                    break;
                }
                case 10: {
                    BotCombatHelper.setBotSkillLevel(player2, 0, 70);
                    BotCombatHelper.setBotSkillLevel(player2, 2, 70 + GameUtil.randomInt(20));
                    BotCombatHelper.setBotSkillLevel(player2, 5, 52);
                    BotCombatHelper.setBotSkillLevel(player2, 6, GameUtil.randomInt(2) == 0 ? 1 : 25);
                    break;
                }
                case 6: {
                    BotCombatHelper.setBotSkillLevel(player2, 6, 1 + (BotCombatHelper.isFreeToPlayWorld() ? GameUtil.randomInt(59) : GameUtil.randomInt(99)));
                    break;
                }
                case 5: {
                    BotCombatHelper.setBotSkillLevel(player2, 4, 1 + GameUtil.randomInt(99));
                    break;
                }
                case 8: {
                    BotCombatHelper.setBotSkillLevel(player2, 0, GameUtil.randomInt(2) == 0 ? 1 : 50);
                    BotCombatHelper.setBotSkillLevel(player2, 2, 60 + GameUtil.randomInt(30));
                    BotCombatHelper.setBotSkillLevel(player2, 4, 50 + GameUtil.randomInt(20));
                    BotCombatHelper.setBotSkillLevel(player2, 5, GameUtil.randomInt(2) == 0 ? 25 : 31);
                    BotCombatHelper.setBotSkillLevel(player2, 6, GameUtil.randomInt(2) == 0 ? 1 : 25);
                    break;
                }
                case 0: 
                case 1: 
                case 2: 
                case 3: 
                case 7: {
                    int value4;
                    int value5 = 1 + GameUtil.randomInt(99);
                    int value6 = value5 / 5 << 1;
                    if (value6 == 0) {
                        value6 = 2;
                    }
                    if ((value4 = value5 - value5 / 5 + GameUtil.randomInt(value6)) < 40) {
                        value4 = 40;
                    }
                    BotCombatHelper.setBotSkillLevel(player2, 0, value4);
                    value6 = value5 / 5 << 1;
                    if (value6 == 0) {
                        value6 = 2;
                    }
                    if ((value4 = value5 - value5 / 5 + GameUtil.randomInt(value6)) < 40) {
                        value4 = 40;
                    }
                    BotCombatHelper.setBotSkillLevel(player2, 2, value4);
                    value6 = value5 / 5 << 1;
                    if (value6 == 0) {
                        value6 = 2;
                    }
                    if ((value4 = value5 - value5 / 5 + GameUtil.randomInt(value6)) < 40) {
                        value4 = 40;
                    }
                    BotCombatHelper.setBotSkillLevel(player2, 1, value4);
                    value6 = value5 / 5 << 1;
                    if (value6 == 0) {
                        value6 = 2;
                    }
                    if ((value4 = value5 - value5 / 5 + GameUtil.randomInt(value6)) < 40) {
                        value4 = 40;
                    }
                    BotCombatHelper.setBotSkillLevel(player2, 4, value4);
                    value6 = value5 / 5 << 1;
                    if (value6 == 0) {
                        value6 = 2;
                    }
                    if ((value4 = value5 - value5 / 5 + GameUtil.randomInt(value6)) < 40) {
                        value4 = 40;
                    }
                    BotCombatHelper.setBotSkillLevel(player2, 6, value4);
                    value6 = value5 / 5 << 1;
                    if (value6 == 0) {
                        value6 = 2;
                    }
                    if ((value4 = value5 - (value5 / 5 << 1) + GameUtil.randomInt(value6)) < 40) {
                        value4 = 40;
                    }
                    BotCombatHelper.setBotSkillLevel(player2, 5, value4);
                    if (!BotCombatHelper.isFreeToPlayWorld()) {
                        value6 = value5 / 5 << 1;
                        if (value6 == 0) {
                            value6 = 2;
                        }
                        if ((value4 = value5 - (value5 / 5 << 1) + GameUtil.randomInt(value6)) < 40) {
                            value4 = 40;
                        }
                        BotCombatHelper.setBotSkillLevel(player2, 16, value4);
                    }
                    BotCombatLoadoutManager.selectCombatStyleFromStats(player2, false);
                }
            }
            player2.getSkillManager().getExperience()[3] = BotCombatHelper.calculateBotHitpointsExperience(player2);
            int[] skillManager3 = player2.getSkillManager().getCurrentLevels();
            player2.getSkillManager();
            skillManager3[3] = SkillManager.getLevelForExperience(player2.getSkillManager().getExperience()[3]);
            player2.getSkillManager().refreshAllSkills();
            BotCombatLoadoutManager.prepareCombatLoadout(player3, false);
        }
        player.botEnabled = true;
        BotCombatHelper.prepareBotPvpSearchPosition(player);
        player3 = player;
        boolean enabled2 = player3.botCombatStyle == 4 || player3.botCombatStyle == 6 || player3.botCombatStyle == 5;
        BotPvpTargetSearchTickTask botPvpTargetSearchTickTask = new BotPvpTargetSearchTickTask(10, player3, enabled2);
        World.getTaskScheduler().schedule(botPvpTargetSearchTickTask);
    }

    public static void startGroupCombatBot(Player player) {
        Object value;
        Object value2;
        Player player2 = player;
        player.botEnabled = true;
        player2.botEscapeStuckTicks = 0;
        player2.botCombatEscapeActive = false;
        player2.botAntipoisonAvailable = false;
        player2.setAutoRetaliate(true);
        player2.botMagicPenaltyGearUnequipped = false;
        player2.botCombatState = null;
        player2.botCombatSpell = null;
        player2.botFoodDepleted = false;
        player2.botStrengthPotionDepleted = false;
        player2.botSpecialAttackEnergyCost = 0;
        player2.botSpecialCombatStyle = 0;
        player2.botWeaponItemId = player2.getEquipmentManager().getItemIdAtSlot(3);
        player2.botPrimaryCombatStyle = 0;
        player2.botCombatStyle = 0;
        if (player2.botWeaponItemId > 0) {
            value2 = ItemDefinition.forId(player2.botWeaponItemId).getName().toLowerCase();
            if (((String)value2).contains("bow") || ((String)value2).contains("knife") || ((String)value2).contains("dart") || ((String)value2).contains("javelin") || ((String)value2).contains("thrownaxe")) {
                player2.botPrimaryCombatStyle = BotPvpCombatHandler.RANGED_COMBAT_STYLE;
                player2.botCombatStyle = 1;
            } else if (((String)value2).contains("staff")) {
                player2.botPrimaryCombatStyle = BotPvpCombatHandler.MAGIC_COMBAT_STYLE;
                player2.botCombatStyle = 2;
            }
        }
        player2.botActiveCombatStyle = player2.botPrimaryCombatStyle;
        player2.botShieldItemId = player2.getEquipmentManager().getItemIdAtSlot(5);
        ItemStack[] itemStackArray = player2.getInventoryManager().getContainer().getItems();
        int length = itemStackArray.length;
        int index = 0;
        while (index < length) {
            value2 = itemStackArray[index];
            if (value2 != null && ((ItemStack)value2).getDefinition().getEquipmentSlot() == 3) {
                player2.botSpecialWeaponItemId = ((ItemStack)value2).getId();
                player2.botSpecialCombatStyle = 0;
                break;
            }
            ++index;
        }
        itemStackArray = player2.getInventoryManager().getContainer().getItems();
        length = itemStackArray.length;
        index = 0;
        while (index < length) {
            value2 = itemStackArray[index];
            if (value2 != null && (value = FoodDefinition.forItemId(((ItemStack)value2).getId())) != null) {
                player2.botFoodItemId = ((ItemStack)value2).getId();
                break;
            }
            ++index;
        }
        itemStackArray = player2.getInventoryManager().getContainer().getItems();
        length = itemStackArray.length;
        index = 0;
        while (index < length) {
            value2 = itemStackArray[index];
            if (value2 != null && player2.getPotionHandler().selectPotionForItemId(((ItemStack)value2).getId()) && PotionHandler.definitions[player2.getPotionHandler().selectedDefinitionIndex].isAntipoison()) {
                player2.botAntipoisonAvailable = true;
                break;
            }
            ++index;
        }
        if (player2.botPrimaryCombatStyle == BotPvpCombatHandler.MAGIC_COMBAT_STYLE) {
            Player magicPlayer = player2;
            ArrayList<SpellDefinition> arrayList = new ArrayList<SpellDefinition>();
            SpellDefinition[] spellDefinitions = BotCombatLoadoutTables.standardCombatSpellProgression;
            int index2 = 0;
            while (index2 < 12) {
                SpellDefinition spellDefinition = spellDefinitions[index2];
                arrayList.add(spellDefinition);
                ++index2;
            }
            if (!ServerSettings.freeToPlayWorld) {
                spellDefinitions = BotCombatLoadoutTables.standardWaveSpellProgression;
                index2 = 0;
                while (index2 < 4) {
                    SpellDefinition spellDefinition = spellDefinitions[index2];
                    arrayList.add(spellDefinition);
                    ++index2;
                }
                if (magicPlayer.botWeaponItemId == 4675) {
                    arrayList = new ArrayList<SpellDefinition>();
                    spellDefinitions = BotCombatLoadoutTables.ancientCombatSpellProgression;
                    index2 = 0;
                    while (index2 < 16) {
                        SpellDefinition spellDefinition = spellDefinitions[index2];
                        arrayList.add(spellDefinition);
                        ++index2;
                    }
                }
            }
            int initialValue = -1;
            index2 = 0;
            while (index2 < arrayList.size()) {
                SpellDefinition spellDefinition = arrayList.get(index2);
                if (spellDefinition.getRequiredLevel() > magicPlayer.getSkillManager().getCurrentLevels()[6]) break;
                if (magicPlayer.getSkillManager().getCurrentLevels()[6] >= spellDefinition.getRequiredLevel() && BotCombatHelper.hasRunesForSpell(magicPlayer, spellDefinition)) {
                    initialValue = index2;
                }
                ++index2;
            }
            if (initialValue != -1) {
                magicPlayer.setAutocastSpell(arrayList.get(initialValue));
            }
            if (!ServerSettings.freeToPlayWorld) {
                if (SpellDefinition.ENTANGLE.getRequiredLevel() <= player2.getSkillManager().getCurrentLevels()[6] && BotCombatHelper.hasRunesForSpell(player2, SpellDefinition.ENTANGLE)) {
                    player2.botCombatSpell = SpellDefinition.ENTANGLE;
                } else if (SpellDefinition.SNARE.getRequiredLevel() <= player2.getSkillManager().getCurrentLevels()[6] && BotCombatHelper.hasRunesForSpell(player2, SpellDefinition.SNARE)) {
                    player2.botCombatSpell = SpellDefinition.SNARE;
                } else if (SpellDefinition.BIND.getRequiredLevel() <= player2.getSkillManager().getCurrentLevels()[6] && BotCombatHelper.hasRunesForSpell(player2, SpellDefinition.BIND)) {
                    player2.botCombatSpell = SpellDefinition.BIND;
                }
            } else if (SpellDefinition.BIND.getRequiredLevel() <= player2.getSkillManager().getCurrentLevels()[6] && BotCombatHelper.hasRunesForSpell(player2, SpellDefinition.BIND)) {
                player2.botCombatSpell = SpellDefinition.BIND;
            }
            if (player2.getAutocastSpell() == null) {
                System.out.print("Warning! playerBot detected as mage, but no autospell set!");
                player2.packetSender.sendGameMessage("Warning! playerBot detected as mage, but no autospell set!");
            }
        }
        player2 = player;
        World.getTaskScheduler().schedule(new BotGroupCombatTickTask(1, player2));
    }

    public static void selectCombatStyleFromStats(Player player, boolean enabled2) {
        int skillManager = (player.getSkillManager().getCurrentLevels()[0] + player.getSkillManager().getCurrentLevels()[2]) / 2;
        int skillManager2 = player.getSkillManager().getCurrentLevels()[6];
        int skillManager3 = player.getSkillManager().getCurrentLevels()[4];
        if (skillManager3 > skillManager && skillManager3 > skillManager2) {
            player.botCombatStyle = 1;
            return;
        }
        if (skillManager2 > skillManager && skillManager2 > skillManager3) {
            player.botCombatStyle = 2;
            return;
        }
        if (skillManager3 == skillManager && skillManager2 == skillManager) {
            int value = GameUtil.randomInt(3);
            if (value == 0) {
                player.botCombatStyle = 1;
                return;
            }
            if (value == 1) {
                player.botCombatStyle = 2;
                return;
            }
            if (value == 2) {
                player.botCombatStyle = 0;
                return;
            }
        } else {
            player.botCombatStyle = 0;
            if (!enabled2 && GameUtil.randomInt(5) == 0 && player.getSkillManager().getCurrentLevels()[0] >= 40 && player.getSkillManager().getCurrentLevels()[1] >= 40 && skillManager3 >= 40) {
                player.botCombatStyle = 7;
            }
        }
    }

    public static void equipRandomCape(Player player) {
        int value = !BotCombatHelper.isFreeToPlayWorld() && GameUtil.randomInt(2) == 0 && ItemDefinition.isDefined(4413) ? BotCombatLoadoutTables.castleWarsCapeIds[GameUtil.randomInt(55)] : BotCombatHelper.selectBotLoadoutItemId(player, BotCombatLoadoutTables.capeIds, BotCombatLoadoutTables.teamCapeIds, true);
        player.getEquipmentManager().getContainer().setItem(1, new ItemStack(value));
    }

    public static void equipGlovesAndBoots(Player player) {
        int value = 1061;
        int value2 = 1059;
        if (!BotCombatHelper.isFreeToPlayWorld()) {
            int[] integerValues = BotCombatHelper.filterEquippableMemberLoadoutItems(player, null, BotCombatLoadoutTables.coloredMageBootIds);
            int[] integerValues2 = BotCombatHelper.filterEquippableMemberLoadoutItems(player, null, BotCombatLoadoutTables.coloredMageGloveIds);
            if (integerValues2.length > 0) {
                value2 = GameUtil.randomInt(integerValues2.length);
                value = integerValues[value2];
                value2 = integerValues2[value2];
            }
        }
        player.getEquipmentManager().getContainer().setItem(9, new ItemStack(value2));
        player.getEquipmentManager().getContainer().setItem(10, new ItemStack(value));
    }

    private static void addCombatSupplies(Player player) {
        int value;
        if (player.getSkillManager().getCurrentLevels()[3] < 25) {
            value = GameUtil.randomInt(2) == 0 ? 333 : 329;
        } else if (player.getSkillManager().getCurrentLevels()[3] >= 25 && player.getSkillManager().getCurrentLevels()[3] < 40) {
            value = GameUtil.randomInt(2) == 0 ? 361 : 379;
        } else {
            int value2 = value = GameUtil.randomInt(2) == 0 ? 379 : 373;
        }
        if (!BotCombatHelper.isFreeToPlayWorld()) {
            if (GameUtil.randomInt(2) == 0) {
                if (player.getSkillManager().getCurrentLevels()[3] >= 60) {
                    value = 385;
                }
            } else if (GameUtil.randomInt(3) == 0 && player.getSkillManager().getCurrentLevels()[3] >= 80) {
                value = 391;
            }
        }
        player.getInventoryManager().addItem(new ItemStack(value, 15));
        player.botFoodItemId = value;
        if (GameUtil.randomInt(100) != 0) {
            if (player.getSpellbook() == Spellbook.MODERN) {
                if (SpellDefinition.VARROCK_TELEPORT.getRequiredLevel() <= player.getSkillManager().getCurrentLevels()[6]) {
                    BotCombatHelper.grantBotSpellRunes(player, SpellDefinition.VARROCK_TELEPORT, 1);
                }
            } else if (player.getSpellbook() == Spellbook.ANCIENT && SpellDefinition.PADDEWWA_TELEPORT.getRequiredLevel() <= player.getSkillManager().getCurrentLevels()[6]) {
                BotCombatHelper.grantBotSpellRunes(player, SpellDefinition.PADDEWWA_TELEPORT, 1);
            }
        }
        if (!BotCombatHelper.isFreeToPlayWorld() && player.getCombatLevel() >= 25 && GameUtil.randomInt(2) == 0) {
            player.getInventoryManager().addItem(new ItemStack(2446, 1));
            player.botAntipoisonAvailable = true;
        }
    }

    private static int selectSpecialAttackWeapon(Player player) {
        int value;
        if (player.botCombatStyle == 9 || player.botCombatStyle == 10) {
            value = 1231;
        } else if (player.botCombatStyle == 8) {
            value = player.getEquipmentManager().canEquipItem(4153) ? 4153 : 6528;
        } else {
            int value2;
            value = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.twoHandedSwordIds, null);
            if (!BotCombatHelper.isFreeToPlayWorld() && GameUtil.randomInt(2) == 0 && (value2 = BotCombatHelper.selectBotLoadoutItemId(player, null, BotCombatLoadoutTables.specialMeleeWeaponIds, true)) != -1) {
                value = value2;
            }
        }
        player.botSpecialWeaponItemId = value;
        player.botSpecialCombatStyle = 0;
        SpecialAttackDefinition specialAttackDefinition = SpecialAttackDefinition.forItem(new ItemStack(player.botSpecialWeaponItemId));
        if (specialAttackDefinition != null) {
            player.botSpecialAttackEnergyCost = specialAttackDefinition.getEnergyCost();
        }
        return player.botSpecialWeaponItemId;
    }

    public static void prepareMeleeLoadout(Player player) {
        BotCombatLoadoutManager.prepareMeleeLoadout(player, false);
    }

    public static void prepareMeleeLoadout(Player player, boolean enabled3) {
        int value;
        int value2;
        int value3;
        int value4;
        boolean enabled2;
        int value5;
        prepareMeleeLoadoutControlExit1: {
            int value6 = GameUtil.randomInt(enabled3 ? 3 : 4);
            value5 = 0;
            enabled2 = false;
            if (value6 == 0) {
                value5 = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.longswordIds, BotCombatLoadoutTables.dragonLongswordIds);
            } else if (value6 == 1) {
                value5 = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.scimitarIds, !ItemDefinition.isDefined(4587) ? null : BotCombatLoadoutTables.dragonScimitarIds);
            } else if (value6 == 2) {
                value5 = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.battleaxeIds, null);
            } else if (value6 == 3) {
                value5 = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.twoHandedSwordIds, null);
                enabled2 = true;
            }
            player.botWeaponItemId = value5;
            player.getEquipmentManager().getContainer().setItem(3, new ItemStack(player.botWeaponItemId));
            if (player.currentBotTask == null) {
                value6 = BotCombatLoadoutManager.selectSpecialAttackWeapon(player);
                if (GameUtil.randomInt(3) == 0 && player.botWeaponItemId != value6) {
                    player.botSpecialWeaponItemId = value6;
                    player.getInventoryManager().addItem(new ItemStack(player.botSpecialWeaponItemId, 1));
                }
            }
            value6 = 0;
            value5 = 0;
            while (value5 < 7) {
                value4 = new ItemStack(BotCombatLoadoutTables.platebodyIds[value5]).getDefinition().getRequiredLevel(1);
                if (value4 > player.getSkillManager().getCurrentLevels()[1]) {
                    value6 = value5 - 1;
                    break;
                }
                value6 = value5++;
            }
            int value7 = value5 = GameUtil.randomInt(2) == 0 ? BotCombatLoadoutTables.mediumHelmetIds[value6] : BotCombatLoadoutTables.fullHelmetIds[value6];
            if (!BotCombatHelper.isFreeToPlayWorld() && GameUtil.randomInt(2) == 0 && (value4 = BotCombatHelper.selectBotLoadoutItemId(player, null, BotCombatLoadoutTables.rareMeleeHelmetIds, true)) != -1) {
                value5 = value4;
            }
            value4 = GameUtil.randomInt(2) == 0 ? BotCombatLoadoutTables.chainbodyIds[value6] : BotCombatLoadoutTables.platebodyIds[value6];
            value3 = GameUtil.randomInt(2) == 0 ? BotCombatLoadoutTables.plateskirtIds[value6] : BotCombatLoadoutTables.platelegIds[value6];
            int value8 = value2 = GameUtil.randomInt(2) == 0 ? BotCombatLoadoutTables.squareShieldIds[value6] : BotCombatLoadoutTables.kiteshieldIds[value6];
            if (player.currentBotTask == null) {
                if (GameUtil.randomInt(4) == 0) {
                    value2 = 1540;
                }
            } else if (enabled3) {
                value2 = 1540;
            }
            if (value6 != 2 && value6 != 4 && value6 != 5) break prepareMeleeLoadoutControlExit1;
            int index = 0;
            int value9 = GameUtil.randomInt(3);
            if (value6 == 4) {
                index = 1;
            } else if (value6 == 5) {
                index = 2;
            }
            int index2 = 0;
            while (index2 < 4) {
                int value10 = value6 == 5 ? 1500 : 1000;
                value10 = GameUtil.randomInt(value10);
                if (value10 == 0) {
                    if (index2 == 0) {
                        value5 = BotCombatLoadoutTables.trimmedFullHelmetIds[index];
                    } else if (index2 == 1) {
                        value4 = BotCombatLoadoutTables.trimmedPlatebodyIds[index];
                    } else if (index2 == 2) {
                        value3 = GameUtil.randomInt(2) == 0 ? BotCombatLoadoutTables.trimmedPlateskirtIds[index] : BotCombatLoadoutTables.trimmedPlatelegIds[index];
                    } else if (index2 == 3) {
                        value2 = BotCombatLoadoutTables.trimmedKiteshieldIds[index];
                    }
                } else if (value10 == 1) {
                    if (index2 == 0) {
                        value5 = BotCombatLoadoutTables.goldTrimmedFullHelmetIds[index];
                    } else if (index2 == 1) {
                        value4 = BotCombatLoadoutTables.goldTrimmedPlatebodyIds[index];
                    } else if (index2 == 2) {
                        value3 = GameUtil.randomInt(2) == 0 ? BotCombatLoadoutTables.goldTrimmedPlateskirtIds[index] : BotCombatLoadoutTables.goldTrimmedPlatelegIds[index];
                    } else if (index2 == 3) {
                        value2 = BotCombatLoadoutTables.goldTrimmedKiteshieldIds[index];
                    }
                } else if (value10 == 3 && value6 == 5) {
                    if (index2 == 0) {
                        value5 = BotCombatLoadoutTables.godFullHelmetIds[value9];
                    } else if (index2 == 1) {
                        value4 = BotCombatLoadoutTables.godPlatebodyIds[value9];
                    } else if (index2 == 2) {
                        value3 = GameUtil.randomInt(2) == 0 ? BotCombatLoadoutTables.godPlateskirtIds[value9] : BotCombatLoadoutTables.godPlatelegIds[value9];
                    } else if (index2 == 3) {
                        value2 = BotCombatLoadoutTables.godKiteshieldIds[value9];
                    }
                }
                ++index2;
            }
        }
        player.getEquipmentManager().getContainer().setItem(0, new ItemStack(value5));
        player.getEquipmentManager().getContainer().setItem(4, new ItemStack(value4));
        player.getEquipmentManager().getContainer().setItem(7, new ItemStack(value3));
        if (!enabled2) {
            player.botShieldItemId = value2;
            player.getEquipmentManager().getContainer().setItem(5, new ItemStack(player.botShieldItemId));
        }
        if (player.getCombatLevel() <= 15) {
            value = 1478;
        } else if (player.getCombatLevel() <= 40 && player.getCombatLevel() > 15) {
            value = GameUtil.randomInt(2) == 0 ? 1725 : 1729;
        } else {
            value = 1731;
        }
        if (!BotCombatHelper.isFreeToPlayWorld() && player.getCombatLevel() >= 60 && GameUtil.randomInt(2) == 0) {
            value = 1712;
        }
        player.getEquipmentManager().getContainer().setItem(2, new ItemStack(value));
    }

    public static void prepareRangedLoadout(Player player) {
        int value;
        int value2;
        int value3 = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.basicRangedHeadIds, (int[])(ItemDefinition.isDefined(3749) ? BotCombatLoadoutTables.rareRangedHeadIds : null));
        int value4 = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.basicRangedBodyIds, BotCombatLoadoutTables.dragonhideBodyIds);
        int value5 = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.basicRangedLegIds, BotCombatLoadoutTables.dragonhideChapsIds);
        int value6 = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.basicRangedVambraceIds, BotCombatLoadoutTables.dragonhideVambraceIds);
        if (ServerSettings.cacheVersion >= 306) {
            value2 = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.post306BowIds, BotCombatLoadoutTables.post306HighTierBowIds);
            value = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.post306ArrowIds, BotCombatLoadoutTables.post306HighTierArrowIds);
        } else {
            value2 = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.legacyBowIds, BotCombatLoadoutTables.legacyHighTierBowIds);
            value = BotCombatHelper.selectBestBotLoadoutItemId(player, BotCombatLoadoutTables.legacyArrowIds, BotCombatLoadoutTables.legacyHighTierArrowIds);
        }
        int value7 = 40 + GameUtil.randomInt(20);
        if (player.clanWarsBot) {
            value7 *= ClanWarsBotManager.clanWarsSupplyMultiplier;
        }
        if (player.currentBotTask != null) {
            value7 = 1000;
        }
        player.botWeaponItemId = value2;
        player.getEquipmentManager().getContainer().setItem(0, new ItemStack(value3));
        player.getEquipmentManager().getContainer().setItem(7, new ItemStack(value5));
        player.getEquipmentManager().getContainer().setItem(4, new ItemStack(value4));
        player.getEquipmentManager().getContainer().setItem(9, new ItemStack(value6));
        player.getEquipmentManager().getContainer().setItem(3, new ItemStack(player.botWeaponItemId));
        player.getEquipmentManager().getContainer().setItem(13, new ItemStack(value, value7));
        player.getEquipmentManager().getContainer().setItem(10, new ItemStack(1061));
        if (player.currentBotTask != null) {
            ItemStack[] itemStackArray;
            if (player.botTaskRequiredItems != null) {
                itemStackArray = new ItemStack[player.botTaskRequiredItems.length + 1];
                value4 = 0;
                while (value4 < itemStackArray.length - 1) {
                    itemStackArray[value4] = player.botTaskRequiredItems[value4];
                    ++value4;
                }
                itemStackArray[player.botTaskRequiredItems.length] = new ItemStack(value, value7);
            } else {
                ItemStack[] itemStackArray2 = new ItemStack[1];
                itemStackArray = itemStackArray2;
                itemStackArray2[0] = new ItemStack(value, value7);
            }
            player.getBankContainer().addToTab(new ItemStack(value, value7 * 10), 0);
            player.botTaskRequiredItems = itemStackArray;
        }
    }

    public static void prepareMagicLoadout(Player player) {
        int[] basicHeadIds = new int[]{579, 1017};
        int[] basicBodyIds = new int[]{577, 546};
        int[] basicLegIds = new int[]{1011, 548};
        int styleRollLimit = BotCombatHelper.isFreeToPlayWorld() ? 2 : 3;
        int styleRoll = GameUtil.randomInt(styleRollLimit);
        int headId;
        int bodyId;
        int legId;
        int bootsId;
        int glovesId;
        if (styleRoll == 2) {
            int[] coloredHeadIds = BotCombatHelper.filterEquippableMemberLoadoutItems(player, null, BotCombatLoadoutTables.coloredMageHeadIds);
            int[] coloredBodyIds = BotCombatHelper.filterEquippableMemberLoadoutItems(player, null, BotCombatLoadoutTables.coloredMageBodyIds);
            int[] coloredLegIds = BotCombatHelper.filterEquippableMemberLoadoutItems(player, null, BotCombatLoadoutTables.coloredMageLegIds);
            int[] coloredBootIds = BotCombatHelper.filterEquippableMemberLoadoutItems(player, null, BotCombatLoadoutTables.coloredMageBootIds);
            int[] coloredGloveIds = BotCombatHelper.filterEquippableMemberLoadoutItems(player, null, BotCombatLoadoutTables.coloredMageGloveIds);
            if (coloredGloveIds.length > 0) {
                int colorIndex = GameUtil.randomInt(coloredGloveIds.length);
                headId = coloredHeadIds[colorIndex];
                bodyId = coloredBodyIds[colorIndex];
                legId = coloredLegIds[colorIndex];
                bootsId = coloredBootIds[colorIndex];
                glovesId = coloredGloveIds[colorIndex];
            } else {
                int basicIndex = GameUtil.randomInt(2);
                headId = basicHeadIds[basicIndex];
                bodyId = basicBodyIds[basicIndex];
                legId = basicLegIds[basicIndex];
                bootsId = 1061;
                glovesId = 1059;
            }
        } else {
            headId = basicHeadIds[styleRoll];
            bodyId = basicBodyIds[styleRoll];
            legId = basicLegIds[styleRoll];
            bootsId = 1061;
            glovesId = 1059;
        }
        boolean skipRuneGrant = player.tradeAdvertMode != -1 || player.dropPartyLeader || player.dropPartyFollower;
        int weaponId = 1381;
        boolean ancientSpellbook = false;
        ArrayList<SpellDefinition> spellProgression = new ArrayList<SpellDefinition>();
        SpellDefinition[] standardSpells = BotCombatLoadoutTables.standardCombatSpellProgression;
        int i = 0;
        while (i < 12) {
            spellProgression.add(standardSpells[i]);
            ++i;
        }
        if (!BotCombatHelper.isFreeToPlayWorld()) {
            SpellDefinition[] waveSpells = BotCombatLoadoutTables.standardWaveSpellProgression;
            i = 0;
            while (i < 4) {
                spellProgression.add(waveSpells[i]);
                ++i;
            }
            if (player.getSkillManager().getCurrentLevels()[6] >= 50 && ItemDefinition.isDefined(4675) && GameUtil.randomInt(3) == 0) {
                ancientSpellbook = true;
                player.packetSender.setSidebarInterface(6, 12855);
                player.setSpellbook(Spellbook.ANCIENT);
                spellProgression = new ArrayList<SpellDefinition>();
                SpellDefinition[] ancientSpells = BotCombatLoadoutTables.ancientCombatSpellProgression;
                i = 0;
                while (i < 16) {
                    spellProgression.add(ancientSpells[i]);
                    ++i;
                }
            }
        }
        int selectedSpellIndex = 0;
        i = 0;
        while (i < spellProgression.size()) {
            SpellDefinition candidateSpell = spellProgression.get(i);
            if (candidateSpell.getRequiredLevel() > player.getSkillManager().getCurrentLevels()[6]) {
                selectedSpellIndex = i - 1;
                break;
            }
            selectedSpellIndex = i;
            ++i;
        }
        SpellDefinition selectedSpell = spellProgression.get(selectedSpellIndex);
        player.botPrimaryAutocastSpell = selectedSpell;
        if (ancientSpellbook) {
            weaponId = 4675;
        } else if (selectedSpell == SpellDefinition.WIND_STRIKE || selectedSpell == SpellDefinition.WIND_BOLT || selectedSpell == SpellDefinition.WIND_BLAST || selectedSpell == SpellDefinition.WIND_WAVE) {
            weaponId = 1381;
        } else if (selectedSpell == SpellDefinition.WATER_STRIKE || selectedSpell == SpellDefinition.WATER_BOLT || selectedSpell == SpellDefinition.WATER_BLAST || selectedSpell == SpellDefinition.WATER_WAVE) {
            weaponId = 1383;
        } else if (selectedSpell == SpellDefinition.EARTH_STRIKE || selectedSpell == SpellDefinition.EARTH_BOLT || selectedSpell == SpellDefinition.EARTH_BLAST || selectedSpell == SpellDefinition.EARTH_WAVE) {
            weaponId = 1385;
        } else if (selectedSpell == SpellDefinition.FIRE_STRIKE || selectedSpell == SpellDefinition.FIRE_BOLT || selectedSpell == SpellDefinition.FIRE_BLAST || selectedSpell == SpellDefinition.FIRE_WAVE) {
            weaponId = 1387;
        }
        player.botWeaponItemId = weaponId;
        player.getEquipmentManager().getContainer().setItem(3, new ItemStack(player.botWeaponItemId));
        if (!skipRuneGrant) {
            int runeAmount = 30 + GameUtil.randomInt(30);
            if (player.clanWarsBot) {
                runeAmount *= ClanWarsBotManager.clanWarsSupplyMultiplier;
            }
            if (player.currentBotTask != null) {
                runeAmount = 1000;
            }
            BotCombatHelper.grantBotSpellRunes(player, selectedSpell, runeAmount);
            if (!ancientSpellbook && player.currentBotTask == null) {
                if (!BotCombatHelper.isFreeToPlayWorld() && GameUtil.randomInt(3) == 0 && 12445 < InterfaceDefinition.interfaceCount && SpellDefinition.TELE_BLOCK.getRequiredLevel() <= player.getSkillManager().getCurrentLevels()[6]) {
                    runeAmount = GameUtil.randomInt(11);
                    BotCombatHelper.grantBotSpellRunes(player, SpellDefinition.TELE_BLOCK, runeAmount);
                }
                if (GameUtil.randomInt(3) == 0) {
                    runeAmount = GameUtil.randomInt(11);
                    if (!BotCombatHelper.isFreeToPlayWorld()) {
                        if (SpellDefinition.ENTANGLE.getRequiredLevel() <= player.getSkillManager().getCurrentLevels()[6]) {
                            BotCombatHelper.grantBotSpellRunes(player, SpellDefinition.ENTANGLE, runeAmount);
                            player.botCombatSpell = SpellDefinition.ENTANGLE;
                        } else if (SpellDefinition.SNARE.getRequiredLevel() <= player.getSkillManager().getCurrentLevels()[6]) {
                            BotCombatHelper.grantBotSpellRunes(player, SpellDefinition.SNARE, runeAmount);
                            player.botCombatSpell = SpellDefinition.SNARE;
                        } else if (SpellDefinition.BIND.getRequiredLevel() <= player.getSkillManager().getCurrentLevels()[6]) {
                            BotCombatHelper.grantBotSpellRunes(player, SpellDefinition.BIND, runeAmount);
                            player.botCombatSpell = SpellDefinition.BIND;
                        }
                    } else if (SpellDefinition.BIND.getRequiredLevel() <= player.getSkillManager().getCurrentLevels()[6]) {
                        BotCombatHelper.grantBotSpellRunes(player, SpellDefinition.BIND, runeAmount);
                        player.botCombatSpell = SpellDefinition.BIND;
                    }
                }
            }
            player.setAutocastSpell(selectedSpell);
        }
        if (!BotCombatHelper.isFreeToPlayWorld() && player.getSkillManager().getCurrentLevels()[6] >= 20 && player.getSkillManager().getCurrentLevels()[1] >= 20 && ItemDefinition.isDefined(3755)) {
            if (player.getSkillManager().getCurrentLevels()[1] >= 45) {
                headId = 3755;
            }
            if (ItemDefinition.isDefined(4097)) {
                headId = BotCombatHelper.selectBestBotLoadoutItemId(player, null, BotCombatLoadoutTables.mageHeadIds);
                bodyId = BotCombatHelper.selectBestBotLoadoutItemId(player, null, BotCombatLoadoutTables.mageBodyIds);
                legId = BotCombatHelper.selectBestBotLoadoutItemId(player, null, BotCombatLoadoutTables.mageLegIds);
                glovesId = BotCombatHelper.selectBestBotLoadoutItemId(player, null, BotCombatLoadoutTables.mageGloveIds);
                bootsId = BotCombatHelper.selectBestBotLoadoutItemId(player, null, BotCombatLoadoutTables.mageBootIds);
            }
        }
        player.getEquipmentManager().getContainer().setItem(0, new ItemStack(headId));
        player.getEquipmentManager().getContainer().setItem(4, new ItemStack(bodyId));
        player.getEquipmentManager().getContainer().setItem(7, new ItemStack(legId));
        int amuletId = 1727;
        player.botShieldItemId = 1540;
        if (player.botCombatStyle == 2 && GameUtil.randomInt(2) == 0) {
            amuletId = 1731;
        }
        if (!BotCombatHelper.isFreeToPlayWorld()) {
            player.botShieldItemId = GameUtil.randomInt(3) == 0 ? 2890 : 1540;
            if (player.getCombatLevel() >= 60 && GameUtil.randomInt(2) == 0) {
                amuletId = 1712;
            }
        }
        player.getEquipmentManager().getContainer().setItem(5, new ItemStack(player.botShieldItemId));
        player.getEquipmentManager().getContainer().setItem(2, new ItemStack(amuletId));
        player.getEquipmentManager().getContainer().setItem(9, new ItemStack(glovesId));
        player.getEquipmentManager().getContainer().setItem(10, new ItemStack(bootsId));
    }

    public static void prepareMinigameCombatLoadout(Player player) {
        BotCombatLoadoutManager.prepareCombatLoadout(player, false);
        if (BotCombatHelper.isFreeToPlayWorld()) {
            return;
        }

        if (player.botPrimaryCombatStyle == BotPvpCombatHandler.MAGIC_COMBAT_STYLE) {
            BotCombatLoadoutManager.upgradeMinigameMagicLoadout(player);
        } else if (player.botPrimaryCombatStyle == 0) {
            BotCombatLoadoutManager.upgradeMinigameMeleeLoadout(player);
        }

        player.getInventoryManager().refresh();
        player.getEquipmentManager().refresh();
    }

    private static void upgradeMinigameMeleeLoadout(Player player) {
        if (GameUtil.randomInt(2) == 0) {
            if (ItemDefinition.isDefined(3140) && player.getEquipmentManager().canEquipItem(3140)) {
                player.getEquipmentManager().getContainer().setItem(4, new ItemStack(3140));
            }

            int[] dragonLegIds = new int[]{4087, 4585};
            int dragonLegId = dragonLegIds[GameUtil.randomInt(dragonLegIds.length)];
            if (ItemDefinition.isDefined(dragonLegId)
                    && player.getEquipmentManager().canEquipItem(dragonLegId)) {
                player.getEquipmentManager().getContainer().setItem(7, new ItemStack(dragonLegId));
            }
        }

        if (GameUtil.randomInt(2) == 0) {
            ArrayList<Integer> highTierPrimaryWeapons = new ArrayList<Integer>();
            if (ItemDefinition.isDefined(4151)
                    && player.getEquipmentManager().canEquipItem(4151)) {
                highTierPrimaryWeapons.add(4151);
            }

            int dragonClawsId = ItemDefinition.findIdByName("Dragon claws");
            if (ServerSettings.content2007Enabled
                    && dragonClawsId > 0
                    && ItemDefinition.isDefined(dragonClawsId)
                    && player.getEquipmentManager().canEquipItem(dragonClawsId)) {
                highTierPrimaryWeapons.add(dragonClawsId);
            }

            if (!highTierPrimaryWeapons.isEmpty()) {
                player.botWeaponItemId =
                        highTierPrimaryWeapons.get(GameUtil.randomInt(highTierPrimaryWeapons.size()));
                player.getEquipmentManager().getContainer().setItem(
                        3, new ItemStack(player.botWeaponItemId));
            }
        }

        int[] highTierSpecials = new int[]{5698, 4153};
        ArrayList<Integer> equippableSpecials = new ArrayList<Integer>();
        for (int specialWeaponId : highTierSpecials) {
            if (ItemDefinition.isDefined(specialWeaponId)
                    && player.getEquipmentManager().canEquipItem(specialWeaponId)) {
                equippableSpecials.add(specialWeaponId);
            }
        }
        if (equippableSpecials.isEmpty()) {
            return;
        }

        int specialWeaponId = equippableSpecials.get(GameUtil.randomInt(equippableSpecials.size()));
        player.botSpecialWeaponItemId = specialWeaponId;
        player.botSpecialCombatStyle = 0;
        SpecialAttackDefinition specialAttackDefinition =
                SpecialAttackDefinition.forItem(new ItemStack(specialWeaponId));
        player.botSpecialAttackEnergyCost =
                specialAttackDefinition == null ? 0 : specialAttackDefinition.getEnergyCost();
        if (player.getInventoryManager().getItemAmount(specialWeaponId) == 0) {
            player.getInventoryManager().addItem(new ItemStack(specialWeaponId, 1));
        }
    }

    private static void upgradeMinigameMagicLoadout(Player player) {
        if (player.getSpellbook() == Spellbook.ANCIENT
                || player.getSkillManager().getCurrentLevels()[6] < 50
                || !ItemDefinition.isDefined(4675)
                || GameUtil.randomInt(2) != 0) {
            return;
        }

        SpellDefinition selectedSpell = null;
        for (SpellDefinition spell : BotCombatLoadoutTables.ancientCombatSpellProgression) {
            if (spell.getRequiredLevel() > player.getSkillManager().getCurrentLevels()[6]) {
                break;
            }
            selectedSpell = spell;
        }
        if (selectedSpell == null) {
            return;
        }

        player.packetSender.setSidebarInterface(6, 12855);
        player.setSpellbook(Spellbook.ANCIENT);
        player.botCombatSpell = null;
        player.botWeaponItemId = 4675;
        player.botPrimaryAutocastSpell = selectedSpell;
        player.getEquipmentManager().getContainer().setItem(3, new ItemStack(player.botWeaponItemId));
        BotCombatHelper.grantBotSpellRunes(player, selectedSpell, 100 + GameUtil.randomInt(100));
        player.setAutocastSpell(selectedSpell);
    }

    public static void prepareCombatLoadout(Player player, boolean enabled3) {
        int value = player.botCombatStyle;
        player.botPrimaryAutocastSpell = null;
        player.setAutocastSpell(null);
        player.getInventoryManager().getContainer().clear();
        player.getEquipmentManager().getContainer().clear();
        boolean enabled2 = true;
        if (enabled3) {
            enabled2 = false;
        }
        switch (value) {
            case 4: {
                int amuletId = 1725;
                player.botActiveCombatStyle = player.botPrimaryCombatStyle = 0;
                int[] integerValues = new int[]{1303, 1333, 1373};
                player.botWeaponItemId = integerValues[GameUtil.randomInt(3)];
                player.getEquipmentManager().getContainer().setItem(3, new ItemStack(player.botWeaponItemId));
                if (player.getSkillManager().getCurrentLevels()[1] >= 10) {
                    player.getEquipmentManager().getContainer().setItem(0, new ItemStack(1165));
                    player.getEquipmentManager().getContainer().setItem(4, new ItemStack(1125));
                    player.getEquipmentManager().getContainer().setItem(7, GameUtil.randomInt(4) == 0 ? new ItemStack(1089) : new ItemStack(1077));
                    player.botShieldItemId = GameUtil.randomInt(2) == 0 ? 1195 : 1540;
                } else {
                    player.getEquipmentManager().getContainer().setItem(0, new ItemStack(1153));
                    player.getEquipmentManager().getContainer().setItem(4, new ItemStack(1115));
                    player.getEquipmentManager().getContainer().setItem(7, GameUtil.randomInt(4) == 0 ? new ItemStack(1081) : new ItemStack(1067));
                    player.botShieldItemId = GameUtil.randomInt(2) == 0 ? 1191 : 1540;
                }
                player.getEquipmentManager().getContainer().setItem(5, new ItemStack(player.botShieldItemId));
                if (player.botCombatStyle == 2 && GameUtil.randomInt(2) == 0) {
                    amuletId = 1731;
                }
                if (!BotCombatHelper.isFreeToPlayWorld() && player.getCombatLevel() >= 60 && GameUtil.randomInt(2) == 0) {
                    amuletId = 1712;
                }
                player.getEquipmentManager().getContainer().setItem(2, new ItemStack(amuletId));
                BotCombatLoadoutManager.equipGlovesAndBoots(player);
                player.getInventoryManager().addItem(new ItemStack(113, 1));
                if (GameUtil.randomInt(3) != 0) break;
                player.getInventoryManager().addItem(new ItemStack(BotCombatLoadoutManager.selectSpecialAttackWeapon(player), 1));
                break;
            }
            case 9: {
                player.botActiveCombatStyle = player.botPrimaryCombatStyle = 0;
                int[] integerValues2 = new int[]{1231, 4587};
                player.botWeaponItemId = integerValues2[GameUtil.randomInt(2)];
                player.getEquipmentManager().getContainer().setItem(3, new ItemStack(player.botWeaponItemId));
                player.getEquipmentManager().getContainer().setItem(0, new ItemStack(6109));
                player.getEquipmentManager().getContainer().setItem(4, new ItemStack(6107));
                player.getEquipmentManager().getContainer().setItem(7, new ItemStack(6108));
                player.botShieldItemId = 3842;
                player.getEquipmentManager().getContainer().setItem(5, new ItemStack(player.botShieldItemId));
                player.getEquipmentManager().getContainer().setItem(2, new ItemStack(1725));
                player.getEquipmentManager().getContainer().setItem(10, new ItemStack(3105));
                player.getEquipmentManager().getContainer().setItem(1, new ItemStack(6111));
                player.getEquipmentManager().getContainer().setItem(12, new ItemStack(2550));
                player.getInventoryManager().addItem(new ItemStack(113, 1));
                if (GameUtil.randomInt(3) == 0 && player.botWeaponItemId != 1231) {
                    player.getInventoryManager().addItem(new ItemStack(BotCombatLoadoutManager.selectSpecialAttackWeapon(player), 1));
                }
                enabled2 = false;
                break;
            }
            case 10: {
                player.botActiveCombatStyle = player.botPrimaryCombatStyle = 0;
                player.botWeaponItemId = 4151;
                player.getEquipmentManager().getContainer().setItem(3, new ItemStack(player.botWeaponItemId));
                player.getEquipmentManager().getContainer().setItem(4, new ItemStack(544));
                player.getEquipmentManager().getContainer().setItem(7, new ItemStack(542));
                player.getEquipmentManager().getContainer().setItem(2, new ItemStack(1731));
                player.getEquipmentManager().getContainer().setItem(10, new ItemStack(3105));
                player.getEquipmentManager().getContainer().setItem(1, new ItemStack(4411));
                player.getEquipmentManager().getContainer().setItem(12, new ItemStack(2550));
                player.getEquipmentManager().getContainer().setItem(9, new ItemStack(2912));
                player.getInventoryManager().addItem(new ItemStack(113, 1));
                if (GameUtil.randomInt(3) == 0) {
                    player.getInventoryManager().addItem(new ItemStack(BotCombatLoadoutManager.selectSpecialAttackWeapon(player), 1));
                }
                enabled2 = false;
                break;
            }
            case 11: {
                player.botActiveCombatStyle = player.botPrimaryCombatStyle = 0;
                player.botWeaponItemId = 1231;
                player.getEquipmentManager().getContainer().setItem(3, new ItemStack(player.botWeaponItemId));
                player.getEquipmentManager().getContainer().setItem(4, new ItemStack(577));
                player.getEquipmentManager().getContainer().setItem(7, new ItemStack(1033));
                player.getEquipmentManager().getContainer().setItem(2, new ItemStack(1731));
                player.getEquipmentManager().getContainer().setItem(10, new ItemStack(3105));
                player.getEquipmentManager().getContainer().setItem(1, new ItemStack(4365));
                player.botShieldItemId = 3842;
                player.getEquipmentManager().getContainer().setItem(5, new ItemStack(player.botShieldItemId));
                player.getInventoryManager().addItem(new ItemStack(113, 1));
                enabled2 = false;
                break;
            }
            case 0: {
                player.botActiveCombatStyle = player.botPrimaryCombatStyle = 0;
                Player player4 = player;
                BotCombatLoadoutManager.prepareMeleeLoadout(player4, false);
                BotCombatLoadoutManager.equipGlovesAndBoots(player);
                player.getInventoryManager().addItem(new ItemStack(113, 1));
                break;
            }
            case 7: {
                int value2 = 1731;
                player.botActiveCombatStyle = player.botPrimaryCombatStyle = 0;
                int[] player3 = new int[]{1303, 1333, 1373};
                player.botWeaponItemId = player3[GameUtil.randomInt(3)];
                value = 1135;
                int value3 = 1099;
                player.botShieldItemId = 1540;
                player.getEquipmentManager().getContainer().setItem(5, new ItemStack(player.botShieldItemId));
                int value4 = 1065;
                int value5 = 1061;
                if (!BotCombatHelper.isFreeToPlayWorld()) {
                    if (player.getCombatLevel() >= 60 && GameUtil.randomInt(2) == 0) {
                        value2 = 1712;
                    }
                    if (player.getEquipmentManager().canEquipItem(4587) && ItemDefinition.isDefined(4587)) {
                        player.botWeaponItemId = 4587;
                    }
                    if (player.getEquipmentManager().canEquipItem(2503)) {
                        value = 2503;
                        value3 = 2497;
                        value4 = 2491;
                    }
                    if (player.getEquipmentManager().canEquipItem(3751)) {
                        player.getEquipmentManager().getContainer().setItem(0, new ItemStack(3751));
                    }
                    player.getEquipmentManager().getContainer().setItem(12, new ItemStack(2550));
                    player.getEquipmentManager().getContainer().setItem(1, new ItemStack(2414));
                    value5 = 3105;
                    enabled2 = false;
                }
                player.getEquipmentManager().getContainer().setItem(3, new ItemStack(player.botWeaponItemId));
                player.getEquipmentManager().getContainer().setItem(2, new ItemStack(value2));
                player.getEquipmentManager().getContainer().setItem(9, new ItemStack(value4));
                player.getEquipmentManager().getContainer().setItem(10, new ItemStack(value5));
                player.getEquipmentManager().getContainer().setItem(4, new ItemStack(value));
                player.getEquipmentManager().getContainer().setItem(7, new ItemStack(value3));
                player.getInventoryManager().addItem(new ItemStack(113, 1));
                if (GameUtil.randomInt(3) != 0) break;
                player.getInventoryManager().addItem(new ItemStack(BotCombatLoadoutManager.selectSpecialAttackWeapon(player), 1));
                break;
            }
            case 2: 
            case 6: {
                player.botActiveCombatStyle = player.botPrimaryCombatStyle = BotPvpCombatHandler.MAGIC_COMBAT_STYLE;
                BotCombatLoadoutManager.prepareMagicLoadout(player);
                break;
            }
            case 8: {
                player.botActiveCombatStyle = player.botPrimaryCombatStyle = BotPvpCombatHandler.RANGED_COMBAT_STYLE;
                Player player2 = player;
                value = GameUtil.randomInt(2) == 0 ? 811 : 868;
                int value6 = BotCombatHelper.selectBestBotLoadoutItemId(player2, BotCombatLoadoutTables.basicRangedLegIds, BotCombatLoadoutTables.dragonhideChapsIds);
                int value7 = BotCombatHelper.selectBestBotLoadoutItemId(player2, BotCombatLoadoutTables.basicRangedVambraceIds, BotCombatLoadoutTables.dragonhideVambraceIds);
                int value8 = 40 + GameUtil.randomInt(20);
                player2.botWeaponItemId = value;
                player2.getEquipmentManager().getContainer().setItem(0, new ItemStack(2910));
                player2.getEquipmentManager().getContainer().setItem(7, new ItemStack(value6));
                player2.getEquipmentManager().getContainer().setItem(4, new ItemStack(1129));
                player2.getEquipmentManager().getContainer().setItem(9, new ItemStack(value7));
                player2.getEquipmentManager().getContainer().setItem(3, new ItemStack(player2.botWeaponItemId, value8));
                player2.getEquipmentManager().getContainer().setItem(10, new ItemStack(1061));
                player.getEquipmentManager().getContainer().setItem(2, new ItemStack(1725));
                if (GameUtil.randomInt(3) != 0) break;
                player.getInventoryManager().addItem(new ItemStack(BotCombatLoadoutManager.selectSpecialAttackWeapon(player), 1));
                break;
            }
            case 1: 
            case 5: {
                player.botActiveCombatStyle = player.botPrimaryCombatStyle = BotPvpCombatHandler.RANGED_COMBAT_STYLE;
                BotCombatLoadoutManager.prepareRangedLoadout(player);
                int skillManager = player.getSkillManager().getCurrentLevels()[4] >= 40 ? 1731 : (GameUtil.randomInt(3) == 0 ? 1478 : 1729);
                if (!BotCombatHelper.isFreeToPlayWorld() && player.getCombatLevel() >= 60 && GameUtil.randomInt(2) == 0) {
                    skillManager = 1712;
                }
                player.getEquipmentManager().getContainer().setItem(2, new ItemStack(skillManager));
            }
        }
        if (enabled2) {
            BotCombatLoadoutManager.equipRandomCape(player);
        }
        BotCombatLoadoutManager.addCombatSupplies(player);
        player.getInventoryManager().refresh();
        player.getEquipmentManager().refresh();
    }
}

package com.rs2.model;

import com.rs2.CacheCoordinateTranslator;
import com.rs2.ServerSettings;
import com.rs2.bot.BotRoute;
import com.rs2.bot.BotTaskDefinition;
import com.rs2.bot.BotTaskPlanner;
import com.rs2.bot.ClanWarsBotManager;
import com.rs2.bot.ClanWarsBotManagerTickTask;
import com.rs2.bot.DropPartyBotJoinTask;
import com.rs2.bot.DropPartyBotManager;
import com.rs2.bot.combat.BotCombatEscapeHandler;
import com.rs2.bot.combat.BotCombatHelper;
import com.rs2.bot.combat.BotCombatLoadoutManager;
import com.rs2.bot.combat.BotPvpCombatHandler;
import com.rs2.cache.CacheArchive;
import com.rs2.cache.CacheFile;
import com.rs2.cache.CacheStore;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.Entity;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.animation.GraphicEffect;
import com.rs2.model.clue.AnagramClue;
import com.rs2.model.clue.CoordinateClueHandler;
import com.rs2.model.clue.PuzzleBoxHandler;
import com.rs2.model.combat.CombatManager;
import com.rs2.model.dialogue.DialogueManager;
import com.rs2.model.gameplay.PositionRange;
import com.rs2.model.gameplay.barrows.BarrowsManager;
import com.rs2.model.gameplay.godwars.GodWarsDungeonManager;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemContainer;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemService;
import com.rs2.model.item.ItemStack;
import com.rs2.model.item.action.CaveLightSourceDefinition;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.npc.NpcMovementMode;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.objects.ObjectDefinition;
import com.rs2.model.player.Player;
import com.rs2.model.player.TradeState;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.randomevent.SkillRandomEventNpc;
import com.rs2.model.shop.ShopManager;
import com.rs2.model.skill.SkillActionHelper;
import com.rs2.model.skill.agility.AgilityObstacleHandler;
import com.rs2.model.skill.crafting.armor.BlackDragonhideCrafting;
import com.rs2.model.skill.crafting.armor.BlackDragonhideRecipe;
import com.rs2.model.skill.crafting.armor.BlueDragonhideCrafting;
import com.rs2.model.skill.crafting.armor.BlueDragonhideRecipe;
import com.rs2.model.skill.crafting.armor.GreenDragonhideCrafting;
import com.rs2.model.skill.crafting.armor.GreenDragonhideRecipe;
import com.rs2.model.skill.crafting.armor.HardLeatherCrafting;
import com.rs2.model.skill.crafting.armor.HardLeatherRecipe;
import com.rs2.model.skill.crafting.armor.LeatherCrafting;
import com.rs2.model.skill.crafting.armor.LeatherRecipe;
import com.rs2.model.skill.crafting.armor.RedDragonhideCrafting;
import com.rs2.model.skill.crafting.armor.RedDragonhideRecipe;
import com.rs2.model.skill.crafting.armor.SnakeskinAccessoryCrafting;
import com.rs2.model.skill.crafting.armor.SnakeskinArmorCrafting;
import com.rs2.model.skill.crafting.armor.SplitbarkCrafting;
import com.rs2.model.skill.magic.Spellbook;
import com.rs2.model.skill.runecrafting.CombinationRuneDefinition;
import com.rs2.model.skill.runecrafting.EssencePouchDefinition;
import com.rs2.model.skill.runecrafting.RunecraftingAltarDefinition;
import com.rs2.model.skill.runecrafting.RunecraftingHandler;
import com.rs2.model.task.TickTask;
import com.rs2.model.travel.ChargedJewelryDefinition;
import com.rs2.net.packet.AccessMode;
import com.rs2.net.packet.ByteOrder;
import com.rs2.net.packet.ByteTransform;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.PacketSender;
import com.rs2.net.packet.PacketWriter;
import com.rs2.util.ByteArrayReader;
import com.rs2.util.CharacterFileManager;
import com.rs2.util.FileUtil;
import com.rs2.util.GameUtil;
import com.rs2.util.TextUtil;
import com.rs2.util.path.WalkingCollisionMap;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.base.AbstractDateTime;

public class GameplayHelper {
    private int tradeAdvertItemId;
    private int[] tradeAdvertQuantityOptions;
    public static int objectDefinitionCount;

    public static void resetBotTaskState(Player player) {
        if (player.botMode == 4) {
            if (player.botEscapeLogoutTask != null && player.botEscapeLogoutTask.isActive()) {
                player.botEscapeLogoutTask.stop();
            }
            if (player.botCombatTickTask != null && player.botCombatTickTask.isActive()) {
                player.botCombatTickTask.stop();
            }
            if (player.pendingGroupCleanup != null) {
                player.pendingGroupCleanup.finishDeferredRemoval(player);
            }
            player.botLootSellItems.clear();
            player.botLootSellGroundItems.clear();
            player.botEscapeStuckTicks = 0;
            player.botCombatEscapeActive = false;
            player.botAntipoisonAvailable = false;
            player.botWeaponItemId = 0;
            player.botPrimaryCombatStyle = 0;
            player.botActiveCombatStyle = 0;
            player.botSpecialCombatStyle = 0;
            player.botSpecialWeaponItemId = 0;
            player.botShieldItemId = 0;
            player.botSpecialAttackEnergyCost = 0;
            player.botCombatSpell = null;
            player.botMagicPenaltyGearUnequipped = false;
            player.setAutoRetaliate(true);
            player.botFoodDepleted = false;
            player.botStrengthPotionDepleted = false;
            player.setAutocastSpell(null);
            player.getPrayerManager().deactivateAll();
            player.botCombatState = null;
            return;
        }
        if (player.botEscapeLogoutTask != null && player.botEscapeLogoutTask.isActive()) {
            player.botEscapeLogoutTask.stop();
        }
        if (player.botCombatTickTask != null && player.botCombatTickTask.isActive()) {
            player.botCombatTickTask.stop();
        }
        if (player.pendingGroupCleanup != null) {
            player.pendingGroupCleanup.finishDeferredRemoval(player);
        }
        player.botEatDelayTicks = 0;
        player.botWeaponSwapDelayTicks = 0;
        player.botMagicGearSwapDelayTicks = 0;
        player.botThreatEscapeDelayTicks = 0;
        player.botPrayerSwitchDelayTicks = 0;
        player.botQueuedPrayerId = -1;
        player.botLootSellItems.clear();
        player.botLootSellGroundItems.clear();
        player.botEscapeStuckTicks = 0;
        player.botCombatEscapeActive = false;
        player.botAntipoisonAvailable = false;
        player.botWeaponItemId = 0;
        player.botPrimaryCombatStyle = 0;
        player.botActiveCombatStyle = 0;
        player.botSpecialCombatStyle = 0;
        player.botSpecialWeaponItemId = 0;
        player.botShieldItemId = 0;
        player.botSpecialAttackEnergyCost = 0;
        Player player2 = player;
        player2.packetSender.setSidebarInterface(6, 1151);
        player.setSpellbook(Spellbook.MODERN);
        player.botCombatSpell = null;
        player.botMagicPenaltyGearUnequipped = false;
        player.setAutoRetaliate(true);
        player.clearPvpCombatReferences();
        player.getRecentCombatTimer().setDelayTicks(0);
        player.getRecentCombatTimer().reset();
        player.getSingleCombatTimer().setDelayTicks(0);
        player.getSingleCombatTimer().reset();
        player.botFoodDepleted = false;
        player.botStrengthPotionDepleted = false;
        player.setAutocastSpell(null);
        player.getMovementQueue().setRunning(false);
        player.setRunEnergyPercent(100);
        player.setSpecialEnergy(100);
        player.setPoisonDamage(0.0);
        player.getPrayerManager().deactivateAll();
        player.botCombatState = null;
        player.clearCombatEffectTasks();
        player.resetCombatState();
        player.getDamageContributions().clear();
    }

    public static void resetBotSkillsToBase(Player player) {
        int index = 0;
        while (index < player.getSkillManager().getCurrentLevels().length) {
            if (index == 3) {
                player.getSkillManager().getCurrentLevels()[index] = 10;
                player.getSkillManager().getExperience()[index] = 1154.0;
            } else {
                player.getSkillManager().getCurrentLevels()[index] = 1;
                player.getSkillManager().getExperience()[index] = 0.0;
            }
            ++index;
        }
    }

    public static void selectAndStartProgressiveBotTask(Player player, boolean enabled5) {
        int value;
        boolean enabled2;
        Object value2 = null;
        boolean enabled3 = false;
        if (enabled5) {
            if (BotTaskDefinition.cookingTasks.contains(player.currentBotTask)) {
                if (!BotTaskPlanner.prepareCookingTaskRequirements(player)) {
                    enabled3 = false;
                } else {
                    value2 = player.currentBotTask;
                    enabled3 = true;
                }
            } else if (BotTaskDefinition.spinningTasks.contains(player.currentBotTask)) {
                if (!BotTaskPlanner.prepareSpinningTaskRequirements(player)) {
                    enabled3 = false;
                } else {
                    value2 = player.currentBotTask;
                    enabled3 = true;
                }
            } else if (BotTaskDefinition.tanningTasks.contains(player.currentBotTask)) {
                if (!BotTaskPlanner.prepareTanningTaskRequirements(player, false)) {
                    enabled3 = false;
                } else {
                    value2 = player.currentBotTask;
                    enabled3 = true;
                }
            } else if (BotTaskDefinition.leatherCraftingTasks.contains(player.currentBotTask)) {
                if (!BotTaskPlanner.prepareLeatherCraftingTaskRequirements(player)) {
                    enabled3 = false;
                } else {
                    value2 = player.currentBotTask;
                    enabled3 = true;
                    Object missingRequiredItems = ((BotTaskDefinition)value2).getMissingRequiredItems(player);
                    enabled2 = true;
                    if (((ArrayList)missingRequiredItems).size() > 0) {
                        if (((ItemStack)((ArrayList)missingRequiredItems).get(0)).getId() == 983) {
                            player.deferredBotTask = (BotTaskDefinition)value2;
                            value2 = (BotTaskDefinition)BotTaskDefinition.brassKeyTasks.get(0);
                            enabled2 = true;
                        } else if ((missingRequiredItems = BotTaskPlanner.selectShopPurchaseTask(player, ((ItemStack)((ArrayList)missingRequiredItems).get(0)).getId(), ((ItemStack)((ArrayList)missingRequiredItems).get(0)).getAmount())) != null) {
                            BotTaskPlanner.resetBotTaskGoals(player);
                            player.deferredBotTask = (BotTaskDefinition)value2;
                            value2 = missingRequiredItems;
                            enabled2 = true;
                        } else {
                            enabled2 = false;
                        }
                    }
                    if (!enabled2) {
                        enabled3 = false;
                    }
                }
            } else if (BotTaskDefinition.smeltingTasks.contains(player.currentBotTask)) {
                if (!BotTaskPlanner.hasSmeltingTaskMaterial(player)) {
                    enabled3 = false;
                } else {
                    value2 = player.currentBotTask;
                    enabled3 = true;
                }
            } else if (BotTaskDefinition.smithingTasks.contains(player.currentBotTask)) {
                if (!BotTaskPlanner.prepareSmithingTaskRequirements(player)) {
                    enabled3 = false;
                } else {
                    value2 = player.currentBotTask;
                    enabled3 = true;
                }
            }
        }
        while (!enabled3) {
            if (player.deferredBotTask != null) {
                value2 = player.deferredBotTask;
                player.deferredBotTask = null;
            } else {
                BotTaskDefinition botTaskDefinition;
                Player player2 = player;
                value2 = new ArrayList();
                enabled2 = false;
                boolean enabled4 = false;
                if (BotTaskDefinition.woodcuttingTasks.contains(player2.currentBotTask) || BotTaskDefinition.smithingTasks.contains(player2.currentBotTask) || BotTaskDefinition.combatTasks.contains(player2.currentBotTask) || player2.currentBotTask == BotTaskDefinition.moneyMakingTasks.get(1) || BotTaskDefinition.leatherCraftingTasks.contains(player2.currentBotTask)) {
                    BotTaskPlanner.populateBotShopSellItemIds(player2);
                    if (player2.botShopSellItemIds.size() != 0) {
                        enabled4 = true;
                    }
                }
                if (BotTaskDefinition.fishingTasks.contains(player2.currentBotTask)) {
                    ((ArrayList)value2).addAll(BotTaskDefinition.cookingTasks);
                    if (player2.currentBotTask != BotTaskDefinition.fishingTasks.get(5)) {
                        ((ArrayList)value2).remove(BotTaskDefinition.cookingTasks.get(2));
                    }
                    enabled2 = true;
                } else if (BotTaskDefinition.miningTasks.contains(player2.currentBotTask)) {
                    ((ArrayList)value2).addAll(BotTaskDefinition.smeltingTasks);
                    enabled2 = true;
                } else if (BotTaskDefinition.tanningTasks.contains(player2.currentBotTask)) {
                    ((ArrayList)value2).addAll(BotTaskDefinition.leatherCraftingTasks);
                } else if (BotTaskDefinition.smeltingTasks.contains(player2.currentBotTask)) {
                    ((ArrayList)value2).addAll(BotTaskDefinition.smithingTasks);
                    enabled2 = true;
                } else if (BotTaskDefinition.sheepShearingTasks.contains(player2.currentBotTask) || player2.currentBotTask == BotTaskDefinition.moneyMakingTasks.get(2)) {
                    ((ArrayList)value2).addAll(BotTaskDefinition.spinningTasks);
                    enabled2 = true;
                } else if (enabled4) {
                    ((ArrayList)value2).addAll(BotTaskDefinition.getLootSellShopTasks());
                    player2.botShopBuyMode = 0;
                    enabled2 = true;
                } else if (BotTaskDefinition.runecraftingTasks.get(0) == player2.currentBotTask) {
                    ((ArrayList)value2).addAll(BotTaskDefinition.runecraftingTasks);
                    ((ArrayList)value2).remove(BotTaskDefinition.runecraftingTasks.get(0));
                } else {
                    ((ArrayList)value2).addAll(BotTaskDefinition.miningTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.fishingTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.woodcuttingTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.combatTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.moneyMakingTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.runecraftingTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.sheepShearingTasks);
                    if (BotTaskPlanner.prepareTanningTaskRequirements(player2, true)) {
                        ((ArrayList)value2).addAll(BotTaskDefinition.tanningTasks);
                    }
                }
                BotTaskDefinition botTaskDefinition2 = GameplayHelper.selectAvailableBotTask(player2, (ArrayList)value2, enabled2);
                if (botTaskDefinition2 != null) {
                    botTaskDefinition = botTaskDefinition2;
                } else {
                    ((ArrayList)value2).clear();
                    ((ArrayList)value2).addAll(BotTaskDefinition.miningTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.fishingTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.woodcuttingTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.combatTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.moneyMakingTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.runecraftingTasks);
                    ((ArrayList)value2).addAll(BotTaskDefinition.sheepShearingTasks);
                    if (BotTaskPlanner.prepareTanningTaskRequirements(player2, true)) {
                        ((ArrayList)value2).addAll(BotTaskDefinition.tanningTasks);
                    }
                    botTaskDefinition = GameplayHelper.selectAvailableBotTask(player2, (ArrayList)value2, false);
                }
                value2 = botTaskDefinition;
            }
            Object missingRequiredItems2 = ((BotTaskDefinition)value2).getMissingRequiredItems(player);
            enabled2 = true;
            if (((ArrayList)missingRequiredItems2).size() > 0) {
                if (((ItemStack)((ArrayList)missingRequiredItems2).get(0)).getId() == 983) {
                    player.deferredBotTask = (BotTaskDefinition)value2;
                    value2 = (BotTaskDefinition)BotTaskDefinition.brassKeyTasks.get(0);
                    enabled2 = true;
                } else if ((missingRequiredItems2 = BotTaskPlanner.selectShopPurchaseTask(player, ((ItemStack)((ArrayList)missingRequiredItems2).get(0)).getId(), ((ItemStack)((ArrayList)missingRequiredItems2).get(0)).getAmount())) != null) {
                    BotTaskPlanner.resetBotTaskGoals(player);
                    player.deferredBotTask = (BotTaskDefinition)value2;
                    value2 = missingRequiredItems2;
                    enabled2 = true;
                } else {
                    enabled2 = false;
                }
            }
            if (BotTaskDefinition.cookingTasks.contains(value2)) {
                if (!BotTaskPlanner.prepareCookingTaskRequirements(player)) {
                    missingRequiredItems2 = player;
                    player.currentBotTask = null;
                    enabled2 = false;
                }
            } else if (BotTaskDefinition.spinningTasks.contains(value2)) {
                if (!BotTaskPlanner.prepareSpinningTaskRequirements(player)) {
                    missingRequiredItems2 = player;
                    player.currentBotTask = null;
                    enabled2 = false;
                }
            } else if (BotTaskDefinition.tanningTasks.contains(value2)) {
                if (!BotTaskPlanner.prepareTanningTaskRequirements(player, false)) {
                    missingRequiredItems2 = player;
                    player.currentBotTask = null;
                    enabled2 = false;
                }
            } else if (BotTaskDefinition.leatherCraftingTasks.contains(value2)) {
                if (!BotTaskPlanner.prepareLeatherCraftingTaskRequirements(player)) {
                    missingRequiredItems2 = player;
                    player.currentBotTask = null;
                    enabled2 = false;
                }
            } else if (BotTaskDefinition.smeltingTasks.contains(value2)) {
                if (!BotTaskPlanner.hasSmeltingTaskMaterial(player)) {
                    missingRequiredItems2 = player;
                    player.currentBotTask = null;
                    enabled2 = false;
                }
            } else if (BotTaskDefinition.smithingTasks.contains(value2) && !BotTaskPlanner.prepareSmithingTaskRequirements(player)) {
                missingRequiredItems2 = player;
                player.currentBotTask = null;
                enabled2 = false;
            }
            if (!enabled2) continue;
            enabled3 = true;
        }
        player.botTaskReturnToBankRequested = false;
        BotTaskDefinition botTaskDefinition = (BotTaskDefinition)value2;
        Player player3 = player;
        player.currentBotTask = botTaskDefinition;
        BotTaskPlanner.configureCurrentBotTaskGoals(player);
        if (player.botShopSellItemIds.size() == 0) {
            BotTaskPlanner.prepareDeferredUpgradePurchase(player);
        }
        player.botEnabled = true;
        player.currentBotTask.startTask(player);
        player.botTaskStartTimeMillis = System.currentTimeMillis();
        player.botTaskSavedElapsedMillis = 0L;
        player.botTaskDurationMinutes = value = 30 + GameUtil.randomInt(60);
        if (player.currentBotTask.usesEscapeMonitor) {
            player.currentBotTask.startEscapeMonitor(player);
        }
    }

    public static void selectAndStartNextProgressiveBotTask(Player player) {
        GameplayHelper.selectAndStartProgressiveBotTask(player, false);
    }

    public static void startNextBotTask(Player player) {
        BotTaskDefinition currentTask = player.currentBotTask;
        double totalWeight = 0.0;
        ArrayList<BotTaskDefinition> availableTasks = new ArrayList<BotTaskDefinition>();
        ArrayList taskPool = player.botMode == 2 ? BotTaskDefinition.tradeAdvertTaskPool : BotTaskDefinition.progressiveTaskPool;
        Iterator taskIterator = taskPool.iterator();
        while (taskIterator.hasNext()) {
            BotTaskDefinition botTaskDefinition = (BotTaskDefinition)taskIterator.next();
            if (currentTask != null && botTaskDefinition == currentTask) continue;
            availableTasks.add(botTaskDefinition);
            totalWeight += (double)botTaskDefinition.selectionWeight;
        }
        double roll = Math.random() * totalWeight;
        double cumulativeWeight = 0.0;
        BotTaskDefinition selectedTask = null;
        Iterator availableIterator = availableTasks.iterator();
        while (availableIterator.hasNext()) {
            BotTaskDefinition botTaskDefinition = (BotTaskDefinition)availableIterator.next();
            cumulativeWeight += (double)botTaskDefinition.selectionWeight;
            if (cumulativeWeight < roll) continue;
            selectedTask = botTaskDefinition;
            break;
        }
        if (selectedTask == null) {
            selectedTask = availableTasks.size() > 0 ? (BotTaskDefinition)availableTasks.get(0) : currentTask;
        }
        if (player.currentBotTask != null) {
            player.currentBotTask.assignedBotPlayers.remove(player);
        }
        player.botTaskReturnToBankRequested = false;
        player.botEnabled = true;
        selectedTask.assignedBotPlayers.add(player);
        player.currentBotTask = selectedTask;
        if (!BotTaskDefinition.shopTasks.contains(selectedTask)) {
            player.botTaskRequiredItems = null;
        }
        selectedTask.startTask(player);
        player.botTaskStartTimeMillis = System.currentTimeMillis();
        int durationRange = player.botMode == 2 ? 16 : 106;
        player.botTaskDurationMinutes = 15 + GameUtil.randomInt(durationRange);
        if (player.currentBotTask.usesEscapeMonitor) {
            player.currentBotTask.startEscapeMonitor(player);
        }
    }

    public static void startDropPartyBotEvent() {
        if (DropPartyBotManager.dropPartyActive) {
            return;
        }
        if (ServerSettings.otherBotCount < DropPartyBotManager.baseDropPartySize) {
            DropPartyBotManager.baseDropPartySize = ServerSettings.otherBotCount;
        }
        DropPartyBotManager.dropPartyParticipants.clear();
        BotTaskDefinition.dropPartyBotJoinIndex = 0;
        DropPartyBotManager.dropPartyActive = true;
        int value = GameUtil.randomInt(BotTaskDefinition.dropPartyTaskPool.size());
        Object botTaskDefinition = (BotTaskDefinition)BotTaskDefinition.dropPartyTaskPool.get(value);
        World.getTaskScheduler().schedule(new DropPartyBotJoinTask(1, (BotTaskDefinition)botTaskDefinition));
    }

    public static void startClanWarsBotEvent() {
        if (ClanWarsBotManager.clanWarsEventActive) {
            return;
        }
        ClanWarsBotManager.clanWarsEventActive = true;
        ClanWarsBotManager.clanWarsBaseCombatLevel = 35 + GameUtil.randomInt(45);
        ClanWarsBotManager.chooseClanWarsTeamTags();
        ClanWarsBotManager.chooseClanWarsTeamCapes();
        ClanWarsBotManager.startClanWarsCombatants();
        ClanWarsBotManagerTickTask clanWarsBotManagerTickTask = new ClanWarsBotManagerTickTask(10);
        World.getTaskScheduler().schedule(clanWarsBotManagerTickTask);
    }

    public static boolean shouldReturnToBankForBotTask(Player player) {
        if (player.botMode != 4) {
            if (System.currentTimeMillis() >= player.botTaskStartTimeMillis + (long)GameUtil.minutesToMillis(player.botTaskDurationMinutes)) {
                player.botTaskReturnToBankRequested = true;
                return true;
            }
        } else {
            if (player.botTaskSavedElapsedMillis + player.getBotTaskRuntimeMillis() >= (long)GameUtil.minutesToMillis(player.botTaskDurationMinutes)) {
                player.botTaskReturnToBankRequested = true;
                return true;
            }
            if (player.botSkillTargetSkillId != -1 && player.getSkillManager().getCurrentLevels()[player.botSkillTargetSkillId] >= player.botSkillTargetLevel) {
                player.botTaskReturnToBankRequested = true;
                return true;
            }
            if (player.botCompletionItemId != -1 && player.ownsItemAmount(player.botCompletionItemId, player.botCompletionItemAmount)) {
                player.botTaskReturnToBankRequested = true;
                return true;
            }
        }
        return false;
    }

    public static void prepareBotCombatStyle(Player player, int value2) {
        player.botCombatStyle = value2;
        if (value2 == -1) {
            BotCombatLoadoutManager.selectCombatStyleFromStats(player, true);
            value2 = player.botCombatStyle;
        }
        if (value2 == 0) {
            player.botActiveCombatStyle = player.botPrimaryCombatStyle = 0;
            BotCombatLoadoutManager.prepareMeleeLoadout(player);
            BotCombatLoadoutManager.equipGlovesAndBoots(player);
        } else if (value2 == 2) {
            player.botActiveCombatStyle = player.botPrimaryCombatStyle = BotPvpCombatHandler.MAGIC_COMBAT_STYLE;
            BotCombatLoadoutManager.prepareMagicLoadout(player);
        } else if (value2 == 1) {
            player.botActiveCombatStyle = player.botPrimaryCombatStyle = BotPvpCombatHandler.RANGED_COMBAT_STYLE;
            BotCombatLoadoutManager.prepareRangedLoadout(player);
            int skillManager = player.getSkillManager().getCurrentLevels()[4] >= 40 ? 1731 : (value2 = GameUtil.randomInt(3) == 0 ? 1478 : 1729);
            if (!BotCombatHelper.isFreeToPlayWorld() && player.getCombatLevel() >= 60 && GameUtil.randomInt(2) == 0) {
                value2 = 1712;
            }
            player.getEquipmentManager().getContainer().setItem(2, new ItemStack(value2));
        }
        BotCombatLoadoutManager.equipRandomCape(player);
    }

    public static BotTaskDefinition selectAvailableBotTask(Player player, ArrayList tasks, boolean nearestStart) {
        if (nearestStart) {
            int closestDistance = 1000;
            BotTaskDefinition closestTask = null;
            Iterator iterator = tasks.iterator();
            while (iterator.hasNext()) {
                BotTaskDefinition task = (BotTaskDefinition)iterator.next();
                if (!task.isAvailableFor(player, false)) continue;
                int distance = GameUtil.getDistance(player.getPosition(), task.startPosition);
                if (distance >= closestDistance) continue;
                closestDistance = distance;
                closestTask = task;
            }
            return closestTask;
        }
        double totalWeight = 0.0;
        BotTaskDefinition currentTask = player.currentBotTask;
        ArrayList<BotTaskDefinition> availableTasks = new ArrayList<BotTaskDefinition>();
        Iterator iterator = tasks.iterator();
        while (iterator.hasNext()) {
            BotTaskDefinition task = (BotTaskDefinition)iterator.next();
            if (currentTask != null && task == currentTask || !task.isAvailableFor(player, false)) continue;
            availableTasks.add(task);
            totalWeight += (double)task.selectionWeight;
        }
        double roll = Math.random() * totalWeight;
        double cumulativeWeight = 0.0;
        for (BotTaskDefinition task : availableTasks) {
            cumulativeWeight += (double)task.selectionWeight;
            if (cumulativeWeight < roll) continue;
            return task;
        }
        return null;
    }

    public static void startBotTaskRoute(Player player) {
        BotTaskDefinition botTaskDefinition = player.currentBotTask;
        player.botTaskState = "worldwalk to bank";
        player.botTaskStartTimeMillis = System.currentTimeMillis();
        player.botTaskDurationMinutes = 15;
        player.botTaskSavedElapsedMillis = 0L;
        BotRoute botRoute = botTaskDefinition.taskRoute != null ? new BotRoute(new Position[]{botTaskDefinition.taskRoute.waypoints[0], botTaskDefinition.startPosition}) : new BotRoute(new Position[]{botTaskDefinition.taskRouteSegments[0].waypoints[0], botTaskDefinition.startPosition});
        int distance = GameUtil.getDistance(player.getPosition(), botRoute.getStartPosition());
        int distance2 = GameUtil.getDistance(player.getPosition(), botTaskDefinition.startPosition);
        if (distance2 <= distance) {
            botRoute = new BotRoute(new Position[]{botTaskDefinition.startPosition});
        }
        player.currentBotRoute = botRoute;
        player.botPathWaypointIndex = 0;
        player.continueBotRoute();
    }

    public GameplayHelper(int tradeAdvertItemId, int[] tradeAdvertQuantityOptions) {
        this.tradeAdvertItemId = tradeAdvertItemId;
        this.tradeAdvertQuantityOptions = tradeAdvertQuantityOptions;
    }

    public int getTradeAdvertItemId() {
        return this.tradeAdvertItemId;
    }

    public int[] getTradeAdvertQuantityOptions() {
        return this.tradeAdvertQuantityOptions;
    }

    public int getPreviousTradeAdvertQuantityOption(int quantity) {
        if (quantity > 0) {
            return this.tradeAdvertQuantityOptions[quantity - 1];
        }
        return -1;
    }

    public static boolean isObjectDefinitionIdValid(int objectId) {
        return objectId < objectDefinitionCount;
    }

    public static void loadObjectDefinitions() {
        if (ServerSettings.cacheVersion == 443) {
            try {
                GameplayHelper.objectDefinitionCount = ObjectDefinition.loadRevision443();
                return;
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to load revision 443 object definitions", exception);
            }
        }
        CacheStore cacheStore = CacheStore.getInstance();
        ByteArrayReader reader = null;
        try {
            reader = new ByteArrayReader(new CacheArchive(cacheStore.readFile(0, 2)).getFileBytes("loc.dat"));
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        GameplayHelper.objectDefinitionCount = cacheStore.getDefinitionIndex().getObjectDefinitionEntries().length;
        ObjectDefinition.definitionsById = new ObjectDefinition[objectDefinitionCount];
        reader.position = 2;
        int objectId = 0;
        while (objectId < objectDefinitionCount) {
            reader.position = cacheStore.getDefinitionIndex().getObjectDefinitionEntry(objectId).getDataOffset();
            ObjectDefinition definition = ObjectDefinition.forId(objectId);
            definition.name = null;
            definition.description = null;
            definition.length = 1;
            definition.width = 1;
            definition.solid = true;
            definition.interactive = false;
            definition.blocksProjectiles = true;
            while (true) {
                int opcode = reader.readUnsignedByte();
                if (opcode == 0) {
                    if (definition.name == null) {
                        definition.name = "";
                    }
                    break;
                }
                if (opcode == 1) {
                    int count = reader.readUnsignedByte();
                    int i = 0;
                    while (i < count) {
                        reader.readUnsignedShort();
                        reader.readUnsignedByte();
                        ++i;
                    }
                    continue;
                }
                if (opcode == 2) {
                    definition.name = reader.readString();
                    continue;
                }
                if (opcode == 3) {
                    definition.description = new String(reader.readLineBytes());
                    continue;
                }
                if (opcode == 5) {
                    int count = reader.readUnsignedByte();
                    int i = 0;
                    while (i < count) {
                        reader.readUnsignedShort();
                        ++i;
                    }
                    continue;
                }
                if (opcode == 14) {
                    definition.width = reader.readUnsignedByte();
                    continue;
                }
                if (opcode == 15) {
                    definition.length = reader.readUnsignedByte();
                    continue;
                }
                if (opcode == 17) {
                    definition.solid = false;
                    continue;
                }
                if (opcode == 18) {
                    continue;
                }
                if (opcode == 19) {
                    definition.interactive = reader.readUnsignedByte() == 1;
                    continue;
                }
                if (opcode == 21 || opcode == 22 || opcode == 23) continue;
                if (opcode == 24) {
                    reader.readUnsignedShort();
                    continue;
                }
                if (opcode == 28) {
                    reader.readUnsignedByte();
                    continue;
                }
                if (opcode == 29) {
                    reader.readByte();
                    continue;
                }
                if (opcode == 39) {
                    reader.readByte();
                    continue;
                }
                if (opcode >= 30 && opcode < 39) {
                    reader.readString();
                    definition.interactive = true;
                    continue;
                }
                if (opcode == 40) {
                    int count = reader.readUnsignedByte();
                    int i = 0;
                    while (i < count) {
                        reader.readUnsignedShort();
                        reader.readUnsignedShort();
                        ++i;
                    }
                    continue;
                }
                if (opcode == 60) {
                    reader.readUnsignedShort();
                    continue;
                }
                if (opcode == 62) continue;
                if (opcode == 64) {
                    definition.blocksProjectiles = false;
                    continue;
                }
                if (opcode == 65) {
                    reader.readUnsignedShort();
                    continue;
                }
                if (opcode == 66) {
                    reader.readUnsignedShort();
                    continue;
                }
                if (opcode == 67) {
                    reader.readUnsignedShort();
                    continue;
                }
                if (opcode == 68) {
                    reader.readUnsignedShort();
                    continue;
                }
                if (opcode == 69) {
                    reader.readUnsignedByte();
                    continue;
                }
                if (opcode == 70) {
                    reader.readShort();
                    continue;
                }
                if (opcode == 71) {
                    reader.readShort();
                    continue;
                }
                if (opcode == 72) {
                    reader.readShort();
                    continue;
                }
                if (opcode == 73 || opcode == 74) continue;
                if (opcode == 75) {
                    reader.readUnsignedByte();
                    continue;
                }
                if (opcode == 77) {
                    reader.readUnsignedShort();
                    reader.readUnsignedShort();
                    int count = reader.readUnsignedByte();
                    int i = 0;
                    while (i <= count) {
                        reader.readUnsignedShort();
                        ++i;
                    }
                }
            }
            definition.projectileCollisionIgnored = definition.isProjectileCollisionIgnored();
            ++objectId;
        }
    }

    public static byte[] inflateGzipCacheFile(CacheFile cacheFile) {
        byte[] compressedBytes = new byte[cacheFile.getBuffer().remaining()];
        cacheFile.getBuffer().get(compressedBytes);
        try {
            byte[] inflatedBuffer = new byte[999999];
            int length = 0;
            GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressedBytes));
            while (true) {
                if (length == 999999) {
                    System.out.println("Error inflating data.\nGZIP buffer overflow.");
                    break;
                }
                int readCount = gzipInputStream.read(inflatedBuffer, length, 999999 - length);
                if (readCount == -1) break;
                length += readCount;
            }
            byte[] inflatedBytes = new byte[length];
            System.arraycopy(inflatedBuffer, 0, inflatedBytes, 0, length);
            if (inflatedBytes.length < 10) {
                return null;
            }
            return inflatedBytes;
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public static int getNpcShopId(int npcId) {
        NpcDefinition npcDefinition = NpcDefinition.forId(npcId);
        int shopId = npcDefinition.getShopId();
        if (shopId >= 0) {
            return shopId;
        }
        return -1;
    }

    public static boolean openNpcShop(Player player, int npcId) {
        if ((npcId = GameplayHelper.getNpcShopId(npcId)) >= 0) {
            ShopManager.openShop(player, npcId);
            return true;
        }
        return false;
    }

    public static void refreshPlayerAreaOverlay(Player player) {
        Player player2;
        if (player.isInWilderness()) {
            player.wildernessEntryAcknowledged = true;
        }
        if ((player2 = player).isInWilderness()) {
            Player player3;
            if (GameplayHelper.updateWalkableInterface(player2, 197)) {
                player3 = player2;
                player3.packetSender.sendPlayerOption("Attack", 1, false);
            }
            if (player2.displayedWildernessLevel != player2.getWildernessLevel()) {
                player3 = player2;
                player3.packetSender.sendInterfaceText("@yel@Level: " + player2.getWildernessLevel(), 199);
                player2.displayedWildernessLevel = player2.getWildernessLevel();
            }
        } else if (player2.isInDuelArena()) {
            Player player4 = player2;
            player4.packetSender.sendPlayerOption("Attack", 1, false);
            GameplayHelper.updateWalkableInterface(player2, 201);
        } else if (player2.isInDuelArenaLobby()) {
            Player player5 = player2;
            player5.packetSender.sendPlayerOption("Challenge", 1, false);
            GameplayHelper.updateWalkableInterface(player2, 201);
        } else if (player2.isInBarrows()) {
            Player player6;
            if (player2.displayedBarrowsKillCount != player2.getBarrowsKillCount()) {
                player6 = player2;
                player6.packetSender.sendInterfaceText("Kill count: " + player2.getBarrowsKillCount(), 4536);
                player2.displayedBarrowsKillCount = player2.getBarrowsKillCount();
            }
            BarrowsManager.ensurePrayerDrainTask(player2);
            if (GameplayHelper.updateWalkableInterface(player2, 4535)) {
                player6 = player2;
                player6.packetSender.sendPlayerOption("null", 1, false);
                player6 = player2;
                player6.packetSender.sendMinimapState(2);
            }
        } else {
            Object value = player2;
            if (((Entity)value).isInArea(2816, 2943, 5248, 5375)) {
                GodWarsDungeonManager.refreshKillCountOverlay(player2);
                if (GameplayHelper.updateWalkableInterface(player2, 19556)) {
                    value = player2;
                    ((Player)value).packetSender.sendPlayerOption("null", 1, false);
                }
            } else {
                value = player2;
                if (((Entity)value).isInArea(2494, 2569, 3701, 3785)) {
                    if (GameplayHelper.updateWalkableInterface(player2, 11877)) {
                        value = player2;
                        ((Player)value).packetSender.sendPlayerOption("null", 1, false);
                    }
                } else {
                    value = player2;
                    if (((Entity)value).isInArea(3136, 3263, 9536, 9599)) {
                        int initialValue = -1;
                        if (player2.getActiveCaveLightLevel() == 0) {
                            initialValue = 12414;
                        } else if (player2.getActiveCaveLightLevel() == 1) {
                            initialValue = 12418;
                        } else if (player2.getActiveCaveLightLevel() == 2) {
                            initialValue = 12416;
                        }
                        if (initialValue != -1 && GameplayHelper.updateWalkableInterface(player2, initialValue)) {
                            Player player7 = player2;
                            player7.packetSender.sendPlayerOption("null", 1, false);
                        }
                    } else if (player2.isInSmokeDungeon()) {
                        if (GameplayHelper.updateWalkableInterface(player2, 13103)) {
                            value = player2;
                            ((Player)value).packetSender.sendPlayerOption("null", 1, false);
                        }
                    } else if (player2.getQuestState(0) != 1 && ServerSettings.cacheVersion > 289) {
                        GameplayHelper.updateWalkableInterface(player2, 8680);
                    } else if (GameplayHelper.updateWalkableInterface(player2, -1)) {
                        value = player2;
                        ((Player)value).packetSender.sendPlayerOption("null", 1, false);
                        value = InterfaceDefinition.forId(11092);
                        if (!player2.isInterfaceOpen((InterfaceDefinition)value)) {
                            value = player2;
                            ((Player)value).packetSender.sendMinimapState(0);
                        }
                    }
                }
            }
        }
        if ((player2 = player).isInMultiCombatArea()) {
            Player player8 = player2;
            player8.packetSender.sendMultiwayAreaState(true);
        } else {
            Player player9 = player2;
            player9.packetSender.sendMultiwayAreaState(false);
        }
        if (player.getAlchemistPlaygroundController().isInsidePlayground()) {
            player.getAlchemistPlaygroundController().refreshPizazzInterface();
        }
        if (player.getEnchantmentChamberController().isInsideChamber()) {
            player.getEnchantmentChamberController().refreshPizazzInterface();
        }
        if (player.getTelekineticTheatreController().isInsideTheatre()) {
            player.getTelekineticTheatreController().refreshPizazzInterface();
        }
        if (player.getCreatureGraveyardController().isInsideGraveyard()) {
            player.getCreatureGraveyardController().refreshPizazzInterface();
        }
    }

    public static void refreshRubberChickenPlayerOption(Player player) {
        if (player.getEquipmentManager().getItemIdAtSlot(3) == 4566) {
            player.packetSender.sendPlayerOption("Whack", 5, false);
            return;
        }
        player.packetSender.sendPlayerOption("null", 5, false);
    }

    public static boolean updateWalkableInterface(Player player, int interfaceId) {
        if (player.getCurrentWalkableInterfaceId() == interfaceId) {
            return false;
        }
        if (player.barrowsChestOpened && interfaceId == 4535) {
            return true;
        }
        player.setCurrentWalkableInterfaceId(interfaceId);
        player.packetSender.showWalkableInterface(interfaceId);
        return true;
    }

    public static Position randomUnblockedPositionInRange(PositionRange positionRange) {
        Position position = new Position(positionRange.getMinPosition().getX() + GameUtil.randomInclusive(positionRange.getMaxPosition().getX() - positionRange.getMinPosition().getX()), positionRange.getMinPosition().getY() + GameUtil.randomInclusive(positionRange.getMaxPosition().getY() - positionRange.getMinPosition().getY()), positionRange.getMinPosition().getPlane());
        while (WalkingCollisionMap.getTileFlags(position.getX(), position.getY(), position.getPlane()) != 0) {
            position = new Position(positionRange.getMinPosition().getX() + GameUtil.randomInclusive(positionRange.getMaxPosition().getX() - positionRange.getMinPosition().getX()), positionRange.getMinPosition().getY() + GameUtil.randomInclusive(positionRange.getMaxPosition().getY() - positionRange.getMinPosition().getY()), positionRange.getMinPosition().getPlane());
        }
        return position;
    }

    public static int getRandomEventCombatLevelOffset(int level) {
        if (level < 10) {
            return 0;
        }
        if (level < 20) {
            return 1;
        }
        if (level < 40) {
            return 2;
        }
        if (level < 70) {
            return 3;
        }
        if (level < 110) {
            return 4;
        }
        return 5;
    }

    public static void spawnSkillRandomEventNpc(Player player, SkillRandomEventNpc skillRandomEventNpc) {
        Player player2 = player;
        if (player2.ownedNpc != null) {
            player2 = player;
            if (!player2.ownedNpc.isDead()) {
                return;
            }
        }
        GameplayHelper.spawnOwnedNpcAdjacentToPlayer(player, new Npc(skillRandomEventNpc.getBaseNpcId() + GameplayHelper.getRandomEventCombatLevelOffset(player.getCombatLevel())), true, true);
    }

    public static void openProductionInterface(Player interfaceId, String interfaceId2) {
        Object value;
        if (interfaceId2 == "potteryUnfired") {
            if (!ItemDefinition.isDefined(4438)) {
                GameplayHelper.showThreeOptionProductionInterface((Player)interfaceId, 1787, 1789, 1791, "Pot", "Pie Dish", "Bowl");
            } else {
                GameplayHelper.showFiveOptionProductionInterface((Player)interfaceId, 1787, 1789, 1791, 5352, 4438, "Pot", "Pie Dish", "Bowl", "Plant pot", "Pot lid");
            }
        } else if (interfaceId2 == "potteryFired") {
            if (!ItemDefinition.isDefined(4438)) {
                GameplayHelper.showThreeOptionProductionInterface((Player)interfaceId, 1931, 2313, 1923, "Pot", "Pie Dish", "Bowl");
            } else {
                GameplayHelper.showFiveOptionProductionInterface((Player)interfaceId, 1931, 2313, 1923, 5350, 4440, "Pot", "Pie Dish", "Bowl", "Plant pot", "Pot lid");
            }
        } else if (interfaceId2 == "silverCrafting") {
            if (!ItemDefinition.isDefined(5525)) {
                GameplayHelper.showTwoOptionProductionInterface((Player)interfaceId, 1714, 2961, "Unstrung symbol", "Silver sickle");
            } else {
                GameplayHelper.showThreeOptionProductionInterface((Player)interfaceId, 1714, 2961, 5525, "Unstrung symbol", "Silver sickle", "Tiara");
            }
        } else if (interfaceId2 == "spinning") {
            if (!ItemDefinition.isDefined(6051)) {
                GameplayHelper.showTwoOptionProductionInterface((Player)interfaceId, 1737, 1779, "Wool", "Flax");
            } else {
                GameplayHelper.showThreeOptionProductionInterface((Player)interfaceId, 1737, 1779, 6051, "Wool", "Flax", "Magic tree");
            }
        } else if (interfaceId2 == "glassMaking") {
            if (!ItemDefinition.isDefined(4529)) {
                GameplayHelper.showThreeOptionProductionInterface((Player)interfaceId, 229, 567, 1919, "Vial", "Orb", "Beer Glass");
            } else if (!ItemDefinition.isDefined(6667)) {
                value = interfaceId;
                ((Player)value).packetSender.showChatboxInterface(11462);
            } else {
                value = interfaceId;
                ((Player)value).packetSender.showInterface(11462);
            }
        } else if (interfaceId2 == "normalLeather") {
            value = interfaceId;
            ((Player)value).packetSender.showInterface(2311);
        } else if (interfaceId2 == "hardLeather") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 1131, "Hard leather body");
        } else if (interfaceId2 == "dramenBranch") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 772, "Dramen staff");
        } else if (interfaceId2 == "greenLeather") {
            GameplayHelper.showThreeOptionProductionInterface((Player)interfaceId, 1065, 1099, 1135, "Vamb", "Chaps", "Body");
        } else if (interfaceId2 == "blueLeather") {
            GameplayHelper.showThreeOptionProductionInterface((Player)interfaceId, 2487, 2493, 2499, "Vamb", "Chaps", "Body");
        } else if (interfaceId2 == "redLeather") {
            GameplayHelper.showThreeOptionProductionInterface((Player)interfaceId, 2489, 2495, 2501, "Vamb", "Chaps", "Body");
        } else if (interfaceId2 == "blackLeather") {
            GameplayHelper.showThreeOptionProductionInterface((Player)interfaceId, 2491, 2497, 2503, "Vamb", "Chaps", "Body");
        } else if (interfaceId2 == "snakeskin1") {
            GameplayHelper.showTwoOptionProductionInterface((Player)interfaceId, 6326, 6328, "Bandana", "Boots");
        } else if (interfaceId2 == "snakeskin2") {
            GameplayHelper.showThreeOptionProductionInterface((Player)interfaceId, 6330, 6324, 6322, "Vamb", "Chaps", "Body");
        } else if (interfaceId2 == "weaving") {
            Object value2;
            String text = "Basket";
            String text2 = "Empty sack";
            String text3 = "Cloth";
            int value3 = 5376;
            value3 = 5418;
            value3 = 3224;
            value = value2 = interfaceId;
            ((Player)value2).packetSender.sendInterfaceText(text3, 8889);
            value = value2;
            ((Player)value).packetSender.sendInterfaceText(text2, 8893);
            value = value2;
            ((Player)value).packetSender.sendInterfaceText(text, 8897);
            value = value2;
            ((Player)value).packetSender.sendInterfaceModel(8883, 150, 3224);
            value = value2;
            ((Player)value).packetSender.sendInterfaceModel(8884, 100, 5418);
            value = value2;
            ((Player)value).packetSender.sendInterfaceModel(8885, 100, 5376);
            value = value2;
            ((Player)value).packetSender.showChatboxInterface(8880);
        } else if (interfaceId2 == "shaft") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 53, "Headless arrows");
        } else if (interfaceId2 == "bronzeArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 882, "Bronze arrows");
        } else if (interfaceId2 == "ironArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 884, "Iron arrows");
        } else if (interfaceId2 == "steelArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 886, "Steel arrows");
        } else if (interfaceId2 == "mithrilArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 888, "Mithril arrows");
        } else if (interfaceId2 == "adamantArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 890, "Adamant arrows");
        } else if (interfaceId2 == "runeArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 892, "Rune arrows");
        } else if (interfaceId2 == "bronzeDart") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 806, "Bronze darts");
        } else if (interfaceId2 == "ironDart") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 807, "Iron darts");
        } else if (interfaceId2 == "steelDart") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 808, "Steel darts");
        } else if (interfaceId2 == "mithrilDart") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 809, "Mithril darts");
        } else if (interfaceId2 == "adamantDart") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 810, "Adamant darts");
        } else if (interfaceId2 == "runeDart") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 811, "Rune darts");
        } else if (interfaceId2 == "bronzeBrutalArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 4773, "Bronze brutal arrows");
        } else if (interfaceId2 == "ironBrutalArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 4778, "Iron brutal arrows");
        } else if (interfaceId2 == "steelBrutalArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 4783, "Black brutal arrows");
        } else if (interfaceId2 == "blackBrutalArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 4788, "Steel brutal arrows");
        } else if (interfaceId2 == "mithrilBrutalArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 4793, "Mithril brutal arrows");
        } else if (interfaceId2 == "adamantBrutalArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 4798, "Adamant brutal arrows");
        } else if (interfaceId2 == "runeBrutalArrow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 4803, "Rune brutal arrows");
        } else if (interfaceId2 == "shortBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 841, "Shortbow");
        } else if (interfaceId2 == "longBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 839, "Longbow");
        } else if (interfaceId2 == "oakShortBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 843, "Oak shortbow");
        } else if (interfaceId2 == "oakLongBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 845, "Oak longbow");
        } else if (interfaceId2 == "compositeOgre") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 4827, "Composite Ogre bow");
        } else if (interfaceId2 == "willowShortBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 849, "Willow shortbow");
        } else if (interfaceId2 == "willowLongBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 849, "Willow longbow");
        } else if (interfaceId2 == "mapleShortBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 853, "Maple shortbow");
        } else if (interfaceId2 == "mapleLongBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 851, "Maple longbow");
        } else if (interfaceId2 == "yewShortBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 857, "Yew shortbow");
        } else if (interfaceId2 == "yewLongBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 855, "Yew longbow");
        } else if (interfaceId2 == "magicShortBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 861, "Magic shortbow");
        } else if (interfaceId2 == "magicLongBow") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 859, "Magic longbow");
        } else if (interfaceId2 == "normalCutting") {
            GameplayHelper.showThreeOptionProductionInterface((Player)interfaceId, 52, 50, 48, "Arrow shafts", "Shortbow", "Longbow");
        } else if (interfaceId2 == "oakCutting") {
            GameplayHelper.showTwoOptionProductionInterface((Player)interfaceId, 54, 56, "Oak shortbow", "Oak longbow");
        } else if (interfaceId2 == "acheyCutting") {
            GameplayHelper.showOneOptionProductionInterface((Player)interfaceId, 4825, "Unstrung comp bow");
        } else if (interfaceId2 == "willowCutting") {
            GameplayHelper.showTwoOptionProductionInterface((Player)interfaceId, 60, 58, "Willow shortbow", "Willow longbow");
        } else if (interfaceId2 == "mapleCutting") {
            GameplayHelper.showTwoOptionProductionInterface((Player)interfaceId, 64, 62, "Maple shortbow", "Maple longbow");
        } else if (interfaceId2 == "yewCutting") {
            GameplayHelper.showTwoOptionProductionInterface((Player)interfaceId, 68, 66, "Yew shortbow", "Yew longbow");
        } else if (interfaceId2 == "magicCutting") {
            GameplayHelper.showTwoOptionProductionInterface((Player)interfaceId, 72, 70, "Magic shortbow", "Magic longbow");
        } else if (interfaceId2 == "dairyChurn") {
            value = interfaceId;
            ((Player)value).packetSender.showChatboxInterface(15336);
        }
        Player player = interfaceId;
        player.interfaceAction = interfaceId2;
    }

    public static void showSplitbarkProductionInterface(Player player, int interfaceId, int value6, int value32, int value42, int value52, String interfaceId2, String value7, String text32, String text42, String text52, String text62) {
        Player player2 = player;
        player2.packetSender.sendInterfaceModel(8941, 120, 3385);
        player2 = player;
        player2.packetSender.sendInterfaceModel(8942, 150, 3387);
        player2 = player;
        player2.packetSender.sendInterfaceModel(8943, 150, 3389);
        player2 = player;
        player2.packetSender.sendInterfaceModel(8944, 120, 3391);
        player2 = player;
        player2.packetSender.sendInterfaceModel(8945, 150, 3393);
        player2 = player;
        player2.packetSender.sendInterfaceText(interfaceId2, 8949);
        player2 = player;
        player2.packetSender.sendInterfaceText(value7, 8953);
        player2 = player;
        player2.packetSender.sendInterfaceText(text32, 8957);
        player2 = player;
        player2.packetSender.sendInterfaceText(text42, 8961);
        player2 = player;
        player2.packetSender.sendInterfaceText(text52, 8965);
        player2 = player;
        player2.packetSender.sendInterfaceText(text62, 8966);
        player2 = player;
        player2.packetSender.showChatboxInterface(8938);
    }

    public static void showFiveOptionProductionInterface(Player player, int interfaceId, int value6, int value32, int value42, int value52, String interfaceId2, String value7, String text32, String text42, String text52) {
        Player player2 = player;
        player2.packetSender.sendInterfaceModel(8941, 120, interfaceId);
        player2 = player;
        player2.packetSender.sendInterfaceModel(8942, 150, value6);
        player2 = player;
        player2.packetSender.sendInterfaceModel(8943, 150, value32);
        player2 = player;
        player2.packetSender.sendInterfaceModel(8944, 120, value42);
        player2 = player;
        player2.packetSender.sendInterfaceModel(8945, 150, value52);
        player2 = player;
        player2.packetSender.sendInterfaceText(interfaceId2, 8949);
        player2 = player;
        player2.packetSender.sendInterfaceText(value7, 8953);
        player2 = player;
        player2.packetSender.sendInterfaceText(text32, 8957);
        player2 = player;
        player2.packetSender.sendInterfaceText(text42, 8961);
        player2 = player;
        player2.packetSender.sendInterfaceText(text52, 8965);
        player2 = player;
        player2.packetSender.showChatboxInterface(8938);
    }

    public static void showThreeOptionProductionInterface(Player player, int interfaceId, int value4, int value32, String interfaceId2, String value5, String text32) {
        if (ServerSettings.cacheVersion < 274) {
            Player player2 = player;
            player2.packetSender.setInterfaceHiddenFlag(1, 2476);
            player2 = player;
            player2.packetSender.setInterfaceHiddenFlag(0, 2479);
            player2 = player;
            player2.packetSender.sendInterfaceText("What would you like to make?", 2470);
            player2 = player;
            player2.packetSender.sendInterfaceText(interfaceId2, 2471);
            player2 = player;
            player2.packetSender.sendInterfaceText(value5, 2472);
            player2 = player;
            player2.packetSender.sendInterfaceText(text32, 2473);
            player2 = player;
            player2.packetSender.showChatboxInterface(2469);
            return;
        }
        Player player3 = player;
        player3.packetSender.sendInterfaceText(interfaceId2, 8889);
        player3 = player;
        player3.packetSender.sendInterfaceText(value5, 8893);
        player3 = player;
        player3.packetSender.sendInterfaceText(text32, 8897);
        player3 = player;
        player3.packetSender.sendInterfaceModel(8883, 180, interfaceId);
        player3 = player;
        player3.packetSender.sendInterfaceModel(8884, 180, value4);
        player3 = player;
        player3.packetSender.sendInterfaceModel(8885, 180, value32);
        player3 = player;
        player3.packetSender.showChatboxInterface(8880);
    }

    public static void showTwoOptionProductionInterface(Player player, int interfaceId, int value3, String interfaceId2, String value4) {
        if (ServerSettings.cacheVersion < 274) {
            Player player2 = player;
            player2.packetSender.setInterfaceHiddenFlag(1, 2465);
            player2 = player;
            player2.packetSender.setInterfaceHiddenFlag(0, 2468);
            player2 = player;
            player2.packetSender.sendInterfaceText("What would you like to make?", 2460);
            player2 = player;
            player2.packetSender.sendInterfaceText(interfaceId2, 2461);
            player2 = player;
            player2.packetSender.sendInterfaceText(value4, 2462);
            player2 = player;
            player2.packetSender.showChatboxInterface(2459);
            return;
        }
        Player player3 = player;
        player3.packetSender.sendInterfaceText(interfaceId2, 8874);
        player3 = player;
        player3.packetSender.sendInterfaceText(value4, 8878);
        player3 = player;
        player3.packetSender.sendInterfaceModel(8869, 180, interfaceId);
        player3 = player;
        player3.packetSender.sendInterfaceModel(8870, 180, value3);
        player3 = player;
        player3.packetSender.showChatboxInterface(8866);
    }

    public static void showOneOptionProductionInterface(Player player, int interfaceId, String interfaceId2) {
        if (ServerSettings.cacheVersion < 334) {
            Player player2 = player;
            player2.packetSender.sendGameMessage("This action does not currently work on this build!");
            return;
        }
        Player player3 = player;
        player3.packetSender.sendInterfaceText(interfaceId2, 2799);
        player3 = player;
        player3.packetSender.sendInterfaceModel(1746, 200, interfaceId);
        player3 = player;
        player3.packetSender.showChatboxInterface(4429);
    }

    public static String getIndefiniteArticle(String text2) {
        String[] stringValues = new String[]{"a", "e", "i", "o", "u", "y"};
        int index = 0;
        while (index < 6) {
            String vowel = stringValues[index];
            if (text2.toLowerCase().startsWith(vowel)) {
                return "an";
            }
            ++index;
        }
        return "a";
    }

    /*
     * Enabled aggressive block sorting
     */
    private static void shoutNearbyGnomeTrainer(Player player, String text) {
        if (player == null || text == null) {
            return;
        }
        Npc nearestTrainer = null;
        int nearestDistance = 16;
        for (Object localNpcObject : player.getLocalNpcs()) {
            Npc npc = (Npc)localNpcObject;
            if (npc == null || npc.getDefinition() == null
                    || !npc.getDefinition().getName().equalsIgnoreCase("Gnome trainer")) {
                continue;
            }
            int distance = GameUtil.getDistance(player.getPosition(), npc.getPosition());
            if (distance >= nearestDistance) {
                continue;
            }
            nearestTrainer = npc;
            nearestDistance = distance;
        }
        if (nearestTrainer != null) {
            nearestTrainer.getUpdateState().setForcedText(text);
        }
    }

    public static boolean handleAgilityObjectAction(Player player, int objectId, int value8, int value32) {
        int value2;
        int value4 = value32;
        int value5 = value8;
        int value6 = objectId;
        Player player2 = player;
        switch (value6) {
            case 2295: {
                if (value5 != 2474 || value4 != 3435) break;
                AgilityObstacleHandler.startAgilityMovement(player2, 7.5, 0, 3429 - player2.getPosition().getY(), -1, 762, -1, 4, "You walk carefully across the slippery log...", "...You make it safely to the other side.");
                player2.gnomeAgilityCourseProgress = 1;
                return true;
            }
            case 2285: {
                if (!(value5 == 2475 && value4 == 3425 || value5 == 2473 && value4 == 3425) && (value5 != 2471 || value4 != 3425) || player2.getPosition().getY() < 3426) break;
                GameplayHelper.shoutNearbyGnomeTrainer(player2, "That's it, straight up");
                value2 = 3424 - player2.getPosition().getY();
                AgilityObstacleHandler.startPositionOffsetObstacle(player2, 7.5, 0, value2, 1, 828, 2, "You climb the netting...", null);
                if (player2.gnomeAgilityCourseProgress != 1) return true;
                player2.gnomeAgilityCourseProgress = 2;
                return true;
            }
            case 2313: {
                if (value5 != 2473 || value4 != 3422) break;
                int branchUpDeltaX = 2473 - player2.getPosition().getX();
                value2 = 3420 - player2.getPosition().getY();
                AgilityObstacleHandler.startPositionOffsetObstacle(player2, 5.0, branchUpDeltaX, value2, 1, 828, 2, "You climb the tree...", "...To the platform above.");
                if (player2.gnomeAgilityCourseProgress != 2) return true;
                player2.gnomeAgilityCourseProgress = 3;
                return true;
            }
            case 2312: {
                if (value5 != 2478 || value4 != 3420) break;
                GameplayHelper.shoutNearbyGnomeTrainer(player2, "Come on scaredy cat get across that rope");
                int ropeDeltaX = 2483 - player2.getPosition().getX();
                int ropeDeltaY = 3420 - player2.getPosition().getY();
                AgilityObstacleHandler.startAgilityMovement(player2, 7.5, ropeDeltaX, ropeDeltaY, -1, 762, -1, 3, "You carefully cross the tightrope.", null);
                if (player2.gnomeAgilityCourseProgress != 3) return true;
                player2.gnomeAgilityCourseProgress = 4;
                return true;
            }
            case 2314: {
                if (value5 != 2486 || value4 != 3419) break;
                GameplayHelper.shoutNearbyGnomeTrainer(player2, "My granny can move faster than you");
                int branchDownDeltaX = 2486 - player2.getPosition().getX();
                int branchDownDeltaY = 3420 - player2.getPosition().getY();
                AgilityObstacleHandler.startPositionOffsetObstacle(player2, 5.0, branchDownDeltaX, branchDownDeltaY, -2, 828, 2, "You climb down the tree...", "You land on the ground.");
                if (player2.gnomeAgilityCourseProgress != 4) return true;
                player2.gnomeAgilityCourseProgress = 5;
                return true;
            }
            case 2315: {
                if (value5 != 2487 || (value4 != 3420 && value4 != 3418)) break;
                GameplayHelper.shoutNearbyGnomeTrainer(player2, "My granny can move faster than you");
                int branchDownDeltaX = 2486 - player2.getPosition().getX();
                int branchDownDeltaY = 3420 - player2.getPosition().getY();
                AgilityObstacleHandler.startPositionOffsetObstacle(player2, 5.0, branchDownDeltaX, branchDownDeltaY, -2, 828, 2, "You climb down the tree...", "You land on the ground.");
                if (player2.gnomeAgilityCourseProgress != 4) return true;
                player2.gnomeAgilityCourseProgress = 5;
                return true;
            }
            case 2286: {
                if (!(value5 == 2483 && value4 == 3426 || value5 == 2485 && value4 == 3426) && (value5 != 2487 || value4 != 3426) || player2.getPosition().getY() > 3425) break;
                GameplayHelper.shoutNearbyGnomeTrainer(player2, "move it, move it, move it!");
                value2 = 3427 - player2.getPosition().getY();
                AgilityObstacleHandler.startPositionOffsetObstacle(player2, 7.5, 0, value2, 0, 828, 2, "You climb the netting...", null);
                if (player2.gnomeAgilityCourseProgress != 5) return true;
                player2.gnomeAgilityCourseProgress = 6;
                return true;
            }
            case 154: 
            case 4058: {
                if ((value5 != 2484 || value4 != 3431) && (value5 != 2487 || value4 != 3431)) break;
                double value7 = player2.gnomeAgilityCourseProgress == 6 ? 46.5 : 7.5;
                AgilityObstacleHandler.startGnomePipeTraversal(player2, value7, value5, 3437);
                player2.gnomeAgilityCourseProgress = 0;
                return true;
            }
        }
        boolean enabled = false;
        if (enabled) {
            return true;
        }
        value4 = value32;
        value5 = value8;
        value6 = objectId;
        player2 = player;
        switch (value6) {
            case 2287: {
                if (value5 != 2552 || value4 != 3559) break;
                if (!SkillActionHelper.checkSkillRequirement(player2, 16, 35, "enter")) {
                    return true;
                }
                AgilityObstacleHandler.startAgilityMovement(player2, 10.0, 0, player2.getPosition().getY() >= 3560 ? -3 : 3, 746, 844, 748, 3, null, null);
                return true;
            }
            case 2282: {
                if (value5 != 2551 || value4 != 3550) break;
                AgilityObstacleHandler.startQueuedObstacleMovement(player2, 22, 0, -5, 1, 60, -1, 751);
                Player player3 = player2;
                player3.packetSender.sendObjectAnimation(2551, 3550, 0, 127);
                player2.agilityCourseProgress = 1;
                return true;
            }
            case 2294: {
                if (value5 != 2550 || value4 != 3546) break;
                AgilityObstacleHandler.startAgilityMovement(player2, 13.0, -10, 0, -1, 762, -1, 5, "You walk carefully across the slippery log...", "...You make it safely to the other side.");
                if (player2.agilityCourseProgress != 1) return true;
                player2.agilityCourseProgress = 2;
                return true;
            }
            case 2284: {
                if (value5 != 2538 || value4 != 3545) break;
                AgilityObstacleHandler.startPositionOffsetObstacle(player2, 8.0, -1, 0, 1, 828, 2, "You climb the netting...", null);
                if (player2.agilityCourseProgress != 2) return true;
                player2.agilityCourseProgress = 3;
                return true;
            }
            case 2302: {
                if (value5 != 2535 || value4 != 3547) break;
                AgilityObstacleHandler.startAgilityMovement(player2, 22.0, -4, 0, -1, 756, -1, 3, null, null);
                if (player2.agilityCourseProgress != 3) return true;
                player2.agilityCourseProgress = 4;
                return true;
            }
            case 1948: {
                if (value5 == 2536 && value4 == 3553) {
                    AgilityObstacleHandler.startAgilityMovement(player2, 13.0, 2, 0, -1, 839, -1, 1, null, null);
                    if (player2.agilityCourseProgress != 4) return true;
                    player2.agilityCourseProgress = 5;
                    return true;
                }
                if (value5 == 2539 && value4 == 3553) {
                    AgilityObstacleHandler.startAgilityMovement(player2, 13.0, 2, 0, -1, 839, -1, 1, null, null);
                    if (player2.agilityCourseProgress != 5) return true;
                    player2.agilityCourseProgress = 6;
                    return true;
                }
                if (value5 != 2542 || value4 != 3553) break;
                value2 = player2.agilityCourseProgress == 6 ? 60 : 13;
                AgilityObstacleHandler.startAgilityMovement(player2, value2, 2, 0, -1, 839, -1, 1, null, null);
                player2.agilityCourseProgress = 0;
                return true;
            }
        }
        boolean enabled2 = false;
        if (enabled2) {
            return true;
        }
        value4 = value32;
        value5 = value8;
        value6 = objectId;
        player2 = player;
        switch (value6) {
            case 2309: {
                if (value5 != 2998 || value4 != 3917) break;
                if (!SkillActionHelper.checkSkillRequirement(player2, 16, 52, "enter")) {
                    return true;
                }
                Player player4 = player2;
                player4.packetSender.openSingleDoor(value6, value5, value4, 0);
                AgilityObstacleHandler.startAgilityMovement(player2, 13.0, 0, 15, -1, 762, -1, 8, "You go through the gate and try to edge over the ridge...", "...You skillfully balance across the ridge...");
                return true;
            }
            case 2307: 
            case 2308: {
                if ((value5 != 2998 || value4 != 3931) && (value5 != 2997 || value4 != 3931)) break;
                AgilityObstacleHandler.startAgilityMovement(player2, 13.0, 0, -15, -1, 762, -1, 8, "You go through the gate and try to edge over the ridge...", "...You skillfully balance across the ridge...");
                return true;
            }
            case 2288: {
                if (value5 != 3004 || value4 != 3938) break;
                if (!SkillActionHelper.checkSkillRequirement(player2, 16, 49, "to go through.")) {
                    return true;
                }
                AgilityObstacleHandler.startAgilityMovement(player2, 12.5, 0, 13, 746, 844, 748, 9, null, null);
                player2.agilityCourseProgress = 1;
                return true;
            }
            case 2283: {
                if (value5 != 3005 || value4 != 3952) break;
                AgilityObstacleHandler.startQueuedObstacleMovement(player2, 20, 0, 7, 1, 60, -1, 751);
                Player player5 = player2;
                player5.packetSender.sendObjectAnimation(2551, 3550, 0, 127);
                player5 = player2;
                player5.packetSender.sendGameMessage("You skillfully swing across.");
                if (player2.agilityCourseProgress != 1) return true;
                player2.agilityCourseProgress = 2;
                return true;
            }
            case 2311: {
                if (value5 != 3001 || value4 != 3960) break;
                player2.moveTo(new Position(2996, 3960, 0));
                player2.getSkillManager().addExperience(16, 20.0);
                if (player2.agilityCourseProgress != 2) return true;
                player2.agilityCourseProgress = 3;
                return true;
            }
            case 2297: {
                if (value5 != 3001 || value4 != 3945) break;
                AgilityObstacleHandler.startAgilityMovement(player2, 20.0, -8, 0, -1, 762, -1, 4, "You walk carefully across the slippery log...", "You skillfully edge across the gap.");
                if (player2.agilityCourseProgress != 3) return true;
                player2.agilityCourseProgress = 4;
                return true;
            }
            case 2328: {
                value2 = player2.agilityCourseProgress == 4 ? 499 : 0;
                AgilityObstacleHandler.startPositionOffsetObstacle(player2, value2, 0, -4, 0, 828, 1, null, null);
                player2.agilityCourseProgress = 0;
                return true;
            }
        }
        boolean enabled3 = false;
        if (enabled3) {
            return true;
        }
        value4 = value32;
        value5 = value8;
        value6 = objectId;
        player2 = player;
        switch (value6) {
            case 2296: {
                if (value5 != 2599 || value4 != 3477) {
                    if (value5 != 2602) return false;
                    if (value4 != 3477) return false;
                }
                if (player2.getSkillManager().getCurrentLevels()[16] < 20) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 20 to do that.");
                    return true;
                }
                AgilityObstacleHandler.startAgilityMovement(player2, GameUtil.randomInclusive(5) == 0 ? 7 : 0, value5 < 2600 ? 5 : -5, 0, -1, 762, -1, 3, "You walk carefully across the slippery log...", "...You make it safely to the other side.");
                return true;
            }
            case 9330: {
                if (value5 != 2601) return false;
                if (value4 != 3336) return false;
                if (player2.getSkillManager().getCurrentLevels()[16] < 32) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 32 to do that.");
                    return true;
                }
                AgilityObstacleHandler.startAgilityMovement(player2, 4.0, -4, 0, -1, 762, -1, 2, "You walk carefully across the slippery log...", "...You make it safely to the other side.");
                return true;
            }
            case 9328: {
                if (value5 != 2599) return false;
                if (value4 != 3336) return false;
                if (player2.getSkillManager().getCurrentLevels()[16] < 32) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 32 to do that.");
                    return true;
                }
                AgilityObstacleHandler.startAgilityMovement(player2, 4.0, 4, 0, -1, 762, -1, 2, "You walk carefully across the slippery log...", "...You make it safely to the other side.");
                return true;
            }
            case 12127: {
                if (value5 == 2400 && value4 == 4403) {
                    if (player2.getSkillManager().getCurrentLevels()[16] < 44) {
                        player2.getDialogueManager().showOneLineStatement("You need a agility level of 44 to do that.");
                        return true;
                    }
                    player2.getUpdateState().setAnimation(754);
                    player2.getSkillManager().addExperience(16, 10.0);
                    AgilityObstacleHandler.startForcedMovement(player2, 0, player2.getPosition().getY() < 4404 ? 2 : -2, 1, 80, 2, true, 0, 0);
                    return true;
                }
                if (value5 == 2408 && value4 == 4395) {
                    if (player2.getSkillManager().getCurrentLevels()[16] < 66) {
                        player2.getDialogueManager().showOneLineStatement("You need a agility level of 66 to do that.");
                        return true;
                    }
                    player2.getUpdateState().setAnimation(754);
                    player2.getSkillManager().addExperience(16, 10.0);
                    AgilityObstacleHandler.startForcedMovement(player2, 0, player2.getPosition().getY() < 4396 ? 2 : -2, 1, 80, 2, true, 0, 0);
                    return true;
                }
                if (value5 != 2415) return false;
                if (value4 != 4402) return false;
                if (player2.getSkillManager().getCurrentLevels()[16] < 66) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 66 to do that.");
                    return true;
                }
                player2.getUpdateState().setAnimation(754);
                player2.getSkillManager().addExperience(16, 10.0);
                AgilityObstacleHandler.startForcedMovement(player2, 0, player2.getPosition().getY() < 4403 ? 2 : -2, 1, 80, 2, true, 0, 0);
                return true;
            }
            case 9300: {
                if (player2.getSkillManager().getCurrentLevels()[16] < 13) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 13 to do that.");
                    return true;
                }

                int plane = player2.getPosition().getPlane();
                int orientation = SkillActionHelper.getObjectOrientation(
                        value6, value5, value4, plane);
                int deltaX = 0;
                int deltaY = 0;

                // Type-0 wall orientations block the edge immediately west,
                // north, east or south of the object's map tile respectively.
                // Move one tile across that blocked edge from whichever side
                // the player approached the broken fence.
                if (orientation == 0) {
                    deltaX = player2.getPosition().getX() < value5 ? 1 : -1;
                } else if (orientation == 1) {
                    deltaY = player2.getPosition().getY() <= value4 ? 1 : -1;
                } else if (orientation == 2) {
                    deltaX = player2.getPosition().getX() <= value5 ? 1 : -1;
                } else if (orientation == 3) {
                    deltaY = player2.getPosition().getY() < value4 ? 1 : -1;
                } else {
                    return false;
                }

                player2.getUpdateState().setFacePosition(
                        new Position(value5, value4, plane));
                player2.getUpdateState().setAnimation(839);
                player2.packetSender.queueRelativeMovementStep(deltaX, deltaY, true);
                return true;
            }
            case 9302: {
                if (value5 != 2575) return false;
                if (value4 != 3111) return false;
                if (player2.getSkillManager().getCurrentLevels()[16] < 16) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 16 to do that.");
                    return true;
                }
                player2.moveTo(new Position(2575, 3107, 0));
                return true;
            }
            case 9301: {
                if (value5 != 2575) return false;
                if (value4 != 3108) return false;
                if (player2.getSkillManager().getCurrentLevels()[16] < 16) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 16 to do that.");
                    return true;
                }
                player2.moveTo(new Position(2575, 3112, 0));
                return true;
            }
            case 9294: {
                if (value5 != 2879) return false;
                if (value4 != 9813) return false;
                if (player2.getSkillManager().getCurrentLevels()[16] < 80) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 80 to do that.");
                    return true;
                }
                if (player2.getPosition().getX() < 2880) {
                    player2.moveTo(new Position(2880, 9813, 0));
                    return true;
                } else {
                    player2.moveTo(new Position(2878, 9813, 0));
                }
                return true;
            }
            case 9326: {
                if (value5 != 2769 || value4 != 10002) {
                    if (value5 != 2774) return false;
                    if (value4 != 10003) return false;
                }
                if (player2.getSkillManager().getCurrentLevels()[16] < 81) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 81 to do that.");
                    return true;
                }
                if (value5 == 2774 && value4 == 10003) {
                    player2.moveTo(new Position(2768, 10002, 0));
                    return true;
                } else {
                    player2.moveTo(new Position(2775, 10003, 0));
                }
                return true;
            }
            case 9321: {
                if (value5 != 2734 || value4 != 10008) {
                    if (value5 != 2731) return false;
                    if (value4 != 10008) return false;
                }
                if (player2.getSkillManager().getCurrentLevels()[16] < 61) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 61 to do that.");
                    return true;
                }
                if (value5 == 2731 && value4 == 10008) {
                    player2.moveTo(new Position(2735, 10008, 0));
                    return true;
                } else {
                    player2.moveTo(new Position(2730, 10008, 0));
                }
                return true;
            }
            case 9324: {
                if (value5 != 2722) return false;
                if (value4 != 3593) return false;
                if (player2.getSkillManager().getCurrentLevels()[16] < 47) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 47 to do that.");
                    return true;
                }
                AgilityObstacleHandler.startAgilityMovement(player2, 0.0, 0, 4, -1, 762, -1, 2, "You walk carefully across the slippery log...", "...You make it safely to the other side.");
                return true;
            }
            case 9322: {
                if (value5 != 2722) return false;
                if (value4 != 3595) return false;
                if (player2.getSkillManager().getCurrentLevels()[16] < 47) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 47 to do that.");
                    return true;
                }
                AgilityObstacleHandler.startAgilityMovement(player2, 0.0, 0, -4, -1, 762, -1, 2, "You walk carefully across the slippery log...", "...You make it safely to the other side.");
                return true;
            }
            case 1947: 
            case 11844: {
                if (value5 != 2935) return false;
                if (value4 != 3355) return false;
                if (!player2.isMember()) {
                    player2.packetSender.sendGameMessage("You need a members account to access members content.");
                    return true;
                }
                if (ServerSettings.freeToPlayWorld) {
                    player2.packetSender.sendGameMessage("You need to be in members world to access members content.");
                    return true;
                }
                if (player2.getSkillManager().getCurrentLevels()[16] < 5) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 5 to do that.");
                    return true;
                }
                AgilityObstacleHandler.startAgilityMovement(player2, 0.5, player2.getPosition().getX() < 2936 ? 2 : -2, 0, -1, 839, -1, 1, null, null);
                return true;
            }
            case 9295: {
                if (value5 == 3150 && value4 == 9906) {
                    if (player2.getSkillManager().getCurrentLevels()[16] < 51) {
                        player2.getDialogueManager().showOneLineStatement("You need a agility level of 51 to do that.");
                        return true;
                    }
                    AgilityObstacleHandler.startAgilityMovement(player2, 0.0, 6, 0, 746, 844, 748, 6, null, null);
                    return true;
                }
                if (value5 != 3153) return false;
                if (value4 != 9906) return false;
                if (player2.getSkillManager().getCurrentLevels()[16] < 51) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 51 to do that.");
                    return true;
                }
                AgilityObstacleHandler.startAgilityMovement(player2, 0.0, -6, 0, 746, 844, 748, 6, null, null);
                return true;
            }
            case 9293: {
                if (value5 == 2887 && value4 == 9799) {
                    if (player2.getSkillManager().getCurrentLevels()[16] < 70) {
                        player2.getDialogueManager().showOneLineStatement("You need a agility level of 70 to do that.");
                        return true;
                    }
                    AgilityObstacleHandler.startAgilityMovement(player2, 0.0, 6, 0, 746, 844, 748, 6, null, null);
                    return true;
                }
                if (value5 != 2890) return false;
                if (value4 != 9799) return false;
                if (player2.getSkillManager().getCurrentLevels()[16] < 70) {
                    player2.getDialogueManager().showOneLineStatement("You need a agility level of 70 to do that.");
                    return true;
                }
                AgilityObstacleHandler.startAgilityMovement(player2, 0.0, -6, 0, 746, 844, 748, 6, null, null);
                return true;
            }
        }
        return false;
    }

    public static boolean handleFlourDoughButton(Player player, int buttonId) {
        Player player2 = player;
        if (player2.interfaceAction != "flour") {
            return false;
        }
        switch (buttonId) {
            case 2482: 
            case 8209: {
                player.getInventoryManager().removeItem(new ItemStack(1929));
                player.getInventoryManager().removeItem(new ItemStack(1933));
                player.getInventoryManager().addItem(new ItemStack(1925));
                player.getInventoryManager().addItem(new ItemStack(1931));
                player2 = player;
                player2.packetSender.sendGameMessage("You put the water on the flour and make it into a bread dough");
                player.getInventoryManager().addOrDropItem(new ItemStack(2307));
                player2 = player;
                player2.packetSender.closeInterfaces();
                return true;
            }
            case 2483: 
            case 8210: {
                if (!player.getInventoryManager().getContainer().containsItem(1929) || !player.getInventoryManager().getContainer().containsItem(1933)) {
                    return true;
                }
                player.getInventoryManager().removeItem(new ItemStack(1929));
                player.getInventoryManager().removeItem(new ItemStack(1933));
                player.getInventoryManager().addItem(new ItemStack(1925));
                player.getInventoryManager().addItem(new ItemStack(1931));
                player2 = player;
                player2.packetSender.sendGameMessage("You put the water on the flour and make it into a pastry dough");
                player.getInventoryManager().addOrDropItem(new ItemStack(1953));
                player2 = player;
                player2.packetSender.closeInterfaces();
                return true;
            }
            case 2484: 
            case 8211: {
                if (!player.getInventoryManager().getContainer().containsItem(1929) || !player.getInventoryManager().getContainer().containsItem(1933)) {
                    return true;
                }
                player.getInventoryManager().removeItem(new ItemStack(1929));
                player.getInventoryManager().removeItem(new ItemStack(1933));
                player.getInventoryManager().addItem(new ItemStack(1925));
                player.getInventoryManager().addItem(new ItemStack(1931));
                player2 = player;
                player2.packetSender.sendGameMessage("You put the water on the flour and make it into a pizza base");
                player.getInventoryManager().addOrDropItem(new ItemStack(2283));
                player2 = player;
                player2.packetSender.closeInterfaces();
                return true;
            }
            case 2485: 
            case 8212: {
                if (!player.getInventoryManager().getContainer().containsItem(1929) || !player.getInventoryManager().getContainer().containsItem(1933)) {
                    return true;
                }
                player.getInventoryManager().removeItem(new ItemStack(1929));
                player.getInventoryManager().removeItem(new ItemStack(1933));
                player.getInventoryManager().addItem(new ItemStack(1925));
                player.getInventoryManager().addItem(new ItemStack(1931));
                player2 = player;
                player2.packetSender.sendGameMessage("You put the water on the flour and make it into a pitta dough");
                player.getInventoryManager().addOrDropItem(new ItemStack(1863));
                player2 = player;
                player2.packetSender.closeInterfaces();
                return true;
            }
        }
        return false;
    }

    public static boolean handleLeatherCraftingItemUse(Player player, int itemId, int value6, int value32, int value42) {
        int value2 = value32 = itemId == 2370 ? value42 : value32;
        if (itemId == 2370 || value6 == 2370) {
            if (!ServerSettings.craftingEnabled) {
                Player player2 = player;
                player2.packetSender.sendGameMessage("This skill is currently disabled.");
                return true;
            }
            if (!player.isMember()) {
                player.packetSender.sendGameMessage("You need a members account to access members content.");
                return true;
            }
            if (ServerSettings.freeToPlayWorld) {
                player.packetSender.sendGameMessage("You need to be in members world to access members content.");
                return true;
            }
            if (itemId == 1131 || value6 == 1131) {
                Player player3 = player;
                player3.packetSender.sendGameMessage("You attach the steel studs to the hard leather body");
                if (player.getInventoryManager().removeItemFromSlot(new ItemStack(2370), value32)) {
                    player.getInventoryManager().setItemInSlot(new ItemStack(1133), value32);
                } else if (player.getInventoryManager().removeItem(new ItemStack(2370))) {
                    player.getInventoryManager().addItem(new ItemStack(1133));
                }
                return true;
            }
            if (itemId == 1095 || value6 == 1095) {
                Player player4 = player;
                player4.packetSender.sendGameMessage("You attach the steel studs to the leather chaps");
                if (player.getInventoryManager().removeItemFromSlot(new ItemStack(2370), value32)) {
                    player.getInventoryManager().setItemInSlot(new ItemStack(1097), value32);
                } else if (player.getInventoryManager().removeItem(new ItemStack(2370))) {
                    player.getInventoryManager().addItem(new ItemStack(1097));
                }
                return true;
            }
        }
        if (itemId == 1733 || value6 == 1733) {
            if (!ServerSettings.craftingEnabled) {
                Player player5 = player;
                player5.packetSender.sendGameMessage("This skill is currently disabled.");
                return true;
            }
            if (itemId == 1741 || value6 == 1741) {
                GameplayHelper.openProductionInterface(player, "normalLeather");
                return true;
            }
            if (itemId == 1743 || value6 == 1743) {
                Object value5 = "hardLeather";
                if (ServerSettings.cacheVersion < 334) {
                    String text = (String)value5;
                    value5 = player;
                    player.interfaceAction = text;
                    GameplayHelper.handleCraftedArmorButton(player, 2799, 1);
                } else {
                    GameplayHelper.openProductionInterface(player, (String)value5);
                }
                return true;
            }
            if (itemId == 1745 || value6 == 1745) {
                GameplayHelper.openProductionInterface(player, "greenLeather");
                return true;
            }
            if (itemId == 2505 || value6 == 2505) {
                GameplayHelper.openProductionInterface(player, "blueLeather");
                return true;
            }
            if (itemId == 2507 || value6 == 2507) {
                GameplayHelper.openProductionInterface(player, "redLeather");
                return true;
            }
            if (itemId == 2509 || value6 == 2509) {
                GameplayHelper.openProductionInterface(player, "blackLeather");
                return true;
            }
            if (itemId == 6287 || value6 == 6287) {
                GameplayHelper.openProductionInterface(player, "snakeskin1");
                return true;
            }
            if (itemId == 6289 || value6 == 6289) {
                GameplayHelper.openProductionInterface(player, "snakeskin2");
                return true;
            }
        }
        return false;
    }

    public static boolean handleCraftedArmorButton(Player player, int buttonId, int value6) {
        Enum enum_;
        int value2;
        int value3;
        Enum[] enumArray;
        int value4;
        Object value5 = player;
        if (((Player)value5).interfaceAction == "normalLeather") {
            if (player.botEnabled) {
                Player player2 = player;
                value5 = null;
                value4 = 0;
                enumArray = LeatherRecipe.values();
                value3 = enumArray.length;
                value2 = 0;
                while (value2 < value3) {
                    enum_ = enumArray[value2];
                    if (((LeatherRecipe)enum_).getRequiredLevel() <= player2.getSkillManager().getCurrentLevels()[12] && ((LeatherRecipe)enum_).getMaterialAmount() <= player2.getInventoryManager().getItemAmount(((LeatherRecipe)enum_).getMaterialItemId()) && ((LeatherRecipe)enum_).getRequiredLevel() > value4) {
                        value4 = ((LeatherRecipe)enum_).getRequiredLevel();
                        value5 = enum_;
                    }
                    ++value2;
                }
                int buttonId2 = buttonId = value5 != null ? ((LeatherRecipe)((Object)value5)).getButtonId() : -1;
            }
            if (LeatherCrafting.createForButton(player, buttonId, value6) != null) {
                LeatherCrafting.createForButton(player, buttonId, value6).startCrafting();
                return true;
            }
        }
        value5 = player;
        if (((Player)value5).interfaceAction == "greenLeather") {
            if (player.botEnabled) {
                Player player3 = player;
                value5 = null;
                value4 = 0;
                enumArray = GreenDragonhideRecipe.values();
                value3 = enumArray.length;
                value2 = 0;
                while (value2 < value3) {
                    enum_ = enumArray[value2];
                    if (((GreenDragonhideRecipe)enum_).getRequiredLevel() <= player3.getSkillManager().getCurrentLevels()[12] && ((GreenDragonhideRecipe)enum_).getMaterialAmount() <= player3.getInventoryManager().getItemAmount(((GreenDragonhideRecipe)enum_).getMaterialItemId()) && ((GreenDragonhideRecipe)enum_).getRequiredLevel() > value4) {
                        value4 = ((GreenDragonhideRecipe)enum_).getRequiredLevel();
                        value5 = enum_;
                    }
                    ++value2;
                }
                int buttonId3 = buttonId = value5 != null ? ((GreenDragonhideRecipe)((Object)value5)).getButtonId() : -1;
            }
            if (GreenDragonhideCrafting.createForButton(player, buttonId, value6) != null) {
                GreenDragonhideCrafting.createForButton(player, buttonId, value6).startCrafting();
                return true;
            }
        }
        value5 = player;
        if (((Player)value5).interfaceAction == "blueLeather") {
            if (player.botEnabled) {
                Player player4 = player;
                value5 = null;
                value4 = 0;
                enumArray = BlueDragonhideRecipe.values();
                value3 = enumArray.length;
                value2 = 0;
                while (value2 < value3) {
                    enum_ = enumArray[value2];
                    if (((BlueDragonhideRecipe)enum_).getRequiredLevel() <= player4.getSkillManager().getCurrentLevels()[12] && ((BlueDragonhideRecipe)enum_).getMaterialAmount() <= player4.getInventoryManager().getItemAmount(((BlueDragonhideRecipe)enum_).getMaterialItemId()) && ((BlueDragonhideRecipe)enum_).getRequiredLevel() > value4) {
                        value4 = ((BlueDragonhideRecipe)enum_).getRequiredLevel();
                        value5 = enum_;
                    }
                    ++value2;
                }
                int buttonId4 = buttonId = value5 != null ? ((BlueDragonhideRecipe)((Object)value5)).getButtonId() : -1;
            }
            if (BlueDragonhideCrafting.createForButton(player, buttonId, value6) != null) {
                BlueDragonhideCrafting.createForButton(player, buttonId, value6).startCrafting();
                return true;
            }
        }
        value5 = player;
        if (((Player)value5).interfaceAction == "redLeather") {
            if (player.botEnabled) {
                Player player5 = player;
                value5 = null;
                value4 = 0;
                enumArray = RedDragonhideRecipe.values();
                value3 = enumArray.length;
                value2 = 0;
                while (value2 < value3) {
                    enum_ = enumArray[value2];
                    if (((RedDragonhideRecipe)enum_).getRequiredLevel() <= player5.getSkillManager().getCurrentLevels()[12] && ((RedDragonhideRecipe)enum_).getMaterialAmount() <= player5.getInventoryManager().getItemAmount(((RedDragonhideRecipe)enum_).getMaterialItemId()) && ((RedDragonhideRecipe)enum_).getRequiredLevel() > value4) {
                        value4 = ((RedDragonhideRecipe)enum_).getRequiredLevel();
                        value5 = enum_;
                    }
                    ++value2;
                }
                int buttonId5 = buttonId = value5 != null ? ((RedDragonhideRecipe)((Object)value5)).getButtonId() : -1;
            }
            if (RedDragonhideCrafting.createForButton(player, buttonId, value6) != null) {
                RedDragonhideCrafting.createForButton(player, buttonId, value6).startCrafting();
                return true;
            }
        }
        value5 = player;
        if (((Player)value5).interfaceAction == "blackLeather") {
            if (player.botEnabled) {
                Player player6 = player;
                value5 = null;
                value4 = 0;
                enumArray = BlackDragonhideRecipe.values();
                value3 = enumArray.length;
                value2 = 0;
                while (value2 < value3) {
                    enum_ = enumArray[value2];
                    if (((BlackDragonhideRecipe)enum_).getRequiredLevel() <= player6.getSkillManager().getCurrentLevels()[12] && ((BlackDragonhideRecipe)enum_).getMaterialAmount() <= player6.getInventoryManager().getItemAmount(((BlackDragonhideRecipe)enum_).getMaterialItemId()) && ((BlackDragonhideRecipe)enum_).getRequiredLevel() > value4) {
                        value4 = ((BlackDragonhideRecipe)enum_).getRequiredLevel();
                        value5 = enum_;
                    }
                    ++value2;
                }
                int buttonId6 = buttonId = value5 != null ? ((BlackDragonhideRecipe)((Object)value5)).getButtonId() : -1;
            }
            if (BlackDragonhideCrafting.createForButton(player, buttonId, value6) != null) {
                BlackDragonhideCrafting.createForButton(player, buttonId, value6).startCrafting();
                return true;
            }
        }
        value5 = player;
        if (((Player)value5).interfaceAction == "hardLeather") {
            if (player.botEnabled) {
                Player player7 = player;
                value5 = null;
                value4 = 0;
                enumArray = HardLeatherRecipe.values();
                value3 = enumArray.length;
                value2 = 0;
                while (value2 < value3) {
                    enum_ = enumArray[value2];
                    if (((HardLeatherRecipe)enum_).getRequiredLevel() <= player7.getSkillManager().getCurrentLevels()[12] && ((HardLeatherRecipe)enum_).getMaterialAmount() <= player7.getInventoryManager().getItemAmount(((HardLeatherRecipe)enum_).getMaterialItemId()) && ((HardLeatherRecipe)enum_).getRequiredLevel() > value4) {
                        value4 = ((HardLeatherRecipe)enum_).getRequiredLevel();
                        value5 = enum_;
                    }
                    ++value2;
                }
                int buttonId7 = buttonId = value5 != null ? ((HardLeatherRecipe)((Object)value5)).getButtonId() : -1;
            }
            if (HardLeatherCrafting.createForButton(player, buttonId, value6) != null) {
                HardLeatherCrafting.createForButton(player, buttonId, value6).startCrafting();
                return true;
            }
        }
        value5 = player;
        if (((Player)value5).interfaceAction == "snakeskin1" && SnakeskinAccessoryCrafting.createForButton(player, buttonId, value6) != null) {
            SnakeskinAccessoryCrafting.createForButton(player, buttonId, value6).startCrafting();
            return true;
        }
        value5 = player;
        if (((Player)value5).interfaceAction == "snakeskin2" && SnakeskinArmorCrafting.createForButton(player, buttonId, value6) != null) {
            SnakeskinArmorCrafting.createForButton(player, buttonId, value6).startCrafting();
            return true;
        }
        value5 = player;
        if (((Player)value5).interfaceAction == "splitbark" && SplitbarkCrafting.createForButton(player, buttonId, value6) != null) {
            SplitbarkCrafting.createForButton(player, buttonId, value6).startCrafting();
            return true;
        }
        return false;
    }

    public static void openTanningInterface(Player player) {
        if (!ServerSettings.craftingEnabled) {
            Player player2 = player;
            player2.packetSender.sendGameMessage("This skill is currently disabled.");
            return;
        }
        int interactionTargetId = player.getInteractionTargetId() == 1041 ? 2 : 1;
        Object value = "tanning";
        Player player3 = player;
        player.interfaceAction = (String)value;
        player3 = player;
        player3.packetSender.showInterface(14670 >= InterfaceDefinition.interfaceCount ? 679 : 14670);
        player3 = player;
        player3.packetSender.sendInterfaceText("Soft Leather", 14777);
        if (player.getInventoryManager().getItemAmount(995) > 0) {
            player3 = player;
            player3.packetSender.sendInterfaceText(String.valueOf(1 * interactionTargetId) + " coins", 14785);
        } else {
            player3 = player;
            player3.packetSender.sendInterfaceText(String.valueOf(1 * interactionTargetId) + " coins", 14785);
        }
        player3 = player;
        player3.packetSender.sendInterfaceText("Hard Leather", 14778);
        if (player.getInventoryManager().getItemAmount(995) >= 3) {
            player3 = player;
            player3.packetSender.sendInterfaceText(String.valueOf(3 * interactionTargetId) + " coins", 14786);
        } else {
            player3 = player;
            player3.packetSender.sendInterfaceText(String.valueOf(3 * interactionTargetId) + " coins", 14786);
        }
        player3 = player;
        player3.packetSender.sendInterfaceModel(14769 >= InterfaceDefinition.interfaceCount ? 7445 : 14769, 250, 1741);
        player3 = player;
        player3.packetSender.sendInterfaceModel(14770 >= InterfaceDefinition.interfaceCount ? 7436 : 14770, 250, 1743);
        player3 = player;
        player3.packetSender.sendInterfaceModel(14773 >= InterfaceDefinition.interfaceCount ? 8694 : 14773, 250, 1753);
        player3 = player;
        player3.packetSender.sendInterfaceModel(14774 >= InterfaceDefinition.interfaceCount ? 8699 : 14774, 250, 1751);
        player3 = player;
        player3.packetSender.sendInterfaceModel(14771, 250, 6287);
        player3 = player;
        player3.packetSender.sendInterfaceModel(14775 >= InterfaceDefinition.interfaceCount ? 8704 : 14775, 250, 1749);
        player3 = player;
        player3.packetSender.sendInterfaceModel(14776 >= InterfaceDefinition.interfaceCount ? 8709 : 14776, 250, 1747);
        player3 = player;
        player3.packetSender.sendInterfaceText("Snakeskin", 14779);
        if (player.getInventoryManager().getItemAmount(995) >= 20) {
            player3 = player;
            player3.packetSender.sendInterfaceText(String.valueOf(interactionTargetId * 20) + " coins", 14787);
        } else {
            player3 = player;
            player3.packetSender.sendInterfaceText(String.valueOf(interactionTargetId * 20) + " coins", 14787);
        }
        player3 = player;
        player3.packetSender.sendInterfaceText("", 14780);
        player3 = player;
        player3.packetSender.sendInterfaceText("", 14788);
        value = new int[]{14781, 14789, 14783, 14791, 14782, 14790, 14784, 14792};
        String[] stringValues = new String[]{"Green d'hide", "Red d'hide", "Blue d'hide", "Black d'hide"};
        String[] stringValues2 = new String[]{String.valueOf(interactionTargetId * 20) + " coins", String.valueOf(interactionTargetId * 20) + " coins", String.valueOf(interactionTargetId * 20) + " coins", String.valueOf(interactionTargetId * 20) + " coins"};
        boolean enabled = false;
        int index = 0;
        while (index < 8) {
            if (!enabled) {
                Player player4 = player;
                player4.packetSender.sendInterfaceText(stringValues[index / 2], ((int[])value)[index]);
                enabled = true;
            } else {
                Player player5;
                if (player.getInventoryManager().getItemAmount(995) > 0) {
                    player5 = player;
                    player5.packetSender.sendInterfaceText(stringValues2[index / 2], ((int[])value)[index]);
                } else {
                    player5 = player;
                    player5.packetSender.sendInterfaceText(stringValues2[index / 2], ((int[])value)[index]);
                }
                enabled = false;
            }
            ++index;
        }
    }

    public static void tanHide(Player player3, int value6, int value22, int value32, int value42) {
        int value5;
        int interactionTargetId = value5 = ((Player)player3).getInteractionTargetId() == 1041 ? 2 : 1;
        if (value6 > ((Player)player3).getInventoryManager().getContainer().getItemAmount(value32)) {
            value6 = ((Player)player3).getInventoryManager().getContainer().getItemAmount(value32);
        }
        ItemStack itemStack = new ItemStack(995, value22 * value6 * value5);
        Player player = player3;
        player.packetSender.closeInterfaces();
        if (!ServerSettings.craftingEnabled) {
            player = player3;
            player.packetSender.sendGameMessage("This skill is currently disabled.");
            return;
        }
        player = player3;
        if (player.interfaceAction != "tanning") {
            return;
        }
        if (!((Player)player3).getInventoryManager().getContainer().containsItem(value32)) {
            ((Player)player3).getDialogueManager().showOneLineStatement("You don't have enough rough hides in your inventory.");
            if (((Player)player3).botEnabled) {
                ((Player)player3).currentBotTask.startWalkToBank((Player)player3);
            }
            return;
        }
        if (((Player)player3).getInventoryManager().getContainer().getItemAmount(995) < itemStack.getAmount()) {
            ((Player)player3).getDialogueManager().showOneLineStatement("You do not have enough coins.");
            if (((Player)player3).botEnabled) {
                ((Player)player3).currentBotTask.startWalkToBank((Player)player3);
            }
            return;
        }
        ((Player)player3).getInventoryManager().removeItem(itemStack);
        ((Player)player3).getInventoryManager().removeItem(new ItemStack(value32, value6));
        ((Player)player3).getInventoryManager().addItem(new ItemStack(value42, value6));
        Player player2 = player3;
        player = player2;
        player2.interfaceAction = "";
    }

    public static boolean handleFarmingPatchObjectAction(Player player, int objectId, int value2) {
        if (player.getAllotmentPatchManager().harvestPatch(objectId, value2)) {
            return true;
        }
        if (player.getFlowerPatchManager().harvestPatch(objectId, value2)) {
            return true;
        }
        if (player.getHerbPatchManager().harvestPatch(objectId, value2)) {
            return true;
        }
        if (player.getHopsPatchManager().harvestPatch(objectId, value2)) {
            return true;
        }
        if (player.getBushPatchManager().harvestPatch(objectId, value2)) {
            return true;
        }
        if (player.getTreePatchManager().checkHealth(objectId, value2)) {
            return true;
        }
        if (player.getTreePatchManager().startCuttingTree(objectId, value2)) {
            return true;
        }
        if (player.getFruitTreePatchManager().harvestPatch(objectId, value2)) {
            return true;
        }
        if (player.getSpecialTreePatchManager().handleSpecialTreeObject(objectId, value2)) {
            return true;
        }
        return player.getSpecialCropPatchManager().harvestPatch(objectId, value2);
    }

    public static boolean handleCombinationRunecrafting(Player player, int value4, int value22) {
        CombinationRuneDefinition combinationRuneDefinition = CombinationRuneDefinition.forTalismanAndAltarObjectId(value4, value22);
        if (combinationRuneDefinition == null) {
            return false;
        }
        if (!ServerSettings.runecraftingEnabled) {
            Player player2 = player;
            player2.packetSender.sendGameMessage("This skill is currently disabled.");
            return true;
        }
        if (!player.isMember()) {
            player.packetSender.sendGameMessage("You need a members account to access members content.");
            return true;
        }
        if (ServerSettings.freeToPlayWorld) {
            player.packetSender.sendGameMessage("You need to be in members world to access members content.");
            return true;
        }
        if (player.getQuestState(14) != 1) {
            Object value3 = QuestDefinition.forId(14);
            String name = ((QuestDefinition)value3).getName();
            value3 = player;
            ((Player)value3).packetSender.sendGameMessage("You need to complete " + name + " to do this.");
            return true;
        }
        if (!player.getInventoryManager().containsItem(combinationRuneDefinition.getTalismanItemId())) {
            Player player3 = player;
            PacketSender packetSender = player3.packetSender;
            StringBuilder stringBuilder = new StringBuilder("You need a ");
            ItemService.getInstance();
            packetSender.sendGameMessage(stringBuilder.append(ItemService.getItemName(combinationRuneDefinition.getTalismanItemId()).toLowerCase()).append(" to do this.").toString());
            return true;
        }
        if (!player.getInventoryManager().containsItem(combinationRuneDefinition.getRuneItemId()) || !player.getInventoryManager().containsItem(RunecraftingHandler.PURE_ESSENCE_ITEM_ID)) {
            Player player4 = player;
            player4.packetSender.sendGameMessage("You need pure essences to do this.");
            return true;
        }
        if (player.getSkillManager().getBaseLevel(20) < combinationRuneDefinition.getRequiredLevel()) {
            Player player5 = player;
            player5.packetSender.sendGameMessage("You need a runecrafting level of " + combinationRuneDefinition.getRequiredLevel() + " to do this.");
            return true;
        }
        player.getInventoryManager().removeItem(new ItemStack(combinationRuneDefinition.getTalismanItemId(), 1));
        Player player6 = player;
        player6.packetSender.sendGameMessage("You attempt to bind the Runes.");
        player6 = player;
        player6.packetSender.sendSoundEffect(481, 1, 0);
        player.getUpdateState().setAnimation(791);
        player.getUpdateState().setGraphicHeight100(186);
        int inventoryManager = player.getInventoryManager().getItemAmount(RunecraftingHandler.PURE_ESSENCE_ITEM_ID);
        int inventoryManager2 = player.getInventoryManager().getItemAmount(combinationRuneDefinition.getRuneItemId());
        inventoryManager = inventoryManager > inventoryManager2 ? inventoryManager2 : inventoryManager;
        inventoryManager2 = 0;
        if (player.getEquipmentManager().getItemIdAtSlot(2) == 5521) {
            inventoryManager2 = inventoryManager;
        } else {
            int index = 0;
            while (index < inventoryManager) {
                if (GameUtil.randomInclusive(1) == 0) {
                    ++inventoryManager2;
                }
                ++index;
            }
        }
        player.getInventoryManager().removeItem(new ItemStack(combinationRuneDefinition.getRuneItemId(), inventoryManager));
        player.getInventoryManager().removeItem(new ItemStack(RunecraftingHandler.PURE_ESSENCE_ITEM_ID, inventoryManager));
        player.getInventoryManager().addItem(new ItemStack(combinationRuneDefinition.getProductRuneItemId(), inventoryManager2));
        player.getSkillManager().addExperience(20, combinationRuneDefinition.getExperience() * (double)inventoryManager2);
        if (player.getEquipmentManager().getItemIdAtSlot(2) == 5521) {
            player.setBindingNecklaceCharge(player.getBindingNecklaceCharge() - 1);
        }
        if (player.getBindingNecklaceCharge() <= 0) {
            player.setBindingNecklaceCharge(15);
            player.getEquipmentManager().replaceSlotItem(5521, 2);
            Player player7 = player;
            player7.packetSender.sendGameMessage("Your binding necklace crumble into dust.");
        }
        return true;
    }

    public static boolean fillEssencePouch(Player player, int value4) {
        EssencePouchDefinition essencePouchDefinition = EssencePouchDefinition.forItemOrIndex(value4);
        if (essencePouchDefinition == null) {
            return false;
        }
        if (value4 != essencePouchDefinition.getItemId() && value4 != essencePouchDefinition.getDegradedItemId()) {
            return false;
        }
        if (!ServerSettings.runecraftingEnabled) {
            Player player2 = player;
            player2.packetSender.sendGameMessage("This skill is currently disabled.");
            return true;
        }
        if (player.getQuestState(14) != 1) {
            Object value2 = QuestDefinition.forId(14);
            value2 = ((QuestDefinition)value2).getName();
            Player player3 = player;
            player3.packetSender.sendGameMessage("You need to complete " + (String)value2 + " to do this.");
            return true;
        }
        int skillManager = player.getSkillManager().getBaseLevel(20);
        if (skillManager < essencePouchDefinition.getRequiredLevel()) {
            Player player4 = player;
            player4.packetSender.sendGameMessage("You need " + essencePouchDefinition.getRequiredLevel() + " Runecrafting to use this pouch.");
            return true;
        }
        ItemStack itemStack = player.getInventoryManager().getContainer().findFlatItem(value4);
        int inventoryManager = player.getInventoryManager().getContainer().indexOfItem(value4);
        while (true) {
            int value3;
            boolean enabled = false;
            if (value4 == essencePouchDefinition.getDegradedItemId()) {
                enabled = true;
            }
            if ((value3 = player.getInventoryManager().getContainer().getItemAmount(7936)) <= 0) {
                Player player5 = player;
                player5.packetSender.sendGameMessage("You don't have any more Pure essence.");
                break;
            }
            value3 = essencePouchDefinition.getCapacity();
            if (enabled) {
                EssencePouchDefinition essencePouchDefinition2 = essencePouchDefinition;
                value3 = essencePouchDefinition2.getCapacity() - essencePouchDefinition2.getDegradedCapacityPenalty();
            }
            if ((value3 -= player.getEssencePouchAmount(essencePouchDefinition.getPouchIndex())) <= 0) {
                Player player6 = player;
                PacketSender packetSender = player6.packetSender;
                StringBuilder stringBuilder = new StringBuilder("Your ");
                ItemService.getInstance();
                packetSender.sendGameMessage(stringBuilder.append(ItemService.getItemName(value4)).append(" is full.").toString());
                break;
            }
            player.setEssencePouchAmount(essencePouchDefinition.getPouchIndex(), player.getEssencePouchAmount(essencePouchDefinition.getPouchIndex()) + 1);
            if (essencePouchDefinition.getDegradedItemId() != -1) {
                itemStack.setMetadata(itemStack.getMetadata() + 1);
            }
            if (!enabled && itemStack.getMetadata() >= essencePouchDefinition.getDegradeAfterUses() && essencePouchDefinition.getDegradedItemId() != -1) {
                player.getInventoryManager().getContainer().setItem(inventoryManager, new ItemStack(essencePouchDefinition.getDegradedItemId()));
                value4 = essencePouchDefinition.getDegradedItemId();
                Player player7 = player;
                PacketSender packetSender = player7.packetSender;
                StringBuilder stringBuilder = new StringBuilder("Your ");
                ItemService.getInstance();
                packetSender.sendGameMessage(stringBuilder.append(ItemService.getItemName(value4)).append(" degraded.").toString());
            }
            player.getInventoryManager().removeItem(new ItemStack(7936, 1));
        }
        return true;
    }

    public static int selectNextAvailableEssencePouch(Entity entity, int value3) {
        if (!entity.isPlayer()) {
            return -1;
        }
        entity = (Player)entity;
        int value2 = value3;
        boolean enabled = true;
        if (((Player)entity).ownsItem(value3)) {
            enabled = false;
        }
        while (!enabled) {
            EssencePouchDefinition essencePouchDefinition;
            value3 = value2;
            EssencePouchDefinition essencePouchDefinition2 = EssencePouchDefinition.forItemOrIndex(value3);
            int itemId = value3 = essencePouchDefinition2 != null && (value3 == essencePouchDefinition2.getItemId() || value3 == essencePouchDefinition2.getDegradedItemId()) && essencePouchDefinition2.getPouchIndex() != 3 && (essencePouchDefinition = EssencePouchDefinition.forItemOrIndex(essencePouchDefinition2.getPouchIndex() + 1)) != null && essencePouchDefinition.getPouchIndex() == essencePouchDefinition2.getPouchIndex() + 1 ? essencePouchDefinition.getItemId() : -1;
            if (!((Player)entity).ownsItem(value3) || value3 == -1) {
                enabled = true;
                continue;
            }
            enabled = false;
            value2 = value3;
        }
        return value3;
    }

    public static boolean handleTiaraCrafting(Player player, int value3, int value22) {
        RunecraftingAltarDefinition altarDefinition = null;
        for (RunecraftingAltarDefinition candidate : RunecraftingAltarDefinition.values()) {
            if (value3 == candidate.getTalismanItemId() && value22 == candidate.getAltarObjectId()) {
                altarDefinition = candidate;
                break;
            }
        }
        if (altarDefinition == null) {
            return false;
        }
        if (!ServerSettings.runecraftingEnabled) {
            player.packetSender.sendGameMessage("This skill is currently disabled.");
            return true;
        }
        if (player.getQuestState(14) != 1) {
            QuestDefinition questDefinition = QuestDefinition.forId(14);
            String name = questDefinition.getName();
            player.packetSender.sendGameMessage("You need to complete " + name + " to do this.");
            return true;
        }
        if (player.getInventoryManager().containsItem(5525)) {
            player.getInventoryManager().removeItem(new ItemStack(5525, 1));
            player.getInventoryManager().removeItem(new ItemStack(altarDefinition.getTalismanItemId(), 1));
            player.getInventoryManager().addItem(new ItemStack(altarDefinition.getTiaraItemId(), 1));
            player.getSkillManager().addExperience(20, altarDefinition.getTiaraExperience());
            player.packetSender.sendGameMessage("You bind the power of the talisman into the tiara.");
        }
        return true;
    }

    public static void refreshRunecraftingTiaraConfig(Player player, int value3) {
        Object value2 = new int[][]{{5527, 1}, {5529, 2}, {5531, 4}, {5535, 8}, {5537, 16}, {5533, 32}, {5539, 64}, {5543, 512}, {5541, 256}, {5545, 128}, {5547, 1024}, {5551, 2048}, {5549, 4096}};
        int[][] nArrayArray = (int[][])value2;
        int index = 0;
        while (index < 13) {
            int[] integerValues = nArrayArray[index];
            value2 = integerValues;
            if (integerValues[0] == value3) {
                player.packetSender.sendConfig(491, integerValues[1]);
                return;
            }
            ++index;
        }
        player.packetSender.sendConfig(491, 0);
    }

    public static boolean handleAnagramClueNpc(Player player, int npcId) {
        AnagramClue anagramClue = AnagramClue.forNpcId(npcId);
        if (anagramClue == null) {
            return false;
        }
        if (!player.getInventoryManager().getContainer().containsItem(anagramClue.getClueItemId())) {
            return false;
        }
        player.getDialogueManager().setDialogueNpcId(npcId);
        if (anagramClue.getFollowupType() == "Challenge") {
            if (CacheArchive.hasChallengeQuestionAnswerItem(player, anagramClue.getClueItemId())) {
                player.activeClueLevel = anagramClue.getLevel();
                player.activeClueItemId = anagramClue.getClueItemId();
                DialogueManager.prepareAnagramClueDialogue(player, 3);
                if (CacheArchive.getChallengeQuestionLines(anagramClue.getClueItemId()).length == 1) {
                    player.getDialogueManager().showNpcOneLineDialogue(CacheArchive.getChallengeQuestionLines(anagramClue.getClueItemId())[0], 588);
                } else if (CacheArchive.getChallengeQuestionLines(anagramClue.getClueItemId()).length == 2) {
                    player.getDialogueManager().showNpcTwoLineDialogue(CacheArchive.getChallengeQuestionLines(anagramClue.getClueItemId())[0], CacheArchive.getChallengeQuestionLines(anagramClue.getClueItemId())[1], 588);
                } else if (CacheArchive.getChallengeQuestionLines(anagramClue.getClueItemId()).length == 3) {
                    player.getDialogueManager().showNpcThreeLineDialogue(CacheArchive.getChallengeQuestionLines(anagramClue.getClueItemId())[0], CacheArchive.getChallengeQuestionLines(anagramClue.getClueItemId())[1], CacheArchive.getChallengeQuestionLines(anagramClue.getClueItemId())[2], 588);
                }
            } else {
                player.clueRequiredItems = new ItemStack[1];
                player.clueRequiredItems[0] = new ItemStack(anagramClue.getClueItemId(), 1);
                player.getDialogueManager().showNpcOneLineDialogue("Here's a challenge for you.", 588);
                CacheArchive.giveChallengeQuestionAnswerItem(player, anagramClue.getClueItemId());
            }
        } else if (anagramClue.getFollowupType() == "Puzzle") {
            if (PuzzleBoxHandler.isCluePuzzleSolved(player) && player.ownsCluePuzzleBox()) {
                DialogueManager.prepareAnagramClueDialogue(player, 2);
                player.clueRequiredItems = new ItemStack[4];
                player.clueRequiredItems[0] = new ItemStack(anagramClue.getClueItemId(), 1);
                player.clueRequiredItems[1] = new ItemStack(2800, 1);
                player.clueRequiredItems[2] = new ItemStack(3571, 1);
                player.clueRequiredItems[3] = new ItemStack(3565, 1);
                player.activeClueLevel = anagramClue.getLevel();
            } else if (player.ownsCluePuzzleBox()) {
                player.getDialogueManager().showNpcOneLineDialogue("The puzzle doesn't seem to be complete yet.", 588);
            } else {
                player.getDialogueManager().showNpcOneLineDialogue("Hello, Solve this puzzle for me please.", 588);
                PuzzleBoxHandler.giveRandomPuzzleBox(player);
            }
        } else {
            DialogueManager.prepareAnagramClueDialogue(player, 2);
            player.getDialogueManager().showNpcOneLineDialogue("Thank you very much.", 588);
            player.clueRequiredItems = new ItemStack[1];
            player.clueRequiredItems[0] = new ItemStack(anagramClue.getClueItemId(), 1);
            player.activeClueLevel = anagramClue.getLevel();
        }
        return true;
    }

    public static int randomAnagramClueItemForLevel(int itemId) {
        int value = new Random().nextInt(AnagramClue.values().length);
        while (AnagramClue.values()[value].getLevel() != itemId) {
            value = new Random().nextInt(AnagramClue.values().length);
        }
        return AnagramClue.values()[value].getClueItemId();
    }

    public static void openSextantInterface(Player player) {
        Player player2 = player;
        player.sextantSunAngleDegrees = 0.0;
        player2.sextantSunVerticalOffset = 0;
        player2.sextantSunHorizontalOffset = 0;
        player2.sextantHorizonRotation = 0;
        player2.sextantHorizonVerticalOffset = 0;
        player2 = player;
        double value = -32.0 + (double)player2.sextantSunHorizontalOffset / 5.7 + (double)GameUtil.randomInclusive((int)(32.0 + (double)player2.sextantSunHorizontalOffset / 5.7 + 28.0 + (double)player2.sextantSunHorizontalOffset / 5.7));
        int value2 = (int)(28.0 + (double)player2.sextantSunHorizontalOffset / 5.7 - value);
        int value3 = Math.abs((int)(value + 32.0 + (double)player2.sextantSunHorizontalOffset / 5.7));
        value2 = GameUtil.randomInclusive(1) == 0 ? GameUtil.randomInclusive(value3) << 1 : -GameUtil.randomInclusive(value2) << 1;
        double value4 = -10.175438596491228 + (double)GameUtil.randomInclusive(17);
        value3 = (int)(7.017543859649122 - value4);
        int value5 = Math.abs((int)(value4 + 10.175438596491228));
        value3 = GameUtil.randomInclusive(1) == 0 ? GameUtil.randomInclusive(value5) : -GameUtil.randomInclusive(value3);
        player2.sextantHorizonVerticalOffset = (int)((double)player2.sextantHorizonVerticalOffset + (double)value3 * 5.7);
        player2.sextantHorizonRotation = (int)((double)player2.sextantHorizonRotation + value4 * 5.7);
        player2.sextantSunVerticalOffset += value2;
        player2.sextantSunAngleDegrees = value;
        player2 = player;
        player2.packetSender.showInterface(6946);
        GameplayHelper.refreshSextantInterface(player);
    }

    public static void refreshSextantInterface(Player player) {
        double value = player.sextantSunAngleDegrees;
        Player player2 = player;
        double value2 = value < -32.0 + (double)player2.sextantSunHorizontalOffset / 5.7 ? -32.0 + (double)player2.sextantSunHorizontalOffset / 5.7 : (value > 28.0 + (double)player2.sextantSunHorizontalOffset / 5.7 ? 28.0 + (double)player2.sextantSunHorizontalOffset / 5.7 : value);
        int value3 = value2 > 0.0 ? (int)(value2 * 5.7) : 2047 - (int)(-value2 * 5.7);
        int value4 = (int)Math.round(Math.abs(140.0 * Math.sin(value2 * Math.PI / 180.0)));
        int value5 = (int)Math.floor(Math.abs(140.0 * (1.0 - Math.cos(value2 * Math.PI / 180.0))));
        Player player3 = player2;
        player3.packetSender.sendInterfaceModelRotation(6957, 513, value3, 580);
        player3 = player2;
        player3.packetSender.sendInterfaceOffset(value2 > 0.0 ? -(value4 - player2.sextantSunHorizontalOffset / 2) : value4 + player2.sextantSunHorizontalOffset / 2, -value5, 6957);
        player3 = player2 = player;
        player2.packetSender.sendInterfaceOffset(0, player2.sextantSunVerticalOffset, 6949);
        player2 = player;
        value3 = player2.sextantHorizonRotation < -58 ? -58 : (player2.sextantHorizonRotation > 40 ? 40 : player2.sextantHorizonRotation);
        value4 = value3 > 0 ? value3 : 2047 - -value3;
        player3 = player2;
        player3.packetSender.sendInterfaceModelRotation(6958, 513, value4, 555);
        player3 = player2;
        player3.packetSender.sendInterfaceModelRotation(6956, 513, value4, 555);
        player3 = player2 = player;
        player2.packetSender.sendInterfaceOffset(0, player2.sextantHorizonVerticalOffset, 6948);
    }

    public static void adjustSextantSun(Player player, boolean enabled6) {
        boolean enabled2;
        player.sextantSunAngleDegrees = enabled6 ? (player.sextantSunAngleDegrees += 1.0) : (player.sextantSunAngleDegrees -= 1.0);
        boolean enabled3 = player.sextantSunAngleDegrees > 28.0 + (double)player.sextantSunHorizontalOffset / 5.7;
        boolean enabled4 = enabled2 = player.sextantSunAngleDegrees < -32.0 + (double)player.sextantSunHorizontalOffset / 5.7;
        if (!enabled3 && !enabled2) {
            boolean enabled5 = enabled6;
            Player player2 = player;
            player2.sextantSunVerticalOffset = enabled5 ? (player2.sextantSunVerticalOffset += 2) : (player2.sextantSunVerticalOffset -= 2);
            GameplayHelper.refreshSextantInterface(player2);
        }
        if (enabled3) {
            player.sextantSunAngleDegrees = 28.0 + (double)player.sextantSunHorizontalOffset / 5.7;
        }
        if (enabled2) {
            player.sextantSunAngleDegrees = -32.0 + (double)player.sextantSunHorizontalOffset / 5.7;
        }
        GameplayHelper.refreshSextantInterface(player);
    }

    public static void adjustSextantHorizon(Player player, boolean enabled8) {
        boolean enabled2;
        if (enabled8) {
            player.sextantHorizonRotation += 7;
            if (player.sextantHorizonRotation <= 40 && player.sextantHorizonRotation >= -58) {
                player.sextantSunAngleDegrees += 1.2280701754385965;
                player.sextantSunHorizontalOffset += 7;
            }
        } else {
            player.sextantHorizonRotation -= 7;
            if (player.sextantHorizonRotation <= 40 && player.sextantHorizonRotation >= -58) {
                player.sextantSunAngleDegrees -= 1.2280701754385965;
                player.sextantSunHorizontalOffset -= 7;
            }
        }
        boolean enabled3 = player.sextantHorizonRotation > 40;
        boolean enabled4 = enabled2 = player.sextantHorizonRotation < -58;
        if (!enabled3 && !enabled2) {
            boolean enabled5;
            boolean enabled6 = enabled8;
            Player player2 = player;
            player2.sextantHorizonVerticalOffset = enabled6 ? (player2.sextantHorizonVerticalOffset += 7) : (player2.sextantHorizonVerticalOffset -= 7);
            GameplayHelper.refreshSextantInterface(player2);
            enabled6 = player2.sextantHorizonVerticalOffset > 70;
            boolean enabled7 = enabled5 = player2.sextantHorizonVerticalOffset < -70;
            if (enabled6) {
                player2.sextantHorizonVerticalOffset = 70;
            }
            if (enabled5) {
                player2.sextantHorizonVerticalOffset = -70;
            }
        }
        if (enabled3) {
            player.sextantHorizonRotation = 40;
        }
        if (enabled2) {
            player.sextantHorizonRotation = -58;
        }
        GameplayHelper.refreshSextantInterface(player);
    }

    public static boolean handleSextantButtonClick(Player buttonId, int buttonId2) {
        switch (buttonId2) {
            case 6955: {
                GameplayHelper.adjustSextantSun((Player)buttonId, true);
                return true;
            }
            case 6954: {
                GameplayHelper.adjustSextantSun((Player)buttonId, false);
                return true;
            }
            case 6953: {
                GameplayHelper.adjustSextantHorizon((Player)buttonId, true);
                return true;
            }
            case 6952: {
                GameplayHelper.adjustSextantHorizon((Player)buttonId, false);
                return true;
            }
            case 6959: {
                boolean enabled;
                Player player;
                Player player2 = buttonId;
                if (Math.abs(player2.sextantHorizonVerticalOffset) > 7) {
                    player = player2;
                    player.packetSender.sendGameMessage("You need to get the horizon in the middle of the eye piece.");
                    enabled = false;
                } else if (player2.sextantSunVerticalOffset != 0) {
                    player = player2;
                    player.packetSender.sendGameMessage("You need to get the sun in the middle of the eye piece.");
                    enabled = false;
                } else if (!(player2.getInventoryManager().getContainer().containsItem(2574) && player2.getInventoryManager().getContainer().containsItem(2576) && player2.getInventoryManager().getContainer().containsItem(2575))) {
                    player2.getDialogueManager().showOneLineStatement("You need a watch and navigator's chart to work out your Position.");
                    enabled = false;
                } else {
                    enabled = true;
                }
                if (enabled) {
                    player2 = buttonId;
                    String[] coordinateLines = CoordinateClueHandler.formatPositionAsCoordinate(player2.getPosition().getX(), player2.getPosition().getY());
                    String coordinateLine = coordinateLines[0];
                    String text = coordinateLines[1];
                    player2.getDialogueManager().showTwoLineStatement(coordinateLine, text);
                    player = player2;
                    player.packetSender.sendGameMessage("the sextant displays:");
                    player = player2;
                    player.packetSender.sendGameMessage(coordinateLine);
                    player = player2;
                    player.packetSender.sendGameMessage(text);
                }
                return true;
            }
        }
        return false;
    }

    public static void loadNpcSpawns() {
        int index = 0;
        new StringBuilder("0");
        try {
            int value;
            byte[] byteValues = FileUtil.readBytes("./data/npcs/Npc spawn.dat");
            ByteArrayReader byteArrayReader = new ByteArrayReader(byteValues);
            byteArrayReader.readUnsignedByte();
            int value2 = value = byteArrayReader.readUnsignedShort();
            int index2 = 0;
            while (index2 < value) {
                loadNpcSpawnsControlExit1: {
                    int value3;
                    int value4;
                    int value5;
                    int value6;
                    int value7;
                    loadNpcSpawnsControlExit2: {
                        loadNpcSpawnsControlExit3: {
                            loadNpcSpawnsControlExit4: {
                                loadNpcSpawnsControlExit5: {
                                    loadNpcSpawnsControlExit6: {
                                        loadNpcSpawnsControlExit7: {
                                            loadNpcSpawnsControlExit8: {
                                                loadNpcSpawnsControlExit9: {
                                                    loadNpcSpawnsControlExit10: {
                                                        loadNpcSpawnsControlExit11: {
                                                            loadNpcSpawnsControlExit12: {
                                                                loadNpcSpawnsControlExit13: {
                                                                    loadNpcSpawnsControlExit14: {
                                                                        loadNpcSpawnsControlExit15: {
                                                                            loadNpcSpawnsControlExit16: {
                                                                                loadNpcSpawnsControlExit17: {
                                                                                    loadNpcSpawnsControlExit18: {
                                                                                        loadNpcSpawnsControlExit19: {
                                                                                            loadNpcSpawnsControlExit20: {
                                                                                                loadNpcSpawnsControlExit21: {
                                                                                                    loadNpcSpawnsControlExit22: {
                                                                                                        value7 = byteArrayReader.readUnsignedShort() - value2 - index2;
                                                                                                        int value8 = byteArrayReader.readUnsignedByte();
                                                                                                        value6 = byteArrayReader.readUnsignedByte();
                                                                                                        value5 = byteArrayReader.readUnsignedShort();
                                                                                                        value4 = byteArrayReader.readUnsignedShort();
                                                                                                        value3 = byteArrayReader.readUnsignedByte();
                                                                                                        if (value7 > 3851 || ServerSettings.freeToPlayWorld && value8 == 1 || value7 == 3852 || value7 == 3853 && ServerSettings.freeToPlayWorld) break loadNpcSpawnsControlExit1;
                                                                                                        if (ServerSettings.cacheVersion < 319 && value7 == 50) {
                                                                                                            value5 = 2718;
                                                                                                            value4 = 9823;
                                                                                                            value3 = 0;
                                                                                                        }
                                                                                                        if (value7 == 1800 || value7 == 3809 || value7 == 3810 || value7 == 3811 || value7 == 3812) {
                                                                                                            value7 = 170;
                                                                                                        }
                                                                                                        if (NpcDefinition.isDefined(value7)) break loadNpcSpawnsControlExit2;
                                                                                                        if (value7 != 1757) break loadNpcSpawnsControlExit22;
                                                                                                        value7 = 7;
                                                                                                        break loadNpcSpawnsControlExit2;
                                                                                                    }
                                                                                                    if (value7 < 1762 || value7 > 1765) break loadNpcSpawnsControlExit21;
                                                                                                    value7 = 43;
                                                                                                    break loadNpcSpawnsControlExit2;
                                                                                                }
                                                                                                if (value7 < 1769 || value7 > 1776) break loadNpcSpawnsControlExit20;
                                                                                                value7 = 100;
                                                                                                break loadNpcSpawnsControlExit2;
                                                                                            }
                                                                                            if (value7 == 1862 || value7 == 2239 || value7 == 2240 || value7 == 2242 || value7 == 2243 || value7 == 2234 || value7 == 2236 || value7 == 2237 || value7 == 2238 || value7 == 2244 || value7 == 2253 || value7 == 2290 || value7 == 2304 || value7 == 2311 || value7 >= 2316 && value7 <= 2317 || value7 == 2323 || value7 == 2333 || value7 == 2335 || value7 == 2340 || value7 == 2341 || value7 == 2342) break loadNpcSpawnsControlExit1;
                                                                                            if (value7 != 2661) break loadNpcSpawnsControlExit19;
                                                                                            value7 = 647;
                                                                                            break loadNpcSpawnsControlExit2;
                                                                                        }
                                                                                        if (value7 == 2693 || value7 == 2709 || value7 == 2710 || value7 == 2711 || value7 == 2712) break loadNpcSpawnsControlExit1;
                                                                                        if (value7 != 2809) break loadNpcSpawnsControlExit18;
                                                                                        value7 = 80;
                                                                                        break loadNpcSpawnsControlExit2;
                                                                                    }
                                                                                    if (value7 != 2810) break loadNpcSpawnsControlExit17;
                                                                                    value7 = 80;
                                                                                    break loadNpcSpawnsControlExit2;
                                                                                }
                                                                                if (value7 != 2811) break loadNpcSpawnsControlExit16;
                                                                                value7 = 80;
                                                                                break loadNpcSpawnsControlExit2;
                                                                            }
                                                                            if (value7 != 2812) break loadNpcSpawnsControlExit15;
                                                                            value7 = 80;
                                                                            break loadNpcSpawnsControlExit2;
                                                                        }
                                                                        if (value7 != 2824) break loadNpcSpawnsControlExit14;
                                                                        value7 = 804;
                                                                        break loadNpcSpawnsControlExit2;
                                                                    }
                                                                    if (value7 == 3021) break loadNpcSpawnsControlExit1;
                                                                    if (value7 != 3219) break loadNpcSpawnsControlExit13;
                                                                    value7 = 118;
                                                                    break loadNpcSpawnsControlExit2;
                                                                }
                                                                if (value7 != 3220) break loadNpcSpawnsControlExit12;
                                                                value7 = 118;
                                                                break loadNpcSpawnsControlExit2;
                                                            }
                                                            if (value7 != 3221) break loadNpcSpawnsControlExit11;
                                                            value7 = 118;
                                                            break loadNpcSpawnsControlExit2;
                                                        }
                                                        if (value7 != 3223) break loadNpcSpawnsControlExit10;
                                                        value7 = 1;
                                                        break loadNpcSpawnsControlExit2;
                                                    }
                                                    if (value7 != 3226) break loadNpcSpawnsControlExit9;
                                                    value7 = 4;
                                                    break loadNpcSpawnsControlExit2;
                                                }
                                                if (value7 != 3228) break loadNpcSpawnsControlExit8;
                                                value7 = 9;
                                                break loadNpcSpawnsControlExit2;
                                            }
                                            if (value7 != 3230) break loadNpcSpawnsControlExit7;
                                            value7 = 9;
                                            break loadNpcSpawnsControlExit2;
                                        }
                                        if (value7 != 3246 && value7 != 3249) break loadNpcSpawnsControlExit6;
                                        value7 = 17;
                                        break loadNpcSpawnsControlExit2;
                                    }
                                    if (value7 != 3247) break loadNpcSpawnsControlExit5;
                                    value7 = 12;
                                    break loadNpcSpawnsControlExit2;
                                }
                                if (value7 == 3295 || value7 == 3296) break loadNpcSpawnsControlExit1;
                                if (value7 != 3307) break loadNpcSpawnsControlExit4;
                                value7 = 33;
                                if (index != 0) break loadNpcSpawnsControlExit1;
                                ++index;
                                break loadNpcSpawnsControlExit2;
                            }
                            if (value7 != 3348) break loadNpcSpawnsControlExit3;
                            value7 = 19;
                            break loadNpcSpawnsControlExit2;
                        }
                        if (value7 == 3806 || value7 == 3809 || ServerSettings.skipUndefinedNpcSpawns) break loadNpcSpawnsControlExit1;
                    }
                    if (CacheCoordinateTranslator.dungeonCoordinateShiftActive && CacheCoordinateTranslator.isDungeonCoordinateShiftSourceRegion(value5, value4)) {
                        value5 += 768;
                        value4 += 5120;
                    }
                    GameplayHelper.spawnNpc(value7, value5, value4, value3, value6);
                    NpcDefinition npcDefinition = NpcDefinition.forId(value7);
                    npcDefinition.getDropTableNpcIdOverride();
                }
                ++index2;
            }
            // Lumbridge Castle bank: standard banker behind the counter.
            GameplayHelper.spawnNpc(494, 3208, 3222, 2, 3);
            return;
        }
        catch (Exception exception) {
            Exception exception2 = exception;
            exception.printStackTrace();
            return;
        }
    }

    public static void spawnNpc(int npcId, int value2, int value32, int value42, int value52) {
        if (npcId == 767 || npcId == 1826) {
            WalkingCollisionMap.addObjectCollision(2620, value2, value32, value42, 0, 10, true);
        }
        Npc npc = new Npc(npcId);
        npc.moveTo(new Position(value2, value32, value42));
        npc.setSpawnPosition(new Position(value2, value32, value42));
        npc.setRespawnEnabled(true);
        npc.setSpawnMinPosition(new Position(value2 - npc.getDefinition().getSpawnRadius(), value32 - npc.getDefinition().getSpawnRadius()));
        npc.setSpawnMaxPosition(new Position(value2 + npc.getDefinition().getSpawnRadius(), value32 + npc.getDefinition().getSpawnRadius()));
        npc.setMovementMode(value52 == 1 || value52 > 5 ? NpcMovementMode.ROAMING : NpcMovementMode.STATIONARY);
        npc.setFacingDirection(value52);
        npc.setSpawnX(value2);
        npc.setSpawnY(value32);
        npc.setRespawnEnabled(true);
        if (npcId == 1431 || npcId == 1432) {
            if (npcId == 1431) {
                Npc.scriptedStageCursor = 0;
                npc.scriptedPathStage = 0;
                npc.queueStageAdvancePath(npc.scriptedPathStage);
            }
            npc.setScriptedMovementEnabled(true);
            Npc.scriptedStageNpcs.add(npc);
        }
        if (npcId == 1454) {
            npc.scriptedPathStage = 0;
            npc.queueSequenceAdvancePath(npc.scriptedPathStage);
            npc.setScriptedMovementEnabled(true);
        }
        World.registerNpc(npc);
    }

    public static void spawnNonRespawningNpc(Npc npc, int npcId, int value2, int value32, int value42) {
        npc.moveTo(new Position(npcId, value2, value32));
        npc.setSpawnPosition(new Position(npcId, value2, value32));
        npc.setRespawnEnabled(false);
        npc.setSpawnMinPosition(new Position(npcId - npc.getDefinition().getSpawnRadius(), value2 - npc.getDefinition().getSpawnRadius()));
        npc.setSpawnMaxPosition(new Position(npcId + npc.getDefinition().getSpawnRadius(), value2 + npc.getDefinition().getSpawnRadius()));
        npc.setMovementMode(value42 == 1 || value42 > 5 ? NpcMovementMode.ROAMING : NpcMovementMode.STATIONARY);
        npc.setFacingDirection(value42);
        npc.setSpawnX(npcId);
        npc.setSpawnY(value2);
        World.registerNpc(npc);
    }

    public static void spawnOwnedNpcAtPosition(Player player, Position npcId, Npc npc, boolean npcId2, boolean value2) {
        npc.moveTo((Position)npcId);
        npc.setSpawnPosition((Position)npcId);
        npc.setMovementMode(NpcMovementMode.STATIONARY);
        npc.setSpawnX(((Position)npcId).getX());
        npc.setSpawnY(((Position)npcId).getY());
        World.registerNpc(npc);
        player.ownedNpc = npc;
        npc.setOwnerPlayerIndex(player.getIndex());
        npc.getUpdateState().setFacePosition(player.getPosition());
        if (npcId2) {
            CombatManager.startCombat(npc, player);
        }
    }

    public static void spawnOwnedNpcAdjacentToPlayer(Player player, Npc npc, boolean npcId, boolean value2) {
        int index = 0;
        int index2 = 0;
        if (player.canStepToOffset(1, 0)) {
            index = 1;
            index2 = 0;
        } else if (player.canStepToOffset(-1, 0)) {
            index = -1;
            index2 = 0;
        } else if (player.canStepToOffset(0, 1)) {
            index = 0;
            index2 = 1;
        } else if (player.canStepToOffset(0, -1)) {
            index = 0;
            index2 = -1;
        }
        index = player.getPosition().getX() + index;
        index2 = player.getPosition().getY() + index2;
        npc.moveTo(new Position(index, index2, player.getPosition().getPlane()));
        npc.setSpawnPosition(new Position(index, index2, player.getPosition().getPlane()));
        npc.setMovementMode(NpcMovementMode.STATIONARY);
        npc.setSpawnX(index);
        npc.setSpawnY(index2);
        World.registerNpc(npc);
        Npc npc2 = npc;
        Player player2 = player;
        player.ownedNpc = npc2;
        npc.setOwnerPlayerIndex(player.getIndex());
        npc.getUpdateState().setFacePosition(player.getPosition());
        if (npcId) {
            CombatManager.startCombat(npc, player);
        } else {
            npc.setAttackRange(1);
            npc.setMovementTarget(player);
        }
        if (value2) {
            player2 = player;
            player2.packetSender.sendEntityHintIcon(1, npc.getIndex());
        }
        if (npc.getNpcId() == 77) {
            npc.getUpdateState().setGraphic(GraphicEffect.createHeight0(78));
        }
    }

    public static void spawnRoamingNpcFacingPlayer(Player player, Npc npc, int npcId, int value5, int value32, int value42, boolean npcId2, boolean value6) {
        npc.moveTo(new Position(npcId, value5, value32));
        npc.setSpawnPosition(new Position(npcId, value5, value32));
        npc.setMovementMode(NpcMovementMode.ROAMING);
        npc.setSpawnX(npcId);
        npc.setSpawnY(value5);
        npc.setSpawnMinPosition(new Position(npcId - npc.getDefinition().getSpawnRadius(), value5 - npc.getDefinition().getSpawnRadius()));
        npc.setSpawnMaxPosition(new Position(npcId + npc.getDefinition().getSpawnRadius(), value5 + npc.getDefinition().getSpawnRadius()));
        World.registerNpc(npc);
        npc.getUpdateState().setAnimation(value42);
        npc.getUpdateState().setFacePosition(player.getPosition());
        if (npc.getNpcId() == 77) {
            npc.getUpdateState().setGraphic(GraphicEffect.createHeight0(78));
        }
    }

    public static void spawnNpcWithRemovalDelay(Npc npc, int npcId, int value2, int value32, int value42) {
        npc.moveTo(new Position(npcId, value2, value32));
        npc.setSpawnPosition(new Position(npcId, value2, value32));
        npc.setSpawnX(npcId);
        npc.setSpawnY(value2);
        npc.setRemovalDelayTicks(value42);
        npc.setFaceEntityUpdateDisabled(true);
        World.registerNpc(npc);
    }

    public static boolean replaceOwnedRoamingNpcAtPosition(Player player, Npc npc, int npcId, int value5, int value32, int value42, boolean npcId2, boolean value6) {
        Player player2 = player;
        if (player2.ownedNpc != null) {
            player2 = player;
            if (!player2.ownedNpc.isDead()) {
                player2 = player;
                player2.ownedNpc.setActive(false);
                player2 = player;
                World.unregisterNpc(player2.ownedNpc);
            }
        }
        npc.moveTo(new Position(npcId, value5, value32));
        npc.setSpawnPosition(new Position(npcId, value5, value32));
        npc.setMovementMode(NpcMovementMode.ROAMING);
        npc.setSpawnX(npcId);
        npc.setSpawnY(value5);
        npc.setSpawnMinPosition(new Position(npcId - npc.getDefinition().getSpawnRadius(), value5 - npc.getDefinition().getSpawnRadius()));
        npc.setSpawnMaxPosition(new Position(npcId + npc.getDefinition().getSpawnRadius(), value5 + npc.getDefinition().getSpawnRadius()));
        World.registerNpc(npc);
        Npc npc2 = npc;
        player2 = player;
        player.ownedNpc = npc2;
        npc.setOwnerPlayerIndex(player.getIndex());
        if (value42 != -1) {
            npc.getUpdateState().setAnimation(value42);
        }
        npc.getUpdateState().setFacePosition(player.getPosition());
        if (npcId2) {
            CombatManager.startCombat(npc, player);
        }
        if (value6) {
            player2 = player;
            player2.packetSender.sendEntityHintIcon(1, npc.getIndex());
        }
        if (npc.getNpcId() == 77) {
            npc.getUpdateState().setGraphic(GraphicEffect.createHeight0(78));
        }
        return true;
    }

    public static void spawnOwnedGroundPlaneNpcAtPosition(Player player, Npc npc, int npcId, int value5, int value32, int value42, boolean npcId2, boolean value6) {
        npc.moveTo(new Position(npcId, value5, 0));
        npc.setSpawnPosition(new Position(npcId, value5, 0));
        npc.setMovementMode(NpcMovementMode.ROAMING);
        npc.setSpawnX(npcId);
        npc.setSpawnY(value5);
        npc.setSpawnMinPosition(new Position(npcId - npc.getDefinition().getSpawnRadius(), value5 - npc.getDefinition().getSpawnRadius()));
        npc.setSpawnMaxPosition(new Position(npcId + npc.getDefinition().getSpawnRadius(), value5 + npc.getDefinition().getSpawnRadius()));
        World.registerNpc(npc);
        Npc npc2 = npc;
        Player player2 = player;
        player.ownedNpc = npc2;
        npc.setOwnerPlayerIndex(player.getIndex());
        npc.getUpdateState().setAnimation(-1);
        npc.getUpdateState().setFacePosition(player.getPosition());
        if (npcId2) {
            CombatManager.startCombat(npc, player);
        }
        if (npc.getNpcId() == 77) {
            npc.getUpdateState().setGraphic(GraphicEffect.createHeight0(78));
        }
    }

    public static boolean hasActiveTemporaryNpc(Player player, int npcId) {
        Player player2 = player;
        if (player2.ownedNpc != null) {
            player2 = player;
            if (!player2.ownedNpc.isDead()) {
                player2 = player;
                if (player2.ownedNpc.getNpcId() == npcId) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void spawnNpcTargetingEntityAtPosition(Entity entity, Npc npc, Position position, boolean npcId, String npcId2) {
        npc.moveTo(position);
        npc.setMovementMode(NpcMovementMode.STATIONARY);
        npc.setSpawnX(position.getX());
        npc.setSpawnY(position.getY());
        npc.setRespawnEnabled(false);
        World.registerNpc(npc);
        if (entity != null) {
            npc.setMovementTarget(entity);
            CombatManager.startCombat(npc, entity);
            npc.getUpdateState().setFacePosition(entity.getPosition());
        }
        entity.isPlayer();
    }

    public static void unregisterTemporaryNpc(Npc npc) {
        if (npc.getOwnerPlayer() != null) {
            npc.getOwnerPlayer().ownedNpc = null;
        }
        npc.setActive(false);
        EntityTargetMovement.clearMovementTarget(npc);
        World.unregisterNpc(npc);
    }
    public static void sendNpcUpdatePacket(Player player) {
        PacketWriter packetWriter;
        Object value;
        Npc npc;
        if (player.isBot) {
            return;
        }
        PacketWriter packetWriter2 = PacketBuffer.allocateWriter(8192);
        PacketWriter packetWriter3 = PacketBuffer.allocateWriter(4096);
        packetWriter2.startVariableShortPacket(player.getOutboundCipher(), 65);
        packetWriter2.setAccessMode(AccessMode.BIT_ACCESS);
        List list = player.getLocalNpcs();
        synchronized (list) {
            packetWriter2.writeBits(8, player.getLocalNpcs().size());
            Iterator iterator = player.getLocalNpcs().iterator();
            while (iterator.hasNext()) {
                npc = (Npc)iterator.next();
                if (npc.isActive() && npc.getPosition().isWithinViewport(player.getPosition()) && !npc.teleportUpdateRequired) {
                    value = npc;
                    packetWriter = packetWriter2;
                    if (((Entity)value).getWalkDirection() == -1) {
                        if (((Entity)value).getUpdateState().isUpdateRequired()) {
                            packetWriter.writeBits(1, 1);
                            packetWriter.writeBits(2, 0);
                        } else {
                            packetWriter.writeBits(1, 0);
                        }
                    } else {
                        packetWriter.writeBits(1, 1);
                        packetWriter.writeBits(2, 1);
                        packetWriter.writeBits(3, ((Entity)value).getWalkDirection());
                        packetWriter.writeBoolean(true);
                    }
                    if (!npc.getUpdateState().isUpdateRequired()) continue;
                    GameplayHelper.writeNpcUpdateBlock(packetWriter3, npc);
                    continue;
                }
                if (npc.teleportUpdateRequired) {
                    npc.teleportUpdateRequired = false;
                }
                packetWriter2.writeBoolean(true);
                packetWriter2.writeBits(2, 3);
                iterator.remove();
            }
        }
        int index = 0;
        int index2 = 0;
        while (index2 < World.getNpcs().length) {
            if (index > 15) break;
            npc = World.getNpcs()[index2];
            if (npc != null && npc.isActive()) {
                List list2 = player.getLocalNpcs();
                synchronized (list2) {
                    if (!player.getLocalNpcs().contains(npc) && npc.getPosition().isWithinViewport(player.getPosition())) {
                        ++index;
                        player.getLocalNpcs().add(npc);
                        Npc npc2 = npc;
                        value = player;
                        packetWriter = packetWriter2;
                        packetWriter.writeBits(14, npc2.getIndex());
                        value = GameUtil.getDelta(((Entity)value).getPosition(), npc2.getPosition());
                        packetWriter.writeBits(5, ((Position)value).getY());
                        packetWriter.writeBits(5, ((Position)value).getX());
                        packetWriter.writeBits(1, 0);
                        packetWriter.writeBits(13, npc2.getNpcId());
                        boolean updateRequired = npc.getUpdateState().isUpdateRequired();
                        packetWriter.writeBoolean(updateRequired);
                        if (updateRequired) {
                            GameplayHelper.writeNpcUpdateBlock(packetWriter3, npc);
                        }
                    }
                }
            }
            ++index2;
        }
        if (packetWriter3.getBuffer().position() > 0) {
            packetWriter2.writeBits(14, 16383);
            packetWriter2.setAccessMode(AccessMode.BYTE_ACCESS);
            packetWriter2.writeBuffer(packetWriter3.getBuffer());
        } else {
            packetWriter2.setAccessMode(AccessMode.BYTE_ACCESS);
        }
        packetWriter2.finishVariableShortPacket();
        player.writePacketBuffer(packetWriter2.getBuffer());
    }

    public static void writeNpcUpdateBlock(PacketWriter packetWriter, Npc npc) {
        int index = 0;
        if (npc.getUpdateState().isAnimationUpdateRequired()) {
            index = 16;
        }
        if (npc.getUpdateState().isPrimaryHitUpdateRequired()) {
            index |= 8;
        }
        if (npc.getUpdateState().isGraphicUpdateRequired()) {
            index |= 0x80;
        }
        if (npc.getUpdateState().isFaceEntityUpdateRequired()) {
            index |= 0x20;
        }
        if (npc.getUpdateState().isForcedTextUpdateRequired()) {
            index |= 1;
        }
        if (npc.getUpdateState().isSecondaryHitUpdateRequired()) {
            index |= 0x40;
        }
        if (npc.isTransformed()) {
            index |= 2;
        }
        if (npc.getUpdateState().isFacePositionUpdateRequired()) {
            index |= 4;
        }
        packetWriter.writeByte(index);
        if (npc.getUpdateState().isAnimationUpdateRequired()) {
            packetWriter.writeShort(npc.getUpdateState().getAnimationId(), ByteOrder.LITTLE);
            packetWriter.writeByte(npc.getUpdateState().getAnimationDelay());
        }
        if (npc.getUpdateState().isPrimaryHitUpdateRequired()) {
            index = npc.getUpdateState().getPrimaryHitDamage();
            packetWriter.writeByte(index, ByteTransform.ADD);
            packetWriter.writeByte(npc.getUpdateState().getPrimaryHitType(), ByteTransform.NEGATE);
            packetWriter.writeByte(GameplayHelper.calculatePercent(npc.getCurrentHitpoints(), npc.getMaxHitpoints(), 100), ByteTransform.ADD);
            packetWriter.writeByte(100);
        }
        if (npc.getUpdateState().isGraphicUpdateRequired()) {
            packetWriter.writeShort(npc.getUpdateState().getGraphicId());
            packetWriter.writeInt(npc.getUpdateState().getGraphicDelay());
        }
        if (npc.getUpdateState().isFaceEntityUpdateRequired()) {
            packetWriter.writeShort(npc.getUpdateState().getFaceEntityId());
        }
        if (npc.getUpdateState().isForcedTextUpdateRequired()) {
            packetWriter.writeString(npc.getUpdateState().getForcedText());
        }
        if (npc.getUpdateState().isSecondaryHitUpdateRequired()) {
            index = npc.getUpdateState().getSecondaryHitDamage();
            packetWriter.writeByte(index, ByteTransform.NEGATE);
            packetWriter.writeByte(npc.getUpdateState().getSecondaryHitType(), ByteTransform.SUBTRACT);
            packetWriter.writeByte(GameplayHelper.calculatePercent(npc.getCurrentHitpoints(), npc.getMaxHitpoints(), 100), ByteTransform.SUBTRACT);
            packetWriter.writeByte(100, ByteTransform.NEGATE);
        }
        if (npc.isTransformed() && npc.getTransformedNpcId() != -1) {
            packetWriter.writeShort(npc.getTransformedNpcId(), ByteTransform.ADD, ByteOrder.LITTLE);
        }
        if (npc.getUpdateState().isFacePositionUpdateRequired()) {
            Position position = npc.getUpdateState().getFacePosition();
            if (position == null) {
                packetWriter.writeShort(0, ByteOrder.LITTLE);
                packetWriter.writeShort(0, ByteOrder.LITTLE);
                return;
            }
            packetWriter.writeShort(npc.getUpdateState().getFacePositionHalfX(), ByteOrder.LITTLE);
            packetWriter.writeShort(npc.getUpdateState().getFacePositionHalfY(), ByteOrder.LITTLE);
        }
    }

    public static int calculatePercent(int value5, int value22, int value32) {
        double value4 = (double)value5 / (double)value22;
        return (int)Math.round(value4 * 100.0);
    }

    public static void takeAxeFromLog(Player player, int value3, int value22) {
        if (player.getInventoryManager().getContainer().getFreeSlots() <= 0) {
            Player player2 = player;
            player2.packetSender.sendGameMessage("Not enough space in your inventory.");
            return;
        }
        player.getInventoryManager().addItem(new ItemStack(1351));
        player.getUpdateState().setAnimation(832);
        Player player3 = player;
        player3.packetSender.sendGameMessage("You take the axe from the log.");
        new DynamicObject(5582, value3, value22, player.getPosition().getPlane(), 2, 10, 5581, 100);
    }

    public static void withdrawCoalTruckCoal(Player player) {
        if (player.getCoalTruckCoalCount() == 0) {
            player.packetSender.sendGameMessage("There is no coal left in the truck.");
            return;
        }
        int inventoryManager = player.getInventoryManager().getContainer().getFreeSlots();
        if (inventoryManager == 0) {
            player.packetSender.sendGameMessage("Not enough space in your inventory.");
            return;
        }
        inventoryManager = player.getCoalTruckCoalCount() < inventoryManager ? player.getCoalTruckCoalCount() : inventoryManager;
        player.getInventoryManager().addItem(new ItemStack(453, inventoryManager));
        player.setCoalTruckCoalCount(player.getCoalTruckCoalCount() - inventoryManager);
    }

    public static void milkCow(Player player) {
        if (!player.getInventoryManager().containsItemAmount(1925, 1)) {
            player.packetSender.sendGameMessage("You need a bucket in order to milk this cow.");
            return;
        }
        player.getUpdateState().setAnimation(2305);
        player.getInventoryManager().removeItem(new ItemStack(1925));
        player.getInventoryManager().addItem(new ItemStack(1927));
        player.packetSender.sendGameMessage("You milk the cow.");
    }

    public static void loadGroundItemSpawns() {
        try {
            int value;
            byte[] byteValues = FileUtil.readBytes("./data/world/Item spawn.dat");
            ByteArrayReader byteArrayReader = new ByteArrayReader(byteValues);
            byteArrayReader.readUnsignedByte();
            int value2 = value = byteArrayReader.readUnsignedShort();
            int index = 0;
            while (index < value) {
                int value3 = byteArrayReader.readUnsignedShort() - value2 - index;
                int value4 = byteArrayReader.readUnsignedShort();
                int value5 = byteArrayReader.readUnsignedByte();
                int value6 = byteArrayReader.readUnsignedShort();
                int value7 = byteArrayReader.readUnsignedShort();
                int value8 = byteArrayReader.readUnsignedByte();

                // Castle Wars supply placement adjustments.
                if (value3 == 590 && value8 == 0) {
                    if (value6 == 2431 && value7 == 3072) {
                        value7 = 3078;
                    } else if (value6 == 2368 && value7 == 3135) {
                        value7 = 3129;
                    }
                }

                boolean removedCastleWarsBucket = value3 == 1925 && value8 == 0
                        && ((value6 == 2424 && value7 == 3074)
                        || (value6 == 2425 && (value7 == 3074 || value7 == 3075))
                        || (value6 == 2375 && value7 == 3132)
                        || (value6 == 2376 && value7 == 3132));

                int value9 = 100;
                if (value3 == 245) {
                    value9 = 20;
                }
                if (ServerSettings.cacheVersion < 298 && value3 == 1982 && value6 == 3085 && value7 == 3261) {
                    value6 = 3087;
                    value7 = 3261;
                }
                if (ServerSettings.cacheVersion < 298 && value3 == 1985 && value6 == 3083 && value7 == 3260) {
                    value6 = 3084;
                    value7 = 3261;
                }
                if (!removedCastleWarsBucket
                        && ItemDefinition.isDefined(value3)
                        && (!ServerSettings.freeToPlayWorld || value5 != 1)) {
                    Object value10 = new ItemStack(value3, value4);
                    if (!ServerSettings.freeToPlayWorld || !((ItemStack)value10).getDefinition().isMembersOnly()) {
                        value9 = (int)((double)value9 * ServerSettings.itemRespawnDelayMultiplier);
                        if (CacheCoordinateTranslator.dungeonCoordinateShiftActive && CacheCoordinateTranslator.isDungeonCoordinateShiftSourceRegion(value6, value7)) {
                            value6 += 768;
                            value7 += 5120;
                        }
                        value10 = new GroundItem((ItemStack)value10, new Position(value6, value7, value8), (int)GameUtil.secondsToTicks(value9), true);
                        GroundItemManager.getInstance().spawn((GroundItem)value10);
                    }
                }
                ++index;
            }
            return;
        }
        catch (Exception exception) {
            Exception exception2 = exception;
            exception.printStackTrace();
            return;
        }
    }

    public static void refreshTradeOfferInterfaces(Player player, Player player2) {
        Player player3 = player;
        player3.packetSender.sendItemContainer(3322, player.getInventoryManager().getContainer().getRawItems());
        player3 = player2;
        player3.packetSender.sendItemContainer(3322, player2.getInventoryManager().getContainer().getRawItems());
        player3 = player;
        player3.packetSender.sendItemContainer(3415, player.getTradeOfferContainer().getRawItems());
        player3 = player;
        player3.packetSender.sendItemContainer(3416, player2.getTradeOfferContainer().getRawItems());
        player3 = player2;
        player3.packetSender.sendItemContainer(3415, player2.getTradeOfferContainer().getRawItems());
        player3 = player2;
        player3.packetSender.sendItemContainer(3416, player.getTradeOfferContainer().getRawItems());
        player3 = player;
        player3.packetSender.sendInterfaceText("Trading With: " + player2.getUsername(), 3417);
        player3 = player2;
        player3.packetSender.sendInterfaceText("Trading With: " + player.getUsername(), 3417);
    }

    public static void handleTradeRequest(Player player, Player player2) {
        if (player.getQuestState(0) != 1) {
            player.pendingTradeTarget = null;
            return;
        }
        if (player.gameMode != 0) {
            Player player3 = player;
            player3.packetSender.sendGameMessage("You are not playing on normal gamemode and cannot trade.");
            player.pendingTradeTarget = null;
            return;
        }
        if (player2.gameMode != 0) {
            Player player4 = player;
            player4.packetSender.sendGameMessage(String.valueOf(player2.getUsername()) + " is not playing on normal gamemode and cannot trade.");
            player.pendingTradeTarget = null;
            return;
        }
        if (!(ServerSettings.adminInteractionsAllowed || player.getPlayerRights() < 2 && player2.getPlayerRights() < 2)) {
            Player player5 = player;
            player5.packetSender.sendGameMessage("You are not allowed to trade this player.");
            player.pendingTradeTarget = null;
            return;
        }
        if (player2.getTradeRequestTarget() != player) {
            Player player6 = player;
            player6.packetSender.sendGameMessage("Sending trade offer...");
            player6 = player2;
            player6.packetSender.sendGameMessage(TextUtil.capitalizeFirst(player.getUsername()) + ":tradereq:");
            player.setTradeState(TradeState.REQUEST_SENT);
            player.setTradeRequestTarget(player2);
            if (player2.isBot) {
                player2.pendingTradeTarget = player;
                return;
            }
        } else {
            player.setTradeState(TradeState.OFFER_SCREEN);
            player2.setTradeState(TradeState.OFFER_SCREEN);
            Player player7 = player2;
            Player player8 = player;
            if (player8.botEnabled) {
                player8.tradeAdvertInitialOfferPlaced = false;
            }
            if (player7.botEnabled) {
                player7.tradeAdvertInitialOfferPlaced = false;
            }
            player8.getTradeOfferContainer().clear();
            player7.getTradeOfferContainer().clear();
            Player player9 = player8;
            player9.packetSender.sendInterfaceText("Trading With: " + player7.getUsername() + " has " + player7.getInventoryManager().getContainer().getFreeSlots() + " free inventory slots.", 3417);
            player9 = player7;
            player9.packetSender.sendInterfaceText("Trading With: " + player8.getUsername() + " has " + player8.getInventoryManager().getContainer().getFreeSlots() + " free inventory slots.", 3417);
            player9 = player8;
            player9.packetSender.sendInterfaceText("", 3431);
            player9 = player7;
            player9.packetSender.sendInterfaceText("", 3431);
            GameplayHelper.refreshTradeOfferInterfaces(player8, player7);
            player8.setTradePartner(player7);
            player7.setTradePartner(player8);
            player9 = player8;
            player9.packetSender.showInterfaceWithInventory(3323, 3321);
            player9 = player7;
            player9.packetSender.showInterfaceWithInventory(3323, 3321);
            player.setTradeRequestTarget(null);
            player2.setTradeRequestTarget(null);
        }
    }

    public static void declineTrade(Player player) {
        if (player.getTradePartner() == null) {
            player.pendingTradeTarget = null;
            return;
        }
        Player player2 = (Player)player.getTradePartner();
        ((Player)player.getTradePartner()).pendingTradeTarget = null;
        player.pendingTradeTarget = null;
        Player player3 = player2;
        player3.packetSender.sendGameMessage("Other player has declined the trade.");
        player3 = player;
        player3.packetSender.closeInterfaces();
        player3 = player2;
        player3.packetSender.closeInterfaces();
        player.setTradeState(TradeState.NONE);
        player2.setTradeState(TradeState.NONE);
        GameplayHelper.returnTradeOfferItems(player2);
        GameplayHelper.returnTradeOfferItems(player);
        player.setTradePartner(null);
        player2.setTradePartner(null);
        if (player.botEnabled) {
            player.tradeAdvertLastOfferAmount = -1;
        }
        if (player2.botEnabled) {
            player2.tradeAdvertLastOfferAmount = -1;
        }
        player3 = player;
        CharacterFileManager.savePlayer(player3);
        player3 = player2;
        CharacterFileManager.savePlayer(player3);
    }

    public static void returnTradeOfferItems(Player player) {
        int index = 0;
        while (index < 28) {
            ItemStack itemStack;
            if (player.getTradeOfferContainer().getItemAt(index) != null && (itemStack = player.getTradeOfferContainer().getItemAt(index)) != null) {
                player.getTradeOfferContainer().remove(itemStack);
                player.getInventoryManager().addItem(itemStack);
            }
            ++index;
        }
        player.getTradeOfferContainer().clear();
    }

    public static void addTradeOfferItem(Player player, int itemId, int value4, int value32) {
        Object value2;
        Player player2 = (Player)player.getTradePartner();
        if (player.getTradeState().equals((Object)TradeState.CONFIRM_SCREEN)) {
            return;
        }
        if (value4 == -1 || player2 == null) {
            return;
        }
        ItemStack itemStack = player.getInventoryManager().getContainer().getItemAt(itemId);
        int inventoryManager = player.getInventoryManager().getContainer().getItemAmount(value4);
        if (itemStack == null || itemStack.getId() != value4 || !itemStack.isValid()) {
            return;
        }
        if (itemStack.getId() <= 0 || !itemStack.isValid() || value32 <= 0) {
            return;
        }
        ItemStack itemStack2 = new ItemStack(value4);
        if (itemStack2.getDefinition().isUntradeable()) {
            if (player.getPlayerRights() < 2) {
                Player player3 = player;
                player3.packetSender.sendGameMessage("You cannot trade that item.");
                return;
            }
            value2 = player;
            ((Player)value2).packetSender.sendGameMessage("Note: " + itemStack2.getDefinition().getName() + " is untradeable item!");
        }
        if (inventoryManager > value32) {
            inventoryManager = value32;
        }
        if (itemStack.getDefinition().isStackable()) {
            if (!player.getInventoryManager().removeItemFromSlot(new ItemStack(value4, inventoryManager), itemId)) {
                player.getInventoryManager().removeItem(new ItemStack(value4, inventoryManager));
            }
        } else {
            itemId = 0;
            while (itemId < inventoryManager) {
                player.getInventoryManager().removeItem(new ItemStack(value4, 1));
                ++itemId;
            }
        }
        if ((itemId = player.getTradeOfferContainer().getItemAmount(value4)) > 0 && itemStack.getDefinition().isStackable()) {
            player.getTradeOfferContainer().setItem(player.getTradeOfferContainer().indexOfItem(itemStack.getId()), new ItemStack(value4, itemId + inventoryManager));
        } else {
            ItemStack itemStack3 = new ItemStack(itemStack.getId(), inventoryManager);
            value2 = player.getTradeOfferContainer();
            ((ItemContainer)value2).add(itemStack3, -1);
        }
        GameplayHelper.refreshTradeOfferInterfaces(player, player2);
        player.setTradeState(TradeState.OFFER_SCREEN);
        player2.setTradeState(TradeState.OFFER_SCREEN);
        value2 = player;
        ((Player)value2).packetSender.sendInterfaceText("", 3431);
        value2 = player2;
        ((Player)value2).packetSender.sendInterfaceText("", 3431);
    }

    public static void removeTradeOfferItem(Player player, int itemId, int value2, int value32) {
        Player player2 = (Player)player.getTradePartner();
        if (player.getTradeState().equals((Object)TradeState.CONFIRM_SCREEN)) {
            return;
        }
        if (value2 == -1 || player2 == null) {
            return;
        }
        ItemStack itemStack = player.getTradeOfferContainer().getItemAt(itemId);
        int tradeOfferContainer = player.getTradeOfferContainer().getItemAmount(value2);
        if (itemStack == null || itemStack.getId() != value2 || value32 <= 0) {
            return;
        }
        if (tradeOfferContainer > value32) {
            tradeOfferContainer = value32;
        }
        itemId = player.getTradeOfferContainer().removeFromSlot(new ItemStack(value2, tradeOfferContainer), itemId);
        player.getInventoryManager().addItem(new ItemStack(itemStack.getId(), itemId));
        GameplayHelper.refreshTradeOfferInterfaces(player, player2);
        player.setTradeState(TradeState.OFFER_SCREEN);
        player2.setTradeState(TradeState.OFFER_SCREEN);
        player.packetSender.sendInterfaceText("", 3431);
        player = player2;
        player.packetSender.sendInterfaceText("", 3431);
    }

    public static void acceptTradeFirstScreen(Player player) {
        ItemStack itemStack;
        Player player2 = (Player)player.getTradePartner();
        player.setTradeState(TradeState.ACCEPTED);
        if (!player2.getTradeState().equals((Object)TradeState.ACCEPTED)) {
            Player player3 = player;
            player3.packetSender.sendInterfaceText("Waiting for other player...", 3431);
            player3 = player2;
            player3.packetSender.sendInterfaceText("Other player accepted.", 3431);
            return;
        }
        int index = 0;
        ItemStack[] itemStackArray = player.getTradeOfferContainer().getItems();
        int length = itemStackArray.length;
        int index2 = 0;
        while (index2 < length) {
            itemStack = itemStackArray[index2];
            if (!(itemStack == null || itemStack.getDefinition().isStackable() && player2.getInventoryManager().containsItem(itemStack.getId()))) {
                ++index;
            }
            ++index2;
        }
        if (player2.getInventoryManager().getContainer().getFreeSlots() < index) {
            Player player4 = player;
            player4.packetSender.sendInterfaceText("Other player doesn't have enough inventory space for this trade.", 3431);
            player4 = player2;
            player4.packetSender.sendInterfaceText("You don't have enough inventory space for this trade.", 3431);
            return;
        }
        index = 0;
        itemStackArray = player2.getTradeOfferContainer().getItems();
        length = itemStackArray.length;
        index2 = 0;
        while (index2 < length) {
            itemStack = itemStackArray[index2];
            if (!(itemStack == null || itemStack.getDefinition().isStackable() && player.getInventoryManager().containsItem(itemStack.getId()))) {
                ++index;
            }
            ++index2;
        }
        if (player.getInventoryManager().getContainer().getFreeSlots() < index) {
            Player player5 = player2;
            player5.packetSender.sendInterfaceText("Other player doesn't have enough inventory space for this trade.", 3431);
            player5 = player;
            player5.packetSender.sendInterfaceText("You don't have enough inventory space for this trade.", 3431);
            return;
        }
        GameplayHelper.refreshTradeOfferInterfaces(player, player2);
        Player player6 = player;
        player6.packetSender.showInterfaceWithInventory(3443, 3213);
        player6 = player2;
        player6.packetSender.showInterfaceWithInventory(3443, 3213);
        player.setTradeState(TradeState.CONFIRM_SCREEN);
        player2.setTradeState(TradeState.CONFIRM_SCREEN);
        player6 = player;
        player6.packetSender.sendInterfaceText("Are you sure you want to accept this trade?", 3535);
        player6 = player2;
        player6.packetSender.sendInterfaceText("Are you sure you want to accept this trade?", 3535);
        GameplayHelper.refreshTradeConfirmationSummary(player);
        GameplayHelper.refreshTradeConfirmationSummary(player2);
    }

    public static void acceptTradeSecondScreen(Player player) {
        if (!player.getTradeState().equals((Object)TradeState.CONFIRM_SCREEN)) {
            return;
        }
        Player player2 = (Player)player.getTradePartner();
        player.setTradeState(TradeState.ACCEPTED);
        if (!player2.getTradeState().equals((Object)TradeState.ACCEPTED)) {
            Player player3 = player;
            player3.packetSender.sendInterfaceText("Waiting for other player...", 3535);
            player3 = player2;
            player3.packetSender.sendInterfaceText("Other player accepted.", 3535);
            return;
        }
        ItemStack[] playerTradeItems = new ItemStack[28];
        int index = 0;
        while (index < 28) {
            ItemStack itemStack = player.getTradeOfferContainer().getItemAt(index);
            if (itemStack != null) {
                playerTradeItems[index] = itemStack;
                player.getTradeOfferContainer().remove(itemStack);
                player2.getInventoryManager().addItem(itemStack);
            }
            ++index;
        }
        ItemStack[] partnerTradeItems = new ItemStack[28];
        index = 0;
        while (index < 28) {
            ItemStack itemStack = player2.getTradeOfferContainer().getItemAt(index);
            if (itemStack != null) {
                partnerTradeItems[index] = itemStack;
                player2.getTradeOfferContainer().remove(itemStack);
                player.getInventoryManager().addItem(itemStack);
            }
            ++index;
        }
        player.setTradeState(TradeState.NONE);
        player2.setTradeState(TradeState.NONE);
        player.packetSender.sendGameMessage("You accept the trade.");
        player2.packetSender.sendGameMessage("You accept the trade.");
        player.packetSender.closeInterfaces();
        player2.packetSender.closeInterfaces();
        player.setTradePartner(null);
        player2.setTradePartner(null);
        player2.pendingTradeTarget = null;
        player.pendingTradeTarget = null;
        CharacterFileManager.savePlayer(player);
        CharacterFileManager.savePlayer(player2);
        boolean bothPlayersAreBots = false;
        if (player.botEnabled && player2.botEnabled) {
            bothPlayersAreBots = true;
        }
        if (player.tradeAdvertMode != -1) {
            BotTaskDefinition.completeTradeAdvertOffer(player, bothPlayersAreBots);
        }
        if (player2.tradeAdvertMode != -1) {
            BotTaskDefinition.completeTradeAdvertOffer(player2, bothPlayersAreBots);
        }
    }

    private static void refreshTradeConfirmationSummary(Player player) {
        int value;
        Player player2 = (Player)player.getTradePartner();
        StringBuilder stringBuilder = new StringBuilder();
        boolean enabled = true;
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        int index = 0;
        while (index < 28) {
            ItemStack itemStack = player.getTradeOfferContainer().getItemAt(index);
            if (itemStack != null) {
                int initialValue = -1;
                value = 0;
                while (value < arrayList.size()) {
                    if (((ItemStack)arrayList.get(value)).getId() == itemStack.getId()) {
                        initialValue = value;
                        break;
                    }
                    ++value;
                }
                if (initialValue == -1) {
                    arrayList.add(new ItemStack(itemStack.getId(), itemStack.getAmount()));
                } else {
                    value = ((ItemStack)arrayList.get(initialValue)).getAmount() + itemStack.getAmount();
                    arrayList.set(initialValue, new ItemStack(itemStack.getId(), value));
                }
            }
            ++index;
        }
        for (ItemStack itemStack : arrayList) {
            if (itemStack == null) continue;
            enabled = false;
            String amount = itemStack.getAmount() >= 1000 && itemStack.getAmount() < 1000000 ? "@cya@" + itemStack.getAmount() / 1000 + "K @whi@(" + GameUtil.formatNumber(itemStack.getAmount()) + ")" : (itemStack.getAmount() >= 1000000 ? "@gre@" + itemStack.getAmount() / 1000000 + " million @whi@(" + GameUtil.formatNumber(itemStack.getAmount()) + ")" : "" + itemStack.getAmount());
            stringBuilder.append(itemStack.getDefinition().getName());
            stringBuilder.append(" x ");
            stringBuilder.append(amount);
            stringBuilder.append("\\n");
        }
        if (enabled) {
            stringBuilder.append("Absolutely nothing!");
        }
        Player player3 = player;
        player3.packetSender.sendInterfaceText(stringBuilder.toString(), 3557);
        stringBuilder = new StringBuilder();
        boolean enabled2 = true;
        ArrayList<ItemStack> arrayList2 = new ArrayList<ItemStack>();
        int index2 = 0;
        while (index2 < 28) {
            ItemStack itemStack = player2.getTradeOfferContainer().getItemAt(index2);
            if (itemStack != null) {
                value = -1;
                int index3 = 0;
                while (index3 < arrayList2.size()) {
                    if (((ItemStack)arrayList2.get(index3)).getId() == itemStack.getId()) {
                        value = index3;
                        break;
                    }
                    ++index3;
                }
                if (value == -1) {
                    arrayList2.add(new ItemStack(itemStack.getId(), itemStack.getAmount()));
                } else {
                    index3 = ((ItemStack)arrayList2.get(value)).getAmount() + itemStack.getAmount();
                    arrayList2.set(value, new ItemStack(itemStack.getId(), index3));
                }
            }
            ++index2;
        }
        if (player2 == null) {
            return;
        }
        for (ItemStack itemStack : arrayList2) {
            if (itemStack == null) continue;
            enabled2 = false;
            String amount2 = itemStack.getAmount() >= 1000 && itemStack.getAmount() < 1000000 ? "@cya@" + itemStack.getAmount() / 1000 + "K @whi@(" + GameUtil.formatNumber(itemStack.getAmount()) + ")" : (itemStack.getAmount() >= 1000000 ? "@gre@" + itemStack.getAmount() / 1000000 + " million @whi@(" + GameUtil.formatNumber(itemStack.getAmount()) + ")" : "" + itemStack.getAmount());
            stringBuilder.append(itemStack.getDefinition().getName());
            stringBuilder.append(" x ");
            stringBuilder.append(amount2);
            stringBuilder.append("\\n");
        }
        if (enabled2) {
            stringBuilder.append("Absolutely nothing!");
        }
        Player player4 = player;
        player4.packetSender.sendInterfaceText(stringBuilder.toString(), 3558);
    }

    public static int getCaveLightLevelForItemId(int itemId) {
        CaveLightSourceDefinition caveLightSourceDefinition = CaveLightSourceDefinition.forItemId(itemId);
        if (caveLightSourceDefinition == null) {
            return 0;
        }
        if (caveLightSourceDefinition.getUnlitItemId() == itemId) {
            return 0;
        }
        return caveLightSourceDefinition.getLightLevel();
    }

    public static boolean extinguishCaveLightSource(Player player, int value2, boolean enabled2) {
        CaveLightSourceDefinition caveLightSourceDefinition = CaveLightSourceDefinition.forItemId(value2);
        if (caveLightSourceDefinition == null || !player.getInventoryManager().containsItem(value2)) {
            return false;
        }
        if (caveLightSourceDefinition.getLitItemId() != value2) {
            return false;
        }
        player.getInventoryManager().removeItem(new ItemStack(value2, 1));
        player.getInventoryManager().addItem(new ItemStack(caveLightSourceDefinition.getUnlitItemId(), 1));
        ItemDefinition itemDefinition = ItemDefinition.forId(value2);
        if (enabled2) {
            player.packetSender.sendGameMessage("You extinguish the " + itemDefinition.getName().toLowerCase() + ".");
        }
        return true;
    }

    public static boolean castSelectedItemTeleport(Player player, Position position) {
        Player player2 = player;
        player2.packetSender.closeInterfaces();
        player2 = player;
        if (player2.interfaceAction.equals("operate") ? player.getEquipmentManager().getItemIdAtSlot(player.getSelectedItemSlot()) != player.getSelectedItemId() : !player.getInventoryManager().containsItem(player.getSelectedItemId())) {
            return false;
        }
        boolean teleportStarted = player.getTeleportManager().castItemTeleport(position);
        if (player.botEnabled && !teleportStarted && player.botCombatState.startsWith("escape")) {
            player.botCombatState = "tele";
            BotCombatEscapeHandler.startBotCombatWalkingEscape(player);
        } else if (player.botEnabled && !teleportStarted && player.botCombatState.equals("tele")) {
            player.botCombatState = "run";
        }
        if (!teleportStarted) {
            return false;
        }
        player2 = player;
        if (player.interfaceAction.equals("operate")) {
            int nextItemId;
            if (player.getEquipmentManager().removeItem(new ItemStack(player.getSelectedItemId())) && (nextItemId = GameplayHelper.getNextDegradedJewelryItemId(player.getSelectedItemId())) > 0) {
                player.getEquipmentManager().setSlotItem(nextItemId, player.getSelectedItemSlot());
            }
        } else {
            int nextItemId;
            if (player.getInventoryManager().removeItem(new ItemStack(player.getSelectedItemId())) && (nextItemId = GameplayHelper.getNextDegradedJewelryItemId(player.getSelectedItemId())) > 0) {
                player.getInventoryManager().addItem(new ItemStack(nextItemId));
            }
        }
        return true;
    }

    public static int getNextDegradedJewelryItemId(int itemId) {
        boolean enabled = false;
        ChargedJewelryDefinition[] chargedJewelryDefinitionArray = ChargedJewelryDefinition.values();
        int length = chargedJewelryDefinitionArray.length;
        int index = 0;
        while (index < length) {
            ChargedJewelryDefinition chargedJewelryDefinition = chargedJewelryDefinitionArray[index];
            int[] itemIdsByDescendingCharge = chargedJewelryDefinition.getItemIdsByDescendingCharge();
            int length2 = itemIdsByDescendingCharge.length;
            int index2 = 0;
            while (index2 < length2) {
                int value = itemIdsByDescendingCharge[index2];
                if (enabled) {
                    return value;
                }
                if (itemId == value) {
                    enabled = true;
                }
                ++index2;
            }
            ++index;
        }
        return 0;
    }

    public static void appendLogLine(String text2, String logName) {
        String path = "./data/logs/" + logName + ".txt";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));
            try {
                writer.write(text2);
                writer.newLine();
            }
            finally {
                writer.close();
            }
            return;
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return;
        }
    }

    public static int getDaysBetweenMidnights(long value3, long value22) {
        DateTime dateTime = new DateTime(value3);
        DateTime dateTime2 = new DateTime(value22);
        int days = Days.daysBetween(dateTime.toDateMidnight(), dateTime2.toDateMidnight()).getDays();
        return days;
    }

    public static int getHoursBetween(long value3, long value22) {
        DateTime dateTime = new DateTime(value3);
        DateTime dateTime2 = new DateTime(value22);
        int hours = Hours.hoursBetween(dateTime, dateTime2).getHours();
        return hours;
    }

    public static long addDaysToTimestamp(long value3, int value4) {
        DateTime dateTime = new DateTime(value3);
        dateTime = dateTime.plusDays(value4);
        return dateTime.getMillis();
    }

    public static long buildTimestampMillis(int value6, int value22, int value32, int value42, int value52) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(value32, value22 - 1, value6, value42, value52, 0);
        return gregorianCalendar.getTimeInMillis();
    }

    public static String formatDateDayMonthYear(long value2) {
        DateTime dateTime = new DateTime(value2);
        return String.valueOf(dateTime.getDayOfMonth()) + "-" + dateTime.getMonthOfYear() + "-" + dateTime.getYear();
    }

    public static String formatDurationHoursMinutes(long duration) {
        String text = String.valueOf(duration / 1000L / 60L / 60L) + "h " + duration / 1000L / 60L % 60L + "min";
        return text;
    }
}

package com.rs2.net.packet.handler;

import com.rs2.ServerSettings;
import com.rs2.cache.CacheArchive;
import com.rs2.cache.InterfaceDefinition;
import com.rs2.model.EntityUpdateState;
import com.rs2.model.GameplayHelper;
import com.rs2.model.gameplay.barrows.BarrowsManager;
import com.rs2.model.gameplay.castlewars.CastleWarsEngineeringManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.gameplay.partyroom.PartyRoomManager;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemStack;
import com.rs2.model.item.action.BarrowsRepairHandler;
import com.rs2.model.item.action.MysticStaffEnchantment;
import com.rs2.model.music.MusicManager;
import com.rs2.model.music.MusicTrackDefinition;
import com.rs2.model.player.BankManager;
import com.rs2.model.player.BankRearrangeMode;
import com.rs2.model.player.GrandExchangeManager;
import com.rs2.model.player.Player;
import com.rs2.model.player.PlayerUpdateTask;
import com.rs2.model.randomevent.RandomEventManager;
import com.rs2.model.skill.cooking.CookingManager;
import com.rs2.model.skill.cooking.DairyChurnHandler;
import com.rs2.model.skill.crafting.CraftingHandler;
import com.rs2.model.skill.magic.MagicSpellAction;
import com.rs2.model.skill.magic.SpellDefinition;
import com.rs2.model.skill.smithing.SmeltingHandler;
import com.rs2.model.travel.TravelManager;
import com.rs2.model.travel.WorldTeleportMenu;
import com.rs2.model.travel.canoe.CanoeTravelManager;
import com.rs2.net.packet.IncomingPacket;
import com.rs2.net.packet.PacketHandler;
import com.rs2.util.GameplayTrace;

public final class ButtonClickPacketHandler
implements PacketHandler {
    @Override
    public final void handle(Player player, IncomingPacket packet) {
        int buttonId = packet.getReader().readSignedShort();
        handleButton(player, buttonId);
    }

    public void handleButton(Player player, int buttonId) {
        InterfaceDefinition interfaceDefinition = InterfaceDefinition.forId(buttonId);
        boolean interfaceOpen = player.isInterfaceIdOpen(buttonId);
        boolean bankControl = player.getOpenInterfaceId() == 5292
                && BankManager.isBankControlButton(buttonId);
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("button decoded player=" + GameplayTrace.describe(player)
                + " buttonId=" + buttonId
                + " parentInterface=" + (interfaceDefinition == null ? -1 : interfaceDefinition.getParentInterfaceId())
                + " interfaceOpen=" + interfaceOpen
                + " spellbook=" + player.getSpellbook());
        }

        // The native 377 catapult interface contains nested controls whose
        // immediate parent is not always the root open interface. Let the
        // catapult handler validate those controls against its own open state
        // before the generic direct-parent interface guard discards the click.
        if (CastleWarsEngineeringManager.handleCatapultButton(player, buttonId)) return;
        if (CastleWarsManager.handleCastleWarsManualButton(player, buttonId)) return;
        // Native group 55's scoreboard close (443 virtual button 19730)
        // is not part of the legacy hierarchy, so handle it before the
        // generic direct-parent interface-open guard.
        if (CastleWarsManager.handleScoreboardCloseButton(player, buttonId)) return;

        // Revision 443 character design is native group 269 while the legacy
        // server tracks the coexisting Tutorial Island chatbox (6179) as the
        // open interface. Its Accept control is translated to legacy button
        // 3651, so the normal legacy parent/open-interface guard would reject
        // a valid click before the handler below can advance stage 0 -> 2.
        boolean revision443CharacterDesignAccept = ServerSettings.clientBuild == 443
                && buttonId == 3651
                && player.getQuestState(0) == 0;
        boolean revision443RunToggle = ServerSettings.clientBuild == 443
                && buttonId == 153;
        if (!interfaceOpen && !bankControl && !revision443CharacterDesignAccept
                && !revision443RunToggle) {
            if (player.isInteractionDebugEnabled()) {
                int parentInterfaceId = interfaceDefinition == null
                        ? -1 : interfaceDefinition.getParentInterfaceId();
                String debugMessage = "Unhandled button: " + buttonId
                        + " (parent=" + parentInterfaceId + ", open=false)";
                player.packetSender.sendGameMessage(debugMessage);
                System.out.println("[button-debug] " + player.getUsername()
                        + " " + debugMessage);
            }
            return;
        }
        if (WorldTeleportMenu.handleButton(player, buttonId)) return;
        if (ServerSettings.debugModeEnabled) {
            System.out.println("button id: " + buttonId);
        }
        if (CastleWarsManager.handleReplacementOfferButton(player, buttonId)) return;
        if (player.getDuelSession().handleButtonClick(buttonId) != false) return;
        if (player.getQuestManager().handleButtonClick(buttonId) != false) return;
        if (buttonId >= 18792 && buttonId <= 18812) {
            int hiscoreType = buttonId - 18792;
            player.hiscorePage = 0;
            player.hiscoreCategory = hiscoreType;
            player.showHiscoreInterface(0);
        }
        if (buttonId == 18791) {
            player.hiscorePage = 0;
            player.hiscoreCategory = 21;
            player.showHiscoreInterface(0);
        }
        if (buttonId == 18813) {
            player.hiscorePage = 0;
            player.hiscoreCategory = 22;
            player.showHiscoreInterface(0);
        }
        if (buttonId == 18884 && player.hiscorePage > 0) {
            --player.hiscorePage;
            player.showHiscoreInterface(0);
        }
        if (buttonId == 18883) {
            ++player.hiscorePage;
            player.showHiscoreInterface(0);
        }
        if (buttonId == 2246) {
            PartyRoomManager.depositStagedItemsToChest(player);
        }
        int bankTabIndex = 0;
        while (bankTabIndex < BankManager.bankTabUpdateTasks.length) {
            PlayerUpdateTask bankTabUpdateTask = BankManager.bankTabUpdateTasks[bankTabIndex];
            if (bankTabUpdateTask.getBankTabButtonId() == buttonId) {
                BankManager.selectTab(player, bankTabIndex);
                return;
            }
            ++bankTabIndex;
        }
        if (player.interfaceAction != "tanning") {
            int trackId = 0;
            while (trackId < MusicTrackDefinition.trackCount) {
                MusicTrackDefinition track = MusicTrackDefinition.forTrackId(trackId);
                if (track.getButtonId() == buttonId) {
                    MusicManager.playManualTrack(player, trackId);
                    return;
                }
                ++trackId;
            }
        }
        if (player.interfaceAction == "tanning" && buttonId == 7451) {
            player.interfaceAction = "";
            player.packetSender.closeInterfaces();
            return;
        }
        if (buttonId == 6269) {
            MusicManager.setAutomaticMode(player, true);
            new MusicManager().updateForPlayerPosition(player);
            return;
        }
        if (buttonId == 6270) {
            MusicManager.setAutomaticMode(player, false);
            return;
        }
        switch (buttonId) {
            case 10162: {
                player.packetSender.closeInterfaces();
                player.activeBookItemId = 0;
                player.activeBookPageIndex = 0;
                break;
            }
            case 6020: {
                player.packetSender.closeInterfaces();
                player.npcTransformationId = -1;
                player.packetSender.refreshSidebarInterfaces();
                player.setAppearanceUpdateRequired(true);
                break;
            }
            case 2422: {
                player.packetSender.closeInterfaces();
                break;
            }
            case 8654: {
                System.out.println("[SKILL DEBUG] opening Attack guide");
                player.getSkillGuideManager().showAttackGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 0;
                return;
            }
            case 8657: {
                player.getSkillGuideManager().showStrengthGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 1;
                return;
            }
            case 8660: {
                player.getSkillGuideManager().showDefenceGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 2;
                return;
            }
            case 8663: {
                player.getSkillGuideManager().showRangedGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 3;
                return;
            }
            case 8666: {
                player.getSkillGuideManager().showPrayerGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 4;
                return;
            }
            case 8669: {
                player.getSkillGuideManager().showMagicGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 5;
                return;
            }
            case 8672: {
                player.getSkillGuideManager().showRunecraftingGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 6;
                return;
            }
            case 8655: {
                player.getSkillGuideManager().showHitpointsGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 7;
                return;
            }
            case 8658: {
                player.getSkillGuideManager().showAgilityGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 8;
                return;
            }
            case 8661: {
                player.getSkillGuideManager().showHerbloreGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 9;
                return;
            }
            case 8664: {
                player.getSkillGuideManager().showThievingGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 10;
                return;
            }
            case 8667: {
                player.getSkillGuideManager().showCraftingGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 11;
                return;
            }
            case 8670: {
                player.getSkillGuideManager().showFletchingGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 12;
                return;
            }
            case 12162: {
                player.getSkillGuideManager().showSlayerGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 13;
                return;
            }
            case 8656: {
                player.getSkillGuideManager().showMiningGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 14;
                return;
            }
            case 8659: {
                player.getSkillGuideManager().showSmithingGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 15;
                return;
            }
            case 8662: {
                player.getSkillGuideManager().showFishingGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 16;
                return;
            }
            case 8665: {
                player.getSkillGuideManager().showCookingGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 17;
                return;
            }
            case 8668: {
                player.getSkillGuideManager().showFiremakingGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 18;
                return;
            }
            case 8671: {
                player.getSkillGuideManager().showWoodcuttingGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 19;
                return;
            }
            case 13928: {
                player.getSkillGuideManager().showFarmingGuide(1);
                player.getSkillGuideManager().selectedSkillIndex = 20;
                return;
            }
            case 8846: {
                player.getSkillGuideManager().showSelectedSkillCategory(1);
                return;
            }
            case 8823: {
                player.getSkillGuideManager().showSelectedSkillCategory(2);
                return;
            }
            case 8824: {
                player.getSkillGuideManager().showSelectedSkillCategory(3);
                return;
            }
            case 8827: {
                player.getSkillGuideManager().showSelectedSkillCategory(4);
                return;
            }
            case 8837: {
                player.getSkillGuideManager().showSelectedSkillCategory(5);
                return;
            }
            case 8840: {
                player.getSkillGuideManager().showSelectedSkillCategory(6);
                return;
            }
            case 8843: {
                player.getSkillGuideManager().showSelectedSkillCategory(7);
                return;
            }
            case 8859: {
                player.getSkillGuideManager().showSelectedSkillCategory(8);
                return;
            }
            case 8862: {
                player.getSkillGuideManager().showSelectedSkillCategory(9);
                return;
            }
            case 8865: {
                player.getSkillGuideManager().showSelectedSkillCategory(10);
                return;
            }
            case 15303: {
                player.getSkillGuideManager().showSelectedSkillCategory(11);
                return;
            }
            case 15306: {
                player.getSkillGuideManager().showSelectedSkillCategory(12);
                return;
            }
            case 15309: {
                player.getSkillGuideManager().showSelectedSkillCategory(13);
                return;
            }
            case 152: {
                player.getMovementQueue().setRunning(false);
                return;
            }
            case 153: {
                if (!player.isTutorialRunUnlocked() || player.getRunEnergyPercent() <= 0) {
                    player.getMovementQueue().setRunning(false);
                    player.packetSender.sendRunEnergy();
                    return;
                }
                boolean tutorialRunPrompt = player.getQuestState(0) == 23;
                if (tutorialRunPrompt) {
                    player.advanceTutorialStage();
                }
                player.getMovementQueue().setRunning(tutorialRunPrompt
                        || ServerSettings.clientBuild != 443
                        || !player.getMovementQueue().isRunning());
                return;
            }
            case 150: {
                player.setAutoRetaliate(true);
                return;
            }
            case 151: {
                player.setAutoRetaliate(false);
                return;
            }
            case 906: {
                player.setBrightness(1);
                player.packetSender.sendConfig(166, player.getBrightness());
                return;
            }
            case 908: {
                player.setBrightness(2);
                player.packetSender.sendConfig(166, player.getBrightness());
                return;
            }
            case 910: {
                player.setBrightness(3);
                player.packetSender.sendConfig(166, player.getBrightness());
                return;
            }
            case 912: {
                player.setBrightness(4);
                player.packetSender.sendConfig(166, player.getBrightness());
                return;
            }
            case 914: {
                player.setMouseButtons(1);
                return;
            }
            case 913: {
                player.setMouseButtons(0);
                return;
            }
            case 915: {
                player.setPublicChatEffects(0);
                return;
            }
            case 916: {
                player.setPublicChatEffects(1);
                return;
            }
            case 957: {
                player.setSplitPrivateChat(1);
                return;
            }
            case 958: {
                player.setSplitPrivateChat(0);
                return;
            }
            case 12464: {
                player.setAcceptAid(0);
                return;
            }
            case 12465: {
                player.setAcceptAid(1);
                return;
            }
            case 930: {
                player.setMusicVolume(4);
                player.packetSender.sendConfig(168, player.getMusicVolume());
                return;
            }
            case 931: {
                player.setMusicVolume(3);
                player.packetSender.sendConfig(168, player.getMusicVolume());
                return;
            }
            case 932: {
                player.setMusicVolume(2);
                player.packetSender.sendConfig(168, player.getMusicVolume());
                return;
            }
            case 933: {
                player.setMusicVolume(1);
                player.packetSender.sendConfig(168, player.getMusicVolume());
                return;
            }
            case 934: {
                player.setMusicVolume(0);
                player.packetSender.sendConfig(168, player.getMusicVolume());
                return;
            }
            case 941: {
                player.setEffectVolume(4);
                player.packetSender.sendConfig(169, player.getEffectVolume());
                return;
            }
            case 942: {
                player.setEffectVolume(3);
                player.packetSender.sendConfig(169, player.getEffectVolume());
                return;
            }
            case 943: {
                player.setEffectVolume(2);
                player.packetSender.sendConfig(169, player.getEffectVolume());
                return;
            }
            case 944: {
                player.setEffectVolume(1);
                player.packetSender.sendConfig(169, player.getEffectVolume());
                return;
            }
            case 945: {
                player.setEffectVolume(0);
                player.packetSender.sendConfig(169, player.getEffectVolume());
                return;
            }
            case 946: case 947: case 948: case 949: case 950: {
                int volume = 950 - buttonId;
                int configId = ServerSettings.clientBuild == 443 ? 872 : 170;
                player.configStates[configId] = volume;
                player.packetSender.sendConfig(configId, volume);
                return;
            }
            case 5386: {
                player.setBankWithdrawNoteMode(true);
                return;
            }
            case 5387: {
                player.setBankWithdrawNoteMode(false);
                return;
            }
            case 18885: {
                BankManager.depositInventory(player);
                return;
            }
            case 18886: {
                BankManager.depositEquipment(player);
                return;
            }
            case 8130: {
                player.setBankRearrangeMode(BankRearrangeMode.SWAP);
                return;
            }
            case 8131: {
                player.setBankRearrangeMode(BankRearrangeMode.INSERT);
                return;
            }
        }
        if (BarrowsManager.handlePuzzleButtonClick(player, buttonId) != false) return;
        if (GrandExchangeManager.handleButtonClick(player, buttonId) != false) return;
        if (MagicSpellAction.handleAutocastButton(player, buttonId) != false) return;
        if (player.getEquipmentManager().handleAttackStyleButton(buttonId)) {
            player.getEquipmentManager().refreshWeaponInterface();
            return;
        }
        if (player.getPrayerManager().handleButtonClick(buttonId) != false) return;
        if (player.isActionLocked() != false) return;
        switch (buttonId) {
            case 14175: {
                if (player.getPendingDestroyItem() != null) {
                    BarrowsRepairHandler repairHandler = BarrowsRepairHandler.forItem(player.getPendingDestroyItem());
                    if (repairHandler != null) {
                        if (player.getInventoryManager().getContainer().containsItem(player.getPendingDestroyItem().getId())) {
                            player.packetSender.sendSoundEffect(376, 1, 0);
                            if (!ServerSettings.adminInteractionsAllowed && player.getPlayerRights() >= 2) {
                                player.packetSender.sendGameMessage("Your item disappears because you're an administrator.");
                            } else {
                                GroundItemManager.getInstance().spawn(new GroundItem(new ItemStack(repairHandler.getFullyDegradedItemId(), 1), player));
                            }
                            if (!player.getInventoryManager().removeItemFromSlot(player.getPendingDestroyItem(), player.getSelectedItemSlot())) {
                                player.getInventoryManager().removeItem(player.getPendingDestroyItem());
                            }
                        }
                        player.getEquipmentManager().refreshCarriedValue();
                    } else {
                        if (player.getPendingDestroyItem().getDefinition().getName().toLowerCase().contains("progress hat")) {
                            player.resetMageTrainingArenaPizazzPoints();
                        }
                        if (player.getPendingDestroyItem().getDefinition().getName().toLowerCase().contains("clue scroll")) {
                            player.treasureTrailStepCount = 0;
                        }
                        player.getInventoryManager().removeItem(player.getPendingDestroyItem());
                    }
                }
                player.setPendingDestroyItem(null);
            }
            case 14176: {
                player.setPendingDestroyItem(null);
                player.packetSender.closeInterfaces();
                break;
            }
            case 12566: {
                player.getTeleportManager().castSpellbookTeleport(player.getTeleotherDestination());
            }
            case 12568: {
                player.packetSender.closeInterfaces();
                return;
            }
            case 168: {
                player.getUpdateState().setAnimation(855);
                return;
            }
            case 169: {
                player.getUpdateState().setAnimation(856);
                return;
            }
            case 162: {
                player.getUpdateState().setAnimation(857);
                return;
            }
            case 164: {
                player.getUpdateState().setAnimation(858);
                return;
            }
            case 165: {
                player.getUpdateState().setAnimation(859);
                return;
            }
            case 161: {
                player.getUpdateState().setAnimation(860);
                return;
            }
            case 170: {
                player.getUpdateState().setAnimation(861);
                return;
            }
            case 171: {
                player.getUpdateState().setAnimation(862);
                return;
            }
            case 163: {
                player.getUpdateState().setAnimation(863);
                return;
            }
            case 167: {
                player.getUpdateState().setAnimation(864);
                return;
            }
            case 172: {
                player.getUpdateState().setAnimation(865);
                return;
            }
            case 166: {
                player.getUpdateState().setAnimation(866);
                return;
            }
            case 13362: {
                player.getUpdateState().setAnimation(2105);
                return;
            }
            case 13363: {
                player.getUpdateState().setAnimation(2106);
                return;
            }
            case 13364: {
                player.getUpdateState().setAnimation(2107);
                return;
            }
            case 13365: {
                player.getUpdateState().setAnimation(2108);
                return;
            }
            case 13366: {
                player.getUpdateState().setAnimation(2109);
                return;
            }
            case 13367: {
                player.getUpdateState().setAnimation(2110);
                return;
            }
            case 13368: {
                player.getUpdateState().setAnimation(2111);
                return;
            }
            case 13369: {
                player.getUpdateState().setAnimation(2112);
                return;
            }
            case 13370: {
                player.getUpdateState().setAnimation(2113);
                return;
            }
            case 11100: {
                player.packetSender.lockPlayerForTicks(4);
                player.getUpdateState().setAnimation(1368);
                player.getUpdateState().setGraphic(574);
                return;
            }
            case 667: {
                player.getUpdateState().setAnimation(1131);
                return;
            }
            case 6503: {
                player.getUpdateState().setAnimation(1130);
                return;
            }
            case 6506: {
                player.getUpdateState().setAnimation(1129);
                return;
            }
            case 666: {
                player.getUpdateState().setAnimation(1128);
                return;
            }
            case 13383: {
                player.getUpdateState().setAnimation(2127);
                return;
            }
            case 13384: {
                player.getUpdateState().setAnimation(2128);
                return;
            }
            case 18691: {
                player.getUpdateState().setAnimation(2836);
                return;
            }
            case 18689: {
                player.getUpdateState().setAnimation(3544);
                return;
            }
            case 18688: {
                player.getUpdateState().setAnimation(3543);
                return;
            }
            case 18464: {
                player.getUpdateState().setAnimation(3544);
                return;
            }
            case 18465: {
                player.getUpdateState().setAnimation(3543);
                return;
            }
            case 15166: {
                player.getUpdateState().setAnimation(2836);
                return;
            }
            case 6020: {
                player.packetSender.closeInterfaces();
                player.getEquipmentManager().unequipSlot(12);
                return;
            }
            case 3546: {
                GameplayHelper.acceptTradeSecondScreen(player);
                return;
            }
            case 3420: {
                GameplayHelper.acceptTradeFirstScreen(player);
                return;
            }
            case 3651: {
                player.setAppearanceUpdateRequired(true);
                player.getUpdateState().setUpdateRequired(true);
                // Revision 443's native character-design Accept closes group 269
                // locally. Do not send a legacy-style close packet here: just
                // advance Tutorial Island and reassert the instruction chatbox.
                if (ServerSettings.clientBuild != 443) {
                    player.packetSender.closeInterfaces();
                }
                if (player.getQuestState(0) != 0) return;
                player.setQuestState(0, 2);
                player.getQuestManager().refreshQuestJournal();
                return;
            }
            case 2458: {
                if (!player.getRecentCombatTimer().hasElapsed()) {
                    player.packetSender.sendGameMessage("You have to wait 10 seconds after combat in order to logout.");
                    return;
                }
                if (player.isInDuelArena()) {
                    player.packetSender.sendGameMessage("You can't logout during a duel fight!");
                    return;
                }
                if (player.getSingleCombatTimer().hasElapsed() == false) return;
                player.packetSender.sendLogout();
                player.disconnect();
                return;
            }
        }
        if (RandomEventManager.handleLampButtonClick(player, buttonId) != false) return;
        if (player.handleSpecialAttackButton(buttonId) != false) return;
        // Dialogue option buttons must be consumed before unrelated button
        // systems. In particular, Tutorial Island's Yes/No choices previously
        // reached the gnome-glider handler first.
        if (player.getDialogueManager().handleOptionButton(buttonId) != false) return;
        if (TravelManager.handleGnomeGliderButton(player, buttonId) != false) return;
        if (CanoeTravelManager.handleBuildButton(player, buttonId) != false) return;
        MysticStaffEnchantment staffEnchantment = MysticStaffEnchantment.forButtonId(buttonId);
        if (staffEnchantment != null) {
            if (!player.getInventoryManager().containsItem(staffEnchantment.getBattlestaffItemId())) {
                player.packetSender.sendGameMessage("You need a battlestaff to do this!");
                return;
            }
            if (!player.getInventoryManager().containsItemAmount(995, 40000)) {
                player.packetSender.sendGameMessage("You don't have enough coins with you!");
                return;
            }
            player.getInventoryManager().removeItem(new ItemStack(staffEnchantment.getBattlestaffItemId(), 1));
            player.getInventoryManager().removeItem(new ItemStack(995, 40000));
            player.getInventoryManager().addItem(new ItemStack(staffEnchantment.getMysticStaffItemId(), 1));
            player.packetSender.sendGameMessage("Thormac enchants your staff into a mystic staff.");
            player.packetSender.closeInterfaces();
            return;
        }
        if (CanoeTravelManager.handleDestinationButton(player, buttonId) != false) return;
        if (player.getSkillGuideManager().handleButtonClick(buttonId) != false) return;
        if (player.getSandwichLadyManager().handleButtonClick(buttonId) != false) return;
        if (GameplayHelper.handleSextantButtonClick(player, buttonId) != false) return;
        if (player.interfaceAction != "tanning") {
            SpellDefinition spell = (SpellDefinition)player.getSpellbook().getSpellByButtonId().get(buttonId);
            if (GameplayTrace.enabled() && spell != null) {
                GameplayTrace.log("button spell resolved player=" + GameplayTrace.describe(player)
                    + " buttonId=" + buttonId
                    + " spell=" + spell);
            }
            if (spell != null) {
                MagicSpellAction.castSelfSpell(player, spell);
                return;
            }
        }
        if (player.getBankPinManager().handleButtonClick(buttonId) != false) return;
        if (player.getEmoteManager().handleButtonClick(buttonId) != false) return;
        if (player.interfaceAction == "tanning" && buttonId == 7454) {
            player.packetSender.sendEnterInputPrompt(buttonId);
            return;
        }
        switch (buttonId) {
            case 1748: 
            case 2414: 
            case 3988: 
            case 3992: 
            case 3996: 
            case 4000: 
            case 4158: 
            case 6200: 
            case 7442: 
            case 7447: 
            case 8687: 
            case 8691: 
            case 8696: 
            case 8701: 
            case 8706: 
            case 8711: 
            case 8871: 
            case 8875: 
            case 8886: 
            case 8890: 
            case 8894: 
            case 8906: 
            case 8910: 
            case 8914: 
            case 8918: 
            case 8946: 
            case 8950: 
            case 8954: 
            case 8958: 
            case 8962: 
            case 11471: 
            case 11475: 
            case 12397: 
            case 12401: 
            case 12405: 
            case 12409: 
            case 13718: 
            case 14801: 
            case 14802: 
            case 14803: 
            case 14804: 
            case 14805: 
            case 14806: 
            case 14807: 
            case 14808: {
                player.packetSender.sendEnterInputPrompt(buttonId);
                return;
            }
            default: {
                break;
            }
        }
        if (DairyChurnHandler.handleButtonClick(player, buttonId) != false) return;
        if (CraftingHandler.handlePotteryButton(player, buttonId, 0) != false) return;
        if (CraftingHandler.handleSilverCraftingButton(player, buttonId, 0) != false) return;
        if (CraftingHandler.handleSpinningButton(player, buttonId, 0) != false) return;
        if (CraftingHandler.handleGlassblowingButton(player, buttonId, 0) != false) return;
        if (GameplayHelper.handleCraftedArmorButton(player, buttonId, 0) != false) return;
        if (player.interfaceAction == "tanning") {
            switch (buttonId) {
                case 7440: 
                case 8689: 
                case 14817: {
                    GameplayHelper.tanHide(player, 1, 1, 1739, 1741);
                    return;
                }
                case 7441: 
                case 8688: 
                case 14809: {
                    GameplayHelper.tanHide(player, 5, 1, 1739, 1741);
                    return;
                }
                case 7443: 
                case 8686: 
                case 14793: {
                    GameplayHelper.tanHide(player, player.getInventoryManager().getContainer().getItemAmount(1739), 1, 1739, 1741);
                    return;
                }
                case 7452: 
                case 8693: 
                case 14818: {
                    GameplayHelper.tanHide(player, 1, 3, 1739, 1743);
                    return;
                }
                case 7453: 
                case 8692: 
                case 14810: {
                    GameplayHelper.tanHide(player, 5, 3, 1739, 1743);
                    return;
                }
                case 7455: 
                case 8690: 
                case 14794: {
                    GameplayHelper.tanHide(player, player.getInventoryManager().getContainer().getItemAmount(1739), 3, 1739, 1743);
                    return;
                }
                case 14819: {
                    GameplayHelper.tanHide(player, 1, 20, 6287, 6289);
                    return;
                }
                case 14811: {
                    GameplayHelper.tanHide(player, 5, 20, 6287, 6289);
                    return;
                }
                case 14795: {
                    GameplayHelper.tanHide(player, player.getInventoryManager().getContainer().getItemAmount(6287), 20, 6287, 6289);
                    return;
                }
                case 8698: 
                case 14821: {
                    GameplayHelper.tanHide(player, 1, 20, 1753, 1745);
                    return;
                }
                case 8697: 
                case 14813: {
                    GameplayHelper.tanHide(player, 5, 20, 1753, 1745);
                    return;
                }
                case 8695: 
                case 14797: {
                    GameplayHelper.tanHide(player, player.getInventoryManager().getContainer().getItemAmount(1753), 20, 1753, 1745);
                    return;
                }
                case 8703: 
                case 14822: {
                    GameplayHelper.tanHide(player, 1, 20, 1751, 2505);
                    return;
                }
                case 8702: 
                case 14814: {
                    GameplayHelper.tanHide(player, 5, 20, 1751, 2505);
                    return;
                }
                case 8700: 
                case 14798: {
                    GameplayHelper.tanHide(player, player.getInventoryManager().getContainer().getItemAmount(1751), 20, 1751, 2505);
                    return;
                }
                case 8708: 
                case 14823: {
                    GameplayHelper.tanHide(player, 1, 20, 1749, 2507);
                    return;
                }
                case 8707: 
                case 14815: {
                    GameplayHelper.tanHide(player, 5, 20, 1749, 2507);
                    return;
                }
                case 8705: 
                case 14799: {
                    GameplayHelper.tanHide(player, player.getInventoryManager().getContainer().getItemAmount(1749), 20, 1749, 2507);
                    return;
                }
                case 8713: 
                case 14824: {
                    GameplayHelper.tanHide(player, 1, 20, 1747, 2509);
                    return;
                }
                case 8712: 
                case 14816: {
                    GameplayHelper.tanHide(player, 5, 20, 1747, 2509);
                    return;
                }
                case 8710: 
                case 14800: {
                    GameplayHelper.tanHide(player, player.getInventoryManager().getContainer().getItemAmount(1747), 20, 1747, 2509);
                    return;
                }
            }
        }
        if (CraftingHandler.handleWeavingButton(player, buttonId, 0) != false) return;
        if (CraftingHandler.handleDramenStaffButton(player, buttonId, 0) != false) return;
        if (EntityUpdateState.handleOrbChargeButton(player, buttonId, 0) != false) return;
        if (CacheArchive.handleLogCuttingButton(player, buttonId, 0) != false) return;
        if (CookingManager.handleCookingButton(player, buttonId) != false) return;
        if (GameplayHelper.handleFlourDoughButton(player, buttonId) != false) return;
        if (SmeltingHandler.handleSmeltingButton(player, buttonId, 0) != false) return;
        if (player.getDialogueManager().handleOptionButton(buttonId) != false) return;

        if (player.isInteractionDebugEnabled()) {
            int parentInterfaceId = interfaceDefinition == null
                    ? -1 : interfaceDefinition.getParentInterfaceId();
            String debugMessage = "Unhandled button: " + buttonId
                    + " (parent=" + parentInterfaceId + ", open=true)";
            player.packetSender.sendGameMessage(debugMessage);
            System.out.println("[button-debug] " + player.getUsername()
                    + " " + debugMessage);
        }

        // Preserve the old global staff/server debug logging as well.
        if (player.getPlayerRights() <= 1) return;
        if (ServerSettings.debugModeEnabled == false) return;
        System.out.println("button " + buttonId + " doesn't do anything");
    }
}

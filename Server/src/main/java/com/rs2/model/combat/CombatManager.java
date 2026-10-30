package com.rs2.model.combat;

import com.rs2.ServerSettings;
import com.rs2.bot.ClanWarsBotManager;
import com.rs2.bot.combat.BotCombatHelper;
import com.rs2.model.Entity;
import com.rs2.model.EntityTargetMovement;
import com.rs2.model.GameplayHelper;
import com.rs2.model.World;
import com.rs2.model.clue.TreasureTrailManager;
import com.rs2.model.combat.AttackBonusType;
import com.rs2.model.combat.AttackStyleDefinition;
import com.rs2.model.combat.AttackXpMode;
import com.rs2.model.combat.CombatAction;
import com.rs2.model.combat.CombatCycleEvent;
import com.rs2.model.combat.CombatType;
import com.rs2.model.combat.DeathAnimationTask;
import com.rs2.model.combat.DeathCleanupTask;
import com.rs2.model.combat.KalphiteQueenRespawnCombatEvent;
import com.rs2.model.combat.NpcRespawnCombatEvent;
import com.rs2.model.combat.attack.CombatAttackState;
import com.rs2.model.combat.attack.WeaponCombatAttack;
import com.rs2.model.combat.hit.HitDefinition;
import com.rs2.model.gameplay.barrows.BarrowsManager;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.player.Player;
import com.rs2.model.randomevent.RandomEventManager;
import com.rs2.model.skill.fishing.FishingSpotManager;
import com.rs2.model.skill.magic.SpellDefinition;
import com.rs2.model.skill.magic.TeleportManager;
import com.rs2.model.skill.prayer.PrayerManager;
import com.rs2.model.skill.smithing.SmithingHandler;
import com.rs2.model.task.CycleEvent;
import com.rs2.model.task.CycleEventHandler;
import com.rs2.model.task.TickTask;
import com.rs2.util.GameUtil;
import com.rs2.util.GameplayTrace;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public final class CombatManager
extends TickTask {
    private static CombatManager instance;
    private final List pendingActions = new LinkedList();
    static Npc kalphiteQueenFirstForm;
    private static Random random;

    static {
        random = new Random(System.currentTimeMillis());
    }

    public CombatManager() {
        super(1);
        World.getTaskScheduler().schedule(this);
    }

    public static void startCombat(Entity entity, Entity entity2) {
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("combat start requested attacker=" + GameplayTrace.describe(entity) + " target=" + GameplayTrace.describe(entity2));
        }
        if (entity2.isNpc() && !((Npc)entity2).isActive() || entity2.isDead()) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("combat start blocked inactive-or-dead-target attacker=" + GameplayTrace.describe(entity) + " target=" + GameplayTrace.describe(entity2));
            }
            if (entity.isPlayer()) {
                Player player = (Player)entity;
                if (player.currentBotTask != null) {
                    player.interactWithBotNpcTargets(player.botInteractionTargetIds);
                }
            }
            return;
        }
        if (entity2.getMaxHitpoints() <= 0 || entity2.isNpc() && (((Npc)entity2).getNpcId() == 411 || RandomEventManager.isRandomEventCombatExcludedNpcId(((Npc)entity2).getNpcId()))) {
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("combat start blocked invalid-target attacker=" + GameplayTrace.describe(entity) + " target=" + GameplayTrace.describe(entity2));
            }
            // The swarm has no Attack option in revision 443, so the only way here is auto retaliate: stay silent.
            if (entity.isPlayer() && !(entity2.isNpc() && ((Npc)entity2).getNpcId() == 411)) {
                Player player = (Player)entity;
                player.packetSender.sendGameMessage("You cannot attack this npc.");
            }
            CombatManager.stopCombat(entity);
            return;
        }
        if (entity2.isPlayer() && entity.isNpc()) {
            Npc npc = (Npc)entity;
            Player player = (Player)entity2;
            if (npc.getNpcId() == 1456 && player.actionSucceeded) {
                CombatManager.stopCombat(entity);
                return;
            }
        }
        if (entity2.isPlayer() && entity.isNpc() && ((Npc)entity).getNpcId() == 745) {
            CombatManager.stopCombat(entity);
            return;
        }
        if (entity.isPlayer() && entity2.isNpc() && !((Player)entity).getQuestManager().canAttackNpc(((Npc)entity2).getNpcId())) {
            Player player = (Player)entity;
            player.packetSender.sendGameMessage("You cannot attack this npc.");
            CombatManager.stopCombat(entity);
            return;
        }
        if (entity.isPlayer() && entity.isInDuelArena() && !((Player)entity).getDuelSession().isActiveDuelStarted()) {
            CombatManager.stopCombat(entity);
            return;
        }
        // Christmas snowballs are projectiles only for the separate Gublinch Pelt action.
        // Remove them before selecting a normal NPC attack, so combat uses bare hands.
        if (entity.isPlayer() && entity2.isNpc()
                && !com.rs2.model.quest.event.GublinchChristmasEvent.isGublinch(((Npc)entity2).getNpcId())) {
            Player player = (Player)entity;
            if (player.getEquipmentManager().getItemIdAtSlot(3)
                    == com.rs2.model.quest.event.GublinchChristmasEvent.SNOWBALL) {
                player.getEquipmentManager().unequipSlot(3);
                if (player.getEquipmentManager().getItemIdAtSlot(3)
                        == com.rs2.model.quest.event.GublinchChristmasEvent.SNOWBALL) {
                    CombatManager.stopCombat(entity);
                    return;
                }
            }
        }
        List attackOptions = new LinkedList();
        int distance = GameUtil.getDistance(entity.getPosition(), entity2.getPosition());
        entity.collectCombatAttackOptions(attackOptions, entity2, distance);
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("combat start options attacker=" + GameplayTrace.describe(entity) + " target=" + GameplayTrace.describe(entity2) + " distance=" + distance + " options=" + attackOptions.size());
        }
        SmithingHandler selectedAttackOption = null;
        Iterator iterator = attackOptions.iterator();
        while (iterator.hasNext()) {
            SmithingHandler attackOption = (SmithingHandler)iterator.next();
            if (attackOption.getState() == CombatAttackState.a || selectedAttackOption != null && attackOption.getAttack().getAttackRange() <= selectedAttackOption.getAttack().getAttackRange()) continue;
            selectedAttackOption = attackOption;
        }
        int attack = selectedAttackOption == null ? 1 : selectedAttackOption.getAttack().getAttackRange();
        entity.setAttackRange(attack);
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("combat start selected attacker=" + GameplayTrace.describe(entity) + " target=" + GameplayTrace.describe(entity2) + " selected=" + (selectedAttackOption == null ? "null" : selectedAttackOption.getAttack().getCombatType() + "/range=" + selectedAttackOption.getAttack().getAttackRange() + "/state=" + selectedAttackOption.getState()) + " attackRange=" + attack);
        }
        if (entity.isPlayer() && ((Player)entity).getUsername().toLowerCase().equals("mod test")) {
            System.out.println("check attack");
        }
        Entity target = entity2;
        Entity attacker = entity;
        boolean shouldStartCombatCycle = true;
        if (attacker.isPlayer()) {
            Player player = (Player)attacker;
            if (player.botCombatEscapeActive || player.currentBotTask != null && !player.botTaskState.equals("do task")) {
                shouldStartCombatCycle = false;
            }
        }
        if (shouldStartCombatCycle) {
            CombatCycleEvent combatCycleEvent = new CombatCycleEvent(attacker, target);
            attacker.setCombatTarget(target);
            attacker.getUpdateState().setFaceEntity(target.getEncodedIndex());
            attacker.getUpdateState().setFacePosition(target.getPosition());
            attacker.setMovementTarget(target);
            attacker.setActiveCycleEvent(combatCycleEvent);
            if (attacker.isPlayer() && ((Player)attacker).getUsername().toLowerCase().equals("mod test")) {
                System.out.println("check start combat");
            }
            if (attacker.isNpc()) {
                Npc attackerNpc = (Npc)attacker;
                if (attackerNpc.getTransformTicksRemaining() <= 0 && attackerNpc.getCombatTransformNpcId() > 0) {
                    attackerNpc.transformToNpcId(attackerNpc.getCombatTransformNpcId(), 999999);
                }
            }
            if (attacker.isPlayer()) {
                Player player = (Player)attacker;
                if (player.npcTransformationId == 2626 || player.npcTransformationId >= 3689 && player.npcTransformationId <= 3694) {
                    player.npcTransformationId = -1;
                    player.packetSender.refreshSidebarInterfaces();
                    player.setAppearanceUpdateRequired(true);
                }
            }
            if (GameplayTrace.enabled()) {
                GameplayTrace.log("combat cycle scheduled attacker=" + GameplayTrace.describe(attacker) + " target=" + GameplayTrace.describe(target));
            }
            CycleEventHandler.getInstance().schedule(attacker, attacker.getActiveCycleEvent(), 1);
        } else if (GameplayTrace.enabled()) {
            GameplayTrace.log("combat cycle not-scheduled attacker=" + GameplayTrace.describe(attacker) + " target=" + GameplayTrace.describe(target));
        }
        if (entity.isPlayer()) {
            showTutorialCombatInstructions((Player)entity);
        }
    }

    static void showTutorialCombatInstructions(Player player) {
        if (player.getQuestState(0) == 47) {
            player.getDialogueManager().showTutorialInstructionOverlay("Sit back and watch.", "While you are fighting you will see a bar over your head. The", "bar shows how much health you have left. Your opponent will", "have one too. You will continue to attack the rat until it's dead", "or you do something else.", true);
        } else if (player.getQuestState(0) == 49) {
            player.getDialogueManager().showTutorialInstructionOverlay("Rat ranging.", "Now you have a bow and some arrows. Before you can use", "them you'll need to equip them. Once equipped with the", "ranging gear, try killing another rat. Remember: to attack, right", "click on the monster and select attack.", true);
        }
    }

    private static boolean handleMageArenaNpcDeath(Entity entity, Entity entity2) {
        if (entity2.isNpc()) {
            entity = (Player)entity;
            if (((Npc)(entity2 = (Npc)entity2)).getNpcId() == 911) {
                entity2.setDead(true);
                CombatManager.handleDeath(entity2);
                ((Player)entity).getTeleportManager().startStandardTeleport(2541, 4717, 0, null);
                ((Player)entity).mageArenaProgressStage = 5;
                return true;
            }
        }
        return false;
    }

    @Override
    public final void execute() {
        try {
            List pendingActionsSnapshot = new LinkedList();
            pendingActionsSnapshot.addAll(this.pendingActions);
            this.pendingActions.clear();
            Iterator iterator = pendingActionsSnapshot.iterator();
            while (iterator.hasNext()) {
                CombatAction combatAction = (CombatAction)iterator.next();
                combatAction.tickDelay();
                if (combatAction.isReady()) {
                    if (!combatAction.getTarget().isDead()) {
                        combatAction.applyHit();
                    }
                    if (combatAction.getTarget().getCurrentHitpoints() > 0 || combatAction.getTarget().isDead()) continue;
                    if (combatAction.getAttacker() != null && combatAction.getTarget() != null && (combatAction.getAttacker().isPlayer() && combatAction.getTarget().isNpc() || combatAction.getAttacker().isNpc() && combatAction.getTarget().isPlayer())) {
                        boolean handledQuestDeath = combatAction.getAttacker().isPlayer() ? ((Player)combatAction.getAttacker()).getQuestManager().handleCombatDeath(combatAction.getAttacker(), combatAction.getTarget()) : ((Player)combatAction.getTarget()).getQuestManager().handleCombatDeath(combatAction.getAttacker(), combatAction.getTarget());
                        if (!handledQuestDeath) {
                            handledQuestDeath = CombatManager.handleMageArenaNpcDeath(combatAction.getAttacker(), combatAction.getTarget());
                        }
                        if (handledQuestDeath) {
                            return;
                        }
                    }
                    if (combatAction.getAttacker() != null && combatAction.getAttacker().isNpc()) {
                        Npc attackerNpc = (Npc)combatAction.getAttacker();
                        if (attackerNpc.questOwnerPlayer != null && attackerNpc.questOwnerPlayer.getQuestManager().handleCombatDeath(combatAction.getAttacker(), combatAction.getTarget())) {
                            return;
                        }
                    }
                    if (combatAction.getTarget() != null && combatAction.getTarget().isNpc()) {
                        Npc targetNpc = (Npc)combatAction.getTarget();
                        if (targetNpc.questOwnerPlayer != null && targetNpc.questOwnerPlayer.getQuestManager().handleCombatDeath(combatAction.getAttacker(), combatAction.getTarget())) {
                            return;
                        }
                    }
                    if (combatAction.getTarget() != null && combatAction.getTarget().isPlayer()) {
                        Player player = (Player)combatAction.getTarget();
                        if (player.godModeEnabled) {
                            player.setCurrentHitpoints(player.getMaxHitpoints());
                            return;
                        }
                    }
                    combatAction.getTarget().setDead(true);
                    CombatManager.handleDeath(combatAction.getTarget());
                    if (!combatAction.getTarget().isPlayer() || !combatAction.getTarget().isInDuelArena()) continue;
                    ((Player)combatAction.getTarget()).getDuelSession().getOpponent().getAttributes().put("canTakeDamage", false);
                    return;
                }
                if (combatAction.getTarget().isDead()) continue;
                this.pendingActions.add(combatAction);
            }
            return;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }

    public static void handleDeath(Entity entity) {
        int value;
        Entity entity2 = entity.getTopDamageContributor();
        ArrayList damageContributorList = entity.getDamageContributorList();
        if (damageContributorList.size() > 0) {
            Iterator iterator = damageContributorList.iterator();
            while (iterator.hasNext()) {
                Entity contributor = (Entity)iterator.next();
                if (entity2 == contributor || contributor.getCombatTarget() != entity || !contributor.isPlayer()) continue;
                Player player = (Player)contributor;
                if (!player.botEnabled || player.currentBotTask == null) continue;
                player.interactWithBotNpcTargets(player.botInteractionTargetIds);
            }
        }
        if (entity != null && entity2 != null && entity.isNpc() && entity2.isPlayer()) {
            Npc deadNpc = (Npc)entity;
            Player[] playerArray = World.getPlayers();
            int length = playerArray.length;
            int index = 0;
            while (index < length) {
                Player player = playerArray[index];
                if (player != null) {
                    if (player.hintedNpcIndex == deadNpc.getIndex() && deadNpc.getOwnerPlayer() == null) {
                        player.packetSender.sendEntityHintIcon(1, Npc.findByDefinitionId(deadNpc.getNpcId()).getIndex());
                    }
                    if (player.hintedNpcIndex == deadNpc.getIndex() && deadNpc.getOwnerPlayer() == player) {
                        player.packetSender.sendEntityHintIcon(1, -1);
                    }
                }
                ++index;
            }
        }
        entity.nextActionSequence();
        entity.setDeathPosition(entity.getPosition().copy());
        if (entity2 != null && entity2.isPlayer()) {
            Player killerPlayer = (Player)entity2;
            killerPlayer.getSingleCombatTimer().setDelayTicks(0);
            killerPlayer.getSingleCombatTimer().reset();
            if (killerPlayer.getQuestState(0) == 47 || killerPlayer.getQuestState(0) == 49) {
                killerPlayer.advanceTutorialStage();
            }
        }
        if (entity.isPlayer()) {
            Player deadPlayer = (Player)entity;
            deadPlayer.setActionLocked(true);
            if (deadPlayer.botEnabled && deadPlayer.currentBotTask == null && !deadPlayer.clanWarsBot) {
                String[] deathMessages = new String[]{"gf", "fuck", "shit", "damn"};
                deadPlayer.queuePublicChatMessage(deathMessages[GameUtil.randomInt(4)]);
            }
        }
        if (entity2 != null && entity2.isPlayer()) {
            Player killerPlayer = (Player)entity2;
            if (killerPlayer.botEnabled) {
                if (killerPlayer.getCombatTarget() == entity) {
                    killerPlayer.botCombatState = "wait for loot";
                }
                if (entity.isPlayer() && killerPlayer.currentGroup == null) {
                    if (killerPlayer.clanWarsBot) {
                        if (killerPlayer.clanWarsTeamId == 1) {
                            killerPlayer.queuePublicChatMessage("#" + ClanWarsBotManager.clanWarsTeamOneTag);
                        } else {
                            killerPlayer.queuePublicChatMessage("#" + ClanWarsBotManager.clanWarsTeamTwoTag);
                        }
                    } else {
                        killerPlayer.queuePublicChatMessage("gf");
                    }
                }
            }
        }
        if ((value = entity.getDeathAnimationId()) != -1) {
            World.getTaskScheduler().schedule(new DeathAnimationTask(2, value, entity, entity2));
        }
        World.getTaskScheduler().schedule(new DeathCleanupTask(entity.getDeathDelayTicks(), entity, entity2));
        entity.nextActionSequence();
        entity.clearCombatEffectTasks();
        entity.getRecentCombatTimer().setDelayTicks(entity.getDeathDelayTicks() + 2);
        entity.getRecentCombatTimer().reset();
        entity.getSingleCombatTimer().setDelayTicks(entity.getDeathDelayTicks() + 2);
        entity.getSingleCombatTimer().reset();
        if (entity2 != null && entity.isPlayer() && ((Player)entity).getActivePrayers()[15]) {
            PrayerManager.triggerRetribution(entity, entity2);
        }
    }

    public static void finishDeath(Entity entity, Entity killer, boolean dropItems) {
        boolean castleWarsDeath = entity.isPlayer() && CastleWarsManager.isInGame((Player)entity);
        SpellDefinition retainedAutocastSpell = null;
        if (castleWarsDeath) {
            Player player = (Player)entity;
            if (player.isAutocastEnabled() && player.getAutocastSpell() != null) {
                retainedAutocastSpell = player.getAutocastSpell();
            }
        }
        if (castleWarsDeath) {
            dropItems = false;
            if (entity.getPoisonDamage() > 0.0) {
                entity.setPoisonDamage(0.0);
            }
            entity.setDeathPosition(null);
        }
        if (dropItems) {
            entity.dropDeathItems(killer);
            if (entity.getPoisonDamage() > 0.0) {
                entity.setPoisonDamage(0.0);
            }
            entity.setDeathPosition(null);
            if (killer != null && killer.isPlayer() && entity.isNpc()) {
                Npc killedNpc = (Npc)entity;
                Player killerPlayer = (Player)killer;
                int bossPetFlags = killerPlayer.getBossPetUnlockFlags();
                ++killerPlayer.npcKillCount;
                if (killedNpc.getNpcId() == 50 && (bossPetFlags & 1) == 0) {
                    killerPlayer.setBossPetUnlockFlags(bossPetFlags + 1);
                }
                if (killedNpc.getNpcId() == 1160 && (bossPetFlags & 2) == 0) {
                    killerPlayer.setBossPetUnlockFlags(bossPetFlags + 2);
                }
                if (killedNpc.getNpcId() == 3200 && (bossPetFlags & 4) == 0) {
                    killerPlayer.setBossPetUnlockFlags(bossPetFlags + 4);
                }
                if (killedNpc.getNpcId() == 2745 && (bossPetFlags & 8) == 0) {
                    killerPlayer.setBossPetUnlockFlags(bossPetFlags + 8);
                }
                TreasureTrailManager.recordClueWizardKill(killerPlayer, killedNpc);
                killerPlayer.getSlayerManager().recordKill(killedNpc);
                killerPlayer.getPrayerManager().awardNpcPrayerExperience(killedNpc);
                BarrowsManager.recordNpcKill(killerPlayer, killedNpc);
            }
        }
        if (entity.isNpc()) {
            Npc npc = (Npc)entity;
            if (killer != null && killer.isPlayer() && entity.isNpc()) {
                if (entity.isDoorSupportNpc()) {
                    new DynamicObject(ServerSettings.placeholderObjectId, entity.getPosition().getX(), entity.getPosition().getY(), entity.getPosition().getPlane(), 0, 10, 8967, 60);
                }
                ((Player)killer).getQuestManager().handleNpcKill(npc.getNpcId());
                Player player = (Player)killer;
                if (npc.isInFightCave() && player.isInFightCave()) {
                    boolean removedFightCaveNpc = false;
                    boolean killedJad = false;
                    if (npc.getNpcId() == 2745) {
                        killedJad = true;
                    }
                    for (Object fightCaveNpcObject : player.getFightCaveNpcs()) {
                        Npc fightCaveNpc = (Npc)fightCaveNpcObject;
                        if (npc != fightCaveNpc) continue;
                        player.removeFightCaveNpc(npc);
                        removedFightCaveNpc = true;
                        break;
                    }
                    if (removedFightCaveNpc && player.getFightCaveNpcs().isEmpty()) {
                        if (!killedJad) {
                            player.getFightCaveController().startNextWave();
                        } else {
                            player.getFightCaveController().completeFightCave();
                        }
                    }
                } else {
                    for (Object fightCaveNpcObject : player.getFightCaveNpcs()) {
                        Npc fightCaveNpc = (Npc)fightCaveNpcObject;
                        if (npc != fightCaveNpc) continue;
                        System.out.println("FIGHT CAVE! Not in area?");
                        break;
                    }
                }
                if (npc.isNpc()) {
                    if (npc.getNpcId() == 907) {
                        npc.setDead(true);
                        Npc nextNpc = new Npc(908);
                        GameplayHelper.spawnOwnedNpcAtPosition(player, npc.getPosition(), nextNpc, true, false);
                        nextNpc.getUpdateState().setForcedText("This is only the beginning; you can't beat me!");
                        player.mageArenaProgressStage = 1;
                    } else if (npc.getNpcId() == 908) {
                        npc.setDead(true);
                        Npc nextNpc = new Npc(909);
                        GameplayHelper.spawnOwnedNpcAtPosition(player, npc.getPosition(), nextNpc, true, false);
                        nextNpc.getUpdateState().setForcedText("Foolish mortal; I am unstoppable.");
                        player.mageArenaProgressStage = 2;
                    } else if (npc.getNpcId() == 909) {
                        npc.setDead(true);
                        Npc nextNpc = new Npc(910);
                        GameplayHelper.spawnOwnedNpcAtPosition(player, npc.getPosition(), nextNpc, true, false);
                        nextNpc.getUpdateState().setForcedText("Now you feel it... The dark energy.");
                        player.mageArenaProgressStage = 3;
                    } else if (npc.getNpcId() == 910) {
                        npc.setDead(true);
                        Npc nextNpc = new Npc(911);
                        GameplayHelper.spawnOwnedNpcAtPosition(player, npc.getPosition(), nextNpc, true, false);
                        nextNpc.getUpdateState().setForcedText("Aaaaaaaarrgghhhh! The power!");
                        player.mageArenaProgressStage = 4;
                    }
                }
            }
            if (!npc.isRespawnEnabled()) {
                if (npc.getNpcId() == 1160 && kalphiteQueenFirstForm != null) {
                    CycleEventHandler.getInstance().schedule(kalphiteQueenFirstForm, new KalphiteQueenRespawnCombatEvent(killer), kalphiteQueenFirstForm.getRespawnDelayTicks());
                }
                npc.setActive(false);
                World.unregisterNpc(npc);
            } else {
                if (npc.isActive()) {
                    npc.setActive(false);
                    npc.setPosition(npc.getSpawnPosition().copy());
                    npc.getMovementQueue().clear();
                    npc.transformToNpcId(npc.getOriginalNpcId(), 0);
                    CombatManager.stopCombat(npc);
                    if (npc.getNpcId() != 1158) {
                        CycleEventHandler.getInstance().schedule(npc, new NpcRespawnCombatEvent(npc, killer), npc.getRespawnDelayTicks());
                        return;
                    }
                    kalphiteQueenFirstForm = npc;
                    Npc firstForm = kalphiteQueenFirstForm;
                    Npc secondForm = new Npc(1160);
                    secondForm.setPosition(firstForm.getPosition());
                    secondForm.setSpawnPosition(firstForm.getSpawnPosition());
                    secondForm.setSpawnMinPosition(firstForm.getSpawnMinPosition());
                    secondForm.setSpawnMaxPosition(firstForm.getSpawnMaxPosition());
                    secondForm.setMovementMode(firstForm.getMovementMode());
                    secondForm.setFacingDirection(firstForm.getFacingDirection());
                    secondForm.setSpawnX(firstForm.getSpawnX());
                    secondForm.setSpawnY(firstForm.getSpawnY());
                    secondForm.setRespawnEnabled(false);
                    World.registerNpc(secondForm);
                    secondForm.getUpdateState().setAnimation(1181);
                    return;
                }
                npc.setActive(true);
                if (npc.getDefinition().getName().toLowerCase().equals("fishing spot") && (Npc)FishingSpotManager.activeSpotsByPosition.get(npc.getPosition()) == null) {
                    FishingSpotManager.activeSpotsByPosition.put(npc.getPosition(), npc);
                }
            }
        }
        entity.getUpdateState().setFaceEntity(-1);
        entity.setDead(false);
        if (entity.isNpc() && ((Npc)entity).getOwnerPlayer() != null) {
            entity.setDead(true);
        }
        entity.setCurrentHitpoints(entity.getMaxHitpoints());
        if (entity.isNpc()) {
            Npc npc = (Npc)entity;
            npc.resetCombatLevels();
        }
        entity.getUpdateState().setAnimation(65535, 0);
        entity.clearCombatEffectTasks();
        if (killer != null && killer.isPlayer() && entity.isPlayer() && killer != entity) {
            Player killerPlayer = (Player)killer;
            Player deadPlayer = (Player)entity;
            if (!entity.isInDuelArena()) {
                ++killerPlayer.playerKillCount;
            }
            killerPlayer.packetSender.sendGameMessage("You have defeated " + GameUtil.formatDisplayName(deadPlayer.getUsername()) + ".");
            if (killerPlayer.botEnabled) {
                BotCombatHelper.stopBotCombatTick(killerPlayer);
            }
        }
        if (entity.isPlayer()) {
            Player player = (Player)entity;
            player.setActionLocked(false);
            player.setHideHeldItemsInAppearance(false);
            player.resetCombatState();
            player.getSkillManager().refreshAllSkills();

            // Castle Wars respawns keep the player's selected autocast spell.
            // Deaths everywhere else retain the existing behaviour and clear
            // autocast normally.
            if (castleWarsDeath && retainedAutocastSpell != null) {
                player.setAutocastSpell(retainedAutocastSpell);
            } else if (!castleWarsDeath) {
                player.setAutocastSpell(null);
            }
        }
        entity.getDamageContributions().clear();
        if (entity.isPlayer() && entity.isInDuelArena()) {
            ((Player)entity).getDuelSession().finishDuelLoss(false);
            return;
        }
        if (entity.isPlayer() && entity.isInFightCave()) {
            ((Player)entity).getFightCaveController().handleDeath();
            return;
        }
        if (entity.isPlayer() && ((Player)entity).getCreatureGraveyardController().isInsideGraveyard()) {
            ((Player)entity).getCreatureGraveyardController().handleGraveyardDeath();
            return;
        }
        if (entity.isPlayer()) {
            Player player = (Player)entity;
            if (CastleWarsManager.isInGame(player)) {
                CastleWarsManager.respawnPlayer(player);
                player.getRecentCombatTimer().setDelayTicks(0);
                player.getRecentCombatTimer().reset();
                player.getSingleCombatTimer().setDelayTicks(0);
                player.getSingleCombatTimer().reset();
                return;
            }
            if (player.isInTenthSquadSigilInstance()) {
                player.clearTemporaryCutsceneNpcs();
            }
            player.moveTo(TeleportManager.RESPAWN_TELEPORT_POSITION);
            player.packetSender.sendGameMessage("Oh dear, you are dead!");
            ++player.deathCount;
            player.packetSender.sendMusicJingle(203, 256);
            player.getRecentCombatTimer().setDelayTicks(0);
            player.getRecentCombatTimer().reset();
            player.getSingleCombatTimer().setDelayTicks(0);
            player.getSingleCombatTimer().reset();
            if (player.botEnabled) {
                player.botLootSellItems.clear();
                player.botCombatState = "died";
            }
        }
    }

    public static void initialize() {
        instance = new CombatManager();
    }

    public static CombatManager getInstance() {
        return instance;
    }

    public final void queueAction(CombatAction combatAction) {
        if (GameplayTrace.enabled()) {
            GameplayTrace.log("combat manager queue-action attacker=" + GameplayTrace.describe(combatAction.getAttacker()) + " target=" + GameplayTrace.describe(combatAction.getTarget()) + " damage=" + combatAction.getDamage());
        }
        this.pendingActions.add(combatAction);
    }

    public static double calculateWeaponMaxHit(Player player, WeaponCombatAttack weaponCombatAttack) {
        AttackStyleDefinition attackStyleDefinition = weaponCombatAttack.getAttackStyle();
        if (attackStyleDefinition.getCombatType() == CombatType.MELEE) {
            return CombatManager.calculateMeleeMaxHit(player, weaponCombatAttack);
        }
        if (attackStyleDefinition.getCombatType() == CombatType.RANGED && weaponCombatAttack.getAmmunition() != null) {
            if (ServerSettings.modernCombatSystemEnabled) {
                double rangedLevel = player.getSkillManager().getCurrentLevels()[4];
                int effectiveRangedLevel = (int)Math.floor(rangedLevel * CombatManager.getPrayerMultiplier(player, 4));
                if (attackStyleDefinition.getXpMode() == AttackXpMode.RANGED_ACCURATE) {
                    effectiveRangedLevel += 3;
                }
                effectiveRangedLevel += 8;
                if (player.hasFullVoidRangedSet()) {
                    effectiveRangedLevel = (int)((double)effectiveRangedLevel * 1.1);
                }
                effectiveRangedLevel = (int)Math.floor(effectiveRangedLevel);
                double rangedStrengthBonus = player.getCombatBonus(12);
                return (int)Math.floor(0.5 + (double)effectiveRangedLevel * (rangedStrengthBonus + 64.0) / 640.0);
            }
            int rangedLevel = (int)(player.getSkillManager().getCurrentLevels()[4]
                    * CombatManager.getPrayerMultiplier(player, 4));
            double styleBonus = 0.0;
            if (attackStyleDefinition.getXpMode() == AttackXpMode.RANGED_ACCURATE) {
                styleBonus = 3.0;
            } else if (attackStyleDefinition.getXpMode() == AttackXpMode.LONGRANGE) {
                styleBonus = 1.0;
            }
            rangedLevel = (int)((double)rangedLevel + styleBonus);
            double rangedStrength = weaponCombatAttack.getAmmunition().getRangedStrength();
            double maxHit = ((double)rangedLevel + rangedStrength / 8.0 + (double)rangedLevel * rangedStrength * Math.pow(64.0, -1.0) + 14.0) / 10.0;
            return (int)Math.floor(maxHit);
        }
        return 0.0;
    }

    public static double calculateMeleeMaxHit(Player player, WeaponCombatAttack weaponCombatAttack) {
        if (ServerSettings.modernCombatSystemEnabled) {
            return CombatManager.calculateMeleeMaxHit((Entity)player, weaponCombatAttack);
        }
        double strengthLevel = player.getSkillManager().getCurrentLevels()[2];
        if (player.getActivePrayers()[1]) {
            strengthLevel *= 1.05;
        } else if (player.getActivePrayers()[4]) {
            strengthLevel *= 1.1;
        } else if (player.getActivePrayers()[10]) {
            strengthLevel *= 1.15;
        }
        AttackStyleDefinition attackStyleDefinition = weaponCombatAttack.getAttackStyle();
        int styleBonus = 0;
        if (attackStyleDefinition.getXpMode() == AttackXpMode.AGGRESSIVE) {
            styleBonus = 3;
        } else if (attackStyleDefinition.getXpMode() == AttackXpMode.CONTROLLED) {
            styleBonus = 1;
        }
        int effectiveStrengthLevel = (int)(strengthLevel + (double)styleBonus);
        double maxHitRoll = 5 + (effectiveStrengthLevel + 8) * (player.getCombatBonus(10) + 64) / 64;
        int maxHit = (int)Math.floor(maxHitRoll);
        return (int)Math.floor(maxHit / 10);
    }

    public static double calculateHitChance(double value6, double value22) {
        if (ServerSettings.modernCombatSystemEnabled) {
            return value6 > value22 ? 1.0 - (value22 + 2.0) / (2.0 * (value6 + 1.0)) : value6 / (2.0 * (value22 + 1.0));
        }
        double value3 = Math.floor(value6);
        double value4 = Math.floor(value22);
        double value5 = value3 < value4 ? (value3 - 1.0) / (value4 * 2.0) : 1.0 - (value4 + 1.0) / (value3 * 2.0);
        value5 = value5 > 0.9999 ? 0.9999 : (value5 < 1.0E-4 ? 1.0E-4 : value5);
        return value5;
    }

    public static boolean rollAccuracy(double value2) {
        return random.nextDouble() <= value2;
    }

    public static double calculateDefenceRoll(Entity entity, HitDefinition hitDefinition) {
        AttackStyleDefinition attackStyleDefinition = hitDefinition.getAttackStyle();
        if (ServerSettings.modernCombatSystemEnabled) {
            double defenceRoll = 0.0;
            if (attackStyleDefinition.getCombatType() == CombatType.MELEE) {
                int effectiveDefenceLevel;
                if (entity.isPlayer()) {
                    Player player = (Player)entity;
                    effectiveDefenceLevel = (int)Math.floor((double)player.getSkillManager().getCurrentLevels()[1] * CombatManager.getPrayerMultiplier(player, 1));
                    AttackStyleDefinition playerAttackStyle = player.getWeaponProfile().getInterfaceDefinition().getAttackStyles()[player.getFightMode()];
                    if (playerAttackStyle.getXpMode() == AttackXpMode.DEFENSIVE) {
                        effectiveDefenceLevel += 3;
                    } else if (playerAttackStyle.getXpMode() == AttackXpMode.CONTROLLED) {
                        ++effectiveDefenceLevel;
                    }
                    effectiveDefenceLevel += 8;
                    effectiveDefenceLevel = (int)Math.floor(effectiveDefenceLevel);
                    int defenceBonus = player.getCombatBonus(attackStyleDefinition.getDefenseBonusIndex());
                    defenceRoll = (int)Math.floor((double)effectiveDefenceLevel * (double)(defenceBonus + 64));
                } else {
                    Npc npc = (Npc)entity;
                    effectiveDefenceLevel = npc.getCurrentDefenceLevel();
                    int defenceBonus = npc.getDefenceBonus(attackStyleDefinition.getDefenseBonusIndex());
                    defenceRoll = (effectiveDefenceLevel + 9) * (defenceBonus + 64);
                }
            } else if (attackStyleDefinition.getCombatType() == CombatType.RANGED) {
                int defenceLevel;
                int defenceBonus;
                if (entity.isPlayer()) {
                    Player player = (Player)entity;
                    defenceLevel = player.getSkillManager().getCurrentLevels()[1];
                    defenceBonus = player.getCombatBonus(9);
                } else {
                    Npc npc = (Npc)entity;
                    defenceLevel = npc.getCurrentDefenceLevel();
                    defenceBonus = npc.getDefenceBonus(attackStyleDefinition.getDefenseBonusIndex());
                }
                defenceRoll = (defenceLevel + 9) * (defenceBonus + 64);
            } else if (attackStyleDefinition.getCombatType() == CombatType.MAGIC) {
                int magicLevel;
                int defenceBonus;
                if (entity.isPlayer()) {
                    Player player = (Player)entity;
                    magicLevel = player.getSkillManager().getCurrentLevels()[6];
                    defenceBonus = player.getCombatBonus(8);
                } else {
                    Npc npc = (Npc)entity;
                    magicLevel = npc.getCurrentMagicLevel();
                    defenceBonus = npc.getDefenceBonus(attackStyleDefinition.getDefenseBonusIndex());
                }
                defenceRoll = (magicLevel + 9) * (defenceBonus + 64);
            }
            return (int)defenceRoll;
        }
        double defenceBonus = entity.getCombatBonus(attackStyleDefinition.getAttackBonusType().getIndex() + AttackBonusType.values().length);
        if (attackStyleDefinition.getXpMode() == AttackXpMode.SARADOMIN_SWORD) {
            defenceBonus = entity.getCombatBonus(AttackBonusType.MAGIC.getIndex() + AttackBonusType.values().length);
        }
        double defenceLevel = entity.getDefenceLevelFor(attackStyleDefinition.getCombatType());
        if (defenceBonus < 0.0) {
            defenceLevel /= 2.0;
            defenceBonus *= 3.0;
        }
        if (attackStyleDefinition.getCombatType() == CombatType.MELEE && entity.isPlayer()) {
            Player player = (Player)entity;
            if (player.getActivePrayers()[0]) {
                defenceLevel *= 1.05;
            } else if (player.getActivePrayers()[3]) {
                defenceLevel *= 1.1;
            } else if (player.getActivePrayers()[9]) {
                defenceLevel *= 1.15;
            }
        }
        double defenceRoll = Math.floor(defenceLevel + defenceBonus) + 8.0;
        if (entity.isPlayer()) {
            Player player = (Player)entity;
            if (attackStyleDefinition.getCombatType() == CombatType.MAGIC) {
                int magicLevel = player.getSkillManager().getCurrentLevels()[6];
                defenceRoll = (int)(Math.floor((double)magicLevel * 0.7) + Math.floor(defenceRoll * 0.3));
            }
        }
        if (hitDefinition.getSpecialEffectId() == 11) {
            defenceRoll *= 0.75;
        }
        if (entity.isNpc()) {
            if (attackStyleDefinition.getCombatType() == CombatType.MAGIC) {
                defenceRoll *= 0.7;
            }
            if (attackStyleDefinition.getCombatType() == CombatType.MELEE) {
                defenceRoll *= 0.6;
            }
        }
        return defenceRoll;
    }

    public static double calculateAttackRoll(Entity entity, HitDefinition hitDefinition) {
        Player player;
        AttackStyleDefinition attackStyleDefinition = hitDefinition.getAttackStyle();
        double accuracyMultiplier = hitDefinition.getAccuracyMultiplier();
        if (ServerSettings.modernCombatSystemEnabled) {
            double attackRoll = 0.0;
            if (attackStyleDefinition.getCombatType() == CombatType.MELEE) {
                int effectiveAttackLevel;
                if (entity.isPlayer()) {
                    Player attackingPlayer = (Player)entity;
                    effectiveAttackLevel = (int)Math.floor((double)attackingPlayer.getSkillManager().getCurrentLevels()[0] * CombatManager.getPrayerMultiplier(attackingPlayer, 0));
                    if (attackStyleDefinition.getXpMode() == AttackXpMode.MELEE_ACCURATE) {
                        effectiveAttackLevel += 3;
                    } else if (attackStyleDefinition.getXpMode() == AttackXpMode.CONTROLLED) {
                        ++effectiveAttackLevel;
                    }
                    effectiveAttackLevel += 8;
                    if (attackingPlayer.hasFullVoidMeleeSet()) {
                        effectiveAttackLevel = (int)((double)effectiveAttackLevel * 1.1);
                    }
                    effectiveAttackLevel = (int)Math.floor(effectiveAttackLevel);
                    int attackBonus = attackingPlayer.getCombatBonus(attackStyleDefinition.getAttackBonusIndex());
                    attackRoll = (int)Math.floor((double)effectiveAttackLevel * (double)(attackBonus + 64));
                } else {
                    Npc npc = (Npc)entity;
                    effectiveAttackLevel = (int)npc.getCurrentAttackLevel();
                    effectiveAttackLevel += 8;
                    effectiveAttackLevel = (int)Math.floor(effectiveAttackLevel);
                    int attackBonus = npc.getMeleeAttackBonus();
                    attackRoll = (int)Math.floor((double)effectiveAttackLevel * (double)(attackBonus + 64));
                }
            } else if (attackStyleDefinition.getCombatType() == CombatType.RANGED) {
                int effectiveRangedLevel;
                if (entity.isPlayer()) {
                    Player attackingPlayer = (Player)entity;
                    effectiveRangedLevel = (int)Math.floor((double)attackingPlayer.getSkillManager().getCurrentLevels()[4] * CombatManager.getPrayerMultiplier(attackingPlayer, 4));
                    if (attackStyleDefinition.getXpMode() == AttackXpMode.RANGED_ACCURATE) {
                        effectiveRangedLevel += 3;
                    }
                    effectiveRangedLevel += 8;
                    if (attackingPlayer.hasFullVoidRangedSet()) {
                        effectiveRangedLevel = (int)((double)effectiveRangedLevel * 1.1);
                    }
                    effectiveRangedLevel = (int)Math.floor(effectiveRangedLevel);
                    int attackBonus = attackingPlayer.getCombatBonus(4);
                    attackRoll = (int)Math.floor((double)effectiveRangedLevel * (double)(attackBonus + 64));
                } else {
                    Npc npc = (Npc)entity;
                    effectiveRangedLevel = (int)npc.getCurrentRangedLevel();
                    effectiveRangedLevel += 8;
                    effectiveRangedLevel = (int)Math.floor(effectiveRangedLevel);
                    int attackBonus = npc.getRangedAttackBonus();
                    attackRoll = (int)Math.floor((double)effectiveRangedLevel * (double)(attackBonus + 64));
                }
            } else if (attackStyleDefinition.getCombatType() == CombatType.MAGIC) {
                int effectiveMagicLevel;
                int attackBonus;
                if (entity.isPlayer()) {
                    Player attackingPlayer = (Player)entity;
                    effectiveMagicLevel = (int)Math.floor((double)attackingPlayer.getSkillManager().getCurrentLevels()[6] * CombatManager.getPrayerMultiplier(attackingPlayer, 6));
                    if (attackingPlayer.hasFullVoidMagicSet()) {
                        effectiveMagicLevel = (int)((double)effectiveMagicLevel * 1.45);
                    }
                    effectiveMagicLevel += 9;
                    effectiveMagicLevel = (int)Math.floor(effectiveMagicLevel);
                    attackBonus = attackingPlayer.getCombatBonus(3);
                } else {
                    Npc npc = (Npc)entity;
                    effectiveMagicLevel = (int)npc.getCurrentMagicLevel();
                    effectiveMagicLevel += 9;
                    effectiveMagicLevel = (int)Math.floor(effectiveMagicLevel);
                    attackBonus = npc.getMagicAttackBonus();
                }
                attackRoll = (int)Math.floor((double)effectiveMagicLevel * (double)(attackBonus + 64));
            }
            return (int)(attackRoll * accuracyMultiplier);
        }
        double attackBonus = entity.getCombatBonus(attackStyleDefinition.getAttackBonusType().getIndex());
        double attackLevel = entity.getAttackLevelFor(attackStyleDefinition.getCombatType());
        if (entity.isPlayer() && attackStyleDefinition.getCombatType() != CombatType.MELEE) {
            attackLevel *= CombatManager.getPrayerMultiplier((Player)entity,
                    attackStyleDefinition.getCombatType() == CombatType.RANGED ? 4 : 6);
        }
        if (attackBonus < 0.0) {
            attackLevel /= 2.0;
            attackBonus *= 3.0;
        }
        if (attackStyleDefinition.getCombatType() == CombatType.MELEE && entity.isPlayer()) {
            player = (Player)entity;
            if (player.getActivePrayers()[2]) {
                attackLevel *= 1.05;
            } else if (player.getActivePrayers()[5]) {
                attackLevel *= 1.1;
            } else if (player.getActivePrayers()[11]) {
                attackLevel *= 1.15;
            }
            if (player.hasFullVoidMeleeSet()) {
                attackLevel *= 1.1;
            }
        }
        if (attackStyleDefinition.getCombatType() == CombatType.RANGED && entity.isPlayer() && (player = (Player)entity).hasFullVoidRangedSet()) {
            attackLevel *= 1.1;
        }
        if (attackStyleDefinition.getCombatType() == CombatType.MAGIC && entity.isPlayer() && (player = (Player)entity).hasFullVoidMagicSet()) {
            attackLevel *= 1.45;
        }
        double attackRoll = Math.floor(attackLevel + attackBonus) + 8.0;
        return (int)(attackRoll * accuracyMultiplier);
    }

    public static void stopCombat(Entity entity) {
        if (entity.isPlayer()) {
            ((Player)entity).setQueuedCombatSpell(null);
            if (((Player)entity).botEnabled) {
                BotCombatHelper.stopBotCombatTick((Player)entity);
            }
        }
        entity.setCombatTarget(null);
        entity.setActiveCycleEvent(null);
        entity.getUpdateState().setFaceEntity(-1);
        EntityTargetMovement.clearMovementTarget(entity);
    }

    private static double getPrayerMultiplier(Player player, int prayerId) {
        double value = 1.0;
        if (prayerId == 4 || prayerId == 6) {
            int firstPrayer = prayerId == 4 ? 18 : 19;
            if (player.getActivePrayers()[firstPrayer + 4]) return 1.15;
            if (player.getActivePrayers()[firstPrayer + 2]) return 1.1;
            if (player.getActivePrayers()[firstPrayer]) return 1.05;
        }
        if (prayerId == 2) {
            if (player.getActivePrayers()[1]) {
                value = 1.05;
            } else if (player.getActivePrayers()[4]) {
                value = 1.1;
            } else if (player.getActivePrayers()[10]) {
                value = 1.15;
            }
        } else if (prayerId == 0) {
            if (player.getActivePrayers()[2]) {
                value = 1.05;
            } else if (player.getActivePrayers()[5]) {
                value = 1.1;
            } else if (player.getActivePrayers()[11]) {
                value = 1.15;
            }
        } else if (prayerId == 1) {
            if (player.getActivePrayers()[0]) {
                value = 1.05;
            } else if (player.getActivePrayers()[3]) {
                value = 1.1;
            } else if (player.getActivePrayers()[9]) {
                value = 1.15;
            }
        }
        return value;
    }

    public static int calculateMeleeMaxHit(Entity entity, WeaponCombatAttack weaponCombatAttack) {
        int value;
        int value2;
        Entity entity2;
        WeaponCombatAttack weaponCombatAttack2 = weaponCombatAttack;
        Entity entity3 = entity;
        if (entity3.isPlayer()) {
            entity2 = (Player)entity3;
            double skillManager = ((Player)entity2).getSkillManager().getCurrentLevels()[2];
            double prayerMultiplier = skillManager * CombatManager.getPrayerMultiplier((Player)entity2, 2);
            value2 = (int)Math.floor(prayerMultiplier);
            AttackStyleDefinition attackStyleDefinition = weaponCombatAttack2.getAttackStyle();
            int index = 0;
            if (attackStyleDefinition.getXpMode() == AttackXpMode.AGGRESSIVE) {
                index = 3;
            } else if (attackStyleDefinition.getXpMode() == AttackXpMode.CONTROLLED) {
                index = 1;
            }
            value2 += index;
            value2 += 8;
            if (((Player)entity2).hasFullVoidMeleeSet()) {
                value2 = (int)((double)value2 * 1.1);
            }
        } else {
            entity2 = (Npc)entity3;
            double currentStrengthLevel = ((Npc)entity2).getCurrentStrengthLevel();
            value2 = (int)currentStrengthLevel;
            ++value2;
            value2 += 8;
        }
        double value3 = value2;
        if (entity.isPlayer()) {
            entity = (Player)entity;
            double combatBonus = entity.getCombatBonus(10);
            double value4 = 0.5 + value3 * ((combatBonus + 64.0) / 640.0);
            double value5 = value4;
            value5 = value4;
            value = (int)Math.floor(value4);
        } else {
            double value6;
            NpcDefinition npcDefinition;
            entity = (Npc)entity;
            NpcDefinition npcDefinition2 = npcDefinition = ((Npc)entity).getDefinition();
            double strengthLevel = value6 = (double)npcDefinition2.getStrengthLevel();
            strengthLevel = value6 + 1.0;
            double meleeStrengthBonus = npcDefinition2.getMeleeStrengthBonus();
            double value7 = 0.5 + (strengthLevel += 8.0) * ((meleeStrengthBonus + 64.0) / 640.0);
            int value8 = (int)Math.floor(value7);
            if (npcDefinition.getMaxHit() != value8) {
                value = npcDefinition.getMaxHit();
            } else {
                value = ((Npc)entity).getCurrentStrengthLevel();
                double value9 = value + 8;
                double meleeStrengthBonus2 = npcDefinition.getMeleeStrengthBonus();
                double value10 = 0.5 + value9 * ((meleeStrengthBonus2 + 64.0) / 640.0);
                value = (int)Math.floor(value10);
            }
        }
        return value;
    }

    public static int calculateSpellMaxHit(Entity entity, SpellDefinition spellDefinition) {
        if (!spellDefinition.isCombatSpell() || spellDefinition.getHitDefinition() == null) {
            return 0;
        }
        HitDefinition hitDefinition = spellDefinition.getHitDefinition().copy();
        int maxDamage = hitDefinition.getMaxDamage();
        if (entity.isPlayer()) {
            if (entity.isChargeSpellActive() && (spellDefinition == SpellDefinition.FLAMES_OF_ZAMORAK || spellDefinition == SpellDefinition.CLAWS_OF_GUTHIX || spellDefinition == SpellDefinition.SARADOMIN_STRIKE)) {
                maxDamage += 10;
            }
            if (((Player)(entity = (Player)entity)).getEquipmentManager().getItemIdAtSlot(9) == 777 && (spellDefinition == SpellDefinition.WIND_BOLT || spellDefinition == SpellDefinition.WATER_BOLT || spellDefinition == SpellDefinition.EARTH_BOLT || spellDefinition == SpellDefinition.FIRE_BOLT)) {
                maxDamage += 3;
            }
            if (((Player)entity).getEquipmentManager().getItemIdAtSlot(3) == 4675 || ((Player)entity).getEquipmentManager().getItemIdAtSlot(3) == 4710 || ((Player)entity).getEquipmentManager().getItemIdAtSlot(3) == 6914) {
                maxDamage = (int)((double)maxDamage * 1.1);
            }
            if (spellDefinition == SpellDefinition.MAGIC_DART) {
                maxDamage += ((Player)entity).getSkillManager().getBaseLevel(6) / 10;
            }
        }
        return maxDamage;
    }
}

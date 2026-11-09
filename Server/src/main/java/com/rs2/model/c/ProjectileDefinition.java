package com.rs2.model.c;

import com.rs2.ServerSettings;
import com.rs2.model.Entity;
import com.rs2.model.c.AberrantSpecterCombatDefinition;
import com.rs2.model.c.AhrimCombatDefinition;
import com.rs2.model.c.AirWizardCombatDefinition;
import com.rs2.model.c.ArmadylSpiritualMageCombatDefinition;
import com.rs2.model.c.ArmadylSpiritualRangerCombatDefinition;
import com.rs2.model.c.AviansieRanged1190Max10CombatDefinition;
import com.rs2.model.c.AviansieRanged1190Max11CombatDefinition;
import com.rs2.model.c.AviansieRanged1190Max12CombatDefinition;
import com.rs2.model.c.AviansieRanged1190Max15CombatDefinition;
import com.rs2.model.c.AviansieRanged1190Max9CombatDefinition;
import com.rs2.model.c.AviansieRanged1191Max10CombatDefinition;
import com.rs2.model.c.AviansieRanged1191Max11CombatDefinition;
import com.rs2.model.c.AviansieRanged1191Max15CombatDefinition;
import com.rs2.model.c.AviansieRanged1191Max16CombatDefinition;
import com.rs2.model.c.AviansieRanged1191Max8CombatDefinition;
import com.rs2.model.c.BalfrugKreeyathCombatDefinition;
import com.rs2.model.c.BandosSpiritualRangerCombatDefinition;
import com.rs2.model.c.BlackDragonCombatDefinition;
import com.rs2.model.c.BlueDragonCombatDefinition;
import com.rs2.model.c.BreeCombatDefinition;
import com.rs2.model.c.BronzeDragonCombatDefinition;
import com.rs2.model.c.CaradoCombatDefinition;
import com.rs2.model.c.ChaosDruidCombatDefinition;
import com.rs2.model.c.ChaosElementalCombatDefinition;
import com.rs2.model.c.CommanderZilyanaCombatDefinition;
import com.rs2.model.c.DagannothCombatDefinition;
import com.rs2.model.c.DagannothPrimeCombatDefinition;
import com.rs2.model.c.DagannothSupremeCombatDefinition;
import com.rs2.model.c.DarkWizardLevel20CombatDefinition;
import com.rs2.model.c.DarkWizardLevel7CombatDefinition;
import com.rs2.model.c.EarthWizardCombatDefinition;
import com.rs2.model.c.ElvargCombatDefinition;
import com.rs2.model.c.FireWizardCombatDefinition;
import com.rs2.model.c.FlockleaderGeerinCombatDefinition;
import com.rs2.model.c.GeneralGraardorCombatDefinition;
import com.rs2.model.c.GreenDragonCombatDefinition;
import com.rs2.model.c.GrowlerCombatDefinition;
import com.rs2.model.c.GuthixBattleMageCombatDefinition;
import com.rs2.model.c.InfernalMageCombatDefinition;
import com.rs2.model.c.IronDragonCombatDefinition;
import com.rs2.model.c.JungleDemonCombatDefinition;
import com.rs2.model.c.KalphiteQueenFirstFormCombatDefinition;
import com.rs2.model.c.KalphiteQueenSecondFormCombatDefinition;
import com.rs2.model.c.KarilCombatDefinition;
import com.rs2.model.c.KetZekCombatDefinition;
import com.rs2.model.c.KingBlackDragonCombatDefinition;
import com.rs2.model.c.KolodionCombatDefinition;
import com.rs2.model.c.KreeArraCombatDefinition;
import com.rs2.model.c.KrilTsutsarothCombatDefinition;
import com.rs2.model.c.MelzarTheMadCombatDefinition;
import com.rs2.model.c.MonkeyArcherCombatDefinition;
import com.rs2.model.c.RedDragonCombatDefinition;
import com.rs2.model.c.SaradominBattleMageCombatDefinition;
import com.rs2.model.c.SaradominMagicFollowerCombatDefinition;
import com.rs2.model.c.SaradominWizardCombatDefinition;
import com.rs2.model.c.SergeantGrimspikeCombatDefinition;
import com.rs2.model.c.SergeantSteelwillCombatDefinition;
import com.rs2.model.c.SheepShearingTask;
import com.rs2.model.c.SkeletalWyvernCombatDefinition;
import com.rs2.model.c.SpinolypCombatDefinition;
import com.rs2.model.c.SpiritualRangerCombatDefinition;
import com.rs2.model.c.SteelDragonCombatDefinition;
import com.rs2.model.c.TokXilCombatDefinition;
import com.rs2.model.c.TzTokJadCombatDefinition;
import com.rs2.model.c.WallasalkiCombatDefinition;
import com.rs2.model.c.WaterWizardCombatDefinition;
import com.rs2.model.c.WingmanSkreeCombatDefinition;
import com.rs2.model.c.WizardCombatDefinition;
import com.rs2.model.c.YtHurKotCombatDefinition;
import com.rs2.model.c.YtMejKotCombatDefinition;
import com.rs2.model.c.ZaklnGritchCombatDefinition;
import com.rs2.model.c.ZamorakBattleMageCombatDefinition;
import com.rs2.model.c.ZamorakSpiritualMageCombatDefinition;
import com.rs2.model.c.ZamorakWizardCombatDefinition;
import com.rs2.model.c.ZooknockCombatDefinition;
import com.rs2.model.combat.ProjectileTiming;
import com.rs2.model.npc.Npc;
import com.rs2.model.npc.combat.NpcCombatDefinition;
import com.rs2.model.player.Player;
import com.rs2.model.task.CycleEventHandler;

public class ProjectileDefinition {
    private ProjectileTiming timing;
    private int projectileId;

    public static void startSheepShearing(Player player) {
        if (player.getInteractionTarget() == null || player.getInteractionTarget().isPlayer()) {
            return;
        }
        if (player.getInventoryManager().getContainer().getFreeSlots() <= 0) {
            Player player2 = player;
            player2.packetSender.sendSoundEffect(1878, 1, 0);
            player2 = player;
            player2.packetSender.sendGameMessage("Not enough space in your inventory.");
            if (player.botEnabled) {
                player.currentBotTask.startWalkToBank(player);
            }
            return;
        }
        Entity entity = (Npc)player.getInteractionTarget();
        entity.getPosition().getX();
        entity.getPosition().getY();
        if (((Npc)entity).getTransformTicksRemaining() > 0) {
            entity = player;
            ((Player)entity).packetSender.sendGameMessage("This sheep has already been sheared.");
            if (player.botEnabled) {
                player.interactWithBotNpcTargets(player.botInteractionTargetIds);
            }
            return;
        }
        player.setActionLocked(true);
        player.getUpdateState().setAnimation(894);
        CycleEventHandler.getInstance().schedule(player, new SheepShearingTask(player, (Npc)entity), 2);
    }

    public ProjectileDefinition(int projectileId, ProjectileTiming projectileTiming) {
        this.projectileId = projectileId;
        this.timing = projectileTiming;
    }

    public ProjectileTiming getTiming() {
        return this.timing;
    }

    public int getProjectileId() {
        return this.projectileId;
    }

    private static NpcCombatDefinition createSaradominWizardCombatDefinition() {
        SaradominWizardCombatDefinition saradominWizardCombatDefinition = new SaradominWizardCombatDefinition();
        return saradominWizardCombatDefinition;
    }

    private static NpcCombatDefinition createZamorakWizardCombatDefinition() {
        ZamorakWizardCombatDefinition zamorakWizardCombatDefinition = new ZamorakWizardCombatDefinition();
        return zamorakWizardCombatDefinition;
    }

    public static void registerNpcCombatDefinitions() {
        NpcCombatDefinition npcCombatDefinition = new KingBlackDragonCombatDefinition();
        NpcCombatDefinition.register(new int[]{50}, npcCombatDefinition);
        npcCombatDefinition = new RedDragonCombatDefinition();
        NpcCombatDefinition.register(new int[]{53}, npcCombatDefinition);
        npcCombatDefinition = new BlackDragonCombatDefinition();
        NpcCombatDefinition.register(new int[]{54}, npcCombatDefinition);
        npcCombatDefinition = new BlueDragonCombatDefinition();
        NpcCombatDefinition.register(new int[]{55}, npcCombatDefinition);
        npcCombatDefinition = new DarkWizardLevel20CombatDefinition();
        NpcCombatDefinition.register(new int[]{172}, npcCombatDefinition);
        npcCombatDefinition = new DarkWizardLevel7CombatDefinition();
        NpcCombatDefinition.register(new int[]{174}, npcCombatDefinition);
        npcCombatDefinition = new ChaosDruidCombatDefinition();
        NpcCombatDefinition.register(new int[]{181}, npcCombatDefinition);
        npcCombatDefinition = new ElvargCombatDefinition();
        NpcCombatDefinition.register(new int[]{742}, npcCombatDefinition);
        npcCombatDefinition = new MelzarTheMadCombatDefinition();
        NpcCombatDefinition.register(new int[]{753}, npcCombatDefinition);
        NpcCombatDefinition.register(new int[]{PriestInPerilMonkCombatDefinition.MONK1}, new PriestInPerilMonkCombatDefinition(PriestInPerilMonkCombatDefinition.MONK1));
        NpcCombatDefinition.register(new int[]{PriestInPerilMonkCombatDefinition.MONK2}, new PriestInPerilMonkCombatDefinition(PriestInPerilMonkCombatDefinition.MONK2));
        NpcCombatDefinition.register(new int[]{PriestInPerilMonkCombatDefinition.MONK3}, new PriestInPerilMonkCombatDefinition(PriestInPerilMonkCombatDefinition.MONK3));
        npcCombatDefinition = new KolodionCombatDefinition();
        NpcCombatDefinition.register(new int[]{907, 908, 909, 910, 911}, npcCombatDefinition);
        npcCombatDefinition = new ZamorakBattleMageCombatDefinition();
        NpcCombatDefinition.register(new int[]{912}, npcCombatDefinition);
        npcCombatDefinition = new SaradominBattleMageCombatDefinition();
        NpcCombatDefinition.register(new int[]{913}, npcCombatDefinition);
        npcCombatDefinition = new GuthixBattleMageCombatDefinition();
        NpcCombatDefinition.register(new int[]{914}, npcCombatDefinition);
        npcCombatDefinition = new GreenDragonCombatDefinition();
        NpcCombatDefinition.register(new int[]{941}, npcCombatDefinition);
        if (ServerSettings.modernCombatSystemEnabled) {
            NpcCombatDefinition.register(new int[]{1007}, ProjectileDefinition.createZamorakWizardCombatDefinition());
            NpcCombatDefinition.register(new int[]{1264}, ProjectileDefinition.createSaradominWizardCombatDefinition());
        } else {
            NpcCombatDefinition.register(new int[]{1007}, ProjectileDefinition.createZamorakWizardCombatDefinition().addAttackBonuses(0, 0, 0, 350, 0));
            NpcCombatDefinition.register(new int[]{1264}, ProjectileDefinition.createSaradominWizardCombatDefinition().addAttackBonuses(500, 500, 500, 500, 0));
        }
        npcCombatDefinition = new KalphiteQueenFirstFormCombatDefinition();
        NpcCombatDefinition.register(new int[]{1158}, npcCombatDefinition);
        npcCombatDefinition = new KalphiteQueenSecondFormCombatDefinition();
        NpcCombatDefinition.register(new int[]{1160}, npcCombatDefinition);
        npcCombatDefinition = new CaradoCombatDefinition();
        NpcCombatDefinition.register(new int[]{1418}, npcCombatDefinition);
        npcCombatDefinition = new ZooknockCombatDefinition();
        NpcCombatDefinition.register(new int[]{1426}, npcCombatDefinition);
        npcCombatDefinition = new MonkeyArcherCombatDefinition();
        NpcCombatDefinition.register(new int[]{1456, 1458}, npcCombatDefinition);
        npcCombatDefinition = new JungleDemonCombatDefinition();
        NpcCombatDefinition.register(new int[]{1472}, npcCombatDefinition);
        npcCombatDefinition = new BronzeDragonCombatDefinition();
        NpcCombatDefinition.register(new int[]{1590}, npcCombatDefinition);
        npcCombatDefinition = new IronDragonCombatDefinition();
        NpcCombatDefinition.register(new int[]{1591}, npcCombatDefinition);
        npcCombatDefinition = new SteelDragonCombatDefinition();
        NpcCombatDefinition.register(new int[]{1592}, npcCombatDefinition);
        npcCombatDefinition = new AberrantSpecterCombatDefinition();
        NpcCombatDefinition.register(new int[]{1604, 1605, 1606, 1607}, npcCombatDefinition);
        npcCombatDefinition = new InfernalMageCombatDefinition();
        NpcCombatDefinition.register(new int[]{1643}, npcCombatDefinition);
        npcCombatDefinition = new AhrimCombatDefinition();
        NpcCombatDefinition.register(new int[]{2025}, npcCombatDefinition);
        npcCombatDefinition = new KarilCombatDefinition();
        NpcCombatDefinition.register(new int[]{2028}, npcCombatDefinition);
        npcCombatDefinition = new DagannothCombatDefinition();
        NpcCombatDefinition.register(new int[]{2456}, npcCombatDefinition);
        npcCombatDefinition = new WallasalkiCombatDefinition();
        NpcCombatDefinition.register(new int[]{2457}, npcCombatDefinition);
        npcCombatDefinition = new TokXilCombatDefinition();
        NpcCombatDefinition.register(new int[]{2631}, npcCombatDefinition);
        npcCombatDefinition = new WizardCombatDefinition();
        NpcCombatDefinition.register(new int[]{13}, npcCombatDefinition);
        npcCombatDefinition = new FireWizardCombatDefinition();
        NpcCombatDefinition.register(new int[]{2709}, npcCombatDefinition);
        npcCombatDefinition = new WaterWizardCombatDefinition();
        NpcCombatDefinition.register(new int[]{2710}, npcCombatDefinition);
        npcCombatDefinition = new EarthWizardCombatDefinition();
        NpcCombatDefinition.register(new int[]{2711}, npcCombatDefinition);
        npcCombatDefinition = new AirWizardCombatDefinition();
        NpcCombatDefinition.register(new int[]{2712}, npcCombatDefinition);
        npcCombatDefinition = new YtMejKotCombatDefinition();
        NpcCombatDefinition.register(new int[]{2741}, npcCombatDefinition);
        npcCombatDefinition = new KetZekCombatDefinition();
        NpcCombatDefinition.register(new int[]{2743, 2744}, npcCombatDefinition);
        npcCombatDefinition = new TzTokJadCombatDefinition();
        NpcCombatDefinition.register(new int[]{2745}, npcCombatDefinition);
        npcCombatDefinition = new YtHurKotCombatDefinition();
        NpcCombatDefinition.register(new int[]{2746}, npcCombatDefinition);
        npcCombatDefinition = new DagannothSupremeCombatDefinition();
        NpcCombatDefinition.register(new int[]{2881}, npcCombatDefinition);
        npcCombatDefinition = new DagannothPrimeCombatDefinition();
        NpcCombatDefinition.register(new int[]{2882}, npcCombatDefinition);
        npcCombatDefinition = new SpinolypCombatDefinition();
        NpcCombatDefinition.register(new int[]{2892}, npcCombatDefinition);
        npcCombatDefinition = new SkeletalWyvernCombatDefinition();
        NpcCombatDefinition.register(new int[]{3068}, npcCombatDefinition);
        npcCombatDefinition = new ChaosElementalCombatDefinition();
        NpcCombatDefinition.register(new int[]{3200}, npcCombatDefinition);
        npcCombatDefinition = new KrilTsutsarothCombatDefinition();
        NpcCombatDefinition.register(new int[]{6203}, npcCombatDefinition);
        npcCombatDefinition = new ZaklnGritchCombatDefinition();
        NpcCombatDefinition.register(new int[]{6206}, npcCombatDefinition);
        npcCombatDefinition = new BalfrugKreeyathCombatDefinition();
        NpcCombatDefinition.register(new int[]{6208}, npcCombatDefinition);
        npcCombatDefinition = new ZamorakSpiritualMageCombatDefinition();
        NpcCombatDefinition.register(new int[]{6221}, npcCombatDefinition);
        npcCombatDefinition = new KreeArraCombatDefinition();
        NpcCombatDefinition.register(new int[]{6222}, npcCombatDefinition);
        npcCombatDefinition = new WingmanSkreeCombatDefinition();
        NpcCombatDefinition.register(new int[]{6223}, npcCombatDefinition);
        npcCombatDefinition = new FlockleaderGeerinCombatDefinition();
        NpcCombatDefinition.register(new int[]{6225}, npcCombatDefinition);
        npcCombatDefinition = new ArmadylSpiritualRangerCombatDefinition();
        NpcCombatDefinition.register(new int[]{6230}, npcCombatDefinition);
        npcCombatDefinition = new ArmadylSpiritualMageCombatDefinition();
        NpcCombatDefinition.register(new int[]{6231}, npcCombatDefinition);
        npcCombatDefinition = new AviansieRanged1191Max8CombatDefinition();
        NpcCombatDefinition.register(new int[]{6232}, npcCombatDefinition);
        npcCombatDefinition = new AviansieRanged1190Max9CombatDefinition();
        NpcCombatDefinition.register(new int[]{6233, 6235}, npcCombatDefinition);
        npcCombatDefinition = new AviansieRanged1190Max10CombatDefinition();
        NpcCombatDefinition.register(new int[]{6234, 6236, 6241}, npcCombatDefinition);
        npcCombatDefinition = new AviansieRanged1191Max11CombatDefinition();
        NpcCombatDefinition.register(new int[]{6237}, npcCombatDefinition);
        npcCombatDefinition = new AviansieRanged1190Max15CombatDefinition();
        NpcCombatDefinition.register(new int[]{6238}, npcCombatDefinition);
        npcCombatDefinition = new AviansieRanged1191Max16CombatDefinition();
        NpcCombatDefinition.register(new int[]{6239}, npcCombatDefinition);
        npcCombatDefinition = new AviansieRanged1191Max10CombatDefinition();
        NpcCombatDefinition.register(new int[]{6240}, npcCombatDefinition);
        npcCombatDefinition = new AviansieRanged1190Max11CombatDefinition();
        NpcCombatDefinition.register(new int[]{6242, 6244, 6245}, npcCombatDefinition);
        npcCombatDefinition = new AviansieRanged1190Max12CombatDefinition();
        NpcCombatDefinition.register(new int[]{6243}, npcCombatDefinition);
        npcCombatDefinition = new AviansieRanged1191Max15CombatDefinition();
        NpcCombatDefinition.register(new int[]{6246}, npcCombatDefinition);
        npcCombatDefinition = new CommanderZilyanaCombatDefinition();
        NpcCombatDefinition.register(new int[]{6247}, npcCombatDefinition);
        npcCombatDefinition = new GrowlerCombatDefinition();
        NpcCombatDefinition.register(new int[]{6250}, npcCombatDefinition);
        npcCombatDefinition = new BreeCombatDefinition();
        NpcCombatDefinition.register(new int[]{6252}, npcCombatDefinition);
        npcCombatDefinition = new SpiritualRangerCombatDefinition();
        NpcCombatDefinition.register(new int[]{6220, 6256}, npcCombatDefinition);
        npcCombatDefinition = new BandosSpiritualRangerCombatDefinition();
        NpcCombatDefinition.register(new int[]{6276}, npcCombatDefinition);
        npcCombatDefinition = new SaradominMagicFollowerCombatDefinition();
        NpcCombatDefinition.register(new int[]{6254, 6257}, npcCombatDefinition);
        npcCombatDefinition = new GeneralGraardorCombatDefinition();
        NpcCombatDefinition.register(new int[]{6260}, npcCombatDefinition);
        npcCombatDefinition = new SergeantSteelwillCombatDefinition();
        NpcCombatDefinition.register(new int[]{6263}, npcCombatDefinition);
        npcCombatDefinition = new SergeantGrimspikeCombatDefinition();
        NpcCombatDefinition.register(new int[]{6265}, npcCombatDefinition);
    }
}


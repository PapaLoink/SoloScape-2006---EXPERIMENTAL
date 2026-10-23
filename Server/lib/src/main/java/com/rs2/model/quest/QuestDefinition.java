package com.rs2.model.quest;

import com.rs2.ServerSettings;
import com.rs2.model.quest.NullQuestScript;
import com.rs2.model.quest.QuestScript;
import com.rs2.model.quest.impl.BlackKnightsFortressQuest;
import com.rs2.model.quest.impl.CooksAssistantQuest;
import com.rs2.model.quest.impl.DemonSlayerQuest;
import com.rs2.model.quest.impl.DoricsQuest;
import com.rs2.model.quest.impl.DragonSlayerQuest;
import com.rs2.model.quest.impl.DruidicRitualQuest;
import com.rs2.model.quest.impl.ElementalWorkshopQuest;
import com.rs2.model.quest.impl.ErnestTheChickenQuest;
import com.rs2.model.quest.impl.FamilyCrestQuest;
import com.rs2.model.quest.impl.FremennikTrialsQuest;
import com.rs2.model.quest.impl.GertrudesCatQuest;
import com.rs2.model.quest.impl.GoblinDiplomacyQuest;
import com.rs2.model.quest.impl.GrandTreeQuest;
import com.rs2.model.quest.impl.HeroesQuest;
import com.rs2.model.quest.impl.HolyGrailQuest;
import com.rs2.model.quest.impl.ImpCatcherQuest;
import com.rs2.model.quest.impl.JunglePotionQuest;
import com.rs2.model.quest.impl.KnightsSwordQuest;
import com.rs2.model.quest.impl.LostCityQuest;
import com.rs2.model.quest.impl.MerlinsCrystalQuest;
import com.rs2.model.quest.impl.MonkeyMadnessQuest;
import com.rs2.model.quest.impl.PiratesTreasureQuest;
import com.rs2.model.quest.impl.PriestInPerilQuest;
import com.rs2.model.quest.impl.PrinceAliRescueQuest;
import com.rs2.model.quest.impl.RestlessGhostQuest;
import com.rs2.model.quest.impl.RomeoAndJulietQuest;
import com.rs2.model.quest.impl.RuneMysteriesQuest;
import com.rs2.model.quest.impl.ScorpionCatcherQuest;
import com.rs2.model.quest.impl.SheepShearerQuest;
import com.rs2.model.quest.impl.ShieldOfArravQuest;
import com.rs2.model.quest.impl.TreeGnomeVillageQuest;
import com.rs2.model.quest.impl.TutorialQuest;
import com.rs2.model.quest.impl.VampireSlayerQuest;
import com.rs2.model.quest.impl.WitchsHouseQuest;
import com.rs2.model.quest.impl.WitchsPotionQuest;
import com.rs2.util.ByteArrayReader;
import com.rs2.util.FileUtil;

public final class QuestDefinition {
    private String name;
    private int journalButtonId;
    private boolean membersOnly;
    public static int questCount = 0;
    public static int questStateCapacity = 200;
    private static QuestDefinition[] definitionsById = new QuestDefinition[0];
    private static QuestScript[] questScripts = new QuestScript[]{new NullQuestScript(-1), new TutorialQuest(0), new BlackKnightsFortressQuest(1), new CooksAssistantQuest(2), new DemonSlayerQuest(3), new DoricsQuest(4), new DragonSlayerQuest(5), new ErnestTheChickenQuest(6), new GoblinDiplomacyQuest(7), new ImpCatcherQuest(8), new KnightsSwordQuest(9), new PiratesTreasureQuest(10), new PrinceAliRescueQuest(11), new RestlessGhostQuest(12), new RomeoAndJulietQuest(13), new RuneMysteriesQuest(14), new SheepShearerQuest(15), new ShieldOfArravQuest(16), new VampireSlayerQuest(17), new WitchsPotionQuest(18), new DruidicRitualQuest(29), new ElementalWorkshopQuest(32), new FamilyCrestQuest(35), new FremennikTrialsQuest(40), new GertrudesCatQuest(42), new GrandTreeQuest(46), new HeroesQuest(50), new HolyGrailQuest(51), new JunglePotionQuest(56), new LostCityQuest(58), new MerlinsCrystalQuest(61), new MonkeyMadnessQuest(62), new PriestInPerilQuest(72), new ScorpionCatcherQuest(80), new TreeGnomeVillageQuest(95), new WitchsHouseQuest(103), new com.rs2.model.quest.impl.ClockTowerQuest(0), new com.rs2.model.quest.impl.MonksFriendQuest(0), new com.rs2.model.quest.impl.SheepHerderQuest(0)};

    public static int getTotalQuestPointReward() {
        int index = 0;
        int initialValue = 1;
        while (initialValue < questCount) {
            QuestScript questScript = QuestDefinition.getQuestScript(initialValue);
            index += questScript.getQuestPointReward();
            ++initialValue;
        }
        return index;
    }

    public static QuestScript getQuestScript(int value2) {
        int index = 0;
        while (index < questScripts.length) {
            int questId = questScripts[index].getQuestId();
            if (value2 == questId) {
                return questScripts[index];
            }
            ++index;
        }
        return questScripts[0];
    }

    public static QuestDefinition forId(int value2) {
        QuestDefinition questDefinition;
        if (value2 < 0) {
            value2 = 1;
        }
        if ((questDefinition = definitionsById[value2]) == null) {
            questDefinition = new QuestDefinition(value2, "UNKNOWN", -1, false);
        }
        return questDefinition;
    }

    public static void loadDefinitions() {
        try {
            Object value = FileUtil.readBytes("./data/content/questing/Quests.dat");
            ByteArrayReader byteArrayReader = new ByteArrayReader((byte[])value);
            value = byteArrayReader;
            questCount = byteArrayReader.readUnsignedByte();
            definitionsById = new QuestDefinition[questCount];
            int index = 0;
            while (index < questCount) {
                boolean enabled;
                String text = ((ByteArrayReader)value).readString();
                int value2 = ((ByteArrayReader)value).readUnsignedShort() - 1;
                boolean enabled2 = enabled = ((ByteArrayReader)value).readUnsignedByte() != 0;
                if (ServerSettings.cacheVersion < 274 && index == 99) {
                    value2 = 7382;
                }
                QuestDefinition.definitionsById[index] = new QuestDefinition(index, text, value2, enabled);
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

    private QuestDefinition(int value3, String name, int journalButtonId, boolean membersOnly) {
        this.name = name;
        this.journalButtonId = journalButtonId;
        this.membersOnly = membersOnly;
    }

    public final String getName() {
        return this.name;
    }

    public final int getJournalButtonId() {
        return this.journalButtonId;
    }

    public final boolean isMembersOnly() {
        return this.membersOnly;
    }
}


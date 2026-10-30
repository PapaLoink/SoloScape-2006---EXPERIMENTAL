package com.rs2.model.skill;

import com.rs2.ServerSettings;
import com.rs2.bot.BotTaskPlanner;
import com.rs2.model.World;
import com.rs2.model.player.Player;
import com.rs2.model.skill.SkillLevelRestoreTask;
import com.rs2.model.skill.SpecialEnergyRestoreTask;
import com.rs2.model.task.TickTask;
import com.rs2.util.DiscordEventNotifier;
import com.rs2.util.GameUtil;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class SkillManager {
    private Player player;
    private int[] currentLevels = new int[22];
    private double[] experience = new double[22];
    private TickTask[] levelRestoreTasks = new TickTask[22];
    private int[] restoreDelayTicks = new int[22];
    private TickTask specialEnergyRestoreTask;
    private static final int[] experienceForLevel = new int[ServerSettings.maxLevel + 1];
    public static final String[] SKILL_NAMES;
    private long actionDelayExpiresAtMillis = -10000L;
    private long drinkDelayExpiresAtMillis = -10000L;
    public static int maxCombatLevel;

    static {
        int index = 0;
        int initialValue = 1;
        while (initialValue <= ServerSettings.maxLevel) {
            int value;
            index = (int)((double)index + Math.floor((double)initialValue + 300.0 * Math.pow(2.0, (double)initialValue / 7.0)));
            SkillManager.experienceForLevel[initialValue] = value = (int)Math.floor(index / 4);
            ++initialValue;
        }
        SKILL_NAMES = new String[]{"Attack", "Defence", "Strength", "Hitpoints", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecrafting", "Construction"};
        maxCombatLevel = 126;
    }

    private double getExperienceCap() {
        if (ServerSettings.capXpAtMaxLevel) {
            return SkillManager.getExperienceForLevel(ServerSettings.maxLevel - 1);
        }
        return 2.0E8;
    }

    public SkillManager(Player player) {
        int[][] nArrayArray = new int[21][];
        int[] integerValues = new int[4];
        integerValues[1] = 6247;
        nArrayArray[0] = integerValues;
        int[] integerValues2 = new int[4];
        integerValues2[0] = 1;
        integerValues2[1] = 6253;
        nArrayArray[1] = integerValues2;
        int[] integerValues3 = new int[4];
        integerValues3[0] = 2;
        integerValues3[1] = 6206;
        nArrayArray[2] = integerValues3;
        int[] integerValues4 = new int[4];
        integerValues4[0] = 3;
        integerValues4[1] = 6216;
        nArrayArray[3] = integerValues4;
        nArrayArray[4] = new int[]{4, 4443, 5453, 6114};
        int[] integerValues5 = new int[4];
        integerValues5[0] = 5;
        integerValues5[1] = 6242;
        nArrayArray[5] = integerValues5;
        int[] integerValues6 = new int[4];
        integerValues6[0] = 6;
        integerValues6[1] = 6211;
        nArrayArray[6] = integerValues6;
        int[] integerValues7 = new int[4];
        integerValues7[0] = 7;
        integerValues7[1] = 6226;
        nArrayArray[7] = integerValues7;
        int[] integerValues8 = new int[4];
        integerValues8[0] = 8;
        integerValues8[1] = 4272;
        nArrayArray[8] = integerValues8;
        int[] integerValues9 = new int[4];
        integerValues9[0] = 9;
        integerValues9[1] = 6231;
        nArrayArray[9] = integerValues9;
        int[] integerValues10 = new int[4];
        integerValues10[0] = 10;
        integerValues10[1] = 6258;
        nArrayArray[10] = integerValues10;
        int[] integerValues11 = new int[4];
        integerValues11[0] = 11;
        integerValues11[1] = 4282;
        nArrayArray[11] = integerValues11;
        int[] integerValues12 = new int[4];
        integerValues12[0] = 12;
        integerValues12[1] = 6263;
        nArrayArray[12] = integerValues12;
        int[] integerValues13 = new int[4];
        integerValues13[0] = 13;
        integerValues13[1] = 6221;
        nArrayArray[13] = integerValues13;
        nArrayArray[14] = new int[]{14, 4416, 4417, 4438};
        int[] integerValues14 = new int[4];
        integerValues14[0] = 15;
        integerValues14[1] = 6237;
        nArrayArray[15] = integerValues14;
        int[] integerValues15 = new int[4];
        integerValues15[0] = 16;
        integerValues15[1] = 4277;
        nArrayArray[16] = integerValues15;
        nArrayArray[17] = new int[]{17, 4261, 4263, 4264};
        int[] integerValues16 = new int[4];
        integerValues16[0] = 18;
        integerValues16[1] = 12122;
        nArrayArray[18] = integerValues16;
        nArrayArray[19] = new int[]{19, 4887, 4889, 4890};
        int[] integerValues17 = new int[4];
        integerValues17[0] = 20;
        integerValues17[1] = 4267;
        nArrayArray[20] = integerValues17;
        this.player = player;
        int index = 0;
        while (index < this.currentLevels.length) {
            if (index == 3) {
                this.currentLevels[index] = 10;
                this.experience[index] = 1154.0;
            } else {
                this.currentLevels[index] = 1;
                this.experience[index] = 0.0;
            }
            this.setSkillRestoreDelay(index, 100);
            ++index;
        }
    }

    public final void startRestorationTasks() {
        Object value;
        int index = 0;
        while (index < 22) {
            if (index != 5) {
                int value2 = index;
                SkillManager skillManager = this;
                value = skillManager.levelRestoreTasks[value2];
                SkillManager skillManager2 = skillManager;
                if (skillManager.currentLevels[value2] != SkillManager.getLevelForExperience(skillManager2.experience[value2])) {
                    if (skillManager.levelRestoreTasks[value2] == null || !skillManager.levelRestoreTasks[value2].isActive()) {
                        int value3 = skillManager.restoreDelayTicks[value2];
                        skillManager.levelRestoreTasks[value2] = new SkillLevelRestoreTask(skillManager, value3, value2);
                        World.getTaskScheduler().schedule(skillManager.levelRestoreTasks[value2]);
                    }
                } else if (value != null) {
                    ((TickTask)value).stop();
                    int value4 = skillManager.restoreDelayTicks[value2];
                    ((TickTask)value).setIntervalTicks(value4);
                    ((TickTask)value).setRemainingTicks(value4);
                }
            }
            ++index;
        }
        SkillManager skillManager = this;
        TickTask tickTask = skillManager.specialEnergyRestoreTask;
        if (skillManager.player.getSpecialEnergy() < 100) {
            value = skillManager;
            if (((SkillManager)value).specialEnergyRestoreTask == null || !((SkillManager)value).specialEnergyRestoreTask.isActive()) {
                ((SkillManager)value).specialEnergyRestoreTask = new SpecialEnergyRestoreTask((SkillManager)value, 50);
                World.getTaskScheduler().schedule(((SkillManager)value).specialEnergyRestoreTask);
            }
            return;
        }
        if (tickTask != null) {
            tickTask.stop();
            tickTask.setIntervalTicks(50);
            tickTask.setRemainingTicks(50);
        }
    }

    public final void setRapidHealRestoreDelay(boolean delayTicks) {
        this.setSkillRestoreDelay(3, delayTicks ? 50 : 100);
    }

    public final void restoreNonPrayerLevels() {
        int index = 0;
        while (index < 22) {
            if (index != 3 && index != 5) {
                SkillManager skillManager = this;
                if (this.currentLevels[index] != SkillManager.getLevelForExperience(skillManager.experience[index])) {
                    skillManager = this;
                    if (this.currentLevels[index] > SkillManager.getLevelForExperience(skillManager.experience[index])) {
                        int value = index;
                        this.currentLevels[value] = this.currentLevels[value] - 1;
                    } else {
                        int value2 = index;
                        this.currentLevels[value2] = this.currentLevels[value2] + 1;
                    }
                    this.refreshSkill(index);
                }
            }
            ++index;
        }
    }

    private void setSkillRestoreDelay(int delayTicks, int value2) {
        this.restoreDelayTicks[delayTicks] = value2;
        TickTask tickTask = this.levelRestoreTasks[delayTicks];
        if (tickTask != null) {
            tickTask.setIntervalTicks(value2);
            tickTask.setRemainingTicks(value2);
        }
    }

    public final boolean isLevelModified(int level) {
        SkillManager skillManager = this;
        return this.currentLevels[level] != SkillManager.getLevelForExperience(skillManager.experience[level]);
    }

    public final boolean isSpecialEnergyBelowMaximum() {
        return this.player.getSpecialEnergy() < 100;
    }

    public final void refreshAllSkills() {
        int index = 0;
        while (index < this.currentLevels.length) {
            Player player = this.player;
            player.packetSender.sendSkillUpdate(index, this.currentLevels[index], this.experience[index]);
            ++index;
        }
        this.player.setCombatLevel(this.getCombatLevel());
        this.player.setAppearanceUpdateRequired(true);
    }

    public final void refreshSkill(int skillId) {
        Player player = this.player;
        player.packetSender.sendSkillUpdate(skillId, this.currentLevels[skillId], this.experience[skillId]);
        this.player.setCombatLevel(this.getCombatLevel());
        this.player.setAppearanceUpdateRequired(true);
    }

    public final int getBaseLevel(int level) {
        return SkillManager.getLevelForExperience(this.experience[level]);
    }

    public static int getLevelForExperience(double level) {
        int initialValue = 1;
        while (initialValue <= ServerSettings.maxLevel) {
            if ((double)experienceForLevel[initialValue] > level) {
                return initialValue;
            }
            ++initialValue;
        }
        return ServerSettings.maxLevel;
    }

    public static int getExperienceForLevel(int level) {
        if (level >= experienceForLevel.length) {
            return Integer.MAX_VALUE;
        }
        return experienceForLevel[level];
    }

    /*
     * Enabled aggressive block sorting
     */
    public final int getTotalLevel() {
        int index = 0;
        int index2 = 0;
        while (index2 < 22) {
            if (!(index2 == 18 && ServerSettings.cacheVersion < 319 || index2 == 19 && ServerSettings.cacheVersion < 336)) {
                index += this.getBaseLevel(index2);
            }
            ++index2;
        }
        return index;
    }

    /*
     * Enabled aggressive block sorting
     */
    public final long getTotalExperience() {
        long value = 0L;
        int index = 0;
        while (index < 22) {
            SkillManager skillManager = this;
            value = (long)((double)value + skillManager.experience[index]);
            ++index;
        }
        return value;
    }

    public final int calculateExperienceGain(int experience, double experience2) {
        experience2 = ServerSettings.progressiveXpMode == 0 ? (experience2 *= ServerSettings.xpRate) : (experience2 *= this.getExperienceRateForSkill(experience));
        if (this.player.isBot && ServerSettings.botXpRateMode == 1) {
            experience2 *= ServerSettings.botXpRateMultiplier;
        } else if ((this.player.isBot || this.player.botEnabled) && ServerSettings.botXpRateMode == 2) {
            experience2 *= ServerSettings.botXpRateMultiplier;
        }
        if (this.player.getEnchantmentChamberController().isInsideChamber() || this.player.getAlchemistPlaygroundController().isInsidePlayground() || this.player.getCreatureGraveyardController().isInsideGraveyard() || this.player.getTelekineticTheatreController().isInsideTheatre()) {
            experience2 *= 0.75;
        }
        experience = (int)experience2;
        return experience;
    }

    private double getExperienceRateForSkill(int experience) {
        double value = 1.0;
        experience = SkillManager.getLevelForExperience(this.experience[experience]);
        if (ServerSettings.progressiveXpMode == 1) {
            double value2 = Math.pow(1.02, experience);
            double value3 = Math.pow(experience, 2.0);
            double value4 = value3 / 3300.0;
            value = 1.0 + (value2 + value4 - 1.0) * ServerSettings.xpRate;
        } else if (ServerSettings.progressiveXpMode == 2) {
            value = 1.0 + (double)(experience - 1) * (0.1 * ServerSettings.xpRate);
        }
        return value;
    }

    private double getExperienceRateForLevel(int level) {
        double value = 1.0;
        if (ServerSettings.progressiveXpMode == 1) {
            double value2 = Math.pow(1.02, level);
            double value3 = Math.pow(level, 2.0);
            double value4 = value3 / 3300.0;
            value = 1.0 + (value2 + value4 - 1.0) * ServerSettings.xpRate;
        } else if (ServerSettings.progressiveXpMode == 2) {
            value = 1.0 + (double)(level - 1) * (0.1 * ServerSettings.xpRate);
        }
        if (this.player.isBot && ServerSettings.botXpRateMode == 1) {
            value *= ServerSettings.botXpRateMultiplier;
        } else if ((this.player.isBot || this.player.botEnabled) && ServerSettings.botXpRateMode == 2) {
            value *= ServerSettings.botXpRateMultiplier;
        }
        return value;
    }

    public final boolean addExperience(int experience, double experience2) {
        int value;
        int value2;
        int levelForExperience = SkillManager.getLevelForExperience(this.experience[experience]);
        if (levelForExperience >= 3 && this.player.getQuestState(0) != 1) {
            return false;
        }
        if (experience2 <= 0.0) {
            return false;
        }
        boolean enabled = false;
        boolean enabled2 = this.player.isMember();
        this.player.temporaryActionValue = levelForExperience;
        experience2 = ServerSettings.progressiveXpMode == 0 ? (experience2 *= ServerSettings.xpRate) : (experience2 *= this.getExperienceRateForSkill(experience));
        if (this.player.isBot && ServerSettings.botXpRateMode == 1) {
            experience2 *= ServerSettings.botXpRateMultiplier;
        } else if ((this.player.isBot || this.player.botEnabled) && ServerSettings.botXpRateMode == 2) {
            experience2 *= ServerSettings.botXpRateMultiplier;
        }
        if (this.player.getEnchantmentChamberController().isInsideChamber() || this.player.getAlchemistPlaygroundController().isInsidePlayground() || this.player.getCreatureGraveyardController().isInsideGraveyard() || this.player.getTelekineticTheatreController().isInsideTheatre()) {
            int value3 = experience;
            this.experience[value3] = this.experience[value3] + experience2 * 0.75;
        } else {
            int value4 = experience;
            this.experience[value4] = this.experience[value4] + experience2;
        }
        if (this.experience[experience] > this.getExperienceCap()) {
            this.experience[experience] = this.getExperienceCap();
        }
        if ((value2 = (value = SkillManager.getLevelForExperience(this.experience[experience])) - levelForExperience) > 0) {
            enabled = true;
            int value5 = experience;
            this.currentLevels[value5] = this.currentLevels[value5] + value2;
            if (experience > 0 && experience <= 6) {
                this.player.setCombatLevel(this.getCombatLevel());
            }
            this.player.currentLevelUpSkillId = experience;     // continueLevelUp shows its unlock (SHOW_SKILL_UNLOCKS)
            this.showLevelUpInterface(experience);
            DiscordEventNotifier.levelUp(this.player, SKILL_NAMES[experience], value);
            if (this.player.botEnabled) {
                if (GameUtil.randomInt(5) == 0 && (value % 2 == 0 || value % 5 == 0)) {
                    this.player.queuePublicChatMessage("Yay " + value + " " + SKILL_NAMES[experience] + "!");
                }
                if (this.player.currentBotTask != null && this.player.currentBotTask.combatTask && value % 5 == 0) {
                    BotTaskPlanner.selectMeleeTrainingFightMode(this.player);
                }
            }
        }
        this.refreshSkill(experience);
        if (ServerSettings.membershipRequirementMode == 2 && !enabled2 && this.player.isMember()) {
            this.player.packetSender.sendGameMessage("You have reached " + ServerSettings.membershipRequirementValue + "+ total lvl and gained access to members content!");
        }
        return enabled;
    }

    public final void addQuestExperience(int experience, double experience2) {
        int value;
        if (experience2 <= 0.0) {
            return;
        }
        this.player.isMember();
        this.player.temporaryActionValue = value = SkillManager.getLevelForExperience(this.experience[experience]);
        int value2 = experience;
        this.experience[value2] = this.experience[value2] + (experience2 *= ServerSettings.questXpRate);
        if (this.experience[experience] > this.getExperienceCap()) {
            this.experience[experience] = this.getExperienceCap();
        }
        int levelForExperience = SkillManager.getLevelForExperience(this.experience[experience]);
        if ((levelForExperience -= value) > 0) {
            int value3 = experience;
            this.currentLevels[value3] = this.currentLevels[value3] + levelForExperience;
            if (experience > 0 && experience <= 6) {
                this.player.setCombatLevel(this.getCombatLevel());
            }
            this.player.currentLevelUpSkillId = experience;     // continueLevelUp shows its unlock (SHOW_SKILL_UNLOCKS)
            this.showLevelUpInterface(experience);
            DiscordEventNotifier.levelUp(this.player, SKILL_NAMES[experience], this.getBaseLevel(experience));
        }
        this.refreshSkill(experience);
    }

    /** Every skill level-up heading uses the client's bright-blue @blu@ tag. */
    private static String levelUpTitle(int skillId) {
        return "@blu@Congratulations, you just advanced a "
                + SKILL_NAMES[skillId] + " level!";
    }

    public final void showLevelUpInterface(int interfaceId) {
        Object value;
        if (this.player.deferLevelUpInterfaces) {
            if (!this.player.queuedLevelUpSkillIds.contains(interfaceId)) {
                this.player.queuedLevelUpSkillIds.add(interfaceId);
            }
            return;
        }
        int[][] nArrayArray = new int[23][];
        int[] integerValues = new int[8];
        integerValues[1] = 6248;
        integerValues[2] = 6249;
        integerValues[3] = 6247;
        integerValues[4] = 219;
        integerValues[5] = 233;
        integerValues[6] = 200;
        integerValues[7] = 400;
        nArrayArray[0] = integerValues;
        nArrayArray[1] = new int[]{1, 6254, 6255, 6253, 216, 210, 256, 256};
        nArrayArray[2] = new int[]{2, 6207, 6208, 6206, 235, 222, 220, 256};
        nArrayArray[3] = new int[]{3, 6217, 6218, 6216, 212, 208, 200, 220};
        nArrayArray[4] = new int[]{4, 5453, 6114, 4443, 202, 224, 256, 256};
        nArrayArray[5] = new int[]{5, 6243, 6244, 6242, 211, 207, 256, 256};
        nArrayArray[6] = new int[]{6, 6212, 6213, 6211, 215, 230, 256, 256};
        nArrayArray[7] = new int[]{7, 6227, 6228, 6226, 196, 217, 256, 180};
        nArrayArray[8] = new int[]{8, 4273, 4274, 4272, 220, 209, 220, 256};
        nArrayArray[9] = new int[]{9, 6232, 6233, 6231, 195, 205, 256, 256};
        nArrayArray[10] = new int[]{10, 6259, 6260, 6258, 213, 226, 256, 256};
        nArrayArray[11] = new int[]{11, 4283, 4284, 4282, 199, 126, 256, 256};
        nArrayArray[12] = new int[]{12, 6264, 6265, 6263, 214, 228, 300, 220};
        nArrayArray[13] = new int[]{13, 6222, 6223, 6221, 229, 206, 256, 256};
        nArrayArray[14] = new int[]{14, 4417, 4438, 4416, 227, 223, 256, 256};
        nArrayArray[15] = new int[]{15, 6238, 6239, 6237, 236, 218, 200, 256};
        nArrayArray[16] = new int[]{16, 4278, 4279, 4277, 231, 231, 256, 256};
        nArrayArray[17] = new int[]{17, 4263, 4264, 4261, 197, 201, 200, 256};
        nArrayArray[18] = new int[]{18, 12123, 12124, 12122, 275, 276, 380, 380};
        nArrayArray[19] = new int[]{19, 313, 312, 310, 422, 424, 320, 175};
        nArrayArray[20] = new int[]{20, 4268, 4269, 4267, 194, 200, 320, 320};
        nArrayArray[21] = new int[]{21, 19567, 19568, 19566, 656, 657, 256, 256};
        nArrayArray[22] = new int[]{22, 19567, 19568, 19566, 656, 657, 256, 256};
        int[][] nArrayArray2 = nArrayArray;
        if (ServerSettings.clientBuild == 443) {
            player.packetSender.sendInterfaceText(
                    levelUpTitle(interfaceId),
                    360);
            player.packetSender.sendInterfaceText(
                    "Your " + SKILL_NAMES[interfaceId] + " level is now "
                            + this.getBaseLevel(interfaceId) + ".",
                    361);
            // Native group 211 has no skill graphic. The client uses this
            // invisible selector to reuse the corresponding 25px Skills-tab
            // sprite for every one of the 22 supported skills.
            player.packetSender.sendInterfaceText(SKILL_NAMES[interfaceId], 19581);
        } else {
            player.packetSender.sendInterfaceText(
                    levelUpTitle(interfaceId),
                    nArrayArray2[interfaceId][1]);
            player.packetSender.sendInterfaceText(
                    "Your " + SKILL_NAMES[interfaceId] + " level is now "
                            + this.getBaseLevel(interfaceId) + ".",
                    nArrayArray2[interfaceId][2]);
        }
            if (ServerSettings.progressiveXpMode != 0) {
                int baseLevel = this.getBaseLevel(interfaceId);
                int value2 = this.player.temporaryActionValue;
                double experienceRateForLevel = this.getExperienceRateForLevel(value2);
                double experienceRateForLevel2 = this.getExperienceRateForLevel(baseLevel);
                Object value3 = new DecimalFormat("#0.00");
                ((DecimalFormat)value3).setRoundingMode(RoundingMode.FLOOR);
                String text = ((NumberFormat)value3).format(experienceRateForLevel).replaceAll(",", ".");
                value3 = ((NumberFormat)value3).format(experienceRateForLevel2).replaceAll(",", ".");
                if (!text.equals(value3)) {
                    Player player2 = this.player;
                    player2.packetSender.sendGameMessage("XP rate increased to: " + (String)value3 + " (Previous XP rate was: " + text + ").");
                }
            }
            this.player.getUpdateState().setGraphic(199);
            this.player.packetSender.sendSoundEffect(323, 1, 0);
            int baseLevel2 = this.getBaseLevel(interfaceId) >= 50 ? 1 : 0;
            value = this.player;
            ((Player)value).packetSender.sendMusicJingle(nArrayArray2[interfaceId][baseLevel2 + 4], nArrayArray2[interfaceId][baseLevel2 + 6]);
            if (interfaceId == 19) {
                value = this.player;
                ((Player)value).packetSender.sendInterfacePosition(311, 0, 30);
                value = this.player;
                ((Player)value).packetSender.sendInterfaceModel(311, 200, 5340);
            }
            if (SkillManager.getLevelForExperience(this.experience[interfaceId])
                    == ServerSettings.maxLevel) {
                this.player.packetSender.sendGameMessage(
                        "Well done! You've achieved the highest possible level in this skill!");
            }
        this.player.packetSender.showChatboxInterface(
                ServerSettings.clientBuild == 443
                        ? 19580 // distinguished from ordinary two-line statements
                        : nArrayArray2[interfaceId][3]);
        this.player.setAppearanceUpdateRequired(true);
        if (ServerSettings.clientBuild != 443) {
            value = this.player;
            ((Player)value).packetSender.sendInterfaceText("Total Lvl: " + this.getTotalLevel(), 3984);
        }
    }

    public final int getCombatLevel() {
        int attack = SkillManager.getLevelForExperience(this.experience[0]);
        int defence = SkillManager.getLevelForExperience(this.experience[1]);
        int strength = SkillManager.getLevelForExperience(this.experience[2]);
        int hitpoints = SkillManager.getLevelForExperience(this.experience[3]);
        int ranged = SkillManager.getLevelForExperience(this.experience[4]);
        int prayer = SkillManager.getLevelForExperience(this.experience[5]);
        int magic = SkillManager.getLevelForExperience(this.experience[6]);

        int base = defence + hitpoints + prayer / 2;
        int melee = attack + strength;
        int rangedStyle = ranged * 3 / 2;
        int magicStyle = magic * 3 / 2;
        int offensive = Math.max(melee, Math.max(rangedStyle, magicStyle));

        return (10 * base + 13 * offensive) / 40;
    }

    public static int getMaxCombatLevel() {
        double value;
        int value2 = ServerSettings.maxLevel;
        int value3 = ServerSettings.maxLevel;
        int value4 = ServerSettings.maxLevel;
        int value5 = ServerSettings.maxLevel;
        int value6 = ServerSettings.maxLevel;
        int value7 = ServerSettings.maxLevel;
        int value8 = ServerSettings.maxLevel;
        double value9 = value3 + value5 + value6 / 2;
        double value10 = (value9 + 1.3 * (1.5 * (double)value8)) / 4.0;
        double value11 = (value9 + 1.3 * (1.5 * (double)value7)) / 4.0;
        double value12 = (value9 + 1.3 * (double)(value2 + value4)) / 4.0;
        if (value12 >= value11 && value12 >= value10) {
            return (int)value12;
        }
        if (value11 >= value12 && value11 >= value10) {
            return (int)value11;
        }
        return (int)value10;
    }

    public final int[] getCurrentLevels() {
        return this.currentLevels;
    }

    public final double[] getExperience() {
        return this.experience;
    }

    public final void setCurrentLevel(int level, int value22) {
        this.currentLevels[level] = value22;
    }

    public final boolean tryStartActionDelay(int delayTicks) {
        if (System.currentTimeMillis() >= this.actionDelayExpiresAtMillis) {
            this.actionDelayExpiresAtMillis = System.currentTimeMillis() + (long)delayTicks;
            return true;
        }
        return false;
    }

    public final boolean tryStartDrinkDelay(int delayTicks) {
        if (System.currentTimeMillis() >= this.drinkDelayExpiresAtMillis) {
            this.drinkDelayExpiresAtMillis = System.currentTimeMillis() + 600L;
            return true;
        }
        return false;
    }

    static Player getPlayer(SkillManager skillManager) {
        return skillManager.player;
    }

    static int[] getCurrentLevels(SkillManager skillManager) {
        return skillManager.currentLevels;
    }
}

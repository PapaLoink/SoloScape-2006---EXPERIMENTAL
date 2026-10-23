package com.rs2.model.gameplay.godwars;

import com.rs2.ServerSettings;
import com.rs2.model.GameplayHelper;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.combat.AttackStyleDefinition;
import com.rs2.model.gameplay.godwars.BandosStrongholdDoorTask;
import com.rs2.model.ground.GroundItem;
import com.rs2.model.ground.GroundItemManager;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.Npc;
import com.rs2.model.objects.DynamicObject;
import com.rs2.model.player.Player;
import com.rs2.model.skill.SkillManager;
import com.rs2.model.skill.agility.AgilityObstacleHandler;
import com.rs2.model.skill.prayer.PrayerManager;
import com.rs2.util.GameUtil;
import java.util.Arrays;
import java.util.List;

public final class GodWarsDungeonManager {
    public static int ropeShortcutConfigId = 1048;
    private static int armadylKillCountIndex = 0;
    private static int bandosKillCountIndex = 1;
    private static int saradominKillCountIndex = 2;
    private static int zamorakKillCountIndex = 3;
    private static int godswordShard1ItemId = 11710;
    private static int godswordShard2ItemId = 11712;
    private static int godswordShard3ItemId = 11714;
    private static int godswordShard1And2ItemId = 11686;
    private static int godswordShard1And3ItemId = 11688;
    private static int godswordShard2And3ItemId = 11692;
    private static int godswordBladeItemId = 11690;
    private static int armadylGodswordItemId = 11694;
    private static int bandosGodswordItemId = 11696;
    private static int saradominGodswordItemId = 11698;
    private static int zamorakGodswordItemId = 11700;
    private static int armadylHiltItemId = 11702;
    private static int bandosHiltItemId = 11704;
    private static int saradominHiltItemId = 11706;
    private static int zamorakHiltItemId = 11708;
    public static List armadylNpcIds = Arrays.asList(6222, 6223, 6225, 6227, 6229, 6230, 6231, 6232, 6233, 6234, 6235, 6236, 6237, 6238, 6239, 6240, 6241, 6242, 6243, 6244, 6245, 6246);
    public static List bandosNpcIds = Arrays.asList(6260, 6261, 6263, 6265, 6267, 6268, 6269, 6270, 6271, 6272, 6273, 6274, 6275, 6276, 6277, 6278, 6279, 6280, 6281, 6282, 6283);
    public static List saradominNpcIds = Arrays.asList(6247, 6248, 6250, 6252, 6254, 6255, 6256, 6257, 6258, 6259);
    public static List zamorakNpcIds = Arrays.asList(6203, 6204, 6206, 6208, 6210, 6211, 6212, 6213, 6214, 6215, 6216, 6217, 6218, 6219, 6220, 6221);
    private static String[] generalGraardorBattleCries = new String[]{"Death to our enemies!", "Brargh!", "Break their bones!", "For the glory of Bandos!", "Split their skulls!", "We feast on the bones of our enemies tonight!", "CHAAARGE!", "Crush them underfoot!", "All glory to Bandos!", "GRRRAAAAAR!", "FOR THE GLORY OF THE BIG HIGH WAR GOD!"};
    private static String[] commanderZilyanaBattleCries = new String[]{"Death to the enemies of the light!", "Slay the evil ones!", "Saradomin lend me strength!", "By the power of Saradomin!", "May Saradomin be my sword!", "Good will always triumph!", "Forward! Our allies are with us!", "Saradomin is with us!", "In the name of Saradomin!", "All praise Saradomin!", "Attack! Find the Godsword!"};

    private static boolean godWarsNpcsSpawned;

    public static void spawnGodWarsNpcs() {
        if (ServerSettings.freeToPlayWorld || godWarsNpcsSpawned) {
            return;
        }
        godWarsNpcsSpawned = true;
        GameplayHelper.spawnNpc(6203, 2924, 5322, 2, 1);
        GameplayHelper.spawnNpc(6204, 2920, 5328, 2, 1);
        GameplayHelper.spawnNpc(6206, 2932, 5328, 2, 1);
        GameplayHelper.spawnNpc(6208, 2932, 5322, 2, 1);
        GameplayHelper.spawnNpc(6210, 2927, 5340, 2, 1);
        GameplayHelper.spawnNpc(6210, 2884, 5325, 2, 1);
        GameplayHelper.spawnNpc(6210, 2837, 5336, 2, 1);
        GameplayHelper.spawnNpc(6210, 2876, 5326, 2, 1);
        GameplayHelper.spawnNpc(6210, 2898, 5306, 2, 1);
        GameplayHelper.spawnNpc(6210, 2886, 5325, 2, 1);
        GameplayHelper.spawnNpc(6210, 2838, 5336, 2, 1);
        GameplayHelper.spawnNpc(6210, 2919, 5345, 2, 1);
        GameplayHelper.spawnNpc(6211, 2920, 5353, 2, 1);
        GameplayHelper.spawnNpc(6211, 2902, 5357, 2, 1);
        GameplayHelper.spawnNpc(6211, 2885, 5315, 2, 1);
        GameplayHelper.spawnNpc(6211, 2898, 5313, 2, 1);
        GameplayHelper.spawnNpc(6211, 2908, 5354, 2, 1);
        GameplayHelper.spawnNpc(6211, 2924, 5354, 2, 1);
        GameplayHelper.spawnNpc(6211, 2925, 5352, 2, 1);
        GameplayHelper.spawnNpc(6211, 2928, 5355, 2, 1);
        GameplayHelper.spawnNpc(6212, 2920, 5339, 2, 1);
        GameplayHelper.spawnNpc(6212, 2836, 5289, 2, 1);
        GameplayHelper.spawnNpc(6212, 2869, 5287, 2, 1);
        GameplayHelper.spawnNpc(6212, 2867, 5333, 2, 1);
        GameplayHelper.spawnNpc(6212, 2921, 5293, 1, 1);
        GameplayHelper.spawnNpc(6212, 2877, 5321, 2, 1);
        GameplayHelper.spawnNpc(6212, 2888, 5326, 2, 1);
        GameplayHelper.spawnNpc(6212, 2872, 5318, 2, 1);
        GameplayHelper.spawnNpc(6212, 2894, 5286, 2, 1);
        GameplayHelper.spawnNpc(6212, 2896, 5312, 2, 1);
        GameplayHelper.spawnNpc(6212, 2894, 5322, 2, 1);
        GameplayHelper.spawnNpc(6212, 2836, 5349, 2, 1);
        GameplayHelper.spawnNpc(6212, 2858, 5267, 2, 1);
        GameplayHelper.spawnNpc(6213, 2894, 5353, 2, 1);
        GameplayHelper.spawnNpc(6213, 2838, 5347, 2, 1);
        GameplayHelper.spawnNpc(6213, 2872, 5318, 2, 1);
        GameplayHelper.spawnNpc(6213, 2875, 5313, 2, 1);
        GameplayHelper.spawnNpc(6213, 2865, 5327, 2, 1);
        GameplayHelper.spawnNpc(6213, 2886, 5293, 2, 1);
        GameplayHelper.spawnNpc(6213, 2909, 5297, 2, 1);
        GameplayHelper.spawnNpc(6213, 2919, 5293, 3, 1);
        GameplayHelper.spawnNpc(6213, 2843, 5352, 2, 1);
        GameplayHelper.spawnNpc(6213, 2908, 5346, 2, 1);
        GameplayHelper.spawnNpc(6213, 2913, 5355, 2, 1);
        GameplayHelper.spawnNpc(6213, 2916, 5345, 2, 1);
        GameplayHelper.spawnNpc(6213, 2919, 5349, 2, 1);
        GameplayHelper.spawnNpc(6213, 2922, 5356, 2, 1);
        GameplayHelper.spawnNpc(6213, 2927, 5354, 2, 1);
        GameplayHelper.spawnNpc(6213, 2928, 5350, 2, 1);
        GameplayHelper.spawnNpc(6213, 2931, 5357, 2, 1);
        GameplayHelper.spawnNpc(6213, 2911, 5338, 2, 1);
        GameplayHelper.spawnNpc(6213, 2925, 5295, 3, 1);
        GameplayHelper.spawnNpc(6213, 2862, 5290, 2, 1);
        GameplayHelper.spawnNpc(6213, 2919, 5293, 1, 1);
        GameplayHelper.spawnNpc(6214, 2919, 5284, 1, 1);
        GameplayHelper.spawnNpc(6214, 2862, 5292, 2, 1);
        GameplayHelper.spawnNpc(6214, 2864, 5319, 2, 1);
        GameplayHelper.spawnNpc(6214, 2828, 5337, 2, 1);
        GameplayHelper.spawnNpc(6214, 2878, 5327, 2, 1);
        GameplayHelper.spawnNpc(6214, 2884, 5312, 2, 1);
        GameplayHelper.spawnNpc(6214, 2883, 5320, 2, 1);
        GameplayHelper.spawnNpc(6214, 2895, 5309, 2, 1);
        GameplayHelper.spawnNpc(6214, 2853, 5327, 2, 1);
        GameplayHelper.spawnNpc(6214, 2854, 5345, 2, 1);
        GameplayHelper.spawnNpc(6214, 2861, 5292, 2, 1);
        GameplayHelper.spawnNpc(6214, 2881, 5291, 2, 1);
        GameplayHelper.spawnNpc(6214, 2892, 5328, 2, 1);
        GameplayHelper.spawnNpc(6214, 2828, 5331, 2, 1);
        GameplayHelper.spawnNpc(6214, 2924, 5292, 3, 1);
        GameplayHelper.spawnNpc(6214, 2918, 5281, 1, 1);
        GameplayHelper.spawnNpc(6214, 2918, 5280, 1, 1);
        GameplayHelper.spawnNpc(6215, 2926, 5345, 2, 1);
        GameplayHelper.spawnNpc(6215, 2888, 5361, 2, 1);
        GameplayHelper.spawnNpc(6215, 2856, 5349, 2, 1);
        GameplayHelper.spawnNpc(6215, 2839, 5275, 2, 1);
        GameplayHelper.spawnNpc(6215, 2880, 5293, 2, 1);
        GameplayHelper.spawnNpc(6215, 2886, 5313, 2, 1);
        GameplayHelper.spawnNpc(6215, 2862, 5334, 2, 1);
        GameplayHelper.spawnNpc(6215, 2881, 5320, 2, 1);
        GameplayHelper.spawnNpc(6215, 2886, 5325, 2, 1);
        GameplayHelper.spawnNpc(6215, 2862, 5337, 2, 1);
        GameplayHelper.spawnNpc(6215, 2845, 5286, 2, 1);
        GameplayHelper.spawnNpc(6215, 2889, 5322, 2, 1);
        GameplayHelper.spawnNpc(6215, 2919, 5359, 2, 1);
        GameplayHelper.spawnNpc(6215, 2855, 5352, 2, 1);
        GameplayHelper.spawnNpc(6215, 2923, 5357, 2, 1);
        GameplayHelper.spawnNpc(6215, 2891, 5353, 2, 1);
        GameplayHelper.spawnNpc(6215, 2832, 5288, 2, 1);
        GameplayHelper.spawnNpc(6216, 2888, 5290, 2, 1);
        GameplayHelper.spawnNpc(6216, 2865, 5306, 2, 1);
        GameplayHelper.spawnNpc(6216, 2871, 5312, 2, 1);
        GameplayHelper.spawnNpc(6216, 2867, 5304, 2, 1);
        GameplayHelper.spawnNpc(6216, 2871, 5281, 2, 1);
        GameplayHelper.spawnNpc(6216, 2864, 5279, 2, 1);
        GameplayHelper.spawnNpc(6217, 2884, 5307, 2, 1);
        GameplayHelper.spawnNpc(6217, 2899, 5304, 2, 1);
        GameplayHelper.spawnNpc(6217, 2915, 5270, 0, 1);
        GameplayHelper.spawnNpc(6218, 2906, 5347, 2, 1);
        GameplayHelper.spawnNpc(6218, 2913, 5358, 2, 1);
        GameplayHelper.spawnNpc(6218, 2856, 5267, 2, 1);
        GameplayHelper.spawnNpc(6218, 2911, 5268, 0, 1);
        GameplayHelper.spawnNpc(6218, 2887, 5318, 2, 1);
        GameplayHelper.spawnNpc(6218, 2888, 5314, 2, 1);
        GameplayHelper.spawnNpc(6218, 2859, 5328, 2, 1);
        GameplayHelper.spawnNpc(6218, 2861, 5294, 2, 1);
        GameplayHelper.spawnNpc(6218, 2931, 5345, 2, 1);
        GameplayHelper.spawnNpc(6218, 2903, 5353, 2, 1);
        GameplayHelper.spawnNpc(6218, 2836, 5290, 2, 1);
        GameplayHelper.spawnNpc(6218, 2907, 5346, 2, 1);
        GameplayHelper.spawnNpc(6218, 2921, 5351, 2, 1);
        GameplayHelper.spawnNpc(6218, 2858, 5266, 2, 1);
        GameplayHelper.spawnNpc(6218, 2916, 5269, 0, 1);
        GameplayHelper.spawnNpc(6219, 2915, 5340, 2, 1);
        GameplayHelper.spawnNpc(6220, 2931, 5356, 2, 1);
        GameplayHelper.spawnNpc(6220, 2846, 5286, 2, 1);
        GameplayHelper.spawnNpc(6220, 2937, 5351, 2, 1);
        GameplayHelper.spawnNpc(6220, 2848, 5268, 2, 1);
        GameplayHelper.spawnNpc(6221, 2906, 5354, 2, 1);
        GameplayHelper.spawnNpc(6221, 2886, 5354, 2, 1);
        GameplayHelper.spawnNpc(6221, 2885, 5352, 2, 1);
        GameplayHelper.spawnNpc(6221, 2901, 5362, 2, 1);
        GameplayHelper.spawnNpc(6221, 2914, 5344, 2, 1);
        GameplayHelper.spawnNpc(6221, 2918, 5343, 2, 1);
        GameplayHelper.spawnNpc(6221, 2918, 5361, 2, 1);
        GameplayHelper.spawnNpc(6221, 2923, 5345, 2, 1);
        GameplayHelper.spawnNpc(6221, 2924, 5352, 2, 1);
        GameplayHelper.spawnNpc(6222, 2831, 5301, 2, 1);
        GameplayHelper.spawnNpc(6223, 2833, 5306, 2, 1);
        GameplayHelper.spawnNpc(6225, 2840, 5302, 2, 1);
        GameplayHelper.spawnNpc(6227, 2836, 5298, 2, 1);
        GameplayHelper.spawnNpc(6229, 2841, 5285, 2, 1);
        GameplayHelper.spawnNpc(6229, 2841, 5289, 2, 1);
        GameplayHelper.spawnNpc(6229, 2856, 5264, 2, 1);
        GameplayHelper.spawnNpc(6229, 2842, 5282, 2, 1);
        GameplayHelper.spawnNpc(6230, 2843, 5265, 2, 1);
        GameplayHelper.spawnNpc(6230, 2851, 5273, 2, 1);
        GameplayHelper.spawnNpc(6230, 2846, 5262, 2, 1);
        GameplayHelper.spawnNpc(6231, 2870, 5265, 2, 1);
        GameplayHelper.spawnNpc(6231, 2869, 5266, 2, 1);
        GameplayHelper.spawnNpc(6231, 2855, 5273, 2, 1);
        GameplayHelper.spawnNpc(6231, 2840, 5263, 2, 1);
        GameplayHelper.spawnNpc(6231, 2831, 5285, 2, 1);
        GameplayHelper.spawnNpc(6232, 2872, 5261, 2, 1);
        GameplayHelper.spawnNpc(6232, 2863, 5308, 2, 1);
        GameplayHelper.spawnNpc(6232, 2872, 5288, 2, 1);
        GameplayHelper.spawnNpc(6233, 2861, 5261, 2, 1);
        GameplayHelper.spawnNpc(6233, 2852, 5264, 2, 1);
        GameplayHelper.spawnNpc(6233, 2865, 5263, 2, 1);
        GameplayHelper.spawnNpc(6234, 2862, 5313, 2, 1);
        GameplayHelper.spawnNpc(6234, 2839, 5293, 2, 1);
        GameplayHelper.spawnNpc(6234, 2842, 5264, 2, 1);
        GameplayHelper.spawnNpc(6235, 2864, 5270, 2, 1);
        GameplayHelper.spawnNpc(6235, 2869, 5314, 2, 1);
        GameplayHelper.spawnNpc(6235, 2859, 5303, 2, 1);
        GameplayHelper.spawnNpc(6235, 2863, 5293, 2, 1);
        GameplayHelper.spawnNpc(6235, 2862, 5286, 2, 1);
        GameplayHelper.spawnNpc(6236, 2854, 5271, 2, 1);
        GameplayHelper.spawnNpc(6236, 2872, 5267, 2, 1);
        GameplayHelper.spawnNpc(6236, 2858, 5269, 2, 1);
        GameplayHelper.spawnNpc(6237, 2840, 5290, 2, 1);
        GameplayHelper.spawnNpc(6237, 2841, 5269, 2, 1);
        GameplayHelper.spawnNpc(6237, 2833, 5292, 2, 1);
        GameplayHelper.spawnNpc(6238, 2874, 5308, 2, 1);
        GameplayHelper.spawnNpc(6238, 2851, 5276, 2, 1);
        GameplayHelper.spawnNpc(6238, 2864, 5268, 2, 1);
        GameplayHelper.spawnNpc(6239, 2875, 5298, 2, 1);
        GameplayHelper.spawnNpc(6239, 2846, 5288, 2, 1);
        GameplayHelper.spawnNpc(6239, 2845, 5281, 2, 1);
        GameplayHelper.spawnNpc(6240, 2837, 5269, 2, 1);
        GameplayHelper.spawnNpc(6240, 2860, 5300, 2, 1);
        GameplayHelper.spawnNpc(6240, 2852, 5311, 2, 1);
        GameplayHelper.spawnNpc(6240, 2879, 5286, 2, 1);
        GameplayHelper.spawnNpc(6241, 2863, 5272, 2, 1);
        GameplayHelper.spawnNpc(6241, 2850, 5269, 2, 1);
        GameplayHelper.spawnNpc(6242, 2836, 5272, 2, 1);
        GameplayHelper.spawnNpc(6243, 2848, 5275, 2, 1);
        GameplayHelper.spawnNpc(6243, 2869, 5304, 2, 1);
        GameplayHelper.spawnNpc(6243, 2850, 5292, 2, 1);
        GameplayHelper.spawnNpc(6243, 2857, 5300, 2, 1);
        GameplayHelper.spawnNpc(6243, 2874, 5287, 2, 1);
        GameplayHelper.spawnNpc(6244, 2847, 5299, 2, 1);
        GameplayHelper.spawnNpc(6244, 2844, 5283, 2, 1);
        GameplayHelper.spawnNpc(6245, 2861, 5299, 2, 1);
        GameplayHelper.spawnNpc(6245, 2833, 5290, 2, 1);
        GameplayHelper.spawnNpc(6245, 2851, 5262, 2, 1);
        GameplayHelper.spawnNpc(6246, 2855, 5311, 2, 1);
        GameplayHelper.spawnNpc(6246, 2872, 5299, 2, 1);
        GameplayHelper.spawnNpc(6246, 2872, 5315, 2, 1);
        GameplayHelper.spawnNpc(6246, 2856, 5284, 2, 1);
        GameplayHelper.spawnNpc(6247, 2894, 5265, 0, 1);
        GameplayHelper.spawnNpc(6248, 2892, 5273, 0, 1);
        GameplayHelper.spawnNpc(6250, 2902, 5272, 0, 1);
        GameplayHelper.spawnNpc(6252, 2903, 5261, 0, 1);
        GameplayHelper.spawnNpc(6254, 2917, 5272, 0, 1);
        GameplayHelper.spawnNpc(6254, 2898, 5295, 2, 1);
        GameplayHelper.spawnNpc(6254, 2881, 5283, 2, 1);
        GameplayHelper.spawnNpc(6254, 2883, 5311, 2, 1);
        GameplayHelper.spawnNpc(6254, 2894, 5301, 2, 1);
        GameplayHelper.spawnNpc(6254, 2882, 5284, 2, 1);
        GameplayHelper.spawnNpc(6254, 2890, 5292, 2, 1);
        GameplayHelper.spawnNpc(6254, 2896, 5296, 2, 1);
        GameplayHelper.spawnNpc(6254, 2905, 5299, 2, 1);
        GameplayHelper.spawnNpc(6254, 2905, 5301, 2, 1);
        GameplayHelper.spawnNpc(6254, 2921, 5283, 3, 1);
        GameplayHelper.spawnNpc(6254, 2925, 5292, 3, 1);
        GameplayHelper.spawnNpc(6254, 2906, 5299, 0, 1);
        GameplayHelper.spawnNpc(6254, 2910, 5297, 0, 1);
        GameplayHelper.spawnNpc(6254, 2915, 5270, 0, 1);
        GameplayHelper.spawnNpc(6254, 2910, 5269, 0, 1);
        GameplayHelper.spawnNpc(6255, 2922, 5290, 1, 1);
        GameplayHelper.spawnNpc(6255, 2880, 5304, 2, 1);
        GameplayHelper.spawnNpc(6255, 2884, 5311, 2, 1);
        GameplayHelper.spawnNpc(6255, 2885, 5311, 2, 1);
        GameplayHelper.spawnNpc(6255, 2887, 5312, 2, 1);
        GameplayHelper.spawnNpc(6255, 2886, 5313, 2, 1);
        GameplayHelper.spawnNpc(6255, 2895, 5310, 2, 1);
        GameplayHelper.spawnNpc(6255, 2884, 5280, 2, 1);
        GameplayHelper.spawnNpc(6255, 2885, 5293, 2, 1);
        GameplayHelper.spawnNpc(6255, 2886, 5291, 2, 1);
        GameplayHelper.spawnNpc(6255, 2894, 5285, 2, 1);
        GameplayHelper.spawnNpc(6255, 2893, 5286, 2, 1);
        GameplayHelper.spawnNpc(6255, 2905, 5292, 2, 1);
        GameplayHelper.spawnNpc(6255, 2908, 5297, 2, 1);
        GameplayHelper.spawnNpc(6255, 2910, 5297, 2, 1);
        GameplayHelper.spawnNpc(6255, 2897, 5308, 2, 1);
        GameplayHelper.spawnNpc(6255, 2919, 5299, 3, 1);
        GameplayHelper.spawnNpc(6255, 2921, 5292, 3, 1);
        GameplayHelper.spawnNpc(6255, 2924, 5288, 3, 1);
        GameplayHelper.spawnNpc(6255, 2925, 5287, 3, 1);
        GameplayHelper.spawnNpc(6255, 2908, 5300, 0, 1);
        GameplayHelper.spawnNpc(6255, 2907, 5294, 0, 1);
        GameplayHelper.spawnNpc(6255, 2923, 5293, 1, 1);
        GameplayHelper.spawnNpc(6255, 2919, 5298, 1, 1);
        GameplayHelper.spawnNpc(6255, 2922, 5291, 1, 1);
        GameplayHelper.spawnNpc(6255, 2917, 5285, 1, 1);
        GameplayHelper.spawnNpc(6255, 2905, 5295, 0, 1);
        GameplayHelper.spawnNpc(6256, 2922, 5285, 1, 1);
        GameplayHelper.spawnNpc(6256, 2886, 5295, 2, 1);
        GameplayHelper.spawnNpc(6256, 2891, 5300, 2, 1);
        GameplayHelper.spawnNpc(6256, 2889, 5310, 2, 1);
        GameplayHelper.spawnNpc(6256, 2887, 5299, 2, 1);
        GameplayHelper.spawnNpc(6256, 2891, 5297, 2, 1);
        GameplayHelper.spawnNpc(6256, 2884, 5283, 2, 1);
        GameplayHelper.spawnNpc(6256, 2885, 5286, 2, 1);
        GameplayHelper.spawnNpc(6256, 2886, 5294, 2, 1);
        GameplayHelper.spawnNpc(6256, 2896, 5286, 2, 1);
        GameplayHelper.spawnNpc(6256, 2908, 5302, 2, 1);
        GameplayHelper.spawnNpc(6256, 2919, 5290, 3, 1);
        GameplayHelper.spawnNpc(6256, 2923, 5298, 3, 1);
        GameplayHelper.spawnNpc(6256, 2924, 5298, 3, 1);
        GameplayHelper.spawnNpc(6256, 2911, 5298, 0, 1);
        GameplayHelper.spawnNpc(6256, 2919, 5293, 1, 1);
        GameplayHelper.spawnNpc(6256, 2917, 5294, 1, 1);
        GameplayHelper.spawnNpc(6256, 2923, 5284, 1, 1);
        GameplayHelper.spawnNpc(6256, 2916, 5267, 0, 1);
        GameplayHelper.spawnNpc(6257, 2913, 5267, 0, 1);
        GameplayHelper.spawnNpc(6257, 2892, 5291, 2, 1);
        GameplayHelper.spawnNpc(6257, 2895, 5313, 2, 1);
        GameplayHelper.spawnNpc(6257, 2879, 5329, 2, 1);
        GameplayHelper.spawnNpc(6257, 2886, 5311, 2, 1);
        GameplayHelper.spawnNpc(6257, 2888, 5305, 2, 1);
        GameplayHelper.spawnNpc(6257, 2877, 5293, 2, 1);
        GameplayHelper.spawnNpc(6257, 2894, 5284, 2, 1);
        GameplayHelper.spawnNpc(6257, 2900, 5287, 2, 1);
        GameplayHelper.spawnNpc(6257, 2905, 5302, 2, 1);
        GameplayHelper.spawnNpc(6257, 2898, 5307, 2, 1);
        GameplayHelper.spawnNpc(6257, 2918, 5292, 3, 1);
        GameplayHelper.spawnNpc(6257, 2924, 5286, 3, 1);
        GameplayHelper.spawnNpc(6257, 2924, 5292, 3, 1);
        GameplayHelper.spawnNpc(6257, 2924, 5297, 3, 1);
        GameplayHelper.spawnNpc(6257, 2926, 5285, 3, 1);
        GameplayHelper.spawnNpc(6257, 2921, 5289, 1, 1);
        GameplayHelper.spawnNpc(6257, 2923, 5294, 1, 1);
        GameplayHelper.spawnNpc(6257, 2922, 5285, 1, 1);
        GameplayHelper.spawnNpc(6257, 2915, 5273, 0, 1);
        GameplayHelper.spawnNpc(6257, 2915, 5271, 0, 1);
        GameplayHelper.spawnNpc(6258, 2918, 5289, 1, 1);
        GameplayHelper.spawnNpc(6258, 2884, 5287, 2, 1);
        GameplayHelper.spawnNpc(6258, 2895, 5294, 2, 1);
        GameplayHelper.spawnNpc(6258, 2897, 5307, 2, 1);
        GameplayHelper.spawnNpc(6258, 2918, 5298, 3, 1);
        GameplayHelper.spawnNpc(6258, 2917, 5291, 1, 1);
        GameplayHelper.spawnNpc(6258, 2916, 5269, 0, 1);
        GameplayHelper.spawnNpc(6259, 2890, 5304, 2, 1);
        GameplayHelper.spawnNpc(6259, 2886, 5299, 2, 1);
        GameplayHelper.spawnNpc(6259, 2891, 5297, 2, 1);
        GameplayHelper.spawnNpc(6259, 2890, 5291, 2, 1);
        GameplayHelper.spawnNpc(6259, 2900, 5286, 2, 1);
        GameplayHelper.spawnNpc(6259, 2905, 5303, 2, 1);
        GameplayHelper.spawnNpc(6259, 2918, 5282, 3, 1);
        GameplayHelper.spawnNpc(6259, 2924, 5295, 3, 1);
        GameplayHelper.spawnNpc(6259, 2905, 5297, 0, 1);
        GameplayHelper.spawnNpc(6259, 2920, 5291, 1, 1);
        GameplayHelper.spawnNpc(6259, 2930, 5290, 0, 1);
        GameplayHelper.spawnNpc(6260, 2870, 5359, 2, 1);
        GameplayHelper.spawnNpc(6261, 2874, 5366, 2, 1);
        GameplayHelper.spawnNpc(6263, 2872, 5354, 2, 1);
        GameplayHelper.spawnNpc(6265, 2866, 5358, 2, 1);
        GameplayHelper.spawnNpc(6267, 2851, 5310, 2, 1);
        GameplayHelper.spawnNpc(6268, 2856, 5355, 2, 1);
        GameplayHelper.spawnNpc(6268, 2838, 5358, 2, 1);
        GameplayHelper.spawnNpc(6268, 2841, 5319, 2, 1);
        GameplayHelper.spawnNpc(6268, 2856, 5331, 2, 1);
        GameplayHelper.spawnNpc(6268, 2854, 5325, 2, 1);
        GameplayHelper.spawnNpc(6268, 2873, 5323, 2, 1);
        GameplayHelper.spawnNpc(6268, 2860, 5321, 2, 1);
        GameplayHelper.spawnNpc(6268, 2868, 5334, 2, 1);
        GameplayHelper.spawnNpc(6268, 2856, 5328, 2, 1);
        GameplayHelper.spawnNpc(6268, 2855, 5332, 2, 1);
        GameplayHelper.spawnNpc(6268, 2845, 5339, 2, 1);
        GameplayHelper.spawnNpc(6268, 2831, 5320, 2, 1);
        GameplayHelper.spawnNpc(6268, 2847, 5334, 2, 1);
        GameplayHelper.spawnNpc(6268, 2858, 5349, 2, 1);
        GameplayHelper.spawnNpc(6268, 2829, 5339, 2, 1);
        GameplayHelper.spawnNpc(6268, 2856, 5361, 2, 1);
        GameplayHelper.spawnNpc(6268, 2839, 5360, 2, 1);
        GameplayHelper.spawnNpc(6269, 2846, 5351, 2, 1);
        GameplayHelper.spawnNpc(6269, 2846, 5339, 2, 1);
        GameplayHelper.spawnNpc(6269, 2840, 5327, 2, 1);
        GameplayHelper.spawnNpc(6269, 2836, 5345, 2, 1);
        GameplayHelper.spawnNpc(6269, 2834, 5328, 2, 1);
        GameplayHelper.spawnNpc(6269, 2857, 5359, 2, 1);
        GameplayHelper.spawnNpc(6269, 2846, 5357, 2, 1);
        GameplayHelper.spawnNpc(6270, 2845, 5336, 2, 1);
        GameplayHelper.spawnNpc(6270, 2848, 5344, 2, 1);
        GameplayHelper.spawnNpc(6270, 2842, 5328, 2, 1);
        GameplayHelper.spawnNpc(6270, 2844, 5349, 2, 1);
        GameplayHelper.spawnNpc(6270, 2836, 5323, 2, 1);
        GameplayHelper.spawnNpc(6270, 2832, 5353, 2, 1);
        GameplayHelper.spawnNpc(6270, 2848, 5358, 2, 1);
        GameplayHelper.spawnNpc(6271, 2840, 5330, 2, 1);
        GameplayHelper.spawnNpc(6271, 2859, 5328, 2, 1);
        GameplayHelper.spawnNpc(6271, 2873, 5317, 2, 1);
        GameplayHelper.spawnNpc(6271, 2862, 5332, 2, 1);
        GameplayHelper.spawnNpc(6271, 2869, 5332, 2, 1);
        GameplayHelper.spawnNpc(6272, 2838, 5326, 2, 1);
        GameplayHelper.spawnNpc(6272, 2870, 5313, 2, 1);
        GameplayHelper.spawnNpc(6272, 2865, 5332, 2, 1);
        GameplayHelper.spawnNpc(6272, 2838, 5334, 2, 1);
        GameplayHelper.spawnNpc(6272, 2837, 5353, 2, 1);
        GameplayHelper.spawnNpc(6273, 2832, 5324, 2, 1);
        GameplayHelper.spawnNpc(6273, 2864, 5325, 2, 1);
        GameplayHelper.spawnNpc(6273, 2873, 5325, 2, 1);
        GameplayHelper.spawnNpc(6273, 2873, 5324, 2, 1);
        GameplayHelper.spawnNpc(6273, 2865, 5336, 2, 1);
        GameplayHelper.spawnNpc(6273, 2840, 5333, 2, 1);
        GameplayHelper.spawnNpc(6273, 2836, 5336, 2, 1);
        GameplayHelper.spawnNpc(6273, 2838, 5354, 2, 1);
        GameplayHelper.spawnNpc(6274, 2858, 5325, 2, 1);
        GameplayHelper.spawnNpc(6274, 2873, 5319, 2, 1);
        GameplayHelper.spawnNpc(6274, 2872, 5307, 2, 1);
        GameplayHelper.spawnNpc(6274, 2867, 5333, 2, 1);
        GameplayHelper.spawnNpc(6274, 2866, 5333, 2, 1);
        GameplayHelper.spawnNpc(6274, 2839, 5331, 2, 1);
        GameplayHelper.spawnNpc(6274, 2837, 5355, 2, 1);
        GameplayHelper.spawnNpc(6275, 2865, 5340, 2, 1);
        GameplayHelper.spawnNpc(6275, 2869, 5326, 2, 1);
        GameplayHelper.spawnNpc(6275, 2875, 5318, 2, 1);
        GameplayHelper.spawnNpc(6275, 2875, 5314, 2, 1);
        GameplayHelper.spawnNpc(6275, 2878, 5313, 2, 1);
        GameplayHelper.spawnNpc(6275, 2879, 5322, 2, 1);
        GameplayHelper.spawnNpc(6275, 2859, 5317, 2, 1);
        GameplayHelper.spawnNpc(6275, 2858, 5319, 2, 1);
        GameplayHelper.spawnNpc(6275, 2878, 5320, 2, 1);
        GameplayHelper.spawnNpc(6275, 2863, 5337, 2, 1);
        GameplayHelper.spawnNpc(6275, 2862, 5338, 2, 1);
        GameplayHelper.spawnNpc(6275, 2867, 5341, 2, 1);
        GameplayHelper.spawnNpc(6275, 2852, 5312, 2, 1);
        GameplayHelper.spawnNpc(6276, 2856, 5360, 2, 1);
        GameplayHelper.spawnNpc(6276, 2854, 5346, 2, 1);
        GameplayHelper.spawnNpc(6276, 2845, 5332, 2, 1);
        GameplayHelper.spawnNpc(6276, 2845, 5344, 2, 1);
        GameplayHelper.spawnNpc(6276, 2848, 5349, 2, 1);
        GameplayHelper.spawnNpc(6276, 2837, 5347, 2, 1);
        GameplayHelper.spawnNpc(6276, 2835, 5324, 2, 1);
        GameplayHelper.spawnNpc(6276, 2835, 5321, 2, 1);
        GameplayHelper.spawnNpc(6276, 2858, 5349, 2, 1);
        GameplayHelper.spawnNpc(6276, 2830, 5339, 2, 1);
        GameplayHelper.spawnNpc(6276, 2828, 5332, 2, 1);
        GameplayHelper.spawnNpc(6276, 2846, 5358, 2, 1);
        GameplayHelper.spawnNpc(6276, 2855, 5356, 2, 1);
        GameplayHelper.spawnNpc(6277, 2843, 5341, 2, 1);
        GameplayHelper.spawnNpc(6277, 2843, 5331, 2, 1);
        GameplayHelper.spawnNpc(6277, 2847, 5341, 2, 1);
        GameplayHelper.spawnNpc(6277, 2852, 5350, 2, 1);
        GameplayHelper.spawnNpc(6277, 2844, 5341, 2, 1);
        GameplayHelper.spawnNpc(6277, 2843, 5347, 2, 1);
        GameplayHelper.spawnNpc(6277, 2836, 5325, 2, 1);
        GameplayHelper.spawnNpc(6277, 2826, 5335, 2, 1);
        GameplayHelper.spawnNpc(6277, 2837, 5351, 2, 1);
        GameplayHelper.spawnNpc(6277, 2845, 5353, 2, 1);
        GameplayHelper.spawnNpc(6277, 2840, 5361, 2, 1);
        GameplayHelper.spawnNpc(6278, 2845, 5359, 2, 1);
        GameplayHelper.spawnNpc(6278, 2830, 5318, 2, 1);
        GameplayHelper.spawnNpc(6278, 2848, 5342, 2, 1);
        GameplayHelper.spawnNpc(6278, 2846, 5332, 2, 1);
        GameplayHelper.spawnNpc(6278, 2843, 5320, 2, 1);
        GameplayHelper.spawnNpc(6278, 2838, 5329, 2, 1);
        GameplayHelper.spawnNpc(6278, 2855, 5349, 2, 1);
        GameplayHelper.spawnNpc(6278, 2837, 5342, 2, 1);
        GameplayHelper.spawnNpc(6278, 2827, 5331, 2, 1);
        GameplayHelper.spawnNpc(6278, 2830, 5315, 2, 1);
        GameplayHelper.spawnNpc(6278, 2845, 5352, 2, 1);
        GameplayHelper.spawnNpc(6278, 2839, 5360, 2, 1);
        GameplayHelper.spawnNpc(6278, 2851, 5359, 2, 1);
        GameplayHelper.spawnNpc(6278, 2860, 5353, 2, 1);
        GameplayHelper.spawnNpc(6279, 2857, 5315, 2, 1);
        GameplayHelper.spawnNpc(6279, 2859, 5318, 2, 1);
        GameplayHelper.spawnNpc(6279, 2858, 5329, 2, 1);
        GameplayHelper.spawnNpc(6279, 2879, 5326, 2, 1);
        GameplayHelper.spawnNpc(6280, 2869, 5332, 2, 1);
        GameplayHelper.spawnNpc(6280, 2871, 5313, 2, 1);
        GameplayHelper.spawnNpc(6280, 2879, 5330, 2, 1);
        GameplayHelper.spawnNpc(6280, 2877, 5327, 2, 1);
        GameplayHelper.spawnNpc(6281, 2865, 5316, 2, 1);
        GameplayHelper.spawnNpc(6281, 2861, 5333, 2, 1);
        GameplayHelper.spawnNpc(6282, 2874, 5318, 2, 1);
        GameplayHelper.spawnNpc(6282, 2859, 5316, 2, 1);
        GameplayHelper.spawnNpc(6282, 2878, 5329, 2, 1);
        GameplayHelper.spawnNpc(6282, 2858, 5330, 2, 1);
        GameplayHelper.spawnNpc(6283, 2877, 5326, 2, 1);
        GameplayHelper.spawnNpc(6283, 2857, 5328, 2, 1);
        GameplayHelper.spawnNpc(6283, 2861, 5315, 2, 1);
        new DynamicObject(26342, 2965, 3944, 0, 1, 10, 5791, 99999);
        int value = 2;
        value = 5280;
        value = 2869;
        value = 1;
        value = 9418;
        Object value2 = new ItemStack(9418, 1);
        value2 = new GroundItem((ItemStack)value2, new Position(2869, 5280, 2), (int)GameUtil.secondsToTicks(100L), true);
        GroundItemManager.getInstance().spawn((GroundItem)value2);
    }

    public static boolean handleBossBattleCry(Npc npc) {
        if (npc.getNpcId() == 6260) {
            if (GameUtil.randomInt(75) == 0) {
                String text = generalGraardorBattleCries[GameUtil.randomInt(generalGraardorBattleCries.length)];
                npc.getUpdateState().setForcedText(text);
            }
            return true;
        }
        if (npc.getNpcId() == 6247) {
            if (GameUtil.randomInt(75) == 0) {
                String text2 = commanderZilyanaBattleCries[GameUtil.randomInt(commanderZilyanaBattleCries.length)];
                npc.getUpdateState().setForcedText(text2);
            }
            return true;
        }
        return false;
    }

    public static void recordKillCount(Player player, int value5) {
        if (armadylNpcIds.contains(value5)) {
            player.godWarsKillCounts[0] = player.godWarsKillCounts[0] + 1;
            GodWarsDungeonManager.refreshKillCountOverlay(player);
            return;
        }
        if (bandosNpcIds.contains(value5)) {
            int value2 = bandosKillCountIndex;
            player.godWarsKillCounts[value2] = player.godWarsKillCounts[value2] + 1;
            GodWarsDungeonManager.refreshKillCountOverlay(player);
            return;
        }
        if (saradominNpcIds.contains(value5)) {
            int value3 = saradominKillCountIndex;
            player.godWarsKillCounts[value3] = player.godWarsKillCounts[value3] + 1;
            GodWarsDungeonManager.refreshKillCountOverlay(player);
            return;
        }
        if (zamorakNpcIds.contains(value5)) {
            int value4 = zamorakKillCountIndex;
            player.godWarsKillCounts[value4] = player.godWarsKillCounts[value4] + 1;
            GodWarsDungeonManager.refreshKillCountOverlay(player);
            return;
        }
    }

    public static void refreshKillCountOverlay(Player player) {
        int index = 0;
        while (index < player.godWarsKillCounts.length) {
            if (player.godWarsKillCounts[index] != player.lastDisplayedGodWarsKillCounts[index]) {
                Player player2 = player;
                player2.packetSender.sendInterfaceText("" + player.godWarsKillCounts[index], index + 19562);
                player.lastDisplayedGodWarsKillCounts[index] = player.godWarsKillCounts[index];
            }
            ++index;
        }
    }

    public static boolean handleGodswordShardOnAnvil(Player player, int value3, int value22) {
        if (value22 == 2783 && (value3 == godswordShard1ItemId || value3 == godswordShard2ItemId || value3 == godswordShard3ItemId || value3 == godswordShard1And2ItemId || value3 == godswordShard1And3ItemId || value3 == godswordShard2And3ItemId)) {
            if ((value3 == godswordShard1And2ItemId || value3 == godswordShard3ItemId) && player.getInventoryManager().containsItem(godswordShard1And2ItemId) && player.getInventoryManager().containsItem(godswordShard3ItemId)) {
                GodWarsDungeonManager.smithGodswordShardCombination(player, godswordShard1And2ItemId, godswordShard3ItemId, godswordBladeItemId);
                return true;
            }
            if ((value3 == godswordShard1And3ItemId || value3 == godswordShard2ItemId) && player.getInventoryManager().containsItem(godswordShard1And3ItemId) && player.getInventoryManager().containsItem(godswordShard2ItemId)) {
                GodWarsDungeonManager.smithGodswordShardCombination(player, godswordShard1And3ItemId, godswordShard2ItemId, godswordBladeItemId);
                return true;
            }
            if ((value3 == godswordShard2And3ItemId || value3 == godswordShard1ItemId) && player.getInventoryManager().containsItem(godswordShard2And3ItemId) && player.getInventoryManager().containsItem(godswordShard1ItemId)) {
                GodWarsDungeonManager.smithGodswordShardCombination(player, godswordShard2And3ItemId, godswordShard1ItemId, godswordBladeItemId);
                return true;
            }
            if ((value3 == godswordShard1ItemId || value3 == godswordShard2ItemId) && player.getInventoryManager().containsItem(godswordShard1ItemId) && player.getInventoryManager().containsItem(godswordShard2ItemId)) {
                GodWarsDungeonManager.smithGodswordShardCombination(player, godswordShard1ItemId, godswordShard2ItemId, godswordShard1And2ItemId);
                return true;
            }
            if ((value3 == godswordShard1ItemId || value3 == godswordShard3ItemId) && player.getInventoryManager().containsItem(godswordShard1ItemId) && player.getInventoryManager().containsItem(godswordShard3ItemId)) {
                GodWarsDungeonManager.smithGodswordShardCombination(player, godswordShard1ItemId, godswordShard3ItemId, godswordShard1And3ItemId);
                return true;
            }
            if ((value3 == godswordShard2ItemId || value3 == godswordShard3ItemId) && player.getInventoryManager().containsItem(godswordShard2ItemId) && player.getInventoryManager().containsItem(godswordShard3ItemId)) {
                GodWarsDungeonManager.smithGodswordShardCombination(player, godswordShard2ItemId, godswordShard3ItemId, godswordShard2And3ItemId);
                return true;
            }
        }
        return false;
    }

    private static void smithGodswordShardCombination(Player player, int value4, int value22, int value32) {
        if (player.getSkillManager().getBaseLevel(13) >= 80) {
            if (!player.getInventoryManager().getContainer().containsItem(2347)) {
                Player player2 = player;
                player2.packetSender.sendGameMessage("You need a hammer to smith on an anvil.");
                return;
            }
            player.getInventoryManager().removeItem(new ItemStack(value4, 1));
            player.getInventoryManager().removeItem(new ItemStack(value22, 1));
            player.getInventoryManager().addOrDropItem(new ItemStack(value32, 1));
            Player player3 = player;
            player3.packetSender.sendSoundEffect(468, 1, 0);
            player.getUpdateState().setAnimation(898);
            player.getSkillManager().addExperience(13, 100.0);
            return;
        }
        Player player4 = player;
        player4.packetSender.sendGameMessage("You need a " + SkillManager.SKILL_NAMES[13] + " level of 80" + " to do that.");
    }

    public static boolean dismantleGodsword(Player player, int value2) {
        if (value2 == armadylGodswordItemId) {
            if (player.getInventoryManager().getContainer().getFreeSlots() <= 0) {
                player.packetSender.sendGameMessage("You need more inventory space to do this.");
            } else {
                player.getInventoryManager().removeItem(new ItemStack(armadylGodswordItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(armadylHiltItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(godswordBladeItemId, 1));
            }
            return true;
        }
        if (value2 == bandosGodswordItemId) {
            if (player.getInventoryManager().getContainer().getFreeSlots() <= 0) {
                player.packetSender.sendGameMessage("You need more inventory space to do this.");
            } else {
                player.getInventoryManager().removeItem(new ItemStack(bandosGodswordItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(bandosHiltItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(godswordBladeItemId, 1));
            }
            return true;
        }
        if (value2 == saradominGodswordItemId) {
            if (player.getInventoryManager().getContainer().getFreeSlots() <= 0) {
                player.packetSender.sendGameMessage("You need more inventory space to do this.");
            } else {
                player.getInventoryManager().removeItem(new ItemStack(saradominGodswordItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(saradominHiltItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(godswordBladeItemId, 1));
            }
            return true;
        }
        if (value2 == zamorakGodswordItemId) {
            if (player.getInventoryManager().getContainer().getFreeSlots() <= 0) {
                player.packetSender.sendGameMessage("You need more inventory space to do this.");
            } else {
                player.getInventoryManager().removeItem(new ItemStack(zamorakGodswordItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(zamorakHiltItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(godswordBladeItemId, 1));
            }
            return true;
        }
        return false;
    }

    public static boolean handleGodWarsItemCombination(Player player, int itemId, int value2) {
        if (itemId == 954 || value2 == 954) {
            if (itemId == 9418 || value2 == 9418) {
                if (player.getSkillManager().getBaseLevel(13) >= 59 && player.getSkillManager().getBaseLevel(9) >= 59) {
                    player.getInventoryManager().removeItem(new ItemStack(954, 1));
                    player.getInventoryManager().removeItem(new ItemStack(9418, 1));
                    player.getInventoryManager().addOrDropItem(new ItemStack(9419, 1));
                } else {
                    player.packetSender.sendGameMessage("You need " + SkillManager.SKILL_NAMES[13] + " and " + SkillManager.SKILL_NAMES[9] + " level of 59" + " to do that.");
                }
                return true;
            }
        } else if (itemId == godswordBladeItemId || value2 == godswordBladeItemId) {
            if (itemId == armadylHiltItemId || value2 == armadylHiltItemId) {
                player.getInventoryManager().removeItem(new ItemStack(godswordBladeItemId, 1));
                player.getInventoryManager().removeItem(new ItemStack(armadylHiltItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(armadylGodswordItemId, 1));
                return true;
            }
            if (itemId == bandosHiltItemId || value2 == bandosHiltItemId) {
                player.getInventoryManager().removeItem(new ItemStack(godswordBladeItemId, 1));
                player.getInventoryManager().removeItem(new ItemStack(bandosHiltItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(bandosGodswordItemId, 1));
                return true;
            }
            if (itemId == saradominHiltItemId || value2 == saradominHiltItemId) {
                player.getInventoryManager().removeItem(new ItemStack(godswordBladeItemId, 1));
                player.getInventoryManager().removeItem(new ItemStack(saradominHiltItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(saradominGodswordItemId, 1));
                return true;
            }
            if (itemId == zamorakHiltItemId || value2 == zamorakHiltItemId) {
                player.getInventoryManager().removeItem(new ItemStack(godswordBladeItemId, 1));
                player.getInventoryManager().removeItem(new ItemStack(zamorakHiltItemId, 1));
                player.getInventoryManager().addOrDropItem(new ItemStack(zamorakGodswordItemId, 1));
                return true;
            }
        }
        return false;
    }

    public static boolean handleFirstObjectAction(Player player, int objectId, int value7, int value32) {
        if (objectId == 26342) {
            if (player.configStates[ropeShortcutConfigId] <= 0) {
                if (player.getInventoryManager().containsItem(954)) {
                    Player player2 = player;
                    player2.packetSender.sendConfig(ropeShortcutConfigId, 1);
                    player.configStates[GodWarsDungeonManager.ropeShortcutConfigId] = 1;
                    player.getInventoryManager().removeItem(new ItemStack(954, 1));
                } else {
                    Player player3 = player;
                    player3.packetSender.sendGameMessage("You don't have a rope to tie around the pillar.");
                }
            } else if (player.getSkillManager().getBaseLevel(2) >= 60 || player.getSkillManager().getBaseLevel(16) >= 60) {
                AttackStyleDefinition.startDelayedObjectMove(player, new Position(2881, 5310, 2));
                Player player4 = player;
                player4.packetSender.sendGameMessage("You climb down the rope.");
            } else {
                Player player5 = player;
                player5.packetSender.sendGameMessage("You need a " + SkillManager.SKILL_NAMES[2] + " or an " + SkillManager.SKILL_NAMES[16] + " level of 60" + " to do that.");
            }
            return true;
        }
        if (objectId == 26293) {
            AttackStyleDefinition.startDelayedObjectMove(player, new Position(2967, 3948, 0));
            return true;
        }
        if (objectId == 26444) {
            GodWarsDungeonManager.handleUpperRopeRockShortcut(player);
            return true;
        }
        if (objectId == 26445) {
            GodWarsDungeonManager.handleLowerRopeRockShortcut(player);
            return true;
        }
        if (objectId == 26298) {
            AttackStyleDefinition.startDelayedObjectMove(player, new Position(2920, 5276, 1));
            return true;
        }
        if (objectId == 26294) {
            AttackStyleDefinition.startDelayedObjectMove(player, new Position(2912, 5300, 2));
            return true;
        }
        if (objectId == 26426) {
            if (player.getPosition().getY() <= 5295) {
                if (player.godWarsKillCounts[0] < 40) {
                    Player player6 = player;
                    player6.packetSender.sendGameMessage("You need atleast 40 Armadyl kills to enter.");
                } else {
                    Player player7 = player;
                    player7.packetSender.queueRelativeMovementStep(0, player.getPosition().getY() <= 5295 ? 1 : -1, true);
                    player7 = player;
                    player7.packetSender.openSingleDoor(objectId, value7, value32, player.getPosition().getPlane());
                    player.godWarsKillCounts[0] = player.godWarsKillCounts[0] - 40;
                }
            } else {
                Player player8 = player;
                player8.packetSender.sendGameMessage("You can't leave through this door. The altar can teleport you out.");
            }
            return true;
        }
        if (objectId == 26425) {
            if (player.getPosition().getX() <= 2863) {
                if (player.godWarsKillCounts[bandosKillCountIndex] < 40) {
                    Player player9 = player;
                    player9.packetSender.sendGameMessage("You need atleast 40 Bandos kills to enter.");
                } else {
                    Player player10 = player;
                    player10.packetSender.queueRelativeMovementStep(player.getPosition().getX() <= 2863 ? 1 : -1, 0, true);
                    player10 = player;
                    player10.packetSender.openSingleDoor(objectId, value7, value32, player.getPosition().getPlane());
                    int value2 = bandosKillCountIndex;
                    player.godWarsKillCounts[value2] = player.godWarsKillCounts[value2] - 40;
                }
            } else {
                Player player11 = player;
                player11.packetSender.sendGameMessage("You can't leave through this door. The altar can teleport you out.");
            }
            return true;
        }
        if (objectId == 26427) {
            if (player.getPosition().getX() >= 2908) {
                if (player.godWarsKillCounts[saradominKillCountIndex] < 40) {
                    Player player12 = player;
                    player12.packetSender.sendGameMessage("You need atleast 40 Saradomin kills to enter.");
                } else {
                    Player player13 = player;
                    player13.packetSender.queueRelativeMovementStep(player.getPosition().getX() >= 2908 ? -1 : 1, 0, true);
                    player13 = player;
                    player13.packetSender.openSingleDoor(objectId, value7, value32, player.getPosition().getPlane());
                    int value4 = saradominKillCountIndex;
                    player.godWarsKillCounts[value4] = player.godWarsKillCounts[value4] - 40;
                }
            } else {
                Player player14 = player;
                player14.packetSender.sendGameMessage("You can't leave through this door. The altar can teleport you out.");
            }
            return true;
        }
        if (objectId == 26428) {
            if (player.getPosition().getY() >= 5332) {
                if (player.godWarsKillCounts[zamorakKillCountIndex] < 40) {
                    Player player15 = player;
                    player15.packetSender.sendGameMessage("You need atleast 40 Zamorak kills to enter.");
                } else {
                    Player player16 = player;
                    player16.packetSender.queueRelativeMovementStep(0, player.getPosition().getY() >= 5332 ? -1 : 1, true);
                    player16 = player;
                    player16.packetSender.openSingleDoor(objectId, value7, value32, player.getPosition().getPlane());
                    int value5 = zamorakKillCountIndex;
                    player.godWarsKillCounts[value5] = player.godWarsKillCounts[value5] - 40;
                }
            } else {
                Player player17 = player;
                player17.packetSender.sendGameMessage("You can't leave through this door. The altar can teleport you out.");
            }
            return true;
        }
        if (objectId == 26384) {
            if (player.getPosition().getX() >= 2851) {
                if (player.getSkillManager().getBaseLevel(2) >= 70) {
                    if (player.getInventoryManager().containsItem(2347)) {
                        player.setActionLocked(true);
                        Player player18 = player;
                        player18.packetSender.sendGameMessage("You bang on the big door.");
                        player18 = player;
                        player18.packetSender.sendSoundEffect(3845, 1, 30);
                        player.getUpdateState().setAnimation(7002, 0);
                        BandosStrongholdDoorTask bandosStrongholdDoorTask = new BandosStrongholdDoorTask(2, player);
                        World.getTaskScheduler().schedule(bandosStrongholdDoorTask);
                    } else {
                        Player player19 = player;
                        player19.packetSender.sendGameMessage("You need a hammer to bang on the door.");
                    }
                } else {
                    Player player20 = player;
                    player20.packetSender.sendGameMessage("You need a " + SkillManager.SKILL_NAMES[2] + " level of 70" + " to do that.");
                }
            } else {
                Player player21 = player;
                player21.packetSender.queueRelativeMovementStep(player.getPosition().getX() >= 2851 ? -1 : 1, 0, true);
            }
            return true;
        }
        if (objectId == 26439) {
            if (player.getSkillManager().getBaseLevel(3) >= 70 && player.getSkillManager().getCurrentLevels()[3] >= 70) {
                if (player.getPosition().getY() <= 5342) {
                    player.moveTo(new Position(2885, 5345, 2));
                    player.getSkillManager().setCurrentLevel(5, 0);
                    player.getSkillManager().refreshSkill(5);
                } else {
                    player.moveTo(new Position(2885, 5332, 2));
                }
            } else {
                Player player22 = player;
                player22.packetSender.sendGameMessage("You need a " + SkillManager.SKILL_NAMES[3] + " level of 70" + " to do that.");
            }
            return true;
        }
        if (objectId == 26303) {
            if (player.getSkillManager().getBaseLevel(4) >= 70) {
                int value6;
                String text;
                objectId = player.getEquipmentManager().getItemIdAtSlot(3);
                value7 = 0;
                value32 = 0;
                if (objectId > 0 && ((text = ItemDefinition.forId(objectId).getName().toLowerCase()).contains("crossbow") || text.contains("c'bow"))) {
                    value7 = 1;
                }
                if ((value6 = player.getEquipmentManager().getItemIdAtSlot(13)) == 9419) {
                    value32 = 1;
                }
                if (value32 == 0) {
                    Player player23 = player;
                    player23.packetSender.sendGameMessage("You need a mithril grapple to cross this.");
                    return true;
                }
                if (value7 == 0) {
                    Player player24 = player;
                    player24.packetSender.sendGameMessage("You need to wield a crossbow to fire a mithril grapple.");
                    return true;
                }
                player.getUpdateState().setGraphicHeight100(1036);
                AgilityObstacleHandler.startAgilityMovement(player, 0.0, 0, player.getPosition().getY() >= 5273 ? -11 : 11, 7081, -1, -1, 5, null, null);
            } else {
                Player player25 = player;
                player25.packetSender.sendGameMessage("You need a " + SkillManager.SKILL_NAMES[4] + " level of 70" + " to do that.");
            }
            return true;
        }
        if (objectId == 26288 || objectId == 26289 || objectId == 26287 || objectId == 26286) {
            if (player.getSingleCombatTimer().hasElapsed()) {
                if (System.currentTimeMillis() - player.godWarsLastAltarBlessingMillis >= 600000L) {
                    PrayerManager.rechargePrayerWithBoost(player);
                    player.godWarsLastAltarBlessingMillis = System.currentTimeMillis();
                } else {
                    Player player26 = player;
                    player26.packetSender.sendGameMessage("The gods blessed you recently - this time they ignore your prayers.");
                }
            } else {
                Player player27 = player;
                player27.packetSender.sendGameMessage("You can't use the altar while in combat.");
            }
            return true;
        }
        return false;
    }

    private static void handleUpperRopeRockShortcut(Player player) {
        if (player.configStates[ropeShortcutConfigId] < 3) {
            if (player.getInventoryManager().containsItem(954)) {
                Player player2 = player;
                player2.packetSender.sendConfig(ropeShortcutConfigId, 3);
                player.configStates[GodWarsDungeonManager.ropeShortcutConfigId] = 3;
                player.getInventoryManager().removeItem(new ItemStack(954, 1));
                return;
            }
            Player player3 = player;
            player3.packetSender.sendGameMessage("You don't have a rope to tie on this rock.");
            return;
        }
        if (player.getSkillManager().getBaseLevel(16) >= 70) {
            AttackStyleDefinition.startDelayedObjectMove(player, new Position(2915, 5300, 1));
            return;
        }
        Player player4 = player;
        player4.packetSender.sendGameMessage("You need an " + SkillManager.SKILL_NAMES[16] + " level of 70" + " to do that.");
    }

    private static void handleLowerRopeRockShortcut(Player player) {
        if (player.configStates[ropeShortcutConfigId] < 15) {
            if (player.getInventoryManager().containsItem(954)) {
                Player player2 = player;
                player2.packetSender.sendConfig(ropeShortcutConfigId, 15);
                player.configStates[GodWarsDungeonManager.ropeShortcutConfigId] = 15;
                player.getInventoryManager().removeItem(new ItemStack(954, 1));
                return;
            }
            Player player3 = player;
            player3.packetSender.sendGameMessage("You don't have a rope to tie on this rock.");
            return;
        }
        AttackStyleDefinition.startDelayedObjectMove(player, new Position(2919, 5274, 0));
    }

    public static boolean handleSecondObjectAction(Player player, int objectId) {
        if (objectId == 26444) {
            GodWarsDungeonManager.handleUpperRopeRockShortcut(player);
            return true;
        }
        if (objectId == 26445) {
            GodWarsDungeonManager.handleLowerRopeRockShortcut(player);
            return true;
        }
        if (objectId == 26288) {
            player.moveTo(new Position(2839, 5295, 2));
            return true;
        }
        if (objectId == 26289) {
            player.moveTo(new Position(2863, 5354, 2));
            return true;
        }
        if (objectId == 26287) {
            player.moveTo(new Position(2908, 5265, 0));
            return true;
        }
        if (objectId == 26286) {
            player.moveTo(new Position(2925, 5332, 2));
            return true;
        }
        return false;
    }
}


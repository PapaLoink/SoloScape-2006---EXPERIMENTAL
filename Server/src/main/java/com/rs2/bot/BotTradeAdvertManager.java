package com.rs2.bot;

import com.rs2.ServerSettings;
import com.rs2.bot.combat.BotCombatHelper;
import com.rs2.bot.combat.BotCombatLoadoutManager;
import com.rs2.model.GameplayHelper;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.GrandExchangeManager;
import com.rs2.model.player.Player;
import com.rs2.model.skill.SkillManager;
import com.rs2.util.GameUtil;
import java.util.ArrayList;

public final class BotTradeAdvertManager {
    public static ArrayList tradeAdvertOfferPool = new ArrayList();
    public static ArrayList commonTradeAdvertOfferPool = new ArrayList();
    private static ArrayList scamTradeAdvertOfferPool = new ArrayList();
    private static ArrayList combatTradeAdvertOfferPool = new ArrayList();
    public static int scammerChanceDivisor = 25;
    public static int commonItemChancePercent = 90;
    private static GameplayHelper[] configuredTradeAdvertOffers = new GameplayHelper[]{new GameplayHelper(0, new int[]{1}), new GameplayHelper(52, new int[]{100, 200, 500, 1000}), new GameplayHelper(115, new int[]{10, 20, 50, 100}), new GameplayHelper(121, new int[]{10, 20, 50, 100}), new GameplayHelper(127, new int[]{10, 20, 50, 100}), new GameplayHelper(133, new int[]{10, 20, 50, 100}), new GameplayHelper(139, new int[]{10, 20, 50, 100}), new GameplayHelper(145, new int[]{10, 20, 50, 100}), new GameplayHelper(151, new int[]{10, 20, 50, 100}), new GameplayHelper(157, new int[]{10, 20, 50, 100}), new GameplayHelper(163, new int[]{10, 20, 50, 100}), new GameplayHelper(169, new int[]{10, 20, 50, 100}), new GameplayHelper(175, new int[]{10, 20, 50, 100}), new GameplayHelper(181, new int[]{10, 20, 50, 100}), new GameplayHelper(187, new int[]{1, 2, 5, 10}), new GameplayHelper(189, new int[]{10, 20, 50, 100}), new GameplayHelper(221, new int[]{10, 20, 50, 100}), new GameplayHelper(223, new int[]{10, 20, 50, 100}), new GameplayHelper(225, new int[]{10, 20, 50, 100}), new GameplayHelper(231, new int[]{10, 20, 50, 100}), new GameplayHelper(237, new int[]{10, 20, 50, 100}), new GameplayHelper(239, new int[]{10, 20, 50, 100}), new GameplayHelper(243, new int[]{10, 20, 50, 100}), new GameplayHelper(245, new int[]{10, 20, 50, 100}), new GameplayHelper(247, new int[]{10, 20, 50, 100}), new GameplayHelper(249, new int[]{10, 20, 50, 100}), new GameplayHelper(251, new int[]{10, 20, 50, 100}), new GameplayHelper(253, new int[]{10, 20, 50, 100}), new GameplayHelper(255, new int[]{10, 20, 50, 100}), new GameplayHelper(257, new int[]{10, 20, 50, 100}), new GameplayHelper(259, new int[]{10, 20, 50, 100}), new GameplayHelper(261, new int[]{10, 20, 50, 100}), new GameplayHelper(263, new int[]{10, 20, 50, 100}), new GameplayHelper(265, new int[]{10, 20, 50, 100}), new GameplayHelper(267, new int[]{10, 20, 50, 100}), new GameplayHelper(269, new int[]{10, 20, 50, 100}), new GameplayHelper(314, new int[]{100, 200, 500, 1000}), new GameplayHelper(315, new int[]{100, 200, 500, 1000}), new GameplayHelper(317, new int[]{100, 200, 500, 1000}), new GameplayHelper(319, new int[]{100, 200, 500, 1000}), new GameplayHelper(321, new int[]{100, 200, 500, 1000}), new GameplayHelper(325, new int[]{100, 200, 500, 1000}), new GameplayHelper(327, new int[]{100, 200, 500, 1000}), new GameplayHelper(329, new int[]{100, 200, 500, 1000}), new GameplayHelper(331, new int[]{100, 200, 500, 1000}), new GameplayHelper(333, new int[]{100, 200, 500, 1000}), new GameplayHelper(335, new int[]{100, 200, 500, 1000}), new GameplayHelper(339, new int[]{100, 200, 500, 1000}), new GameplayHelper(341, new int[]{100, 200, 500, 1000}), new GameplayHelper(345, new int[]{100, 200, 500, 1000}), new GameplayHelper(347, new int[]{100, 200, 500, 1000}), new GameplayHelper(349, new int[]{100, 200, 500, 1000}), new GameplayHelper(351, new int[]{100, 200, 500, 1000}), new GameplayHelper(353, new int[]{100, 200, 500, 1000}), new GameplayHelper(355, new int[]{100, 200, 500, 1000}), new GameplayHelper(359, new int[]{100, 200, 500, 1000}), new GameplayHelper(361, new int[]{100, 200, 500, 1000}), new GameplayHelper(363, new int[]{100, 200, 500, 1000}), new GameplayHelper(365, new int[]{100, 200, 500, 1000}), new GameplayHelper(371, new int[]{100, 200, 500, 1000}), new GameplayHelper(373, new int[]{100, 200, 500, 1000}), new GameplayHelper(377, new int[]{100, 200, 500, 1000}), new GameplayHelper(379, new int[]{100, 200, 500, 1000}), new GameplayHelper(381, new int[]{1}), new GameplayHelper(383, new int[]{100, 200, 500, 1000}), new GameplayHelper(385, new int[]{100, 200, 500, 1000}), new GameplayHelper(389, new int[]{100, 200, 500, 1000}), new GameplayHelper(391, new int[]{100, 200, 500, 1000}), new GameplayHelper(395, new int[]{100, 200, 500, 1000}), new GameplayHelper(397, new int[]{100, 200, 500, 1000}), new GameplayHelper(405, new int[]{1}), new GameplayHelper(407, new int[]{1}), new GameplayHelper(434, new int[]{100, 200, 500, 1000}), new GameplayHelper(436, new int[]{100, 200, 500, 1000}), new GameplayHelper(438, new int[]{100, 200, 500, 1000}), new GameplayHelper(440, new int[]{100, 200, 500, 1000}), new GameplayHelper(442, new int[]{100, 200, 500, 1000}), new GameplayHelper(444, new int[]{100, 200, 500, 1000}), new GameplayHelper(447, new int[]{100, 200, 500, 1000}), new GameplayHelper(449, new int[]{100, 200, 500, 1000}), new GameplayHelper(451, new int[]{100, 200, 500, 1000}), new GameplayHelper(453, new int[]{100, 200, 500, 1000}), new GameplayHelper(464, new int[]{1}), new GameplayHelper(526, new int[]{100, 200, 500, 1000}), new GameplayHelper(528, new int[]{100, 200, 500, 1000}), new GameplayHelper(530, new int[]{100, 200, 500, 1000}), new GameplayHelper(532, new int[]{100, 200, 500, 1000}), new GameplayHelper(534, new int[]{100, 200, 500, 1000}), new GameplayHelper(536, new int[]{100, 200, 500, 1000}), new GameplayHelper(554, new int[]{500, 1000, 2000, 5000, 10000}), new GameplayHelper(555, new int[]{500, 1000, 2000, 5000, 10000}), new GameplayHelper(556, new int[]{500, 1000, 2000, 5000, 10000}), new GameplayHelper(557, new int[]{500, 1000, 2000, 5000, 10000}), new GameplayHelper(558, new int[]{500, 1000, 2000, 5000, 10000}), new GameplayHelper(559, new int[]{500, 1000, 2000, 5000, 10000}), new GameplayHelper(560, new int[]{100, 200, 500, 1000}), new GameplayHelper(561, new int[]{100, 200, 500, 1000}), new GameplayHelper(562, new int[]{100, 200, 500, 1000}), new GameplayHelper(563, new int[]{100, 200, 500, 1000}), new GameplayHelper(564, new int[]{100, 200, 500, 1000}), new GameplayHelper(565, new int[]{100, 200, 500, 1000}), new GameplayHelper(566, new int[]{100, 200, 500, 1000}), new GameplayHelper(800, new int[]{100, 200, 500, 1000}), new GameplayHelper(801, new int[]{100, 200, 500, 1000}), new GameplayHelper(802, new int[]{100, 200, 500, 1000}), new GameplayHelper(803, new int[]{100, 200, 500, 1000}), new GameplayHelper(804, new int[]{100, 200, 500, 1000}), new GameplayHelper(805, new int[]{100, 200, 500, 1000}), new GameplayHelper(806, new int[]{100, 200, 500, 1000}), new GameplayHelper(807, new int[]{100, 200, 500, 1000}), new GameplayHelper(808, new int[]{100, 200, 500, 1000}), new GameplayHelper(809, new int[]{100, 200, 500, 1000}), new GameplayHelper(810, new int[]{100, 200, 500, 1000}), new GameplayHelper(811, new int[]{100, 200, 500, 1000}), new GameplayHelper(851, new int[]{1}), new GameplayHelper(853, new int[]{1}), new GameplayHelper(855, new int[]{1}), new GameplayHelper(857, new int[]{1}), new GameplayHelper(859, new int[]{1}), new GameplayHelper(861, new int[]{1}), new GameplayHelper(863, new int[]{100, 200, 500, 1000}), new GameplayHelper(864, new int[]{100, 200, 500, 1000}), new GameplayHelper(865, new int[]{100, 200, 500, 1000}), new GameplayHelper(866, new int[]{100, 200, 500, 1000}), new GameplayHelper(867, new int[]{100, 200, 500, 1000}), new GameplayHelper(868, new int[]{100, 200, 500, 1000}), new GameplayHelper(869, new int[]{100, 200, 500, 1000}), new GameplayHelper(877, new int[]{100, 200, 500, 1000}), new GameplayHelper(879, new int[]{100, 200, 500, 1000}), new GameplayHelper(880, new int[]{100, 200, 500, 1000}), new GameplayHelper(881, new int[]{100, 200, 500, 1000}), new GameplayHelper(882, new int[]{500, 1000, 2000, 5000, 10000}), new GameplayHelper(884, new int[]{500, 1000, 2000, 5000, 10000}), new GameplayHelper(886, new int[]{500, 1000, 2000, 5000, 10000}), new GameplayHelper(888, new int[]{100, 200, 500, 1000}), new GameplayHelper(890, new int[]{100, 200, 500, 1000}), new GameplayHelper(892, new int[]{100, 200, 500, 1000}), new GameplayHelper(952, new int[]{1}), new GameplayHelper(962, new int[]{1}), new GameplayHelper(985, new int[]{1}), new GameplayHelper(987, new int[]{1}), new GameplayHelper(989, new int[]{1}), new GameplayHelper(991, new int[]{1}), new GameplayHelper(1033, new int[]{1}), new GameplayHelper(1035, new int[]{1}), new GameplayHelper(1038, new int[]{1}), new GameplayHelper(1040, new int[]{1}), new GameplayHelper(1042, new int[]{1}), new GameplayHelper(1044, new int[]{1}), new GameplayHelper(1046, new int[]{1}), new GameplayHelper(1048, new int[]{1}), new GameplayHelper(1050, new int[]{1}), new GameplayHelper(1053, new int[]{1}), new GameplayHelper(1055, new int[]{1}), new GameplayHelper(1057, new int[]{1}), new GameplayHelper(1059, new int[]{1}), new GameplayHelper(1061, new int[]{1}), new GameplayHelper(1063, new int[]{1}), new GameplayHelper(1065, new int[]{1}), new GameplayHelper(1069, new int[]{1}), new GameplayHelper(1071, new int[]{1}), new GameplayHelper(1073, new int[]{1}), new GameplayHelper(1077, new int[]{1}), new GameplayHelper(1079, new int[]{1}), new GameplayHelper(1083, new int[]{1}), new GameplayHelper(1085, new int[]{1}), new GameplayHelper(1089, new int[]{1}), new GameplayHelper(1091, new int[]{1}), new GameplayHelper(1093, new int[]{1}), new GameplayHelper(1095, new int[]{1}), new GameplayHelper(1097, new int[]{1}), new GameplayHelper(1099, new int[]{1}), new GameplayHelper(1105, new int[]{1}), new GameplayHelper(1107, new int[]{1}), new GameplayHelper(1109, new int[]{1}), new GameplayHelper(1111, new int[]{1}), new GameplayHelper(1113, new int[]{1}), new GameplayHelper(1119, new int[]{1}), new GameplayHelper(1121, new int[]{1}), new GameplayHelper(1123, new int[]{1}), new GameplayHelper(1125, new int[]{1}), new GameplayHelper(1127, new int[]{1}), new GameplayHelper(1129, new int[]{1}), new GameplayHelper(1131, new int[]{1}), new GameplayHelper(1133, new int[]{1}), new GameplayHelper(1135, new int[]{1}), new GameplayHelper(1141, new int[]{1}), new GameplayHelper(1143, new int[]{1}), new GameplayHelper(1145, new int[]{1}), new GameplayHelper(1147, new int[]{1}), new GameplayHelper(1149, new int[]{1}), new GameplayHelper(1151, new int[]{1}), new GameplayHelper(1157, new int[]{1}), new GameplayHelper(1159, new int[]{1}), new GameplayHelper(1161, new int[]{1}), new GameplayHelper(1163, new int[]{1}), new GameplayHelper(1165, new int[]{1}), new GameplayHelper(1167, new int[]{1}), new GameplayHelper(1169, new int[]{1}), new GameplayHelper(1177, new int[]{1}), new GameplayHelper(1179, new int[]{1}), new GameplayHelper(1181, new int[]{1}), new GameplayHelper(1183, new int[]{1}), new GameplayHelper(1185, new int[]{1}), new GameplayHelper(1187, new int[]{1}), new GameplayHelper(1193, new int[]{1}), new GameplayHelper(1195, new int[]{1}), new GameplayHelper(1197, new int[]{1}), new GameplayHelper(1199, new int[]{1}), new GameplayHelper(1201, new int[]{1}), new GameplayHelper(1207, new int[]{1}), new GameplayHelper(1209, new int[]{1}), new GameplayHelper(1211, new int[]{1}), new GameplayHelper(1213, new int[]{1}), new GameplayHelper(1215, new int[]{1}), new GameplayHelper(1217, new int[]{1}), new GameplayHelper(1231, new int[]{1}), new GameplayHelper(1265, new int[]{1}), new GameplayHelper(1267, new int[]{1}), new GameplayHelper(1269, new int[]{1}), new GameplayHelper(1271, new int[]{1}), new GameplayHelper(1273, new int[]{1}), new GameplayHelper(1275, new int[]{1}), new GameplayHelper(1281, new int[]{1}), new GameplayHelper(1283, new int[]{1}), new GameplayHelper(1285, new int[]{1}), new GameplayHelper(1287, new int[]{1}), new GameplayHelper(1289, new int[]{1}), new GameplayHelper(1295, new int[]{1}), new GameplayHelper(1297, new int[]{1}), new GameplayHelper(1299, new int[]{1}), new GameplayHelper(1301, new int[]{1}), new GameplayHelper(1303, new int[]{1}), new GameplayHelper(1305, new int[]{1}), new GameplayHelper(1311, new int[]{1}), new GameplayHelper(1313, new int[]{1}), new GameplayHelper(1315, new int[]{1}), new GameplayHelper(1317, new int[]{1}), new GameplayHelper(1319, new int[]{1}), new GameplayHelper(1325, new int[]{1}), new GameplayHelper(1327, new int[]{1}), new GameplayHelper(1329, new int[]{1}), new GameplayHelper(1331, new int[]{1}), new GameplayHelper(1333, new int[]{1}), new GameplayHelper(1339, new int[]{1}), new GameplayHelper(1341, new int[]{1}), new GameplayHelper(1343, new int[]{1}), new GameplayHelper(1345, new int[]{1}), new GameplayHelper(1347, new int[]{1}), new GameplayHelper(1349, new int[]{1}), new GameplayHelper(1351, new int[]{1}), new GameplayHelper(1353, new int[]{1}), new GameplayHelper(1355, new int[]{1}), new GameplayHelper(1357, new int[]{1}), new GameplayHelper(1359, new int[]{1}), new GameplayHelper(1361, new int[]{1}), new GameplayHelper(1365, new int[]{1}), new GameplayHelper(1367, new int[]{1}), new GameplayHelper(1369, new int[]{1}), new GameplayHelper(1371, new int[]{1}), new GameplayHelper(1373, new int[]{1}), new GameplayHelper(1377, new int[]{1}), new GameplayHelper(1379, new int[]{1}), new GameplayHelper(1381, new int[]{1}), new GameplayHelper(1383, new int[]{1}), new GameplayHelper(1385, new int[]{1}), new GameplayHelper(1387, new int[]{1}), new GameplayHelper(1391, new int[]{10, 20, 50, 100}), new GameplayHelper(1393, new int[]{1}), new GameplayHelper(1395, new int[]{1}), new GameplayHelper(1397, new int[]{1}), new GameplayHelper(1399, new int[]{1}), new GameplayHelper(1401, new int[]{1}), new GameplayHelper(1403, new int[]{1}), new GameplayHelper(1405, new int[]{1}), new GameplayHelper(1407, new int[]{1}), new GameplayHelper(1424, new int[]{1}), new GameplayHelper(1426, new int[]{1}), new GameplayHelper(1428, new int[]{1}), new GameplayHelper(1430, new int[]{1}), new GameplayHelper(1432, new int[]{1}), new GameplayHelper(1434, new int[]{1}), new GameplayHelper(1436, new int[]{100, 200, 500, 1000, 2000}), new GameplayHelper(1438, new int[]{1}), new GameplayHelper(1440, new int[]{1}), new GameplayHelper(1442, new int[]{1}), new GameplayHelper(1444, new int[]{1}), new GameplayHelper(1446, new int[]{1}), new GameplayHelper(1448, new int[]{1}), new GameplayHelper(1452, new int[]{1}), new GameplayHelper(1454, new int[]{1}), new GameplayHelper(1456, new int[]{1}), new GameplayHelper(1462, new int[]{1}), new GameplayHelper(1470, new int[]{1}), new GameplayHelper(1472, new int[]{1}), new GameplayHelper(1474, new int[]{1}), new GameplayHelper(1476, new int[]{1}), new GameplayHelper(1511, new int[]{100, 200, 500, 1000}), new GameplayHelper(1513, new int[]{100, 200, 500, 1000}), new GameplayHelper(1515, new int[]{100, 200, 500, 1000}), new GameplayHelper(1517, new int[]{100, 200, 500, 1000}), new GameplayHelper(1519, new int[]{100, 200, 500, 1000}), new GameplayHelper(1521, new int[]{100, 200, 500, 1000}), new GameplayHelper(1539, new int[]{100, 200, 500, 1000}), new GameplayHelper(1601, new int[]{1, 2, 5, 10}), new GameplayHelper(1603, new int[]{1, 2, 5, 10}), new GameplayHelper(1605, new int[]{1, 2, 5, 10}), new GameplayHelper(1607, new int[]{1, 2, 5, 10}), new GameplayHelper(1609, new int[]{10, 20, 50, 100}), new GameplayHelper(1611, new int[]{10, 20, 50, 100}), new GameplayHelper(1613, new int[]{10, 20, 50, 100}), new GameplayHelper(1615, new int[]{1, 2, 5, 10}), new GameplayHelper(1617, new int[]{1, 2, 5, 10}), new GameplayHelper(1619, new int[]{1, 2, 5, 10}), new GameplayHelper(1621, new int[]{1, 2, 5, 10}), new GameplayHelper(1623, new int[]{1, 2, 5, 10}), new GameplayHelper(1625, new int[]{10, 20, 50, 100}), new GameplayHelper(1627, new int[]{10, 20, 50, 100}), new GameplayHelper(1629, new int[]{10, 20, 50, 100}), new GameplayHelper(1631, new int[]{1, 2, 5, 10}), new GameplayHelper(1635, new int[]{1}), new GameplayHelper(1637, new int[]{1}), new GameplayHelper(1639, new int[]{1}), new GameplayHelper(1641, new int[]{1}), new GameplayHelper(1643, new int[]{1}), new GameplayHelper(1645, new int[]{1}), new GameplayHelper(1654, new int[]{1}), new GameplayHelper(1656, new int[]{1}), new GameplayHelper(1658, new int[]{1}), new GameplayHelper(1660, new int[]{1}), new GameplayHelper(1662, new int[]{1}), new GameplayHelper(1664, new int[]{1}), new GameplayHelper(1712, new int[]{1}), new GameplayHelper(1725, new int[]{1}), new GameplayHelper(1727, new int[]{1}), new GameplayHelper(1729, new int[]{1}), new GameplayHelper(1731, new int[]{1}), new GameplayHelper(1716, new int[]{1}), new GameplayHelper(1718, new int[]{1}), new GameplayHelper(1722, new int[]{1}), new GameplayHelper(1724, new int[]{1}), new GameplayHelper(1739, new int[]{100, 200, 500, 1000}), new GameplayHelper(1747, new int[]{100, 200, 500, 1000}), new GameplayHelper(1749, new int[]{100, 200, 500, 1000}), new GameplayHelper(1751, new int[]{100, 200, 500, 1000}), new GameplayHelper(1753, new int[]{100, 200, 500, 1000}), new GameplayHelper(1777, new int[]{100, 200, 500, 1000}), new GameplayHelper(1779, new int[]{100, 200, 500, 1000}), new GameplayHelper(1891, new int[]{10, 20, 50, 100}), new GameplayHelper(1897, new int[]{10, 20, 50, 100}), new GameplayHelper(1949, new int[]{1}), new GameplayHelper(1959, new int[]{1}), new GameplayHelper(1961, new int[]{1}), new GameplayHelper(1969, new int[]{10, 20, 50, 100}), new GameplayHelper(1971, new int[]{10, 20, 50, 100}), new GameplayHelper(1973, new int[]{10, 20, 50, 100}), new GameplayHelper(1975, new int[]{10, 20, 50, 100}), new GameplayHelper(1993, new int[]{10, 20, 50, 100}), new GameplayHelper(2003, new int[]{10, 20, 50, 100}), new GameplayHelper(2142, new int[]{10, 20, 50, 100}), new GameplayHelper(2152, new int[]{10, 20, 50, 100}), new GameplayHelper(2289, new int[]{10, 20, 50, 100}), new GameplayHelper(2293, new int[]{10, 20, 50, 100}), new GameplayHelper(2297, new int[]{10, 20, 50, 100}), new GameplayHelper(2301, new int[]{10, 20, 50, 100}), new GameplayHelper(2309, new int[]{10, 20, 50, 100}), new GameplayHelper(2323, new int[]{10, 20, 50, 100}), new GameplayHelper(2325, new int[]{10, 20, 50, 100}), new GameplayHelper(2327, new int[]{10, 20, 50, 100}), new GameplayHelper(2349, new int[]{100, 200, 500, 1000}), new GameplayHelper(2351, new int[]{100, 200, 500, 1000}), new GameplayHelper(2353, new int[]{100, 200, 500, 1000}), new GameplayHelper(2355, new int[]{100, 200, 500, 1000}), new GameplayHelper(2357, new int[]{100, 200, 500, 1000}), new GameplayHelper(2359, new int[]{100, 200, 500, 1000}), new GameplayHelper(2361, new int[]{100, 200, 500, 1000}), new GameplayHelper(2363, new int[]{100, 200, 500, 1000}), new GameplayHelper(2366, new int[]{1}), new GameplayHelper(2368, new int[]{1}), new GameplayHelper(2454, new int[]{10, 20, 50, 100}), new GameplayHelper(2481, new int[]{10, 20, 50, 100}), new GameplayHelper(2487, new int[]{1}), new GameplayHelper(2489, new int[]{1}), new GameplayHelper(2491, new int[]{1}), new GameplayHelper(2493, new int[]{1}), new GameplayHelper(2495, new int[]{1}), new GameplayHelper(2497, new int[]{1}), new GameplayHelper(2499, new int[]{1}), new GameplayHelper(2501, new int[]{1}), new GameplayHelper(2503, new int[]{1}), new GameplayHelper(2577, new int[]{1}), new GameplayHelper(2579, new int[]{1}), new GameplayHelper(2581, new int[]{1}), new GameplayHelper(2583, new int[]{1}), new GameplayHelper(2585, new int[]{1}), new GameplayHelper(2587, new int[]{1}), new GameplayHelper(2589, new int[]{1}), new GameplayHelper(2591, new int[]{1}), new GameplayHelper(2593, new int[]{1}), new GameplayHelper(2595, new int[]{1}), new GameplayHelper(2597, new int[]{1}), new GameplayHelper(2599, new int[]{1}), new GameplayHelper(2601, new int[]{1}), new GameplayHelper(2603, new int[]{1}), new GameplayHelper(2605, new int[]{1}), new GameplayHelper(2607, new int[]{1}), new GameplayHelper(2609, new int[]{1}), new GameplayHelper(2611, new int[]{1}), new GameplayHelper(2613, new int[]{1}), new GameplayHelper(2615, new int[]{1}), new GameplayHelper(2617, new int[]{1}), new GameplayHelper(2619, new int[]{1}), new GameplayHelper(2621, new int[]{1}), new GameplayHelper(2623, new int[]{1}), new GameplayHelper(2625, new int[]{1}), new GameplayHelper(2627, new int[]{1}), new GameplayHelper(2629, new int[]{1}), new GameplayHelper(2631, new int[]{1}), new GameplayHelper(2633, new int[]{1}), new GameplayHelper(2635, new int[]{1}), new GameplayHelper(2637, new int[]{1}), new GameplayHelper(2639, new int[]{1}), new GameplayHelper(2641, new int[]{1}), new GameplayHelper(2643, new int[]{1}), new GameplayHelper(2645, new int[]{1}), new GameplayHelper(2647, new int[]{1}), new GameplayHelper(2649, new int[]{1}), new GameplayHelper(2651, new int[]{1}), new GameplayHelper(2653, new int[]{1}), new GameplayHelper(2655, new int[]{1}), new GameplayHelper(2657, new int[]{1}), new GameplayHelper(2659, new int[]{1}), new GameplayHelper(2661, new int[]{1}), new GameplayHelper(2663, new int[]{1}), new GameplayHelper(2665, new int[]{1}), new GameplayHelper(2667, new int[]{1}), new GameplayHelper(2669, new int[]{1}), new GameplayHelper(2671, new int[]{1}), new GameplayHelper(2673, new int[]{1}), new GameplayHelper(2675, new int[]{1}), new GameplayHelper(2859, new int[]{100, 200, 500, 1000}), new GameplayHelper(2862, new int[]{100, 200, 500, 1000}), new GameplayHelper(2970, new int[]{10, 20, 50, 100}), new GameplayHelper(3018, new int[]{10, 20, 50, 100}), new GameplayHelper(3026, new int[]{10, 20, 50, 100}), new GameplayHelper(3042, new int[]{10, 20, 50, 100}), new GameplayHelper(3053, new int[]{1}), new GameplayHelper(3054, new int[]{1}), new GameplayHelper(3093, new int[]{100, 200, 500, 1000}), new GameplayHelper(3122, new int[]{1}), new GameplayHelper(3125, new int[]{100, 200, 500, 1000}), new GameplayHelper(3142, new int[]{100, 200, 500, 1000}), new GameplayHelper(3144, new int[]{100, 200, 500, 1000}), new GameplayHelper(3379, new int[]{100, 200, 500, 1000}), new GameplayHelper(3381, new int[]{100, 200, 500, 1000}), new GameplayHelper(3385, new int[]{1}), new GameplayHelper(3387, new int[]{1}), new GameplayHelper(3389, new int[]{1}), new GameplayHelper(3391, new int[]{1}), new GameplayHelper(3393, new int[]{1}), new GameplayHelper(3470, new int[]{1, 2, 5, 10}), new GameplayHelper(3472, new int[]{1}), new GameplayHelper(3473, new int[]{1}), new GameplayHelper(3474, new int[]{1}), new GameplayHelper(3475, new int[]{1}), new GameplayHelper(3476, new int[]{1}), new GameplayHelper(3477, new int[]{1}), new GameplayHelper(3478, new int[]{1}), new GameplayHelper(3479, new int[]{1}), new GameplayHelper(3480, new int[]{1}), new GameplayHelper(3481, new int[]{1}), new GameplayHelper(3483, new int[]{1}), new GameplayHelper(3485, new int[]{1}), new GameplayHelper(3486, new int[]{1}), new GameplayHelper(3488, new int[]{1}), new GameplayHelper(3827, new int[]{1}), new GameplayHelper(3828, new int[]{1}), new GameplayHelper(3829, new int[]{1}), new GameplayHelper(3830, new int[]{1}), new GameplayHelper(3831, new int[]{1}), new GameplayHelper(3832, new int[]{1}), new GameplayHelper(3833, new int[]{1}), new GameplayHelper(3834, new int[]{1}), new GameplayHelper(3835, new int[]{1}), new GameplayHelper(3836, new int[]{1}), new GameplayHelper(3837, new int[]{1}), new GameplayHelper(3838, new int[]{1}), new GameplayHelper(4089, new int[]{1}), new GameplayHelper(4091, new int[]{1}), new GameplayHelper(4093, new int[]{1}), new GameplayHelper(4095, new int[]{1}), new GameplayHelper(4097, new int[]{1}), new GameplayHelper(4099, new int[]{1}), new GameplayHelper(4101, new int[]{1}), new GameplayHelper(4103, new int[]{1}), new GameplayHelper(4105, new int[]{1}), new GameplayHelper(4107, new int[]{1}), new GameplayHelper(4109, new int[]{1}), new GameplayHelper(4111, new int[]{1}), new GameplayHelper(4113, new int[]{1}), new GameplayHelper(4115, new int[]{1}), new GameplayHelper(4117, new int[]{1}), new GameplayHelper(4119, new int[]{1}), new GameplayHelper(4121, new int[]{1}), new GameplayHelper(4123, new int[]{1}), new GameplayHelper(4125, new int[]{1}), new GameplayHelper(4127, new int[]{1}), new GameplayHelper(4129, new int[]{1}), new GameplayHelper(4131, new int[]{1}), new GameplayHelper(4151, new int[]{1}), new GameplayHelper(4153, new int[]{1}), new GameplayHelper(4587, new int[]{1}), new GameplayHelper(4708, new int[]{1}), new GameplayHelper(4710, new int[]{1}), new GameplayHelper(4712, new int[]{1}), new GameplayHelper(4714, new int[]{1}), new GameplayHelper(4716, new int[]{1}), new GameplayHelper(4718, new int[]{1}), new GameplayHelper(4720, new int[]{1}), new GameplayHelper(4722, new int[]{1}), new GameplayHelper(4724, new int[]{1}), new GameplayHelper(4726, new int[]{1}), new GameplayHelper(4728, new int[]{1}), new GameplayHelper(4730, new int[]{1}), new GameplayHelper(4732, new int[]{1}), new GameplayHelper(4734, new int[]{1}), new GameplayHelper(4736, new int[]{1}), new GameplayHelper(4738, new int[]{1}), new GameplayHelper(4740, new int[]{100, 200, 500, 1000}), new GameplayHelper(4745, new int[]{1}), new GameplayHelper(4747, new int[]{1}), new GameplayHelper(4749, new int[]{1}), new GameplayHelper(4751, new int[]{1}), new GameplayHelper(4753, new int[]{1}), new GameplayHelper(4755, new int[]{1}), new GameplayHelper(4757, new int[]{1}), new GameplayHelper(4759, new int[]{1}), new GameplayHelper(5001, new int[]{100, 200, 500, 1000}), new GameplayHelper(5003, new int[]{100, 200, 500, 1000}), new GameplayHelper(5014, new int[]{1}), new GameplayHelper(5016, new int[]{1}), new GameplayHelper(5018, new int[]{1}), new GameplayHelper(5096, new int[]{10, 20, 50, 100}), new GameplayHelper(5097, new int[]{10, 20, 50, 100}), new GameplayHelper(5098, new int[]{10, 20, 50, 100}), new GameplayHelper(5099, new int[]{10, 20, 50, 100}), new GameplayHelper(5100, new int[]{10, 20, 50, 100}), new GameplayHelper(5101, new int[]{10, 20, 50, 100}), new GameplayHelper(5102, new int[]{10, 20, 50, 100}), new GameplayHelper(5103, new int[]{10, 20, 50, 100}), new GameplayHelper(5104, new int[]{10, 20, 50, 100}), new GameplayHelper(5105, new int[]{10, 20, 50, 100}), new GameplayHelper(5106, new int[]{10, 20, 50, 100}), new GameplayHelper(5280, new int[]{1, 2, 5, 10}), new GameplayHelper(5281, new int[]{1, 2, 5, 10}), new GameplayHelper(5282, new int[]{1, 2, 5, 10}), new GameplayHelper(5283, new int[]{1, 2, 5, 10}), new GameplayHelper(5284, new int[]{1, 2, 5, 10}), new GameplayHelper(5285, new int[]{1, 2, 5, 10}), new GameplayHelper(5286, new int[]{1, 2, 5, 10}), new GameplayHelper(5287, new int[]{1, 2, 5, 10}), new GameplayHelper(5288, new int[]{1, 2, 5, 10}), new GameplayHelper(5289, new int[]{1, 2, 5, 10}), new GameplayHelper(5290, new int[]{1, 2, 5, 10}), new GameplayHelper(5291, new int[]{10, 20, 50, 100}), new GameplayHelper(5292, new int[]{10, 20, 50, 100}), new GameplayHelper(5293, new int[]{10, 20, 50, 100}), new GameplayHelper(5294, new int[]{10, 20, 50, 100}), new GameplayHelper(5295, new int[]{10, 20, 50, 100}), new GameplayHelper(5296, new int[]{10, 20, 50, 100}), new GameplayHelper(5297, new int[]{10, 20, 50, 100}), new GameplayHelper(5298, new int[]{10, 20, 50, 100}), new GameplayHelper(5299, new int[]{10, 20, 50, 100}), new GameplayHelper(5300, new int[]{10, 20, 50, 100}), new GameplayHelper(5301, new int[]{10, 20, 50, 100}), new GameplayHelper(5302, new int[]{10, 20, 50, 100}), new GameplayHelper(5303, new int[]{10, 20, 50, 100}), new GameplayHelper(5304, new int[]{10, 20, 50, 100}), new GameplayHelper(5305, new int[]{10, 20, 50, 100}), new GameplayHelper(5306, new int[]{10, 20, 50, 100}), new GameplayHelper(5307, new int[]{10, 20, 50, 100}), new GameplayHelper(5308, new int[]{10, 20, 50, 100}), new GameplayHelper(5309, new int[]{10, 20, 50, 100}), new GameplayHelper(5310, new int[]{10, 20, 50, 100}), new GameplayHelper(5311, new int[]{10, 20, 50, 100}), new GameplayHelper(5312, new int[]{1, 2, 5, 10}), new GameplayHelper(5313, new int[]{1, 2, 5, 10}), new GameplayHelper(5314, new int[]{1, 2, 5, 10}), new GameplayHelper(5315, new int[]{1, 2, 5, 10}), new GameplayHelper(5316, new int[]{1, 2, 5, 10}), new GameplayHelper(5318, new int[]{10, 20, 50, 100}), new GameplayHelper(5319, new int[]{10, 20, 50, 100}), new GameplayHelper(5320, new int[]{10, 20, 50, 100}), new GameplayHelper(5321, new int[]{10, 20, 50, 100}), new GameplayHelper(5322, new int[]{10, 20, 50, 100}), new GameplayHelper(5323, new int[]{10, 20, 50, 100}), new GameplayHelper(5324, new int[]{10, 20, 50, 100}), new GameplayHelper(5325, new int[]{1}), new GameplayHelper(5329, new int[]{1}), new GameplayHelper(5331, new int[]{1}), new GameplayHelper(5341, new int[]{1}), new GameplayHelper(5343, new int[]{1}), new GameplayHelper(5350, new int[]{1, 2, 5, 10}), new GameplayHelper(5376, new int[]{10, 20, 50, 100}), new GameplayHelper(5418, new int[]{10, 20, 50, 100}), new GameplayHelper(5525, new int[]{1}), new GameplayHelper(5527, new int[]{1}), new GameplayHelper(5529, new int[]{1}), new GameplayHelper(5531, new int[]{1}), new GameplayHelper(5533, new int[]{1}), new GameplayHelper(5535, new int[]{1}), new GameplayHelper(5537, new int[]{1}), new GameplayHelper(5539, new int[]{1}), new GameplayHelper(5541, new int[]{1}), new GameplayHelper(5543, new int[]{1}), new GameplayHelper(5547, new int[]{1}), new GameplayHelper(5680, new int[]{1}), new GameplayHelper(5698, new int[]{1}), new GameplayHelper(6032, new int[]{10, 20, 50, 100}), new GameplayHelper(6036, new int[]{10, 20, 50, 100}), new GameplayHelper(6333, new int[]{100, 200, 500, 1000}), new GameplayHelper(6522, new int[]{1}), new GameplayHelper(6523, new int[]{1}), new GameplayHelper(6524, new int[]{1}), new GameplayHelper(6525, new int[]{1}), new GameplayHelper(6526, new int[]{1}), new GameplayHelper(6527, new int[]{1}), new GameplayHelper(6528, new int[]{1}), new GameplayHelper(6568, new int[]{1}), new GameplayHelper(6585, new int[]{1}), new GameplayHelper(6581, new int[]{1}), new GameplayHelper(6809, new int[]{1}), new GameplayHelper(6889, new int[]{1}), new GameplayHelper(6908, new int[]{1}), new GameplayHelper(6910, new int[]{1}), new GameplayHelper(6912, new int[]{1}), new GameplayHelper(6914, new int[]{1}), new GameplayHelper(6916, new int[]{1}), new GameplayHelper(6918, new int[]{1}), new GameplayHelper(6920, new int[]{1}), new GameplayHelper(6922, new int[]{1}), new GameplayHelper(6924, new int[]{1}), new GameplayHelper(7386, new int[]{1}), new GameplayHelper(7390, new int[]{1}), new GameplayHelper(7394, new int[]{1}), new GameplayHelper(7936, new int[]{100, 200, 500, 1000, 2000}), new GameplayHelper(10330, new int[]{1}), new GameplayHelper(10332, new int[]{1}), new GameplayHelper(10334, new int[]{1}), new GameplayHelper(10336, new int[]{1}), new GameplayHelper(10338, new int[]{1}), new GameplayHelper(10340, new int[]{1}), new GameplayHelper(10342, new int[]{1}), new GameplayHelper(10344, new int[]{1}), new GameplayHelper(10346, new int[]{1}), new GameplayHelper(10348, new int[]{1}), new GameplayHelper(10350, new int[]{1}), new GameplayHelper(10352, new int[]{1}), new GameplayHelper(10368, new int[]{1}), new GameplayHelper(10370, new int[]{1}), new GameplayHelper(10372, new int[]{1}), new GameplayHelper(10374, new int[]{1}), new GameplayHelper(10376, new int[]{1}), new GameplayHelper(10378, new int[]{1}), new GameplayHelper(10380, new int[]{1}), new GameplayHelper(10382, new int[]{1}), new GameplayHelper(10384, new int[]{1}), new GameplayHelper(10386, new int[]{1}), new GameplayHelper(10388, new int[]{1}), new GameplayHelper(10390, new int[]{1}), new GameplayHelper(10440, new int[]{1}), new GameplayHelper(10442, new int[]{1}), new GameplayHelper(10444, new int[]{1}), new GameplayHelper(10446, new int[]{1}), new GameplayHelper(10448, new int[]{1}), new GameplayHelper(10450, new int[]{1}), new GameplayHelper(10452, new int[]{1}), new GameplayHelper(10454, new int[]{1}), new GameplayHelper(10456, new int[]{1}), new GameplayHelper(10458, new int[]{1}), new GameplayHelper(10460, new int[]{1}), new GameplayHelper(10462, new int[]{1}), new GameplayHelper(10464, new int[]{1}), new GameplayHelper(10466, new int[]{1}), new GameplayHelper(10468, new int[]{1}), new GameplayHelper(10470, new int[]{1}), new GameplayHelper(10472, new int[]{1}), new GameplayHelper(10474, new int[]{1}), new GameplayHelper(11128, new int[]{1}), new GameplayHelper(11284, new int[]{1}), new GameplayHelper(11286, new int[]{1}), new GameplayHelper(11335, new int[]{1}), new GameplayHelper(11716, new int[]{1}), new GameplayHelper(11718, new int[]{1}), new GameplayHelper(11720, new int[]{1}), new GameplayHelper(11722, new int[]{1}), new GameplayHelper(11724, new int[]{1}), new GameplayHelper(11726, new int[]{1}), new GameplayHelper(11728, new int[]{1}), new GameplayHelper(11730, new int[]{1}), new GameplayHelper(11732, new int[]{1}), new GameplayHelper(11736, new int[]{1}), new GameplayHelper(11738, new int[]{1})};

    public static void initializeTradeAdvertOfferPools() {
        GameplayHelper[] gameplayHelperArray = configuredTradeAdvertOffers;
        int length = configuredTradeAdvertOffers.length;
        int index = 0;
        while (index < length) {
            GameplayHelper gameplayHelper = gameplayHelperArray[index];
            ItemDefinition itemDefinition = ItemDefinition.forId(gameplayHelper.getTradeAdvertItemId());
            if (!(!ItemDefinition.isDefined(gameplayHelper.getTradeAdvertItemId()) || ServerSettings.freeToPlayWorld && itemDefinition.isMembersOnly() && gameplayHelper.getTradeAdvertItemId() != 0 || !ServerSettings.content2007Enabled && gameplayHelper.getTradeAdvertItemId() >= 7955 || !ServerSettings.otherBotsEnabled && (gameplayHelper.getTradeAdvertItemId() == 381 || gameplayHelper.getTradeAdvertItemId() == 0))) {
                tradeAdvertOfferPool.add(gameplayHelper);
                if (gameplayHelper.getTradeAdvertItemId() == 115 || gameplayHelper.getTradeAdvertItemId() == 127 || gameplayHelper.getTradeAdvertItemId() == 139 || gameplayHelper.getTradeAdvertItemId() == 175 || gameplayHelper.getTradeAdvertItemId() == 333 || gameplayHelper.getTradeAdvertItemId() == 379 || gameplayHelper.getTradeAdvertItemId() == 385 || gameplayHelper.getTradeAdvertItemId() == 560 || gameplayHelper.getTradeAdvertItemId() == 562 || gameplayHelper.getTradeAdvertItemId() == 565 || gameplayHelper.getTradeAdvertItemId() == 811 || gameplayHelper.getTradeAdvertItemId() == 861 || gameplayHelper.getTradeAdvertItemId() == 868 || gameplayHelper.getTradeAdvertItemId() == 886 || gameplayHelper.getTradeAdvertItemId() == 888 || gameplayHelper.getTradeAdvertItemId() == 890 || gameplayHelper.getTradeAdvertItemId() == 892 || gameplayHelper.getTradeAdvertItemId() == 1163 || gameplayHelper.getTradeAdvertItemId() == 1201 || gameplayHelper.getTradeAdvertItemId() == 1215 || gameplayHelper.getTradeAdvertItemId() == 1231 || gameplayHelper.getTradeAdvertItemId() == 1303 || gameplayHelper.getTradeAdvertItemId() == 1319 || gameplayHelper.getTradeAdvertItemId() == 1333 || gameplayHelper.getTradeAdvertItemId() == 1373 || gameplayHelper.getTradeAdvertItemId() == 2487 || gameplayHelper.getTradeAdvertItemId() == 2489 || gameplayHelper.getTradeAdvertItemId() == 2491 || gameplayHelper.getTradeAdvertItemId() == 2493 || gameplayHelper.getTradeAdvertItemId() == 2495 || gameplayHelper.getTradeAdvertItemId() == 2497 || gameplayHelper.getTradeAdvertItemId() == 2499 || gameplayHelper.getTradeAdvertItemId() == 2501 || gameplayHelper.getTradeAdvertItemId() == 2503 || gameplayHelper.getTradeAdvertItemId() == 4131 || gameplayHelper.getTradeAdvertItemId() == 4151 || gameplayHelper.getTradeAdvertItemId() == 4153 || gameplayHelper.getTradeAdvertItemId() == 4740 || gameplayHelper.getTradeAdvertItemId() == 5680 || gameplayHelper.getTradeAdvertItemId() == 5698 || gameplayHelper.getTradeAdvertItemId() == 6524 || gameplayHelper.getTradeAdvertItemId() == 6528 || gameplayHelper.getTradeAdvertItemId() == 6568 || gameplayHelper.getTradeAdvertItemId() == 6585) {
                    combatTradeAdvertOfferPool.add(gameplayHelper);
                }
                if (gameplayHelper.getTradeAdvertItemId() == 127 || gameplayHelper.getTradeAdvertItemId() == 139 || gameplayHelper.getTradeAdvertItemId() == 175 || gameplayHelper.getTradeAdvertItemId() == 223 || gameplayHelper.getTradeAdvertItemId() == 225 || gameplayHelper.getTradeAdvertItemId() == 231 || gameplayHelper.getTradeAdvertItemId() == 237 || gameplayHelper.getTradeAdvertItemId() == 249 || gameplayHelper.getTradeAdvertItemId() == 251 || gameplayHelper.getTradeAdvertItemId() == 253 || gameplayHelper.getTradeAdvertItemId() == 255 || gameplayHelper.getTradeAdvertItemId() == 257 || gameplayHelper.getTradeAdvertItemId() == 314 || gameplayHelper.getTradeAdvertItemId() == 333 || gameplayHelper.getTradeAdvertItemId() == 335 || gameplayHelper.getTradeAdvertItemId() == 377 || gameplayHelper.getTradeAdvertItemId() == 379 || gameplayHelper.getTradeAdvertItemId() == 383 || gameplayHelper.getTradeAdvertItemId() == 385 || gameplayHelper.getTradeAdvertItemId() == 453 || gameplayHelper.getTradeAdvertItemId() == 532 || gameplayHelper.getTradeAdvertItemId() == 536 || gameplayHelper.getTradeAdvertItemId() == 556 || gameplayHelper.getTradeAdvertItemId() == 561 || gameplayHelper.getTradeAdvertItemId() == 563 || gameplayHelper.getTradeAdvertItemId() == 564 || gameplayHelper.getTradeAdvertItemId() == 1355 || gameplayHelper.getTradeAdvertItemId() == 1357 || gameplayHelper.getTradeAdvertItemId() == 1359 || gameplayHelper.getTradeAdvertItemId() == 1391 || gameplayHelper.getTradeAdvertItemId() == 1436 || gameplayHelper.getTradeAdvertItemId() == 1438 || gameplayHelper.getTradeAdvertItemId() == 1440 || gameplayHelper.getTradeAdvertItemId() == 1442 || gameplayHelper.getTradeAdvertItemId() == 1444 || gameplayHelper.getTradeAdvertItemId() == 1446 || gameplayHelper.getTradeAdvertItemId() == 1448 || gameplayHelper.getTradeAdvertItemId() == 1452 || gameplayHelper.getTradeAdvertItemId() == 1454 || gameplayHelper.getTradeAdvertItemId() == 1462 || gameplayHelper.getTradeAdvertItemId() == 1470 || gameplayHelper.getTradeAdvertItemId() == 1472 || gameplayHelper.getTradeAdvertItemId() == 1474 || gameplayHelper.getTradeAdvertItemId() == 1476 || gameplayHelper.getTradeAdvertItemId() == 1511 || gameplayHelper.getTradeAdvertItemId() == 1513 || gameplayHelper.getTradeAdvertItemId() == 1515 || gameplayHelper.getTradeAdvertItemId() == 1517 || gameplayHelper.getTradeAdvertItemId() == 1519 || gameplayHelper.getTradeAdvertItemId() == 1521 || gameplayHelper.getTradeAdvertItemId() == 1601 || gameplayHelper.getTradeAdvertItemId() == 1603 || gameplayHelper.getTradeAdvertItemId() == 1605 || gameplayHelper.getTradeAdvertItemId() == 1607 || gameplayHelper.getTradeAdvertItemId() == 1615 || gameplayHelper.getTradeAdvertItemId() == 1617 || gameplayHelper.getTradeAdvertItemId() == 1619 || gameplayHelper.getTradeAdvertItemId() == 1621 || gameplayHelper.getTradeAdvertItemId() == 1623 || gameplayHelper.getTradeAdvertItemId() == 1631 || gameplayHelper.getTradeAdvertItemId() == 1712 || gameplayHelper.getTradeAdvertItemId() == 1725 || gameplayHelper.getTradeAdvertItemId() == 1727 || gameplayHelper.getTradeAdvertItemId() == 1729 || gameplayHelper.getTradeAdvertItemId() == 1731 || gameplayHelper.getTradeAdvertItemId() == 1739 || gameplayHelper.getTradeAdvertItemId() == 1747 || gameplayHelper.getTradeAdvertItemId() == 1749 || gameplayHelper.getTradeAdvertItemId() == 1751 || gameplayHelper.getTradeAdvertItemId() == 1753 || gameplayHelper.getTradeAdvertItemId() == 1777 || gameplayHelper.getTradeAdvertItemId() == 1779 || gameplayHelper.getTradeAdvertItemId() == 7936) {
                    commonTradeAdvertOfferPool.add(gameplayHelper);
                }
                if (gameplayHelper.getTradeAdvertItemId() == 0 || gameplayHelper.getTradeAdvertItemId() == 381 || gameplayHelper.getTradeAdvertItemId() == 1319) {
                    scamTradeAdvertOfferPool.add(gameplayHelper);
                }
            }
            ++index;
        }
    }

    public static int roundAdvertUnitPrice(boolean enabled2, int value3, int value22) {
        int initialValue = 1;
        if (value3 < 1000) {
            initialValue = value3;
            initialValue = enabled2 ? (int)(Math.ceil((double)initialValue / 10.0) * 10.0) : (int)(Math.floor((double)initialValue / 10.0) * 10.0);
            initialValue /= value22;
        }
        if (value3 >= 1000 && value3 < 10000) {
            initialValue = value3;
            initialValue = enabled2 ? (int)(Math.ceil((double)initialValue / 100.0) * 100.0) : (int)(Math.floor((double)initialValue / 100.0) * 100.0);
            initialValue /= value22;
        }
        if (value3 >= 10000 && value3 < 100000) {
            initialValue = value3;
            initialValue = enabled2 ? (int)(Math.ceil((double)initialValue / 1000.0) * 1000.0) : (int)(Math.floor((double)initialValue / 1000.0) * 1000.0);
            initialValue /= value22;
        }
        if (value3 >= 100000 && value3 < 1000000) {
            initialValue = value3;
            initialValue = enabled2 ? (int)(Math.ceil((double)initialValue / 10000.0) * 10000.0) : (int)(Math.floor((double)initialValue / 10000.0) * 10000.0);
            initialValue /= value22;
        }
        if (value3 >= 1000000 && value3 < 10000000) {
            initialValue = value3;
            initialValue = enabled2 ? (int)(Math.ceil((double)initialValue / 100000.0) * 100000.0) : (int)(Math.floor((double)initialValue / 100000.0) * 100000.0);
            initialValue /= value22;
        }
        if (value3 >= 10000000) {
            initialValue = value3;
            initialValue = enabled2 ? (int)(Math.ceil((double)initialValue / 1000000.0) * 1000000.0) : (int)(Math.floor((double)initialValue / 1000000.0) * 1000000.0);
            initialValue /= value22;
        }
        return initialValue;
    }

    public static void prepareTradeAdvertOffer(Player player) {
        int value;
        double value2;
        int value3;
        player.tradeAdvertMode = GameUtil.randomInt(2);
        player.tradeAdvertVariableQuantity = false;
        player.tradeAdvertScam = false;
        if (ServerSettings.otherBotsEnabled && GameUtil.randomInt(scammerChanceDivisor) == 0) {
            player.tradeAdvertScam = true;
        }
        Object value4 = player.currentBotTask.usesCombatTradeAdvertItems ? combatTradeAdvertOfferPool : ((value3 = GameUtil.randomInt(100)) >= commonItemChancePercent ? tradeAdvertOfferPool : commonTradeAdvertOfferPool);
        if (player.tradeAdvertMode == 0 && player.tradeAdvertScam && GameUtil.randomInt(2) == 0) {
            value4 = scamTradeAdvertOfferPool;
        }
        value3 = GameUtil.randomInt(((ArrayList)value4).size());
        value4 = (GameplayHelper)((ArrayList)value4).get(value3);
        player.tradeAdvertOfferPoolIndex = value3 = tradeAdvertOfferPool.indexOf(value4);
        player.botAdvertItemId = ((GameplayHelper)tradeAdvertOfferPool.get(player.tradeAdvertOfferPoolIndex)).getTradeAdvertItemId();
        value4 = ItemDefinition.forId(player.botAdvertItemId);
        double guidePrice = GrandExchangeManager.getGuidePrice(player.botAdvertItemId);
        if (guidePrice < 1.0) {
            guidePrice = 1.0;
        }
        if (player.botAdvertItemId == 381) {
            value3 = GameUtil.randomInt(4);
            if (value3 == 0) {
                guidePrice = 1000.0;
            } else if (value3 == 1) {
                guidePrice = 5000.0;
            } else if (value3 == 2) {
                guidePrice = 10000.0;
            } else if (value3 >= 3) {
                guidePrice = 20000.0;
            }
        }
        value3 = GameUtil.rollPriceFluctuationPercent(11);
        double value5 = guidePrice / 100.0 * (double)value3;
        double value6 = player.tradeAdvertMode == 0 ? guidePrice + value5 : guidePrice - value5;
        value3 = (int)value6;
        if (player.botAdvertItemId == 0) {
            value = GameUtil.randomInt(4);
            if (value == 0) {
                value3 = 1000;
            } else if (value == 1) {
                value3 = 5000;
            } else if (value == 2) {
                value3 = 10000;
            } else if (value >= 3) {
                value3 = 20000;
            }
            if (GameUtil.randomInt(2) == 0) {
                value3 = 1;
            }
        }
        player.tradeAdvertQuantityOptionIndex = value = GameUtil.randomInt(((GameplayHelper)tradeAdvertOfferPool.get(player.tradeAdvertOfferPoolIndex)).getTradeAdvertQuantityOptions().length);
        value = ((GameplayHelper)tradeAdvertOfferPool.get(player.tradeAdvertOfferPoolIndex)).getTradeAdvertQuantityOptions()[player.tradeAdvertQuantityOptionIndex];
        int value7 = value * value3;
        if (player.botAdvertItemId != 0) {
            int value8 = value;
            Player player2 = player;
            int initialValue = 1;
            if (value7 < 1000) {
                initialValue = value7;
                initialValue = player2.tradeAdvertMode == 0 ? (int)(Math.ceil((double)initialValue / 10.0) * 10.0) : (int)(Math.floor((double)initialValue / 10.0) * 10.0);
                initialValue /= value8;
            }
            if (value7 >= 1000 && value7 < 10000) {
                initialValue = value7;
                initialValue = player2.tradeAdvertMode == 0 ? (int)(Math.ceil((double)initialValue / 100.0) * 100.0) : (int)(Math.floor((double)initialValue / 100.0) * 100.0);
                initialValue /= value8;
            }
            if (value7 >= 10000 && value7 < 100000) {
                initialValue = value7;
                initialValue = player2.tradeAdvertMode == 0 ? (int)(Math.ceil((double)initialValue / 1000.0) * 1000.0) : (int)(Math.floor((double)initialValue / 1000.0) * 1000.0);
                initialValue /= value8;
            }
            if (value7 >= 100000 && value7 < 1000000) {
                initialValue = value7;
                initialValue = player2.tradeAdvertMode == 0 ? (int)(Math.ceil((double)initialValue / 10000.0) * 10000.0) : (int)(Math.floor((double)initialValue / 10000.0) * 10000.0);
                initialValue /= value8;
            }
            if (value7 >= 1000000 && value7 < 10000000) {
                initialValue = value7;
                initialValue = player2.tradeAdvertMode == 0 ? (int)(Math.ceil((double)initialValue / 100000.0) * 100000.0) : (int)(Math.floor((double)initialValue / 100000.0) * 100000.0);
                initialValue /= value8;
            }
            if (value7 >= 10000000) {
                initialValue = value7;
                initialValue = player2.tradeAdvertMode == 0 ? (int)(Math.ceil((double)initialValue / 1000000.0) * 1000000.0) : (int)(Math.floor((double)initialValue / 1000000.0) * 1000000.0);
                initialValue /= value8;
            }
            value3 = initialValue;
        }
        value7 = player.botAdvertItemId;
        if (!((ItemDefinition)value4).isStackable() && value >= 10) {
            value7 = ((ItemDefinition)value4).getNotedId();
        }
        if (player.tradeAdvertScam && value7 == 1319) {
            value7 = ((ItemDefinition)value4).getNotedId();
        }
        player.botAdvertItemId = value7;
        player.tradeAdvertQuantityRemaining = value;
        if (value3 <= 0) {
            value3 = 1 + GameUtil.randomInt(5);
        }
        player.tradeAdvertUnitPrice = value3;
        if (player.tradeAdvertMode == 1 && value >= 1000) {
            player.tradeAdvertVariableQuantity = GameUtil.randomInt(5) == 0;
        }
        BotTradeAdvertManager.updateTradeAdvertMessage(player);
        player.botPublicChatColor = GameUtil.randomInt(12);
        int[] integerValues = new int[3];
        integerValues[1] = 1;
        integerValues[2] = 3;
        value4 = integerValues;
        value3 = GameUtil.randomInt(3);
        player.botPublicChatEffect = ((int[])value4)[value3];
    }

    public static void updateTradeAdvertMessage(Player player) {
        Object value = ItemDefinition.forId(player.botAdvertItemId);
        if (((ItemDefinition)value).isNote()) {
            value = ItemDefinition.forId(((ItemDefinition)value).getUnnotedId());
        }
        int value2 = player.tradeAdvertQuantityRemaining;
        String text = GameUtil.formatCompactAmount(player.tradeAdvertQuantityRemaining);
        int value3 = player.tradeAdvertUnitPrice * player.tradeAdvertQuantityRemaining;
        String text2 = GameUtil.formatCompactAmountDetailed(value3);
        String text3 = GameUtil.formatCompactAmountDetailed(player.tradeAdvertUnitPrice);
        String text4 = value2 <= 10 && value2 > 1 || player.tradeAdvertVariableQuantity ? String.valueOf(text3) + " ea" : (value3 < 1000 ? String.valueOf(text2) + "gp" : text2);
        text2 = player.tradeAdvertMode == 0 ? "Selling" : "Buying";
        value = ((ItemDefinition)value).getDisplayName();
        value = value2 > 1 && !((String)value).endsWith("s") ? String.valueOf(value) + "s" : value;
        text = String.valueOf(text2) + " " + text + " " + (String)value + " " + text4 + " - " + player.getUsername();
        if (value2 == 1) {
            text = String.valueOf(text2) + " " + (String)value + " " + text4 + " - " + player.getUsername();
        } else if (player.tradeAdvertVariableQuantity) {
            text = String.valueOf(text2) + " all " + (String)value + " " + text4 + " - " + player.getUsername();
        }
        if (player.botAdvertItemId == 0 && player.tradeAdvertUnitPrice > 1) {
            text = "Doubling money min " + text4 + " - " + player.getUsername();
        }
        if (player.botAdvertItemId == 0 && player.tradeAdvertUnitPrice == 1) {
            text = "I need free stuff - " + player.getUsername();
        }
        player.botPublicChatMessage = text;
    }

    public static void prepareTradeAdvertCombatLoadout(Player player) {
        GameplayHelper.resetBotSkillsToBase(player);
        int value = 1 + GameUtil.randomInt(99);
        int value2 = value / 5 << 1;
        if (value2 == 0) {
            value2 = 2;
        }
        BotCombatHelper.setBotSkillLevel(player, 0, value - value / 5 + GameUtil.randomInt(value2));
        value2 = value / 5 << 1;
        if (value2 == 0) {
            value2 = 2;
        }
        BotCombatHelper.setBotSkillLevel(player, 2, value - value / 5 + GameUtil.randomInt(value2));
        value2 = value / 5 << 1;
        if (value2 == 0) {
            value2 = 2;
        }
        BotCombatHelper.setBotSkillLevel(player, 1, value - value / 5 + GameUtil.randomInt(value2));
        value2 = value / 5 << 1;
        if (value2 == 0) {
            value2 = 2;
        }
        BotCombatHelper.setBotSkillLevel(player, 4, value - value / 5 + GameUtil.randomInt(value2));
        value2 = value / 5 << 1;
        if (value2 == 0) {
            value2 = 2;
        }
        BotCombatHelper.setBotSkillLevel(player, 6, value - value / 5 + GameUtil.randomInt(value2));
        value2 = value / 5 << 1;
        if (value2 == 0) {
            value2 = 2;
        }
        BotCombatHelper.setBotSkillLevel(player, 5, value - (value / 5 << 1) + GameUtil.randomInt(value2));
        player.getSkillManager().getExperience()[3] = BotCombatHelper.calculateBotHitpointsExperience(player);
        int[] skillManager = player.getSkillManager().getCurrentLevels();
        player.getSkillManager();
        skillManager[3] = SkillManager.getLevelForExperience(player.getSkillManager().getExperience()[3]);
        player.getSkillManager().refreshAllSkills();
        BotCombatLoadoutManager.selectCombatStyleFromStats(player, true);
    }

    public static void prepareTradeAdvertInventory(Player player) {
        player.getInventoryManager().getContainer().clear();
        player.getEquipmentManager().getContainer().clear();
        if (player.tradeAdvertMode == 0) {
            if (player.tradeAdvertScam && player.botAdvertItemId == 1320) {
                player.getInventoryManager().addItem(new ItemStack(player.botAdvertItemId, player.tradeAdvertQuantityRemaining));
                player.getInventoryManager().addItem(new ItemStack(1310, player.tradeAdvertQuantityRemaining));
            } else if (player.botAdvertItemId != 0) {
                player.getInventoryManager().addItem(new ItemStack(player.botAdvertItemId, player.tradeAdvertQuantityRemaining));
            }
        } else if (player.tradeAdvertMode == 1) {
            player.getInventoryManager().addItem(new ItemStack(995, player.tradeAdvertUnitPrice * player.tradeAdvertQuantityRemaining));
        }
        Player player2 = player;
        if (player2.botCombatStyle == 0) {
            BotCombatLoadoutManager.prepareMeleeLoadout(player2);
            BotCombatLoadoutManager.equipGlovesAndBoots(player2);
        } else if (player2.botCombatStyle == 2) {
            BotCombatLoadoutManager.prepareMagicLoadout(player2);
        } else if (player2.botCombatStyle == 1) {
            int value;
            BotCombatLoadoutManager.prepareRangedLoadout(player2);
            value = player2.getSkillManager().getCurrentLevels()[4] >= 40 ? 1731 : (GameUtil.randomInt(3) == 0 ? 1478 : 1729);
            if (!BotCombatHelper.isFreeToPlayWorld() && player2.getCombatLevel() >= 60 && GameUtil.randomInt(2) == 0) {
                value = 1712;
            }
            player2.getEquipmentManager().getContainer().setItem(2, new ItemStack(value));
        }
        BotCombatLoadoutManager.equipRandomCape(player2);
        player.getInventoryManager().refresh();
        player.getEquipmentManager().refresh();
    }

    public static void advanceTradeAdvertTradeFlow(Player player) {
        if (player.getOpenInterfaceId() == 3323) {
            GameplayHelper.acceptTradeFirstScreen(player);
            return;
        }
        if (player.getOpenInterfaceId() == 3443) {
            GameplayHelper.acceptTradeSecondScreen(player);
        }
    }
}


import com.rs2.ServerSettings;
import com.rs2.cache.js5.Interfaces;
import com.rs2.model.combat.*;
import com.rs2.model.combat.attack.*;
import com.rs2.model.combat.hit.*;
import com.rs2.model.item.*;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.skill.SkillManager;
import com.rs2.model.skill.magic.SpellDefinition;
import java.lang.reflect.Field;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/** Equipment aggregation and the actual combat consumers in both formula modes. */
public final class EquipmentCombatBonusChecks {
    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        ServerSettings.freeToPlayWorld = false;
        Interfaces.load();
        QuestDefinition.loadDefinitions();
        ItemDefinition.loadDefinitions();
        Player player = new Player(null);
        player.setEncodedIndex(32769);
        player.setSize(1);
        player.setQuestState(0, 1);
        for (int skill = 0; skill < 7; ++skill) {
            player.getSkillManager().getExperience()[skill] = SkillManager.getExperienceForLevel(80);
            player.getSkillManager().getCurrentLevels()[skill] = 80;
        }
        Field socket = Player.class.getDeclaredField("socketChannel");
        socket.setAccessible(true);
        int[] ids = {1007, 577, 1059, 1635};
        int[][] originals = new int[ids.length][];
        for (int i = 0; i < ids.length; ++i) originals[i] = ItemDefinition.forId(ids[i]).getBonuses().clone();
        try (SocketChannel transport = SocketChannel.open()) {
            transport.close();
            socket.set(player, transport);
            for (boolean modern : new boolean[]{false, true}) {
                ServerSettings.modernCombatSystemEnabled = modern;
                player.getEquipmentManager().getContainer().clear();
                player.getEquipmentManager().refresh();
                checkStaffDefaults(player);
                player.getEquipmentManager().getContainer().clear();
                player.getEquipmentManager().refresh();
                double[] attacks = rolls(player, true);
                double[] defences = rolls(player, false);
                int prayer = player.getPrayerDrainThreshold();
                for (int i = 0; i < ids.length; ++i) {
                    int[] bonuses = ItemDefinition.forId(ids[i]).getBonuses();
                    Arrays.fill(bonuses, i + 1);
                    bonuses[13] = new int[]{10, 15, 20, 5}[i];
                    player.getEquipmentManager().getContainer().setItem(
                            new int[]{1, 4, 9, 12}[i], new ItemStack(ids[i]));
                }
                player.getEquipmentManager().refresh();
                for (int index = 0; index < 13; ++index) {
                    require(player.getCombatBonus(index) == 10, "Equipped bonus not aggregated: " + index);
                }
                require(player.getCombatBonus(13) == 50, "Magic damage did not stack across slots");
                double[] boostedAttacks = rolls(player, true);
                double[] boostedDefences = rolls(player, false);
                for (int i = 0; i < 5; ++i) {
                    require(boostedAttacks[i] > attacks[i], "Attack bonus ignored: " + i);
                    require(boostedDefences[i] > defences[i], "Defence bonus ignored: " + i);
                }
                require(player.getPrayerDrainThreshold() > prayer, "Prayer bonus ignored");
                checkMagic(player, SpellDefinition.FIRE_WAVE, 20);
                checkMagic(player, SpellDefinition.ICE_BARRAGE, 30);
                checkMagic(player, SpellDefinition.MAGIC_DART, 10 + player.getSkillManager().getBaseLevel(6) / 10);
                // Charge and chaos gauntlets remain flat boosts before the percentage.
                player.activateChargeSpell();
                checkMagic(player, SpellDefinition.FLAMES_OF_ZAMORAK, 30);
                player.getEquipmentManager().getContainer().setItem(9, new ItemStack(777));
                player.getEquipmentManager().refresh();
                checkMagic(player, SpellDefinition.FIRE_BOLT, 15);
                checkWeaponStrength(player);
                player.getEquipmentManager().getContainer().clear();
                player.getEquipmentManager().refresh();
                require(player.getCombatBonus(13) == 0, "Magic bonus stale after removing equipment");
                checkMagic(player, SpellDefinition.FIRE_WAVE, 20);
                for (int i = 0; i < ids.length; ++i) {
                    System.arraycopy(originals[i], 0, ItemDefinition.forId(ids[i]).getBonuses(), 0, 14);
                }
            }
        } finally {
            for (int i = 0; i < ids.length; ++i) {
                System.arraycopy(originals[i], 0, ItemDefinition.forId(ids[i]).getBonuses(), 0, 14);
            }
        }
        System.out.println("Equipment combat bonus checks passed: all 14 bonuses, both combat modes");
        System.exit(0);
    }

    private static void checkStaffDefaults(Player player) {
        int base = SpellDefinition.FIRE_WAVE.getHitDefinition().getMaxDamage();
        for (int staff : new int[]{4675, 4710, 6914}) {
            require(ItemDefinition.forId(staff).getBonus(13) == 0, "Stock staff has magic damage");
            player.getEquipmentManager().getContainer().setItem(3, new ItemStack(staff));
            player.getEquipmentManager().refresh();
            require(CombatManager.calculateSpellMaxHit(player, SpellDefinition.FIRE_WAVE) == base,
                    "Hardcoded staff damage remains: " + staff);
            MagicCombatAttack attack = new MagicCombatAttack(player, new Player(null), SpellDefinition.FIRE_WAVE);
            attack.prepare();
            require(attack.getHitDefinitions()[0].getMaxDamage() == base, "Staff boost remains in attack path");
        }
    }

    private static void checkMagic(Player player, SpellDefinition spell, int expectedBase) {
        int bonus = player.getCombatBonus(13);
        player.setCombatBonus(13, 0);
        int base = CombatManager.calculateSpellMaxHit(player, spell);
        require(base == expectedBase, "Spell base/flat boost changed: " + spell);
        player.setCombatBonus(13, bonus);
        int expected = (int)((long)base * (100 + bonus) / 100);
        require(CombatManager.calculateSpellMaxHit(player, spell) == expected, "Incorrect magic percentage: " + spell);
        MagicCombatAttack attack = new MagicCombatAttack(player, new Player(null), spell);
        attack.prepare();
        require(attack.getHitDefinitions()[0].getMaxDamage() == expected, "Spell attack ignored equipment: " + spell);
    }

    private static void checkWeaponStrength(Player player) throws Exception {
        WeaponCombatAttack melee = new WeaponCombatAttack(player, new Player(null), WeaponProfile.forItem(new ItemStack(4151)));
        double initialMelee = CombatManager.calculateMeleeMaxHit(player, melee);
        int strength = player.getCombatBonus(10);
        player.setCombatBonus(10, strength + 100);
        require(CombatManager.calculateMeleeMaxHit(player, melee) > initialMelee, "Melee Strength ignored");
        player.setCombatBonus(10, strength);
        WeaponCombatAttack ranged = new WeaponCombatAttack(player, new Player(null), WeaponProfile.forItem(new ItemStack(861)));
        Field ammo = WeaponCombatAttack.class.getDeclaredField("ammunition");
        ammo.setAccessible(true);
        ammo.set(ranged, AmmunitionDefinition.findEquippedAmmunition(rangedPlayer(player),
                WeaponProfile.forItem(new ItemStack(861)), true));
        double initialRanged = CombatManager.calculateWeaponMaxHit(player, ranged);
        int rangedStrength = player.getCombatBonus(12);
        player.setCombatBonus(12, rangedStrength + 100);
        require(CombatManager.calculateWeaponMaxHit(player, ranged) > initialRanged, "Ranged Strength ignored");
        player.setCombatBonus(12, rangedStrength);
    }

    private static Player rangedPlayer(Player player) {
        player.getEquipmentManager().getContainer().setItem(3, new ItemStack(861));
        player.getEquipmentManager().getContainer().setItem(13, new ItemStack(892, 100));
        player.getEquipmentManager().refresh();
        return player;
    }

    private static double[] rolls(Player player, boolean attack) {
        double[] result = new double[5];
        for (AttackBonusType type : AttackBonusType.values()) {
            CombatType combat = type == AttackBonusType.MAGIC ? CombatType.MAGIC
                    : type == AttackBonusType.RANGED ? CombatType.RANGED : CombatType.MELEE;
            HitDefinition hit = new HitDefinition(new AttackStyleDefinition(combat, AttackXpMode.MELEE_ACCURATE, type),
                    HitType.NORMAL, 10);
            result[type.getIndex()] = attack ? CombatManager.calculateAttackRoll(player, hit)
                    : CombatManager.calculateDefenceRoll(player, hit);
        }
        return result;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

package com.rs2.bot;

import com.rs2.ServerSettings;
import com.rs2.bot.combat.BotCombatLoadoutManager;
import com.rs2.cache.js5.Interfaces;
import com.rs2.model.combat.AmmunitionDefinition;
import com.rs2.model.combat.WeaponProfile;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.player.Player;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.model.skill.SkillManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import sun.reflect.ReflectionFactory;

/** Loadouts must stay usable across ranged and mystic requirement boundaries. */
public final class CastleWarsEquipmentLoadoutChecks {
    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        Interfaces.load();
        QuestDefinition.loadDefinitions();
        ItemDefinition.loadDefinitions();
        for (int id = 4089; id <= 4117; id += 2) {
            require(ItemDefinition.forId(id).getRequiredLevel(6) == 40, "Mystic Magic requirement: " + id);
            require(ItemDefinition.forId(id).getRequiredLevel(1) == 20, "Mystic Defence requirement: " + id);
        }
        Constructor<Player> base = Player.class.getDeclaredConstructor(SelectionKey.class);
        Constructor<?> fixture = ReflectionFactory.getReflectionFactory()
                .newConstructorForSerialization(BotPlayer.class, base);
        fixture.setAccessible(true);
        Method refill = MinigameBotManager.class.getDeclaredMethod("prepareCastleWarsCombatStacks", BotPlayer.class);
        refill.setAccessible(true);
        for (boolean freeWorld : new boolean[]{true, false}) {
            ServerSettings.freeToPlayWorld = freeWorld;
            for (int level : new int[]{10, 19, 20, 29, 30, 39, 40, 49, 50, 99}) {
                BotPlayer bot = (BotPlayer) fixture.newInstance((Object)null);
                try (SocketChannel channel = SocketChannel.open()) {
                    channel.close();
                    Field socket = Player.class.getDeclaredField("socketChannel");
                    socket.setAccessible(true);
                    socket.set(bot, channel);
                    for (int skill = 0; skill < 7; ++skill) setLevel(bot, skill, level);
                    bot.botPrimaryCombatStyle = 1;
                    BotCombatLoadoutManager.prepareRangedLoadout(bot);
                    int arrows = bot.getEquipmentManager().getItemIdAtSlot(13);
                    refill.invoke(null, bot);
                    require(bot.getEquipmentManager().getItemIdAtSlot(13) == arrows, "Refill changed arrow tier");
                    require(bot.getEquipmentManager().getContainer().getItemAt(13).getAmount() == Integer.MAX_VALUE,
                            "Castle Wars ammo was not replenished");
                    ItemStack bow = bot.getEquipmentManager().getContainer().getItemAt(3);
                    require(AmmunitionDefinition.findEquippedAmmunition(bot, WeaponProfile.forItem(bow), true) != null,
                            "Incompatible bow/ammo: level=" + level + " freeWorld=" + freeWorld);
                    bot.getEquipmentManager().getContainer().clear();
                    bot.getInventoryManager().getContainer().clear();
                    bot.tradeAdvertMode = 0; // This fixture does not need spell runes.
                    BotCombatLoadoutManager.prepareMagicLoadout(bot);
                    for (int slot : new int[]{0, 4, 7, 9, 10}) {
                        ItemStack item = bot.getEquipmentManager().getContainer().getItemAt(slot);
                        require(item != null && item.getId() >= 0, "Mage lost armour at level " + level);
                        require(bot.getEquipmentManager().canEquipItem(item.getId()), "Mage cannot equip chosen armour");
                    }
                    boolean mystic = bot.getEquipmentManager().getItemIdAtSlot(4) == 4091;
                    require(mystic == (!freeWorld && level >= 40), "Incorrect mystic upgrade at level " + level);
                }
            }
        }
        System.out.println("Castle Wars equipment checks passed: compatible ammo and mystic boundaries");
        System.exit(0);
    }

    private static void setLevel(Player player, int skill, int level) {
        player.getSkillManager().getExperience()[skill] = SkillManager.getExperienceForLevel(level);
        player.getSkillManager().getCurrentLevels()[skill] = level;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}

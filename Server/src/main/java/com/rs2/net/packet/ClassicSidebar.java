package com.rs2.net.packet;

import com.rs2.model.player.Player;

/**
 * The optional 2004 sidebar ("Classic interface" client setting). The client builds
 * Game Options (group 506) and Player controls (group 507) from LostCity's revision 274
 * layouts; settings buttons set the player's values directly and emotes reuse the native 443 emote tab.
 */
public final class ClassicSidebar {
    public static final int OPTIONS_GROUP = 506;
    public static final int CONTROLS_GROUP = 507;
    private static final int OPTIONS_TAB = 11, CONTROLS_TAB = 12;
    private static final int NATIVE_OPTIONS = 261, NATIVE_EMOTES = 464;

    // First child of the client's added rows of later emotes (button, label, button, label...).
    public static final int EXTRA_EMOTE_FIRST_CHILD = 52;
    // Native 443 emote children absent from the 2004 tab, in emote-tab order:
    // Shrug, Jump for Joy, Yawn, Jig, Spin, Headbang, Blow Kiss, Panic, Raspberry, Salute,
    // Goblin Bow, Goblin Salute, Slap Head, Stamp, Flap, Idea, Zombie Walk, Zombie Dance,
    // Scared, Rabbit Hop, Skill Cape.
    public static final int[] EXTRA_EMOTES = {7, 11, 12, 14, 15, 16, 18, 19, 20, 22, 23, 24,
            29, 30, 31, 32, 33, 34, 35, 36, 37};

    private ClassicSidebar() { }

    private static final int COMBAT_TAB = 0;

    /** Native tab group, or its classic replacement while the player uses the classic sidebar. */
    public static int tabGroup(Player player, int tab, int legacyGroup, int group) {
        if (!player.classicSidebar) return group;
        if (tab == OPTIONS_TAB && group == NATIVE_OPTIONS) return OPTIONS_GROUP;
        if (tab == CONTROLS_TAB && group == NATIVE_EMOTES) return CONTROLS_GROUP;
        if (tab == COMBAT_TAB) {
            int classic = ClassicCombat.groupForRoot(legacyGroup);
            if (classic != -1) return classic;
        }
        return group;
    }

    /** Re-sends the classic-capable tabs after the setting changes. */
    public static void refreshTabs(Player player) {
        player.packetSender.setSidebarInterface(OPTIONS_TAB, player.getSidebarInterfaceId(OPTIONS_TAB));
        player.packetSender.setSidebarInterface(CONTROLS_TAB, player.getSidebarInterfaceId(CONTROLS_TAB));
        // The combat tab's weapon name, special bar and style state are sent with the weapon interface.
        player.getEquipmentManager().refreshWeaponInterface();
        player.packetSender.refreshAutocastConfig();
    }

    /**
     * Applies a classic settings button, as LostCity's game_options.rs2 / player_controls.rs2 do:
     * each button sets its value outright. The legacy 377 handlers can't be reused here: their
     * nested option buttons fail the open-interface check, and 443's run button toggles.
     */
    public static boolean handleSetting(Player player, int group, int child) {
        if (group == OPTIONS_GROUP) {
            if (child >= 0 && child <= 7) {                              // brightness 1-4 (two parts per button)
                player.setBrightness(child / 2 + 1);
                VarpPacket.send(player, 166, player.getBrightness());
            } else if (child == 8 || child == 9) {                       // mouse buttons: 0 one, 1 two
                player.setMouseButtons(child - 8);
                VarpPacket.send(player, 170, player.getMouseButtons());
            } else if (child == 10 || child == 11) {                     // chat effects: 0 on, 1 off
                player.setPublicChatEffects(child - 10);
                VarpPacket.send(player, 171, player.getPublicChatEffects());
            } else if (child >= 25 && child <= 29) {                     // music volume 4..0
                player.setMusicVolume(29 - child);
                VarpPacket.send(player, 168, player.getMusicVolume());
            } else if (child >= 36 && child <= 40) {                     // sound effect volume 4..0
                player.setEffectVolume(40 - child);
                VarpPacket.send(player, 169, player.getEffectVolume());
            } else if (child == 47 || child == 48) {                     // split private chat: 1 on, 0 off
                player.setSplitPrivateChat(child == 47 ? 1 : 0);
                VarpPacket.send(player, 287, player.getSplitPrivateChat());
            } else {
                return false;
            }
            return true;
        }
        if (group == CONTROLS_GROUP) {
            if (child == 2 || child == 3) {                              // auto retaliate on / off (sends varp 172)
                player.setAutoRetaliate(child == 2);
            } else if (child == 4) {                                     // walk
                player.getMovementQueue().setRunning(false);
            } else if (child == 5) {                                     // run, unless out of energy (sends varp 173)
                boolean energy = player.getRunEnergyPercent() > 0 && player.isTutorialRunUnlocked();
                player.getMovementQueue().setRunning(energy);
                if (!energy) player.packetSender.sendRunEnergy();
            } else {
                return false;
            }
            return true;
        }
        return false;
    }

    // 2004 staff tab (combat_staff_2): auto_toggle turns autocast on and off; auto_spell names the spell.
    private static final int STAFF_AUTOCAST_TOGGLE = 349, STAFF_SPELL_NAME = 352;

    /** Classic staff tab state: varp 108 (attackstyle_magic) is non-zero with a spell chosen, bit 0 while autocasting. */
    public static void refreshAutocast(Player player) {
        if (player.isBot || !player.classicSidebar) return;
        com.rs2.model.skill.magic.SpellDefinition spell = player.getAutocastSpell();
        VarpPacket.send(player, 108, spell == null ? 0 : player.isAutocastEnabled() ? 3 : 2);
        player.packetSender.sendInterfaceText(spell == null ? "" : spellLabel(spell), STAFF_SPELL_NAME);
    }

    private static String spellLabel(com.rs2.model.skill.magic.SpellDefinition spell) {
        StringBuilder label = new StringBuilder();
        for (String word : spell.name().toLowerCase().split("_")) {
            if (word.isEmpty()) continue;
            if (label.length() > 0) label.append(' ');
            label.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return label.toString();
    }

    /** Classic combat-tab buttons with no native 443 equivalent; true when handled. */
    public static boolean handleCombat(Player player, int legacyComponent) {
        if (legacyComponent != STAFF_AUTOCAST_TOGGLE) return false;
        if (player.getAutocastSpell() == null) {
            player.packetSender.sendGameMessage("You need to choose a spell first.");
        } else {
            player.setAutocastEnabled(!player.isAutocastEnabled());
        }
        return true;
    }

    /** Native 443 emote-tab child for a classic emote button, or -1. */
    public static int nativeEmoteChild(int group, int child) {
        if (group != CONTROLS_GROUP) return -1;
        switch (child) {
            case 13: return 17; // Cry
            case 14: return 5;  // Think
            case 15: return 6;  // Wave
            case 16: return 3;  // Bow
            case 17: return 4;  // Angry
            case 18: return 13; // Dance
            case 19: return 9;  // Beckon
            case 20: return 1;  // Yes
            case 21: return 2;  // No
            case 22: return 10; // Laugh
            case 23: return 8;  // Cheer
            case 24: return 21; // Clap
            case 41: return 28; // Glass Wall
            case 42: return 25; // Glass Box
            case 43: return 26; // Climb Rope
            case 44: return 27; // Lean
            default: break;
        }
        int extra = child - EXTRA_EMOTE_FIRST_CHILD;
        if (extra >= 0 && extra % 2 == 0 && extra / 2 < EXTRA_EMOTES.length) return EXTRA_EMOTES[extra / 2];
        return -1;
    }
}

package com.rs2.model.player;

import com.rs2.ServerSettings;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.net.packet.PacketWriter;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Ensure a Zamorak staff triggers 2006 Castle Wars god-portal morphs and the
 * revision 443 appearance packet carries the NPC ID to the client.
 */
public final class CastleWarsPortalMorphChecks {
    private static final int ZAMORAK_STAFF = 2417;
    private static final int RABBIT_NPC = 1530;
    private static final int SHEEP_NPC = 1529;

    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        QuestDefinition.loadDefinitions();
        ItemDefinition.loadDefinitions();

        Player player = new Player(null);
        require(ItemDefinition.forId(ZAMORAK_STAFF).getEquipmentSlot() == 3,
                "Zamorak staff definition must equip in the weapon slot");
        player.getEquipmentManager().getContainer().setItem(3, new ItemStack(ZAMORAK_STAFF));

        Method apply = CastleWarsManager.class.getDeclaredMethod(
                "applyWaitingRoomGodTransformation", Player.class, int.class);
        apply.setAccessible(true);
        Method build = ModernPlayerUpdateTask.class.getDeclaredMethod(
                "buildAppearance", Player.class);
        build.setAccessible(true);

        // The opposite god's staff morphs at the blue Saradomin portal.
        apply.invoke(null, player, CastleWarsManager.SARADOMIN_PORTAL_ID);
        require(player.npcTransformationId == RABBIT_NPC,
                "Zamorak staff should turn into a rabbit at the Saradomin portal");
        checkAppearance(build, player, RABBIT_NPC);

        // Guthix's neutral portal turns a Zamorak devotee into a sheep.
        player.npcTransformationId = -1;
        apply.invoke(null, player, CastleWarsManager.GUTHIX_PORTAL_ID);
        require(player.npcTransformationId == SHEEP_NPC,
                "Zamorak staff should turn into a sheep at the Guthix portal");
        checkAppearance(build, player, SHEEP_NPC);

        // Wearing the same god's item at the red portal must not morph.
        player.npcTransformationId = -1;
        apply.invoke(null, player, CastleWarsManager.ZAMORAK_PORTAL_ID);
        require(player.npcTransformationId == -1,
                "Zamorak staff should not morph at the Zamorak portal");
        ByteBuffer human = ((PacketWriter) build.invoke(null, player)).getBuffer();
        human.flip();
        human.position(3);
        require((human.getShort() & 65535) != 65535,
                "Untransformed players must use ordinary equipment appearance");

        // Leaving the waiting room restores human appearance.
        player.npcTransformationId = RABBIT_NPC;
        CastleWarsManager.leaveWaitingRoom(player);
        require(player.npcTransformationId == -1,
                "Leaving the waiting room must clear Castle Wars morphs");

        System.out.println("Castle Wars Zamorak staff portal morph packet checks passed");
        System.exit(0);
    }

    private static void checkAppearance(Method build, Player player, int expectedNpc)
            throws Exception {
        ByteBuffer buffer = ((PacketWriter) build.invoke(null, player)).getBuffer();
        buffer.flip();
        require((buffer.get() & 255) == player.getGender(), "Wrong gender prefix");
        buffer.get(); // Skull icon
        buffer.get(); // Prayer icon
        require((buffer.getShort() & 65535) == 65535,
                "Revision 443 appearance must include the NPC morph sentinel");
        require((buffer.getShort() & 65535) == expectedNpc,
                "Revision 443 appearance has the wrong NPC ID");
        for (int color : player.getAppearanceColors()) {
            require((buffer.get() & 255) == color, "Morph shifted appearance colours");
        }
        // 7 movement animation shorts, followed by name, combat and skill.
        buffer.position(buffer.position() + 7 * 2);
        require(buffer.getLong() == player.getNameHash(), "Morph shifted username");
        require((buffer.get() & 255) == player.getCombatLevel(),
                "Morph shifted combat level");
        require(buffer.getShort() == 0 && !buffer.hasRemaining(),
                "Morph packet length or tail was corrupted");
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}

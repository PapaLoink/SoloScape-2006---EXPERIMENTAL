package com.rs2.model.player;

import com.rs2.ServerSettings;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.model.item.ItemDefinition;
import com.rs2.model.item.ItemStack;
import com.rs2.model.npc.NpcDefinition;
import com.rs2.model.quest.QuestDefinition;
import com.rs2.net.packet.PacketWriter;
import com.rs2.net.packet.PacketBuffer;
import com.rs2.net.packet.ByteOrder;
import com.rs2.net.packet.ByteTransform;
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
    private static final int IMP_NPC = 1531;

    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        QuestDefinition.loadDefinitions();
        ItemDefinition.loadDefinitions();
        NpcDefinition.loadDefinitions();

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

        // The Zamorak portal can produce an imp when wearing another god's
        // gear. Verify its animated appearance uses the same safe NPC format.
        player.npcTransformationId = IMP_NPC;
        checkAppearance(build, player, IMP_NPC);

        // All three god morphs must discard player emote/attack sequences.
        verifyEmoteAndAnimationBlocking(player);

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
        // The seven movement slots must use this NPC's own cached sequences.
        // All -1s would eliminate stretching but leave a T-pose and sliding.
        int[] expected = ModernPlayerUpdateTask.castleWarsMorphMovementAnimations(expectedNpc);
        require(expected.length == 7, "NPC morph should supply seven movement slots");
        NpcDefinition npc = NpcDefinition.forId(expectedNpc);
        require(npc.getIdleAnimationId() >= 0,
                "NPC " + expectedNpc + " has no cache idle animation");
        require(npc.getWalkAnimationId() >= 0,
                "NPC " + expectedNpc + " has no cache walking animation");
        require(expected[0] == npc.getIdleAnimationId()
                        && expected[2] == npc.getWalkAnimationId()
                        && expected[6] == npc.getWalkAnimationId(),
                "NPC " + expectedNpc + " uses wrong idle/walk/run slots");
        for (int index = 0; index < 7; ++index) {
            require((buffer.getShort() & 65535) == (expected[index] & 65535),
                    "NPC " + expectedNpc + " wrong movement animation slot " + index);
        }
        System.out.println("Morph " + expectedNpc + " idle=" + expected[0]
                + " walk=" + expected[2] + " sequences=" + java.util.Arrays.toString(expected));
        require(buffer.getLong() == player.getNameHash(), "Morph shifted username");
        require((buffer.get() & 255) == player.getCombatLevel(),
                "Morph shifted combat level");
        require(buffer.getShort() == 0 && !buffer.hasRemaining(),
                "Morph packet length or tail was corrupted");
    }

    private static void verifyEmoteAndAnimationBlocking(Player player) throws Exception {
        Method writeMask = ModernPlayerUpdateTask.class.getDeclaredMethod(
                "writeUpdateMask", Player.class, Player.class, PacketWriter.class, int.class);
        writeMask.setAccessible(true);
        for (int npcId : new int[]{SHEEP_NPC, RABBIT_NPC, IMP_NPC}) {
            player.npcTransformationId = npcId;
            player.getUpdateState().setAnimation(-1);
            require(player.getEmoteManager().handleButtonClick(168),
                    "Normal emote button must be recognised while morphed");
            require(player.getUpdateState().getAnimationId() == -1,
                    "A morphed player must not start a human emote");
            require(player.getEmoteManager().handleButtonClick(18465),
                    "Special emote button must be recognised while morphed");
            require(player.getUpdateState().getAnimationId() == -1,
                    "Special emotes must not animate a morphed player");
            require(!player.getEmoteManager().handleButtonClick(12345),
                    "Unrelated buttons must not be swallowed");

            // Other systems might still enqueue animations; the network
            // encoder must never send human frames to a Castle Wars NPC rig.
            player.getUpdateState().setAnimation(855, 7);
            checkAnimationMask(writeMask, player, -1, 0);
        }

        // Leaving the morph restores normal emote and packet handling.
        player.npcTransformationId = -1;
        require(player.getEmoteManager().handleButtonClick(168),
                "Human emote button should remain available");
        require(player.getUpdateState().getAnimationId() == 855,
                "Human emotes should still trigger their normal animation");
        player.getUpdateState().setAnimation(855, 7);
        checkAnimationMask(writeMask, player, 855, 7);
    }

    private static void checkAnimationMask(Method writeMask, Player player,
                                           int animation, int delay) throws Exception {
        PacketWriter actual = PacketBuffer.allocateWriter(16);
        writeMask.invoke(null, player, player, actual, 0x8);
        PacketWriter expected = PacketBuffer.allocateWriter(16);
        expected.writeByte(0x8);
        expected.writeShort(animation, ByteTransform.ADD, ByteOrder.LITTLE);
        expected.writeByte(delay, ByteTransform.NEGATE);
        ByteBuffer sent = actual.getBuffer();
        ByteBuffer wanted = expected.getBuffer();
        sent.flip();
        wanted.flip();
        require(sent.equals(wanted),
                "Wrong 443 animation mask for NPC morph state " + player.npcTransformationId);
    }

    private static void require(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}

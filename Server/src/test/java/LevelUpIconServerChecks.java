import com.rs2.ServerSettings;
import com.rs2.cache.js5.Interfaces;
import com.rs2.model.player.Player;
import com.rs2.model.skill.SkillManager;
import com.rs2.net.packet.InterfaceBridge;

/** Native 443 skill icons and dedicated level-up chatbox mapping for 22 skills. */
public final class LevelUpIconServerChecks {
    public static void main(String[] args) throws Exception {
        ServerSettings.clientBuild = 443;
        ServerSettings.progressiveXpMode = 0;
        Interfaces.load();
        int[] spriteWidgets = {
            48, 54, 51, 49, 57, 60, 63, 59, 65, 64, 56,
            62, 61, 53, 50, 55, 52, 58, 110, 115, 105, 45
        };
        require(SkillManager.SKILL_NAMES.length == spriteWidgets.length,
                "Level-up skill count does not match the skill-icon table");
        require(InterfaceBridge.translateGroup(19580) == 211,
                "Dedicated level-up root is not a native two-line chatbox");
        require(InterfaceBridge.translateGroup(359) == 211,
                "Original two-line statement mapping was changed");
        require(InterfaceBridge.translate(19581) == ((211 << 16) | 3),
                "Hidden level-up skill selector is not mapped");
        require(InterfaceBridge.translate(360) == (211 << 16)
                && InterfaceBridge.translate(361) == ((211 << 16) | 1),
                "Level-up statement text mapping was changed");

        // Verify the actual heading generated for each skill, not just the
        // packet target. The client font maps @blu@ to #0000FF, whereas the
        // former @dbl@ tag was only #000080.
        java.lang.reflect.Method titleBuilder =
                SkillManager.class.getDeclaredMethod("levelUpTitle", int.class);
        titleBuilder.setAccessible(true);

        Player player = new Player(null);
        player.isBot = true;
        for (int i = 0; i < spriteWidgets.length; i++) {
            Interfaces.Component sprite = Interfaces.forId(320, spriteWidgets[i]);
            require(sprite != null && sprite.type == 5,
                    SkillManager.SKILL_NAMES[i] + " has no native sprite");
            require(sprite.width == 25 && sprite.height == 25,
                    SkillManager.SKILL_NAMES[i] + " is not a 25x25 native skill icon");
            String title = (String) titleBuilder.invoke(null, i);
            require(title.equals("@blu@Congratulations, you just advanced a "
                            + SkillManager.SKILL_NAMES[i] + " level!"),
                    SkillManager.SKILL_NAMES[i] + " level-up heading is not bright blue");
            player.getSkillManager().showLevelUpInterface(i);
            require(player.getOpenInterfaceId() == 19580,
                    SkillManager.SKILL_NAMES[i] + " did not open the dedicated level-up chatbox");
        }
        player.packetSender.showChatboxInterface(359);
        require(player.getOpenInterfaceId() == 359,
                "Level-up UI prevented ordinary statements");
        System.out.println("Level-up server checks passed for all "
                + spriteWidgets.length + " skills bright-blue headings and native 443 chatbox mapping.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

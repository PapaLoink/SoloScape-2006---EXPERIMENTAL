import com.rs2.cache.js5.Interfaces;
import com.rs2.model.gameplay.castlewars.CastleWarsManager;
import com.rs2.net.packet.InterfaceBridge;
import java.lang.reflect.Field;
import java.util.Map;

/** Inspect the revision-443 Castle Wars match HUD's native component geometry. */
public final class CastleWarsHudPositionChecks {
    public static void main(String[] args) throws Exception {
        Interfaces.load();
        Map<Integer, Interfaces.Component> group = Interfaces.group(59);
        if (group.isEmpty()) throw new AssertionError("Missing Castle Wars HUD group 59");
        System.out.println("Native Castle Wars HUD group 59 (" + group.size() + " children)");
        for (Map.Entry<Integer, Interfaces.Component> entry : group.entrySet()) {
            Interfaces.Component c = entry.getValue();
            System.out.println("HUD 59:" + c.childId + " xy=(" + c.x + "," + c.y
                    + ") size=" + c.width + "x" + c.height
                    + " type=" + c.type + " parent=" + c.parentId);
        }
        Interfaces.Component timer = group.get(8);
        if (timer == null || timer.type != 4 || timer.parentId != -1
                || timer.x != 442 || timer.y != 318
                || timer.width != 68 || timer.height != 11) {
            throw new AssertionError("Unexpected stock Castle Wars match timer geometry");
        }
        if (InterfaceBridge.translate(11353) != (59 << 16 | 8)) {
            throw new AssertionError("Match countdown must map to native text child 59:8");
        }
        Field xOffset = CastleWarsManager.class.getDeclaredField("GAME_TIMER_443_OFFSET_X");
        Field yOffset = CastleWarsManager.class.getDeclaredField("GAME_TIMER_443_OFFSET_Y");
        xOffset.setAccessible(true);
        yOffset.setAccessible(true);
        int targetX = timer.x + xOffset.getInt(null);
        int targetY = timer.y + yOffset.getInt(null);
        Interfaces.Component score = group.get(1); // Saradomin score in top row
        if (score == null || targetY != score.y
                || targetX < score.x + score.width
                || targetX + timer.width > 512) {
            throw new AssertionError("Match timer must sit top-right on score row with no overlap");
        }
        System.out.println("Castle Wars timer moved from (" + timer.x + "," + timer.y
                + ") to (" + targetX + "," + targetY + ")");
        System.out.println("Castle Wars match timer native position check passed");
    }
}

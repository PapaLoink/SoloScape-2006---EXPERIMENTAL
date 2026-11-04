import com.rs2.cache.js5.Interfaces;
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
        if (timer == null || timer.type != 4) {
            throw new AssertionError("Castle Wars match timer must be native text child 59:8");
        }
    }
}

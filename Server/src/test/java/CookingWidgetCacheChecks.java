import com.rs2.cache.js5.Interfaces;
import java.util.Map;

/** Inspect the actual revision-443 group 307 from Server/cache. */
public final class CookingWidgetCacheChecks {
    public static void main(String[] args) throws Exception {
        Interfaces.load();
        Map<Integer, Interfaces.Component> group = Interfaces.group(307);
        if (group.isEmpty()) throw new AssertionError("Missing cooking interface 307");
        System.out.println("Cooking interface 307 (" + group.size() + " children):");
        for (Map.Entry<Integer, Interfaces.Component> e : group.entrySet()) {
            Interfaces.Component c = e.getValue();
            StringBuilder ascii = new StringBuilder();
            StringBuilder run = new StringBuilder();
            for (byte b : c.getData()) {
                int n = b & 255;
                if (n >= 32 && n < 127) {
                    run.append((char) n);
                } else {
                    if (run.length() >= 5) ascii.append(" [").append(run).append("]");
                    run.setLength(0);
                }
            }
            if (run.length() >= 5) ascii.append(" [").append(run).append("]");
            System.out.println("307:" + c.childId + " type=" + c.type
                + " action=" + c.actionType + " xy=(" + c.x + "," + c.y
                + ") size=" + c.width + "x" + c.height
                + " parent=" + c.parentId + " strings=" + ascii);
        }
    }
}

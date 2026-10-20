import com.rs2.cache.js5.Interfaces;
import com.rs2.model.objects.ObjectDefinition;
import java.util.Map;

/** Cache-backed inspection for issue #2 spinning-wheel support. */
public final class SpinningInterfaceChecks {
    public static void main(String[] args) throws Exception {
        ObjectDefinition.loadRevision443();
        for (int objectId : new int[]{2644}) {
            ObjectDefinition def = ObjectDefinition.forId(objectId);
            System.out.println("Object " + objectId + " name=" + def.getName());
            for (int i = 0; i < 5; i++) {
                System.out.println("  option " + (i + 1) + "=" + def.getAction(i));
            }
        }
        Interfaces.load();
        for (int groupId : new int[]{303, 304, 305, 306, 309, 459}) {
            Map<Integer, Interfaces.Component> group = Interfaces.group(groupId);
            System.out.println("Group " + groupId + " (" + group.size() + " children)");
            for (Interfaces.Component c : group.values()) {
                StringBuilder ascii = new StringBuilder(), run = new StringBuilder();
                for (byte b : c.getData()) {
                    int n = b & 255;
                    if (n >= 32 && n < 127) run.append((char) n);
                    else {
                        if (run.length() >= 4) ascii.append(" [").append(run).append("]");
                        run.setLength(0);
                    }
                }
                if (run.length() >= 4) ascii.append(" [").append(run).append("]");
                System.out.println(" " + groupId + ":" + c.childId + " type=" + c.type
                        + " action=" + c.actionType + " pos=" + c.x + "," + c.y
                        + " size=" + c.width + "x" + c.height + " strings=" + ascii);
            }
        }
    }
}

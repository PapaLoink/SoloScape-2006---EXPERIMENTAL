import com.rs2.cache.js5.Interfaces;
import java.util.Map;

/** Inspect the stock revision-443 Castle Wars catapult aiming interface. */
public final class CastleWarsCatapultWidgetCacheChecks {
    public static void main(String[] args) throws Exception {
        Interfaces.load();
        Map<Integer, Interfaces.Component> group = Interfaces.group(54);
        if (group.isEmpty()) {
            throw new AssertionError("Missing native 443 catapult group 54");
        }
        System.out.println("Catapult group 54 (" + group.size() + " children)");
        // Keep an explicit bounds dump: the target marker may be a layered widget.
        System.out.println("Inspecting coordinate digits and map-marker layering");
        for (Map.Entry<Integer, Interfaces.Component> entry : group.entrySet()) {
            Interfaces.Component c = entry.getValue();
            StringBuilder strings = new StringBuilder();
            StringBuilder word = new StringBuilder();
            for (byte b : c.getData()) {
                int n = b & 0xff;
                if (n >= 32 && n <= 126) {
                    word.append((char) n);
                } else {
                    if (word.length() >= 5) strings.append(" [").append(word).append("]");
                    word.setLength(0);
                }
            }
            if (word.length() >= 5) strings.append(" [").append(word).append("]");
            if (c.childId == 116 || c.childId == 117 || c.childId == 132
                    || c.childId == 133 || c.childId == 146 || c.childId == 147
                    || c.childId == 148 || c.childId == 149
                    || c.childId == 159 || c.childId == 160 || c.childId == 161) {
                java.nio.ByteBuffer b = java.nio.ByteBuffer.wrap(c.getData());
                b.position(17); // legacy widget header: through hover target
                int conditionCount = b.get() & 255;
                StringBuilder conditions = new StringBuilder();
                for (int i = 0; i < conditionCount; i++) {
                    conditions.append(" ").append(b.get() & 255)
                            .append("=").append(b.getShort() & 0xffff);
                }
                int scripts = b.get() & 255;
                StringBuilder scriptsText = new StringBuilder();
                for (int i = 0; i < scripts; i++) {
                    int length = b.getShort() & 0xffff;
                    scriptsText.append(" {");
                    for (int j = 0; j < length; j++)
                        scriptsText.append(j == 0 ? "" : ",").append(b.getShort() & 0xffff);
                    scriptsText.append("}");
                }
                if (c.type == 6) {
                    StringBuilder models = new StringBuilder();
                    for (int i = 0; i < 7; i++) models.append(" ").append(b.getShort() & 0xffff);
                    System.out.println("   model/details: conditions=" + conditions
                            + " scripts=" + scriptsText + " modelFields=" + models);
                }
            }
            System.out.println("54:" + c.childId + " type=" + c.type
                    + " action=" + c.actionType + " xy=(" + c.x + "," + c.y
                    + ") size=" + c.width + "x" + c.height
                    + " parent=" + c.parentId + " strings=" + strings);
        }
    }
}

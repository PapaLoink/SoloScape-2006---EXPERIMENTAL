import com.rs2.cache.js5.Interfaces;
import com.rs2.cache.js5.Js5CacheStore;
import java.io.File;
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
        // All ten glyphs must actually be present in the native model archive.
        try (Js5CacheStore cache = new Js5CacheStore(new File("cache"))) {
            for (int digit = 0; digit <= 9; digit++) {
                int modelId = 4863 + digit;
                Map<Integer, byte[]> model = cache.readFiles(7, modelId);
                if (model.isEmpty())
                    throw new AssertionError("Missing native numeral model " + modelId);
                System.out.println("Native catapult numeral " + digit + " model=" + modelId);
            }
        }
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
                    int modelId = b.getShort() & 0xffff;
                    models.append(" ").append(modelId);
                    for (int i = 1; i < 7; i++) models.append(" ").append(b.getShort() & 0xffff);
                    if (c.childId == 159 || c.childId == 160) {
                        if (modelId != 4853)
                            throw new AssertionError("Catapult-site icon model changed at 54:" + c.childId);
                    } else if (c.childId == 161) {
                        if (modelId != 4856)
                            throw new AssertionError("Expected movable red X model at 54:161");
                    } else if (c.childId == 116 || c.childId == 117
                            || c.childId == 132 || c.childId == 133) {
                        if (modelId != 4857)
                            throw new AssertionError("Native digit backing model changed at 54:" + c.childId);
                    } else if (c.childId >= 146 && c.childId <= 149) {
                        if (modelId != 4864 + (c.childId - 146))
                            throw new AssertionError("Expected stock visible numeral models 1..4 at 54:" + c.childId);
                    }
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

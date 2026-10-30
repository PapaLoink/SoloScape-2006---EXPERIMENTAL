package unpackaged;

import java.lang.reflect.Field;

/** Pure logic checks for WebClient pinch zoom camera distance, Java 8 compatible. */
public final class BrowserCameraZoomChecks {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        Field enabled = WebClientBridge.class.getDeclaredField("enabled");
        enabled.setAccessible(true);
        boolean previous = enabled.getBoolean(null);
        try {
            enabled.setBoolean(null, true);
            int initialOffset = WebClientBridge.cameraDistanceOffset();
            int normal = 1200;

            WebClientBridge.adjustCameraZoom(-2);
            check(WebClientBridge.cameraDistance(normal) == normal + initialOffset - 140,
                    "pinch out should bring the camera closer");

            WebClientBridge.adjustCameraZoom(4);
            check(WebClientBridge.cameraDistance(normal) == normal + initialOffset + 140,
                    "pinch in should move the camera away");

            WebClientBridge.adjustCameraZoom(-100);
            check(WebClientBridge.cameraDistanceOffset() >= -550,
                    "lower zoom offset must be bounded");
            for (int i = 0; i < 20; i++) WebClientBridge.adjustCameraZoom(-10);
            check(WebClientBridge.cameraDistanceOffset() == -550,
                    "zoom-in camera limit must be enforced");
            check(WebClientBridge.cameraDistance(500) == 450,
                    "absolute camera distance must not fall below safe minimum");

            for (int i = 0; i < 20; i++) WebClientBridge.adjustCameraZoom(10);
            check(WebClientBridge.cameraDistanceOffset() == 850,
                    "zoom-out camera limit must be enforced");
            check(WebClientBridge.cameraDistance(2200) == 2600,
                    "absolute camera distance must not exceed safe maximum");

            enabled.setBoolean(null, false);
            check(WebClientBridge.cameraDistance(normal) == normal,
                    "original client camera must not use browser zoom");
            System.out.println("Browser camera zoom checks passed.");
        } finally {
            enabled.setBoolean(null, previous);
        }
    }
}

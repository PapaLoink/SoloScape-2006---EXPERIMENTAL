import com.rs2.ServerSettings;
import com.rs2.model.player.Player;
import com.rs2.util.GameplayTrace;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

/** General debug mode must not enable high-frequency gameplay or packet logging. */
public final class GameplayTraceNoiseChecks {
    private GameplayTraceNoiseChecks() {
    }

    public static void main(String[] args) throws Exception {
        // Set properties before GameplayTrace's static initializer is reached.
        System.setProperty("prs.traceGameplay", "false");
        System.setProperty("prs.traceConsole", "false");

        boolean oldDebugMode = ServerSettings.debugModeEnabled;
        ServerSettings.debugModeEnabled = true;
        try {
            if (GameplayTrace.enabled()) {
                throw new AssertionError("General debug mode must not enable gameplay/packet tracing");
            }

            PrintStream oldOut = System.out;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                System.setOut(new PrintStream(output, true, "UTF-8"));
                GameplayTrace.log("combat cycle tick attacker=example");
                GameplayTrace.log("packet dispatch opcode=86 length=0");
                GameplayTrace.logInteraction(null, "generic interaction diagnostic");
                GameplayTrace.logException("packet handler diagnostic",
                        new IllegalStateException("synthetic trace exception"));
            } finally {
                System.setOut(oldOut);
            }
            if (output.size() != 0) {
                throw new AssertionError("General debug mode leaked high-volume packet-debug logs: "
                        + output.toString("UTF-8"));
            }

            // Explicit per-player interaction tracing remains available.
            Player player = new Player(null);
            player.isBot = true;
            Field enabled = Player.class.getDeclaredField("interactionDebugEnabled");
            enabled.setAccessible(true);
            enabled.setBoolean(player, true);
            if (!player.isInteractionDebugEnabled()) {
                throw new AssertionError("Per-player interaction debug toggle unavailable");
            }
            PrintStream oldOut2 = System.out;
            ByteArrayOutputStream explicit = new ByteArrayOutputStream();
            try {
                System.setOut(new PrintStream(explicit, true, "UTF-8"));
                GameplayTrace.logInteraction(player, "manual interaction probe");
            } finally {
                System.setOut(oldOut2);
            }
            if (!explicit.toString("UTF-8").contains("manual interaction probe")) {
                throw new AssertionError("Explicit per-player interaction diagnostics must still be visible");
            }
        } finally {
            ServerSettings.debugModeEnabled = oldDebugMode;
        }
        System.out.println("Gameplay trace console noise checks passed");
    }
}

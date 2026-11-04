package com.rs2.util;

import com.rs2.model.Entity;
import com.rs2.model.Position;
import com.rs2.model.npc.Npc;
import com.rs2.model.player.Player;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class GameplayTrace {
    // General launcher Debug Mode is for interactive diagnostics, NOT an
    // opt-in to high-volume combat/packet traces (especially Castle Wars).
    private static final boolean TRACE_PROPERTY_ENABLED = Boolean.getBoolean("prs.traceGameplay");
    private static final boolean TRACE_CONSOLE_ENABLED = Boolean.getBoolean("prs.traceConsole");
    private static final String FILTER = System.getProperty("prs.traceFilter", "");
    private static final Object LOCK = new Object();

    private GameplayTrace() {
    }

    public static boolean enabled() {
        // Trace only when explicitly requested. Debug Mode must not implicitly
        // enable thousands of combat-cycle and packet-dispatch logs per second.
        return TRACE_PROPERTY_ENABLED;
    }

    public static void log(String message) {
        if (!enabled()) {
            return;
        }
        // Explicit gameplay traces still go to qa-output/gameplay-trace.log.
        // Console output requires a separate explicit switch.
        write(message, TRACE_CONSOLE_ENABLED);
    }

    public static void logInteraction(Player player, String message) {
        boolean playerDebugEnabled = player != null && player.isInteractionDebugEnabled();
        if (!enabled() && !playerDebugEnabled) {
            return;
        }
        // Keep manual per-player interaction diagnostics working in console.
        write(message, TRACE_CONSOLE_ENABLED || playerDebugEnabled);
    }

    private static void write(String message, boolean printToTerminal) {
        if (!FILTER.isEmpty() && !message.contains(FILTER)) {
            return;
        }
        synchronized (LOCK) {
            String line = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + " " + message;
            if (printToTerminal) {
                System.out.println("[packet-debug] " + line);
            }
            try {
                File directory = new File("qa-output");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                PrintWriter writer = new PrintWriter(new FileWriter(new File(directory, "gameplay-trace.log"), true));
                writer.println(line);
                writer.close();
            }
            catch (Exception exception) {
                System.err.println("[gameplay-trace] " + message);
                exception.printStackTrace();
            }
        }
    }

    public static void logException(String context, Throwable throwable) {
        if (!enabled()) {
            return;
        }
        log(context + " exception=" + throwable);
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int limit = Math.min(stackTrace.length, 12);
        for (int index = 0; index < limit; ++index) {
            log("  at " + stackTrace[index]);
        }
    }

    public static String describe(Entity entity) {
        if (entity == null) {
            return "null";
        }
        String type = entity.isPlayer() ? "player" : (entity.isNpc() ? "npc" : "entity");
        String name = "";
        try {
            if (entity.isPlayer()) {
                name = ((Player)entity).getUsername();
            } else if (entity.isNpc()) {
                Npc npc = (Npc)entity;
                name = npc.getNpcId() + ":" + npc.getDefinition().getName();
            }
        }
        catch (Exception exception) {
            name = "?";
        }
        return type + "[" + name + " index=" + entity.getIndex() + " pos=" + position(entity.getPosition()) + " hp=" + entity.getCurrentHitpoints() + "/" + entity.getMaxHitpoints() + "]";
    }

    public static String position(Position position) {
        if (position == null) {
            return "null";
        }
        return position.getX() + "," + position.getY() + "," + position.getPlane();
    }
}

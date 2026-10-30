import com.rs2.Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/** Simple Java 8 regression checks for hot-reloaded AFK logout configuration. */
public final class IdleLogoutConfigChecks {
    private static Boolean parse(String config) throws IOException {
        return Server.parseIdleLogoutSetting(new BufferedReader(new StringReader(config)));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void invalid(String config) throws IOException {
        try {
            parse(config);
            throw new AssertionError("Malformed value was accepted: " + config);
        } catch (IOException expected) {
            // A bad edit must not silently reset the existing AFK policy.
        }
    }

    public static void main(String[] args) throws Exception {
        check(Boolean.FALSE.equals(parse("[IDLE_LOGOUT];0\n")),
                "zero disables the five-minute idle logout");
        check(Boolean.TRUE.equals(parse("[IDLE_LOGOUT];1\n")),
                "one enables the five-minute idle logout");
        check(Boolean.FALSE.equals(parse(
                "// A server.cfg comment\n[IDLE_LOGOUT];1\n"
                + "[IDLE_LOGOUT];0\n")), "last config entry wins");
        check(Boolean.FALSE.equals(parse("  [IDLE_LOGOUT] ; 0  \n")),
                "whitespace around the value is allowed");
        check(parse("[P2P_WORLD];1\n") == null,
                "missing idle setting must not silently change its value");
        invalid("[IDLE_LOGOUT];\n");
        invalid("[IDLE_LOGOUT];disabled\n");
        invalid("[IDLE_LOGOUT];0;1\n");
        System.out.println("Idle logout config checks passed.");
    }
}

import com.rs2.ServerSettings;
import com.rs2.model.Position;
import com.rs2.model.World;
import com.rs2.model.player.Player;
import com.rs2.net.IsaacCipher;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/** Recovery rates, movement gating, and the revision 443 world-tick regression. */
public final class RunEnergyChecks {
    public static void main(String[] args) throws Exception {
        try {
            ServerSettings.clientBuild = 443;
            ServerSettings.freeToPlayWorld = false;
            Player player = new Player(null);
            player.isBot = true;
            player.getSkillManager().getCurrentLevels()[16] = 1;
            player.setRunEnergyRaw(0);
            player.getMovementQueue().setRunning(true);
            for (int tick = 0; tick < 1250; tick++) player.restoreRunEnergy();
            require(player.getRunEnergyRaw() == 10000, "Level 1 recovery should take 750 seconds");
            player.restoreRunEnergy();
            require(player.getRunEnergyRaw() == 10000, "Recovery exceeded 100%");

            player.setRunEnergyRaw(5000);
            player.setWalkDirection(1);
            player.restoreRunEnergy();
            require(player.getRunEnergyRaw() == 5008, "Walking should recover energy");
            player.setRunDirection(1);
            player.getMovementQueue().clear();
            player.restoreRunEnergy();
            require(player.getRunEnergyRaw() == 5008, "Final running tick recovered energy");
            player.setRunDirection(-1);
            player.forcedMovementActive = true;
            player.restoreRunEnergy();
            require(player.getRunEnergyRaw() == 5008, "Obstacle traversal recovered energy");
            player.forcedMovementActive = false;
            player.getSkillManager().getCurrentLevels()[16] = 99;
            player.restoreRunEnergy();
            require(player.getRunEnergyRaw() == 5032, "Level 99 should recover 24 units per tick");
            ServerSettings.freeToPlayWorld = true;
            player.restoreRunEnergy();
            require(player.getRunEnergyRaw() == 5040, "F2P should use level 1 recovery");
            ServerSettings.freeToPlayWorld = false;
            player.setRunEnergyRaw(9999);
            player.restoreRunEnergy();
            require(player.getRunEnergyRaw() == 10000, "Near-full energy failed to cap at 100%");

            checkModernWorldTick();
            System.out.println("Run energy checks passed (idle with Run enabled, walking, running, obstacles, Agility, F2P, cap, 443 world tick packet, and debug-mode drain).");
            System.exit(0);
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }

    private static void checkModernWorldTick() throws Exception {
        try (ServerSocketChannel listener = ServerSocketChannel.open()) {
            listener.bind(new InetSocketAddress("127.0.0.1", 0));
            try (SocketChannel client = SocketChannel.open(listener.getLocalAddress());
                 SocketChannel server = listener.accept()) {
                Player player = new Player(null);
                player.setUsername("energycheck");
                player.setPosition(new Position(3222, 3222, 0));
                player.getLastKnownRegionPosition().set(new Position(3222, 3222, 0));
                player.setRunEnergyRaw(4996);
                player.getMovementQueue().setRunning(true);
                player.setOutboundCipher(new IsaacCipher(new int[4]));
                Field socket = Player.class.getDeclaredField("socketChannel");
                socket.setAccessible(true);
                socket.set(player, server);
                World.registerPlayer(player);
                try {
                    World.processTick();
                    require(player.getRunEnergyRaw() == 5004, "443 world tick skipped or doubled recovery");
                    ByteBuffer packet = ByteBuffer.allocate(2);
                    client.configureBlocking(false);
                    long deadline = System.nanoTime() + 1000000000L;
                    while (packet.hasRemaining() && System.nanoTime() < deadline) client.read(packet);
                    require(!packet.hasRemaining(), "Missing run-energy packet");
                    packet.flip();
                    int opcode = (packet.get() - new IsaacCipher(new int[4]).nextInt()) & 255;
                    require(opcode == 226 && (packet.get() & 255) == 50, "Incorrect 443 energy update");
                    // Debug mode is for diagnostics, not a global infinite-run cheat.
                    boolean previousDebug = ServerSettings.debugModeEnabled;
                    try {
                        ServerSettings.debugModeEnabled = true;
                        player.setRunEnergyRaw(10000);
                        player.forcedMovementActive = true; // Avoid map collision dependencies.
                        player.getMovementQueue().clear();
                        player.getMovementQueue().addStep(new Position(3224, 3222, 0));
                        player.getMovementQueue().removeFirstStep();
                        World.processTick();
                        require(player.getPosition().getX() == 3224, "443 player did not move two running steps");
                        require(player.getRunEnergyRaw() == 9933,
                                "Run energy did not drain while debug mode was enabled");
                    } finally {
                        ServerSettings.debugModeEnabled = previousDebug;
                    }
                } finally {
                    World.getPlayers()[player.getIndex()] = null;
                }
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

package com.rs2;

import com.rs2.model.World;
import com.rs2.model.player.Player;
import com.rs2.model.player.PlayerConnectionState;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/** Serves live stats of every logged-in player (bots included) for the client Hiscores drawer. */
public final class HiscoresHttp {
    private HiscoresHttp() {
    }

    public static void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 43597), 0);
            server.createContext("/hiscores", exchange -> {
                byte[] body = build().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(body);
                }
            });
            server.createContext("/players", exchange -> {
                byte[] body = buildPlayers().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(body);
                }
            });
            server.createContext("/map/image", exchange -> {
                java.io.File f = new java.io.File("./data/launcher/world_map_2006.jpg");
                if (!f.isFile()) {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                    return;
                }
                exchange.getResponseHeaders().add("Content-Type", "image/jpeg");
                exchange.sendResponseHeaders(200, f.length());
                try (OutputStream out = exchange.getResponseBody()) {
                    java.nio.file.Files.copy(f.toPath(), out);
                }
            });
            server.createContext("/map/sprite", exchange -> {
                byte[] body = null;
                try {
                    String q = exchange.getRequestURI().getQuery();
                    int id = Integer.parseInt(q.substring(q.indexOf("id=") + 3).split("&")[0]);
                    java.io.File f = spriteFor(id);
                    if (f != null) body = java.nio.file.Files.readAllBytes(f.toPath());
                } catch (Throwable ignored) {
                }
                if (body == null) {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                    return;
                }
                exchange.getResponseHeaders().add("Content-Type", "image/png");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(body);
                }
            });
            server.setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "hiscores-http");
                t.setDaemon(true);
                return t;
            }));
            server.start();
            System.out.println("Hiscores endpoint on 127.0.0.1:43597");
        } catch (Throwable t) {
            System.out.println("Hiscores endpoint failed: " + t);
        }
    }

    private static java.util.Map<Integer, java.io.File> sprites;

    private static synchronized java.io.File spriteFor(int regionId) {
        if (sprites == null) {
            sprites = new java.util.HashMap<>();
            java.io.File[] files = new java.io.File("./data/launcher/sprites/0").listFiles();
            if (files != null) {
                for (java.io.File f : files) {
                    String n = f.getName();
                    if (!n.endsWith(".png")) continue;
                    String[] parts = n.substring(0, n.length() - 4).split(" ");
                    if (parts.length == 2) {
                        try { sprites.put(Integer.parseInt(parts[1]), f); } catch (NumberFormatException ignored) { }
                    }
                }
            }
        }
        return sprites.get(regionId);
    }

    private static String buildPlayers() {
        StringBuilder sb = new StringBuilder();
        Player[] players = World.getPlayers();
        for (int i = 1; i < players.length; i++) {
            try {
                Player p = players[i];
                if (p == null || p.getConnectionState() == PlayerConnectionState.DISCONNECTED || p.getUsername() == null) continue;
                sb.append(p.getUsername().replace('\t', ' ')).append('\t').append(p.getPosition().getX()).append('\t')
                    .append(p.getPosition().getY()).append('\t').append(p.getPosition().getPlane()).append('\t')
                    .append(p.isBot ? 1 : 0).append('\n');
            } catch (Throwable ignored) {
            }
        }
        return sb.toString();
    }

    private static String build() {
        StringBuilder sb = new StringBuilder();
        Player[] players = World.getPlayers();
        for (int i = 1; i < players.length; i++) {
            try {
                Player p = players[i];
                if (p == null || p.getConnectionState() == PlayerConnectionState.DISCONNECTED || p.getUsername() == null) continue;
                int total = 0;
                double totalXp = 0;
                StringBuilder lv = new StringBuilder();
                StringBuilder xp = new StringBuilder();
                for (int s = 0; s < 21; s++) {
                    int l = p.getSkillManager().getBaseLevel(s);
                    double x = p.getSkillManager().getExperience()[s];
                    total += l;
                    totalXp += x;
                    lv.append('\t').append(l);
                    xp.append('\t').append((long) x);
                }
                sb.append(p.getUsername().replace('\t', ' ')).append('\t').append(total).append('\t').append((long) totalXp).append(lv).append(xp).append('\n');
            } catch (Throwable ignored) {
            }
        }
        return sb.toString();
    }
}

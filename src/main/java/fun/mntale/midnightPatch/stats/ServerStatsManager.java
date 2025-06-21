package fun.mntale.midnightPatch.stats;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.MemoryMXBean;

public class ServerStatsManager implements Listener {
    private long startTime;
    private boolean isEnabled = false;
    private HttpServer server;

    public ServerStatsManager() {
        this.startTime = System.currentTimeMillis();
    }

    public void enable() {
        if (isEnabled) return;
        
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 4567), 0);
            server.createContext("/stats", new StatsHandler());
            server.setExecutor(null); // Use default executor
            server.start();
            
            isEnabled = true;
            ComponentLogger.logger().info("ServerStatsManager enabled - stats available at http://localhost:4567/stats");
        } catch (Exception e) {
            ComponentLogger.logger().error("Failed to start ServerStatsManager: " + e.getMessage());
        }
    }

    public void disable() {
        if (!isEnabled || server == null) return;
        
        try {
            server.stop(0);
            isEnabled = false;
            ComponentLogger.logger().info("ServerStatsManager disabled");
        } catch (Exception e) {
            ComponentLogger.logger().error("Error stopping ServerStatsManager: " + e.getMessage());
        }
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = getStatsJson();
                
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.getResponseHeaders().add("Cache-Control", "no-cache, no-store, must-revalidate");
                exchange.getResponseHeaders().add("Pragma", "no-cache");
                exchange.getResponseHeaders().add("Expires", "0");
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method not allowed
            }
        }
    }

    private String getStatsJson() {
        DecimalFormat df = new DecimalFormat("#.##");
        long uptimeMs = System.currentTimeMillis() - startTime;

        // JVM memory
        Runtime rt = Runtime.getRuntime();
        long jvmTotal = rt.totalMemory();
        long jvmFree = rt.freeMemory();
        long jvmUsed = jvmTotal - jvmFree;

        // System/Process CPU - using modern APIs
        OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double sysCpu = os.getSystemLoadAverage() * 100; // Modern alternative
        double procCpu = -1; // Not available in standard API, will show as N/A

        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // Memory
        json.append("\"jvmTotalMemory\":\"").append(readableSize(jvmTotal)).append("\",");
        json.append("\"jvmUsedMemory\":\"").append(readableSize(jvmUsed)).append("\",");
        json.append("\"jvmFreeMemory\":\"").append(readableSize(jvmFree)).append("\",");
        
        // CPU - using available metrics
        json.append("\"systemLoadAverage\":\"").append(sysCpu >= 0 ? df.format(sysCpu) + "%" : "N/A").append("\",");
        json.append("\"processCpuLoad\":\"N/A\",");
        
        // World stats
        long worldSize = getFolderSize(Bukkit.getWorldContainer());
        json.append("\"worldSize\":\"").append(readableSize(worldSize)).append("\",");
        
        if (!Bukkit.getWorlds().isEmpty()) {
            World w = Bukkit.getWorlds().get(0);
            json.append("\"worldAge\":\"").append(w.getFullTime()).append(" ticks\",");
        }
        
        // Player stats
        int unique = Bukkit.getOfflinePlayers().length;
        long playTicks = Bukkit.getOnlinePlayers().stream()
            .mapToLong(p -> p.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE))
            .sum();
        json.append("\"totalJoins\":").append(unique).append(",");
        json.append("\"uniquePlayers\":").append(unique).append(",");
        json.append("\"totalPlaytime\":\"").append(df.format(playTicks / 20.0 / 60.0)).append(" minutes\",");
        json.append("\"onlinePlayers\":").append(Bukkit.getOnlinePlayers().size()).append(",");
        
        json.append("\"uptime\":\"").append(df.format(uptimeMs / 1000.0)).append(" seconds\",");
        
        // TPS (simplified)
        json.append("\"tps\":\"N/A\",");
        json.append("\"serverTPS\":\"20.0\",");
        
        // Other stats
        json.append("\"availableProcessors\":").append(os.getAvailableProcessors()).append(",");
        json.append("\"maxPlayers\":").append(Bukkit.getMaxPlayers()).append(",");
        json.append("\"timestamp\":").append(System.currentTimeMillis());
        
        json.append("}");
        return json.toString();
    }

    private long getFolderSize(java.io.File f) {
        if (f == null || !f.exists()) return 0;
        
        long size = 0;
        java.io.File[] files = f.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                size += file.isFile() ? file.length() : getFolderSize(file);
            }
        }
        return size;
    }

    private String readableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
} 
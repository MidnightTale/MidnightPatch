package fun.mntale.midnightPatch.module.entity.player.locatorbar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAssets;
import fun.mntale.midnightPatch.command.ToggleLocatorBarCommand;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import fun.mntale.midnightPatch.MidnightPatch;

public class LocatorBar implements Listener {
    private static final Map<UUID, Location> playerLocations = new HashMap<>();
    private static boolean enabled = false;
    
    public static void start() {
        if (enabled) return;
        enabled = true;
    }
    
    public static void stop() {
        enabled = false;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            FoliaScheduler.getEntityScheduler().run(player, MidnightPatch.instance, (task) -> {
                removeAllWaypoints(player);
            }, null);
        }
        playerLocations.clear();
    }
    
    public static void setEnabled(boolean enabled) {
        if (enabled) {
            start();
        } else {
            stop();
        }
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        FoliaScheduler.getEntityScheduler().run(player, MidnightPatch.instance, (task) -> {
            playerLocations.put(player.getUniqueId(), player.getLocation());
            
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(player)) {
                    sendWaypoint(player, onlinePlayer, onlinePlayer.getLocation());
                }
            }
        }, null);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        FoliaScheduler.getEntityScheduler().run(player, MidnightPatch.instance, (task) -> {
            removeWaypointFromAllPlayers(playerId);
            playerLocations.remove(playerId);
        }, null);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!enabled) return;
        
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to != null && (Math.abs(to.getX() - from.getX()) > 0.5 || 
                          Math.abs(to.getY() - from.getY()) > 0.5 || 
                          Math.abs(to.getZ() - from.getZ()) > 0.5)) {
            
            FoliaScheduler.getEntityScheduler().run(player, MidnightPatch.instance, (task) -> {
                playerLocations.put(player.getUniqueId(), to);
                updatePlayerWaypoint(player, to);
            }, null);
        }
    }
    
    private static void updatePlayerWaypoint(Player movingPlayer, Location newLocation) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.equals(movingPlayer)) {
                FoliaScheduler.getEntityScheduler().run(onlinePlayer, MidnightPatch.instance, (task) -> {
                    sendWaypoint(onlinePlayer, movingPlayer, newLocation);
                }, null);
            }
        }
    }
    
    private static void sendWaypoint(Player recipient, Player target, Location location) {
        try {
            if (!ToggleLocatorBarCommand.isLocatorBarEnabled(target)) {
                return;
            }
            
            ServerPlayer nmsPlayer = 
                ((CraftPlayer) recipient).getHandle();
            
            Waypoint.Icon icon = new Waypoint.Icon();
            icon.style = WaypointStyleAssets.DEFAULT;
            icon.color = Optional.of(generateColorFromUUID(target.getUniqueId()));
            
            BlockPos blockPos = new net.minecraft.core.BlockPos(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
            );
            
            net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket packet = 
                net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket.addWaypointPosition(
                    target.getUniqueId(),
                    icon,
                    blockPos
                );
            
            nmsPlayer.connection.send(packet);
            
        } catch (Exception e) {
            System.err.println("Error sending waypoint packet: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void removeWaypointFromAllPlayers(UUID playerId) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            FoliaScheduler.getEntityScheduler().run(onlinePlayer, MidnightPatch.instance, (task) -> {
                removeWaypoint(onlinePlayer, playerId);
            }, null);
        }
    }
    
    private static void removeWaypoint(Player recipient, UUID targetId) {
        try {
            ServerPlayer nmsPlayer = 
                ((CraftPlayer) recipient).getHandle();
            
            ClientboundTrackedWaypointPacket packet = 
                ClientboundTrackedWaypointPacket.removeWaypoint(targetId);
            
            nmsPlayer.connection.send(packet);
            
        } catch (Exception e) {
            System.err.println("Error removing waypoint packet: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void removeAllWaypoints(Player player) {
        try {
            ServerPlayer nmsPlayer = 
                ((CraftPlayer) player).getHandle();
            
            for (UUID playerId : playerLocations.keySet()) {
                net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket packet = 
                    net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket.removeWaypoint(playerId);
                
                nmsPlayer.connection.send(packet);
            }
            
        } catch (Exception e) {
            System.err.println("Error removing all waypoints: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate a consistent color from a player's UUID
     * @param uuid The player's UUID
     * @return RGB color as integer
     */
    private static int generateColorFromUUID(UUID uuid) {
        // Use the UUID's hash code to generate a consistent color
        int hash = uuid.hashCode();
        
        // Generate RGB values using different parts of the hash
        int red = Math.abs(hash) % 256;
        int green = Math.abs(hash >> 8) % 256;
        int blue = Math.abs(hash >> 16) % 256;
        
        // Ensure minimum brightness for visibility
        if (red + green + blue < 200) {
            red = Math.min(255, red + 100);
            green = Math.min(255, green + 100);
            blue = Math.min(255, blue + 100);
        }
        
        return (red << 16) | (green << 8) | blue;
    }
}

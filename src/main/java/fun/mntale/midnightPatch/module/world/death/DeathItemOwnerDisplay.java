package fun.mntale.midnightPatch.module.world.death;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import fun.mntale.midnightPatch.MidnightPatch;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.entity.Display;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shows death item owner name only for the item the player is looking at (center of FOV).
 */
public class DeathItemOwnerDisplay implements Listener {
    
    // Cached reflection fields for performance
    private static Field isDeathLootField;
    private static Field targetField;
    private static boolean fieldsInitialized = false;
    
    // Pre-created transformation for bigger text
    private static final Transformation BIGGER_TRANSFORM = new Transformation(
        new Vector3f(0, 0, 0),
        new AxisAngle4f(0, 0, 0, 1),
        new Vector3f(1.2f, 1.2f, 1.2f), // 120% size - bigger text
        new AxisAngle4f(0, 0, 0, 1)
    );
    
    // Simple caches
    private final Map<UUID, String> nameCache = new ConcurrentHashMap<>();
    private final Map<String, Component> componentCache = new ConcurrentHashMap<>();
    
    private final Map<UUID, WrappedTask> tasks = new ConcurrentHashMap<>();
    private final Map<UUID, TextDisplay> playerDisplays = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> lastLookedAtItem = new ConcurrentHashMap<>();
    
    public static void initializeReflectionFields() {
        if (fieldsInitialized) return;
        
        try {
            isDeathLootField = ItemEntity.class.getDeclaredField("isDeathLoot");
            isDeathLootField.setAccessible(true);
            
            targetField = ItemEntity.class.getDeclaredField("target");
            targetField.setAccessible(true);
            
            fieldsInitialized = true;
        } catch (Exception e) {
            // Fields don't exist
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        startFor(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        stopFor(event.getPlayer());
    }
    
    public void startFor(Player player) {
        stopFor(player);
        
        // Run every tick for instant response when looking at items
        WrappedTask task = MidnightPatch.instance.foliaLib.getScheduler().runAtEntityTimer(player, () -> {
            updateDisplayForPlayer(player);
        }, null, 0L, 1L);
        
        tasks.put(player.getUniqueId(), task);
    }
    
    public void stopFor(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        WrappedTask task = tasks.remove(playerUUID);
        if (task != null) {
            task.cancel();
        }
        
        removeDisplay(player);
        lastLookedAtItem.remove(playerUUID);
    }
    
    private void updateDisplayForPlayer(Player player) {
        if (!fieldsInitialized) return;
        
        UUID playerUUID = player.getUniqueId();
        
        // Find death items in a cone around the player's look direction
        Item closestDeathItem = null;
        UUID closestOwnerUUID = null;
        double closestDistance = Double.MAX_VALUE;
        
        // Check nearby items within a reasonable range
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(10, 6, 10)) {
            if (!(entity instanceof Item item)) continue;
            
            UUID ownerUUID = getDeathItemOwnerFast(item);
            if (ownerUUID == null) continue;
            
            // Check if item is within the player's field of view cone
            if (isInFieldOfView(player, item)) {
                double distance = player.getEyeLocation().distance(item.getLocation());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestDeathItem = item;
                    closestOwnerUUID = ownerUUID;
                }
            }
        }
        
        UUID currentLookedAtItem = closestDeathItem != null ? closestDeathItem.getUniqueId() : null;
        UUID lastItem = lastLookedAtItem.get(playerUUID);
        
        // If looking at the same item, just update position
        if (currentLookedAtItem != null && currentLookedAtItem.equals(lastItem)) {
            updateDisplayPosition(player, closestDeathItem);
            return;
        }
        
        // Remove old display if exists
        removeDisplay(player);
        
        // Create new display if looking at a death item
        if (currentLookedAtItem != null && closestOwnerUUID != null) {
            createDisplay(player, closestDeathItem, closestOwnerUUID);
            lastLookedAtItem.put(playerUUID, currentLookedAtItem);
        } else {
            lastLookedAtItem.remove(playerUUID);
        }
    }
    
    /**
     * Check if an item is within the player's field of view cone
     */
    private boolean isInFieldOfView(Player player, Item item) {
        Location eyeLoc = player.getEyeLocation();
        Location itemLoc = item.getLocation();
        
        // Vector from player to item
        org.bukkit.util.Vector toItem = itemLoc.toVector().subtract(eyeLoc.toVector());
        
        // Player's look direction
        org.bukkit.util.Vector lookDirection = eyeLoc.getDirection();
        
        // Normalize vectors
        toItem.normalize();
        lookDirection.normalize();
        
        // Calculate angle between look direction and item direction
        double dotProduct = toItem.dot(lookDirection);
        double angle = Math.acos(Math.max(-1.0, Math.min(1.0, dotProduct)));
        
        // Convert to degrees and check if within FOV cone (45 degrees from center)
        double angleDegrees = Math.toDegrees(angle);
        return angleDegrees <= 45.0; // Bigger cone - 90 degree total FOV
    }
    
    private void createDisplay(Player player, Item item, UUID ownerUUID) {
        String ownerName = getOwnerName(ownerUUID);
        Component message = getNameComponent(ownerName);
        
        Location displayLoc = item.getLocation().clone();
        displayLoc.add(0, 1.2, 0); // Higher position - 1.2 blocks above item
        
        TextDisplay display = item.getWorld().spawn(displayLoc, TextDisplay.class, e -> {
            e.setBillboard(Display.Billboard.CENTER);
            e.setSeeThrough(true);
            e.setShadowed(false);
            e.text(message);
            e.setViewRange(16f);
            e.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
            e.setTransformation(BIGGER_TRANSFORM);
        });
        
        // Hide from other players
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer != player) {
                onlinePlayer.hideEntity(MidnightPatch.instance, display);
            }
        }
        
        playerDisplays.put(player.getUniqueId(), display);
    }
    
    private void updateDisplayPosition(Player player, Item item) {
        TextDisplay display = playerDisplays.get(player.getUniqueId());
        if (display != null && display.isValid()) {
            Location newLoc = item.getLocation().clone();
            newLoc.add(0, 1.2, 0); // Same higher position
            display.teleportAsync(newLoc);
        }
    }
    
    private String getOwnerName(UUID ownerUUID) {
        return nameCache.computeIfAbsent(ownerUUID, uuid -> {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(uuid);
            String name = owner.getName();
            return name != null ? name : "Unknown";
        });
    }
    
    private Component getNameComponent(String name) {
        return componentCache.computeIfAbsent(name, n -> 
            Component.text(n, NamedTextColor.YELLOW)
        );
    }
    
    private void removeDisplay(Player player) {
        TextDisplay display = playerDisplays.remove(player.getUniqueId());
        if (display != null && display.isValid()) {
            display.remove();
        }
    }
    
    /**
     * Fast death item owner check using cached reflection
     */
    private UUID getDeathItemOwnerFast(Item item) {
        if (!fieldsInitialized) return null;
        
        try {
            CraftItem craftItem = (CraftItem) item;
            ItemEntity nmsItem = craftItem.getHandle();
            
            boolean isDeathLoot = isDeathLootField.getBoolean(nmsItem);
            if (isDeathLoot) {
                Object target = targetField.get(nmsItem);
                return target instanceof UUID ? (UUID) target : null;
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return null;
    }
    
    /**
     * Cleanup method for plugin disable
     */
    public void cleanup() {
        // Stop all tasks
        for (WrappedTask task : tasks.values()) {
            if (task != null) task.cancel();
        }
        tasks.clear();
        
        // Remove all displays
        for (TextDisplay display : playerDisplays.values()) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        playerDisplays.clear();
        lastLookedAtItem.clear();
        nameCache.clear();
        componentCache.clear();
    }
}
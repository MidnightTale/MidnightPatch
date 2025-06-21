package fun.mntale.midnightPatch.chunk.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.util.RayTraceResult;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.folia.TaskWrapper;
import fun.mntale.midnightPatch.command.ToggleReachAroundCommand;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ReachAroundBlockManager implements Listener {
    
    private final Map<UUID, BlockDisplay> previewDisplays = new ConcurrentHashMap<>();
    private final Map<UUID, Location> lastPreviewLocation = new ConcurrentHashMap<>();
    private final Map<UUID, TaskWrapper> playerTasks = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Start preview task for the player
        startPlayerTask(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Stop preview task and remove displays
        stopPlayerTask(event.getPlayer());
        removePreview(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        
        if (item == null || !item.getType().isBlock()) {
            removePreview(player);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Filter out only right-click and main hand
        if (!event.getAction().isRightClick() || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Check if reach-around is enabled for this player
        if (!ToggleReachAroundCommand.isReachAroundEnabled(player)) {
            return;
        }
        
        ItemStack item = event.getItem();
        
        // Skip if no item or not a placeable block
        if (item == null || !item.getType().isBlock()) {
            return;
        }
        
        // Try to find reach-around placement location
        Location reachAroundLocation = getPlayerReachAroundTarget(player);
        
        if (reachAroundLocation != null) {
            Block targetBlock = reachAroundLocation.getBlock();
            
            // If preview shows, we can place here
            if (targetBlock.getType().isAir()) {
                // Place the block at the reach-around location
                targetBlock.setType(item.getType());
                
                // Handle block data if needed (for directional blocks)
                if (item.getType().name().contains("STAIRS") || 
                    item.getType().name().contains("SLAB") ||
                    item.getType().name().contains("FENCE") ||
                    item.getType().name().contains("WALL")) {
                    // Set appropriate block data based on player direction
                    setBlockDirection(targetBlock, player);
                }
                
                // Consume item if not in creative mode
                if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    item.setAmount(item.getAmount() - 1);
                }
                
                // Play sound effect based on block type
                org.bukkit.Sound sound = getBlockPlaceSound(item.getType());
                player.getWorld().playSound(reachAroundLocation, sound, 1.0f, 1.0f);
                
                // Spawn particles
                // player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, reachAroundLocation.add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2, 0.0);
                
                // Debug message
                // player.sendMessage("§aReach-around block placed at: " + reachAroundLocation.getBlockX() + ", " + reachAroundLocation.getBlockY() + ", " + reachAroundLocation.getBlockZ());
            }
        }
    }
    
    private void startPlayerTask(Player player) {
        UUID playerId = player.getUniqueId();
        if (playerTasks.containsKey(playerId)) {
            return; // Task already running
        }
        
        // Start continuous preview task
        TaskWrapper task = FoliaScheduler.getRegionScheduler().runAtFixedRate(
            fun.mntale.midnightPatch.MidnightPatch.instance,
            player.getLocation(),
            taskBase -> {
                if (!player.isOnline()) {
                    stopPlayerTask(player);
                    return;
                }
                
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null || !item.getType().isBlock()) {
                    removePreview(player);
                    return;
                }
                
                // Check if reach-around is enabled for this player
                if (!ToggleReachAroundCommand.isReachAroundEnabled(player)) {
                    removePreview(player);
                    return;
                }
                
                // Update preview
                updatePreview(player, item.getType());
            },
            60L, // Start after 3 seconds (60 ticks)
            2L   // Run every 2 ticks (10 times per second)
        );
        
        playerTasks.put(playerId, task);
    }
    
    private void stopPlayerTask(Player player) {
        UUID playerId = player.getUniqueId();
        TaskWrapper task = playerTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }
    
    public Location getPlayerReachAroundTarget(Player player) {
        // First try normal raycast to see if we hit a solid block face
        RayTraceResult rayTraceResult = player.getWorld().rayTraceBlocks(
            player.getEyeLocation(), 
            player.getEyeLocation().getDirection(), 
            5
        );
        
        // If we hit a solid block face, check if we can place against it normally
        if (rayTraceResult != null && rayTraceResult.getHitBlock() != null) {
            Block hitBlock = rayTraceResult.getHitBlock();
            BlockFace hitFace = rayTraceResult.getHitBlockFace();
            
            // Check if the face we hit is a solid face we can place against
            if (hitFace != null && hitBlock.getType().isSolid()) {
                Block adjacentBlock = hitBlock.getRelative(hitFace);
                
                // If the adjacent block is air, normal placement is possible
                if (adjacentBlock.getType().isAir()) {
                    return null; // Normal placement possible, no reach-around needed
                }
            }
        }
        
        // If no direct hit OR if the hit face is blocked, try reach-around methods
        Location target = getPlayerVerticalReachAround(player);
        if (target == null) {
            target = getPlayerHorizontalReachAround(player);
        }
        
        return target;
    }
    
    private Location getPlayerVerticalReachAround(Player player) {
        // Try vertical reach-around (placing above/below blocks)
        Vector vec = new Vector(0, 0.5, 0);
        RayTraceResult rayTrace = player.getWorld().rayTraceBlocks(
            player.getEyeLocation(), 
            player.getEyeLocation().getDirection().clone().add(vec), 
            5
        );
        
        if (rayTrace != null && rayTrace.getHitBlock() != null) {
            Location block = rayTrace.getHitBlock().getLocation();
            Location playerLoc = player.getLocation();
            
            // Exact same distance checks as original plugin
            if (playerLoc.getZ() - block.getZ() < 1.3 && 
                playerLoc.getY() - block.getY() == 1 && 
                playerLoc.getX() - block.getX() < 1.3) {
                
                Location target = block.subtract(0, 1, 0);
                if (target.getBlock().getType() == Material.AIR) {
                    return target;
                }
            }
        }
        
        return null;
    }
    
    private Location getPlayerHorizontalReachAround(Player player) {
        // Try horizontal reach-around (placing beside blocks)
        Location playerLoc = player.getLocation();
        BlockFace facing = player.getFacing();
        Vector direction = player.getEyeLocation().getDirection();
        Vector vec = new Vector(0.5 * facing.getModX(), 0, 0.5 * facing.getModZ());
        
        RayTraceResult rayTrace = player.getWorld().rayTraceBlocks(
            player.getEyeLocation(), 
            direction.clone().subtract(vec), 
            4
        );
        
        if (rayTrace != null && rayTrace.getHitBlock() != null) {
            Location loc = rayTrace.getHitBlock().getLocation();
            
            // Exact same distance calculation as original plugin
            double distance = (playerLoc.getX() - loc.getX()) + (playerLoc.getY() - loc.getY()) + (playerLoc.getZ() - loc.getZ()) / 3;
            
            // Exact same distance range as original plugin
            if (distance < 1.85 && distance > 1.3) {
                Block target = loc.getBlock().getRelative(player.getFacing());
                if (target.getType() == Material.AIR) {
                    return target.getLocation();
                }
            }
        }
        
        return null;
    }
    
    private void updatePreview(Player player, Material blockType) {
        Location previewLocation = getPlayerReachAroundTarget(player);
        
        if (previewLocation == null) {
            removePreview(player);
            return;
        }
        
        // Check if preview location changed
        Location lastLocation = lastPreviewLocation.get(player.getUniqueId());
        if (lastLocation != null && lastLocation.equals(previewLocation)) {
            return; // No need to update
        }
        
        // Remove old preview
        removePreview(player);
        
        // Create new preview
        BlockDisplay display = player.getWorld().spawn(previewLocation, BlockDisplay.class);
        
        // Create block data with proper rotation for directional blocks
        org.bukkit.block.data.BlockData blockData = blockType.createBlockData();
        
        // Handle directional blocks
        if (blockData instanceof org.bukkit.block.data.Directional directional) {
            // Set direction based on player's facing direction
            BlockFace facing = getPlayerFacingDirection(player);
            directional.setFacing(facing);
            blockData = directional;
        }
        
        display.setBlock(blockData);
        
        // Make it semi-transparent with dynamic brightness based on time of day
        long time = player.getWorld().getTime();
        int brightness;
        
        // Day time (0-12000): use dark brightness
        // Night time (12000-24000): use light brightness
        if (time >= 0 && time < 12000) {
            // Day time - use dark brightness
            brightness = 1; // Dark
        } else {
            // Night time - use light brightness
            brightness = 15; // Light
        }
        
        display.setBrightness(new Display.Brightness(brightness, brightness));
        display.setTransformation(new Transformation(
            new Vector3f(0, 0, 0),
            new AxisAngle4f(0, 0, 0, 1),
            new Vector3f(1, 1, 1),
            new AxisAngle4f(0, 0, 0, 1)
        ));
        
        // Set transparency and color
        display.setInterpolationDuration(0);
        display.setInterpolationDelay(0);
        
        // Store the preview
        previewDisplays.put(player.getUniqueId(), display);
        lastPreviewLocation.put(player.getUniqueId(), previewLocation);
    }
    
    private BlockFace getPlayerFacingDirection(Player player) {
        // Set block direction based on player's facing direction
        float yaw = player.getLocation().getYaw();
        
        // Normalize yaw to 0-360
        if (yaw < 0) yaw += 360;
        
        // Determine the facing direction
        if (yaw >= 315 || yaw < 45) {
            return BlockFace.SOUTH;
        } else if (yaw >= 45 && yaw < 135) {
            return BlockFace.WEST;
        } else if (yaw >= 135 && yaw < 225) {
            return BlockFace.NORTH;
        } else {
            return BlockFace.EAST;
        }
    }
    
    private void removePreview(Player player) {
        UUID playerId = player.getUniqueId();
        BlockDisplay display = previewDisplays.remove(playerId);
        if (display != null && !display.isDead()) {
            display.remove();
        }
        lastPreviewLocation.remove(playerId);
    }

    private void setBlockDirection(Block block, Player player) {
        // Apply the direction to the block data
        try {
            if (block.getBlockData() instanceof org.bukkit.block.data.Directional directional) {
                BlockFace facing = getPlayerFacingDirection(player);
                directional.setFacing(facing);
                block.setBlockData(directional);
            }
        } catch (Exception e) {
            // Ignore if block data can't be set
        }
    }

    private org.bukkit.Sound getBlockPlaceSound(Material blockType) {
        String materialName = blockType.name();
        
        // Wood blocks
        if (materialName.contains("WOOD") || materialName.contains("LOG") || materialName.contains("PLANKS") || 
            materialName.contains("FENCE") || materialName.contains("STAIRS") || materialName.contains("SLAB") ||
            materialName.contains("TRAPDOOR") || materialName.contains("DOOR") || materialName.contains("SIGN")) {
            return org.bukkit.Sound.BLOCK_WOOD_PLACE;
        }
        
        // Stone blocks
        if (materialName.contains("STONE") || materialName.contains("COBBLESTONE") || materialName.contains("BRICKS") ||
            materialName.contains("CONCRETE") || materialName.contains("TERRACOTTA")) {
            return org.bukkit.Sound.BLOCK_STONE_PLACE;
        }
        
        // Glass blocks
        if (materialName.contains("GLASS") || materialName.contains("ICE")) {
            return org.bukkit.Sound.BLOCK_GLASS_PLACE;
        }
        
        // Metal blocks
        if (materialName.contains("IRON") || materialName.contains("GOLD") || materialName.contains("DIAMOND") ||
            materialName.contains("EMERALD") || materialName.contains("LAPIS") || materialName.contains("REDSTONE") ||
            materialName.contains("COAL") || materialName.contains("QUARTZ")) {
            return org.bukkit.Sound.BLOCK_METAL_PLACE;
        }
        
        // Dirt/grass blocks
        if (materialName.contains("DIRT") || materialName.contains("GRASS") || materialName.contains("SAND") ||
            materialName.contains("GRAVEL") || materialName.contains("CLAY")) {
            return org.bukkit.Sound.BLOCK_GRAVEL_PLACE;
        }
        
        // Wool/carpet
        if (materialName.contains("WOOL") || materialName.contains("CARPET")) {
            return org.bukkit.Sound.BLOCK_WOOL_PLACE;
        }
        
        // Nether blocks
        if (materialName.contains("NETHER") || materialName.contains("SOUL") || materialName.contains("BASALT")) {
            return org.bukkit.Sound.BLOCK_NETHERRACK_PLACE;
        }
        
        // Default to stone sound
        return org.bukkit.Sound.BLOCK_STONE_PLACE;
    }
} 
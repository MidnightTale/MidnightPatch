package fun.mntale.midnightPatch.module.world.enchantment;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RecastingEnchantment implements Listener {
    
    private static final int RECAST_DELAY_TICKS = 20; // 1 second delay before recasting
    
    private final Map<UUID, WrappedTask> autoFishTasks = new ConcurrentHashMap<>();
    
    private static final NamespacedKey RECAST_KEY = NamespacedKey.fromString("midnightpatch:recasting");

    private boolean hasRecastEnchantment(Player player) {
        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod.getType() != Material.FISHING_ROD) {
            rod = player.getInventory().getItemInOffHand();
            if (rod.getType() != Material.FISHING_ROD) {
                return false;
            }
        }
        Enchantment recastEnchant = io.papermc.paper.registry.RegistryAccess.registryAccess()
            .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
            .get(Objects.requireNonNull(RECAST_KEY));
        return recastEnchant != null && rod.containsEnchantment(recastEnchant);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (!hasRecastEnchantment(player)) {
            return;
        }
        
        switch (event.getState()) {
            case FISHING:
                // Rod was cast, stop any existing recast task
                stopRecastTask(player);
                break;
                
            case CAUGHT_FISH:
                // Fish was caught, schedule recast
                scheduleRecast(player);
                break;
                
            case IN_GROUND:
            case FAILED_ATTEMPT:
                // Bobber hit ground or failed, recast immediately
                stopRecastTask(player);
                break;
                
            case BITE:
                // Fish is biting, right-click to catch it after a short delay
                MidnightPatch.instance.foliaLib.getScheduler().runAtEntityLater(player, (taskObj) -> {
                    if (hasRecastEnchantment(player) && player.isOnline()) {
                        // Right-click to catch the fish (not recast)
                        catchFish(player);
                    }
                }, 5); // 5 tick delay to simulate reaction time
                break;
                
            case LURED:
                // Fish is being lured, this is handled automatically by the game
                break;
                
            case CAUGHT_ENTITY:
                // Caught an entity (not fish), recast
                scheduleRecast(player);
                break;
                
            case REEL_IN:
                // Reeling in, this is handled automatically by the game
                break;
        }
    }
    
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        if (newItem == null || newItem.getType() != Material.FISHING_ROD) {
            stopRecastTask(player);
        }
    }
    
    private void scheduleRecast(Player player) {
        // Cancel any existing task
        stopRecastTask(player);
        
        // Schedule new recast task
        WrappedTask task =  MidnightPatch.instance.foliaLib.getScheduler().runAtEntityLater(player, () -> {
            if (hasRecastEnchantment(player) && player.isOnline()) {
                recastFishingRod(player);
            }
            autoFishTasks.remove(player.getUniqueId());
        }, RECAST_DELAY_TICKS);
        
        autoFishTasks.put(player.getUniqueId(), task);
    }
    
    private void stopRecastTask(Player player) {
        WrappedTask task = autoFishTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    
    private void recastFishingRod(Player player) {
        // Check if player still has a fishing rod
        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod.getType() != Material.FISHING_ROD) {
            rod = player.getInventory().getItemInOffHand();
            if (rod.getType() != Material.FISHING_ROD) {
                return; // No fishing rod found
            }
        }
        
        // Check if rod has durability
        if (rod.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
            if (damageable.getDamage() >= rod.getType().getMaxDurability()) {
                return;
            }
        }
        
        // Simulate right-click with fishing rod to cast
        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (taskObj) -> {
            // Use the fishing rod to cast
            castFishingRod(player);
            
            // Play recast sound and particles
            Location loc = player.getLocation();
            player.playSound(loc, Sound.ENTITY_FISHING_BOBBER_THROW, 0.5f, 1.0f);
            player.spawnParticle(Particle.DRIPPING_WATER, loc.add(0, 1, 0), 5, 0.3, 0.3, 0.3);
        });
    }
    
    private void castFishingRod(Player player) {
        useFishingRod(player);
    }
    
    private void catchFish(Player player) {
        useFishingRod(player);
    }
    
    private void useFishingRod(Player player) {
        // Get the fishing rod from player's hand
        ItemStack rod = player.getInventory().getItemInMainHand();
        org.bukkit.inventory.EquipmentSlot hand = org.bukkit.inventory.EquipmentSlot.HAND;
        if (rod.getType() != Material.FISHING_ROD) {
            rod = player.getInventory().getItemInOffHand();
            hand = org.bukkit.inventory.EquipmentSlot.OFF_HAND;
            if (rod.getType() != Material.FISHING_ROD) {
                return; // No fishing rod found
            }
        }
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Client.USE_ITEM);
            com.comphenix.protocol.wrappers.EnumWrappers.Hand protocolHand =
                (hand == org.bukkit.inventory.EquipmentSlot.HAND) ?
                com.comphenix.protocol.wrappers.EnumWrappers.Hand.MAIN_HAND :
                com.comphenix.protocol.wrappers.EnumWrappers.Hand.OFF_HAND;
            packet.getHands().write(0, protocolHand);
            packet.getIntegers().write(0, 0); // Sequence (0 is fine for plugins)
            float yaw = player.getLocation().getYaw();
            float pitch = player.getLocation().getPitch();
            packet.getFloat().write(0, yaw);
            packet.getFloat().write(1, pitch);
            protocolManager.receiveClientPacket(player, packet);
            // Play hand animation for feedback
            PacketContainer animationPacket = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Client.ARM_ANIMATION);
            animationPacket.getHands().write(0, protocolHand);
            protocolManager.receiveClientPacket(player, animationPacket);
            // Play sound
            Location loc = player.getLocation();
            player.playSound(loc, Sound.ENTITY_FISHING_BOBBER_THROW, 0.5f, 1.0f);
        } catch (Exception e) {
            Location loc = player.getLocation();
            player.playSound(loc, Sound.ENTITY_FISHING_BOBBER_THROW, 0.5f, 1.0f);
        }
    }


}
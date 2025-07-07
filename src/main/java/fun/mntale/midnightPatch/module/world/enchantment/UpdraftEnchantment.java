package fun.mntale.midnightPatch.module.world.enchantment;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UpdraftEnchantment implements Listener {
    private static final NamespacedKey UPDRAFT_KEY = NamespacedKey.fromString("midnightpatch:updraft");
    public static final Enchantment UPDRAFT_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(Objects.requireNonNull(UPDRAFT_KEY));

    // Track number of updraft jumps used per player per airtime
    private final Map<UUID, Integer> updraftJumps = new ConcurrentHashMap<>();
    private final Map<UUID, Double> fallStartY = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> wasFalling = new ConcurrentHashMap<>();

    /**
     * Handles player join events. Ensures the player's flight state is set correctly
     * based on whether they have the Updraft enchantment on their boots. Resets jump count.
     */
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (task) -> {
            if (isCreativeOrSpectator(player)) {
                player.setAllowFlight(true); // Always allow flight in these modes
                return;
            }
            boolean hasUpdraft = shouldHaveUpdraft(player);
            player.setAllowFlight(hasUpdraft);
            player.setFlying(false);
            if (hasUpdraft && player.isOnGround()) {
                updraftJumps.remove(uuid);
            }
        });
    }

    /**
     * Handles player movement. Updates flight permission:
     * - Disables flight if player loses Updraft.
     * - Enables flight on ground and resets jump count.
     * - In air, allows flight if player has remaining updraft jumps.
     */
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (isCreativeOrSpectator(player)) return;
        boolean hasUpdraft = shouldHaveUpdraft(player);

        if (!hasUpdraft) {
            MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (task) -> {
                if (!isCreativeOrSpectator(player)) {
                    player.setAllowFlight(false);
                }
                fallStartY.remove(uuid);
                wasFalling.remove(uuid);
            });
            return;
        }

        if (player.isOnGround()) {
            // Landed
            if (wasFalling.getOrDefault(uuid, false)) {
                double startY = fallStartY.getOrDefault(uuid, player.getLocation().getY());
                double endY = player.getLocation().getY();
                double fallDistance = Math.max(0, startY - endY);

                int jumpsUsed = updraftJumps.getOrDefault(uuid, 0);

                if (fallDistance > 3 && jumpsUsed < 1) {
                    // No updraft used, apply vanilla fall damage formula
                    double damage = Math.max(0, fallDistance - 3);

                    // Apply Feather Falling reduction
                    ItemStack boots = player.getInventory().getBoots();
                    if (boots != null && boots.getType() != Material.AIR) {
                        int featherLevel = boots.getEnchantmentLevel(Enchantment.FEATHER_FALLING);
                        if (featherLevel > 0) {
                            double featherReduction = 1.0 - (0.12 * featherLevel);
                            damage *= featherReduction;
                        }
                    }

                    if (damage > 0) {
                        player.damage(damage);
                    }
                }
            }
            // Reset fall tracking and jump count
            fallStartY.remove(uuid);
            wasFalling.remove(uuid);
            updraftJumps.remove(uuid); // Always reset on ground
            MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (task) -> player.setAllowFlight(true));
        } else {
            // Just started falling
            if (!fallStartY.containsKey(uuid)) {
                fallStartY.put(uuid, player.getLocation().getY());
                wasFalling.put(uuid, true);
            }
            MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (task) -> {
                int level = getUpdraftLevel(player);
                int jumpsUsed = updraftJumps.getOrDefault(uuid, 0);
                player.setAllowFlight(jumpsUsed < level);
            });
        }
    }

    /**
     * Handles double-jump (flight toggle) events for Updraft:
     * - Cancels default flight.
     * - Increments jump count.
     * - Applies velocity boost in the direction the player is facing.
     * - Plays particle and sound effects.
     * - Disables further flight if jump limit is reached.
     */
    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (isCreativeOrSpectator(player)) return;
        if (!shouldHaveUpdraft(player)) return;
        int level = getUpdraftLevel(player);
        if (level < 1) return;
        UUID uuid = player.getUniqueId();
        int jumpsUsed = updraftJumps.getOrDefault(uuid, 0);
        if (jumpsUsed >= level) return;
        // Require 1 wind charge to use updraft
        ItemStack windCharge = new ItemStack(Material.WIND_CHARGE);
        if (!player.getInventory().containsAtLeast(windCharge, 1)) {
            // Play sound if no wind charge, do not updraft
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 0.7f);
            event.setCancelled(true);
            return;
        }
        // Remove 1 wind charge
        player.getInventory().removeItem(windCharge);
        event.setCancelled(true);
        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (task) -> {
            updraftJumps.put(uuid, jumpsUsed + 1);
            player.setAllowFlight(jumpsUsed + 1 < level);
            player.setFlying(false);
            Vector dir = player.getLocation().getDirection().normalize();
            double y = Math.max(0.35, Math.min(0.85, dir.getY()));
            dir.setY(y);
            dir.multiply(0.7 * (1.1 + 0.2 * (level - 1))); // Nerf: 70% of original
            player.setVelocity(dir);
            player.setFallDistance(0);
            player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation().add(0, 0.1, 0), 20, 0.2, 0.05, 0.2, 0.01);
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.2f);
            // Reduce boots durability by 1, factoring in Unbreaking
            ItemStack boots = player.getInventory().getBoots();
            if (boots != null && boots.getType() != Material.AIR && boots.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
                int unbreaking = boots.getEnchantmentLevel(Enchantment.UNBREAKING);
                boolean shouldDamage = unbreaking == 0 || java.util.concurrent.ThreadLocalRandom.current().nextInt(unbreaking + 1) == 0;
                if (shouldDamage) {
                    int newDamage = damageable.getDamage() + 1;
                    damageable.setDamage(newDamage);
                    boots.setItemMeta(damageable);
                }
            }
        });
    }

    /**
     * Handles landing after using Updraft (fall damage event):
     * - Only triggers if Updraft was actually used (midair jump).
     * - Cancels vanilla fall damage, applies custom reduced damage based on jumps used.
     * - Each updraft jump reduces fall damage by 50% (2 jumps = 100% reduction).
     * - Resets jump count and restores flight if player still has Updraft.
     */
    @EventHandler
    public void onLand(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!shouldHaveUpdraft(player)) return;

        UUID uuid = player.getUniqueId();
        int jumpsUsed = updraftJumps.getOrDefault(uuid, 0);

        event.setCancelled(true);

        // Calculate fall distance
        double startY = fallStartY.getOrDefault(uuid, player.getLocation().getY());
        double endY = player.getLocation().getY();
        double fallDistance = Math.max(0, startY - endY);

        // Use vanilla formula: damage = (fallDistance - 3)
        double originalDamage = Math.max(0, fallDistance - 3);

        double customDamage;
        if (jumpsUsed >= 1) {
            double damageMultiplier = Math.max(0.0, 1.0 - 0.5 * jumpsUsed);
            customDamage = originalDamage * damageMultiplier;
        } else {
            customDamage = originalDamage;
        }

        // Apply Feather Falling reduction (12% per level)
        ItemStack boots = player.getInventory().getBoots();
        if (boots != null && boots.getType() != Material.AIR) {
            int featherLevel = boots.getEnchantmentLevel(Enchantment.FEATHER_FALLING);
            if (featherLevel > 0) {
                double featherReduction = 1.0 - (0.12 * featherLevel);
                customDamage *= featherReduction;
            }
        }

        if (customDamage > 0) {
            player.damage(customDamage);
        }

        // Reset jump count, restore flight, clear fall start
        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (task) -> {
            updraftJumps.remove(uuid);
            fallStartY.remove(uuid);
            wasFalling.remove(uuid);
            if (shouldHaveUpdraft(player)) player.setAllowFlight(true);
        });
    }

    /**
     * Handles player quit events. Cleans up jump count tracking for the player.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        updraftJumps.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Handles armor changes. Updates flight permission if Updraft boots are equipped or removed.
     */
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (task) -> {
            if (isCreativeOrSpectator(player)) {
                player.setAllowFlight(true);
                return;
            }
            boolean hasUpdraft = shouldHaveUpdraft(player);
            if (!hasUpdraft) {
                player.setAllowFlight(false);
                // Do NOT remove updraftJumps here!
            } else if (player.isOnGround()) {
                player.setAllowFlight(true);
                updraftJumps.remove(uuid); // Only reset on ground
            } else {
                // In air, only allow flight if jumps remain
                int level = getUpdraftLevel(player);
                int jumpsUsed = updraftJumps.getOrDefault(uuid, 0);
                player.setAllowFlight(jumpsUsed < level);
            }
        });
    }

    /**
     * Checks if the player should have Updraft flight based on their boots and game mode.
     */
    private boolean shouldHaveUpdraft(Player player) {
        if (isCreativeOrSpectator(player)) return false;
        ItemStack boots = player.getInventory().getBoots();
        return boots != null && boots.getType() != Material.AIR && boots.containsEnchantment(Objects.requireNonNull(UPDRAFT_ENCHANT));
    }

    /**
     * Gets the Updraft enchantment level from the player's boots.
     */
    private int getUpdraftLevel(Player player) {
        ItemStack boots = player.getInventory().getBoots();
        return (boots != null && boots.getType() != Material.AIR) ? boots.getEnchantmentLevel(Objects.requireNonNull(UPDRAFT_ENCHANT)) : 0;
    }

    /**
     * Checks if the player is in Creative or Spectator mode.
     */
    private boolean isCreativeOrSpectator(Player player) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
    }
} 
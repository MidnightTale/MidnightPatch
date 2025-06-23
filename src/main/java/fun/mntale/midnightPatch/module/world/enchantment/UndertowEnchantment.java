package fun.mntale.midnightPatch.module.world.enchantment;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Particle;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import fun.mntale.midnightPatch.MidnightPatch;

public class UndertowEnchantment implements Listener {
    private static final NamespacedKey UNDERTOW_KEY = NamespacedKey.fromString("midnightpatch:undertow");

    private Enchantment getUndertowEnchantment() {
        return org.bukkit.Registry.ENCHANTMENT.get(UNDERTOW_KEY);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onTridentHit(EntityDamageByEntityEvent event) {
        // Melee trident attack
        if (event.getDamager() instanceof Player player) {
            if (!(event.getEntity() instanceof LivingEntity target)) return;
            ItemStack weapon = player.getInventory().getItemInMainHand();
            if (weapon == null || weapon.getType() != Material.TRIDENT) return;
            Enchantment undertow = getUndertowEnchantment();
            if (undertow == null || !weapon.containsEnchantment(undertow)) return;
            if (!player.isInWater()) return;
            int level = weapon.getEnchantmentLevel(undertow);
            if (level <= 0) return;
            // Melee: lower pull, cap so target never passes player
            pullTargetToPlayerMelee(target, player, level);
        }
        // Thrown trident
        else if (event.getDamager() instanceof Trident trident) {
            if (!(event.getEntity() instanceof LivingEntity target)) return;
            ItemStack tridentItem = trident.getItemStack();
            Enchantment undertow = getUndertowEnchantment();
            if (undertow == null || tridentItem == null || !tridentItem.containsEnchantment(undertow)) return;
            if (!(trident.getShooter() instanceof Player player)) return;
            if (!player.isInWater()) return;
            int level = tridentItem.getEnchantmentLevel(undertow);
            if (level <= 0) return;
            // Thrown: higher pull, even more if target is in water
            pullTargetToPlayerThrown(target, player, level);
        }
    }

    // Melee: lower pull, never pull past player
    private void pullTargetToPlayerMelee(LivingEntity target, Player player, int level) {
        FoliaScheduler.getEntityScheduler().run(target, MidnightPatch.instance, (task) -> {
            double baseStrength = 0.3 + 0.15 * (level - 1); // e.g., 0.3, 0.45, 0.6
            org.bukkit.util.Vector toPlayer = player.getLocation().toVector().subtract(target.getLocation().toVector());
            double distance = toPlayer.length();
            if (distance < 0.01) return; // Already at player
            org.bukkit.util.Vector pull = toPlayer.clone().normalize().multiply(baseStrength);
            // Cap pull so target never passes player
            if (pull.length() > distance) {
                pull = toPlayer.clone();
            }
            pull.setY(0);
            target.setVelocity(target.getVelocity().add(pull));
            // Play sound and particle effect
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.0f);
            target.getWorld().spawnParticle(Particle.BUBBLE, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
        }, null);
    }

    // Thrown: higher pull, even more if target is in water
    private void pullTargetToPlayerThrown(LivingEntity target, Player player, int level) {
        FoliaScheduler.getEntityScheduler().run(target, MidnightPatch.instance, (task) -> {
            double baseStrength = 0.7 + 0.3 * (level - 1); // e.g., 0.7, 1.0, 1.3
            if (target.isInWater()) {
                baseStrength *= 1.5; // 50% more if target is in water
            }
            org.bukkit.util.Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(baseStrength);
            pull.setY(0);
            target.setVelocity(target.getVelocity().add(pull));
            // Play sound and particle effect
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.0f);
            target.getWorld().spawnParticle(Particle.BUBBLE, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
        }, null);
    }
} 
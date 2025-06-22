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
            pullTargetToPlayer(target, player, level);
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
            pullTargetToPlayer(target, player, level);
        }
    }

    private void pullTargetToPlayer(LivingEntity target, Player player, int level) {
        FoliaScheduler.getEntityScheduler().run(target, MidnightPatch.instance, (task) -> {
            double strength = 0.5 + 0.25 * (level - 1); // 0.5, 0.75, 1.0
            org.bukkit.util.Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(strength);
            pull.setY(0);
            target.setVelocity(target.getVelocity().add(pull));
            // Play sound and particle effect
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.0f);
            target.getWorld().spawnParticle(Particle.BUBBLE, target.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
        }, null);
    }
} 
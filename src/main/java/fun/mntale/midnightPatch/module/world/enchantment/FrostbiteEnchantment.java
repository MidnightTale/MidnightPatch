package fun.mntale.midnightPatch.module.world.enchantment;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import fun.mntale.midnightPatch.MidnightPatch;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

import java.util.Objects;

public class FrostbiteEnchantment implements Listener {
    private static final NamespacedKey FROSTBITE_KEY = NamespacedKey.fromString("midnightpatch:frostbite");
    private static final int FREEZE_TICKS_PER_LEVEL = 160; // 8 seconds per level

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null) return;
        Enchantment frostbite = io.papermc.paper.registry.RegistryAccess.registryAccess()
            .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
            .get(Objects.requireNonNull(FROSTBITE_KEY));
        if (frostbite == null || !weapon.containsEnchantment(frostbite)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        int level = weapon.getEnchantmentLevel(frostbite);
        if (level <= 0) return;
        int freezeTicks = level * FREEZE_TICKS_PER_LEVEL;
        FoliaScheduler.getEntityScheduler().run(target, MidnightPatch.instance, (task) -> {
            target.setFreezeTicks(Math.max(target.getFreezeTicks(), freezeTicks));
            // Start a repeating particle task that matches the freeze timer
            spawnFreezeParticles(target);
        }, null);
    }

    private void spawnFreezeParticles(LivingEntity target) {
        FoliaScheduler.getEntityScheduler().runDelayed(target, MidnightPatch.instance, (t) -> {
            if (target.getFreezeTicks() > 0) {
                target.getWorld().spawnParticle(org.bukkit.Particle.SNOWFLAKE, target.getLocation().add(0, target.getHeight() / 2, 0), 20, 0.3, 0.5, 0.3, 0.01);
                // Schedule next repeat
                spawnFreezeParticles(target);
            }
        }, null, 10L);
    }
} 
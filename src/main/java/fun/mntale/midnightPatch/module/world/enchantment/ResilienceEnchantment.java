package fun.mntale.midnightPatch.module.world.enchantment;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fun.mntale.midnightPatch.MidnightPatch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Sound;
import org.bukkit.Particle;

public class ResilienceEnchantment implements Listener {
    private static final NamespacedKey RESILIENCE_KEY = NamespacedKey.fromString("midnightpatch:resilience");
    private static final PotionEffect RESISTANCE_EFFECT = new PotionEffect(PotionEffectType.RESISTANCE, 60, 3, true, false, true); // 2s, level 4
    private static final long COOLDOWN_MILLIS = 300_000L; // 5 minutes
    private final Map<Player, Long> cooldowns = new ConcurrentHashMap<>();

    private boolean hasFullResilienceSet(Player player) {
        Enchantment resilience = getResilienceEnchantment();
        if (resilience == null) return false;
        for (var armor : player.getInventory().getArmorContents()) {
            if (armor == null || !armor.containsEnchantment(resilience)) return false;
        }
        return true;
    }

    private Enchantment getResilienceEnchantment() {
        return io.papermc.paper.registry.RegistryAccess.registryAccess()
            .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
            .get(Objects.requireNonNull(RESILIENCE_KEY));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!hasFullResilienceSet(player)) return;
        double finalHealth = player.getHealth() - event.getFinalDamage();
        if (finalHealth > 0) return;
        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(player, 0L) > now) return;
        // Activate Last Breath
        event.setCancelled(true);
        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity( player, (task) -> {
            player.setHealth(1.0);
            player.addPotionEffect(RESISTANCE_EFFECT);
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int) (COOLDOWN_MILLIS / 50), 0, true, false, true));
            cooldowns.put(player, now + COOLDOWN_MILLIS);
            // Play sound and particle effect
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.1);
        });
    }
} 
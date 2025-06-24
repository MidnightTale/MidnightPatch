package fun.mntale.midnightPatch.module.world.enchantment;

import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.NamespacedKey;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

import java.util.Objects;

public class GraceEnchantment implements Listener {
    private static final NamespacedKey GRACE_KEY = NamespacedKey.fromString("midnightpatch:grace");
    private static final PotionEffect SPEED_EFFECT = new PotionEffect(PotionEffectType.SPEED, 40, 0, true, false, true);

    private boolean hasFullGraceSet(Player player) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) return false;
            Enchantment grace = getGraceEnchantment();
            if (grace == null || !armor.containsEnchantment(grace)) return false;
        }
        return true;
    }

    private Enchantment getGraceEnchantment() {
        return RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.ENCHANTMENT)
            .get(Objects.requireNonNull(GRACE_KEY));
    }

    private void updateGraceEffect(Player player) {
        if (hasFullGraceSet(player) && isFullHealth(player)) {
            if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.addPotionEffect(SPEED_EFFECT);
            }
        } else {
            player.removePotionEffect(PotionEffectType.SPEED);
        }
    }

    private boolean isFullHealth(Player player) {
        double max = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
        return player.getHealth() >= max;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        updateGraceEffect(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        updateGraceEffect(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemBreak(PlayerItemBreakEvent event) {
        updateGraceEffect(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        FoliaScheduler.getEntityScheduler().run(
            event.getPlayer(),
            fun.mntale.midnightPatch.MidnightPatch.instance,
            (task) -> updateGraceEffect(event.getPlayer()),
            null
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        event.getPlayer().removePotionEffect(PotionEffectType.SPEED);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            FoliaScheduler.getEntityScheduler().run(
                player,
                fun.mntale.midnightPatch.MidnightPatch.instance,
                (task) -> updateGraceEffect(player),
                null
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeal(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player player) {
            FoliaScheduler.getEntityScheduler().run(
                player,
                fun.mntale.midnightPatch.MidnightPatch.instance,
                (task) -> updateGraceEffect(player),
                null
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMainHandChange(PlayerChangedMainHandEvent event) {
        updateGraceEffect(event.getPlayer());
    }
} 
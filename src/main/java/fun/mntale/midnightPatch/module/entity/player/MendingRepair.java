package fun.mntale.midnightPatch.module.entity.player;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class MendingRepair implements Listener {
    private final Map<UUID, Integer> repairStep = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastInteract = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) || !player.isSneaking()) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasEnchant(Enchantment.MENDING)) {
            return;
        }
        int playerExperience = ExperienceUtil.getPlayerExp(player);
        if (!(meta instanceof Damageable damageableMeta)) {
            return;
        }
        if (damageableMeta.getDamage() == 0) {
            return;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        int step = repairStep.getOrDefault(uuid, 1);
        long last = lastInteract.getOrDefault(uuid, 0L);
        if (now - last < 300) {
            step = Math.min(step * 2, 32);
            lastInteract.put(uuid, now);
            repairStep.put(uuid, step);
        } else {
            step = 1;
            lastInteract.remove(uuid);
            repairStep.remove(uuid);
        }

        int damage = damageableMeta.getDamage();
        int maxRepairable = Math.min(step, Math.min(playerExperience / 2, damage));
        if (maxRepairable > 0) {
            player.giveExp(-2 * maxRepairable);
            damageableMeta.setDamage(damage - maxRepairable);
            item.setItemMeta(meta);
            player.updateInventory();
        }
    }
} 
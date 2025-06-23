package fun.mntale.midnightPatch.module.entity.player;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class LootMobTargetListener implements Listener {
    @EventHandler
    public void onMobTargetPlayer(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (LootMobTargetUtil.isPlayerInLootGUI(player)) {
            event.setCancelled(true);
        }
    }
} 
package fun.mntale.midnightPatch.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.InventoryType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class LootMobTargetManager implements Listener {
    @EventHandler
    public void onMobTargetPlayer(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        // Check if player has a GUI open (not just their inventory)
        if (player.getOpenInventory() != null && player.getOpenInventory().getType() != InventoryType.CRAFTING) {
            Component titleComponent = player.getOpenInventory().title();
            String title = PlainTextComponentSerializer.plainText().serialize(titleComponent);
            if (title.contains("Loot")) {
                event.setCancelled(true);
            }
        }
    }
} 
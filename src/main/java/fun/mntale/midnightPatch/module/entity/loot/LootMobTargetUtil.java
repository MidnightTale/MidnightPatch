package fun.mntale.midnightPatch.module.entity.loot;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class LootMobTargetUtil {
    public static boolean isPlayerInLootGUI(Player player) {
        if (player.getOpenInventory() != null && player.getOpenInventory().getType() != InventoryType.CRAFTING) {
            Component titleComponent = player.getOpenInventory().title();
            String title = PlainTextComponentSerializer.plainText().serialize(titleComponent);
            return title.contains("Loot");
        }
        return false;
    }
} 
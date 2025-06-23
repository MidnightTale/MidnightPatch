package fun.mntale.midnightPatch.module.entity.player;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.loot.Lootable;
import org.bukkit.inventory.InventoryHolder;

public class LootMobTargetUtil {
    public static boolean isPlayerInLootGUI(Player player) {
        if (player.getOpenInventory() != null && player.getOpenInventory().getType() != InventoryType.CRAFTING) {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
            if (holder instanceof Lootable lootable) {
                if (lootable.getLootTable() != null && fun.mntale.midnightPatch.command.ToggleLootChestProtectionCommand.isLootChestProtectionEnabled(player)) {
                    return true;
                }
            }
        }
        return false;
    }
} 
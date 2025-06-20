package fun.mntale.midnightPatch.skin;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

public class SkinManager implements Listener {
    private static final String WRAP_COMMAND_TEMPLATE = "hmcwraps:wraps wrap %s %s";

    public boolean setSkin(CommandSender sender, String tier, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            return false;
        }

        // Access player inventory in entity task
        FoliaScheduler.getEntityScheduler().run(targetPlayer, MidnightPatch.instance, (taskw) -> {
            ItemStack mainHand = targetPlayer.getInventory().getItemInMainHand();
            if (mainHand.getType() == Material.AIR) {
                return;
            }

            String toolType = getToolType(mainHand.getType());
            if (toolType == null) {
                return;
            }

            // Extract the base tool type (e.g., "axe" from "axe_tier1")
            String baseToolType = toolType.split("_")[0];
            
            // Create the wrap command with the correct format: <tool>_<tier> <player>
            String wrapCommand = String.format(WRAP_COMMAND_TEMPLATE, baseToolType + "_" + tier, targetPlayerName);
            
            // Execute the wrap command as console
            FoliaScheduler.getGlobalRegionScheduler().run(MidnightPatch.instance, (taskwc) -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), wrapCommand));
        }, null);
        
        return true;
    }

    private String getToolType(Material material) {
        return switch (material) {
            case WOODEN_AXE, STONE_AXE, IRON_AXE, GOLDEN_AXE, DIAMOND_AXE, NETHERITE_AXE -> "axe_tier1";
            case WOODEN_PICKAXE, STONE_PICKAXE, IRON_PICKAXE, GOLDEN_PICKAXE, DIAMOND_PICKAXE, NETHERITE_PICKAXE -> "pickaxe_tier1";
            case WOODEN_SHOVEL, STONE_SHOVEL, IRON_SHOVEL, GOLDEN_SHOVEL, DIAMOND_SHOVEL, NETHERITE_SHOVEL -> "shovel_tier1";
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, GOLDEN_SWORD, DIAMOND_SWORD, NETHERITE_SWORD -> "sword_tier1";
            default -> null;
        };
    }
} 
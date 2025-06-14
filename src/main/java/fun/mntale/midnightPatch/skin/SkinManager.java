package fun.mntale.midnightPatch.skin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;

public class SkinManager {
    private static final String WRAP_COMMAND_TEMPLATE = "/wraps wrap %s %s";
    private final Plugin plugin;

    public SkinManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean setSkin(CommandSender sender, String tier, String targetPlayerName) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Player " + targetPlayerName + " is not online!", NamedTextColor.RED));
            return false;
        }

        ItemStack mainHand = targetPlayer.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.AIR) {
            sender.sendMessage(Component.text("Target player must be holding a tool in their main hand!", NamedTextColor.RED));
            return false;
        }

        String toolType = getToolType(mainHand.getType());
        if (toolType == null) {
            sender.sendMessage(Component.text("Target player must be holding a valid tool (axe, pickaxe, shovel, or sword)!", NamedTextColor.RED));
            return false;
        }

        // Extract the base tool type (e.g., "axe" from "axe_tier1")
        String baseToolType = toolType.split("_")[0];
        
        // Create the wrap command with the correct tier
        String wrapCommand = String.format(WRAP_COMMAND_TEMPLATE, baseToolType + "_" + tier, tier);
        
        // Execute the wrap command in the global region
        GlobalRegionScheduler scheduler = Bukkit.getGlobalRegionScheduler();
        scheduler.execute(plugin, () -> {
            targetPlayer.performCommand(wrapCommand);
            sender.sendMessage(Component.text("Successfully applied " + tier + " skin to " + targetPlayer.getName() + "'s " + baseToolType + "!", NamedTextColor.GREEN));
        });
        
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
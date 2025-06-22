package fun.mntale.midnightPatch.module.world.fertilizer;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.World;

public class FertilizerUtil {
    public static BlockFace getDispenserFacing(Block dispenserBlock) {
        org.bukkit.block.data.Directional directional = (org.bukkit.block.data.Directional) dispenserBlock.getBlockData();
        return directional.getFacing();
    }

    public static boolean isValidBoneMealUse(PlayerInteractEvent event) {
        return event.getAction() == Action.RIGHT_CLICK_BLOCK &&
               event.getItem() != null &&
               event.getItem().getType() == Material.BONE_MEAL &&
               event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND;
    }

    public static void handleSuccessfulGrowth(Player player, Block block) {
        if (player != null && player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            // This will be handled by the event system
        }
        World world = block.getWorld();
        world.playSound(block.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.0f);
        world.spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5);
    }
} 
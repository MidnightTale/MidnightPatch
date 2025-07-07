package fun.mntale.midnightPatch.module.world.reacharound;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import fun.mntale.midnightPatch.MidnightPatch;

public class ReachAroundBlockPlacer {
    public void placeBlock(Player player, ItemStack item, Location location) {
        if (item == null || !item.getType().isBlock() || location == null) return;
        if (!item.getType().isSolid()) return;
        Block targetBlock = location.getBlock();
        if (!targetBlock.getType().isAir()) return;
        final Block finalTargetBlock = targetBlock;
        final Material finalItemType = item.getType();
        final Player finalPlayer = player;
        MidnightPatch.instance.foliaLib.getScheduler().runAtLocation(targetBlock.getLocation(), (placeBlock) -> {
            finalTargetBlock.setType(finalItemType);
            if (finalItemType.name().contains("STAIRS") || 
                finalItemType.name().contains("SLAB") ||
                finalItemType.name().contains("FENCE") ||
                finalItemType.name().contains("WALL")) {
                setBlockDirection(finalTargetBlock, finalPlayer);
            }
        });
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
        org.bukkit.Sound sound = ReachAroundUtil.getBlockPlaceSound(item.getType());
        player.getWorld().playSound(location, sound, 1.0f, 1.0f);
    }

    private void setBlockDirection(Block block, Player player) {
        final Block finalBlock = block;
        final Player finalPlayer = player;
        MidnightPatch.instance.foliaLib.getScheduler().runAtLocation(block.getLocation(), (setBlockDirection) -> {
            try {
                if (finalBlock.getBlockData() instanceof org.bukkit.block.data.Directional directional) {
                    BlockFace facing = ReachAroundUtil.getPlayerFacingDirection(finalPlayer);
                    directional.setFacing(facing);
                    finalBlock.setBlockData(directional);
                }
            } catch (Exception e) {
                // Ignore if block data can't be set
            }
        });
    }
} 
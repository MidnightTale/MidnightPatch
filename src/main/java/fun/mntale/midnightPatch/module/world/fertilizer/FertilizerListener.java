package fun.mntale.midnightPatch.module.world.fertilizer;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.jspecify.annotations.NullMarked;
import fun.mntale.midnightPatch.MidnightPatch;

@NullMarked
public class FertilizerListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!FertilizerUtil.isValidBoneMealUse(event)) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        FertilizerGrowthType growthType = FertilizerGrowthType.fromMaterial(clickedBlock.getType());
        if (growthType == null) {
            return;
        }
        if (growthType.attemptGrowth(clickedBlock, MidnightPatch.instance)) {
            FertilizerUtil.handleSuccessfulGrowth(event.getPlayer(), clickedBlock);
            // Consume bone meal if not in creative
            if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
                if (event.getItem() != null) {
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                }
            }
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        if (event.getItem().getType() != Material.BONE_MEAL) {
            return;
        }
        Block dispenserBlock = event.getBlock();
        BlockFace facing = FertilizerUtil.getDispenserFacing(dispenserBlock);
        if (facing == null) {
            return;
        }
        Block targetBlock = dispenserBlock.getRelative(facing);
        FertilizerGrowthType growthType = FertilizerGrowthType.fromMaterial(targetBlock.getType());
        if (growthType == null) {
            return;
        }
        if (growthType.attemptGrowth(targetBlock, MidnightPatch.instance)) {
            event.setCancelled(true); // Prevent default dispensing
            // Manually consume one bone meal
            event.getItem().setAmount(event.getItem().getAmount() - 1);
            FertilizerUtil.handleSuccessfulGrowth(null, targetBlock);
        }
    }
} 
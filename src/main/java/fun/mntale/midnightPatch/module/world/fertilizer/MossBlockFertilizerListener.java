package fun.mntale.midnightPatch.module.world.fertilizer;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;

public class MossBlockFertilizerListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null || event.getItem().getType() != Material.BONE_MEAL) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.MOSS_BLOCK) {
            return;
        }
        event.setCancelled(true);
        if (clickedBlock.getRelative(0, 1, 0).getType() != Material.AIR) {
            return;
        }
        MossBlockFertilizerSpread.applyMossSpread(clickedBlock);
        if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
            event.getItem().setAmount(event.getItem().getAmount() - 1);
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        if (!(event.getBlock().getState() instanceof Dispenser dispenser) || event.getItem().getType() != Material.BONE_MEAL) {
            return;
        }
        BlockFace facing = ((org.bukkit.block.data.Directional) dispenser.getBlockData()).getFacing();
        Block targetBlock = event.getBlock().getRelative(facing);
        if (targetBlock.getType() != Material.MOSS_BLOCK) {
            return;
        }
        event.setCancelled(true);
        if (targetBlock.getRelative(0, 1, 0).getType() != Material.AIR) {
            return;
        }
        MossBlockFertilizerSpread.applyMossSpread(targetBlock);
        event.getItem().setAmount(event.getItem().getAmount() - 1);
    }
} 
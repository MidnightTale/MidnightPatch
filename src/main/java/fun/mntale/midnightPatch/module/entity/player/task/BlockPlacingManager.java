package fun.mntale.midnightPatch.module.entity.player.task;

import fun.mntale.midnightPatch.MidnightPatch;
import fun.mntale.midnightPatch.module.entity.player.task.effect.BlockSoundUtil;
import fun.mntale.midnightPatch.module.entity.player.task.packet.ItemInteractUtil;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.folia.TaskWrapper;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BlockPlacingManager {
    private static final Map<Player, TaskWrapper> placeTasks = new HashMap<>();
    private static final Map<Player, Block> lastBlockMap = new HashMap<>();
    private static final Map<Player, BlockFace> lastFaceMap = new HashMap<>();

    public static boolean isPlaceTaskRunning(Player player) {
        return placeTasks.containsKey(player);
    }

    public static void startPlaceTask(Player player, int interval) {
        if (placeTasks.containsKey(player)) return;
        
        TaskWrapper task = FoliaScheduler.getEntityScheduler().runAtFixedRate(player, MidnightPatch.instance, (ignored) -> {
            player.swingMainHand();
            double range = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).getValue();
            var result = player.rayTraceBlocks(range);
            Block targetBlock = null;
            BlockFace targetFace = null;
            
            if (result != null && result.getHitBlock() != null && result.getHitBlockFace() != null) {
                targetBlock = result.getHitBlock();
                targetFace = result.getHitBlockFace();
                lastBlockMap.put(player, targetBlock);
                lastFaceMap.put(player, targetFace);
            } else {
                Block lastBlock = lastBlockMap.get(player);
                BlockFace lastFace = lastFaceMap.get(player);
                if (lastBlock != null && lastFace != null && lastBlock.getType().isSolid()) {
                    targetBlock = lastBlock;
                    targetFace = lastFace;
                } else {
                    Block blockUnder = player.getLocation().subtract(0, 1, 0).getBlock();
                    if (blockUnder.getType().isSolid()) {
                        targetBlock = blockUnder;
                        targetFace = BlockFace.UP;
                    }
                }
            }
            
            if (targetBlock != null && targetFace != null) {
                BlockSoundUtil.playBlockPlaceSound(targetBlock, player);
                ItemInteractUtil.sendNMSUseItemOn(player, targetBlock, targetFace, true);
            } else {
                BlockSoundUtil.playItemUseSound(player);
                ItemInteractUtil.sendNMSUseItem(player, true);
            }
        }, null, 0L, interval);
        
        placeTasks.put(player, task);
    }

    public static void stopPlaceTask(Player player) {
        TaskWrapper task = placeTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
        lastBlockMap.remove(player);
        lastFaceMap.remove(player);
    }
} 
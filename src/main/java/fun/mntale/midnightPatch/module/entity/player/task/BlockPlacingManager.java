package fun.mntale.midnightPatch.module.entity.player.task;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import fun.mntale.midnightPatch.MidnightPatch;
import fun.mntale.midnightPatch.module.entity.player.task.effect.BlockSoundUtil;
import fun.mntale.midnightPatch.module.entity.player.task.packet.ItemInteractUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class BlockPlacingManager {
    private static final Map<Player, WrappedTask> placeTasks = new ConcurrentHashMap<>();
    private static final Map<Player, Block> lastBlockMap = new ConcurrentHashMap<>();
    private static final Map<Player, BlockFace> lastFaceMap = new ConcurrentHashMap<>();

    public static boolean isPlaceTaskRunning(Player player) {
        return placeTasks.containsKey(player);
    }

    public static void startPlaceTask(Player player, int interval) {
        if (placeTasks.containsKey(player)) return;
        
        WrappedTask task = MidnightPatch.instance.foliaLib.getScheduler().runAtEntityTimer(player, () -> {
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
        WrappedTask task = placeTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
        lastBlockMap.remove(player);
        lastFaceMap.remove(player);
    }
} 
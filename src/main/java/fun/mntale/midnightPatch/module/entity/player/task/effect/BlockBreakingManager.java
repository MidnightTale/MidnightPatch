package fun.mntale.midnightPatch.module.entity.player.task.effect;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import fun.mntale.midnightPatch.MidnightPatch;
import fun.mntale.midnightPatch.module.entity.player.task.tool.BlockTargetingUtil;
import fun.mntale.midnightPatch.module.entity.player.task.tool.BlockValidationUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BlockBreakingManager {
    private static final Map<Player, WrappedTask> breakTasks = new ConcurrentHashMap<>();
    private static final Map<Player, Map<Block, WrappedTask>> activeBreakingTasks = new ConcurrentHashMap<>();
    private static final Logger logger = MidnightPatch.instance.getLogger();

    public static boolean isBreakTaskRunning(Player player) {
        return breakTasks.containsKey(player);
    }

    public static void startBreakTask(Player player, int interval) {
        if (breakTasks.containsKey(player)) return;
        
        Map<Block, WrappedTask> playerActiveTasks = new ConcurrentHashMap<>();
        activeBreakingTasks.put(player, playerActiveTasks);
        
        WrappedTask outerTask = MidnightPatch.instance.foliaLib.getScheduler().runAtEntityTimer(player, () -> {
            double range = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).getValue();
            Block targetBlock = null;
            var result = player.rayTraceBlocks(range);
            if (result != null && result.getHitBlock() != null) {
                targetBlock = result.getHitBlock();
            }
            
            if (targetBlock == null || !BlockValidationUtil.isBreakableBlock(targetBlock)) {
                return;
            }
            
            if (playerActiveTasks.containsKey(targetBlock)) {
                return;
            }
            
            int breakTime = BlockBreakingUtil.estimateBreakTime(player, targetBlock);
            startBlockBreakingAnimation(player, targetBlock, breakTime, playerActiveTasks);
            
        }, null, 0L, interval);
        
        breakTasks.put(player, outerTask);
    }

    public static void stopBreakTask(Player player) {
        WrappedTask task = breakTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
        
        Map<Block, WrappedTask> playerTasks = activeBreakingTasks.remove(player);
        if (playerTasks != null) {
            for (WrappedTask WrappedTask : playerTasks.values()) {
                WrappedTask.cancel();
            }
        }
        
        try {
            Block blockUnder = player.getLocation().subtract(0, 1, 0).getBlock();
            if (blockUnder != null && blockUnder.getWorld() != null) {
                BlockAnimationUtil.sendBlockCrackAnimation(blockUnder, player.getEntityId(), -1);
            }
            
            double range = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).getValue();
            var result = player.rayTraceBlocks(range);
            if (result != null && result.getHitBlock() != null) {
                BlockAnimationUtil.sendBlockCrackAnimation(result.getHitBlock(), player.getEntityId(), -1);
            }
        } catch (Exception e) {
            logger.warning("Error stopping break task: " + e.getMessage());
        }
    }

    private static void startBlockBreakingAnimation(Player player, Block block, int breakTime, Map<Block, WrappedTask> activeTasks) {
        final int entityId = player.getEntityId();
        final net.minecraft.core.BlockPos blockPos = ((org.bukkit.craftbukkit.block.CraftBlock) block).getPosition();
        final int delay = Math.max(1, breakTime / 10);
        
        final int[] progress = {0};
        final Block targetBlock = block;
        final WrappedTask[] taskRef = new WrappedTask[1];
        
        taskRef[0] = MidnightPatch.instance.foliaLib.getScheduler().runAtLocationTimer(
            player.getLocation(),
            () -> {
                Block currentTargetBlock = BlockTargetingUtil.getPlayerTargetBlock(player);
                if (currentTargetBlock == null || !currentTargetBlock.getLocation().equals(targetBlock.getLocation())) {
                    BlockAnimationUtil.sendBlockDestructionPacket(entityId, blockPos, -1);
                    activeTasks.remove(targetBlock);
                    taskRef[0].cancel();
                    return;
                }
                
                if (targetBlock.getType().isAir() || !BlockValidationUtil.isBreakableBlock(targetBlock)) {
                    BlockAnimationUtil.sendBlockDestructionPacket(entityId, blockPos, -1);
                    activeTasks.remove(targetBlock);
                    taskRef[0].cancel();
                    return;
                }
                
                player.swingMainHand();
                BlockParticleUtil.spawnBlockBreakingParticles(targetBlock, player);
                BlockSoundUtil.playBreakingProgressSound(targetBlock, player);
                BlockAnimationUtil.sendBlockDestructionPacket(entityId, blockPos, progress[0]);
                
                if (progress[0] >= 10) {
                    BlockAnimationUtil.sendBlockDestructionPacket(entityId, blockPos, -1);
                    BlockSoundUtil.playBlockBreakSound(targetBlock, player);
                    BlockParticleUtil.spawnBlockBreakParticles(targetBlock, player);
                    targetBlock.breakNaturally(player.getInventory().getItemInMainHand());
                    activeTasks.remove(targetBlock);
                    taskRef[0].cancel();
                    return;
                }
                
                progress[0]++;
            }, 
            0L, 
            delay
        );
        
        activeTasks.put(block, taskRef[0]);
    }
} 
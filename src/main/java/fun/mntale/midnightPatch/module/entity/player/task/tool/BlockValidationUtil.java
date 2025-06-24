package fun.mntale.midnightPatch.module.entity.player.task.tool;

import org.bukkit.block.Block;

public class BlockValidationUtil {
    
    public static boolean isBreakableBlock(Block block) {
        if (block == null || block.getType().isAir()) {
            return false;
        }
        
        switch (block.getType()) {
            case BEDROCK:
            case COMMAND_BLOCK:
            case CHAIN_COMMAND_BLOCK:
            case REPEATING_COMMAND_BLOCK:
            case STRUCTURE_BLOCK:
            case STRUCTURE_VOID:
            case BARRIER:
            case LIGHT:
                return false;
            default:
                return block.getType().getHardness() >= 0;
        }
    }
} 
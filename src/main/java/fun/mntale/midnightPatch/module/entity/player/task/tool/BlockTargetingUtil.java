package fun.mntale.midnightPatch.module.entity.player.task.tool;

import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockTargetingUtil {
    
    public static Block getPlayerTargetBlock(Player player) {
        double range = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).getValue();
        var result = player.rayTraceBlocks(range);
        if (result != null && result.getHitBlock() != null) {
            return result.getHitBlock();
        } else {
            Block blockUnder = player.getLocation().subtract(0, 1, 0).getBlock();
            if (blockUnder.getType().isSolid() && !blockUnder.getType().isAir()) {
                return blockUnder;
            }
        }
        return null;
    }
} 
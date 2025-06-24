package fun.mntale.midnightPatch.module.entity.player.task.effect;

import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class BlockParticleUtil {
    
    public static void spawnBlockBreakingParticles(Block block, Player player) {
        try {
            org.bukkit.Particle particleType = getBlockParticle(block.getType());
            BlockFace hitFace = getHitFace(player, block);
            if (hitFace == null) return;
            
            org.bukkit.Location particleLoc = getParticleLocation(block, hitFace);
            
            block.getWorld().spawnParticle(
                particleType,
                particleLoc,
                3,
                0.1, 0.1, 0.1,
                0.01,
                block.getBlockData()
            );
            
        } catch (Exception e) {
        }
    }
    
    public static void spawnBlockBreakParticles(Block block, Player player) {
        try {
            org.bukkit.Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
            
            block.getWorld().spawnParticle(
                org.bukkit.Particle.BLOCK,
                blockCenter,
                15,
                0.3, 0.3, 0.3,
                0.1,
                block.getBlockData()
            );
            
        } catch (Exception e) {
        }
    }
    
    private static org.bukkit.Particle getBlockParticle(org.bukkit.Material material) {
        switch (material) {
            case STONE:
            case COBBLESTONE:
            case DEEPSLATE:
            case ANDESITE:
            case DIORITE:
            case GRANITE:
                return org.bukkit.Particle.BLOCK;
            case DIRT:
            case GRASS_BLOCK:
            case SAND:
            case GRAVEL:
            case CLAY:
                return org.bukkit.Particle.BLOCK;
            case OAK_LOG:
            case BIRCH_LOG:
            case SPRUCE_LOG:
            case JUNGLE_LOG:
            case ACACIA_LOG:
            case DARK_OAK_LOG:
            case MANGROVE_LOG:
            case CHERRY_LOG:
            case CRIMSON_STEM:
            case WARPED_STEM:
                return org.bukkit.Particle.BLOCK;
            case IRON_ORE:
            case GOLD_ORE:
            case COAL_ORE:
            case DIAMOND_ORE:
            case EMERALD_ORE:
            case REDSTONE_ORE:
            case LAPIS_ORE:
            case NETHERITE_BLOCK:
            case ANCIENT_DEBRIS:
                return org.bukkit.Particle.BLOCK;
            default:
                return org.bukkit.Particle.BLOCK;
        }
    }
    
    private static BlockFace getHitFace(Player player, Block block) {
        double range = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).getValue();
        var result = player.rayTraceBlocks(range);
        if (result != null && result.getHitBlockFace() != null) {
            return result.getHitBlockFace();
        }
        return BlockFace.UP;
    }
    
    private static org.bukkit.Location getParticleLocation(Block block, BlockFace face) {
        org.bukkit.Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
        
        switch (face) {
            case UP:
                return blockCenter.add(0, 0.5, 0);
            case DOWN:
                return blockCenter.add(0, -0.5, 0);
            case NORTH:
                return blockCenter.add(0, 0, -0.5);
            case SOUTH:
                return blockCenter.add(0, 0, 0.5);
            case EAST:
                return blockCenter.add(0.5, 0, 0);
            case WEST:
                return blockCenter.add(-0.5, 0, 0);
            default:
                return blockCenter;
        }
    }
} 
package fun.mntale.midnightPatch.module.entity.player.task.effect;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockSoundUtil {
    
    public static void playBreakingProgressSound(Block block, Player player) {
        try {
            org.bukkit.Sound sound = getBlockBreakingSound(block.getType());
            block.getWorld().playSound(block.getLocation(), sound, org.bukkit.SoundCategory.BLOCKS, 0.5f, 1.0f);
        } catch (Exception e) {}
    }

    public static void playBlockBreakSound(Block block, Player player) {
        try {
            org.bukkit.Sound sound = getBlockBreakSound(block.getType());
            block.getWorld().playSound(block.getLocation(), sound, org.bukkit.SoundCategory.BLOCKS, 1.0f, 1.0f);
        } catch (Exception e) {}
    }
    
    public static void playBlockPlaceSound(Block block, Player player) {
        try {
            org.bukkit.Sound sound = getBlockPlaceSound(block.getType());
            block.getWorld().playSound(block.getLocation(), sound, org.bukkit.SoundCategory.BLOCKS, 0.5f, 1.0f);
        } catch (Exception e) {}
    }

    public static void playItemUseSound(Player player) {
        try {
            org.bukkit.Sound sound = getItemUseSound(player.getInventory().getItemInMainHand().getType());
            player.getWorld().playSound(player.getLocation(), sound, org.bukkit.SoundCategory.BLOCKS, 0.5f, 1.0f);
        } catch (Exception e) {}
    }
    
    private static org.bukkit.Sound getBlockBreakingSound(org.bukkit.Material material) {
        switch (material) {
            case STONE, COBBLESTONE, DEEPSLATE, ANDESITE, DIORITE, GRANITE:
            case IRON_ORE, GOLD_ORE, COAL_ORE, DIAMOND_ORE, EMERALD_ORE, REDSTONE_ORE, LAPIS_ORE:
            case NETHERITE_BLOCK, ANCIENT_DEBRIS:
                return org.bukkit.Sound.BLOCK_STONE_BREAK;
            case DIRT, GRASS_BLOCK, SAND, GRAVEL, CLAY:
                return org.bukkit.Sound.BLOCK_GRAVEL_BREAK;
            case OAK_LOG, BIRCH_LOG, SPRUCE_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG:
            case MANGROVE_LOG, CHERRY_LOG, CRIMSON_STEM, WARPED_STEM:
                return org.bukkit.Sound.BLOCK_WOOD_BREAK;
            case GLASS, WHITE_STAINED_GLASS, ORANGE_STAINED_GLASS, MAGENTA_STAINED_GLASS:
            case LIGHT_BLUE_STAINED_GLASS, YELLOW_STAINED_GLASS, LIME_STAINED_GLASS, PINK_STAINED_GLASS:
            case GRAY_STAINED_GLASS, LIGHT_GRAY_STAINED_GLASS, CYAN_STAINED_GLASS, PURPLE_STAINED_GLASS:
            case BLUE_STAINED_GLASS, BROWN_STAINED_GLASS, GREEN_STAINED_GLASS, RED_STAINED_GLASS, BLACK_STAINED_GLASS:
                return org.bukkit.Sound.BLOCK_GLASS_BREAK;
            default:
                return org.bukkit.Sound.BLOCK_STONE_BREAK;
        }
    }
    
    private static org.bukkit.Sound getBlockBreakSound(org.bukkit.Material material) {
        return getBlockBreakingSound(material);
    }

    private static org.bukkit.Sound getBlockPlaceSound(org.bukkit.Material material) {
        switch (material) {
            case STONE, COBBLESTONE, DEEPSLATE, ANDESITE, DIORITE, GRANITE:
            case IRON_ORE, GOLD_ORE, COAL_ORE, DIAMOND_ORE, EMERALD_ORE, REDSTONE_ORE, LAPIS_ORE:
            case NETHERITE_BLOCK, ANCIENT_DEBRIS:
                return org.bukkit.Sound.BLOCK_STONE_PLACE;
            case DIRT, GRASS_BLOCK, SAND, GRAVEL, CLAY:
                return org.bukkit.Sound.BLOCK_GRAVEL_PLACE;
            case OAK_LOG, BIRCH_LOG, SPRUCE_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG:
            case MANGROVE_LOG, CHERRY_LOG, CRIMSON_STEM, WARPED_STEM:
                return org.bukkit.Sound.BLOCK_WOOD_PLACE;
            case GLASS, WHITE_STAINED_GLASS, ORANGE_STAINED_GLASS, MAGENTA_STAINED_GLASS:
            case LIGHT_BLUE_STAINED_GLASS, YELLOW_STAINED_GLASS, LIME_STAINED_GLASS, PINK_STAINED_GLASS:
            case GRAY_STAINED_GLASS, LIGHT_GRAY_STAINED_GLASS, CYAN_STAINED_GLASS, PURPLE_STAINED_GLASS:
            case BLUE_STAINED_GLASS, BROWN_STAINED_GLASS, GREEN_STAINED_GLASS, RED_STAINED_GLASS, BLACK_STAINED_GLASS:
                return org.bukkit.Sound.BLOCK_GLASS_PLACE;
            default:
                return org.bukkit.Sound.BLOCK_STONE_PLACE;
        }
    }

    private static org.bukkit.Sound getItemUseSound(org.bukkit.Material material) {
        return getBlockBreakingSound(material);
    }
} 